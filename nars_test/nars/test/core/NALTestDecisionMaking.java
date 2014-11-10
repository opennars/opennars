package nars.test.core;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class NALTestDecisionMaking extends NALTest  {

    @Parameterized.Parameters
    public static Collection params() {
        
        Map<String,Object> l = new TreeMap();
        
        final String[] directories = new String[] { "nal/DecisionMaking" };
        
        for (String dir : directories ) {

            File folder = new File(dir);
        
            for (final File file : folder.listFiles()) {
                if (file.getName().equals("README.txt"))
                    continue;
                if(!("extra".equals(file.getName()))) {
                    addTest(file.getName());
                    l.put(file.getName(), new Object[] { file.getAbsolutePath() } );
                }
            }
            
        }
                
        return l.values();
    }
    
    public static void main(String[] args) {
        runTests(NALTestDecisionMaking.class);
    }

    public NALTestDecisionMaking(String scriptPath) {
        super(scriptPath);
    }

    @Test
    public void test() {
        testNAL(scriptPath);
    }
}
