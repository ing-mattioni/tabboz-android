#!/usr/bin/env python3
"""
Converte le risorse originali di Tabboz Simulator (tabboz-main/resources) negli asset dell'app Android.

- resources/dialogs/dialogs.rc      -> assets/dialogs/dialogs.json (definizione dei dialoghi)
- resources/menus/ZARROSIM.json     -> assets/menus/ZARROSIM.json
- resources/strings/strings.json    -> assets/strings/strings.json
- resources/bitmaps/*.png           -> assets/bitmaps/*.png (+ bitmap di novantotto: OK/Cancel/Yes/No, check, radio)
- resources/icons/*.ico             -> assets/icons/*.png (32x32) (+ icone message box di novantotto)
- resources/wavs/*.wav              -> assets/wavs/
- resources/icons/ZARROSIM.ico      -> res/mipmap-*/ic_launcher*.png (icona dell'app)

Uso: python tools/convert_resources.py [--src ../tabboz-main] [--novantotto <dir>]
"""
import argparse
import json
import re
import shutil
import sys
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
ANDROID = HERE.parent
APP = ANDROID / "app" / "src" / "main"
ASSETS = APP / "assets"
RES = APP / "res"

PATTERN = re.compile(r'(?:[^,"]|"(?:\\.|[^"])*")*')

ID_DEFINES = {
    "IDOK": 1, "IDCANCEL": 2, "IDABORT": 3, "IDRETRY": 4, "IDIGNORE": 5,
    "IDYES": 6, "IDNO": 7, "IDCLOSE": 8, "IDHELP": 9,
}


def split_args(line: str) -> list[str]:
    return [m.strip() for m in PATTERN.findall(line) if m.strip()]


def unquote(s: str) -> str:
    s = s.strip()
    if s.startswith('"') and s.endswith('"') and len(s) >= 2:
        s = s[1:-1]
    return s


def unescape_text(s: str) -> str:
    s = unquote(s)
    s = s.replace('\\"', '"').replace("\\n", "\n").replace("\\t", "\t")
    # Hotkey: "&Start" -> "Start", "&&" -> "&"
    s = re.sub(r"&(?!&)", "", s).replace("&&", "&")
    return s


def to_int(s: str) -> int:
    s = s.strip()
    if s in ID_DEFINES:
        return ID_DEFINES[s]
    v = int(s, 0)
    if v > 32767:  # coordinate negative memorizzate come WORD (65535 = -1)
        v -= 65536
    return v


def parse_styles(s: str) -> tuple[list[str], int]:
    names = []
    num = 0
    for tok in s.split("|"):
        tok = tok.strip()
        if not tok:
            continue
        if tok.startswith("0x") or tok.isdigit():
            num |= int(tok, 0)
        else:
            names.append(tok)
    return names, num


