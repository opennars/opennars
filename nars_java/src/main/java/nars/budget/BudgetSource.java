package nars.budget;

import reactor.jarjar.jsr166e.extra.AtomicDouble;

/**
 * Created by me on 3/24/15.
 */
public interface BudgetSource {

    /** total energy available */
    public double energy();

    /** set the total energy */
    public double energy(double newValue);

    /** add to the total available energy */
    default public double energyAdd(final double newValue) {
        if (newValue == 0) return energy();
        return energy( energy() + newValue );
    }

    /** set total availbale energy to zero */
    default public void energyReset() { energy(0);     }

    /** returns any "change" not taken */
    default public double send(BudgetTarget target, double x) {
        double resultingBalance = energy() - x;
        if (resultingBalance < 0)
            return x;

        double r = target.receive(x);

        return energyAdd( -(x - r) );
    }

    public static class DefaultBudgetBuffer implements BudgetSource, BudgetTarget {


        private AtomicDouble energy = new AtomicDouble();

        @Override
        public double energy() {
            return energy.get();
        }

        @Override
        public double energy(double newValue) {
            energy.set(newValue);
            return newValue;
        }

        @Override
        public double receive(double amount) {
            energyAdd(amount);
            return 0; //return nothing
        }
    }

}
