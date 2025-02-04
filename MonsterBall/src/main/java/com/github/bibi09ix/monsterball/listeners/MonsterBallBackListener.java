package com.github.bibi09ix.monsterball.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.bibi09ix.monsterball.utils.DataManager;

public class MonsterBallBackListener implements Listener {

    private final DataManager dataManager;
    private final Map<Item, Integer> thrownBacks = new HashMap<>();

    public MonsterBallBackListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onBackThrow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction().toString().contains("RIGHT_CLICK") && player.isSneaking()
                && item.getType() == Material.BUNDLE) {
            event.setCancelled(true);

            // バックを投げる
            Item thrownItem = player.getWorld().dropItem(player.getEyeLocation(), item);
            thrownItem.setVelocity(player.getLocation().getDirection().multiply(1.0));
            thrownItem.setPickupDelay(Integer.MAX_VALUE);
            thrownBacks.put(thrownItem, 1);
            player.getInventory().remove(item);
        }
    }

    @EventHandler
    public void onBackLand(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;
        if (!thrownBacks.containsKey(item)) return;

        Location loc = item.getLocation();
        ItemStack backItem = item.getItemStack();
        BundleMeta bundleMeta = (BundleMeta) backItem.getItemMeta();
        List<ItemStack> storedItems = bundleMeta.getItems();

        for (ItemStack ball : storedItems) {
            if (ball != null && ball.hasItemMeta()) {
                ItemMeta meta = ball.getItemMeta();
                if (meta.hasLore()) {
                    String lore = meta.getLore().get(0).replace("ID: ", "");
                    try {
                        int entityID = Integer.parseInt(lore);
                        String entityData = dataManager.getEntityData(entityID);

                        if (entityData != null) {
                            loc.getWorld().spawnParticle(Particle.CLOUD, loc, 10); // 召喚エフェクト
                            loc.getWorld().spawnEntity(loc, EntityType.valueOf(entityData));
                            dataManager.deleteEntityData(entityID);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // 空のバックを落とす
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.BUNDLE));

        thrownBacks.remove(item);
        item.remove();
    }
}
