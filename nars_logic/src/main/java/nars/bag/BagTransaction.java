package nars.bag;

import nars.budget.Itemized;

/** transaction interface for lazily constructing bag items, and efficiently updating existing items.
  * this avoids construction when only updating the budget of an item already in the bag  */
public interface BagTransaction<K,V extends Itemized<K>> extends BagSelector<K,V> {

    /** item's key; if null, the bag will use a peekNext operation to as the next item */
    public K name();

    /** called if putIn a bag and the item specified by the key doesn't exist,
     * so this will create it and the bag will insert the new instance  */
    public V newItem();



}
