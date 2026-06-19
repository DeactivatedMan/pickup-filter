package me.deac.pickupFilter;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PickupFilter extends JavaPlugin {

    private PFdataManager dataManager;

    @Override
    public void onEnable() {
        // Make folder
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        dataManager = new PFdataManager(this);

        PluginCommand command = getCommand("filter");
        if (command != null) {
            PFcmd cmdHandler = new PFcmd(this);
            command.setExecutor(cmdHandler);
            command.setTabCompleter(cmdHandler);
        }
    }

    public PFdataManager getDataManager() {
        return dataManager;
    }
}
