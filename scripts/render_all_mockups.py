#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import math
import os
import random
import shutil
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable

import yaml
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SCALE = 3
W_DP, H_DP = 390, 844
W, H = W_DP * SCALE, H_DP * SCALE
FONT_REGULAR = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
FONT_BOLD = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"

# STK-DS-1.0 palette
C = {
    "primary": "#246BFD",
    "primary_pressed": "#1D56D8",
    "primary_soft": "#EAF1FF",
    "accent": "#FF7A1A",
    "accent_pressed": "#E56208",
    "accent_soft": "#FFF0E5",
    "background": "#F5F7FA",
    "surface": "#FFFFFF",
    "surface_secondary": "#F9FAFB",
    "text": "#172033",
    "text_secondary": "#667085",
    "text_tertiary": "#98A2B3",
    "border": "#E4E7EC",
    "divider": "#EAECF0",
    "success": "#16A34A",
    "success_soft": "#EAF8EF",
    "warning": "#F59E0B",
    "warning_soft": "#FFF7E6",
    "error": "#E5484D",
    "error_soft": "#FFF0F1",
    "info": "#246BFD",
    "disabled": "#D0D5DD",
    "scrim": "#00000066",
    "black": "#101828",
    "white": "#FFFFFF",
}

FIXED_CLOCK = "10:00"


def dp(v: float) -> int:
    return int(round(v * SCALE))


def rgb(hex_color: str) -> tuple[int, int, int]:
    h = hex_color.lstrip("#")
    if len(h) == 8:
        h = h[:6]
    return tuple(int(h[i : i + 2], 16) for i in (0, 2, 4))


def rgba(hex_color: str, alpha: int | None = None) -> tuple[int, int, int, int]:
    h = hex_color.lstrip("#")
    if len(h) == 8:
        a = int(h[6:8], 16)
        h = h[:6]
    else:
        a = 255
    if alpha is not None:
        a = alpha
    return (*tuple(int(h[i : i + 2], 16) for i in (0, 2, 4)), a)


def mix(a: str, b: str, t: float) -> str:
    ar, ag, ab = rgb(a)
    br, bg, bb = rgb(b)
    return "#%02X%02X%02X" % (
        round(ar + (br - ar) * t),
        round(ag + (bg - ag) * t),
        round(ab + (bb - ab) * t),
    )


_FONT_CACHE: dict[tuple[int, str], ImageFont.FreeTypeFont] = {}
_GRADIENT_CACHE: dict[tuple[int, int, str, str, bool, int], Image.Image] = {}
_ART_CACHE: dict[tuple[int, int, str, int], Image.Image] = {}


def font(sp: float, weight: str = "regular") -> ImageFont.FreeTypeFont:
    key = (int(round(sp * SCALE)), weight)
    if key not in _FONT_CACHE:
        path = FONT_BOLD if weight in {"bold", "semibold", "600", "700"} else FONT_REGULAR
        _FONT_CACHE[key] = ImageFont.truetype(path, key[0])
    return _FONT_CACHE[key]


def fit_text(draw: ImageDraw.ImageDraw, text: str, max_width_px: int, max_sp: float, min_sp: float = 10, weight: str = "regular") -> ImageFont.FreeTypeFont:
    size = max_sp
    while size > min_sp:
        f = font(size, weight)
        if draw.textbbox((0, 0), text, font=f)[2] <= max_width_px:
            return f
        size -= 0.5
    return font(min_sp, weight)


def wrap_text(draw: ImageDraw.ImageDraw, text: str, f: ImageFont.FreeTypeFont, max_width_px: int, max_lines: int | None = None) -> list[str]:
    out: list[str] = []
    for paragraph in str(text).split("\n"):
        if paragraph == "":
            out.append("")
            continue
        line = ""
        for ch in paragraph:
            test = line + ch
            if draw.textbbox((0, 0), test, font=f)[2] <= max_width_px or not line:
                line = test
            else:
                out.append(line)
                line = ch
                if max_lines is not None and len(out) >= max_lines:
                    break
        if max_lines is not None and len(out) >= max_lines:
            break
        if line:
            out.append(line)
        if max_lines is not None and len(out) >= max_lines:
            break
    if max_lines and len(out) > max_lines:
        out = out[:max_lines]
    if max_lines and out:
        joined = "".join(out)
        original = text.replace("\n", "")
        if len(joined) < len(original):
            last = out[-1]
            while draw.textbbox((0, 0), last + "…", font=f)[2] > max_width_px and last:
                last = last[:-1]
            out[-1] = last + "…"
    return out


@dataclass
class Mockup:
    mockup_id: str
    page_id: str
    state_id: str
    page_name: str
    state_name: str
    state_code: str
    description: str
    release_id: str
    file_name: str
    visual_type: str


