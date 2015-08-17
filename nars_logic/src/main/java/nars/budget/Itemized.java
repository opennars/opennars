package nars.budget;

import nars.util.data.id.Named;

/**
 * Created by me on 5/25/15.
 */
public interface Itemized<K> extends Budgeted, Named<K> {
    public void delete();
}
