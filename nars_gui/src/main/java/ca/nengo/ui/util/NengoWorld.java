package ca.nengo.ui.util;

import ca.nengo.model.Node;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.model.UINeoNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import java.awt.geom.Point2D;
import java.util.Iterator;

public class NengoWorld extends WorldImpl implements NodeContainer {

	public NengoWorld() {
		super("Nengo", new WorldGroundImpl());
	}

	/*@Override
	protected void constructMenu(PopupMenuBuilder menu) {

		super.constructMenu(menu);

		// Add models section
		menu.addSection("Add model");

		// Create network action
		menu.addAction(new CreateModelAction("New Network", this, new CNetwork()));
	}*/


	public UINeoNode addNodeModel(Node node) throws ContainerException {
		return addNodeModel(node, null, null);
	}


	public UINeoNode addNodeModel(Node node, Double posX, Double posY) throws ContainerException {
		/*if (!(node instanceof Network)) {
			throw new ContainerException("Only Networks are allowed to be added to the top-level Window");
		}*/

		UINeoNode nodeUI = UINeoNode.createNodeUI(node);
        if (nodeUI!=null) {
            addNodeModel(nodeUI, posX, posY);
        }
        return nodeUI;
	}

    public WorldObject addNodeModel(WorldObject nodeUI, Double posX, Double posY) throws ContainerException {
        if (posX != null && posY != null) {
            nodeUI.setOffset(posX, posY);
        }
        else {
            nodeUI.setOffset(0,0);
        }


        getGround().addChild(nodeUI);


        return nodeUI;

    }

    @Override
    public Iterable<WorldObject> getWorldObjects() {
        return getGround().getChildren();
    }

    @Override
    public Iterator<Node> getNodeModels() {
        return Iterators.transform(
                Iterators.filter(getGround().getChildren().iterator(), UINeoNode.class),
                nodeOfneoNode);
    }

    public Node getNodeModel(String name) {
		for (WorldObject wo : getGround().getChildren()) {
			if (wo instanceof UINeoNode) {
				UINeoNode nodeUI = (UINeoNode) wo;

				if (nodeUI.name().equals(name)) {
					return nodeUI.node();
				}
			}
		}
		return null;
	}

	public Point2D localToView(Point2D localPoint) {
		localPoint = getSky().parentToLocal(localPoint);
		localPoint = getSky().localToView(localPoint);
		return localPoint;
	}

    public void removeChildren() {
        pnode.removeAllChildren();
    }

    public final Function<UINeoNode,Node> nodeOfneoNode = new Function<UINeoNode,Node>() {
        @Override
        public Node apply(UINeoNode input) {
            return input.node();
        }
    };
}
