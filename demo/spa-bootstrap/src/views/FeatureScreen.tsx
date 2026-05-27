import type { ReactNode } from 'react'

interface LoadingState {
  isLoading: boolean
  error: string | null
}

// Generic loading/error/content frame. Every feature's UiState has the same
// { isLoading, error, ... } shape, so one component handles all five.
export function FeatureScreen({
  title,
  subtitle,
  state,
  children,
}: {
  title: string
  subtitle: string
  state: LoadingState | null
  children: ReactNode
}) {
  return (
    <section className="panel">
      <header className="panel-head">
        <h2>{title}</h2>
        <span className="kicker">{subtitle}</span>
      </header>
      {!state || state.isLoading ? (
        <p className="muted">// loading from the KMP SDK…</p>
      ) : state.error ? (
        <p className="error">// {state.error}</p>
      ) : (
        children
      )}
    </section>
  )
}
