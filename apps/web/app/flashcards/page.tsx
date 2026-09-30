"use client";

import React, { useState } from "react";
import { SAMPLE_FLASHCARDS, FlashcardItem } from "@/lib/data";
import {
  CreditCard,
  Volume2,
  RotateCw,
  Sparkles,
  ChevronLeft,
  ChevronRight,
  Filter,
} from "lucide-react";

export default function FlashcardsPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("ALL");
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isFlipped, setIsFlipped] = useState<boolean>(false);
  const [isPlayingAudio, setIsPlayingAudio] = useState<boolean>(false);

  const categories = ["ALL", "NUMBERS", "BODY_PARTS", "ANIMALS", "CLASSROOM"];

  const filteredCards =
    selectedCategory === "ALL"
      ? SAMPLE_FLASHCARDS
      : SAMPLE_FLASHCARDS.filter((card) => card.category === selectedCategory);

  const activeCard: FlashcardItem = filteredCards[currentIndex] || filteredCards[0];

  const handleNext = () => {
    setIsFlipped(false);
    setCurrentIndex((prev) => (prev + 1) % filteredCards.length);
  };

  const handlePrev = () => {
    setIsFlipped(false);
    setCurrentIndex((prev) =>
      prev === 0 ? filteredCards.length - 1 : prev - 1
    );
  };

  const handlePlayAudio = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsPlayingAudio(true);
    setTimeout(() => {
      setIsPlayingAudio(false);
    }, activeCard.audioDurationMs || 400);
  };

  return (
    <div className="py-10 px-4 sm:px-6 lg:px-8 max-w-4xl mx-auto space-y-8">
      {/* Header */}
      <div className="border-b border-slate-800 pb-6 text-center max-w-2xl mx-auto">
        <div className="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 mb-2">
          <CreditCard className="w-3.5 h-3.5" />
          Interactive FLN Vocabulary Drill
        </div>
        <h1 className="text-3xl font-extrabold text-white tracking-tight">
          Santali Vocabulary Flashcards
        </h1>
        <p className="text-sm text-slate-400 mt-1.5 leading-relaxed">
          Tap the flashcard to flip between Hindi conceptual cues and Ol Chiki (<span className="font-olchiki text-emerald-400">ᱥᱟᱱᱛᱟᱲᱤ</span>)
          representations with native audio verification.
        </p>
      </div>

      {/* Category Filter Chips */}
      <div className="flex items-center justify-center gap-2 flex-wrap">
        <span className="text-xs text-slate-400 flex items-center gap-1 mr-1">
          <Filter className="w-3.5 h-3.5 text-indigo-400" />
          Topic:
        </span>
        {categories.map((cat) => (
          <button
            key={cat}
            type="button"
            onClick={() => {
              setSelectedCategory(cat);
              setCurrentIndex(0);
              setIsFlipped(false);
            }}
            className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
              selectedCategory === cat
                ? "bg-indigo-600 text-white shadow"
                : "bg-slate-900 border border-slate-800 text-slate-400 hover:text-slate-200"
            }`}
          >
            {cat.replace("_", " ")}
          </button>
        ))}
      </div>

      {/* Main Flashcard Container */}
      <div className="flex flex-col items-center justify-center space-y-6">
        <div
          role="button"
          tabIndex={0}
          onClick={() => setIsFlipped(!isFlipped)}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") {
              e.preventDefault();
              setIsFlipped(!isFlipped);
            }
          }}
          className="w-full max-w-lg aspect-[4/3] cursor-pointer perspective"
        >
          <div
            className={`relative w-full h-full rounded-3xl transition-transform duration-500 transform-style-3d shadow-2xl ${
              isFlipped ? "rotate-y-180" : ""
            }`}
            style={{
              transformStyle: "preserve-3d",
              transform: isFlipped ? "rotateY(180deg)" : "rotateY(0deg)",
              transition: "transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)",
            }}
          >
            {/* FRONT FACE (Hindi Prompt) */}
            <div
              className="absolute inset-0 w-full h-full rounded-3xl p-8 flex flex-col justify-between glass-panel border border-slate-700/80 bg-gradient-to-br from-slate-900 via-slate-950 to-indigo-950/40 backface-hidden"
              style={{ backfaceVisibility: "hidden" }}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-indigo-400 uppercase tracking-wider bg-indigo-500/10 px-2.5 py-1 rounded-full border border-indigo-500/20">
                  {activeCard.category.replace("_", " ")}
                </span>
                <span className="text-xs text-slate-500 font-medium">
                  {currentIndex + 1} / {filteredCards.length}
                </span>
              </div>

              <div className="text-center py-6">
                <span className="text-xs text-slate-400 block mb-1 uppercase tracking-wider">
                  Hindi Word
                </span>
                <div className="text-3xl sm:text-4xl font-extrabold text-white">
                  {activeCard.hindiWord}
                </div>
              </div>

              <div className="flex items-center justify-between text-xs text-slate-400 pt-4 border-t border-slate-800">
                <span className="flex items-center gap-1.5 text-indigo-400 font-medium">
                  <RotateCw className="w-3.5 h-3.5" />
                  Tap card to reveal Ol Chiki
                </span>
                <span className="text-[11px] text-slate-500">SIH26042 Primary Kit</span>
              </div>
            </div>

            {/* BACK FACE (Ol Chiki Mother-Tongue) */}
            <div
              className="absolute inset-0 w-full h-full rounded-3xl p-8 flex flex-col justify-between glass-panel border border-emerald-500/40 bg-gradient-to-br from-slate-950 via-slate-900 to-emerald-950/50 backface-hidden"
              style={{
                backfaceVisibility: "hidden",
                transform: "rotateY(180deg)",
              }}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-emerald-400 uppercase tracking-wider bg-emerald-500/10 px-2.5 py-1 rounded-full border border-emerald-500/30">
                  Santali &bull; Ol Chiki
                </span>
                <button
                  type="button"
                  onClick={handlePlayAudio}
                  className="flex items-center gap-1.5 text-xs font-bold bg-emerald-600/30 hover:bg-emerald-600 text-emerald-300 hover:text-white px-3 py-1 rounded-full border border-emerald-500/40 transition-colors"
                  title="Simulate Native Audio"
                >
                  <Volume2 className={`w-3.5 h-3.5 ${isPlayingAudio ? "animate-pulse" : ""}`} />
                  {isPlayingAudio ? "Playing..." : "Listen"}
                </button>
              </div>

              <div className="text-center py-2">
                <div className="font-olchiki text-4xl sm:text-5xl font-black text-white tracking-wider my-2">
                  {activeCard.olChiki}
                </div>
                <div className="text-sm font-semibold text-emerald-300">
                  {activeCard.transliterationLatin}
                </div>
                <div className="text-xs text-slate-400 mt-0.5">
                  Devanagari: {activeCard.transliterationDevanagari}
                </div>
              </div>

              <div className="pt-3 border-t border-slate-800 text-xs text-slate-400 flex items-center justify-between">
                <span>{activeCard.notes}</span>
                <span className="flex items-center gap-1 text-slate-500">
                  <RotateCw className="w-3.5 h-3.5" /> Flip
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Carousel Navigation Buttons */}
        <div className="flex items-center gap-4">
          <button
            type="button"
            onClick={handlePrev}
            className="flex items-center gap-1.5 rounded-lg bg-slate-900 border border-slate-700 px-4 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition-all shadow"
          >
            <ChevronLeft className="w-4 h-4" />
            Previous
          </button>

          <span className="text-xs font-medium text-slate-400">
            Card {currentIndex + 1} of {filteredCards.length}
          </span>

          <button
            type="button"
            onClick={handleNext}
            className="flex items-center gap-1.5 rounded-lg bg-slate-900 border border-slate-700 px-4 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition-all shadow"
          >
            Next
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
