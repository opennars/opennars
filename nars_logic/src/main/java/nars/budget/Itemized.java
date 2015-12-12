package nars.budget;

import nars.util.data.id.Named;

/**
 * Created by me on 5/25/15.
 */
public interface Itemized<K> extends Budgeted, Named<K> {

    /**
     *
     * it may be helpful to make this method synchronized
     * so that a successful deletion process occurs only once
     * and uninterrupted
     *
     * @return if it was already deleted, will immediately return false.
     */
     boolean delete();



}
