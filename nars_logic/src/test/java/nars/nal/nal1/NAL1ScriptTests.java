package nars.nal.nal1;


import nars.NARSeed;
import nars.model.impl.*;
import nars.nal.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.LibraryInput.getParams;

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
                new Curve().setInternalExperience(null),
                new Curve().level(1),
                new DefaultMicro(),
                new Classic(),
                new Solid(1, 32, 1, 1, 1,2)
        );
    }

    public int getMaxCycles() {
        if (build instanceof Solid)
            return 1; //yes needs only ONE cycle for these tests

        return 500;
    }



}

