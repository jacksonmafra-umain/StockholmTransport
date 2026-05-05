#!/usr/bin/env node
import { createInterface } from 'node:readline';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { Services } from '../lib/services.js';
import { publishAll } from '../lib/publish.js';
import { banner, c, dim, fail, info, ok, PROMPT, tag, warn } from '../lib/ui.js';

const REPO_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..');
const services = new Services(REPO_ROOT);

const COMMANDS = {
    start: {
        help: 'Boot Node API + ngrok tunnel, write the public URL into gradle.properties.',
        run: cmdStart,
    },
    stop: {
        help: 'Stop the Node API + ngrok and restore the original gradle.properties.',
        run: cmdStop,
    },
    publish: {
        help: 'Build + publish library: mavenLocal, JS bundle, iOS XCFramework. Use `publish --no-ios` to skip Apple builds.',
        run: cmdPublish,
    },
    status: {
        help: 'Show running services and the current gradle.properties API URL.',
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

async function cmdStatus() {
    const s = await services.status();
    info(`Node API: ${s.nodeApi ? c.green('UP') : c.dim('down')}`);
    info(`ngrok URL: ${s.ngrokUrl ? c.bold(s.ngrokUrl) : c.dim('not connected')}`);
    info(`gradle.properties serverHostURL: ${c.bold(s.gradleUrl ?? '(unset)')}`);
    if (s.hasBackup) warn('A backup of gradle.properties exists — run `stop` to restore.');
    return 0;
}

async function cmdExit() {
    if (services.isRunning()) {
        info('Stopping services…');
        await services.stop({ silent: false });
    }
    ok('Bye.');
    process.exit(0);
}

function showHelp() {
    process.stdout.write('\n' + tag('COMMANDS') + '\n');
    for (const [name, def] of Object.entries(COMMANDS)) {
        const padded = name.padEnd(10);
        process.stdout.write('  ' + c.green(padded) + c.dim(' › ') + def.help + '\n');
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
            info('Caught EOF. Stopping services…');
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
        // start was a one-shot — keep services alive but hand control back via signals
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
