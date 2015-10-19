package nars.bag.tx;

import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.budget.Itemized;

/**
* Created by me on 1/19/15.
*/
abstract public class BagActivator<K,V extends Itemized<K>> implements BagTransaction<K,V> {

    protected K key;

    //TODO make this thread safe
    final transient private Budget nextActivation = new Budget();


    public final void setBudget(Budget budget) {
        nextActivation.budget(budget);
    }

    public final Budget getBudget() {
        return nextActivation;
    }

    public final void setKey(K key) {
        this.key = key;
    }

    @Override
    public K name() {
        return key;
    }


    public float getActivationFactor() {
        return 1f;
    }

    abstract public long time();

    @Override public void updateItem(final V v, final Budget result) {
        result.forget(time(), getForgetCycles(), 0);

        //TODO make this merge function parametric
        result.mergePlus(nextActivation, getActivationFactor());
    }

    abstract public float getForgetCycles();



    @Override
    public String toString() {
        return "BagActivator[" + key + ',' + super.toString() + ']';
    }
}
