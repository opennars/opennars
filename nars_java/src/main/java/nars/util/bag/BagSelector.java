package nars.util.bag;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Item;

/** interface for lazily constructing bag items, updating existing items.
 * this avoids construction when only updating the budget of an item already in the bag  */
public interface BagSelector<K,V extends Item<K>> extends BudgetValue.Budgetable {
    //TODO make a version which accepts an array or list of keys to select in batch

    /** item's key */
    public K name();

    /** called if putIn a bag and the item specified by the key doesn't exist,
     * so this will create it and the bag will insert the new instance  */
    public V newItem();

    /** allow selector to modify it, then if it returns non-null, reinsert
     * if this method simply returns null it will have no effect
     * */
    default public V updateItem(V v) {
        return null;
    }

    /** called when a bag operation produces an overflow (displaced item) */
    default void overflow(V overflow) {

    }


    public static class ForgetNext<K, V extends Item<K>> implements BagSelector<K,V> {

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
            return x.name();
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
            throw new RuntimeException("This bag does not support creation of new items, only updating existing ones");
        }

        @Override
        public BudgetValue getBudget() {
            return currentItem.budget;
        }

        public V getItem() {
            return currentItem;
        }
    }
}
