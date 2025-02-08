package com.github.bibi09ix.monsterball.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import com.github.bibi09ix.monsterball.CustomItemUtil;
import com.github.bibi09ix.monsterball.inventory.MonsterBallBackInventoryManager;

public class MonsterBallBackCraftListener implements Listener {

    /**
     * クラフト画面に表示される完成品を、いったん "普通の鞍" に置き換える
     */
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        ItemStack result = event.getRecipe().getResult();
        if (result == null) return;
        // カスタムデータ付きの鞍(モンスターボールバッグ)かどうかチェック
        if (CustomItemUtil.isMonsterBallBack(result)) {
            // -> 普通の鞍(SADDLE) に置き換え
            ItemStack normalSaddle = new ItemStack(Material.SADDLE);
            event.getInventory().setResult(normalSaddle);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        // 現在クラフト結果として取り出そうとしているアイテム
        ItemStack current = event.getCurrentItem();
        if (current == null) return;
        
        // もし普通の鞍なら、モンスターボールバッグ(鞍)に差し替える
        if (current.getType() == Material.SADDLE && !CustomItemUtil.isMonsterBallBack(current)) {
            // まずバッグ用インベントリを作成
            String uuid = MonsterBallBackInventoryManager.createNewInventory();
            // カスタムデータ付きの鞍を生成
            ItemStack customSaddleBack = CustomItemUtil.getMonsterBallBackItem(uuid);

            // 取り出しを1回だけ処理するなら:
            event.setCurrentItem(customSaddleBack);

            // もし SHIFT クリックで複数生成を許可する場合は、作成個数に応じてロジック追加が必要。
            // ここでは単純に1回取り出すごとに1個だけ差し替える想定。
        }
    }
}
