/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CutAction.java". Description:
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

package ca.nengo.ui.action;

import ca.nengo.model.Node;
import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.model.UINeoNode;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO
 * 
 * @author TODO
 */
public class CutAction extends StandardAction {
    private final Collection<UINeoNode> nodeUIs;

    /**
     * @param description TODO
     * @param nodeUI TODO
     */
    public CutAction(String description, Collection<UINeoNode> nodeUIs) {
        super(description);
        this.nodeUIs = nodeUIs;
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected final void action() throws ActionException {
    	ArrayList<Node> nodes = new ArrayList<Node>();
    	ArrayList<Point2D> offsets = new ArrayList<Point2D>();
    	
    	// compute the mean of all the nodes' positions
    	Point2D averagePoint = new Point2D.Double(0, 0);
    	for (UINeoNode nodeUI : nodeUIs) {
    		averagePoint.setLocation(averagePoint.getX() + nodeUI.getOffset().getX(), averagePoint.getY() + nodeUI.getOffset().getY());
    	}
    	averagePoint.setLocation(averagePoint.getX() / nodeUIs.size(), averagePoint.getY() / nodeUIs.size());
    	
    	WorldImpl world = null;
    	for (UINeoNode nodeUI : nodeUIs) {
    		if (world == null && nodeUI.getWorld() != null) {
    			world = nodeUI.getWorld();
    		}
    		try {
    			nodes.add(nodeUI.node().clone());
    			offsets.add(new Point2D.Double(nodeUI.getOffset().getX() - averagePoint.getX(), nodeUI.getOffset().getY() - averagePoint.getY()));
    		} catch (CloneNotSupportedException e) {
    			throw new ActionException("Could not clone node: ", e);
    		}
	    }

        /*
         * This removes the node from its parent and externalities
         */
    	for (UINeoNode nodeUI : nodeUIs) {
    		nodeUI.destroyModel();
    	}

        AbstractNengo.getInstance().getClipboard().setContents(nodes, offsets, world);
    }

}
