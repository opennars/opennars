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
import ca.nengo.ui.model.widget.UITarget;
import ca.nengo.util.ScriptGenException;

import java.awt.*;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LinePlot extends AbstractWidget {

    //public static final ColorArray grayScale = new ColorArray(16, Color.GRAY, Color.WHITE);
    public static final ColorArray grayScale = new ColorArray(128, Color.GREEN, Color.RED);


    private String label = "?";

    Deque<Double> history = new ConcurrentLinkedDeque<>(); //TODO use seomthing more efficient
    final int maxHistory = 128;
    double[] hv = new double[maxHistory]; //values are cached here for fast access

    private boolean changed = true;

    double minValue, maxValue;
    private ObjectTarget input;


    @Override
    protected void paint(PaintContext paintContext, double width, double height) {

        if (changed) {
            changed = false;
            minValue = Float.POSITIVE_INFINITY;
            maxValue = Float.NEGATIVE_INFINITY;
            int i = 0;
            Iterator<Double> x = history.iterator();
            while (x.hasNext()) {
                double v = x.next();
                if (v < minValue) minValue = v;
                if (v > maxValue) maxValue = v;
                hv[i++] = v;
            }
        }


        Graphics2D g = paintContext.getGraphics();

        int nh = history.size();
        double x = 0;
        double dx = (width/ nh);
        final float bh = (float)height;
        final int ih = (int)bh;

        g.setColor(Color.WHITE);

        if (maxValue != minValue) {
            int prevX = -1;
            for (int i = 0; i < history.size(); i++) {
                final double v = hv[i];
                final double py = (v - minValue) / (maxValue - minValue);
                double y = py * bh;

                final int iy = (int) y;

                g.setColor(grayScale.get(py));
                g.fillRect(prevX+1, (int)(bh / 2f - y / 2), (int) Math.ceil(x - prevX), iy);

                prevX = (int)x;
                x += dx;
            }
        }

        g.setFont(NengoStyle.FONT_BOLD);
        g.setColor(Color.WHITE);
        g.drawString(name(), 10, 10);
        g.setFont(NengoStyle.FONT_NORMAL);
        g.drawString(label, 10, 30);

    }



    public LinePlot(String name) {
        this(name, 64, 64);
    }

    public LinePlot(String name, double width, double height) {
        super(name, width, height);

        input = new ObjectTarget(this, "_input", Object.class);
        //setInputs(input);


        ui.addWidget(UITarget.createTerminationUI(ui, input));




    }



    @Override
    public void run(float startTime, float endTime) throws SimulationException {


        label = "?";

        Object i;
        if ((input!=null && (i = input.get())!=null)) {

            if (i instanceof InstantaneousOutput) {
                i = input.get();
            }

            if (i instanceof SpikeOutput) {
                SpikeOutput so = (SpikeOutput) input.get();
                boolean[] v = so.getValues();
                push( v[0]  ? 1f : 0f);
                label = String.valueOf(v[0]);
            }
            else if (i instanceof RealSource) {
                float[] v = ((RealSource) input.get()).getValues();
                push(v[0]);
                label = String.valueOf(v[0]);
            }
            else if (i instanceof AtomicDoubleSource) {
                float v;
                push(v = ((AtomicDoubleSource) input.get()).get().floatValue());
                label = String.valueOf(v);
            }
            else if (i instanceof Number) {
                double d = ((Number)i).doubleValue();
                push(d);
                label = String.valueOf(d);
            }
            else {
                System.err.println(this + " can not use " + i + " (" + i.getClass() + ')');
            }

        }


        //System.out.println(LinePlot.this + " " + startTime + " " + endTime);

    }


    public void push(double f) {
        history.addLast(f);
        if (history.size() == maxHistory)
            history.removeFirst();
        changed = true;
        ui.repaint();
    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return "";
    }

    @Override
    public void reset(boolean randomize) {
        history.clear();

    }

}
