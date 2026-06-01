import * as kmp from '@jacksonmafra-umain/stockholm-transport'

// One singleton for the whole app. Kotlin/JS exports the `object` as a class
// with a static getInstance(); initialize() is called exactly once — the same
// two lines the Node demo (demo/node-api/server.js) runs.
//
// initialize() wires the static SDK against BuildConfig.API_BASE_URL, which the
// sl-cli bakes to the ngrok `/v1` URL at publish time. So in the browser the
// library talks to the same upstream the phones do — we add nothing.
let api: kmp.StockholmTransportApi | null = null

export function getApi(): kmp.StockholmTransportApi {
  if (!api) {
    api = kmp.StockholmTransportApi.getInstance()
    api.initialize()
    // eslint-disable-next-line no-console
    console.log('✅ KMP SDK initialised in the browser tab')
  }
  return api
}
