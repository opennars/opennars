package ca.nengo.ui;


import automenta.vivisect.swing.NSlider;
import automenta.vivisect.swing.NWindow;
import ca.nengo.ui.lib.world.piccolo.primitive.Universe;
import org.piccolo2d.util.PPaintContext;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;

import javax.swing.*;
import java.awt.*;
import java.util.*;

abstract public class Nengrow extends AbstractNengo {

    private float simulationDT;
    protected double fps = 30;
    private java.util.Timer timer;

    public Nengrow() {
        this(new DefaultApplication());


        //default: low quality
        getUniverse().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

    }
    public Nengrow(Application app) {
        super();
        setApplication(app);
    }

    public NWindow newWindow(int w, int h) {
        return new NWindow("", this).show(w, h, true);
    }

    @Override
    protected void initialize() {
        super.initialize();

        //menuBar.add(newSpeedControl());




        init(getUniverse());

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final protected void init(Universe universe) {
        add(universe, BorderLayout.CENTER);
    }

    @Override
    protected void loadPreferences() {
        //nothing
    }

    @Deprecated protected void setSimulationDT(float newDT) {
        this.simulationDT = newDT;
    }

    /** delta-time added each simulation iteration; while zero, simulation pauses */
    @Deprecated public float getSimulationDT() {
        return simulationDT;
    }

    private JComponent newSpeedControl() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));

        NSlider n = new NSlider(0.001f, 0, 0.01f) {

            @Override
            public void onChange(float v) {

                setSimulationDT(v);

            }
        };
        n.setPrefix("dt (s)");
        setSimulationDT(n.value());

        j.add(n);

        return j;
    }

    abstract public void init() throws Exception;

/*    public static void main(String[] args) {
        new Nengrow();
    }*/

    /** each cycle while running, this is called */
    public void run() {

    }


    public double getFPS() { return fps; }

    public void setFPS(double fps) {
        this.fps = fps;
        start();
    }


    protected void start() {
        stop();

        double fps = getFPS();
        System.out.println("nengrow: " + this + " FPS=" + fps);
        if (fps > 0) {
            timer = new java.util.Timer("", false);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Nengrow.this.run();
                    //repaint();
                }
            }, 0, (int) (1000.0 / fps));
        }
    }
    protected void stop() {
        if (timer!=null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void visibility(boolean appearedOrDisappeared) {
        if (appearedOrDisappeared) {
            start();
        }
        else {
            stop();
        }
    }
}
