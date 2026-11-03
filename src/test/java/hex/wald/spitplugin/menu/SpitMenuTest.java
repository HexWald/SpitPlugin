package hex.wald.spitplugin.menu;

import hex.wald.spitplugin.SpitPlugin;
import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.model.SpitType;
import hex.wald.spitplugin.service.SpitSelectionService;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpitMenuTest {

    @Test
    public void anAlreadyOpenMenuCannotSelectADisabledType() {
        SpitPlugin plugin = mock(SpitPlugin.class);
        when(plugin.getSettings()).thenReturn(new SpitSettings(new YamlConfiguration()));
        SpitMenu menu = new SpitMenu(plugin, new SpitSelectionService());
        assertEquals(SpitType.HONEY, menu.resolve(Material.HONEYCOMB));

        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled-types", Collections.singletonList("SLIME"));
        when(plugin.getSettings()).thenReturn(new SpitSettings(config));

        assertNull(menu.resolve(Material.HONEYCOMB));
        assertEquals(SpitType.SLIME, menu.resolve(Material.SLIME_BALL));
        assertNull(menu.resolve(Material.STONE));
    }
}
