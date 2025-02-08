package com.github.bibi09ix.monsterball.listener;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.github.bibi09ix.monsterball.CapturedMobData;
import com.github.bibi09ix.monsterball.CustomItemUtil;
import com.github.bibi09ix.monsterball.MonsterBall;
import com.github.bibi09ix.monsterball.util.NBTUtil;

public class PlayerInteractEntityListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !CustomItemUtil.isMonsterBall(item) || CustomItemUtil.isMonsterBallLoaded(item))
            return;
        if (!(event.getRightClicked() instanceof LivingEntity))
            return;
        LivingEntity target = (LivingEntity) event.getRightClicked();
        if (target.getType().toString().contains("ENDER_DRAGON") || target.getType().toString().contains("WITHER")) {
            player.sendMessage(ChatColor.RED + "ボスモブは捕獲できません。");
            return;
        }
        if (MonsterBall.getInstance().getCooldownManager().isOnGlobalCooldown(player)) {
            player.sendMessage(ChatColor.RED + "クールダウン中です。しばらくお待ちください。");
            return;
        }
        MonsterBall.getInstance().getCooldownManager().setGlobalCooldown(player);

        String nbt = NBTUtil.getNBTData(target);
        CapturedMobData mobData = new CapturedMobData(nbt);
        target.remove();
        int mobId = MonsterBall.getInstance().getDataManager().getNextId();
        MonsterBall.getInstance().getDataManager().saveCapturedMobData(mobId, mobData);
        String mobName = NBTUtil.getMobDisplayName(nbt);
        String mobType = target.getType().toString();
        ItemStack loadedBall = CustomItemUtil.getMonsterBallItem(true, mobId, mobType, mobName);
        player.getInventory().setItemInMainHand(loadedBall);
        player.sendMessage(ChatColor.GREEN + "モブを捕獲しました！");
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        // 捕獲直後、重複召喚防止用にメタデータを設定（1秒間）
        player.setMetadata("recentlyCaptured", new org.bukkit.metadata.FixedMetadataValue(MonsterBall.getInstance(), System.currentTimeMillis()));
        event.setCancelled(true);
    }
}
