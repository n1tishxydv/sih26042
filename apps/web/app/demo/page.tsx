"use client";

import React, { useState } from "react";
import {
  VERIFIED_CLASSROOM_PHRASES,
  PhraseItem,
} from "@/lib/data";
import {
  Volume2,
  VolumeX,
  ShieldCheck,
  AlertTriangle,
  Sparkles,
  Info,
  CheckCircle2,
  Play,
  RotateCcw,
  BookOpen,
} from "lucide-react";

export default function DemoPage() {
  const [selectedPhrase, setSelectedPhrase] = useState<PhraseItem>(
    VERIFIED_CLASSROOM_PHRASES[0]
  );
  const [customInput, setCustomInput] = useState<string>("");
  const [isPlayingAudio, setIsPlayingAudio] = useState<boolean>(false);
  const [isCustomMode, setIsCustomMode] = useState<boolean>(false);

  // Simple rule-based or heuristic translator for unknown custom input to demonstrate fallback
  const handleCustomSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customInput.trim()) return;

    // Check if it matches any alias in our database
    const query = customInput.trim().toLowerCase();
    const matched = VERIFIED_CLASSROOM_PHRASES.find(
      (p) =>
        p.hindiCanonical.toLowerCase() === query ||
        p.hindiAliases.some((alias) => alias.toLowerCase() === query)
    );

    if (matched) {
      setSelectedPhrase(matched);
      setIsCustomMode(false);
    } else {
      // Create an unverified machine-generated fallback item
      const fallbackItem: PhraseItem = {
        phraseId: "custom_unverified_fallback",
        category: "INSTRUCTION",
        intent: "UNVERIFIED_OPEN_SENTENCE",
        hindiCanonical: customInput.trim(),
        hindiAliases: [],
        santaliOlChiki: "ᱚᱞ ᱪᱤᱠᱤ ᱛᱚᱨᱡᱚᱢᱟ (ᱢᱮᱥᱤᱱ ᱛᱮ ᱛᱮᱭᱟᱨ)",
        santaliTransliterationLatin: "Ol Chiki torjoma (Mesin te teyar - Fallback)",
        santaliTransliterationDevanagari: "ओल चिकी तोरजमा (मशीन अनुवाद)",
        pedagogicalContext:
          "Open-domain sentence outside the curated primary curriculum phrase bank. Handled via experimental rule-based fallback pipeline.",
        provenance: "RULE_BASED",
        hasNativeAudio: false,
        difficultyLevel: 3,
      };
      setSelectedPhrase(fallbackItem);
      setIsCustomMode(true);
    }
  };

  const handleSimulateAudio = () => {
    if (!selectedPhrase.hasNativeAudio) return;
    setIsPlayingAudio(true);
    const duration = selectedPhrase.audioDurationMs || 600;
    setTimeout(() => {
      setIsPlayingAudio(false);
    }, duration);
  };

  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="border-b border-slate-800 pb-6">
        <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-indigo-500/10 border border-indigo-500/30 text-indigo-400 mb-2">
          <Sparkles className="w-3.5 h-3.5" />
          Interactive Classroom Workflow Simulator
        </div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">
          Classroom Speech & Translation Demo
        </h1>
        <p className="text-sm text-slate-400 max-w-3xl mt-1.5 leading-relaxed">
          Simulate how an educator commands in Hindi and receives verified Santali (<span className="font-olchiki text-emerald-400">ᱥᱟᱱᱛᱟᱲᱤ</span>)
          with Ol Chiki script rendering, exact provenance tracking, and authentic native speaker audio.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Preset Classroom Directives & Custom Input */}
        <div className="lg:col-span-5 space-y-6">
          {/* Preset Buttons */}
          <div className="glass-panel rounded-xl p-5 border border-slate-800 space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                <BookOpen className="w-4 h-4 text-indigo-400" />
                Curriculum Directives (NIPUN)
              </h2>
              <span className="text-[10px] bg-emerald-500/10 text-emerald-400 px-2 py-0.5 rounded border border-emerald-500/20">
                100% Intent Matched
              </span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              {VERIFIED_CLASSROOM_PHRASES.map((phrase) => {
                const isSelected =
                  !isCustomMode &&
                  selectedPhrase.phraseId === phrase.phraseId;
                return (
                  <button
                    key={phrase.phraseId}
                    type="button"
                    onClick={() => {
                      setSelectedPhrase(phrase);
                      setIsCustomMode(false);
                      setCustomInput("");
                    }}
                    className={`text-left p-2.5 rounded-lg border text-xs transition-all ${
                      isSelected
                        ? "bg-indigo-600/20 border-indigo-500 text-white shadow-sm"
                        : "bg-slate-900/60 border-slate-800 text-slate-300 hover:bg-slate-800 hover:text-white"
                    }`}
                  >
                    <div className="font-medium truncate">{phrase.hindiCanonical}</div>
                    <div className="text-[10px] text-slate-400 font-olchiki truncate mt-0.5">
                      {phrase.santaliOlChiki}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Freeform Hindi Input Form */}
          <div className="glass-panel rounded-xl p-5 border border-slate-800 space-y-3">
            <h2 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
              <Sparkles className="w-4 h-4 text-emerald-400" />
              Test Freeform Hindi Speech / Text
            </h2>
            <p className="text-[11px] text-slate-400">
              Type a variation (e.g. &quot;बैठिए&quot;, &quot;सब बच्चे शांत हो जाओ&quot;) or an uncurated phrase to witness how the safety provenance gate triggers.
            </p>

            <form onSubmit={handleCustomSubmit} className="space-y-3">
              <div className="relative">
                <input
                  type="text"
                  value={customInput}
                  onChange={(e) => setCustomInput(e.target.value)}
                  placeholder="e.g. अपनी किताब खोलो या नया वाक्य..."
                  className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 rounded-lg bg-indigo-600 px-3 py-2 text-xs font-semibold text-white hover:bg-indigo-500 transition-colors shadow"
                >
                  Analyze & Translate
                </button>
                {(isCustomMode || customInput) && (
                  <button
                    type="button"
                    onClick={() => {
                      setCustomInput("");
                      setIsCustomMode(false);
                      setSelectedPhrase(VERIFIED_CLASSROOM_PHRASES[0]);
                    }}
                    className="p-2 rounded-lg bg-slate-800 text-slate-400 hover:text-white hover:bg-slate-700 transition-colors"
                    title="Reset to default"
                  >
                    <RotateCcw className="w-4 h-4" />
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>

        {/* Right Column: Active Output Display & Forensic Provenance */}
        <div className="lg:col-span-7 space-y-6">
          {/* Main Translation Card */}
          <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 bg-slate-900/40 relative space-y-6">
            {/* Top Bar with Provenance Status Badge */}
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-4">
              <div>
                <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                  Target Language & Script
                </span>
                <div className="text-sm font-bold text-white flex items-center gap-2">
                  Santali (<span className="font-olchiki text-emerald-400">ᱥᱟᱱᱛᱟᱲᱤ</span>) &bull; Ol Chiki
                </div>
              </div>

              {/* Provenance Badge */}
              {selectedPhrase.provenance === "VERIFIED_NATIVE" ? (
                <div className="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold bg-emerald-950/80 border border-emerald-500/50 text-emerald-300">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                  VERIFIED NATIVE (Curriculum Fast-Path)
                </div>
              ) : selectedPhrase.provenance === "CURATED_ACADEMIC" ? (
                <div className="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold bg-blue-950/80 border border-blue-500/50 text-blue-300">
                  <CheckCircle2 className="w-3.5 h-3.5 text-blue-400" />
                  CURATED ACADEMIC
                </div>
              ) : (
                <div className="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold bg-amber-950/80 border border-amber-500/50 text-amber-300">
                  <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
                  UNVERIFIED / MACHINE-GENERATED FALLBACK
                </div>
              )}
            </div>

            {/* Input Audio / Hindi Prompt */}
            <div className="space-y-1">
              <span className="text-[11px] font-medium text-slate-400">
                Teacher Input (Hindi ASR Normalized):
              </span>
              <div className="text-lg font-semibold text-slate-200 bg-slate-950/60 p-3 rounded-lg border border-slate-800/80">
                {selectedPhrase.hindiCanonical}
              </div>
            </div>

            {/* Ol Chiki Primary Display */}
            <div className="space-y-2 p-5 rounded-xl bg-gradient-to-br from-indigo-950/30 to-emerald-950/20 border border-slate-700/60">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">
                  Primary Classroom Display (Ol Chiki Script)
                </span>
                <span className="text-[10px] text-slate-400 font-mono">
                  Unicode: U+1C50 - U+1C7F
                </span>
              </div>
              <div className="font-olchiki text-3xl sm:text-4xl font-extrabold text-white tracking-wide py-2">
                {selectedPhrase.santaliOlChiki}
              </div>

              {/* Transliterations */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-3 border-t border-slate-800 text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 block">Latin Romanization:</span>
                  <span className="font-medium text-slate-200">
                    {selectedPhrase.santaliTransliterationLatin}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 block">Devanagari Transliteration:</span>
                  <span className="font-medium text-slate-200">
                    {selectedPhrase.santaliTransliterationDevanagari}
                  </span>
                </div>
              </div>
            </div>

            {/* Audio Output Status & Simulator */}
            <div className="p-4 rounded-xl bg-slate-950/80 border border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Volume2 className="w-4 h-4 text-indigo-400" />
                  Native Audio Playback Status
                </span>
                {selectedPhrase.hasNativeAudio ? (
                  <span className="text-[10px] font-semibold text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-500/30">
                    16 kHz Studio Recording Available
                  </span>
                ) : (
                  <span className="text-[10px] font-semibold text-amber-400 bg-amber-950/60 px-2 py-0.5 rounded border border-amber-500/30">
                    TTS Gate Inactive (Refusal to Fake)
                  </span>
                )}
              </div>

              {selectedPhrase.hasNativeAudio ? (
                <div className="flex items-center justify-between gap-4">
                  <button
                    type="button"
                    onClick={handleSimulateAudio}
                    disabled={isPlayingAudio}
                    className="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-xs font-bold text-white shadow hover:bg-emerald-500 disabled:opacity-50 transition-all"
                  >
                    <Play className={`w-3.5 h-3.5 ${isPlayingAudio ? "animate-spin" : ""}`} />
                    {isPlayingAudio ? "Playing Studio WAV..." : "Simulate Speaker Audio"}
                  </button>

                  <div className="flex-1 flex items-center justify-end gap-1">
                    {isPlayingAudio ? (
                      <div className="flex items-center gap-1 h-6">
                        <span className="w-1 bg-emerald-400 animate-pulse h-4" />
                        <span className="w-1 bg-emerald-400 animate-pulse h-6" />
                        <span className="w-1 bg-emerald-400 animate-pulse h-3" />
                        <span className="w-1 bg-emerald-400 animate-pulse h-5" />
                        <span className="w-1 bg-emerald-400 animate-pulse h-2" />
                      </div>
                    ) : (
                      <span className="text-[11px] text-slate-400">
                        Duration: {selectedPhrase.audioDurationMs} ms
                      </span>
                    )}
                  </div>
                </div>
              ) : (
                <div className="p-3 rounded-lg bg-amber-950/30 border border-amber-500/20 text-xs text-amber-200/90 flex items-start gap-2">
                  <VolumeX className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                  <div>
                    <strong>Synthetic Voice Blocked:</strong> To prevent misleading acoustic hallucinations in tribal classrooms, unverified synthetic TTS is strictly barred. Only vetted human recordings are played aloud.
                  </div>
                </div>
              )}
            </div>

            {/* Pedagogical Guidance Tip */}
            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-xs space-y-1">
              <span className="font-semibold text-slate-300 flex items-center gap-1.5">
                <Info className="w-3.5 h-3.5 text-indigo-400" />
                Teacher Implementation Tip:
              </span>
              <p className="text-slate-400 leading-relaxed">
                {selectedPhrase.pedagogicalContext}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
