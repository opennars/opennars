/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NetworkImpl.java". Description:
"Default implementation of Network"

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
 * Created on 23-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.model.nef.impl.DecodableGroupImpl;
import ca.nengo.model.nef.impl.NEFGroupImpl;
import ca.nengo.model.neuron.Neuron;
import ca.nengo.sim.Simulator;
import ca.nengo.sim.impl.LocalSimulator;
import ca.nengo.util.*;
import ca.nengo.util.impl.ProbeTask;
import ca.nengo.util.impl.ScriptGenerator;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Default implementation of Network.
 *
 * @author Bryan Tripp
 */
public class NetworkImpl implements Network, VisiblyMutable, VisiblyMutable.Listener, TaskSpawner {

	/**
	 * Default name for a Network
	 */
	public static final String DEFAULT_NAME = "Network";

	private static final long serialVersionUID = 1L;
	private static final Logger ourLogger = LogManager.getLogger(NetworkImpl.class);

	private Map<String, Node> myNodeMap; //keyed on name
	private Map<Target, Projection> myProjectionMap; //keyed on Termination
	private String myName;
	private SimulationMode myMode;
	private List<SimulationMode> myFixedModes;
	private Simulator mySimulator;
	private float myStepSize;
	private final Map<String, Probeable> myProbeables;
	private final Map<String, String> myProbeableStates;
	private Map<String, Source> myExposedOrigins;
	private Map<String, Target> myExposedTerminations;

	private List<Source> orderedExposedSources;
	private List<Target> orderedExposedTargets;

	private String myDocumentation;
	private Map<String, Object> myMetaData;

	private Map<Source, String> myExposedOriginNames;
	private Map<Target, String> myExposedTerminationNames;

	private transient List<VisiblyMutable.Listener> myListeners;

	protected int myNumGPU = 0;
	protected int myNumJavaThreads = 1;
	protected boolean myUseGPU = true;

    private transient Collection<StepListener> myStepListeners;

	private transient Map<String, Object> myMetadata;
	
    

	/**
	 * Sets up a network's data structures
	 */
	public NetworkImpl() {
		myNodeMap = new HashMap<String, Node>(20);
		myProjectionMap	= new HashMap<Target, Projection>(50);
		myName = DEFAULT_NAME;
		myStepSize = .001f;
		myProbeables = new HashMap<String, Probeable>(30);
		myProbeableStates = new HashMap<String, String>(30);
		myExposedOrigins = new HashMap<String, Source>(10);
		myExposedOriginNames = new HashMap<Source, String>(10);
		myExposedTerminations = new HashMap<String, Target>(10);
		myExposedTerminationNames = new HashMap<Target, String>(10);
		myMode = SimulationMode.DEFAULT;
		myFixedModes = null;
		myMetaData = new HashMap<String, Object>(20);
		myListeners = new ArrayList<Listener>(10);

		orderedExposedSources = new ArrayList<Source> ();
		orderedExposedTargets = new ArrayList<Target> ();
		
		myStepListeners = new ArrayList<StepListener>(1);		
	}

	/**
	 * @param simulator Simulator with which to run this Network
	 */
	public void setSimulator(Simulator simulator) {
		mySimulator = simulator;
		mySimulator.initialize(this);
	}

	/**
	 * @return Simulator used to run this Network (a LocalSimulator by default)
	 */
	public Simulator getSimulator() {
		if (mySimulator == null) {
			mySimulator = new LocalSimulator();
			mySimulator.initialize(this);
		}
		return mySimulator;
	}

	/**
	 * @param stepSize New timestep size at which to simulate Network (some components of the network
	 * 		may run with different step sizes, but information is exchanged between components with
	 * 		this step size). Defaults to 0.001s.
	 */
	public void setStepSize(float stepSize) {
		myStepSize = stepSize;
	}

	/**
	 * @return Timestep size at which Network is simulated.
	 */
	public float getStepSize() {
		return myStepSize;
	}

	/**
	 * @param time The current simulation time. Sets the current time on the Network's subnodes.
   * (Mainly for NEFEnsembles).
	 */
	public void setTime(float time) {
			Node[] nodes = getNodes();
			
			for(int i = 0; i < nodes.length; i++){
				Node workingNode = nodes[i];
				
				if(workingNode instanceof DecodableGroupImpl){
					((DecodableGroupImpl) workingNode).setTime(time);
				}else if(workingNode instanceof NetworkImpl){
					((NetworkImpl) workingNode).setTime(time);
				}
			}
	}
	
	/**
	 * @see ca.nengo.model.Network#addNode(ca.nengo.model.Node)
	 */
	public void addNode(Node node) throws StructuralException {
		if (myNodeMap.containsKey(node.getName())) {
			throw new StructuralException("This Network already contains a Node named " + node.getName());
		}

		myNodeMap.put(node.getName(), node);
		node.addChangeListener(this);

		getSimulator().initialize(this);
		fireVisibleChangeEvent();
	}

	/**
	 * Counts how many neurons are contained within this network.
	 * 
	 * @return number of neurons in this network
	 */
	public int countNeurons()
	{
		Node[] myNodes = getNodes();
		int count = 0;
		for(Node node : myNodes)
		{
			if(node instanceof NetworkImpl)
				count += ((NetworkImpl)node).countNeurons();
			else if(node instanceof Group)
				count += ((Group)node).getNodes().length;
			else if(node instanceof Neuron)
				count += 1;
		}
		
		return count;
	}
	
	/***
	 * Kills a certain percentage of neurons in the network (recursively including subnetworks).
	 *
	 * @param killrate the percentage (0.0 to 1.0) of neurons to kill
	 */
	public void killNeurons(float killrate)
	{
		killNeurons(killrate, false);
	}

