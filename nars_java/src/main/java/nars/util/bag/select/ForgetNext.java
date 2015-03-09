package nars.util.bag.select;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Item;
import nars.util.bag.Bag;
import nars.util.bag.BagSelector;

/**
* Created by me on 2/4/15.
*/
public class ForgetNext<K, V extends Item<K>> implements BagSelector<K,V> {

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

    protected boolean forgetWillChangeBudget(BudgetValue v) {
        final long now = memory.time();
        if (v.getLastForgetTime() == -1) {
            v.setLastForgetTime(now);
            return false;
        }
        return (v.getLastForgetTime() != memory.time()) && //there is >0 time across which forgetting would be applied
                (v.getPriority() > v.getQuality() * Parameters.FORGET_QUALITY_RELATIVE); //there is sufficient priority for forgetting to occurr
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

        if (!forgetWillChangeBudget(v.budget)) {
            return null; //unaffected (null means that the item's budget was not changed, so the bag knows it can avoid any reindexing it)
        }

        memory.forget(v, forgetCycles, Parameters.FORGET_QUALITY_RELATIVE);

        return v;
    }

    @Override
    public V newItem() {
        return null;
    }

    @Override
    public BudgetValue getBudget() {
        //this returns null to avoid the default budget merging;
        // budget manipulation happens entirely in this class's updateItem method
        return null;

    }

    @Override
    public BudgetValue getBudgetRef() {
        return null;
    }

    public V getItem() {
        return null;
    }
}
