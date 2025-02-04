package com.github.bibi09ix.monsterball.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class DataManager {
    
    private final Plugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "captured_entities.yml");

        if (!dataFile.exists()) {
            try {
                plugin.getLogger().info("Creating new captured_entities.yml...");
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create captured_entities.yml!");
                e.printStackTrace();
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public int getNextEntityID() {
        int nextID = dataConfig.getInt("next_id", 1);
        dataConfig.set("next_id", nextID + 1);
        save();
        return nextID;
    }

    public void saveEntityData(int entityID, String entityType, String nbtData) {
        dataConfig.set("entities." + entityID + ".type", entityType);
        dataConfig.set("entities." + entityID + ".nbt", nbtData);
        saveConfig();
    }

    public String getEntityData(int entityID) {
        reloadConfig();
        return dataConfig.getString("entities." + entityID + ".type");
    }

    public String getEntityNBT(int entityID) {
        reloadConfig();
        return dataConfig.getString("entities." + entityID + ".nbt");
    }

    public void deleteEntityData(int entityID) {
        dataConfig.set("entities." + entityID, null);
        saveConfig();
    }

    public void reloadConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save entity data!");
            e.printStackTrace();
        }
    }


    private void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save entity data!");
            e.printStackTrace();
        }
    }
}
