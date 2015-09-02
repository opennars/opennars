package nars.meter.bag;

import nars.budget.Item;
import nars.truth.DefaultTruth;
import nars.util.data.random.XORShiftRandom;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Random;

/** Empty Item implementation useful for testing */
public class NullItem extends Item.StringKeyItem {

    final static Random rng = new XorShift1024StarRandom(1);
    static int ID = 1;

    public final String key;

    public NullItem() {
        this(rng.nextFloat()); // * (1.0f - DefaultTruth.DEFAULT_TRUTH_EPSILON));
    }

    public NullItem(float priority, String key) {
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
