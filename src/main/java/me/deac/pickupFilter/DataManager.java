package me.deac.pickupFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DataManager {
    private final PickupFilter plugin;

    private final Cache<UUID, YamlConfiguration> dataCache; // Used to minimise reads of files
    private final Map<UUID, Byte> indexMap = new HashMap<>();

    public DataManager(PickupFilter plugin) {
        this.plugin = plugin;
        dataCache = Caffeine.newBuilder().expireAfterAccess(2L, TimeUnit.MINUTES).build();
    }
    //region Data
    private File getPlayerFile(UUID uuid) {return new File(plugin.getDataFolder(), "playerdata/"+uuid+".yml");}

    private void savePlayerData(UUID uuid, YamlConfiguration data) {
        try {
            data.save( getPlayerFile(uuid) );
            dataCache.put(uuid, data);
        }
        catch (IOException e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }
    private YamlConfiguration getPlayerData(UUID uuid) {
        YamlConfiguration data = dataCache.getIfPresent(uuid);
        if (data != null) return data;

        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try { file.createNewFile(); }
            catch (IOException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        dataCache.put(uuid, data);
        return data;
    }
    public void ensureDefaults(UUID uuid) {
        YamlConfiguration data = getPlayerData(uuid);
        boolean changed = false;

        for (int i = 1; i <= 9; i++) {
            String path = "profile" + i;

            if (!data.contains(path) || !data.isList(path)) {
                // Set an empty list of ItemStacks
                data.set(path, new ArrayList<ItemStack>());
                changed = true;
            }
        }

        if (changed) savePlayerData(uuid, data);
    }
    //endregion Data

    //region Profile
    public List<ItemStack> getProfile(UUID uuid, byte index) {
        YamlConfiguration data = getPlayerData(uuid);
        List<?> rawList = data.getList("profile"+index, new ArrayList<>());

        List<ItemStack> stackList = new ArrayList<>();
        for (Object object : rawList) {
            if (object instanceof ItemStack itemStack) stackList.add(itemStack);
        }

        return stackList;
    }
    public void setProfile(UUID uuid, byte index, List<ItemStack> stackList) {
        YamlConfiguration data = getPlayerData(uuid);
        data.set("profile"+index, stackList);
        savePlayerData(uuid, data);
    }
    public boolean profileContains(UUID uuid, ItemStack itemFilter) {
        if (!indexMap.containsKey(uuid) || indexMap.get(uuid) == 0) return true; // If player is in "off" profile

        Material materialFilter = itemFilter.getType();

        YamlConfiguration data = getPlayerData(uuid);
        List<?> rawList = data.getList("profile"+getIndex(uuid), null);
        if (rawList == null || rawList.isEmpty()) return false;

        for (Object object : rawList) {
            if (object instanceof ItemStack itemStack && itemStack.getType() == materialFilter) return true;
        }

        return false;
    }
    //endregion Profile

    //region Index Map
    public void setIndex(UUID uuid, byte index) {
        if (index == 0) indexMap.remove(uuid);
        else indexMap.put(uuid, index);
    }
    public byte getIndex(UUID uuid) {
        return indexMap.getOrDefault(uuid, (byte) 0);
    }
    public void removeIndex(UUID uuid) {
        indexMap.remove(uuid);
    }
    //endregion Index Map
}
