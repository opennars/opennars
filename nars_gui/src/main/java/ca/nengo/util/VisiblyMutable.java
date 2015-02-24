/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "VisiblyMutable.java". Description: 
"An object that fires an event when its properties change in such a way that it expects 
  the user interface to display it differently"

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

/*
 * Created on 28-Jan-08
 */
package ca.nengo.util;

import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;

/**
 * An object that fires an event when its properties change in such a way that it expects 
 * the user interface to display it differently. This allows the user interface to 
 * update when the object is changed through another means, such as scripting.   
 *   
 * @author Bryan Tripp
 */
public interface VisiblyMutable {

	/**
	 * @param listener Listener to add
	 */
	public void addChangeListener(Listener listener);
	
	/**
	 * @param listener Listener to remove
	 */
	public void removeChangeListener(Listener listener);

	/**
	 * A listener for changes to a VisiblyMutable object. 
	 *   
	 * @author Bryan Tripp
	 */
	public static interface Listener {
		
		/**
		 * @param e An object that has changed in some way (all properties
		 * 		that influence the display of the object should be checked)  
		 */
		public void changed(Event e) throws StructuralException;
	}
	
	/**
	 * Encapsulates a change to a VisiblyMutable object. The event doesn't 
	 * specify which changes occurred, just the object that changed. Therefore
	 * all properties of the object that influence its display should be checked.  
	 *  
	 * @author Bryan Tripp
	 */
	public static interface Event {
		
		/**
		 * @return An object that has changed in some way 
		 */
		public VisiblyMutable getObject();		

	}
	
	/**
	 * Encapsulates a change in the name of a VisiblyMutable object. This event means 
	 * that no other changes to the object have occurred except for the name change.  
	 * 
	 * @author Bryan Tripp
	 */
	public static interface NameChangeEvent extends Event {
		
		/**
		 * @return The previous name of the object
		 */
		public String getOldName();
		
		/**
		 * @return The new name of the object
		 */
		public String getNewName();
	}
	
	/**
	 * Encapsulates a "node removed" change in the VisiblyMutable object.
	 */
	public static interface NodeRemovedEvent extends Event {
		/**
		 * @return the node that has been removed
		 */
		public Node getNode();
	}
	
}
