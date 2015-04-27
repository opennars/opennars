package nars.nal.nal1;


import nars.ProtoNAR;
import nars.nal.ScriptNALTest;
import nars.prototype.Curve;
import nars.prototype.Default;
import nars.prototype.Classic;
import nars.prototype.Solid;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL1ScriptTests extends ScriptNALTest {

    public NAL1ScriptTests(ProtoNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test1"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(1),
                new Curve(),
                new Default.DefaultMicro(),
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

