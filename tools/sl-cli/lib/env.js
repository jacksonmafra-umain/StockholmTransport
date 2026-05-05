import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execSync } from 'node:child_process';

const HOME = os.homedir();
const REQUIRED_JAVA_MAJOR = 21;

/**
 * Resolve ANDROID_HOME without requiring the user to export it.
 *
 * Honors $ANDROID_HOME / $ANDROID_SDK_ROOT first, then falls back to the
 * conventional Android Studio install path on each OS.
 *
 * @returns {{ value: string, source: string } | null}
 */
export function resolveAndroidHome() {
    if (process.env.ANDROID_HOME && fs.existsSync(process.env.ANDROID_HOME)) {
        return { value: process.env.ANDROID_HOME, source: '$ANDROID_HOME' };
    }
    if (process.env.ANDROID_SDK_ROOT && fs.existsSync(process.env.ANDROID_SDK_ROOT)) {
        return { value: process.env.ANDROID_SDK_ROOT, source: '$ANDROID_SDK_ROOT' };
    }

    const candidates = [];
    if (process.platform === 'darwin') {
        candidates.push(path.join(HOME, 'Library/Android/sdk'));
    } else if (process.platform === 'linux') {
        candidates.push(path.join(HOME, 'Android/Sdk'));
    } else if (process.platform === 'win32' && process.env.LOCALAPPDATA) {
        candidates.push(path.join(process.env.LOCALAPPDATA, 'Android/Sdk'));
    }

    for (const c of candidates) {
        if (c && fs.existsSync(c)) {
            return { value: c, source: 'detected: ' + c };
        }
    }
    return null;
}

/**
 * Resolve a JAVA_HOME pointing at the required JDK major (21).
 *
 * Order:
 *   1. $JAVA_HOME if it's the right major.
 *   2. macOS `/usr/libexec/java_home -v 21`.
 *   3. Common install dirs (Library JVMs, Homebrew, SDKMAN).
 *
 * @returns {{ value: string, source: string } | null}
 */
export function resolveJavaHome() {
    const fromEnv = readJdkMajor(process.env.JAVA_HOME);
    if (fromEnv === REQUIRED_JAVA_MAJOR) {
        return { value: process.env.JAVA_HOME, source: '$JAVA_HOME' };
    }

    if (process.platform === 'darwin') {
        try {
            const home = execSync(`/usr/libexec/java_home -v ${REQUIRED_JAVA_MAJOR}`, {
                stdio: ['ignore', 'pipe', 'ignore'],
                encoding: 'utf8',
            }).trim();
            if (home && readJdkMajor(home) === REQUIRED_JAVA_MAJOR) {
                return { value: home, source: '/usr/libexec/java_home' };
            }
        } catch {
            // not installed via macOS java_home, keep looking
        }
    }

    for (const candidate of jdkCandidates()) {
        if (candidate && fs.existsSync(candidate) && readJdkMajor(candidate) === REQUIRED_JAVA_MAJOR) {
            return { value: candidate, source: 'detected: ' + candidate };
        }
    }
    return null;
}

/**
 * Build the env for a Gradle subprocess. Inherits process.env and overlays
 * detected ANDROID_HOME / JAVA_HOME values when they're discoverable.
 *
 * @returns {{ env: NodeJS.ProcessEnv, android: object | null, java: object | null }}
 */
export function buildGradleEnv() {
    const env = { ...process.env };
    const android = resolveAndroidHome();
    if (android) env.ANDROID_HOME = android.value;
    const java = resolveJavaHome();
    if (java) env.JAVA_HOME = java.value;
    return { env, android, java };
}

function jdkCandidates() {
    const out = [];

    if (process.platform === 'darwin') {
        const jvmDirs = [
            '/Library/Java/JavaVirtualMachines',
            path.join(HOME, 'Library/Java/JavaVirtualMachines'),
        ];
        for (const dir of jvmDirs) {
            for (const child of safeReaddir(dir)) {
                out.push(path.join(dir, child, 'Contents/Home'));
            }
        }

        for (const cellar of safeReaddir('/opt/homebrew/Cellar/openjdk@' + REQUIRED_JAVA_MAJOR)) {
            out.push(`/opt/homebrew/Cellar/openjdk@${REQUIRED_JAVA_MAJOR}/${cellar}/libexec/openjdk.jdk/Contents/Home`);
        }
        out.push(`/opt/homebrew/opt/openjdk@${REQUIRED_JAVA_MAJOR}/libexec/openjdk.jdk/Contents/Home`);
    }

    if (process.platform === 'linux') {
        for (const dir of safeReaddir('/usr/lib/jvm')) {
            out.push(path.join('/usr/lib/jvm', dir));
        }
    }

    // SDKMAN — same path on every OS
    const sdkmanRoot = path.join(HOME, '.sdkman/candidates/java');
    for (const child of safeReaddir(sdkmanRoot)) {
        out.push(path.join(sdkmanRoot, child));
    }

    return out;
}

function safeReaddir(dir) {
    try {
        return fs.readdirSync(dir);
    } catch {
        return [];
    }
}

/**
 * Read the major version (e.g. 21) of a JDK installation by reading its
 * release file. Avoids spawning java just to print -version.
 */
function readJdkMajor(home) {
    if (!home) return null;
    const releaseFile = path.join(home, 'release');
    try {
        const text = fs.readFileSync(releaseFile, 'utf8');
        const match = text.match(/JAVA_VERSION="?(\d+)/);
        if (match) return Number(match[1]);
    } catch {
        // fall through and try probing the binary
    }
    const javaBin = path.join(home, 'bin/java');
    if (!fs.existsSync(javaBin)) return null;
    try {
        const out = execSync(`"${javaBin}" -version`, {
            stdio: ['ignore', 'ignore', 'pipe'],
            encoding: 'utf8',
        });
        const m = out.match(/version "?(\d+)/);
        return m ? Number(m[1]) : null;
    } catch {
        return null;
    }
}
