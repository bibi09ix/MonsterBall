package com.github.bibi09ix.monsterball;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.bibi09ix.monsterball.listeners.EntityDespawnListener;
import com.github.bibi09ix.monsterball.listeners.MonsterBallBackListener;
import com.github.bibi09ix.monsterball.listeners.MonsterBallListener;
import com.github.bibi09ix.monsterball.utils.DataManager;
import com.github.bibi09ix.monsterball.utils.ItemManager;

public class MonsterBall extends JavaPlugin {
    
    private DataManager dataManager;
    private ItemManager itemManager;
    private NamespacedKey monsterBallKey;

    @Override
    public void onEnable() {
        getLogger().info("MonsterBall Plugin has been enabled!");

        this.dataManager = new DataManager(this);
        this.itemManager = new ItemManager();
        this.monsterBallKey = new NamespacedKey(this, "monster_ball_id");

        // レシピ登録
        registerRecipes();

        // イベントリスナー登録
        getServer().getPluginManager().registerEvents(new MonsterBallListener(dataManager, monsterBallKey, getLogger()), this);
        getServer().getPluginManager().registerEvents(new MonsterBallBackListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new EntityDespawnListener(dataManager), this);

        // 設定ファイルの読み込み
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("MonsterBall Plugin has been disabled!");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * 🔹 クラフトレシピを登録
     */
    private void registerRecipes() {
        // モンスターボールのレシピ
    	int entityID = dataManager.getNextEntityID();
    	ItemStack monsterBall = ItemManager.createMonsterBall(entityID, monsterBallKey);
        NamespacedKey ballKey = new NamespacedKey(this, "monster_ball");
        ShapedRecipe ballRecipe = new ShapedRecipe(ballKey, monsterBall);
        ballRecipe.shape("GAG", "AOA", "GAG");
        ballRecipe.setIngredient('G', org.bukkit.Material.GHAST_TEAR);
        ballRecipe.setIngredient('A', org.bukkit.Material.AMETHYST_BLOCK);
        ballRecipe.setIngredient('O', org.bukkit.Material.ENDER_PEARL);
        Bukkit.addRecipe(ballRecipe);

        // モンスターボールバックのレシピ
        ItemStack monsterBallBack = ItemManager.createMonsterBallBack();
        NamespacedKey backKey = new NamespacedKey(this, "monster_ball_back");
        ShapedRecipe backRecipe = new ShapedRecipe(backKey, monsterBallBack);
        backRecipe.shape("GEG", "CBC", "GEG");
        backRecipe.setIngredient('G', org.bukkit.Material.GHAST_TEAR);
        backRecipe.setIngredient('E', org.bukkit.Material.ENDER_CHEST);
        backRecipe.setIngredient('C', org.bukkit.Material.CHEST);
        backRecipe.setIngredient('B', org.bukkit.Material.BUNDLE);
        Bukkit.addRecipe(backRecipe);

        getLogger().info("Recipes have been registered!");
    }
}
