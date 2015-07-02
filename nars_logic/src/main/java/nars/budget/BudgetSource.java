package nars.budget;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Created by me on 3/24/15.
 */
public interface BudgetSource {

    /** total energy available */
    public float energy();

    /** set the total energy */
    public float energy(float newValue);

    /** add to the total available energy */
    default public float energyAdd(final float newValue) {
        if (newValue == 0) return energy();
        return energy( energy() + newValue );
    }

    /** set total availbale energy to zero */
    default public void energyReset() { energy(0);     }

    /** returns any "change" not taken */
    default public float send(BudgetTarget target, float x) {
        float resultingBalance = energy() - x;
        if (resultingBalance < 0)
            return x;

        float r = target.receive(x);

        return energyAdd( -(x - r) );
    }

    public static class DefaultBudgetBuffer implements BudgetSource, BudgetTarget {


        private final AtomicDouble energy = new AtomicDouble();

        @Override
        public float energy() {
            return energy.floatValue();
        }

        @Override
        public float energy(float newValue) {
            energy.set(newValue);
            return newValue;
        }

        @Override
        public float receive(float amount) {
            energyAdd(amount);
            return 0; //return nothing
        }
    }

}