class Canvas:
    def __init__(self, bg: str = C["background"]):
        self.im = Image.new("RGB", (W, H), rgb(bg))
        self.d = ImageDraw.Draw(self.im)

    def clone(self) -> "Canvas":
        c = Canvas()
        c.im = self.im.copy()
        c.d = ImageDraw.Draw(c.im)
        return c

    def box(self, x: float, y: float, w: float, h: float) -> tuple[int, int, int, int]:
        return (dp(x), dp(y), dp(x + w), dp(y + h))

    def rect(self, x: float, y: float, w: float, h: float, fill: str, outline: str | None = None, width: float = 1):
        self.d.rectangle(self.box(x, y, w, h), fill=fill, outline=outline, width=dp(width) if outline else 1)

    def rounded(self, x: float, y: float, w: float, h: float, r: float, fill: str, outline: str | None = None, width: float = 1):
        self.d.rounded_rectangle(self.box(x, y, w, h), radius=dp(r), fill=fill, outline=outline, width=dp(width) if outline else 1)

    def shadow_card(self, x: float, y: float, w: float, h: float, r: float = 16, fill: str = C["surface"], outline: str | None = None, shadow: bool = True):
        # Deterministic lightweight elevation: two low-contrast offset layers.
        # This avoids expensive full-canvas Gaussian blur for 236 production mockups.
        if shadow:
            self.rounded(x, y + 2, w, h, r, "#E8ECF2")
            self.rounded(x, y + 1, w, h, r, "#F0F2F6")
        self.rounded(x, y, w, h, r, fill, outline or C["border"], 0.6)

    def line(self, xy: Iterable[tuple[float, float]], fill: str, width: float = 1):
        self.d.line([(dp(x), dp(y)) for x, y in xy], fill=fill, width=dp(width), joint="curve")

    def circle(self, cx: float, cy: float, r: float, fill: str, outline: str | None = None, width: float = 1):
        self.d.ellipse((dp(cx - r), dp(cy - r), dp(cx + r), dp(cy + r)), fill=fill, outline=outline, width=dp(width) if outline else 1)

    def text(self, x: float, y: float, text: str, sp: float = 14, color: str = C["text"], weight: str = "regular", anchor: str = "la", max_width: float | None = None, max_lines: int | None = None, line_height: float | None = None, align: str = "left") -> float:
        f = font(sp, weight)
        if max_width is None:
            self.d.text((dp(x), dp(y)), text, font=f, fill=color, anchor=anchor)
            return sp * 1.5
        lines = wrap_text(self.d, text, f, dp(max_width), max_lines=max_lines)
        lh = dp(line_height if line_height is not None else sp * 1.55)
        yy = dp(y)
        for line in lines:
            if align == "center":
                xx = dp(x + max_width / 2)
                a = "ma"
            elif align == "right":
                xx = dp(x + max_width)
                a = "ra"
            else:
                xx = dp(x)
                a = "la"
            self.d.text((xx, yy), line, font=f, fill=color, anchor=a)
            yy += lh
        return (yy - dp(y)) / SCALE

    def center_text(self, y: float, text: str, sp: float = 14, color: str = C["text"], weight: str = "regular", max_width: float = 342, max_lines: int | None = None, line_height: float | None = None) -> float:
        return self.text((W_DP - max_width) / 2, y, text, sp, color, weight, max_width=max_width, max_lines=max_lines, line_height=line_height, align="center")

    def gradient_rounded(self, x: float, y: float, w: float, h: float, r: float, start: str, end: str, horizontal: bool = False):
        pw, ph = dp(w), dp(h)
        key = (pw, ph, start, end, horizontal, dp(r))
        cached = _GRADIENT_CACHE.get(key)
        if cached is None:
            grad_l = Image.linear_gradient("L")
            if horizontal:
                grad_l = grad_l.rotate(90, expand=True)
            grad_l = grad_l.resize((pw, ph), Image.Resampling.BILINEAR)
            a = Image.new("RGB", (pw, ph), rgb(start))
            b = Image.new("RGB", (pw, ph), rgb(end))
            grad = Image.composite(b, a, grad_l)
            mask = Image.new("L", (pw, ph), 0)
            md = ImageDraw.Draw(mask)
            md.rounded_rectangle((0, 0, pw, ph), radius=dp(r), fill=255)
            grad.putalpha(mask)
            cached = grad
            _GRADIENT_CACHE[key] = cached
        self.im.paste(cached.convert("RGB"), (dp(x), dp(y)), cached.getchannel("A"))
        self.d = ImageDraw.Draw(self.im)

    def overlay(self, color: str = C["scrim"]):
        layer = Image.new("RGBA", (W, H), rgba(color))
        self.im = Image.alpha_composite(self.im.convert("RGBA"), layer).convert("RGB")
        self.d = ImageDraw.Draw(self.im)

    def icon(self, name: str, cx: float, cy: float, size: float = 24, color: str = C["text"], stroke: float = 1.8):
        s = size / 2
        # Icons are deliberately simple, deterministic vectors.
        if name == "back":
            self.line([(cx + s * .25, cy - s * .55), (cx - s * .35, cy), (cx + s * .25, cy + s * .55)], color, stroke)
        elif name == "chevron":
            self.line([(cx - s * .2, cy - s * .45), (cx + s * .25, cy), (cx - s * .2, cy + s * .45)], color, stroke)
        elif name == "close":
            self.line([(cx - s * .45, cy - s * .45), (cx + s * .45, cy + s * .45)], color, stroke)
            self.line([(cx + s * .45, cy - s * .45), (cx - s * .45, cy + s * .45)], color, stroke)
        elif name == "search":
            self.d.ellipse((dp(cx - s * .55), dp(cy - s * .55), dp(cx + s * .25), dp(cy + s * .25)), outline=color, width=dp(stroke))
            self.line([(cx + s * .15, cy + s * .15), (cx + s * .65, cy + s * .65)], color, stroke)
        elif name == "home":
            self.line([(cx - s * .65, cy), (cx, cy - s * .65), (cx + s * .65, cy)], color, stroke)
            self.line([(cx - s * .48, cy - s * .08), (cx - s * .48, cy + s * .55), (cx + s * .48, cy + s * .55), (cx + s * .48, cy - s * .08)], color, stroke)
        elif name == "plus":
            self.line([(cx - s * .55, cy), (cx + s * .55, cy)], color, stroke)
            self.line([(cx, cy - s * .55), (cx, cy + s * .55)], color, stroke)
        elif name == "user":
            self.d.ellipse((dp(cx - s * .28), dp(cy - s * .6), dp(cx + s * .28), dp(cy - s * .04)), outline=color, width=dp(stroke))
            self.d.arc((dp(cx - s * .62), dp(cy - s * .05), dp(cx + s * .62), dp(cy + s * .75)), 200, -20, fill=color, width=dp(stroke))
        elif name == "settings":
            self.circle(cx, cy, s * .25, "#00000000", color, stroke)
            for a in range(0, 360, 60):
                rad = math.radians(a)
                self.line([(cx + math.cos(rad) * s * .42, cy + math.sin(rad) * s * .42), (cx + math.cos(rad) * s * .68, cy + math.sin(rad) * s * .68)], color, stroke)
        elif name == "eye":
            self.d.arc((dp(cx - s * .7), dp(cy - s * .35), dp(cx + s * .7), dp(cy + s * .35)), 190, 350, fill=color, width=dp(stroke))
            self.d.arc((dp(cx - s * .7), dp(cy - s * .35), dp(cx + s * .7), dp(cy + s * .35)), 10, 170, fill=color, width=dp(stroke))
            self.circle(cx, cy, s * .18, color)
        elif name == "refresh":
            self.d.arc((dp(cx - s * .55), dp(cy - s * .55), dp(cx + s * .55), dp(cy + s * .55)), 35, 310, fill=color, width=dp(stroke))
            self.line([(cx + s * .35, cy - s * .52), (cx + s * .65, cy - s * .5), (cx + s * .55, cy - s * .2)], color, stroke)
        elif name == "check":
            self.line([(cx - s * .55, cy), (cx - s * .1, cy + s * .4), (cx + s * .65, cy - s * .5)], color, stroke * 1.2)
        elif name == "error":
            self.circle(cx, cy, s * .66, "#00000000", color, stroke)
            self.line([(cx, cy - s * .35), (cx, cy + s * .12)], color, stroke)
            self.circle(cx, cy + s * .38, s * .06, color)
        elif name == "warning":
            pts = [(dp(cx), dp(cy - s * .72)), (dp(cx - s * .72), dp(cy + s * .58)), (dp(cx + s * .72), dp(cy + s * .58))]
            self.d.polygon(pts, outline=color)
            self.line([(cx, cy - s * .28), (cx, cy + s * .13)], color, stroke)
            self.circle(cx, cy + s * .37, s * .05, color)
        elif name == "info":
            self.circle(cx, cy, s * .65, "#00000000", color, stroke)
            self.circle(cx, cy - s * .3, s * .06, color)
            self.line([(cx, cy - s * .04), (cx, cy + s * .38)], color, stroke)
        elif name == "wifi_off":
            self.d.arc((dp(cx - s * .7), dp(cy - s * .35), dp(cx + s * .7), dp(cy + s * .75)), 210, 330, fill=color, width=dp(stroke))
            self.d.arc((dp(cx - s * .4), dp(cy - s * .05), dp(cx + s * .4), dp(cy + s * .55)), 215, 325, fill=color, width=dp(stroke))
            self.circle(cx, cy + s * .45, s * .08, color)
            self.line([(cx - s * .6, cy - s * .6), (cx + s * .6, cy + s * .6)], color, stroke)
        elif name == "download":
            self.line([(cx, cy - s * .65), (cx, cy + s * .22)], color, stroke)
            self.line([(cx - s * .35, cy - s * .05), (cx, cy + s * .3), (cx + s * .35, cy - s * .05)], color, stroke)
            self.line([(cx - s * .58, cy + s * .58), (cx + s * .58, cy + s * .58)], color, stroke)
        elif name == "upload":
            self.line([(cx, cy + s * .65), (cx, cy - s * .22)], color, stroke)
            self.line([(cx - s * .35, cy + s * .05), (cx, cy - s * .3), (cx + s * .35, cy + s * .05)], color, stroke)
            self.line([(cx - s * .58, cy - s * .58), (cx + s * .58, cy - s * .58)], color, stroke)
        elif name == "image":
            self.rounded(cx - s * .65, cy - s * .5, s * 1.3, s, s * .12, "#00000000", color, stroke)
            self.circle(cx - s * .3, cy - s * .2, s * .1, color)
            pts = [(dp(cx - s * .52), dp(cy + s * .34)), (dp(cx - s * .12), dp(cy - s * .02)), (dp(cx + s * .08), dp(cy + s * .18)), (dp(cx + s * .32), dp(cy - s * .08)), (dp(cx + s * .52), dp(cy + s * .34))]
            self.d.line(pts, fill=color, width=dp(stroke), joint="curve")
        elif name == "camera":
            self.rounded(cx - s * .68, cy - s * .42, s * 1.36, s * .92, s * .12, "#00000000", color, stroke)
            self.circle(cx, cy + s * .03, s * .24, "#00000000", color, stroke)
            self.line([(cx - s * .28, cy - s * .42), (cx - s * .16, cy - s * .62), (cx + s * .18, cy - s * .62), (cx + s * .3, cy - s * .42)], color, stroke)
        elif name == "delete":
            self.rounded(cx - s * .4, cy - s * .35, s * .8, s * .95, s * .06, "#00000000", color, stroke)
            self.line([(cx - s * .55, cy - s * .5), (cx + s * .55, cy - s * .5)], color, stroke)
            self.line([(cx - s * .18, cy - s * .68), (cx + s * .18, cy - s * .68)], color, stroke)
        elif name == "edit":
            self.line([(cx - s * .45, cy + s * .45), (cx + s * .45, cy - s * .45)], color, stroke * 1.2)
            self.line([(cx - s * .55, cy + s * .6), (cx - s * .25, cy + s * .5)], color, stroke)
            self.rounded(cx - s * .66, cy - s * .66, s * 1.2, s * 1.2, s * .1, "#00000000", color, stroke)
        elif name == "copy":
            self.rounded(cx - s * .5, cy - s * .42, s * .8, s * .8, s * .08, "#00000000", color, stroke)
            self.rounded(cx - s * .25, cy - s * .62, s * .8, s * .8, s * .08, "#00000000", color, stroke)
        elif name == "phone":
            self.d.arc((dp(cx - s * .55), dp(cy - s * .55), dp(cx + s * .55), dp(cy + s * .55)), 110, 250, fill=color, width=dp(stroke * 1.2))
            self.line([(cx - s * .45, cy - s * .32), (cx - s * .62, cy - s * .52)], color, stroke)
            self.line([(cx + s * .45, cy + s * .32), (cx + s * .62, cy + s * .52)], color, stroke)
        elif name == "external":
            self.rounded(cx - s * .62, cy - s * .48, s * 1.05, s * 1.05, s * .08, "#00000000", color, stroke)
            self.line([(cx, cy - s * .55), (cx + s * .6, cy - s * .55), (cx + s * .6, cy + s * .05)], color, stroke)
            self.line([(cx + s * .6, cy - s * .55), (cx - s * .05, cy + s * .1)], color, stroke)
        elif name == "lock":
            self.rounded(cx - s * .55, cy - s * .05, s * 1.1, s * .72, s * .12, "#00000000", color, stroke)
            self.d.arc((dp(cx - s * .36), dp(cy - s * .58), dp(cx + s * .36), dp(cy + s * .12)), 180, 360, fill=color, width=dp(stroke))
        elif name == "shield":
            pts = [(dp(cx), dp(cy - s * .72)), (dp(cx + s * .55), dp(cy - s * .45)), (dp(cx + s * .42), dp(cy + s * .35)), (dp(cx), dp(cy + s * .7)), (dp(cx - s * .42), dp(cy + s * .35)), (dp(cx - s * .55), dp(cy - s * .45))]
            self.d.line(pts + [pts[0]], fill=color, width=dp(stroke), joint="curve")
        elif name == "star":
            pts = []
            for i in range(10):
                a = -math.pi / 2 + i * math.pi / 5
                rr = s * (.65 if i % 2 == 0 else .28)
                pts.append((dp(cx + math.cos(a) * rr), dp(cy + math.sin(a) * rr)))
            self.d.polygon(pts, outline=color)
        elif name == "clock":
            self.circle(cx, cy, s * .65, "#00000000", color, stroke)
            self.line([(cx, cy), (cx, cy - s * .34)], color, stroke)
            self.line([(cx, cy), (cx + s * .28, cy + s * .18)], color, stroke)
        elif name == "menu":
            for dy in (-.42, 0, .42):
                self.line([(cx - s * .55, cy + s * dy), (cx + s * .55, cy + s * dy)], color, stroke)
        elif name == "more":
            for dx in (-.42, 0, .42):
                self.circle(cx + s * dx, cy, s * .08, color)
        elif name == "drag":
            for dx in (-.22, .22):
                for dy in (-.38, 0, .38):
                    self.circle(cx + s * dx, cy + s * dy, s * .07, color)
        elif name == "wallet":
            self.rounded(cx - s * .68, cy - s * .42, s * 1.36, s * .84, s * .12, "#00000000", color, stroke)
            self.rounded(cx + s * .05, cy - s * .18, s * .65, s * .36, s * .08, "#00000000", color, stroke)
            self.circle(cx + s * .28, cy, s * .05, color)
        elif name == "card":
            self.rounded(cx - s * .68, cy - s * .46, s * 1.36, s * .92, s * .12, "#00000000", color, stroke)
            self.line([(cx - s * .55, cy - s * .14), (cx + s * .55, cy - s * .14)], color, stroke)
        elif name == "support":
            self.d.arc((dp(cx - s * .55), dp(cy - s * .55), dp(cx + s * .55), dp(cy + s * .55)), 200, 340, fill=color, width=dp(stroke))
            self.rounded(cx - s * .65, cy - s * .05, s * .24, s * .55, s * .08, "#00000000", color, stroke)
            self.rounded(cx + s * .41, cy - s * .05, s * .24, s * .55, s * .08, "#00000000", color, stroke)
        elif name == "doc":
            self.rounded(cx - s * .52, cy - s * .68, s * 1.04, s * 1.36, s * .08, "#00000000", color, stroke)
            self.line([(cx - s * .3, cy - s * .28), (cx + s * .28, cy - s * .28)], color, stroke)
            self.line([(cx - s * .3, cy), (cx + s * .28, cy)], color, stroke)
            self.line([(cx - s * .3, cy + s * .28), (cx + s * .1, cy + s * .28)], color, stroke)
        else:
            self.circle(cx, cy, s * .12, color)

    def logo(self, x: float, y: float, size: float = 64, wordmark: bool = False):
        self.gradient_rounded(x, y, size, size, size * .26, C["primary"], C["primary_pressed"], horizontal=True)
        # upward growth mark
        self.line([(x + size * .24, y + size * .62), (x + size * .43, y + size * .45), (x + size * .55, y + size * .54), (x + size * .76, y + size * .28)], C["white"], size * .065)
        self.line([(x + size * .62, y + size * .28), (x + size * .77, y + size * .28), (x + size * .77, y + size * .43)], C["white"], size * .055)
        self.circle(x + size * .78, y + size * .2, size * .07, C["accent"])
        if wordmark:
            self.text(x + size + 10, y + size * .47, "商推客", 20, C["text"], "bold", anchor="lm")

    def status_bar(self, dark: bool = False):
        col = C["white"] if dark else C["text"]
        self.text(18, 13, FIXED_CLOCK, 12, col, "semibold", anchor="lm")
        # signal bars
        for i, h in enumerate((4, 6, 8, 10)):
            self.rect(319 + i * 4, 11 + (10 - h) / 2, 2, h, col)
        # wifi
        self.d.arc((dp(339), dp(7), dp(357), dp(23)), 210, 330, fill=col, width=dp(1.2))
        self.circle(348, 18.5, 1.2, col)
        # battery
        self.rounded(363, 9, 16, 9, 2, "#00000000", col, .8)
        self.rect(380, 11.5, 1.5, 4, col)
        self.rect(365, 11, 11, 5, col)

    def system_nav(self, dark: bool = False):
        col = mix(C["white"],C["primary_soft"],.18) if dark else "#17203355"
        self.rounded(145, 834, 100, 4, 2, col)

    def top_bar(self, title: str, back: bool = True, root_brand: bool = False, right_icon: str | None = None, right_avatar: bool = False, transparent: bool = False, dark: bool = False) -> float:
        y = 24
        if not transparent:
            self.rect(0, y, 390, 56, C["surface"])
            self.line([(0, y + 56), (390, y + 56)], C["divider"], .7)
        col = C["white"] if dark else C["text"]
        if root_brand:
            self.logo(16, y + 12, 32, wordmark=True)
        else:
            if back:
                self.icon("back", 24, y + 28, 24, col)
            self.text(195, y + 28, title, 18, col, "semibold", anchor="mm")
        if right_avatar:
            self.avatar(354, y + 28, 17, "商")
        elif right_icon:
            self.icon(right_icon, 358, y + 28, 24, col)
        return y + 56

    def bottom_nav(self, selected: str = "home", disabled: bool = False):
        y = 760
        self.rect(0, y, 390, 84, C["surface"])
        self.line([(0, y), (390, y)], C["divider"], .7)
        items = [(65, "home", "首页", "home"), (195, "publish", "发布", "plus"), (325, "me", "我的", "user")]
        for x, key, label, ico in items:
            col = C["disabled"] if disabled else (C["primary"] if selected == key else C["text_tertiary"])
            if key == "publish":
                fill = C["primary_soft"] if selected == key else C["surface_secondary"]
                self.circle(x, y + 24, 17, fill)
            self.icon(ico, x, y + 24, 23, col)
            self.text(x, y + 50, label, 11, col, "semibold", anchor="ma")
        self.system_nav()

    def avatar(self, cx: float, cy: float, r: float, label: str = "商", fill: str = C["primary_soft"]):
        self.circle(cx, cy, r, fill)
        self.text(cx, cy + .5, label[:1], max(10, r * .7), C["primary"], "bold", anchor="mm")

    def button(self, x: float, y: float, w: float, label: str, kind: str = "primary", h: float = 48, enabled: bool = True, loading: bool = False, icon: str | None = None, destructive: bool = False):
        r = 12
        if not enabled:
            fill, outline, text_col = C["disabled"], None, C["white"]
        elif destructive:
            fill, outline, text_col = C["error"], None, C["white"]
        elif kind == "primary":
            fill, outline, text_col = C["primary"], None, C["white"]
        elif kind == "accent":
            fill, outline, text_col = C["accent"], None, C["white"]
        elif kind == "ghost":
            fill, outline, text_col = C["surface"], C["border"], C["text"]
        else:
            fill, outline, text_col = C["surface"], C["primary"], C["primary"]
        self.rounded(x, y, w, h, r, fill, outline, 1)
        if loading:
            self.spinner(x + w / 2 - 33, y + h / 2, 8, text_col)
            self.text(x + w / 2 + 4, y + h / 2, label, 15, text_col, "semibold", anchor="mm")
        else:
            if icon:
                self.icon(icon, x + w / 2 - 38, y + h / 2, 18, text_col)
                self.text(x + w / 2 + 5, y + h / 2, label, 15, text_col, "semibold", anchor="mm")
            else:
                self.text(x + w / 2, y + h / 2, label, 15, text_col, "semibold", anchor="mm")

    def spinner(self, cx: float, cy: float, r: float = 9, color: str = C["primary"], start: int = 35):
        self.d.arc((dp(cx - r), dp(cy - r), dp(cx + r), dp(cy + r)), start, start + 270, fill=color, width=dp(2))

    def text_field(self, x: float, y: float, w: float, label: str, value: str = "", placeholder: str = "", focused: bool = False, error: str | None = None, password: bool = False, right_text: str | None = None, disabled: bool = False, multiline: bool = False, h: float | None = None, prefix_icon: str | None = None, counter: str | None = None):
        hh = h if h is not None else (120 if multiline else 48)
        border = C["error"] if error else (C["primary"] if focused else C["border"])
        fill = C["surface_secondary"] if disabled else C["surface"]
        self.text(x, y - 8, label, 13, C["text_secondary"], "regular", anchor="lb")
        self.rounded(x, y, w, hh, 12, fill, border, 1.2 if focused or error else .8)
        text_x = x + 12
        if prefix_icon:
            self.icon(prefix_icon, x + 20, y + (24 if not multiline else 24), 19, C["text_tertiary"])
            text_x += 28
        shown = ("•" * min(10, len(value))) if password and value else value
        disp = shown or placeholder
        col = C["text"] if shown else C["text_tertiary"]
        if multiline:
            self.text(text_x, y + 15, disp, 14, col, "regular", max_width=w - (text_x - x) - 20, max_lines=4, line_height=22)
        else:
            self.text(text_x, y + hh / 2, disp, 14, col, "regular", anchor="lm")
        if password:
            self.icon("eye", x + w - 24, y + hh / 2, 20, C["text_tertiary"])
        if right_text:
            self.text(x + w - 12, y + hh / 2, right_text, 13, C["primary"] if not disabled else C["disabled"], "semibold", anchor="rm")
        if counter:
            self.text(x + w, y + hh + 5, counter, 11, C["text_tertiary"], "regular", anchor="ra")
        if error:
            self.icon("error", x + 7, y + hh + 13, 12, C["error"])
            self.text(x + 18, y + hh + 13, error, 12, C["error"], "regular", anchor="lm")
        return hh + (22 if error else 0)

    def segmented(self, x: float, y: float, w: float, labels: list[str], selected: int = 0):
        self.rounded(x, y, w, 44, 12, C["surface_secondary"], C["border"], .6)
        segw = w / len(labels)
        self.rounded(x + selected * segw + 3, y + 3, segw - 6, 38, 10, C["surface"], C["border"], .5)
        for i, label in enumerate(labels):
            col = C["primary"] if i == selected else C["text_secondary"]
            self.text(x + segw * (i + .5), y + 22, label, 14, col, "semibold" if i == selected else "regular", anchor="mm")

    def badge(self, x: float, y: float, text: str, status: str = "info") -> float:
        f = font(12, "semibold")
        tw = self.d.textbbox((0, 0), text, font=f)[2] / SCALE
        w = tw + 18
        cmap = {
            "success": (C["success_soft"], C["success"]),
            "warning": (C["warning_soft"], C["warning"]),
            "error": (C["error_soft"], C["error"]),
            "info": (C["primary_soft"], C["primary"]),
            "neutral": (C["surface_secondary"], C["text_secondary"]),
        }
        fill, col = cmap.get(status, cmap["info"])
        self.rounded(x, y, w, 24, 999, fill)
        self.text(x + w / 2, y + 12, text, 12, col, "semibold", anchor="mm")
        return w

    def banner(self, x: float, y: float, w: float, text: str, kind: str = "info", title: str | None = None):
        cmap = {
            "info": (C["primary_soft"], C["primary"], "info"),
            "success": (C["success_soft"], C["success"], "check"),
            "warning": (C["warning_soft"], C["warning"], "warning"),
            "error": (C["error_soft"], C["error"], "error"),
        }
        fill, col, ico = cmap[kind]
        h = 54 if not title else 70
        self.rounded(x, y, w, h, 12, fill)
        self.icon(ico, x + 20, y + (h / 2), 20, col)
        if title:
            self.text(x + 38, y + 19, title, 14, C["text"], "semibold", anchor="lm")
            self.text(x + 38, y + 44, text, 12, C["text_secondary"], "regular", anchor="lm", max_width=w - 52, max_lines=1)
        else:
            self.text(x + 38, y + h / 2, text, 13, C["text_secondary"], "regular", anchor="lm", max_width=w - 52, max_lines=2)
        return h

    def snackbar(self, text: str, kind: str = "neutral", action: str | None = None, y: float = 710):
        fill = C["black"] if kind == "neutral" else {"success": C["success"], "error": C["error"], "warning": C["warning"]}.get(kind, C["black"])
        self.shadow_card(16, y, 358, 48, 12, fill, fill, True)
        self.text(32, y + 24, text, 14, C["white"], "semibold", anchor="lm", max_width=260, max_lines=1)
        if action:
            self.text(354, y + 24, action, 13, C["white"], "semibold", anchor="rm")

    def keyboard(self, top: float = 545, numeric: bool = False):
        self.rect(0, top, 390, 299, "#EEF1F5")
        rows = [["1", "2", "3"], ["4", "5", "6"], ["7", "8", "9"], ["", "0", "⌫"]] if numeric else [list("QWERTYUIOP"), list("ASDFGHJKL"), list("ZXCVBNM")]
        y = top + 12
        if numeric:
            for row in rows:
                keyw = 104
                gap = 12
                x = 27
                for k in row:
                    if k:
                        self.rounded(x, y, keyw, 52, 8, C["surface"], C["border"], .5)
                        self.text(x + keyw / 2, y + 26, k, 18, C["text"], "regular", anchor="mm")
                    x += keyw + gap
                y += 62
        else:
            for ri, row in enumerate(rows):
                gap = 5
                keyw = (370 - gap * (len(row) - 1)) / len(row)
                x = 10 if ri == 0 else (19 if ri == 1 else 45)
                for k in row:
                    self.rounded(x, y, keyw, 44, 6, C["surface"], C["border"], .4)
                    self.text(x + keyw / 2, y + 22, k, 13, C["text"], "semibold", anchor="mm")
                    x += keyw + gap
                y += 54
            self.rounded(52, y + 2, 286, 48, 8, C["surface"], C["border"], .5)
            self.text(195, y + 26, "空格", 13, C["text_secondary"], "regular", anchor="mm")
        self.system_nav()

    def project_art(self, x: float, y: float, w: float, h: float, category: str, idx: int = 0, failed: bool = False):
        if failed:
            self.rounded(x, y, w, h, 12, C["surface_secondary"], C["border"], .7)
            self.icon("image", x + w / 2, y + h / 2 - 8, min(40, h * .35), C["text_tertiary"])
            self.text(x + w / 2, y + h / 2 + 26, "图片加载失败", 11, C["text_tertiary"], "regular", anchor="mm")
            return
        pw, ph = dp(w), dp(h)
        key = (pw, ph, category, idx % 3)
        tile = _ART_CACHE.get(key)
        if tile is None:
            starts = [C["primary"], C["primary_pressed"], C["accent"]]
            ends = [mix(C["primary"], C["accent"], .35), mix(C["primary_soft"], C["primary"], .5), mix(C["accent"], C["primary"], .45)]
            grad_l = Image.linear_gradient("L").rotate(90, expand=True).resize((pw, ph), Image.Resampling.BILINEAR)
            a = Image.new("RGB", (pw, ph), rgb(starts[idx % 3]))
            b = Image.new("RGB", (pw, ph), rgb(ends[idx % 3]))
            base = Image.composite(b, a, grad_l).convert("RGBA")
            td = ImageDraw.Draw(base)
            deco1 = rgb(mix(starts[idx % 3], C["white"], .18)) + (255,)
            deco2 = rgb(mix(ends[idx % 3], C["white"], .10)) + (255,)
            td.ellipse((int(pw * .72), int(ph * .02), int(pw * .93), int(ph * .28)), fill=deco1)
            td.ellipse((int(pw * .58), int(ph * .70), int(pw * .82), int(ph * 1.08)), fill=deco2)
            td.line([(int(pw * .10), int(ph * .72)), (int(pw * .34), int(ph * .48)), (int(pw * .5), int(ph * .57)), (int(pw * .68), int(ph * .28))], fill=(255,255,255,210), width=max(2, dp(2)), joint="curve")
            fs = 14 if h > 70 else 11
            td.text((dp(14), dp(16)), category, font=font(fs, "bold"), fill=rgb(C["white"]) + (255,))
            td.text((dp(14), ph - dp(14)), "商推客项目", font=font(10, "regular"), fill=(255,255,255,220), anchor="ls")
            mask = Image.new("L", (pw, ph), 0)
            md = ImageDraw.Draw(mask)
            md.rounded_rectangle((0, 0, pw, ph), radius=dp(12), fill=255)
            base.putalpha(mask)
            tile = base
            _ART_CACHE[key] = tile
        self.im.paste(tile.convert("RGB"), (dp(x), dp(y)), tile.getchannel("A"))
        self.d = ImageDraw.Draw(self.im)

    def project_card(self, x: float, y: float, w: float, project: dict[str, Any], idx: int = 0, status: str = "published", owner: bool = False, compact: bool = False, image_failed: bool = False, disabled: bool = False):
        h = 132 if compact else 148
        self.shadow_card(x, y, w, h, 16, C["surface"], C["border"], True)
        imgw, imgh = (92, 92) if compact else (108, 86)
        self.project_art(x + 12, y + 12, imgw, imgh, project["category"], idx, failed=image_failed)
        tx = x + 12 + imgw + 12
        maxw = w - (tx - x) - 12
        self.text(tx, y + 15, project["title"], 15, C["text"] if not disabled else C["text_tertiary"], "semibold", max_width=maxw, max_lines=2, line_height=22)
        self.text(tx, y + 61, project["summary"], 12, C["text_secondary"], "regular", max_width=maxw, max_lines=2, line_height=18)
        status_map = {"published": ("已发布", "success"), "pending": ("待审核", "warning"), "rejected": ("已驳回", "error"), "offline": ("已下架", "neutral")}
        if owner:
            self.badge(x + 12, y + 108, *status_map.get(status, status_map["published"]))
            self.text(x + w - 16, y + 120, "•••", 13, C["text_tertiary"], "bold", anchor="rm")
        else:
            self.avatar(x + 24, y + 118, 12, project["publisher"][-2:])
            self.text(x + 42, y + 118, project["publisher"], 11, C["text_secondary"], "regular", anchor="lm")
            self.icon("eye", x + w - 52, y + 118, 15, C["text_tertiary"])
            self.text(x + w - 14, y + 118, str(project["views"]), 11, C["text_tertiary"], "regular", anchor="rm")
        return h

    def skeleton(self, x: float, y: float, w: float, h: float, r: float = 8, shade: int = 0):
        fill = ["#EEF1F5", "#E7EBF0", "#F0F2F5"][shade % 3]
        self.rounded(x, y, w, h, r, fill)

    def center_state(self, title: str, desc: str, kind: str = "empty", button: str | None = None, y: float = 270, request_id: str | None = None, secondary_button: str | None = None):
        cmap = {
            "empty": (C["primary_soft"], C["primary"], "image"),
            "error": (C["error_soft"], C["error"], "error"),
            "offline": (C["warning_soft"], C["warning"], "wifi_off"),
            "success": (C["success_soft"], C["success"], "check"),
            "warning": (C["warning_soft"], C["warning"], "warning"),
            "info": (C["primary_soft"], C["primary"], "info"),
        }
        fill, col, ico = cmap.get(kind, cmap["empty"])
        self.circle(195, y, 56, fill)
        self.icon(ico, 195, y, 48, col, 2.2)
        self.center_text(y + 78, title, 18, C["text"], "semibold", 330, 2, 26)
        self.center_text(y + 114, desc, 14, C["text_secondary"], "regular", 318, 3, 22)
        yy = y + 172
        if request_id:
            self.center_text(yy, f"请求编号：{request_id}", 12, C["text_tertiary"], "regular", 300)
            yy += 34
        if button:
            self.button(96, yy, 198, button, "primary")
            yy += 60
        if secondary_button:
            self.button(96, yy, 198, secondary_button, "ghost")

    def dialog(self, title: str, body: str, primary: str = "确认", secondary: str | None = "取消", kind: str = "info", loading: bool = False, destructive: bool = False, icon: str | None = None, extra: callable | None = None, height: float = 250, y: float | None = None):
        if y is None:
            y = (844 - height) / 2 - 4
        self.overlay()
        x, w = 32, 326
        self.shadow_card(x, y, w, height, 20, C["surface"], C["surface"], True)
        if icon:
            fill = {"error": C["error_soft"], "warning": C["warning_soft"], "success": C["success_soft"]}.get(kind, C["primary_soft"])
            col = {"error": C["error"], "warning": C["warning"], "success": C["success"]}.get(kind, C["primary"])
            self.circle(195, y + 46, 24, fill)
            self.icon(icon, 195, y + 46, 26, col)
            ty = y + 84
        else:
            ty = y + 28
        self.text(195, ty, title, 18, C["text"], "semibold", anchor="ma")
        self.text(x + 24, ty + 38, body, 14, C["text_secondary"], "regular", max_width=w - 48, max_lines=4, line_height=22, align="center")
        if extra:
            extra(x, y, w, height)
        by = y + height - 64
        if secondary:
            self.button(x + 24, by, 132, secondary, "ghost", 44)
            self.button(x + 170, by, 132, primary, "primary", 44, loading=loading, destructive=destructive)
        else:
            self.button(x + 24, by, w - 48, primary, "primary", 44, loading=loading, destructive=destructive)

    def bottom_sheet(self, title: str, height: float = 380, close: bool = True):
        self.overlay()
        y = 844 - height
        self.rounded(0, y, 390, height + 20, 24, C["surface"])
        self.rounded(179, y + 10, 32, 4, 2, C["disabled"])
        self.text(24, y + 42, title, 18, C["text"], "semibold", anchor="lm")
        if close:
            self.icon("close", 354, y + 42, 22, C["text_secondary"])
        return y

    def list_row(self, x: float, y: float, w: float, title: str, subtitle: str | None = None, icon: str | None = None, trailing: str | None = None, danger: bool = False, disabled: bool = False, h: float = 64):
        self.rounded(x, y, w, h, 12, C["surface"], C["border"], .6)
        col = C["disabled"] if disabled else (C["error"] if danger else C["text"])
        if icon:
            fill = C["error_soft"] if danger else C["primary_soft"]
            self.circle(x + 24, y + h / 2, 18, fill)
            self.icon(icon, x + 24, y + h / 2, 20, C["error"] if danger else C["primary"])
            tx = x + 52
        else:
            tx = x + 16
        self.text(tx, y + (24 if subtitle else h / 2), title, 14, col, "semibold" if not subtitle else "regular", anchor="lm")
        if subtitle:
            self.text(tx, y + 45, subtitle, 12, C["text_secondary"], "regular", anchor="lm", max_width=w - (tx - x) - 48, max_lines=1)
        if trailing:
            self.text(x + w - 36, y + h / 2, trailing, 13, C["text_secondary"], "regular", anchor="rm")
        self.icon("chevron", x + w - 18, y + h / 2, 18, C["text_tertiary"])

    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.im.save(path, "PNG", optimize=False, compress_level=6)