	/***
	 * Kills a certain percentage of neurons in the network (recursively including subnetworks).
	 *
	 * @param killrate the percentage (0.0 to 1.0) of neurons to kill
	 * @param saveRelays if true, exempt populations with only one node from the slaughter
	 */
	public void killNeurons(float killrate, boolean saveRelays)
	{
		Node[] nodes = getNodes();
		for (Node node : nodes) {
			if(node instanceof NetworkImpl) {
                ((NetworkImpl)node).killNeurons(killrate, saveRelays);
            } else if(node instanceof NEFGroupImpl) {
                ((NEFGroupImpl)node).killNeurons(killrate, saveRelays);
            }
		}

	}

	/**
	 * Kills a certain percentage of the dendritic inputs in the network (recursively including subnetworks).
	 *
	 * @param killrate the percentage (0.0 to 1.0) of dendritic inputs to kill
	 */
//	public void killDendrites(float killrate)
//	{
//		Node[] nodes = getNodes();
//		for(int i = 0; i < nodes.length; i++)
//		{
//			if(nodes[i] instanceof NetworkImpl)
//				((NetworkImpl)nodes[i]).killDendrites(killrate);
//			else if(nodes[i] instanceof NEFEnsembleImpl)
//				((NEFEnsembleImpl)nodes[i]).killDendrites(killrate);
//		}
//
//	}

	/**
	 * Handles any changes/errors that may arise from objects within the network changing.
	 *
	 * @see ca.nengo.util.VisiblyMutable.Listener#changed(ca.nengo.util.VisiblyMutable.Event)
	 */
	public void changed(Event e) throws StructuralException {
		if (e instanceof VisiblyMutable.NameChangeEvent) {
			VisiblyMutable.NameChangeEvent ne = (VisiblyMutable.NameChangeEvent) e;

			if (myNodeMap.containsKey(ne.getNewName()) && !ne.getNewName().equals(ne.getOldName())) {
				throw new StructuralException("This Network already contains a Node named " + ne.getNewName());
			}

			/*
			 * Only do the swap if the name has changed.
			 * Otherwise, the node will be dereferenced from the map.
			 * 
			 * Also only do the swap if the node being changed is already in myNodeMap.
			 */
			if (!ne.getOldName().equals(ne.getNewName()) && (ne.getObject() == getNode(ne.getOldName()))) {
				myNodeMap.put(ne.getNewName(), (Node)ne.getObject());
				myNodeMap.remove(ne.getOldName());
			}
		}
		
		fireVisibleChangeEvent();
	}

	/**
	 * Gathers all the terminations of nodes contained in this network.
	 *
	 * @return arraylist of terminations
	 */
	public ArrayList<Target> getNodeTerminations()
	{
		Node[] nodes = getNodes();
        ArrayList<Target> nodeTargets = new ArrayList<Target>(nodes.length);
		for (Node node : nodes) {
			Target[] terms = node.getTerminations();
            Collections.addAll(nodeTargets, terms);
		}

		return nodeTargets;
	}

	/**
	 * Gathers all the origins of nodes contained in this network.
	 *
	 * @return arraylist of origins
	 */
	public ArrayList<Source> getNodeOrigins()
	{
		Node[] nodes = getNodes();
        ArrayList<Source> nodeSources = new ArrayList<Source>(nodes.length);
		for (Node node : nodes) {
			Source[] origs = node.getOrigins();
            Collections.addAll(nodeSources, origs);
		}

		return nodeSources;
	}

	/**
	 * @see ca.nengo.model.Network#getNodes()
	 */
	public Node[] getNodes() {
        Collection<Node> var = myNodeMap.values();
        return var.toArray(new Node[var.size()]);
	}

    @Override
    public SpikePattern getSpikePattern() {
        return null;
    }

    @Override
    public void collectSpikes(boolean collect) {

    }

    @Override
    public boolean isCollectingSpikes() {
        return false;
    }

    @Override
    public void redefineNodes(Node[] nodes) {

    }

    /**
	 * @see ca.nengo.model.Network#getNode(java.lang.String)
	 */
	public Node getNode(String name) throws StructuralException {
		if (!myNodeMap.containsKey(name)) {
			throw new StructuralException("No Node named " + name + " in this Network");
		}
		return myNodeMap.get(name);
	}

	/**
	 * @return number of top-level nodes
	 */
	public int getNodeCount(){
		return getNodes().length;
	}

	/**
	 * @return number of neurons in all levels
	 */
	public int getNeuronCount(){
		int neuron_count = 0;
		Node[] nodes = getNodes();

		for (Node node : nodes) {
			if(node instanceof NetworkImpl) {
                neuron_count += ((NetworkImpl)node).getNeuronCount();
            } else if(node instanceof NEFGroupImpl) {
                neuron_count += ((NEFGroupImpl)node).getNeuronCount();
            }
		}

		return neuron_count;
	}

	/**
	 * @see ca.nengo.model.Network#removeNode(java.lang.String)
	 */
	public void removeNode(String name) throws StructuralException {
		if (myNodeMap.containsKey(name)) {
			Node node = myNodeMap.get(name);

			if(node instanceof Network)
			{
				Network net = (Network)node;
				Probe[] probes = net.getSimulator().getProbes();
				for (Probe probe : probes) {
                    try
					{
						net.getSimulator().removeProbe(probe);
					}
					catch(SimulationException se)
					{
						System.err.println(se);
						return;
					}
                }

				Node[] nodes = net.getNodes();
				for (Node node2 : nodes) {
                    net.removeNode(node2.getName());
                }
			}
			else if(node instanceof DecodableGroupImpl)
			{
				NEFGroupImpl pop = (NEFGroupImpl)node;
				Source[] sources = pop.getOrigins();
				for (Source source : sources) {
					String exposedName = getExposedOriginName(source);
					if(exposedName != null) {
                        hideOrigin(exposedName);
                    }
				}
			}
			else if(node instanceof SocketUDPNode)
			{
				// If the node to be removed is a SocketUDPNode, make sure to close down the socket
				// before removing it.
				((SocketUDPNode) node).close();
			}

			myNodeMap.remove(name);
			node.removeChangeListener(this);
//			VisiblyMutableUtils.nodeRemoved(this, node, myListeners);
			
			getSimulator().initialize(this);
			fireVisibleChangeEvent();
		} else {
			throw new StructuralException("No Node named " + name + " in this Network");
		}
	}

