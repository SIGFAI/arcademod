# SIGF modified version

This is a modified version of Arcade Mod Reloaded (https://github.com/Bay4lly/ArcadeMod, commit
eb09571cf6883651f7516da85682b5e9c9c6b166) by Bay4lly (1.20.1 port) and SuperHB / KenLPham (original 1.12.2 mod),
licensed under the GNU General Public License v3.0 (see LICENSE). Changes by SIGF, 2026-10-05:

- Replaced the arcade sprite sheets, cabinet textures, Girder Climber textures, plushie and prize counter textures,
  the mod icon and every sound with original pixel art and chiptune sounds made for SIGF (no assets from Namco,
  Nintendo, Taito, Atari or any other game). Removed unused textures (`textures/arcades/`, `textures/kong/`,
  `textures/gui/kong.png`, `textures/gui/kong.psd`, `textures/gui/kong/final.png`, `lose.png`, `logo.png`,
  `textures/block/plushie/cow.png`).
- Renamed the games: Pac-Man -> Pellet Muncher, Space Invaders -> Alien Barrage, Kong / Donkey Kong -> Girder Climber,
  Pong -> Paddle Rally, Tetris -> Tetrominoes (display names only; internal ids unchanged).
- `gradle.properties`: `mod_license` set to GPL-3.0-only (matches LICENSE), version suffix `-sigf1`, removed the
  author's local `org.gradle.java.home`.

How the new assets were made: https://github.com/SIGFAI/arcademod (see the SIGF library recipe, PROVENANCE.md).
