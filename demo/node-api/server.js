import express from 'express';
import kmp from 'StockholmTransport-stockholm-transport';

// --- 1. Initialize the KMP Library ---
// This function sets up the entire dependency graph.
kmp.com.umain.transport.di.initKoinForJs();

// --- 2. Get a handle to our public JS API ---
const transportApi = kmp.com.umain.transport.js.StockholmTransportApi;

// --- 3. Setup Express Server ---
const app = express();
const port = 3000;

const modules = [
    { id: 'lines', title: 'Transport Lines', getViewModel: transportApi.getLinesViewModel },
    { id: 'sites', title: 'Sites / Stations', getViewModel: transportApi.getSitesViewModel },
    { id: 'departures', title: 'Departures', getViewModel: transportApi.getDeparturesViewModel },
    { id: 'stoppoints', title: 'Stop Points', getViewModel: transportApi.getStopPointsViewModel },
    { id: 'authorities', title: 'Transport Authorities', getViewModel: transportApi.getAuthoritiesViewModel },
];

// --- 4. Define API Endpoints ---

app.get('/modules', (req, res) => {
    // We only return the id and title, not the function.
    res.json(modules.map(m => ({ id: m.id, title: m.title })));
});

app.get('/modules/:moduleId', async (req, res) => {
    const { moduleId } = req.params;
    const moduleInfo = modules.find(m => m.id === moduleId);

    if (!moduleInfo) {
        return res.status(404).json({ error: 'Module not found' });
    }

    try {
        // Get a fresh ViewModel instance using our JS API function
        const viewModel = moduleInfo.getViewModel();

        const result = await new Promise(resolve => {
            viewModel.subscribe(state => {
                if (!state.isLoading) {
                    resolve(state);
                }
            });

            // A generic way to call the 'load' function
            const loadFunctionName = `load${moduleId.charAt(0).toUpperCase() + moduleId.slice(1)}`;
            if (viewModel[loadFunctionName]) {
                // Special case for departures which needs an ID
                if (moduleId === 'departures') {
                    viewModel[loadFunctionName](9192); // Demo siteId
                } else {
                    viewModel[loadFunctionName]();
                }
            } else {
                // This case should not happen if ViewModels are consistent
                resolve({ error: `Load function ${loadFunctionName} not found on ViewModel.` });
            }
        });

        // Clean up the ViewModel's coroutine scope
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

// --- 5. Start the Server ---
app.listen(port, () => {
    console.log(`Demo API server listening on http://localhost:${port}`);
});