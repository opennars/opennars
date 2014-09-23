package nars.test.multistep;

import java.io.IOException;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.Window;
import nars.gui.output.graph.SentenceGraphPanel;
import nars.io.TextOutput;
import org.junit.Test;



public class GraphPlanTest {

    static String input = "";
    static {
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
        input += "<goal --> reached>!\n";
    }
    
    @Test
    public void testGraphPlan() throws IOException {
        NAR n = new DefaultNARBuilder().build();
                
        new TextOutput(n, System.out);
        n.addInput(input);

        //new Window("Implications", new JGraphXGraphPanel(n.memory.executive.graph.implication)).show(500,500);
        
        
        for (int i = 0; i < 3; i++) {
        
            n.step(1);
            //System.in.read();
        }
        new Window("Implications", new SentenceGraphPanel(n, n.memory.executive.graph.implication)).show(500,500);
        
    }
    
    
    public static void main(String[] args) throws IOException {
        new GraphPlanTest().testGraphPlan();
        System.in.read();
    }
    /*
    @Test 
    public void testNAL8() {
	
        NAR n = new DefaultNARBuilder().build();
                
        new TextOutput(n, System.out);
        n.addInput(input);        
        n.finish(100);
     
        //expected plan:
     
        //(^pick,X),+2,+1,(^pick,Y),+3,(^pick,Z),+1
    
    }*/
    
    
}
