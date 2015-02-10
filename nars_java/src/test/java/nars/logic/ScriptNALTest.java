package nars.logic;

import nars.core.*;
import nars.io.ExampleFileInput;
import nars.io.condition.OutputCondition;
import nars.io.meter.Metrics;
import nars.io.meter.event.DoubleMeter;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ObjectMeter;
import nars.logic.entity.Task;
import nars.util.data.CuckooMap;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * NAL tests implemented in a .nal script file
 */
@Ignore
@RunWith(Parameterized.class)
abstract public class ScriptNALTest extends AbstractNALTest {


    private final long rngSeed;


    private final String path;

    public ScriptNALTest(NewNAR b, String path) {
        this(b, path, 1);
    }

    public ScriptNALTest(NewNAR b, String path, long rngSeed) {
        super(b);

        this.path = path;
        this.rngSeed = rngSeed;

    }



    abstract public int getMaxCycles();

    @Test
    public void theTest() {

        runScript(nar, path, getMaxCycles(), rngSeed);

    }

    public static Collection<String> getPaths(String... directories) {
        Map<String, String> et = ExampleFileInput.getUnitTests(directories);
        Collection<String> t = et.values();
        return t;
    }

    public static Collection getParams(String directories, NewNAR... builds) {
        return getParams(new String[] { directories }, builds);
    }

    //@Parameterized.Parameters(name="{1} {0}")
    public static Collection getParams(String[] directories, NewNAR... builds) {
        Collection<String> t = getPaths(directories);

        Collection<Object[]> params = new ArrayList(t.size() * builds.length);
        for (String script : t) {
            for (NewNAR b : builds) {
                params.add(new Object[] { b, script });
            }
        }
        return params;
    }

}
