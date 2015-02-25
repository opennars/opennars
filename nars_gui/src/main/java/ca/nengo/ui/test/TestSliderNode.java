package ca.nengo.ui.test;

import ca.nengo.math.impl.GaussianPDF;
import ca.nengo.model.Node;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectSource;
import ca.nengo.model.impl.ObjectTarget;
import ca.nengo.neural.neuron.impl.GruberNeuronFactory;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.models.UIBuilder;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.icons.EmptyIcon;
import ca.nengo.ui.models.nodes.UINetwork;
import ca.nengo.util.ScriptGenException;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventListener;
import reactor.jarjar.jsr166e.extra.AtomicDouble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.HashMap;


public class TestSliderNode extends Nengrow {

    long time = 0;


    @Override
    public void init() throws Exception {
        NetworkImpl network = new NetworkImpl();

        network.addNode( new SliderNode("Slide", 0.25f, 0, 1f));
        network.addNode( new SpikingNeuron(null, null, 1, 0.5f, "B"));
        network.addNode( new GruberNeuronFactory(new GaussianPDF(), new GaussianPDF()).make("x"));


        UINetwork networkUI = (UINetwork) addNodeModel(network);

        networkUI.doubleClicked();

        network.run(0,0);


        new Timer(10, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = 0.001f; //myStepSize
                    network.run(time, time+dt);
                    time += dt;
                } catch (SimulationException e1) {
                    e1.printStackTrace();
                }
                //cycle();
            }
        }).start();

    }

    public static class AtomicDoubleSource extends ObjectSource<Double> {

        private final AtomicDouble val;

        public AtomicDoubleSource(Node parent, String name, AtomicDouble d) {
            super(parent, name);
            this.val = d;
        }

        @Override
        public Double get() {
            return val.doubleValue();
        }
    }

    public static class AtomicDoubleTarget extends ObjectTarget {

        protected final AtomicDouble val;

        public AtomicDoubleTarget(Node parent, String name, AtomicDouble d) {
            super(parent, name, Object.class);
            this.val = d;
        }

        @Override
        public boolean applies(Object value) {
            if (value instanceof Number) return true;
            if (value instanceof RealOutput) {
                return ((RealOutput)value).getDimension() > 0;
            }
            return false;
        }

        @Override
        public void apply(Object v) throws SimulationException {
            if (v instanceof Number) {
                val.set(((Number)v).doubleValue());
            }
            else if (v instanceof RealOutput) {
                val.set(((RealOutput)v).getValues()[0]);
            }
        }


    }

    public static class SliderNode extends AbstractNode implements UIBuilder {

        private SliderNodeUI ui;
        public final AtomicDouble value;
        protected double min;
        protected double max;
        protected Color barColor = null;
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

            setInputs(new AtomicDoubleTarget(this, name + " in", new AtomicDouble()) {
                @Override
                public void apply(Object v) throws SimulationException {
                    super.apply(v);
                    setValue(val.doubleValue());
                }
            });
        }



        @Override
        public void run(float startTime, float endTime) throws SimulationException {

        }

        @Override
        public Node[] getChildren() {
            return new Node[0];
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return null;
        }

        @Override
        public void reset(boolean randomize) {

        }

        @Override
        public UINeoNode newUI() {
            this.ui = new SliderNodeUI();
            return ui;
        }

        private class SliderNodeUI extends UINeoNode {

            double marginPct = 0.05;
            private boolean hover = false;

            public SliderNodeUI() {
                super(SliderNode.this);


                setIcon(new EmptyIcon(this));
                setBounds(0, 0, 150, 50);

                setSelected(true);



                nf.setMaximumFractionDigits(3);

                //setBorder(new LineBorder(Color.GRAY));

                //addMouseListener(this);
                //addMouseMotionListener(this);

                //setFont(defaultLabelFont);


                addInputEventListener(new PInputEventListener() {
                    @Override
                    public void processEvent(PInputEvent pInputEvent, int i) {
                        if (pInputEvent.isMouseEvent()) {
                            Point2D pos = pInputEvent.getPositionRelativeTo(SliderNodeUI.this.getPiccolo());
                            double y = pos.getY();
                            double x = pos.getX();
                            double margin = getMargin();
                            boolean insideComponent = (y > 0 && x > 0 && x< getWidth() && y < getHeight());
                            boolean insideSlider = insideComponent && ((y > margin && y < getHeight() - margin && x > margin && x < getWidth()-margin));

                            if (pInputEvent.isLeftMouseButton()) {

                                if ((pInputEvent.getSourceSwingEvent().getModifiers() & MouseEvent.MOUSE_PRESSED) > 0) {
                                    if (insideSlider) {
                                        updatePosition(pos.getX());
                                        pInputEvent.setHandled(true);
                                    }
                                }

                                repaint();
                            }

                            setHover(insideComponent);
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
            public void paint(PaintContext paintContext) {
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

                g.setColor(barColor);
                g.fillRect(m, m, wp-m*2, h-m*2);

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







//            @Override
//            public void mouseClicked(MouseEvent e) {
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                dragging = true;
//
//                updatePosition(e.getX());
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                updatePosition(e.getX());
//                dragging = false;
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                updatePosition(e.getX());
//            }
//
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                if (dragging) {
//                    updatePosition(e.getX());
//                }
//            }
//


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



    public static void main(String[] args) {
        new TestSliderNode();
    }
}
