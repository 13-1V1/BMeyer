# Getting a real model into Sprite Forge

By default Sprite Forge runs on a built-in **procedural preview** engine, so the
app works with no setup — but it produces abstract placeholder images, not real
sprites. To generate actual images on-device you install a **MediaPipe-format
Stable Diffusion 1.5 bundle** once, via the app's **On-device engine** card
(**Download model** from a URL, or **Import** a local `.zip`).

This guide explains how to produce, package, and host that bundle. Three stages:
**convert → package → host**, then paste the link into the app.

> The bundle is large (~1–2 GB). Downloading and installing it needs free
> storage roughly twice that during extraction.

---

## 1. Convert SD-1.5 → MediaPipe format

MediaPipe's Image Generator only accepts models that **exactly match the Stable
Diffusion v1.5 architecture**, converted with Google's converter. Two routes:

### Route A — Google's conversion Colab (easiest, no local setup)
Open the official codelab and run its conversion notebook in the browser; it
produces an output folder of converted files you download:

- **On-Device Image Generation on Android with MediaPipe** —
  <https://codelabs.developers.google.com/mp-image-generation-basic-android>
- Android guide (model section) —
  <https://ai.google.dev/edge/mediapipe/solutions/vision/image_generator/android>

### Route B — local conversion
On a machine with Python 3.10+ and ~10 GB free:

```bash
pip install mediapipe torch safetensors
```

Get an SD-1.5 checkpoint (e.g. `v1-5-pruned-emaonly.safetensors` from a
Hugging Face SD-1.5 repo), then run **Google's image-generator converter** on it
with an input checkpoint path and an output directory.

> ⚠️ **Copy the exact converter command from the official page above.** The
> converter invocation changes between MediaPipe releases, so this guide
> deliberately does not pin a command that could go stale. What matters here is
> the *output*: a folder of converted files (UNet / text-encoder / VAE-decoder
> weights, etc.). That folder is exactly what MediaPipe's
> `setImageGeneratorModelDirectory` expects — and what this app unzips into.

---

## 2. Package — the critical part

`ModelManager.installFromZip` extracts each zip entry to `modelDir/<entry-name>`,
and MediaPipe wants the converted files **directly** in the model directory. So
**zip the _contents_ of the output folder, not the folder itself** — the files
must sit at the **zip root**.

✅ Correct (files at root):

```
sd15-mediapipe.zip
├── (converted file 1)
├── (converted file 2)
└── ...
```

❌ Wrong (nested folder — MediaPipe won't find the files):

```
sd15-mediapipe.zip
└── output/
    ├── (converted file 1)
    └── ...
```

From inside the output folder:

```bash
cd path/to/converted_output
zip -r ../sd15-mediapipe.zip .   # trailing "." zips the CONTENTS, not the folder
```

---

## 3. Host with a direct link

The URL must return the **raw bytes** of the `.zip` (the app follows redirects
but expects a file, not an HTML page). Good options:

| Host | Direct-link form |
|---|---|
| **Hugging Face** (upload as a model repo) | `https://huggingface.co/<user>/<repo>/resolve/main/sd15-mediapipe.zip` |
| **GitHub Release** asset | `https://github.com/<user>/<repo>/releases/download/<tag>/sd15-mediapipe.zip` |
| **Your own server / S3** | the plain object URL |
| **Google Drive** | `https://drive.google.com/uc?export=download&id=<FILE_ID>` — works only if it skips the virus-scan interstitial; Hugging Face is more reliable for 1–2 GB |

---

## 4. Use it in the app

Open Sprite Forge → **On-device engine** card → paste the URL → **Download
model**. Watch the progress bar; on completion the top-right chip flips from
**"Preview (procedural)"** to **"On-device SD 1.5"**. Now a prompt like
"m4 rifle" generates a real image.

On-device diffusion is heavy — expect tens of seconds per image, longer on older
hardware. Fewer diffusion steps (set per style preset) trade quality for speed.

---

## Troubleshooting

- **Chip still says "Preview (procedural)" after download** — the zip was
  probably nested (see §2) so the files didn't land at the model-dir root, or
  the converted files don't match the SD-1.5 architecture MediaPipe requires.
- **"Download failed: HTTP 4xx/3xx"** — the link isn't a direct file link
  (it returned an HTML page or an expired signed URL). Use a Hugging Face
  `resolve/` URL or a release asset.
- **Out of storage** — a 1–2 GB bundle needs ~2× that free during extraction.
  Free space and retry.
