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
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 *
 * @author patrick.hammer
 */
public class drawPanel extends JPanel {

    int inc=0;
    int lastinc=0;
    public class move extends Operator {

        public move() {
            super("^move");        
        }
        
        @Override
        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
            if(args.length==2) { //left, self
                inc++;
                if(args[0].toString().equals("left")) {
                    x-=10;
                    if(x>setpoint) {
                        nar.addInput("<SELF --> [good]>. :|: %1.00;0.90%");
                    } else if (x<setpoint) {
                        nar.addInput("<SELF --> [good]>. :|: %0.00;0.90%");
                    }
                }
                if(args[0].toString().equals("right")) {
                    x+=10;
                    if(x>setpoint) {
                        nar.addInput("<SELF --> [good]>. :|: %0.00;0.90%");
                    } else if (x<setpoint)  {
                        nar.addInput("<SELF --> [good]>. :|: %1.00;0.90%");
                    }
                }
            }
            return null;
        }
    }
    
    NAR nar;
    public drawPanel() {
        Parameters.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF=false;
        nar=new Default().build();
        nar.addPlugin(new move());
        //new NARSwing(nar);
        nar.addInput("<SELF --> [good]>!");
        new javax.swing.Timer(30, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        }).start();
    }
            
    int setpoint=80;
    int x=160;
    int y=10;
    int k=0;
    private void doDrawing(Graphics g) {
        
        if(k<1) {
            nar.addInput("move(left)! :|:");
            nar.addInput("move(right)! :|:");
            nar.addInput("move(left)! :|:");
            nar.addInput("move(right)! :|:");
        }
        for(int u=0;u<100;u++) {
            boolean cond = (inc!=lastinc);
            lastinc=inc;
            
            if((cond || k%50==0) && x==setpoint) {
                    nar.addInput("<SELF --> [good]>. :|: %1.00;0.90%");
            }
            if(cond) {
                System.out.println(x);

                nar.addInput("<SELF --> [good]>! :|:");

                if(x>setpoint) {
                    nar.addInput("<target --> left>. :|:");
                }
                if(x<setpoint) {
                    nar.addInput("<target --> right>. :|:");
                }
            }

            nar.step(1);
        }
        k++;
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.blue);
        g2d.fillOval(x, y, 10, 10);
        g2d.setColor(Color.red);
        g2d.fillOval(setpoint, y, 10, 10);
    }

    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        doDrawing(g);
    }
}