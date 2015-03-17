package ca.nengo.model;

import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import org.piccolo2d.util.PBounds;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
* Created by me on 3/3/15.
*/
public class AgentNode extends AbstractWidget {

    float animationLerpRate = 0.05f; //LERP interpolation rate

    double heading = 0, cheading = 0.57;
    double cx, cy, x, y;
    double time = 0;
    private Rectangle2D movementBounds = null;

    public AgentNode(String name) {
        super(name);

        cx = x = ui.getOffset().getX();
        cy = y = ui.getOffset().getY();

        setBounds(new PBounds(0,0,64,64));
    }

    public void forward(double dist) {
        x += Math.cos(heading) * dist;
        y += Math.sin(heading) * dist;
        ui.repaint();
    }

    public void say(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                ui.showPopupMessage(message);
            }
        });
    }

    public double rotate(double dA) {
        heading += dA;
        ui.repaint();
        return heading;
    }
    public double heading(double a) {
        heading = a;
        ui.repaint();
        return heading;
    }

    public double getHeading() {
        return heading;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    protected boolean canMove() {
        return !ui.isSelected();
    }

    public Rectangle2D getMovementBounds() {
        return movementBounds;
    }

    public void setMovementBounds(Rectangle2D movementBounds) {
        this.movementBounds = movementBounds;
    }

    @Override
    public void draggedTo(double x, double y) {
        super.draggedTo(x, y);
        cx = this.x = x;
        cy = this.y = y;
    }

    @Override
    protected void paint(PaintContext paintContext, double width, double height) {

        Graphics2D g = paintContext.getGraphics();

        float scale = (float) Math.sin(time * 10f) * 0.05f + 1.0f;

        double ww = width * scale;
        double hh = height * scale;


        if (canMove()) {
            cx = (cx * (1.0f - animationLerpRate)) + (x * animationLerpRate);
            cy = (cy * (1.0f - animationLerpRate)) + (y * animationLerpRate);
            cheading = (cheading * (1.0f - animationLerpRate / 2.0f)) + (heading * animationLerpRate / 2.0f);


            /*
            Rectangle2D mb = getMovementBounds();
            if (mb!=null) {
                if (x < mb.getMinX()) cx = x = mb.getMinX();
                if (x > mb.getMaxX()) cx = x = mb.getMaxX();

                if (y < mb.getMinY()) cy = y = mb.getMinY();
                if (y > mb.getMaxY()) cy = y = mb.getMaxY();
            }
            */




            //System.out.println(x + " " + y+ " <= " + cx + " " + cy);
            move(cx,cy);
            //move(cx - ui.getOffset().getX(), cy - ui.getOffset().getY() );

            //ui.setOffset(cx,cy);

            //System.out.println(ui.getOffset().getX() + " " + ui.getY() + " " + ui.getPiccolo().getX() + " " + mb);
            //ui.animateToPosition(cx, cy, 0);
            //space.translate(cx, cy);
        }
        else {
            //freeze in place
            x = cx;
            y = cy;
            heading = cheading;
        }

        g.setPaint(Color.ORANGE);
        g.translate((int)ww/2, (int)hh/2);
        g.fillOval((int) (-ww / 2), (int) (-hh / 2), (int) (ww), (int) hh);



        //eyes
        g.setPaint(Color.BLUE);
        g.rotate(cheading);
        int eyeDiam = (int)(width * 0.2f);
        int eyeSpace = (int)(0.15f*width);
        int eyeDist = (int)(0.4f*width);
        g.fillOval(eyeDist, -eyeSpace-eyeDiam/2, eyeDiam, eyeDiam);
        g.fillOval(eyeDist, eyeSpace-eyeDiam/2, eyeDiam, eyeDiam);
        g.rotate(0);

    }



    @Override
    public void run(float startTime, float endTime) throws SimulationException {
        ui.repaint();
    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return "";
    }

}
