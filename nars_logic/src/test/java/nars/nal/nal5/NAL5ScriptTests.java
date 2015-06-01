package nars.nal.nal5;


import nars.model.impl.Curve;
import nars.model.impl.Default;
import nars.NARSeed;
import nars.nal.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;

public class NAL5ScriptTests extends ScriptNALTest {

    public NAL5ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test5"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(5),
                new Curve());
    }


    public int getMaxCycles() { return 100; }


}

