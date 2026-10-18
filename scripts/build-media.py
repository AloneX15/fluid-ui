"""Encode real client-game-test frames for the README and project pages (requires Pillow)."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "versions/26.3/build/run/clientGameTest/screenshots"
OUTPUT = ROOT / "docs/img"


def load(path, box=None):
    with Image.open(path) as source:
        image = source.convert("RGB")
    if box:
        image = image.crop(box)
    return image


def animation(sequence, name, box):
    frames = [load(path, box) for path in sorted(SOURCE.glob(f"*media_{sequence}_*.png"))]
    if not frames:
        raise RuntimeError(f"Missing {sequence} frames: run with FLUID_UI_CAPTURE=1 first")
    # A shared palette avoids flickering colors; delta encoding keeps the static UI small.
    samples = Image.new("RGB", (frames[0].width, frames[0].height * 8))
    for index in range(8):
        samples.paste(frames[index * (len(frames) - 1) // 7], (0, index * frames[0].height))
    palette = samples.quantize(colors=128)
    encoded = [frame.quantize(palette=palette, dither=Image.Dither.NONE) for frame in frames]
    encoded[0].save(OUTPUT / name, save_all=True, append_images=encoded[1:],
                    duration=80, loop=0, optimize=True, disposal=1)


animation("hover", "demo-hover.gif", (240, 70, 615, 405))
animation("particles", "demo-particulas.gif", (190, 65, 640, 410))
animation("hotbar", "demo-hotbar.gif", (180, 290, 675, 480))

for pattern, name in [("*media_inventory.png", "captura-inventario.png"),
                      ("*media_hotbar_020.png", "captura-hotbar.png"),
                      ("*fluid_ui_chest.png", "captura-cofre.png")]:
    image = load(next(SOURCE.glob(pattern)))
    image.save(OUTPUT / name, optimize=True, compress_level=9)

for path in sorted(OUTPUT.glob("*")):
    if path.suffix == ".gif" or path.name.startswith("captura-"):
        with Image.open(path) as image:
            image.verify()
        print(f"{path.name}: {path.stat().st_size / 1024:.1f} KiB")