	/**
	 * @see ca.nengo.model.Network#addProjection(ca.nengo.model.Source, ca.nengo.model.Target)
	 */
	public Projection addProjection(Source source, Target target) throws StructuralException {
		if (myProjectionMap.containsKey(target)) {
			throw new StructuralException("There is already an Origin connected to the specified Termination");
		} else if (source.getDimensions() != target.getDimensions()) {
			throw new StructuralException("Can't connect Origin of dimension " + source.getDimensions()
					+ " to Termination of dimension " + target.getDimensions());
		} else {
			Projection result = new ProjectionImpl(source, target, this);
			myProjectionMap.put(target, result);
			getSimulator().initialize(this);
			fireVisibleChangeEvent();
	
			return result;
		}
	}

	/**
	 * @see ca.nengo.model.Network#getProjections()
	 */
	public Projection[] getProjections() {
        Collection<Projection> var = myProjectionMap.values();
        return var.toArray(new Projection[var.size()]);
	}
	
	public Map<Target, Projection> getProjectionMap() {
		return myProjectionMap;
	}

	/**
	 * @see ca.nengo.model.Network#removeProjection(ca.nengo.model.Target)
	 */
	public void removeProjection(Target target) throws StructuralException {
		if (myProjectionMap.containsKey(target)) {
			Projection p = myProjectionMap.get(target);
			p.getTermination().reset(false);
			
			myProjectionMap.remove(target);
		} else {
			throw new StructuralException("The Network contains no Projection ending on the specified Termination");
		}

		getSimulator().initialize(this);
		fireVisibleChangeEvent();
	}

	/**
	 * @see ca.nengo.model.Node#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @param name New name of Network (must be unique within any networks of which this one
	 * 		will be a part)
	 */
	public void setName(String name) throws StructuralException {
		if (!myName.equals(name)) {
			myName = name;
			VisiblyMutableUtils.nameChanged(this, getName(), name, myListeners);
		}
	}


	/**
	 * @see ca.nengo.model.Node#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
		if(myFixedModes != null && !myFixedModes.contains(mode))
			return;
		myMode = mode;

		Iterator<Node> it = myNodeMap.values().iterator();
		while (it.hasNext()) {
			Node node = it.next();
            node.setMode(mode);
		}
	}

	/**
	 * Used to just change the mode of this network (without recursively
	 * changing the mode of nodes in the network)
	 */
	protected void setMyMode(SimulationMode mode) {
		if(myFixedModes == null || myFixedModes.contains(mode)) {
            myMode = mode;
        }
	}

	/**
	 * Fix the simulation mode to the current mode.
	 */
	public void fixMode() {
		fixMode(new SimulationMode[]{getMode()});
	}
	
	/**
	 * Set the allowed simulation modes.
	 */
	public void fixMode(SimulationMode[] modes) {
		myFixedModes = Arrays.asList(modes);
	}
	
	/**
	 * @see ca.nengo.model.Node#getMode()
	 */
	public SimulationMode getMode() {
		return myMode;
	}

	/**
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		getSimulator().run(startTime, endTime, myStepSize);
	}

	/**
	 * Runs the model with the optional parameter topLevel.
	 *
     * @param startTime simulation time at which running starts (s)
     * @param endTime simulation time at which running ends (s)
	 * @param topLevel true if the network being run is the top level network, false if it is a subnetwork
	 * @throws SimulationException if there's an error in the simulation
	 */
	public void run(float startTime, float endTime, boolean topLevel) throws SimulationException
	{
		getSimulator().run(startTime, endTime, myStepSize, topLevel);
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		Iterator<String> it = myNodeMap.keySet().iterator();
		while (it.hasNext()) {
			Node n = myNodeMap.get(it.next());
			n.reset(randomize);
		}
	}

	/**
	 * @param use Use GPU?
	 */
	public void setUseGPU(boolean use)
	{
		//myUseGPU = use;
		
		Node[] nodes = getNodes();

		for (Node workingNode : nodes) {
			if(workingNode instanceof NEFGroupImpl) {
				((NEFGroupImpl) workingNode).setUseGPU(use);
			} else if(workingNode instanceof NetworkImpl) {
				((NetworkImpl) workingNode).setUseGPU(use);
			}
		}
	}

	/**
	 * @return Using GPU?
	 */
	public boolean getUseGPU(){
		Node[] nodes = getNodes();

		for (Node workingNode : nodes) {
			if(workingNode instanceof NEFGroupImpl) {
				if(!((NEFGroupImpl) workingNode).getUseGPU()){
					return false;
				}
			} else if(workingNode instanceof NetworkImpl) {
				if(!((NetworkImpl) workingNode).getUseGPU()){
					return false;
				}
			}
		}
		
		//return myMode == SimulationMode.DEFAULT || myMode == SimulationMode.RATE;
		return true;
	}

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		Probeable p = myProbeables.get(stateName);
		String n = myProbeableStates.get(stateName);

		return p.getHistory(n);
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		Properties result = new Properties();

