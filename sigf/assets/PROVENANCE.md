# ArcadeMod asset provenance (SIGF build `1.0-1.20.1-sigf1`)

Upstream: https://github.com/Bay4lly/ArcadeMod @ `eb09571cf6883651f7516da85682b5e9c9c6b166` (GPL-3.0).
Every replacement below is produced by [`gen_assets.py`](gen_assets.py) (pixel art and chiptunes designed by Claude
for SIGF, 2026-10-05). No fal generation was used, and the script reads no upstream image or sound: it reads only the
cabinet model JSON (UV rectangles) and uses the maze wall map and sprite-cell coordinates taken from the upstream
Java code, so the new art lands where the game code samples it. Binaries are not in git: regenerate with
`python gen_assets.py <out> --upstream <checkout>` (numpy, Pillow, scipy, soundfile with Vorbis).

Classification of the upstream file: **ripped** = taken or traced from a Namco / Nintendo / Taito / Atari / Tetris
game or cabinet, **unclear** = no provenance and plausibly third-party (stock clipart, arcade recordings, host-game
texture edits), **original** = drawn by the mod authors with no third-party content. Hashes: first 16 hex of the
sha256 of the generated file in the build inputs (local generation, 2026-10-05; tarball sha256
`4323200fd0ea39a41f64dd870c4737d00698f904183a5725eb2deb88a237948f`). PNGs regenerate byte-identical; OGG bytes differ
per run (random Ogg stream serial) while the decoded audio is identical.

## Replaced textures (22)

