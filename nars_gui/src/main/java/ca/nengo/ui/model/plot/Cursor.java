package ca.nengo.ui.model.plot;


import ca.nengo.model.impl.NetworkImpl;
import nars.gui.output.graph.nengo.SubCycle;
import ca.nengo.model.*;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.util.ScriptGenException;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.util.HashMap;

public class Cursor extends AbstractWidget {

    private Boolean on = Boolean.TRUE;

    @Override
    public boolean isResizable() {
        return false;
    }

    public Cursor(String name, int w, int h, NetworkImpl network) {
        super(name, w, h);
        setBounds(new PBounds(0, 0, w, h));
        reset();
        //UINeoNode x = ((UINeoNode)ui.getParent()).node();
        network.addStepListener(subCycle);
    }

    public SubCycle subCycle = new SubCycle() {
        @Override
        public double getTimePerCycle() {
            return on ? 0.2 : 0.1;
        }

        @Override
        public void run(int numCycles, float endTime, long deltaMS){
            on = !on;
        }
    };

    @Override
    public void run(float startTime, float endTime) throws SimulationException {

    }

    @Override
    protected void paint(PaintContext paintContext, double ww, double hh) {

        Graphics2D g = paintContext.getGraphics();

        g.setColor(on ? Color.WHITE : Color.BLACK);

        g.drawRoundRect(0, 0, (int)ww, (int)hh, (int)ww, (int)hh/4);


    }

    public void reset() {
        on = Boolean.TRUE;
        ui.repaint();
    }


    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return "";
    }

}
