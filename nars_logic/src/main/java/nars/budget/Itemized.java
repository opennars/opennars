package nars.budget;

/**
 * Created by me on 5/25/15.
 */
public interface Itemized<K> extends Budgeted {

    /**
     *
     * it may be helpful to make this method synchronized
     * so that a successful deletion process occurs only once
     * and uninterrupted
     *
     */
     void delete();

    K name();
}
