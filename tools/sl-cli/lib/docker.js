import { spawn } from 'node:child_process';
import { c, dim, fail, info, ok, prefixStream, warn } from './ui.js';

/**
 * Spawn `docker compose <args>` in the repo root, streaming output through
 * the CLI's prefixed stdout.
 */
function runCompose(repoRoot, args, opts = {}) {
    return new Promise((resolve, reject) => {
        const proc = spawn('docker', ['compose', ...args], {
            cwd: repoRoot,
            stdio: ['ignore', 'pipe', 'pipe'],
            env: { ...process.env, COMPOSE_DOCKER_CLI_BUILD: '1', DOCKER_BUILDKIT: '1' },
        });
        let stdout = '';
        let stderr = '';
        if (opts.captureOutput) {
            proc.stdout.on('data', (d) => { stdout += d.toString(); });
            proc.stderr.on('data', (d) => { stderr += d.toString(); });
        } else {
            prefixStream(proc.stdout, '[docker]', 'cyan');
            prefixStream(proc.stderr, '[docker!]', 'yellow');
        }
        proc.on('exit', (code) => resolve({ code, stdout, stderr }));
        proc.on('error', reject);
    });
}

/**
 * Run a one-shot `docker compose exec` against a service. Used to fire the
 * realtime-api container's own scripts (seed, bootstrap, …) without leaving
 * the CLI.
 */
function runExec(repoRoot, service, cmd) {
    return new Promise((resolve, reject) => {
        const proc = spawn('docker', ['compose', 'exec', '-T', service, ...cmd], {
            cwd: repoRoot,
            stdio: ['ignore', 'pipe', 'pipe'],
        });
        prefixStream(proc.stdout, `[${service}]`, 'magenta');
        prefixStream(proc.stderr, `[${service}!]`, 'yellow');
        proc.on('exit', (code) => resolve({ code }));
        proc.on('error', reject);
    });
}

// Fixed container_names from docker-compose.yml. Used only as a last-resort
// force-remove target — removing a container never touches named volumes, so
// the seeded mongo_data survives.
const CONTAINER_NAMES = ['stockholm-mongo', 'stockholm-node-api', 'stockholm-realtime-api'];

/** `docker rm -f <names>` — force-remove containers, ignoring "no such container". */
function forceRemoveContainers(names) {
    return new Promise((resolve) => {
        const proc = spawn('docker', ['rm', '-f', ...names], { stdio: ['ignore', 'pipe', 'pipe'] });
        proc.stdout.on('data', () => {});
        proc.stderr.on('data', () => {}); // swallow "No such container"
        proc.on('exit', () => resolve());
        proc.on('error', () => resolve());
    });
}

/**
 * `docker compose up -d --force-recreate --remove-orphans [--build] [services…]`.
 *
 * Always recreates containers — even ones that already exist — so a stale,
 * fixed `container_name` left over from a previous run can't block the boot
 * with a "name is already in use" conflict.
 *
 * Volume-safe: `--force-recreate` and `down` (without `-v`) only ever touch
 * containers, never named volumes, so the seeded `mongo_data` is preserved.
 * Use `clear` / `wipe` (down -v) if you actually want to drop the data.
 *
 * Recovery ladder if a lingering container still holds a name:
 *   1. up --force-recreate --remove-orphans
 *   2. down --remove-orphans (volume kept) → retry up
 *   3. docker rm -f <named containers> (volume kept) → retry up
 */
export async function dockerUp(repoRoot, { build = true, services = [] } = {}) {
    info(`Starting Docker stack in background${build ? ' (with --build)' : ''}, force-recreating containers…`);
    const args = ['up', '-d', '--force-recreate', '--remove-orphans'];
    if (build) args.push('--build');
    if (services.length) args.push(...services);

    let { code } = await runCompose(repoRoot, args);

    if (code !== 0) {
        warn('up hit a container conflict — tearing the stack down (mongo_data volume preserved) and retrying…');
        await runCompose(repoRoot, ['down', '--remove-orphans']); // no -v → data kept
        ({ code } = await runCompose(repoRoot, args));
    }

    if (code !== 0) {
        warn('still conflicting — force-removing lingering containers (volume preserved) and retrying once more…');
        await forceRemoveContainers(CONTAINER_NAMES);
        ({ code } = await runCompose(repoRoot, args));
    }

    if (code !== 0) {
        fail(`docker compose up failed (exit ${code}).`);
        return false;
    }
    ok('Stack is up (containers recreated, mongo_data preserved). Run `ps` to check states or `logs <service>` to tail.');
    return true;
}

