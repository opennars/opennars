/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "RandomHypersphereVG.java". Description: 
"Generates random vectors distributed on or in a hypersphere"

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
 * Created on 4-Jun-2006
 */
package ca.nengo.util.impl;

import ca.nengo.math.PDFTools;
import ca.nengo.math.impl.GaussianPDF;
import ca.nengo.util.VectorGenerator;

/**
 * Generates random vectors distributed on or in a hypersphere. 
 * 
 * TODO: Reference Deak, Muller
 * 
 * @author Bryan Tripp
 */
public class RandomHypersphereVG implements VectorGenerator, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	
	private boolean mySurface;
	private float myRadius;
	private float myAxisClusterFactor;
	private boolean myAllOnAxes; //true if vectors are all to lie on an axis
	private float myAxisRatio; //ratio of vector density between cluster-centre axis to other axes
	
	/**
	 * @param surface If true, vectors are generated on surface of hypersphere; if false, throughout
	 * 		volume of hypersphere
	 * @param radius Radius of hypersphere
	 * @param axisClusterFactor Value between 0 and 1, with higher values indicating greater clustering
	 * 		of vectors around axes. 0 means even distribution; 1 means all vectors on axes.  
	 */
	public RandomHypersphereVG(boolean surface, float radius, float axisClusterFactor) {
		setOnSurface(surface);
		setRadius(radius);				
		setAxisClusterFactor(axisClusterFactor);
	}
	
	/**
	 * Uses default settings (on surface; radius 1; no axis cluster)  
	 */
	public RandomHypersphereVG() {
		this(true, 1, 0);
	}
	
	/**
	 * @return True if generated vectors are on surface of hypersphere
	 */
	public boolean getOnSurface() {
		return mySurface;
	}
	
	/**
	 * @param onSurface True if generated vectors are on surface of hypersphere
	 */
	public void setOnSurface(boolean onSurface) {
		mySurface = onSurface;
	}

	/**
	 * @return Radius of hypersphere
	 */
	public float getRadius() {
		return myRadius;
	}
	
	/**
	 * @param radius Radius of hypersphere
	 */
	public void setRadius(float radius) {
		if (radius <= 0) {
			throw new IllegalArgumentException(radius + " is not a valid radius (must be > 0)");
		}		
		myRadius = radius;
	}

	/**
	 * @return Value between 0 and 1, with higher values indicating greater clustering
	 * 		of vectors around axes. 0 means even distribution; 1 means all vectors on axes.  
	 */
	public float getAxisClusterFactor() {
		return myAxisClusterFactor;
	}

	/**
	 * @param axisClusterFactor Value between 0 and 1, with higher values indicating greater clustering
	 * 		of vectors around axes. 0 means even distribution; 1 means all vectors on axes.
	 */
	public void setAxisClusterFactor(float axisClusterFactor) {
		if (axisClusterFactor < 0 || axisClusterFactor > 1) {
			throw new IllegalArgumentException(axisClusterFactor + " is not a valid cluster factor (must be between 0 and 1)");
		}

		myAxisClusterFactor = axisClusterFactor;
		if (axisClusterFactor > .999) {
			myAllOnAxes = true;
		} else {
			myAllOnAxes = false;
			myAxisRatio = (float) Math.tan( (.5 + axisClusterFactor/2) * (Math.PI / 2) );
		}		
	}

	/**
	 * @see ca.nengo.util.VectorGenerator#genVectors(int, int)
	 */
	public float[][] genVectors(int number, int dimension) {
		float[][] result = new float[number][]; //we'll generate from a unit sphere then scale to radius
		
		for (int i = 0; i < number; i++) {
			float[] vector = null;
			
			if (dimension == 1) {
				vector = new float[]{ genScalar(myRadius, mySurface) };
			} else if (myAllOnAxes) {
				vector = genOnAxes(dimension, myRadius, mySurface);
			} else {
				vector = genOffAxes(dimension, myRadius, mySurface);
			}
			
			result[i] = vector;
		}
		
		return result;
	}
	
	private static float genScalar(float radius, boolean surface) {
		if (surface) {
			return PDFTools.random() > .5 ? radius : -radius;
		} else {
			return 2f * (float) (PDFTools.random() - .5) * radius;
		}		
	}
	
	private static float[] genOnAxes(int dimension, float radius, boolean surface) {
		float[] result = new float[dimension];			
		int axis = (int) Math.floor(PDFTools.random() * dimension); 

		for (int i = 0; i < dimension; i++) {
			result[i] = (i == axis) ? genScalar(radius, surface) : 0f;
		}
		
		return result;
	}
	
	private float[] genOffAxes(int dimension, float radius, boolean surface) {
		float[] result = new float[dimension];			
		int axis = (int) Math.floor(PDFTools.random() * dimension); 
		
		float scale = mySurface ? 1f : (float) Math.pow(PDFTools.random(), 1d / ((double) dimension));

		for (int i = 0; i < dimension; i = i + 2) { //note the increment by 2 
			float[] samples = GaussianPDF.doSample();
			result[i] = samples[0] * myRadius;
			if (i < dimension - 1) result[i+1] = samples[1] * myRadius;
		}				
		result[axis] = result[axis] * myAxisRatio;
						
		float normSquared = 0f;
		for (int i = 0; i < dimension; i++) {
			normSquared += result[i] * result[i];
		}
		
		float norm = (float) Math.pow(normSquared, .5); 
		for (int i = 0; i < dimension; i++) {
			result[i] = result[i] * scale / norm;
		}

		return result;
	}
	
//	private float[][] genRegularScalars(int number, float radius) {
//	float[][] result = new float[number][];
//	
//	int n1 = Math.round((float) number / 2f);
//	
//	for (int i = 0; i < number; i++) {
//		result[i] = new float[]{ i < n1 ? radius : -radius };
//	}
//	
//	return result;
//}

}
