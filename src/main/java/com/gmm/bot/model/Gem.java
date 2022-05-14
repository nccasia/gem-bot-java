package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemModifier;
import com.gmm.bot.enumeration.GemType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gem {
    private static final int HEIGHT = 8;
    private static final int WIDTH = 8;
    private int index;
    private int x;
    private int y;
    private GemType type;
    private GemModifier modifier;

    public Gem(int index, GemType type,GemModifier gemModifier) {
        this.index = index;
        this.type = type;
        this.modifier = gemModifier;
        updatePosition();
    }

    public Gem(int index, GemType gemType){
        this(index,gemType,GemModifier.NONE);
    }

    private void updatePosition() {
        y = index / HEIGHT;
        x = index - y * WIDTH;
    }

    public boolean sameType(Gem other) {
        return this.type == other.type;
    }

    public boolean sameType(GemType type) {
        return this.type == type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gem gem = (Gem) o;

        if (index != gem.index) return false;
        return type == gem.type;
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