/** `docker compose down [-v]` */
export async function dockerDown(repoRoot, { volumes = false } = {}) {
    info(`Stopping Docker stack${volumes ? ' (wiping mongo_data volume)' : ''}…`);
    const args = ['down'];
    if (volumes) args.push('-v');
    const { code } = await runCompose(repoRoot, args);
    if (code !== 0) {
        fail(`docker compose down failed (exit ${code}).`);
        return false;
    }
    ok(volumes ? 'Stack stopped and Mongo volume wiped.' : 'Stack stopped.');
    return true;
}

/** `docker compose ps --format json` → parsed array of container states. */
export async function dockerPs(repoRoot) {
    const { code, stdout, stderr } = await runCompose(repoRoot, ['ps', '--format', 'json'], { captureOutput: true });
    if (code !== 0) {
        warn(`docker compose ps exited ${code}: ${stderr.trim().slice(0, 200)}`);
        return [];
    }
    return stdout
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
        .map((line) => {
            try { return JSON.parse(line); }
            catch { return null; }
        })
        .filter(Boolean);
}

/** Print a tidy table of `dockerPs()` output. */
export function printPsTable(rows) {
    if (!rows.length) {
        dim('  (no compose containers — run `up` first)');
        return;
    }
    const headers = ['SERVICE', 'STATE', 'PORTS'];
    const data = rows.map((r) => {
        // Compose reports IPv4 + IPv6 publishers separately for the same
        // host:container pair, so dedupe before printing.
        let ports = '—';
        if (r.Publishers?.length) {
            const seen = new Set();
            const unique = [];
            for (const p of r.Publishers) {
                const key = `${p.PublishedPort}:${p.TargetPort}`;
                if (!seen.has(key) && p.PublishedPort) {
                    seen.add(key);
                    unique.push(`${p.PublishedPort}->${p.TargetPort}`);
                }
            }
            if (unique.length) ports = unique.join(', ');
        } else if (r.Ports) {
            ports = r.Ports;
        }
        return [
            r.Service ?? r.Name ?? '?',
            (r.State ?? r.Status ?? '?').toLowerCase(),
            ports,
        ];
    });
    const widths = headers.map((h, i) =>
        Math.max(h.length, ...data.map((row) => String(row[i] ?? '').length)),
    );
    process.stdout.write('  ' + headers.map((h, i) => c.bold(h.padEnd(widths[i] + 2))).join('') + '\n');
    for (const row of data) {
        const stateColor = /running|up/.test(row[1]) ? 'green'
            : /exit|stopped|dead/.test(row[1]) ? 'red'
            : 'yellow';
        const cells = row.map((v, i) => {
            const padded = String(v ?? '').padEnd(widths[i] + 2);
            if (i === 1) return c[stateColor](padded);
            if (i === 0) return c.bold(padded);
            return c.dim(padded);
        });
        process.stdout.write('  ' + cells.join('') + '\n');
    }
}

/** `docker compose logs [-f] [--tail N] <service>` */
export async function dockerLogs(repoRoot, service, { follow = false, tail = 100 } = {}) {
    const args = ['logs', '--tail', String(tail)];
    if (follow) args.push('-f');
    if (service) args.push(service);
    info(`Tailing logs for ${service ?? 'all services'} (last ${tail}, Ctrl+C to stop)…`);
    const { code } = await runCompose(repoRoot, args);
    return code === 0;
}

/**
 * Force a re-seed of the realtime simulator. Runs the seed scripts inside
 * the realtime-api container directly (bypassing bootstrap.js's idempotency
 * check) so existing Stop / Line / Vehicle docs get re-upserted rather than
 * skipped.
 */
export async function dockerSeed(repoRoot) {
    info('Seeding realtime-api Mongo (Trafiklab snapshots + routes-to-lines)…');
    const { code: code1 } = await runExec(repoRoot, 'realtime-api', [
        'node', 'scripts/seed-from-trafiklab.js', '--data', './data', '--schema', './openapi.json',
    ]);
    if (code1 !== 0) {
        fail(`seed-from-trafiklab.js exited ${code1}.`);
        return false;
    }
    const { code: code2 } = await runExec(repoRoot, 'realtime-api', [
        'node', 'scripts/seed-routes-to-lines.js',
    ]);
    if (code2 !== 0) {
        fail(`seed-routes-to-lines.js exited ${code2}.`);
        return false;
    }
    ok('Mongo re-seeded. Restart realtime-api (`up`) to repopulate auto-started trips.');
    return true;
}
