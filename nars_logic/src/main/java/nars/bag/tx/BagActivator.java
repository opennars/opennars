package nars.bag.tx;

import nars.budget.Budget;
import nars.nal.Item;
import nars.bag.BagTransaction;

/**
* Created by me on 1/19/15.
*/
abstract public class BagActivator<K,V extends Item<K>> implements BagTransaction<K,V> {

    protected Budget budget;

    protected K key;


    public K getKey() {
        return key;
    }


    public void setBudget(Budget budget) {
        this.budget = budget;
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
        return budget.clone();
    }

    /** returns a reference to the budgetvalue; not a clone */
    public Budget getBudgetRef() {
        return budget;
    }
}
