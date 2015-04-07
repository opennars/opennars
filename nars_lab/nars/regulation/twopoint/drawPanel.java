/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.regulation.twopoint;
 
//TODO: Integrate ideas from [23:39] <sseehh_> patham9,  here's code https://gist.github.com/automenta/569bd8694a789a5d9490 i'm done for now

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import nars.core.EventEmitter;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.NAR.PluginState;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.plugin.mental.InternalExperience;
 
/**
 *
 * @author patrick.hammer
 */
public class drawPanel extends JPanel {
 
    final int feedbackCycles = 100;
    
    int movement = 0;
    int lastMovement = 0;
 
    public class move extends Operator {
 
        public move() {
            super("^move");
        }
 
        @Override
        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {

            if (args.length == 2 || args.length==3) { //left, self
                prevy.add(y);
                prevx.add(x);
                long now = nar.time();
               /* if (now - lastMovementAt < minCyclesPerMovement) {
                    moving();
                    return null;
                }*/
                
                if (args[0].toString().equals("left")) {
                    x -= 10;
                    if (x < setpoint) {
                        System.out.println("BAD:\n" + operation.getTask().getExplanation());
                        bad();
                    } else {
                        good();
                    }
                }
                if (args[0].toString().equals("right")) {
                    x += 10;
                    if (x > setpoint) {
                        System.out.println("BAD:\n" + operation.getTask().getExplanation());
                        bad();
                    } else {
                        good();
                    }
                }
                
                movement++;
               // lastMovementAt = now;
                
            }
            return null;
        }
    }
    
    public void beGood() {
        nar.addInput("<SELF --> [good]>!");
    }
    
    public void moving() {
        nar.addInput("<SELF --> [moving]>. :|:");
    }
    
    public void beGoodNow() {
        nar.addInput("<SELF --> [good]>! :|:");
    }
 
    public void good() {
        nar.addInput("<SELF --> [good]>. :|: %1.00;0.90%");
    }
    
    public void bad() {
        nar.addInput("<SELF --> [good]>. :|: %0.00;0.90%");
    }
 
    public void target(String direction) {
        nar.addInput("<target --> " + direction + ">. :|:");      
    }
    
    NAR nar;
 
    public drawPanel() {
     //   Parameters.TEMPORAL_INDUCTION_SAMPLES=0;
     //   Parameters.DERIVATION_DURABILITY_LEAK=0.1f;
       // Parameters.DERIVATION_PRIORITY_LEAK=0.1f;
       // Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF = false;
        nar = new Default().build();
 
        nar.addPlugin(new move());
        
        int k=0;
        for(PluginState s: nar.getPlugins()) {
            if(s.plugin instanceof InternalExperience) {
                nar.removePlugin(s);
                break;
            }
            k++;
        }
        
        nar.on(CycleEnd.class, new EventEmitter.EventObserver() {
 
            @Override
            public void event(Class event, Object[] args) {
                boolean hasMoved = (movement != lastMovement);
                lastMovement = movement;
 
                if(nar.time()%100==0) {
                    y+=1;
                    prevy.add(y);
                    prevx.add(x);
                }
                
                if (hasMoved || nar.time() % feedbackCycles == 0) {
                    if (x == setpoint)
                        good();
                }
                
                if(nar.time()%1000==0) {
                    if (x > setpoint)
                        target("left");                    
                    else if (x < setpoint)
                        target("right");                    
                    else
                        target("here");
                    
                }
                if(nar.time()%10000==0) {
                    beGood();
                }
                
               /* if (hasMoved) {
                    
                }*/
                
                repaint();
            }
        });
 
        NARSwing.themeInvert();
        new NARSwing(nar);
 
        
        intialDesire();
 
    }
 
    static int setpoint = 80; //80 230
    int x = 160;
    int y = 10;
 
    protected void intialDesire() {
        nar.addInput("move(left)! :|: %1.00;0.65%");
        nar.addInput("move(right)! :|: %1.00;0.65%");
    }
    
    List<Integer> prevx=new ArrayList<>();
    List<Integer> prevy=new ArrayList<>();
    
    private void doDrawing(Graphics g) {
        
 
        //nar.step(10);
        Graphics2D g2d = (Graphics2D) g;
 
        g2d.setColor(Color.blue);
        for(int i=0;i<prevx.size();i++) {
            g2d.fillRect(prevy.get(i), prevx.get(i), 10, 10);
        }
        
        g2d.fillRect(y, x, 10, 10);
        g2d.setColor(Color.red);
        g2d.drawLine(0, setpoint+5, 2000, setpoint+5);
    }
 
    @Override
    public void paintComponent(Graphics g) {
 
        super.paintComponent(g);
        doDrawing(g);
    }
}