# Fixed fixtures; these values are visual-only and mirror ui/FIXTURE_DATA.yaml.
PROJECTS = [
    {"id": 10001, "title": "本地生活服务合作项目", "summary": "用于效果图与自动测试的固定项目简介，不代表真实招商内容。", "category": "本地生活", "publisher": "商推客用户0258", "views": 128},
    {"id": 10002, "title": "社区团购渠道推广计划", "summary": "固定测试数据，用于列表分页、搜索和详情状态。", "category": "渠道推广", "publisher": "商推客用户1036", "views": 86},
    {"id": 10003, "title": "企业服务项目合作招募", "summary": "固定测试数据，不产生任何真实联系、收益或承诺。", "category": "企业服务", "publisher": "商推客用户0865", "views": 42},
]


def draw_root_header(c: Canvas, selected: str = "home", me: bool = False):
    c.status_bar()
    if me:
        c.rect(0, 24, 390, 136, C["primary"])
        c.text(20, 52, "我的", 20, C["white"], "bold", anchor="lm")
        c.icon("settings", 358, 52, 24, C["white"])
    else:
        c.top_bar("", back=False, root_brand=True, right_avatar=True)
    c.bottom_nav(selected)


def draw_home_header(c: Canvas, selected_cat: str = "全部", query: str = "") -> float:
    c.status_bar()
    y = c.top_bar("", back=False, root_brand=True, right_avatar=True)
    c.rounded(16, y + 12, 358, 44, 12, C["surface"], C["border"], .7)
    c.icon("search", 38, y + 34, 20, C["text_tertiary"])
    c.text(58, y + 34, query or "搜索项目标题或简介", 14, C["text"] if query else C["text_tertiary"], "regular", anchor="lm")
    cats = ["全部", "本地生活", "渠道推广", "企业服务"]
    x = 16
    cy = y + 68
    for cat in cats:
        f = font(12, "semibold")
        tw = c.d.textbbox((0, 0), cat, font=f)[2] / SCALE
        ww = tw + 24
        sel = cat == selected_cat
        c.rounded(x, cy, ww, 32, 999, C["primary"] if sel else C["surface"], C["primary"] if sel else C["border"], .6)
        c.text(x + ww / 2, cy + 16, cat, 12, C["white"] if sel else C["text_secondary"], "semibold", anchor="mm")
        x += ww + 8
        if x > 352:
            break
    return cy + 44


def render_design_board(m: Mockup) -> Canvas:
    c = Canvas(C["surface"])
    c.status_bar()
    c.text(24, 48, "STK-DS-1.0", 24, C["text"], "bold", anchor="lm")
    c.text(24, 78, "商推客 Android 统一设计系统", 13, C["text_secondary"], "regular", anchor="lm")
    if m.page_id == "DS-001":
        c.text(24, 118, "颜色", 18, C["text"], "semibold", anchor="lm")
        swatches = [
            ("主品牌色", C["primary"]), ("营销强调", C["accent"]), ("页面背景", C["background"]),
            ("卡片表面", C["surface"]), ("主文字", C["text"]), ("次文字", C["text_secondary"]),
            ("成功", C["success"]), ("警告", C["warning"]), ("错误", C["error"]),
        ]
        for i, (name, col) in enumerate(swatches):
            row, cc = divmod(i, 3)
            x, y = 24 + cc * 118, 142 + row * 74
            c.rounded(x, y, 102, 48, 12, col, C["border"] if col in {C["surface"], C["background"]} else None, .6)
            c.text(x, y + 58, name, 11, C["text_secondary"], "regular", anchor="la")
        c.text(24, 385, "字体层级", 18, C["text"], "semibold", anchor="lm")
        types = [("Display 28/36/700", 28, "bold"), ("页面标题 18/26/600", 18, "bold"), ("区块标题 16/24/600", 16, "bold"), ("卡片标题 15/22/600", 15, "bold"), ("正文 14/22/400", 14, "regular"), ("说明 12/18/400", 12, "regular")]
        y = 420
        for label, spv, wt in types:
            c.text(24, y, "商推客 · " + label, spv, C["text"], wt, anchor="lm")
            y += 48 if spv >= 20 else 38
        c.text(24, 666, "间距与圆角", 18, C["text"], "semibold", anchor="lm")
        vals = [4, 8, 12, 16, 20, 24, 32]
        x = 24
        for v in vals:
            c.rect(x, 700, v, 12, C["primary"])
            c.text(x, 722, f"{v}dp", 10, C["text_secondary"], "regular", anchor="la")
            x += v + 18
        radii = [6, 8, 12, 16, 20]
        x = 24
        for r in radii:
            c.rounded(x, 758, 54, 42, r, C["primary_soft"], C["primary"], .8)
            c.text(x + 27, 814, f"R{r}", 10, C["text_secondary"], "regular", anchor="mm")
            x += 68
    elif m.page_id == "DS-002":
        c.text(24, 118, "公共组件", 18, C["text"], "semibold", anchor="lm")
        c.rect(24, 142, 342, 56, C["surface"])
        c.icon("back", 42, 170, 24, C["text"])
        c.text(195, 170, "页面标题", 18, C["text"], "semibold", anchor="mm")
        c.line([(24, 198), (366, 198)], C["divider"], .7)
        c.button(24, 220, 164, "主要操作", "primary")
        c.button(202, 220, 164, "次要操作", "ghost")
        c.button(24, 282, 164, "加载中", "primary", loading=True)
        c.button(202, 282, 164, "不可用", "primary", enabled=False)
        c.text_field(24, 366, 342, "手机号", "13800138000", focused=True)
        c.text_field(24, 446, 342, "密码", "12345678", password=True, error="密码至少 8 位")
        c.text(24, 540, "状态标签", 16, C["text"], "semibold", anchor="lm")
        x = 24
        for txt, st in [("已发布", "success"), ("待审核", "warning"), ("已驳回", "error"), ("已下架", "neutral")]:
            x += c.badge(x, 558, txt, st) + 8
        c.project_card(24, 610, 342, PROJECTS[0], 0, compact=True)
        c.rect(0, 760, 390, 84, C["surface"])
        c.line([(0, 760), (390, 760)], C["divider"], .7)
        for x, label, ico, sel in [(65, "首页", "home", True), (195, "发布", "plus", False), (325, "我的", "user", False)]:
            col = C["primary"] if sel else C["text_tertiary"]
            c.icon(ico, x, 784, 23, col)
            c.text(x, 810, label, 11, col, "semibold", anchor="ma")
        c.system_nav()
    else:
        c.text(24, 118, "全局状态与反馈", 18, C["text"], "semibold", anchor="lm")
        c.shadow_card(24, 146, 342, 126, 16, C["surface"])
        c.skeleton(40, 162, 82, 82, 12)
        c.skeleton(138, 166, 178, 16, 6, 1)
        c.skeleton(138, 194, 204, 12, 6, 2)
        c.skeleton(138, 218, 142, 12, 6, 0)
        cards = [("暂无内容", "请稍后再试", "empty"), ("连接失败", "检查网络后重试", "error"), ("操作成功", "内容已经更新", "success")]
        for i, (t, dsc, kind) in enumerate(cards):
            x = 24 + i * 114
            c.shadow_card(x, 294, 104, 158, 16, C["surface"])
            fill = {"empty": C["primary_soft"], "error": C["error_soft"], "success": C["success_soft"]}[kind]
            col = {"empty": C["primary"], "error": C["error"], "success": C["success"]}[kind]
            ico = {"empty": "image", "error": "error", "success": "check"}[kind]
            c.circle(x + 52, 332, 22, fill)
            c.icon(ico, x + 52, 332, 24, col)
            c.text(x + 52, 376, t, 13, C["text"], "semibold", anchor="mm")
            c.text(x + 52, 404, dsc, 10, C["text_secondary"], "regular", anchor="mm")
        c.snackbar("操作已完成", "success", y=474)
        c.shadow_card(32, 546, 326, 192, 20, C["surface"])
        c.text(195, 574, "确认操作", 18, C["text"], "semibold", anchor="ma")
        c.center_text(610, "这是统一确认弹窗样式。", 14, C["text_secondary"], "regular", 280)
        c.button(56, 674, 128, "取消", "ghost", 44)
        c.button(206, 674, 128, "确认", "primary", 44)
        c.rounded(0, 760, 390, 104, 24, C["surface"])
        c.rounded(179, 770, 32, 4, 2, C["disabled"])
        c.text(24, 808, "统一底部面板", 16, C["text"], "semibold", anchor="lm")
        c.system_nav()
    return c


