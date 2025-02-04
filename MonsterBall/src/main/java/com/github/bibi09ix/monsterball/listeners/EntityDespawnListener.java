package com.github.bibi09ix.monsterball.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.bibi09ix.monsterball.utils.DataManager;

public class EntityDespawnListener implements Listener {

    private final DataManager dataManager;

    public EntityDespawnListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            String lore = meta.getLore().get(0);
            if (lore.startsWith("ID: ")) {
                try {
                    int entityID = Integer.parseInt(lore.replace("ID: ", ""));
                    dataManager.deleteEntityData(entityID);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
