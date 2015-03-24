package nars.nal.meta;

import nars.prototype.Default;
import nars.Memory;
import nars.NAR;
import nars.Global;
import nars.io.ExampleFileInput;
import nars.io.test.TestNAR;

public class Derivations1  {



    public static void main(String[] args) {

        Global.DEBUG = true;

        Derivations d = new Derivations(false, false);


        for (int seed = 0; seed < 4; seed++) {
            for (String s : ExampleFileInput.getPaths("test2")) {
                Memory.resetStatic(seed);
                NAR n = new TestNAR(new Default().setInternalExperience(null).level(3));
                d.record(n);
                n.input(ExampleFileInput.getExample(s));
                n.run(200);
            }
        }

        d.print(System.out);
    }
}
