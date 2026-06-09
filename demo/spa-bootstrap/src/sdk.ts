import StockholmTransportApi from '@jacksonmafra-umain/stockholm-transport'

// One singleton for the whole app. Kotlin 2.3+ exports
// `StockholmTransportApi` as the module's default export (via
// `@JsExport.Default`) with `@JsStatic` companion members, so JS callers
// use it as a namespace directly — no `.getInstance()` ceremony.
// initialize() is called exactly once — the same line the Node demo
// (demo/node-api/server.js) runs.
//
// initialize() wires the static SDK against BuildConfig.API_BASE_URL, which the
// sl-cli bakes to the ngrok `/v1` URL at publish time. So in the browser the
// library talks to the same upstream the phones do — we add nothing.
let initialised = false

export function getApi(): typeof StockholmTransportApi {
  if (!initialised) {
    StockholmTransportApi.initialize()
    initialised = true
    // eslint-disable-next-line no-console
    console.log('✅ KMP SDK initialised in the browser tab')
  }
  return StockholmTransportApi
}
