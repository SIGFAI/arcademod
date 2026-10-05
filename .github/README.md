# Arcade Mod Reloaded

Working arcade cabinets in Minecraft: insert coins, play Pellet Muncher, Alien Barrage, Girder Climber, Paddle Rally, Tetrominoes and Snake, win tickets and trade them at the prize counter.

**Arcade Mod Reloaded is made by [Bay4lly](https://github.com/Bay4lly).** All credit for the mod goes to them. It is derived from [KenLPham/ArcadeMod](https://github.com/KenLPham/ArcadeMod), the original 1.12.2 mod by SuperHB (KenLPham), by KenLPham.

- Original project: https://github.com/Bay4lly/ArcadeMod
- Report bugs and ask questions there: https://github.com/Bay4lly/ArcadeMod/issues
- Upstream version packaged here: 1.0.0 (commit [`eb09571`](https://github.com/Bay4lly/ArcadeMod/tree/eb09571cf6883651f7516da85682b5e9c9c6b166))
- **Built by SIGF from commit [`eb09571cf6883651f7516da85682b5e9c9c6b166`](https://github.com/Bay4lly/ArcadeMod/tree/eb09571cf6883651f7516da85682b5e9c9c6b166)** with the changes described below, on a disposable build machine (AWS EC2 i-07436d854aba1942b (c6i.xlarge, Amazon Linux 2023, Corretto 17, Gradle 8.8 official zip; terminated after the build)). The app installs these SIGF builds, not binaries from the author.

> **Beta.** Nobody at SIGF has played this build yet. Back up your saves.
> Bugs in the mod itself go to the author's issue tracker above; problems with the one-click install go to this repository's issues.

## What you need

- **Minecraft**: Java Edition 1.20.1.
- Windows and the [SIGF app](https://sigf.ai). The app installs forge 47.1.0 for you.

## Install

In the SIGF app, open **Arcade Mod Reloaded** in the catalog, press **Install**, then **Play**. **Restore** puts your game folders back exactly as they were.
The app follows `mashup.json` in this repository: every download is pinned by sha256. The files come from the release [`v1.0.0`](../../releases/tag/v1.0.0).

### Good to know

- You need Minecraft: Java Edition. The app makes its own Prism instance "sigf-arcademod" (Minecraft 1.20.1, Forge 47.1.0, Java 17).
- Craft coins, place an arcade machine, right-click it and insert a coin. Scores pay out tickets; spend them at the Prize Counter (plushies, diamonds and more).
- SIGF build: the cabinets, sprites, sounds and icon were redrawn and recomposed from scratch and the games renamed (Pellet Muncher, Alien Barrage, Girder Climber, Paddle Rally, Tetrominoes); gameplay is unchanged from the upstream port.
- Beta: a fresh port of an old mod (the Girder Climber cabinet says BETA in game). Report bugs on the SIGF fork's issue tracker.

## SIGF changes

Upstream's arcade art and sounds were taken from Namco, Nintendo, Taito and other games. SIGF replaced every ripped or unclear texture and sound with original pixel art and chiptunes (made by a script, `sigf/assets/gen_assets.py`, designed by Claude for SIGF), renamed the games (Pellet Muncher, Alien Barrage, Girder Climber, Paddle Rally, Tetrominoes, Snake), and set the mod's license string to GPL-3.0, as the repository's LICENSE says. The changes are listed in `SIGF-NOTICE.md` (GPL-3.0 section 5) and `sigf/assets/PROVENANCE.md`.

## What this repository holds

1. The Corresponding Source of the SIGF build: the upstream tree at commit [`eb09571cf6883651f7516da85682b5e9c9c6b166`](https://github.com/Bay4lly/ArcadeMod/tree/eb09571cf6883651f7516da85682b5e9c9c6b166) with `sigf/patches/0001-sigf-original-names-and-license.patch` applied (display names, the GPL-3.0 license string, `SIGF-NOTICE.md`), the unused ripped or unclear files of `sigf/patches/delete.txt` removed, and the textures and sounds made by `sigf/assets/gen_assets.py` in place of upstream's (`sigf/assets/PROVENANCE.md`). These are the exact build inputs: the PNGs regenerate byte for byte, the OGGs decode to the same audio (a fresh run writes a random Ogg stream serial). Every other file is upstream's, unchanged. Upstream's ripped art is not in this repository. Upstream's own `README.md` is there; GitHub shows this file (`.github/README.md`) first.
2. Added by SIGF in the same commit: this file, `sigf/patches/` (the patch, the delete list and the builder script), `sigf/assets/` (the asset generator and the provenance of every replaced file), and `sigf/` (the scripts that built the release assets, for reference: they run inside the SIGF repository).
3. `mashup.json`, the SIGF app recipe (the next commit).
4. The release `v1.0.0` (its tag is the first commit):

| Asset | Size | sha256 | What it is |
|---|---|---|---|
| `arcademod.mrpack` | 1566464 B | `7fc51c8d842a139e18d0d3ea21b5244953ae4a062af78a0278bdf3c76beb0868` | Minecraft 1.20.1 with Forge 47.1.0: the SIGF build `arcademod-1.0-1.20.1-sigf1.jar` (sha256 `75920624...9ee2`) of the source in this repository, with the GPL-3.0 LICENSE and `SIGF-NOTICE.md` under `licenses/`. |

## Licenses

| Part | License | Where |
|---|---|---|
| Arcade Mod Reloaded (Bay4lly; original ArcadeMod by SuperHB / KenLPham) and the SIGF build | GPL-3.0 | `LICENSE`, `SIGF-NOTICE.md` |
| SIGF's replacement art, sounds and scripts | GPL-3.0 (with the mod) | `sigf/assets/PROVENANCE.md` |

## Why this repository exists

The SIGF app (https://sigf.ai) installs mods from recipes (`mashup.json`) whose downloads are pinned release files. This repository makes Arcade Mod Reloaded installable in one click, credited to Bay4lly. If you are the author and want anything changed or taken down, open an issue here.
