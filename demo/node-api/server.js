import express from 'express';
// Use 'import * as kmp from ...' to import the entire module namespace.
import * as kmp from 'stockholm-transport';

// --- 1. Get a handle to our public JS API and initialize it ---
// Now, our exported object is correctly located at kmp.StockholmTransportApi
const transportApi = kmp.StockholmTransportApi;
transportApi.initialize();
console.log("✅ KMP Library Initialized successfully.");


// --- 2. Setup Express Server ---
const app = express();
const port = 3000;

// Helper data structure to map URL routes to our library's ViewModels.
const modules = [
    { id: 'lines', title: 'Transport Lines', getViewModel: transportApi.getLinesViewModel },
    { id: 'sites', title: 'Sites / Stations', getViewModel: transportApi.getSitesViewModel },
    { id: 'departures', title: 'Departures', getViewModel: transportApi.getDeparturesViewModel },
    { id: 'stoppoints', title: 'Stop Points', getViewModel: transportApi.getStopPointsViewModel },
    { id: 'authorities', title: 'Transport Authorities', getViewModel: transportApi.getAuthoritiesViewModel },
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

// --- 4. START THE SERVER ---
app.listen(port, () => {
    console.log(`🚀 Demo API server listening on http://localhost:${port}`);
    console.log('Try visiting:');
    console.log(`  http://localhost:${port}/modules`);
    console.log(`  http://localhost:${port}/modules/lines`);
});
