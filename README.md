# SpitPlugin

Lets players spit like llamas. Built against Spigot 1.20.1.

`/spit` fires a projectile. `/spitmenu` opens the effect selector.
Both commands use `spit.use`, which is enabled for everyone by default.

Four effects are available: Bone Meal, Dust, Honey and Slime. Bone Meal is
the default. The others add a particle trail and a burst on impact.
Honey and Slime are visual effects; they do not apply potion effects.

Spit deals 1 damage before armor and other modifiers. Speed varies from
1.1 to 1.3. These values and the sound are set in code.
There is no cooldown, configuration file or reload command.
Effect selections are kept in memory and reset when the plugin restarts.

## Build

Requires JDK 17 or newer and Maven.

```sh
mvn clean package
```

The JAR is written to `target/spitplugin-1.0.jar`. The build also copies it
to `C:\Test-Server\plugins\SpitPlugin.jar`. To use another directory:

```sh
mvn package -Dserver.plugins.dir=target/test-plugins
```

For installation, place the JAR in the server's `plugins` directory and
restart the server.
