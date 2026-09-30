"use client";

import React, { useState } from "react";
import { SAMPLE_LESSONS, NipunLesson } from "@/lib/data";
import {
  BookOpen,
  CheckCircle2,
  Volume2,
  Sparkles,
  Award,
  Layers,
  GraduationCap,
} from "lucide-react";

export default function LessonsPage() {
  const [activeLesson, setActiveLesson] = useState<NipunLesson>(SAMPLE_LESSONS[0]);
  const [activePhaseIndex, setActivePhaseIndex] = useState<number>(0);

  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto space-y-8">
      {/* Page Header */}
      <div className="border-b border-slate-800 pb-6">
        <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 mb-2">
          <GraduationCap className="w-3.5 h-3.5" />
          NIPUN Bharat &bull; Foundational Literacy & Numeracy (FLN)
        </div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">
          Bilingual Primary Pedagogy Lessons
        </h1>
        <p className="text-sm text-slate-400 max-w-3xl mt-1.5 leading-relaxed">
          Structured 15-minute bridge lessons designed for multi-lingual rural classrooms.
          Enables teachers to instruct in standard Hindi while providing immediate, authentic
          Santali mother-tongue scaffolding in Ol Chiki (<span className="font-olchiki text-emerald-400">ᱥᱟᱱᱛᱟᱲᱤ</span>).
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Lesson Selector */}
        <div className="lg:col-span-4 space-y-4">
          <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <Layers className="w-4 h-4 text-indigo-400" />
            Curriculum Units
          </h2>

          <div className="space-y-3">
            {SAMPLE_LESSONS.map((lesson) => {
              const isSelected = activeLesson.id === lesson.id;
              return (
                <button
                  key={lesson.id}
                  type="button"
                  onClick={() => {
                    setActiveLesson(lesson);
                    setActivePhaseIndex(0);
                  }}
                  className={`w-full text-left p-4 rounded-xl border transition-all ${
                    isSelected
                      ? "bg-indigo-600/20 border-indigo-500 shadow-md text-white"
                      : "bg-slate-900/60 border-slate-800 text-slate-300 hover:bg-slate-800/80"
                  }`}
                >
                  <div className="flex items-center justify-between text-[11px] mb-1">
                    <span className="text-indigo-400 font-semibold">{lesson.grade}</span>
                    <span className="bg-slate-800 px-2 py-0.5 rounded text-slate-400 text-[10px]">
                      {lesson.subject}
                    </span>
                  </div>
                  <h3 className="font-bold text-sm text-slate-100">{lesson.title}</h3>
                  <p className="text-xs text-slate-400 line-clamp-2 mt-1">
                    {lesson.summary}
                  </p>
                </button>
              );
            })}
          </div>

          {/* Pedagogy Note */}
          <div className="glass-panel p-4 rounded-xl border border-slate-800 text-xs text-slate-400 space-y-2">
            <div className="font-semibold text-slate-200 flex items-center gap-1.5">
              <Award className="w-4 h-4 text-amber-400" />
              FLN Bridging Philosophy
            </div>
            <p className="leading-relaxed">
              Children learn best when initial conceptual associations are anchored in their mother tongue before transitioning to state languages. The Co-Teacher prevents the early cognitive shock of unfamiliar Hindi medium instruction.
            </p>
          </div>
        </div>

        {/* Lesson Stepper & Visualizer */}
        <div className="lg:col-span-8 space-y-6">
          <div className="glass-panel rounded-2xl p-6 sm:p-8 border border-slate-800 bg-slate-900/40 space-y-6">
            {/* Header with Competency */}
            <div className="border-b border-slate-800 pb-4">
              <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
                <span className="text-xs font-semibold text-emerald-400 bg-emerald-950/60 px-2.5 py-1 rounded-full border border-emerald-500/30">
                  {activeLesson.subject} &bull; {activeLesson.grade}
                </span>
                <span className="text-xs text-slate-400 font-medium">
                  {activeLesson.phases.length} Classroom Phases
                </span>
              </div>
              <h2 className="text-xl sm:text-2xl font-bold text-white">
                {activeLesson.title}
              </h2>
              <div className="mt-2 text-xs text-slate-300 bg-slate-950/60 p-2.5 rounded border border-slate-800 flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-indigo-400 shrink-0 mt-0.5" />
                <span>
                  <strong>Target Competency:</strong> {activeLesson.targetCompetency}
                </span>
              </div>
            </div>

            {/* Stepper Tabs */}
            <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-slate-800">
              {activeLesson.phases.map((phase, idx) => {
                const isActive = activePhaseIndex === idx;
                return (
                  <button
                    key={phase.name}
                    type="button"
                    onClick={() => setActivePhaseIndex(idx)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors flex items-center gap-1.5 ${
                      isActive
                        ? "bg-indigo-600 text-white shadow"
                        : "bg-slate-950 text-slate-400 hover:bg-slate-800 hover:text-slate-200"
                    }`}
                  >
                    <span>{phase.name}</span>
                  </button>
                );
              })}
            </div>

            {/* Active Phase Content */}
            {activeLesson.phases[activePhaseIndex] && (
              <div className="space-y-6">
                {/* Teacher Directive in Hindi */}
                <div className="space-y-1.5">
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
                    Teacher Spoken Directive (Hindi):
                  </span>
                  <div className="text-base sm:text-lg font-bold text-slate-100 bg-slate-950/70 p-4 rounded-xl border border-slate-800">
                    &quot;{activeLesson.phases[activePhaseIndex].hindiDirective}&quot;
                  </div>
                </div>

                {/* Co-Teacher Bridge in Santali Ol Chiki */}
                <div className="space-y-2 p-5 rounded-xl bg-gradient-to-br from-indigo-950/40 via-slate-900 to-emerald-950/30 border border-slate-700/80">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-emerald-400 uppercase tracking-wider">
                      Co-Teacher Mother-Tongue Bridge (Ol Chiki)
                    </span>
                    <span className="text-[10px] bg-emerald-500/10 text-emerald-300 px-2 py-0.5 rounded border border-emerald-500/20 flex items-center gap-1">
                      <Volume2 className="w-3 h-3" /> Native Audio Verified
                    </span>
                  </div>
                  <div className="font-olchiki text-3xl sm:text-4xl font-extrabold text-white py-2">
                    {activeLesson.phases[activePhaseIndex].santaliOlChiki}
                  </div>
                  <div className="text-xs text-slate-300 pt-2 border-t border-slate-800">
                    <span className="text-slate-500">Transliteration:</span>{" "}
                    <span className="font-medium">
                      {activeLesson.phases[activePhaseIndex].santaliTransliteration}
                    </span>
                  </div>
                </div>

                {/* Expected Student Response / Action */}
                <div className="p-4 rounded-xl bg-slate-950/80 border border-slate-800 space-y-1.5">
                  <span className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                    Observed Classroom Student Response:
                  </span>
                  <p className="text-sm text-slate-300 leading-relaxed font-medium">
                    {activeLesson.phases[activePhaseIndex].studentAction}
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
