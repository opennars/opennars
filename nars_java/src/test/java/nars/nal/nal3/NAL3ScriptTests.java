package nars.nal.nal3;


import nars.prototype.Curve;
import nars.prototype.Default;
import nars.ProtoNAR;
import nars.nal.ScriptNALTest;
import nars.prototype.Classic;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL3ScriptTests extends ScriptNALTest {

    public NAL3ScriptTests(ProtoNAR b, String input) {
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

