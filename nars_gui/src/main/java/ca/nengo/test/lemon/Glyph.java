package ca.nengo.test.lemon;

import automenta.vivisect.Video;
import ca.nengo.model.SimulationException;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by you on 27.3.15.
 */
public class Glyph extends AbstractWidget {

    public static final Stroke border = new BasicStroke(1);
    public static final Stroke noStroke = new BasicStroke(0);
    Color textcolor = Color.WHITE;
    Color borderColor = new Color(70,70,70);
    Color bgColor = new Color(40,40,40);
    Font f = Video.monofont.deriveFont(64f);
    public int c;
    private boolean lockPos;

    public Glyph(int c) {
        super("unicode "+c);
        this.c = c;

    }

    public void lockPosition(boolean l) {
        this.lockPos = l;
    }

    @Override
    public boolean isDraggable() {
        return !lockPos;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double width, double height) {
        Graphics2D g = paintContext.getGraphics();


        //border and background
        final int iw = (int) width;
        final int ih = (int) height;
        if (bgColor != null || borderColor != null) {
            if (bgColor != null) {
                //g.setStroke(noStroke);
                g.setPaint(bgColor);
                g.fillRect(0, 0, iw, ih);
            }
            if (borderColor != null) {
                g.setStroke(border);
                g.setPaint(borderColor);
                g.drawRect(0, 0, iw, ih);
            }

        }

        //draw glyph
        if (textcolor != null) {
            g.setColor(textcolor);
            g.setFont(f);
            final int fs = f.getSize()/2;
            double x = width/2  - ui.getX()/2 - fs/2;
            double y = height / 2 - ui.getY() / 2 + fs/2;
            g.drawString(String.valueOf((char)c), (int) x, (int) y);
        }
    }



    public void setTextColor(Color textcolor) {
        this.textcolor = textcolor;
        ui.repaint();
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        ui.repaint();
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {


    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return null;
    }//use default?


    public int getChar() {
        return c;
    }
}
