package com.github.bibi09ix.monsterball;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemUtil {
    private static final String MONSTER_BALL_KEY = "monsterball";
    private static final String MONSTER_BALL_BACK_KEY = "monsterballback";

    public static ItemStack getMonsterBallItem(boolean loaded, int id, String typeName, String displayName) {
    	ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "モンスターボール");

        if (loaded) {
            String disp = (displayName != null && !displayName.isEmpty()) ? displayName : "未命名";
            String typeStr = (typeName != null && !typeName.isEmpty()) ? typeName : "???";

            meta.setLore(Arrays.asList(
                "捕獲Mob： " + disp,
                "MobType： " + typeStr,
                "ID: " + id
            ));
        } else {
            meta.setLore(Arrays.asList("空のモンスターボール"));
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(MonsterBall.getInstance(), "type"), PersistentDataType.STRING, MONSTER_BALL_KEY);
        container.set(new NamespacedKey(MonsterBall.getInstance(), "loaded"), PersistentDataType.INTEGER, loaded ? 1 : 0);
        container.set(new NamespacedKey(MonsterBall.getInstance(), "mobid"), PersistentDataType.INTEGER, id);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getMonsterBallItem(boolean loaded, int id) {
        return getMonsterBallItem(loaded, id, "", "");
    }

    public static ItemStack getMonsterBallBackItem(String uuid) {
        // カスタムデータ付き鞍を生成
        ItemStack item = new ItemStack(Material.SADDLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "モンスターボールバッグ");
        meta.setLore(Arrays.asList("モンスターボールを保管する"));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(MonsterBall.getInstance(), "type"), PersistentDataType.STRING, MONSTER_BALL_BACK_KEY);
        container.set(new NamespacedKey(MonsterBall.getInstance(), "uuid"), PersistentDataType.STRING, uuid);
        item.setItemMeta(meta);
        return item;
    }


    public static boolean isMonsterBall(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(new NamespacedKey(MonsterBall.getInstance(), "type"), PersistentDataType.STRING);
        return type != null && type.equals(MONSTER_BALL_KEY);
    }

    public static boolean isMonsterBallLoaded(ItemStack item) {
        if (!isMonsterBall(item)) return false;
        ItemMeta meta = item.getItemMeta();
        Integer loaded = meta.getPersistentDataContainer().get(new NamespacedKey(MonsterBall.getInstance(), "loaded"), PersistentDataType.INTEGER);
        return loaded != null && loaded == 1;
    }

    public static int getStoredMobId(ItemStack item) {
        if (!isMonsterBall(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        Integer id = meta.getPersistentDataContainer().get(new NamespacedKey(MonsterBall.getInstance(), "mobid"), PersistentDataType.INTEGER);
        return id != null ? id : 0;
    }

    public static boolean isMonsterBallBack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(new NamespacedKey(MonsterBall.getInstance(), "type"), PersistentDataType.STRING);
        return type != null && type.equals(MONSTER_BALL_BACK_KEY);
    }

    public static String getMonsterBallBackUUID(ItemStack item) {
        if (!isMonsterBallBack(item)) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(new NamespacedKey(MonsterBall.getInstance(), "uuid"), PersistentDataType.STRING);
    }
}
