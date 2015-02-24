package nars.gui.io;

import automenta.vivisect.Vis;
import automenta.vivisect.gui.*;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import processing.core.PGraphics;


public class DiagramEdit {

    public static void main(String[] arg) {


        GButton g;
        PCanvas p = new PCanvas(new Vis() {

            @Override
            public boolean draw(PGraphics g) {

                return true;
            }
        });
        p.setFrameRate(26);
        p.loop();
        p.renderEveryFrame(false);


        //GGroup g = new GGroup(p);

        g = new GButton(p, 10, 10, 200, 200, "abc");
        new GSlider(p, -200,-200,100,20, 15);
        new GSlider2D(p, -100,-100,300,300);

        new NWindow("_", p ).show(500,400,true);
    }
}
