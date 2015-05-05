package ca.nengo.ui.model.widget;

import ca.nengo.model.AtomicDoubleSource;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.EmptyIcon;
import ca.nengo.util.ScriptGenException;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventListener;
import com.google.common.util.concurrent.AtomicDouble;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.HashMap;


public class PadNode extends AbstractNode implements UIBuilder {

    private SliderNodeUI ui;
    public final AtomicDouble x, y;
    protected double bounds[][];
    protected Color barColor = null;
    protected Color barBgColor = Color.DARK_GRAY;
    protected Color backgroundColor = Color.BLACK;
    protected boolean dragging;
    protected NumberFormat nf = NumberFormat.getInstance();
    protected String prefix = "";
    String label = "";
    private Color knobColor = Color.ORANGE;


    public PadNode(String name, double _x, double minX, double maxX, double _y, double minY, double maxY) {
        super(name);

        bounds = new double[][] {  { minX, maxX}, { minY, maxY} };

        x = new AtomicDouble(_x);
        y = new AtomicDouble(_y);

        setOutputs(
                new AtomicDoubleSource(this, name + " X", x),
                new AtomicDoubleSource(this, name + " Y", y)
        );
    }



    @Override
    public void run(float startTime, float endTime) throws SimulationException {

    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return null;
    }

    @Override
    public void reset(boolean randomize) {

    }

    @Override
    public UINeoNode newUI(double width, double height) {
        if (this.ui == null)
            this.ui = new SliderNodeUI();
        return ui;
    }

    private class SliderNodeUI extends UINeoNode {

        double marginPct = 0.05;
        private boolean hover = false;

        public SliderNodeUI() {
            super(PadNode.this);

            EmptyIcon icon = new EmptyIcon(this);
            icon.setLabelVisible(false);
            setIcon(icon);

            setBounds(0, 0, 150, 150);

            setSelected(true);

            nf.setMaximumFractionDigits(3);

            addInputEventListener(new PInputEventListener() {
                @Override
                public void processEvent(PInputEvent pInputEvent, int i) {
                    if (pInputEvent.isMouseEvent()) {
                        Point2D pos = pInputEvent.getPositionRelativeTo(getPNode());
                        double y = pos.getY();
                        double x = pos.getX();
                        double margin = getMargin();
                        boolean insideComponent = (y > 0 && x > 0 && x< getWidth() && y < getHeight());
                        boolean insideSlider = insideComponent && ((y > margin && y < getHeight() - margin && x > margin && x < getWidth()-margin));


                        if (insideComponent && pInputEvent.isLeftMouseButton()) {

                            if (insideSlider) {
                                if ((pInputEvent.getSourceSwingEvent().getModifiers() & MouseEvent.MOUSE_PRESSED) > 0) {
                                    updatePosition(x, y);
                                    pInputEvent.setHandled(true);
                                }
                            }

                            setHover(insideComponent);

                            repaint();
                        }


                    }
                }
            });
        }

        private void setHover(boolean mouseEnteredOrMouseExited) {
            if (this.hover != mouseEnteredOrMouseExited) {
                this.hover = mouseEnteredOrMouseExited;
                repaint();
            }
        }

        public float x() { return x.floatValue(); }
        public float y() { return y.floatValue(); }

        @Override
        public void paint(ca.nengo.ui.lib.world.PaintContext paintContext) {
            paint(paintContext.getGraphics());
        }

        double getMargin() {
            int w = (int)getWidth();
            int h = (int)getHeight();
            return Math.max(w, h) * marginPct;
        }

        public void paint(Graphics g) {
            int w = (int)getWidth();
            int h = (int)getHeight();
            int m = (int)getMargin();

            g.setPaintMode();
            g.setColor(backgroundColor);
            g.fillRect(0, 0, w, h);

            double px = (x() - bounds[0][0]) / (bounds[0][1] - bounds[0][0]);
            double py = (y() - bounds[1][0]) / (bounds[1][1] - bounds[1][0]);
            /*
            if (barColor == null) {
                //Green->Yellow->Red
                g.setColor(Color.getHSBColor( (1f - (float)p) / 3.0f , 0.2f, 0.9f));
                // g.setColor(Color.getHSBColor( (1f - p) / 3.0f , 0.2f, 0.8f + 0.15f));

            }
            else {
                g.setColor(barColor);
            }
            */

            double ww = getWidth() - m * 2;
            double hh = getHeight() - m * 2;
            int thick = m;// (int)(0.1f * ww);


            g.setColor(barBgColor);
            g.fillRect(m, m, (int)ww, (int)hh);

            int wpx = (int)(((float)ww) * px )+thick;
            int wpy = (int)(((float)hh) * py )+thick;
            g.setColor(knobColor);
            g.fillRect(m, wpy-thick/2, (int)ww, thick);
            g.fillRect(wpx-thick/2, m, thick, (int)hh);



            if (hover) {
                g.setColor(Color.GRAY);
                g.drawRect(m-1, m-1, w-m*2+1, h-m*2+1);
            }
        }

        public void onValueUpdated() {
            repaint();
        }

        public String getText() {
            return label;
        }


        protected void updatePosition(double px, double py) {
            //TODO
            double m = getMargin();
            double ppx = (px - m) / (getWidth() - m * 2);
            double vx = ppx * (bounds[0][1]-bounds[0][0]) + bounds[0][0];
            vx = Math.max(vx, bounds[0][0]);
            vx = Math.min(vx, bounds[0][1]);
            double ppy = (py - m) / (getHeight() - m * 2);
            double vy = ppy * (bounds[1][1]-bounds[1][0]) + bounds[1][0];
            vy = Math.max(vy, bounds[1][0]);
            vy = Math.min(vy, bounds[1][1]);

            x.set(vx);
            y.set(vy);
            repaint();
        }

        @Override
        public String getTypeName() {
            return "PadNode";
        }
    }



    public double x() { return x.doubleValue(); }
    public double y() { return y.doubleValue(); }

//    public void setValue(double v) {
//        if (v != value.doubleValue()) {
//            value.set( v );
//
//            label = prefix + " " + nf.format(value.floatValue());
//
//            onChange(v);
//        }
//    }


    public void onChange(double v) {
        if (ui!=null)
            ui.repaint();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
