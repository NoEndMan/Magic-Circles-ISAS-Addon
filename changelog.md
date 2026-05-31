# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-05-31

### Changed
- Circles are now centered to the client cursor and for other entities, to the caster head, by default. 
(Applied when the mod config file is created. Circle offset is changeable using the mod config).
- Abandoned vanilla transparency as it causes unwanted occlusion with sky and water, 
even if circle is rendered as transparent. Using instead a custom code to fake fade in/out 
effect using color manipulation.
- Completely changed how the circles are being rendered: Circles are now rendered as "client side entities" 
in order to achieve a general shader compatibility. Those entities are never saved to the level, only 
registered to the client and only and viewable to the client. This is done in a way that allowing the mod 
to be client side only.
- Refined fade-in-scaling animation for scale 5 circle
- New size for circles in scale 1-3

### Fixed
- Fixed circles not displayed fully because of water mask.
- Fixed circles loosing opacity depending on where the caster is looking at.
- Fixed circles rendering behind clouds even if they are being closer to camera.
- Fixed circles not always appearing glowing in the dark. 
- Fixed circles style change using the config not always working.
- Fixed circles fade out movement
- Achieved general shader compatibility.

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