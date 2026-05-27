# `sl` — Stockholm Transport KMP dev CLI

Interactive REPL that orchestrates the four-platform demo loop:

- boots the Node API behind ngrok (with nodemon hot-reload)
- writes the public ngrok URL into `gradle.properties` so subsequent library builds bake it in
- publishes the library to Maven Local + the JS bundle + the iOS XCFramework in one pass
- restores `gradle.properties` on exit

## Setup

```bash
export NGROK_AUTHTOKEN=<your-token>     # free at https://dashboard.ngrok.com
```

That's it. The repo-root `./sl` wrapper auto-installs `tools/sl-cli/`'s npm deps on first run, so there's no separate `npm install` step.

The CLI requires a working Gradle wrapper at the repo root and `npm` on PATH.

## Use

The recommended entrypoint is `./sl` at the repo root — it's a tiny shell wrapper that resolves the right path regardless of cwd:

REPL:

```bash
./sl
▶ sl › help
```

One-shot:

```bash
./sl start            # services run until Ctrl+C
./sl publish          # build + publish, then exit
./sl publish --no-ios # skip the iOS XCFramework
./sl status
./sl stop             # restore gradle.properties from backup
```

If you're already inside `tools/sl-cli/`, `node bin/sl.js …` works too.

## Install globally (the gemini / claude experience)

If you want a real global `sl` command on PATH:

```bash
cd tools/sl-cli
npm link              # symlinks the bin into your global npm prefix

# from anywhere on the system
sl start
sl publish
```

`npm unlink -g sl-cli` undoes it.

## Commands

Grouped the way `sl help` prints them.

### ngrok dev loop

| Command   | What it does                                                                                               |
|-----------|------------------------------------------------------------------------------------------------------------|
| `start`   | Spawn `nodemon demo/node-api/server.js`, open ngrok on `:3000`, back up + rewrite `gradle.properties`.     |
| `stop`    | Close the tunnel, kill nodemon, restore `gradle.properties` from the `.sl-cli.bak` backup.                 |

### Publish

| Command                | What it does                                                                                               |
|------------------------|------------------------------------------------------------------------------------------------------------|
| `publish`              | `:stockholm-transport:publishToMavenLocal` + `jsBrowserDistribution` + `assembleStockholmTransportXCFramework` + `npm install` in `demo/node-api`. |
| `publish --no-ios`     | Skip the Apple XCFramework build for faster iteration.                                                     |

### Docker stack (background)

| Command                       | What it does                                                                                                   |
|-------------------------------|----------------------------------------------------------------------------------------------------------------|
| `up [services…]`              | `docker compose up -d --build`. Boots `mongo` + `node-api` + `realtime-api` (or just the named services).      |
| `up --no-build`               | Skip the rebuild step (fast restart of existing images).                                                       |
| `down`                        | `docker compose down`. Stops every container; mongo_data volume is preserved.                                  |
| `down --volumes` / `down -v`  | Also drops the `mongo_data` volume — wipes the seeded simulator state.                                         |
| `ps`                          | Tabulated `docker compose ps` (service / state / ports), with running services in green.                       |
| `logs <service> [--follow] [--tail N]` | Tail compose logs. Defaults to `--tail 100`; pass `--follow` to keep streaming.                       |

### Realtime data

| Command          | What it does                                                                                                                         |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `load`           | Bring the stack up; `bootstrap.js` inside the realtime-api container auto-seeds Mongo on the first boot.                             |
| `seed`           | Force a re-seed inside the running container — runs `seed-from-trafiklab.js` then `seed-routes-to-lines.js` (both upsert).           |
| `clear --yes`    | Destructive: `docker compose down -v`. Wipes the `mongo_data` volume so the next `up` re-bootstraps from scratch. Refuses without `--yes`. |
| `wipe --yes`     | Alias for `clear --yes`.                                                                                                              |

### Lifecycle

| Command   | What it does                                                                                                                                                                              |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `status`  | ngrok / nodemon state, current `serverHostURL`, ANDROID_HOME / JAVA_HOME detection, plus the Docker stack table (alias for `ps` at the end). |
| `help`    | List the grouped commands above.                                                                                                                                                          |
| `exit`    | Stop ngrok/nodemon services, then quit.                                                                                                                                                   |

## How `start` makes ngrok the talk-day backend

When `start` finishes, `gradle.properties` reads `serverHostURL="https://<random>.ngrok-free.app/v1"`. Any subsequent `./gradlew :stockholm-transport:publishToMavenLocal` bakes that URL into `BuildConfig.API_BASE_URL`, so the rebuilt mobile/web apps issue requests against the ngrok tunnel.

The Node API exposes three surfaces:

- `/modules/{lines,sites,departures,stoppoints,authorities}` — ViewModel-driven static-SDK routes the talk's Act 1 demo uses (`curl /modules/lines`).
- `/modules/active-trips` — drives the library's realtime `TripSelectionViewModel` (added in the Option C lift-realtime-into-the-library refactor). Needs the docker stack up so `realtime-api` is reachable; otherwise the call returns a `NetworkError`.
- `/v1/*` — passthrough proxy to `https://transport.integration.sl.se/v1/*`. The library's own `httpClient.get("v1/lines")` lands here, gets forwarded to SL, and the response flows back through the tunnel.

Net effect: a single command turns your laptop into the network endpoint for every platform in the demo.

## Failure modes

- **`NGROK_AUTHTOKEN env var is required`** — set the env var, re-run `start`. Free tier is fine.
- **`ngrok tunnel session limit reached`** — close any other open ngrok session (browser, other CLI). Free tier is one tunnel at a time.
- **`Existing backup detected`** — a previous `start` didn't run `stop`. Run `stop` to restore `gradle.properties`, then `start` again.
- **`npx nodemon` not found** — first run takes a few seconds while npm fetches it; subsequent runs are cached. If you want a faster start, `cd demo/node-api && npm install` once to get nodemon into local `node_modules`.
- **Hot-reload doesn't pick up library changes** — run `publish` in a second `sl` REPL or terminal; nodemon watches the JS bundle output dir and restarts on change.

## Layout

```
tools/sl-cli/
├── bin/sl.js          # entry, REPL loop, one-shot dispatch
├── lib/
│   ├── ui.js          # banner, prompt, colored output helpers
│   ├── gradle-props.js # read/write/backup gradle.properties
│   ├── services.js    # nodemon + ngrok lifecycle
│   └── publish.js     # gradle task chain + npm install refresh
├── package.json
└── README.md
```

No tests, no build step, no transpilation — pure ESM Node 20+.
