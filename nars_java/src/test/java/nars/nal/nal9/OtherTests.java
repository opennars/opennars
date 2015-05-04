package nars.nal.nal9;


import nars.ProtoNAR;
import nars.nal.ScriptNALTest;
import nars.prototype.Default;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class OtherTests extends ScriptNALTest {

    public OtherTests(ProtoNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"operator"},
                new Default()
        );
    }

    public int getMaxCycles() { return 100; }


}

