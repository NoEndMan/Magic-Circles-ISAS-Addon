# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.1] - WIP

### Changed
- Abandoned vanilla transparency as it causes unwanted occlusion with sky and water, even if circle is rendered 
as transparent. Using instead a custom code for a "fake" fade in/out using color manipulation.
- Abandoned vanilla additive transparent style as it causes color tint with sky. Circles would move back to use, 
"solid" style, with custom logic to make it appears glowing no matter the time.

### Fixed
- Fixed circles not displayed fully when some part of it is underwater and some part of it above water.
- Fixed circles loosing opacity depending on where the caster is looking at.
- Fixed circles rendering behind clouds even if they are being closer to camera.
- Fixed circles not always appearing glowing.

## [1.1.0] - 2026-04-11
### Added
- New floor-wheel circles animations
- New circles textures
- Client configuration to control circles offset and circles style (between old and new render)

### Changed
- Code overhaul
- New textures, animations and render for the rendered magic circles (configurable, used by default)
- Floor-wheel circles would be rendered only for long pre-casting time spells (like summoning spells).

### Fixed
- Fixed compatibility with [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks) backport versions (3.15.0+)
- Fixed magic circles not rendered for mobs and for other players
- Fixed magic circles rendered not intentionally for instant-cast type spells
- Fixed hand positioned magic circle not following caster look direction correctly
- Fixed mod description