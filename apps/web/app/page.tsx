import React from "react";
import Link from "next/link";
import Image from "next/image";
import {
  Sparkles,
  ArrowRight,
  ShieldCheck,
  Zap,
  Volume2,
  BookOpen,
  Cpu,
  Layers,
  CheckCircle2,
  AlertTriangle,
  FileCheck2,
  Flame,
  Award,
} from "lucide-react";
import { VERIFIED_METRICS } from "@/lib/data";

export default function HomePage() {
  return (
    <div className="relative overflow-hidden">
      {/* Background Decorative Gradients */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[450px] bg-gradient-to-b from-indigo-600/15 via-emerald-600/5 to-transparent blur-3xl pointer-events-none -z-10" />

      {/* Hero Section */}
      <section className="relative pt-12 pb-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="text-center space-y-6 max-w-4xl mx-auto">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 rounded-full px-3.5 py-1 text-xs font-semibold bg-indigo-500/10 border border-indigo-500/30 text-indigo-300">
            <Award className="w-3.5 h-3.5 text-indigo-400" />
            <span>Smart India Hackathon 2024 &bull; Problem Statement SIH26042</span>
          </div>

          {/* Main Title */}
          <h1 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-tight">
            AI-Powered Vernacular Pedagogy for{" "}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 via-teal-300 to-indigo-400">
              Mother-Tongue Primary Education
            </span>
          </h1>

          {/* Subtitle */}
          <p className="text-base sm:text-lg text-slate-300 max-w-3xl mx-auto leading-relaxed">
            A 100% offline edge co-teacher for primary schools in tribal regions.
            Bridges teacher Hindi classroom directives to Santali (<span className="font-olchiki text-emerald-400 font-semibold">ᱥᱟᱱᱛᱟᱲᱤ</span> in Ol Chiki), Mundari, and Ho
            with zero cloud dependencies during instruction.
          </p>

          {/* Action CTAs */}
          <div className="pt-4 flex flex-wrap items-center justify-center gap-4">
            <Link
              href="/demo"
              className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-600/30 hover:bg-indigo-500 transition-all hover:scale-[1.02]"
            >
              <Sparkles className="w-4 h-4" />
              Launch Interactive Demo
              <ArrowRight className="w-4 h-4 ml-1" />
            </Link>

            <Link
              href="/lessons"
              className="inline-flex items-center gap-2 rounded-lg bg-slate-900 border border-slate-700 px-6 py-3 text-sm font-semibold text-slate-200 hover:bg-slate-800 hover:text-white transition-all"
            >
              <BookOpen className="w-4 h-4 text-emerald-400" />
              Browse NIPUN Curriculum
            </Link>

            <Link
              href="/architecture"
              className="inline-flex items-center gap-2 rounded-lg bg-slate-900/60 border border-slate-800 px-5 py-3 text-sm font-medium text-slate-400 hover:text-slate-200 hover:border-slate-700 transition-colors"
            >
              <Cpu className="w-4 h-4 text-indigo-400" />
              System Architecture
            </Link>
          </div>

          {/* Offline Trust Banner */}
          <div className="pt-6 flex flex-wrap items-center justify-center gap-6 text-xs text-slate-400">
            <div className="flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span>Offline Edge Inference (No Internet Required)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Zap className="w-4 h-4 text-indigo-400" />
              <span>780 ms Fast-Path Latency on 2GB RAM</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Volume2 className="w-4 h-4 text-amber-400" />
              <span>49 Native Speaker Studio Recordings</span>
            </div>
          </div>
        </div>
      </section>

      {/* The Core Problem & The Solution */}
      <section className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto border-t border-slate-800/80">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-stretch">
          {/* Problem Card */}
          <div className="glass-panel rounded-2xl p-8 border border-red-500/20 bg-gradient-to-b from-red-950/20 to-slate-950/40 relative">
            <div className="flex items-center gap-2 text-red-400 font-semibold text-xs uppercase tracking-wider mb-3">
              <Flame className="w-4 h-4" />
              The Ground Reality
            </div>
            <h2 className="text-2xl font-bold text-white mb-4">
              The Tribal Primary Language Barrier
            </h2>
            <p className="text-slate-300 text-sm leading-relaxed mb-4">
              In remote government primary schools across Jharkhand, Odisha, and West Bengal,
              teachers are assigned from urban districts and speak Hindi, Bengali, or Odia.
              However, 80%+ of early grade children only understand their mother tongue
              (Santali, Mundari, Ho).
            </p>
            <ul className="space-y-3 text-xs text-slate-400">
              <li className="flex items-start gap-2">
                <span className="text-red-400 font-bold">&bull;</span>
                <span>
                  <strong>72% of tribal schools lack reliable cellular connectivity</strong>:
                  Cloud-based API translators (Google Translate, Bhashini online) fail completely in classrooms.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-red-400 font-bold">&bull;</span>
                <span>
                  <strong>Severe Cognitive Anxiety</strong>: Grade 1 children unable to understand basic instructions
                  like &quot;बैठ जाओ&quot; or &quot;किताब खोलो&quot; disengage, leading to high early dropout rates.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-red-400 font-bold">&bull;</span>
                <span>
                  <strong>Low-Resource Translation Hallucinations</strong>: Generic neural MT models frequently mistranslate
                  elementary classroom commands into unintelligible gibberish.
                </span>
              </li>
            </ul>
          </div>

          {/* Solution Card */}
          <div className="glass-panel rounded-2xl p-8 border border-emerald-500/30 bg-gradient-to-b from-emerald-950/20 to-slate-950/40 relative">
            <div className="flex items-center gap-2 text-emerald-400 font-semibold text-xs uppercase tracking-wider mb-3">
              <ShieldCheck className="w-4 h-4" />
              Our Verified Solution
            </div>
            <h2 className="text-2xl font-bold text-white mb-4">
              SIH26042 Offline Co-Teacher
            </h2>
            <p className="text-slate-300 text-sm leading-relaxed mb-4">
              An offline Android platform that acts as a co-teacher in the pocket of rural educators.
              Combines offline Sherpa-ONNX Hindi ASR with a deterministic verified classroom phrase bank,
              delivering authentic Santali voice and Ol Chiki visuals in under 1 second.
            </p>
            <ul className="space-y-3 text-xs text-slate-300">
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>
                  <strong>Deterministic Fast-Path</strong>: 100% exact intent matching for NIPUN classroom
                  directives with zero hallucination risk.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>
                  <strong>Authentic Native Voice</strong>: 49 studio-verified WAV assets spoken by native Santali speakers,
                  not metallic synthesized speech.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>
                  <strong>NIPUN FLN Pedagogy Toolkit</strong>: Interactive counting drills, bilingual call-and-response,
                  flashcards, and printable PDF worksheets.
                </span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      {/* Verified Master Metrics Strip */}
      <section className="py-12 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="glass-panel rounded-2xl p-6 border border-slate-800 bg-slate-900/50">
          <div className="flex flex-col md:flex-row items-center justify-between pb-6 mb-6 border-b border-slate-800 gap-4">
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <FileCheck2 className="w-4 h-4 text-emerald-400" />
                Audited & Measured Performance Register
              </h3>
              <p className="text-xs text-slate-400">
                Measured on physical hardware (Nokia C01 Plus, 2GB RAM). No synthetic benchmarks.
              </p>
            </div>
            <Link
              href="/metrics"
              className="text-xs font-semibold text-indigo-400 hover:text-indigo-300 flex items-center gap-1"
            >
              View Full Forensic Audit Report
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">
                ASR Acoustic WER / CER
              </span>
              <span className="text-2xl font-extrabold text-emerald-400 mt-1 block">
                {VERIFIED_METRICS.asr.wer}
              </span>
              <span className="text-[10px] text-slate-500 mt-1 block">
                20 raw 16 kHz classroom WAVs
              </span>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">
                Classroom Intent Retrieval
              </span>
              <span className="text-2xl font-extrabold text-emerald-400 mt-1 block">
                {VERIFIED_METRICS.asr.intentRetrieval.split(" ")[0]}
              </span>
              <span className="text-[10px] text-slate-500 mt-1 block">
                19/19 curriculum directives
              </span>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">
                Fast-Path Latency (P50)
              </span>
              <span className="text-2xl font-extrabold text-indigo-400 mt-1 block">
                {VERIFIED_METRICS.latency.fastPathP50}
              </span>
              <span className="text-[10px] text-slate-500 mt-1 block">
                Audio End &rarr; Native SoundPool
              </span>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80">
              <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">
                Peak Device RAM
              </span>
              <span className="text-2xl font-extrabold text-teal-400 mt-1 block">
                91.9 MB
              </span>
              <span className="text-[10px] text-slate-500 mt-1 block">
                Runs stably on 2 GB Android 11 Go
              </span>
            </div>
          </div>

          {/* Honest Metric Boundary Alert */}
          <div className="mt-4 p-3 rounded-lg bg-amber-950/30 border border-amber-500/30 flex items-start gap-2.5 text-xs text-amber-200/90">
            <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
            <div>
              <strong>Scientific Honesty Boundary:</strong> Open-sentence neural Machine Translation on held-out test data measures at BLEU: 0.01 / chrF: 27.43. We refuse to claim 100% neural translation accuracy; classroom safety is strictly ensured via our verified phrase registry with explicit visual provenance badges.
            </div>
          </div>
        </div>
      </section>

      {/* Real Android Runtime Screenshots */}
      <section className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto border-t border-slate-800/80">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 mb-3">
            <span>Verified Android Physical Runtime</span>
          </div>
          <h2 className="text-3xl font-bold text-white">
            Classroom-Tested Android Application
          </h2>
          <p className="text-slate-400 text-xs sm:text-sm mt-2">
            Engineered with Jetpack Compose, Kotlin Coroutines, and local SQLite. High-contrast typography optimized for outdoor sunlight and dim rural classrooms.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="glass-panel rounded-xl overflow-hidden border border-slate-800 group">
            <div className="relative aspect-[9/16] bg-slate-900 overflow-hidden">
              <Image
                src="/screenshots/screen_01_home.png"
                alt="Home Screen"
                fill
                className="object-cover group-hover:scale-105 transition-transform duration-300"
              />
            </div>
            <div className="p-3 bg-slate-950/80">
              <h4 className="text-xs font-bold text-white">1. Home & Pack Selector</h4>
              <p className="text-[11px] text-slate-400">Offline pack status & one-tap speech mic</p>
            </div>
          </div>

          <div className="glass-panel rounded-xl overflow-hidden border border-slate-800 group">
            <div className="relative aspect-[9/16] bg-slate-900 overflow-hidden">
              <Image
                src="/screenshots/screen_03_live_classroom.png"
                alt="Live Classroom Translation"
                fill
                className="object-cover group-hover:scale-105 transition-transform duration-300"
              />
            </div>
            <div className="p-3 bg-slate-950/80">
              <h4 className="text-xs font-bold text-white">2. Live Classroom Audio</h4>
              <p className="text-[11px] text-slate-400">Hindi speech to Ol Chiki & native audio</p>
            </div>
          </div>

          <div className="glass-panel rounded-xl overflow-hidden border border-slate-800 group">
            <div className="relative aspect-[9/16] bg-slate-900 overflow-hidden">
              <Image
                src="/screenshots/screen_07_worksheet.png"
                alt="Worksheet Engine"
                fill
                className="object-cover group-hover:scale-105 transition-transform duration-300"
              />
            </div>
            <div className="p-3 bg-slate-950/80">
              <h4 className="text-xs font-bold text-white">3. Worksheet Drills</h4>
              <p className="text-[11px] text-slate-400">Bilingual count & match exercises</p>
            </div>
          </div>

          <div className="glass-panel rounded-xl overflow-hidden border border-slate-800 group">
            <div className="relative aspect-[9/16] bg-slate-900 overflow-hidden">
              <Image
                src="/screenshots/screen_08_flashcards.png"
                alt="Flashcard Engine"
                fill
                className="object-cover group-hover:scale-105 transition-transform duration-300"
              />
            </div>
            <div className="p-3 bg-slate-950/80">
              <h4 className="text-xs font-bold text-white">4. FLN Flashcards</h4>
              <p className="text-[11px] text-slate-400">Ol Chiki vocabulary with native speech</p>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Exploration Grid */}
      <section className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto border-t border-slate-800/80">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold text-white">
            Explore the Web Demonstrator Portal
          </h2>
          <p className="text-slate-400 text-xs sm:text-sm mt-2">
            Inspect the underlying curricula, interactive flashcards, language pack schemas, and engineering telemetry.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Link
            href="/demo"
            className="glass-panel glass-panel-hover rounded-xl p-6 border border-slate-800 block group"
          >
            <div className="h-10 w-10 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-4 group-hover:bg-indigo-500/20">
              <Sparkles className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-indigo-300 transition-colors">
              Classroom Workflow Demo
            </h3>
            <p className="text-xs text-slate-400 mt-2 leading-relaxed">
              Test canonical teacher prompts in Hindi and observe real-time Ol Chiki rendering, provenance flags, and native audio availability.
            </p>
          </Link>

          <Link
            href="/lessons"
            className="glass-panel glass-panel-hover rounded-xl p-6 border border-slate-800 block group"
          >
            <div className="h-10 w-10 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 mb-4 group-hover:bg-emerald-500/20">
              <BookOpen className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-emerald-300 transition-colors">
              NIPUN / FLN Lessons
            </h3>
            <p className="text-xs text-slate-400 mt-2 leading-relaxed">
              Examine Grade 1 structured lesson plans bridging Hindi classroom management into mother-tongue physical and cognitive activities.
            </p>
          </Link>

          <Link
            href="/language-packs"
            className="glass-panel glass-panel-hover rounded-xl p-6 border border-slate-800 block group"
          >
            <div className="h-10 w-10 rounded-lg bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 mb-4 group-hover:bg-teal-500/20">
              <Layers className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-teal-300 transition-colors">
              Pluggable Language Packs
            </h3>
            <p className="text-xs text-slate-400 mt-2 leading-relaxed">
              Inspect the validated Santali pack alongside the extensible Mundari and Ho architectural stubs with cryptographically verified checksums.
            </p>
          </Link>
        </div>
      </section>
    </div>
  );
}
