package ca.nengo.test.lemon;

import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.lib.world.handler.KeyboardHandler;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by you on 27.3.15.
 */

public class Cursor extends AbstractWidget {
    public int c, r;
    private KeyboardHandler keyHandler;
    private Editor editor;

    //private Boolean on = Boolean.TRUE;

    @Override
    public boolean isResizable() {
        return false;
    }

    public Cursor(String name, Editor editor) {
        super(name, 666, 666);
        this.editor = editor;
        reset();
        ui.setTransparency(0.35f);
        ui.setPickable(false);
        ui.setSelectable(false);

        //UINeoNode x = ((UINeoNode)ui.getParent()).node();
        //network.addStepListener(subCycle);

    }

    @Override
    public String toString() {
        return name() + "@" + c + ','+ r;
    }

    /*
    public SubCycle subCycle = new SubCycle() {
        @Override
        public double getTimePerCycle() {
            return on ? 0.2 : 0.1;
        }

        @Override
        public void run(int numCycles, float endTime, long deltaMS){
            on = !on;
        }
    };*/

    final ColorArray ca = new ColorArray(32, Color.YELLOW, Color.GREEN);

    @Override
    public void run(float startTime, float endTime) throws SimulationException {
        enableInput();
    }

    protected void enableInput() {
        if ((keyHandler == null) && (editor.viewer != null)) {
            keyHandler = new KeyboardHandler() {

                @Override
                public void keyReleased(PInputEvent event) {
                    //editor.keyReleased(event);
                }

                @Override
                public void keyPressed(PInputEvent event) {
                    editor.keyPressed(event, c, r);
                }
            };
            //ui.getPNode().getRoot().addInputEventListener(keyHandler);
            //ui.getViewer().getSky().addInputEventListener(keyHandler);
            editor.viewer.getSky().addInputEventListener(keyHandler);
            //viewer.getSky().addInputEventListener(keyHandler);

        }
    }



    @Override
    protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double ww, double hh) {

        long now = System.currentTimeMillis();

        Graphics2D g = paintContext.getGraphics();

        float tc = (float)Math.sin(now / 30.0 / (Math.PI*2) ) * 0.5f + 0.5f;
        g.setPaint( ca.get( tc ));


        int ee = (int)(tc * (ww/4f))/2; //extra border
        g.fillRect((int)(-ee), (int)(-ee), (int) (ww + ee*2), (int) (hh+ ee*2));



    }

    public void reset() {
        ui.repaint();
    }


    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return "";
    }

    public void move(int c, int r)
    {
        this.c = c;
        this.r = r;
    }


}


