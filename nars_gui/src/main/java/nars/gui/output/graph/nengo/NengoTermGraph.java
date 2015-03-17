package nars.gui.output.graph.nengo;

import ca.nengo.model.SimulationException;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;
import nars.build.Default;
import nars.core.NAR;
import org.piccolo2d.util.PPaintContext;

import java.util.TimerTask;


public class NengoTermGraph extends Nengrow {

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst


    static int n = 0;
    private TermGraphNode graphNode;
    float time = 0;
    boolean printFPS = false;


    public static TermGraphNode newGraph(NAR n) {
        TermGraphNode network = new TermGraphNode(n);
        return network;
    }

    public static void main(String[] args) {
        Default d = new Default(32, 1, 1);
        d.setSubconceptBagSize(0);
        NAR nar = new NAR(d);
        nar.input("<a-->b>.");
        nar.run(1);
        new NengoTermGraph(nar).window(800, 600);
    }

    public NengoTermGraph(NAR n) {
        this(newGraph(n));
    }

    public NengoTermGraph(TermGraphNode node) {
        super();
        try {
            init(node);
        } catch (ContainerException e) {
            e.printStackTrace();
        }
    }


    UINetwork networkUI = null;

    public void init(TermGraphNode graph) throws ContainerException {

        this.graphNode = graph;

        UINetwork networkUI = (UINetwork) addNodeModel(graphNode);
        NodeViewer window = networkUI.openViewer(Window.WindowState.MAXIMIZED);


        getUniverse().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);


        float fps = 30f;

        java.util.Timer timer = new java.util.Timer("", false);
        timer.scheduleAtFixedRate(new TimerTask() {

            int frame = 0;

            @Override
            public void run() {

                long start = 0;

                float dt = 0.25f;
                try {

                    if (printFPS) {
                        if (frame % 100 == 0) {
                            start = System.nanoTime();
                        }
                    }

                    if (networkUI!=null)
                        networkUI.node().run(time, time + dt, 1);


                    if (printFPS) {
                        if (frame % 100 == 0) {
                            long end = System.nanoTime();
                            double time = (end - start) / 1e6;

                            System.out.println(this + " " + time + " ms");
                        }
                    }


                } catch (SimulationException e) {
                    e.printStackTrace();
                }

                time += dt;
                frame++;
            }
        }, 0, (int) (1000 / fps));


    }


    @Override
    public void init() throws Exception {

    }
}
