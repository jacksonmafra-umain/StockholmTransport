import { spawn } from 'node:child_process';
import path from 'node:path';
import { c, info, ok, fail, prefixStream } from './ui.js';

const GRADLE_TASKS = [
    [':stockholm-transport:publishToMavenLocal', 'Publishing library to mavenLocal (Android / JVM / iOS klibs)'],
    [':stockholm-transport:jsBrowserDistribution', 'Building JS bundle (browser + Node consumers)'],
    [':stockholm-transport:assembleXCFramework', 'Assembling iOS XCFramework (Swift consumers via SPM)'],
];

export async function publishAll(repoRoot, { skipIos = false } = {}) {
    const tasks = skipIos ? GRADLE_TASKS.slice(0, 2) : GRADLE_TASKS;

    for (const [task, label] of tasks) {
        info(label);
        const code = await runGradle(repoRoot, task);
        if (code !== 0) {
            fail(`Gradle task ${task} failed (exit ${code}).`);
            return false;
        }
    }

    info('Refreshing demo/node-api dependencies (file: dep)…');
    const npmCode = await runCmd('npm', ['install', '--silent'], path.join(repoRoot, 'demo/node-api'));
    if (npmCode !== 0) {
        fail(`npm install failed (exit ${npmCode}).`);
        return false;
    }

    ok('All packages built. Mobile + web demos can pick up the new artefacts.');
    return true;
}

function runGradle(cwd, task) {
    return runCmd('./gradlew', [task, '--console=plain'], cwd);
}

function runCmd(cmd, args, cwd) {
    return new Promise((resolve, reject) => {
        const proc = spawn(cmd, args, {
            cwd,
            stdio: ['ignore', 'pipe', 'pipe'],
            env: { ...process.env, FORCE_COLOR: '0' },
        });
        prefixStream(proc.stdout, '[' + path.basename(cmd) + '  ]', 'cyan');
        prefixStream(proc.stderr, '[' + path.basename(cmd) + '! ]', 'yellow');
        proc.on('exit', resolve);
        proc.on('error', reject);
    });
}
