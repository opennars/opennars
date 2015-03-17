package nars.gui.output.graph.nengo;

import ca.nengo.model.SimulationException;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;
import nars.build.Default;
import nars.core.NAR;
import org.piccolo2d.util.PPaintContext;


public class NengoNetworkPanel extends Nengrow {

    private NodeViewer networkUIViewer;
    private NodeViewer window;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst



    private TermGraphNode graphNode;
    float time = 0;
    boolean printFPS = false;
    int frame = 0;

    UINetwork networkUI = null;

    public static TermGraphNode newGraph(NAR n) {
        TermGraphNode network = new TermGraphNode(n.memory);
        return network;
    }

    public static void main(String[] args) {
        Default d = new Default(32, 1, 1);
        d.setSubconceptBagSize(0);
        NAR nar = new NAR(d);
        nar.input("<a-->b>.");
        nar.input("<b-->c>.");
        nar.input("<c-->d>.");
        nar.run(16);
        new NengoNetworkPanel(nar).window(800, 600);
    }

    public NengoNetworkPanel(NAR n) {
        this(newGraph(n));
    }

    public NengoNetworkPanel(TermGraphNode graph) {
        super();


        getUniverse().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

        this.graphNode = graph;


            //networkUI = (UINetwork) addNodeModel(graphNode);
            //networkUI = (UINetwork) getNengoWorld().addNodeModel(graphNode);

            networkUI = graphNode.newUI(800,600); ///UINetwork) addNodeModel(graphNode);



        getNengoWorld().addChild(networkUIViewer = networkUI.createViewerInstance());
            //networkUIViewer.applyDefaultLayout();
            //window = networkUI.openViewer(Window.WindowState.MAXIMIZED);






    }

//
//    @Override
//    protected NodeContainer getRoot() {
//        //return super.getRoot();
//        return networkUIViewer;
//    }





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


    @Override
    public void init() throws Exception {

    }
}
