package nars.bag.tx;

import nars.bag.BagSelector;
import nars.budget.Budget;
import nars.budget.Itemized;

import java.util.function.Function;

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
    public V lastForgotten = null;

    protected long now;


    public BagForgetting() {
        this.forgetCycles = Float.NaN;
    }


    /** updates with current time, etc. call immediately before update() will be called */
    public BagForgetting<K,V> set(float forgetCycles, long now) {
        this.forgetCycles = forgetCycles;
        this.now = now;
        return this;
    }

    private final Function<V, ForgetAction> defaultModel = o -> ForgetAction.SelectAndForget;

    @Override
    public Function<V, ForgetAction> getModel() {
        return defaultModel;
    }

    //    @Override
//    public Function<V, ForgetAction> getModel() {
////        @Override
////        public ForgetAction apply(TermLink termLink) {
////            return ForgetAction.SelectAndForget;
////        }
//        return null;
//    }


    @Override
    public Budget updateItem(V v, Budget result) {
        this.lastForgotten = v;

        //final float priorityStart = v.getPriority();

        /*final float priorityEnd = */
        /*switch (param.forgetting) {
            case Iterative:
                BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                break;
            case Periodic:*/
            result.forget(now, forgetCycles, 0);
        //break;
        //}


        /** even if budget is unchanged, we need to set the last forget time */
        v.getBudget().setLastForgetTime(result.getLastForgetTime());

        return result;
    }

}
