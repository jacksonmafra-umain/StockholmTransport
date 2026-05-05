import { spawn } from 'node:child_process';
import path from 'node:path';
import { GradleProps } from './gradle-props.js';
import { info, ok, warn, prefixStream } from './ui.js';

const NODE_API_PORT = 3000;
const NODE_API_BOOT_TIMEOUT_MS = 30_000;

export class Services {
    constructor(repoRoot) {
        this.repoRoot = repoRoot;
        this.nodeApiDir = path.join(repoRoot, 'demo/node-api');
        this.gradleProps = new GradleProps(repoRoot);

        this.nodemon = null;
        this.ngrokListener = null;
        this.ngrokUrl = null;
    }

    isRunning() {
        return this.nodemon !== null || this.ngrokListener !== null;
    }

    async start() {
        if (this.isRunning()) {
            throw new Error('Services already running. Run `stop` first.');
        }

        info('Booting nodemon (demo/node-api)…');
        this.nodemon = spawn('npx', ['--yes', 'nodemon', '--quiet', '--watch', 'server.js', 'server.js'], {
            cwd: this.nodeApiDir,
            stdio: ['ignore', 'pipe', 'pipe'],
            env: { ...process.env, FORCE_COLOR: '1' },
        });

        prefixStream(this.nodemon.stdout, '[node ]', 'magenta');
        prefixStream(this.nodemon.stderr, '[node!]', 'red');

        this.nodemon.on('exit', (code, signal) => {
            if (this.nodemon !== null) {
                warn(`nodemon exited (code=${code} signal=${signal})`);
                this.nodemon = null;
            }
        });

        await waitForListening(this.nodemon.stdout, NODE_API_BOOT_TIMEOUT_MS);
        ok(`Node API up on :${NODE_API_PORT}`);

        info('Opening ngrok tunnel…');
        const ngrok = await import('@ngrok/ngrok');
        this.ngrokListener = await ngrok.connect({
            addr: NODE_API_PORT,
            authtoken_from_env: true,
        });
        this.ngrokUrl = this.ngrokListener.url();
        ok(`ngrok ▶ ${this.ngrokUrl}`);

        const created = await this.gradleProps.backup();
        if (created) {
            info('Backed up gradle.properties → gradle.properties.sl-cli.bak');
        } else {
            warn('Existing backup detected — not overwriting (run `stop` to restore).');
        }

        const apiUrl = `${this.ngrokUrl}/v1`;
        await this.gradleProps.writeKey(apiUrl);
        ok(`gradle.properties: serverHostURL ▶ ${apiUrl}`);

        return { ngrokUrl: this.ngrokUrl, apiUrl };
    }

    async stop({ silent = false } = {}) {
        if (!this.isRunning() && !(await this.gradleProps.hasBackup())) {
            if (!silent) warn('Nothing to stop.');
            return;
        }

        if (this.ngrokListener) {
            try {
                await this.ngrokListener.close();
                if (!silent) ok('ngrok tunnel closed.');
            } catch (e) {
                if (!silent) warn(`ngrok close failed: ${e.message}`);
            }
            this.ngrokListener = null;
            this.ngrokUrl = null;
        }

        if (this.nodemon) {
            const proc = this.nodemon;
            this.nodemon = null;
            await new Promise((resolve) => {
                proc.once('exit', resolve);
                proc.kill('SIGTERM');
                setTimeout(() => {
                    try { proc.kill('SIGKILL'); } catch { /* ignore */ }
                    resolve();
                }, 3000);
            });
            if (!silent) ok('Node API stopped.');
        }

        const restored = await this.gradleProps.restoreIfBackedUp();
        if (restored && !silent) {
            ok('gradle.properties restored from backup.');
        }
    }

    async status() {
        const gradleUrl = await this.gradleProps.readKey();
        return {
            nodeApi: this.nodemon !== null,
            ngrokUrl: this.ngrokUrl,
            gradleUrl,
            hasBackup: await this.gradleProps.hasBackup(),
        };
    }
}

function waitForListening(stream, timeoutMs) {
    return new Promise((resolve, reject) => {
        let buf = '';
        const onData = (chunk) => {
            buf += chunk.toString();
            if (/listening on/i.test(buf) || /Demo API server listening/i.test(buf)) {
                cleanup();
                resolve();
            }
        };
        const onError = (err) => { cleanup(); reject(err); };
        const timer = setTimeout(() => {
            cleanup();
            reject(new Error('Timed out waiting for Node API to listen.'));
        }, timeoutMs);
        function cleanup() {
            clearTimeout(timer);
            stream.off('data', onData);
            stream.off('error', onError);
        }
        stream.on('data', onData);
        stream.on('error', onError);
    });
}
