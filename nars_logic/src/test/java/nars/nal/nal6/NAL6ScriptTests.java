package nars.nal.nal6;


import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.nar.Default;
import nars.nar.experimental.Solid;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;

public class NAL6ScriptTests extends ScriptNALTest {

    public NAL6ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test6"},
                new Default(),
                new Default().setInternalExperience(null).level(6),
                new Solid(1, 256, 1, 4, 1, 3).setInternalExperience(null)
                //new Default().setInternalExperience(null),
                //new Classic().setInternalExperience(null)
        );
    }

    public int getMaxCycles() {
        if (build instanceof Solid) return 32;
        else
            return 1850;
    }


}

