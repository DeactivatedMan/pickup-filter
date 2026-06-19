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

    public List<ItemStack> getProfile(UUID uuid, byte index) {
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
    public boolean profileContains(UUID uuid, ItemStack itemFilter) {
        Material materialFilter = itemFilter.getType();
        /*ItemMeta metaFilter = itemFilter.getItemMeta();
        if (metaFilter == null) return false;

        Component displayName = metaFilter.hasDisplayName() ? metaFilter.displayName() : null;
        boolean hasName = displayName!=null;

        List<Component> lore = metaFilter.lore();
        boolean hasLore = lore!=null && !lore.isEmpty();

        Map<Enchantment, Integer> enchants = metaFilter.getEnchants();
        boolean hasEnchants = !enchants.isEmpty();*/

        YamlConfiguration config = getPlayerConfig(uuid);
        List<?> rawList = config.getList("profile"+getIndex(uuid), null);
        if (rawList == null || rawList.isEmpty()) return false;
        //plugin.getLogger().info("rawList is this long: " + rawList.size());
        for (Object object : rawList) {
            if (object instanceof ItemStack itemStack && itemStack.getType() == materialFilter) return true;

            /*if (
                    object instanceof ItemStack itemStack &&
                            itemStack.getType() != materialFilter // First basic filter
            ) {
                // Do filter stuff
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    if ( hasName && meta.hasDisplayName() &&
                            Objects.equals(displayName, meta.displayName())
                    ) return true; // Display name is the same

                    else if ( hasLore && meta.hasLore() &&
                            Objects.equals(lore, meta.lore())
                    ) return true; // Lore is the same

                    else if ( hasEnchants && meta.hasEnchants() ) {
                        int shared = 0;

                        for (Enchantment enchant : enchants.keySet()) {
                            if (meta.hasEnchant(enchant)) shared++;
                            if (shared >= 2) return true; // Has two or more of same enchantment
                        }
                    }
                }
            }*/
        }

        return false;
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
