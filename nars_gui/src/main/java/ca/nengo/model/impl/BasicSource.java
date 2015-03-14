/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "BasicOrigin.java". Description:
"A generic implementation of Origin"

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
 * Created on 3-Jun-07
 */
package ca.nengo.model.impl;

import ca.nengo.config.PropretiesUtil;
import ca.nengo.config.Configurable;
import ca.nengo.config.Configuration;
import ca.nengo.config.Property;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.config.impl.SingleValuedPropertyImpl;
import ca.nengo.model.*;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * A generic implementation of Origin. Nodes that contain an Origin of this type should call one
 * of the setValues() methods with every Node.run(...).
 *
 * @author Bryan Tripp
 */
public class BasicSource implements NSource<InstantaneousOutput>, Noise.Noisy, Resettable, Configurable {

	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(BasicSource.class);

	private Node myNode;
	private String myName;
	private int myDimension;
	private Units myUnits;
	private InstantaneousOutput myValues;
	private Noise myNoise;
	private Noise[] myNoises; //per output
	private transient ConfigurationImpl myConfiguration;
	private boolean myRequiredOnCPU;



    public BasicSource(Node node, String name) {
        this(node, name, 0, Units.UNK);
    }
	/**
	 * @param node The parent Node
	 * @param name Name of origin
	 * @param dimension Dimension of output of this Origin
	 * @param units The output units
	 */
	public BasicSource(Node node, String name, int dimension, Units units) {
		myNode = node;
		myName = name;
		myDimension = dimension;
		myUnits = units;
		myValues = new RealOutputImpl(new float[dimension], units, 0);
	}

	private void initConfiguration() {
		myConfiguration = PropretiesUtil.defaultConfiguration(this);
		myConfiguration.removeProperty("dimensions");
		try {
			Property p = new SingleValuedPropertyImpl(myConfiguration, "dimensions", Integer.TYPE,
					this.getClass().getMethod("getDimensions"));
			myConfiguration.defineProperty(p);
		} catch (Exception e) {
			ourLogger.warn("Can't define property 'dimensions'", e);
		}
	}

	/**
	 * @see ca.nengo.config.Configurable#getConfiguration()
	 */
	public Configuration getConfiguration() {
		if (myConfiguration == null) {
			initConfiguration();
		}
		return myConfiguration;
	}

	/**
	 * This method is normally called by the Node that contains this Origin, to set the input that is
	 * read by other nodes from getValues(). If the Noise model has been set, noise is applied to the
	 * given values.
	 *
	 * @param startTime Start time of step for which outputs are being defined
	 * @param endTime End time of step for which outputs are being defined
	 * @param values Values underlying RealOutput that is to be output by this Origin in subsequent
	 * 		calls to getValues()
	 */
	public void setValues(float startTime, float endTime, float[] values) {
		assert values.length == myDimension;

		float[] v = values;
		if (myNoise != null) {
			v = new float[myDimension];
			System.arraycopy(values, 0, v, 0, values.length);
			for (int i = 0; i < myDimension; i++) {
				v[i] = myNoises[i].getValue(startTime, endTime, values[i]);
			}
		}

		myValues = new RealOutputImpl(v, myUnits, endTime);
	}

	/**
	 * This method is normally called by the Node that contains this Origin, to set the input that is
	 * read by other nodes from getValues(). No noise is applied to the given values.
	 *
	 * @param values Values to be output by this Origin in subsequent calls to getValues()
	 */
	public void accept(InstantaneousOutput values) {
		assert values.getDimension() == myDimension;

		myValues = values;
	}

	/**
	 * @see ca.nengo.model.NSource#getDimensions()
	 */
	public int getDimensions() {
		return myDimension;
	}

	/**
	 * @param dim Origin dimensionality
	 */
	public void setDimensions(int dim) {
		myDimension = dim;
		if (myNoise != null) {
			setNoise(myNoise);
		}
		reset(false);
	}

	/**
	 * @see ca.nengo.model.NSource#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @param name Origin name
	 */
	public void setName(String name) {
		myName = name;
	}

	/**
	 * @return Units used by this origin
	 */
	public Units getUnits() {
		return myUnits;
	}

	/**
	 * @param units Units used by this origin
	 */
	public void setUnits(Units units) {
		myUnits = units;
	}

	/**
	 * @see ca.nengo.model.NSource#get()
	 */
	public InstantaneousOutput get()  {
		return myValues;
	}

	/**
	 * @see ca.nengo.model.Noise.Noisy#getNoise()
	 */
	public Noise getNoise() {
		return myNoise;
	}

	/**
	 * Note that noise is only applied to RealOutput.
	 *
	 * @see ca.nengo.model.Noise.Noisy#setNoise(ca.nengo.model.Noise)
	 */
	public void setNoise(Noise noise) {
		myNoise = noise;
		myNoises = new Noise[myDimension];
		for (int i = 0; i < myDimension; i++) {
			myNoises[i] = myNoise.clone();
		}
	}

	/**
	 * @see ca.nengo.model.NSource#getNode()
	 */
	public Node getNode() {
		return myNode;
	}

//	public void setNode(Node node) {
//		myNode = node;
//	}

	@Override
	public BasicSource clone() throws CloneNotSupportedException {
		BasicSource result = (BasicSource) super.clone();
		if (myNoise != null) {
            result.setNoise(myNoise.clone());
        }
		if (myValues != null) {
			result.accept(myValues.clone());
		}
		return result;
	}
	
	public BasicSource clone(Node node) throws CloneNotSupportedException {
		BasicSource result = this.clone();
		result.myNode = node;
		return result;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		if (myNoise != null) {
            myNoise.reset(randomize);
        }
		if (myNoises != null) {
			for (Noise myNoise2 : myNoises) {
				myNoise2.reset(randomize);
			}
		}
		myValues = new RealOutputImpl(new float[myDimension], myUnits, 0);
	}

	public void setRequiredOnCPU(boolean val){
	    myRequiredOnCPU = val;
	}
    
    public boolean getRequiredOnCPU(){
        return myRequiredOnCPU;
    }
}