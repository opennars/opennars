package ca.nengo.ui;


import automenta.vivisect.swing.NSlider;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;

import javax.swing.*;
import java.awt.*;

abstract public class Nengrow extends AbstractNengo {

    private float simulationDT;

    public Nengrow() {
        this(new DefaultApplication());
    }
    public Nengrow(Application app) {
        super();
        setApplication(app);
    }

    @Override
    protected void initialize() {
        super.initialize();

        menuBar.add(newSpeedControl());
        add(getUniverse());


        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void loadPreferences() {
        //nothing
    }

    protected void setSimulationDT(float newDT) {
        this.simulationDT = newDT;
    }

    /** delta-time added each simulation iteration; while zero, simulation pauses */
    public float getSimulationDT() {
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
}
