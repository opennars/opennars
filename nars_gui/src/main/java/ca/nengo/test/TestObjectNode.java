package ca.nengo.test;

import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectNode;
import ca.nengo.ui.NengrowPanel;
import nars.NAR;
import nars.nar.Default;
import nars.truth.DefaultTruth;


public class TestObjectNode {


    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst

    public static NetworkImpl newObjectNodeDemo() throws StructuralException {
        NetworkImpl network = new DefaultNetwork<>();

        network.addNode(new ObjectNode("NAR", new NAR(new Default())));
        network.addNode(new ObjectNode("The Whole Truth", new DefaultTruth()));

        return network;
    }
    public static void main(String[] args) throws StructuralException {
        new NengrowPanel(newObjectNodeDemo()).newWindow(800,800);
    }


//
//    @Override
//    public void init() throws Exception {
//
//
//
//        UINetwork networkUI = (UINetwork) addNodeModel(newObjectNodeDemo());
//        networkUI.doubleClicked();
//
//        /*
//        new Timer(25, new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    float dt = getSimulationDT();
//                    networkUI.getModel().run(time, time + dt);
//                    time += dt;
//                } catch (SimulationException e1) {
//                    e1.printStackTrace();
//                }
//                //cycle();
//            }
//        }).start();
//        */
//    }
//
//    float time = 0;
//
//
//    public void cycle() {
//
//
//        for (WorldObject x : this.getNengoWorld().getChildren()) {
//                    //System.out.println( x.getChildren() );
//            //x.run(time, time+1);
//        }
//    }
//
//    public static void main(String[] args) {
//        new TestObjectNode();
//    }
//

}
