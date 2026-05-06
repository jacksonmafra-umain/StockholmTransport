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

function runSeed() {
    return new Promise((resolve, reject) => {
        const proc = spawn(
            'node',
            ['scripts/seed-from-trafiklab.js', '--data', './data', '--schema', './openapi.json'],
            { stdio: 'inherit' },
        );
        proc.on('exit', (code) => {
            if (code === 0) resolve();
            else reject(new Error(`seed-from-trafiklab.js exited ${code}`));
        });
        proc.on('error', reject);
    });
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
