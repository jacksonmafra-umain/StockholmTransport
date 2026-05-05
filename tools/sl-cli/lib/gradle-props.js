import fs from 'node:fs/promises';
import path from 'node:path';

const KEY = 'serverHostURL';

export class GradleProps {
    constructor(repoRoot) {
        this.path = path.join(repoRoot, 'gradle.properties');
        this.backupPath = this.path + '.sl-cli.bak';
    }

    async readKey(key = KEY) {
        const text = await fs.readFile(this.path, 'utf-8');
        const match = text.match(new RegExp(`^${key}=(.*)$`, 'm'));
        if (!match) return null;
        return stripQuotes(match[1].trim());
    }

    async writeKey(value, key = KEY) {
        const text = await fs.readFile(this.path, 'utf-8');
        const newLine = `${key}="${value}"`;
        if (!new RegExp(`^${key}=.*$`, 'm').test(text)) {
            throw new Error(`${key} not found in ${this.path}`);
        }
        const updated = text.replace(new RegExp(`^${key}=.*$`, 'm'), newLine);
        await fs.writeFile(this.path, updated, 'utf-8');
    }

    async backup() {
        try {
            await fs.access(this.backupPath);
            // backup already exists — keep the original to avoid clobbering
            return false;
        } catch {
            await fs.copyFile(this.path, this.backupPath);
            return true;
        }
    }

    async restoreIfBackedUp() {
        try {
            await fs.access(this.backupPath);
        } catch {
            return false;
        }
        await fs.copyFile(this.backupPath, this.path);
        await fs.unlink(this.backupPath);
        return true;
    }

    async hasBackup() {
        try {
            await fs.access(this.backupPath);
            return true;
        } catch {
            return false;
        }
    }
}

function stripQuotes(s) {
    if (s.length >= 2 && s.startsWith('"') && s.endsWith('"')) return s.slice(1, -1);
    return s;
}
