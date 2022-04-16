package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemType;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
@Setter
public class Grid {
    private List<Gem> gems = new ArrayList<>();
    private Set<GemType> gemTypes = new HashSet<>();
    private Set<GemType> myHeroGemType;

    public Grid(ISFSArray gemsCode,Set<GemType> heroGemType) {
        updateGems(gemsCode);
        this.myHeroGemType = heroGemType;
    }

    public void updateGems(ISFSArray gemsCode) {
        gems.clear();
        gemTypes.clear();
        for (int i = 0; i < gemsCode.size(); i++) {
            Gem gem = new Gem(i, GemType.from(gemsCode.getByte(i)));
            gems.add(gem);
            gemTypes.add(gem.getType());
            //log.info("Gem info| index: "+i+" type "+gem.getType());
        }
       // printArrayGems();
    }

    public Pair<Integer> recommendSwapGem(){
        List<GemSwapInfo> listMatchGem = suggestMatch();
        if(listMatchGem.isEmpty()){
            return new Pair<>(-1,-1);
        }
        Optional<GemSwapInfo> matchGemSizeThanFour =
                listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 4 ).findFirst();
        if (matchGemSizeThanFour.isPresent()){
            return matchGemSizeThanFour.get().getIndexSwapGem();
        }
        Optional<GemSwapInfo> matchGemSizeThanThree =
                listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 3 ).findFirst();
        if (matchGemSizeThanThree.isPresent()){
            return matchGemSizeThanThree.get().getIndexSwapGem();
        }
        Optional<GemSwapInfo> matchGemSword =
                listMatchGem.stream().filter(gemMatch -> gemMatch.getType() == GemType.SWORD).findFirst();
        if (matchGemSword.isPresent()){
            return matchGemSword.get().getIndexSwapGem();
        }
        for (GemType type : myHeroGemType) {
            Optional<GemSwapInfo> matchGem =
                    listMatchGem.stream().filter(gemMatch -> gemMatch.getType() == type).findFirst();
            if (matchGem.isPresent()){
                return matchGem.get().getIndexSwapGem();
            }
        }
        return listMatchGem.get(0).getIndexSwapGem();
    }


    public List<GemSwapInfo> suggestMatch() {
        List<GemSwapInfo> listMatchGem = new ArrayList<>();
        for (Gem currentGem : gems) {
            Gem swapGem = null;
            // If x > 0 => swap left & check
            if (currentGem.getX() > 0) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX() - 1, currentGem.getY()));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
            // If x < 7 => swap right & check
            if (currentGem.getX() < 7) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX() + 1, currentGem.getY()));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
            // If y < 7 => swap up & check
            if (currentGem.getY() < 7) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX(), currentGem.getY() + 1));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
            // If y > 0 => swap down & check
            if (currentGem.getY() > 0) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX(), currentGem.getY() - 1));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
        }
        return listMatchGem;
    }

    private void checkMatchSwapGem(List<GemSwapInfo> listMatchGem, Gem currentGem, Gem swapGem) {
        swap(currentGem, swapGem, gems);
        Set<Gem> matchGems = matchesAt(currentGem.getX(), currentGem.getY());
        swap(currentGem, swapGem, gems);
        if (!matchGems.isEmpty()) {
            listMatchGem.add( new GemSwapInfo(currentGem.getIndex(), swapGem.getIndex(),matchGems.size(),currentGem.getType()));
        }
    }

    private int getGemIndexAt(int x, int y) {
        return x + y * 8;
    }

    private void swap(Gem a, Gem b, List<Gem> gems) {
        int tempIndex = a.getIndex();
        int tempX = a.getX();
        int tempY = a.getY();

        // update reference
        gems.set(a.getIndex(), b);
        gems.set(b.getIndex(), a);

        // update data of element
        a.setIndex(b.getIndex());
        a.setX(b.getX());
        a.setY(b.getY());

        b.setIndex(tempIndex);
        b.setX(tempX);
        b.setY(tempY);
    }

    private Set<Gem> matchesAt(int x, int y) {
        Set<Gem> res = new HashSet<>();
        Gem center = gemAt(x, y);
        if (center == null) {
            return res;
        }

        // check horizontally
        List<Gem> hor = new ArrayList<>();
        hor.add(center);
        int xLeft = x - 1, xRight = x + 1;
        while (xLeft >= 0) {
            Gem gemLeft = gemAt(xLeft, y);
            if (gemLeft != null) {
                if (!gemLeft.sameType(center)) {
                    break;
                }
                hor.add(gemLeft);
            }
            xLeft--;
        }
        while (xRight < 8) {
            Gem gemRight = gemAt(xRight, y);
            if (gemRight != null) {
                if (!gemRight.sameType(center)) {
                    break;
                }
                hor.add(gemRight);
            }
            xRight++;
        }
        if (hor.size() >= 3) res.addAll(hor);

        // check vertically
        List<Gem> ver = new ArrayList<>();
        ver.add(center);
        int yBelow = y - 1, yAbove = y + 1;
        while (yBelow >= 0) {
            Gem gemBelow = gemAt(x, yBelow);
            if (gemBelow != null) {
                if (!gemBelow.sameType(center)) {
                    break;
                }
                ver.add(gemBelow);
            }
            yBelow--;
        }
        while (yAbove < 8) {
            Gem gemAbove = gemAt(x, yAbove);
            if (gemAbove != null) {
                if (!gemAbove.sameType(center)) {
                    break;
                }
                ver.add(gemAbove);
            }
            yAbove++;
        }
        if (ver.size() >= 3) res.addAll(ver);

        return res;
    }

    // Find Gem at Position (x, y)
    private Gem gemAt(int x, int y) {
        for (Gem g : gems) {
            if (g != null && g.getX() == x && g.getY() == y) {
                return g;
            }
        }
        return null;
    }

    private void printArrayGems() {
        int width = 8;
        int height = (gems.size() - 1) / width;
        for (int i = height; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                System.out.print((gems.get(j + i * width).getType().getCode() + "\t"));
            }
            System.out.println("");
        }
        System.out.println("");
    }

}
