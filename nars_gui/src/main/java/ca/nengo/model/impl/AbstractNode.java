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
import ca.nengo.util.VisiblyChanges;
import ca.nengo.util.VisiblyChangesUtils;

import java.util.*;

/**
 * A base implementation of Node.
 *
 * @author Bryan Tripp
 */
public abstract class AbstractNode implements Node<Node> {

	private static final long serialVersionUID = 1L;

	private String myName;
	private SimulationMode myMode;
	private Map<String, NSource> mySources;
	private Map<String, NTarget> myTargets;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;

	/**
	 * @param name Name of Node
	 * @param sources List of Origins from the Node
	 * @param targets List of Terminations onto the Node
	 */
	public AbstractNode(String name, List<NSource> sources, List<NTarget> targets) {
        super();
		myName = name;
		myMode = SimulationMode.DEFAULT;


        setOutputs(sources);

        setInputs(targets);
	}

    @Override
    public int hashCode() {
        return myName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractNode)
            return myName.equals(((AbstractNode)obj).myName);
        return false;
    }

    public void setOutputs(NSource... s) {
        if (mySources == null && s.length > 0)
            mySources = new LinkedHashMap<String, NSource>(10);
        for (NSource o : s) {
            mySources.put(o.getName(), o);
        }
    }
    public void setOutputs(List<NSource> t) {
        setOutputs(t.toArray(new NSource[t.size()]));
    }

    public void setInputs(NTarget... s) {
        if ((s!=null) && (s.length > 0)) {
            if (myTargets==null)
                myTargets = new LinkedHashMap<String, NTarget>(10);
            for (NTarget o : s) {
                if (o!=null)
                    myTargets.put(o.getName(), o);
            }
        }
    }
    public void setInputs(List<NTarget> t) {
        setInputs(t.toArray(new NTarget[t.size()]));
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
	 * @see ca.nengo.model.Node#name()
	 */
	public String name() {
		return myName;
	}

	/**
	 * @param name The new name
	 */
	public void setName(String name) throws StructuralException {
		VisiblyChangesUtils.nameChanged(this, name(), name, myListeners);
		myName = name;
	}

	/**
	 * @see ca.nengo.model.Node#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
		return mySources.get(name);
	}

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
	public NSource[] getSources() {
        if (mySources==null)
            return emptySourceArray;
        java.util.Collection<NSource> var = mySources.values();
        return var.toArray(new NSource[var.size()]);
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		return myTargets.get(name);
	}

    final static NSource[] emptySourceArray = new NSource[0];
    final static NTarget[] emptyTargetArray = new NTarget[0];
	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
        if (myTargets == null)
            return emptyTargetArray;
        java.util.Collection<NTarget> var = myTargets.values();
        return var.toArray(new NTarget[var.size()]);
	}

    public int getTargetCount() {
        return myTargets.size();
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
	 * @see ca.nengo.util.VisiblyChanges#addChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void addChangeListener(Listener listener) {
		if (myListeners == null) {
			myListeners = new ArrayList<Listener>(2);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#removeChangeListener(ca.nengo.util.VisiblyChanges.Listener)
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
