/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

import jurls.core.utils.MatrixImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 *
 * @author me
 */
abstract class IOMatrixExample extends JPanel implements Runnable {

    private final MatrixImage inputImage;
    private final MatrixImage outputImage;
    private double[] input;
    private double[] output;
    
    static final int padding = 4;
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public IOMatrixExample() {
        super(new GridLayout(0, 1, padding, padding));
        
        setBorder(new EmptyBorder(padding,padding,padding,padding));
        
        add(inputImage = new MatrixImage(400, 50));
        add(outputImage = new MatrixImage(400, 50));
    }
    
    @Override public void run() {
        int cycle = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
            
            output = update(cycle, input = input(cycle));            
            cycle++;
            
            inputImage.draw(input, 0, 1.0, false);
            outputImage.draw(output, 0, 1.0, false);
            
            try {
                Thread.sleep(getDelayMS());
            } catch (InterruptedException ex) {
            }
        }
    }

    
    public abstract long getDelayMS();
    
    public abstract double[] input(int cycle);
    public abstract double[] update(int cycle, double[] i);

    

    public void newWindow(int w, int h) {
        JFrame j = new JFrame("");
        j.getContentPane().add(this);
        j.setSize(w, h);
        j.setVisible(true);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }    
}
