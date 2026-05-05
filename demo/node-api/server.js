import express from 'express';
// Use 'import * as kmp from ...' to import the entire module namespace.
import * as kmp from 'StockholmTransport-stockholm-transport';

// --- 1. Get a handle to our public JS API and initialize it ---
// Kotlin/JS exports `object` declarations as a class with a static
// getInstance() — call it once to unwrap the singleton, then drive it
// like any normal JS object.
const transportApi = kmp.StockholmTransportApi.getInstance();
transportApi.initialize();
console.log("✅ KMP Library Initialized successfully.");


// --- 2. Setup Express Server ---
const app = express();
const port = 3000;

// Helper data structure to map URL routes to our library's ViewModels.
// Each getViewModel is wrapped in an arrow so the singleton `this` is
// preserved — capturing `transportApi.getLinesViewModel` as a bare
// property reference loses the binding and the generated Kotlin code
// can no longer reach its private Koin field.
const modules = [
    { id: 'lines', title: 'Transport Lines', getViewModel: () => transportApi.getLinesViewModel() },
    { id: 'sites', title: 'Sites / Stations', getViewModel: () => transportApi.getSitesViewModel() },
    { id: 'departures', title: 'Departures', getViewModel: () => transportApi.getDeparturesViewModel() },
    { id: 'stoppoints', title: 'Stop Points', getViewModel: () => transportApi.getStopPointsViewModel() },
    { id: 'authorities', title: 'Transport Authorities', getViewModel: () => transportApi.getAuthoritiesViewModel() },
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

            const loadFunctionName = `load${moduleId.charAt(0).toUpperCase() + moduleId.slice(1)}`;
            if (viewModel[loadFunctionName]) {
                if (moduleId === 'departures') {
                    viewModel[loadFunctionName](9192); // Demo siteId for Slussen
                } else {
                    viewModel[loadFunctionName]();
                }
            } else {
                resolve({ error: `Load function ${loadFunctionName} not found on ViewModel.` });
            }
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
    console.log(`  http://localhost:${port}/v1/lines?transport_authority_id=1   (SL passthrough)`);
});
