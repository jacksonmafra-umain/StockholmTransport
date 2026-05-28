# `lp/` — Landing page for the talk

Static SPA (Vite + React + TypeScript) that mirrors the cyberpunk theme of the
deck. It carries a short briefing of the talk, the repo link, and the full Q&A
— since on-stage time for Q&A is limited, the answers live here so the audience
can keep reading after the session.

Designed to be deployed to **Vercel** (auto-detected as a Vite project — no
extra config needed).

## Local dev

```bash
cd lp
npm install
npm run dev            # http://localhost:5174 (5173 is the demo SPA's port)
```

## Build

```bash
npm run build          # tsc -b && vite build  →  dist/
npm run preview        # serve dist/ locally to verify before deploy
```

## Deploy to Vercel

Two options:

**1. From this directory, with the Vercel CLI:**

```bash
npm i -g vercel        # one-time
cd lp
vercel                 # first deploy: links the project, picks a name
vercel --prod          # promote to production
```

Vercel auto-detects Vite. Build command: `npm run build`. Output: `dist/`.

**2. Connect the GitHub repo:**

In the Vercel dashboard, "Add new project" → import `jacksonmafra-umain/StockholmTransport`
→ set **Root Directory** to `lp` → deploy. Subsequent pushes auto-deploy.

## What's inside

```
src/
├── main.tsx           # React 19 entry
├── App.tsx            # Hero · Briefing · Q&A · Footer
├── styles.css         # cyberpunk theme tokens, mirrored from Slides.pen
└── data/
    └── qa.tsx         # the Q&A items (adapted from presenter notes)
```

No router, no state library, no API calls. Q&A items use native `<details>` /
`<summary>` for accessibility and zero JS state. Fonts come from Google Fonts
(Geist + Geist Mono + IBM Plex Mono).

## Editing the Q&A

Edit [`src/data/qa.tsx`](src/data/qa.tsx) — each item is `{ q, a }` where `a`
is JSX so you can drop `<code>`, `<em>`, links, etc. The list renders in
declared order.
