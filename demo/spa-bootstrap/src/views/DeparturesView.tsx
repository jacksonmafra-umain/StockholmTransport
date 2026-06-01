import type { DeparturesUiState, DeparturesViewModel } from '@jacksonmafra-umain/stockholm-transport'
import { getApi } from '../sdk'
import { ktArray } from '../kt'
import { useStockholmTransport } from '../hooks/useStockholmTransport'
import { FeatureScreen } from './FeatureScreen'

// 9192 = Slussen — same site the Node demo loads.
const SLUSSEN = 9192

export function DeparturesView() {
  const state = useStockholmTransport<DeparturesUiState, DeparturesViewModel>(
    () => getApi().getDeparturesViewModel(),
    (vm) => vm.loadDepartures(SLUSSEN),
  )

  return (
    <FeatureScreen
      title="Departures"
      subtitle="// getDeparturesViewModel().loadDepartures(9192) — Slussen"
      state={state}
    >
      <ul className="rows">
        {ktArray(state?.departures).map((dep, i) => (
          <li key={`${dep.lineDesignation}-${dep.destination}-${i}`} className="row">
            <span className={`badge mode-${dep.transportMode.toLowerCase()}`}>{dep.lineDesignation}</span>
            <span className="row-main">{dep.destination}</span>
            <em className="row-meta">{dep.displayTime}</em>
          </li>
        ))}
      </ul>
    </FeatureScreen>
  )
}
