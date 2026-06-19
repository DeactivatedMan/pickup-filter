package me.deac.pickupFilter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MainListener implements Listener {
    private final PickupFilter plugin;

    public MainListener(PickupFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if ( !(event.getEntity() instanceof Player player) ) return;

        ItemStack itemStack = event.getItem().getItemStack();

        if (itemStack.getItemMeta() != null && !plugin.getDataManager().profileContains(player.getUniqueId(), itemStack)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        plugin.getDataManager().removeIndex(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = event.getView().title();
        String titleText = PlainTextComponentSerializer.plainText().serialize(title);
        if (!title.hasDecoration(TextDecoration.BOLD) || !titleText.startsWith("Profile ") || titleText.length() != 9) return; // If title isnt bolded or doesnt start with "Profile"

        char lastChar = titleText.charAt(titleText.length()-1);
        byte profileIndex = (byte) ( Character.isDigit(lastChar) ? Character.getNumericValue(lastChar) : 0 );

        plugin.getLogger().info("Clicked inventory on profile " + profileIndex);
    }
}
