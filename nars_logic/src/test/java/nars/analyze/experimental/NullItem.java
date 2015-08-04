package nars.analyze.experimental;

import nars.budget.Item;
import nars.truth.DefaultTruth;

/** Empty Item implementation useful for testing */
public class NullItem extends Item.StringKeyItem {
    public String key;

    public NullItem() {
        this(BagPerf.rng.nextFloat() * (1.0f - DefaultTruth.DEFAULT_TRUTH_EPSILON));
    }

    public NullItem(float priority, String key) {
        super(priority, priority, priority);
        this.key = key;
    }

    public NullItem(float priority) {
        super(priority, priority, priority);
        this.key = "" + (BagPerf.itemID++);
    }

    @Override
    public CharSequence name() {
        return key;
    }

}
