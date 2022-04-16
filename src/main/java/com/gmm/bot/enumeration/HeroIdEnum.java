package com.gmm.bot.enumeration;

public enum HeroIdEnum {
    THUNDER_GOD(0),
    MONK(1),
    AIR_SPIRIT(2),
    SEA_GOD(3),
    MERMAID(4),
    SEA_SPIRIT(5),
    FIRE_SPIRIT(6),
    CERBERUS(7),
    DISPATER(8),
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

