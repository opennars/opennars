package nars.bag.tx;

import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.nal.Item;
import nars.bag.Bag;
import nars.bag.BagTransaction;

/**
* Applies forgetting to the next sequence of sampled bag items
*/
public class ForgetNext<K, V extends Item<K>> implements BagTransaction<K,V> {

    private final Bag<K, V> bag;
    private float forgetCycles;
    private Memory memory;
    public V current = null;


    public ForgetNext(Bag<K, V> bag) {
        this.bag = bag;
        this.forgetCycles = Float.NaN;
    }

    @Override
    public K name() {
        return null; //signals to the bag updater to use the next item
    }

    protected boolean forgetWillChangeBudget(final Budget v) {
        final long now = memory.time();
        if (v.getLastForgetTime() == -1) {
            v.setLastForgetTime(now);
            return false;
        }
        return (v.getLastForgetTime() != now) && //there is >0 time across which forgetting would be applied
                (v.getPriority() > v.getQuality() * Global.FORGET_QUALITY_RELATIVE); //there is sufficient priority for forgetting to occurr
    }

    public void set(float forgetCycles, Memory memory) {
        this.forgetCycles = forgetCycles;
        this.memory = memory;
    }

    @Override
    public V updateItem(final V v) {
        /*if (Parameters.DEBUG) {
            if (!Float.isFinite(forgetCycles))
                throw new RuntimeException("Invalid forgetCycles parameter; set() method was probably not called prior");
        }*/

        this.current = v;

        if (!forgetWillChangeBudget(v)) {
            return null; //unaffected (null means that the item's budget was not changed, so the bag knows it can avoid any reindexing it)
        }

        memory.forget(v, forgetCycles, Global.FORGET_QUALITY_RELATIVE);

        return v;
    }

    @Override
    public V newItem() {
        return null;
    }

    @Override
    public Budget getBudget() {
        //this returns null to avoid the default budget merging;
        // budget manipulation happens entirely in this class's updateItem method
        return null;

    }

    @Override
    public Budget getBudgetRef() {
        return null;
    }

    public V getItem() {
        return null;
    }
}
