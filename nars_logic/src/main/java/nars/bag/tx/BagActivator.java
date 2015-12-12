package nars.bag.tx;

import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.budget.UnitBudget;

/**
* Created by me on 1/19/15.
*/
public abstract class BagActivator<K,V extends Itemized<K>> implements BagTransaction<K,V> {

    protected K key;

    //TODO make this thread safe
    private final transient UnitBudget nextActivation = new UnitBudget();


    public final void setBudget(Budget budget) {
        nextActivation.budget(budget);
    }

    public final UnitBudget getBudget() {
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
        return 1.0f;
    }

    public abstract long time();

    @Override public void updateItem(V v, UnitBudget result) {
        result.forget(time(), getForgetCycles(), 0);

        //TODO make this merge function parametric
        result.mergePlus(nextActivation, getActivationFactor());
    }

    public abstract float getForgetCycles();



    @Override
    public String toString() {
        return "BagActivator[" + key + ',' + super.toString() + ']';
    }
}
