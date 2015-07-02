package nars.budget;

import nars.util.data.id.Named;

/**
 * Created by me on 5/25/15.
 */
public interface Itemized<K> extends Budget.Budgetable, Named<K> {
    public void delete();
}
