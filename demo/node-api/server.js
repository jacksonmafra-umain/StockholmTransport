import express from 'express';
// Use 'import * as kmp from ...' to import the entire module namespace.
import * as kmp from '@jacksonmafra-umain/stockholm-transport';

// The library is published with the public ngrok URL baked into BuildConfig (so
// phones can reach it). Server-side we don't need the tunnel — and it's often
// offline or stale between `./sl start` sessions — so reroute the library's
// static-SDK calls to this server's own /v1 proxy on :3000. This keeps the
// /modules/* demo working regardless of ngrok state (which dead URL is baked
// no longer matters). Mobile clients still use the real tunnel.
const _nativeFetch = globalThis.fetch;
const rewriteNgrok = (u) => u.replace(/^https?:\/\/[a-z0-9-]+\.ngrok(-free)?\.(app|io)/i, 'http://localhost:3000');
globalThis.fetch = (input, init) => {
    if (typeof input === 'string') return _nativeFetch(rewriteNgrok(input), init);
    if (input instanceof URL) return _nativeFetch(rewriteNgrok(input.href), init);
    if (input && typeof input === 'object' && 'url' in input) {
        return _nativeFetch(new Request(rewriteNgrok(input.url), input), init);
    }
    return _nativeFetch(input, init);
};

// --- 1. Get a handle to our public JS API and initialize it ---
// Kotlin/JS exports `object` declarations as a class with a static
// getInstance() — call it once to unwrap the singleton, then drive it
// like any normal JS object. Use `initializeWithRealtime` so the realtime
// module points at the docker-compose simulator (mongo + ws) running on
// :3001 — without it, /modules/active-trips can only return errors.
const transportApi = kmp.StockholmTransportApi.getInstance();
// Defaults assume `npm start` from a host shell — the realtime-api Docker
// service maps 3001 → 3000, so localhost:3001 is the right host-side URL.
// Inside docker compose the node-api container should set
// REALTIME_HTTP_URL=http://realtime-api:3000 (the in-network port). See
// docker-compose.yml for the wiring.
const REALTIME_HTTP = process.env.REALTIME_HTTP_URL ?? 'http://localhost:3001';
const REALTIME_HOST = process.env.REALTIME_WS_HOST ?? 'localhost';
const REALTIME_PORT = Number(process.env.REALTIME_WS_PORT ?? 3001);
const REALTIME_SECURE = (process.env.REALTIME_WS_SECURE ?? 'false') === 'true';
transportApi.initializeWithRealtime(REALTIME_HTTP, REALTIME_HOST, REALTIME_PORT, REALTIME_SECURE);
console.log("✅ KMP Library Initialized successfully.");
console.log(`   realtime ▶ ${REALTIME_HTTP}  (ws ${REALTIME_HOST}:${REALTIME_PORT}, secure=${REALTIME_SECURE})`);


// --- 2. Setup Express Server ---
const app = express();
const port = 3000;

// CORS — the browser SPA (demo/spa-bootstrap, http://localhost:5173) talks to
// the same /v1 passthrough the phones use, but cross-origin. Mobile and the
// Node demo never trip CORS; a browser does. Allow any origin and short-circuit
// the preflight so the KMP HttpClient's requests aren't blocked. (The mobile
// apps and Node ignore these headers — only the browser needs them.)
app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', '*');
    if (req.method === 'OPTIONS') {
        return res.sendStatus(204);
    }
    next();
});

