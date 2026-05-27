import { useEffect, useState } from 'react'
import type { SubscribableViewModel } from './useStockholmTransport'

// ---------------------------------------------------------------------------
// A tiny global registry that counts how many ViewModel subscriptions are
// currently "live" (subscribed but not yet onCleared()). It's the browser-side
// stand-in for the leaked coroutine scopes — every mount acquires; only a
// proper cleanup releases. This is what the on-stage counter reads from.
// ---------------------------------------------------------------------------
let liveCount = 0
const listeners = new Set<(n: number) => void>()

function emit() {
  for (const l of listeners) l(liveCount)
}

export function acquire() {
  liveCount += 1
  emit()
}

export function release() {
  liveCount = Math.max(0, liveCount - 1)
  emit()
}

export function useLiveSubscriptionCount(): number {
  const [n, setN] = useState(liveCount)
  useEffect(() => {
    listeners.add(setN)
    setN(liveCount)
    return () => {
      listeners.delete(setN)
    }
  }, [])
  return n
}

// ---------------------------------------------------------------------------
// FIXED — the correct hook. Acquire on mount, and on unmount call onCleared()
// AND release(). Navigate away ten times: the counter goes back to flat.
// ---------------------------------------------------------------------------
export function useFixedTransport<TState, TViewModel extends SubscribableViewModel<TState>>(
  factory: () => TViewModel,
  loader: (vm: TViewModel) => void,
): TState | null {
  const [state, setState] = useState<TState | null>(null)
  useEffect(() => {
    const vm = factory()
    acquire()
    vm.subscribe(setState)
    loader(vm)
    return () => {
      vm.onCleared() // ← the one line
      release()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])
  return state
}

// ---------------------------------------------------------------------------
// LEAKY — the same hook with the cleanup return deleted. The coroutine scope
// is never cancelled, the subscription never released. Navigate away ten times
// and the counter only climbs. This is the slide-18 screenshot.
// ---------------------------------------------------------------------------
export function useLeakyTransport<TState, TViewModel extends SubscribableViewModel<TState>>(
  factory: () => TViewModel,
  loader: (vm: TViewModel) => void,
): TState | null {
  const [state, setState] = useState<TState | null>(null)
  useEffect(() => {
    const vm = factory()
    acquire()
    vm.subscribe(setState)
    loader(vm)
    // ❌ no cleanup return — onCleared() is never called, release() never runs.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])
  return state
}
