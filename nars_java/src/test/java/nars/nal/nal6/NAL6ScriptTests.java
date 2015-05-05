package nars.nal.nal6;


import nars.prototype.Curve;
import nars.prototype.Default;
import nars.ProtoNAR;
import nars.nal.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.LibraryInput.getParams;

public class NAL6ScriptTests extends ScriptNALTest {

    public NAL6ScriptTests(ProtoNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test6"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(6),
                new Curve());
    }

    public int getMaxCycles() { return 500; }


}

