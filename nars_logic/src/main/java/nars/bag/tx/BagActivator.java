package nars.bag.tx;

import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.budget.Itemized;

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


        nextActivation.mulPriority(getActivationFactor());
        result
                .forget(time(), getForgetCycles(), 0)
                .accumulate(nextActivation);

        /** even if budget is unchanged, we need to set the last forget time */
        v.getBudget().setLastForgetTime(result.getLastForgetTime());

        return result;
    }

    abstract public float getForgetCycles();



    @Override
    public String toString() {
        return "BagActivator[" + key + ',' + super.toString() + ']';
    }
}
