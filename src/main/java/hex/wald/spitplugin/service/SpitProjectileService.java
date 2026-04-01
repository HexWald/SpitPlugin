package hex.wald.spitplugin.service;

import hex.wald.spitplugin.model.SpitType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;

public class SpitProjectileService {

    private final JavaPlugin plugin;
    private final SpitSelectionService selectionService;
    private final Map<UUID, SpitType> trackedProjectiles = new HashMap<UUID, SpitType>();
    private final BlockData lightPowder = Material.LIGHT_GRAY_CONCRETE_POWDER.createBlockData();
    private final ItemStack honeyItem = new ItemStack(Material.HONEY_BOTTLE);
    private final ItemStack slimeItem = new ItemStack(Material.SLIME_BALL);

    public SpitProjectileService(JavaPlugin plugin, SpitSelectionService selectionService) {
        this.plugin = plugin;
        this.selectionService = selectionService;
    }

    public void launch(Player player) {
        SpitType spitType = selectionService.getSelected(player.getUniqueId());
        Projectile projectile = spitType == SpitType.BONE_MEAL ? launchVanilla(player) : launchCustom(player);

        trackedProjectiles.put(projectile.getUniqueId(), spitType);

        if (spitType != SpitType.BONE_MEAL) {
            startTrail(projectile, spitType);
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.2f, 0.9f);
    }

    public void handleDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        SpitType spitType = trackedProjectiles.get(event.getDamager().getUniqueId());

        if (spitType == null) {
            return;
        }

        event.setDamage(1.0);
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

    private Projectile launchVanilla(Player player) {
        Location location = player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.8));
        LlamaSpit spit = (LlamaSpit) player.getWorld().spawnEntity(location, EntityType.LLAMA_SPIT);
        spit.setShooter(player);
        spit.setVelocity(player.getEyeLocation().getDirection().multiply(randomSpeed()));
        return spit;
    }

    private Projectile launchCustom(Player player) {
        Location location = player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.8));
        LlamaSpit spit = (LlamaSpit) player.getWorld().spawnEntity(location, EntityType.LLAMA_SPIT);
        spit.setShooter(player);
        spit.setVelocity(player.getEyeLocation().getDirection().multiply(randomSpeed()));
        return spit;
    }

    private double randomSpeed() {
        return 1.1 + (Math.random() * 0.2);
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
                new Predicate<Entity>() {
                    @Override
                    public boolean test(Entity entity) {
                        return entity != projectile && entity != shooter;
                    }
                }
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
                damageable.damage(1.0, (Player) projectile.getShooter());
            } else {
                damageable.damage(1.0);
            }
        }

        spawnBurst(impactLocation, spitType);
        trackedProjectiles.remove(projectile.getUniqueId());
        projectile.remove();
        return true;
    }

    private void spawnDustTrail(Location location, Vector velocity) {
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0.05, 0.05, 0.05, 0.006);
        location.getWorld().spawnParticle(Particle.ASH, location, 4, 0.08, 0.08, 0.08, 0.0);
        location.getWorld().spawnParticle(Particle.FALLING_DUST, location, 5, 0.1, 0.1, 0.1, 0.0, lightPowder);
        location.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 1, 0.02, 0.02, 0.02, 0.0);
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 4, 0.07, 0.07, 0.07, 0.0, new Particle.DustOptions(Color.fromRGB(140, 140, 140), 1.35f));
        Location wake = location.clone().subtract(velocity.clone().normalize().multiply(0.12));
        location.getWorld().spawnParticle(Particle.FALLING_DUST, wake, 4, 0.06, 0.06, 0.06, 0.0, lightPowder);
        location.getWorld().spawnParticle(Particle.REDSTONE, wake, 2, 0.05, 0.05, 0.05, 0.0, new Particle.DustOptions(Color.fromRGB(115, 115, 115), 1.05f));
    }

    private void spawnSlimeTrail(Location location, Vector velocity) {
        location.getWorld().spawnParticle(Particle.ITEM_CRACK, location, 5, 0.09, 0.09, 0.09, 0.015, slimeItem);
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0.08, 0.08, 0.08, 0.0, new Particle.DustOptions(Color.fromRGB(85, 205, 85), 1.25f));
        Location wake = location.clone().subtract(velocity.clone().normalize().multiply(0.12));
        location.getWorld().spawnParticle(Particle.ITEM_CRACK, wake, 3, 0.07, 0.07, 0.07, 0.01, slimeItem);
        location.getWorld().spawnParticle(Particle.REDSTONE, wake, 3, 0.06, 0.06, 0.06, 0.0, new Particle.DustOptions(Color.fromRGB(60, 180, 75), 1.0f));
    }

    private void spawnHoneyTrail(Location location, Vector velocity) {
        location.getWorld().spawnParticle(Particle.ITEM_CRACK, location, 4, 0.08, 0.08, 0.08, 0.012, honeyItem);
        location.getWorld().spawnParticle(Particle.WAX_ON, location, 2, 0.07, 0.07, 0.07, 0.0);
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 4, 0.07, 0.07, 0.07, 0.0, new Particle.DustOptions(Color.fromRGB(245, 191, 66), 1.3f));
        Location wake = location.clone().subtract(velocity.clone().normalize().multiply(0.12));
        location.getWorld().spawnParticle(Particle.ITEM_CRACK, wake, 2, 0.06, 0.06, 0.06, 0.01, honeyItem);
        location.getWorld().spawnParticle(Particle.REDSTONE, wake, 2, 0.05, 0.05, 0.05, 0.0, new Particle.DustOptions(Color.fromRGB(214, 149, 36), 1.0f));
    }

    private void spawnBurst(Location location, SpitType spitType) {
        if (spitType == SpitType.DUST) {
            location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 2, 0.14, 0.14, 0.14, 0.012);
            location.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 1, 0.1, 0.1, 0.1, 0.005);
            location.getWorld().spawnParticle(Particle.ASH, location, 8, 0.24, 0.24, 0.24, 0.01);
            location.getWorld().spawnParticle(Particle.FALLING_DUST, location, 12, 0.24, 0.24, 0.24, 0.0, lightPowder);
            location.getWorld().spawnParticle(Particle.REDSTONE, location, 8, 0.18, 0.18, 0.18, 0.0, new Particle.DustOptions(Color.fromRGB(150, 150, 150), 1.75f));
            return;
        }

        if (spitType == SpitType.HONEY) {
            location.getWorld().spawnParticle(Particle.ITEM_CRACK, location, 8, 0.18, 0.18, 0.18, 0.02, honeyItem);
            location.getWorld().spawnParticle(Particle.WAX_ON, location, 4, 0.16, 0.16, 0.16, 0.01);
            location.getWorld().spawnParticle(Particle.REDSTONE, location, 7, 0.18, 0.18, 0.18, 0.0, new Particle.DustOptions(Color.fromRGB(248, 196, 72), 1.75f));
            location.getWorld().spawnParticle(Particle.REDSTONE, location, 4, 0.13, 0.13, 0.13, 0.0, new Particle.DustOptions(Color.fromRGB(196, 124, 22), 1.15f));
            return;
        }

        location.getWorld().spawnParticle(Particle.ITEM_CRACK, location, 9, 0.2, 0.2, 0.2, 0.025, slimeItem);
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 8, 0.18, 0.18, 0.18, 0.0, new Particle.DustOptions(Color.fromRGB(70, 210, 85), 1.7f));
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 4, 0.14, 0.14, 0.14, 0.0, new Particle.DustOptions(Color.fromRGB(35, 145, 55), 1.2f));
    }
}
