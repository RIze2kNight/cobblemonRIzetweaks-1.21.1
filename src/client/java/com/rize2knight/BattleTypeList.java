package com.rize2knight;

import java.util.HashMap;
import java.util.Map;

public class BattleTypeList {
    private static final Map<String, String> battleTypeChanges = new HashMap<>();

    public static Map<String, String> getBattleTypeChanges() {
        return battleTypeChanges;
    }
    public static void addTypeChange(String key, String type) {
        battleTypeChanges.put(key, type);
    }
    public static void clearBattleTypeChanges() {
        battleTypeChanges.clear();
    }
}
