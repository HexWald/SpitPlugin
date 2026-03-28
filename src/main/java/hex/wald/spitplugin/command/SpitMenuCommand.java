package hex.wald.spitplugin.command;

import hex.wald.spitplugin.menu.SpitMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpitMenuCommand implements CommandExecutor {

    private final SpitMenu spitMenu;

    public SpitMenuCommand(SpitMenu spitMenu) {
        this.spitMenu = spitMenu;
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

        player.openInventory(spitMenu.create(player));
        return true;
    }
}