| Path (under `src/main/resources/`) | Size | Upstream | Evidence | Replacement (gen_assets.py) | sha256 |
|---|---|---|---|---|---|
| `assets/arcademod/textures/gui/pacman.png` | 512x512 | ripped | Pac-Man frames, ghost, bonus fruit row (cherry .. Galaxian, bell, key); 224x248 maze = arcade board size | `gen_muncher_sheet`: bevel frame, maze re-drawn from the code's wall map as rounded translucent blocks, mint one-eyed "muncher" with a box mouth, antenna "drones", star power pellet, 8 new treats (donut, cupcake, cone, lollipop, cookie, gem, crown, trophy), pop animation | `e36ada52990927a9` |
| `assets/arcademod/textures/gui/spaceinvaders.png` | 512x512 | ripped | Space Invaders crab/squid/octopus invaders and UFO, Pac-Man leftovers | `gen_alien_sheet`: frame, play-field border, 5 new aliens x2 frames (spike orb, beetle, jelly, diamond skull, comet pod), cannon ship, bolt, zigzag bombs, particle explosion | `98bbe43d8473d17a` |
| `assets/arcademod/textures/gui/kong/a.png` | 308x287 | ripped | Donkey Kong arcade sprite sheet (Jumpman/Mario, DK, Pauline, barrels, fireballs) | `gen_girder_sheet`: hard-hat worker (stand, 2 walk frames, climb), scrap robot boss (2 frames), steel drums (4 frames), only the cells GuiKong samples | `0b1f38a76ca00c61` |
| `assets/arcademod/textures/gui/kong/peach.png` | 39x22 | ripped | Pauline (Donkey Kong) sprite, code comment "Princess" | `gen_rescue`: lost kitten with a "HELP!" bubble | `5172af95d930c1bc` |
| `assets/arcademod/textures/gui/kong/platform.png` | 150x8 RGB | ripped | red riveted Donkey Kong girder | `gen_platform`: grey/teal steel truss | `1f8ff79d0174ccbd` |
| `assets/arcademod/textures/gui/kong/ladder.png` | 469x532 | unclear | photo-style wooden ladder, stock-clipart look | `gen_ladder`: chunky yellow steel ladder | `eab77c6c17f27c3d` |
| `assets/arcademod/textures/gui/kong/health.png` | 1200x1200 | unclear | common pixel heart, no source | `gen_health`: first-aid kit, 12x12 grid | `0aa97fd052a0a13f` |
| `assets/arcademod/textures/gui/kong/boost.png` | 512x512 | unclear | vector lightning-bolt clipart | `gen_boost`: pixel bolt, 16x16 grid | `1e5d8788374974ca` |
| `assets/arcademod/textures/block/pacman_machine.png` | 1024 | ripped | Pac-Man cabinet side art, marquee and bezel | `paint_cabinet('pacman')`: teal cabinet, "MUNCHER" marquee, attract screen, coin door, control panel | `f2e85be0175e863e` |
| `assets/arcademod/textures/block/space_invaders_machine.png` | 1024 | ripped | Space Invaders marquee/side art and invader graphics | `paint_cabinet('space_invaders')`: purple, "BARRAGE" | `be9c2077d19a7559` |
| `assets/arcademod/textures/block/kong_machine.png` | 2048 | ripped | Donkey Kong cabinet side art (DK, Mario) | `paint_cabinet('kong')`: hazard-yellow, "CLIMBER" (painted at 1024, x2 nearest) | `f8fd7615aba0ede1` |
| `assets/arcademod/textures/block/pong_machine.png` | 1024 | ripped | "PONG" marquee (Atari trademark) and cabinet art | `paint_cabinet('pong')`: charcoal, "PADDLE" | `0ab734a6257ddfa9` |
| `assets/arcademod/textures/block/tetris_machine.png` | 1024 | unclear | Tetris-style cabinet art, origin unknown | `paint_cabinet('tetris')`: navy, "TETROMINO" | `9ee4ea90e4b03a42` |
| `assets/arcademod/textures/block/snake_machine.png` | 1024 | unclear | cabinet art with snake artwork and lettering, origin unknown | `paint_cabinet('snake')`: green, "SNAKE" | `09f3e9185371752f` |
| `assets/arcademod/textures/block/prize_counter.png` | 32x32 | unclear | texture-pack-like gradient/emerald texture, no source | `gen_prize_counter`: glass, gold rails, shelf, toy pile | `e1c3c51d962ce139` |
| `assets/arcademod/textures/block/plushie/pig.png` | 64x64 | unclear | 1.12 plush texture resembling the Minecraft pig texture | `gen_pig`: pink noise, eyes, snout per model UVs | `d8565b4a230007e8` |
| `assets/arcademod/textures/block/plushie/creeper/creeper_body_back.png` | 16x16 | unclear | resembles an upscaled Minecraft creeper texture | `gen_creeper('body_back')`: seeded green noise, seams | `ffd0dd344e079d82` |
| `assets/arcademod/textures/block/plushie/creeper/creeper_body_front.png` | 16x16 | unclear | same | `gen_creeper('body_front')` | `fc14b797a964902f` |
| `assets/arcademod/textures/block/plushie/creeper/creeper_foot.png` | 16x16 | unclear | same | `gen_creeper('foot')` | `3a3a76ce4cbdba2d` |
| `assets/arcademod/textures/block/plushie/creeper/creeper_head_back.png` | 16x16 | unclear | same | `gen_creeper('head_back')` | `7a54a7c2685522d7` |
| `assets/arcademod/textures/block/plushie/creeper/creeper_head_front.png` | 16x16 | unclear | same, face | `gen_creeper('head_front')`: plush face drawn here (host-game mob look-alike) | `72408f15b36c007a` |
| `icon.png` | 1254x1254 RGB | ripped (derived) | AI-style art containing a Pac-Man ghost (Pinky) and cherries | `gen_icon`: pixel cabinet showing the muncher and a drone, "ARCADE MOD RELOADED" in the script's own 5x7 font | `a6b9d4be243b0cd5` |

## Replaced sounds (23, all Ogg Vorbis mono 44.1 kHz)

