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


    /** updates with current time, etc. call immediately before update() will be called */
    public void set(float forgetCycles, long now) {
        this.forgetCycles = forgetCycles;
        this.now = now;
    }

    public void set(float forgetCycles, AbstractMemory memory) {
        set(forgetCycles, memory.time());
    }


    @Override
    public Budget updateItem(V v, Budget result) {
        this.selected = v;

        //final float priorityStart = v.getPriority();

        /*final float priorityEnd = */
        Memory.forget(now, result, forgetCycles, Global.MIN_FORGETTABLE_PRIORITY);

        return result;
    }


    @Override
    public V newItem() {
        return null;
    }


    public V getItem() {
        return null;
    }
}
