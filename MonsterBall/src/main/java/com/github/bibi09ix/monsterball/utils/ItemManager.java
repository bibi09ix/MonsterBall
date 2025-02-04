package com.github.bibi09ix.monsterball.utils;

import java.util.Collections;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemManager {

    /**
     * 🔹 モンスターボールを作成（ID付き & 永続データとして保存）
     */
    public static ItemStack createMonsterBall(int entityID, NamespacedKey monsterBallKey) {
        ItemStack ball = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = ball.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "モンスターボール");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "ID: " + entityID));

        // IDを保存（データを保持する）
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(monsterBallKey, PersistentDataType.INTEGER, entityID);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        ball.setItemMeta(meta);
        return ball;
    }

    /**
     * 🔹 モンスターボールバックを作成（エンチャントエフェクト付き）
     */
    public static ItemStack createMonsterBallBack() {
        ItemStack back = new ItemStack(Material.BUNDLE);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName("§bモンスターボールバック");

        // エンチャントエフェクトを追加
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        back.setItemMeta(meta);
        return back;
    }
}
