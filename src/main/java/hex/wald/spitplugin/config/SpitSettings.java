package hex.wald.spitplugin.config;

import hex.wald.spitplugin.model.SpitType;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class SpitSettings {

    private final double damage;
    private final double minSpeed;
    private final double maxSpeed;
    private final boolean soundEnabled;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final boolean particlesEnabled;
    private final double particleAmount;
    private final EnumSet<SpitType> enabledTypes;

    public SpitSettings(ConfigurationSection config) {
        for (String section : new String[] {"speed", "sound", "particles"}) {
            if (config.contains(section) && !config.isConfigurationSection(section)) {
                throw new IllegalArgumentException(section + " must be a section.");
            }
        }

        damage = number(config, "damage", 1.0, 0.0, 1000.0);
        minSpeed = number(config, "speed.min", 1.1, 0.1, 4.0);
        maxSpeed = number(config, "speed.max", 1.3, 0.1, 4.0);
        if (minSpeed > maxSpeed) {
            throw new IllegalArgumentException("speed.min cannot be greater than speed.max.");
        }

        soundEnabled = flag(config, "sound.enabled", true);
        Object soundName = config.get("sound.name", "ENTITY_LLAMA_SPIT");
        if (!(soundName instanceof String)) {
            throw new IllegalArgumentException("sound.name must be a Bukkit sound name.");
        }
        try {
            sound = Sound.valueOf(((String) soundName).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown sound.name: " + soundName);
        }
        volume = (float) number(config, "sound.volume", 1.2, 0.0, 4.0);
        pitch = (float) number(config, "sound.pitch", 0.9, 0.5, 2.0);
        particlesEnabled = flag(config, "particles.enabled", true);
        particleAmount = number(config, "particles.amount", 1.0, 0.0, 5.0);

        Object types = config.get("enabled-types");
        if (types == null) {
            enabledTypes = EnumSet.allOf(SpitType.class);
        } else {
            if (!(types instanceof List)) {
                throw new IllegalArgumentException("enabled-types must be a list.");
            }
            enabledTypes = EnumSet.noneOf(SpitType.class);
            for (Object value : (List<?>) types) {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("enabled-types must contain spit names.");
                }
                try {
                    enabledTypes.add(SpitType.valueOf(((String) value).trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown spit type: " + value);
                }
            }
            if (enabledTypes.isEmpty()) {
                throw new IllegalArgumentException("Enable at least one spit type.");
            }
        }
    }

    public double getDamage() {
        return damage;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public int particleCount(int count) {
        if (!particlesEnabled || particleAmount == 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(count * particleAmount));
    }

    public boolean isEnabled(SpitType type) {
        return enabledTypes.contains(type);
    }

    public SpitType getDefaultType() {
        return isEnabled(SpitType.BONE_MEAL) ? SpitType.BONE_MEAL : enabledTypes.iterator().next();
    }

    private static double number(ConfigurationSection config, String path, double fallback, double min, double max) {
        Object value = config.get(path);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(path + " must be a number.");
        }
        double number = ((Number) value).doubleValue();
        if (!Double.isFinite(number) || number < min || number > max) {
            throw new IllegalArgumentException(path + " must be between " + min + " and " + max + ".");
        }
        return number;
    }

    private static boolean flag(ConfigurationSection config, String path, boolean fallback) {
        Object value = config.get(path);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(path + " must be true or false.");
        }
        return (Boolean) value;
    }
}
