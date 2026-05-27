// Dev shim — reroute the baked ngrok tunnel to the local node-api.
//
// The library bundle has the public ngrok URL baked into BuildConfig (so the
// phones and the published package can reach it). But ngrok's *free-tier
// browser interstitial* answers browser requests with an HTML warning page
// that carries NO CORS headers — so a `fetch` from the SPA gets blocked by
// CORS before it ever reaches node-api.
//
// The browser runs on the same machine as node-api, so we don't need the
// tunnel at all here: rewrite any `*.ngrok(-free).app|io` origin to
// http://localhost:3000 (the host Node API from `./sl start`, which serves
// permissive CORS). Mobile is untouched — it still uses the real tunnel.
//
// If you'd rather keep the browser on the public URL, drop this import and
// instead disable the ngrok interstitial (paid domain / traffic policy) or
// send the `ngrok-skip-browser-warning` header.
const NGROK_RE = /^https?:\/\/[a-z0-9-]+\.ngrok(-free)?\.(app|io)/i
const LOCAL_API = 'http://localhost:3000'

const nativeFetch = window.fetch.bind(window)

window.fetch = ((input: RequestInfo | URL, init?: RequestInit) => {
  if (typeof input === 'string') {
    return nativeFetch(input.replace(NGROK_RE, LOCAL_API), init)
  }
  if (input instanceof URL) {
    return nativeFetch(input.href.replace(NGROK_RE, LOCAL_API), init)
  }
  if (input instanceof Request && NGROK_RE.test(input.url)) {
    return nativeFetch(new Request(input.url.replace(NGROK_RE, LOCAL_API), input), init)
  }
  return nativeFetch(input, init)
}) as typeof window.fetch
