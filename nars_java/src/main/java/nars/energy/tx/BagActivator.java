package nars.energy.tx;

import nars.energy.Budget;
import nars.nal.entity.Item;
import nars.energy.BagTransaction;

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
