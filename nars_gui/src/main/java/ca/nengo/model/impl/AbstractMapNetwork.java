package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.util.Probe;
import ca.nengo.util.VisiblyChanges;
import javolution.util.FastMap;

import java.util.*;

public abstract class AbstractMapNetwork<K, N extends Node> extends NetworkImpl<K, N> {

    protected final Map<K, N> nodeMap; //keyed on name

    public AbstractMapNetwork() {
        this(DEFAULT_NAME);
    }

    public AbstractMapNetwork(String s) {
        super(s);
        //nodeMap = new LinkedHashMap<>();
        nodeMap = new FastMap<K,N>().atomic();
        //nodeMap = new FastSortedMap<K,N>().atomic();
        //nodeMap = new FastMap<K,N>().shared();
    }






    @Override
    protected boolean add(K name, N node) {
        return (nodeMap.put(name, node)==null);
    }

    @Override
    protected N remove(K name) {
        return nodeMap.remove(name);
    }

    @Override
    protected void changed(VisiblyChanges object, K oldName, K newName) {
        nodeMap.put(newName, (N) object);
        nodeMap.remove(oldName);
    }

    @Override
    public Iterable<? extends Node> nodes() {
        return nodeMap.values();
    }

    @Override
    protected Collection<N> getNodeCollection() {
        return nodeMap.values();
    }

    @Override
    public N getNode(final K name) /*throws StructuralException*/ {
        return nodeMap.get(name);
    }

    @Override
    public String toPostScript(HashMap<String, Object> scriptData) {
        return null;
    }

    @Override
    public Network clone() throws CloneNotSupportedException {
        DefaultNetwork<N> result = (DefaultNetwork<N>) super.clone();

        result.nodeMap.clear();
        for (N oldNode : getNodeCollection()) {
            N newNode = (N)oldNode.clone();
            try {
                result.addNode(newNode);
            } catch (StructuralException e) {
                throw new RuntimeException(e);
            }
            newNode.addChangeListener(result);
        }

        //TODO: Exposed states aren't handled currently, pending redesign of Probes (it should be possible
        //		to probe things that are nested deeply, in which case exposing state woulnd't be necessary)
//		result.myProbeables
//		result.myProbeableStates

        //TODO: this works with a single Projection impl & no params; should add Projection.copy(Origin, Termination, Network)?
        result.myProjectionMap.clear();
        for (Projection oldProjection : getProjections()) {
            try {
                NSource newSource = result.getNode(oldProjection.getSource().getNode().name())
                        .getSource(oldProjection.getSource().getName());
                NTarget newTarget = result.getNode(oldProjection.getTarget().getNode().name())
                        .getTarget(oldProjection.getTarget().getName());
                Projection newProjection = new ProjectionImpl(newSource, newTarget, result);
                result.myProjectionMap.put(newTarget, newProjection);
            } catch (StructuralException e) {
                throw new CloneNotSupportedException("Problem copying Projectio: " + e.getMessage());
            }
        }

        result.myExposedSources = new HashMap<String, NSource>(myExposedSources.size());
        result.exposedSourceNames = new HashMap<NSource, String>(exposedSourceNames.size());
        result.orderedExposedSources = new LinkedList<NSource>();
        for (NSource exposed : getSources()) {
            String name = exposed.getName();
            NSource wrapped = ((SourceWrapper) exposed).getWrappedOrigin();
            try {
                // Check to see if referenced node is the network itself. If it is, handle the origin differently.
                if (wrapped.getNode().name() != myName) {
                    NSource toExpose = result.getNode(wrapped.getNode().name()).getSource(wrapped.getName());
                    result.exposeOrigin(toExpose, name);
                }
            } catch (StructuralException e) {
                throw new CloneNotSupportedException("Problem exposing Origin: " + e.getMessage());
            }
        }

        result.myExposedTargets = new HashMap<String, NTarget>(10);
        result.exposedTargetNames = new HashMap<NTarget, String>(10);
        result.orderedExposedTargets = new LinkedList<NTarget>();
        for (NTarget exposed : getTargets()) {
            String name = exposed.getName();
            NTarget wrapped = ((TargetWrapper) exposed).getWrappedTermination();
            try {
                // Check to see if referenced node is the network itself. If it is, handle the termination differently.
                if (wrapped.getNode().name() != myName) {
                    NTarget toExpose = result.getNode(wrapped.getNode().name()).getTarget(wrapped.getName());
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
        result.mySimulator.update(result);
        Probe[] oldProbes = mySimulator.getProbes();
        for (Probe oldProbe : oldProbes) {
            Probeable target = oldProbe.getTarget();
            if (target instanceof Node) {
                Node oldNode = (Node) target;
                if (oldProbe.isInEnsemble()) {
                    try {
                        Group oldGroup = (Group) getNode((K)oldProbe.getEnsembleName());
                        int neuronIndex = -1;
                        for (int j = 0; j < oldGroup.getNodes().length && neuronIndex < 0; j++) {
                            if (oldNode == oldGroup.getNodes()[j]) {
                                neuronIndex = j;
                            }
                        }
                        result.mySimulator.addProbe(oldProbe.getEnsembleName(), neuronIndex, oldProbe.getStateName(), true);
                    } catch (Exception e) {
                        throw new RuntimeException("Problem copying Probe", e);
                    }
//					} catch (StructuralException e) {
//						ourLogger.warn("Problem copying Probe", e);
//					}
                } else {
                    try {
                        result.mySimulator.addProbe(oldNode.name(), oldProbe.getStateName(), true);
                    } catch (Exception e) {
                        throw new RuntimeException("Problem copying Probe", e);
                    }
                }
            } else {
                throw new RuntimeException("Can't copy Probe on type " + target.getClass().getName()
                        + " (to be addressed in a future release)");
            }
        }

        return result;
    }

}
