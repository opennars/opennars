package ca.nengo.ui;

import ca.nengo.model.Network;
import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;

/**
 Simple panel which can be used to display a
 basic Network view
 */
public class NengrowPanel extends Nengrow {

    /** simulation timestep */
    protected float dt = 0.01f;

    protected float time = 0;

    //protected allows subclasses to access it, private does not
    protected final UINetwork networkUI;
    protected final NodeViewer window;

    public NengrowPanel(Network view) {
        super();

        UINetwork networkUIx;
        NodeViewer windowx;
        try {
            networkUIx = (UINetwork) addNodeModel(view);
            //networkUI.doubleClicked();
            windowx = networkUIx.openViewer(Window.WindowState.MAXIMIZED);
        } catch (ContainerException e) {
            //TODO display error message in the nengo view or statusbar
            e.printStackTrace();
            networkUIx = null;
            windowx = null;
        }

        this.window = windowx;
        networkUI = networkUIx;

    }


    @Override
    public void run() {
        try {
            //System.out.println(time);


            networkUI.node().run(time, time + dt, 1);
            time += dt;

        } catch (SimulationException e1) {
            e1.printStackTrace();
        }

    }


    @Override
    public void init() throws Exception {


    }
}
