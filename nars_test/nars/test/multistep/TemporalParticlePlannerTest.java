package nars.test.multistep;

import java.io.IOException;
import nars.core.NAR;
import nars.core.build.Default;
import nars.test.core.NALTest.ExpectContains;
import static org.junit.Assert.assertEquals;
import org.junit.Test;



public class TemporalParticlePlannerTest {

    
    /** NOTE: needs search depth >= 12 */
    @Test public void testGraphPlan1Repeat() throws Exception {
        String input = "";
        
        
        //same as above with inputs rearranged
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
        input += "<goal --> reached>!\n";
//        if (repeat) {
//            input += "100\n";
//            input += "<goal --> reached>!\n";
//        }
        String exp = "<(&/,(^pick,X),+3,(^pick,Y),+3,(^pick,Z)) =/> <goal --> reached>>!";
        
        testGraphPlan(input, exp, true);
        //testGraphPlan(input, exp, false);
    }    

    public void testGraphPlan(String input, String expected, boolean withPlanner) throws IOException {
        
        NAR n = 
                withPlanner?
                    new Default().temporalPlanner(12, 64, 24).build() :
                    new Default().build();
        
        (n.param).decisionThreshold.set(0.3f);
        
        ExpectContains e = new ExpectContains(n, expected, true);
               
        n.addInput(input);

        n.step(44);
        
        assertEquals(withPlanner, e.success());
        
    }


//    @Test public void testGraphPlan1a() throws Exception {
//        String input = "";
//        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
//        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
//        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
//        input += "<goal --> reached>!\n";
//        testGraphPlan(input, 
//                "<(&/,(^pick,X),+3,(^pick,Y),+3,(^pick,Z),+1) =/> <goal --> reached>>! %1.00"
//        );
//    }
//    @Test public void testGraphPlan1() throws Exception {
//        String input = "";
//        
//        //same as above with inputs rearranged
//        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
//        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
//        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
//        input += "<goal --> reached>!\n";
//        testGraphPlan(input, 
//                "<(&/,(^pick,X),+3,(^pick,Y),+3,(^pick,Z),+1) =/> <goal --> reached>>! %1.00"
//        );
//    }    
//    
//    @Test public void testGraphPlan2() throws Exception {
//        String input = "";
//        input += "<C =/> <goal --> reached>>.\n";
//        input += "<B =/> C>.\n";
//        input += "<A =/> B>.\n";
//        input += "<(&/,(^pick,X),+1) =/> A>.\n";    
//        input += "<goal --> reached>!\n";
//        testGraphPlan(input, "<(&/,(^pick,X),+1) =/> <goal --> reached>>! %1.00");
//    }
    

        
        //System.out.println(input);
        /*new NWindow("Implication Graph",
        new ProcessingGraphPanel(n,
        new ImplicationGraphCanvas(
        n.memory.executive.graph))).show(500, 500);*/    
}
