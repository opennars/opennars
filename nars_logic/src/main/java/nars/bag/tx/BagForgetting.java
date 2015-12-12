package nars.bag.tx;

import nars.bag.BagSelector;
import nars.budget.Itemized;
import nars.budget.UnitBudget;

/**
* Applies forgetting to the next sequence of sampled bag items
 *
 * NOT thread safe
*/
public class BagForgetting<K, V extends Itemized<K>> implements BagSelector<K,V> {


    protected float forgetCycles;

    /** provides a way for callee to get the current/last affected item,
     *  because it may have been removed in the update,
     *  in which case would update() return null*/
    public V current = null;

    protected long now;


    public BagForgetting() {
        forgetCycles = Float.NaN;
    }


    /** updates with current time, etc. call immediately before update() will be called */
    public BagForgetting<K,V> set(float forgetCycles, long now) {
        this.forgetCycles = forgetCycles;
        this.now = now;
        return this;
    }



    @Override
    public void updateItem(V v, UnitBudget result) {
        current = v;

        //final float priorityStart = v.getPriority();

        /*final float priorityEnd = */
        /*switch (param.forgetting) {
            case Iterative:
                BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                break;
            case Periodic:*/
        result.forget(now, forgetCycles, 0);

        //break;		         //break;
        //}		         //}

    }

}
