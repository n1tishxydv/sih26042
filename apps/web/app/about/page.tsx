import React from "react";
import Link from "next/link";
import {
  Info,
  Github,
  Download,
  Terminal,
  Cpu,
  Layers,
  Sparkles,
  ExternalLink,
  ShieldCheck,
  CheckCircle2,
  Smartphone,
} from "lucide-react";

export default function AboutPage() {
  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto space-y-12">
      {/* Header */}
      <div className="border-b border-slate-800 pb-6">
        <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-indigo-500/10 border border-indigo-500/30 text-indigo-400 mb-2">
          <Info className="w-3.5 h-3.5" />
          Smart India Hackathon 2024 &bull; Technical Dossier
        </div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">
          About Project SIH26042
        </h1>
        <p className="text-sm text-slate-400 max-w-3xl mt-1.5 leading-relaxed">
          An AI-powered vernacular pedagogy and real-time translation tool engineered specifically for mother-tongue-based primary education in tribal districts across India.
        </p>
      </div>

      {/* Core Project Statement */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 bg-slate-900/40 space-y-4">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-emerald-400" />
          Problem Statement & National Mandate
        </h2>
        <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
          National Education Policy (NEP) 2020 and the NIPUN Bharat mission mandate that early primary education (Grades 1 to 3) be conducted in the child&apos;s mother tongue. However, in regions where tribal languages like Santali, Mundari, and Ho predominate, most teachers are unfamiliar with local tribal dialects, while over 70% of schools have zero cellular internet connectivity.
        </p>
        <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
          SIH26042 bridges this critical divide with an offline-first, on-device AI co-teacher that provides immediate acoustic and visual mother-tongue scaffolding on standard entry-level Android devices.
        </p>
      </div>

      {/* Complete Technology Stack */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-white flex items-center gap-2">
          <Cpu className="w-5 h-5 text-indigo-400" />
          End-to-End Technology Stack
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          <div className="p-5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm">Android Mobile Application</span>
              <span className="text-[10px] text-emerald-400 bg-emerald-950 px-2 py-0.5 rounded border border-emerald-500/30">
                Offline Runtime
              </span>
            </div>
            <ul className="space-y-1.5 text-slate-400">
              <li>&bull; <strong>Language:</strong> Kotlin 2.0 with Coroutines &amp; StateFlow</li>
              <li>&bull; <strong>UI Framework:</strong> Jetpack Compose with high-contrast theming</li>
              <li>&bull; <strong>Audio Engine:</strong> Android SoundPool (low-latency PCM playback)</li>
              <li>&bull; <strong>Local Database:</strong> Room SQLite with FTS (Full-Text Search)</li>
              <li>&bull; <strong>Target Hardware:</strong> Android 8.0+ (Tested on Android 11 Go, 2GB RAM)</li>
            </ul>
          </div>

          <div className="p-5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm">On-Device Edge ML Pipeline</span>
              <span className="text-[10px] text-indigo-400 bg-indigo-950 px-2 py-0.5 rounded border border-indigo-500/30">
                Edge Inference
              </span>
            </div>
            <ul className="space-y-1.5 text-slate-400">
              <li>&bull; <strong>ASR Engine:</strong> Sherpa-ONNX Hindi Zipformer (INT8 Quantized)</li>
              <li>&bull; <strong>Audio Ingestion:</strong> 16 kHz Mono PCM real-time stream</li>
              <li>&bull; <strong>Intent Retrieval:</strong> Unicode NFKC Normalizer with Levenshtein fuzzy matcher</li>
              <li>&bull; <strong>MT Fallback:</strong> ONNX Runtime Mobile (IndicTrans2 INT8)</li>
              <li>&bull; <strong>Font Engine:</strong> Embedded Noto Sans Ol Chiki typography</li>
            </ul>
          </div>

          <div className="p-5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm">Cloud Control Plane</span>
              <span className="text-[10px] text-teal-400 bg-teal-950 px-2 py-0.5 rounded border border-teal-500/30">
                Render Hosting
              </span>
            </div>
            <ul className="space-y-1.5 text-slate-400">
              <li>&bull; <strong>Backend Framework:</strong> FastAPI (Python 3.12, Asyncio)</li>
              <li>&bull; <strong>Database &amp; Migrations:</strong> SQLAlchemy 2.0, Alembic</li>
              <li>&bull; <strong>Security:</strong> Path traversal filters (<code className="text-indigo-300">SAFE_ID_REGEX</code>)</li>
              <li>&bull; <strong>Sync Endpoints:</strong> Pack downloads, teacher feedback, telemetry</li>
              <li>&bull; <strong>Containerization:</strong> Docker Python 3.12-slim</li>
            </ul>
          </div>

          <div className="p-5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm">Language Pack Builder Tooling</span>
              <span className="text-[10px] text-amber-400 bg-amber-950 px-2 py-0.5 rounded border border-amber-500/30">
                Compiler CLI
              </span>
            </div>
            <ul className="space-y-1.5 text-slate-400">
              <li>&bull; <strong>Pack Format:</strong> Signed <code className="text-indigo-300">.slp</code> (ZIP archive)</li>
              <li>&bull; <strong>CLI Tool:</strong> Python Click with Rich formatting</li>
              <li>&bull; <strong>Validation:</strong> Pydantic 2.0 schema validation &amp; SHA-256</li>
              <li>&bull; <strong>Audio Validator:</strong> Clipping detection, SNR, sample rate check</li>
              <li>&bull; <strong>Test Coverage:</strong> 27 Pytest unit &amp; regression tests</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Running the Real Android APK & Live Preview */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 bg-slate-900/60 space-y-6">
        <h2 className="text-xl font-bold text-white flex items-center gap-2">
          <Smartphone className="w-5 h-5 text-emerald-400" />
          How to Run the Real Android Application
        </h2>
        <p className="text-xs text-slate-300 leading-relaxed">
          The true SIH26042 application is an Android APK that executes completely offline. To run and inspect the app on your computer or physical phone:
        </p>

        <div className="space-y-4 text-xs">
          {/* Step 1 */}
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <div className="font-bold text-indigo-300 flex items-center gap-1.5">
              <Terminal className="w-4 h-4" />
              1. Build & Install via ADB
            </div>
            <div className="bg-slate-900 p-3 rounded font-mono text-[11px] text-slate-300 overflow-x-auto">
              # From repository root:<br />
              ./apps/android/gradlew.bat -p apps/android assembleDebug<br />
              adb install -r apps/android/app/build/outputs/apk/debug/app-debug.apk<br />
              adb shell am start -n org.sih26042.coteacher/.presentation.MainActivity
            </div>
          </div>

          {/* Step 2 */}
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <div className="font-bold text-emerald-300 flex items-center gap-1.5">
              <Terminal className="w-4 h-4" />
              2. Run the Live Web Preview Stream
            </div>
            <p className="text-slate-400">
              Stream live Android screen captures and UI state directly in your browser:
            </p>
            <div className="bg-slate-900 p-3 rounded font-mono text-[11px] text-slate-300 overflow-x-auto">
              python scripts/live_preview_server.py<br />
              # Open http://localhost:8501 to view real-time emulator frames
            </div>
          </div>

          {/* Step 3 */}
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <div className="font-bold text-teal-300 flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4" />
              3. Verify Offline Airplane Mode
            </div>
            <p className="text-slate-400">
              Turn off all Wi-Fi and mobile data on your Android phone or emulator. Notice how speech recognition, Ol Chiki rendering, worksheets, flashcards, and native audio playback remain 100% functional.
            </p>
          </div>
        </div>
      </div>

      {/* GitHub Repository Links */}
      <div className="p-6 rounded-2xl bg-gradient-to-r from-indigo-950/40 via-slate-900 to-emerald-950/40 border border-slate-800 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Github className="w-5 h-5 text-indigo-400" />
            Open Source Monorepo
          </h3>
          <p className="text-xs text-slate-400 mt-1">
            Complete source code, reproducible test harnesses, and documentation are publicly available on GitHub.
          </p>
        </div>

        <a
          href="https://github.com/n1tishxydv/sih26042"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-5 py-2.5 text-xs font-bold text-white shadow hover:bg-indigo-500 transition-colors shrink-0"
        >
          <span>View on GitHub</span>
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>
    </div>
  );
}
