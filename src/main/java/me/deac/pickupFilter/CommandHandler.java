package me.deac.pickupFilter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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

            if (args.length < 1) player.sendMessage(tellCommandsText);
            else {
                String preConvert = args[args.length-1].replace("off", "0");
                byte value = preConvert.matches("^[0-9]$") ? Byte.parseByte(preConvert) : 10;
                if (value == 10) {
                    player.sendMessage(Component.text("PickupFilter > Profile index not found!").color(NamedTextColor.RED));
                    return true;
                }

                if (args.length == 1) handleSwitch(player, value);
                if (args.length == 2) {
                    switch (args[0].toLowerCase()) {
                        case "edit" -> handleEdit(player, value);
                        case "switch" -> handleSwitch(player, value);
                    }
                }
            }

            if (args.length != 2) {
                // Explanation + commands
                player.sendMessage(tellCommandsText);
            } else {
                String preConvert = args[1].replace("off", "0");
                byte value = preConvert.matches("^[0-9]$") ? Byte.parseByte(preConvert) : 10;
                if (value == 10) {
                    player.sendMessage(Component.text("PickupFilter > Profile index not found!").color(NamedTextColor.RED));
                }

                switch (args[0].toLowerCase()) {
                    case "edit" -> {
                        if (value == 0) player.sendMessage(Component.text("PickupFilter > Cannot edit Off profile").color(NamedTextColor.RED));
                        else handleEdit(player, value);
                    }
                    case "switch" -> handleSwitch(player, value);
                }
            }
        } else sender.sendMessage("Filter commands only executable by players!");
        return true;
    }

    private void handleEdit(Player player, byte index) {
        if (index == 0) player.sendMessage(Component.text("PickupFilter > Cannot edit Off profile").color(NamedTextColor.RED));
        // Open chest GUI for this profile
        FilterMenuHolder holder = new FilterMenuHolder();

        List<ItemStack> stackList = plugin.getDataManager().getProfile(player.getUniqueId(), index);
        Inventory inventory = Bukkit.createInventory(
                holder, 27,
                Component.text("Profile "+index)
                        .decoration(TextDecoration.BOLD, true)
        );
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
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> profiles = new ArrayList<>(List.of("off", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

        if (args.length == 1) {
            profiles.addAll(List.of("edit", "switch"));
            return StringUtil.copyPartialMatches(args[0], profiles, completions);
        } else return profiles;
    }
}
