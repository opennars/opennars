package nars.bag.tx;

import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.nal.Itemized;

/**
* Created by me on 1/19/15.
*/
abstract public class BagActivator<K,V extends Itemized<K>> extends Budget implements BagTransaction<K,V> {



    protected K key;

    public BagActivator() {
        super(0,0,0);
    }

    public K getKey() {
        return key;
    }


    public void setBudget(Budget budget) {
        set(budget);
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public K name() {
        return key;
    }


    @Override
    public Budget getBudget() {
        return clone();
    }

    /** returns a reference to the budgetvalue; not a clone */
    @Deprecated public Budget getBudgetRef() {
        return this;
    }

    @Override
    public String toString() {
        return "BagActivator[" + key + ',' + super.toString() + ']';
    }
}
