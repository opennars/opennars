/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "AbstractEnsemble.java". Description:
"Abstract class that can be used as a basis for Ensemble implementations.

  @author Bryan Tripp"

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
 * Created on 31-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.neural.PreciseSpikeOutput;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.util.SpikePattern;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.VisiblyChanges;
import ca.nengo.util.VisiblyChangesUtils;
import ca.nengo.util.impl.SpikePatternImpl;
import ca.nengo.util.impl.TimeSeriesImpl;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * Abstract class that can be used as a basis for Ensemble implementations.
 *
 * @author Bryan Tripp
 */
public abstract class AbstractGroup implements Group<Node>, Probeable, VisiblyChanges {

	private static final long serialVersionUID = -5498397418584843304L;

	private static final Logger ourLogger = LogManager.getLogger(AbstractGroup.class);

	private String myName;
	private Map<String, List<Integer>> myStateNames; // for Probeable
	private SimulationMode myMode;
	private transient SpikePatternImpl mySpikePattern;
	private boolean myCollectSpikesFlag;
	private int myCollectSpikesRatio = 1;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;
	private Node[] myNodes;
	private Map<String, NSource> mySources;
	private Map<String, GroupTarget> myTargets;
	
	private transient Map<String, Object> myMetadata;

	/**
	 * Note that setMode(SimulationMode.DEFAULT) is called at construction time.
	 *
	 * @param name Unique name of Ensemble
	 * @param nodes Nodes that Ensemble contains
	 */
	public AbstractGroup(String name, Node[] nodes) {
		myName = name;
		myNodes = nodes;
		mySpikePattern = new SpikePatternImpl(nodes.length);
		myCollectSpikesFlag = false;

		init();

		setMode(SimulationMode.DEFAULT);
	}

	private void init() {
		// Using LinkedHashMap to keep ordering
		mySources = new LinkedHashMap<String, NSource>(10);
		NSource[] sources = findOrigins(this, myNodes);
		for (NSource source : sources) {
		    mySources.put(source.getName(), source);
		}

		myTargets = new LinkedHashMap<String, GroupTarget>(10);
		GroupTarget[] terminations = findTargets(this, myNodes);
		for (GroupTarget termination : terminations) {
		    myTargets.put(termination.getName(), termination);
		}

        myStateNames = findStateNames(myNodes);
        
        if(myListeners == null){
        	myListeners = new ArrayList<Listener>(3);
        }
	}
	
	public Object getMetadata(String key) {
		if (myMetadata==null) myMetadata = new LinkedHashMap<String, Object>(2);
		return myMetadata.get(key);
	}
	public void setMetadata(String key, Object value) {
		if (myMetadata==null) myMetadata = new LinkedHashMap<String, Object>(2);		
		myMetadata.put(key, value);
	}
	

	/**
	 * Replaces the set of nodes inside the Ensemble
	 */
    public void redefineNodes(Node[] nodes) {
		myNodes=nodes;
		mySpikePattern = new SpikePatternImpl(myNodes.length);
		//setupNodeRunners(numNodeRunners);

		init();
	}

	/**
	 * @see ca.nengo.model.Group#name()
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
	 * @see ca.nengo.model.Group#getNodes()
	 */
    public Node[] getNodes() {
		return myNodes;
	}

	/**
	 * When this method is called, setMode(...) is called on each Node in the Ensemble.
	 * Each Node will then run in the mode that is closest to the requested mode (this
	 * could be different for different Node). Note that at Ensemble construction time,
	 * setMode(SimulationMode.DEFAULT) is called.
	 *
	 * @see ca.nengo.model.Group#setMode(ca.nengo.model.SimulationMode)
	 */
    public void setMode(SimulationMode mode) {
		myMode = mode;

		for (Node myNode : myNodes) {
			myNode.setMode(mode);
		}
		
		// Added for issue #310: Setting mode can now be a visible change
		fireVisibleChangeEvent();
	}


	/**
	 * Note that this reflects the latest mode requested of the Ensemble, and that individual
	 * Neurons may run in different modes (see setMode).
	 *
	 * @see ca.nengo.model.Group#getMode()
	 */
    public SimulationMode getMode() {
		return myMode;
	}


