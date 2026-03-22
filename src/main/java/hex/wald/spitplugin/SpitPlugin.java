package hex.wald.spitplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpitPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("Spit Loaded :)");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Spit Unloaded :(");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("spit")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("spit.use")) {
                player.sendMessage(ChatColor.RED + "No Permission.");
                return true;
            }

            Location location = player.getEyeLocation()
                    .add(player.getLocation().getDirection().multiply(0.8));

            Entity spit = player.getWorld().spawnEntity(location, EntityType.LLAMA_SPIT);

            double speed = 2.3 + (Math.random() * 0.5);
            spit.setVelocity(player.getEyeLocation().getDirection().multiply(speed));

            player.getWorld().playSound(
                    player.getLocation(),
                    Sound.ENTITY_LLAMA_SPIT,
                    1.2f,
                    0.9f
            );

            return true;
        }

        return false;
    }

    @EventHandler
    public void onSpitHit(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.LLAMA_SPIT) {
            e.setDamage(1.0);
        }
    }
}