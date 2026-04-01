package hex.wald.spitplugin;

import hex.wald.spitplugin.command.SpitCommand;
import hex.wald.spitplugin.command.SpitMenuCommand;
import hex.wald.spitplugin.listener.SpitMenuListener;
import hex.wald.spitplugin.listener.SpitProjectileListener;
import hex.wald.spitplugin.menu.SpitMenu;
import hex.wald.spitplugin.service.SpitProjectileService;
import hex.wald.spitplugin.service.SpitSelectionService;
import org.bukkit.plugin.java.JavaPlugin;

public class SpitPlugin extends JavaPlugin {


    @Override
    public void onEnable() {
        getLogger().info("Spit Loaded :)");

        SpitSelectionService selectionService = new SpitSelectionService();
        SpitMenu spitMenu = new SpitMenu(selectionService);
        SpitProjectileService projectileService = new SpitProjectileService(this, selectionService);

        getServer().getPluginManager().registerEvents(new SpitMenuListener(selectionService, spitMenu), this);
        getServer().getPluginManager().registerEvents(new SpitProjectileListener(projectileService), this);

        getCommand("spit").setExecutor(new SpitCommand(projectileService));
        getCommand("spitmenu").setExecutor(new SpitMenuCommand(spitMenu));
    }

    @Override
    public void onDisable() {
        getLogger().info("Spit Unloaded :(");
    }
}
