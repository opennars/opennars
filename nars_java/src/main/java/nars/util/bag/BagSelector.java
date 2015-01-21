package nars.util.bag;

import nars.core.Memory;
import nars.logic.entity.*;

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
}
