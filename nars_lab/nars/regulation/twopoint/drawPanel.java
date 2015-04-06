/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.regulation.twopoint;
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import nars.core.EventEmitter;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
 
/**
 *
 * @author patrick.hammer
 */
public class drawPanel extends JPanel {
 
    final int feedbackCycles = 50;
    private long minCyclesPerMovement = 50;
    private long lastMovementAt = -minCyclesPerMovement-1;
    
    int movement = 0;
    int lastMovement = 0;
 
    public class move extends Operator {
 
        public move() {
            super("^move");
        }
 
        @Override
        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
            if (args.length == 2) { //left, self
                
                long now = nar.time();
                if (now - lastMovementAt < minCyclesPerMovement) {
                    moving();
                    return null;
                }
                
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
                lastMovementAt = now;
                
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
        //nar.addInput("move(left)! :|:");       
    }
    
    NAR nar;
 
    public drawPanel() {
        Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF = false;
        nar = new Default().build();
        //nar.param.decisionThreshold.set(0.1f);
 
        nar.addPlugin(new move());
        
        nar.on(CycleEnd.class, new EventEmitter.EventObserver() {
 
            @Override
            public void event(Class event, Object[] args) {
                boolean hasMoved = (movement != lastMovement);
                lastMovement = movement;
 
                if (hasMoved || nar.time() % feedbackCycles == 0) {
                    if (x == setpoint)
                        good();
                    else
                        bad();
                }
                
                if (hasMoved) {
                    
                    beGoodNow();
 
                    if (x > setpoint)
                        target("left");                    
                    else if (x < setpoint)
                        target("right");                    
                    else
                        target("here");
                    
                }
                
                repaint();
            }
        });
 
        NARSwing.themeInvert();
        new NARSwing(nar);
        
        
        beGood();
 
        
        train();
 
    }
 
    int setpoint = 80;
    int x = 160;
    int y = 10;
 
    protected void train() {
        //nar.addInput("move(left). :|: %0.00;0.99%");
        // nar.addInput("move(right). :|: %0.00;0.99%");
        
        
        long trainDelayCycles = minCyclesPerMovement * 2;
        nar.addInput("move(left)! :|:\n" + trainDelayCycles + "\n" + "move(right)! :|:");
    }
    
    private void doDrawing(Graphics g) {
        
 
        //nar.step(10);
        Graphics2D g2d = (Graphics2D) g;
 
        g2d.setColor(Color.blue);
        g2d.fillOval(x, y, 10, 10);
        g2d.setColor(Color.red);
        g2d.fillOval(setpoint, y, 10, 10);
 
        /*for (int i = 0; i <= 1000; i++) {

         Dimension size = getSize();
         Insets insets = getInsets();

         int w = size.width - insets.left - insets.right;
         int h = size.height - insets.top - insets.bottom;

         Random r = new Random();
         int x = Math.abs(r.nextInt()) % w;
         int y = Math.abs(r.nextInt()) % h;
         g2d.drawLine(x, y, x, y);
         }*/
    }
 
    @Override
    public void paintComponent(Graphics g) {
 
        super.paintComponent(g);
        doDrawing(g);
    }
}