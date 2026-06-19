package me.deac.pickupFilter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PFdataManager {
    private final PickupFilter plugin;

    public PFdataManager(PickupFilter plugin) {
        this.plugin = plugin;
    }
    private File getPlayerFile(String uuid) {return new File(plugin.getDataFolder(), "playerdata/"+uuid+".yml");}

    private void savePlayerConfig(String uuid, YamlConfiguration config) {
        try { config.save( getPlayerFile(uuid) ); }
        catch (IOException e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }

    private YamlConfiguration getPlayerConfig(String uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try { file.createNewFile(); }
            catch (IOException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public void ensureDefaults(String uuid) {
        YamlConfiguration config = getPlayerConfig(uuid);
        boolean changed = false;

        for (int i = 1; i <= 9; i++) {
            String path = "profile" + i;

            if (!config.contains(path) || !config.isList(path)) {
                // Set an empty list of ItemStacks
                config.set(path, new ArrayList<ItemStack>());
                changed = true;
            }
        }

        if (changed) savePlayerConfig(uuid, config);
    }

    public List<ItemStack> getProfle(String uuid, byte index) {
        YamlConfiguration config = getPlayerConfig(uuid);
        List<?> rawList = config.getList("profile"+index, new ArrayList<>());

        List<ItemStack> stackList = new ArrayList<>();
        for (Object object : rawList) {
            if (object instanceof ItemStack itemStack) stackList.add(itemStack);
        }

        return stackList;
    }

    public void setProfile(String uuid, byte index, List<ItemStack> stackList) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set("profile"+index, stackList);
        savePlayerConfig(uuid, config);
    }
}
