import React from "react";
import Link from "next/link";
import { Github, Shield, Radio, Heart } from "lucide-react";

export default function Footer() {
  return (
    <footer className="border-t border-slate-800/80 bg-slate-950 text-slate-400 text-xs">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Column 1: Mission */}
          <div className="md:col-span-2 space-y-3">
            <div className="flex items-center gap-2">
              <span className="font-bold text-white text-sm tracking-tight">
                SIH26042 Co-Teacher
              </span>
              <span className="rounded bg-emerald-500/10 px-2 py-0.5 text-[10px] font-medium text-emerald-400 border border-emerald-500/20">
                100% Offline Edge Architecture
              </span>
            </div>
            <p className="text-slate-400 max-w-md text-xs leading-relaxed">
              AI-Powered Vernacular Pedagogy and Real-Time Translation Tool for
              Mother-Tongue-Based Primary Education. Engineered for low-resource tribal classrooms
              in Jharkhand, Odisha, and West Bengal (Santali, Mundari, Ho).
            </p>
            <div className="flex items-center gap-4 pt-1">
              <span className="text-[11px] text-slate-500 flex items-center gap-1.5">
                <Shield className="w-3.5 h-3.5 text-indigo-400" />
                Zero Cloud Dependency in Classrooms
              </span>
              <span className="text-[11px] text-slate-500 flex items-center gap-1.5">
                <Radio className="w-3.5 h-3.5 text-emerald-400" />
                Deterministic Curated Fast-Path
              </span>
            </div>
          </div>

          {/* Column 2: Navigation */}
          <div className="space-y-2">
            <h4 className="font-semibold text-slate-200 text-xs tracking-wider uppercase">
              Demonstration
            </h4>
            <ul className="space-y-1.5">
              <li>
                <Link href="/demo" className="hover:text-indigo-400 transition-colors">
                  Classroom Workflow Demo
                </Link>
              </li>
              <li>
                <Link href="/lessons" className="hover:text-indigo-400 transition-colors">
                  NIPUN / FLN Lessons
                </Link>
              </li>
              <li>
                <Link href="/worksheets" className="hover:text-indigo-400 transition-colors">
                  Worksheet Engine
                </Link>
              </li>
              <li>
                <Link href="/flashcards" className="hover:text-indigo-400 transition-colors">
                  Interactive Flashcards
                </Link>
              </li>
              <li>
                <Link href="/language-packs" className="hover:text-indigo-400 transition-colors">
                  Language Packs (Santali/Mundari/Ho)
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 3: Forensic & Technical */}
          <div className="space-y-2">
            <h4 className="font-semibold text-slate-200 text-xs tracking-wider uppercase">
              Engineering & Validation
            </h4>
            <ul className="space-y-1.5">
              <li>
                <Link href="/architecture" className="hover:text-indigo-400 transition-colors">
                  System Architecture
                </Link>
              </li>
              <li>
                <Link href="/metrics" className="hover:text-indigo-400 transition-colors">
                  Forensic Metrics (ASR & MT)
                </Link>
              </li>
              <li>
                <Link href="/about" className="hover:text-indigo-400 transition-colors">
                  About Project & Tech Stack
                </Link>
              </li>
              <li>
                <a
                  href="https://github.com/n1tishxydv/sih26042"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-indigo-400 transition-colors flex items-center gap-1"
                >
                  <Github className="w-3.5 h-3.5" />
                  GitHub Repository
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Banner */}
        <div className="mt-8 pt-6 border-t border-slate-800/60 flex flex-col sm:flex-row items-center justify-between gap-4 text-[11px] text-slate-500">
          <div>
            Smart India Hackathon 2024 &bull; National Level Finalist &bull; Problem Statement SIH26042
          </div>
          <div className="flex items-center gap-1">
            Built with rigorous engineering honesty &bull; Preserving <span className="font-olchiki text-slate-300">ᱥᱟᱱᱛᱟᱲᱤ</span> culture
          </div>
        </div>
      </div>
    </footer>
  );
}
