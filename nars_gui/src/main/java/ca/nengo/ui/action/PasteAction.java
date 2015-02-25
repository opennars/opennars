/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PasteAction.java". Description:
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
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.model.NodeContainer.ContainerException;
import ca.nengo.ui.util.NengoClipboard;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * TODO
 * 
 * @author TODO
 */
public class PasteAction extends StandardAction {

    private static final long serialVersionUID = 1L;

    private final NodeContainer nodeContainer;
    
    private Double posX = null;
    private Double posY = null;
    
    private boolean isFromTopMenu = false;

    /**
     * @param description TODO
     * @param nodeContainer TODO
     */
    public PasteAction(String description, NodeContainer nodeContainer, boolean fromTopMenu) {
        super(description);
        isFromTopMenu = fromTopMenu;
        this.nodeContainer = nodeContainer;
    }

    @Override
    protected void action() throws ActionException {
    	NengoClipboard clipboard = AbstractNengo.getInstance().getClipboard();
        if (clipboard.hasContents()) {
			ArrayList<Node> nodes = clipboard.getContents();
			ArrayList<Point2D> offsets = clipboard.getOffsets();
			WorldImpl clipboardSrcWorld = clipboard.getSourceWorld();
        	
        	for (int i = 0; i < nodes.size(); i++) {
        		Node node = nodes.get(i);
        		try {
        			CreateModelAction.ensureNonConflictingName(node, nodeContainer);
        			if (posX == null || posY == null) {
        				nodeContainer.addNodeModel(node, posX, posY);
        			} else {
        				nodeContainer.addNodeModel(node, posX + offsets.get(i).getX(), posY + offsets.get(i).getY());
        			}
        		} catch (ContainerException e) {
        			// Did the attempt to paste to the mouse location fail?
        			// If so, try to paste into the network that the clipboard contents came from
        			if (isFromTopMenu) {
        				try {
        					CreateModelAction.ensureNonConflictingName(node, ((NodeContainer)clipboardSrcWorld));
        					((NodeContainer)clipboardSrcWorld).addNodeModel(node);
        				} catch (ContainerException ex) {
        					throw new ActionException(ex);
        				}
        			} else {
        				throw new ActionException(e);
        			}
        		}
        	}
        } else {
            throw new ActionException("Clipboard is empty");
        }
    }
    
    public void setPosition(Double x, Double y) {
        posX = x;
        posY = y;
    }
}
