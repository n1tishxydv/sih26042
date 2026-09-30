# SIH26042 Web Frontend Completion Report

## 1. Executive Summary

A production-quality web application has been created in `apps/web/` for the SIH26042 repository.
This application serves as a **judge-facing demonstrator, pedagogical curriculum explorer, and control-plane monitor**.

> **Architectural Boundary:** The web application explicitly presents the system architecture and does **not** pretend to replace the offline Android application. Classroom operations remain 100% offline on physical Android devices.

All existing components (`apps/android`, `services/api`, `services/pack-builder`, `data/packs`, models, and automated tests) remain 100% untouched and functional.

---

## 2. Files Created

```text
apps/web/
├── .env.example                               # Backend URL environment template
├── .env.local                                 # Local development environment configuration
├── README.md                                  # Local development and Vercel deployment guide
├── next.config.mjs                            # Next.js 14 production configuration
├── package.json                               # Web dependencies (Next.js, React, Tailwind, Lucide)
├── postcss.config.mjs                         # PostCSS configuration
├── tailwind.config.ts                         # Custom design system with Ol Chiki font support
├── tsconfig.json                              # Strict TypeScript configuration
├── app/
│   ├── globals.css                            # Global styles, Ol Chiki web font, glassmorphism
│   ├── layout.tsx                             # Root layout wrapping Navbar and Footer
│   ├── page.tsx                               # 1. Home (Hero, problem, solution, metrics, screenshots)
│   ├── demo/
│   │   └── page.tsx                           # 2. Interactive Classroom Demo & Provenance Simulator
│   ├── lessons/
│   │   └── page.tsx                           # 3. NIPUN FLN Lessons Explorer (Multi-phase directives)
│   ├── worksheets/
│   │   └── page.tsx                           # 4. Bilingual Worksheets & Counting Drills Engine
│   ├── flashcards/
│   │   └── page.tsx                           # 5. Interactive 3D FLN Vocabulary Flashcards
│   ├── language-packs/
│   │   └── page.tsx                           # 6. Language Packs Registry (Santali, Mundari, Ho)
│   ├── architecture/
│   │   └── page.tsx                           # 7. System Architecture Blueprint & Edge Boundaries
│   ├── metrics/
│   │   └── page.tsx                           # 8. Audited Forensic Metrics & Limitation Register
│   └── about/
│       └── page.tsx                           # 9. Project Tech Stack, GitHub & APK Instructions
├── components/
│   ├── Navbar.tsx                             # Global header with dynamic backend health polling
│   └── Footer.tsx                             # Global footer with mission and offline disclaimers
├── lib/
│   ├── api.ts                                 # Backend API client with health & pack endpoints
│   └── data.ts                                # Static verified curriculum & audited metrics registry
└── public/
    └── screenshots/                           # Real Android physical runtime screenshots
        ├── screen_01_home.png
        ├── screen_02_teacher_toolkit.png
        ├── screen_03_live_classroom.png
        ├── screen_04_verified_santali.png
        ├── screen_05_lesson.png
        ├── screen_06_activity.png
        ├── screen_07_worksheet.png
        ├── screen_08_flashcards.png
        ├── screen_09_diagnostics.png
        ├── screen_10_judge_mode.png
        └── screen_offline_airplane.png
```

---

## 3. Architecture & Design Principles

1. **Edge vs. Cloud Separation:**
   - The web app dynamically communicates with the control plane at `NEXT_PUBLIC_API_BASE_URL`.
   - The navigation bar continuously polls `/api/v1/health`.
   - Displays `Backend ONLINE` with an emerald pulse when the backend is active.
   - Displays `Backend OFFLINE / UNAVAILABLE` with an amber badge when unreachable, explaining that offline primary classrooms are completely unaffected by cloud state.

