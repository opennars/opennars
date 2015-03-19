/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NodeLayout.java". Description: 
"Layout of nodes which is serializable
  
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

package ca.nengo.ui.model.viewer;

import ca.nengo.ui.model.UINeoNode;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Layout of nodes which is serializable
 * 
 * @author Shu Wu
 */
public class NodeLayout implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Name of the layout
	 */
	private final String layoutName;

	/**
	 * Whether elastic layout is enabled
	 */
	private final boolean elasticMode;

	/**
	 * Node positions referenced by name
	 */
	private final HashMap<String, PointSerializable> nodePositions;

	/**
	 * Saved view bounds
	 */
	private final Rectangle2D savedViewBounds;

	/**
	 * @param layoutName
	 *            Name of the layout
	 * @param world
	 *            Viewer containing nodes
	 */
	public NodeLayout(String layoutName, NodeViewer world, boolean elasticMode) {
		super();
		this.layoutName = layoutName;
		this.elasticMode = elasticMode;

		nodePositions = new HashMap(world.getUINodes().size());

		for (UINeoNode object : world.getUINodes()) {
			addPosition(object, object.getOffset());
		}

		savedViewBounds = world.getSky().getViewBounds();

	}

	/**
	 * @param nodeName
	 *            Name of node
	 * @param position
	 *            Position of node
	 */
	private void addPosition(UINeoNode wo, Point2D position) {
		nodePositions.put(wo.node().name(), new PointSerializable(
				position));
	}

	/**
	 * @return Layout name
	 */
	public String getName() {
		return layoutName;
	}

	/**
	 * @param nodeName
	 *            Name of node
	 * @return Position of node
	 */
	public Point2D getPosition(UINeoNode node) {
		PointSerializable savedPosition = nodePositions.get(node.node()
				.name());
		if (savedPosition != null) {
			return savedPosition.toPoint2D();
		} else {
			return null;
		}
	}

	/**
	 * @return Saved view bounds
	 */
	public Rectangle2D getSavedViewBounds() {
		return savedViewBounds;
	}

	public boolean elasticModeEnabled() {
		return elasticMode;
	}

}

/**
 * Wraps point2D in a serializable wrapper
 * 
 * @author Shu Wu
 */
class PointSerializable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final double x;
    final double y;

	public PointSerializable(Point2D point) {
		x = point.getX();
		y = point.getY();
	}

	public Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}

}
