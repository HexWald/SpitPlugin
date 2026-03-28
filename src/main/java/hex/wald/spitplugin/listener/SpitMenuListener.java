package hex.wald.spitplugin.listener;

import hex.wald.spitplugin.menu.SpitMenu;
import hex.wald.spitplugin.model.SpitType;
import hex.wald.spitplugin.service.SpitSelectionService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SpitMenuListener implements Listener {

    private final SpitSelectionService selectionService;
    private final SpitMenu spitMenu;

    public SpitMenuListener(SpitSelectionService selectionService, SpitMenu spitMenu) {
        this.selectionService = selectionService;
        this.spitMenu = spitMenu;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!spitMenu.isMenu(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        SpitType spitType = spitMenu.resolve(item.getType());

        if (spitType == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        selectionService.setSelected(player.getUniqueId(), spitType);
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected spit type: " + spitType.getDisplayName());
    }
}
