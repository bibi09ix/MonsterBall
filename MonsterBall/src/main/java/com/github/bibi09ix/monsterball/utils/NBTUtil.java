package com.github.bibi09ix.monsterball.utils;

import java.util.Base64;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 🔹 NBTデータの保存・復元ユーティリティ
 */
public class NBTUtil {

    /**
     * 🔹 エンティティのNBTデータをBase64形式で保存
     */
    public static String serializeEntity(Entity entity) {
        try {
            PersistentDataContainer data = entity.getPersistentDataContainer();
            byte[] bytes = data.getOrDefault(new NamespacedKey(JavaPlugin.getProvidingPlugin(NBTUtil.class), "nbt_data"),
                    PersistentDataType.BYTE_ARRAY, new byte[0]);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 🔹 NBTデータをもとにエンティティを復元
     */
    public static void restoreEntityNBT(Entity entity, String nbtData) {
        try {
            PersistentDataContainer data = entity.getPersistentDataContainer();
            byte[] bytes = Base64.getDecoder().decode(nbtData);
            data.set(new NamespacedKey(JavaPlugin.getProvidingPlugin(NBTUtil.class), "nbt_data"),
                    PersistentDataType.BYTE_ARRAY, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
