/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SimulatorDataModel.java". Description:
""

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

package ca.nengo.ui.data;

import ca.nengo.model.Group;
import ca.nengo.model.Network;
import ca.nengo.model.Node;
import ca.nengo.model.Probeable;
import ca.nengo.model.impl.AbstractGroup;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.util.Probe;
import ca.nengo.util.SpikePattern;
import ca.nengo.util.TimeSeries;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.util.*;

/**
 * TODO
 * 
 * @author TODO
 */
public class SimulatorDataModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;
    private final ProbePlotHelper plotterStrategy;

    /**
     * Creates a new node in the parent only if a node with the same name does
     * not already exist
     * 
     * @param parent
     * @param newNodeName
     */
    private static SortableMutableTreeNode createSortableNode(DefaultMutableTreeNode parent,
            Node neoNode) {
        String name = neoNode.name();

        if (neoNode instanceof Network) {
            name += " (Network)";
        }

        SortableMutableTreeNode newNode = findInDirectChildren(parent, name);

        if (newNode == null) {
            newNode = new NengoTreeNode(name, neoNode);
            parent.add(newNode);
        }

        return newNode;
    }

    //	private static SortableMutableTreeNode createSortableNode(DefaultMutableTreeNode parent,
    //			String name) {
    //
    //		SortableMutableTreeNode newNode = findInDirectChildren(parent, name);
    //
    //		if (newNode == null) {
    //			newNode = new SortableMutableTreeNode(name);
    //			parent.add(newNode);
    //		}
    //
    //		return newNode;
    //	}

    /**
     * Using O(n) search. Performance can be improved here.
     * 
     * @param parent
     * @param name
     * @return
     */
    private static SortableMutableTreeNode findInDirectChildren(DefaultMutableTreeNode parent,
            String name) {

        Enumeration<?> enumeration = parent.children();
        SortableMutableTreeNode targetNode = null;

        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj instanceof SortableMutableTreeNode) {
                SortableMutableTreeNode node = (SortableMutableTreeNode) obj;

                if (node.getUserObject().toString().compareTo(name) == 0) {
                    targetNode = node;
                    break;
                }
            } else {
                throw new UnsupportedOperationException("An unsupported Node type was found");
            }
        }
        return targetNode;
    }

    private static void sortTree(MutableTreeNode node) {
        if (node instanceof SortableMutableTreeNode) {
            ((SortableMutableTreeNode) node).sort();
        }

        if (!node.isLeaf()) {
            Enumeration<?> enumeration = node.children();

            while (enumeration.hasMoreElements()) {
                Object obj = enumeration.nextElement();

                if (obj instanceof MutableTreeNode) {
                    sortTree(((MutableTreeNode) obj));
                }
            }
        }
    }

    private final HashSet<String> nameLUT = new HashSet<String>();

    private final Hashtable<Integer, DefaultMutableTreeNode> topLevelNetworks = new Hashtable<Integer, DefaultMutableTreeNode>();

    /**
     * TODO
     */
    public SimulatorDataModel() {
        super(new DefaultMutableTreeNode("root"));
        plotterStrategy = ProbePlotHelper.getInstance();
        this.setRoot(new DefaultMutableTreeNode("Results"));
    }

    private boolean addSpikePatterns(DefaultMutableTreeNode top, Network network) {
        Node[] nodes = network.getNodes();

        boolean childCollecting = false;
        for (Node node : nodes) {
            if (node instanceof Group) {
                Group group = (Group) node;

                if (group.isCollectingSpikes()) {
                    SortableMutableTreeNode ensNode = createSortableNode(top, group);
                    /*
                     * Make a clone of the data
                     */
                    SpikePattern spikePattern = (SpikePattern) Util.cloneSerializable(group.getSpikePattern());
                    DefaultMutableTreeNode spNode = new SpikePatternNode(spikePattern);
                    ensNode.add(spNode);

                    childCollecting = true;
                }

            } else if (node instanceof Network) {
                Network subNet = (Network) node;

                DefaultMutableTreeNode netNode = createSortableNode(top, subNet);

                if(!addSpikePatterns(netNode, subNet)) {
                    top.remove(top.getIndex(netNode));
                } else {
                    childCollecting = true;
                }

            }
        }
        return childCollecting;
    }

    /**
     * Recursively searches down Node hierarchy looking for a specific Node.
     * Returns a direct child of currentNode that is an ancestor of targetNode.
     * If targetNode could not be found then null is returned.
     * 
     * @param currentNode The root Node to begin searching from.
     * @param targetNode The Node that is being looked for.
     * @return Node The child of currentNode that is an ancestor of targetNode.
     * @author Steven Leigh
     */
    private Node findNodeAncestor (Node currentNode, Node targetNode){
        Node[] nodes;

        if (currentNode instanceof Network){
            nodes=((Network) currentNode).getNodes();
        }else if (currentNode instanceof AbstractGroup){
            nodes=((AbstractGroup) currentNode).getNodes();
        }else {
            return null;
        }

        for (Node node : nodes){
            if (node.equals(targetNode)){
                return node;  //target node was found so begin propagating back up hierarchy
            }else{
                if (findNodeAncestor(node,targetNode)!=null){
                    return node;  //target node was found and we are now propagating back up hierarchy
                }
            }
        }
        return null;  //target node was not found in this branch

    }

    private void addTimeSeries(DefaultMutableTreeNode top, Network topnetwork, Network probenetwork) {
        Probe[] probes = probenetwork.getSimulator().getProbes();
        for (Probe probe : probes) {
            DefaultMutableTreeNode top0 = top;

            Probeable target = probe.getTarget();
            if (!(target instanceof Node)) {
                Util.Assert(false, "Probe target is not a node");
                continue;
            }

            //create branch down to target node
            Node ancestor=findNodeAncestor(topnetwork, (Node)target);
            while(ancestor!=null && !(ancestor.equals(target))){
                top0=createSortableNode(top0, ancestor);
                ancestor=findNodeAncestor(ancestor, (Node)target);
            }

            if(ancestor==null || !(ancestor.equals(target))){
                Util.Assert(false, "Probe target could not be found in Network");
                continue;
            }


            SortableMutableTreeNode targetNode = createSortableNode(top0, (Node) target);

            /*
             * Make a clone of the data
             */
            TimeSeries probeData = (TimeSeries) Util.cloneSerializable(probe.getData());

            DefaultMutableTreeNode stateNode = new ProbeDataNode(probeData,
                    probe.getStateName(), plotterStrategy.isApplyTauFilterByDefault(probe));

            targetNode.add(stateNode);

        }

        Node[] nodes = probenetwork.getNodes();
        for(Node node : nodes)
        {
            if(node instanceof Network) {
                addTimeSeries(top, topnetwork, (Network)node);
            }
        }
    }

    /**
     * Captures the current data from a network and copies it to this simulator
     * data tree
     * @param network TODO
     * @return TODO
     */
    public SortableMutableTreeNode captureData(Network network) {

        Util.Assert(network.getSimulator() != null, "No simulator available for data view");

        DefaultMutableTreeNode networkNode = topLevelNetworks.get(network.hashCode());

        if (networkNode != null && networkNode.getParent() == null) {
            // Node has already been removed from the tree by the user
            //
            topLevelNetworks.remove(network.hashCode());
            nameLUT.remove(network.name());
            networkNode = null;
        }

        if (networkNode == null) {
            String originalName = network.name();

            String name = originalName;

            /*
             * Ensure a unique name
             */
            int i = 1;
            while (nameLUT.contains(name)) {
                name = String.format("%s (%d)", originalName, i++);
            }
            nameLUT.add(name);

            networkNode = new DefaultMutableTreeNode(name);
            topLevelNetworks.put(network.hashCode(), networkNode);

            this.insertNodeInto(networkNode, ((MutableTreeNode) getRoot()), 0);
        }

        Calendar cal = new GregorianCalendar();

        SortableMutableTreeNode captureNode = new SortableMutableTreeNode("Simulation "
                + cal.get(Calendar.HOUR_OF_DAY) + 'h' + cal.get(Calendar.MINUTE) + 'm'
                + cal.get(Calendar.SECOND) + "s " + cal.get(Calendar.MONTH) + 'M'
                + cal.get(Calendar.DATE) + 'D');

        addSpikePatterns(captureNode, network);
        addTimeSeries(captureNode, network, network);
        sortTree(captureNode);

        if (captureNode.getChildCount() == 0) {
            captureNode.add(new DefaultMutableTreeNode("no data collected"));
        }

        this.insertNodeInto(captureNode, networkNode, 0);
        return captureNode;
    }

    ArrayList<String> parseEnsembleName(String name)
    {
        ArrayList<String> result = new ArrayList<String>();
        String net_name;

        name = name.substring(1, name.length()-1);
        try
        {
            net_name = name.substring(0, name.indexOf('['));
        }
        catch(IndexOutOfBoundsException ioobe)
        {
            result.add(name);
            return result;
        }
        String recur_name = name.substring(name.indexOf('['));

        result.add(net_name);
        result.addAll(parseEnsembleName(recur_name));
        return result;


    }

}
