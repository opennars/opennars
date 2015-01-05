/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.storage;

/**
 * Variation of LevelBag which follows a different distributor policy but 
 * is much faster.  The policy should be equally fair as LevelBag
 */
public class LevelBag2 extends LevelBag {

    public LevelBag2(int levels, int capacity) {
        super(levels, capacity);
    }

    
    /** look for a non-empty level */
    @Override
    protected void nextNonEmptyLevel() {
               
        int cl = DISTRIBUTOR[(levelIndex++) % distributorLength];        
        while (levelEmpty[cl]) {
            cl++;
            cl%=levels;
        }

        currentLevel = cl;
                
        if (currentLevel < fireCompleteLevelThreshold) { // for dormant levels, take one item
            currentCounter = 1;
        } else {                  // for active levels, take all current items
            currentCounter = getNonEmptyLevelSize(currentLevel);
        }
    }
    
    
}
