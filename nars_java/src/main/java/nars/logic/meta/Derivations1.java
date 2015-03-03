package nars.logic.meta;

import nars.build.Default;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.ExampleFileInput;
import nars.io.test.TestNAR;

public class Derivations1  {



    public static void main(String[] args) {

        Parameters.DEBUG = true;

        Derivations d = new Derivations(false, false);


        for (int seed = 0; seed < 4; seed++) {
            for (String s : ExampleFileInput.getPaths("test2")) {
                Memory.resetStatic(seed);
                NAR n = new TestNAR(new Default().setInternalExperience(null).level(3));
                d.record(n);
                n.addInput(ExampleFileInput.getExample(s));
                n.run(200);
            }
        }

        d.print(System.out);
    }
}
