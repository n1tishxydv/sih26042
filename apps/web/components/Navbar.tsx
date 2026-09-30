"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  checkBackendHealth,
  HealthResponse,
  API_BASE_URL,
} from "@/lib/api";
import {
  Menu,
  X,
  Server,
  Activity,
  Layers,
  Sparkles,
  BookOpen,
  FileText,
  CreditCard,
  Cpu,
  BarChart3,
  Info,
} from "lucide-react";

const NAV_ITEMS = [
  { label: "Home", href: "/", icon: Sparkles },
  { label: "Live Demo", href: "/demo", icon: Activity },
  { label: "NIPUN Lessons", href: "/lessons", icon: BookOpen },
  { label: "Worksheets", href: "/worksheets", icon: FileText },
  { label: "Flashcards", href: "/flashcards", icon: CreditCard },
  { label: "Language Packs", href: "/language-packs", icon: Layers },
  { label: "Architecture", href: "/architecture", icon: Cpu },
  { label: "Metrics", href: "/metrics", icon: BarChart3 },
  { label: "About", href: "/about", icon: Info },
];

export default function Navbar() {
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [health, setHealth] = useState<{
    online: boolean;
    loading: boolean;
    data: HealthResponse | null;
  }>({
    online: false,
    loading: true,
    data: null,
  });

  useEffect(() => {
    let isMounted = true;
    const updateHealth = async () => {
      const res = await checkBackendHealth();
      if (isMounted) {
        setHealth({
          online: res.online,
          loading: false,
          data: res.data,
        });
      }
    };

    updateHealth();
    const interval = setInterval(updateHealth, 15000);
    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  return (
    <header className="sticky top-0 z-50 w-full border-b border-slate-800/80 bg-slate-950/85 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Brand / Logo */}
        <div className="flex items-center gap-3">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-tr from-indigo-600 to-emerald-500 text-white font-bold shadow-md shadow-indigo-500/20 group-hover:scale-105 transition-transform">
              S
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-bold tracking-tight text-white text-base">SIH26042</span>
                <span className="rounded bg-indigo-500/10 px-1.5 py-0.5 text-[10px] font-medium text-indigo-400 border border-indigo-500/20">
                  Co-Teacher
                </span>
              </div>
              <p className="text-[10px] text-slate-400 hidden sm:block">
                Mother-Tongue FLN Pedagogy Platform
              </p>
            </div>
          </Link>
        </div>

        {/* Desktop Navigation */}
        <nav className="hidden xl:flex items-center space-x-1">
          {NAV_ITEMS.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 ${
                  isActive
                    ? "bg-indigo-600/15 text-indigo-400 border border-indigo-500/30"
                    : "text-slate-300 hover:text-white hover:bg-slate-800/60"
                }`}
              >
                <item.icon className="w-3.5 h-3.5 opacity-80" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* Live Backend Indicator & Mobile Toggle */}
        <div className="flex items-center gap-3">
          {/* Backend Status Badge */}
          <div
            className={`hidden md:flex items-center gap-2 rounded-full px-2.5 py-1 text-[11px] font-medium border ${
              health.loading
                ? "bg-slate-800/60 border-slate-700 text-slate-400"
                : health.online
                ? "bg-emerald-950/60 border-emerald-500/40 text-emerald-400"
                : "bg-amber-950/50 border-amber-500/40 text-amber-300"
            }`}
            title={`Backend URL: ${API_BASE_URL} (${health.online ? "Online" : "Offline / Unreachable"})`}
          >
            <span
              className={`h-2 w-2 rounded-full ${
                health.loading
                  ? "bg-slate-400 animate-pulse"
                  : health.online
                  ? "bg-emerald-400 animate-ping shadow-[0_0_8px_#34d399]"
                  : "bg-amber-400"
              }`}
            />
            <span className="flex items-center gap-1">
              <Server className="w-3 h-3" />
              {health.loading ? (
                "Connecting..."
              ) : health.online ? (
                <span className="font-semibold">Backend ONLINE</span>
              ) : (
                <span>Backend OFFLINE / UNAVAILABLE</span>
              )}
            </span>
          </div>

          {/* Quick CTA to Android APK / Live Demo */}
          <Link
            href="/demo"
            className="hidden sm:inline-flex items-center justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-indigo-500 transition-colors"
          >
            Launch Demo
          </Link>

          {/* Mobile hamburger button */}
          <button
            type="button"
            className="inline-flex xl:hidden items-center justify-center rounded-md p-2 text-slate-400 hover:bg-slate-800 hover:text-white"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="xl:hidden border-b border-slate-800 bg-slate-950 px-4 pt-2 pb-6 space-y-2">
          {/* Mobile Status */}
          <div className="pb-3 mb-2 border-b border-slate-800 flex items-center justify-between">
            <span className="text-xs text-slate-400">Control Plane Status:</span>
            <span
              className={`text-xs font-medium px-2 py-0.5 rounded-full border ${
                health.online
                  ? "bg-emerald-950/60 border-emerald-500/40 text-emerald-400"
                  : "bg-amber-950/50 border-amber-500/40 text-amber-300"
              }`}
            >
              {health.online ? "Backend ONLINE" : "Backend OFFLINE / UNAVAILABLE"}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-2">
            {NAV_ITEMS.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center gap-2 px-3 py-2 text-xs font-medium rounded-lg transition-colors ${
                    isActive
                      ? "bg-indigo-600 text-white"
                      : "text-slate-300 hover:bg-slate-900 hover:text-white"
                  }`}
                >
                  <item.icon className="w-4 h-4 opacity-75" />
                  {item.label}
                </Link>
              );
            })}
          </div>

          <div className="pt-3">
            <Link
              href="/demo"
              onClick={() => setMobileMenuOpen(false)}
              className="block w-full text-center rounded-lg bg-indigo-600 px-4 py-2.5 text-xs font-bold text-white shadow hover:bg-indigo-500"
            >
              Explore Classroom Live Demo
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
