package me.deac.pickupFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DataManager {
    private final PickupFilter plugin;

    private Cache<UUID, YamlConfiguration> dataCache; // Used to minimise reads of files
    private Map<UUID, Byte> indexMap = new HashMap<>();

    public DataManager(PickupFilter plugin) {
        this.plugin = plugin;
        dataCache = Caffeine.newBuilder().expireAfterAccess(2L, TimeUnit.MINUTES).build();
    }
    private File getPlayerFile(UUID uuid) {return new File(plugin.getDataFolder(), "playerdata/"+uuid+".yml");}

    private void savePlayerConfig(UUID uuid, YamlConfiguration config) {
        try {
            config.save( getPlayerFile(uuid) );
            dataCache.put(uuid, config);
        }
        catch (IOException e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }
    private YamlConfiguration getPlayerConfig(UUID uuid) {
        YamlConfiguration config = dataCache.getIfPresent(uuid);
        if (config != null) return config;

        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try { file.createNewFile(); }
            catch (IOException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        dataCache.put(uuid, config);
        return config;
    }
    public void ensureDefaults(UUID uuid) {
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

    public List<ItemStack> getProfle(UUID uuid, byte index) {
        YamlConfiguration config = getPlayerConfig(uuid);
        List<?> rawList = config.getList("profile"+index, new ArrayList<>());

        List<ItemStack> stackList = new ArrayList<>();
        for (Object object : rawList) {
            if (object instanceof ItemStack itemStack) stackList.add(itemStack);
        }

        return stackList;
    }
    public void setProfile(UUID uuid, byte index, List<ItemStack> stackList) {
        YamlConfiguration config = getPlayerConfig(uuid);
        config.set("profile"+index, stackList);
        savePlayerConfig(uuid, config);
    }

    //region Index Map
    public void setIndex(UUID uuid, byte index) {
        indexMap.put(uuid, index);
    }
    public byte getIndex(UUID uuid) {
        return indexMap.getOrDefault(uuid, (byte) 1);
    }
    public void removeIndex(UUID uuid) {
        indexMap.remove(uuid);
    }
    //endregion Index Map
}
