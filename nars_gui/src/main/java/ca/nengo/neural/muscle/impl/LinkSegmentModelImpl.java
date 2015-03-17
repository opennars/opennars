/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "LinkSegmentModelImpl.java". Description:
"Default implementation of LinkSegmentModel"

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

/**
 *
 */
package ca.nengo.neural.muscle.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.math.Function;
import ca.nengo.model.*;
import ca.nengo.neural.muscle.LinkSegmentModel;
import ca.nengo.neural.muscle.SkeletalMuscle;
import ca.nengo.util.*;
import ca.nengo.util.impl.TimeSeries1DImpl;
import ca.nengo.util.impl.TimeSeriesImpl;

import java.util.*;

/**
 * Default implementation of LinkSegmentModel.
 *
 * @author Bryan Tripp
 */
public class LinkSegmentModelImpl implements LinkSegmentModel {

	private static final long serialVersionUID = 1L;

	private String myName;
	private DynamicalSystem myDynamics;
	private Map<String, Function[]> myJointDefinitions;
	private SkeletalMuscle[] myMuscles;
	private Function[] myLengths;
	private Function[] myMomentArms;
	private Properties myStates;
	private float myTimeStep;
	private float myTime;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;

	/**
	 * @param name Segment name
	 * @param dynamics Dynamical system governing function
	 * @param timeStep dt
	 */
	public LinkSegmentModelImpl(String name, DynamicalSystem dynamics, float timeStep) {
		myDynamics = dynamics;
		myMuscles = new SkeletalMuscle[dynamics.getInputDimension()];
		myLengths = new Function[dynamics.getInputDimension()];
		myMomentArms = new Function[dynamics.getInputDimension()];
		myJointDefinitions = new HashMap<String, Function[]>(10);

		myStates = new Properties();
		for (int i = 0; i < dynamics.getState().length; i++) {
			myStates.setProperty("q" + i, "Generalized coordinate " + i);
		}
	}

	/**
	 * @param name Name of joint
	 * @param definition 2 or 3 Functions of generalized coordinates, corresponding to (x,y) position
	 * 		of the joint or (x,y,z) position of the joint
	 */
	public void defineJoint(String name, Function[] definition) {
		if (definition.length != 2 && definition.length != 3) {
			throw new IllegalArgumentException("Either 2 or 3 functions of generalized coordinates " +
					"are needed to define a joint: (x,y) or (x.y,z)");
		}

		myJointDefinitions.put(name, definition);
		myStates.setProperty(name, "Joint coordinates for " + name);
	}

	/**
	 * @param input Which of the n inputs are we defining?
	 * @param muscle Muscle being defined
	 * @param length Function defining length?
	 * @param momentArm Function defining momentum?
	 */
	public void defineMuscle(int input, SkeletalMuscle muscle, Function length, Function momentArm) {
		myMuscles[input] = muscle;
		myLengths[input] = length;
		myMomentArms[input] = momentArm;
	}

	/**
	 * @see ca.nengo.neural.muscle.LinkSegmentModel#getJointNames()
	 */
	public String[] getJointNames() {
        Set<String> var = myJointDefinitions.keySet();
        return var.toArray(new String[var.size()]);
	}

	/**
	 * @see ca.nengo.neural.muscle.LinkSegmentModel#getMuscles()
	 */
	public SkeletalMuscle[] getMuscles() {
		return myMuscles;
	}

	/**
	 * @see ca.nengo.model.Node#getMode()
	 */
	public SimulationMode getMode() {
		return SimulationMode.DEFAULT;
	}

	/**
	 * @see ca.nengo.model.Node#name()
	 */
	public String name() {
		return myName;
	}

	/**
	 * @see ca.nengo.model.Node#setName(java.lang.String)
	 */
	public void setName(String name) throws StructuralException {
		VisiblyChangesUtils.nameChanged(this, name(), name, myListeners);
		myName = name;
	}

