package nars.nal.nal2;


import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.model.impl.Curve;
import nars.model.impl.Default;
import nars.model.impl.Classic;
import nars.model.impl.DefaultMicro;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.LibraryInput.getParams;

public class NAL2ScriptTests extends ScriptNALTest {

    public NAL2ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[] { "test2"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(3), //needs 3 for sets
                new Curve().setInternalExperience(null),
                new Curve().level(3),
                new DefaultMicro(),
                new Classic()
        );
    }

    public int getMaxCycles() { return 400; }


}

