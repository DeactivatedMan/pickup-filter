package me.deac.pickupFilter;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PickupFilter extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        // Make folder
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        dataManager = new DataManager(this);

        PluginCommand command = getCommand("filter");
        if (command != null) {
            CommandHandler cmdHandler = new CommandHandler(this);
            command.setExecutor(cmdHandler);
            command.setTabCompleter(cmdHandler);
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
