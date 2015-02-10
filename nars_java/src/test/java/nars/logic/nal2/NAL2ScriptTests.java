package nars.logic.nal2;


import nars.build.Curve;
import nars.build.Default;
import nars.core.Memory;
import nars.core.NewNAR;
import nars.core.Parameters;
import nars.io.TextOutput;
import nars.io.narsese.InvalidInputException;
import nars.logic.AbstractNALScriptTests;
import nars.logic.AbstractNALTest;
import org.junit.After;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static nars.logic.nal7.Tense.Eternal;

public class NAL2ScriptTests extends AbstractNALScriptTests {

    public NAL2ScriptTests(NewNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(
                new String[] { "test2", "test3", "test4" },
                new Default(),
                new Default().setInternalExperience(null),
                new Curve() );
    }

    public int getMaxCycles() { return 200; }


//    @After
//    public void test() {
//        super.test();
//    }
}

