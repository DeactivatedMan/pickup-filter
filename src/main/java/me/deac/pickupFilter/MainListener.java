package me.deac.pickupFilter;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainListener implements Listener {
    private final PickupFilter plugin;

    public MainListener(PickupFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        plugin.getLogger().info("Pickup event!");
        if ( !(event.getEntity() instanceof Player player) ) return;
        plugin.getLogger().info("It's a player!");

        ItemStack itemStack = event.getItem().getItemStack();
        plugin.getLogger().info("They're holding " + itemStack.getType().name() + "!");
        if (itemStack.getItemMeta() != null) plugin.getLogger().info("It has item meta!");

        boolean wasd = plugin.getDataManager().profileContains(player.getUniqueId(), itemStack);
        plugin.getLogger().info("The profile does"+(wasd ? "" : "n't")+" contain it!");

        if (itemStack.getItemMeta() != null && !wasd) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        plugin.getDataManager().removeIndex(event.getPlayer().getUniqueId());
    }

    /*@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = event.getView().title();
        String titleText = PlainTextComponentSerializer.plainText().serialize(title);
        if (!title.hasDecoration(TextDecoration.BOLD) || !titleText.startsWith("Profile ") || titleText.length() != 9) return; // If title isnt bolded or doesnt start with "Profile"

        char lastChar = titleText.charAt(titleText.length()-1);
        byte profileIndex = (byte) ( Character.isDigit(lastChar) ? Character.getNumericValue(lastChar) : 0 );

        plugin.getLogger().info("Clicked inventory on profile " + profileIndex);
    }*/

    //region Filter Inventory
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof FilterMenuHolder holder)) return;
        event.setCancelled(true);

        Inventory topInventory = event.getView().getTopInventory();
        ItemStack clickedItem = event.getCurrentItem();
        //ItemStack cursorItem = event.getCursor();

        // Player clicked in filter inventory
        if (
                event.getClickedInventory() == topInventory
                && clickedItem != null &&
                clickedItem.getType() != Material.AIR
        ) {
            event.setCurrentItem(null);
            updateProfile(event.getWhoClicked().getUniqueId(), holder.index, topInventory);
        }

        // Player clicked in own inventory
        else if (
                event.getClickedInventory() == event.getView().getBottomInventory()
                && clickedItem != null &&
                clickedItem.getType() != Material.AIR
        ) {
            addItemClone(topInventory, clickedItem);
            updateProfile(event.getWhoClicked().getUniqueId(), holder.index, topInventory);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof FilterMenuHolder holder)) return;

        // Cancel default dragging mechanics
        event.setCancelled(true);

        Inventory topInventory = event.getView().getTopInventory();
        ItemStack draggedItem = event.getOldCursor();

        if (draggedItem.getType() != Material.AIR) {
            // Check if any of the dragged slots are in the top inventory
            for (int slot : event.getRawSlots()) {
                if (slot < topInventory.getSize()) {
                    addItemClone(topInventory, draggedItem);
                    updateProfile(event.getWhoClicked().getUniqueId(), holder.index, topInventory);
                    break; // Only add one clone
                }
            }
        }
    }

    private void addItemClone(Inventory inventory, ItemStack original) {
        // Check if the item is already anywhere in the filter menu to prevent duplicates
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.isSimilar(original)) return;
        }

        int firstEmpty = inventory.firstEmpty();
        if (firstEmpty != -1) {
            ItemStack clone = original.clone();
            clone.setAmount(1); // Force quantity to exactly 1
            inventory.setItem(firstEmpty, clone);
        }
    }

    private void updateProfile(UUID uuid, byte index, Inventory inventory) {
        List<ItemStack> stackList = new ArrayList<>();
        for (int i=0; i < 27; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null) stackList.add(itemStack);
        }

        plugin.getDataManager().setProfile(uuid, index, stackList);
    }
    //endregion Filter Inventory
}