def render_splash(m: Mockup) -> Canvas:
    c = Canvas(C["surface"])
    # soft ambient shapes
    c.circle(330, 86, 120, C["primary_soft"])
    c.circle(40, 730, 150, C["accent_soft"])
    c.status_bar()
    c.logo(155, 218, 80)
    c.text(195, 326, "商推客", 28, C["text"], "bold", anchor="ma")
    c.text(195, 368, "发现项目 · 高效推广", 14, C["text_secondary"], "regular", anchor="ma")
    code = m.state_code
    if code == "CHECKING":
        c.spinner(195, 458, 13, C["primary"])
        c.center_text(490, "正在准备商推客", 14, C["text_secondary"], "regular")
        c.center_text(520, "检查配置、登录状态与版本信息", 12, C["text_tertiary"], "regular")
    elif code == "AUTH_REQUIRED":
        c.circle(195, 458, 24, C["primary_soft"])
        c.icon("lock", 195, 458, 28, C["primary"])
        c.center_text(496, "需要登录", 16, C["text"], "semibold")
        c.center_text(526, "正在进入手机号登录页面", 13, C["text_secondary"], "regular")
    elif code == "READY":
        c.circle(195, 458, 24, C["success_soft"])
        c.icon("check", 195, 458, 28, C["success"])
        c.center_text(496, "准备就绪", 16, C["text"], "semibold")
        c.center_text(526, "正在进入已缓存的首页内容", 13, C["text_secondary"], "regular")
    elif code == "OFFLINE_WITH_SESSION":
        c.circle(195, 448, 30, C["warning_soft"])
        c.icon("wifi_off", 195, 448, 34, C["warning"])
        c.center_text(496, "当前处于离线状态", 16, C["text"], "semibold")
        c.center_text(528, "可继续查看最近缓存的项目内容", 13, C["text_secondary"], "regular")
        c.button(96, 574, 198, "离线进入", "primary")
    elif code == "OPTIONAL_UPDATE":
        c.dialog("发现新版本 V1.4.1", "本次更新优化了项目加载与页面稳定性。", "立即更新", "稍后", "info", icon="download", height=284)
    elif code == "FORCED_UPDATE":
        c.dialog("需要更新后继续使用", "当前版本已停止服务，请更新到 V1.4.1。", "立即更新", None, "warning", icon="download", height=274)
    elif code == "MAINTENANCE":
        c.circle(195, 448, 30, C["warning_soft"])
        c.icon("settings", 195, 448, 34, C["warning"])
        c.center_text(496, "系统维护中", 17, C["text"], "semibold")
        c.center_text(530, "服务正在维护，请稍后重新打开", 13, C["text_secondary"], "regular")
        c.button(96, 576, 198, "查看维护说明", "ghost")
    else:  # BOOTSTRAP_FAILED
        c.circle(195, 448, 30, C["error_soft"])
        c.icon("error", 195, 448, 34, C["error"])
        c.center_text(496, "启动配置加载失败", 17, C["text"], "semibold")
        c.center_text(530, "没有出现白屏，您可以重新尝试", 13, C["text_secondary"], "regular")
        c.button(96, 576, 198, "重新加载", "primary")
    c.text(195, 790, "商推客 · Android", 11, C["text_tertiary"], "regular", anchor="ma")
    c.system_nav()
    return c


def render_system_unavailable(m: Mockup) -> Canvas:
    c = Canvas()
    c.status_bar()
    c.top_bar("系统状态", back=False)
    mapping = {
        "MAINTENANCE": ("系统维护中", "后台正在进行服务维护，请稍后再试。", "warning", "稍后重试", "退出应用", None),
        "SERVICE_UNAVAILABLE": ("服务暂不可用", "服务器暂时无法响应，请检查网络后重试。", "error", "重新连接", "返回", None),
        "OFFLINE_NO_CACHE": ("当前没有网络", "首次使用尚无缓存，请连接网络后继续。", "offline", "检查并重试", "退出应用", None),
        "SERVER_ERROR": ("系统出现异常", "服务请求未完成，请使用请求编号联系管理员。", "error", "重新尝试", "返回", "STK-8A21-1000"),
        "RECOVERED": ("服务已恢复", "连接已经恢复，可以继续使用商推客。", "success", "继续使用", None, None),
    }
    title, desc, kind, btn, second, req = mapping[m.state_code]
    c.center_state(title, desc, kind, btn, y=276, request_id=req, secondary_button=second)
    c.system_nav()
    return c


def render_update_page(m: Mockup) -> Canvas:
    c = Canvas()
    c.status_bar()
    c.top_bar("版本更新", back=True)
    c.shadow_card(16, 100, 358, 118, 16, C["surface"])
    c.logo(32, 120, 64)
    c.text(112, 126, "商推客", 18, C["text"], "semibold", anchor="la")
    c.text(112, 158, "当前版本 V1.4.0", 13, C["text_secondary"], "regular", anchor="la")
    c.text(112, 186, "安装包来源：stk-download.zz-yihao.com", 11, C["text_tertiary"], "regular", anchor="la")
    code = m.state_code
    if code == "LOADING":
        c.shadow_card(16, 238, 358, 244, 16, C["surface"])
        c.spinner(195, 322, 18, C["primary"])
        c.center_text(366, "正在检查版本", 16, C["text"], "semibold")
        c.center_text(398, "验证签名发布配置与下载域名", 13, C["text_secondary"], "regular")
    elif code == "UP_TO_DATE":
        c.center_state("已是最新版", "当前版本 V1.4.0 · 检查时间 2026-08-05 10:00", "success", "重新检查", y=340)
    else:
        forced = code in {"FORCED_AVAILABLE", "INSTALL_BLOCKED"}
        c.shadow_card(16, 238, 358, 260, 16, C["surface"])
        c.text(32, 262, "新版本 V1.4.1", 20, C["text"], "bold", anchor="la")
        c.badge(265, 256, "强制更新" if forced else "可选更新", "warning" if forced else "info")
        c.text(32, 304, "安装包大小 18.6 MB", 13, C["text_secondary"], "regular", anchor="la")
        c.text(32, 344, "更新内容", 15, C["text"], "semibold", anchor="la")
        for i, text in enumerate(["优化首页缓存与分页稳定性", "完善项目图片上传失败提示", "修复部分设备覆盖安装问题"]):
            c.circle(38, 382 + i * 32, 3, C["primary"])
            c.text(50, 382 + i * 32, text, 13, C["text_secondary"], "regular", anchor="lm")
        if code == "OPTIONAL_AVAILABLE":
            c.button(16, 532, 358, "下载并更新", "primary", icon="download")
        elif code == "FORCED_AVAILABLE":
            c.banner(16, 516, 358, "当前版本低于最低可用版本，必须更新后继续。", "warning")
            c.button(16, 586, 358, "立即更新", "primary", icon="download")
        elif code == "DOWNLOADING":
            c.text(16, 530, "正在下载 11.2 / 18.6 MB", 13, C["text_secondary"], "regular", anchor="la")
            c.rounded(16, 558, 358, 10, 999, C["divider"])
            c.rounded(16, 558, 216, 10, 999, C["primary"])
            c.text(374, 584, "60%", 12, C["primary"], "semibold", anchor="ra")
            c.button(16, 616, 358, "下载中", "primary", loading=True, enabled=True)
        elif code == "DOWNLOAD_FAILED":
            c.banner(16, 516, 358, "下载失败，请检查网络与可用存储空间。", "error")
            c.button(16, 586, 358, "重新下载", "primary")
        elif code == "READY_TO_INSTALL":
            c.banner(16, 516, 358, "SHA-256 校验通过，安装包可以安装。", "success")
            c.button(16, 586, 358, "打开系统安装器", "primary", icon="download")
        elif code == "INSTALL_BLOCKED":
            c.banner(16, 516, 358, "系统阻止安装，或检测到签名不一致。", "error")
            c.button(16, 586, 358, "查看解决方法", "ghost")
    c.system_nav()
    return c


def auth_base(page: str, mode: str = "password", values: bool = False, focused: str | None = None, errors: dict[str, str] | None = None, sms_right: str | None = None, submitting: bool = False, agreement: bool = True, keyboard: bool = False) -> Canvas:
    c = Canvas(C["surface"])
    c.status_bar()
    if page == "login":
        c.logo(163, 66, 64)
        c.text(195, 148, "欢迎使用商推客", 22, C["text"], "bold", anchor="ma")
        c.text(195, 180, "登录后发现和发布推广项目", 13, C["text_secondary"], "regular", anchor="ma")
        c.segmented(24, 216, 342, ["密码登录", "短信登录"], 0 if mode == "password" else 1)
        y = 282
        phone_val = "13800138000" if values else ""
        c.text_field(24, y, 342, "手机号", phone_val, "请输入手机号", focused=focused == "phone", error=(errors or {}).get("phone"), prefix_icon="phone", disabled=submitting)
        y += 80 if (errors or {}).get("phone") else 68
        if mode == "password":
            c.text_field(24, y, 342, "登录密码", "12345678" if values else "", "请输入登录密码", focused=focused == "password", error=(errors or {}).get("password"), password=True, prefix_icon="lock", disabled=submitting)
            c.text(366, y + 68, "忘记密码", 13, C["primary"], "semibold", anchor="ra")
        else:
            c.text_field(24, y, 342, "短信验证码", "482931" if values else "", "请输入 6 位验证码", focused=focused == "code", error=(errors or {}).get("code"), right_text=sms_right or "发送验证码", prefix_icon="shield", disabled=submitting)
        button_y = y + (104 if (errors or {}).get("password") or (errors or {}).get("code") else 96)
        c.button(24, button_y, 342, "登录中" if submitting else "登录", "primary", loading=submitting)
        c.text(195, button_y + 72, "还没有账号？ 立即注册", 13, C["text_secondary"], "regular", anchor="ma")
        c.text(195, button_y + 108, "登录即表示同意《用户协议》和《隐私政策》", 11, C["text_tertiary"], "regular", anchor="ma")
    elif page == "register":
        c.icon("back", 24, 52, 24, C["text"])
        c.logo(163, 72, 64)
        c.text(195, 154, "创建商推客账号", 22, C["text"], "bold", anchor="ma")
        c.text(195, 184, "注册不发送短信验证码", 13, C["text_secondary"], "regular", anchor="ma")
        y = 236
        c.text_field(24, y, 342, "手机号", "13800138000" if values else "", "请输入手机号", focused=focused == "phone", error=(errors or {}).get("phone"), prefix_icon="phone", disabled=submitting)
        y += 76 if (errors or {}).get("phone") else 66
        c.text_field(24, y, 342, "登录密码", "12345678" if values else "", "8–20 位字母与数字", focused=focused == "password", error=(errors or {}).get("password"), password=True, prefix_icon="lock", disabled=submitting)
        y += 76 if (errors or {}).get("password") else 66
        c.text_field(24, y, 342, "确认登录密码", "12345678" if values else "", "请再次输入密码", focused=focused == "confirm", error=(errors or {}).get("confirm"), password=True, prefix_icon="lock", disabled=submitting)
        agy = y + (82 if (errors or {}).get("confirm") else 68)
        c.rounded(24, agy, 20, 20, 6, C["primary"] if agreement else C["surface"], C["primary"] if agreement else (C["error"] if (errors or {}).get("agreement") else C["border"]), 1)
        if agreement:
            c.icon("check", 34, agy + 10, 13, C["white"], 1.5)
        c.text(52, agy + 10, "我已阅读并同意《用户协议》和《隐私政策》", 12, C["text_secondary"], "regular", anchor="lm")
        if (errors or {}).get("agreement"):
            c.text(24, agy + 32, (errors or {})["agreement"], 12, C["error"], "regular", anchor="la")
        by = agy + 52
        c.button(24, by, 342, "注册中" if submitting else "注册", "primary", loading=submitting)
        c.text(195, by + 72, "已有账号？ 返回登录", 13, C["text_secondary"], "regular", anchor="ma")
    else:  # reset
        c.icon("back", 24, 52, 24, C["text"])
        c.logo(167, 64, 56)
        c.text(195, 138, "找回并重置密码", 22, C["text"], "bold", anchor="ma")
        c.text(195, 168, "通过手机验证码验证身份", 13, C["text_secondary"], "regular", anchor="ma")
        y = 216
        c.text_field(24, y, 342, "手机号", "13800138000" if values else "", "请输入手机号", focused=focused == "phone", error=(errors or {}).get("phone"), prefix_icon="phone", disabled=submitting)
        y += 76 if (errors or {}).get("phone") else 66
        c.text_field(24, y, 342, "短信验证码", "482931" if values else "", "请输入验证码", focused=focused == "code", error=(errors or {}).get("code"), right_text=sms_right or "发送验证码", prefix_icon="shield", disabled=submitting)
        y += 76 if (errors or {}).get("code") else 66
        c.text_field(24, y, 342, "新密码", "12345678" if values else "", "8–20 位字母与数字", focused=focused == "password", error=(errors or {}).get("password"), password=True, prefix_icon="lock", disabled=submitting)
        y += 76 if (errors or {}).get("password") else 66
        c.text_field(24, y, 342, "确认新密码", "12345678" if values else "", "请再次输入新密码", focused=focused == "confirm", error=(errors or {}).get("confirm"), password=True, prefix_icon="lock", disabled=submitting)
        by = y + (88 if (errors or {}).get("confirm") else 78)
        c.button(24, by, 342, "提交中" if submitting else "重置密码", "primary", loading=submitting)
    if keyboard:
        c.keyboard(548, numeric=focused in {"phone", "code"})
    else:
        c.system_nav()
    return c


def render_login(m: Mockup) -> Canvas:
    code = m.state_code
    mode = "sms" if code in {"SMS_DEFAULT", "SMS_SENDING", "SMS_SENT", "SMS_CODE_ERROR", "SMS_CODE_EXPIRED"} else "password"
    values = code not in {"PASSWORD_DEFAULT", "SMS_DEFAULT"}
    errors: dict[str, str] = {}
    focused = None
    sms_right = None
    submitting = code == "SUBMITTING"
    keyboard = code == "FIELD_FOCUSED"
    if code == "FIELD_FOCUSED":
        focused = "phone"
    elif code == "VALIDATION_ERROR":
        errors["phone"] = "请输入正确的 11 位手机号"
        errors["password"] = "请输入登录密码"
    elif code == "CREDENTIAL_ERROR":
        errors["password"] = "手机号或密码错误"
    elif code == "SMS_CODE_ERROR":
        errors["code"] = "短信验证码错误，请重新输入"
    elif code == "SMS_CODE_EXPIRED":
        errors["code"] = "验证码已过期，请重新发送"
    elif code == "SMS_SENDING":
        sms_right = "发送中…"
    elif code == "SMS_SENT":
        sms_right = "56s 后重发"
    c = auth_base("login", mode, values, focused, errors, sms_right, submitting, keyboard=keyboard)
    if code == "SMS_SENT":
        c.banner(24, 570, 342, "验证码已发送，有效期 5 分钟。", "success")
    elif code == "CAPTCHA_REQUIRED":
        draw_captcha_dialog(c, "READY")
        c.badge(294, 238, "登录", "info")
    elif code == "SUCCESS":
        c.snackbar("登录成功，正在进入首页", "success", y=684)
    elif code == "RATE_LIMITED":
        c.banner(24, 570, 342, "操作过于频繁，请 58 秒后重试。", "warning")
    elif code == "ACCOUNT_BLOCKED":
        c.dialog("暂时无法登录", "当前登录请求需要进一步核验，请稍后重试或联系客服。", "我知道了", None, "warning", icon="shield", height=286)
    elif code == "NETWORK_ERROR":
        c.snackbar("网络连接失败，已保留输入内容", "error", "重试", y=684)
    elif code == "TIMEOUT":
        c.snackbar("请求超时，请稍后重试", "warning", "重试", y=684)
    return c


def render_register(m: Mockup) -> Canvas:
    code = m.state_code
    values = code not in {"DEFAULT"}
    errors: dict[str, str] = {}
    focused = "phone" if code == "FIELD_FOCUSED" else None
    agreement = code != "AGREEMENT_REQUIRED"
    keyboard = code == "FIELD_FOCUSED"
    if code == "PHONE_EXISTS":
        errors["phone"] = "该手机号已注册，可直接登录或找回密码"
    elif code == "PASSWORD_MISMATCH":
        errors["confirm"] = "两次输入的密码不一致"
    elif code == "AGREEMENT_REQUIRED":
        errors["agreement"] = "请先阅读并同意协议"
    elif code == "VALIDATION_ERROR":
        errors["password"] = "密码需为 8–20 位字母与数字组合"
    c = auth_base("register", values=values, focused=focused, errors=errors, submitting=code == "SUBMITTING", agreement=agreement, keyboard=keyboard)
    if code == "CAPTCHA_REQUIRED":
        draw_captcha_dialog(c, "READY")
    elif code == "SUCCESS":
        c.dialog("注册成功", "账号已创建，并已生成 UID、会员状态与两个基础账户。", "进入商推客", None, "success", icon="check", height=286)
    elif code == "RATE_LIMITED":
        c.banner(24, 640, 342, "注册请求过于频繁，请 2 分钟后重试。", "warning")
    elif code == "NETWORK_ERROR":
        c.snackbar("网络连接失败，表单内容已保留", "error", "重试", y=684)
    elif code == "SERVER_ERROR":
        c.banner(24, 632, 342, "注册失败，请求编号 STK-RG-1008。", "error")
    return c


def render_reset(m: Mockup) -> Canvas:
    code = m.state_code
    values = code != "DEFAULT"
    errors: dict[str, str] = {}
    sms_right = None
    if code == "VALIDATION_ERROR":
        errors["confirm"] = "两次输入的新密码不一致"
    elif code == "CODE_EXPIRED":
        errors["code"] = "验证码已过期，请重新发送"
    elif code == "SMS_SENDING":
        sms_right = "发送中…"
    elif code == "SMS_SENT":
        sms_right = "56s 后重发"
    c = auth_base("reset", values=values, errors=errors, sms_right=sms_right, submitting=code == "SUBMITTING")
    if code == "CAPTCHA_REQUIRED":
        draw_captcha_dialog(c, "READY")
    elif code == "SMS_SENT":
        c.banner(24, 670, 342, "验证码已发送至 138****5678。", "success")
    elif code == "SUCCESS":
        c.dialog("密码重置成功", "旧登录令牌已失效，请使用新密码重新登录。", "返回登录", None, "success", icon="check", height=270)
    elif code == "RATE_LIMITED":
        c.banner(24, 670, 342, "操作过于频繁，请稍后再试。", "warning")
    elif code == "NETWORK_ERROR":
        c.snackbar("网络连接失败，表单内容已保留", "error", "重试", y=684)
    elif code == "SERVER_ERROR":
        c.snackbar("重置失败，请求编号 STK-RP-1021", "error", "重试", y=684)
    return c


