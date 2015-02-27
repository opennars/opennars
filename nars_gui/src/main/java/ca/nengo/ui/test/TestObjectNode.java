package ca.nengo.ui.test;

import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectNode;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.model.node.UINetwork;
import nars.build.Default;
import nars.core.NAR;
import nars.logic.entity.TruthValue;


public class TestObjectNode extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst

    public static Node newObjectNodeDemo() throws StructuralException {
        NetworkImpl network = new NetworkImpl();

        network.addNode(new ObjectNode("NAR", new NAR(new Default())));
        network.addNode(new ObjectNode("The Whole Truth", new TruthValue()));

        return network;
    }


    @Override
    public void init() throws Exception {



        UINetwork networkUI = (UINetwork) addNodeModel(newObjectNodeDemo());
        networkUI.doubleClicked();

        /*
        new Timer(25, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT();
                    networkUI.getModel().run(time, time + dt);
                    time += dt;
                } catch (SimulationException e1) {
                    e1.printStackTrace();
                }
                //cycle();
            }
        }).start();
        */
    }

    float time = 0;


    public void cycle() {


        for (WorldObject x : this.getNengoWorld().getChildren()) {
                    //System.out.println( x.getChildren() );
            //x.run(time, time+1);
        }
    }

    public static void main(String[] args) {
        new TestObjectNode();
    }


}
