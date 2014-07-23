package nars.test.core;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class NALTest extends TestUtil {

    static final boolean testPerformance = false;
    
    @Parameterized.Parameters
    public static Collection params() {
        List l = new LinkedList();
        
        File folder = new File("nal/test");
        
        for (final File file : folder.listFiles()) {
            l.add(new Object[] { file.getAbsolutePath() } );
        }
                  
        return l;
    }

    private final String scriptPath;

    public NALTest(String scriptPath) {
        super(testPerformance);
        this.scriptPath = scriptPath;
    }

    @Test
    public void test() {
        testNAL(scriptPath);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(NALTest.class);
    }

}
