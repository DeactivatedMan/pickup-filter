package me.deac.pickupFilter;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final PickupFilter plugin;

    private final Component tellCommandsText;

    public CommandHandler(PickupFilter plugin) {
        this.plugin = plugin;

        tellCommandsText = Component.text("Commands:\n")
            .color(NamedTextColor.DARK_AQUA)
            .decoration(TextDecoration.BOLD, true)

            .append(
                Component.text(" - ").color(NamedTextColor.GRAY)
                    .append(Component.text("edit 1-9")
                        .color(NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.UNDERLINED, true)

                        .clickEvent(ClickEvent.suggestCommand("/filter edit "))
                        .hoverEvent(HoverEvent.showText(Component.text("Opens the filter editor")))
                    )
            )

            .append(
                    Component.text("\n - ").color(NamedTextColor.GRAY)
                            .append(Component.text("(switch) off-9")
                                    .color(NamedTextColor.DARK_PURPLE)
                                    .decoration(TextDecoration.UNDERLINED, true)

                                    .clickEvent(ClickEvent.suggestCommand("/filter "))
                                    .hoverEvent(HoverEvent.showText(Component.text("Switches to selected filter profile (or disables)")))
                            )
            )
            .append(
                    Component.text("\n\nPickupFilter is a plugin that filters picked up items so that you only pick up what you want and when you want it thanks to having multiple profiles.")
                            .color(NamedTextColor.GRAY)
            );
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {
        if ( sender instanceof Player player ) {
            plugin.getDataManager().ensureDefaults(player.getUniqueId());

            if (args.length < 1) handleEdit(player, (byte) 0);
            else {
                if (args[0].equalsIgnoreCase("help")) {
                    player.sendMessage(tellCommandsText);
                    return true;
                }

                String preConvert = args[args.length-1].replace("off", "0");
                byte value = preConvert.matches("^[0-9]$") ? Byte.parseByte(preConvert) : 10;
                if (value == 10) player.sendMessage(Component.text("PickupFilter > Profile index not found!").color(NamedTextColor.RED));

                else if (args.length == 1) handleSwitch(player, value);

                else if (args.length == 2) { switch (args[0].toLowerCase()) {

                        case "edit" -> {
                            if (value == 0) player.sendMessage(Component.text("PickupFilter > Cannot edit Off profile").color(NamedTextColor.RED));
                            else handleEdit(player, value);
                        }
                        case "switch" -> handleSwitch(player, value);
                    }
                }
            }

        } else sender.sendMessage("Filter commands only executable by players!");
        return true;
    }
    private void handleEdit(Player player, byte index) {
        // Opens chest GUI
        UUID uuid = player.getUniqueId();

        FilterMenuHolder holder = new FilterMenuHolder();
        holder.index = index;

        Inventory inventory = Bukkit.createInventory(
                holder, index == 0 ? 18 : 36,
                Component.text("Profile "+( index==0 ? "Selector" : index ) )
                        .decoration(TextDecoration.BOLD, true)
        );
        List<ItemStack> stackList = new ArrayList<>();
        if (index != 0) {
            List<Boolean> boolList = plugin.getDataManager().getProfileBools(uuid, index);// if (boolList.size() < 9) boolList.addAll( Collections.nCopies(9, false) );
            for (int i=0; i<9; i++) {
                String category = PickupFilter.categoryFilters[i];
                //plugin.getLogger().info(category + " | " + boolList.get(i));
                String[] parts = category.split("-", 2); if (parts.length < 2) parts = List.of(category, "ERR").toArray(new String[2]);

                ItemStack itemStack = new ItemStack(Material.FLINT);

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(Component.text(parts[1] + " - " + (boolList.get(i) ? "ON" : "OFF") ).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(List.of(Component.text("If enabled, picks up " + parts[1] + (i==6 ? " items" : "") ).decoration(TextDecoration.ITALIC, false) ));
                itemStack.setItemMeta(itemMeta);

                itemStack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(parts[0]));

                stackList.add(itemStack);
            }

            //List<ItemStack> displayableList = new ArrayList<>();
            for (Material material : plugin.getDataManager().getProfileMaterials(uuid, index)) {
                stackList.add( plugin.displayCopyOf(material) );
            }
            //stackList.addAll(plugin.getDataManager().getProfileItems(uuid, index));
        }
        else {
            // Do custom main GUI
            byte selected = plugin.getDataManager().getIndex(uuid);
            //plugin.getLogger().info("Selected: " + selected);
            for (byte i=1; i < 10; i++) {
                stackList.add(i==selected ? plugin.greenPane : plugin.redPane);
                //plugin.getLogger().info("Is selected: " + (i==selected) );
            }

            for (byte i=1; i < 10; i++) {
                ItemStack displayItem = new ItemStack(Material.HOPPER);
                ItemMeta displayMeta = displayItem.getItemMeta();
                displayMeta.displayName(Component.text("Profile "+i).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
                displayMeta.lore(List.of(Component.text("Click to edit")));
                displayItem.setItemMeta(displayMeta);
                stackList.add(displayItem);
            }
        }
        inventory.setContents( stackList.toArray(new ItemStack[0]) );

        holder.setInventory(inventory);
        holder.index = index;

        player.openInventory(inventory);
    }
    private void handleSwitch(Player player, byte index) {
        // Switch to the requested profile
        plugin.getDataManager().setIndex(player.getUniqueId(), index);
        player.sendMessage(Component.text(
                index == 0 ? "PickupFilter > Switched to off state"
                : "PickupFIlter > Switched to profile "+index
        ).color(NamedTextColor.DARK_GREEN));
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        List<String> profiles = new ArrayList<>(List.of("off", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

        if (args.length == 1) {
            profiles.addAll(List.of("help", "edit", "switch"));
            return StringUtil.copyPartialMatches(args[0], profiles, completions);
        } else return profiles;
    }
}
