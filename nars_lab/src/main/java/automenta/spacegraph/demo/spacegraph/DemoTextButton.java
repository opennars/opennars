/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.demo.spacegraph;

import automenta.spacegraph.ui.GridRect;
import automenta.spacegraph.ui.PointerLayer;
import automenta.spacegraph.ui.TextButton;
import automenta.vivisect.swing.NWindow;

/**
 *
 * @author seh
 */
public class DemoTextButton extends AbstractSurfaceDemo {

    @Override
    public String getName() {
        return "Demo Button";
    }

    @Override
    public String getDescription() {
        return "Demo Button";
    }

    public DemoTextButton() {
        super();

        add(new GridRect(6, 6));

        int w = 1;
        int h = 1;
        for (int x = -w; x <= w; x++) {
            for (int y = -h; y <= h; y++) {
                add(new TextButton("Abc").scale(0.9f, 0.9f).center(x, y));
            }            
        }


        add(new PointerLayer(this));
    }

    public static void main(String[] args) {
        new NWindow(AbstractSurfaceDemo.newPanel(new DemoTextButton()), 800, 800, true);
    }
}
