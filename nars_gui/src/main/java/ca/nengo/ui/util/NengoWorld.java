package ca.nengo.ui.util;

import ca.nengo.model.Node;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.elastic.ElasticWorld;
import ca.nengo.ui.models.NodeContainer;
import ca.nengo.ui.models.UINeoNode;

import java.awt.geom.Point2D;

public class NengoWorld extends ElasticWorld implements NodeContainer {

	public NengoWorld() {
		super("Nengo");
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
        addNodeModel(nodeUI, posX, posY);
        return nodeUI;
	}

    public WorldObject addNodeModel(WorldObject nodeUI, Double posX, Double posY) throws ContainerException {
        if (posX != null && posY != null) {
            nodeUI.setOffset(posX, posY);

            getGround().addChild(nodeUI);
        } else {
            getGround().addChildFancy(nodeUI);
        }



        return nodeUI;

    }



	public Node getNodeModel(String name) {
		for (WorldObject wo : getGround().getChildren()) {
			if (wo instanceof UINeoNode) {
				UINeoNode nodeUI = (UINeoNode) wo;

				if (nodeUI.getName().equals(name)) {
					return nodeUI.getModel();
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

}
