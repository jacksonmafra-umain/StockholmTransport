#!/usr/bin/env node
import { createInterface } from 'node:readline';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { Services } from '../lib/services.js';
import { publishAll } from '../lib/publish.js';
import { resolveAndroidHome, resolveJavaHome } from '../lib/env.js';
import {
    dockerDown,
    dockerLogs,
    dockerPs,
    dockerSeed,
    dockerUp,
    printPsTable,
} from '../lib/docker.js';
import { banner, c, dim, fail, info, ok, PROMPT, tag, warn } from '../lib/ui.js';

const REPO_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const services = new Services(REPO_ROOT);

const COMMANDS = {
    // ----- ngrok / nodemon dev loop -----
    start: {
        help: 'Boot Node API + ngrok tunnel, write the public URL into gradle.properties.',
        run: cmdStart,
    },
    stop: {
        help: 'Stop the Node API + ngrok and restore the original gradle.properties.',
        run: cmdStop,
    },

    // ----- gradle publish -----
    publish: {
        help: 'Build + publish library: mavenLocal, JS bundle, iOS XCFramework. Use `publish --no-ios` to skip Apple builds.',
        run: cmdPublish,
    },

    // ----- docker compose stack -----
    up: {
        help: 'Start the Docker stack in background (mongo + node-api + realtime-api). `up --no-build` skips the rebuild.',
        run: cmdUp,
    },
    down: {
        help: 'Stop the Docker stack. `down --volumes` (or `down -v`) also wipes the mongo_data volume.',
        run: cmdDown,
    },
    ps: {
        help: 'Show Docker compose container states (service / state / ports).',
        run: cmdPs,
    },
    logs: {
        help: 'Tail logs from a compose service. Usage: `logs <service> [--follow] [--tail N]`. Default tail=100.',
        run: cmdLogs,
    },

    // ----- realtime data -----
    load: {
        help: 'Bring the Docker stack up and let bootstrap auto-seed Mongo if empty (idempotent).',
        run: cmdLoad,
    },
    seed: {
        help: 'Re-run the realtime-api seed scripts inside the running container (Trafiklab + routes-to-lines, upserts).',
        run: cmdSeed,
    },
    clear: {
        help: 'Wipe the realtime simulator data: stops the stack and drops the mongo_data volume. Requires `--yes`.',
        run: cmdClear,
    },

    // ----- introspection / lifecycle -----
    status: {
        help: 'Show running services, Docker stack, gradle.properties URL, detected toolchain.',
        run: cmdStatus,
    },
    help: {
        help: 'Show this help.',
        run: () => { showHelp(); return 0; },
    },
    exit: {
        help: 'Stop services and quit.',
        run: cmdExit,
    },
};

// Aliases for convenience — `wipe` reads more naturally than `clear --yes` for some folks.
COMMANDS.wipe = COMMANDS.clear;

// =====================================================================
// Command handlers
// =====================================================================

async function cmdStart() {
    if (!process.env.NGROK_AUTHTOKEN) {
        fail('NGROK_AUTHTOKEN env var is required. Get one at https://dashboard.ngrok.com.');
        return 1;
    }
    try {
        const { ngrokUrl, apiUrl } = await services.start();
        process.stdout.write('\n');
        ok(`Stack is up. Point any KMP consumer at ${c.bold().green(apiUrl)}.`);
        dim('  • mobile / web: ./gradlew :stockholm-transport:publishToMavenLocal && rebuild apps');
        dim('  • node demo:    already wired (file: dep, picks up changes via nodemon)');
        dim(`  • public:       ${ngrokUrl}/v1/lines?transport_authority_id=1`);
        return 0;
    } catch (e) {
        fail(`start failed: ${e.message}`);
        await services.stop({ silent: true });
        return 1;
    }
}

async function cmdStop() {
    await services.stop();
    return 0;
}

async function cmdPublish(args) {
    const skipIos = args.includes('--no-ios');
    const success = await publishAll(REPO_ROOT, { skipIos });
    return success ? 0 : 1;
}

async function cmdUp(args) {
    const build = !args.includes('--no-build');
    const services = args.filter((a) => !a.startsWith('--'));
    const success = await dockerUp(REPO_ROOT, { build, services });
    return success ? 0 : 1;
}

async function cmdDown(args) {
    const volumes = args.includes('--volumes') || args.includes('-v');
    const success = await dockerDown(REPO_ROOT, { volumes });
    return success ? 0 : 1;
}

async function cmdPs() {
    const rows = await dockerPs(REPO_ROOT);
    info(`Docker compose containers (${rows.length}):`);
    printPsTable(rows);
    return 0;
}

async function cmdLogs(args) {
    const flags = new Set(args.filter((a) => a.startsWith('--')));
    const positional = args.filter((a) => !a.startsWith('--') && a !== '-f');
    const service = positional[0];
    if (!service) {
        fail('Usage: logs <service> [--follow] [--tail N]');
        return 1;
    }
    const follow = flags.has('--follow') || args.includes('-f');
    let tail = 100;
    const tailIdx = args.indexOf('--tail');
    if (tailIdx >= 0 && args[tailIdx + 1]) {
        tail = Number(args[tailIdx + 1]) || 100;
    }
    const success = await dockerLogs(REPO_ROOT, service, { follow, tail });
    return success ? 0 : 1;
}