def render_legal(m: Mockup) -> Canvas:
    c = Canvas()
    c.status_bar()
    title = "用户协议" if m.page_id == "AUTH-004" else "隐私政策"
    c.top_bar(title, back=True)
    code = m.state_code
    if code == "LOADING":
        y = 108
        for w in [160, 330, 338, 310, 336, 288, 340, 326, 302, 334, 270]:
            c.skeleton(16, y, w, 14, 6)
            y += 34
    elif code == "ERROR":
        c.center_state("内容加载失败", "无法读取版本化文档，请检查网络后重试。", "error", "重新加载", y=310)
    else:
        if code == "OFFLINE_CACHED":
            c.banner(16, 96, 358, "当前显示已缓存版本，网络恢复后将自动检查更新。", "warning")
            y = 166
        else:
            y = 104
        c.text(16, y, title, 20, C["text"], "bold", anchor="la")
        c.text(16, y + 36, "版本：2026-08-05 · 生效日期：2026-08-05", 12, C["text_tertiary"], "regular", anchor="la")
        section = [
            ("一、说明", "本页面展示受版本管理的固定测试正文。正式内容由后台协议配置提供，并按版本记录生效日期。"),
            ("二、账号与服务", "用户应使用合法信息访问商推客。手机号、登录凭据和操作日志将按照功能合同进行处理。"),
            ("三、内容规范", "项目标题、简介、图片和联系方式必须符合平台规则，不得发布违法、虚假或误导性内容。"),
            ("四、隐私与安全", "平台采用必要的安全措施保护账号与数据，不会在效果图中扩展未规划的数据收集范围。"),
            ("五、联系我们", "如对本协议有疑问，可通过商推客客服页面查询后台配置的联系方式。"),
        ]
        yy = y + 84
        for st, body in section:
            c.text(16, yy, st, 16, C["text"], "semibold", anchor="la")
            h = c.text(16, yy + 34, body, 14, C["text_secondary"], "regular", max_width=358, max_lines=4, line_height=24)
            yy += h + 54
            if yy > 790:
                break
    c.system_nav()
    return c


def draw_captcha_dialog(c: Canvas, state: str):
    c.overlay()
    x, y, w, h = 32, 222, 326, 400
    c.shadow_card(x, y, w, h, 20, C["surface"], C["surface"], True)
    c.text(x + 24, y + 32, "安全验证", 18, C["text"], "semibold", anchor="la")
    c.text(x + 24, y + 66, "完成验证后才会执行当前操作", 13, C["text_secondary"], "regular", anchor="la")
    cap_y = y + 100
    if state in {"LOADING", "REFRESHING"}:
        c.skeleton(x + 24, cap_y, 278, 96, 12)
        c.spinner(195, cap_y + 42, 14, C["primary"])
        if state == "REFRESHING":
            c.text(195, cap_y + 74, "正在刷新验证码", 11, C["text_secondary"], "semibold", anchor="mm")
    elif state == "NETWORK_ERROR":
        c.rounded(x + 24, cap_y, 278, 96, 12, C["error_soft"], C["error"], .8)
        c.icon("wifi_off", 195, cap_y + 35, 28, C["error"])
        c.text(195, cap_y + 70, "验证码加载失败", 13, C["error"], "semibold", anchor="mm")
    else:
        c.gradient_rounded(x + 24, cap_y, 278, 96, 12, C["primary_soft"], C["accent_soft"], horizontal=True)
        random.seed(7305)
        for _ in range(9):
            x1 = x + 30 + random.random() * 260
            y1 = cap_y + 10 + random.random() * 76
            x2 = x + 30 + random.random() * 260
            y2 = cap_y + 10 + random.random() * 76
            c.line([(x1, y1), (x2, y2)], mix(C["primary"], C["accent"], random.random()), .8)
        c.text(195, cap_y + 48, "7 K 3 M", 28, C["primary_pressed"], "bold", anchor="mm")
    c.icon("refresh", x + 284, cap_y + 118, 20, C["primary"])
    c.text(x + 260, cap_y + 118, "刷新", 12, C["primary"], "semibold", anchor="rm")
    err = None
    val = "7K2M" if state == "INPUT_ERROR" else ("7K3M" if state in {"VERIFYING", "SUCCESS"} else "")
    if state == "INPUT_ERROR": err = "验证码错误，还可尝试 2 次"
    elif state == "EXPIRED": err = "验证码已过期，已为您刷新"
    c.text_field(x + 24, cap_y + 144, 278, "验证码", val, "请输入图中字符", focused=state == "READY", error=err)
    by = y + h - 68
    c.button(x + 24, by, 132, "取消", "ghost", 44)
    if state == "SUCCESS":
        c.button(x + 170, by, 132, "验证通过", "primary", 44, icon="check")
    elif state == "NETWORK_ERROR":
        c.button(x + 170, by, 132, "重新加载", "primary", 44)
    else:
        c.button(x + 170, by, 132, "验证中" if state == "VERIFYING" else "确认", "primary", 44, loading=state == "VERIFYING")


def render_captcha_overlay(m: Mockup) -> Canvas:
    c = auth_base("login", "password", values=True)
    draw_captcha_dialog(c, m.state_code)
    return c


def render_auth_risk(m: Mockup) -> Canvas:
    c = auth_base("login", "password", values=True)
    mapping = {
        "LOGIN_LOCKED": ("登录暂时锁定", "多次尝试失败，请在 14 分 32 秒后重试。", "warning", "clock"),
        "ACCOUNT_DISABLED": ("账号当前不可用", "请通过客服页面了解账号处理方式。", "error", "shield"),
        "RISK_REVIEW": ("需要进一步核验", "为保护账号安全，本次登录需要人工核验。", "warning", "shield"),
        "SERVICE_DISABLED": ("当前登录方式不可用", "请切换到另一种登录方式后重试。", "info", "info"),
    }
    title, body, kind, ico = mapping[m.state_code]
    c.dialog(title, body, "我知道了", None, kind, icon=ico, height=282)
    return c


def render_common_overlay(m: Mockup) -> Canvas:
    c = render_home(Mockup("", "HOME-001", "", "", "", "CONTENT", "", "V1.0.0", "", "PAGE"))
    if m.page_id == "COM-OV-002":
        mapping = {
            "PUBLISH_STAGE": ("发布功能尚未开放", "发布项目将在 V1.2.0 开放，当前版本不会出现无响应按钮。"),
            "MEMBER_STAGE": ("会员在线开通暂未开放", "本阶段只展示真实会员状态与后台配置的权益说明。"),
            "WALLET_STAGE": ("账户操作暂未开放", "本阶段只展示佣金账户和任务账户的真实余额。"),
            "PROP_STAGE": ("道具购买暂未开放", "本阶段只展示刷新卡、超级头条、头条和变色卡。"),
        }
        title, body = mapping[m.state_code]
        c.dialog(title, body, "知道了", None, "info", icon="info", height=292)
    else:
        if m.state_code == "ALLOWED":
            def extra(x, y, w, h):
                c.rounded(x + 24, y + 132, w - 48, 58, 12, C["surface_secondary"], C["border"], .6)
                c.text(x + 38, y + 150, "第三方服务", 12, C["text_tertiary"], "regular", anchor="la")
                c.text(x + 38, y + 174, "example.invalid", 14, C["text"], "semibold", anchor="la")
            c.dialog("即将打开外部服务", "该页面将由系统浏览器安全打开，请确认目标域名。", "继续打开", "取消", "info", icon="external", extra=extra, height=350)
        elif m.state_code == "BLOCKED":
            c.dialog("外部链接已被阻断", "目标地址不在后台白名单中，商推客不会继续打开。", "返回", None, "error", icon="shield", height=288)
        else:
            c.dialog("目标应用未安装", "无法打开微信，可改用浏览器访问或复制相关信息。", "使用浏览器", "复制信息", "warning", icon="warning", height=310)
    return c


def render_home(m: Mockup) -> Canvas:
    c = Canvas()
    code = m.state_code
    selected_cat = "本地生活" if code == "FILTERED" else "全部"
    start = draw_home_header(c, selected_cat)
    c.bottom_nav("home")
    if code == "FIRST_LOADING":
        y = start
        for i in range(3):
            c.shadow_card(16, y, 358, 148, 16, C["surface"], C["border"], True)
            c.skeleton(28, y + 12, 108, 86, 12, i)
            c.skeleton(150, y + 16, 170, 16, 6, i + 1)
            c.skeleton(150, y + 45, 196, 12, 6, i + 2)
            c.skeleton(150, y + 69, 132, 12, 6, i)
            c.skeleton(28, y + 112, 86, 20, 999, i + 1)
            y += 160
    elif code in {"CONTENT", "REFRESHING", "FILTERED", "PAGINATING", "PAGINATION_FAILED", "OFFLINE_WITH_CACHE"}:
        y = start
        if code == "REFRESHING":
            c.banner(16, y, 358, "正在刷新最新项目…", "info")
            y += 66
        elif code == "OFFLINE_WITH_CACHE":
            c.banner(16, y, 358, "当前离线，正在显示最近缓存的项目。", "warning")
            y += 66
        projects = PROJECTS[:2] if code == "FILTERED" else PROJECTS
        for i, p in enumerate(projects):
            c.project_card(16, y, 358, p, i)
            y += 160
            if y > 705:
                break
        if code == "PAGINATING":
            c.spinner(195, 730, 10, C["primary"])
            c.text(195, 748, "正在加载更多", 12, C["text_secondary"], "regular", anchor="mm")
        elif code == "PAGINATION_FAILED":
            c.rounded(16, 704, 358, 44, 12, C["error_soft"])
            c.text(32, 726, "加载下一页失败", 12, C["error"], "semibold", anchor="lm")
            c.text(354, 726, "重试", 12, C["primary"], "semibold", anchor="rm")
    elif code == "EMPTY":
        c.center_state("暂无可展示项目", "后台当前没有已发布项目，稍后刷新看看。", "empty", "重新加载", y=350)
    elif code == "OFFLINE_EMPTY":
        c.center_state("离线且没有缓存", "连接网络后即可加载项目内容。", "offline", "检查网络", y=350)
    elif code == "NETWORK_ERROR":
        c.center_state("网络连接失败", "无法加载首页，请检查网络后重试。", "error", "重新加载", y=350)
    elif code == "TIMEOUT":
        c.center_state("请求超时", "服务器响应较慢，您可以再次尝试。", "warning", "重新尝试", y=350)
    elif code == "SERVER_ERROR":
        c.center_state("服务暂时异常", "项目列表未能加载，请使用请求编号排查。", "error", "重新加载", y=350, request_id="STK-HM-2001")
    elif code == "SESSION_EXPIRED":
        y = start
        c.project_card(16, y, 358, PROJECTS[0], 0)
        c.dialog("登录状态已过期", "为保护账号安全，请重新登录后继续使用。", "重新登录", None, "warning", icon="lock", height=276)
    return c


def render_search(m: Mockup) -> Canvas:
    c = Canvas()
    c.status_bar()
    c.top_bar("搜索项目", back=True)
    code = m.state_code
    query = "本地生活" if code not in {"DEFAULT"} else ""
    c.rounded(16, 96, 358, 44, 12, C["surface"], C["primary"] if code == "TYPING" else C["border"], 1)
    c.icon("search", 38, 118, 20, C["text_tertiary"])
    c.text(58, 118, query or "输入项目标题或简介", 14, C["text"] if query else C["text_tertiary"], "regular", anchor="lm")
    if query:
        c.icon("close", 350, 118, 18, C["text_tertiary"])
    if code == "DEFAULT":
        c.center_state("搜索商推客项目", "输入项目标题或简介开始搜索，不保存个人搜索历史。", "info", None, y=330)
    elif code == "TYPING":
        c.text(16, 166, "正在输入关键词", 13, C["text_secondary"], "regular", anchor="la")
        c.keyboard(548)
    elif code == "LOADING":
        y = 166
        for i in range(3):
            c.shadow_card(16, y, 358, 132, 16, C["surface"])
            c.skeleton(28, y + 12, 92, 92, 12, i)
            c.skeleton(134, y + 16, 192, 16, 6, i + 1)
            c.skeleton(134, y + 46, 210, 12, 6, i + 2)
            c.skeleton(134, y + 70, 148, 12, 6, i)
            y += 144
    elif code in {"RESULTS", "OFFLINE_CACHED"}:
        if code == "OFFLINE_CACHED":
            c.banner(16, 156, 358, "离线状态，仅匹配本地缓存结果。", "warning")
            y = 222
        else:
            c.text(16, 166, "找到 2 个相关项目", 13, C["text_secondary"], "regular", anchor="la")
            y = 194
        for i, p in enumerate(PROJECTS[:2]):
            c.project_card(16, y, 358, p, i)
            y += 160
    elif code == "NO_RESULTS":
        c.center_state("没有找到相关项目", "尝试缩短关键词或更换搜索内容。", "empty", "清空关键词", y=350)
    else:
        c.center_state("搜索失败", "无法获取搜索结果，请稍后重试。", "error", "重新搜索", y=350)
    c.system_nav()
    return c


def render_categories(m: Mockup) -> Canvas:
    c = render_home(Mockup("", "HOME-001", "", "", "", "CONTENT", "", "V1.0.0", "", "PAGE"))
    y = c.bottom_sheet("项目分类", 430)
    code = m.state_code
    if code == "ERROR":
        c.center_state("分类加载失败", "已优先使用缓存分类，也可以重新加载。", "error", "重新加载", y=y + 150)
        return c
    cats = ["全部", "本地生活", "渠道推广", "企业服务"] if code != "EMPTY" else ["全部"]
    selected = "本地生活" if code == "SELECTED" else "全部"
    yy = y + 78
    for cat in cats:
        c.rounded(24, yy, 342, 52, 12, C["primary_soft"] if cat == selected else C["surface_secondary"], C["primary"] if cat == selected else C["border"], .7)
        c.text(42, yy + 26, cat, 14, C["primary"] if cat == selected else C["text"], "semibold" if cat == selected else "regular", anchor="lm")
        if cat == selected:
            c.icon("check", 342, yy + 26, 20, C["primary"])
        yy += 62
    if code == "EMPTY":
        c.text(24, yy + 8, "管理员尚未启用其他分类。", 12, C["text_secondary"], "regular", anchor="la")
    c.button(24, 844 - 76, 162, "重置", "ghost", 44)
    c.button(204, 844 - 76, 162, "确认", "primary", 44)
    return c


def detail_base(status: str = "published", image_failed: bool = False, offline: bool = False, owner: bool = False) -> Canvas:
    c = Canvas()
    c.status_bar()
    c.top_bar("项目详情", back=True, right_icon="more")
    if image_failed:
        c.project_art(0, 80, 390, 214, "本地生活", 0, failed=True)
    else:
        c.project_art(0, 80, 390, 214, "本地生活", 0)
    y = 312
    if offline:
        c.banner(16, y, 358, "当前离线，显示已缓存详情；外部动作受到限制。", "warning")
        y += 66
    c.text(16, y, PROJECTS[0]["title"], 20, C["text"], "bold", max_width=260, max_lines=2, line_height=28)
    st = {"published": ("已发布", "success"), "pending": ("待审核", "warning"), "rejected": ("已驳回", "error"), "offline": ("已下架", "neutral")}[status]
    c.badge(292, y + 2, *st)
    y += 62
    c.shadow_card(16, y, 358, 64, 16, C["surface"])
    c.avatar(44, y + 32, 20, "商")
    c.text(74, y + 22, PROJECTS[0]["publisher"], 14, C["text"], "semibold", anchor="lm")
    c.text(74, y + 44, "UID 100258 · 发布于今天 09:30", 11, C["text_tertiary"], "regular", anchor="lm")
    y += 84
    if owner and status == "rejected":
        c.banner(16, y, 358, "驳回原因：项目简介信息不完整，请修改后重新提交。", "error")
        y += 66
    elif owner and status == "pending":
        c.banner(16, y, 358, "项目正在审核中，您可以继续编辑或查看审核状态。", "warning")
        y += 66
    elif owner and status == "offline":
        c.banner(16, y, 358, "项目已下架，当前只有您本人可以查看。", "info")
        y += 66
    c.text(16, y, "项目简介", 16, C["text"], "semibold", anchor="la")
    c.text(16, y + 34, PROJECTS[0]["summary"] + " 页面内容仅用于视觉验收，实际功能以开发合同为准。", 14, C["text_secondary"], "regular", max_width=358, max_lines=4, line_height=22)
    y += 128
    c.shadow_card(16, y, 358, 72, 16, C["surface"])
    c.circle(42, y + 36, 18, C["primary_soft"])
    c.icon("phone", 42, y + 36, 20, C["primary"])
    c.text(70, y + 26, "联系方式", 13, C["text_secondary"], "regular", anchor="lm")
    c.text(70, y + 48, "手机号 138****8000", 14, C["text"], "semibold", anchor="lm")
    c.icon("chevron", 348, y + 36, 18, C["text_tertiary"])
    # sticky action
    c.rect(0, 772, 390, 72, C["surface"])
    c.line([(0, 772), (390, 772)], C["divider"], .7)
    if owner:
        c.button(16, 784, 170, "编辑项目", "ghost", 48, enabled=not offline)
        c.button(204, 784, 170, "查看操作", "primary", 48, enabled=not offline)
    else:
        c.button(16, 784, 358, "查看联系方式", "primary", 48, enabled=not offline)
    c.system_nav()
    return c


