package nars.nal.nal1;


import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.nar.Classic;
import nars.nar.Default;
import nars.nar.DefaultMicro;
import nars.nar.experimental.Solid;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;

public class NAL1ScriptTests extends ScriptNALTest {

    public NAL1ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test1"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(1),
                new DefaultMicro().setInternalExperience(null),
                new Classic().setInternalExperience(null),
                new Solid(1, 16, 1, 1, 1, 1).setInternalExperience(null)
        );
    }

    public int getMaxCycles() {
        if (build instanceof Solid)
            return 4; //yes needs only ONE cycle for these tests

        return 200;
    }



}

