package com.github.bibi09ix.monsterball;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.bibi09ix.monsterball.inventory.MonsterBallBackInventoryManager;
import com.github.bibi09ix.monsterball.listener.MonsterBallBackCraftListener;
import com.github.bibi09ix.monsterball.listener.PlayerInteractEntityListener;
import com.github.bibi09ix.monsterball.listener.PlayerInteractListener;
import com.github.bibi09ix.monsterball.listener.ProjectileHitListener;

public class MonsterBall extends JavaPlugin {
    private static MonsterBall instance;
    private DataManager dataManager;
    private CooldownManager cooldownManager;
    private MonsterBallBackInventoryManager backInventoryManager;

    public static MonsterBall getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("MonsterBall plugin enabled.");

        dataManager = new DataManager(this);
        dataManager.loadData();
        cooldownManager = new CooldownManager();
        backInventoryManager = new MonsterBallBackInventoryManager(this);

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);
        getServer().getPluginManager().registerEvents(new MonsterBallBackCraftListener(), this);

        registerRecipes();
    }

    @Override
    public void onDisable() {
        dataManager.saveData();
        getLogger().info("MonsterBall plugin disabled.");
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public MonsterBallBackInventoryManager getBackInventoryManager() {
        return backInventoryManager;
    }

    private void registerRecipes() {
        // ★ モンスターボールのレシピ ★
        // [GHAST_TEAR] [圧縮アメジストブロック] [GHAST_TEAR]
        // [圧縮アメジストブロック] [ENDER_PEARL] [圧縮アメジストブロック]
        // [GHAST_TEAR] [圧縮アメジストブロック] [GHAST_TEAR]
        ShapedRecipe ballRecipe = new ShapedRecipe(new NamespacedKey(this, "monster_ball"),
                CustomItemUtil.getMonsterBallItem(false, 0));
        ballRecipe.shape("ABA", "BCB", "ABA");
        ballRecipe.setIngredient('A', Material.GHAST_TEAR);
        if (getServer().getPluginManager().getPlugin("CompressedAmethyst") != null) {
            try {
                Class<?> utilClass = Class.forName("com.github.bibi09ix.compressedamethyst.CompressedAmethystUtil");
                java.lang.reflect.Method method = utilClass.getMethod("getCompressedAmethystBlock");
                Object result = method.invoke(null);
                if (result instanceof ItemStack) {
                    ballRecipe.setIngredient('B', new RecipeChoice.ExactChoice((ItemStack) result));
                } else {
                    ballRecipe.setIngredient('B', Material.AMETHYST_BLOCK);
                    getLogger().warning("CompressedAmethystUtil.getCompressedAmethystBlock() did not return an ItemStack. Using AMETHYST_BLOCK.");
                }
            } catch (Exception e) {
                ballRecipe.setIngredient('B', Material.AMETHYST_BLOCK);
                getLogger().warning("Error accessing CompressedAmethystUtil: " + e.getMessage() + ". Using AMETHYST_BLOCK.");
            }
        } else {
            ballRecipe.setIngredient('B', Material.AMETHYST_BLOCK);
            getLogger().warning("CompressedAmethyst plugin not found, using AMETHYST_BLOCK.");
        }
        ballRecipe.setIngredient('C', Material.ENDER_PEARL);
        getServer().addRecipe(ballRecipe);
        getLogger().info("Monster Ball recipe registered.");

        // モンスターボールバッグのレシピ
        // [GHAST_TEAR] [ENDER_CHEST] [GHAST_TEAR]
        // [CHEST]      [SADDLE]      [CHEST]
        // [GHAST_TEAR] [ENDER_CHEST] [GHAST_TEAR]

        String invUUID = MonsterBallBackInventoryManager.createNewInventory();
        ItemStack backItem = CustomItemUtil.getMonsterBallBackItem(invUUID);

        NamespacedKey backKey = new NamespacedKey(this, "monster_ball_back");
        ShapedRecipe backRecipe = new ShapedRecipe(backKey, backItem);
        backRecipe.shape("ADA", "BCB", "ADA");
        backRecipe.setIngredient('A', Material.GHAST_TEAR);
        backRecipe.setIngredient('D', Material.ENDER_CHEST);
        backRecipe.setIngredient('B', Material.CHEST);
        backRecipe.setIngredient('C', Material.SADDLE); 
        getServer().addRecipe(backRecipe);
    }
}
