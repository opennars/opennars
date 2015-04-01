package nars.budget;

import reactor.jarjar.jsr166e.extra.AtomicDouble;

/**
 * Created by me on 3/24/15.
 */
public interface BudgetSource {

    /** total energy */
    public double energy();
    public double energy(double newValue);
    default public double energyAdd(final double newValue) {
        if (newValue == 0) return energy();
        return energy( energy() + newValue );
    }
    default public void energyReset() { energy(0);     }

    /** returns any "change" not taken */
    default public double send(BudgetTarget target, double x) {
        double resultingBalance = energy() - x;
        if (resultingBalance < 0)
            return x;

        double r = target.receive(x);

        return energyAdd( -(x - r) );
    }

    public static class DefaultEnergyBuffer extends AtomicDouble implements BudgetSource, BudgetTarget {


        @Override
        public double energy() {
            return get();
        }

        @Override
        public double energy(double newValue) {
            set(newValue);
            return newValue;
        }

        @Override
        public double receive(double amount) {
            energyAdd(amount);
            return 0; //return nothing
        }
    }

}
