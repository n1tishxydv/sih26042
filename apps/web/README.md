# SIH26042 Co-Teacher Web Demonstration Portal

Production-quality Next.js + TypeScript + Tailwind CSS demonstration and judging portal for **SIH26042: AI-Powered Vernacular Pedagogy and Real-Time Translation Tool for Mother-Tongue-Based Primary Education**.

> **Note on Architecture:** This web application is a judge-facing demonstrator, curriculum explorer, and control-plane monitor. It does **not** replace the primary offline Android classroom application. Primary rural classrooms operate with 100% offline edge inference on physical Android devices.

---

## Features

- **Classroom Workflow Demo (`/demo`):** Interactive Hindi-to-Santali simulator featuring Ol Chiki script rendering, Latin/Devanagari transliterations, strict provenance tagging (`VERIFIED_NATIVE` vs `RULE_BASED`), and studio WAV audio playback indicators.
- **NIPUN / FLN Curriculum Lessons (`/lessons`):** Structured primary bridge lessons with multi-step classroom phases.
- **Worksheets Engine (`/worksheets`):** Interactive and printable multi-lingual evaluation worksheets.
- **FLN Flashcards (`/flashcards`):** 3D flipping vocabulary cards with category filtering.
- **Language Pack Registry (`/language-packs`):** Complete manifest inspection distinguishing FULL (Santali v1.0.0) from STUB (Mundari, Ho v0.1.0) packages, with live sync to the FastAPI backend.
- **System Architecture Blueprint (`/architecture`):** Comprehensive breakdown of edge Android AI, Sherpa-ONNX Zipformer, language pack containers (`.slp`), and FastAPI control plane.
- **Forensic Metrics Register (`/metrics`):** Audited empirical measurements only (ASR: 0.00% WER/CER, 100% Intent retrieval; MT: BLEU 0.01 / chrF 27.43; Memory: 91.9 MB PSS; Sub-3s SLA: 780 ms P50).
- **Project Dossier (`/about`):** Technology stack breakdown, GitHub links, and APK execution instructions.
- **Real-Time Control Plane Health Check:** Dynamic polling of `/api/v1/health` with online/offline badge in the global navigation bar.

---

## Local Development

### 1. Prerequisites
- Node.js 18.17+ or 20+
- npm 9+

### 2. Installation
From the `apps/web` directory:
```bash
npm install
```

### 3. Environment Configuration
Create a `.env.local` file:
```bash
cp .env.example .env.local
```

Configurable variables:
```env
# URL of the SIH26042 FastAPI backend
# Default production Render deployment:
NEXT_PUBLIC_API_BASE_URL=https://sih26042-1.onrender.com

# For local development with backend running locally on port 8000:
# NEXT_PUBLIC_API_BASE_URL=http://localhost:8000
```

### 4. Running the Dev Server
```bash
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) in your browser.

---

## Production Build & Run

To create an optimized production build:
```bash
npm run build
npm run start
```
The server will start on port 3000 by default.

---

## Vercel Deployment Instructions

`apps/web` is structured as an independent Next.js application that can be deployed to Vercel with zero friction:

1. **Root Directory Setting:**
   In the Vercel project configuration, set **Root Directory** to:
   ```text
   apps/web
   ```
2. **Build Settings:**
   - Framework Preset: `Next.js`
   - Build Command: `npm run build`
   - Output Directory: `.next`
   - Install Command: `npm install`
3. **Environment Variables:**
   Add the following environment variable in the Vercel Dashboard:
   ```text
   NEXT_PUBLIC_API_BASE_URL = https://sih26042-1.onrender.com
   ```
4. **Deploy:**
   Trigger the deployment. The App Router routes will statically pre-render where applicable and connect client-side to the Render control plane.

---

## Monorepo Compatibility

`apps/web` has zero dependencies on Python environments or Android Gradle builds. It operates as an isolated package inside the `apps/` directory, maintaining total monorepo integrity:

```text
apps/web/
├── app/                  # Next.js App Router pages
│   ├── about/
│   ├── architecture/
│   ├── demo/
│   ├── flashcards/
│   ├── language-packs/
│   ├── lessons/
│   ├── metrics/
│   ├── worksheets/
│   ├── globals.css
│   ├── layout.tsx
│   └── page.tsx
├── components/           # Reusable UI components (Navbar, Footer, etc.)
├── lib/                  # API client and verified static datasets
├── public/               # Static assets & real runtime screenshots
├── next.config.mjs
├── package.json
├── postcss.config.mjs
├── tailwind.config.ts
└── tsconfig.json
```
