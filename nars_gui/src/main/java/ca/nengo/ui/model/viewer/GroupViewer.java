/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "EnsembleViewer.java". Description: 
"Viewer for peeking into an Ensemble
  
  @author Shu"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU 
Public License license (the GPL License), in which case the provisions of GPL 
License are applicable  instead of those above. If you wish to allow use of your 
version of this file only under the terms of the GPL License and not to allow 
others to use your version of this file under the MPL, indicate your decision 
by deleting the provisions above and replace  them with the notice and other 
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

package ca.nengo.ui.model.viewer;

import ca.nengo.model.Group;
import ca.nengo.model.Network;
import ca.nengo.model.Node;
import ca.nengo.model.Probeable;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.elastic.ElasticGround;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UIGroup;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.node.UINeuron;
import ca.nengo.ui.model.node.UINodeViewable;
import ca.nengo.util.Probe;

import java.util.Iterator;
import java.util.Map;

/**
 * Viewer for peeking into an Ensemble
 * 
 * @author Shu
 */
public class GroupViewer<N extends Group, G extends UINodeViewable> extends NodeViewer {

    public GroupViewer(G ensembleUI) {
        this(ensembleUI, new ElasticGround());
    }

	/**
	 * @param ensembleUI
	 *            Parent Ensemble UI Wrapper
	 */
	public GroupViewer(G ensembleUI, WorldGroundImpl g) {
		super(ensembleUI, g);
	}

	@Override
	public void applyDefaultLayout() {
		if (getUINodes().size() == 0)
			return;

		applySortLayout(SortMode.BY_NAME);
	}

	@Override
	public N getModel() {

		return (N) super.getModel();
	}

	@Override
	public G getViewerParent() {
		return (G) super.getViewerParent();
	}

	@Override
	public void updateViewFromModel(boolean isFirstUpdate) {
	       /*
         * Get the current children and map them
         */
        /*HashMap<Node, UINeoNode> currentNodes = new HashMap<Node, UINeoNode>(
                getGround().getChildrenCount());*/

        //List<UINeoNode> toAdd = null;
        Iterator<Map.Entry<Node, UINeoNode>> it = neoNodesChildren.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Node, UINeoNode> e = it.next();
            //Node node = e.getKey();
            UINeoNode nodeUI = e.getValue();
            if (nodeUI ==null || nodeUI.isDestroyed()) {
                getGround().removeChild(nodeUI);
                it.remove();
            }
        }

        for (Object o : getModel().nodes()) {
            Node n = (Node)o; //HACK
            UINeoNode u = neoNodesChildren.get(n);
            if (u == null) {
                u = createUINode(n, isFirstUpdate);
                neoNodesChildren.put(n, u);
                getGround().addChild(u);
            }
        }

//        for (Map.Entry<Node,UINeoNode> e : ) {
//            UINeoNode node = e.getValue();
//            if (!node.isDestroyed()) {
//                //Util.Assert(node.getModel() != null);
//                currentNodes.put(e.getKey(), node);
//            }
//        }
//        neoNodesChildren.clear();


        /*
         * Construct Nodes from the Network model
         */
        /*
        Node[] nodes = getModel().getNodes();

        for (Node node : nodes) {
            if (getUINode(node) == null) {
                UINeoNode nodeUI = currentNodes.get(node);

                if (nodeUI == null) {
                    nodeUI = createUINode(node, isFirstUpdate);
                } else {
                    neoNodesChildren.put(nodeUI.getModel(), nodeUI);
                }

            } else {
                Util.Assert(false, "Trying to add node which already exists");
            }
        }
        */


            /*
         * Prune existing nodes by deleting them
         */
        /*
        for (Node node : currentNodes.keySet()) {
            // Remove nodes which are no longer referenced by the network model
            if (getUINode(node) == null) {
                UINeoNode nodeUI = currentNodes.get(node);
                nodeUI.showPopupMessage("Node " + nodeUI.getName() + " removed from Network");
                nodeUI.destroy();
            }
        }
        */

        afterViewUpdated(isFirstUpdate);
	}

    protected void afterViewUpdated(boolean isFirstUpdate) {

        if (getViewerParent().getNetworkParent() != null) {
			/*
			 * Construct probes
			 */
            Probe[] probes = getViewerParent().getNetworkParent().getSimulator().getProbes();

            for (Probe probe : probes) {
                Probeable target = probe.getTarget();

                if(target != null){
                    if (!(target instanceof Node)) {
                        UserMessages.showError("Unsupported target type for probe");
                    } else {

                        if (probe.isInEnsemble() && probe.getEnsembleName() == getModel().name()) {
                            Node node = (Node) target;

                            UINeoNode nodeUI = getUINode(node);
                            if(nodeUI  != null){
                                nodeUI.showProbe(probe);
                            }
                        }
                    }
                }
            }
        }

        applyDefaultLayout();
    }
    protected UINeoNode createUINode(Node node, boolean isFirstUpdate) {

        if (node instanceof Neuron) {
            Neuron neuron = (Neuron) node;

            UINeuron neuronUI = new UINeuron(neuron);

            return addUINode(neuronUI, false, false);
        } else if (node instanceof Group) {
            Group group = (Group)node;
            UIGroup ensembleUI = new UIGroup(group);
            return addUINode(ensembleUI,false,false);

        } else if (node instanceof Network) {
            Network network = (Network)node;
            UINetwork networkUI = new UINetwork(network);
            return addUINode(networkUI, false, false);
        } else {
            UserMessages.showError("Unsupported node type " + node.getClass().getSimpleName()
                    + " in EnsembleViewer");
        }
        return null;
    }

    @Override
	protected void removeChildModel(Node node) {
		Util.Assert(false, "Cannot remove model");
	}

	@Override
	protected boolean canRemoveChildModel(Node node) {
		return false;
	}

}
