/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "VisiblyMutableUtils.java". Description: 
"Utility methods for VisiblyMutable objects"

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

import java.util.ArrayList;

/**
 * Utility methods for VisiblyMutable objects. 
 * 
 * @author Bryan Tripp
 */
public class VisiblyChangesUtils {
	
	//private static final Logger ourLogger = LogManager.getLogger(VisiblyChangesUtils.class);

	/**
	 * Notifies listeners of a change to the given VisiblyMutable object.  
	 * 
	 * @param vm The changed VisiblyMutable object
	 * @param listeners List of things listening for changes
	 */
	public static void changed(final VisiblyChanges vm, ArrayList<VisiblyChanges.Listener> listeners) {
		VisiblyChanges.Event event = new MyEvent(vm);
		
		try {
			fire(event, listeners);
		} catch (StructuralException e) {
			System.err.println(e);
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isValidName(final String name) {
		if (name.indexOf('.')>=0) return false;
		if (name.indexOf(':')>=0) return false;
		return true;
	}
	

	/**
	 * @param vm The changed VisiblyMutable object
	 * @param oldName The old (existing) name of the VisiblyMutable 
	 * @param newName The new (replacement) name of the VisiblyMutable
	 * @param listeners List of things listening for changes
	 * @throws StructuralException if the new name is invalid
	 */
	public static void nameChanged(final VisiblyChanges vm, final String oldName, final String newName,
			ArrayList<VisiblyChanges.Listener> listeners) throws StructuralException {
			
		if (!isValidName(newName)) throw new StructuralException("Name '"+newName+"' must not contain '.' or ':'");
		
		VisiblyChanges.NameChangeEvent event = new VCChangeEvent(newName, oldName, vm);
		
		try {
			fire(event, listeners);			
		} catch (RuntimeException e) {
			throw new StructuralException(e.getMessage(), e);
		}
	}
	
	/**
	 * Notifies listeners that a node has been removed within the given object.  
	 * 
	 * @param vm The changed VisiblyMutable object
	 * @param n The node that was removed
	 * @param listeners List of things listening for changes
	 */
	public static void nodeRemoved(final VisiblyChanges vm, final Node n, ArrayList<VisiblyChanges.Listener> listeners) {
		VisiblyChanges.NodeRemovedEvent event = new VCRemovedEvent(vm, n);
		
		try {
			fire(event, listeners);
		} catch (StructuralException e) {
            System.err.println(e);
			throw new RuntimeException(e);
		}
	}
	
	private static void fire(final VisiblyChanges.Event event, final ArrayList<VisiblyChanges.Listener> listeners) throws StructuralException {
		if (listeners != null) {
            int n = listeners.size();
            for (int i = 0; i < n; i++) {
                VisiblyChanges.Listener listener = listeners.get(i);
				listener.changed(event);
			}			
		}
	}

    private static class MyEvent implements VisiblyChanges.Event {
        private final VisiblyChanges vm;

        public MyEvent(VisiblyChanges vm) {
            this.vm = vm;
        }

        public VisiblyChanges getObject() {
            return vm;
        }
    }

    private static class VCChangeEvent implements VisiblyChanges.NameChangeEvent {

        private final String newName;
        private final String oldName;
        private final VisiblyChanges vm;

        public VCChangeEvent(String newName, String oldName, VisiblyChanges vm) {
            this.newName = newName;
            this.oldName = oldName;
            this.vm = vm;
        }

        public String getNewName() {
            return newName;
        }

        public String getOldName() {
            return oldName;
        }

        public VisiblyChanges getObject() {
            return vm;
        }

    }

    private static class VCRemovedEvent implements VisiblyChanges.NodeRemovedEvent {
        private final VisiblyChanges vm;
        private final Node n;

        public VCRemovedEvent(VisiblyChanges vm, Node n) {
            this.vm = vm;
            this.n = n;
        }

        public VisiblyChanges getObject() {
            return vm;
        }

        public Node getNode()
        {
            return n;
        }
    }
}
