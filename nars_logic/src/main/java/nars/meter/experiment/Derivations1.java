package nars.meter.experiment;

import nars.Global;
import nars.NAR;
import nars.io.in.LibraryInput;
import nars.meter.Derivations;
import nars.meter.TestNAR;
import nars.nar.Default;

import java.io.FileNotFoundException;

public class Derivations1  {



    public static void main(String[] args) throws FileNotFoundException {

        Global.DEBUG = true;

        Derivations d = new Derivations(false, false);


        int maxCycles = 500;
        int maxNAL = 5;
        int seeds = 32; //iterate seeds to be sure we get everything, eventually it will converge

        for (int seed = 1; seed < seeds; seed++) {
            for (String s : LibraryInput.getPaths("test1"/*, "test2"*/)) {
                NAR n = new TestNAR(new Default().setInternalExperience(null).level(maxNAL));
                n.memory.reset(seed);
                d.record(n);

                n.input(LibraryInput.getExample(s));
                n.frame(maxCycles);
            }
            int r = d.size();
            System.out.println("  " + r + " derivations");
        }

        d.print("/tmp/e.csv");
    }
}
