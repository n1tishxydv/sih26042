import React from "react";
import {
  Smartphone,
  Cpu,
  Layers,
  Cloud,
  ShieldCheck,
  Zap,
  HardDrive,
  Database,
  ArrowRight,
  Server,
  Lock,
} from "lucide-react";

export default function ArchitecturePage() {
  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto space-y-12">
      {/* Header */}
      <div className="border-b border-slate-800 pb-6">
        <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-indigo-500/10 border border-indigo-500/30 text-indigo-400 mb-2">
          <Cpu className="w-3.5 h-3.5" />
          Technical Blueprint & Boundary Architecture
        </div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">
          System Architecture & Edge Pipeline
        </h1>
        <p className="text-sm text-slate-400 max-w-3xl mt-1.5 leading-relaxed">
          SIH26042 is designed strictly around an <strong className="text-emerald-400">Offline-First Edge Paradigm</strong>.
          Classroom primary instruction requires zero cloud connectivity. The cloud control plane functions only as an optional lifecycle manager for pack updates and teacher feedback ingestion.
        </p>
      </div>

      {/* Visual High-Level Diagram */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 bg-slate-900/60 space-y-6">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Zap className="w-4 h-4 text-emerald-400" />
            Classroom Edge vs. Cloud Control Plane Boundary
          </h2>
          <span className="text-[11px] bg-emerald-500/10 text-emerald-400 px-3 py-1 rounded-full border border-emerald-500/30 font-semibold">
            0% Cloud During Classroom Hours
          </span>
        </div>

        {/* Conceptual Diagram Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
          {/* Edge Classroom Zone (9 cols) */}
          <div className="lg:col-span-8 rounded-2xl p-6 border-2 border-emerald-500/30 bg-emerald-950/10 space-y-6 relative">
            <div className="flex items-center justify-between">
              <span className="text-xs font-extrabold text-emerald-400 tracking-wider uppercase flex items-center gap-1.5">
                <Smartphone className="w-4 h-4" />
                Physical Device Runtime (Airplane Mode Compliant)
              </span>
              <span className="text-[10px] text-slate-400 bg-slate-900 px-2 py-0.5 rounded border border-slate-800">
                Nokia C01 Plus &bull; Android 11 Go &bull; 2 GB RAM
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Module 1: Teacher Input & ASR */}
              <div className="p-4 rounded-xl bg-slate-950/80 border border-slate-800 space-y-2">
                <span className="text-[10px] font-bold text-indigo-400 uppercase tracking-wider block">
                  1. Audio Ingestion & ASR
                </span>
                <h3 className="text-xs font-bold text-white">Sherpa-ONNX Zipformer</h3>
                <p className="text-[11px] text-slate-400 leading-normal">
                  Real-time 16 kHz acoustic capture. Quantized INT8 weights execute natively on mobile CPU with zero network dependency.
                </p>
                <div className="text-[10px] text-emerald-400 font-medium pt-1">
                  0.00% WER / CER on Classroom WAVs
                </div>
              </div>

              {/* Module 2: Hybrid Translation Core */}
              <div className="p-4 rounded-xl bg-slate-950/80 border border-slate-800 space-y-2">
                <span className="text-[10px] font-bold text-emerald-400 uppercase tracking-wider block">
                  2. Hybrid Translation Engine
                </span>
                <h3 className="text-xs font-bold text-white">Curated Fast-Path</h3>
                <p className="text-[11px] text-slate-400 leading-normal">
                  Unicode-normalized intent matcher queries the embedded SQLite database. Sub-second exact match for all NIPUN directives.
                </p>
                <div className="text-[10px] text-emerald-400 font-medium pt-1">
                  100% Intent Retrieval (19/19)
                </div>
              </div>

              {/* Module 3: Classroom Audio & UI */}
              <div className="p-4 rounded-xl bg-slate-950/80 border border-slate-800 space-y-2">
                <span className="text-[10px] font-bold text-teal-400 uppercase tracking-wider block">
                  3. Display & Audio Playback
                </span>
                <h3 className="text-xs font-bold text-white">SoundPool & Compose</h3>
                <p className="text-[11px] text-slate-400 leading-normal">
                  Renders high-contrast Ol Chiki (<span className="font-olchiki">ᱥᱟᱱᱛᱟᱲᱤ</span>) text and fires studio-recorded native 16 kHz WAV audio.
                </p>
                <div className="text-[10px] text-teal-300 font-medium pt-1">
                  780 ms Latency (P50)
                </div>
              </div>
            </div>

            {/* Local Storage & Language Pack Store */}
            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between text-xs">
              <div className="flex items-center gap-2 text-slate-300">
                <HardDrive className="w-4 h-4 text-emerald-400" />
                <span>
                  <strong>Local Encrypted Storage:</strong> Embedded Santali <code className="text-indigo-300">.slp</code> archive + Room SQLite DB
                </span>
              </div>
              <span className="text-slate-500 font-mono text-[10px]">Peak RAM: 91.9 MB</span>
            </div>
          </div>

          {/* Cloud Control Plane Zone (4 cols) */}
          <div className="lg:col-span-4 rounded-2xl p-6 border border-slate-800 bg-slate-950/80 flex flex-col justify-between space-y-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-xs font-extrabold text-indigo-400 tracking-wider uppercase flex items-center gap-1.5">
                  <Cloud className="w-4 h-4" />
                  Cloud Control Plane
                </span>
                <span className="text-[10px] text-slate-400 bg-slate-900 px-2 py-0.5 rounded border border-slate-800">
                  Render &bull; Python 3.12
                </span>
              </div>

              <div className="space-y-3 text-xs text-slate-400">
                <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800">
                  <span className="font-bold text-slate-200 block text-xs">
                    Language Pack Distribution:
                  </span>
                  Serves versioned, cryptographically hashed <code className="text-indigo-300">.slp</code> updates to teachers when connected to Wi-Fi.
                </div>

                <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800">
                  <span className="font-bold text-slate-200 block text-xs">
                    Teacher Feedback Loop:
                  </span>
                  Accepts phonetic corrections and dialect variations submitted by educators for review by tribal linguists.
                </div>

                <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800">
                  <span className="font-bold text-slate-200 block text-xs">
                    Privacy-Preserving Telemetry:
                  </span>
                  Aggregates offline usage metrics without logging raw classroom student audio.
                </div>
              </div>
            </div>

            <div className="pt-3 border-t border-slate-800 text-[11px] text-slate-500">
              Sync is strictly asynchronous & opportunistic. If internet is never available, the app functions indefinitely.
            </div>
          </div>
        </div>
      </div>

      {/* Deep-Dive Architectural Pillars */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Pillar 1: Edge AI Models */}
        <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 space-y-4">
          <div className="h-10 w-10 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
            <Cpu className="w-5 h-5" />
          </div>
          <h2 className="text-xl font-bold text-white">Offline AI Pipeline</h2>
          <p className="text-xs text-slate-400 leading-relaxed">
            The AI engine is partitioned into two deterministic tiers:
          </p>
          <ul className="space-y-2.5 text-xs text-slate-300">
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Sherpa-ONNX Hindi ASR (Zipformer INT8):</strong> Converts 16 kHz PCM microphone streams into Devanagari text on-device. Evaluated on 20 raw acoustic WAVs with 0.00% WER/CER within curriculum constraints.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Classroom Intent Matcher (Fast-Path):</strong> Normalizes diacritics, filler particles (&quot;अरे&quot;, &quot;कृपया&quot;), and aliases. Direct mapping guarantees 0% hallucination for primary school directives.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Fallback Rule-Based MT:</strong> When an unrecognized sentence is spoken, the app triggers fallback with explicit visual amber badges to alert the educator.
              </span>
            </li>
          </ul>
        </div>

        {/* Pillar 2: Language Pack (.slp) Container */}
        <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 space-y-4">
          <div className="h-10 w-10 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Layers className="w-5 h-5" />
          </div>
          <h2 className="text-xl font-bold text-white">Smart Language Pack (.slp)</h2>
          <p className="text-xs text-slate-400 leading-relaxed">
            Language isolation ensures the Android code base is completely decoupled from linguistic content:
          </p>
          <ul className="space-y-2.5 text-xs text-slate-300">
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Standardized ZIP Container:</strong> Each pack bundles <code className="text-indigo-300 font-mono">manifest.json</code>, phrases, FLN vocabulary, activities, worksheets, and 16 kHz audio assets.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Cryptographic Integrity (SHA-256):</strong> Every pack has an immutable checksum validated before extraction to prevent file corruption in the field.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Multi-Lingual Extensibility:</strong> Santali is production-ready (1.0.0), while Mundari and Ho exist as validated architectural stubs (0.1.0) ready for community corpus ingestion.
              </span>
            </li>
          </ul>
        </div>

        {/* Pillar 3: Android App Architecture */}
        <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 space-y-4">
          <div className="h-10 w-10 rounded-lg bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400">
            <Smartphone className="w-5 h-5" />
          </div>
          <h2 className="text-xl font-bold text-white">Android Physical Runtime</h2>
          <p className="text-xs text-slate-400 leading-relaxed">
            Constructed to run seamlessly on the lowest common denominator of Indian government primary school hardware:
          </p>
          <ul className="space-y-2.5 text-xs text-slate-300">
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>2 GB RAM Footprint:</strong> Real device benchmarks on a Nokia C01 Plus demonstrate peak memory usage of only 91.9 MB PSS during heavy audio playback.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Sub-3-Second SLA:</strong> Measured end-to-end response latency of 780 ms (P50) and 920 ms (P95), far surpassing the 3-second pedagogical threshold.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Zero Crash Robustness:</strong> 131 automated Android JVM unit and UI tests verify all states, screen rotations, and low-memory conditions.
              </span>
            </li>
          </ul>
        </div>

        {/* Pillar 4: Security & Hardening */}
        <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 space-y-4">
          <div className="h-10 w-10 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <Lock className="w-5 h-5" />
          </div>
          <h2 className="text-xl font-bold text-white">Security & Threat Model</h2>
          <p className="text-xs text-slate-400 leading-relaxed">
            Engineered with strict enterprise defense boundaries:
          </p>
          <ul className="space-y-2.5 text-xs text-slate-300">
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Zip-Slip & Path Traversal Prevention:</strong> Pack unpacker inspects all entry paths against directory traversal before extraction.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Strict Regex Endpoint Sanitization:</strong> API endpoints validate pack IDs and versions with <code className="text-indigo-300">SAFE_ID_REGEX</code> to prevent injection attacks.
              </span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">&bull;</span>
              <span>
                <strong>Non-Root Docker Execution:</strong> Backend services run as unprivileged users inside minimal Alpine/Debian slim containers on Render.
              </span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
