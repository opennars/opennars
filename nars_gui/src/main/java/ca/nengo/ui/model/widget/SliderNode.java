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


public class SliderNode extends AbstractNode implements UIBuilder {

    private SliderNodeUI ui;
    public final AtomicDouble value;
    protected double min;
    protected double max;
    protected Color barColor = null;
    protected Color barBgColor = Color.DARK_GRAY;
    protected Color backgroundColor = Color.BLACK;
    protected boolean dragging;
    protected NumberFormat nf = NumberFormat.getInstance();
    protected String prefix = "";
    String label = "";


    public SliderNode(String name, double val, double min, double max) {
        super(name);

        value = new AtomicDouble();
        this.min = min;
        this.max = max;
        setValue(val);


        setOutputs(new AtomicDoubleSource(this, name + " value", value));

//        setInputs(new TestSliderNode.AtomicDoubleTarget(this, name + " in", new AtomicDouble()) {
//            @Override
//            public void apply(Object v) throws SimulationException {
//                super.apply(v);
//                setValue(val.doubleValue());
//            }
//        });
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
        if (this.ui==null)
            this.ui = new SliderNodeUI();
        return ui;
    }

    private class SliderNodeUI extends UINeoNode {

        double marginPct = 0.05;
        private boolean hover = false;

        public SliderNodeUI() {
            super(SliderNode.this);

            EmptyIcon icon = new EmptyIcon(this);
            icon.setLabelVisible(false);
            setIcon(icon);

            setBounds(0, 0, 150, 50);

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
                                    updatePosition(pos.getX());
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

        public float value() { return value.floatValue(); }

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

            double p = (value.floatValue() - min) / (max-min);
            if (barColor == null) {
                //Green->Yellow->Red
                g.setColor(Color.getHSBColor( (1f - (float)p) / 3.0f , 0.2f, 0.9f));
                // g.setColor(Color.getHSBColor( (1f - p) / 3.0f , 0.2f, 0.8f + 0.15f));

            }
            else {
                g.setColor(barColor);
            }

            int wp = (int)(((float)w) * p );
            final int barWidth = Math.max(0, wp - m*2);
            final int barHeight = h-m*2;
            g.fillRect(m, m, barWidth, barHeight);

            g.setColor(barBgColor);
            g.fillRect(m + barWidth, m, w-m*2 - barWidth, barHeight);

            g.setXORMode(Color.BLACK);
            g.drawString(getText(), m*4, h/2);
            g.setPaintMode();

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


        protected void updatePosition(double x) {
            double m = getMargin();
            double p = (x - m) / (getWidth() - m * 2);
            double v = p * (max-min) + min;
            v = Math.max(v, min);
            v = Math.min(v, max);
            setValue(v);
            repaint();
        }

        @Override
        public String getTypeName() {
            return "SliderNode";
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getValue() { return value.doubleValue(); }

    public void setValue(double v) {
        if (v != value.doubleValue()) {
            value.set( v );

            label = prefix + " " + nf.format(value.floatValue());

            onChange(v);
        }
    }


    public void onChange(double v) {
        if (ui!=null)
            ui.repaint();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMax(float max) {
        this.max = max;
    }

}
