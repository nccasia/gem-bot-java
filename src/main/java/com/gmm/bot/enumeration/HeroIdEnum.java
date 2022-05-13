package com.gmm.bot.enumeration;

public enum HeroIdEnum {
    THUNDER_GOD((byte) 0),
    MONK((byte) 1),
    AIR_SPIRIT((byte) 2),
    SEA_GOD((byte) 3),
    MERMAID((byte) 4),
    SEA_SPIRIT((byte) 5),
    FIRE_SPIRIT((byte) 6),
    CERBERUS((byte) 7),
    DISPATER((byte) 8),
    ELIZAH((byte) 9),
    TALOS((byte) 10),
    MONKEY((byte) 11),
    GUTS((byte) 12),

    // MINIONS. Start from 100, save space for heroes
    SKELETON((byte) 100),
    SPIDER((byte) 101),
    WOLF((byte) 102),
    BAT((byte) 103),
    BERSERKER((byte) 104),
    SNAKE((byte) 105),
    GIANT_SNAKE((byte) 106)
    ;

    public static final int SIZE = values().length;

    private final int code;

    HeroIdEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static HeroIdEnum from(byte value) {
        for (HeroIdEnum type : values()) {
            if (type.code == value) return type;
        }

        return null;
    }

    public static HeroIdEnum from(String value) {
        for (HeroIdEnum type : values()) {
            if (value.equals(type.toString())) return type;
        }
        return null;
    }
}

