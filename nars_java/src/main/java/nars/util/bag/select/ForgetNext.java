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
    private V currentItem;
    private float forgetCycles;
    private Memory memory;


    public ForgetNext(Bag<K, V> bag) {
        this.bag = bag;
        this.forgetCycles = Float.NaN;
    }

    @Override
    public K name() {
        V x = bag.PEEKNEXT();
        this.currentItem = x;
        if (x == null) return null;

        //check whether forgetting will actually change anything to avoid the cost of needlessly updating an item (ex: re-leveling in LevelBag)
        if (!forgetWillChangeBudget())
            return null;

        return x.name();
    }

    protected boolean forgetWillChangeBudget() {
        BudgetValue v = this.currentItem.budget;
        return (v.getLastForgetTime() != memory.time()) && //there is >0 time across which forgetting would be applied
                (v.getPriority() > Parameters.FORGET_QUALITY_RELATIVE); //there is sufficient priority for forgetting to occurr
    }

    public void set(float forgetCycles, Memory memory) {
        this.forgetCycles = forgetCycles;
        this.memory = memory;
    }

    @Override
    public V updateItem(V v) {
        if (!Float.isFinite(forgetCycles))
            throw new RuntimeException("Invalid forgetCycles parameter; set() method was probably not called prior");

        memory.forget(currentItem, forgetCycles, Parameters.FORGET_QUALITY_RELATIVE);
        return currentItem;
    }

    @Override
    public V newItem() {
        System.err.println("This bag does not support creation of new items, only updating existing ones");
        //throw new RuntimeException();
        return null;
    }

    @Override
    public BudgetValue getBudget() {
        return currentItem.budget;
    }

    public V getItem() {
        return currentItem;
    }
}
