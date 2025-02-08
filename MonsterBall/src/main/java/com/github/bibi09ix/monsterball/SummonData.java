package com.github.bibi09ix.monsterball;

public class SummonData {
    private String action; // "capture", "summon", "summon_back"
    private Integer mobId; // 捕獲済みモブID（capture/summon時）
    private String invUUID; // バッグからの召喚時

    public SummonData(String action, Integer mobId, String invUUID) {
        this.action = action;
        this.mobId = mobId;
        this.invUUID = invUUID;
    }

    public String getAction() {
        return action;
    }

    public Integer getMobId() {
        return mobId;
    }

    public String getInvUUID() {
        return invUUID;
    }
}
