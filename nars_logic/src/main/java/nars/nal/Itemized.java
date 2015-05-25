package nars.nal;

import nars.budget.Budget;

/**
 * Created by me on 5/25/15.
 */
public interface Itemized<K> extends Budget.Budgetable, Named<K> {
    public void delete();
}
