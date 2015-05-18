package ca.nengo.ui.model.plot;


import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.AtomicDoubleSource;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.RealSource;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.ObjectTarget;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.primitive.PXPath;
import ca.nengo.ui.model.widget.UITarget;
import ca.nengo.util.ScriptGenException;
import nars.Global;
import nars.io.Texts;
import org.apache.commons.math3.util.FastMath;
import org.piccolo2d.extras.nodes.PNodeCache;
import org.piccolo2d.util.PPaintContext;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;

public class LinePlot extends AbstractWidget {

    //public static final ColorArray grayScale = new ColorArray(16, Color.GRAY, Color.WHITE);
    public static final ColorArray BlueRed = new ColorArray(128, Color.BLUE, Color.RED);
    private final PXPath plotSurface;
    private final PNodeCache plotSurfaceWrap;


    long minRepaintPeriod = 150; //milliseconds
    long lastRepaint = 0;

    private String label = "?";

    final Deque<Double> history = new ArrayDeque<>(); //TODO use seomthing more efficient

    final int maxHistory = Global.METRICS_HISTORY_LENGTH;
    double[] hv = new double[maxHistory]; //values are cached here for fast access

    private boolean changed = true;

    double minValue, maxValue;
    private ObjectTarget input;




    @Override
    protected void paint(PaintContext paintContext, double width, double height) {


    }



    public LinePlot(String name) {
        this(name, 64, 64);
    }

    public LinePlot(String name, double width, double height) {
        super(name, width, height);

        input = new ObjectTarget(this, "_input", Object.class);
        //setInputs(input);


        ui.addWidget(UITarget.createTerminationUI(ui, input));


        Rectangle2D.Float rect = new Rectangle2D.Float();
        rect.setFrame(0, 0, width, height);
        plotSurface = new PXPath(rect) {
            @Override
            protected void paint(PPaintContext paintContext) {
                //super.paint(paintContext);

                if (changed) {
                    changed = false;
                    minValue = Float.POSITIVE_INFINITY;
                    maxValue = Float.NEGATIVE_INFINITY;
                    int i = 0;
                    synchronized(history) {
                        Iterator<Double> x = history.iterator();
                        while (x.hasNext()) {
                            double v = x.next();
                            if (v < minValue) minValue = v;
                            if (v > maxValue) maxValue = v;
                            hv[i++] = v;
                        }
                    }
                    label = Texts.n4(minValue) + " | " + Texts.n4(maxValue);
                }



                Graphics2D g = paintContext.getGraphics();

                double W = LinePlot.this.ui.getWidth();
                double H = LinePlot.this.ui.getHeight();

                int nh = history.size();
                double x = 0;
                double dx = (W/ nh);
                final float bh = (float)H;
                final double mv = minValue;
                final double Mv = maxValue;
                //final int ih = (int)bh;

                g.setColor(Color.WHITE);

                if (mv != Mv) {
                    int prevX = -1;
                    final int histSize = history.size();
                    final double HV[] = hv;
                    for (int i = 0; i < histSize; i++) {
                        final double v = HV[i];

                        double py = (v - mv) / (Mv - mv); if (py < 0) py = 0; if (py > 1.0) py = 1.0;

                        double y = py * bh;

                        final int iy = (int) y;

                        g.setColor(BlueRed.get(py));
                        g.fillRect(prevX+1, (int)(bh / 2f - y / 2), (int) FastMath.ceil(x - prevX), iy);

                        prevX = (int)x;
                        x += dx;
                    }
                }

                g.setFont(NengoStyle.FONT_BOLD);
                g.setColor(Color.WHITE);
                g.drawString(name(), 10, 10);
                g.setFont(NengoStyle.FONT_SMALL);
                g.drawString(label, 10, 25);

            }
        };

        plotSurfaceWrap = ui.addChildCache(plotSurface);

        redraw();


    }




    @Override
    public void run(float startTime, float endTime) throws SimulationException {


        //label = "?";

        Object i;
        if ((input!=null && (i = input.get())!=null)) {

            if (i instanceof InstantaneousOutput) {
                i = input.get();
            }

            if (i instanceof SpikeOutput) {
                SpikeOutput so = (SpikeOutput) input.get();
                boolean[] v = so.getValues();
                push( v[0]  ? 1f : 0f);
                //label = String.valueOf(v[0]);
            }
            else if (i instanceof RealSource) {
                float[] v = ((RealSource) input.get()).getValues();
                push(v[0]);
                //label = String.valueOf(v[0]);
            }
            else if (i instanceof AtomicDoubleSource) {
                float v;
                push(v = ((AtomicDoubleSource) input.get()).get().floatValue());
                //label = String.valueOf(v);
            }
            else if (i instanceof Number) {
                double d = ((Number)i).doubleValue();
                push(d);
                //label = String.valueOf(d);
            }
            else {
                System.err.println(this + " can not use " + i + " (" + i.getClass() + ')');
            }

        }


        //System.out.println(LinePlot.this + " " + startTime + " " + endTime);

    }


    public void push(double f) {
        synchronized (history) {
            history.addLast(f);
            if (history.size() == maxHistory)
                history.removeFirst();
            changed = true;
        }

        redraw();
    }

    protected void redraw() {
        if (!ui.getBounds().equals(plotSurface.getBounds())) {
            plotSurface.setBounds(ui.getBounds());
            plotSurfaceWrap.setBounds(ui.getBounds());
        }


        long now = System.currentTimeMillis();
        if (now - lastRepaint > minRepaintPeriod) {
            plotSurfaceWrap.repaint();
            lastRepaint = now;
        }
        else {
            //plotSurfaceWrap.invalidatePaint();
        }

    }
    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return "";
    }

    @Override
    public void reset(boolean randomize) {
        synchronized(history) {
            history.clear();
        }

    }

}