		Iterator<String> it = myProbeables.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Probeable p = myProbeables.get(key);
			String n = myProbeableStates.get(key);
			result.put(key, p.listStates().getProperty(n));
		}

		return result;
	}
	
	/**
	 * @see ca.nengo.model.Network#exposeOrigin(ca.nengo.model.Source,
	 *      java.lang.String)
	 */
	public void exposeOrigin(Source source, String name) {
		Source temp;

		temp = new SourceWrapper(this, source, name);

		myExposedOrigins.put(name, temp );
		myExposedOriginNames.put(source, name);
		orderedExposedSources.add(temp);

		// automatically add exposed origin to exposed states
		if (source.getNode() instanceof Probeable) {
			Probeable p=(Probeable)(source.getNode());
			try {
				exposeState(p, source.getName(),name);
			} catch (StructuralException e) {
                e.printStackTrace();
			}
		}
		fireVisibleChangeEvent();
	}

	/**
	 * @see ca.nengo.model.Network#hideOrigin(java.lang.String)
	 */
	public void hideOrigin(String name) throws StructuralException {
		if(myExposedOrigins.get(name) == null) {
            throw new StructuralException("No origin named " + name + " exists");
        }

		orderedExposedSources.remove(myExposedOrigins.get(name));
		SourceWrapper originWr = (SourceWrapper)myExposedOrigins.remove(name);


		if (originWr != null) {
			myExposedOriginNames.remove(originWr.myWrapped);


			// remove the automatically exposed state
			if (originWr.myWrapped.getNode() instanceof Probeable) {
				this.hideState(name);
			}
		}

		fireVisibleChangeEvent();
	}

	/**
	 * @see ca.nengo.model.Network#getExposedOriginName(ca.nengo.model.Source)
	 */
	public String getExposedOriginName(Source insideSource) {
		return myExposedOriginNames.get(insideSource);
	}

	/**
	 * @see ca.nengo.model.Network#getOrigin(java.lang.String)
	 */
	public Source getOrigin(String name) throws StructuralException {
		if ( !myExposedOrigins.containsKey(name) ) {
			throw new StructuralException("There is no exposed Origin named " + name);
		}
		return myExposedOrigins.get(name);
	}

	/**
	 * @see ca.nengo.model.Network#getOrigins()
	 */
	public Source[] getOrigins() {
		if (myExposedOrigins.values().size() == 0) {
            Collection<Source> var = myExposedOrigins.values();
            return var.toArray(new Source[var.size()]);
        }
		return orderedExposedSources.toArray(new Source[orderedExposedSources.size()]);
	}

	/**
	 * @see ca.nengo.model.Network#exposeTermination(ca.nengo.model.Target, java.lang.String)
	 */
	public void exposeTermination(Target target, String name) {
		Target term;
		
			term = new TargetWrapper(this, target, name);
			
			myExposedTerminations.put(name, term);
			myExposedTerminationNames.put(target, name);
			orderedExposedTargets.add(term);
		
		fireVisibleChangeEvent();
	}

	/**
	 * @see ca.nengo.model.Network#hideTermination(java.lang.String)
	 */
	public void hideTermination(String name) {
		Target term = myExposedTerminations.get(name);
		
		if(term == null) return;
		
		orderedExposedTargets.remove(term);
		TargetWrapper termination = (TargetWrapper)myExposedTerminations.remove(name);
		if (termination != null) {
			myExposedTerminationNames.remove(termination.myWrapped);
		}
		fireVisibleChangeEvent();
	}

	/**
	 * @see ca.nengo.model.Network#getExposedTerminationName(ca.nengo.model.Target)
	 */
	public String getExposedTerminationName(Target insideTarget) {
		return myExposedTerminationNames.get(insideTarget);
	}

	/**
	 * @see ca.nengo.model.Network#getTermination(java.lang.String)
	 */
	public Target getTermination(String name) throws StructuralException {
		if ( !myExposedTerminations.containsKey(name) ) {
			throw new StructuralException("There is no exposed Termination named " + name);
		}
		return myExposedTerminations.get(name);
	}

	/**
	 * @see ca.nengo.model.Network#getTerminations()
	 */
	public Target[] getTerminations() {
		if (myExposedTerminations.values().size() == 0) {
            Collection<Target> var = myExposedTerminations.values();
            return var.toArray(new Target[var.size()]);
        }
		return orderedExposedTargets.toArray(new Target[orderedExposedTargets.size()]);
	}

	/**
	 * @see ca.nengo.model.Network#exposeState(ca.nengo.model.Probeable, java.lang.String, java.lang.String)
	 */
	public void exposeState(Probeable probeable, String stateName, String name) throws StructuralException {
		if (probeable.listStates().get(stateName) == null) {
			throw new StructuralException("The state " + stateName + " does not exist");
		}

		myProbeables.put(name, probeable);
		myProbeableStates.put(name, stateName);
	}

	/**
	 * @see ca.nengo.model.Network#hideState(java.lang.String)
	 */
	public void hideState(String name) {
		myProbeables.remove(name);
		myProbeableStates.remove(name);
	}
	
	/**
     */
    public ThreadTask[] getTasks(){
    	
    	if(mySimulator == null)
    		return new ThreadTask[0];
    		
    	Probe[] probes = mySimulator.getProbes();
    	ProbeTask[] probeTasks = new ProbeTask[probes.length];
    	
    	for(int i = 0; i < probes.length; i++){
    		probeTasks[i] = probes[i].getProbeTask();
    	}

    	return probeTasks;
    }

    /**
     */
    public void setTasks(ThreadTask[] tasks){
    }

    /**
     */
    public void addTasks(ThreadTask[] tasks){
    }

	/**
	 * Wraps an Origin with a new name (for exposing outside Network).
	 *
	 * @author Bryan Tripp
	 */
	public static class SourceWrapper implements Source {

		private static final long serialVersionUID = 1L;

		private Node myNode;
		private Source myWrapped;
		private String myName;

		/**
		 * @param node Parent node
		 * @param wrapped Warpped Origin
		 * @param name Name of new origin
		 */
		public SourceWrapper(Node node, Source wrapped, String name) {
			myNode = node;
			myWrapped = wrapped;
			myName = name;
		}

		/**
		 * Default constructor
		 * TODO: Is this necessary?
		 */
		public SourceWrapper() {
			this(null, null, "exposed");
		}

		/**
		 * @return The underlying wrapped Origin
		 */
		public Source getWrappedOrigin() {
			return myWrapped;
		}

        /**
         * Unwraps Origin until it finds one that isn't wrapped
         *
         * @return Base origin if there are multiple levels of wrapping
         */
        public Source getBaseOrigin() {
            SourceWrapper other = this;
            while (true) {
                if (other.myWrapped instanceof SourceWrapper) {
                    other = ((SourceWrapper) other.myWrapped);
                } else {
                    return other.myWrapped;
                }
            }
        }

		/**
		 * @param wrapped Set the underlying wrapped Origin
		 */
		public void setWrappedOrigin(Source wrapped) {
			myWrapped = wrapped;
		}

		public String getName() {
			return myName;
		}

		/**
		 * @param name Name
		 */
		public void setName(String name) {
			myName = name;
		}

		public int getDimensions() {
			return myWrapped.getDimensions();
		}

		public InstantaneousOutput get() {
			return myWrapped.get();
		}

		public void accept(InstantaneousOutput values) {
			myWrapped.accept(values);
		}
		
		public Node getNode() {
			return myNode;
		}

		/**
		 * @param node Parent node
		 */
		public void setNode(Node node) {
			myNode = node;
		}

		@Override
		public Source clone() throws CloneNotSupportedException {
			return (Source) super.clone();
		}
		
		public Source clone(Node node) throws CloneNotSupportedException {
			return this.clone();
		}

		public void setRequiredOnCPU(boolean val){
		    myWrapped.setRequiredOnCPU(val);
		}
		    
		public boolean getRequiredOnCPU(){
		   return myWrapped.getRequiredOnCPU();
		}
	}

	/**
	 * Wraps a Termination with a new name (for exposing outside Network).
	 *
	 * @author Bryan Tripp
	 */
	public static class TargetWrapper implements Target {

		private static final long serialVersionUID = 1L;

		private final Node myNode;
		private final Target myWrapped;
		private final String myName;

		/**
		 * @param node Parent node
		 * @param wrapped Termination being wrapped
		 * @param name New name
		 */
		public TargetWrapper(Node node, Target wrapped, String name) {
			myNode = node;
			myWrapped = wrapped;
			myName = name;
		}

		/**
		 * @return Wrapped Termination
		 */
		public Target getWrappedTermination() {
			return myWrapped;
		}

        /**
         * Unwraps terminations until it finds one that isn't wrapped
         *
         * @return Underlying Termination, not wrapped
         */
        public Target getBaseTermination() {
            TargetWrapper other = this;
            while (true) {
                if (other.myWrapped instanceof TargetWrapper) {
                    other = ((TargetWrapper) other.myWrapped);
                } else {
                    return other.myWrapped;
                }
            }
        }

		public String getName() {
			return myName;
		}

		public int getDimensions() {
			return myWrapped.getDimensions();
		}

		public void setValues(InstantaneousOutput values) throws SimulationException {
			myWrapped.setValues(values);
		}

		public Node getNode() {
			return myNode;
		}

		public boolean getModulatory() {
			return myWrapped.getModulatory();
		}

		public float getTau() {
			return myWrapped.getTau();
		}

		public void setModulatory(boolean modulatory) {
			myWrapped.setModulatory(modulatory);
		}

		public void setTau(float tau) throws StructuralException {
			myWrapped.setTau(tau);
		}
		
		/**
		 * @return Extract the input to the termination.
		 */
		public InstantaneousOutput get(){
			return myWrapped.get();
		}

		/**
		 * @see ca.nengo.model.Resettable#reset(boolean)
		 */
		public void reset(boolean randomize) {
			myWrapped.reset(randomize);
		}

		@Override
		public TargetWrapper clone() throws CloneNotSupportedException {
			return this.clone(myNode);
		}
		
		public TargetWrapper clone(Node node) throws CloneNotSupportedException {
			throw new CloneNotSupportedException("TerminationWrapper not cloneable");
//			TerminationWrapper result = (TerminationWrapper) super.clone();
//			result.myNode = node;
//			return result;
		}

	}
	
	public void dumpToScript() throws FileNotFoundException
	{
		File file = new File(this.getName().replace(' ', '_') + ".py");
		
		ScriptGenerator scriptGen = new ScriptGenerator(file);
		scriptGen.startDFS(this);
	}
	
	public void dumpToScript(String filepath) throws FileNotFoundException
	{
		File file = new File(filepath);
		
		ScriptGenerator scriptGen = new ScriptGenerator(file);
		scriptGen.startDFS(this);
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
	 * @see ca.nengo.model.Network#getMetaData(java.lang.String)
	 */
	public Object getMetaData(String key) {
		return myMetaData.get(key);
	}

	/**
	 * @see ca.nengo.model.Network#setMetaData(java.lang.String, java.lang.Object)
	 */
	public void setMetaData(String key, Object value) {
		if ( !(value instanceof Serializable) ) {
			throw new RuntimeException("Metadata must be serializable");
		}
		myMetaData.put(key, value);
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

	private void fireVisibleChangeEvent() {
		VisiblyMutableUtils.changed(this, myListeners);
	}

    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		StringBuilder py = new StringBuilder();
        String pythonNetworkName = scriptData.get("prefix") + myName.replaceAll("\\p{Blank}|\\p{Punct}", scriptData.get("spaceDelim").toString());

        py.append("\n\n# Network ").append(myName).append(" Start\n");

        if ((Boolean)scriptData.get("isSubnet"))
        {
            py.append(String.format("%s = %s.make_subnetwork('%s')\n", 
                    pythonNetworkName,
                    scriptData.get("netName"),
                    myName
                    ));
        }
        else
        {
            py.append(String.format("%s = nef.Network('%s')\n", 
                    pythonNetworkName, 
                    myName));
        }

        py.append("\n# ").append(myName).append(" - Nodes\n");
        
        return py.toString();
    }

	@Override
	public Network clone() throws CloneNotSupportedException {
		NetworkImpl result = (NetworkImpl) super.clone();

		result.myNodeMap = new HashMap<String, Node>(10);
		for (Node oldNode : myNodeMap.values()) {
			Node newNode = oldNode.clone();
			result.myNodeMap.put(newNode.getName(), newNode);
			newNode.addChangeListener(result);
		}

		//TODO: Exposed states aren't handled currently, pending redesign of Probes (it should be possible
		//		to probe things that are nested deeply, in which case exposing state woulnd't be necessary)
//		result.myProbeables
//		result.myProbeableStates

		//TODO: this works with a single Projection impl & no params; should add Projection.copy(Origin, Termination, Network)?
		result.myProjectionMap = new HashMap<Target, Projection>(10);
		for (Projection oldProjection : getProjections()) {
			try {
				Source newSource = result.getNode(oldProjection.getOrigin().getNode().getName())
					.getOrigin(oldProjection.getOrigin().getName());
				Target newTarget = result.getNode(oldProjection.getTermination().getNode().getName())
					.getTermination(oldProjection.getTermination().getName());
				Projection newProjection = new ProjectionImpl(newSource, newTarget, result);
				result.myProjectionMap.put(newTarget, newProjection);
			} catch (StructuralException e) {
				throw new CloneNotSupportedException("Problem copying Projectio: " + e.getMessage());
			}
		}

		result.myExposedOrigins = new HashMap<String, Source>(10);
		result.myExposedOriginNames = new HashMap<Source, String>(10);
		result.orderedExposedSources = new LinkedList <Source> ();
		for (Source exposed : getOrigins()) {
			String name = exposed.getName();
			Source wrapped = ((SourceWrapper) exposed).getWrappedOrigin();
			try {
				// Check to see if referenced node is the network itself. If it is, handle the origin differently.
				if (wrapped.getNode().getName() != myName ) {
					Source toExpose = result.getNode(wrapped.getNode().getName()).getOrigin(wrapped.getName());
					result.exposeOrigin(toExpose, name);
				}
			} catch (StructuralException e) {
				throw new CloneNotSupportedException("Problem exposing Origin: " + e.getMessage());
			}
		}

		result.myExposedTerminations = new HashMap<String, Target>(10);
		result.myExposedTerminationNames = new HashMap<Target, String>(10);
		result.orderedExposedTargets = new LinkedList <Target> ();
		for (Target exposed : getTerminations()) {
			String name = exposed.getName();
			Target wrapped = ((TargetWrapper) exposed).getWrappedTermination();
			try {
				// Check to see if referenced node is the network itself. If it is, handle the termination differently.
				if (wrapped.getNode().getName() != myName ) {
					Target toExpose = result.getNode(wrapped.getNode().getName()).getTermination(wrapped.getName());
					result.exposeTermination(toExpose, name);
				}
			} catch (StructuralException e) {
				throw new CloneNotSupportedException("Problem exposing Termination: " + e.getMessage());
			}
		}

		result.myListeners = new ArrayList<Listener>(5);

		result.myMetaData = new HashMap<String, Object>(10);
		for (Map.Entry<String, Object> stringObjectEntry : myMetaData.entrySet()) {
			Object o = stringObjectEntry.getValue();
			if (o instanceof Cloneable) {
				Object copy = tryToClone((Cloneable) o);
				result.myMetaData.put(stringObjectEntry.getKey(), copy);
			} else {
				result.myMetaData.put(stringObjectEntry.getKey(), o);
			}
		}

		//TODO: take another look at Probe design (maybe Probeables reference Probes?)
		result.mySimulator = mySimulator.clone();
		result.mySimulator.initialize(result);
		Probe[] oldProbes = mySimulator.getProbes();
		for (Probe oldProbe : oldProbes) {
			Probeable target = oldProbe.getTarget();
			if (target instanceof Node) {
				Node oldNode = (Node) target;
				if (oldProbe.isInEnsemble()) {
					try {
						Group oldGroup = (Group) getNode(oldProbe.getEnsembleName());
						int neuronIndex = -1;
						for (int j = 0; j < oldGroup.getNodes().length && neuronIndex < 0; j++) {
							if (oldNode == oldGroup.getNodes()[j]) {
                                neuronIndex = j;
                            }
						}
						result.mySimulator.addProbe(oldProbe.getEnsembleName(), neuronIndex, oldProbe.getStateName(), true);
					} catch (SimulationException e) {
						ourLogger.warn("Problem copying Probe", e);
					} catch (StructuralException e) {
						ourLogger.warn("Problem copying Probe", e);
					}
				} else {
					try {
						result.mySimulator.addProbe(oldNode.getName(), oldProbe.getStateName(), true);
					} catch (SimulationException e) {
						ourLogger.warn("Problem copying Probe", e);
					}
				}
			} else {
				ourLogger.warn("Can't copy Probe on type " + target.getClass().getName()
						+ " (to be addressed in a future release)");
			}
		}

		return result;
	}

	private static Object tryToClone(Cloneable o) {
		Object result = null;

		try {
			Method cloneMethod = o.getClass().getMethod("clone");
			result = cloneMethod.invoke(o);
		} catch (Exception e) {
			ourLogger.warn("Couldn't clone data of type " + o.getClass().getName(), e);
		}

		return result;
	}
	
	public void addStepListener(StepListener listener) {
		if (myStepListeners == null) {
			myStepListeners = new ArrayList<StepListener>(1);
		}
        myStepListeners.add(listener);
	}
	public void removeStepListener(StepListener listener) {
		if (myStepListeners == null) {
			myStepListeners = new ArrayList<StepListener>(1);
		}
        myStepListeners.remove(listener);
	}
	
	public void fireStepListeners(float time) {
		if (myStepListeners == null) {
			myStepListeners = new ArrayList<StepListener>(1);
		}
		for (StepListener listener: myStepListeners) {
			listener.stepStarted(time);
		}
	}

	public Node[] getChildren() {
		return getNodes();
	}

	@SuppressWarnings("unchecked")
	public String toPostScript(HashMap<String, Object> scriptData) {
		StringBuilder py = new StringBuilder();

		String pythonNetworkName = scriptData.get("prefix") + myName.replaceAll("\\p{Blank}|\\p{Punct}", scriptData.get("spaceDelim").toString());

        py.append("\n# ").append(myName).append(" - Templates\n");
		
        if (myMetaData.get("NetworkArray") != null) {
            Iterator iter = ((HashMap)myMetaData.get("NetworkArray")).values().iterator();
            while (iter.hasNext())
            {
                HashMap array = (HashMap)iter.next();

                if (!myNodeMap.containsKey(array.get("name")))
                {
                    continue;
                }

            	/*
                py.append(String.format("nef.templates.networkarray.make(%s, name='%s', neurons=%d, length=%d, radius=%.1f, rLow=%f, rHigh=%f, iLow=%f, iHigh=%f, encSign=%d, useQuick=%s)\n",
                            pythonNetworkName,
                            array.get("name"),
                            (Integer)array.get("neurons"),
                            (Integer)array.get("length"),
                            (Double)array.get("radius"),
                            (Double)array.get("rLow"),
                            (Double)array.get("rHigh"),
                            (Double)array.get("iLow"),
                            (Double)array.get("iHigh"),
                            (Integer)array.get("encSign"),
                            useQuick));
                            */
            	
            	 py.append(String.format("%s.make_array(name='%s', neurons=%d, length=%d, dimensions=%d",
            			 	pythonNetworkName,
            			 	array.get("name"),
                         array.get("neurons"),
                         array.get("length"),
                         array.get("dimensions")
                            ));
            	 
            	 if(array.containsKey("radius")){ py.append(", radius=").append(Double.toString((Double) array.get("radius"))); }
            	 
            	 if(array.containsKey("rLow") && array.containsKey("rHigh"))
            	 { py.append(", max_rate=(").append(Double.toString((Double) array.get("rLow"))).append(", ").append(Double.toString((Double) array.get("rHigh"))).append(')'); }
            	 
            	 if(array.containsKey("iLow") && array.containsKey("iHigh"))
            	 { py.append(", intercept=(").append(Double.toString((Double) array.get("iLow"))).append(", ").append(Double.toString((Double) array.get("iHigh"))).append(')'); }
            	 
            	 if(array.containsKey("useQuick"))
            	 {  
            		String useQuick = (Boolean)array.get("useQuick") ? "True" : "False";
            	 	py.append(", quick=").append(useQuick);
            	 }
            	 
            	 if(array.containsKey("encoders")){ py.append(", encoders=").append(array.get("encoders")); }
            	 py.append(")\n");
            }
        } 
		
        if (myMetaData.get("BasalGanglia") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("BasalGanglia")).values().iterator();
            while (iter.hasNext())
            {
                HashMap bg = (HashMap)iter.next();

                if (!myNodeMap.containsKey(bg.get("name")))
                {
                    continue;
                }

            	String same_neurons = (Boolean)bg.get("same_neurons") ? "True" : "False";

                py.append(String.format("nef.templates.basalganglia.make(%s, name='%s', dimensions=%d, neurons=%d, pstc=%.3f, same_neurons=%s)\n",
                            pythonNetworkName,
                            bg.get("name"),
                        bg.get("dimensions"),
                        bg.get("neurons"),
                        bg.get("pstc"),
                            same_neurons));
            }
        } 
		
        if (myMetaData.get("Thalamus") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("Thalamus")).values().iterator();
            while (iter.hasNext())
            {
                HashMap thal = (HashMap)iter.next();

                if (!myNodeMap.containsKey(thal.get("name")))
                {
                    continue;
                }

            	String useQuick = (Boolean)thal.get("useQuick") ? "True" : "False";

                py.append(String.format("nef.templates.thalamus.make(%s, name='%s', neurons=%d, dimensions=%d, inhib_scale=%d, tau_inhib=%.3f, useQuick=%s)\n",
                            pythonNetworkName,
                            thal.get("name"),
                        thal.get("neurons"),
                        thal.get("dimensions"),
                        thal.get("inhib_scale"),
                        thal.get("tau_inhib"),
                            useQuick));
            }
        }
		
        if (myMetaData.get("integrator") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("integrator")).values().iterator();
            while (iter.hasNext())
            {
                HashMap integrator = (HashMap)iter.next();

                if (!myNodeMap.containsKey(integrator.get("name")))
                {
                    continue;
                }

                py.append(String.format("nef.templates.integrator.make(%s, name='%s', neurons=%d, dimensions=%d, tau_feedback=%g, tau_input=%g, scale=%g)\n",
                			pythonNetworkName,
                            integrator.get("name"),
                        integrator.get("neurons"),
                        integrator.get("dimensions"),
                        integrator.get("tau_feedback"),
                        integrator.get("tau_input"),
                        integrator.get("scale")));
            }
        }  
		
        if (myMetaData.get("oscillator") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("oscillator")).values().iterator();
            while (iter.hasNext())
            {
                HashMap oscillator = (HashMap)iter.next();

                String controlled = (Boolean)oscillator.get("controlled") ? "True" : "False";

                if (!myNodeMap.containsKey(oscillator.get("name")))
                {
                    continue;
                }

                py.append(String.format("nef.templates.oscillator.make(%s, name='%s', neurons=%d, dimensions=%d, frequency=%g, tau_feedback=%g, tau_input=%g, scale=%g, controlled=%s)\n",
                			pythonNetworkName,
                            oscillator.get("name"),
                        oscillator.get("neurons"),
                        oscillator.get("dimensions"),
                        oscillator.get("frequency"),
                        oscillator.get("tau_feedback"),
                        oscillator.get("tau_input"),
                        oscillator.get("scale"),
                            controlled));
            }
        }      

        if (myMetaData.get("linear") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("linear")).values().iterator();
            while (iter.hasNext())
            {
                HashMap linear = (HashMap)iter.next();

                if (!myNodeMap.containsKey(linear.get("name")))
                {
                    continue;
                }

                StringBuilder a = new StringBuilder("[");
                double[][] arr = (double[][])linear.get("A");
                for (int i = 0; i < arr.length; i++)
                {
                    a.append('[');
                    for (int j = 0; j < arr[i].length; j++)
                    {
                        a.append(arr[i][j]);
                        if ((j+1) < arr[i].length)
                        {
                            a.append(',');
                        }
                    }
                    a.append(']');
                    if ((i + 1) < arr.length)
                    {
                        a.append(',');
                    }
                }
                a.append(']');

                py.append(String.format("nef.templates.linear_system.make(%s, name='%s', neurons=%d, A=%s, tau_feedback=%g)\n",
                			pythonNetworkName,
                            linear.get("name"),
                        linear.get("neurons"),
                            a.toString(),
                        linear.get("tau_feedback")));
            }
        } 

        if (myMetaData.get("learnedterm") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("learnedterm")).values().iterator();
            while (iter.hasNext())
            {
                HashMap learnedterm = (HashMap)iter.next();

                if (!myNodeMap.containsKey(learnedterm.get("errName")))
                {
                    continue;
                }

                py.append(String.format("nef.templates.learned_termination.make(%s, errName='%s', N_err=%d, preName='%s', postName='%s', rate=%g)\n",
                			pythonNetworkName,
                            learnedterm.get("errName"),
                        learnedterm.get("N_err"),
                        learnedterm.get("preName"),
                        learnedterm.get("postName"),
                        learnedterm.get("rate")));
            }
        }   

        if (myMetaData.get("convolution") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("convolution")).values().iterator();
            while (iter.hasNext())
            {
                HashMap binding = (HashMap)iter.next();

                if (!myNodeMap.containsKey(binding.get("name")))
                {
                    continue;
                }
                
                String invert_first = (Boolean)binding.get("invert_first") ? "True" : "False";
                String invert_second = (Boolean)binding.get("invert_second") ? "True" : "False";
                String quick = (Boolean)binding.get("quick") ? "True" : "False";
                String A = binding.get("A") == null ? "None" : "'" + binding.get("A") + '\'';
                String B = binding.get("B") == null ? "None" : "'" + binding.get("B") + '\'';

                StringBuilder encoders = new StringBuilder("[");
                double[][] arr = (double[][])binding.get("encoders");
                for (int i = 0; i < arr.length; i++)
                {
                    encoders.append('[');
                    for (int j = 0; j < arr[i].length; j++)
                    {
                        encoders.append(arr[i][j]);
                        if ((j+1) < arr[i].length)
                        {
                            encoders.append(',');
                        }
                    }
                    encoders.append(']');
                    if ((i + 1) < arr.length)
                    {
                        encoders.append(',');
                    }
                }
                encoders.append(']');
                
                py.append(String.format("nef.convolution.make_convolution(%s, name='%s', A=%s, B=%s, C='%s', N_per_D=%d, quick=%s, encoders=%s, radius=%d, pstc_out=%g, pstc_in=%g, pstc_gate=%g, invert_first=%s, invert_second=%s, mode='%s', output_scale=%d)\n",
                			pythonNetworkName,
                            binding.get("name"),
                            A,
                            B,
                        binding.get("C"),
                        binding.get("N_per_D"),
                            quick,
                            encoders.toString(),
                        binding.get("radius"),
                        binding.get("pstc_out"),
                        binding.get("pstc_in"),
                        binding.get("pstc_gate"),
                            invert_first,
                            invert_second,
                        binding.get("mode"),
                        binding.get("output_scale")));
            }
        } 

        if (myMetaData.get("bgrule") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("bgrule")).values().iterator();
            while (iter.hasNext())
            {
                HashMap bgrule = (HashMap)iter.next();

                if (!myNodeMap.containsKey(bgrule.get("name")))
                {
                    continue;
                }
                
                String use_single_input = (Boolean)bgrule.get("use_single_input") ? "True" : "False";
                
                // going to assume the current network is the BG...BG_rules can only be added on BG networks.
                py.append(String.format("nef.templates.basalganglia_rule.make(%s, %s.network.getNode('%s'), index=%d, dim=%d, pattern='%s', pstc=%g, use_single_input=%s)\n",
                            pythonNetworkName,
                            pythonNetworkName,
                        bgrule.get("name"),
                        bgrule.get("index"),
                        bgrule.get("dim"),
                        bgrule.get("pattern"),
                        bgrule.get("pstc"),
                            use_single_input));
            }
        } 

        if (myMetaData.get("gate") != null)
        {
            Iterator iter = ((HashMap)myMetaData.get("gate")).values().iterator();
            while (iter.hasNext())
            {
                HashMap gate = (HashMap)iter.next();

                if (!myNodeMap.containsKey(gate.get("name")))
                {
                    continue;
                }

                py.append(String.format("nef.templates.gate.make(%s, name='%s', gated='%s', neurons=%d, pstc=%g)\n",
                			pythonNetworkName,
                            gate.get("name"),
                        gate.get("gated"),
                        gate.get("neurons"),
                        gate.get("pstc")));
            }
        }    

		return py.toString();
	}
	
	public Object getMetadata(String key) {
		if (myMetadata==null) myMetadata = new LinkedHashMap<String, Object>(2);
		return myMetadata.get(key);
	}
	public void setMetadata(String key, Object value) {
		if (myMetadata==null) myMetadata = new LinkedHashMap<String, Object>(2);		
		myMetadata.put(key, value);
	}

    @Override
    public void stopProbing(String stateName) {

    }

}