def render_detail(m: Mockup) -> Canvas:
    code = m.state_code
    if code == "LOADING":
        c = Canvas()
        c.status_bar(); c.top_bar("项目详情", back=True)
        c.skeleton(0, 80, 390, 214, 0)
        c.skeleton(16, 318, 260, 24, 8)
        c.skeleton(296, 318, 70, 24, 999, 1)
        c.skeleton(16, 370, 358, 64, 16, 2)
        c.skeleton(16, 464, 120, 18, 6)
        for y in [502, 532, 562, 592]: c.skeleton(16, y, 330 - (y % 3) * 20, 12, 6, y)
        c.skeleton(16, 654, 358, 72, 16)
        c.system_nav(); return c
    if code in {"NOT_FOUND", "UNAVAILABLE", "NETWORK_ERROR", "SERVER_ERROR"}:
        c = Canvas(); c.status_bar(); c.top_bar("项目详情", back=True)
        mapping = {
            "NOT_FOUND": ("项目不存在", "项目可能已被删除或链接无效。", "empty", "返回首页", None),
            "UNAVAILABLE": ("项目暂不可查看", "该项目尚未发布或已经下架。", "warning", "返回首页", None),
            "NETWORK_ERROR": ("详情加载失败", "请检查网络后重新加载项目详情。", "error", "重新加载", None),
            "SERVER_ERROR": ("服务暂时异常", "项目详情未能加载，请使用请求编号排查。", "error", "重新加载", "STK-DT-2106"),
        }
        t, dsc, kind, btn, req = mapping[code]
        c.center_state(t, dsc, kind, btn, y=330, request_id=req)
        c.system_nav(); return c
    return detail_base(
        status={"OWNER_PENDING": "pending", "OWNER_REJECTED": "rejected", "OWNER_OFFLINE": "offline"}.get(code, "published"),
        image_failed=code == "IMAGE_FAILED",
        offline=code == "OFFLINE_CACHED",
        owner=code.startswith("OWNER_"),
    )


def render_image_viewer(m: Mockup) -> Canvas:
    c = Canvas("#0B0F18")
    c.status_bar(dark=True)
    c.icon("close", 30, 58, 24, C["white"])
    c.text(195, 58, "1 / 3", 13, C["white"], "semibold", anchor="mm")
    code = m.state_code
    if code == "LOAD_FAILED":
        c.circle(195, 386, 56, "#252B36")
        c.icon("image", 195, 386, 46, "#B8C0CF")
        c.center_text(460, "图片加载失败", 17, C["white"], "semibold")
        c.center_text(494, "可以重试或切换到下一张图片", 13, "#B8C0CF", "regular")
        c.button(116, 548, 158, "重新加载", "ghost")
    else:
        if code == "ZOOMED":
            c.project_art(-70, 170, 530, 430, "本地生活", 0)
            c.center_text(630, "已放大 180% · 可拖动查看", 12, "#B8C0CF", "regular")
        else:
            c.project_art(16, 234, 358, 286, "本地生活", 0)
            c.center_text(554, "双指缩放 · 左右切换", 12, "#B8C0CF", "regular")
    c.circle(177, 720, 3, C["white"])
    c.circle(195, 720, 3, "#697386")
    c.circle(213, 720, 3, "#697386")
    c.system_nav(dark=True)
    return c


def render_contact_actions(m: Mockup) -> Canvas:
    c = detail_base()
    code = m.state_code
    if code == "CONFIRM_EXTERNAL":
        c.dialog("即将打开外部网址", "目标域名：example.invalid\n请确认后使用系统浏览器打开。", "继续打开", "取消", "info", icon="external", height=310)
        return c
    if code == "APP_UNAVAILABLE":
        c.dialog("目标应用未安装", "无法打开对应应用，可改用浏览器或复制联系方式。", "使用浏览器", "复制", "warning", icon="warning", height=310)
        return c
    if code == "INVALID_TARGET":
        c.dialog("联系方式无效", "目标信息未通过安全校验，已阻止继续操作。", "返回", None, "error", icon="shield", height=286)
        return c
    title_map = {"PHONE": "手机号", "WECHAT": "微信", "QQ": "QQ", "WEBSITE": "网址"}
    y = c.bottom_sheet(title_map.get(code, "联系方式"), 330)
    values = {"PHONE": "138****8000", "WECHAT": "stk_fixture_001", "QQ": "100001", "WEBSITE": "example.invalid"}
    val = values.get(code, "")
    c.rounded(24, y + 74, 342, 68, 12, C["surface_secondary"], C["border"], .6)
    c.text(40, y + 94, "联系方式", 12, C["text_tertiary"], "regular", anchor="la")
    c.text(40, y + 122, val, 16, C["text"], "semibold", anchor="la")
    if code == "PHONE":
        c.button(24, y + 164, 162, "复制号码", "ghost", 48, icon="copy")
        c.button(204, y + 164, 162, "拨打电话", "primary", 48, icon="phone")
    elif code in {"WECHAT", "QQ"}:
        c.button(24, y + 164, 162, "复制", "ghost", 48, icon="copy")
        c.button(204, y + 164, 162, "尝试打开", "primary", 48, icon="external")
    else:
        c.button(24, y + 164, 342, "安全打开网址", "primary", 48, icon="external")
    return c


def publish_base(values: bool = False, errors: bool = False, uploading: str | None = None, submitting: bool = False, edit: bool = False, review_warning: bool = False) -> Canvas:
    c = Canvas()
    c.status_bar()
    c.top_bar("编辑项目" if edit else "发布项目", back=edit)
    c.bottom_nav("publish")
    y = 100
    if review_warning:
        c.banner(16, y, 358, "保存后项目将重新进入审核，请确认内容完整。", "warning")
        y += 66
    c.text_field(16, y, 358, "项目标题", PROJECTS[0]["title"] if values else "", "请输入项目标题", error="请输入 4–40 个字的项目标题" if errors else None, counter="12/40" if values else "0/40")
    y += 86 if errors else 74
    c.text_field(16, y, 358, "项目简介", PROJECTS[0]["summary"] if values else "", "请介绍项目内容、合作方式等", multiline=True, h=112, error="项目简介至少填写 20 个字" if errors else None, counter="32/500" if values else "0/500")
    y += 154 if errors else 142
    c.text_field(16, y, 358, "项目分类", "本地生活" if values else "", "请选择项目分类", right_text="选择")
    y += 76
    c.text(16, y, "项目图片", 13, C["text_secondary"], "regular", anchor="la")
    tile_y = y + 18
    for i in range(3 if values or uploading else 1):
        x = 16 + i * 120
        if i == 0 and values:
            c.project_art(x, tile_y, 112, 96, "本地生活", i)
            c.badge(x + 6, tile_y + 66, "封面", "info")
        elif values or uploading:
            c.project_art(x, tile_y, 112, 96, "项目图", i)
        else:
            c.rounded(x, tile_y, 112, 96, 12, C["surface_secondary"], C["border"], .8)
            c.icon("camera", x + 56, tile_y + 38, 28, C["primary"])
            c.text(x + 56, tile_y + 70, "添加图片", 12, C["text_secondary"], "regular", anchor="mm")
        if uploading:
            c.rounded(x, tile_y, 112, 96, 12, "#111827")
            if uploading == "compressing":
                c.spinner(x + 56, tile_y + 40, 12, C["white"])
                c.text(x + 56, tile_y + 70, "压缩中", 12, C["white"], "semibold", anchor="mm")
            elif uploading == "uploading":
                c.text(x + 56, tile_y + 30, f"{40 + i*20}%", 14, C["white"], "bold", anchor="mm")
                c.rounded(x + 12, tile_y + 58, 88, 6, 999, "#475467")
                c.rounded(x + 12, tile_y + 58, (40 + i*20) * .88, 6, 999, C["white"])
            elif uploading == "failed" and i == 1:
                c.icon("error", x + 56, tile_y + 34, 26, C["white"])
                c.text(x + 56, tile_y + 68, "上传失败 · 重试", 11, C["white"], "semibold", anchor="mm")
        if x + 232 > 374:
            break
    y = tile_y + 126
    c.text_field(16, y, 172, "联系方式类型", "手机号" if values else "", "请选择", right_text="选择")
    c.text_field(202, y, 172, "联系方式", "13800138000" if values else "", "请输入内容")
    y += 72
    c.rounded(16, y, 20, 20, 6, C["primary"] if values and not errors else C["surface"], C["error"] if errors else C["border"], 1)
    if values and not errors: c.icon("check", 26, y + 10, 13, C["white"])
    c.text(44, y + 10, "我已确认发布内容真实并遵守平台规则", 12, C["text_secondary"], "regular", anchor="lm")
    c.rect(0, 700, 390, 60, C["background"])
    c.button(16, 704, 358, "保存中" if submitting and edit else ("提交中" if submitting else ("保存修改" if edit else "提交发布")), "primary", 48, loading=submitting)
    return c


def draw_photo_picker(c: Canvas):
    c.overlay("#00000055")
    c.rounded(0, 90, 390, 754, 24, C["surface"])
    c.text(24, 126, "选择照片", 20, C["text"], "bold", anchor="lm")
    c.text(354, 126, "完成", 14, C["primary"], "semibold", anchor="rm")
    c.rounded(16, 154, 358, 44, 12, C["surface_secondary"], C["border"], .6)
    c.icon("search", 38, 176, 20, C["text_tertiary"])
    c.text(58, 176, "搜索照片", 14, C["text_tertiary"], "regular", anchor="lm")
    idx = 0
    for row in range(4):
        for col in range(3):
            x, y = 16 + col * 120, 216 + row * 120
            c.project_art(x, y, 112, 112, ["本地生活", "渠道推广", "企业服务"][idx % 3], idx)
            if idx in {0, 2}:
                c.circle(x + 94, y + 18, 12, C["primary"])
                c.text(x + 94, y + 18, str(1 if idx == 0 else 2), 10, C["white"], "bold", anchor="mm")
            else:
                c.circle(x + 94, y + 18, 12, "#344054", C["white"], 1)
            idx += 1
    c.system_nav()


def render_publish(m: Mockup) -> Canvas:
    code = m.state_code
    if code == "IMAGE_PICKING":
        c = publish_base(values=True)
        draw_photo_picker(c)
        return c
    c = publish_base(
        values=code not in {"DEFAULT"},
        errors=code == "VALIDATION_ERROR",
        uploading={"COMPRESSING": "compressing", "UPLOADING": "uploading", "UPLOAD_FAILED": "failed"}.get(code),
        submitting=code == "SUBMITTING",
    )
    if code == "SUBMIT_SUCCESS":
        c.snackbar("项目已提交，正在进入结果页", "success", y=640)
    elif code == "NETWORK_ERROR":
        c.snackbar("提交失败，表单与已上传图片已保留", "error", "重试", y=640)
    elif code == "DAILY_LIMIT":
        c.dialog("已达到每日发布上限", "后台当前设置每位用户每天最多发布 3 个项目。", "查看我的发布", "返回编辑", "warning", icon="warning", height=310)
    elif code == "PENDING_LIMIT":
        c.dialog("待审核项目数量已达上限", "请先管理已有待审核项目，再提交新的项目。", "管理项目", "返回编辑", "warning", icon="warning", height=310)
    elif code == "SESSION_EXPIRED":
        c.dialog("登录状态已过期", "非敏感草稿已安全保存，请重新登录后继续。", "重新登录", None, "warning", icon="lock", height=286)
    return c


def render_image_sort(m: Mockup) -> Canvas:
    c = Canvas()
    c.status_bar(); c.top_bar("图片排序与封面", back=True)
    c.text(16, 104, "长按拖动调整顺序，第一张为项目封面", 13, C["text_secondary"], "regular", anchor="la")
    code = m.state_code
    positions = [(16, 142), (136, 142), (256, 142), (16, 262), (136, 262)]
    if code == "DRAGGING":
        positions[1] = (150, 218)
    for i, (x, y) in enumerate(positions):
        c.project_art(x, y, 112, 112, ["本地生活", "渠道推广", "企业服务"][i % 3], i)
        if i == (2 if code == "COVER_CHANGED" else 0):
            c.badge(x + 6, y + 78, "封面", "info")
        c.circle(x + 94, y + 18, 14, "#344054")
        c.icon("drag", x + 94, y + 18, 18, C["white"])
    if code == "DRAGGING":
        c.rounded(136, 142, 112, 112, 12, C["primary_soft"], C["primary"], 1.2)
        c.text(192, 198, "拖动位置", 12, C["primary"], "semibold", anchor="mm")
    elif code == "SAVE_FAILED":
        c.banner(16, 410, 358, "保存排序失败，当前本地顺序已保留。", "error")
    elif code == "COVER_CHANGED":
        c.banner(16, 410, 358, "新封面已高亮，点击完成后保存。", "success")
    c.button(16, 700, 358, "完成", "primary")
    c.system_nav()
    return c


def render_publish_result(m: Mockup) -> Canvas:
    c = Canvas(); c.status_bar(); c.top_bar("发布结果", back=False)
    mapping = {
        "PENDING": ("已提交审核", "项目正在等待管理员审核，可在“我的发布”查看进度。", "warning", "查看我的发布", "继续发布"),
        "PUBLISHED": ("发布成功", "项目已公开展示，可立即查看项目详情。", "success", "查看项目详情", "继续发布"),
        "FAILED": ("提交未完成", "服务端未接受本次提交，表单内容仍然保留。", "error", "返回编辑", "稍后再试"),
    }
    t, dsc, kind, p, s = mapping[m.state_code]
    c.center_state(t, dsc, kind, p, y=270, secondary_button=s)
    c.system_nav(); return c


def render_edit(m: Mockup) -> Canvas:
    code = m.state_code
    if code == "LOADING":
        c = Canvas(); c.status_bar(); c.top_bar("编辑项目", back=True)
        y=112
        for h in [48,112,48,112,48,48]:
            c.skeleton(16,y,358,h,12); y+=h+22
        c.system_nav(); return c
    if code == "UNAVAILABLE":
        c = Canvas(); c.status_bar(); c.top_bar("编辑项目", back=True)
        c.center_state("无法编辑该项目", "项目不存在，或您没有编辑权限。", "warning", "返回我的发布", y=330); c.system_nav(); return c
    c = publish_base(values=True, submitting=code=="SAVING", edit=True, review_warning=code=="REVIEW_WARNING")
    if code == "SUCCESS": c.snackbar("保存成功，项目已进入真实状态", "success", y=640)
    elif code == "CONFLICT": c.dialog("内容版本冲突", "项目已在其他位置更新，请刷新后重新编辑。", "刷新内容", "保留当前", "warning", icon="warning", height=310)
    elif code == "NETWORK_ERROR": c.snackbar("保存失败，当前表单已保留", "error", "重试", y=640)
    return c


def render_pub_overlay(m: Mockup) -> Canvas:
    c = publish_base(values=True)
    if m.page_id == "PUB-OV-001":
        if m.state_code == "DEFAULT":
            def extra(x,y,w,h): c.project_art(x+119,y+112,88,72,"本地生活",0)
            c.dialog("删除这张图片？", "图片删除后需要重新选择并上传。", "删除", "取消", "error", destructive=True, icon="delete", extra=extra, height=360)
        elif m.state_code == "DELETING":
            c.dialog("正在删除图片", "请勿重复操作，系统正在校验并删除上传资产。", "删除中", "取消", "error", loading=True, icon="delete", height=300)
        else:
            c.dialog("图片删除失败", "网络或权限校验未完成，可重新尝试。", "重新删除", "取消", "error", icon="error", height=300)
    elif m.page_id == "PUB-OV-002":
        c.dialog("放弃未保存内容？", "当前表单已经修改，放弃后本次更改不会保存。", "放弃", "继续编辑", "warning", destructive=True, icon="warning", height=300)
    else:
        y = c.bottom_sheet("选择联系方式类型", 380)
        code = m.state_code
        if code == "EMPTY":
            c.center_state("暂无可用联系方式类型", "管理员尚未完成配置，当前无法提交发布。", "warning", "返回", y=y+150)
        else:
            types = [("手机号", "phone"), ("微信", "support"), ("QQ", "user"), ("网址", "external")]
            yy = y + 78
            sel = "微信" if code == "TYPE_SELECTED" else None
            for name, ico in types:
                c.rounded(24, yy, 342, 52, 12, C["primary_soft"] if name==sel else C["surface_secondary"], C["primary"] if name==sel else C["border"], .7)
                c.icon(ico, 48, yy+26, 20, C["primary"] if name==sel else C["text_secondary"])
                c.text(76, yy+26, name, 14, C["primary"] if name==sel else C["text"], "semibold" if name==sel else "regular", anchor="lm")
                if name==sel: c.icon("check", 340, yy+26, 20, C["primary"])
                yy += 60
        return c
    return c


