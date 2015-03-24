package nars.gui.output.graph.nengo;

import ca.nengo.model.Network;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NetworkViewer;
import ca.nengo.ui.model.viewer.NodeViewer;

/**
* Created by me on 3/12/15.
*/
public class DefaultUINetwork<N extends Network> extends UINetwork {


    private final N nargraph;

    public DefaultUINetwork(N n) {
        super(n);
        this.nargraph = n;
    }

    @Override
    public ModelIcon getIcon() {
        return (ModelIcon) super.getIcon();
    }

    @Override
    public NodeViewer newViewer() {
        return new UINARGraphViewer(this);
    }

    @Override
    public void layoutChildren() {


    }



    public static class UINARGraphGround extends WorldGroundImpl /*ElasticGround*/ {

        @Override
        public void layoutChildren() {

        }



    }

    final public static class UINARGraphViewer extends NetworkViewer {
        public UINARGraphViewer(UINetwork g) {
            super(g, new UINARGraphGround());
        }

        @Override
        protected boolean isDropEffect() {
            return false;
        }

        @Override
        public void layoutChildren() {

        }

        @Override
        public void applyDefaultLayout() {
            //System.out.println("no default layout");
        }


    }
}
