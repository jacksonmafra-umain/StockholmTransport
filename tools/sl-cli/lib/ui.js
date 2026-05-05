import kleur from 'kleur';

export const c = kleur;

export const PROMPT = c.green().bold('▶ sl ') + c.dim('› ');

export function banner({ ngrokUrl, gradleUrl }) {
    const lines = [
        '',
        c.green('  ╱╱ STOCKHOLM·TRANSPORT  ──  KMP DEV CLI  ╱╱'),
        c.dim('  ────────────────────────────────────────────────'),
        c.dim('  ANDROID + IOS + JVM + WEB · v1.0.0 · Kotlin 2.3.21'),
        c.dim('  ────────────────────────────────────────────────'),
    ];
    if (ngrokUrl) lines.push(c.green('  ▶ NGROK  ') + c.bold(ngrokUrl));
    if (gradleUrl) lines.push(c.dim('  ▶ GRADLE ') + c.bold(gradleUrl));
    lines.push(c.dim('  Type ') + c.green('help') + c.dim(' for commands. ') + c.green('exit') + c.dim(' to quit.'));
    lines.push('');
    return lines.join('\n');
}

export function tag(label, color = 'cyan') {
    return c[color]().bold(`▶ ${label}`);
}

export function ok(msg) {
    process.stdout.write(c.green('✔ ') + msg + '\n');
}

export function fail(msg) {
    process.stdout.write(c.red('✘ ') + msg + '\n');
}

export function info(msg) {
    process.stdout.write(c.cyan('▶ ') + msg + '\n');
}

export function warn(msg) {
    process.stdout.write(c.yellow('! ') + msg + '\n');
}

export function dim(msg) {
    process.stdout.write(c.dim(msg) + '\n');
}

export function prefixStream(stream, prefix, color) {
    let buffered = '';
    stream.on('data', (chunk) => {
        buffered += chunk.toString();
        let idx;
        while ((idx = buffered.indexOf('\n')) !== -1) {
            const line = buffered.slice(0, idx);
            buffered = buffered.slice(idx + 1);
            process.stdout.write(c[color](prefix) + ' ' + line + '\n');
        }
    });
}
