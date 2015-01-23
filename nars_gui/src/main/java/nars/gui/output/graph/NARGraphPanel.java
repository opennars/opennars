/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph;

import automenta.vivisect.swing.NPanel;
import automenta.vivisect.swing.NSlider;
import automenta.vivisect.swing.PCanvas;
import nars.core.Events;
import nars.core.NAR;
import nars.gui.WrapLayout;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author me
 */
public class NARGraphPanel extends NPanel {
    private final NARGraphVis vis;
    private final PCanvas canvas;
    private final JPanel visControl, layoutControl, canvasControl;
    private final JComponent menu;
    private final JPanel graphControl;

    float paintFPS = 30f;
    float updateFPS = 10f;
    long lastUpdateMS = -1;
    
    public NARGraphPanel(NAR n) {
        super(new BorderLayout());
        



        vis = new NARGraphVis(n) {
            @Override public void setMode(NARGraphVis.GraphMode g) {
                super.setMode(g);
                doLayout();
                updateUI();
            }

            @Override
            public void event(Class event, Object[] args) {
                super.event(event, args);

                long updateCycleFPS = (long)(1000f / updateFPS);
                if (event == Events.FrameEnd.class) {
                    if ((canvas!=null) && (isVisible())) {
                        long now = System.currentTimeMillis();
                        if (now - lastUpdateMS > updateCycleFPS) {
                            canvas.predraw();
                            canvas.redraw();
                            lastUpdateMS = now;
                        }
                    }
                }
            }
        };
        canvas = new PCanvas(vis);
        canvas.setFrameRate(paintFPS);
        canvas.loop();
        canvas.renderEveryFrame(true);

        visControl = vis.newStylePanel();
        canvasControl = newCanvasPanel();
        layoutControl = vis.newLayoutPanel();
        graphControl = vis.newGraphPanel();
        
        
        menu = new JPanel(new WrapLayout(FlowLayout.LEFT));
        menu.setOpaque(false);
        menu.add(graphControl);
        menu.add(visControl);
        menu.add(canvasControl);
        menu.add(layoutControl);
        
        add(canvas, BorderLayout.CENTER);
        add(menu, BorderLayout.NORTH);
        
    }

    
    @Override  protected void onShowing(boolean showing) {
        canvas.predraw();
    }
    
    
    protected JPanel newCanvasPanel() {
        JPanel m = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        NSlider blur = new NSlider(0, 0, 1.0f) {
            @Override
            public void onChange(float v) {
                canvas.setMotionBlur(v);
            }
        };
        blur.setPrefix("Blur: ");
        blur.setPreferredSize(new Dimension(60, 25));
        m.add(blur);

        return m;
    }
}
