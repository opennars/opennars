/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.core.build.Neuromorphic;
import nars.core.logic.NALTest;

/**
 *
 * @author me
 */

//@Ignore
public class NALTestNeuromorphic extends NALTest {

    public NALTestNeuromorphic(String scriptPath) {
        super(new Neuromorphic(2), scriptPath);
        System.out.println(scriptPath);
    }


    public static void main(String[] args) {        
        Parameters.DEBUG = false;
        Parameters.THREADS = 1;
        runTests(NALTestNeuromorphic.class);
    }
    
    
    
}
