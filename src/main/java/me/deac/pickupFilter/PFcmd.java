package me.deac.pickupFilter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PFcmd implements CommandExecutor, TabCompleter {
    private final PickupFilter plugin;
    private final Component tellCommandsText;

    public PFcmd(PickupFilter plugin) {
        this.plugin = plugin;

        tellCommandsText = Component.text("Commands:\n")
            .color(NamedTextColor.DARK_AQUA)
            .decoration(TextDecoration.BOLD, true)

            .append(
                Component.text(" - ").color(NamedTextColor.GRAY)
                    .append(Component.text("open")
                        .color(NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.UNDERLINED, true)

                        .clickEvent(ClickEvent.suggestCommand("/filter open 1-9"))
                        .hoverEvent(HoverEvent.showText(Component.text("Run command")))
                    )
            )

            .append(
                    Component.text("\n - ").color(NamedTextColor.GRAY)
                            .append(Component.text("switch 1-9")
                                    .color(NamedTextColor.DARK_PURPLE)
                                    .decoration(TextDecoration.UNDERLINED, true)

                                    .clickEvent(ClickEvent.suggestCommand("/filter switch "))
                                    .hoverEvent(HoverEvent.showText(Component.text("Run command")))
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
            if (args.length != 2) {
                // Explanation + commands
                player.sendMessage(tellCommandsText);
            } else {
                byte value = ConvertToByte(args[1]);
                if (value == 0) {
                    player.sendMessage(Component.text("PickupFilter > Profile index not found!").color(NamedTextColor.RED));
                }

                switch (args[0].toLowerCase()) {
                    case "open" -> HandleOpen(player);
                    case "switch" -> HandleSwitch(player);
                }
            }
        } else sender.sendMessage("Filter commands only executable by players!");
        return true;
    }

    private void HandleOpen(Player player) {
        // Open chest GUI for this profile
    }

    private void HandleSwitch(Player player) {
        // Switch to this profile
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("open", "switch"), completions);
        } else return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    private byte ConvertToByte(String input) {
        if (input != null && input.matches("^[1-9]$")) {
            return Byte.parseByte(input);
        } else return 0;
    }
}
