#!/usr/bin/env python3
"""
Hindi->Santali MT Quality Forensic Benchmark Tool.
Evaluates the actual offline MT engine (token-dictionary + Ol Chiki phonetic fallback)
against the held-out reference set, eliminating synthetic reference-to-reference self-comparison.
"""

import collections
import json
import math
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
MANIFEST_PATH = REPO_ROOT / "data" / "mt_test_set" / "hindi_santali_eval_manifest.json"


def get_ngrams(tokens, n):
    return [tuple(tokens[i : i + n]) for i in range(len(tokens) - n + 1)]


def compute_sentence_bleu(ref_tokens, hyp_tokens, max_n=4):
    if not hyp_tokens:
        return 0.0

    precisions = []
    for n in range(1, max_n + 1):
        ref_ngrams = collections.Counter(get_ngrams(ref_tokens, n))
        hyp_ngrams = collections.Counter(get_ngrams(hyp_tokens, n))

        overlap = sum((hyp_ngrams & ref_ngrams).values())
        total = max(len(hyp_tokens) - n + 1, 0)
        if total == 0 or overlap == 0:
            precisions.append(1e-6)
        else:
            precisions.append(overlap / total)

    # Brevity penalty
    c = len(hyp_tokens)
    r = len(ref_tokens)
    bp = 1.0 if c >= r else math.exp(1.0 - float(r) / max(c, 1))

    geom_mean = math.exp(sum(math.log(p) for p in precisions) / max_n)
    return bp * geom_mean * 100.0


def compute_chrf(ref_str, hyp_str, n=6, beta=2.0):
    ref_chars = list(ref_str.replace(" ", ""))
    hyp_chars = list(hyp_str.replace(" ", ""))

    if not hyp_chars:
        return 0.0

    total_f = 0.0
    valid_ngrams = 0

    for k in range(1, n + 1):
        ref_ngrams = collections.Counter(get_ngrams(ref_chars, k))
        hyp_ngrams = collections.Counter(get_ngrams(hyp_chars, k))

        matched = sum((hyp_ngrams & ref_ngrams).values())
        total_hyp = max(len(hyp_chars) - k + 1, 0)
        total_ref = max(len(ref_chars) - k + 1, 0)

        if total_hyp == 0 or total_ref == 0:
            continue

        precision = matched / total_hyp if total_hyp > 0 else 0
        recall = matched / total_ref if total_ref > 0 else 0

        if precision + recall > 0:
            f_score = ((1 + beta**2) * precision * recall) / ((beta**2 * precision) + recall)
            total_f += f_score
            valid_ngrams += 1

    return (total_f / valid_ngrams * 100.0) if valid_ngrams > 0 else 0.0


def offline_engine_translate(text: str) -> str:
    """Mirrors the Kotlin OfflineHindiSantaliMtEngine rule and phonetic mapping."""
    vocab = {
        "किताब": "ᱯᱩᱛᱷᱤ", "कलम": "ᱠᱚᱞᱚᱢ", "पानी": "ᱫᱟᱜ", "हाथ": "ᱛᱤ", "पैर": "ᱡᱟᱝᱜᱟ",
        "बैठो": "ᱫᱩᱲᱩᱵ", "बैठ": "ᱫᱩᱲᱩᱵ", "बैठिए": "ᱫᱩᱲᱩᱵ", "खड़े": "ᱛᱤᱸᱜᱩᱱ", "खड़ा": "ᱛᱤᱸᱜᱩᱱ",
        "सुनों": "ᱟᱧᱡᱚᱢ", "सुनो": "ᱟᱧᱡᱚᱢ", "सुनिए": "ᱟᱧᱡᱚᱢ", "जाओ": "ᱪᱟᱞᱟᱜ", "जाइए": "ᱪᱟᱞᱟᱜ",
        "आओ": "ᱦᱤᱡᱩᱜ", "आइए": "ᱦᱤᱡᱩᱜ", "खोलो": "ᱡᱷᱤᱡ", "बंद": "ᱵᱚᱸᱫᱽ", "करो": "ᱢᱮ", "करें": "ᱢᱮ",
        "बच्चों": "ᱜᱤᱫᱽᱨᱟᱹ", "बच्चे": "ᱜᱤᱫᱽᱨᱟᱹ", "दिखाओ": "ᱩᱫᱩᱜ", "गिनो": "ᱞᱮᱠᱷᱟ",
        "एक": "ᱢᱤᱫ", "दो": "ᱵᱟᱨ", "तीन": "ᱯᱮ", "चार": "ᱯᱩᱱ", "पांच": "ᱢᱚᱬᱮ", "पाँच": "ᱢᱚᱬᱮ",
        "कॉपी": "ᱠᱷᱟᱛᱟ", "चित्र": "ᱪᱤᱛᱟᱹᱨ", "देखो": "ᱧᱮᱞ", "उत्तर": "ᱛᱮᱞᱟ", "देगा": "ᱮᱢ",
        "शाबाश": "ᱵᱮᱥ", "बहुत": "ᱟᱹᱰᱤ", "अच्छा": "ᱵᱮᱥ"
    }

    def phonetic_fallback(tok: str) -> str:
        cmap = {
            "क": "ᱠ", "ख": "ᱠᱷ", "ग": "ᱜ", "घ": "ᱜᱷ", "च": "ᱪ", "छ": "ᱪᱷ", "ज": "ᱡ", "झ": "ᱡᱷ",
            "ट": "ᱴ", "ठ": "ᱴᱷ", "ड": "ᱰ", "ढ": "ᱰᱷ", "त": "ᱛ", "थ": "ᱛᱷ", "द": "ᱫ", "ध": "ᱫᱷ",
            "न": "ᱱ", "प": "ᱯ", "फ": "ᱯᱷ", "ब": "ᱵ", "भ": "ᱵᱷ", "म": "ᱢ", "य": "ᱭ", "र": "ᱨ",
            "ल": "ᱞ", "व": "ᱣ", "श": "ᱥ", "ष": "ᱥ", "स": "ᱥ", "ह": "ᱦ",
            "ा": "ᱟ", "ि": "ᱤ", "ी": "ᱤ", "ु": "ᱩ", "ू": "ᱩ", "े": "ᱮ", "ै": "ᱮ", "ो": "ᱳ", "ौ": "ᱳ",
            "ं": "ᱸ", "ः": "ᱺ"
        }
        res = "".join([cmap.get(c, "") for c in tok])
        return res if res else "ᱪᱟᱞᱟᱜ"

    tokens = [t.strip("।,?!.\"") for t in re.split(r"\s+", text) if t.strip("।,?!.\"")]
    translated = [vocab.get(tok, phonetic_fallback(tok)) for tok in tokens]
    return " ".join(translated)


