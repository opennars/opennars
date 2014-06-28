package nars.test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class NALTest extends TestUtil {

    @Parameterized.Parameters
    public static Collection params() {
        List l = new LinkedList();
        
        //NAL1
        for (int i = 0; i <= 8; i++)
            l.add(new Object[]{"nal1." + i + ".nal"});
        
        return l;
    }

    private final String scriptPath;

    public NALTest(String scriptPath) {
        super(true);
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
