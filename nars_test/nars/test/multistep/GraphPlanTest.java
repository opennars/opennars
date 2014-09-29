package nars.test.multistep;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.io.Output;
import static org.junit.Assert.assertTrue;
import org.junit.Test;



public class GraphPlanTest {

    @Test public void testGraphPlan1() throws Exception {
        String input = "";
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
        input += "<goal --> reached>!\n";
        testGraphPlan(input, 
                "<(&/,(^pick,X),+2,(^pick,Y),+3,(^pick,Z),+1) =/> <goal --> reached>>. %1.00;0.90%"    
        );
    }
    @Test public void testGraphPlan2() throws Exception {
        String input = "";
        input += "<C =/> <goal --> reached>>.\n";
        input += "<B =/> C>.\n";
        input += "<A =/> B>.\n";
        input += "<(&/,(^pick,X),+1) =/> A>.\n";    
        input += "<goal --> reached>!\n";
        testGraphPlan(input, "<(&/,(^pick,X),+2) =/> <goal --> reached>>. %1.00;0.90%");
    }
    
    public void testGraphPlan(String input, String expected) throws IOException {
        NAR n = new DefaultNARBuilder().build();
                
        AtomicBoolean success = new AtomicBoolean(false);
        
        n.addOutput(new Output() {

            @Override
            public void output(Class channel, Object o) {
                if (o.toString().contains(expected))
                    success.set(true);
            }
        
        });
        
        n.addInput(input);

        n.finish(25);
        
        assertTrue(success.get());
    }
    

    
}