	/**
	 * Runs each neuron in the Ensemble.
	 *
	 * @see ca.nengo.model.Group#run(float, float)
	 */
    public void run(float startTime, float endTime) throws SimulationException {
		if (mySpikePattern == null) {
			mySpikePattern = new SpikePatternImpl(myNodes.length);
		}

		for (int i = 0; i < myNodes.length; i++) {
			myNodes[i].run(startTime, endTime);

			if (myCollectSpikesFlag && (myCollectSpikesRatio == 1 || i % myCollectSpikesRatio == 0)) {
				try {
					Object output = myNodes[i].getSource(Neuron.AXON).get();
					if (output instanceof PreciseSpikeOutput) {
						PreciseSpikeOutput precise=((PreciseSpikeOutput) output);
						if (precise.getValues()[0]) {
							mySpikePattern.addSpike(i, endTime+precise.getSpikeTimes()[0]);
						}
					} else if (output instanceof SpikeOutput && ((SpikeOutput) output).getValues()[0]) {
						mySpikePattern.addSpike(i, endTime);
					}
				} catch (StructuralException e) {
					ourLogger.warn("Ensemble has been set to collect spikes, but not all components have Origin Neuron.AXON", e);
				}
			}
		}
	}

	/**
	 * Resets each Node in this Ensemble.
	 *
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
    public void reset(boolean randomize) {
		for (Node myNode : myNodes) {
			myNode.reset(randomize);
		}
		for (NTarget t : myTargets.values()) {
			t.reset(randomize);
		}


		mySpikePattern = new SpikePatternImpl(myNodes.length);
	}

	/**
	 * @see ca.nengo.model.Group#getSource(java.lang.String)
	 */
    public NSource getSource(String name) throws StructuralException {
		return mySources.get(name);
	}

	/**
	 * @see ca.nengo.model.Group#getTarget(java.lang.String)
	 */
    public NTarget getTarget(String name) throws StructuralException {
		return myTargets.get(name);
	}

    /**
     * @param name Name of the Origin to remove from the ensemble
     * @return the removed Origin object
     * @throws StructuralException if named Origin does not exist
     * @see ca.nengo.model.ExpandableNode#removeTarget(java.lang.String)
     */
    public synchronized NSource removeSource(String name) throws StructuralException {
        if (mySources.containsKey(name)) {
            NSource result = mySources.remove(name);

            fireVisibleChangeEvent();
            return result;
        }
        throw new StructuralException("Origin " + name + " does not exist");
    }

    /**
     * @param name Name of the Termination to remove from the ensemble
     * @return the removed Termination object
     * @throws StructuralException if named Termination does not exist
     * @see ca.nengo.model.ExpandableNode#removeTarget(java.lang.String)
     */
    public synchronized NTarget removeTarget(String name) throws StructuralException {
        if (myTargets.containsKey(name)) {
            NTarget result = myTargets.remove(name);

            fireVisibleChangeEvent();
            return result;
        }

        throw new StructuralException("Termination " + name + " does not exist");
    }

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
    public NSource[] getSources() {
        ArrayList<NSource> result = new ArrayList<NSource>(10);
        for (NSource o : mySources.values()) {
            result.add(o);
        }
        return result.toArray(new NSource[result.size()]);
	}

	/**
	 * @see ca.nengo.model.Group#getTargets()
	 */
    public NTarget[] getTargets() {
	    ArrayList<NTarget> result = new ArrayList<NTarget>(10);
	    for (NTarget t : myTargets.values()) {
            result.add(t);
        }
	    return result.toArray(new NTarget[result.size()]);
	}

	/**
	 * @see ca.nengo.model.Group#collectSpikes(boolean)
	 */
    public void collectSpikes(boolean collect) {
		myCollectSpikesFlag = collect;
	}

	/**
	 * @see ca.nengo.model.Group#isCollectingSpikes()
	 */
    public boolean isCollectingSpikes() {
		return myCollectSpikesFlag;
	}

	/**
	 * @return Inverse of the proportion of nodes from which to collect spikes
	 */
	public int getCollectSpikesRatio() {
		return myCollectSpikesRatio;
	}

	/**
	 * @param n Inverse of the proportion of nodes from which to collect spikes
	 */
	public void setCollectSpikesRatio(int n) {
		myCollectSpikesRatio = n;
	}

	/**
	 * @see ca.nengo.model.Group#getSpikePattern()
	 */
    public SpikePattern getSpikePattern() {
		if (!myCollectSpikesFlag) {
            ourLogger.warn("Warning: collect spikes flag is off");
        }
		return mySpikePattern;
	}

