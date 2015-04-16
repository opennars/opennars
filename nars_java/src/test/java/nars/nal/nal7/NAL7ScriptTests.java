package nars.nal.nal7;


import nars.ProtoNAR;
import nars.nal.ScriptNALTest;
import nars.prototype.Curve;
import nars.prototype.Default;
import nars.prototype.Discretinuous;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL7ScriptTests extends ScriptNALTest {

    public NAL7ScriptTests(ProtoNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test7"},
                new Default(),
                new Default().setInternalExperience(null),
                new Discretinuous(),
                new Curve()
        );
    }

    public int getMaxCycles() { return 800; }


}

