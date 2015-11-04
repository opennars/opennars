package nars.meter.bag;

import nars.budget.Item;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Random;

/** Empty Item implementation useful for testing */
public class NullItem extends Item.StringKeyItem {

    final static Random rng = new XorShift1024StarRandom(1);
    static int ID = 1;

    public final CharSequence key;

    public NullItem() {
        this(rng.nextFloat()); // * (1.0f - DefaultTruth.DEFAULT_TRUTH_EPSILON));
    }

    /** random between range of priorities */
    public NullItem(float priMin, float priMax) {
        this(rng.nextFloat() * ( priMax-priMin) + priMin);
    }

    public NullItem(float priority, CharSequence key) {
        super(priority, priority, priority);
        this.key = key;
    }

    public NullItem(float priority) {
        this(priority, Integer.toString(ID++));
    }

    @Override
    public CharSequence name() {
        return key;
    }

}
