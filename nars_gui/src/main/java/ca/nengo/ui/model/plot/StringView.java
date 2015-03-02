package ca.nengo.ui.model.plot;


import ca.nengo.model.*;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.model.impl.ObjectTarget;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.object.BoundsHandle;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.EmptyIcon;
import ca.nengo.ui.model.widget.UITarget;
import ca.nengo.util.ScriptGenException;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

public class StringView extends AbstractNode implements UIBuilder {

    final Target<InstantaneousOutput> input = new ObjectTarget(this, "input",Object.class);

    private ToStringUI ui;
    String label = "";
    private Object currentValue = null;

    public class ToStringUI extends UINeoNode<StringView> {

        public ToStringUI() {
            super(StringView.this);

            BoundsHandle.addBoundsHandlesTo(this);

            EmptyIcon icon = new EmptyIcon(this);
            icon.setLabelVisible(false);
            setIcon(icon);

            //img = new BufferedImage(400, 200, BufferedImage.TYPE_4BYTE_ABGR);
            //setIcon(new WorldObjectImpl(new PXImage(img)));
            setBounds(0, 0, 128, 64);

            addWidget(UITarget.createTerminationUI(this, getInput()));



            repaint();
        }

        @Override
        public String getTypeName() {
            return "ToStringUI";
        }



        double min, max;
        float fontSize = 1;
        boolean fontSizeLocked = false;
        int offX = 0, offY = 0;
        int upOrDown = 0;

        @Override
        public void paint(PaintContext paintContext) {
            super.paint(paintContext);

            Graphics2D g = paintContext.getGraphics();


            Font f = g.getFont().deriveFont(fontSize);
            if (!fontSizeLocked) {
                double ww = getWidth();
                double hh = getHeight();
                Rectangle2D sb = f.getStringBounds(label, g.getFontRenderContext());
                if ((sb.getWidth() > ww) || (sb.getHeight() > hh)) {
                    fontSize--;
                    f = g.getFont().deriveFont(fontSize);
                    if (upOrDown == 1)
                        fontSizeLocked = true;
                    else
                        upOrDown = -1;
                }
                else if ((sb.getWidth() < ww) || (sb.getHeight() < hh)) {
                    fontSize++;
                    f = g.getFont().deriveFont(fontSize);
                    if (upOrDown == -1)
                        fontSizeLocked = true;
                    else
                        upOrDown = +1;
                }

                offX = 0;
                offY = (int)(hh/2-sb.getHeight()/2);
            }

            g.setFont(f);
            g.setColor(Color.WHITE);
            g.drawString(label, offX, offY);
        }

        public void reset() {
            fontSizeLocked = false;
            upOrDown = 0;
            repaint();
        }
    }



    private Target getInput() {
        return input;
    }

    public StringView(String name) {
        super(name);
        reset(false);
    }

    @Override
    public UINeoNode newUI() {
        if (ui==null)
            ui = new ToStringUI();
        return ui;
    }


    @Override
    public void run(float startTime, float endTime) throws SimulationException {


        Object i;
        if ((input!=null && (i = input.get())!=null)) {

            if (i instanceof InstantaneousOutput) {
                i = input.get();
            }

            if (i instanceof SpikeOutput) {
                SpikeOutput so = (SpikeOutput) input.get();
                boolean[] v = so.getValues();
                push( v[0]  ? 1f : 0f);
            }
            else if (i instanceof RealSource) {
                float[] v = ((RealSource) input.get()).getValues();
                push(v[0]);
            }
            else if (i instanceof AtomicDoubleSource) {
                float v;
                push(v = ((AtomicDoubleSource) input.get()).get().floatValue());
            }
            else {
                push(i);
            }

        }


        //System.out.println(LinePlot.this + " " + startTime + " " + endTime);

    }


    protected void push(Object o) {
        String newLabel;
        if (o == null) {
            newLabel = "";
        }
        else  {
            newLabel  = o.toString();
        }

        currentValue = o;

        if (!newLabel.equals(label)) {
            label = newLabel;
            ui.reset();
        }
    }

    @Override
    public void reset(boolean randomize) {
        currentValue = null;
        label = "";
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
