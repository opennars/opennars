package nars.test.multistep;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.io.TextOutput;
import org.junit.Test;



public class GraphPlanTest {

    static String input = "";
    static {
        input += "<(&/,<a --> b>,+1,(^pick,test),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,test2),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,test3),+1) =/> <c --> d>>.\n";        
    }
    
    @Test
    public void testGraphPlan() {
        NAR n = new DefaultNARBuilder().build();
                
        new TextOutput(n, System.out);
        n.addInput(input);
        
        for (int i = 0; i < 130; i++) {
        
            n.step(1);
        }
    }
    
    /*
    @Test 
    public void testNAL8() {
	
        NAR n = new DefaultNARBuilder().build();
                
        new TextOutput(n, System.out);
        n.addInput(input);        
        n.finish(100);
     
        //expected plan:
     
        //(^pick,test2),+2,+1,(^pick,test),+3,(^pick,test3),+1
        //OUT: <(&/,(^pick,test2),+2,+1,(^pick,test),+3,<c --> d>) =/> <goal --> reached>>. %1.00;0.81% {94 : 1;0<(&/,(^pick,test2),+2) =/> <a --> b>>;<(&/,<a --> b>,+1,(^pick,test),+3,<c --> d>) =/> <goal --> reached>>} 
    }*/
    
    
}
