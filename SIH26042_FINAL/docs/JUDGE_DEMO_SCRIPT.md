# SIH26042 Official Judge Demonstration Script & Presentation Guide

## 1. 30-Second Elevator Pitch (The Problem & Solution)

> "Over 8 million tribal primary school children across India enter Grade 1 speaking only their mother tongue — such as Santali, Ho, or Mundari. Most primary teachers assigned to these schools do not speak the local tribal language. Existing translation tools fail because remote tribal schools have **zero internet connectivity**, and generic cloud translation tools do not support native scripts like **Ol Chiki** or provide age-appropriate classroom instructions.  
> **SIH26042 is a 100% offline AI Classroom Co-Teacher**. It runs entirely on low-cost 2 GB Android tablets, recognizes teacher speech in real-time, and instantly responds with **verified native Santali voice audio** and NIPUN Bharat foundational learning activities — with sub-1-second latency and zero cloud dependency."

---

## 2. 2-Minute Technical Explanation (Architecture & Engineering)

> "Our system is built on four core engineering pillars:
> 
> 1. **Dual-Track AI Pipeline**: We recognize speech using an on-device int8 quantized **Sherpa-ONNX streaming Zipformer** model consuming only 34 MB of RAM. For foundational classroom interactions, recognized speech is mapped to a verified intent and plays back **49 authentic 16 kHz native Santali audio recordings** with sub-1-second latency.
> 
> 2. **Scientific Honesty & Graceful Fallback**: When a teacher speaks an unmapped sentence, our system falls back to an offline hybrid dictionary and Ol Chiki transliterator. Unlike other tools that fake neural perfection or synthesize synthetic hallucinations, we explicitly label machine output as **`RULE-BASED / PHONETIC`** with an orange badge, and disable audio with an explicit **`AUDIO UNAVAILABLE`** label because no validated offline Santali TTS exists.
> 
> 3. **2 GB Android Target Hardening**: We engineered the app to run on entry-level Android 11 Go devices. Total peak PSS memory is **91.9 MB** — well below the 192 MB heap limit — leaving over 650 MB of free RAM on the device.
> 
> 4. **Modular Language Packs (`.slp`)**: Curriculum content, Ol Chiki fonts, audio recordings, and worksheets are packaged into cryptographically signed, versioned `.slp` archives that can be validated, updated, and swapped atomically offline."

---

## 3. 5-Minute Live Demonstration Flow

### Step 1: Proof of Offline Environment (30 seconds)
1. Pick up the test device (or Android emulator).
2. Swipe down the Android status bar: show **Airplane Mode ENABLED**, **Wi-Fi DISABLED**, **Mobile Data DISABLED**.
3. Open the **Classroom Co-Teacher** application. Point out the top badge: `● 100% OFFLINE READY`.

### Step 2: The Verified Classroom Fast Path (90 seconds)
1. Tap the purple card: **`⚖️ SIH JUDGE DEMO MODE`** (or go to `START LIVE CLASSROOM MIC`).
2. Show Step 1: Class 2 Santali Foundational Numeracy scenario.
3. Tap **PROCEED TO LIVE VOICE PIPELINE**.
4. Speak or trigger canonical input: **`"एक से पांच तक गिनो"`** (Count from 1 to 5).
5. Highlight what happens in real-time:
   - Real-time Zipformer ASR recognizes the Hindi sentence.
   - Normalizer matches intent `FLN_NUMERACY_COUNT`.
   - The device immediately plays authentic native Santali voice: *"ᱢᱤᱫ ᱠᱷᱚᱱ ᱢᱚᱬᱮ ᱫᱷᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟᱭ ᱢᱮ"* (*Mid khon mone dhabij lekhay me*).
   - Show the stopwatch latency: **~780 ms** total pipeline response.
   - Show the green badge: **`✓ VERIFIED NATIVE PHRASE`**.

### Step 3: Interactive Child Activity & Offline Worksheet (60 seconds)
1. Proceed to Step 3: Show the student counting activity with tribal visual counters (Elephant, Deer, Tiger).
2. Proceed to Step 4: Show the **Offline Printable Worksheet**.
3. Emphasize: *This worksheet is generated directly on the tablet using the Android PdfDocument API without requiring cloud servers, ready for Bluetooth micro-printers.*

### Step 4: Engineering Honesty & Fallback Demonstration (90 seconds)
1. Proceed to Step 5: **Honest System Boundary Demonstration**.
2. Trigger an unmapped, open-ended sentence: **`"आज हम सब मिलकर बाग में तितलियाँ देखेंगे"`**.
3. Show the result to the judges:
   - System transparently routes to `Offline-Hybrid-Dictionary-Phonetic-Engine`.
   - Result is labeled with an orange badge: **`RULE-BASED / PHONETIC`**.
   - Audio shows: **`🔇 AUDIO UNAVAILABLE`**.
   - Explain to judges: *"We refuse to generate synthetic voice hallucinations or play Hindi voices speaking tribal phonemes. We maintain complete linguistic safety in primary classrooms."*

### Step 5: Summary & Community Loop (30 seconds)
1. Show how unverified phrases enter the **Native Validator Review Workflow** for community sign-off.
2. Conclude with the master metric summary.

---

## 4. Failure Handling & Edge Case Recovery

If speech recognition is uncertain or classroom noise causes a mismatch:
- The UI displays: **`Didn't Understand / आवाज़ समझ नहीं आई`**.
- The teacher is never stuck in an infinite repeat loop. Two instant recovery buttons appear:
  - **`[🔄 TRY AGAIN]`**: Clears state and re-arms the microphone.
  - **`[⌨️ TYPE MANUALLY]`**: Opens an on-screen Hindi keyboard dialog for instant textual fallback.
  - **`[FOUNDATIONAL PHRASES]`**: One-tap quick buttons for standard classroom directives.

---

## 5. Known Limitations & Roadmap

- **Current Limitations**: Open-sentence MT is rule-based with low BLEU (0.01); offline Santali neural TTS is not available on mobile; audio library currently covers 49 verified assets.
- **Next Horizon**: Fine-tune an ultra-compact (45M param) quantized Munda-specific transformer; record 200+ classroom prompts in community field workshops; expand Ho and Mundari from stubs to production status.
