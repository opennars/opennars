package nars.test;


import org.junit.Test;

/**
 * Unit Test Reasoning, using input and output files from nal/Examples ;
 * <pre>
 * To create a new test input, add the NARS input as XX-in.txt in nal/Examples ,
 *  run the test suite, and move resulting file in temporary directory
 * /tmp/nars_test/XX-out.txt
 * into nal/Example
 * </pre>
 *
 */
public class NALTest extends TestUtil {

    public NALTest() {
        super(true);
    }
    
    @Test public void testExample1_0() { testNAL("nal1.0.nal");  }
    @Test public void testExample1_1() { testNAL("nal1.1.nal");  }
    @Test public void testExample1_2() { testNAL("nal1.2.nal");  }
    @Test public void testExample1_3() { testNAL("nal1.3.nal");  }
    @Test public void testExample1_4() { testNAL("nal1.4.nal");  }
    @Test public void testExample1_5() { testNAL("nal1.5.nal");  }
    @Test public void testExample1_6() { testNAL("nal1.6.nal");  }
    @Test public void testExample1_7() { testNAL("nal1.7.nal");  }
    @Test public void testExample1_8() { testNAL("nal1.8.nal");  }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(NALTest.class);
    }


}
