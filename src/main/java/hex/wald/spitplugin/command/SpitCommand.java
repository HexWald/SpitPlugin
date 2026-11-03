package hex.wald.spitplugin.command;

import hex.wald.spitplugin.SpitPlugin;
import hex.wald.spitplugin.service.SpitProjectileService;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SpitCommand implements CommandExecutor, TabCompleter {

    private final SpitPlugin plugin;
    private final SpitProjectileService projectileService;

    public SpitCommand(SpitPlugin plugin, SpitProjectileService projectileService) {
        this.plugin = plugin;
        this.projectileService = projectileService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("spit.reload")) {
                sender.sendMessage(ChatColor.RED + "No Permission.");
                return true;
            }
            if (plugin.reloadSettings()) {
                sender.sendMessage(ChatColor.GREEN + "Spit settings reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "Could not reload config.yml. Previous settings are still active. Check the console.");
            }
            return true;
        }
        if (args.length != 0) {
            return false;
        }
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("spit.reload")
                && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
