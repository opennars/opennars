package nars.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nars.io.ExampleFileInput;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;



/**
 * runs a subset of the test cases, selected by the boolean include(filename) function
 */
@RunWith(Parameterized.class)
@Ignore
public class NALTestSome extends NALTest {

    public static String testFilenameContains = "nal5.19";

    static {
        showOutput = true;
        showSuccess = showOutput;
        showTrace = false;               
    }
    
   public static boolean include(String filename) {
       return filename.contains(testFilenameContains);
   }

   
    @Parameterized.Parameters
    public static Collection params() {

        return Lists.newLinkedList( Iterables.filter(ExampleFileInput.getUnitTestPaths(), new Predicate<String>(){
            @Override
            public boolean apply(String s) {
                return include(s);
            }
        }));

    }
   
   public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(NALTestSome.class);
   }    

   public NALTestSome(String scriptPath) {
       super(scriptPath);//, true);

   }
//   
//   public NALTestSome(String scriptPath, boolean showOutput) {
//        super(scriptPath);
//        
//        this.showSuccess = showOutput;
//        this.showOutput = showOutput;
//        this.showTrace = false;
//        System.out.println("Running: "  + scriptPath);
//        //setOutput(true);
//   }
   
}