def main():
    if not MANIFEST_PATH.exists():
        print(f"Error: Dataset manifest not found at {MANIFEST_PATH}", file=sys.stderr)
        return 1

    with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
        data = json.load(f)

    samples = data.get("samples", [])
    total_samples = len(samples)

    real_bleu_scores = []
    real_chrf_scores = []
    real_exact_matches = 0
    sample_results = []

    human_scores = collections.Counter()

    for s in samples:
        ref = s["reference_santali"]
        src = s["source_hindi"]

        # 1. Real Engine Hypothesis
        real_hyp = offline_engine_translate(src)

        ref_tokens = ref.split()
        hyp_tokens = real_hyp.split()

        bleu = compute_sentence_bleu(ref_tokens, hyp_tokens)
        chrf = compute_chrf(ref, real_hyp)

        real_bleu_scores.append(bleu)
        real_chrf_scores.append(chrf)
        is_exact = (ref.strip() == real_hyp.strip())
        if is_exact:
            real_exact_matches += 1

        review = s.get("human_review", {})
        human_scores[review.get("score", "Unreviewed")] += 1

        sample_results.append({
            "sample_id": s.get("sample_id"),
            "category": s.get("category"),
            "source_hindi": src,
            "reference_santali": ref,
            "hypothesis_santali": real_hyp,
            "sentence_bleu": round(bleu, 4),
            "chrf": round(chrf, 2),
            "exact_match": is_exact,
            "human_review": review
        })

    avg_real_bleu = sum(real_bleu_scores) / total_samples
    avg_real_chrf = sum(real_chrf_scores) / total_samples
    real_exact_rate = (real_exact_matches / total_samples) * 100.0

    print("=" * 68)
    print("SIH26042 HINDI->SANTALI (OL CHIKI) FORENSIC MT QUALITY BENCHMARK")
    print(f"Dataset: {data['dataset_name']} (N={total_samples})")
    print("=" * 68)
    print("1. MEASURED OFFLINE HYBRID ENGINE BENCHMARK (REAL INFERENCE HYPOTHESIS):")
    print(f"  • Sentence-Level BLEU (Mean):      {avg_real_bleu:.2f}")
    print(f"  • Character F-score (chrF):        {avg_real_chrf:.2f}")
    print(f"  • Exact Match Rate:                {real_exact_rate:.2f}% ({real_exact_matches}/{total_samples})")
    print("\n2. FORENSIC AUDIT OF PREVIOUS REPORTS:")
    print("  • Previous Reported Metrics:       BLEU = 90.32, chrF = 100.00%, Exact = 100.00%")
    print("  • Previous Methodology Flaw:       Test script set 'hyp = s[\"reference_santali\"]'")
    print("                                     (self-referential evaluation shortcut).")
    print("  • Forensic Classification:         PREVIOUS METRIC WAS SYNTHETIC TEST VECTOR")
    print("                                     (NEURAL INT8 TENSOR MODEL UNVERIFIED ON-DEVICE)")
    print("\n3. NATIVE-SPEAKER HUMAN EVALUATION ON HELD-OUT REFERENCES:")
    for score_type in ["Correct", "Minor correction", "Wrong", "Unusable"]:
        count = human_scores.get(score_type, 0)
        pct = (count / total_samples) * 100.0
        print(f"  • {score_type:<18}: {count:2d} samples ({pct:5.1f}%)")

    report_data = {
        "benchmark": "SIH26042 Forensic Held-Out MT Evaluation",
        "dataset_name": data["dataset_name"],
        "total_samples": total_samples,
        "engine": "Offline-Hybrid-Dictionary-Phonetic-Engine",
        "metrics": {
            "mean_sentence_bleu": round(avg_real_bleu, 4),
            "mean_chrf": round(avg_real_chrf, 2),
            "exact_match_rate": round(real_exact_rate, 2),
            "exact_matches_count": real_exact_matches
        },
        "human_evaluation_held_out": human_scores,
        "samples": sample_results
    }
    output_path = MANIFEST_PATH.parent / "mt_eval_results.json"
    with open(output_path, "w", encoding="utf-8") as out_f:
        json.dump(report_data, out_f, indent=2, ensure_ascii=False)
    print(f"\n4. SAMPLE-BY-SAMPLE AUDIT REPORT:")
    print(f"  • Detailed forensic report saved to:   {output_path.relative_to(REPO_ROOT)}")
    print("=" * 68)
    return 0


if __name__ == "__main__":
    sys.exit(main())
