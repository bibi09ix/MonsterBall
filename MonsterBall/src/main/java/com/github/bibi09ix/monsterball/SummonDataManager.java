package com.github.bibi09ix.monsterball;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SummonDataManager {
    private static final ConcurrentHashMap<UUID, SummonData> dataMap = new ConcurrentHashMap<>();

    public static void putSummonData(UUID projectileId, SummonData data) {
        dataMap.put(projectileId, data);
    }

    public static SummonData getAndRemoveSummonData(UUID projectileId) {
        return dataMap.remove(projectileId);
    }
}
