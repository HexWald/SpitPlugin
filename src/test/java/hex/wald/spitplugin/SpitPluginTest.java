package hex.wald.spitplugin;

import hex.wald.spitplugin.config.SpitSettings;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpitPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private SpitPlugin plugin;
    private Path config;

    @Before
    public void setUp() {
        plugin = mock(SpitPlugin.class);
        when(plugin.getDataFolder()).thenReturn(folder.getRoot());
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(plugin.reloadSettings()).thenCallRealMethod();
        when(plugin.getSettings()).thenCallRealMethod();
        config = folder.getRoot().toPath().resolve("config.yml");
    }

    @Test
    public void pluginMetadataKeepsTheApiVersionAndReloadPermission() throws Exception {
        PluginDescriptionFile description = new PluginDescriptionFile(getClass().getResourceAsStream("/plugin.yml"));
        assertEquals("1.20", description.getAPIVersion());
        assertTrue(description.getCommands().containsKey("spit"));
        assertEquals(PermissionDefault.OP, description.getPermissions().stream()
                .filter(permission -> permission.getName().equals("spit.reload"))
                .findFirst().get().getDefault());
    }

    @Test
    public void aValidReloadReplacesTheSettings() throws Exception {
        write("damage: 2\n");
        assertTrue(plugin.reloadSettings());
        SpitSettings before = plugin.getSettings();
        write("damage: 3\nsound:\n  enabled: false\n");
        assertTrue(plugin.reloadSettings());
        assertNotSame(before, plugin.getSettings());
        assertEquals(3.0, plugin.getSettings().getDamage(), 0.0);
        assertFalse(plugin.getSettings().isSoundEnabled());
    }

    @Test
    public void invalidValuesDoNotApplyAnyOfTheReload() throws Exception {
        write("damage: 2\n");
        assertTrue(plugin.reloadSettings());
        SpitSettings before = plugin.getSettings();
        write("damage: 7\nenabled-types: [FIRE]\n");
        assertFalse(plugin.reloadSettings());
        assertSame(before, plugin.getSettings());
        assertEquals(2.0, plugin.getSettings().getDamage(), 0.0);
    }

    @Test
    public void brokenYamlKeepsTheWorkingSettings() throws Exception {
        write("damage: 2\n");
        assertTrue(plugin.reloadSettings());
        SpitSettings before = plugin.getSettings();
        write("enabled-types: [HONEY\n");
        assertFalse(plugin.reloadSettings());
        assertSame(before, plugin.getSettings());
    }

    @Test
    public void missingFileKeepsTheWorkingSettings() throws Exception {
        write("damage: 2\n");
        assertTrue(plugin.reloadSettings());
        SpitSettings before = plugin.getSettings();
        Files.delete(config);
        assertFalse(plugin.reloadSettings());
        assertSame(before, plugin.getSettings());
    }

    @Test
    public void invalidFirstLoadDoesNotCreateSettings() throws Exception {
        write("damage: -2\n");
        assertFalse(plugin.reloadSettings());
        assertNull(plugin.getSettings());
    }

    private void write(String contents) throws Exception {
        Files.write(config, contents.getBytes(StandardCharsets.UTF_8));
    }
}
