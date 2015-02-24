/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PolynomialCurveFitter.java". Description:
"A least-squares polynomial CurveFitter"

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
 * Created on 1-Dec-2006
 */
package ca.nengo.math.impl;

import Jama.Matrix;
import ca.nengo.math.CurveFitter;
import ca.nengo.math.Function;

/**
 * <p>A least-squares polynomial CurveFitter.</p>
 *
 * <p>See http://mathworld.wolfram.com/LeastSquaresFittingPolynomial.html </p>
 *
 * TODO: write proper tests
 *
 * @author Bryan Tripp
 */
public class PolynomialCurveFitter implements CurveFitter {

	private final int myOrder;

	/**
	 * @param order Order of polynomials used to approximate example points
	 */
	public PolynomialCurveFitter(int order) {
		myOrder = order;
	}

	/**
	 * @see ca.nengo.math.CurveFitter#fit(float[], float[])
	 */
	public Function fit(float[] x, float[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Arrays x and y must have the same length; we take it that y = f(x)");
		}

		Matrix X = new Matrix(x.length, myOrder+1);
		Matrix Y = new Matrix(y.length, 1);
		for (int i = 0; i < x.length; i++) {
			X.set(i, 0, 1d);
			Y.set(i, 0, y[i]);

			float xpowj = x[i];
			for (int j = 1; j < myOrder+1; j++) {
				X.set(i, j, xpowj);
				xpowj = xpowj*x[i];
			}
		}

		//Note: this is the form given on Mathworld but don't see the advantage
		//Matrix XTX = X.transpose().times(X);
		//Matrix A = XTX.inverse().times(X.transpose()).times(Y);

		Matrix A = X.inverse().times(Y);

		float[] coefficients = new float[myOrder+1];
		for (int i = 0; i < coefficients.length; i++) {
			coefficients[i] = (float) A.get(i,0);
		}

		return new Polynomial(coefficients);
	}

	/**
	 * @return Order of polynomials used to approximate points (eg 1 corresponds to linear
	 * 		approximation, 2 to quadratic, etc)
	 */
	public int getOrder() {
		return myOrder;
	}

	@Override
	public CurveFitter clone() throws CloneNotSupportedException {
		return (CurveFitter) super.clone();
	}
}
