/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.demo.spacegraph;

import automenta.spacegraph.shape.Rect;
import automenta.spacegraph.ui.GridRect;
import automenta.spacegraph.ui.PointerLayer;
import automenta.vivisect.swing.NWindow;

/**
 *
 * @author seh
 */
public class DemoBlend extends AbstractSurfaceDemo {

    @Override
    public String getName() {
        return "2D Fractal Surface";
    }

    @Override
    public String getDescription() {
        return "Zoomable fractal 2D surface.  Multiple adjustable layers.  Adjustable control logic.";
    }

    public DemoBlend() {
        super();

        add(new GridRect(6, 6));

        /* add rectangles, testing:
        --position
        --size
        --color
        --tilt
         */
        int numRectangles = 32;
        float maxRadius = 0.1f;
        float r = 4.0f;
        float w = 1.5f;
        float h = 0.75f;
        for (int i = 0; i < numRectangles; i++) {
            float s = 1.0f + (float) Math.random() * maxRadius;
            float a = (float) i / 2.0f;
            float x = ((float) Math.random() - 0.5f) * r ;
            float y = ((float) Math.random() - 0.5f) * r ;

            float red = (float)Math.random();
            float green = (float)Math.random();
            float blue = (float)Math.random();

            Rect r1 = new Rect().color(red, green, blue, 0.5f);
            r1.center(x, y, 0);
            r1.scale(w, h);

            add(r1);

        }

        add(new PointerLayer(this));
    }

    public static void main(String[] args) {
        new NWindow(AbstractSurfaceDemo.newPanel(new DemoBlend()), 800, 800, true);
    }
}
