package nars.util.bag;

import nars.logic.entity.BudgetValue;
import nars.logic.entity.Item;

/** interface for lazily constructing bag items, and avoiding construction
 * when only updating the budget of an item already in the bag  */
public interface BagSelector<K,V extends Item<K>> extends BudgetValue.Budgetable {

    public K name();

    /** the budget value used by a new instance, or to merge with an existing */
    public BudgetValue getBudget();

    /** called if putIn a bag and the item specified by the key doesn't exist,
     * so this will create it and the bag will insert the new instance  */
    public V newInstance();

}
