package nars.bag.tx;

import nars.Global;
import nars.Memory;
import nars.budget.Itemized;

import java.util.function.Function;

/**
 * Created by me on 8/5/15.
 */
public class ParametricBagForgetting<K, V extends Itemized<K>> extends BagForgetting<K, V> {


    public static enum ForgetAction {
        Select, SelectAndForget, Ignore, IgnoreAndForget
    }

    Function<V, ForgetAction> model = null;

    public ParametricBagForgetting() {
        super();
    }

    public void setModel(Function<V, ForgetAction> model) {
        this.model = model;
    }

    public V update(V v) {

        final boolean select, forget;
        final ForgetAction a;

        if (model != null)
            a = model.apply(v);
        else
            a = ForgetAction.SelectAndForget;

        switch (a) {
            case Ignore:
                select = forget = false;
                break;
            case IgnoreAndForget:
                select = false;
                forget = true;
                break;
            case Select:
                select = true;
                forget = true;
                break;
            case SelectAndForget:
                select = forget = true;
                break;
            default:
                throw new RuntimeException("invalid model");
        }

        this.selected = select ? v : null;


        if (!forget || !forgettingCouldAffectItemBudget(now, v)) {
            //unaffected; null means that the item's budget was not changed,
            // so the bag knows it can avoid any reindexing it)
            return null;
        }

        Memory.forget(now, v, forgetCycles, Global.MIN_FORGETTABLE_PRIORITY);

        return v;
    }


}
