import type { LinesUiState, LinesViewModel } from '@umain/stockholm-transport'
import { getApi } from '../sdk'
import { ktArray } from '../kt'
import { useStockholmTransport } from '../hooks/useStockholmTransport'
import { FeatureScreen } from './FeatureScreen'

export function LinesView() {
  const state = useStockholmTransport<LinesUiState, LinesViewModel>(
    () => getApi().getLinesViewModel(),
    (vm) => vm.loadLines(),
  )

  return (
    <FeatureScreen title="Lines" subtitle="// getLinesViewModel().loadLines()" state={state}>
      <ul className="rows">
        {ktArray(state?.lines).map((line) => (
          <li key={line.id} className="row">
            <span className={`badge mode-${line.transportMode.toLowerCase()}`}>{line.designation}</span>
            <span className="row-main">{line.name}</span>
            <em className="row-meta">{line.transportMode}</em>
          </li>
        ))}
      </ul>
    </FeatureScreen>
  )
}
