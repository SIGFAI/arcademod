// Arcade Mod Reloaded (Bay4lly's Forge 1.20.1 port of SuperHB / KenLPham's ArcadeMod, GPL-3.0): playable arcade
// cabinets in Minecraft (Pellet Muncher, Alien Barrage, Girder Climber, Paddle Rally, Tetrominoes, Snake) with coins,
// tickets and a prize counter. Single-game mod. Upstream's assets were ripped from Namco / Nintendo / Taito games, so
// SIGF ships its own build: commit eb09571 + patches/ (display names, license string) + every ripped or unclear asset
// replaced by assets/gen_assets.py (original pixel art and chiptunes, assets/PROVENANCE.md), built on a disposable AWS
// builder (library/QC.md section 4; source.json "built"). The upstream Modrinth jar is never used.
//   SIGF_LIBRARY_BUILDS=<dir> node library/arcademod/build.mjs      (outputs: library/lib.mjs)
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { instanceName } from '../../orchestrator/scripts/package-fusion.mjs';
import { asset, card, dl, emit, rawAt } from '../lib.mjs';
import { builtArtifacts, builtField, forgePack, sourceOf } from '../um-gta5-passthrough/sigf-build.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const ID = 'arcademod', VERSION = '1.0.0', NAME = 'Arcade Mod Reloaded';
const SRC = sourceOf(ID);
const UP = { repo: SRC.repo, commit: SRC.commit, authors: ['Bay4lly', 'KenLPham'], original: 'https://github.com/KenLPham/ArcadeMod' };
const MC = { mc: '1.20.1', forge: '47.1.0', java: '17' }; // gradle.properties at the commit (forge_version, range [47,))
const JAR = 'arcademod-1.0-1.20.1-sigf1.jar';
const TAGLINE = 'Working arcade cabinets in Minecraft: insert coins, play Pellet Muncher, Alien Barrage, Girder Climber, Paddle Rally, Tetrominoes and Snake, win tickets and trade them at the prize counter.';

const files = builtArtifacts(ID);
const license = await rawAt(UP.repo, UP.commit, 'LICENSE');
// The GPL notice of our changes is the SIGF-NOTICE.md the patch adds (shipped beside LICENSE in the pack).
const patch = fs.readFileSync(path.join(here, 'patches', '0001-sigf-original-names-and-license.patch'), 'utf8');
const notice = patch.split(/^diff --git /m).find(d => d.startsWith('a/SIGF-NOTICE.md'));
if (!notice) throw new Error('patch has no SIGF-NOTICE.md');
const noticeText = notice.split('\n').filter(l => l.startsWith('+') && !l.startsWith('+++')).map(l => l.slice(1)).join('\n') + '\n';

const pack = asset(`${ID}.mrpack`, forgePack({
  name: NAME, summary: TAGLINE, versionId: VERSION, mc: MC.mc, forge: MC.forge,
  overrides: [
    { name: `overrides/mods/${JAR}`, data: files.get(JAR) },
    { name: 'overrides/licenses/arcademod-LICENSE.txt', data: license },
    { name: 'overrides/licenses/arcademod-SIGF-NOTICE.md', data: Buffer.from(noticeText) },
  ],
}));
const assets = [pack];

const make = (urls, set) => ({
  id: `sigf/${ID}`,
  version: VERSION,
  name: NAME,
  tagline: TAGLINE,
  kind: 'mod',
  games: [
    { game: 'minecraft', role: 'host', label: 'Minecraft', engine: `Minecraft Java ${MC.mc} + Forge ${MC.forge} mod (Java)`, mc: MC.mc, loader: `forge@${MC.forge}`, java: MC.java },
  ],
  requires: [{ id: 'forge', version: MC.forge }],
  install: [{ game: 'minecraft', strategy: 'mrpack', pack: { src: pack.name, ...dl(pack, urls) } }],
  launch: [{ game: 'minecraft' }],
  files: set.map(a => ({ name: a.name, ...dl(a, urls) })),
  source: {
    repo: UP.repo, license: 'GPL-3.0', upstream_license: 'GPL-3.0 (LICENSE; mods.toml said All Rights Reserved, the Forge MDK default)', commit: UP.commit,
    hosted: `https://github.com/SIGFAI/${ID}`,
    derived_from: { repo: UP.original, license: 'GPL-3.0', note: 'original 1.12.2 mod by SuperHB (KenLPham)' },
    built: builtField(ID),
    modified: 'every ripped or unclear texture and sound replaced with original SIGF assets; game names changed (library/arcademod/assets/PROVENANCE.md)',
  },
  media: {},
  built_by: { author: UP.authors[0], authors: UP.authors, packaged_by: 'SIGF' },
  idea_by: 'KenLPham',
  built_at: '2026-10-05T00:00:00.000Z',
  ...card(UP.repo),
  // The fork has issues disabled: bugs go to the SIGF fork that carries this build.
  issues: `https://github.com/SIGFAI/${ID}/issues`,
  notes: [
    'You need Minecraft: Java Edition. The app makes its own Prism instance "' + instanceName(`sigf/${ID}`) + `" (Minecraft ${MC.mc}, Forge ${MC.forge}, Java ${MC.java}).`,
    'Craft coins, place an arcade machine, right-click it and insert a coin. Scores pay out tickets; spend them at the Prize Counter (plushies, diamonds and more).',
    'SIGF build: the cabinets, sprites, sounds and icon were redrawn and recomposed from scratch and the games renamed (Pellet Muncher, Alien Barrage, Girder Climber, Paddle Rally, Tetrominoes); gameplay is unchanged from the upstream port.',
    'Beta: a fresh port of an old mod (the Girder Climber cabinet says BETA in game). Report bugs on the SIGF fork\'s issue tracker.',
  ],
});

emit({ slug: ID, version: VERSION, assets, make });
