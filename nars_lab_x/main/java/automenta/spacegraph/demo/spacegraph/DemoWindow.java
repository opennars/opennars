/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.demo.spacegraph;

import automenta.spacegraph.shape.Curve;
import automenta.spacegraph.ui.GridRect;
import automenta.spacegraph.ui.PointerLayer;
import automenta.spacegraph.ui.Window;
import automenta.vivisect.swing.NWindow;

/**
 *
 * @author seh
 */
public class DemoWindow extends AbstractSurfaceDemo {

    @Override
    public String getName() {
        return "Window";
    }

    @Override
    public String getDescription() {
        return "Window";
    }
    
    public DemoWindow() {
        super();

        
        
        add(new GridRect(6, 6));

        Window w1 = new Window();
        w1.scale(4, 3).center(1, 1, 0);
        
        Window w2 = new Window();
        w2.scale(2, 1).center(-1, -1, 0);
        
        Curve c = new Curve(w1, w2, 4, 4);
        c.setLineWidth(6);
        c.setColor(0.5f, 0.5f, 0.5f);

        add(c);
        add(w1);
        add(w2);
                
//        new RectLayout(this).withRectInScale(w1, new Panel(), -0.25f, -0.25f, 0.25f, 0.25f);
//        new RectLayout(this).withRectInScale(w1, new Panel(), -0.25f, 0.25f, 0.25f, 0.25f);
//        new RectLayout(this).withRectInScale(w1, new Panel(), 0.25f, 0.25f, 0.25f, 0.25f);
        
        add(new PointerLayer(this));

    }

    public static void main(String[] args) {
        new NWindow("DemoWindow", AbstractSurfaceDemo.newPanel(new DemoWindow())).show(800,800);
    }
}
