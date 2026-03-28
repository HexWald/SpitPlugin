package hex.wald.spitplugin.listener;

import hex.wald.spitplugin.service.SpitProjectileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class SpitProjectileListener implements Listener {

    private final SpitProjectileService projectileService;

    public SpitProjectileListener(SpitProjectileService projectileService) {
        this.projectileService = projectileService;
    }

    @EventHandler
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        projectileService.handleDamage(event);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        projectileService.handleHit(event);
    }
}
