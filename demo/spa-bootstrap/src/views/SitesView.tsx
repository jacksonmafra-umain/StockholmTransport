import type { SitesUiState, SitesViewModel } from 'StockholmTransport-stockholm-transport'
import { getApi } from '../sdk'
import { ktArray } from '../kt'
import { useStockholmTransport } from '../hooks/useStockholmTransport'
import { FeatureScreen } from './FeatureScreen'

export function SitesView() {
  const state = useStockholmTransport<SitesUiState, SitesViewModel>(
    () => getApi().getSitesViewModel(),
    (vm) => vm.loadSites(),
  )

  return (
    <FeatureScreen title="Sites" subtitle="// getSitesViewModel().loadSites()" state={state}>
      <ul className="rows">
        {ktArray(state?.sites).map((site) => (
          <li key={site.id} className="row">
            <span className="badge">{site.id}</span>
            <span className="row-main">{site.name}</span>
            <em className="row-meta">
              {site.latitude.toFixed(4)}, {site.longitude.toFixed(4)}
            </em>
          </li>
        ))}
      </ul>
    </FeatureScreen>
  )
}
