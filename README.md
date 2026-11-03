# SpitPlugin

Lets players spit like llamas. Built against Spigot 1.20.1.

`/spit` fires a projectile. `/spitmenu` opens the effect selector.
Both commands use `spit.use`, which is enabled for everyone by default.
`/spit reload` reloads the settings and requires `spit.reload` (operators
only by default). It also works from the console.

Four effects are available: Bone Meal, Dust, Honey and Slime. Bone Meal is
the default. The others add a particle trail and a burst on impact.
Honey and Slime are visual effects; they do not apply potion effects.

By default, spit deals 1 damage before armor and other modifiers and moves
at a random speed between 1.1 and 1.3. There is no cooldown.
Effect selections are kept in memory and reset when the plugin restarts.

## Settings

`plugins/SpitPlugin/config.yml` is created on first start. Edit it and run
`/spit reload`. If the file is invalid, the previous settings stay active
and the console shows the error. An invalid config at startup disables the plugin.
Missing keys use the defaults shipped with the plugin.

| Setting | Default | Accepted values |
| --- | --- | --- |
| `damage` | `1.0` | 0 to 1000 |
| `speed.min`, `speed.max` | `1.1`, `1.3` | 0.1 to 4, min cannot exceed max |
| `sound.enabled` | `true` | true or false |
| `sound.name` | `ENTITY_LLAMA_SPIT` | A Bukkit sound name for the server version |
| `sound.volume`, `sound.pitch` | `1.2`, `0.9` | Volume 0 to 4, pitch 0.5 to 2 |
| `particles.enabled` | `true` | true or false |
| `particles.amount` | `1.0` | Count multiplier from 0 to 5 |
| `enabled-types` | All four types | A nonempty list of DUST, BONE_MEAL, HONEY, SLIME |

Particle settings affect the extra trails and impact bursts. An amount of
0 sends none; 0.5 roughly halves the count, with at least one per component.
The vanilla llama spit appearance is unchanged.

Disabled types disappear from the menu and cannot be selected in an already
open menu. If a selected type is disabled, the player uses Bone Meal if
available, otherwise the first enabled type in the usual menu order.
Reload also changes damage and extra particles for spits already in flight.

## Build

Requires JDK 17 or newer and Maven.

```sh
mvn clean package
```

The JAR is written to `target/spitplugin-1.1.0.jar`. The build also copies it
to `C:\Test-Server\plugins\SpitPlugin.jar`. To use another directory:

```sh
mvn package -Dserver.plugins.dir=target/test-plugins
```

For installation, place the JAR in the server's `plugins` directory and
restart the server.

Run `mvn test` to check configuration validation, reloading, commands and
the settings used by projectiles. Tests use mocks and do not start a server.
