package hex.wald.spitplugin.model;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum SpitType {
    DUST(Material.GUNPOWDER, ChatColor.DARK_GRAY + "Dust"),
    BONE_MEAL(Material.BONE_MEAL, ChatColor.WHITE + "Bone Meal"),
    SLIME(Material.SLIME_BALL, ChatColor.GREEN + "Slime");

    private final Material material;
    private final String displayName;

    SpitType(Material material, String displayName) {
        this.material = material;
        this.displayName = displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }
}
