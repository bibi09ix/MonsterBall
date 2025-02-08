package com.github.bibi09ix.monsterball;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("CapturedMobData")
public class CapturedMobData implements ConfigurationSerializable {
    private String nbtData;

    public CapturedMobData(String nbtData) {
        this.nbtData = nbtData;
    }

    public String getNbtData() {
        return nbtData;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("nbtData", nbtData);
        return map;
    }

    public CapturedMobData(Map<String, Object> map) {
        this.nbtData = (String) map.get("nbtData");
    }
}