2. **Pedagogical Faithfulness:**
   - Ol Chiki script rendering (`U+1C50 - U+1C7F`) is embedded via Google Fonts (`Noto Sans Ol Chiki`) and custom CSS font stacks.
   - Preserves verified Santali curriculum phrases extracted from `data/packs/santali/phrases.json`.
   - Integrates real NIPUN Bharat Foundational Literacy and Numeracy (FLN) competencies (`NIPUN_FLN_M1`, `NIPUN_FLN_L1`).

3. **Engineering Honesty & Strict Provenance:**
   - Output from known curriculum directives is badged with green **VERIFIED NATIVE**.
   - Output from uncurated or open sentences triggers the orange **UNVERIFIED / MACHINE-GENERATED** warning.
   - Native audio playback is only offered for the 49 verified studio WAV assets. Synthetic TTS is formally gated off as **UNAVAILABLE (Refusal to fake)**.

---

## 4. Local Run & Development Commands

```bash
# 1. Navigate to the web package
cd apps/web

# 2. Install dependencies
npm install

# 3. Start development server
npm run dev

# 4. Open in browser
# http://localhost:3000
```

---

## 5. Production Build Results

Executed command:
```bash
npm run build
```

Build Output:
```text
  ▲ Next.js 14.2.15
  - Environments: .env.local

   Creating an optimized production build ...
 ✓ Compiled successfully
   Linting and checking validity of types ...
   Collecting page data ...
   Generating static pages (0/12) ...
   Generating static pages (3/12) 
   Generating static pages (6/12) 
   Generating static pages (9/12) 
 ✓ Generating static pages (12/12)
   Finalizing page optimization ...
   Collecting build traces ...

Route (app)                              Size     First Load JS
┌ ○ /                                    5.26 kB        99.2 kB
├ ○ /_not-found                          873 B            88 kB
├ ○ /about                               146 B          87.3 kB
├ ○ /architecture                        146 B          87.3 kB
├ ○ /demo                                4.39 kB        97.2 kB
├ ○ /flashcards                          2.57 kB        95.3 kB
├ ○ /language-packs                      4.01 kB        96.8 kB
├ ○ /lessons                             2.91 kB        95.7 kB
├ ○ /metrics                             146 B          87.3 kB
└ ○ /worksheets                          2.77 kB        95.5 kB
+ First Load JS shared by all            87.1 kB
  ├ chunks/117-fff2b879f1e9f8f4.js       31.6 kB
  ├ chunks/fd9d1056-4a43c3ba43a0759a.js  53.6 kB
  └ other shared chunks (total)          1.89 kB

○  (Static)  prerendered as static content
```
Result: **Zero errors, 100% clean production build.**

---

## 6. Backend URL Configuration

Configured via environment variable in `apps/web/.env.local` and `apps/web/.env.example`:
```env
NEXT_PUBLIC_API_BASE_URL=https://sih26042-1.onrender.com
```

- When running locally against a local backend, change to `http://localhost:8000`.
- The URL is consumed through the centralized client in `apps/web/lib/api.ts` and never hardcoded into UI pages.

---

## 7. Known Limitations & Forensic Disclosures

1. **Physical Acoustic ASR is Android-Bound:** Real-time microphone acoustic processing using the quantized Sherpa-ONNX Zipformer model requires Android NDK / ARM64 execution. The web demo provides interactive simulation and intent routing.
2. **Machine Translation Quality Boundary:** Open-sentence neural MT benchmark remains measured at **BLEU: 0.01 / chrF: 27.43 / Exact Match: 0/20**. High-confidence classroom interaction relies on the verified phrase registry.
3. **Mundari & Ho Language Packs are Stubs:** Mundari (`unr`) and Ho (`hoc`) are versioned at `0.1.0` as architectural schema proofs-of-concept. Full deployment requires field acoustic collection.
4. **TTS Gate Rejection:** Offline Santali neural TTS is marked **UNAVAILABLE** to prevent classroom acoustic hallucinations.
