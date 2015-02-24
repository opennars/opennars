/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "FunctionBasis.java". Description: 
"A list of orthogonal functions.
  
  Function bases are useful in function representation, because they 
  make function representation equivalent to vector representation (see 
  Eliasmith & Anderson, 2003)"

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
 * Created on May 16, 2006
 */
package ca.nengo.math;

/**
 * <p>A list of orthogonal functions.</p>
 * 
 * <p>Function bases are useful in function representation, because they 
 * make function representation equivalent to vector representation (see 
 * Eliasmith & Anderson, 2003). Essentially, functions in an orthogonal 
 * basis correspond to dimensions in a vector. Cosine tuning curves in a 
 * vector space are equivalent to inner-product tuning curves in the 
 * corresponding function space.</p>
 * 
 * <p>Examples of orthogonal sets of functions include Fourier and wavelet 
 * bases.</p> 
 * 
 * @author Bryan Tripp
 */
public interface FunctionBasis extends Function {

	/**
	 * @return Dimensionality of basis
	 */
	public int getBasisDimension();
	
	/**
	 * @param basisIndex Dimension index
	 * @return Basis function corresponding to given dimension 
	 */
	public Function getFunction(int basisIndex);
	
	/**
	 * @param coefficients Coefficient for summing basis functions 
	 */
	public void setCoefficients(float[] coefficients);
	
}
