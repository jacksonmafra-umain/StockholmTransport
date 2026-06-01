import type { KtList } from '@jacksonmafra-umain/stockholm-transport'

// Kotlin List<T> → JS array.
//
// A Kotlin `List` crossing the JS boundary is NOT a JS array: it has no
// `.map`, no `.length`, no `[Symbol.iterator]`. Kotlin/JS gives it
// `asJsReadonlyArrayView()` instead, which returns a real (read-only) JS array.
// Every view funnels its list through here before rendering. Returns [] for a
// null/undefined state so callers don't need their own guards.
export function ktArray<T>(list: KtList<T> | null | undefined): ReadonlyArray<T> {
  return list ? list.asJsReadonlyArrayView() : []
}
