package nars.test.multistep;

import java.io.IOException;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.Window;
import nars.gui.output.JGraphXGraphPanel;
import nars.io.TextOutput;
import org.junit.Test;

/**
 *
 * @author me
 */


public class GraphPlanCycleTest {

    static String input = "";
    static {
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,X),<c --> d>,+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),<a--> b>, +1) =/> <c --> d>>.\n";
        input += "<goal --> reached>!\n";
    }
    
    @Test
    public void testGraphPlanCycle() throws IOException {
        NAR n = new DefaultNARBuilder().build();
                
        new TextOutput(n, System.out);
        n.addInput(input);
        
        
        for (int i = 0; i < 12; i++) {
        
            n.step(1);
        }
        
        new Window("Implications", new JGraphXGraphPanel(n.memory.executive.graph.implication)).show(500,500);
        
    }
    
   
    public static void main(String[] args) throws IOException {
        new GraphPlanCycleTest().testGraphPlanCycle();
        System.in.read();
    }    
}