// Helper data structure to map URL routes to our library's ViewModels.
// Each getViewModel is wrapped in an arrow so the singleton `this` is
// preserved — capturing `transportApi.getLinesViewModel` as a bare
// property reference loses the binding and the generated Kotlin code
// can no longer reach its private Koin field.
//
// `load` names the side-effect kick that drives the ViewModel through its
// loading → success/error transition. We resolve `subscribe()` once
// `state.isLoading === false` and tear the ViewModel down with
// `onCleared()`. Same flow as the React `useStockholmTransport` hook —
// the talk reuses this pattern verbatim.
const modules = [
    { id: 'lines',         title: 'Transport Lines',       getViewModel: () => transportApi.getLinesViewModel(),         load: (vm) => vm.loadLines() },
    { id: 'sites',         title: 'Sites / Stations',      getViewModel: () => transportApi.getSitesViewModel(),         load: (vm) => vm.loadSites() },
    { id: 'departures',    title: 'Departures',            getViewModel: () => transportApi.getDeparturesViewModel(),    load: (vm) => vm.loadDepartures(9192) /* Slussen */ },
    { id: 'stoppoints',    title: 'Stop Points',           getViewModel: () => transportApi.getStopPointsViewModel(),    load: (vm) => vm.loadStopPoints() },
    { id: 'authorities',   title: 'Transport Authorities', getViewModel: () => transportApi.getAuthoritiesViewModel(),   load: (vm) => vm.loadAuthorities() },
    // Realtime is the same SDK contract — just sourced from the library's
    // realtime feature module after Option C. The ViewModel hits the
    // simulator's /api/trips/active endpoint via Koin-provided HttpClient
    // + RealtimeConfig, no ws connection needed for this snapshot call.
    { id: 'active-trips',  title: 'Active Trips',          getViewModel: () => transportApi.getTripSelectionViewModel(), load: (vm) => vm.loadActiveTrips() },
];


// --- 3. Define API Endpoints ---
app.get('/modules', (req, res) => {
    res.json(modules.map(m => ({ id: m.id, title: m.title })));
});

app.get('/modules/:moduleId', async (req, res) => {
    const { moduleId } = req.params;
    const moduleInfo = modules.find(m => m.id === moduleId);

    if (!moduleInfo) {
        return res.status(404).json({ error: 'Module not found' });
    }

    console.log(`Fetching data for module: ${moduleId}`);

    try {
        const viewModel = moduleInfo.getViewModel();

        const result = await new Promise(resolve => {
            viewModel.subscribe(state => {
                if (!state.isLoading) {
                    resolve(state);
                }
            });
            moduleInfo.load(viewModel);
        });

        viewModel.onCleared();

        if (result.error) {
            res.status(500).json({ error: result.error });
        } else {
            res.json(result);
        }
    } catch (e) {
        console.error(`Error processing module ${moduleId}:`, e);
        res.status(500).json({ error: 'An internal server error occurred' });
    }
});

// --- 4. SL PROXY — forwards /v1/* to the upstream SL Transport API ---
// When the sl-cli writes the ngrok URL into gradle.properties as
// serverHostURL="${ngrokUrl}/v1", the rebuilt mobile/web apps issue
// requests like ${ngrokUrl}/v1/lines?... — this middleware mirrors
// those onto the real https://transport.integration.sl.se/v1/* so a
// single ngrok tunnel becomes the network endpoint for every client.
const SL_UPSTREAM = 'https://transport.integration.sl.se';

app.use('/v1', async (req, res, next) => {
    const upstreamUrl = SL_UPSTREAM + req.originalUrl;
    try {
        const headers = { ...req.headers };
        delete headers.host;
        delete headers['content-length'];
        delete headers['accept-encoding'];

        const upstreamRes = await fetch(upstreamUrl, {
            method: req.method,
            headers,
            body: ['GET', 'HEAD'].includes(req.method) ? undefined : req,
            duplex: 'half',
        });

        const body = Buffer.from(await upstreamRes.arrayBuffer());
        res.status(upstreamRes.status);
        upstreamRes.headers.forEach((value, key) => {
            const lower = key.toLowerCase();
            if (lower !== 'content-encoding' && lower !== 'transfer-encoding' && lower !== 'content-length') {
                res.setHeader(key, value);
            }
        });
        res.send(body);
    } catch (err) {
        next(err);
    }
});

// --- 5. START THE SERVER ---
app.listen(port, () => {
    console.log(`🚀 Demo API server listening on http://localhost:${port}`);
    console.log('Try visiting:');
    console.log(`  http://localhost:${port}/modules`);
    console.log(`  http://localhost:${port}/modules/lines`);
    console.log(`  http://localhost:${port}/modules/active-trips`);
    console.log(`  http://localhost:${port}/v1/lines?transport_authority_id=1   (SL passthrough)`);
});
