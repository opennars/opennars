package nars.util.bag.select;

import nars.logic.entity.BudgetValue;
import nars.logic.entity.Item;
import nars.util.bag.BagSelector;

/**
* Created by me on 1/19/15.
*/
abstract public class BagActivator<K,V extends Item<K>> implements BagSelector<K,V> {

    protected BudgetValue budget;

    protected K key;


    public K getKey() {
        return key;
    }


    public void setBudget(BudgetValue budget) {
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
    public BudgetValue getBudget() {
        return budget;
    }

}