	/**
	 * @param spikes The pattern of spikes (0.0f for not spiking, else? for spiking)
	 * @param endTime End time for the spike pattern
	 */
	public void setSpikePattern(float[] spikes, float endTime) {
		if(myCollectSpikesFlag) {
			if (mySpikePattern == null) {
				mySpikePattern = new SpikePatternImpl(myNodes.length);
			}

			for(int i = 0; i < myNodes.length; i++) {

				if(myCollectSpikesRatio == 1 || i % myCollectSpikesRatio == 0) {
					if(spikes[i] != 0.0f) {
						mySpikePattern.addSpike(i, endTime);
					}
				}
			}
		}
	}

    final static float[] emptyFloat = new float[0];

	/**
	 * @return Composite of Node states by given name. States of different nodes may be defined at different
	 * 		times, so only the states at the end of the most recent step are given. Only the first
	 * 		dimension of each Node state is included in the composite.
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
    public TimeSeries getHistory(String stateName) throws SimulationException {
		if (!myStateNames.containsKey(stateName)) {
			throw new SimulationException("The state " + stateName + " is unknown");
		}

		List<Integer> nodeNumbers = myStateNames.get(stateName);
		float[] firstNodeTimes = ((Probeable) myNodes[nodeNumbers.get(0).intValue()]).getHistory(stateName).getTimes();

		float[] times = emptyFloat;
		float[][] values = new float[0][];
		Units[] units = Units.uniform(Units.UNK, myNodes.length);

		if (firstNodeTimes.length >= 1) {
			times = new float[]{firstNodeTimes[firstNodeTimes.length - 1]};

			values = new float[][]{new float[myNodes.length]};
			for (int i = 0; i < myNodes.length; i++) {
				if (nodeNumbers.contains(Integer.valueOf(i))) {
					TimeSeries history = ((Probeable) myNodes[i]).getHistory(stateName);
					int index = history.getTimes().length - 1;
					values[0][i] = history.getValues()[index][0];
					if (i == 0) {
                        units[i] = history.getUnits()[0];
                    }
				}
			}
		}

		return new TimeSeriesImpl(times, values, units);
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
    public Properties listStates() {
		Properties result = new Properties();
		Iterator<String> keys = myStateNames.keySet().iterator();
		while (keys.hasNext()) {
			result.setProperty(keys.next(), "Composite of Node states by the same name");
		}
		return result;
	}
    
    public void stopProbing(String stateName){
	}

	/**
	 * Finds existing one-dimensional Origins by same name on the given Nodes, and groups
	 * them into EnsembleOrigins.
	 *
	 * @param nodes Nodes on which to look for Origins
	 * @return Ensemble Origins encompassing Node-level Origins
	 */
	private static NSource[] findOrigins(Node parent, Node[] nodes) {
		Map<String, List<NSource>> groups = group1DOrigins(nodes);
		Iterator<String> it = groups.keySet().iterator();
		List<NSource> result = new ArrayList<NSource>(10);
		while (it.hasNext()) {
			String name = it.next();
			List<NSource> group = groups.get(name);
			result.add(new GroupSource(parent, name, group.toArray(new NSource[group.size()])));
		}

		return result.toArray(new NSource[result.size()]);
	}

	/**
	 * @param nodes A list of Nodes in an Ensemble
	 * @return A grouping of one-dimensional origins on these nodes, by name
	 */
	private static Map<String, List<NSource>> group1DOrigins(Node[] nodes) {
		Map<String, List<NSource>> groups = new LinkedHashMap<String, List<NSource>>(10);

		for (Node node : nodes) {
			NSource[] sources = node.getSources();
			for (NSource source : sources) {
				if (source.getDimensions() == 1) {
					List<NSource> group = groups.get(source.getName());
					if (group == null) {
						group = new ArrayList<NSource>(nodes.length * 2);
						groups.put(source.getName(), group);
					}
					group.add(source);
				}
			}
		}

		return groups;
	}

	/**
	 * @param nodes A list of Nodes
	 * @return Names of one-dimensional origins that are shared by all the nodes
	 */
	public static List<String> findCommon1DOrigins(Node[] nodes) {
		List<String> result = get1DOriginNames(nodes[0]);

		for (int i = 1; i < nodes.length; i++) {
			result.retainAll(get1DOriginNames(nodes[i]));
		}

		return result;
	}