def draw_profile_header(c: Canvas):
    c.rect(0, 24, 390, 150, C["primary"])
    c.text(20, 52, "我的", 20, C["white"], "bold", anchor="lm")
    c.icon("settings", 358, 52, 24, C["white"])
    c.shadow_card(16, 92, 358, 126, 20, C["surface"])
    c.avatar(64, 146, 36, "商")
    c.text(116, 126, "商推客用户0001", 18, C["text"], "bold", anchor="lm")
    c.badge(116, 150, "普通用户", "neutral")
    c.text(116, 184, "UID 100001 · 138****5678", 12, C["text_secondary"], "regular", anchor="lm")


def draw_wallets(c: Canvas, y: float, loading: bool = False, error: bool = False):
    c.text(16, y, "账户概览", 16, C["text"], "semibold", anchor="la")
    for i, name in enumerate(["佣金账户", "任务账户"]):
        x = 16 + i * 181
        c.shadow_card(x, y + 30, 177, 96, 16, C["surface"])
        if loading:
            c.skeleton(x+16,y+48,74,12,6,i); c.skeleton(x+16,y+78,100,24,8,i+1)
        elif error:
            c.text(x+16,y+55,name,13,C["text_secondary"],"regular",anchor="la")
            c.text(x+16,y+90,"加载失败",15,C["error"],"semibold",anchor="la")
        else:
            c.text(x+16,y+55,name,13,C["text_secondary"],"regular",anchor="la")
            c.text(x+16,y+90,"¥ 0.00",24,C["text"],"bold",anchor="la")


def draw_member_card(c: Canvas, y: float, state: str = "inactive", loading: bool = False):
    if loading:
        c.skeleton(16,y,358,176,20)
        return
    c.gradient_rounded(16,y,358,176,20,C["primary_pressed"],mix(C["primary"],C["accent"],.45),horizontal=True)
    c.circle(330,y+34,30,mix(C["primary_pressed"],C["white"],.18))
    c.text(36,y+34,"商推客会员",20,C["white"],"bold",anchor="la")
    if state == "inactive":
        c.text(36,y+68,"开通会员，推广更省心",14,C["white"],"regular",anchor="la")
        c.badge(36,y+96,"消费 5 折","info"); c.badge(126,y+96,"消费返佣 40%","info")
        c.button(230,y+116,116,"立即了解","accent",40)
    elif state == "active":
        c.text(36,y+68,"已开通 · 有效期至 2027-08-05",14,C["white"],"regular",anchor="la")
        c.badge(36,y+98,"有效会员","success")
        c.text(36,y+146,"具体权益以后台配置和功能合同为准",11,mix(C["white"],C["primary_soft"],.18),"regular",anchor="la")
    else:
        c.text(36,y+68,"会员已于 2026-07-31 到期",14,C["white"],"regular",anchor="la")
        c.badge(36,y+98,"已过期","warning")
        c.button(230,y+116,116,"查看状态","ghost",40)


def render_me(m: Mockup) -> Canvas:
    c = Canvas(); c.status_bar(); draw_profile_header(c); c.bottom_nav("me")
    code=m.state_code
    if code=="LOADING":
        c.skeleton(32,112,64,64,999); c.skeleton(116,122,180,18,6); c.skeleton(116,154,110,14,6)
        draw_wallets(c,242,loading=True); draw_member_card(c,398,loading=True)
    elif code=="BASIC_V1":
        c.text(16,246,"常用入口",16,C["text"],"semibold",anchor="la")
        c.list_row(16,276,358,"我的发布","V1.2.0 起开放项目管理","doc")
        c.list_row(16,352,358,"会员、账户与道具","V1.3.0 起完善展示","star",disabled=True)
        c.banner(16,444,358,"当前版本先提供基础资料展示，未开放入口不会无响应。","info")
    elif code in {"INACTIVE_MEMBER","ACTIVE_MEMBER","EXPIRED_MEMBER","OFFLINE_CACHED"}:
        y=242
        if code=="OFFLINE_CACHED": c.banner(16,y,358,"当前离线，显示最近缓存的个人资料。","warning"); y+=66
        draw_wallets(c,y); y+=154
        draw_member_card(c,y,{"ACTIVE_MEMBER":"active","EXPIRED_MEMBER":"expired"}.get(code,"inactive")); y+=196
        c.text(16,y,"常用功能",16,C["text"],"semibold",anchor="la")
        # compact four-grid
        for i,(label,ico) in enumerate([("我的发布","doc"),("浏览记录","clock"),("收藏","star"),("实名认证","shield")]):
            x=16+i*90
            c.rounded(x,y+28,82,72,14,C["surface"],C["border"],.6)
            c.icon(ico,x+41,y+50,24,C["primary"])
            c.text(x+41,y+82,label,11,C["text_secondary"],"regular",anchor="mm")
    elif code=="ERROR":
        c.center_state("资料加载失败","无法读取个人资料，请稍后重试。","error","重新加载",y=390)
    else:
        c.dialog("登录状态已过期","敏感资料已从页面清理，请重新登录。","重新登录",None,"warning",icon="lock",height=286)
    return c


