package com.github.bibi09ix.monsterball.listeners;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.github.bibi09ix.monsterball.utils.DataManager;
import com.github.bibi09ix.monsterball.utils.NBTUtil;

public class MonsterBallListener implements Listener {

    private final DataManager dataManager;
    private final NamespacedKey monsterBallKey;
    private final Logger logger;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public MonsterBallListener(DataManager dataManager, NamespacedKey key, Logger logger) {
        this.dataManager = dataManager;
        this.monsterBallKey = key;
        this.logger = logger;
    }
    
    /**
     * 🔹 クールダウンチェック
     */
    private boolean isCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            long lastTime = cooldowns.get(uuid);
            if (System.currentTimeMillis() - lastTime < 3000) {
                player.sendMessage(ChatColor.RED + "クールダウン中です！");
                return true;
            }
        }
        return false;
    }
    
    /**
     * 🔹 クールダウンをセット
     */
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * 🔹 モンスターボールでエンティティを捕獲
     */
    @EventHandler
    public void onEntityCapture(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (isCooldown(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (isValidMonsterBall(item)) {
            Entity entity = event.getRightClicked();
            int entityID = getMonsterBallID(item);

            if (entityID == -1) {
                entityID = dataManager.getNextEntityID();
                setMonsterBallID(item, entityID);
            }

            String nbtData = NBTUtil.serializeEntity(entity);
            String entityName = entity.getType().toString();
            String captureTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

            dataManager.saveEntityData(entityID, entityName, nbtData);

            // 捕獲エフェクト
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation(), 10);

            // エンティティ削除（捕獲）
            entity.remove();
            player.sendMessage(ChatColor.GREEN + "エンティティを捕獲しました！");

            // デバッグログ
            logger.info("[MonsterBall] Captured Entity: ID=" + entityID + ", Type=" + entityName);

            // モンスターボールのツールチップ更新
            updateMonsterBallTooltip(item, entityID, entityName, captureTime);

            setCooldown(player);
        }
    }

    /**
     * 🔹 モンスターボールでエンティティを召喚
     */
    @EventHandler
    public void onPlayerUseMonsterBall(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isCooldown(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null || !isValidMonsterBall(item)) return;

            int entityID = getMonsterBallID(item);
            logger.info("[MonsterBall] Attempting to summon entity with ID=" + entityID);

            String entityType = dataManager.getEntityData(entityID);
            String nbtData = dataManager.getEntityNBT(entityID);

            if (entityType != null && nbtData != null) {
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10);
                Entity entity = player.getWorld().spawnEntity(player.getLocation(), org.bukkit.entity.EntityType.valueOf(entityType));
                NBTUtil.restoreEntityNBT(entity, nbtData);

                // データ削除（召喚完了）
                dataManager.deleteEntityData(entityID);
                dataManager.reloadConfig(); // 🔹 修正: データ削除後、YAMLをリロード
                player.sendMessage(ChatColor.YELLOW + "エンティティを召喚しました！");

                // デバッグログ
                logger.info("[MonsterBall] Summoned Entity: ID=" + entityID + ", Type=" + entityType);

                // モンスターボールを空にする（ツールチップとIDをクリア）
                clearMonsterBall(item);

                setCooldown(player);
            } else {
                logger.warning("[MonsterBall] No entity data found for ID=" + entityID);
            }
        }
    }

    /**
     * 🔹 モンスターボールのツールチップを更新
     */
    private void updateMonsterBallTooltip(ItemStack item, int entityID, String entityName, String captureTime) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "モンスターボール");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "ID: " + entityID,
                ChatColor.GRAY + "捕獲モンスター: " + entityName,
                ChatColor.GRAY + "捕獲時間: " + captureTime
        ));

        // IDをPersistentDataContainerに保存
        setMonsterBallID(item, entityID);

        item.setItemMeta(meta);

        // デバッグログ
        logger.info("[MonsterBall] Updated tooltip: ID=" + entityID + ", Entity=" + entityName);
    }

    /**
     * 🔹 モンスターボールを空にする（召喚後）
     */
    private void clearMonsterBall(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "モンスターボール");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "捕獲モンスター: なし",
                ChatColor.GRAY + "捕獲時間: なし"
        ));

        // PersistentDataContainer から ID を削除
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.remove(monsterBallKey);

        item.setItemMeta(meta);

        // デバッグログ
        logger.info("[MonsterBall] Cleared MonsterBall ID.");
    }

    /**
     * 🔹 モンスターボールのIDを取得
     */
    private int getMonsterBallID(ItemStack item) {
        if (item.hasItemMeta()) {
            PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
            int id = data.getOrDefault(monsterBallKey, PersistentDataType.INTEGER, -1);
            logger.info("[MonsterBall] Retrieved MonsterBall ID: " + id);
            return id;
        }
        return -1;
    }

    /**
     * 🔹 モンスターボールのIDを設定
     */
    private void setMonsterBallID(ItemStack item, int entityID) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(monsterBallKey, PersistentDataType.INTEGER, entityID);
        item.setItemMeta(meta);

        // デバッグログ
        logger.info("[MonsterBall] Set MonsterBall ID: " + entityID);
    }

    /**
     * 🔹 モンスターボールかどうかを判定
     */
    private boolean isValidMonsterBall(ItemStack item) {
        return item != null && item.getType() == Material.HEART_OF_THE_SEA &&
               item.hasItemMeta();
    }
}
