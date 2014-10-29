package nars.test.multistep;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.io.Output;
import org.junit.Test;



public class GraphPlanTest {

    @Test public void testGraphPlan1a() throws Exception {
        String input = "";
        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<goal --> reached>!\n";
        testGraphPlan(input, 
                "<(&/,(^pick,X),+3,(^pick,Y),+3,(^pick,Z),+1) =/> <goal --> reached>>! %1.00"
        );
    }
    @Test public void testGraphPlan1() throws Exception {
        String input = "";
        
        //same as above with inputs rearranged
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
        input += "<goal --> reached>!\n";
        testGraphPlan(input, 
                "<(&/,(^pick,X),+3,(^pick,Y),+3,(^pick,Z),+1) =/> <goal --> reached>>! %1.00"
        );
    }    
    @Test public void testGraphPlan1Repeat() throws Exception {
        String input = "";
        
        //same as above with inputs rearranged
        input += "<(&/,<a --> b>,+1,(^pick,Y),+3,<c --> d>) =/> <goal --> reached>>.\n";
        input += "<(&/,(^pick,X),+2) =/> <a --> b>>.\n";
        input += "<(&/,(^pick,Z),+1) =/> <c --> d>>.\n";    
        input += "<goal --> reached>!\n";
        input += "100\n";
        input += "<goal --> reached>!\n";
        testGraphPlan(input, 
                "<(&/,(^pick,X),+3,(^pick,Y),+3,(^pick,Z),+1) =/> <goal --> reached>>! %1.00"
        );
    }    
    
    @Test public void testGraphPlan2() throws Exception {
        String input = "";
        input += "<C =/> <goal --> reached>>.\n";
        input += "<B =/> C>.\n";
        input += "<A =/> B>.\n";
        input += "<(&/,(^pick,X),+1) =/> A>.\n";    
        input += "<goal --> reached>!\n";
        testGraphPlan(input, "<(&/,(^pick,X),+1) =/> <goal --> reached>>! %1.00");
    }
    
    public void testGraphPlan(String input, String expected) throws IOException {
        NAR n = new DefaultNARBuilder().build();
        
        n.param().decisionThreshold.set(0.3f);
                
        //AtomicBoolean success = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(true);
        
        //System.out.println(input);

                /*new NWindow("Implication Graph", 
                            new ProcessingGraphPanel(n, 
                                    new ImplicationGraphCanvas(
                                            n.memory.executive.graph))).show(500, 500);*/
        
        new Output(n) {

            @Override
            public void event(Class channel, Object... args) {
                /*if (channel == OUT.class)
                    System.out.println("OUT: " + args[0]);*/
                
                /*
                try {
                    //System.out.println(o);
                    System.in.read();
                } catch (IOException ex) {
                    Logger.getLogger(GraphPlanTest.class.getName()).log(Level.SEVERE, null, ex);
                }*/
                
                if (args.length > 2) {
                    Object o = args[1];
                    if (o.toString().contains(expected))
                        success.set(true);
                }
            }
        
        };
        
        n.addInput(input);

        n.finish(185);
        
        //assertTrue(success.get());
        
//        new NARSwing(n);
        //n.start(100, 1);
        
        
    }
    

    public static void main(String[] args) throws Exception {
        new GraphPlanTest().testGraphPlan1Repeat();
    }
    
}
