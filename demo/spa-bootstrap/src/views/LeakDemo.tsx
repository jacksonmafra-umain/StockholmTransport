import { useState } from 'react'
import type { LinesUiState, LinesViewModel } from '@umain/stockholm-transport'
import { getApi } from '../sdk'
import { ktArray } from '../kt'
import { useFixedTransport, useLeakyTransport } from '../hooks/useLeakDemo'

// Two widgets that do the SAME work — subscribe to the lines ViewModel — but
// differ by ONE line: the fixed one returns a cleanup that calls onCleared();
// the leaky one doesn't. Mount/unmount each repeatedly and watch the live
// subscription counter in the header.

function LeakyWidget() {
  const state = useLeakyTransport<LinesUiState, LinesViewModel>(
    () => getApi().getLinesViewModel(),
    (vm) => vm.loadLines(),
  )
  return (
    <p className="widget danger-text">
      {!state || state.isLoading ? '// subscribing…' : `// subscribed · ${ktArray(state.lines).length} lines · NEVER cleared`}
    </p>
  )
}

function FixedWidget() {
  const state = useFixedTransport<LinesUiState, LinesViewModel>(
    () => getApi().getLinesViewModel(),
    (vm) => vm.loadLines(),
  )
  return (
    <p className="widget neon-text">
      {!state || state.isLoading ? '// subscribing…' : `// subscribed · ${ktArray(state.lines).length} lines · cleared on unmount`}
    </p>
  )
}

export function LeakDemo() {
  const [leakyMounted, setLeakyMounted] = useState(false)
  const [fixedMounted, setFixedMounted] = useState(false)

  return (
    <section className="panel">
      <header className="panel-head">
        <h2>The leak</h2>
        <span className="kicker">// one line between a library and a leak</span>
      </header>

      <p className="muted leak-intro">
        Each widget subscribes to the same ViewModel. Toggle each one a dozen times and
        watch the live-subscription counter in the header. The leaky one never calls{' '}
        <code>onCleared()</code> — the count only climbs. The fixed one returns a cleanup —
        the count snaps back to flat.
      </p>

      <div className="cols">
        <div className="col col-danger">
          <div className="col-head">
            <strong className="danger-text">WITHOUT onCleared()</strong>
            <button className="btn btn-danger" onClick={() => setLeakyMounted((m) => !m)}>
              {leakyMounted ? 'Unmount' : 'Mount'}
            </button>
          </div>
          {leakyMounted ? <LeakyWidget /> : <p className="widget muted">// not mounted</p>}
          <pre className="snippet">{`useEffect(() => {
  const vm = factory()
  vm.subscribe(setState)
  loader(vm)
  // ❌ no cleanup
}, [])`}</pre>
        </div>

        <div className="col col-neon">
          <div className="col-head">
            <strong className="neon-text">WITH onCleared()</strong>
            <button className="btn btn-neon" onClick={() => setFixedMounted((m) => !m)}>
              {fixedMounted ? 'Unmount' : 'Mount'}
            </button>
          </div>
          {fixedMounted ? <FixedWidget /> : <p className="widget muted">// not mounted</p>}
          <pre className="snippet">{`useEffect(() => {
  const vm = factory()
  vm.subscribe(setState)
  loader(vm)
  return () => vm.onCleared() // ✅
}, [])`}</pre>
        </div>
      </div>
    </section>
  )
}
