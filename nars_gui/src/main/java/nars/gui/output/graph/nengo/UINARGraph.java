package nars.gui.output.graph.nengo;

import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NetworkViewer;
import ca.nengo.ui.model.viewer.NodeViewer;

/**
* Created by me on 3/12/15.
*/
public class UINARGraph extends UINetwork {


    private final TermGraphNode nargraph;

    public UINARGraph(TermGraphNode n) {
        super(n);
        this.nargraph = n;
    }

    @Override
    public ModelIcon getIcon() {
        return (ModelIcon) super.getIcon();
    }

    @Override
    public NodeViewer createViewerInstance() {
        return new UINARGraphViewer(this);
    }

    @Override
    public void layoutChildren() {


    }

    class UINARGraphGround extends WorldGroundImpl /*ElasticGround*/ {

        @Override
        public void layoutChildren() {

        }

    }

    final private class UINARGraphViewer extends NetworkViewer {
        public UINARGraphViewer(UINARGraph g) {
            super(g, new UINARGraphGround());
        }

        @Override
        protected boolean isDropEffect() {
            return false;
        }

        @Override
        public void layoutChildren() {

        }
    }
}
