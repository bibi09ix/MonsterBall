package com.github.bibi09ix.monsterball.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.github.bibi09ix.monsterball.CustomItemUtil;
import com.github.bibi09ix.monsterball.DataManager;
import com.github.bibi09ix.monsterball.MonsterBall;

/**
 * アイテムの自然消滅時に、捕獲データを削除するリスナー
 */
public class ItemDespawnListener implements Listener {
    private MonsterBall plugin;
    private DataManager dataManager;

    public ItemDespawnListener(MonsterBall plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (CustomItemUtil.isMonsterBall(item) && CustomItemUtil.isMonsterBallLoaded(item)) {
            int mobId = CustomItemUtil.getStoredMobId(item);
            dataManager.removeCapturedMobData(mobId);
            plugin.getLogger().info("Captured mob data removed for mobId: " + mobId + " due to item despawn.");
        }
    }
}
