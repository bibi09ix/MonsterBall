package com.github.bibi09ix.monsterball.listener;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import com.github.bibi09ix.monsterball.CapturedMobData;
import com.github.bibi09ix.monsterball.CustomItemUtil;
import com.github.bibi09ix.monsterball.DataManager;
import com.github.bibi09ix.monsterball.MonsterBall;
import com.github.bibi09ix.monsterball.SummonData;
import com.github.bibi09ix.monsterball.SummonDataManager;
import com.github.bibi09ix.monsterball.inventory.MonsterBallBackInventoryManager;
import com.github.bibi09ix.monsterball.util.NBTUtil;

public class ProjectileHitListener implements Listener {
    private MonsterBall plugin;
    private DataManager dataManager;

    public ProjectileHitListener(MonsterBall plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball))
            return;
        Snowball proj = (Snowball) event.getEntity();
        UUID projId = proj.getUniqueId();
        SummonData summonData = SummonDataManager.getAndRemoveSummonData(projId);
        if (summonData == null)
            return;
        String action = summonData.getAction();
        Player player = plugin.getServer().getPlayer(UUID.fromString(proj.getMetadata("player_uuid").get(0).asString()));
        if (player == null)
            return;
        Location hitLocation = proj.getLocation();
        World world = hitLocation.getWorld();

        if (action.equals("capture")) {
            Entity hitEntity = event.getHitEntity();
            if (hitEntity != null && hitEntity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) hitEntity;
                if (target.getType().toString().contains("ENDER_DRAGON") || target.getType().toString().contains("WITHER")) {
                    player.sendMessage(ChatColor.RED + "ボスモブは捕獲できません。");
                    proj.remove();
                    return;
                }
                String nbt = NBTUtil.getNBTData(target);
                CapturedMobData mobData = new CapturedMobData(nbt);
                target.remove();
                int mobId = dataManager.getNextId();
                dataManager.saveCapturedMobData(mobId, mobData);
                String mobName = NBTUtil.getMobDisplayName(nbt);
                String mobType = target.getType().toString();
                ItemStack loadedBall = CustomItemUtil.getMonsterBallItem(true, mobId, mobType, mobName);
                player.getInventory().addItem(loadedBall);
                player.sendMessage(ChatColor.GREEN + "モブを捕獲しました！");
                world.spawnParticle(Particle.HAPPY_VILLAGER, hitLocation, 20, 0.5, 0.5, 0.5, 0.1);
            } else {
                world.dropItemNaturally(hitLocation, CustomItemUtil.getMonsterBallItem(false, 0));
                player.sendMessage(ChatColor.YELLOW + "捕獲に失敗しました。");
            }
            proj.remove();
            return;
        } else if (action.equals("summon")) {
            Integer mobId = summonData.getMobId();
            if (mobId == null) {
                player.sendMessage(ChatColor.RED + "召喚用のモンスター情報が見つかりません。");
                proj.remove();
                return;
            }
            CapturedMobData mobData = dataManager.getCapturedMobData(mobId);
            if (mobData == null) {
                player.sendMessage(ChatColor.RED + "捕獲データが見つかりません。");
                proj.remove();
                return;
            }
            Location spawnLocation = findSafeLocation(hitLocation);
            LivingEntity spawned = NBTUtil.spawnEntityFromNBT(mobData.getNbtData(), spawnLocation);
            if (spawned != null) {
                player.sendMessage(ChatColor.GREEN + "モブを召喚しました！");
                world.spawnParticle(Particle.CLOUD, spawnLocation, 30, 0.5, 0.5, 0.5, 0.1);
            } else {
                player.sendMessage(ChatColor.RED + "モブの召喚に失敗しました。");
            }
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(CustomItemUtil.getMonsterBallItem(false, 0));
            } else {
                world.dropItemNaturally(hitLocation, CustomItemUtil.getMonsterBallItem(false, 0));
            }
            dataManager.removeCapturedMobData(mobId);
            proj.remove();
        } else if (action.equals("summon_back")) {
            String invUUID = summonData.getInvUUID();
            if (invUUID == null || invUUID.isEmpty()) {
                player.sendMessage(ChatColor.RED + "有効なモンスターボールバッグ情報が見つかりません。");
                proj.remove();
                return;
            }
            org.bukkit.inventory.Inventory inv = MonsterBallBackInventoryManager.getInventory(invUUID);
            if (inv == null) {
                player.sendMessage(ChatColor.RED + "モンスターボールバッグの中身が見つかりません。");
                proj.remove();
                return;
            }
            int ballCount = 0;
            for (ItemStack ball : inv.getContents()) {
                if (ball != null && CustomItemUtil.isMonsterBall(ball) && CustomItemUtil.isMonsterBallLoaded(ball)) {
                    int ballMobId = CustomItemUtil.getStoredMobId(ball);
                    CapturedMobData mobData = dataManager.getCapturedMobData(ballMobId);
                    if (mobData != null) {
                        Location spawnLoc = findSafeLocation(hitLocation);
                        LivingEntity spawned = NBTUtil.spawnEntityFromNBT(mobData.getNbtData(), spawnLoc);
                        if (spawned != null) {
                            world.spawnParticle(Particle.CLOUD, spawnLoc, 20, 0.5, 0.5, 0.5, 0.1);
                            dataManager.removeCapturedMobData(ballMobId);
                            ballCount++;
                        }
                    }
                }
            }
            for (int i = 0; i < ballCount; i++) {
                world.dropItemNaturally(hitLocation, CustomItemUtil.getMonsterBallItem(false, 0));
            }
            world.dropItemNaturally(hitLocation, CustomItemUtil.getMonsterBallBackItem(invUUID));
            player.sendMessage(ChatColor.GREEN + "モンスターボールバッグからモブを召喚しました！");
            plugin.getBackInventoryManager().clearInventory(invUUID);
            proj.remove();
        }
    }

    private Location findSafeLocation(Location loc) {
    	
        return loc.clone().add(0.5, 1, 0.5);
    }
}
