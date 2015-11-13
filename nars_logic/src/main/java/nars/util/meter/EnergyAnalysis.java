package nars.util.meter;

import nars.NAR;

/**
 * Utility class for Thermodynamic analysis of system Budgets
 */
public class EnergyAnalysis {

    public final NAR nar;

    public EnergyAnalysis(NAR n) {
        this.nar = n;
    }


    public MemoryBudget energy() {
        return new MemoryBudget(nar);
    }

    public void printEnergy() {
        System.out.println(nar.time() + ": " + energy());
    }



}
