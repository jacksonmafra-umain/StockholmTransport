import { useEffect, useState } from 'react'

// A ViewModel exposes exactly two things to JavaScript: subscribe(callback)
// and onCleared(). Both come straight from the library's BaseViewModel<T>.
// This hook adds NO new API — it only maps the Kotlin lifecycle onto React's.
export interface SubscribableViewModel<TState> {
  subscribe(onStateUpdate: (state: TState) => void): void
  onCleared(): void
}

/**
 * Bridges a KMP ViewModel's StateFlow into React state.
 *
 *   mount   → factory() builds the VM · subscribe(setState) starts collecting ·
 *             loader(vm) kicks the load.
 *   unmount → onCleared() cancels the coroutine scope. No leak.
 *
 * This is the whole talk in a dozen lines: the SDK boundary is identical on
 * every platform. Android/iOS collect the StateFlow with collectAsState();
 * React subscribes to a plain callback. Same ViewModel, same load methods.
 */
export function useStockholmTransport<TState, TViewModel extends SubscribableViewModel<TState>>(
  factory: () => TViewModel,
  loader: (vm: TViewModel) => void,
): TState | null {
  const [state, setState] = useState<TState | null>(null)

  useEffect(() => {
    const vm = factory()
    vm.subscribe(setState)
    loader(vm)
    return () => vm.onCleared()
    // factory/loader are stable per mount; we intentionally run this once.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return state
}
