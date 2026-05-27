import { BrowserRouter, NavLink, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useLiveSubscriptionCount } from './hooks/useLeakDemo'
import { LinesView } from './views/LinesView'
import { SitesView } from './views/SitesView'
import { DeparturesView } from './views/DeparturesView'
import { StopPointsView } from './views/StopPointsView'
import { AuthoritiesView } from './views/AuthoritiesView'
import { LeakDemo } from './views/LeakDemo'

const NAV = [
  { to: '/lines', label: 'Lines' },
  { to: '/sites', label: 'Sites' },
  { to: '/departures', label: 'Departures' },
  { to: '/stoppoints', label: 'Stop Points' },
  { to: '/authorities', label: 'Authorities' },
  { to: '/leak', label: 'The Leak' },
]

function Layout() {
  const live = useLiveSubscriptionCount()
  return (
    <div className="wrap">
      <header className="header">
        <div className="header-puck">SL</div>
        <div>
          <h1>Stockholm Transport · in the browser</h1>
          <div className="subtitle">
            The same KMP SDK Android, iOS and Node use — rendered by React.
          </div>
        </div>
        <div className={`live ${live > 0 ? 'live-on' : ''}`}>
          live subscriptions: <strong>{live}</strong>
        </div>
      </header>

      <nav className="nav">
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `nav-item ${isActive ? 'nav-item-active' : ''}`}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      <main>
        <Outlet />
      </main>

      <footer className="footer">
        Built on the same KMP library and JS package the Node demo and the phones consume —
        zero business logic in this browser.
      </footer>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Navigate to="/lines" replace />} />
          <Route path="/lines" element={<LinesView />} />
          <Route path="/sites" element={<SitesView />} />
          <Route path="/departures" element={<DeparturesView />} />
          <Route path="/stoppoints" element={<StopPointsView />} />
          <Route path="/authorities" element={<AuthoritiesView />} />
          <Route path="/leak" element={<LeakDemo />} />
          <Route path="*" element={<Navigate to="/lines" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
