import type { Metadata } from "next";
import "./globals.css";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";

export const metadata: Metadata = {
  title: "SIH26042 Co-Teacher | AI-Powered Vernacular Pedagogy Platform",
  description:
    "Smart India Hackathon 2024 (SIH26042) - Offline-first vernacular pedagogy and real-time translation tool for mother-tongue-based primary education (Santali, Mundari, Ho).",
  keywords: [
    "SIH26042",
    "Smart India Hackathon",
    "Santali",
    "Ol Chiki",
    "Mundari",
    "Ho",
    "FLN",
    "NIPUN Bharat",
    "Offline ASR",
    "Tribal Education",
  ],
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen flex flex-col bg-slate-950 text-slate-100 antialiased selection:bg-indigo-500 selection:text-white">
        <Navbar />
        <main className="flex-1 w-full">{children}</main>
        <Footer />
      </body>
    </html>
  );
}
