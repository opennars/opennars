package nars.bag;

import nars.budget.Budget;
import nars.budget.Itemized;

/** transaction interface for lazily constructing bag items, and efficiently updating existing items.
  * this avoids construction when only updating the budget of an item already in the bag  */
public interface BagTransaction<K,V extends Itemized<K>> extends Budget.Budgetable {
    //TODO make a version which accepts an array or list of keys to select in batch

    ////TODO called before anything, including name().  return false to cancel the process before anything else happens */
    //default void start() {    }

    /** item's key; if null, the bag will use a peekNext operation to as the next item */
    public K name();

    /** called if putIn a bag and the item specified by the key doesn't exist,
     * so this will create it and the bag will insert the new instance  */
    public V newItem();

    /** allow selector to modify it, then if it returns non-null, reinsert
     * if this method simply returns null it means it has not changed the item
     * */
    V updateItem(V v);

    public default V update(final V v) {
        //if (v == null) throw new RuntimeException("null item");

        boolean changed = false;

        //1. merge the budget, if specified
        Budget b = getBudgetRef();
        if (b!=null) {
            changed = v.getBudget().merge(b);
        }

        //2. perform the update defined by implementations
        V w = updateItem(v);
        if (w != null)
            return w;
        else {
            if (changed)
                return v;
            return null;
        }
    }

    /** called when a bag operation produces an overflow (displaced item) */
    default void overflow(V overflow) {
        //System.err.println(this + " unhandled overflow: " + overflow);
        //new Exception().printStackTrace();
        overflow.delete();
    }

    /** returns a reference to the budgetvalue; not a clone */
    public Budget getBudgetRef();

}
