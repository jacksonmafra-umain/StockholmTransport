import type { StopPointsUiState, StopPointsViewModel } from '@umain/stockholm-transport'
import { getApi } from '../sdk'
import { ktArray } from '../kt'
import { useStockholmTransport } from '../hooks/useStockholmTransport'
import { FeatureScreen } from './FeatureScreen'

export function StopPointsView() {
  const state = useStockholmTransport<StopPointsUiState, StopPointsViewModel>(
    () => getApi().getStopPointsViewModel(),
    (vm) => vm.loadStopPoints(),
  )

  return (
    <FeatureScreen
      title="Stop Points"
      subtitle="// getStopPointsViewModel().loadStopPoints()"
      state={state}
    >
      <ul className="rows">
        {ktArray(state?.stopPoints).map((sp) => (
          <li key={sp.id} className="row">
            <span className="badge">{sp.type}</span>
            <span className="row-main">{sp.name}</span>
            <em className="row-meta">{sp.stopAreaName}</em>
          </li>
        ))}
      </ul>
    </FeatureScreen>
  )
}
