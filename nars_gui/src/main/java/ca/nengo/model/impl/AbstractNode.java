/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "AbstractNode.java". Description:
"A base implementation of Node"

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
 * Created on 9-Mar-07
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.util.VisiblyMutable;
import ca.nengo.util.VisiblyMutableUtils;

import java.util.*;

/**
 * A base implementation of Node.
 *
 * @author Bryan Tripp
 */
public abstract class AbstractNode implements Node {

	private static final long serialVersionUID = 1L;

	private String myName;
	private SimulationMode myMode;
	private final Map<String, Source> myOrigins;
	private final Map<String, Target> myTerminations;
	private String myDocumentation;
	private transient List<VisiblyMutable.Listener> myListeners;

	/**
	 * @param name Name of Node
	 * @param sources List of Origins from the Node
	 * @param targets List of Terminations onto the Node
	 */
	public AbstractNode(String name, List<Source> sources, List<Target> targets) {
		myName = name;
		myMode = SimulationMode.DEFAULT;

		myOrigins = new LinkedHashMap<String, Source>(10);
		for (Source o : sources) {
			myOrigins.put(o.getName(), o);
		}

		myTerminations = new LinkedHashMap<String, Target>(10);
		for (Target t : targets) {
			myTerminations.put(t.getName(), t);
		}
	}

    public AbstractNode(String name) {
        this(name, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    /**
	 * @see ca.nengo.model.Node#getMode()
	 */
	public SimulationMode getMode() {
		return myMode;
	}

	/**
	 * @see ca.nengo.model.Node#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @param name The new name
	 */
	public void setName(String name) throws StructuralException {
		VisiblyMutableUtils.nameChanged(this, getName(), name, myListeners);
		myName = name;
	}

	/**
	 * @see ca.nengo.model.Node#getOrigin(java.lang.String)
	 */
	public Source getOrigin(String name) throws StructuralException {
		return myOrigins.get(name);
	}

	/**
	 * @see ca.nengo.model.Node#getOrigins()
	 */
	public Source[] getOrigins() {
        java.util.Collection<Source> var = myOrigins.values();
        return var.toArray(new Source[var.size()]);
	}

	/**
	 * @see ca.nengo.model.Node#getTermination(java.lang.String)
	 */
	public Target getTermination(String name) throws StructuralException {
		return myTerminations.get(name);
	}

	/**
	 * @see ca.nengo.model.Node#getTerminations()
	 */
	public Target[] getTerminations() {
        java.util.Collection<Target> var = myTerminations.values();
        return var.toArray(new Target[var.size()]);
	}

	/**
	 * Does nothing.
	 *
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public abstract void run(float startTime, float endTime) throws SimulationException;

	/**
	 * @see ca.nengo.model.Node#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
		myMode = mode;
	}

	/**
	 * Does nothing.
	 *
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public abstract void reset(boolean randomize);

	/**
	 * @see ca.nengo.model.Node#getDocumentation()
	 */
	public String getDocumentation() {
		return myDocumentation;
	}

	/**
	 * @see ca.nengo.model.Node#setDocumentation(java.lang.String)
	 */
	public void setDocumentation(String text) {
		myDocumentation = text;
	}

	/**
	 * @see ca.nengo.util.VisiblyMutable#addChangeListener(ca.nengo.util.VisiblyMutable.Listener)
	 */
	public void addChangeListener(Listener listener) {
		if (myListeners == null) {
			myListeners = new ArrayList<Listener>(2);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyMutable#removeChangeListener(ca.nengo.util.VisiblyMutable.Listener)
	 */
	public void removeChangeListener(Listener listener) {
		myListeners.remove(listener);
	}

	/**
	 * Performs a shallow copy. Origins and Terminations are not cloned, because generally they
	 * will have to be reparameterized, at least to point to the new Node.
	 */
	@Override
	public Node clone() throws CloneNotSupportedException {
		Node result = (Node) super.clone();
		return result;
	}

}
