package me.deac.pickupFilter;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class PickupFilter extends JavaPlugin {

    private DataManager dataManager;

    public ItemStack redPane;
    public ItemStack greenPane;

    public static final Set<Material> mobDrops = EnumSet.of(
            Material.ROTTEN_FLESH,
            Material.BONE,
            Material.ARROW,
            Material.GUNPOWDER,
            Material.STRING,
            Material.SPIDER_EYE,
            Material.BLAZE_ROD,
            Material.ENDER_PEARL,
            Material.SLIME_BALL,
            Material.GHAST_TEAR,
            Material.FEATHER,
            Material.LEATHER
    );

    public static final String[] categoryFilters = List.of(
            "diamond_helmet-Helmets",
            "diamond_chestplate-Chestplates",
            "diamond_leggings-Leggings",
            "diamond_boots-Boots",

            "diamond_sword-Melee Weapons",
            "bow-Ranged Weapons",

            "golden_apple-Misc PVP",

            "rotten_flesh-Mob Drops",

            "experience_bottle-Vouchers + XP"
    ).toArray(new String[0]);

    //public List<Material> booleanIcons;

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

        //booleanIcons.addAll( Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,  );

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
    /*
    public ItemStack displayCopyOf(ItemStack itemStack) {return displayCopyOf(itemStack, "");}
    public ItemStack displayCopyOf(ItemStack itemStack, String displayName) {
        ItemStack copyStack = new ItemStack(Material.FLINT);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(
                displayName.isEmpty()
                        ? itemStack.displayName()
                        : Component.text(displayName)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, true)
        );
        copyStack.setItemMeta(itemMeta);

        copyStack.setItemMeta( itemMeta );

        copyStack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(itemStack.getType().name().toLowerCase()));

        return displayCopyOf(itemStack.getType(), displayName);
    }
    */

    public ItemStack displayCopyOf(Material material) {return  displayCopyOf(material, "");}
    public ItemStack displayCopyOf(Material material, String displayName) {
        ItemStack copyStack = new ItemStack(Material.FLINT);

        ItemMeta itemMeta = copyStack.getItemMeta();
        String materialName = material.name();
        itemMeta.displayName(
                Component.text(displayName.isEmpty() ? toTitleCase( materialName ) : displayName)
                        .decoration(TextDecoration.ITALIC, false)
        );
        copyStack.setItemMeta(itemMeta);

        copyStack.setItemMeta( itemMeta );

        copyStack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(materialName.toLowerCase()));

        return copyStack;
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) continue;

            String formattedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            result.append(formattedWord);

            if (i < words.length - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }
}
