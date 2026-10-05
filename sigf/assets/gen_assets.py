#!/usr/bin/env python3
"""Original replacement art and sounds for ArcadeMod (Bay4lly/ArcadeMod @ eb09571, GPL-3.0).

Every pixel and every sample below is produced by this script (pixel art and chiptunes designed by Claude for SIGF);
nothing is read from the upstream assets. Layouts (sprite-sheet cells, model UV rectangles, the maze wall map)
come from the upstream *code and model JSON* so the new art lands where the game code samples it.

    python gen_assets.py <out_dir> [--upstream <checkout>]

<out_dir> receives a tree mirroring src/main/resources (assets/arcademod/..., icon.png). --upstream is only used to
read the cabinet / plushie / prize-counter model JSON (UV rectangles); without it the JSON copies in models/ are used.
Requires: numpy, Pillow, scipy, soundfile (libsndfile with Vorbis).
"""
import json, math, os, sys
import numpy as np
from PIL import Image
from scipy import ndimage
import soundfile as sf

HERE = os.path.dirname(os.path.abspath(__file__))
A = 'assets/arcademod/'


def hexc(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(round(v * f)))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)


class Canvas:
    def __init__(self, w, h, bg=(0, 0, 0, 0)):
        self.a = np.zeros((h, w, 4), np.uint8)
        self.a[:, :] = bg
        self.w, self.h = w, h

    def px(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.a[y, x] = c

    def rect(self, x, y, w, h, c):
        x0, y0, x1, y1 = max(0, x), max(0, y), min(self.w, x + w), min(self.h, y + h)
        if x1 > x0 and y1 > y0:
            self.a[y0:y1, x0:x1] = c

    def frame(self, x, y, w, h, c, t=1):
        self.rect(x, y, w, t, c); self.rect(x, y + h - t, w, t, c)
        self.rect(x, y, t, h, c); self.rect(x + w - t, y, t, h, c)

    def blit(self, img, x, y, scale=1):
        """Alpha-over a small RGBA array (h, w, 4) at (x, y), nearest-neighbour scaled."""
        if scale != 1:
            img = np.repeat(np.repeat(img, scale, 0), scale, 1)
        h, w = img.shape[:2]
        for j in range(h):
            yy = y + j
            if not 0 <= yy < self.h:
                continue
            for i in range(w):
                xx = x + i
                if 0 <= xx < self.w and img[j, i, 3] > 0:
                    if img[j, i, 3] == 255:
                        self.a[yy, xx] = img[j, i]
                    else:
                        al = img[j, i, 3] / 255.0
                        base = self.a[yy, xx].astype(float)
                        out = img[j, i, :3] * al + base[:3] * (1 - al)
                        self.a[yy, xx, :3] = out.astype(np.uint8)
                        self.a[yy, xx, 3] = max(int(base[3]), int(img[j, i, 3]))

    def image(self, mode='RGBA'):
        im = Image.fromarray(self.a)
        return im if mode == 'RGBA' else im.convert(mode)


def sprite(rows, pal):
    """ASCII pixel art -> RGBA array. '.' is transparent."""
    h, w = len(rows), max(len(r) for r in rows)
    out = np.zeros((h, w, 4), np.uint8)
    for j, r in enumerate(rows):
        for i, ch in enumerate(r):
            if ch != '.':
                out[j, i] = pal[ch]
    return out


def flip(s):
    return s[:, ::-1].copy()


def rot(s, k):  # k * 90 degrees counter-clockwise
    return np.rot90(s, k).copy()


# ---------------------------------------------------------------------------------------------------------------
# 5x7 pixel font (designed for this file) and a 3x5 mini font
# ---------------------------------------------------------------------------------------------------------------
F5 = {
    'A': ['.###.', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
    'B': ['####.', '#...#', '#...#', '####.', '#...#', '#...#', '####.'],
    'C': ['.####', '#....', '#....', '#....', '#....', '#....', '.####'],
    'D': ['####.', '#...#', '#...#', '#...#', '#...#', '#...#', '####.'],
    'E': ['#####', '#....', '#....', '####.', '#....', '#....', '#####'],
    'F': ['#####', '#....', '#....', '####.', '#....', '#....', '#....'],
    'G': ['.####', '#....', '#....', '#.###', '#...#', '#...#', '.###.'],
    'H': ['#...#', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
    'I': ['#####', '..#..', '..#..', '..#..', '..#..', '..#..', '#####'],
    'J': ['..###', '...#.', '...#.', '...#.', '...#.', '#..#.', '.##..'],
    'K': ['#...#', '#..#.', '#.#..', '##...', '#.#..', '#..#.', '#...#'],
    'L': ['#....', '#....', '#....', '#....', '#....', '#....', '#####'],
    'M': ['#...#', '##.##', '#.#.#', '#.#.#', '#...#', '#...#', '#...#'],
    'N': ['#...#', '##..#', '#.#.#', '#..##', '#...#', '#...#', '#...#'],
    'O': ['.###.', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
    'P': ['####.', '#...#', '#...#', '####.', '#....', '#....', '#....'],
    'Q': ['.###.', '#...#', '#...#', '#...#', '#.#.#', '#..#.', '.##.#'],
    'R': ['####.', '#...#', '#...#', '####.', '#.#..', '#..#.', '#...#'],
    'S': ['.####', '#....', '#....', '.###.', '....#', '....#', '####.'],
    'T': ['#####', '..#..', '..#..', '..#..', '..#..', '..#..', '..#..'],
    'U': ['#...#', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
    'V': ['#...#', '#...#', '#...#', '#...#', '.#.#.', '.#.#.', '..#..'],
    'W': ['#...#', '#...#', '#...#', '#.#.#', '#.#.#', '##.##', '#...#'],
    'X': ['#...#', '.#.#.', '..#..', '..#..', '..#..', '.#.#.', '#...#'],
    'Y': ['#...#', '.#.#.', '..#..', '..#..', '..#..', '..#..', '..#..'],
    'Z': ['#####', '....#', '...#.', '..#..', '.#...', '#....', '#####'],
    '0': ['.###.', '#..##', '#.#.#', '#.#.#', '##..#', '#...#', '.###.'],
    '1': ['..#..', '.##..', '..#..', '..#..', '..#..', '..#..', '.###.'],
    '!': ['..#..', '..#..', '..#..', '..#..', '..#..', '.....', '..#..'],
    ' ': ['.....'] * 7,
}
F3 = {
    'H': ['#.#', '#.#', '###', '#.#', '#.#'], 'E': ['###', '#..', '##.', '#..', '###'],
    'L': ['#..', '#..', '#..', '#..', '###'], 'P': ['##.', '#.#', '##.', '#..', '#..'],
    '!': ['.#.', '.#.', '.#.', '...', '.#.'], 'C': ['.##', '#..', '#..', '#..', '.##'],
    'O': ['.#.', '#.#', '#.#', '#.#', '.#.'], 'I': ['###', '.#.', '.#.', '.#.', '###'],
    'N': ['#.#', '###', '###', '###', '#.#'], ' ': ['...'] * 5,
    'A': ['.#.', '#.#', '###', '#.#', '#.#'], 'R': ['##.', '#.#', '##.', '#.#', '#.#'], 'D': ['##.', '#.#', '#.#', '#.#', '##.'],
}


def text_sprite(s, color, font=F5, gap=1):
    glyphs = [font[ch] for ch in s]
    gw, gh = len(glyphs[0][0]), len(glyphs[0])
    w = len(glyphs) * (gw + gap) - gap
    out = np.zeros((gh, w, 4), np.uint8)
    for k, g in enumerate(glyphs):
        for j, row in enumerate(g):
            for i, ch in enumerate(row):
                if ch == '#':
                    out[j, k * (gw + gap) + i] = color
    return out


def outlined(s, color):
    """Add a 1px outline of `color` around a sprite (grows it by 1px on each side)."""
    h, w = s.shape[:2]
    big = np.zeros((h + 2, w + 2, 4), np.uint8)
    m = s[:, :, 3] > 0
    mm = np.zeros((h + 2, w + 2), bool)
    mm[1:-1, 1:-1] = m
    dil = ndimage.binary_dilation(mm, structure=np.ones((3, 3)))
    big[dil & ~mm] = color
    big[1:-1, 1:-1][m] = s[m]
    return big


# ---------------------------------------------------------------------------------------------------------------
# Shared characters
# ---------------------------------------------------------------------------------------------------------------
MINT = hexc('#3fd6a3'); MINT_D = hexc('#127a5a'); MINT_L = hexc('#b4f7de'); MOUTH = hexc('#3a0d1e')
WHITE = hexc('#ffffff'); BLACK = hexc('#000000')


def muncher(open_amt, face='right'):
    """'Pellet Muncher' hero: a mint round critter with one eye and a box mouth full of teeth (15x15, facing right)."""
    s = np.zeros((15, 15, 4), np.uint8)
    cx, cy, r = 7.0, 7.0, 6.4
    for y in range(15):
        for x in range(15):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                s[y, x] = MINT_D if d > r - 1.1 else MINT
    # highlight (upper-left)
    for (x, y) in [(4, 3), (5, 3), (3, 4), (3, 5), (4, 4)]:
        s[y, x] = MINT_L
    # eye
    s[3:6, 7:10] = WHITE
    s[4, 8] = BLACK; s[4, 9] = BLACK
    # mouth: a box slot on the right half, height grows with open_amt
    top, bot = 8 - open_amt, 9 + open_amt
    for y in range(top, bot + 1):
        for x in range(8, 15):
            if s[y, x, 3]:
                s[y, x] = MOUTH
    if open_amt > 0:
        for x in range(9, 14, 2):  # teeth
            if s[top, x, 3]: s[top, x] = WHITE
            if s[bot, x, 3]: s[bot, x] = WHITE
    else:
        for x in range(9, 14, 2):
            s[8, x] = WHITE
    return s


def drone_body(frame):
    """Maze chaser 'drone', white so the game can tint it (14x14)."""
    W = hexc('#ffffff'); G = hexc('#d4d4d4'); D = hexc('#a8a8a8')
    s = np.zeros((14, 14, 4), np.uint8)
    # antenna
    tip = 6 if frame == 0 else 7
    s[0, tip] = W; s[1, 6:8] = D
    # rounded box body rows 2..11
    for y in range(2, 12):
        x0 = 1 if y in (2, 11) else 0
        s[y, x0:14 - x0] = W
        s[y, 14 - x0 - 1] = G
    s[11, 1:13] = G
    # side fins
    s[5:9, 0] = D; s[5:9, 13] = D
    # thrusters
    xs = (2, 6, 10) if frame == 0 else (4, 8)
    for x in xs:
        s[12, x:x + 2] = G
        s[13, x:x + 2] = D if frame == 0 else W
    return s


def worker(kind):
    """'Girder Climber' hero: hard hat, hi-vis vest (right-facing 15x16; climb is 16x15 back view)."""
    pal = {'Y': hexc('#f2c230'), 'y': hexc('#b8860b'), 's': hexc('#f1c27d'), 'k': hexc('#2b2b2b'),
           'O': hexc('#ff7f1f'), 'G': hexc('#3e8e41'), 'B': hexc('#4a5a78'), 'b': hexc('#5b3a1e'), 'W': hexc('#e8f0f0')}
    head = ['......YYYY.....', '.....YYYYYY....', '....yyyyyyyyy..', '.....ssssk.....', '.....sssss.....',
            '......sss......', '....GOOOOOG....', '...GOWWWWOG....', '...sOOOOOOs....', '.....OOOOO.....',
            '.....BBBBB.....']
    legs = {
        'stand': ['.....BB.BB.....', '.....BB.BB.....', '.....BB.BB.....', '....bbb.bbb....', '...............'],
        'walk1': ['....BB...BB....', '...BB.....BB...', '...B.......B...', '..bb.......bb..', '...............'],
        'walk2': ['.....BBBB......', '......BBB......', '......BB.......', '.....bbbb......', '...............'],
    }
    if kind == 'climb':
        rows = ['..s..YYYYYY..s..', '..s.YYYYYYYY.s..', '..G..ssssss..G..', '..G.OOOOOOOO.G..', '...GOWWWWWWOG...',
                '....OOOOOOOO....', '....OOOOOOOO....', '.....BBBBBB.....', '.....BB..BB.....', '.....BB..BB.....',
                '.....BB..BB.....', '....bbb..bbb....', '................', '................', '................']
        return sprite(rows, pal)
    return sprite(head + legs[kind], pal)


# ---------------------------------------------------------------------------------------------------------------
# GUI frame (Minecraft-style bevel window with a black play area)
# ---------------------------------------------------------------------------------------------------------------
def gui_frame(c, w, h):
    LIGHT = hexc('#ffffff'); MID = hexc('#c6c6c6'); DARK = hexc('#555555'); EDGE = hexc('#1e1e1e')
    c.rect(1, 1, w - 2, h - 2, MID)
    c.rect(1, 0, w - 2, 1, EDGE); c.rect(1, h - 1, w - 2, 1, EDGE); c.rect(0, 1, 1, h - 2, EDGE); c.rect(w - 1, 1, 1, h - 2, EDGE)
    c.rect(1, 1, w - 3, 2, LIGHT); c.rect(1, 1, 2, h - 3, LIGHT)
    c.rect(3, h - 3, w - 4, 2, DARK); c.rect(w - 3, 3, 2, h - 4, DARK)
    c.rect(5, 5, w - 10, h - 10, BLACK)
    c.rect(5, 5, w - 10, 1, DARK); c.rect(5, 5, 1, h - 10, DARK)


# ---------------------------------------------------------------------------------------------------------------
# Pellet Muncher sheet (textures/gui/pacman.png, 512x512). Cells from GuiPacMan.java.
# ---------------------------------------------------------------------------------------------------------------
MAZE = """
WWWWWWWWWWWWWWWWWWWWWWWWWWWW
W............WW............W
W.WWWW.WWWWW.WW.WWWWW.WWWW.W
W.WWWW.WWWWW.WW.WWWWW.WWWW.W
W.WWWW.WWWWW.WW.WWWWW.WWWW.W
W..........................W
W.WWWW.WW.WWWWWWWW.WW.WWWW.W
W.WWWW.WW.WWWWWWWW.WW.WWWW.W
W......WW....WW....WW......W
WWWWWW.WWWWW.WW.WWWWW.WWWWWW
WWWWWW.WWWWW.WW.WWWWW.WWWWWW
WWWWWW.WW..........WW.WWWWWW
WWWWWW.WW.WWWggWWW.WW.WWWWWW
WWWWWW.WW.WggggggW.WW.WWWWWW
T.........WggggggW.........T
WWWWWW.WW.WggggggW.WW.WWWWWW
WWWWWW.WW.WWWWWWWW.WW.WWWWWW
WWWWWW.WW..........WW.WWWWWW
WWWWWW.WW.WWWWWWWW.WW.WWWWWW
WWWWWW.WW.WWWWWWWW.WW.WWWWWW
W............WW............W
W.WWWW.WWWWW.WW.WWWWW.WWWW.W
W.WWWW.WWWWW.WW.WWWWW.WWWW.W
W...WW................WW...W
WWW.WW.WW.WWWWWWWW.WW.WW.WWW
WWW.WW.WW.WWWWWWWW.WW.WW.WWW
W......WW....WW....WW......W
W.WWWWWWWWWW.WW.WWWWWWWWWW.W
W.WWWWWWWWWW.WW.WWWWWWWWWW.W
W..........................W
WWWWWWWWWWWWWWWWWWWWWWWWWWWW
""".split()  # = the wall map built by GuiPacMan.setupTiles() (game logic, GPL-3.0 upstream code)


def maze_layer():
    """224x248 white maze: rounded wall blocks (outline + translucent fill), tinted by the game."""
    tiles = np.array([[ch == 'W' for ch in row] for row in MAZE])
    m = np.repeat(np.repeat(tiles, 8, 0), 8, 1)  # 248 x 224
    pad = np.pad(m, 6, constant_values=True)     # outside of the board counts as wall
    d = ndimage.distance_transform_edt(pad)[6:-6, 6:-6]
    out = np.zeros((248, 224, 4), np.uint8)
    body = d > 3.0
    edge = body & (d <= 4.2)
    out[body] = (255, 255, 255, 70)
    out[edge] = (255, 255, 255, 255)
    # dotted inner rib every 4px for texture
    rib = body & (d > 6.5) & (d <= 7.5)
    yy, xx = np.nonzero(rib)
    for y, x in zip(yy, xx):
        if (x + y) % 4 == 0:
            out[y, x] = (255, 255, 255, 170)
    return out


def bonus(i):
    """Eight original bonus treats (14x14, own colours)."""
    s = np.zeros((14, 14, 4), np.uint8)
    C = lambda h: hexc(h)
    if i == 0:  # donut
        for y in range(14):
            for x in range(14):
                d = math.hypot(x - 6.5, y - 7)
                if 2.2 < d <= 6.2: s[y, x] = C('#d9a066') if y > 7 else C('#ff77b7')
        for (x, y) in [(4, 3), (8, 2), (10, 5), (3, 6), (9, 7)]: s[y, x] = C('#fff3a0')
    elif i == 1:  # cupcake with a star
        for y in range(8, 14): s[y, 3 + (y - 8) // 3:11 - (y - 8) // 3] = C('#4aa3df')
        for y in range(4, 8): s[y, 2:12] = C('#fff0f5')
        s[3, 4:10] = C('#fff0f5')
        s[1, 7] = C('#ffd23f'); s[2, 6:9] = C('#ffd23f'); s[3, 7] = C('#ffd23f')
    elif i == 2:  # ice-cream cone
        for y in range(1, 7):
            for x in range(2, 12):
                if math.hypot(x - 6.5, y - 5) <= 4.6: s[y, x] = C('#9be7ff') if y < 4 else C('#c3a6ff')
        for y in range(7, 14):
            w = (14 - y) // 2
            s[y, 7 - w:7 + w] = C('#e0a456')
            if y % 2: s[y, 7 - w:7 + w:2] = C('#b5782f')
    elif i == 3:  # lollipop
        for y in range(9):
            for x in range(1, 13):
                d = math.hypot(x - 6.5, y - 4.5)
                if d <= 4.6:
                    a = (math.atan2(y - 4.5, x - 6.5) + d * 0.9) % (2 * math.pi)
                    s[y, x] = C('#ff4d6d') if a < math.pi else C('#ffffff')
        s[9:14, 6:8] = C('#f5f5dc')
    elif i == 4:  # cookie
        for y in range(14):
            for x in range(14):
                if math.hypot(x - 6.5, y - 6.5) <= 6.2: s[y, x] = C('#d39b52')
        for (x, y) in [(4, 4), (8, 3), (9, 8), (5, 9), (3, 7), (10, 5)]: s[y, x] = C('#4b2a14')
    elif i == 5:  # gem
        for y in range(1, 13):
            w = 6 - abs(y - 5) if y <= 5 else max(0, 6 - (y - 5) * 6 // 7)
            if y <= 5: w = min(6, 3 + y)
            s[y, 7 - w:7 + w] = C('#2ee6d6')
        s[2:5, 5:7] = C('#d6fffb'); s[6:12, 7:9] = C('#17a89c')
    elif i == 6:  # crown
        s[6:12, 1:13] = C('#ffcc33')
        for x in (1, 6, 11): s[3:6, x:x + 2] = C('#ffcc33')
        s[8, 3] = C('#ff3b3b'); s[8, 7] = C('#3b7bff'); s[8, 10] = C('#3bff7a'); s[11, 1:13] = C('#c98e00')
    else:  # trophy
        s[1:7, 3:11] = C('#ffd23f'); s[2:5, 1:3] = C('#e0a800'); s[2:5, 11:13] = C('#e0a800')
        s[7:10, 6:8] = C('#e0a800'); s[10:12, 4:10] = C('#ffd23f'); s[12:13, 3:11] = C('#8a5a00')
        s[2:5, 4] = C('#fff6c0')
    return s


def gen_muncher_sheet():
    c = Canvas(512, 512)
    gui_frame(c, 234, 284)
    c.blit(maze_layer(), 234, 0)
    GX, MY = 234, 248
    c.rect(GX, MY, 2, 2, hexc('#ffd9a0'))                                   # pellet (DOT)
    c.rect(GX, MY + 2, 2, 2, WHITE)                                         # pupil (tinted)
    star = sprite(['...##...', '..#..#..', '.#.##.#.', '#.####.#', '#.####.#', '.#.##.#.', '..#..#..', '...##...'],
                  {'#': hexc('#ffe28a')})
    c.blit(star, GX + 2, MY)                                                # power pellet (ENERGIZER 8x8)
    c.blit(sprite(['.##.', '####', '####', '####', '.##.'], {'#': WHITE}), GX + 6, MY + 8)   # eye 4x5
    c.blit(sprite(['#..#..#..#..', '.##..##..##.'], {'#': WHITE}), GX, MY + 14)              # scared mouth 12x2
    c.rect(GX + 8, 264, 16, 2, hexc('#ff9e3d'))                             # house door
    c.blit(drone_body(0), GX + 10, MY); c.blit(drone_body(1), GX + 24, MY)
    for i in range(8):
        c.blit(bonus(i), GX + 8 + 2 + 28 + 14 * i, MY)
    # hero rows: y=300 right, 315 left, 330 up (+ pop animation), 345 down; frame 0 closed, frame 1 open
    for row, k in ((0, None), (1, 'L'), (2, 'U'), (3, 'D')):
        for f in (0, 1):
            s = muncher(0 if f == 0 else 2)
            if k == 'L': s = flip(s)
            elif k == 'U': s = rot(s, 1)
            elif k == 'D': s = rot(s, 3)
            c.blit(s, 15 * f, 300 + 15 * row)
    # pop animation (deathAnimation 2..12 on row y=330)
    for k in range(2, 13):
        s = np.zeros((15, 15, 4), np.uint8)
        if k <= 7:
            r = 6.4 - (k - 2) * 1.1
            ry = r * (1 - (k - 2) * 0.08)
            for y in range(15):
                for x in range(15):
                    if ((x - 7) / max(r, .5)) ** 2 + ((y - 7) / max(ry, .5)) ** 2 <= 1:
                        s[y, x] = MINT if k % 2 else MINT_D
        else:
            rr = 2 + (k - 8) * 1.4
            for a in range(8):
                x = int(round(7 + rr * math.cos(a * math.pi / 4))); y = int(round(7 + rr * math.sin(a * math.pi / 4)))
                if 0 <= x < 15 and 0 <= y < 15: s[y, x] = MINT_L if k < 12 else MINT_D
        c.blit(s, 15 * k, 330)
    return c


# ---------------------------------------------------------------------------------------------------------------
# Alien Barrage sheet (textures/gui/spaceinvaders.png, 512x512). Cells from GuiSpaceInvaders.java.
# ---------------------------------------------------------------------------------------------------------------
def alien(t, f):
    W = WHITE; G = hexc('#c8c8c8')
    s = np.zeros((14, 14, 4), np.uint8)
    if t == 0:  # spike orb with a hollow eye
        for y in range(14):
            for x in range(14):
                d = math.hypot(x - 6.5, y - 6.5)
                if d <= 4.3 and not d <= 1.6: s[y, x] = W
        pts = [(6, 0), (7, 0), (6, 13), (7, 13), (0, 6), (0, 7), (13, 6), (13, 7)] if f == 0 else \
              [(1, 1), (12, 1), (1, 12), (12, 12), (2, 2), (11, 2), (2, 11), (11, 11)]
        for x, y in pts: s[y, x] = G
        if f == 0:
            s[1, 6:8] = W; s[12, 6:8] = W; s[6:8, 1] = W; s[6:8, 12] = W
    elif t == 1:  # beetle with flapping wings
        s[4:11, 4:10] = W; s[3, 5:9] = W; s[11, 5:9] = W
        s[6, 5] = 0; s[6, 8] = 0
        if f == 0:
            for i in range(4): s[3 - i // 2 + i, 3 - i] = G; s[3 - i // 2 + i, 10 + i] = G
        else:
            for i in range(4): s[8 + i // 2, 3 - i] = G; s[8 + i // 2, 10 + i] = G
        s[1:3, 5] = W; s[1:3, 8] = W
    elif t == 2:  # jelly with swaying tentacles
        for y in range(1, 7):
            for x in range(14):
                if ((x - 6.5) / 6) ** 2 + ((y - 6) / 5) ** 2 <= 1: s[y, x] = W
        s[7, 1:13] = G
        s[4, 4:6] = 0; s[4, 8:10] = 0
        for k, x in enumerate((2, 5, 8, 11)):
            for y in range(8, 14):
                dx = (1 if ((y + k + f) // 2) % 2 else 0)
                s[y, min(13, x + dx)] = W
    elif t == 3:  # diamond skull ship
        for y in range(12):
            w = y + 1 if y < 6 else 12 - y
            s[y, 7 - w:7 + w] = W
        s[4:6, 4:6] = 0; s[4:6, 8:10] = 0; s[8, 6:8] = 0
        if f == 0: s[12:14, 3] = G; s[12:14, 10] = G
        else: s[12:14, 5] = G; s[12:14, 8] = G
    else:  # comet pod with blinking lights
        for y in range(3, 11):
            w = 7 - abs(y - 6.5) // 1.5
            s[y, int(7 - w):int(7 + w)] = W
        s[1:3, 5:9] = G
        for i, x in enumerate((2, 5, 8, 11)):
            s[7, x] = 0 if (i + f) % 2 else G
    return s


def gen_alien_sheet():
    c = Canvas(512, 512)
    gui_frame(c, 234, 284)
    # play-field border (234,0, 224x248), white -> tinted by the game
    b = np.zeros((248, 224, 4), np.uint8)
    cb = Canvas(224, 248)
    cb.frame(1, 1, 222, 246, WHITE, 2)
    for (x, y) in [(1, 1), (2, 1), (1, 2), (221, 1), (222, 1), (222, 2), (1, 245), (1, 246), (2, 246), (222, 245), (221, 246), (222, 246)]:
        cb.px(x, y, (0, 0, 0, 0))
    for x in range(12, 212, 20):
        cb.rect(x, 4, 4, 1, (255, 255, 255, 140)); cb.rect(x + 8, 243, 4, 1, (255, 255, 255, 140))
    c.blit(cb.a, 234, 0)
    GX, MY = 234, 248
    for t in range(5):
        for f in range(2):
            c.blit(alien(t, f), GX + 10 + 28 * t + 14 * f, MY)
    # bombs (two frames) at (240,270) and (257,270)
    for k, x0 in enumerate((240, 257)):
        z = sprite(['.#.', '#..', '.#.', '..#', '.#.', '#..', '.#.'] if k == 0 else
                   ['.#.', '..#', '.#.', '#..', '.#.', '..#', '.#.'], {'#': WHITE})
        c.blit(z, x0 + 5, 273)
    # laser bolt (30,301, 14x4)
    c.rect(36, 301, 2, 4, WHITE)
    # player cannon (0,300) and lives icon
    ship = sprite(['.......#.......', '.......#.......', '......###......', '......#c#......', '.....##c##.....',
                   '...#########...', '..#tttttttt##..', '.#ttttttttttt#.', '#tttdttttdttt##', '#ttttttttttttt#',
                   '.#####...#####.', '.ooo.......ooo.'],
                  {'#': hexc('#d8fff4'), 't': hexc('#23b39b'), 'c': hexc('#7ff7ff'), 'd': hexc('#0b5d52'), 'o': hexc('#ff9e3d')})
    c.blit(ship, 0, 302)
    # explosion frames (y=330, 13 frames)
    for k in range(13):
        s = np.zeros((15, 15, 4), np.uint8)
        if k == 0:
            s[:12] = 0
            s2 = ship.copy(); s2[s2[:, :, 3] > 0] = hexc('#ff9e3d'); s[2:14, 0:15] = s2[:12]
        else:
            rng = np.random.RandomState(100 + k)
            r = 1 + k * 0.6
            for _ in range(10 + k * 2):
                a = rng.uniform(0, 2 * math.pi); d = rng.uniform(0, r)
                x = int(round(7 + d * math.cos(a))); y = int(round(8 + d * math.sin(a) * 0.8))
                if 0 <= x < 15 and 0 <= y < 15:
                    s[y, x] = hexc('#fff2a8') if d < r * 0.4 and k < 8 else (hexc('#ff9e3d') if d < r * 0.75 else hexc('#c4302b'))
        c.blit(s, 15 * k, 330)
    return c


# ---------------------------------------------------------------------------------------------------------------
# Girder Climber (textures/gui/kong/*). Cells from GuiKong.java.
# ---------------------------------------------------------------------------------------------------------------
def junk_robot(arms_up):
    """46x32 boss: a boxy scrap robot that hurls steel drums."""
    c = Canvas(46, 32)
    O = hexc('#2d3138'); M = hexc('#8c96a0'); L = hexc('#c3ccd4'); R = hexc('#ff3b3b'); Y = hexc('#f2c230'); T = hexc('#4a4f57')
    # treads
    c.rect(9, 27, 28, 5, O); c.rect(10, 28, 26, 3, T)
    for x in range(11, 36, 4): c.rect(x, 29, 2, 1, L)
    # body
    c.rect(12, 13, 22, 14, O); c.rect(13, 14, 20, 12, M); c.rect(13, 14, 20, 2, L)
    c.rect(16, 18, 14, 6, T); c.rect(18, 20, 3, 2, Y); c.rect(25, 20, 3, 2, Y)
    # head
    c.rect(16, 3, 14, 10, O); c.rect(17, 4, 12, 8, M); c.rect(18, 6, 10, 3, R); c.rect(19, 7, 2, 1, hexc('#ffd0d0'))
    c.rect(22, 0, 2, 3, O); c.rect(21, 0, 4, 1, R)
    if arms_up:
        c.rect(6, 1, 6, 14, O); c.rect(7, 2, 4, 12, M); c.rect(34, 1, 6, 14, O); c.rect(35, 2, 4, 12, M)
        c.rect(4, 0, 10, 3, O); c.rect(32, 0, 10, 3, O)
    else:
        c.rect(5, 13, 7, 14, O); c.rect(6, 14, 5, 12, M); c.rect(34, 13, 7, 14, O); c.rect(35, 14, 5, 12, M)
        c.rect(4, 24, 9, 4, O); c.rect(33, 24, 9, 4, O)
    return c.a


def drum(frame):
    c = Canvas(12, 10)
    B = hexc('#2f6fb5'); D = hexc('#1b3f6b'); L = hexc('#9cc9f5')
    c.rect(1, 0, 10, 10, B); c.rect(0, 1, 12, 8, B)
    c.rect(0, 1, 1, 8, D); c.rect(11, 1, 1, 8, D); c.rect(1, 0, 10, 1, D); c.rect(1, 9, 10, 1, D)
    x = 2 + frame * 2
    c.rect(x, 1, 2, 8, L)
    c.rect(1, 4, 10, 1, D)
    return c.a


def gen_girder_sheet():
    c = Canvas(308, 287)
    right = {'stand': (158, 3), 'walk1': (176, 4), 'walk2': (197, 3)}
    left = {'stand': (136, 3), 'walk1': (115, 4), 'walk2': (94, 3)}
    for k, (x, y) in right.items(): c.blit(worker(k), x, y)
    for k, (x, y) in left.items(): c.blit(flip(worker(k)), x, y)
    c.blit(worker('climb'), 145, 24)
    for i, (x, y) in enumerate(((66, 258), (81, 258), (66, 270), (81, 270))):
        c.blit(drum(i), x, y)
    c.blit(junk_robot(False), 58, 152)
    c.blit(junk_robot(True), 202, 152)
    return c


def gen_rescue():
    """textures/gui/kong/peach.png (39x22): the goal, a lost kitten calling for help."""
    c = Canvas(39, 22)
    pal = {'o': hexc('#ff9c3a'), 'd': hexc('#c46a12'), 'k': hexc('#1b1b1b'), 'w': hexc('#ffffff'), 'p': hexc('#ff8fb1')}
    cat = sprite(['.d.....d.', 'ddd...ddd', 'ooooooooo', 'okwoooowk', 'ooooppooo', '.ooooooo.', '..ooooo..',
                  '.ooooooo.', 'ooodooooo', 'oooodoooo', 'ooooodooo', 'ooooooooo', '.oo...oo.', 'dd.....dd'], pal)
    c.blit(cat, 2, 7)
    c.rect(14, 1, 24, 11, hexc('#1b1b1b')); c.rect(15, 2, 22, 9, WHITE)
    c.blit(text_sprite('HELP!', hexc('#e0245e'), F3), 17, 4)
    c.rect(14, 12, 3, 2, hexc('#1b1b1b')); c.px(13, 14, hexc('#1b1b1b'))
    return c


def gen_platform():
    """platform.png (150x8, RGB): steel truss girder."""
    c = Canvas(150, 8, hexc('#14202b'))
    F = hexc('#7d8f9e'); FL = hexc('#b8c7d3'); T = hexc('#3c8d93')
    c.rect(0, 0, 150, 2, F); c.rect(0, 0, 150, 1, FL); c.rect(0, 6, 150, 2, F); c.rect(0, 7, 150, 1, hexc('#4f5d69'))
    for x in range(150):
        k = x % 10
        y = 2 + (k if k < 4 else (8 - k if k < 8 else 0))
        if k < 8 and 2 <= y <= 5:
            c.px(x, y, T)
    for x in range(4, 150, 10):
        c.px(x, 1, hexc('#e6eef4')); c.px(x, 6, hexc('#e6eef4'))
    return c


def gen_ladder():
    """ladder.png (469x532): yellow steel ladder, chunky pixels."""
    c = Canvas(469, 532)
    S = hexc('#f2c230'); SD = hexc('#b8860b'); SL = hexc('#fff0a6'); O = hexc('#3a2a05')
    for x0 in (60, 369):
        c.rect(x0 - 4, 0, 48, 532, O); c.rect(x0, 0, 40, 532, S); c.rect(x0, 0, 10, 532, SL); c.rect(x0 + 30, 0, 10, 532, SD)
    for y in range(30, 532, 76):
        c.rect(100, y - 4, 269, 34, O); c.rect(100, y, 269, 26, S); c.rect(100, y, 269, 6, SL); c.rect(100, y + 20, 269, 6, SD)
    return c


def big_pixels(rows, pal, size, cell):
    s = sprite(rows, pal)
    c = Canvas(size, size)
    n = len(rows)
    off = (size - n * cell) // 2
    c.blit(s, off, off, cell)
    return c


def gen_health():
    """health.png (1200x1200): first-aid kit pickup on a 12x12 grid."""
    rows = ['............', '....kkkk....', '....k..k....', 'kkkkkkkkkkkk', 'kwwwwrrwwwwk', 'kwwwwrrwwwwk',
            'kwwrrrrrrwwk', 'kwwrrrrrrwwk', 'kwwwwrrwwwwk', 'kwwwwrrwwwgk', 'kggggggggggk', 'kkkkkkkkkkkk']
    return big_pixels(rows, {'k': hexc('#2a1a1a'), 'w': hexc('#f4f4f4'), 'r': hexc('#e8334a'), 'g': hexc('#c9c9c9')}, 1200, 100)


def gen_boost():
    """boost.png (512x512): lightning bolt pickup on a 16x16 grid."""
    rows = ['.........kkkk...', '........kyyk....', '.......kyyk.....', '......kyyk......', '.....kyyyk......',
            '....kyyyykkkk...', '...kyyyyyyyyk...', '..kkkkkyyyyk....', '......kyyyk.....', '.....kyyok......',
            '....kyyok.......', '...kyook........', '..kyok..........', '..kok...........', '..kk............',
            '................']
    return big_pixels(rows, {'k': hexc('#3b2a00'), 'y': hexc('#ffd23f'), 'o': hexc('#ff9e3d')}, 512, 32)


# ---------------------------------------------------------------------------------------------------------------
# Plushies (creeper 16x16 parts, pig 64x64). Look-alikes of the host game's mobs, drawn here.
# ---------------------------------------------------------------------------------------------------------------
def noise_fill(c, x, y, w, h, colors, seed):
    rng = np.random.RandomState(seed)
    for j in range(h):
        for i in range(w):
            c.px(x + i, y + j, colors[rng.randint(len(colors))])


def gen_creeper(part):
    seed = {'body_back': 11, 'body_front': 12, 'foot': 13, 'head_back': 14, 'head_front': 15}[part]
    greens = [hexc('#5fbf4a'), hexc('#4fae3d'), hexc('#77cf62'), hexc('#3f9a30'), hexc('#68c455'), hexc('#8fdc7c')]
    c = Canvas(16, 16)
    noise_fill(c, 0, 0, 16, 16, greens, seed)
    for x in range(0, 16, 4):  # stitched seams of a plush toy
        c.px(x, 0, hexc('#2f7a24'))
    if part == 'head_front':  # face on the north face (uv 8..16, 8..16)
        K = hexc('#1d2b1a'); D = hexc('#2e4029')
        for (x, y) in [(9, 10), (10, 10), (9, 11), (10, 11), (13, 10), (14, 10), (13, 11), (14, 11)]:
            c.px(x, y, K)
        for (x, y) in [(11, 12), (12, 12), (11, 13), (12, 13), (10, 13), (13, 13), (10, 14), (13, 14), (11, 14), (12, 14)]:
            c.px(x, y, D if (x, y) in [(11, 14), (12, 14)] else K)
    if part == 'foot':
        c.rect(4, 8, 4, 2, hexc('#2f7a24'))
    return c


def gen_pig():
    c = Canvas(64, 64)
    pinks = [hexc('#f2a7a7'), hexc('#efb3b3'), hexc('#f5bcbc'), hexc('#e99a9a')]
    noise_fill(c, 0, 0, 64, 64, pinks, 21)
    # head north face (8..16, 8..16): eyes
    c.rect(8, 11, 2, 2, WHITE); c.px(9, 12, BLACK)
    c.rect(14, 11, 2, 2, WHITE); c.px(14, 12, BLACK)
    # snout (north uv 4.3,4.3 - 5.2,5.0 -> px 17..21, 17..20) and its sides
    c.rect(16, 16, 8, 6, hexc('#e07f8f')); c.px(18, 18, hexc('#7a2f3b')); c.px(20, 18, hexc('#7a2f3b'))
    # legs (uv y 4..6.5 -> px 16..26 rows of 0..16): darker hooves at the bottom
    c.rect(0, 24, 16, 2, hexc('#b56b6b'))
    # plush stitches on the body
    for x in range(36, 64, 3): c.px(x, 30, hexc('#d98c8c'))
    return c


# ---------------------------------------------------------------------------------------------------------------
# Cabinets (textures/block/*_machine.png): painted per model face from models/arcades/*.json UVs
# ---------------------------------------------------------------------------------------------------------------
GAMES = {
    'snake': dict(body='#1f5e2e', accent='#9cff57', accent2='#0e3318', text='SNAKE', size=1024),
    'tetris': dict(body='#1d2a6b', accent='#ffd23f', accent2='#0f1640', text='TETROMINO', size=1024),
    'pacman': dict(body='#15706b', accent='#7ff2c9', accent2='#0a3b38', text='MUNCHER', size=1024),
    'pong': dict(body='#2b2b33', accent='#f4f4f4', accent2='#141418', text='PADDLE', size=1024),
    'space_invaders': dict(body='#3b1d6b', accent='#ff5bd1', accent2='#1d0d38', text='BARRAGE', size=1024),
    'kong': dict(body='#c99a12', accent='#2b2b2b', accent2='#6b4f08', text='CLIMBER', size=2048),
}


def screen_scene(game, w, h):
    """Attract-mode screen, w x h logical pixels."""
    c = Canvas(w, h, hexc('#05070d'))
    if game == 'snake':
        G = hexc('#9cff57'); D = hexc('#3fa52a')
        path = [(x, h // 2) for x in range(6, w // 2)] + [(w // 2, y) for y in range(h // 2, 6, -1)]
        for i, (x, y) in enumerate(path[::2]):
            c.rect(x, y, 2, 2, G if i % 2 else D)
        c.rect(w - 12, 8, 3, 3, hexc('#ff4d4d'))
    elif game == 'tetris':
        cols = ['#ffd23f', '#3fd6ff', '#ff5bd1', '#7dff6b', '#ff9e3d']
        rng = np.random.RandomState(4)
        for x in range(4, w - 4, 4):
            top = h - 4 - 4 * rng.randint(1, 5)
            for y in range(top, h - 4, 4):
                c.rect(x, y, 4, 4, hexc(cols[rng.randint(5)])); c.frame(x, y, 4, 4, hexc('#05070d'))
        c.rect(w // 2 - 6, 6, 12, 4, hexc('#3fd6ff')); c.rect(w // 2 - 2, 10, 4, 4, hexc('#3fd6ff'))
    elif game == 'pacman':
        c.frame(2, 2, w - 4, h - 4, hexc('#2a6bff'))
        c.rect(12, 10, w - 24, 2, hexc('#2a6bff')); c.rect(12, h - 12, w - 24, 2, hexc('#2a6bff'))
        for x in range(6, w - 6, 4): c.px(x, h // 2, hexc('#ffd9a0'))
        c.blit(muncher(2), 3 if w < 50 else 8, h // 2 - 7)
        d = drone_body(0).copy(); d[d[:, :, 3] > 0, :3] = (d[d[:, :, 3] > 0, :3] * np.array([1.0, .3, .35])).astype(np.uint8)
        c.blit(d, w - (17 if w < 50 else 22), h // 2 - 7)
    elif game == 'pong':
        for y in range(2, h - 2, 4): c.rect(w // 2, y, 1, 2, WHITE)
        c.rect(4, h // 2 - 6, 2, 10, WHITE); c.rect(w - 6, h // 2 - 2, 2, 10, WHITE); c.rect(w // 2 + 8, h // 2 - 8, 2, 2, WHITE)
    elif game == 'space_invaders':
        rng = np.random.RandomState(7)
        for _ in range(20): c.px(rng.randint(w), rng.randint(h), hexc('#6f6f9f'))
        tints = [(1, .4, .9), (.4, 1, .9), (1, .9, .3)]
        for row in range(3):
            for k in range(3):
                a = alien(row + 1, 0).astype(float); a[:, :, :3] *= tints[row]
                c.blit(a.astype(np.uint8), 4 + k * 17, 2 + row * 11)
        c.rect(w // 2, h - 14, 1, 4, WHITE)
    else:  # kong
        S = hexc('#7d8f9e')
        for i, y in enumerate((12, 24, 36)):
            c.rect(2, y + (i % 2), w - 4, 2, S)
        c.rect(w - 10, 14, 2, 10, hexc('#f2c230')); c.rect(w - 6, 14, 2, 10, hexc('#f2c230'))
        r = junk_robot(False)[::2, ::2]
        c.blit(r, 2, 0)
        c.blit(worker('stand'), w // 2, 21)
        c.blit(drum(1), w // 2 - 14, 27)
    return c


def paint_cabinet(game, model):
    g = GAMES[game]
    size = g['size']
    base = 1024
    unit = base / 16.0  # px per uv unit at 1024
    PIX = 2             # logical pixel size
    c = Canvas(base, base)
    body, accent, dark = hexc(g['body']), hexc(g['accent']), hexc(g['accent2'])

    def R(uv):
        x0, y0, x1, y1 = uv
        return (int(round(min(x0, x1) * unit)), int(round(min(y0, y1) * unit)),
                int(round(max(x0, x1) * unit)), int(round(max(y0, y1) * unit)))

    def fill_body(r, col=body, bevel=True):
        x0, y0, x1, y1 = r
        if x1 <= x0 or y1 <= y0: return
        c.rect(x0, y0, x1 - x0, y1 - y0, col)
        for j in range(y0, y1, PIX * 6):  # faint wood/vinyl grain
            c.rect(x0, j, x1 - x0, 1, shade(col, 0.93))
        if bevel and x1 - x0 > 8 and y1 - y0 > 8:
            c.rect(x0, y0, x1 - x0, PIX, shade(col, 1.18)); c.rect(x0, y1 - PIX, x1 - x0, PIX, shade(col, 0.7))
            c.rect(x0, y0, PIX, y1 - y0, shade(col, 1.1)); c.rect(x1 - PIX, y0, PIX, y1 - y0, shade(col, 0.75))

    def stripes(r):
        x0, y0, x1, y1 = r
        h = y1 - y0
        for k, col in enumerate((accent, dark)):
            yy = y0 + int(h * (0.55 + 0.12 * k))
            c.rect(x0, yy, x1 - x0, PIX * 3, col)

    def motif(r):
        x0, y0, x1, y1 = r
        spr = {'snake': None, 'tetris': None, 'pacman': muncher(2), 'pong': None, 'space_invaders': alien(2, 0),
               'kong': worker('stand')}[game]
        if game == 'space_invaders':
            spr = spr.astype(float); spr[:, :, :3] *= (1, .4, .9); spr = spr.astype(np.uint8)
        if spr is None:
            spr = screen_scene(game, 28, 22).a
        sc = max(1, min((x1 - x0) // (spr.shape[1] + 4), (y1 - y0) // (spr.shape[0] + 4)) // 2)
        sw, sh = spr.shape[1] * sc, spr.shape[0] * sc
        c.blit(spr, x0 + (x1 - x0 - sw) // 2, y0 + int((y1 - y0) * 0.12), sc)

    def marquee(r):
        x0, y0, x1, y1 = r
        c.rect(x0, y0, x1 - x0, y1 - y0, dark)
        c.frame(x0, y0, x1 - x0, y1 - y0, accent, PIX)
        t = text_sprite(g['text'], accent if game != 'kong' else hexc('#f2c230'))
        sc = 2 if t.shape[1] * 2 <= (x1 - x0) - 6 else 1
        tt = outlined(t, BLACK)
        c.blit(tt, x0 + ((x1 - x0) - tt.shape[1] * sc) // 2, y0 + ((y1 - y0) - tt.shape[0] * sc) // 2, sc)

    def coin_door(r):
        fill_body(r)
        x0, y0, x1, y1 = r
        w, h = x1 - x0, y1 - y0
        px0, py0 = x0 + w // 4, y0 + h // 3
        c.rect(px0, py0, w // 2, h // 2, hexc('#1a1a1a')); c.frame(px0, py0, w // 2, h // 2, hexc('#6b6b6b'), PIX)
        for k in (0, 1):
            sx = px0 + w // 8 + k * w // 5
            c.rect(sx, py0 + PIX * 4, PIX * 4, PIX * 8, hexc('#ff6a00')); c.rect(sx + PIX, py0 + PIX * 5, PIX * 2, PIX * 6, hexc('#2a0d00'))
        c.rect(px0 + PIX * 3, py0 + h // 2 - PIX * 6, w // 2 - PIX * 6, PIX * 2, hexc('#6b6b6b'))

    def control(r):
        x0, y0, x1, y1 = r
        c.rect(x0, y0, x1 - x0, y1 - y0, hexc('#1e1e24'))
        c.frame(x0, y0, x1 - x0, y1 - y0, accent, PIX)
        w, h = x1 - x0, y1 - y0
        jx, jy = x0 + w // 4, y0 + h // 2
        c.rect(jx - PIX * 3, jy - PIX * 3, PIX * 6, PIX * 6, hexc('#e8334a')); c.rect(jx - PIX * 2, jy - PIX * 2, PIX * 2, PIX * 2, hexc('#ffb3bd'))
        for k, col in enumerate(('#3fd6ff', '#ffd23f', '#7dff6b')):
            bx = x0 + w // 2 + k * PIX * 7
            c.rect(bx, jy - PIX * 2, PIX * 4, PIX * 4, hexc(col))

    def screen(r):
        x0, y0, x1, y1 = r
        c.rect(x0, y0, x1 - x0, y1 - y0, hexc('#101010'))
        lw, lh = (x1 - x0) // PIX - 4, (y1 - y0) // PIX - 4
        if lw > 4 and lh > 4:
            c.blit(screen_scene(game, lw, lh).a, x0 + PIX * 2, y0 + PIX * 2, PIX)

    # marquee = the highest element whose front (north) face is tall enough to carry the title
    tall = [e for e in model['elements'] if not e.get('rotation', {}).get('angle') and e['from'][1] >= 22
            and abs(e['faces']['north']['uv'][3] - e['faces']['north']['uv'][1]) >= 0.4 and abs(e['from'][0] - 1.1) > 1e-6]
    top = max(tall, key=lambda e: e['to'][1]) if tall else None
    for e in model['elements']:
        f, t = e['from'], e['to']
        ang = (e.get('rotation') or {}).get('angle', 0)
        kind = ('base' if f[1] == 0 else 'screen' if ang == 22.5 else 'control' if ang == -22.5 else
                'marquee' if e is top else 'trim' if abs(f[0] - 1.1) < 1e-6 else 'body')
        for d, face in e['faces'].items():
            r = R(face['uv'])
            if kind == 'screen' and d == 'north': screen(r)
            elif kind == 'control' and d == 'up': control(r)
            elif kind == 'marquee' and d == 'north': marquee(r)
            elif kind == 'base' and d == 'north': coin_door(r)
            elif kind == 'trim': fill_body(r, accent if d in ('north', 'south') else dark, bevel=False)
            elif d in ('east', 'west') and kind in ('base', 'body'):
                fill_body(r); stripes(r)
                if kind == 'base': motif(r)
            else:
                fill_body(r)
    im = c.image()
    if size != base:
        im = im.resize((size, size), Image.NEAREST)
    return im


def gen_prize_counter():
    """block/prize_counter.png (32x32) for models/block/prize_box.json: gold-trimmed glass case with toys."""
    c = Canvas(32, 32, hexc('#7a1f2b'))
    noise_fill(c, 0, 0, 14, 13, [hexc('#cfe9f2'), hexc('#c4e2ee'), hexc('#d8eef5')], 31)    # glass
    noise_fill(c, 0, 13, 12, 12, [hexc('#cfe9f2'), hexc('#c4e2ee'), hexc('#d8eef5')], 32)
    for k in range(4): c.px(2 + k, 1 + k, WHITE); c.px(2 + k, 15 + k, WHITE)
    c.rect(12, 13, 16, 2, hexc('#e0b030')); c.rect(14, 0, 2, 12, hexc('#e0b030'))          # gold rails
    c.rect(12, 15, 2, 12, hexc('#e0b030')); c.rect(14, 15, 2, 12, hexc('#c08f10'))
    c.rect(14, 12, 14, 1, hexc('#c08f10'))
    noise_fill(c, 16, 0, 12, 4, [hexc('#5a2d0c'), hexc('#6b3812')], 33)                     # wooden shelf
    c.rect(16, 4, 14, 3, hexc('#e0b030'))
    noise_fill(c, 16, 7, 12, 5, [hexc('#3a0f16'), hexc('#4a141d')], 34)                     # velvet base
    toys = ['#ff5bd1', '#3fd6ff', '#7dff6b', '#ffd23f', '#ff9e3d', '#b07dff']
    rng = np.random.RandomState(35)
    for y in range(15, 28):
        for x in range(16, 28):
            c.px(x, y, hexc(toys[rng.randint(len(toys))]) if rng.rand() < 0.75 else hexc('#ffffff'))
    for x in range(0, 12):
        c.px(x, 25, hexc('#e0b030')); c.px(x, 26, hexc('#c08f10'))
    return c


# ---------------------------------------------------------------------------------------------------------------
# Mod icon (icon.png 1254x1254 RGB): 114x114 pixel scene x11
# ---------------------------------------------------------------------------------------------------------------
def gen_icon():
    N = 114
    c = Canvas(N, N, hexc('#140b2e'))
    for y in range(N):  # vertical gradient
        c.rect(0, y, N, 1, shade(hexc('#1c0f45'), 0.6 + 0.6 * y / N))
    rng = np.random.RandomState(9)
    for _ in range(60):
        c.px(rng.randint(N), rng.randint(N), hexc(['#ffffff', '#ffd23f', '#7ff2c9', '#ff5bd1'][rng.randint(4)]))
    # cabinet
    O = hexc('#0a0618'); B = hexc('#1fa7a0'); BL = hexc('#7ff2c9'); BD = hexc('#11635f')
    c.rect(33, 8, 48, 76, O); c.rect(35, 10, 44, 72, B); c.rect(35, 10, 4, 72, BL); c.rect(75, 10, 4, 72, BD)
    c.rect(39, 12, 36, 10, O); c.rect(40, 13, 34, 8, hexc('#2b1059'))
    t = text_sprite('ARCADE', hexc('#ffd23f'), F3)
    c.blit(t, 57 - t.shape[1] // 2, 14)
    c.rect(39, 25, 36, 28, O)
    c.blit(screen_scene('pacman', 34, 26).a, 40, 26)
    c.rect(37, 55, 40, 8, hexc('#2b2b33')); c.rect(43, 57, 3, 3, hexc('#e8334a'))
    for k, col in enumerate(('#3fd6ff', '#ffd23f', '#7dff6b')): c.rect(56 + k * 6, 58, 3, 3, hexc(col))
    c.rect(49, 68, 16, 10, O); c.rect(52, 70, 2, 5, hexc('#ff6a00')); c.rect(60, 70, 2, 5, hexc('#ff6a00'))
    # title
    for s, y, col in (('ARCADE MOD', 88, '#ffd23f'), ('RELOADED', 99, '#ff5bd1')):
        tt = outlined(text_sprite(s, hexc(col)), O)
        c.blit(tt, (N - tt.shape[1]) // 2, y)
    im = c.image().resize((1254, 1254), Image.NEAREST)
    return im.convert('RGB')


# ---------------------------------------------------------------------------------------------------------------
# Sounds: chiptune synthesis (square / triangle / noise), written as Ogg Vorbis
# ---------------------------------------------------------------------------------------------------------------
SR = 44100


def t_(d):
    return np.arange(int(SR * d)) / SR


def square(f, d, duty=0.5, vol=0.3):
    t = t_(d)
    ph = np.cumsum(np.broadcast_to(np.asarray(f, float), t.shape) / SR) % 1.0
    return np.where(ph < duty, vol, -vol)


def tri(f, d, vol=0.35):
    t = t_(d)
    ph = np.cumsum(np.broadcast_to(np.asarray(f, float), t.shape) / SR) % 1.0
    return vol * (4 * np.abs(ph - 0.5) - 1)


def noise(d, vol=0.3, step=4, seed=1):
    rng = np.random.RandomState(seed)
    n = int(SR * d)
    v = rng.uniform(-1, 1, n // step + 1).repeat(step)[:n]
    return vol * v


def env(x, a=0.005, r=0.05):
    n = len(x)
    e = np.ones(n)
    na, nr = int(SR * a), int(SR * r)
    if na: e[:na] = np.linspace(0, 1, na)
    if nr: e[-nr:] *= np.linspace(1, 0, nr)
    return x * e


def note(n):
    """MIDI note -> Hz."""
    return 440.0 * 2 ** ((n - 69) / 12)


def seq(notes, bpm, voice='sq', duty=0.5, vol=0.25, gap=0.12):
    """notes: list of (midi or None, beats)."""
    out = []
    for n, b in notes:
        d = 60.0 / bpm * b
        if n is None:
            out.append(np.zeros(int(SR * d)))
        else:
            x = square(note(n), d, duty, vol) if voice == 'sq' else tri(note(n), d, vol)
            out.append(env(x, 0.003, min(d * gap + 0.01, d * 0.5)))
    return np.concatenate(out)


def mix(*parts):
    n = max(len(p) for p in parts)
    out = np.zeros(n)
    for p in parts:
        out[:len(p)] += p
    return out


def fade_loop(x, ms=8):
    n = int(SR * ms / 1000)
    x = x.copy(); x[:n] *= np.linspace(0, 1, n); x[-n:] *= np.linspace(1, 0, n)
    return x


def sweep(f0, f1, d, voice='sq', vol=0.3, duty=0.5):
    f = np.linspace(f0, f1, int(SR * d))
    return square(f, d, duty, vol) if voice == 'sq' else tri(f, d, vol)


def melody_theme(scale_root, minor, seed_bars, bpm, bars=16):
    """An original 16-bar chiptune loop built from hand-written motifs (lead, bass, hat)."""
    sc = [0, 2, 3, 5, 7, 8, 10] if minor else [0, 2, 4, 5, 7, 9, 11]
    deg = lambda d: scale_root + 12 * (d // 7) + sc[d % 7]
    motifs = seed_bars  # list of bars, each a list of (degree or None, beats) summing to 4
    lead, bass = [], []
    prog = [0, 5, 3, 4] if minor else [0, 3, 4, 0]
    for b in range(bars):
        m = motifs[b % len(motifs)]
        shift = 2 if (b // 4) % 4 == 2 else 0
        lead += [((deg(d + shift + 7) if d is not None else None), l) for d, l in m]
        root = prog[(b // 2) % 4]
        bass += [(deg(root) - 12, 1), (deg(root + 4) - 12, 1), (deg(root) - 12, 1), (deg(root + 2) - 12, 1)]
    L = seq(lead, bpm, 'sq', 0.25, 0.16)
    Bs = seq(bass, bpm, 'tri', vol=0.32, gap=0.05)
    beat = 60.0 / bpm
    hat = np.zeros(len(Bs))
    hn = noise(0.03, 0.08, 1, 5)
    for k in range(int(bars * 4 * 2)):
        i = int(k * beat / 2 * SR)
        if i + len(hn) < len(hat): hat[i:i + len(hn)] += env(hn, 0.001, 0.025)
    return fade_loop(mix(L, Bs, hat))


SOUNDS = {}


def S(path, fn, **kw):
    SOUNDS[path] = (fn, kw)


S('insert_coin', lambda: np.concatenate([env(square(note(83), 0.07, .25, .3), r=.01), env(square(note(88), 0.3, .25, .3), r=.25)]))
for k in range(1, 7):
    S(f'pacman/waka.{k}', (lambda k: lambda: env(np.concatenate([
        sweep(260 + 30 * k, 520 + 30 * k, 0.05, 'tri', 0.45), sweep(520 + 30 * k, 300 + 30 * k, 0.05, 'tri', 0.45)]), 0.002, 0.01))(k))
S('pacman/death', lambda: np.concatenate([
    env(square(note(n) * (1 + 0.01 * np.sin(2 * np.pi * 9 * t_(0.12))), 0.12, 0.5, 0.25), r=0.03) for n in (76, 74, 71, 69, 67, 64, 62, 59, 57)]
    + [np.zeros(int(SR * 0.08)), env(noise(0.08, 0.3, 8), r=0.06), np.zeros(int(SR * 0.05)), env(noise(0.08, 0.3, 12, 2), r=0.06)]))
S('pacman/eaten', lambda: fade_loop(np.concatenate([sweep(400, 1400, 0.175, 'sq', 0.18, 0.25) for _ in range(6)])))
S('pacman/fright', lambda: fade_loop(np.concatenate([sweep(180, 330, 0.185, 'tri', 0.4), sweep(330, 180, 0.185, 'tri', 0.4)] * 2)))
S('pacman/fruit', lambda: np.concatenate([env(square(note(n), 0.07, 0.125, 0.25), r=0.02) for n in (72, 79, 84, 91)] + [np.zeros(int(SR * 0.16))]))
S('pacman/ghost', lambda: env(np.concatenate([sweep(200, 1600, 0.35, 'sq', 0.2, 0.5), sweep(1600, 900, 0.23, 'sq', 0.2, 0.5)]), r=0.05))
S('pacman/life', lambda: np.concatenate([env(square(note(n), 0.11, 0.5, 0.22), r=0.03) for n in (79, 84, 79, 84, 88, 91)] + [np.zeros(int(SR * 0.1))]))
S('pacman/pacman', lambda: mix(
    seq([(72, .5), (76, .5), (79, .5), (84, .5), (83, .5), (79, .5), (76, 1), (74, .5), (77, .5), (81, .5), (86, .5), (84, .5), (81, .5), (77, 1),
         (76, .5), (79, .5), (84, .5), (88, .5), (86, .5), (83, .5), (79, .5), (74, .5), (72, 2)], 200, 'sq', 0.25, 0.2),
    seq([(48, 1), (55, 1), (48, 1), (55, 1), (50, 1), (57, 1), (50, 1), (57, 1), (52, 1), (55, 1), (55, 1), (47, 1), (48, 2)], 200, 'tri', vol=0.35)))
S('pacman/siren', lambda: fade_loop(np.concatenate([
    square(np.concatenate([np.linspace(330, 440, int(SR * 0.27)), np.linspace(440, 330, int(SR * 0.27))]), 0.54, 0.5, 0.12)] * 3)))
S('pong/hit', lambda: env(square(note(81), 0.1, 0.5, 0.3), r=0.02))
S('pong/wall', lambda: env(square(note(74), 0.15, 0.5, 0.3), r=0.04))
S('pong/miss', lambda: env(square(np.linspace(note(57), note(45), int(SR * 0.26)), 0.26, 0.5, 0.3), r=0.08))
S('spaceinvaders/shoot', lambda: env(mix(sweep(2200, 300, 0.29, 'sq', 0.18, 0.25), noise(0.29, 0.06, 2, 3)), 0.001, 0.1))
S('spaceinvaders/explode', lambda: env(noise(1.76, 0.4, 6, 4) * np.exp(-t_(1.76) * 2.2), 0.001, 0.3))
S('spaceinvaders/destroyed', lambda: env(mix(noise(0.74, 0.3, 10, 6) * np.exp(-t_(0.74) * 3), sweep(700, 90, 0.74, 'sq', 0.15)), 0.001, 0.2))
S('spaceinvaders/theme', lambda: melody_theme(57, True, [
    [(0, .5), (2, .5), (4, 1), (3, .5), (2, .5), (0, 1)], [(4, .5), (5, .5), (6, .5), (7, .5), (6, 1), (4, 1)],
    [(2, 1), (None, .5), (2, .5), (3, .5), (4, .5), (2, 1)], [(1, .5), (0, .5), (-1, .5), (0, .5), (1, 2)]], 132, 24))
S('tetris/theme', lambda: melody_theme(62, False, [
    [(4, 1), (2, .5), (3, .5), (4, .5), (5, .5), (4, 1)], [(2, .5), (0, .5), (1, 1), (2, 1), (None, 1)],
    [(5, .5), (4, .5), (3, .5), (2, .5), (3, 1), (5, 1)], [(4, 1.5), (3, .5), (2, 1), (0, 1)]], 140, 24))


def write_ogg(path, x):
    x = np.clip(np.asarray(x, float), -1, 1)
    # block writes: libsndfile's Vorbis encoder crashes on very large single writes
    with sf.SoundFile(path, 'w', SR, 1, format='OGG', subtype='VORBIS') as f:
        for i in range(0, len(x), 4096):
            f.write(x[i:i + 4096].astype(np.float32))


# ---------------------------------------------------------------------------------------------------------------
def main():
    out = sys.argv[1]
    up = sys.argv[sys.argv.index('--upstream') + 1] if '--upstream' in sys.argv else None

    def model(rel):
        p = os.path.join(up, 'src/main/resources', A, 'models', rel) if up else os.path.join(HERE, 'models', os.path.basename(rel))
        return json.load(open(p))

    def save(rel, im, mode='RGBA'):
        p = os.path.join(out, rel)
        os.makedirs(os.path.dirname(p), exist_ok=True)
        (im if isinstance(im, Image.Image) else im.image(mode)).save(p, optimize=True)

    save(A + 'textures/gui/pacman.png', gen_muncher_sheet())
    save(A + 'textures/gui/spaceinvaders.png', gen_alien_sheet())
    save(A + 'textures/gui/kong/a.png', gen_girder_sheet())
    save(A + 'textures/gui/kong/peach.png', gen_rescue())
    save(A + 'textures/gui/kong/platform.png', gen_platform(), 'RGB')
    save(A + 'textures/gui/kong/ladder.png', gen_ladder())
    save(A + 'textures/gui/kong/health.png', gen_health())
    save(A + 'textures/gui/kong/boost.png', gen_boost())
    for part in ('body_back', 'body_front', 'foot', 'head_back', 'head_front'):
        save(A + f'textures/block/plushie/creeper/creeper_{part}.png', gen_creeper(part))
    save(A + 'textures/block/plushie/pig.png', gen_pig())
    save(A + 'textures/block/prize_counter.png', gen_prize_counter())
    for game in GAMES:
        save(A + f'textures/block/{game}_machine.png', paint_cabinet(game, model(f'arcades/{game}_machine.json')))
    save('icon.png', gen_icon())
    for rel, (fn, kw) in SOUNDS.items():
        p = os.path.join(out, A + 'sounds', rel + '.ogg')
        os.makedirs(os.path.dirname(p), exist_ok=True)
        write_ogg(p, fn(**kw))
    print('ok', out)


if __name__ == '__main__':
    main()
