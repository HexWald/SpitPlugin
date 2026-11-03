package hex.wald.spitplugin.service;

import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.model.SpitType;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpitSelectionServiceTest {

    @Test
    public void aDisabledSelectionFallsBackWithoutChangingOtherPlayers() {
        SpitSelectionService service = new SpitSelectionService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        service.setSelected(first, SpitType.HONEY);
        service.setSelected(second, SpitType.SLIME);

        SpitSettings original = new SpitSettings(new YamlConfiguration());
        assertEquals(SpitType.HONEY, service.getSelected(first, original));
        assertEquals(SpitType.SLIME, service.getSelected(second, original));

        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled-types", Arrays.asList("DUST", "SLIME"));
        SpitSettings changed = new SpitSettings(config);
        assertEquals(SpitType.DUST, service.getSelected(first, changed));
        assertEquals(SpitType.SLIME, service.getSelected(second, changed));
        assertEquals(SpitType.HONEY, service.getSelected(first, original));
    }
}
