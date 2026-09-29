# ADR 004: Phrase-Bank-First Architecture

## Status
ACCEPTED

## Context
In primary classrooms with 5-to-8-year-old children, conversational needs are highly concentrated around core classroom management commands, routines, and foundational numeracy/literacy exercises (NIPUN Bharat). Running continuous seq2seq MT models introduces hallucinations, inaccurate honorifics, and 2-4 second latency that breaks classroom rhythm.

## Decision
We enforce a **Phrase-Bank-First Two-Tier Pipeline**:
- **Tier 1 (Fast-Path)**: Normalized teacher speech is matched against a pre-verified phrase bank via exact canonical, alias, and Levenshtein similarity (threshold >= 0.80). Execution latency is under 15 ms, triggering pre-recorded native speaker audio in under 1 second.
- **Tier 2 (Fallback)**: If no phrase matches, the input is routed to quantized on-device MT + TTS, with an explicit `MACHINE_GENERATED` or `LOW_CONFIDENCE` provenance badge.

## Consequences
- **Positive**:
  - Near-instant response time (~550 ms - 1.0 s) for over 85% of typical classroom commands.
  - Children hear authentic native speaker intonation, not robotic TTS, for foundational phrases.
  - Zero hallucination risk on verified pedagogical phrases.
- **Negative / Trade-off**:
  - High initial linguistic curation requirement for each tribal language pack.