def parse_rc(path: Path) -> dict:
    dialogs = {}
    dialog = None
    in_body = False
    # Il file .rc esportato da novantotto e' in UTF-8; in caso di byte non validi si ripiega su cp1252.
    data = path.read_bytes()
    try:
        text = data.decode("utf-8")
    except UnicodeDecodeError:
        text = data.decode("cp1252")
    if True:
        for raw in text.splitlines():
            line = raw.strip()
            if not line or line.startswith("//") or line.startswith("#"):
                continue
            m = re.match(r"^(\d+)\s+DIALOG(EX)?\s+(.*)$", line)
            if m:
                did = int(m.group(1))
                x, y, w, h = [to_int(v) for v in m.group(3).split(",")[:4]]
                dialog = {"id": did, "x": x, "y": y, "w": w, "h": h, "caption": "",
                          "styles": [], "menu": None, "font": 10, "controls": []}
                dialogs[str(did)] = dialog
                in_body = False
                continue
            if dialog is None:
                continue
            if line in ("{", "BEGIN"):
                in_body = True
                continue
            if line in ("}", "END"):
                in_body = False
                dialog = None
                continue
            head, _, rest = line.partition(" ")
            if not in_body:
                if head == "CAPTION":
                    dialog["caption"] = unescape_text(rest)
                elif head == "STYLE":
                    names, num = parse_styles(rest)
                    dialog["styles"] = names
                    dialog["styleNum"] = num
                elif head == "MENU":
                    dialog["menu"] = unquote(rest).upper()
                elif head == "FONT":
                    dialog["font"] = int(rest.split(",")[0])
                # CLASS / LANGUAGE ignorati
                continue
            # corpo del dialogo
            if head == "CONTROL":
                a = split_args(rest)
                if len(a) < 8:
                    print("Riga CONTROL non valida:", line, file=sys.stderr)
                    continue
                names, num = parse_styles(a[3])
                ctl = {
                    "text": unescape_text(a[0]),
                    "id": to_int(a[1]),
                    "cls": unquote(a[2]),
                    "styles": names,
                    "styleNum": num,
                    "x": to_int(a[4]), "y": to_int(a[5]), "w": to_int(a[6]), "h": to_int(a[7]),
                }
                dialog["controls"].append(ctl)
            elif head in ("LTEXT", "CTEXT", "RTEXT", "PUSHBUTTON", "DEFPUSHBUTTON", "ICON",
                          "EDITTEXT", "CHECKBOX", "RADIOBUTTON", "GROUPBOX", "COMBOBOX"):
                a = split_args(rest)
                # forme abbreviate: text, id, x, y, w, h [, style]
                base_style = {
                    "LTEXT": ("STATIC", ["SS_LEFT"]), "CTEXT": ("STATIC", ["SS_CENTER"]), "RTEXT": ("STATIC", ["SS_RIGHT"]),
                    "PUSHBUTTON": ("BUTTON", ["BS_PUSHBUTTON"]), "DEFPUSHBUTTON": ("BUTTON", ["BS_DEFPUSHBUTTON"]),
                    "ICON": ("STATIC", ["SS_ICON"]), "EDITTEXT": ("EDIT", ["ES_LEFT"]),
                    "CHECKBOX": ("BUTTON", ["BS_CHECKBOX"]), "RADIOBUTTON": ("BUTTON", ["BS_RADIOBUTTON"]),
                    "GROUPBOX": ("BUTTON", ["BS_GROUPBOX"]), "COMBOBOX": ("COMBOBOX", []),
                }[head]
                if head in ("EDITTEXT", "COMBOBOX"):
                    text = ""
                    idx = 0
                else:
                    text = unescape_text(a[0])
                    idx = 1
                names, num = parse_styles(a[idx + 5]) if len(a) > idx + 5 else ([], 0)
                ctl = {
                    "text": text, "id": to_int(a[idx]), "cls": base_style[0],
                    "styles": base_style[1] + names, "styleNum": num,
                    "x": to_int(a[idx + 1]), "y": to_int(a[idx + 2]),
                    "w": to_int(a[idx + 3]), "h": to_int(a[idx + 4]),
                }
                if head == "ICON":
                    ctl["w"] = ctl["w"] or 16
                    ctl["h"] = ctl["h"] or 16
                dialog["controls"].append(ctl)
            else:
                print("Statement ignorato:", line, file=sys.stderr)
    return dialogs


def apply_patches(dialogs: dict) -> None:
    """Piccoli adattamenti per Android (usabilita' touch)."""
    # Dialogo 14 (Configuration): il pulsante "Reset" originale e' un BorBtn di 3x3 DLU quasi invisibile,
    # accanto ad una scritta "reset". Lo trasformiamo in un normale pulsante visibile.
    d = dialogs.get("14")
    if d:
        new_controls = []
        for c in d["controls"]:
            if c["id"] == 203 and c["cls"] == "BorBtn":
                c = dict(c, cls="BUTTON", text="Reset", styles=["BS_PUSHBUTTON", "WS_CHILD", "WS_VISIBLE", "WS_TABSTOP"],
                         styleNum=0, x=97, y=152, w=31, h=11)
            elif c["id"] == -1 and c["cls"] == "STATIC" and c["text"] == "reset":
                continue
            new_controls.append(c)
        d["controls"] = new_controls


def convert_icon(src: Path, dst: Path, size: int = 32) -> None:
    im = Image.open(src)
    im = im.convert("RGBA")
    if im.size != (size, size):
        im = im.resize((size, size), Image.NEAREST)
    im.save(dst)


