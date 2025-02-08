package com.github.bibi09ix.monsterball.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTEntity;

@SuppressWarnings("deprecation")
public class NBTUtil {

    /**
     * キャプチャ時、エンティティの主要なNBTデータを文字列として取得します。
     * ※ 位置情報（Pos, Motion, Rotation）は削除しておく（再現時に新たな位置でスポーンさせるため）
     */
    public static String getNBTData(LivingEntity entity) {
        NBTEntity nbtEntity = new NBTEntity(entity);
        NBTContainer container = new NBTContainer(nbtEntity.getCompound());
        // 位置情報関連のタグは削除
        container.removeKey("Pos");
        container.removeKey("Motion");
        container.removeKey("Rotation");
        // 常に id タグをセット
        container.setString("id", entity.getType().name());
        // CustomName が存在する場合のみ保存（存在しなければ何も追加しない）
        if (entity.getCustomName() != null && !entity.getCustomName().isEmpty()) {
            container.setString("CustomName", entity.getCustomName());
        }
        container.setDouble("Health", entity.getHealth());
        // ゾンビの場合：子供かどうか
        if (entity.getType() == EntityType.ZOMBIE && entity instanceof org.bukkit.entity.Zombie) {
            boolean isBaby = ((org.bukkit.entity.Zombie) entity).isBaby();
            container.setBoolean("IsBaby", isBaby);
        }
        // 狼の場合：所有者UUID（必要なら）
        if (entity.getType() == EntityType.WOLF && entity instanceof org.bukkit.entity.Wolf) {
            org.bukkit.entity.Wolf wolf = (org.bukkit.entity.Wolf) entity;
            if (wolf.getOwner() != null) {
                container.setString("OwnerUUID", wolf.getOwner().getUniqueId().toString());
            }
        }
        return container.toString();
    }

    /**
     * NBTデータ文字列からツールチップ用表示名を取得します。
     * CustomName タグがあればその値を、なければ空文字を返します。
     */
    public static String getMobDisplayName(String nbtData) {
        try {
            NBTContainer container = new NBTContainer(nbtData);
            if (container.hasTag("CustomName")) {
                String customName = container.getString("CustomName");
                if (customName != null && !customName.isEmpty()) {
                    return customName;
                }
            }
            return "";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * NBTデータ文字列からエンティティを再現して召喚します。
     * 再現前にNBTデータに含まれる位置情報は削除しているので、spawnLocation で新たにスポーンさせます。
     */
    public static LivingEntity spawnEntityFromNBT(String nbtData, Location loc) {
        try {
            NBTContainer container = new NBTContainer(nbtData);
            String id = container.getString("id");
            if (id == null || id.isEmpty()) return null;
            EntityType type;
            try {
                type = EntityType.valueOf(id.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
            World world = loc.getWorld();
            LivingEntity entity = (LivingEntity) world.spawnEntity(loc, type);
            // 位置情報はすでに削除されているので mergeCompound() で他の情報を適用
            container.removeKey("id");
            NBTEntity nbtEntity = new NBTEntity(entity);
            nbtEntity.mergeCompound(container);
            if (container.hasTag("CustomName")) {
                entity.setCustomName(container.getString("CustomName"));
                entity.setCustomNameVisible(true);
            }
            if (container.hasTag("Health")) {
                double health = container.getDouble("Health");
                double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
                entity.setHealth(Math.min(health, maxHealth));
            }
            if (entity.getType() == EntityType.ZOMBIE && container.hasTag("IsBaby") && entity instanceof org.bukkit.entity.Zombie) {
                boolean isBaby = container.getBoolean("IsBaby");
                ((org.bukkit.entity.Zombie) entity).setBaby(isBaby);
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
