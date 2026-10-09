# FreeRPG (Paper 26.x fork)

A maintained fork of [customjack/Minecraft_FreeRPG_1.16](https://github.com/customjack/Minecraft_FreeRPG_1.16),
the free RPG skills plugin (Digging, Woodcutting, Mining, Farming, Fishing, Archery, Beast Mastery,
Swordsmanship, Defense, Axe Mastery, Repair, Agility, Alchemy, Smelting and Enchanting).

The original stopped at Minecraft 1.16. This fork targets current Minecraft on **Paper**.

| | |
|---|---|
| Minecraft / server | Paper **26.2** |
| Java | **25** |
| Optional | WorldGuard 7, PlaceholderAPI |

## What changed from the original

- Builds against `paper-api` 26.2 with Java 25. Removed or renamed APIs were migrated: enchantments, potion
  effects, attributes, entity types, particles, mount events and the potion-data API.
- Supports content added from 1.17 to 26.2: deepslate and copper ores, raw ores, new stones, mangrove, cherry,
  bamboo and pale oak woods, new crops, copper tools and armor, mace, spears, and new mobs such as axolotl, goat,
  warden, camel, sniffer, armadillo, breeze, bogged and creaking.
- Bug fixes, including several open upstream issues:
  - #7 – placing and re-breaking ores with flame pick or mega dig duplicated items and farmed EXP. The tracker
    for placed blocks could also stop saving.
  - #11 – play time was wrong: new players showed about 450,000 hours, and saves kept only the current session.
  - #12 – the leaderboard sort crashed with "Comparison method violates its general contract!".
  - #14 – brewing stand item duplication.
  - #17 – normal fishing catches gave no fishing EXP.
  - #18 – breaking glowstone deleted the held item.
  - #6 – some FreeRPG recipes could be crafted without the skill, from the 2x2 grid or as a mirror image.
  - #9 – per-skill toggles were swapped on every save, and stats were accessed from several threads unsafely.
  - Smaller fixes: salvage lost items when the inventory was full, cactus gave sugar cane EXP, beetroot EXP was
    misspelled in the config, and more.

## Building

```bash
mvn clean package
```

The plugin jar is written to `target/freerpg-<version>.jar`. Copy it to your server's `plugins/` folder.

## Upgrading from the 1.16 plugin

Existing player data and configs are kept. Missing EXP keys are filled in from the bundled defaults. Old potion
and effect names in configs (`JUMP`, `SPEED`, `FAST_DIGGING`, `DAMAGE_RESISTANCE`, …) are still accepted. The
`mining.veinMinerBlocks` list is not overwritten, so add the deepslate and copper ores to it yourself if you want
vein miner to work on them.

## License

MIT, same as the original. See [LICENSE](LICENSE).
