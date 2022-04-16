package com.gmm.bot.enumeration;

public enum GemType {
    EMPTY(-1),
    SWORD(0),
    GREEN(1),
    YELLOW(2),
    RED(3),
    PURPLE(4),
    BLUE(5),
    BROWN(6),
    ;

    public static final int SIZE = values().length;

    private final int code;

    GemType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static GemType from(byte value) {
        for (GemType type : values()) {
            if (type.code == value) return type;
        }
        return null;
    }

    public static GemType from(String value) {
        for (GemType type : values()) {
            if (value.equals(type.toString())) return type;
        }
        return null;
    }
}
