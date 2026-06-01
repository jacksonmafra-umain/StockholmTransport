import { spawn } from 'node:child_process';
import path from 'node:path';
import { buildGradleEnv } from './env.js';
import { c, dim, fail, info, ok, prefixStream, warn } from './ui.js';

// `assembleXCFramework` became ambiguous in Gradle 9 + AGP 9 after the
// XCFramework was renamed to "StockholmTransport" — Gradle now sees it
// alongside the legacy auto-derived task name. Use the fully qualified
// `assembleStockholmTransportXCFramework` to disambiguate.
//
// `packTalkTgz` is the talk's npm-publish-equivalent: it rewrites the
// auto-generated package.json with the scoped @jacksonmafra-umain/stockholm-transport
// name + modern ESM exports + types, then runs `npm pack` to produce a
// real installable tarball under build/distributions/npm/. The mobile
// demos don't consume the tgz directly (they still use the file: dep
// pointing at the package dir), but the npmPack output is what would
// land on a registry — closes 4 of the 5 gaps from slide 21 locally.
const GRADLE_TASKS = [
    [':stockholm-transport:publishToMavenLocal', 'Publishing library to mavenLocal (Android / JVM / iOS klibs)'],
    [':stockholm-transport:jsBrowserDistribution', 'Building JS bundle (browser + Node consumers)'],
    [':stockholm-transport:packTalkTgz', 'Packaging npm tarball (@jacksonmafra-umain/stockholm-transport-x.y.z.tgz)'],
    [':stockholm-transport:assembleStockholmTransportXCFramework', 'Assembling iOS XCFramework (Swift consumers via SPM)'],
];

export async function publishAll(repoRoot, { skipIos = false } = {}) {
    // skipIos drops the final XCFramework step; the npm tgz is part of the
    // core flow either way (Node + browser demos consume the same artefact).
    const tasks = skipIos ? GRADLE_TASKS.slice(0, 3) : GRADLE_TASKS;

    const { env, android, java } = buildGradleEnv();

    if (android) {
        dim(`  ANDROID_HOME = ${android.value}  (${android.source})`);
    } else {
        warn('ANDROID_HOME not detected — publishToMavenLocal will fail. Install Android Studio or set $ANDROID_HOME.');
    }
    if (java) {
        dim(`  JAVA_HOME    = ${java.value}  (${java.source})`);
    } else {
        warn('JDK 21 not detected — Gradle will use $JAVA_HOME or its own toolchain probe. If the build fails, install JDK 21 (e.g. `brew install openjdk@21`).');
    }

    for (const [task, label] of tasks) {
        info(label);
        const code = await runGradle(repoRoot, task, env);
        if (code !== 0) {
            fail(`Gradle task ${task} failed (exit ${code}).`);
            return false;
        }
    }

    info('Refreshing demo/node-api dependencies (file: dep)…');
    const npmCode = await runCmd('npm', ['install', '--silent'], path.join(repoRoot, 'demo/node-api'), env);
    if (npmCode !== 0) {
        fail(`npm install failed (exit ${npmCode}).`);
        return false;
    }

    ok('All packages built. Mobile + web demos can pick up the new artefacts.');
    dim('  • static demos: GET /modules/lines /sites /departures /stoppoints /authorities');
    dim('  • realtime:     GET /modules/active-trips  (needs `up` for the simulator)');
    return true;
}

function runGradle(cwd, task, env) {
    return runCmd('./gradlew', [task, '--console=plain'], cwd, env);
}

function runCmd(cmd, args, cwd, env) {
    return new Promise((resolve, reject) => {
        const proc = spawn(cmd, args, {
            cwd,
            stdio: ['ignore', 'pipe', 'pipe'],
            env: { ...env, FORCE_COLOR: '0' },
        });
        prefixStream(proc.stdout, '[' + path.basename(cmd) + '  ]', 'cyan');
        prefixStream(proc.stderr, '[' + path.basename(cmd) + '! ]', 'yellow');
        proc.on('exit', resolve);
        proc.on('error', reject);
    });
}