	private static List<String> get1DOriginNames(Node node) {
		List<String> result = new ArrayList<String>(10);
		NSource[] sources = node.getSources();
		for (NSource source : sources) {
			if (source.getDimensions() == 1) {
                result.add(source.getName());
            }
		}
		return result;
	}

	/**
	 * Finds existing one-dimensional Terminations by the same name on different nodes, and
	 * groups them into EnsembleTerminations.
	 *
	 * @param parent The ensemble to which new terminations will belong
	 * @param nodes Nodes on which to look for Terminations
	 * @return Ensemble Terminations encompassing Node-level Terminations
	 */
	private static GroupTarget[] findTargets(Node parent, Node[] nodes) {
		Map<String, List<NTarget>> groups = new LinkedHashMap<String, List<NTarget>>(10);

		for (Node node : nodes) {
			NTarget[] targets = node.getTargets();
			for (NTarget target : targets) {
				if (target.getDimensions() == 1) {
					List<NTarget> group = groups.get(target.getName());
					if (group == null) {
						group = new ArrayList<NTarget>(nodes.length * 2);
						groups.put(target.getName(), group);
					}
					group.add(target);
				}
			}
		}

		Iterator<String> it = groups.keySet().iterator();
		List<GroupTarget> result = new ArrayList<GroupTarget>(10);
		while (it.hasNext()) {
			String name = it.next();
			List<NTarget> group = groups.get(name);
			try {
				result.add(new GroupTarget(parent, name, group.toArray(new NTarget[group.size()])));
			} catch (StructuralException e) {
				throw new Error("Composite Termination should consist only of 1D Terminations, but apparently does not", e);
			}
		}

		return result.toArray(new GroupTarget[result.size()]);
	}

	private static Map<String, List<Integer>> findStateNames(Node[] nodes) {
		Map<String, List<Integer>> result = new LinkedHashMap<String, List<Integer>>(10);

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] instanceof Probeable) {
				Properties p = ((Probeable) nodes[i]).listStates();
				Iterator<Object> keys = p.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next().toString();
					if (!result.containsKey(key)) {
						result.put(key, new ArrayList<Integer>(10));
					}
					result.get(key).add(Integer.valueOf(i));
				}
			}
		}

		return result;
	}

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
			myListeners = new ArrayList<Listener>(1);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#removeChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
    public void removeChangeListener(Listener listener) {
		if (myListeners != null) {
            myListeners.remove(listener);
        }
	}

	/**
	 * Called by subclasses when properties have changed in such a way that the
	 * display of the ensemble may need updating.
	 */
	protected void fireVisibleChangeEvent() {
		VisiblyChangesUtils.changed(this, myListeners);
	}

	@Override
    public Group clone() throws CloneNotSupportedException {
		AbstractGroup result = (AbstractGroup) super.clone();
		
		/////////////////////////////////////////////////////////////
		// undo unintentional object.clone() side effects
		result.myListeners = new ArrayList<Listener>(3);

		/////////////////////////////////////////////////////////////
		// manually clone all the necessary sub-components
		
		Node[] oldNodes = getNodes();
		Node[] nodes = oldNodes.clone(); //use clone rather than new Node[] to retain array type, e.g. NEFNode[]
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = oldNodes[i].clone();
		}
		result.myNodes = nodes;
		result.myStateNames = findStateNames(nodes);
		
		result.mySources = new LinkedHashMap<String, NSource>(mySources.size());
		for (NSource source : mySources.values()) {
			result.mySources.put(source.getName(), source.clone(result));
		}
		
		result.myTargets = new LinkedHashMap<String, GroupTarget>(myTargets.size());
		for (GroupTarget termination : myTargets.values()) {
			result.myTargets.put(termination.getName(), termination.clone(result));
		}
		
		if (mySpikePattern != null) {
            result.mySpikePattern = (SpikePatternImpl) mySpikePattern.clone();
        }
		
		// Currently, stateNames is never modified, and therefore does not need to be cloned
//		result.myStateNames = new LinkedHashMap<String, List<Integer>>(myStateNames.size());
//		for (String key : myStateNames.keySet()) {
//			result.myStateNames.put(key, new ArrayList<Integer>(myStateNames.get(key)));
//		}
		
		return result;
	}
}
