package hex.wald.spitplugin.config;

import hex.wald.spitplugin.model.SpitType;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpitSettingsTest {

    @Test
    public void bundledConfigKeepsTheOriginalDefaults() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream("/config.yml"), StandardCharsets.UTF_8)) {
            config.load(reader);
        }
        SpitSettings settings = new SpitSettings(config);
        assertEquals(1.0, settings.getDamage(), 0.0);
        assertEquals(1.1, settings.getMinSpeed(), 0.0);
        assertEquals(1.3, settings.getMaxSpeed(), 0.0);
        assertEquals(Sound.ENTITY_LLAMA_SPIT, settings.getSound());
        assertTrue(settings.isSoundEnabled());
        assertEquals(1.2f, settings.getVolume(), 0.0f);
        assertEquals(0.9f, settings.getPitch(), 0.0f);
        assertEquals(5, settings.particleCount(5));
        for (SpitType type : SpitType.values()) {
            assertTrue(settings.isEnabled(type));
        }
    }

    @Test
    public void missingKeysUseDefaults() {
        SpitSettings settings = new SpitSettings(new YamlConfiguration());
        assertEquals(SpitType.BONE_MEAL, settings.getDefaultType());
        assertEquals(1.0, settings.getDamage(), 0.0);
        assertEquals(5, settings.particleCount(5));
    }

    @Test
    public void zeroParticlesAndDisabledParticlesBothReturnZero() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("particles.amount", 0.0);
        assertEquals(0, new SpitSettings(config).particleCount(5));
        config.set("particles.amount", 2.0);
        config.set("particles.enabled", false);
        assertEquals(0, new SpitSettings(config).particleCount(5));
    }

    @Test
    public void particleAmountScalesWithoutUsingTheZeroCountMode() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("particles.amount", 2.0);
        assertEquals(10, new SpitSettings(config).particleCount(5));
        config.set("particles.amount", 0.01);
        assertEquals(1, new SpitSettings(config).particleCount(5));
    }

    @Test
    public void disabledBoneMealFallsBackToAnEnabledType() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled-types", Arrays.asList("honey", "HONEY"));
        SpitSettings settings = new SpitSettings(config);
        assertEquals(SpitType.HONEY, settings.getDefaultType());
        assertTrue(settings.isEnabled(SpitType.HONEY));
        assertFalse(settings.isEnabled(SpitType.BONE_MEAL));
    }

    @Test
    public void zeroDamageAndFixedSpeedAreAllowed() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("damage", 0);
        config.set("speed.min", 2);
        config.set("speed.max", 2);
        SpitSettings settings = new SpitSettings(config);
        assertEquals(0.0, settings.getDamage(), 0.0);
        assertEquals(settings.getMinSpeed(), settings.getMaxSpeed(), 0.0);
    }

    @Test
    public void rejectsInvalidNumbersAndWrongValueTypes() {
        for (Object damage : Arrays.asList(-1, 1001, "2", Double.NaN, Double.POSITIVE_INFINITY)) {
            assertRejected("damage", damage);
        }
        assertRejected("speed.min", 0);
        assertRejected("speed.max", 5);
        assertRejected("sound.volume", -1);
        assertRejected("sound.pitch", 3);
        assertRejected("particles.amount", 6);
        assertRejected("particles.enabled", "false");
        assertRejected("sound.enabled", "yes");
        assertRejected("speed", "fast");
        assertRejected("sound", true);
        assertRejected("particles", 4);
        assertRejected("sound.name", "NOT_A_SOUND");
        assertRejected("sound.name", 7);
    }

    @Test
    public void rejectsReversedSpeedRange() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("speed.min", 3);
        config.set("speed.max", 2);
        assertThrows(IllegalArgumentException.class, () -> new SpitSettings(config));
    }

    @Test
    public void rejectsEmptyAndUnknownSpitTypes() {
        assertRejected("enabled-types", Collections.emptyList());
        assertRejected("enabled-types", "HONEY");
        assertRejected("enabled-types", Arrays.asList("HONEY", "FIRE"));
        assertRejected("enabled-types", Arrays.asList("HONEY", 12));
    }

    private void assertRejected(String path, Object value) {
        YamlConfiguration config = new YamlConfiguration();
        config.set(path, value);
        assertThrows(path, IllegalArgumentException.class, () -> new SpitSettings(config));
    }
}
