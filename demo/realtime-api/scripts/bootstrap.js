#!/usr/bin/env node
//
// Idempotent first-boot seeder. Runs once before the server, no-ops thereafter.
//
// Connects to Mongo, counts the foundational Stop collection, and if it's
// empty hands off to seed-from-trafiklab.js (which reads ./openapi.json and
// the JSON snapshots in ./data and populates Stop / Line / Timetable / Vehicle).
//
// Usage:
//   node scripts/bootstrap.js          # one-shot
//   node scripts/bootstrap.js && npm start    # chained from docker-compose
//
// Re-seed manually after wiping: docker compose down -v && docker compose up.

import { spawn } from 'node:child_process';
import { connectMongo, disconnectMongo } from '../config/mongo.js';
import { Stop } from '../domain/models/Stop.js';

const TAG = '[bootstrap]';

async function isEmpty() {
    await connectMongo();
    try {
        return (await Stop.countDocuments()) === 0;
    } finally {
        await disconnectMongo();
    }
}

function runScript(args) {
    return new Promise((resolve, reject) => {
        const proc = spawn('node', args, { stdio: 'inherit' });
        proc.on('exit', (code) => {
            if (code === 0) resolve();
            else reject(new Error(`${args[0]} exited ${code}`));
        });
        proc.on('error', reject);
    });
}

async function runSeed() {
    // Pass 1: Trafiklab snapshots → Stop / Line / Timetable / Vehicle.
    //         Lines come out without per-line `stops` ordering (seed
    //         doesn't know route topology).
    await runScript([
        'scripts/seed-from-trafiklab.js',
        '--data', './data',
        '--schema', './openapi.json',
    ]);
    // Pass 2: inline route definitions → fills Line.stops with the ordered
    //         station list per line code so SimulationEngine.startTrip can
    //         actually advance through stops.
    await runScript(['scripts/seed-routes-to-lines.js']);
}

async function main() {
    if (await isEmpty()) {
        console.log(`${TAG} 🌱 Mongo is empty — running first-time seed…`);
        await runSeed();
        console.log(`${TAG} ✅ Seed done.`);
    } else {
        console.log(`${TAG} ✓ Mongo already seeded — skipping.`);
    }
}

main().catch((err) => {
    console.error(`${TAG} ✘`, err.message ?? err);
    process.exit(1);
});
