// Ambient declaration for the Kotlin/JS package.
//
// Kotlin/JS *does* emit `.d.ts` via `generateTypeScriptDefinitions()`, but the
// generated package.json has no `types` field yet (the `npm-publish` plugin is
// not applied — see docs/PUBLISHING.md / slide 20). Until that gap is closed,
// this hand-written declaration is what gives the demo full type-safety, and it
// doubles as a precise map of the SDK boundary: the exact surface JS sees.
//
// It mirrors, 1:1:
//   - shared/<feature>/domain/model/*.kt          (the data classes)
//   - shared/<feature>/presentation/*ViewModel.kt (the UiState + load methods)
//   - shared/core/presentation/BaseViewModel.kt   (subscribe / onCleared)
//   - shared/src/.../js/JsApi.kt                  (StockholmTransportApi)
//
// When the `types` field lands in the published package, delete this file.
declare module '@jacksonmafra-umain/stockholm-transport' {
  // ----- Domain models -----
  export interface Line {
    readonly id: number
    readonly name: string
    readonly designation: string
    readonly transportMode: string
    readonly authority: string
  }
  export interface Site {
    readonly id: number
    readonly name: string
    readonly latitude: number
    readonly longitude: number
  }
  export interface Departure {
    readonly lineDesignation: string
    readonly destination: string
    readonly displayTime: string
    readonly transportMode: string
  }
  export interface StopPoint {
    readonly id: number
    readonly name: string
    readonly type: string
    readonly stopAreaName: string
    readonly authorityName: string
    readonly latitude: number
    readonly longitude: number
  }
  export interface Authority {
    readonly id: number
    readonly name: string
    readonly formalName: string | null
    readonly city: string | null
    readonly country: string | null
  }

  // Kotlin `List<T>` exposed to JS is NOT a JS array — it has no .map/.length/
  // [Symbol.iterator]. It carries a conversion instead: asJsReadonlyArrayView()
  // returns a real JS array. This is one of the genuine interop seams of the talk.
  export interface KtList<T> {
    asJsReadonlyArrayView(): ReadonlyArray<T>
  }

  // ----- UiState shells — always { isLoading, error, <data> } -----
  export interface LinesUiState {
    readonly isLoading: boolean
    readonly error: string | null
    readonly lines: KtList<Line>
  }
  export interface SitesUiState {
    readonly isLoading: boolean
    readonly error: string | null
    readonly sites: KtList<Site>
  }
  export interface DeparturesUiState {
    readonly isLoading: boolean
    readonly error: string | null
    readonly departures: KtList<Departure>
  }
  export interface StopPointsUiState {
    readonly isLoading: boolean
    readonly error: string | null
    readonly stopPoints: KtList<StopPoint>
  }
  export interface AuthoritiesUiState {
    readonly isLoading: boolean
    readonly error: string | null
    readonly authorities: KtList<Authority>
  }

  // ----- BaseViewModel<T> JS surface -----
  // StateFlow is @JsExport.Ignore'd; subscribe(callback) + onCleared() bridge it.
  export interface ViewModel<TState> {
    subscribe(onStateUpdate: (state: TState) => void): void
    onCleared(): void
  }
  export interface LinesViewModel extends ViewModel<LinesUiState> {
    loadLines(): void
  }
  export interface SitesViewModel extends ViewModel<SitesUiState> {
    loadSites(): void
  }
  export interface DeparturesViewModel extends ViewModel<DeparturesUiState> {
    loadDepartures(siteId: number): void
  }
  export interface StopPointsViewModel extends ViewModel<StopPointsUiState> {
    loadStopPoints(): void
  }
  export interface AuthoritiesViewModel extends ViewModel<AuthoritiesUiState> {
    loadAuthorities(): void
  }

  // ----- The single JS entry point -----
  // Kotlin/JS exports `object` declarations as a class with a static
  // getInstance(). Call initialize() (or initializeWithRealtime) exactly once.
  export class StockholmTransportApi {
    static getInstance(): StockholmTransportApi
    initialize(): void
    initializeWithRealtime(
      httpBaseUrl: string,
      wsHost: string,
      wsPort: number,
      wsSecure: boolean,
    ): void
    getLinesViewModel(): LinesViewModel
    getSitesViewModel(): SitesViewModel
    getDeparturesViewModel(): DeparturesViewModel
    getStopPointsViewModel(): StopPointsViewModel
    getAuthoritiesViewModel(): AuthoritiesViewModel
  }
}
