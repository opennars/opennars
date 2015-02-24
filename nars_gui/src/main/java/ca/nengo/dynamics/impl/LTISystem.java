/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "LTISystem.java". Description:
"A linear time-invariant dynamical system model in state-space form"

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
 * Created on 1-Jun-2006
 */
package ca.nengo.dynamics.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.LinearSystem;
import ca.nengo.model.Units;
import ca.nengo.util.MU;

/**
 * A linear time-invariant dynamical system model in state-space form. Such a system
 * can be defined in terms of the four matrices that must be provided in the constructor.
 *
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class LTISystem implements LinearSystem {

	private static final long serialVersionUID = 1L;

	private float[][] A;
	private float[][] B;
	private float[][] C;
	private float[][] D;
	private float[] x;
	private Units[] myOutputUnits;

	/**
	 * Each argument is an array of arrays that represents a matrix. The first
	 * dimension represents the matrix row and the second the matrix column, so
	 * that A_ij corresponds to A[i-1][j-1] (since arrays are indexed from 0).
	 *
	 * The matrices must have valid dimensions for a state-space model: A must be
	 * n x n; B must be n x p; C must be q x n; and D must be q x p.
	 *
	 * @param A Dynamics matrix
	 * @param B Input matrix
	 * @param C Output matrix
	 * @param D Passthrough matrix
	 * @param x0 Initial state
	 * @param outputUnits Units in which each dimension of the output are expressed
	 */
	public LTISystem(float[][] A, float[][] B, float[][] C, float[][] D, float[] x0, Units[] outputUnits) {
		checkIsStateModel(A, B, C, D, x0);
		if (outputUnits.length != C.length) {
			throw new IllegalArgumentException("Units needed for each output");
		}

		init(A, B, C, D, x0, outputUnits);
	}

	private void init(float[][] A, float[][] B, float[][] C, float[][] D, float[] x0, Units[] outputUnits) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.D = D;
		this.x = x0;
		this.myOutputUnits = outputUnits;
	}


	//checks that matrices have the dimensions that form a valid state model
	private static void checkIsStateModel(float[][] A, float[][] B, float[][] C, float[][] D, float[] x) {
		checkIsMatrix(A);
		checkIsMatrix(B);
		checkIsMatrix(C);
		checkIsMatrix(D);

		checkSameDimension(A.length, A[0].length, "A matrix must be square");
		checkSameDimension(x.length, A.length, "A matrix must be nXn, where n is length of state vector");
		checkSameDimension(A.length, B.length, "Numbers of rows in A and B must be the same");
		checkSameDimension(A[0].length, C[0].length, "Numbers of columns in A and C must be the same");
		checkSameDimension(D.length, C.length, "Numbers of rows in C and D must be the same");
		checkSameDimension(D[0].length, B[0].length, "Numbers of columns in B and D must be the same");
	}

	private static void checkIsMatrix(float[][] matrix) {
		if (!MU.isMatrix(matrix)) {
			throw new IllegalArgumentException("Not all matrix rows have the same length");
		}
	}

	private static void checkSameDimension(int one, int two, String message) {
		if (one != two) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * @return Ax + Bu
	 *
	 * @see ca.nengo.dynamics.DynamicalSystem#f(float, float[])
	 */
	public float[] f(float t, float[] u) {
		assert u.length == getInputDimension();

		return a1x1plusa2x2(A, x, B, u);
	}

	/**
	 * @return Cx + Du
	 *
	 * @see ca.nengo.dynamics.DynamicalSystem#g(float, float[])
	 */
	public float[] g(float t, float[] u) {
		assert u.length == getInputDimension();

		return a1x1plusa2x2(C, x, D, u);
	}

	//does not check dimensions -- we leave this to prior assertion of dimensionsOK(...)
	private static float[] a1x1plusa2x2(float[][] A1, float[] x1, float[][] A2, float[] x2) {
		float[] result = new float[A1.length];

		for (int i = 0; i < A1.length; i++) {
			for (int j = 0; j < A1[0].length; j++) {
				result[i] += A1[i][j] * x1[j];
			}
			for (int j = 0; j < A2[0].length; j++) {
				result[i] += A2[i][j] * x2[j];
			}
		}

		return result;
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getState()
	 */
	public float[] getState() {
		return x;
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#setState(float[])
	 */
	public void setState(float[] state) {
		assert state.length == x.length;

		x = state;
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getInputDimension()
	 */
	public int getInputDimension() {
		return B[0].length;
	}

	/**
	 * @param dim Input dimensionality. Affects B and D.
	 */
	public void setInputDimension(int dim) {
		B = copyColumns(B, dim);
		D = copyColumns(D, dim);
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getOutputDimension()
	 */
	public int getOutputDimension() {
		return C.length;
	}

	/**
	 * @param dim Output dimensionality. Affects C and D.
	 */
	public void setOutputDimension(int dim) {
		C = copyRows(C, dim);
		D = copyRows(D, dim);

		Units[] newUnits = Units.uniform(Units.UNK, dim);
		System.arraycopy(myOutputUnits, 0, newUnits, 0, Math.min(dim, myOutputUnits.length));
		myOutputUnits = newUnits;
	}

	/**
	 * @return State (x) dimensionality.
	 */
	public int getStateDimension() {
		return x.length;
	}

	/**
	 * @param dim State (x) dimensionality
	 */
	public void setStateDimension(int dim) {
		float[] newX = new float[dim];
		System.arraycopy(x, 0, newX, 0, Math.min(dim, x.length));
		x = newX;

		A = copyRows(A, dim);
		A = copyColumns(A, dim);
		B = copyRows(B, dim);
		C = copyColumns(C, dim);
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getOutputUnits(int)
	 */
	public Units getOutputUnits(int outputDimension) {
		return myOutputUnits[outputDimension];
	}

	/**
	 * @param outputDimension dimensionality of output
	 * @param units Units to work in
	 */
	public void setOutputUnits(int outputDimension, Units units) {
		myOutputUnits[outputDimension] = units;
	}

	/**
	 * @see ca.nengo.dynamics.LinearSystem#getA(float)
	 */
	public float[][] getA(float t) {
		return MU.clone(A);
	}

	/**
	 * @return The dynamics matrix at the current time
	 */
	public float[][] getA() {
		return MU.clone(A);
	}

	/**
	 * @param newA New dynamics matrix
	 */
	public void setA(float[][] newA) {
		checkIsMatrix(newA);
		checkSameDimension(newA.length, newA[0].length, "A matrix must be square");
		checkSameDimension(newA.length, A.length, "A matrix must match state dimension " + A.length);
		A = newA;
	}

	/**
	 * @see ca.nengo.dynamics.LinearSystem#getB(float)
	 */
	public float[][] getB(float t) {
		return MU.clone(B);
	}

	/**
	 * @return The input matrix at the current time
	 */
	public float[][] getB() {
		return MU.clone(B);
	}

	/**
	 * @param newB New input matrix
	 */
	public void setB(float[][] newB) {
		checkIsMatrix(newB);
		checkSameDimension(newB.length, B.length, "B matrix must match state dimension " + B.length);
		checkSameDimension(newB[0].length, B[0].length, "B matrix must match input dimension " + B[0].length);
		B = newB;
	}

	/**
	 * @see ca.nengo.dynamics.LinearSystem#getC(float)
	 */
	public float[][] getC(float t) {
		return MU.clone(C);
	}

	/**
	 * @return The output matrix at the current time
	 */
	public float[][] getC() {
		return MU.clone(C);
	}

	/**
	 * @param newC New output matrix
	 */
	public void setC(float[][] newC) {
		checkIsMatrix(newC);
		checkSameDimension(newC.length, C.length, "C matrix must match output dimension " + C.length);
		checkSameDimension(newC[0].length, C[0].length, "B matrix must match state dimension " + C[0].length);
		C = newC;
	}

	/**
	 * @see ca.nengo.dynamics.LinearSystem#getD(float)
	 */
	public float[][] getD(float t) {
		return MU.clone(D);
	}

	/**
	 * @return The passthrough matrix at the current time
	 */
	public float[][] getD() {
		return MU.clone(D);
	}

	/**
	 * @param newD New passthrough matrix
	 */
	public void setD(float[][] newD) {
		checkIsMatrix(newD);
		checkSameDimension(newD.length, D.length, "D matrix must match output dimension " + D.length);
		checkSameDimension(newD[0].length, D[0].length, "D matrix must match input dimension " + D[0].length);
		D = newD;
	}

	private float[][] copyRows(float[][] original, int n) {
		float[][] result = new float[n][];
		System.arraycopy(original, 0, result, 0, Math.min(n, original.length));
		for (int i = original.length; i < n; i++) {
			result[i] = new float[original[0].length];
		}
		return result;
	}

	private float[][] copyColumns(float[][] original, int n) {
		return MU.transpose(copyRows(MU.transpose(original), n));
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#clone()
	 */
	public DynamicalSystem clone() throws CloneNotSupportedException {
		LTISystem result = (LTISystem) super.clone();

		float[] state = new float[result.getState().length];
		System.arraycopy(result.getState(), 0, state, 0, state.length);
		result.setState(state);

		return result;
	}

}
