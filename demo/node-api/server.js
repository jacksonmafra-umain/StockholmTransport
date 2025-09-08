import express from 'express';

/*
import * as kmp from 'stockholm-transport';
OR
import kmp from 'stockholm-transport';
// OR
import { com, org } from 'stockholm-transport';
*/
import * as kmp from 'stockholm-transport';

console.log(Object.keys(kmp));

/*
// --- 1. Initialize the KMP Library ---
// The initKoin function is the entry point to our library's dependency injection.
kmp.com.umain.transport.di.initKoin();

// Get the Koin instance. This is our service locator.
const koin = kmp.org.koin.core.context.GlobalContext.INSTANCE.get();

// --- 2. Setup Express Server ---
const app = express();
const port = 3000;

// A simple data structure to represent our library's modules
const modules = [
    { id: 'lines', title: 'Transport Lines' },
    { id: 'sites', title: 'Sites / Stations' },
    { id: 'departures', title: 'Departures' },
    { id: 'stoppoints', title: 'Stop Points' },
    { id: 'authorities', title: 'Transport Authorities' },
];

// --- 3. Define API Endpoints ---
// Endpoint to list all available modules
app.get('/modules', (req, res) => {
    console.log('Request received for /modules');
    res.json(modules);
});

// We will implement the other endpoints in the next step.

app.listen(port, () => {
    console.log(`Demo API server listening on http://localhost:${port}`);
    console.log('Available endpoints:');
    console.log(`  GET /modules`);
});
 */