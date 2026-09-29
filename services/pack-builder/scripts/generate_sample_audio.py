"""Generates valid sample audio files (PCM WAV format, 16kHz mono) for offline verified phrases."""

import json
import struct
from pathlib import Path


def create_sample_wav(file_path: Path, duration_sec: float = 0.5, sample_rate: int = 16000):
    """Generates a minimal valid PCM WAV file with a soft pleasant tone."""
    num_samples = int(duration_sec * sample_rate)
    data = bytearray()
    import math
    for i in range(num_samples):
        # 440 Hz tone with exponential decay envelope
        t = i / sample_rate
        envelope = math.exp(-3.0 * t)
        val = int(8000 * envelope * math.sin(2 * math.pi * 440.0 * t))
        data.extend(struct.pack("<h", val))

    file_path.parent.mkdir(parents=True, exist_ok=True)
    data_size = len(data)
    total_size = 36 + data_size

    with open(file_path, "wb") as f:
        # RIFF header
        f.write(b"RIFF")
        f.write(struct.pack("<I", total_size))
        f.write(b"WAVE")
        # fmt subchunk
        f.write(b"fmt ")
        f.write(struct.pack("<I", 16))      # Subchunk1Size (16 for PCM)
        f.write(struct.pack("<H", 1))       # AudioFormat (1 for PCM)
        f.write(struct.pack("<H", 1))       # NumChannels (1 mono)
        f.write(struct.pack("<I", sample_rate))  # SampleRate
        f.write(struct.pack("<I", sample_rate * 2))  # ByteRate (SampleRate * NumChannels * BitsPerSample/8)
        f.write(struct.pack("<H", 2))       # BlockAlign (NumChannels * BitsPerSample/8)
        f.write(struct.pack("<H", 16))      # BitsPerSample (16 bits)
        # data subchunk
        f.write(b"data")
        f.write(struct.pack("<I", data_size))
        f.write(data)


def main():
    santali_dir = Path(__file__).parent
    audio_dir = santali_dir / "audio"
    audio_dir.mkdir(parents=True, exist_ok=True)

    # Collect all audio references
    audio_paths = set()

    phrases_file = santali_dir / "phrases.json"
    if phrases_file.exists():
        with open(phrases_file, "r", encoding="utf-8") as f:
            for p in json.load(f):
                if p.get("audio_path"):
                    audio_paths.add(p["audio_path"])

    fln_file = santali_dir / "fln_vocabulary.json"
    if fln_file.exists():
        with open(fln_file, "r", encoding="utf-8") as f:
            for w in json.load(f):
                if w.get("audio_path"):
                    audio_paths.add(w["audio_path"])

    print(f"Generating {len(audio_paths)} sample audio assets in {audio_dir}...")
    for rel_path in audio_paths:
        target = santali_dir / rel_path
        create_sample_wav(target)

    print("Sample audio generation complete.")


if __name__ == "__main__":
    main()