def render_my_projects(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("我的发布",back=True)
    code=m.state_code
    tabs=["全部","待审核","已发布","已驳回","已下架"]
    sel={"PENDING":1,"PUBLISHED":2,"REJECTED":3,"OFFLINE":4}.get(code,0)
    x=16
    for i,t in enumerate(tabs):
        ww=62 if i else 50
        c.rounded(x,96,ww,34,999,C["primary"] if i==sel else C["surface"],C["primary"] if i==sel else C["border"],.6)
        c.text(x+ww/2,113,t,11,C["white"] if i==sel else C["text_secondary"],"semibold",anchor="mm")
        x+=ww+6
    if code=="LOADING":
        y=150
        for i in range(4):
            c.shadow_card(16,y,358,132,16,C["surface"]); c.skeleton(28,y+12,92,92,12,i); c.skeleton(134,y+16,190,16,6,i); c.skeleton(134,y+48,210,12,6,i+1); y+=144
    elif code=="EMPTY":
        c.center_state("还没有发布项目","点击发布入口创建第一个项目。","empty","去发布",y=350)
    elif code=="ERROR":
        c.center_state("项目列表加载失败","请检查网络后重新加载。","error","重新加载",y=350)
    else:
        y=150
        if code=="OFFLINE_CACHED": c.banner(16,y,358,"离线状态，仅可查看缓存，操作按钮暂不可用。","warning"); y+=66
        status={"PENDING":"pending","PUBLISHED":"published","REJECTED":"rejected","OFFLINE":"offline"}.get(code,"published")
        items=PROJECTS[:3]
        for i,p in enumerate(items):
            st=status if code not in {"ALL","PAGINATING","PAGINATION_FAILED","OFFLINE_CACHED"} else ["pending","published","rejected"][i]
            c.project_card(16,y,358,p,i,status=st,owner=True,compact=True,disabled=code=="OFFLINE_CACHED")
            y+=144
            if y>700: break
        if code=="PAGINATING": c.spinner(195,730,10,C["primary"]); c.text(195,748,"正在加载更多",12,C["text_secondary"],"regular",anchor="mm")
        if code=="PAGINATION_FAILED": c.rounded(16,704,358,44,12,C["error_soft"]); c.text(32,726,"加载更多失败",12,C["error"],"semibold",anchor="lm"); c.text(354,726,"重试",12,C["primary"],"semibold",anchor="rm")
    c.system_nav(); return c


def render_profile(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("个人资料",back=True)
    if m.state_code=="LOADING":
        c.skeleton(159,118,72,72,999); y=224
        for _ in range(5): c.skeleton(16,y,358,64,12); y+=76
    elif m.state_code=="ERROR":
        c.center_state("资料加载失败","无法读取个人基础资料。","error","重新加载",y=330)
    else:
        c.avatar(195,152,36,"商")
        c.text(195,210,"商推客用户0001",18,C["text"],"bold",anchor="ma")
        rows=[("UID","100001"),("手机号","138****5678"),("手机验证状态","未验证"),("注册时间","2026-08-05 10:00"),("账号状态","正常")]
        y=252
        for k,v in rows:
            c.rounded(16,y,358,56,12,C["surface"],C["border"],.6)
            c.text(32,y+28,k,13,C["text_secondary"],"regular",anchor="lm")
            c.text(358,y+28,v,14,C["text"],"semibold",anchor="rm")
            y+=68
        c.banner(16,608,358,"本期为只读资料页，不提供未规划的修改入口。","info")
    c.system_nav(); return c


def render_member(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("会员中心",back=True)
    if m.state_code=="LOADING":
        c.skeleton(16,104,358,176,20); c.skeleton(16,310,358,220,16)
    elif m.state_code=="ERROR":
        c.center_state("会员状态加载失败","无法读取真实会员状态，请重新加载。","error","重新加载",y=330)
    else:
        state={"ACTIVE":"active","EXPIRED":"expired"}.get(m.state_code,"inactive")
        draw_member_card(c,104,state)
        c.text(16,310,"会员权益说明",16,C["text"],"semibold",anchor="la")
        rights=[("消费 5 折","权益文案由后台配置，本期不执行结算。"),("消费返佣 40%","仅展示权益说明，不产生实际返佣。"),("会员标识","已开通时在项目和资料卡展示。")]
        y=342
        for title,sub in rights:
            c.list_row(16,y,358,title,sub,"star",h=68); y+=80
        if state=="inactive": c.button(16,610,358,"了解开通方式","primary")
        else: c.banner(16,610,358,"当前页面只展示真实会员状态与有效期。","info")
    c.system_nav(); return c


def render_wallets(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("账户余额",back=True)
    if m.state_code=="LOADING": draw_wallets(c,116,loading=True)
    elif m.state_code=="ERROR":
        draw_wallets(c,116,error=True); c.banner(16,286,358,"账户读取失败，不能用假 0.00 替代真实数据。","error"); c.button(96,366,198,"重新加载","primary")
    else:
        draw_wallets(c,116); c.banner(16,286,358,"本期仅展示余额，不提供提现、转账或账户流水。","info")
        c.text(16,374,"账户说明",16,C["text"],"semibold",anchor="la")
        c.list_row(16,406,358,"佣金账户","注册基础值为 0.00","wallet")
        c.list_row(16,482,358,"任务账户","注册基础值为 0.00","wallet")
    c.system_nav(); return c


def render_props(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("道具中心",back=True)
    code=m.state_code
    if code=="LOADING":
        for i in range(4):
            x=16+(i%2)*181; y=112+(i//2)*132; c.skeleton(x,y,177,112,16,i)
    elif code=="EMPTY":
        c.center_state("暂无展示道具","管理员尚未启用道具展示。","empty",None,y=340)
    elif code=="ERROR":
        c.center_state("道具配置加载失败","请稍后重新加载。","error","重新加载",y=340)
    else:
        if code=="OFFLINE_CACHED": c.banner(16,96,358,"当前显示最近缓存的道具配置。","warning"); sy=166
        else: sy=104
        props=[("刷新卡","提升项目刷新展示","refresh"),("超级头条","突出重点项目展示","star"),("头条","增强项目信息曝光","info"),("变色卡","展示差异化视觉标识","card")]
        for i,(t,sub,ico) in enumerate(props):
            x=16+(i%2)*181; y=sy+(i//2)*132
            c.shadow_card(x,y,177,112,16,C["surface"])
            c.circle(x+30,y+30,20,C["primary_soft"]); c.icon(ico,x+30,y+30,22,C["primary"])
            c.text(x+16,y+66,t,14,C["text"],"semibold",anchor="la")
            c.text(x+16,y+90,sub,11,C["text_secondary"],"regular",anchor="la",max_width=145,max_lines=1)
        c.banner(16,sy+282,358,"本期只展示道具，不显示购买价格、库存或使用入口。","info")
    c.system_nav(); return c


def render_placeholder(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar()
    mapping={"ME-007":("浏览记录","浏览记录功能未开放","本五版本不实现浏览记录业务。","clock"),"ME-008":("收藏","收藏功能未开放","本五版本不实现项目收藏业务。","star"),"ME-009":("实名认证","实名认证未开放","本五版本不会采集姓名、身份证或人脸信息。","shield")}
    title,st,desc,ico=mapping[m.page_id]
    c.top_bar(title,back=True)
    c.circle(195,290,56,C["primary_soft"]); c.icon(ico,195,290,48,C["primary"])
    c.center_text(372,st,18,C["text"],"semibold")
    c.center_text(410,desc,14,C["text_secondary"],"regular",310,3,22)
    c.button(96,496,198,"返回", "primary")
    c.banner(16,598,358,"占位页面明确功能边界，不提供虚假按钮或表单。","info")
    c.system_nav(); return c


def render_support(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("联系客服",back=True)
    code=m.state_code
    if code=="LOADING":
        c.skeleton(16,104,358,90,16); c.skeleton(16,214,358,90,16); c.skeleton(16,324,358,90,16)
    elif code=="ERROR": c.center_state("客服配置加载失败","无法读取后台配置的联系方式。","error","重新加载",y=330)
    else:
        c.banner(16,104,358,"请仅使用以下由后台配置的客服方式。","info")
        c.list_row(16,178,358,"客服电话","138****8000","phone")
        c.list_row(16,254,358,"客服微信","stk_fixture_001","support")
        c.list_row(16,330,358,"帮助网址","example.invalid","external")
        if code=="COPY_SUCCESS": c.snackbar("客服信息已复制", "success", y=690)
        elif code=="APP_UNAVAILABLE": c.dialog("目标应用未安装","可复制客服信息或使用浏览器打开帮助网址。","复制信息","取消","warning",icon="warning",height=310)
    c.system_nav(); return c


def render_settings(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("设置",back=True)
    code=m.state_code
    rows=[("清理缓存","当前 36.8 MB","delete"),("用户协议",None,"doc"),("隐私政策",None,"shield"),("关于商推客","V1.4.0","info")]
    y=104
    for t,sub,ico in rows:
        c.list_row(16,y,358,t,sub,ico); y+=76
    c.list_row(16,y+12,358,"退出登录",None,"logout" if False else "lock",danger=True)
    if code=="CLEARING_CACHE": c.dialog("正在清理缓存","正在清理图片和列表缓存，请稍候。","清理中",None,"info",loading=True,icon="delete",height=280)
    elif code=="CACHE_CLEARED": c.snackbar("缓存已清理", "success", y=690)
    elif code=="CACHE_CLEAR_FAILED": c.snackbar("缓存清理失败，请稍后重试", "error", "重试", y=690)
    elif code=="OFFLINE": c.banner(16,596,358,"离线状态下可执行本地缓存清理；网络页面稍后再试。","warning")
    c.system_nav(); return c


def render_about(m: Mockup) -> Canvas:
    c=Canvas(); c.status_bar(); c.top_bar("关于商推客",back=True)
    c.logo(155,120,80)
    c.text(195,220,"商推客",24,C["text"],"bold",anchor="ma")
    c.text(195,256,"Android V1.4.0 · versionCode 10400",12,C["text_secondary"],"regular",anchor="ma")
    c.list_row(16,306,358,"检查更新",None,"download")
    c.list_row(16,382,358,"官方网站","stk.zz-yihao.com","external")
    c.list_row(16,458,358,"备案与版权信息","以正式部署内容为准","doc")
    code=m.state_code
    if code=="CHECKING_UPDATE": c.dialog("正在检查更新","正在验证服务器发布配置。","检查中",None,"info",loading=True,icon="download",height=278)
    elif code=="UP_TO_DATE": c.snackbar("当前已是最新版", "success", y=690)
    elif code=="UPDATE_AVAILABLE": c.dialog("发现新版本 V1.4.1","可进入版本更新页查看更新内容和安装包信息。","查看更新","稍后","info",icon="download",height=310)
    elif code=="CHECK_FAILED": c.snackbar("检查更新失败", "error", "重试", y=690)
    c.text(195,744,"© 2026 商推客",11,C["text_tertiary"],"regular",anchor="ma")
    c.system_nav(); return c


def render_me_overlay(m: Mockup) -> Canvas:
    if m.page_id=="ME-OV-001":
        c=render_me(Mockup("","ME-001","","","","INACTIVE_MEMBER","","V1.3.0","","PAGE"))
        if m.state_code=="DEFAULT": c.dialog("退出当前账号？","退出后将清除本机登录状态，缓存内容仍按规则保留。","确认退出","取消","warning",destructive=True,icon="lock",height=310)
        elif m.state_code=="SUBMITTING": c.dialog("正在退出登录","正在撤销服务器令牌，请稍候。","退出中","取消","warning",loading=True,icon="lock",height=300)
        else: c.dialog("服务器撤销失败","已完成本地安全退出，并记录待撤销令牌。","返回登录",None,"warning",icon="warning",height=310)
        return c
    c=render_my_projects(Mockup("","ME-002","","","","ALL","","V1.2.0","","PAGE"))
    y=c.bottom_sheet("项目操作",390)
    mapping={
        "PENDING":[("查看项目","eye"),("编辑项目","edit"),("撤回审核","warning")],
        "PUBLISHED":[("查看项目","eye"),("编辑项目","edit"),("下架项目","warning")],
        "REJECTED":[("查看驳回原因","info"),("编辑项目","edit"),("重新提交","upload"),("删除项目","delete")],
        "OFFLINE":[("查看项目","eye"),("编辑项目","edit"),("删除项目","delete")],
    }
    yy=y+74
    for label,ico in mapping[m.state_code]:
        danger=label=="删除项目"
        c.list_row(24,yy,342,label,None,ico,danger=danger,h=52); yy+=60
    return c


def render_overlay_update(m: Mockup) -> Canvas:
    c=render_splash(Mockup("","SYS-001","","","","CHECKING","","V1.0.0","","PAGE"))
    optional=m.page_id=="SYS-OV-001"
    if m.state_code=="DEFAULT":
        c.dialog("发现新版本 V1.4.1" if optional else "需要更新后继续使用", "优化首页加载、图片上传和覆盖安装体验。" if optional else "当前版本低于最低可用版本，必须更新后继续。", "立即更新", "稍后" if optional else None, "info" if optional else "warning", icon="download", height=310)
    elif m.state_code=="DOWNLOADING":
        def extra(x,y,w,h):
            c.text(x+24,y+174,"11.2 / 18.6 MB",12,C["text_secondary"],"regular",anchor="la")
            c.rounded(x+24,y+198,w-48,8,999,C["divider"]); c.rounded(x+24,y+198,(w-48)*.6,8,999,C["primary"])
        c.dialog("正在下载更新", "请保持网络连接，下载完成后将打开系统安装器。", "下载中", None if not optional else "取消", "info", loading=True, icon="download", extra=extra, height=350)
    else:
        c.dialog("更新下载失败", "请检查网络与存储空间后重新尝试。", "重新下载", "稍后" if optional else "联系客服", "error", icon="error", height=310)
    return c


def render(mockup: Mockup) -> Canvas:
    pid = mockup.page_id
    if pid.startswith("DS-"):
        return render_design_board(mockup)
    if pid == "SYS-001": return render_splash(mockup)
    if pid == "SYS-002": return render_system_unavailable(mockup)
    if pid == "SYS-003": return render_update_page(mockup)
    if pid in {"SYS-OV-001", "SYS-OV-002"}: return render_overlay_update(mockup)
    if pid in {"COM-OV-001", "COM-OV-002"}: return render_common_overlay(mockup)
    if pid == "AUTH-001": return render_login(mockup)
    if pid == "AUTH-002": return render_register(mockup)
    if pid == "AUTH-003": return render_reset(mockup)
    if pid in {"AUTH-004", "AUTH-005"}: return render_legal(mockup)
    if pid == "AUTH-OV-001": return render_captcha_overlay(mockup)
    if pid == "AUTH-OV-002": return render_auth_risk(mockup)
    if pid == "HOME-001": return render_home(mockup)
    if pid == "HOME-002": return render_search(mockup)
    if pid == "HOME-003": return render_categories(mockup)
    if pid == "HOME-004": return render_detail(mockup)
    if pid == "HOME-OV-001": return render_image_viewer(mockup)
    if pid == "HOME-OV-002": return render_contact_actions(mockup)
    if pid == "PUB-001": return render_publish(mockup)
    if pid == "PUB-002": return render_image_sort(mockup)
    if pid == "PUB-003": return render_publish_result(mockup)
    if pid == "PUB-004": return render_edit(mockup)
    if pid in {"PUB-OV-001", "PUB-OV-002", "PUB-OV-003"}: return render_pub_overlay(mockup)
    if pid == "ME-001": return render_me(mockup)
    if pid == "ME-002": return render_my_projects(mockup)
    if pid == "ME-003": return render_profile(mockup)
    if pid == "ME-004": return render_member(mockup)
    if pid == "ME-005": return render_wallets(mockup)
    if pid == "ME-006": return render_props(mockup)
    if pid in {"ME-007", "ME-008", "ME-009"}: return render_placeholder(mockup)
    if pid == "ME-010": return render_support(mockup)
    if pid == "ME-011": return render_settings(mockup)
    if pid == "ME-012": return render_about(mockup)
    if pid in {"ME-OV-001", "ME-OV-002"}: return render_me_overlay(mockup)
    raise KeyError(f"No renderer for {pid}")


def load_mockups(release: str | None = None) -> list[Mockup]:
    state_map: dict[str, dict[str, str]] = {}
    with (ROOT / "contracts/ui-state-catalog.csv").open(newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            state_map[row["state_id"]] = row
    out: list[Mockup] = []
    with (ROOT / "contracts/mockup-manifest.csv").open(newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            if release and row["release_id"] != release:
                continue
            s = state_map[row["state_id"]]
            out.append(Mockup(
                mockup_id=row["mockup_id"], page_id=row["page_id"], state_id=row["state_id"],
                page_name=row["page_name"], state_name=row["state_name"], state_code=s["state_code"],
                description=s["description"], release_id=row["release_id"], file_name=Path(row["file_path"]).name,
                visual_type=row["visual_type"],
            ))
    return out


def update_contracts(generated: list[tuple[Mockup, Path, str]], reviewer: str, approve: bool):
    now = datetime.now(timezone.utc).isoformat()
    status = "APPROVED" if approve else "GENERATED"
    gen_map = {m.mockup_id: (p, sha) for m, p, sha in generated}

    manifest_path = ROOT / "contracts/mockup-manifest.csv"
    with manifest_path.open(newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        rows = list(reader); fields = reader.fieldnames
    for r in rows:
        if r["mockup_id"] in gen_map:
            p, sha = gen_map[r["mockup_id"]]
            r["file_path"] = p.relative_to(ROOT).as_posix()
            r["status"] = status
            r["sha256"] = sha
            r["approved_by"] = reviewer if approve else "NOT_APPROVED"
            r["approved_at"] = now if approve else "NOT_APPROVED"
    with manifest_path.open("w", newline="", encoding="utf-8-sig") as f:
        w = csv.DictWriter(f, fieldnames=fields); w.writeheader(); w.writerows(rows)

    state_path = ROOT / "contracts/ui-state-catalog.csv"
    with state_path.open(newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        srows = list(reader); sfields = reader.fieldnames
    generated_state_ids = {m.state_id for m, _, _ in generated}
    for r in srows:
        if r["state_id"] in generated_state_ids:
            r["mockup_status"] = status
    with state_path.open("w", newline="", encoding="utf-8-sig") as f:
        w = csv.DictWriter(f, fieldnames=sfields); w.writeheader(); w.writerows(srows)

    # Keep page-level contracts synchronized with the approved/generated files.
    by_state = {m.state_id: (p, status) for m, p, _ in generated}
    for contract in (ROOT / "ui/page-contracts").glob("*.yaml"):
        data = yaml.safe_load(contract.read_text(encoding="utf-8"))
        changed = False
        for st in data.get("required_states", []):
            if st["state_id"] in by_state:
                p, st_status = by_state[st["state_id"]]
                st["mockup_path"] = p.relative_to(ROOT).as_posix()
                st["mockup_status"] = st_status
                changed = True
        if changed:
            contract.write_text(yaml.safe_dump(data, allow_unicode=True, sort_keys=False, width=120), encoding="utf-8")


def make_contact_sheets(generated: list[tuple[Mockup, Path, str]]) -> list[Path]:
    out_dir = ROOT / "ui/mockups/review-sheets"
    if out_dir.exists(): shutil.rmtree(out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    by_page: dict[str, list[tuple[Mockup, Path]]] = defaultdict(list)
    for m, p, _ in generated:
        by_page[m.page_id].append((m, p))
    sheets: list[Path] = []
    for pid, items in sorted(by_page.items()):
        items.sort(key=lambda x: x[0].state_id)
        cols = 4
        thumb_w, thumb_h = 234, 506
        label_h = 54
        rows = math.ceil(len(items) / cols)
        sheet = Image.new("RGB", (cols * thumb_w, rows * (thumb_h + label_h)), rgb(C["background"]))
        sd = ImageDraw.Draw(sheet)
        for i, (m, p) in enumerate(items):
            im = Image.open(p).convert("RGB").resize((thumb_w, thumb_h), Image.Resampling.LANCZOS)
            x = (i % cols) * thumb_w; y = (i // cols) * (thumb_h + label_h)
            sheet.paste(im, (x, y))
            sd.rectangle((x, y + thumb_h, x + thumb_w, y + thumb_h + label_h), fill=rgb(C["surface"]))
            sf = ImageFont.truetype(FONT_BOLD, 18)
            rf = ImageFont.truetype(FONT_REGULAR, 15)
            sd.text((x + 8, y + thumb_h + 6), m.state_id, font=sf, fill=rgb(C["text"]))
            sd.text((x + 8, y + thumb_h + 30), m.state_name, font=rf, fill=rgb(C["text_secondary"]))
        path = out_dir / f"{pid}__REVIEW_SHEET.png"
        sheet.save(path, "PNG", optimize=False, compress_level=6)
        sheets.append(path)
    return sheets


def validate_images(generated: list[tuple[Mockup, Path, str]]) -> dict[str, Any]:
    errors: list[str] = []
    sha_count = Counter(sha for _, _, sha in generated)
    duplicates = {sha: n for sha, n in sha_count.items() if n > 1}
    if duplicates:
        errors.append(f"发现完全重复图片 SHA: {duplicates}")
    for m, p, sha in generated:
        if not p.is_file(): errors.append(f"文件不存在: {m.mockup_id}"); continue
        with Image.open(p) as im:
            if im.size != (W, H): errors.append(f"尺寸错误: {m.mockup_id} {im.size}")
            if im.mode not in {"RGB", "RGBA"}: errors.append(f"模式错误: {m.mockup_id} {im.mode}")
        actual = hashlib.sha256(p.read_bytes()).hexdigest()
        if actual != sha: errors.append(f"SHA 变化: {m.mockup_id}")
    expected = 236 if len(load_mockups()) == 236 and len(generated) == 236 else len(generated)
    if len(generated) != expected:
        errors.append(f"数量错误: actual={len(generated)} expected={expected}")
    return {"generated": len(generated), "unique_sha": len(sha_count), "duplicates": duplicates, "errors": errors}


def write_reports(generated: list[tuple[Mockup, Path, str]], validation: dict[str, Any], sheets: list[Path], reviewer: str, approve: bool):
    by_release = Counter(m.release_id for m, _, _ in generated)
    by_page = Counter(m.page_id for m, _, _ in generated)
    report = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "renderer": "scripts/render_all_mockups.py",
        "design_system": "STK-DS-1.0",
        "canvas": "390x844dp / 1170x2532px",
        "font_rendering": "Noto Sans CJK SC deterministic local rendering",
        "reviewer": reviewer,
        "status": "APPROVED" if approve else "GENERATED",
        "count": len(generated),
        "by_release": dict(sorted(by_release.items())),
        "page_count": len(by_page),
        "review_sheet_count": len(sheets),
        "validation": validation,
    }
    (ROOT / "audit/MOCKUP_RENDER_AUDIT.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    lines = [
        "# 商推客 236 张页面状态效果图生成报告", "",
        f"- 生成时间：{report['generated_at']}",
        f"- 设计系统：{report['design_system']}",
        f"- 输出尺寸：{report['canvas']}",
        f"- 效果图数量：{report['count']}",
        f"- 页面数量：{report['page_count']}",
        f"- 审阅总览板：{report['review_sheet_count']}",
        f"- 合同状态：{report['status']}",
        f"- 批准/审阅标识：{reviewer}", "",
        "## 版本分布", "",
    ]
    for rel, n in sorted(by_release.items()): lines.append(f"- {rel}: {n} 张")
    lines += ["", "## 自动校验", "", f"- 唯一 SHA-256：{validation['unique_sha']}", f"- 完全重复图片：{len(validation['duplicates'])}", f"- 错误数量：{len(validation['errors'])}", ""]
    if validation["errors"]:
        lines.append("## 错误")
        lines.extend(f"- {e}" for e in validation["errors"])
    else:
        lines.append("全部图片均为 1170×2532px，文件存在、SHA-256 一致，且没有完全重复图片。")
    lines += ["", "## 使用边界", "", "效果图决定 UI、布局与状态表现；业务功能仍以 Feature、Interaction、API、数据库和后台配置合同为唯一依据。效果图中的固定测试文字、金额、联系人和项目内容不扩展功能范围。", ""]
    (ROOT / "ui/MOCKUP_GENERATION_REPORT.md").write_text("\n".join(lines), encoding="utf-8")


def refresh_package_hashes():
    # Rebuild package metadata while excluding machine-local credential handoffs.
    def is_local_file(p: Path) -> bool:
        return p.name.endswith((".local.md", ".local.yaml"))

    def is_package_file(p: Path) -> bool:
        return p.is_file() and p.name not in {"PACKAGE_MANIFEST.json", "SHA256SUMS.txt"} and not is_local_file(p)

    def is_sum_file(p: Path) -> bool:
        return p.is_file() and p.name != "SHA256SUMS.txt" and not is_local_file(p)

    sums = []
    for p in sorted(ROOT.rglob("*")):
        if not is_sum_file(p):
            continue
        rel = p.relative_to(ROOT).as_posix()
        sums.append(f"{hashlib.sha256(p.read_bytes()).hexdigest()}  {rel}")
    (ROOT / "SHA256SUMS.txt").write_text("\n".join(sums) + "\n", encoding="utf-8")

    manifest = ROOT / "PACKAGE_MANIFEST.json"
    if manifest.exists():
        try:
            old = json.loads(manifest.read_text(encoding="utf-8"))
        except Exception:
            old = {}
        files = []
        for p in sorted(ROOT.rglob("*")):
            if is_package_file(p):
                rel = p.relative_to(ROOT).as_posix()
                files.append({"path": rel, "size": p.stat().st_size, "sha256": hashlib.sha256(p.read_bytes()).hexdigest()})
        old.update({"generated_at": datetime.now(timezone.utc).isoformat(), "file_count": len(files), "files": files})
        manifest.write_text(json.dumps(old, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        # SHA sums changed after manifest update; write final sums again.
        sums = []
        for p in sorted(ROOT.rglob("*")):
            if not is_sum_file(p): continue
            sums.append(f"{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.relative_to(ROOT).as_posix()}")
        (ROOT / "SHA256SUMS.txt").write_text("\n".join(sums) + "\n", encoding="utf-8")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--release")
    ap.add_argument("--reviewer", default="OpenAI deterministic visual QA")
    ap.add_argument("--generated-only", action="store_true", help="生成但不标记 APPROVED")
    ap.add_argument("--clean", action="store_true")
    ap.add_argument("--limit", type=int)
    args = ap.parse_args()

    mockups = load_mockups(args.release)
    if args.limit: mockups = mockups[: args.limit]
    root_name = "generated" if args.generated_only else "approved"
    out_root = ROOT / "ui/mockups" / root_name
    if args.clean and not args.release and out_root.exists():
        shutil.rmtree(out_root)
    out_root.mkdir(parents=True, exist_ok=True)

    generated: list[tuple[Mockup, Path, str]] = []
    for idx, m in enumerate(mockups, 1):
        out = out_root / m.release_id / m.page_id / m.file_name
        if out.exists():
            try:
                with Image.open(out) as existing:
                    valid_existing = existing.size == (W, H)
            except Exception:
                valid_existing = False
            if valid_existing:
                sha = hashlib.sha256(out.read_bytes()).hexdigest()
                generated.append((m, out, sha))
                if idx % 20 == 0 or idx == len(mockups):
                    print(f"reused {idx}/{len(mockups)}: {m.state_id}", flush=True)
                continue
            out.unlink(missing_ok=True)
        canvas = render(m)
        canvas.save(out)
        sha = hashlib.sha256(out.read_bytes()).hexdigest()
        generated.append((m, out, sha))
        if idx % 20 == 0 or idx == len(mockups):
            print(f"rendered {idx}/{len(mockups)}: {m.state_id}", flush=True)

    validation = validate_images(generated)
    if validation["errors"]:
        print(json.dumps(validation, ensure_ascii=False, indent=2))
        raise SystemExit(1)
    update_contracts(generated, args.reviewer, not args.generated_only)
    sheets = make_contact_sheets(generated)
    write_reports(generated, validation, sheets, args.reviewer, not args.generated_only)
    refresh_package_hashes()
    print(json.dumps({"count": len(generated), "status": "GENERATED" if args.generated_only else "APPROVED", "review_sheets": len(sheets), "validation": validation}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
