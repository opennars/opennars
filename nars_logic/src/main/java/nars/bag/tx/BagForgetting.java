package nars.bag.tx;

import nars.AbstractMemory;
import nars.Global;
import nars.Memory;
import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.budget.Itemized;

/**
* Applies forgetting to the next sequence of sampled bag items
 *
 * NOT thread safe
*/
public class BagForgetting<K, V extends Itemized<K>> implements BagTransaction<K,V> {


    protected float forgetCycles;
    public V selected = null;
    protected long now;



    public BagForgetting() {
        this.forgetCycles = Float.NaN;
    }

    @Override
    public K name() {
        return null; //signals to the bag updater to use the next item
    }

    protected static boolean forgettingCouldAffectItemBudget(final long now, final Itemized v) {

        final Budget b = v.getBudget();
        final long lastForgetTime = b.getLastForgetTime();

        if (lastForgetTime == -1) {
            b.setLastForgetTime(now);
            return false;
        }

        ////there is >0 time across which forgetting would be applied
        return (lastForgetTime != now);

                //&& (b.getPriority() > b.getQuality() * Global.MIN_FORGETTABLE_PRIORITY); //there is sufficient priority for forgetting to occurr
    }

    /** updates with current time, etc. call immediately before update() will be called */
    public void set(float forgetCycles, long now) {
        this.forgetCycles = forgetCycles;
        this.now = now;
    }

    public void set(float forgetCycles, AbstractMemory memory) {
        set(forgetCycles, memory.time());
    }



    @Override
    public V update(final V v) {
        this.selected = v;

        if (!forgettingCouldAffectItemBudget(now, v)) {
            //unaffected; null means that the item's budget was not changed,
            // so the bag knows it can avoid any reindexing it)
            return null;
        }

        Memory.forget(now, v, forgetCycles, Global.MIN_FORGETTABLE_PRIORITY);

        return v;
    }

    @Override
    public V updateItem(final V v) {
        throw new RuntimeException("should not be called since update() is overridden");
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
