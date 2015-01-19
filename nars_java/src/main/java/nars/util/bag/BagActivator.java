package nars.util.bag;

import nars.core.Memory;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Item;

/**
* Created by me on 1/19/15.
*/
abstract public class BagActivator<K,V extends Item<K>> implements BagSelector<K,V> {

    public final Memory memory;
    protected BudgetValue budget;

    K key;

    public BagActivator(Memory memory) {
        this.memory = memory;
    }

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
