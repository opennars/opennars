package nars.meter.experiment;

import nars.Global;
import nars.io.in.LibraryInput;
import nars.meter.DerivationGraph;
import nars.meter.TestNAR;
import nars.nar.Default;

import java.io.FileNotFoundException;

public class Derivations1  {



    public static void main(String[] args) throws FileNotFoundException {

        Global.DEBUG = true;

        DerivationGraph d = new DerivationGraph(false, false);


        int maxCycles = 500;
        int maxNAL = 5;
        int seeds = 32; //iterate seeds to be sure we get everything, eventually it will converge

        for (int seed = 1; seed < seeds; seed++) {
            for (String s : LibraryInput.getPaths("test1"/*, "test2"*/)) {
                TestNAR n = new TestNAR(new Default().nal(maxNAL));
                n.nar.memory.clear();
                n.nar.memory.setRandomSeed(seed);
                d.record(n.nar);

                n.nar.input(LibraryInput.getExample(s));
                n.run(maxCycles);
            }
            int r = d.size();
            System.out.println("  " + r + " derivations");
        }

        d.print("/tmp/e.csv");
    }
}
