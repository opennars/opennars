/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.demo.spacegraph;

import automenta.spacegraph.Surface;
import automenta.spacegraph.control.FractalControl;
import automenta.spacegraph.control.Pointer;
import automenta.spacegraph.control.Touchable;
import automenta.spacegraph.demo.Demo;
import automenta.spacegraph.math.linalg.Vec2f;
import automenta.spacegraph.shape.Drawable;
import automenta.spacegraph.video.SGPanel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author me
 */
abstract public class AbstractSurfaceDemo extends Surface implements Demo {

    public AbstractSurfaceDemo() {
        super();
    }

    
    public static JPanel newPanel(Surface space) {
        JPanel j = new JPanel(new BorderLayout());
        {
            SGPanel sdc = new SGPanel(space);

            new FractalControl(sdc);

            final ControlRigPanel crp = new ControlRigPanel(space, 0.25f);
            new Thread(crp).start();

            j.add(sdc, BorderLayout.CENTER);
            j.add(crp, BorderLayout.SOUTH);
        }
        return j;
    }

    @Override
    public JPanel newPanel() {
        JPanel j = new JPanel(new BorderLayout());
        {
            DemoRectTilt dc = new DemoRectTilt();
            SGPanel sdc = new SGPanel(dc);

            new FractalControl(sdc);

            final ControlRigPanel crp = new ControlRigPanel(dc, 0.25f);
            new Thread(crp).start();

            j.add(sdc, BorderLayout.CENTER);
            j.add(crp, BorderLayout.SOUTH);
        }
        return j;
    }

    @Override
    protected synchronized void handleTouch(Pointer p) {
        super.handleTouch(p);
        Set<Touchable> touchingNow = new HashSet();
        final Vec2f v = new Vec2f(p.world.x(), p.world.y());
        
        synchronized (getSpace().getDrawables()) {
            for (Drawable d : getSpace().getDrawables()) {
                if (d instanceof Touchable) {
                    Touchable t = (Touchable) d;
                    if (t.isTouchable()) {
                        if (t.intersects(v)) {
                            touchingNow.add(t);
                        }
                    }
                }
            }
        }
        for (Touchable t : touchingNow) {
            if (!p.touching.contains(t)) {
                t.onTouchChange(p, true);
                p.touching.add(t);
            }
        }
        List<Touchable> toRemove = new LinkedList();
        for (Touchable t : p.touching) {
            if (!touchingNow.contains(t)) {
                t.onTouchChange(p, false);
                toRemove.add(t);
            } else {
                t.onTouchChange(p, true);
            }
        }
        for (Touchable t : toRemove) {
            p.touching.remove(t);
        }
    }

    public static class ControlRigPanel extends JPanel implements Runnable {

        private final JTextField pixelPointerText;
        private final JTextField worldPointerText;
        private final Surface surface;
        private final JTextField camText;
        private final long delay;

        public ControlRigPanel(Surface surface, float updateTime) {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            this.delay = (long) (updateTime * 1000.0f);
            this.surface = surface;

            pixelPointerText = new JTextField();
            add(pixelPointerText);
            worldPointerText = new JTextField();
            add(worldPointerText);
            camText = new JTextField();
            add(camText);

            update();
        }

        public void update() {
            pixelPointerText.setText("Pointer Pixel: " + surface.getPointer().pixel.toString());
            worldPointerText.setText("Pointer World: " + surface.getPointer().world.toString());
            camText.setText("Cam: " + surface.getCamera().camPos);
        }

        @Override
        public void run() {
            while (true) {
                update();

                try {
                    Thread.sleep(delay);

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }
}
