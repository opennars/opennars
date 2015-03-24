package nars.nal;

import junit.framework.TestCase;
import nars.ProtoNAR;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * NAL tests implemented in a .nal script file
 */
@Ignore
@RunWith(Parameterized.class)
abstract public class ScriptNALTest extends AbstractNALTest {




    private final String path;

    public ScriptNALTest(ProtoNAR b, String path) {
        super(b);

        this.path = path;

    }



    abstract public int getMaxCycles();

    @Test
    public void theTest() {

        runScript(nar, path, getMaxCycles());

        String result = nar.evaluate();
        if (result!=null) {
            TestCase.assertTrue(result, false);
        }

    }



}
