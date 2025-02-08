package com.github.bibi09ix.monsterball;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager {
    private MonsterBall plugin;
    private File dataFile;
    private YamlConfiguration config;
    private int nextId = 1;
    private Map<Integer, CapturedMobData> capturedMobs;

    public DataManager(MonsterBall plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "monsterballs.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create monsterballs.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        capturedMobs = new HashMap<>();
    }

    public void loadData() {
        nextId = config.getInt("nextId", 1);
        if (config.isConfigurationSection("capturedMobs")) {
            for (String key : config.getConfigurationSection("capturedMobs").getKeys(false)) {
                int id = Integer.parseInt(key);
                CapturedMobData data = (CapturedMobData) config.get("capturedMobs." + key);
                capturedMobs.put(id, data);
            }
        }
        plugin.getLogger().info("Loaded " + capturedMobs.size() + " captured mob entries.");
    }

    public void saveData() {
        config.set("nextId", nextId);
        for (Map.Entry<Integer, CapturedMobData> entry : capturedMobs.entrySet()) {
            config.set("capturedMobs." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save monsterballs.yml!");
        }
    }

    public int getNextId() {
        return nextId++;
    }

    public void saveCapturedMobData(int id, CapturedMobData data) {
        capturedMobs.put(id, data);
        config.set("capturedMobs." + id, data);
        saveData();
    }

    public CapturedMobData getCapturedMobData(int id) {
        return capturedMobs.get(id);
    }

    public void removeCapturedMobData(int id) {
        capturedMobs.remove(id);
        config.set("capturedMobs." + id, null);
//        plugin.getLogger().info("Captured mob data for id " + id + " removed.");
        saveData();
    }
}
