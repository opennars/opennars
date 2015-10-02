//package nars.bag.tx;
//
//import nars.budget.Budget;
//import nars.budget.Itemized;
//
//import java.util.function.Function;
//
///**
// * Created by me on 8/5/15.
// */
//public class ParametricBagForgetting<K, V extends Itemized<K>> extends BagForgetting<K, V> {
//
//
//
//    Function<V, ForgetAction> model = null;
//
//    public ParametricBagForgetting() {
//        super();
//    }
//
//    public Function<V, ForgetAction> getModel() {
//        return model;
//    }
//
//    public void setModel(Function<V, ForgetAction> model) {
//        this.model = model;
//    }
//
//    @Override
//    public Budget updateItem(V v, Budget result) {
//
//        final boolean select, forget;
//        final ForgetAction a;
//
//        if (model != null)
//            a = getModel().apply(v);
//        else
//            a = ForgetAction.SelectAndForget;
//
//        switch (a) {
//            case Ignore:
//                select = forget = false;
//                break;
//            case IgnoreAndForget:
//                select = false;
//                forget = true;
//                break;
//            case Select:
//                select = true;
//                forget = true;
//                break;
//            case SelectAndForget:
//                select = forget = true;
//                break;
//            default:
//                throw new RuntimeException("invalid model");
//        }
//
//        this.lastForgotten = select ? v : null;
//
//
//        if (!forget) {
//            //unaffected; null means that the item's budget was not changed,
//            // so the bag knows it can avoid any reindexing it)
//            return null;
//        }
//
//        final float priorityStart = v.getPriority();
//
//        /*switch (param.forgetting) {
//            case Iterative:
//                BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
//                break;
//            case Periodic:*/
//        //break;
//        //}
//        final float priorityEnd = result.forget(now, forgetCycles, 0).getPriority();
//        if (priorityStart == priorityEnd) {
//            /** null means it was not changed */
//            return null;
//        }
//
//        /** even if budget is unchanged, we need to set the last forget time */
//        v.getBudget().setLastForgetTime(result.getLastForgetTime());
//
//        return result;
//    }
//
//
//}
