package com.github.bibi09ix.monsterball;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class CooldownManager {
    private HashMap<UUID, Long> globalCooldowns = new HashMap<>();
    private final long cooldownMillis = 1000; // 1秒

    public boolean isOnGlobalCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (globalCooldowns.containsKey(uuid)) {
            long last = globalCooldowns.get(uuid);
            return System.currentTimeMillis() - last < cooldownMillis;
        }
        return false;
    }

    public void setGlobalCooldown(Player player) {
        globalCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
