package me.deac.pickupFilter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class PickupFilter extends JavaPlugin {

    private DataManager dataManager;

    public ItemStack redPane;
    public ItemStack greenPane;

    @Override
    public void onEnable() {
        // Make folder
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        dataManager = new DataManager(this);

        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = red.getItemMeta();
        redMeta.displayName(Component.text("Not Selected").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        redMeta.lore(List.of(Component.text("Click to select")));
        red.setItemMeta(redMeta);
        redPane = red;

        ItemStack green = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta greenMeta = green.getItemMeta();
        greenMeta.displayName(Component.text("Selected").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        redMeta.lore(List.of(Component.text("Click to deselect")));
        green.setItemMeta(greenMeta);
        greenPane = green;

        PluginCommand command = getCommand("filter");
        if (command != null) {
            CommandHandler cmdHandler = new CommandHandler(this);
            command.setExecutor(cmdHandler);
            command.setTabCompleter(cmdHandler);
        }

        MainListener listener = new MainListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
