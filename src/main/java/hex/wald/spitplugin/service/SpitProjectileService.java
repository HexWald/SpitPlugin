package hex.wald.spitplugin.service;

import hex.wald.spitplugin.SpitPlugin;
import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.model.SpitType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SpitProjectileService {

    private final SpitPlugin plugin;
    private final SpitSelectionService selectionService;
    private final NamespacedKey spitKey;
    private final Map<UUID, SpitType> trackedProjectiles = new HashMap<>();
    private final BlockData lightPowder = Material.LIGHT_GRAY_CONCRETE_POWDER.createBlockData();
    private final ItemStack honeyItem = new ItemStack(Material.HONEY_BOTTLE);
    private final ItemStack slimeItem = new ItemStack(Material.SLIME_BALL);

    public SpitProjectileService(SpitPlugin plugin, SpitSelectionService selectionService) {
        this.plugin = plugin;
        this.selectionService = selectionService;
        this.spitKey = new NamespacedKey(plugin, "spit");
    }

    public void launch(Player player) {
        SpitSettings settings = plugin.getSettings();
        SpitType spitType = selectionService.getSelected(player.getUniqueId(), settings);
        Projectile projectile = launchSpit(player);

        trackedProjectiles.put(projectile.getUniqueId(), spitType);

        if (spitType != SpitType.BONE_MEAL) {
            startTrail(projectile, spitType);
        }

        if (settings.isSoundEnabled()) {
            player.getWorld().playSound(player.getLocation(), settings.getSound(), settings.getVolume(), settings.getPitch());
        }
    }

    public void handleDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        if (!event.getDamager().getPersistentDataContainer().has(spitKey, PersistentDataType.BYTE)) {
            return;
        }

        event.setDamage(plugin.getSettings().getDamage());
    }

    public void handleHit(ProjectileHitEvent event) {
        SpitType spitType = trackedProjectiles.remove(event.getEntity().getUniqueId());

        if (spitType == null) {
            return;
        }

        if (spitType != SpitType.BONE_MEAL) {
            spawnBurst(event.getEntity().getLocation(), spitType);
        }
    }

    private Projectile launchSpit(Player player) {
        Location location = player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.8));
        LlamaSpit spit = (LlamaSpit) player.getWorld().spawnEntity(location, EntityType.LLAMA_SPIT);
        spit.getPersistentDataContainer().set(spitKey, PersistentDataType.BYTE, (byte) 1);
        spit.setShooter(player);
        spit.setVelocity(player.getEyeLocation().getDirection().multiply(randomSpeed()));
        return spit;
    }

    private double randomSpeed() {
        SpitSettings settings = plugin.getSettings();
        return settings.getMinSpeed() + Math.random() * (settings.getMaxSpeed() - settings.getMinSpeed());
    }

    private void startTrail(final Projectile projectile, final SpitType spitType) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!projectile.isValid() || projectile.isDead()) {
                    trackedProjectiles.remove(projectile.getUniqueId());
                    cancel();
                    return;
                }

                if (handleCustomCollision(projectile, spitType)) {
                    cancel();
                    return;
                }

                Location location = projectile.getLocation().clone();

                if (spitType == SpitType.DUST) {
                    spawnDustTrail(location, projectile.getVelocity());
                    return;
                }

                if (spitType == SpitType.HONEY) {
                    spawnHoneyTrail(location, projectile.getVelocity());
                    return;
                }

                spawnSlimeTrail(location, projectile.getVelocity());
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean handleCustomCollision(Projectile projectile, SpitType spitType) {
        Vector velocity = projectile.getVelocity();
        double distance = Math.max(velocity.length(), 0.35D);
        Entity shooter = projectile.getShooter() instanceof Entity ? (Entity) projectile.getShooter() : null;
        RayTraceResult result = projectile.getWorld().rayTrace(
                projectile.getLocation(),
                velocity.clone().normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true,
                0.2,
                entity -> entity != projectile && entity != shooter
        );

        if (result == null) {
            return false;
        }

        Location impactLocation = result.getHitPosition() == null
                ? projectile.getLocation()
                : result.getHitPosition().toLocation(projectile.getWorld());

        if (result.getHitEntity() instanceof Damageable) {
            Damageable damageable = (Damageable) result.getHitEntity();

            if (projectile.getShooter() instanceof Player) {
                damageable.damage(plugin.getSettings().getDamage(), (Player) projectile.getShooter());
            } else {
                damageable.damage(plugin.getSettings().getDamage());
            }
        }

        spawnBurst(impactLocation, spitType);
        trackedProjectiles.remove(projectile.getUniqueId());
        projectile.remove();
        return true;
    }

    private void spawnDustTrail(Location location, Vector velocity) {
        spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0.05, 0.05, 0.05, 0.006);
        spawnParticle(Particle.ASH, location, 4, 0.08, 0.08, 0.08, 0.0);
        spawnParticle(Particle.FALLING_DUST, location, 5, 0.1, 0.1, 0.1, 0.0, lightPowder);
        spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 1, 0.02, 0.02, 0.02, 0.0);
        spawnParticle(Particle.REDSTONE, location, 4, 0.07, 0.07, 0.07, 0.0, new Particle.DustOptions(Color.fromRGB(140, 140, 140), 1.35f));
        Location wake = location.clone().subtract(velocity.clone().normalize().multiply(0.12));
        spawnParticle(Particle.FALLING_DUST, wake, 4, 0.06, 0.06, 0.06, 0.0, lightPowder);
        spawnParticle(Particle.REDSTONE, wake, 2, 0.05, 0.05, 0.05, 0.0, new Particle.DustOptions(Color.fromRGB(115, 115, 115), 1.05f));
    }

    private void spawnSlimeTrail(Location location, Vector velocity) {
        spawnParticle(Particle.ITEM_CRACK, location, 5, 0.09, 0.09, 0.09, 0.015, slimeItem);
        spawnParticle(Particle.REDSTONE, location, 5, 0.08, 0.08, 0.08, 0.0, new Particle.DustOptions(Color.fromRGB(85, 205, 85), 1.25f));
        Location wake = location.clone().subtract(velocity.clone().normalize().multiply(0.12));
        spawnParticle(Particle.ITEM_CRACK, wake, 3, 0.07, 0.07, 0.07, 0.01, slimeItem);
        spawnParticle(Particle.REDSTONE, wake, 3, 0.06, 0.06, 0.06, 0.0, new Particle.DustOptions(Color.fromRGB(60, 180, 75), 1.0f));
    }

    private void spawnHoneyTrail(Location location, Vector velocity) {
        spawnParticle(Particle.ITEM_CRACK, location, 4, 0.08, 0.08, 0.08, 0.012, honeyItem);
        spawnParticle(Particle.WAX_ON, location, 2, 0.07, 0.07, 0.07, 0.0);
        spawnParticle(Particle.REDSTONE, location, 4, 0.07, 0.07, 0.07, 0.0, new Particle.DustOptions(Color.fromRGB(245, 191, 66), 1.3f));
        Location wake = location.clone().subtract(velocity.clone().normalize().multiply(0.12));
        spawnParticle(Particle.ITEM_CRACK, wake, 2, 0.06, 0.06, 0.06, 0.01, honeyItem);
        spawnParticle(Particle.REDSTONE, wake, 2, 0.05, 0.05, 0.05, 0.0, new Particle.DustOptions(Color.fromRGB(214, 149, 36), 1.0f));
    }

    private void spawnBurst(Location location, SpitType spitType) {
        if (spitType == SpitType.DUST) {
            spawnParticle(Particle.SMOKE_NORMAL, location, 2, 0.14, 0.14, 0.14, 0.012);
            spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 1, 0.1, 0.1, 0.1, 0.005);
            spawnParticle(Particle.ASH, location, 8, 0.24, 0.24, 0.24, 0.01);
            spawnParticle(Particle.FALLING_DUST, location, 12, 0.24, 0.24, 0.24, 0.0, lightPowder);
            spawnParticle(Particle.REDSTONE, location, 8, 0.18, 0.18, 0.18, 0.0, new Particle.DustOptions(Color.fromRGB(150, 150, 150), 1.75f));
            return;
        }

        if (spitType == SpitType.HONEY) {
            spawnParticle(Particle.ITEM_CRACK, location, 8, 0.18, 0.18, 0.18, 0.02, honeyItem);
            spawnParticle(Particle.WAX_ON, location, 4, 0.16, 0.16, 0.16, 0.01);
            spawnParticle(Particle.REDSTONE, location, 7, 0.18, 0.18, 0.18, 0.0, new Particle.DustOptions(Color.fromRGB(248, 196, 72), 1.75f));
            spawnParticle(Particle.REDSTONE, location, 4, 0.13, 0.13, 0.13, 0.0, new Particle.DustOptions(Color.fromRGB(196, 124, 22), 1.15f));
            return;
        }

        spawnParticle(Particle.ITEM_CRACK, location, 9, 0.2, 0.2, 0.2, 0.025, slimeItem);
        spawnParticle(Particle.REDSTONE, location, 8, 0.18, 0.18, 0.18, 0.0, new Particle.DustOptions(Color.fromRGB(70, 210, 85), 1.7f));
        spawnParticle(Particle.REDSTONE, location, 4, 0.14, 0.14, 0.14, 0.0, new Particle.DustOptions(Color.fromRGB(35, 145, 55), 1.2f));
    }

    private void spawnParticle(Particle particle, Location location, int count, double x, double y, double z, double speed) {
        spawnParticle(particle, location, count, x, y, z, speed, null);
    }

    private void spawnParticle(Particle particle, Location location, int count, double x, double y, double z, double speed, Object data) {
        int amount = plugin.getSettings().particleCount(count);
        if (amount > 0) {
            location.getWorld().spawnParticle(particle, location, amount, x, y, z, speed, data);
        }
    }
}