	/**
	 * @see ca.nengo.model.Node#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
		throw new StructuralException("A LinkSegmentModel itself has no Origins (neural output arises from component SkeletalMuscles)");
	}

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
	public NSource[] getSources() {
		return new NSource[0];
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		throw new StructuralException("A LinkSegmentModel itself has no Terminations (neural input is to component SkeletalMuscles)");
	}

	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
		return new NTarget[0];
	}

	/**
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = startTime;

		while (myTime < endTime) {
			float stepLength = (myTime + myTimeStep * 1.1 >= endTime) ? endTime - myTime : myTimeStep;
			step(myTime, stepLength);
		}

		myTime = endTime;
	}

	private void step(float startTime, float stepLength) throws SimulationException {
		float[] state = myDynamics.getState();
		float[] muscleTorques = new float[myDynamics.getInputDimension()];
		for (int i = 0; i < muscleTorques.length; i++) {
			myMuscles[i].setLength(myLengths[i].map(state));
			myMuscles[i].run(startTime, startTime + stepLength);
			muscleTorques[i] = myMuscles[i].getForce() * myMomentArms[i].map(state);
		}

		float[] dxdt = myDynamics.f(startTime, muscleTorques);
		myDynamics.setState(MU.sum(state, MU.prod(dxdt, stepLength)));
		myTime += stepLength;
	}

	/**
	 * @see ca.nengo.model.Node#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myDynamics.setState(new float[myDynamics.getState().length]);
	}

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		TimeSeries result = null;

		if (myJointDefinitions.containsKey(stateName)) {
			Function[] definition = myJointDefinitions.get(stateName);
			float[] jointCoordinates = new float[definition.length];
			float[] genCoordinates = myDynamics.getState();
			for (int i = 0; i < definition.length; i++) {
				jointCoordinates[i] = definition[i].map(genCoordinates);
			}
			result = new TimeSeriesImpl(new float[]{myTime},
					new float[][]{jointCoordinates}, Units.uniform(Units.M, definition.length));
		} else if (stateName.matches("p\\d+")) {
			int coord = Integer.parseInt(stateName.substring(1));
			result = new TimeSeries1DImpl(new float[]{myTime}, new float[]{myDynamics.getState()[coord]}, Units.UNK);
		} else {
			throw new SimulationException("The state " + stateName + " is unknown");
		}

		return result;
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		return myStates;
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

	@Override
	public LinkSegmentModel clone() throws CloneNotSupportedException {
		LinkSegmentModelImpl result = (LinkSegmentModelImpl) super.clone();
		result.myDynamics = myDynamics.clone();

		Map<String, Function[]> jointDefs = new HashMap<String, Function[]>(10);
		for (Map.Entry<String, Function[]> stringEntry : myJointDefinitions.entrySet()) {
			Function[] functions  = new Function[stringEntry.getValue().length];
			for (int i = 0; i < functions.length; i++) {
				functions[i] = stringEntry.getValue()[i];
			}
			jointDefs.put(stringEntry.getKey(), functions);
		}
		result.myJointDefinitions = jointDefs;

		Function[] lengths = new Function[myLengths.length];
		for (int i = 0; i < lengths.length; i++) {
			lengths[i] = myLengths[i].clone();
		}
		result.myLengths = lengths;

		Function[] momentArms = new Function[myMomentArms.length];
		for (int i = 0; i < momentArms.length; i++) {
			momentArms[i] = myMomentArms[i].clone();
		}
		result.myMomentArms = momentArms;

		SkeletalMuscle[] muscles = new SkeletalMuscle[myMuscles.length];
		for (int i = 0; i < muscles.length; i++) {
			muscles[i] = (SkeletalMuscle) myMuscles[i].clone();
		}
		result.myMuscles = muscles;

		result.myStates = (Properties) myStates.clone();
		return result;
	}

	public Node[] getChildren() {
		return new Node[0];
	}

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		return "";
	}

}
