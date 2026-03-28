package hex.wald.spitplugin.command;

import hex.wald.spitplugin.service.SpitProjectileService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpitCommand implements CommandExecutor {

    private final SpitProjectileService projectileService;

    public SpitCommand(SpitProjectileService projectileService) {
        this.projectileService = projectileService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("spit.use")) {
            player.sendMessage(ChatColor.RED + "No Permission.");
            return true;
        }

        projectileService.launch(player);
        return true;
    }
}
