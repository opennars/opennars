package ca.nengo.ui.models.plot;


import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.*;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.model.impl.ObjectTarget;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.objects.BoundsHandle;
import ca.nengo.ui.models.UIBuilder;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.icons.EmptyIcon;
import ca.nengo.ui.models.nodes.widgets.UITermination;
import ca.nengo.util.ScriptGenException;

import java.awt.*;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LinePlot extends AbstractNode implements UIBuilder {

    //public static final ColorArray grayScale = new ColorArray(16, Color.GRAY, Color.WHITE);
    public static final ColorArray grayScale = new ColorArray(16, Color.GREEN, Color.RED);

    final Target<InstantaneousOutput> input = new ObjectTarget(this, "input",InstantaneousOutput.class);
    private String label = "?";

    Deque<Double> history = new ConcurrentLinkedDeque<>(); //TODO use seomthing more efficient
    final int maxHistory = 128;
    double[] hv = new double[maxHistory]; //values are cached here for fast access

    private LinePlotUI ui;
    private boolean changed = true;

    public class LinePlotUI extends UINeoNode<LinePlot> {

        public LinePlotUI() {
            super(LinePlot.this);

            BoundsHandle.addBoundsHandlesTo(this);

            setIcon(new EmptyIcon(this));
            //img = new BufferedImage(400, 200, BufferedImage.TYPE_4BYTE_ABGR);
            //setIcon(new WorldObjectImpl(new PXImage(img)));
            setBounds(0, 0, 512, 256);

            addWidget(UITermination.createTerminationUI(this, getInput()));



            repaint();
        }

        @Override
        public String getTypeName() {
            return "X";
        }



        double min, max;

        @Override
        public void paint(PaintContext paintContext) {
            super.paint(paintContext);

            if (changed) {
                changed = false;
                min = Float.POSITIVE_INFINITY;
                max = Float.NEGATIVE_INFINITY;
                int i = 0;
                Iterator<Double> x = history.iterator();
                while (x.hasNext()) {
                    double v = x.next();
                    if (v < min) min = v;
                    if (v > max) max = v;
                    hv[i++] = v;
                }
            }


            Graphics2D g = paintContext.getGraphics();

            int nh = history.size();
            double x = 0;
            double dx = (getWidth() / nh);
            final float bh = (float)getHeight();
            final int ih = (int)bh;

            g.setColor(Color.WHITE);

            if (max != min) {
                int prevX = 0;
                for (int i = 0; i < history.size(); i++) {
                    final double v = hv[i];
                    final double py = (v - min) / (max - min);
                    double y = py * bh;

                    final int iy = (int) y;

                    g.setColor(grayScale.get(py));
                    g.fillRect(prevX, ih / 2 - iy / 2, (int) Math.ceil(x - prevX), iy);

                    System.out.println(x + ' '  + v);
                    prevX = (int)x;
                    x += dx;
                }
            }

            g.setColor(Color.WHITE);
            g.drawString(label, 10, 10);
        }

    }



    private Target getInput() {
        return input;
    }

    public LinePlot(String name) {
        super(name);
    }

    @Override
    public UINeoNode newUI() {
        ui = new LinePlotUI();
        return ui;
    }


    @Override
    public void run(float startTime, float endTime) throws SimulationException {

        label = "?";

        if ((input!=null && input.get()!=null)) {
            InstantaneousOutput i = input.get();
            if (i instanceof SpikeOutput) {
                SpikeOutput so = (SpikeOutput) input.get();
                boolean[] v = so.getValues();
                push( v[0]  ? 1f : 0f);
                label = String.valueOf(v[0]);
            }
            else if (i instanceof RealOutput) {
                float[] v = ((RealOutput) input.get()).getValues();
                push(v[0]);
                label = String.valueOf(v[0]);
            }
            else if (i instanceof Number) {
                push(((Number)i).doubleValue());
            }

        }


        //System.out.println(LinePlot.this + " " + startTime + " " + endTime);

    }


    protected void push(double f) {
        history.addLast(f);
        if (history.size() == maxHistory)
            history.removeFirst();
        changed = true;
        ui.repaint();
    }

    @Override
    public void reset(boolean randomize) {

        history.clear();

    }

    @Override
    public Node[] getChildren() {
        return new Node[0];
    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return "";
    }
}
