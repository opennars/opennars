package nars.bag;

import nars.budget.Itemized;
import nars.budget.UnitBudget;

/**
 * TODO make a version which accepts an array or list of keys to select in batch
 */
public interface BagSelector<K, V extends Itemized<K>> {




    /**
     * returns the budget instance result if a new budget update is determined for item v.
     * also allow selector to modify anything else about the item besides budget
     * then if it returns non-null, reinsert
     * if this method simply returns null it means it has not changed the item
     *
     * @param result will be intialized to v's original budget value
     * */
    void updateItem(V v, UnitBudget result);




    /** called when a bag operation produces an overflow (displaced item) */
    default void overflow(V overflow) {
        //System.err.println(this + " unhandled overflow: " + overflow);
        //new Exception().printStackTrace();


        //HACK
//        if (overflow instanceof Task) {
//            Task t = ((Task)overflow);
//            if (t.isDeleted())
//                this.
//        }

        //overflow.delete();
    }

    class AnyItemSelector implements BagSelector {

        @Override
        public void updateItem(Itemized v, UnitBudget result) {

        }

    }

    BagSelector anyItemSelector = new AnyItemSelector();

}
