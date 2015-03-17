/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NengoClipboard.java". Description: 
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

package ca.nengo.ui.util;

import ca.nengo.model.Node;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;

import java.awt.geom.Point2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;

public class NengoClipboard {

	private ArrayList<Node> selectedObjs = null;
	private ArrayList<Point2D> objectOffsets = null;
	private WorldImpl sourceWorld = null;
	
	///////////////////////////////////////////////////////////////////////////
	/// Listeners

	public static interface ClipboardListener {
		public void clipboardChanged();
	}

	final LinkedList<ClipboardListener> listeners = new LinkedList<ClipboardListener>();

	public void addClipboardListener(ClipboardListener listener) {
		listeners.add(listener);
	}

	public void removeClipboardListener(ClipboardListener listener) {
		if (!listeners.contains(listener)) {
			listeners.remove(listener);
		} else {
			throw new InvalidParameterException();
		}
	}
	
	private void fireChanged() {
		for (ClipboardListener listener : listeners) {
			listener.clipboardChanged();
		}
	}

	///////////////////////////////////////////////////////////////////////////
	/// Accessors and mutators
	
	public boolean hasContents() {
		return (selectedObjs != null && selectedObjs.size() > 0); 
	}
	
	public ArrayList<String> getContentsNames() {
		if (hasContents()) {
			ArrayList<String> result = new ArrayList<String>(selectedObjs.size());
			for (Node obj : selectedObjs)
				result.add(obj.name());
			return result;
		} else {
			return null;
		}
	}

	public ArrayList<Node> getContents() {
		if (hasContents()) {
			ArrayList<Node> clonedObjects = new ArrayList<Node>();
			for (Node curObj : selectedObjs) {
				try {
					// If the object supports cloning, use it to make another model
					curObj = curObj.clone();
					
					clonedObjects.add(curObj);
				} catch (CloneNotSupportedException e) {
					curObj = null;
				}
			}
			
			return clonedObjects;
		} else {
			return null;
		}
	}

	public ArrayList<Point2D> getOffsets() {
		return objectOffsets;
	}

	public WorldImpl getSourceWorld() {
		return sourceWorld;
	}
	
	public void setContents(ArrayList<Node> nodes, ArrayList<Point2D> objOffsets) {
		setContents(nodes, objOffsets, null);
	}
	
	public void setContents(ArrayList<Node> nodes, ArrayList<Point2D> objOffsets, WorldImpl srcWorld) {
		selectedObjs = nodes;
		objectOffsets = objOffsets;
		sourceWorld = srcWorld;
		fireChanged();
	}
}
