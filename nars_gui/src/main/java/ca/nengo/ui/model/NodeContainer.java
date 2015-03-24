/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NodeContainer.java". Description: 
"A Container of PNeoNode
  
  @author Shu Wu"

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

package ca.nengo.ui.model;

import ca.nengo.model.Node;
import ca.nengo.ui.lib.UIException;
import ca.nengo.ui.lib.world.WorldObject;

import java.awt.geom.Point2D;
import java.util.Iterator;

/**
 * A Container of PNeoNode
 * 
 * @author Shu Wu
 */
public interface NodeContainer {

	/**
	 * Adds a child node to the container
	 * 
	 * @param node
	 *            Node to be added
	 * @return UI Node Wrapper
	 */
	public UINeoNode addNodeModel(Node node) throws ContainerException;

	/**
	 * @param node
	 *            Node to be added
	 * @param posX
	 *            X Position of node
	 * @param posY
	 *            Y Position of node
	 * @return
	 */
	public UINeoNode addNodeModel(Node node, Double posX, Double posY) throws ContainerException;
    public WorldObject addNodeModel(WorldObject obj, Double posX, Double posY) throws ContainerException;

	public Point2D localToView(Point2D localPoint);

	public Node getNodeModel(String name);

    public Iterable<? extends WorldObject> getWorldObjects();

    public Iterator<Node> getNodeModels();



    public static class ContainerException extends UIException {
		private static final long serialVersionUID = 1L;

		public ContainerException() {
			super();
		}

		public ContainerException(String arg0) {
			super(arg0);
		}

	}

}
