import './devProxy' // must run before any KMP fetch — reroutes ngrok → localhost:3000
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './styles.css'

// StrictMode double-mounts effects in dev — that's intentional here. The fixed
// hook survives it (cleanup runs between the two mounts); the leaky hook shows
// the cost. Both behaviours are the point of the talk's Act 2.
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