async function cmdLoad() {
    info('`load` brings the stack up and lets bootstrap.js seed Mongo if empty.');
    const success = await dockerUp(REPO_ROOT, { build: true });
    if (!success) return 1;
    dim('  Tail the bootstrap log: `logs realtime-api --tail 200`');
    return 0;
}

async function cmdSeed() {
    const rows = await dockerPs(REPO_ROOT);
    const realtimeRunning = rows.some(
        (r) => /realtime-api/i.test(r.Service ?? r.Name ?? '') && /running|up/i.test(r.State ?? r.Status ?? ''),
    );
    if (!realtimeRunning) {
        warn('realtime-api container is not running — bringing the stack up first…');
        const up = await dockerUp(REPO_ROOT, { build: false });
        if (!up) return 1;
    }
    const success = await dockerSeed(REPO_ROOT);
    return success ? 0 : 1;
}

async function cmdClear(args) {
    if (!args.includes('--yes') && !args.includes('-y')) {
        warn('`clear` is destructive — it wipes the mongo_data Docker volume.');
        dim('  Re-run with `clear --yes` (or `wipe --yes`) to confirm.');
        return 1;
    }
    const success = await dockerDown(REPO_ROOT, { volumes: true });
    return success ? 0 : 1;
}

async function cmdStatus() {
    const s = await services.status();
    info(`Node API (sl-cli): ${s.nodeApi ? c.green('UP') : c.dim('down')}`);
    info(`ngrok URL: ${s.ngrokUrl ? c.bold(s.ngrokUrl) : c.dim('not connected')}`);
    info(`gradle.properties serverHostURL: ${c.bold(s.gradleUrl ?? '(unset)')}`);
    if (s.hasBackup) warn('A backup of gradle.properties exists — run `stop` to restore.');

    const android = resolveAndroidHome();
    const java = resolveJavaHome();
    info(`ANDROID_HOME: ${android ? c.green(android.value) + c.dim(`  (${android.source})`) : c.red('not found')}`);
    info(`JAVA_HOME (21): ${java ? c.green(java.value) + c.dim(`  (${java.source})`) : c.red('not found')}`);

    process.stdout.write('\n');
    info('Docker compose stack:');
    const rows = await dockerPs(REPO_ROOT);
    printPsTable(rows);
    return 0;
}

async function cmdExit() {
    if (services.isRunning()) {
        info('Stopping ngrok / nodemon services…');
        await services.stop({ silent: false });
    }
    ok('Bye.');
    process.exit(0);
}

function showHelp() {
    process.stdout.write('\n' + tag('COMMANDS') + '\n');
    const groups = [
        ['ngrok dev loop',   ['start', 'stop']],
        ['publish',          ['publish']],
        ['docker stack',     ['up', 'down', 'ps', 'logs']],
        ['realtime data',    ['load', 'seed', 'clear']],
        ['lifecycle',        ['status', 'help', 'exit']],
    ];
    for (const [groupTitle, names] of groups) {
        process.stdout.write('  ' + c.dim(`— ${groupTitle} —`) + '\n');
        for (const name of names) {
            const def = COMMANDS[name];
            if (!def) continue;
            const padded = name.padEnd(10);
            process.stdout.write('  ' + c.green(padded) + c.dim(' › ') + def.help + '\n');
        }
    }
    process.stdout.write('\n');
}

async function dispatch(line) {
    const trimmed = line.trim();
    if (!trimmed) return 0;
    const parts = trimmed.split(/\s+/);
    const name = parts[0];
    const args = parts.slice(1);
    const cmd = COMMANDS[name];
    if (!cmd) {
        fail(`Unknown command: ${name}. Type \`help\`.`);
        return 1;
    }
    return cmd.run(args);
}

async function repl() {
    const initialStatus = await services.status();
    process.stdout.write(banner({ ngrokUrl: initialStatus.ngrokUrl, gradleUrl: initialStatus.gradleUrl }));

    const rl = createInterface({ input: process.stdin, output: process.stdout, prompt: PROMPT });
    rl.prompt();

    rl.on('line', async (line) => {
        try {
            await dispatch(line);
        } catch (e) {
            fail(e.message);
        }
        rl.prompt();
    });

    rl.on('close', async () => {
        if (services.isRunning()) {
            process.stdout.write('\n');
            info('Caught EOF. Stopping ngrok / nodemon services…');
            await services.stop({ silent: false });
        }
        process.exit(0);
    });

    const handleSignal = async () => {
        process.stdout.write('\n');
        await cmdExit();
    };
    process.on('SIGINT', handleSignal);
    process.on('SIGTERM', handleSignal);
}

async function main() {
    const argv = process.argv.slice(2);
    if (argv.length === 0) {
        await repl();
        return;
    }
    const code = await dispatch(argv.join(' '));
    if (services.isRunning()) {
        // `start` is a one-shot that keeps ngrok / nodemon alive — leave them
        // running and let SIGINT trigger stop + restore.
        info('Services are running. Press Ctrl+C to stop and restore gradle.properties.');
        process.on('SIGINT', async () => {
            process.stdout.write('\n');
            await services.stop();
            process.exit(0);
        });
    } else {
        process.exit(code ?? 0);
    }
}

main().catch((e) => {
    fail(e.stack ?? e.message);
    process.exit(1);
});
