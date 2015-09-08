package nars.bag;

import nars.budget.Budget;
import nars.budget.Itemized;

import java.util.function.Function;

/**
 * TODO make a version which accepts an array or list of keys to select in batch
 */
public interface BagSelector<K, V extends Itemized<K>> {

    public static enum ForgetAction {
        Select, SelectAndForget, Ignore, IgnoreAndForget
    }

    //final static Function<Object,ForgetAction> defaultModel = (o) -> ForgetAction.SelectAndForget;

    default public Function<V, ForgetAction> getModel() {
        return null;
    }


    /**
     * returns the budget instance result if a new budget update is determined for item v.
     * also allow selector to modify anything else about the item besides budget
     * then if it returns non-null, reinsert
     * if this method simply returns null it means it has not changed the item
     *
     * @param result will be intialized to v's original budget value
     * */
    Budget updateItem(V v, Budget result);




    /** called when a bag operation produces an overflow (displaced item) */
    default void overflow(V overflow) {
        //System.err.println(this + " unhandled overflow: " + overflow);
        //new Exception().printStackTrace();
        overflow.delete();
    }

    public static class AnyItemSelector implements BagSelector {

        @Override
        public Budget updateItem(Itemized v, Budget result) {
            return null;
        }

    }

    public static final AnyItemSelector anyItemSelector = new AnyItemSelector();

}
