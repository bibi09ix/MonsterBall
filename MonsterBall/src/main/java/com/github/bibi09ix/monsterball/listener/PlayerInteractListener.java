package com.github.bibi09ix.monsterball.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.bibi09ix.monsterball.CapturedMobData;
import com.github.bibi09ix.monsterball.CustomItemUtil;
import com.github.bibi09ix.monsterball.MonsterBall;
import com.github.bibi09ix.monsterball.SummonData;
import com.github.bibi09ix.monsterball.SummonDataManager;
import com.github.bibi09ix.monsterball.inventory.MonsterBallBackInventoryManager;
import com.github.bibi09ix.monsterball.util.NBTUtil;

public class PlayerInteractListener implements Listener {
    private MonsterBall plugin;

    public PlayerInteractListener(MonsterBall plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // 重複防止：直近に捕獲処理があった場合は直接召喚をスキップ（1秒間）
        if (player.hasMetadata("recentlyCaptured")) {
            long capturedTime = player.getMetadata("recentlyCaptured").get(0).asLong();
            if (System.currentTimeMillis() - capturedTime < 1000) {
                return;
            }
        }

        // シフト押下時：投擲処理（必ず投げる）
        if (player.isSneaking()) {
            if (CustomItemUtil.isMonsterBall(item)) {
                Snowball ball = player.launchProjectile(Snowball.class);
                ball.setVelocity(player.getLocation().getDirection().multiply(1.5));
                if (CustomItemUtil.isMonsterBallLoaded(item)) {
                    int mobId = CustomItemUtil.getStoredMobId(item);
                    ball.setMetadata("monsterball_action", new org.bukkit.metadata.FixedMetadataValue(plugin, "summon"));
                    SummonDataManager.putSummonData(ball.getUniqueId(), new SummonData("summon", mobId, null));
                } else {
                    ball.setMetadata("monsterball_action", new org.bukkit.metadata.FixedMetadataValue(plugin, "capture"));
                    SummonDataManager.putSummonData(ball.getUniqueId(), new SummonData("capture", null, null));
                }
                ball.setMetadata("player_uuid", new org.bukkit.metadata.FixedMetadataValue(plugin, player.getUniqueId().toString()));
                ball.setMetadata("monsterball_type", new org.bukkit.metadata.FixedMetadataValue(plugin, "ball"));
                int amount = item.getAmount();
                if (amount <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    item.setAmount(amount - 1);
                }
                event.setCancelled(true);
                return;
            }
            if (CustomItemUtil.isMonsterBallBack(item)) {
                // バッグ投擲処理
                Snowball ball = player.launchProjectile(Snowball.class);
                ball.setVelocity(player.getLocation().getDirection().multiply(1.5));
                ball.setMetadata("monsterball_action", new org.bukkit.metadata.FixedMetadataValue(plugin, "summon_back"));
                ball.setMetadata("player_uuid", new org.bukkit.metadata.FixedMetadataValue(plugin, player.getUniqueId().toString()));
                ball.setMetadata("monsterball_type", new org.bukkit.metadata.FixedMetadataValue(plugin, "back"));
                String invUUID = CustomItemUtil.getMonsterBallBackUUID(item);
                SummonDataManager.putSummonData(ball.getUniqueId(), new SummonData("summon_back", null, invUUID));
                int amount = item.getAmount();
                if (amount <= 1) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    item.setAmount(amount - 1);
                }
                event.setCancelled(true);
                return;
            }
        } else { // シフト非押下の場合：直接右クリックで召喚処理
            if (CustomItemUtil.isMonsterBall(item) && CustomItemUtil.isMonsterBallLoaded(item)) {
                Location spawnLocation;
                // プレイヤーの視線先（最大50ブロック先）のブロックの上にスポーンさせる
                Block targetBlock = player.getTargetBlockExact(50);
                if (targetBlock != null) {
                    spawnLocation = targetBlock.getLocation().add(0.5, 1, 0.5);
                } else {
                    spawnLocation = player.getLocation().clone().add(0, 1, 0);
                }
                int mobId = CustomItemUtil.getStoredMobId(item);
                CapturedMobData mobData = plugin.getDataManager().getCapturedMobData(mobId);
                if (mobData == null) {
                    player.sendMessage(ChatColor.RED + "捕獲データが見つかりません。");
                    return;
                }
                LivingEntity spawned = NBTUtil.spawnEntityFromNBT(mobData.getNbtData(), spawnLocation);
                if (spawned != null) {
                    player.sendMessage(ChatColor.GREEN + "モブを召喚しました！");
                } else {
                    player.sendMessage(ChatColor.RED + "モブの召喚に失敗しました。");
                }
                plugin.getDataManager().removeCapturedMobData(mobId);
                // 召喚後、空のモンスターボールを返却
                player.getInventory().removeItem(item);
                player.getInventory().addItem(CustomItemUtil.getMonsterBallItem(false, 0));
                event.setCancelled(true);
                return;
            }
            if (CustomItemUtil.isMonsterBallBack(item)) {
                String uuid = CustomItemUtil.getMonsterBallBackUUID(item);
                MonsterBallBackInventoryManager invManager = plugin.getBackInventoryManager();
                invManager.openInventory(player, uuid);
                event.setCancelled(true);
                return;
            }
        }
    }
}
