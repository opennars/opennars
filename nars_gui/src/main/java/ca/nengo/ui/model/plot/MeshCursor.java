package ca.nengo.ui.model.plot;


import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.util.ScriptGenException;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.util.HashMap;

public class MeshCursor extends AbstractWidget {
    private int x, y;

    //private Boolean on = Boolean.TRUE;

    @Override
    public boolean isResizable() {
        return false;
    }

    public MeshCursor(String name, int w, int h, NetworkImpl network) {
        super(name, w, h);
        setBounds(new PBounds(0, 0, w, h));
        reset();
        ui.setTransparency(0.35f);
        ui.setPickable(false);
        ui.setSelectable(false);

        //UINeoNode x = ((UINeoNode)ui.getParent()).node();
        //network.addStepListener(subCycle);

    }

    @Override
    public String toString() {
        return name() + "@" + getX() + ','+ getY();
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


    }



    @Override
    protected void paint(PaintContext paintContext, double ww, double hh) {

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

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        setX(x); setY(y);
    }
}
