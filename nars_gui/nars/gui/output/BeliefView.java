package nars.gui.output;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;
import processing.core.PVector;

/**
 * 2D scatter plot of belief frequency/certainty
 */
public class BeliefView extends PPanel {

    public BeliefView() {
        super();
    }


    public void setup() {
        super.setup();
        
    }

    
// Draws the chart and a title.
    public void draw() {
//        background(0);
//        textSize(9);
//        
//        fill(240f);
//        lineChart.draw(15, 15, width - 30, height - 30);
//
//        // Draw a title over the top of the chart.
//        
//        textSize(16);        
//        text(title, 40, 30);
//        /*textSize(11);
//        text("Gross domestic product measured in inflation-corrected $US",
//                70, 45);*/
        
    }


    /*
    public static void main(String[] args) {
        PLineChart p = new PLineChart("Average",10);
        p.addPoint(2,2);
        p.addPoint(4,4);
        
        Window w = new Window("", p.newPanel());
        
        
        
        w.setSize(400,400);
        w.setVisible(true);
    }
    */

}

