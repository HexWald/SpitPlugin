package hex.wald.spitplugin;

import hex.wald.spitplugin.command.SpitCommand;
import hex.wald.spitplugin.command.SpitMenuCommand;
import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.listener.SpitMenuListener;
import hex.wald.spitplugin.listener.SpitProjectileListener;
import hex.wald.spitplugin.menu.SpitMenu;
import hex.wald.spitplugin.service.SpitProjectileService;
import hex.wald.spitplugin.service.SpitSelectionService;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SpitPlugin extends JavaPlugin {

    private SpitSettings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!reloadSettings()) {
            getLogger().severe("Fix config.yml before enabling SpitPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Spit Loaded :)");

        SpitSelectionService selectionService = new SpitSelectionService();
        SpitMenu spitMenu = new SpitMenu(this, selectionService);
        SpitProjectileService projectileService = new SpitProjectileService(this, selectionService);

        getServer().getPluginManager().registerEvents(new SpitMenuListener(selectionService, spitMenu), this);
        getServer().getPluginManager().registerEvents(new SpitProjectileListener(projectileService), this);

        SpitCommand spitCommand = new SpitCommand(this, projectileService);
        getCommand("spit").setExecutor(spitCommand);
        getCommand("spit").setTabCompleter(spitCommand);
        getCommand("spitmenu").setExecutor(new SpitMenuCommand(spitMenu));
    }

    @Override
    public void onDisable() {
        getLogger().info("Spit Unloaded :(");
    }

    public SpitSettings getSettings() {
        return settings;
    }

    public boolean reloadSettings() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new File(getDataFolder(), "config.yml"));
            settings = new SpitSettings(config);
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            getLogger().warning("Could not load config.yml: " + e.getMessage());
            return false;
        }
    }
}
