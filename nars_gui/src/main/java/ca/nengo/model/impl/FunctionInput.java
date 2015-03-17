/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "FunctionInput.java". Description:
"An Node that produces real-valued output based on functions of time"

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
 * Created on 6-Jun-2006
 */
package ca.nengo.model.impl;

/**
 * An Node that produces real-valued output based on functions of time.
 *
 * @author Bryan Tripp
 */

import ca.nengo.math.Function;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.math.impl.FourierFunction;
import ca.nengo.math.impl.PostfixFunction;
import ca.nengo.model.*;
import ca.nengo.util.ScriptGenException;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.VisiblyChanges;
import ca.nengo.util.VisiblyChangesUtils;
import ca.nengo.util.impl.TimeSeriesImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * A class to compute functions analytically and provide that input to
 * other Nodes in a network.
 */
public class FunctionInput implements Node<Node>, Probeable {

	/**
	 * Name for the default origin
	 */
	public static final String ORIGIN_NAME = "origin";

	/**
	 * Name for the default input
	 */
	public static final String STATE_NAME = "input";

	private static final long serialVersionUID = 1L;

	private String myName;
	private Function[] myFunctions;
	private Units myUnits;
	private float myTime;
//	private float[] myValues;
	private BasicSource myOrigin;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;

	/**
	 * @param name The name of this Node
	 * @param functions Functions of time (simulation time) that produce the values
	 * 		that will be output by this Node. Each given function corresponds to
	 * 		a dimension in the output vectors. Each function must have input dimension 1.
	 * @param units The units in which the output values are to be interpreted
	 * @throws StructuralException if functions are not all 1D functions of time
	 */
	public FunctionInput(String name, Function[] functions, Units units) throws StructuralException {
		myOrigin = new BasicSource(this, FunctionInput.ORIGIN_NAME, functions.length, units);
		setFunctions(functions);

		myName = name;
		myUnits = units;

		run(0f, 0f); //set initial state to f(0)
	}

	private static void checkFunctionDimension(Function[] functions) throws StructuralException {
		for (Function function : functions) {
			if (function.getDimension() != 1) {
				throw new StructuralException("All functions in a FunctionOrigin must be 1-D functions of time");
			}
		}
	}

	/**
	 * @param functions New list of functions (of simulation time) that define the output of this Node.
	 * 		(Must have the same length as existing Function list.)
	 * @throws StructuralException if functions are not all 1D functions of time
	 */
	public void setFunctions(Function[] functions) throws StructuralException {
		checkFunctionDimension(functions);
		myOrigin.setDimensions(functions.length);
		myFunctions = functions;
	}

	/**
	 * @return array of functions
	 */
	public Function[] getFunctions() {
		return myFunctions;
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
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public void run(float startTime, float endTime) {
		myTime = endTime;

		float[] values = new float[myFunctions.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = myFunctions[i].map(new float[]{myTime});
		}

		myOrigin.setValues(startTime, endTime, values);
	}

	/**
	 * This method does nothing, as the FunctionInput has no state.
	 *
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myOrigin.reset(randomize);
	}

	/**
	 * This call has no effect. DEFAULT mode is always used.
	 *
	 * @see ca.nengo.model.Node#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
	}

	/**
	 * @return SimulationMode.DEFAULT
	 *
	 * @see ca.nengo.model.Node#getMode()
	 */
	public SimulationMode getMode() {
		return SimulationMode.DEFAULT;
	}

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		TimeSeries result = null;

		if (!STATE_NAME.equals(stateName)) {
			throw new SimulationException("State " + stateName + " is unknown");
		}

		float[] values = ((RealSource) myOrigin.get()).getValues();
		result = new TimeSeriesImpl(new float[]{myTime}, new float[][]{values}, Units.uniform(myUnits, values.length));

		return result;
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		Properties result = new Properties();
		result.setProperty(STATE_NAME, "Function of time");
		return result;
	}
	
