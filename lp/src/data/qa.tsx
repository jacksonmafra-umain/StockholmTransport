// The Q&A list. Adapted from docs/SLIDES_PRESENTER_NOTES.md (A1–A21) for a
// public, written audience — slightly more polished prose than the spoken cues.
// Twelve picks, ordered most-asked first.
import type { ReactNode } from 'react'

export interface QAItem {
  q: string
  a: ReactNode
}

export const QA: QAItem[] = [
  {
    q: 'I have not used KMP. What is it, in one paragraph?',
    a: (
      <>
        <strong>Kotlin Multiplatform</strong> lets you write Kotlin code once and compile it to
        Android (JVM bytecode), iOS (native via LLVM), the JVM, and JavaScript. The UI stays
        native on each platform — Compose on Android, SwiftUI on iOS, React/Vue/Svelte on the web
        — but the data, networking, business logic, and state-management layer is one shared
        codebase. The talk's library is exactly that shared layer for the SL public-transport API:
        five REST features plus a realtime WebSocket trip feed, consumed identically from a phone
        and a browser. KMP has been Stable since November 2023 and is in production at Cash App,
        McDonald's, Netflix, Forbes, and others.
      </>
    ),
  },
  {
    q: 'Why a library at all? Why not just fetch() the API?',
    a: (
      <>
        It is not the one-line <code>httpClient.get("v1/lines")</code> — it is the 200 lines around it:
        DTO-to-domain mapping, error categorisation, the <code>DataResult</code> contract, the{' '}
        <code>subscribe</code>/<code>onCleared</code> lifecycle, the JSON config, the timeout policy.
        Without it, every client rebuilds that — badly, differently. The Act 3 demo only works
        because the same Kotlin runs in V8, the JVM, and iOS.
      </>
    ),
  },
  {
    q: 'What is the bundle size? Is 800 KB a lot?',
    a: (
      <>
        The full production bundle is about <strong>800 KB raw, ~200 KB gzipped</strong>. For a
        reference point: that is roughly the size of Firebase's JavaScript SDK, or about one third
        of a typical React dashboard bundle. Not free, not catastrophic. It pays off when you
        already maintain Android + iOS and have non-trivial domain logic — the productivity of
        shared contracts beats the bytes. It does <em>not</em> pay off for a static landing page
        or a 1 MiB edge runtime; there, plain <code>fetch()</code> is the right call. The talk is
        explicit about both cases.
      </>
    ),
  },
  {
    q: 'Is there a separate package for Node and the browser?',
    a: (
      <>
        No — <strong>one single npm package serves both</strong>. <code>./sl publish</code> emits
        one tarball, <code>jacksonmafra-umain-stockholm-transport-1.0.0.tgz</code>, containing pure
        ESM <code>.mjs</code> files plus <code>.d.mts</code> types. The same{' '}
        <code>{`import * as kmp from '@jacksonmafra-umain/stockholm-transport'`}</code> works in
        Node 18+, in Vite/webpack/Rollup/esbuild, and in raw browser modules.
        <br />
        <br />
        Three reasons it works:
        <ul>
          <li>
            <strong>The library has zero DOM coupling.</strong> No <code>window</code>,{' '}
            <code>localStorage</code>, <code>fs</code>, or <code>http</code> calls in the public
            API — it's a pure data-layer, so the same code runs server-side and client-side.
          </li>
          <li>
            <strong>Ktor JS uses <code>fetch</code> uniformly.</strong> In the browser that's{' '}
            <code>window.fetch</code>; on Node 18+ it's the native global <code>fetch</code>.
            Identical contract.
          </li>
          <li>
            <strong>ESM is universal in 2026.</strong> Node natively loads <code>.mjs</code> as
            ESM; every modern bundler honours <code>module</code> + <code>exports</code> regardless
            of environment.
          </li>
        </ul>
        Framework adapters are ~10 lines each: React's <code>useStockholmTransport</code> hook
        wraps <code>subscribe</code>/<code>onCleared</code> with <code>useState</code> +{' '}
        <code>useEffect</code>; Vue would wrap them in <code>onMounted</code>/{' '}
        <code>onUnmounted</code>; Svelte in <code>onMount</code>/<code>onDestroy</code>. The
        library has no UI component layer at all — just the data subscription contract that every
        UI framework can wire to.
      </>
    ),
  },
  {
    q: 'Why a callback bridge, not expose StateFlow?',
    a: (
      <>
        <code>StateFlow</code> is a Kotlin Coroutines type. Kotlin/JS <em>can</em> technically
        export it, but JavaScript has no native concept of Coroutines or <code>Flow</code> — so
        two things go wrong:
        <ul>
          <li>
            <strong>The generated TypeScript definitions explode.</strong> The auto-emitted{' '}
            <code>.d.mts</code> drags in <code>kotlin.coroutines.flow.StateFlow</code> and its
            many internal tag types. The JS consumer ends up dealing with Kotlin standard-library
            internals instead of clean primitives.
          </li>
          <li>
            <strong>The lifecycle becomes the consumer's problem.</strong> Consuming a StateFlow
            from JS forces the caller to manage <code>CoroutineScope</code> and{' '}
            <code>Dispatcher</code> teardown — a recipe for memory leaks inside a React component
            or a Node request handler (and exactly what slide 19 demos live).
          </li>
        </ul>
        A two-method bridge — <code>subscribe(callback)</code> + <code>onCleared()</code> — hides
        every Kotlin internal and gives every framework the same primitive: a function call when
        state changes, a function call to tear down. Works flawlessly in React, Vue, Svelte,
        vanilla JS, or Node.
      </>
    ),
  },
  {
    q: 'Do I hand-roll TypeScript types?',
    a: (
      <>
        No. Kotlin/JS auto-emits <code>.d.mts</code> definitions (one line in the gradle{' '}
        <code>js(IR)</code> config: <code>generateTypeScriptDefinitions()</code>). TypeScript
        consumers get typed <code>Line</code>, <code>LinesUiState</code>, <code>DataResult</code>,{' '}
        <code>NetworkError</code> — the same types the Compose code binds to.
      </>
    ),
  },
  {
    q: 'How does Kotlin compile to JavaScript? What does the gradle setup look like?',
    a: (
      <>
        Through the <strong>JS (IR) compiler</strong> — Kotlin lowers your source to an Intermediate
        Representation and then emits modern JavaScript (ES2015+) with dead-code elimination, lazy
        top-level initialisation, and clean <code>@JsExport</code> interop with TypeScript (the
        legacy backend mangled exported names; the IR backend does not). KMP is the umbrella —
        Android, iOS, JVM, and JS targets all live in the same module. The whole web setup in{' '}
        <code>shared/build.gradle.kts</code> is <strong>four lines</strong>:
        <pre>
          <code>{`kotlin {
  js(IR) {
    browser()                       // or nodejs()
    useEsModules()                  // emit .mjs, not legacy UMD
    generateTypeScriptDefinitions() // free .d.mts
    binaries.executable()           // produce a runnable artefact
  }
}`}</code>
        </pre>
        Honest gotchas the talk hits head-on: Kotlin 2.3 made <code>Long → BigInt</code> the default
        (was opt-in via <code>-Xes-long-as-bigint</code> in 2.2) — <code>JSON.stringify</code>{' '}
        can't serialize BigInt, so use <code>String</code> for IDs that cross the wire. And a
        Kotlin <code>List&lt;T&gt;</code> exported to JS is <em>not</em> a JS Array — call{' '}
        <code>.asJsReadonlyArrayView()</code> to convert (the React demo does this in one helper).
      </>
    ),
  },
  {
    q: 'Why does the build emit two package.json files?',
    a: (
      <>
        Inside <code>build/js/</code> you'll find:
        <ul>
          <li>
            <code>build/js/package.json</code> — <strong>workspace root manifest</strong>. It is
            marked <code>{`"private": true`}</code> and only lists{' '}
            <code>workspaces</code>, pointing at every transitive Kotlin/JS library the toolchain
            unpacked (Ktor, kotlinx-datetime, kotlinx-serialization, Koin, …). This is internal
            build scaffolding — the Kotlin/JS plugin uses Yarn workspaces to resolve the
            dependency graph during compilation. It is <em>never</em> published.
          </li>
          <li>
            <code>build/js/packages/StockholmTransport-stockholm-transport/package.json</code> —
            the <strong>real publishable package</strong>. The auto-generated file is rewritten
            by a Gradle task (<code>enhanceNpmPackageMetadata</code>) to advertise the scoped
            name <code>@jacksonmafra-umain/stockholm-transport</code>, modern{' '}
            <code>main</code> / <code>module</code> / <code>exports</code> / <code>types</code>{' '}
            fields, plus <code>publishConfig.registry</code> routing to GitHub Packages. This is
            what consumers see and what <code>npm pack</code> bundles into the tarball.
          </li>
        </ul>
        Think of it as a Yarn monorepo where one root manages many sub-packages — except the only
        sub-package you care about is the library itself; the rest is internal coordination.
      </>
    ),
  },
  {
    q: 'How does one Kotlin source hit four HTTP libraries? What is expect / actual?',
    a: (
      <>
        KMP's mechanism for "shared API, platform-specific implementation" has a name:{' '}
        <strong><code>expect</code> / <code>actual</code></strong>. In{' '}
        <code>commonMain</code> you declare <code>expect fun createHttpClient(): HttpClient</code>{' '}
        — just the shape, no body. Then in each platform source set you supply the{' '}
        <code>actual</code> implementation. The compiler links the right one per target. In this
        library that wires four HTTP engines: <strong>OkHttp</strong> on Android (Square's library,
        the Android default), <strong>Darwin</strong> on iOS (URLSession under the hood),{' '}
        <strong>CIO</strong> on the JVM (Ktor's pure-Kotlin engine), and <strong>Ktor JS</strong>{' '}
        in the browser/Node (wraps the platform's native <code>fetch</code>). Above all four, the
        calling code is identical: <code>httpClient.get("v1/lines")</code>.
      </>
    ),
  },
  {
    q: 'Why a callback and not async / await for the ViewModel?',
    a: (
      <>
        For one-shot suspend functions, <code>async</code>/<code>await</code> IS available — the{' '}
        <code>kotlinx-coroutines-core-js</code> library ships a <code>promise {'{ }'}</code>{' '}
        builder. You'd write{' '}
        <code>{`fun fetchProfileAsync(): Promise<Profile> = GlobalScope.promise { fetchProfile() }`}</code>{' '}
        and JS calls <code>await api.fetchProfileAsync()</code> like any other Promise — type-safe
        via the generated <code>.d.mts</code>.
        <br />
        <br />
        The library exposes a <em>callback</em> instead because the primary contract is a{' '}
        <strong>stream</strong>, not a one-shot. A <code>StateFlow</code> keeps emitting —{' '}
        loading → success → error → reload → success — and a Promise resolves once. You'd have to
        poll, which fights the underlying Flow's lifecycle. The callback walks the flow once and{' '}
        <code>onCleared()</code> tears it down — the contract Flow wants. Both primitives can
        coexist: callback for streams, <code>promise {'{ }'}</code> for single results.
      </>
    ),
  },
  {
    q: 'Is KMP production-ready? It burned people before.',
    a: (
      <>
        Stable since November 2023, Google-endorsed at I/O 2024. In production at Cash App,
        McDonald's, Netflix, Forbes, 9GAG, Philips. The old pain — frozen-object memory model,
        awkward Swift interop, slow builds — is fixed. The new memory manager removed the freezing;
        Swift interop maps a Kotlin <code>suspend fun</code> to <code>try await</code> on the Swift
        side. iOS engineers stop treating shared code as foreign.
      </>
    ),
  },
  {
    q: 'What if the SL API changes?',
    a: (
      <>
        Real example from this exact talk-prep: the SL API silently drops <code>lat</code>/
        <code>lon</code> on some site records and <code>gid</code>/<code>stop_area</code> on some
        departure stop-points. Without centralising, every platform breaks at once and gets a
        parallel fix PR. With the SDK: a handful of lines — mark those fields nullable in{' '}
        <code>SiteDto</code> / <code>StopPointDto</code>, default coords to <code>0.0</code> in the
        mapper, and <code>coerceInputValues = true</code> on the JSON config — then one{' '}
        <code>./sl publish</code>. Android, iOS, Node, and the React app all pick the fix up on
        their next launch. Drift window: zero.
      </>
    ),
  },
  {
    q: 'What about WebSockets and realtime?',
    a: (
      <>
        Same <code>subscribe</code> contract. The live trip feed is a Ktor WebSocket implemented{' '}
        <em>once</em> in the library (<code>TripUpdateDataSource</code> → <code>Flow</code> →{' '}
        <code>TripViewModel</code> as <code>StateFlow</code>); every platform consumes it via{' '}
        <code>subscribe</code>/<code>onCleared</code>, identical to the REST features. The library
        encapsulates connect + parse + error-as-state + lifecycle — <em>not</em> reconnection (no
        backoff today; that gets added once, in the library, and every platform inherits it). REST
        or WebSocket, the client just subscribes.
      </>
    ),
  },
  {
    q: 'Why not hand-roll a parallel JS SDK?',
    a: (
      <>
        Drift. Two SDKs = two surfaces, two test suites, two changelogs — out of sync within a
        sprint. KMP makes the JS SDK a build artefact of the Kotlin one; drift is structurally
        impossible.
      </>
    ),
  },
  {
    q: 'Why React in the demo, not Compose Multiplatform for Web?',
    a: (
      <>
        To prove the SDK does not leak Kotlin. In React,{' '}
        <code>viewModel.subscribe(setState)</code> is just a plain JS callback — the boundary is
        honest. Compose Web (Beta since September 2025) would have made it look like Kotlin calling
        Kotlin, which is uninteresting. The SDK is UI-agnostic — use React for SEO-facing web, CMP
        Web for an internal dashboard, Compose for mobile. One library under all of them.
      </>
    ),
  },
  {
    q: 'iOS — SPM or CocoaPods?',
    a: (
      <>
        SPM. The library ships an XCFramework plus a <code>Package.swift</code>; Swift consumers do{' '}
        <code>.package(url: ...)</code> like any other dependency. CocoaPods still works for teams
        that need it, but SPM is the default.
      </>
    ),
  },
  {
    q: "AGP 9 — doesn't it break com.android.application + KMP?",
    a: (
      <>
        Yes — AGP 9 disallows the Android application plugin inside a KMP module. The published
        library is a <em>library</em>, so that does not affect it. The Compose demo app is a
        separate Gradle project with the application plugin in the normal place.
      </>
    ),
  },
  {
    q: 'Does this scale to a team of 20?',
    a: (
      <>
        Healthier with KMP than without. One library team owns the boundary (a contract everyone
        reads); platform teams are consumers. Without it: three drifting clients, duplicated
        reviews. The org cost is real — someone owns and versions the library — but that role is
        visible by design, instead of hidden in three Slack threads.
      </>
    ),
  },
]
