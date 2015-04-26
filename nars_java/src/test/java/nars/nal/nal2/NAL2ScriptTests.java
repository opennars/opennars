package nars.nal.nal2;


import nars.ProtoNAR;
import nars.nal.ScriptNALTest;
import nars.prototype.Curve;
import nars.prototype.Default;
import nars.prototype.Classic;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL2ScriptTests extends ScriptNALTest {

    public NAL2ScriptTests(ProtoNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[] { "test2"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(3), //needs 3 for sets
                new Curve(),
                new Default.DefaultMicro(),
                new Classic()
        );
    }

    public int getMaxCycles() { return 300; }


}

