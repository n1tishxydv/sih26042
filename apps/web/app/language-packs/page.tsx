"use client";

import React, { useEffect, useState } from "react";
import { LANGUAGE_PACKS, LanguagePackInfo } from "@/lib/data";
import { fetchRemotePacks, PackSummaryResponse, API_BASE_URL } from "@/lib/api";
import {
  Layers,
  ShieldCheck,
  AlertCircle,
  FileCode,
  Download,
  Cpu,
  Volume2,
  HardDrive,
  RefreshCw,
  Server,
} from "lucide-react";

export default function LanguagePacksPage() {
  const [remotePacks, setRemotePacks] = useState<PackSummaryResponse[] | null>(null);
  const [loadingRemote, setLoadingRemote] = useState<boolean>(true);
  const [selectedPack, setSelectedPack] = useState<LanguagePackInfo>(LANGUAGE_PACKS[0]);

  const loadPacks = async () => {
    setLoadingRemote(true);
    const data = await fetchRemotePacks();
    setRemotePacks(data);
    setLoadingRemote(false);
  };

  useEffect(() => {
    loadPacks();
  }, []);

  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="border-b border-slate-800 pb-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-indigo-500/10 border border-indigo-500/30 text-indigo-400 mb-2">
            <Layers className="w-3.5 h-3.5" />
            Extensible Language Pack Registry (.slp)
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">
            Vernacular Language Packs
          </h1>
          <p className="text-sm text-slate-400 max-w-3xl mt-1.5 leading-relaxed">
            Pluggable, cryptographically signed offline packages. The runtime engine is language-agnostic; adding support for a new tribal language only requires compiling a verified <code className="text-indigo-300 font-mono">.slp</code> archive without recompiling the Android APK.
          </p>
        </div>

        {/* Remote Sync Button */}
        <button
          type="button"
          onClick={loadPacks}
          disabled={loadingRemote}
          className="inline-flex items-center gap-2 rounded-lg bg-slate-900 border border-slate-700 px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition-colors shadow-sm disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 text-indigo-400 ${loadingRemote ? "animate-spin" : ""}`} />
          {loadingRemote ? "Syncing..." : "Sync Control Plane"}
        </button>
      </div>

      {/* Backend Remote Sync Status Bar */}
      <div className="glass-panel p-4 rounded-xl border border-slate-800 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2.5">
          <Server className="w-4 h-4 text-indigo-400 shrink-0" />
          <span className="text-slate-300">
            Control Plane URL: <code className="text-indigo-300 font-mono">{API_BASE_URL}</code>
          </span>
        </div>

        <div>
          {loadingRemote ? (
            <span className="text-slate-400 flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-slate-400 animate-pulse" />
              Polling remote registry...
            </span>
          ) : remotePacks && remotePacks.length > 0 ? (
            <span className="text-emerald-400 flex items-center gap-1.5 font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-400" />
              Connected: {remotePacks.length} active pack(s) indexed on cloud
            </span>
          ) : (
            <span className="text-amber-400 flex items-center gap-1.5 font-medium">
              <span className="w-2 h-2 rounded-full bg-amber-400" />
              Backend offline or unseeded &bull; Displaying local embedded manifest
            </span>
          )}
        </div>
      </div>

      {/* Main Pack Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {LANGUAGE_PACKS.map((pack) => {
          const isSelected = selectedPack.id === pack.id;
          const isFull = pack.status === "FULL";

          return (
            <div
              key={pack.id}
              role="button"
              tabIndex={0}
              onClick={() => setSelectedPack(pack)}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  setSelectedPack(pack);
                }
              }}
              className={`text-left rounded-2xl p-6 border transition-all cursor-pointer relative flex flex-col justify-between ${
                isSelected
                  ? "bg-slate-900 border-indigo-500 shadow-xl shadow-indigo-500/10 ring-1 ring-indigo-500"
                  : "bg-slate-950/80 border-slate-800 hover:border-slate-700 hover:bg-slate-900/60"
              }`}
            >
              <div>
                {/* Top Badge: FULL vs STUB */}
                <div className="flex items-center justify-between mb-3">
                  <span className="font-mono text-xs text-slate-400">
                    {pack.code.toUpperCase()} &bull; v{pack.version}
                  </span>
                  {isFull ? (
                    <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/40">
                      <ShieldCheck className="w-3 h-3" /> FULL PACK (VALIDATED)
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[10px] font-bold bg-amber-950/60 text-amber-300 border border-amber-500/40">
                      <AlertCircle className="w-3 h-3" /> ARCHITECTURAL STUB
                    </span>
                  )}
                </div>

                {/* Names */}
                <h2 className="text-xl font-black text-white flex items-center gap-2">
                  {pack.name}
                  <span className="font-olchiki text-emerald-400 font-normal text-lg">
                    ({pack.nativeName})
                  </span>
                </h2>
                <div className="text-xs text-slate-400 mt-1">
                  Script: <span className="text-slate-200 font-medium">{pack.script}</span> ({pack.scriptIso})
                </div>

                <p className="text-xs text-slate-400 mt-3 leading-relaxed">
                  {pack.description}
                </p>

                {/* Quantitative Asset Statistics */}
                <div className="grid grid-cols-2 gap-2 mt-5 text-[11px] pt-4 border-t border-slate-800/80">
                  <div className="bg-slate-900/80 p-2 rounded-lg border border-slate-800">
                    <span className="text-slate-500 block">Classroom Phrases:</span>
                    <span className="font-bold text-white text-xs">{pack.phrasesCount}</span>
                  </div>
                  <div className="bg-slate-900/80 p-2 rounded-lg border border-slate-800">
                    <span className="text-slate-500 block">FLN Vocabulary:</span>
                    <span className="font-bold text-white text-xs">{pack.flnVocabCount}</span>
                  </div>
                  <div className="bg-slate-900/80 p-2 rounded-lg border border-slate-800">
                    <span className="text-slate-500 block">Native Audio WAVs:</span>
                    <span className="font-bold text-emerald-400 text-xs">{pack.audioFilesCount}</span>
                  </div>
                  <div className="bg-slate-900/80 p-2 rounded-lg border border-slate-800">
                    <span className="text-slate-500 block">Worksheets & Acts:</span>
                    <span className="font-bold text-white text-xs">
                      {pack.worksheetsCount + pack.activitiesCount}
                    </span>
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-800 flex items-center justify-between text-xs">
                <span className="text-indigo-400 font-medium">
                  {isSelected ? "Active Inspection" : "Click to Inspect Spec"}
                </span>
                <span className="text-slate-500 text-[10px]">{pack.unicodeRange}</span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Selected Pack Deep-Dive Specification Sheet */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 bg-slate-900/40 space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-4">
          <div>
            <span className="text-xs uppercase font-bold text-slate-400 tracking-wider">
              Formal Manifest Inspector
            </span>
            <h2 className="text-2xl font-black text-white mt-0.5">
              {selectedPack.name} Language Pack Specification
            </h2>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs font-mono text-slate-400 bg-slate-950 px-3 py-1 rounded-lg border border-slate-800">
              {selectedPack.id}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs">
          {/* Models & AI Engine Configuration */}
          <div className="p-5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-3">
            <h3 className="font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5 text-xs">
              <Cpu className="w-4 h-4 text-indigo-400" />
              Edge AI Models Assigned in Manifest
            </h3>
            <div className="space-y-2 text-slate-300">
              <div>
                <span className="text-slate-500 block text-[10px]">ASR Acoustic Model:</span>
                <span className="font-mono text-emerald-300">{selectedPack.models.asr}</span>
              </div>
              <div>
                <span className="text-slate-500 block text-[10px]">MT Translation Model:</span>
                <span className="font-mono text-indigo-300">{selectedPack.models.mt}</span>
              </div>
              <div>
                <span className="text-slate-500 block text-[10px]">TTS Synthesis Model:</span>
                <span className="font-mono text-amber-300">{selectedPack.models.tts}</span>
              </div>
            </div>
          </div>

          {/* Linguistic Provenance & Validation */}
          <div className="p-5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-3">
            <h3 className="font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5 text-xs">
              <HardDrive className="w-4 h-4 text-emerald-400" />
              Linguistic Validation & Provenance
            </h3>
            <p className="text-slate-300 leading-relaxed">
              {selectedPack.provenance}
            </p>
            <div className="pt-2 border-t border-slate-800/80 text-[11px] text-slate-400 space-y-1">
              <div>
                <strong>Script Standard:</strong> {selectedPack.script} ({selectedPack.scriptIso})
              </div>
              <div>
                <strong>Unicode Allocation:</strong> {selectedPack.unicodeRange}
              </div>
            </div>
          </div>
        </div>

        {/* Technical Pack Container Breakdown */}
        <div className="p-5 rounded-xl bg-slate-950/50 border border-slate-800 space-y-2">
          <h4 className="font-bold text-slate-300 text-xs flex items-center gap-2">
            <FileCode className="w-4 h-4 text-indigo-400" />
            Smart Language Pack (.slp) Container Architecture
          </h4>
          <p className="text-xs text-slate-400 leading-relaxed">
            Every <code className="text-indigo-300">.slp</code> bundle is a deterministic ZIP archive verified by SHA-256 before extraction. It isolates assets into strictly validated schemas:
            <code className="text-slate-300 ml-1">manifest.json</code>,
            <code className="text-slate-300 ml-1">phrases.json</code>,
            <code className="text-slate-300 ml-1">fln_vocab.json</code>,
            <code className="text-slate-300 ml-1">worksheets.json</code>,
            <code className="text-slate-300 ml-1">activities.json</code>, and
            <code className="text-slate-300 ml-1">audio/*.wav</code>.
          </p>
        </div>
      </div>
    </div>
  );
}
