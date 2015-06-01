package nars.nal.nal3;


import nars.model.impl.Curve;
import nars.model.impl.Default;
import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.model.impl.Classic;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;

public class NAL3ScriptTests extends ScriptNALTest {

    public NAL3ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test3"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(3),
                new Curve(),
                new Classic()
        );
    }

    public int getMaxCycles() { return 200; }


}