	/**
	 * @see ca.nengo.model.Node#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
		if (!ORIGIN_NAME.equals(name)) {
			throw new StructuralException("This Node only has origin FunctionInput.ORIGIN_NAME");
		}

		return myOrigin;
	}

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
	public NSource[] getSources() {
		return new NSource[]{myOrigin};
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		throw new StructuralException("This node has no Terminations");
	}

	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
		return new NTarget[0];
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

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		StringBuilder py = new StringBuilder();
        boolean isFourier = true;

        py.append('\n');
        
        for (int i = 0; i < myFunctions.length; i++) {
            if (!(myFunctions[i] instanceof FourierFunction))
            {
                isFourier = false;
                break;
            }
        }

        if (isFourier) {
            StringBuilder base = new StringBuilder("[");
            StringBuilder high = new StringBuilder("[");
            StringBuilder power = new StringBuilder("[");
            
            for (int i = 0; i < myFunctions.length; i++) {           
                FourierFunction func = (FourierFunction)myFunctions[i];
                
                if (func.getFundamental() == 0.0f) {
                    throw new ScriptGenException("Cannot generate a Fourier Function that was built by specifiying all frequencies, amplitudes and phases");
                }

                base.append(func.getFundamental());
                high.append(func.getCutoff());
                power.append(func.getRms());
                if ((i + 1) < myFunctions.length) {
                    base.append(',');
                    high.append(',');
                    power.append(',');
                }
            }

            base.append(']');
            high.append(']');
            power.append(']');

            py.append(String.format("%s.make_fourier_input('%s', dimensions=%d, base=%s, high=%s, power=%s)\n",
                        scriptData.get("netName"),
                        myName,
                        myFunctions.length,
                        base,
                        high,
                        power));
        } else {
            StringBuilder funcs = new StringBuilder("[");
            for (int i = 0; i < myFunctions.length; i++) {
            	
            	String functionName = String.format("Function%c%s%c%d",
                        scriptData.get("spaceDelim"),
													myName.replaceAll("\\p{Blank}|\\p{Punct}", scriptData.get("spaceDelim").toString()),
                        scriptData.get("spaceDelim"),
													i);

                if (myFunctions[i] instanceof ConstantFunction) {
                    ConstantFunction func = (ConstantFunction)myFunctions[i];
                    
                    py.append(String.format("%s = ConstantFunction(%d, %.3f)\n",
                                			functionName,
                                			func.getDimension(),
                                			func.getValue()));
                    
                } else if (myFunctions[i] instanceof FourierFunction) {
                    FourierFunction func = (FourierFunction)myFunctions[i];

                    py.append(String.format("%s = FourierFunction(%f, %f, %f, %d)\n",
                    			functionName,
                                func.getFundamental(),
                                func.getCutoff(),
                                func.getRms(),
                                func.getSeed()));
                } else if (myFunctions[i] instanceof PostfixFunction) {
                    PostfixFunction func = (PostfixFunction)myFunctions[i];

                    py.append(String.format("%s = PostfixFunction('%s', %d)\n",
                    			functionName,
                                func.getExpression(),
                                func.getDimension()));
                }

                funcs.append(functionName);
                
                if ((i + 1) < myFunctions.length) {
                    funcs.append(", ");
                }
            }
            funcs.append(']');
                                
            py.append(String.format("%s.make_input('%s', values=%s)\n",
                    scriptData.get("netName"),
                    myName,
                    funcs.toString()));
        }
        
        return py.toString();
    }

	@Override
	public Node clone() throws CloneNotSupportedException {
		FunctionInput result = (FunctionInput) super.clone();

		Function[] functions = new Function[myFunctions.length];
		for (int i = 0; i < functions.length; i++) {
			functions[i] = myFunctions[i].clone();
		}
		result.myFunctions = functions;

		result.myOrigin = new BasicSource(result, FunctionInput.ORIGIN_NAME, functions.length, myUnits);
		if (myOrigin.getNoise() != null) {
            result.myOrigin.setNoise(myOrigin.getNoise().clone());
        }

		result.myOrigin.accept(myOrigin.get());


		result.myListeners = new ArrayList<Listener>(5);

		return result;
	}

	public Node[] getChildren() {
		return new Node[0];
	}
}
