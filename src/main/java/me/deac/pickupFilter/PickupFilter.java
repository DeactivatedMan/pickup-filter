package me.deac.pickupFilter;

import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PickupFilter extends JavaPlugin {

    @Override
    public void onEnable() {
        // Make folder
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        PluginCommand command = getCommand("filter");
        if (command != null) {
            PFcmd cmdHandler = new PFcmd(this);
            command.setExecutor(cmdHandler);
            command.setTabCompleter(cmdHandler);
        }
    }

    public List<ItemStack> getProfile(String uuid, byte slot) {
        return new ArrayList<>();
    }

    public void setProfile(String uuid, byte slot, List<ItemStack> itemList) {
        //
    }
}
