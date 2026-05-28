import { QA } from './data/qa'

const REPO_URL = 'https://github.com/jacksonmafra-umain/StockholmTransport'

export default function App() {
  return (
    <>
      <div className="wrap">
        <header className="topbar">
          <div className="left">
            <div className="sl-puck">SL</div>
            <span>▶ TAMING THE WEB · mDEVCAMP 2026</span>
          </div>
          <div className="counter">
            <strong>JUNE 04</strong> · 15:35 · LOOP
          </div>
        </header>

        <Hero />
      </div>

      <section className="block">
        <div className="wrap">
          <Briefing />
        </div>
      </section>

      <section className="block">
        <div className="wrap">
          <QASection />
        </div>
      </section>

      <footer className="foot">
        <div className="wrap">
          <Foot />
        </div>
      </footer>
    </>
  )
}

/* ============================================================ HERO */
function Hero() {
  return (
    <section className="hero">
      <div className="hero-kicker">
        <span className="bullet" aria-hidden="true" />
        <span>MDEVCAMP · 2026.06.04 · 15:35 · LOOP</span>
      </div>

      <h1>
        TAMING<br />
        THE WEB<br />
        <span className="neon">WITH KMP.</span>
      </h1>

      <p className="tagline">
        A KMP library is an SDK with multiple front doors. The same Kotlin code — networking, models,
        lifecycle — running on Android, iOS, a Node server, and live in the browser. No rewrite.
      </p>

      <div className="cta-row">
        <a className="btn btn-primary" href={REPO_URL} target="_blank" rel="noreferrer">
          ▶ View the repo
        </a>
        <a className="btn btn-ghost" href="#qa">
          ▼ Read the Q&amp;A
        </a>
      </div>
    </section>
  )
}

/* ============================================================ BRIEFING */
function Briefing() {
  return (
    <div className="briefing">
      <div className="section-head">
        <span className="kicker">// THE TALK</span>
        <h2>What you'll see.</h2>
      </div>

      <p className="lead">
        Mobile teams found an answer to <em>"build it twice"</em>: share the logic, keep the UI
        native. This talk is about the rebuild nobody mentions — the web is the third one.
      </p>

      <p>
        In 35 minutes I take a real Kotlin Multiplatform library — the same one shipping to Android
        and iOS — and add a JavaScript target. The same models, the same networking, the same
        lifecycle, running in Node and live in the browser. <strong>The same SDK, four front doors.</strong>
      </p>

      <p>
        The talk is honest. It names the cost (~800&nbsp;KB), the gaps ("not <code>npm install</code>-able
        yet"), the ecosystem unevenness (Room can't, SQLDelight can), and the team this is — and
        isn't — for.
      </p>

      <div className="acts">
        <ActCard num="01" title="The Dream" body="What 'SDK with multiple front doors' really means: the compile pipeline, the expect/actual seam, the four lines of js(IR), and the export discipline." />
        <ActCard num="02" title="The Reality" body="Where it gets hard: StateFlow → callback, where coroutines actually run on Node and the browser, ecosystem gaps, and the screenshot-moment memory leak demo." />
        <ActCard num="03" title="The Payoff" body="Shipping it: npm distribution, what's still missing, identifying consumers, and the mic-drop demo — fix one line in Kotlin, three platforms reload." />
        <ActCard num="04" title="Take it home" body="Three rules: export the behaviour, hide the machinery. Coroutine scope is your lifecycle contract. The SDK boundary is the same on every platform." />
      </div>
    </div>
  )
}

function ActCard({ num, title, body }: { num: string; title: string; body: string }) {
  return (
    <div className="act-card">
      <div className="act-num">{num}</div>
      <h3>{title}</h3>
      <p>{body}</p>
    </div>
  )
}

/* ============================================================ Q&A */
function QASection() {
  return (
    <div id="qa">
      <div className="section-head">
        <span className="kicker">// Q&amp;A</span>
        <h2>The audience usually asks…</h2>
      </div>
      <p className="qa-intro">
        Time on stage is tight, so the questions live here. The honest answers — including what the
        library does and doesn't do — are written down so you can keep reading after the talk.
      </p>
      <div className="qa-list">
        {QA.map((item, i) => (
          <details key={i} className="qa-item">
            <summary>
              <span className="qa-num" aria-hidden="true">A{(i + 1).toString().padStart(2, '0')}</span>
              <span>{item.q}</span>
              <span className="chev" aria-hidden="true">›</span>
            </summary>
            <div className="qa-body">{item.a}</div>
          </details>
        ))}
      </div>
    </div>
  )
}

/* ============================================================ FOOTER */
function Foot() {
  return (
    <>
      <div className="grid">
        <div className="who">
          <strong>Jackson Mafra</strong>
          Mobile engineer · Umain
        </div>
        <div className="links">
          <a href={REPO_URL} target="_blank" rel="noreferrer">
            github.com/jacksonmafra-umain/StockholmTransport
          </a>
          <a href="https://www.linkedin.com/in/jacksonmafra" target="_blank" rel="noreferrer">
            linkedin.com/in/jacksonmafra
          </a>
        </div>
      </div>
      <div className="tag">// TAMING THE WEB WITH KMP · mDEVCAMP 2026 · LOOP STAGE</div>
    </>
  )
}
