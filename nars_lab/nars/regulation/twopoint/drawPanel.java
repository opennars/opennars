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
                    } else {
                        nar.addInput("<SELF --> [good]>. :|: %0.00;0.90%");
                    }
                }
                if(args[0].toString().equals("right")) {
                    x+=10;
                    if(x>setpoint) {
                        nar.addInput("<SELF --> [good]>. :|: %0.00;0.90%");
                    } else {
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
            
    int setpoint=100;
    int x=160;
    int y=10;
    int k=0;
    private void doDrawing(Graphics g) {
        int modu=10;
        boolean cond = (inc!=lastinc);
        lastinc=inc;
        if(k<1) {
          //nar.addInput("move(left). :|: %0.00;0.99%");
         // nar.addInput("move(right). :|: %0.00;0.99%");
            nar.addInput("move(right)! :|:");
            nar.addInput("move(left)! :|:");
            nar.addInput("move(right)! :|:");
        }
        if((cond || k%50==0) && x==setpoint) {
                nar.addInput("<SELF --> [good]>. :|: %1.00;0.90%");
        }
        if(cond) {
            System.out.println(x);
            
            if(cond) {
                nar.addInput("<SELF --> [good]>! :|:");
            }
            

            if(x>setpoint) {
                nar.addInput("<target --> left>. :|:");
                //nar.addInput("move(left)! :|:");
            }
            if(x<setpoint) {
                nar.addInput("<target --> right>. :|:");
               // nar.addInput("move(right)! :|:");
            }
        }
        k++;
        
        
        
        nar.step(100);
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