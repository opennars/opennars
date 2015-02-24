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
import ca.nengo.model.neuron.Neuron;
import ca.nengo.util.SpikePattern;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.VisiblyMutable;
import ca.nengo.util.VisiblyMutableUtils;
import ca.nengo.util.impl.SpikePatternImpl;
import ca.nengo.util.impl.TimeSeriesImpl;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * Abstract class that can be used as a basis for Ensemble implementations.
 *
 * @author Bryan Tripp
 */
public abstract class AbstractGroup implements Group, Probeable, VisiblyMutable {

	private static final long serialVersionUID = -5498397418584843304L;

	private static final Logger ourLogger = LogManager.getLogger(AbstractGroup.class);

	private String myName;
	private Map<String, List<Integer>> myStateNames; // for Probeable
	private SimulationMode myMode;
	private transient SpikePatternImpl mySpikePattern;
	private boolean myCollectSpikesFlag;
	private int myCollectSpikesRatio = 1;
	private String myDocumentation;
	private transient List<VisiblyMutable.Listener> myListeners;
	private Node[] myNodes;
	private Map<String, Source> myOrigins;
	private Map<String, GroupTarget> myTerminations;
	
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
		myOrigins = new LinkedHashMap<String, Source>(10);
		Source[] sources = findOrigins(this, myNodes);
		for (Source source : sources) {
		    myOrigins.put(source.getName(), source);
		}

		myTerminations = new LinkedHashMap<String, GroupTarget>(10);
		GroupTarget[] terminations = findTerminations(this, myNodes);
		for (GroupTarget termination : terminations) {
		    myTerminations.put(termination.getName(), termination);
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
	 * @see ca.nengo.model.Group#getName()
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
					InstantaneousOutput output = myNodes[i].getOrigin(Neuron.AXON).get();
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
		for (Target t : myTerminations.values()) {
			t.reset(randomize);
		}


		mySpikePattern = new SpikePatternImpl(myNodes.length);
	}

	/**
	 * @see ca.nengo.model.Group#getOrigin(java.lang.String)
	 */
    public Source getOrigin(String name) throws StructuralException {
		return myOrigins.get(name);
	}

	/**
	 * @see ca.nengo.model.Group#getTermination(java.lang.String)
	 */
    public Target getTermination(String name) throws StructuralException {
		return myTerminations.get(name);
	}

    /**
     * @param name Name of the Origin to remove from the ensemble
     * @return the removed Origin object
     * @throws StructuralException if named Origin does not exist
     * @see ca.nengo.model.ExpandableNode#removeTermination(java.lang.String)
     */
    public synchronized Source removeOrigin(String name) throws StructuralException {
        if (myOrigins.containsKey(name)) {
            Source result = myOrigins.remove(name);

            fireVisibleChangeEvent();
            return result;
        }
        throw new StructuralException("Origin " + name + " does not exist");
    }

    /**
     * @param name Name of the Termination to remove from the ensemble
     * @return the removed Termination object
     * @throws StructuralException if named Termination does not exist
     * @see ca.nengo.model.ExpandableNode#removeTermination(java.lang.String)
     */
    public synchronized Target removeTermination(String name) throws StructuralException {
        if (myTerminations.containsKey(name)) {
            Target result = myTerminations.remove(name);

            fireVisibleChangeEvent();
            return result;
        }

        throw new StructuralException("Termination " + name + " does not exist");
    }

	/**
	 * @see ca.nengo.model.Node#getOrigins()
	 */
    public Source[] getOrigins() {
        ArrayList<Source> result = new ArrayList<Source>(10);
        for (Source o : myOrigins.values()) {
            result.add(o);
        }
        return result.toArray(new Source[result.size()]);
	}

	/**
	 * @see ca.nengo.model.Group#getTerminations()
	 */
    public Target[] getTerminations() {
	    ArrayList<Target> result = new ArrayList<Target>(10);
	    for (Target t : myTerminations.values()) {
            result.add(t);
        }
	    return result.toArray(new Target[result.size()]);
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
	private static Source[] findOrigins(Node parent, Node[] nodes) {
		Map<String, List<Source>> groups = group1DOrigins(nodes);
		Iterator<String> it = groups.keySet().iterator();
		List<Source> result = new ArrayList<Source>(10);
		while (it.hasNext()) {
			String name = it.next();
			List<Source> group = groups.get(name);
			result.add(new GroupSource(parent, name, group.toArray(new Source[group.size()])));
		}

		return result.toArray(new Source[result.size()]);
	}

	/**
	 * @param nodes A list of Nodes in an Ensemble
	 * @return A grouping of one-dimensional origins on these nodes, by name
	 */
	private static Map<String, List<Source>> group1DOrigins(Node[] nodes) {
		Map<String, List<Source>> groups = new LinkedHashMap<String, List<Source>>(10);

		for (Node node : nodes) {
			Source[] sources = node.getOrigins();
			for (Source source : sources) {
				if (source.getDimensions() == 1) {
					List<Source> group = groups.get(source.getName());
					if (group == null) {
						group = new ArrayList<Source>(nodes.length * 2);
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
		Source[] sources = node.getOrigins();
		for (Source source : sources) {
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
	private static GroupTarget[] findTerminations(Node parent, Node[] nodes) {
		Map<String, List<Target>> groups = new LinkedHashMap<String, List<Target>>(10);

		for (Node node : nodes) {
			Target[] targets = node.getTerminations();
			for (Target target : targets) {
				if (target.getDimensions() == 1) {
					List<Target> group = groups.get(target.getName());
					if (group == null) {
						group = new ArrayList<Target>(nodes.length * 2);
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
			List<Target> group = groups.get(name);
			try {
				result.add(new GroupTarget(parent, name, group.toArray(new Target[group.size()])));
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
	 * @see ca.nengo.util.VisiblyMutable#addChangeListener(ca.nengo.util.VisiblyMutable.Listener)
	 */
    public void addChangeListener(Listener listener) {
		if (myListeners == null) {
			myListeners = new ArrayList<Listener>(1);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyMutable#removeChangeListener(ca.nengo.util.VisiblyMutable.Listener)
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
		VisiblyMutableUtils.changed(this, myListeners);
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
		
		result.myOrigins = new LinkedHashMap<String, Source>(myOrigins.size());
		for (Source source : myOrigins.values()) {
			result.myOrigins.put(source.getName(), source.clone(result));
		}
		
		result.myTerminations = new LinkedHashMap<String, GroupTarget>(myTerminations.size());
		for (GroupTarget termination : myTerminations.values()) {
			result.myTerminations.put(termination.getName(), termination.clone(result));
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
