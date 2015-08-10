package nars.bag.tx;

import nars.Memory;
import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.Itemized;
import sun.rmi.server.Activation;

/**
* Created by me on 1/19/15.
*/
abstract public class BagActivator<K,V extends Itemized<K>> implements BagTransaction<K,V> {



    protected K key;
    Budget nextActivation = new Budget();


    public K getKey() {
        return key;
    }


    public void setBudget(Budget budget) {
        nextActivation.set(budget);
    }

    public Budget getBudget() {
        return nextActivation;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public K name() {
        return key;
    }


    public float getActivationFactor() {
        return 1f;
    }

    abstract public long time();

    @Override public Budget updateItem(V v, Budget result) {
        Memory.forget(time(), result, getForgetCycles(), getRelativeThreshold());

        BudgetFunctions.activate(result, nextActivation, BudgetFunctions.Activating.Accum, getActivationFactor());

        result.accumulate(nextActivation);
        return result;
    }

    abstract public float getForgetCycles();

    abstract public float getRelativeThreshold();

    @Override
    public String toString() {
        return "BagActivator[" + key + ',' + super.toString() + ']';
    }
}
