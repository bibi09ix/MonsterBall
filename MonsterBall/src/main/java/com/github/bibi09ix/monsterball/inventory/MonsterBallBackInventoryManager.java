package com.github.bibi09ix.monsterball.inventory;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.bibi09ix.monsterball.MonsterBall;

public class MonsterBallBackInventoryManager {
    private static final HashMap<String, Inventory> inventories = new HashMap<>();

    public MonsterBallBackInventoryManager(MonsterBall plugin) {
        // plugin フィールドは不要
    }

    public void openInventory(Player player, String invUUID) {
        Inventory inv = getInventory(invUUID);
        if (inv == null) {
            inv = Bukkit.createInventory(null, 54, "モンスターボールバッグ");
            inventories.put(invUUID, inv);
        }
        player.openInventory(inv);
    }

    public void clearInventory(String invUUID) {
        Inventory inv = getInventory(invUUID);
        if (inv != null) {
            inv.clear();
        }
    }

    public static Inventory getInventory(String invUUID) {
        return inventories.get(invUUID);
    }

    public static String createNewInventory() {
        String uuid = UUID.randomUUID().toString();
        Inventory inv = Bukkit.createInventory(null, 54, "モンスターボールバッグ");
        inventories.put(uuid, inv);
        return uuid;
    }
}