| Path (`assets/arcademod/sounds/`) | Upstream | Evidence | Replacement (`SOUNDS` table) | sha256 |
|---|---|---|---|---|
| `pacman/waka.1..6.ogg` | ripped | Pac-Man "waka" chomp (names, 0.1 s stereo cuts) | triangle up/down blips, pitch per index | `ac2962772cf53460` `c127a3ed3295e0e1` `3458608f0140f533` `84c46bc8ea0dc34a` `e3fe21fed21da840` `8e4e1a2c034036b4` |
| `pacman/pacman.ogg` | ripped | Pac-Man start jingle (4.2 s, 22 kHz) | original 4.2 s C-major square/triangle jingle | `bca430a7eab62e74` |
| `pacman/siren.ogg` | ripped | Pac-Man siren loop | 330-440 Hz square glide loop | `3d0cbcd0739fc325` |
| `pacman/fright.ogg` | ripped | Pac-Man frightened loop | triangle wobble loop | `daee9b18e02498f3` |
| `pacman/eaten.ogg` | ripped | Pac-Man eyes-returning loop | fast rising square sweeps | `aef4d06863b6b664` |
| `pacman/death.ogg` | ripped | Pac-Man death | descending vibrato arpeggio + two noise pops | `9f259d045886a049` |
| `pacman/fruit.ogg` | ripped | Pac-Man fruit | 4-note square arpeggio | `2eb469826f592985` |
| `pacman/ghost.ogg` | ripped | Pac-Man eat ghost | up/down square sweep | `b93f52e5b1db7cad` |
| `pacman/life.ogg` | ripped | Pac-Man extra life | 6-note fanfare | `465edab003531bc4` |
| `spaceinvaders/shoot.ogg` | ripped | Space Invaders shot (1.12 file, same name) | descending laser sweep + noise | `1ca58cc60c466846` |
| `spaceinvaders/explode.ogg` | ripped | Space Invaders explosion | decaying noise burst | `8c69a2fd2ab2f1d9` |
| `spaceinvaders/destroyed.ogg` | ripped | invader killed | noise + falling sweep | `cb59ec4fbf4bd86d` |
| `spaceinvaders/theme.ogg` | unclear | 71 s stereo music, origin unknown | original 24-bar A-minor chiptune loop (132 bpm) | `82b0437108e4709c` |
| `tetris/theme.ogg` | unclear | 83 s stereo music for the Tetris-style machine, origin unknown | original 24-bar D-major chiptune loop (140 bpm), not Korobeiniki | `d5a6c8653e80afd2` |
| `pong/hit.ogg`, `pong/wall.ogg`, `pong/miss.ogg` | unclear | Pong-style beeps, source unknown | square beeps (A5, D5, falling A3-A2) | `35bb7f10021f4228` `e3ae86490948197e` `571662a7cafbd370` |
| `insert_coin.ogg` | unclear | coin chime, source unknown | two-note square chime | `94c35b5b913af869` |

## Removed (18, unused by the code at the pinned commit: 8 ripped, 9 unclear, 1 duplicate of an original)

Listed in [`../patches/delete.txt`](../patches/delete.txt): `textures/arcades/{snake,tetris}_machine.png` (duplicates of
the cabinet textures: unclear), `textures/kong/*` (10 files: duplicates of `gui/kong/*` plus `background-2.jpg`:
ripped/unclear), `textures/gui/kong.png` and `kong.psd` (full Donkey Kong arcade sprite rip: ripped),
`textures/gui/kong/final.png` and `lose.png` (Mario and Pauline sprites: ripped), `textures/gui/kong/logo.png`
(unclear), `textures/block/plushie/cow.png` (unused 1.12 plush texture: unclear).

## Kept as original (11)

`textures/gui/{generic_gui,button_arrows,gui_arrows,prize_box,snake,tetrominoes,pong}.png` (SuperHB 1.12.2 GUI
frames, arrows, Pong digits and WINNER/LOSER text: plain UI, no third-party content), `textures/item/{coin,ticket}.png`
(SuperHB), `textures/gui/kong/bar.png` (Bay4lly, plain filled bars), `logo.png` (Bay4lly, cabinet + mod name, no
third-party marks). Models (Blockbench JSON) and blockstates are the authors' own and unchanged.

## Names (patch `0001`)

Display names only; registry ids, sound event ids and Java class names stay (`pacman`, `GuiKong`, ...), they are not
shown to players. Lang `en_us.json` and GUI titles: Pac-Man -> **Pellet Muncher**, Space Invaders -> **Alien Barrage**,
Kong / "DONKEY KONG" -> **Girder Climber**, Pong -> **Paddle Rally**, Tetris -> **Tetrominoes**. Snake stays.

## Residual note for review

The Pellet Muncher maze layout is the upstream game logic (28x31 wall map in `GuiPacMan.setupTiles()`), which follows
the arcade game's board. The art is new; the layout is a mechanic/level and is kept as code. Flag for Pj if a
different board is wanted (it would be a Java change, not an asset change).
