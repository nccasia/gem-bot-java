package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class GemSwapInfo {
    private int index1;
    private int index2;
    private int sizeMatch;
    private GemType type;

    public Pair<Integer> getIndexSwapGem(){
        return new Pair<>(index1,index2);
    }
}
