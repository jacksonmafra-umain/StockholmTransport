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
    q: 'What is the bundle size?',
    a: (
      <>
        About 80 KiB runtime plus ~800 KiB production bundle. It pays off when you already maintain
        Android + iOS and have non-trivial domain logic — the productivity of shared contracts beats
        the bytes. It does <em>not</em> pay off for a static landing page or a 1 MiB edge runtime;
        there, plain <code>fetch()</code> is the right call. The talk is explicit about both cases.
      </>
    ),
  },
  {
    q: 'Why a callback bridge, not expose StateFlow?',
    a: (
      <>
        <code>StateFlow</code> is a coroutines type — JavaScript has no suspending functions or{' '}
        <code>Flow</code>. Exposing it would either leak Kotlin internals into JS or lose the
        lifecycle contract. <code>subscribe(callback)</code> + <code>onCleared()</code> — two
        methods, framework-agnostic, works in React, Vue, Svelte, vanilla, or Node.
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
