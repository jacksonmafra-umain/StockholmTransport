import type { AuthoritiesUiState, AuthoritiesViewModel } from '@jacksonmafra-umain/stockholm-transport'
import { getApi } from '../sdk'
import { ktArray } from '../kt'
import { useStockholmTransport } from '../hooks/useStockholmTransport'
import { FeatureScreen } from './FeatureScreen'

export function AuthoritiesView() {
  const state = useStockholmTransport<AuthoritiesUiState, AuthoritiesViewModel>(
    () => getApi().getAuthoritiesViewModel(),
    (vm) => vm.loadAuthorities(),
  )

  return (
    <FeatureScreen
      title="Authorities"
      subtitle="// getAuthoritiesViewModel().loadAuthorities()"
      state={state}
    >
      <ul className="rows">
        {ktArray(state?.authorities).map((a) => (
          <li key={a.id} className="row">
            <span className="badge">{a.id}</span>
            <span className="row-main">{a.name}</span>
            <em className="row-meta">{[a.city, a.country].filter(Boolean).join(', ')}</em>
          </li>
        ))}
      </ul>
    </FeatureScreen>
  )
}
