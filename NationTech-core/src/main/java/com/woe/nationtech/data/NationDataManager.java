package com.woe.nationtech.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.woe.nationtech.NationTech;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NationDataManager {

    private final NationTech plugin;
    private final File dataFolder;
    private final LoadingCache<UUID, NationData> nationDataCache;

    public NationDataManager(JavaPlugin plugin) {
        this.plugin = (NationTech) plugin;
        this.dataFolder = new File(this.plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.nationDataCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .removalListener(notification -> saveNationData((NationData) notification.getValue()))
                .build(new CacheLoader<>() {
                    @Override
                    public NationData load(UUID key) {
                        return loadNationDataFromFile(key);
                    }
                });
    }

    public NationData getNationData(UUID nationUUID) {
        try {
            return nationDataCache.get(nationUUID);
        } catch (ExecutionException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load nation data for " + nationUUID, e);
            return new NationData(nationUUID);
        }
    }

    private NationData loadNationDataFromFile(UUID nationUUID) {
        File nationFile = new File(dataFolder, nationUUID.toString() + ".yml");
        NationData data = new NationData(nationUUID);
        if (nationFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(nationFile);
            List<String> unlocked = config.getStringList("unlocked-technologies");
            unlocked.forEach(data::unlockTechnology);
            data.setDirty(false);
        }
        return data;
    }

    public void saveNationData(NationData data) {
        File nationFile = new File(dataFolder, data.getNationUUID().toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("unlocked-technologies", List.copyOf(data.getUnlockedTechnologies()));
        try {
            config.save(nationFile);
            data.setDirty(false);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data for nation " + data.getNationUUID(), e);
        }
    }

    public void saveNationData(UUID nationUUID) {
        NationData data = nationDataCache.getIfPresent(nationUUID);
        if (data != null && data.isDirty()) {
            saveNationData(data);
        }
    }

    public void close() {
        nationDataCache.invalidateAll();
    }
}