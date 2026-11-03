package hex.wald.spitplugin.menu;

import hex.wald.spitplugin.SpitPlugin;
import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.model.SpitType;
import hex.wald.spitplugin.service.SpitSelectionService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpitMenu {

    private static final String TITLE = ChatColor.WHITE + "Spit Menu";
    private final SpitPlugin plugin;
    private final SpitSelectionService selectionService;

    public SpitMenu(SpitPlugin plugin, SpitSelectionService selectionService) {
        this.plugin = plugin;
        this.selectionService = selectionService;
    }

    public Inventory create(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        ItemStack filler = createItem(Material.WHITE_STAINED_GLASS_PANE, " ", false);
        SpitSettings settings = plugin.getSettings();
        SpitType selected = selectionService.getSelected(player.getUniqueId(), settings);

        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, filler);
        }

        for (int slot = 18; slot < 27; slot++) {
            inventory.setItem(slot, filler);
        }

        int slot = 10;
        for (SpitType type : SpitType.values()) {
            if (settings.isEnabled(type)) {
                inventory.setItem(slot, createItem(type.getMaterial(), type.getDisplayName(), selected == type));
            }
            slot += 2;
        }
        return inventory;
    }

    public boolean isMenu(String title) {
        return TITLE.equals(title);
    }

    public SpitType resolve(Material material) {
        for (SpitType spitType : SpitType.values()) {
            if (spitType.getMaterial() == material && plugin.getSettings().isEnabled(spitType)) {
                return spitType;
            }
        }

        return null;
    }

    private ItemStack createItem(Material material, String name, boolean selected) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            if (selected) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        if (selected) {
            item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        }

        return item;
    }
}
