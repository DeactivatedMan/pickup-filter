package me.deac.pickupFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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
            String path = "profile" + i + ".items";
            if (!data.contains(path) || !data.isList(path)) {
                // Set an empty list of Materials
                data.set(path, new ArrayList<Material>());
                changed = true;
            }

            path = "profile" + i + ".bools";
            if (!data.contains(path) || !data.isList(path)) {
                // Set an empty list of Booleans
                data.set(path, new ArrayList<Boolean>());
                changed = true;
            }
        }

        if (changed) savePlayerData(uuid, data);
    }
    //endregion Data

    //region Profile
    public List<Boolean> getProfileBools(UUID uuid, byte index) {
        YamlConfiguration data = getPlayerData(uuid);

        List<?> rawList = data.getList("profile"+index+".bool", new ArrayList<>());

        if (rawList.isEmpty()) return Collections.nCopies(9, false);

        List<Boolean> booleans = new ArrayList<>();
        for (Object object : rawList) {
            if (object instanceof Boolean bool) booleans.add(bool);
        }

        return booleans;
    }
    public List<Material> getProfileMaterials(UUID uuid, byte index) {
        YamlConfiguration data = getPlayerData(uuid);

        List<?> rawList = data.getList("profile"+index+".item", new ArrayList<>());

        List<Material> materials = new ArrayList<>();
        for (Object object : rawList) {
            if (object instanceof Material material) materials.add(material);
        }

        return materials;
    }

    public void setProfileBools(UUID uuid, byte index, List<Boolean> booleans) {
        YamlConfiguration data = getPlayerData(uuid);
        data.set("profile"+index+".bool", booleans);
        savePlayerData(uuid, data);
    }
    public void setProfileMaterials(UUID uuid, byte index, List<Material> materials) {
        YamlConfiguration data = getPlayerData(uuid);
        data.set("profile"+index+".item", materials);
        savePlayerData(uuid, data);
    }

    public boolean profileContains(UUID uuid, ItemStack itemFilter) {
        if (!indexMap.containsKey(uuid) || indexMap.get(uuid) == 0) return true; // If player is in "off" profile

        // Material Filter
        Material materialFilter = itemFilter.getType();

        YamlConfiguration data = getPlayerData(uuid);
        List<?> itemListRaw = data.getList("profile"+getIndex(uuid)+".item", null);
        if (itemListRaw != null && !itemListRaw.isEmpty()) {
            for (Object object : itemListRaw) {
                if (object instanceof Material material && material == materialFilter) return true;
            }
        }

        // Category Filter
        List<?> catListRaw = data.getList("profile"+getIndex(uuid)+".bool", null);
        if (catListRaw != null && !catListRaw.isEmpty()) {
            String name = materialFilter.name().toLowerCase();

            for (int i=0; i<Math.min(9, catListRaw.size()); i++) {
                Object object = catListRaw.get(i);

                if (object instanceof Boolean bool && bool) {
                    switch (i) {
                        case 0 -> { if (name.contains("helmet")) return true; }
                        case 1 -> { if (name.contains("chestplate")) return true; }
                        case 2 -> { if (name.contains("leggings")) return true; }
                        case 3 -> { if (name.contains("boots")) return true; }

                        case 4 -> { if (name.contains("sword") || name.contains("axe") || materialFilter == Material.MACE) return true; } // Melee
                        case 5 -> { if (materialFilter == Material.BOW || materialFilter == Material.CROSSBOW ) return true; } // Ranged

                        case 6 -> { if ( // Misc PVP
                                materialFilter == Material.ENDER_PEARL ||
                                materialFilter == Material.POTION ||
                                materialFilter == Material.SPLASH_POTION ||
                                materialFilter == Material.GOLDEN_APPLE ||
                                materialFilter == Material.ENCHANTED_GOLDEN_APPLE
                            ) return true; }
                        case 7 -> { if ( materialFilter.isEdible() || PickupFilter.mobDrops.contains(materialFilter) || name.contains("wool") ) return true; }
                        case 8 -> { // Check if custom money or xp
                            if (materialFilter == Material.EXPERIENCE_BOTTLE) return true;
                            @Nullable List<Component> components = itemFilter.getItemMeta().lore();
                            if (components != null && !components.isEmpty()) {
                                for (Component component : components) {
                                    if (PlainTextComponentSerializer.plainText().serialize(component).replaceAll("\\s", "").startsWith("Signer")) return true;
                                }
                            }

                        }
                    }
                }
            }
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
