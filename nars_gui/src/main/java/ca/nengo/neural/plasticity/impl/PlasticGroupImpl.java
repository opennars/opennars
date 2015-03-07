/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PlasticEnsembleImpl.java". Description:
"An extension of the default ensemble; connection weights can be modified by a plasticity rule"

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
package ca.nengo.neural.plasticity.impl;

import ca.nengo.model.*;
import ca.nengo.model.impl.GroupImpl;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.nef.impl.DecodedTarget;
import ca.nengo.util.TaskSpawner;
import ca.nengo.util.ThreadTask;
import ca.nengo.util.impl.LearningTask;

import java.util.*;

/**
 * <p>An extension of the default ensemble; connection weights can be modified
 * by a plasticity rule.</p>
 *
 * TODO: test
 *
 * @author Trevor Bekolay
 */
public class PlasticGroupImpl extends GroupImpl implements TaskSpawner {

    private static final long serialVersionUID = 1L;

    private float myPlasticityInterval;
    private float myLastPlasticityTime;
    private boolean myLearning = true;

    protected Map<String, PlasticGroupTarget> myPlasticEnsembleTerminations;

    private ArrayList<LearningTask> myTasks;

    /**
     * @param name Name of Ensemble
     * @param nodes Nodes that make up the Ensemble
     * @throws StructuralException if the given Nodes contain Terminations with the same
     *      name but different dimensions
     */
    public PlasticGroupImpl(String name, Node[] nodes) {
        super(name, nodes);
        myTasks = new ArrayList<LearningTask>();
        myPlasticEnsembleTerminations = new LinkedHashMap<String, PlasticGroupTarget>(6);
        myLastPlasticityTime = 0.0f;
    }

    public PlasticGroupImpl(String name, NodeFactory factory, int n) throws StructuralException {
        super(name, factory, n);
        myTasks = new ArrayList<LearningTask>();
        myPlasticEnsembleTerminations = new LinkedHashMap<String, PlasticGroupTarget>(6);
        myLastPlasticityTime = 0.0f;
    }

    public boolean getLearning() {
        return myLearning;
    }

    public void setLearning(boolean learning) {
        for (PlasticGroupTarget pet : myPlasticEnsembleTerminations.values()) {
            pet.setLearning(learning);
        }
        myLearning = learning;
    }

    protected static boolean isPopulationPlastic(NTarget[] targets) {
        boolean result = true;

        for (int i=0; i < targets.length; i++) {
            if (!(targets[i] instanceof PlasticNodeTarget)) {
                result = false;
            }
        }

        return result;
    }

    /**
     * @see ca.nengo.neural.plasticity.PlasticGroup#setPlasticityInterval(float)
     */
    public void setPlasticityInterval(float time) {
        myPlasticityInterval = time;
    }

    /**
     * @see ca.nengo.neural.plasticity.PlasticGroup#getPlasticityInterval()
     */
    public float getPlasticityInterval() {
        return myPlasticityInterval;
    }

    /**
     * @see ca.nengo.model.Group#run(float, float)
     */
    @Override
    public void run(float startTime, float endTime) throws SimulationException {
        super.run(startTime, endTime);

        setStates(endTime); // updates myLastPlasticityTime

        if ((myPlasticityInterval <= 0 && myLearning) ||
                (myLearning && endTime >= myLastPlasticityTime + myPlasticityInterval)) {
            for (LearningTask task : myTasks) {
                task.reset(false);
            }
        }
    }

    public void setStates(float endTime) throws SimulationException {
        if (myLastPlasticityTime < endTime) {
            for (PlasticGroupTarget pet : myPlasticEnsembleTerminations.values()) {
                try {
                    NSource<InstantaneousOutput> source = this.getSource(pet.getOriginName());
                    pet.setOriginState(source.getName(), source.get(), endTime);
                    pet.setTerminationState(endTime);

                    if (pet instanceof ModulatedPlasticGroupTarget) {
                        DecodedTarget modTerm = (DecodedTarget)
                        		this.getTarget(((ModulatedPlasticGroupTarget) pet).getModTermName());

                        InstantaneousOutput input = new RealOutputImpl(modTerm.getOutput(), Units.UNK, endTime);
                        ((ModulatedPlasticGroupTarget) pet).setModTerminationState
                        	(modTerm.getName(), input, endTime);
                    }
                }
                catch (StructuralException e) {
                    throw new SimulationException(e.getMessage());
                }
            }

            myLastPlasticityTime = endTime;
        }
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    public void reset(boolean randomize) {
        super.reset(randomize);
        myLastPlasticityTime = 0.0f;
    }

    /**
     * @see ca.nengo.util.TaskSpawner#getTasks
     */
    public ThreadTask[] getTasks() {
        return myTasks.toArray(new LearningTask[myTasks.size()]);
    }

    /**
     * @see ca.nengo.model.Node#getTarget(java.lang.String)
     */
    @Override
    public NTarget getTarget(String name) throws StructuralException {
        return myPlasticEnsembleTerminations.containsKey(name) ?
                myPlasticEnsembleTerminations.get(name) : super.getTarget(name);
    }

    /**
     * @see ca.nengo.model.Group#getTargets()
     */
    @Override
    public NTarget[] getTargets() {
        ArrayList<NTarget> result = new ArrayList<NTarget>(10);
        NTarget[] composites = super.getTargets();
        Collections.addAll(result, composites);

        for (NTarget t : myPlasticEnsembleTerminations.values()) {
            result.add(t);
        }
        return result.toArray(new NTarget[result.size()]);
    }

    /**
     * @see ca.nengo.util.TaskSpawner#addTasks
     */
    public void addTasks(ThreadTask[] tasks) {
        myTasks.addAll(Arrays.asList((LearningTask[]) tasks));
    }

    /**
     * @see ca.nengo.util.TaskSpawner#setTasks
     */
    public void setTasks(ThreadTask[] tasks) {
        myTasks.clear();
        this.addTasks(tasks);
    }

    @Override
    public PlasticGroupImpl clone() throws CloneNotSupportedException {
        PlasticGroupImpl result = (PlasticGroupImpl) super.clone();
        
        result.myTasks = new ArrayList<LearningTask>(myTasks.size());
        for (LearningTask task : myTasks) {
        	result.myTasks.add(task.clone(result));
        }
        
        
        result.myPlasticEnsembleTerminations = new LinkedHashMap<String, PlasticGroupTarget>(6);
        for (Map.Entry<String, PlasticGroupTarget> stringPlasticEnsembleTerminationEntry : myPlasticEnsembleTerminations.entrySet()) {
        	PlasticGroupTarget term = stringPlasticEnsembleTerminationEntry.getValue();
        	result.myPlasticEnsembleTerminations.put(stringPlasticEnsembleTerminationEntry.getKey(), term.clone(result));
        }
        
        return result;
    }
}
