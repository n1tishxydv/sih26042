"use client";

import React, { useState } from "react";
import { SAMPLE_WORKSHEETS } from "@/lib/data";
import {
  FileText,
  CheckCircle2,
  XCircle,
  Printer,
  Sparkles,
  HelpCircle,
  RotateCcw,
} from "lucide-react";

export default function WorksheetsPage() {
  const currentWorksheet = SAMPLE_WORKSHEETS[0];
  const [selectedAnswers, setSelectedAnswers] = useState<Record<number, string>>({});
  const [submitted, setSubmitted] = useState<boolean>(false);

  const handleSelectOption = (qNum: number, label: string) => {
    if (submitted) return;
    setSelectedAnswers((prev) => ({
      ...prev,
      [qNum]: label,
    }));
  };

  const handleReset = () => {
    setSelectedAnswers({});
    setSubmitted(false);
  };

  const score = currentWorksheet.questions.reduce((acc, q) => {
    const chosen = selectedAnswers[q.qNum];
    const correctOpt = q.options.find((o) => o.isCorrect);
    if (chosen && correctOpt && chosen === correctOpt.label) {
      return acc + 1;
    }
    return acc;
  }, 0);

  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto space-y-8">
      {/* Page Header */}
      <div className="border-b border-slate-800 pb-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-indigo-500/10 border border-indigo-500/30 text-indigo-400 mb-2">
            <FileText className="w-3.5 h-3.5" />
            Classroom Assessment Engine
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">
            NIPUN Bilingual Worksheet Preview
          </h1>
          <p className="text-sm text-slate-400 mt-1 max-w-2xl leading-relaxed">
            Multi-lingual classroom drill sheets printable for rural learners.
            Supports counting drills and object recognition in Ol Chiki (<span className="font-olchiki text-emerald-400">ᱥᱟᱱᱛᱟᱲᱤ</span>) alongside Hindi.
          </p>
        </div>

        <button
          type="button"
          onClick={() => window.print()}
          className="inline-flex items-center gap-2 rounded-lg bg-slate-900 border border-slate-700 px-4 py-2 text-xs font-semibold text-slate-200 hover:bg-slate-800 hover:text-white transition-all shadow-sm"
        >
          <Printer className="w-4 h-4 text-indigo-400" />
          Print / PDF Export
        </button>
      </div>

      {/* Main Worksheet Sheet Canvas */}
      <div className="glass-panel rounded-2xl p-6 sm:p-10 border border-slate-800 bg-slate-950/90 shadow-2xl space-y-8">
        {/* Worksheet Header Header Strip */}
        <div className="border-b-2 border-slate-800 pb-6 flex flex-wrap items-center justify-between gap-4">
          <div>
            <span className="text-[11px] font-bold text-indigo-400 uppercase tracking-wider block">
              {currentWorksheet.grade} &bull; {currentWorksheet.competency}
            </span>
            <h2 className="text-xl sm:text-2xl font-black text-white mt-1">
              {currentWorksheet.title}
            </h2>
          </div>

          <div className="flex items-center gap-3">
            <div className="text-right text-xs">
              <span className="text-slate-400 block">Student: ___________________</span>
              <span className="text-slate-400 block mt-1">Date: ___________________</span>
            </div>
          </div>
        </div>

        {/* Questions Loop */}
        <div className="space-y-8">
          {currentWorksheet.questions.map((q) => {
            const chosen = selectedAnswers[q.qNum];
            const correctOpt = q.options.find((o) => o.isCorrect);
            const isAnswered = Boolean(chosen);
            const isCorrect = chosen === correctOpt?.label;

            return (
              <div
                key={q.qNum}
                className="p-5 sm:p-6 rounded-xl bg-slate-900/50 border border-slate-800/80 space-y-4"
              >
                {/* Question Prompts */}
                <div className="space-y-1">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
                    <span className="flex h-5 w-5 items-center justify-center rounded-full bg-indigo-600/30 text-indigo-300 text-xs font-bold border border-indigo-500/40">
                      {q.qNum}
                    </span>
                    <span>Prompt (Hindi):</span>
                  </div>
                  <div className="text-base font-semibold text-slate-200 pl-7">
                    {q.promptHindi}
                  </div>
                </div>

                {/* Ol Chiki Translation Prompt */}
                <div className="p-3.5 rounded-lg bg-emerald-950/20 border border-emerald-500/20 space-y-1">
                  <span className="text-[10px] font-semibold text-emerald-400 uppercase tracking-wider block">
                    Santali (Ol Chiki):
                  </span>
                  <div className="font-olchiki text-xl sm:text-2xl font-bold text-white">
                    {q.promptSantaliOlChiki}
                  </div>
                  <div className="text-xs text-slate-400">
                    {q.promptTransliteration}
                  </div>
                </div>

                {/* Multiple Choice Options */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
                  {q.options.map((opt) => {
                    const isSelected = chosen === opt.label;
                    let optionStyle =
                      "bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700 hover:bg-slate-900";

                    if (submitted) {
                      if (opt.isCorrect) {
                        optionStyle =
                          "bg-emerald-950/80 border-emerald-500 text-emerald-200 font-bold";
                      } else if (isSelected && !opt.isCorrect) {
                        optionStyle =
                          "bg-red-950/80 border-red-500 text-red-200";
                      }
                    } else if (isSelected) {
                      optionStyle =
                        "bg-indigo-600/30 border-indigo-500 text-white shadow";
                    }

                    return (
                      <button
                        key={opt.label}
                        type="button"
                        onClick={() => handleSelectOption(q.qNum, opt.label)}
                        className={`p-3.5 rounded-xl border text-left flex items-center justify-between transition-all ${optionStyle}`}
                      >
                        <div className="space-y-0.5">
                          <span className="text-xs font-bold text-slate-400 mr-2">
                            ({opt.label})
                          </span>
                          <span className="font-olchiki text-lg text-white font-extrabold mr-2">
                            {opt.olChiki}
                          </span>
                          <span className="text-xs text-slate-400 block sm:inline">
                            {opt.hindi}
                          </span>
                        </div>
                        {submitted && opt.isCorrect && (
                          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                        )}
                        {submitted && isSelected && !opt.isCorrect && (
                          <XCircle className="w-4 h-4 text-red-400 shrink-0" />
                        )}
                      </button>
                    );
                  })}
                </div>

                {/* Feedback & Explanation */}
                {submitted && (
                  <div
                    className={`p-3 rounded-lg text-xs flex items-start gap-2 ${
                      isCorrect
                        ? "bg-emerald-950/30 border border-emerald-500/30 text-emerald-300"
                        : "bg-red-950/30 border border-red-500/30 text-red-300"
                    }`}
                  >
                    <HelpCircle className="w-4 h-4 shrink-0 mt-0.5" />
                    <div>
                      <span className="font-bold">
                        {isCorrect ? "Correct!" : "Incorrect."}
                      </span>{" "}
                      {q.explanation}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Action Buttons & Results */}
        <div className="pt-6 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            {submitted ? (
              <div className="text-sm font-bold text-white flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-emerald-400" />
                Score: {score} / {currentWorksheet.questions.length} Correct
              </div>
            ) : (
              <span className="text-xs text-slate-400">
                {Object.keys(selectedAnswers).length} of{" "}
                {currentWorksheet.questions.length} questions answered
              </span>
            )}
          </div>

          <div className="flex items-center gap-3">
            {submitted ? (
              <button
                type="button"
                onClick={handleReset}
                className="inline-flex items-center gap-1.5 rounded-lg bg-slate-800 px-4 py-2 text-xs font-semibold text-slate-200 hover:bg-slate-700"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Try Again
              </button>
            ) : (
              <button
                type="button"
                onClick={() => setSubmitted(true)}
                disabled={
                  Object.keys(selectedAnswers).length !==
                  currentWorksheet.questions.length
                }
                className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 px-5 py-2 text-xs font-bold text-white shadow hover:bg-indigo-500 disabled:opacity-40 disabled:pointer-events-none transition-all"
              >
                Submit & Grade Worksheet
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