def make_launcher_icons(icon: Path) -> None:
    base = Image.open(icon).convert("RGBA")
    teal = (0, 128, 128, 255)
    densities = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
    for name, scale in densities.items():
        d = RES / f"mipmap-{name}"
        d.mkdir(parents=True, exist_ok=True)
        # icona legacy 48dp
        px = int(48 * scale)
        img = Image.new("RGBA", (px, px), teal)
        inner = int(px * 0.75)
        ic = base.resize((inner, inner), Image.NEAREST)
        img.alpha_composite(ic, ((px - inner) // 2, (px - inner) // 2))
        img.save(d / "ic_launcher.png")
        img.save(d / "ic_launcher_round.png")
        # foreground adattivo 108dp (zona sicura: 66dp centrali)
        px = int(108 * scale)
        img = Image.new("RGBA", (px, px), (0, 0, 0, 0))
        inner = int(px * 0.5)
        ic = base.resize((inner, inner), Image.NEAREST)
        img.alpha_composite(ic, ((px - inner) // 2, (px - inner) // 2))
        img.save(d / "ic_launcher_foreground.png")
    anydpi = RES / "mipmap-anydpi-v26"
    anydpi.mkdir(parents=True, exist_ok=True)
    xml = ('<?xml version="1.0" encoding="utf-8"?>\n'
           '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
           '    <background android:drawable="@color/ic_launcher_background" />\n'
           '    <foreground android:drawable="@mipmap/ic_launcher_foreground" />\n'
           '</adaptive-icon>\n')
    (anydpi / "ic_launcher.xml").write_text(xml, encoding="utf-8")
    (anydpi / "ic_launcher_round.xml").write_text(xml, encoding="utf-8")
    values = RES / "values"
    values.mkdir(parents=True, exist_ok=True)
    (values / "ic_launcher_background.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
        '    <color name="ic_launcher_background">#008080</color>\n</resources>\n', encoding="utf-8")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=str(ANDROID.parent / "tabboz-main"))
    ap.add_argument("--novantotto", default=None, help="cartella con i sorgenti di novantotto (bitmaps/, icons/)")
    args = ap.parse_args()
    src = Path(args.src)
    res = src / "resources"

    # --- dialoghi ---
    dialogs = parse_rc(res / "dialogs" / "dialogs.rc")
    apply_patches(dialogs)
    (ASSETS / "dialogs").mkdir(parents=True, exist_ok=True)
    with (ASSETS / "dialogs" / "dialogs.json").open("w", encoding="utf-8") as f:
        json.dump({"dialogs": dialogs}, f, ensure_ascii=False, indent=1)
    print(f"dialoghi: {len(dialogs)}")

    # --- menu e stringhe ---
    (ASSETS / "menus").mkdir(parents=True, exist_ok=True)
    shutil.copy(res / "menus" / "ZARROSIM.json", ASSETS / "menus" / "ZARROSIM.json")
    (ASSETS / "strings").mkdir(parents=True, exist_ok=True)
    shutil.copy(res / "strings" / "strings.json", ASSETS / "strings" / "strings.json")

    # --- bitmap ---
    bdir = ASSETS / "bitmaps"
    bdir.mkdir(parents=True, exist_ok=True)
    n = 0
    for p in sorted((res / "bitmaps").glob("*.png")):
        if p.stat().st_size == 0:
            continue
        shutil.copy(p, bdir / p.name)
        n += 1
    nov_bitmaps = res / "bitmaps" / "novantotto"
    if args.novantotto:
        nov_bitmaps = Path(args.novantotto) / "bitmaps"
    for p in sorted(nov_bitmaps.glob("*.png")):
        shutil.copy(p, bdir / p.name)
        n += 1
    print(f"bitmap: {n}")

    # --- icone ---
    idir = ASSETS / "icons"
    idir.mkdir(parents=True, exist_ok=True)
    n = 0
    for p in sorted((res / "icons").glob("*.ico")):
        convert_icon(p, idir / (p.stem + ".png"))
        n += 1
    nov_icons = res / "icons" / "novantotto"
    if args.novantotto:
        nov_icons = Path(args.novantotto) / "icons"
    for p in sorted(nov_icons.glob("*.png")):
        shutil.copy(p, idir / ("mb_" + p.name))
        n += 1
    print(f"icone: {n}")

    # --- suoni ---
    wdir = ASSETS / "wavs"
    wdir.mkdir(parents=True, exist_ok=True)
    n = 0
    for p in sorted((res / "wavs").glob("*.wav")):
        shutil.copy(p, wdir / p.name.lower())
        n += 1
    print(f"suoni: {n}")

    # --- icona app ---
    make_launcher_icons(res / "icons" / "ZARROSIM.ico")
    print("icona app generata")


if __name__ == "__main__":
    main()
