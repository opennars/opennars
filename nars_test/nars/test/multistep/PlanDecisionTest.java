package nars.test.multistep;

import java.util.Arrays;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Output;
import nars.io.Texts;



public class PlanDecisionTest {
 
    public void decisionTest() {
        decision(0.9, 0.9);
        decision(0.5, 0.5);
        decision(0.9, 0.1);
        decision(0.1, 0.9);
        decision(0.1, 0.1);
        
        decision(0.54, 0.55);
       
    }
    
    public void decision(double... confidences) {
        String i = "";
    
        for (int n = 0; n < confidences.length; n++) {            
            i += "<<c" + n + " --> decision> =/> goal>." + "\n";
        }
        for (int n = 0; n < confidences.length; n++) {            
            i += "<(^pick,p" + n + ") =/> <c" + n + " --> decision>>." + "\n";
        }
        for (int n = 0; n < confidences.length; n++) {
            double c = confidences[n];
            i += "<c" + n + " --> decision>. :|: %1.00;" + Texts.n2((float)c) + "%\n";
        }

        i += "goal!\n";

        System.out.println(Arrays.toString(confidences));
        
        NAR n = new DefaultNARBuilder().build();
        n.addInput(i);
        //new TextOutput(n, System.out);
        
        n.addOutput(new Output() {            
            @Override public void output(Class c, Object o) {
                /*if (c == IN.class) {
                    System.out.println("IN: " + n.getTime() + ": " + o);
                }*/
                if (c == OUT.class) {
                    if (o instanceof Task) {
                        Sentence s = ((Task)o).sentence;
                        if (s.punctuation == '!') {
                            System.out.println(o);
                        }
                    }
                }
                if (c == EXE.class) {
                    System.out.println("EXE: " + n.getTime() + ": " + o);
                }
            }
        });
        
        n.finish(2000);
        
        System.out.println();
        
    }
}
