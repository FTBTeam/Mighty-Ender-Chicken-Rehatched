# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.6]

### Fixed
- Added missing translation message for players killed by the chicken's laser
- Chicken can now only be hurt by players (and not fake players or any other entity) by default. New config settings:
  - `only_players_can_hurt_chicken` default true
  - `fake_players_can_hurt_chicken` default false

## [21.1.5]

### Fixed
- Fixed some modded weapon items with an AoE attack being able to ignore the chicken's forcefield

## [21.1.4]

### Added
- Players near the Ender Chicken (but outside the arena radius) are now warned every 5 seconds if despawn is imminent due to lack of players in the arena

### Fixed
- Fixed the Ender Chicken leaving an uninteractable zombie rider behind if it despawned due to no players in arena

## [21.1.3]

### Changed
- Don't try to teleport to spawn position if the chicken has moved to a new dimension

## [21.1.2]

### Changed
- Don't play boss music if Ender Chicken's AI has been disabled, e.g. by Industrial Foregoing Stasis Chamber

## [21.1.1]

### Fixed
- Fixed spurious shield up/down events being posted when the entity is being deserialized from NBT

## [21.1.0]

### Added

- The mod
