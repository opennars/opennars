/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "MU.java". Description: 
""Matrix Utilities""

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
package ca.nengo.util;

import ca.nengo.math.PDF;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.text.NumberFormat;

/**
 * "Matrix Utilities". Utility methods related to matrices and vectors of floats.
 * 
 * TODO: test
 * 
 * @author Bryan Tripp
 */
public class MU {
	
	private static final Logger ourLogger = LogManager.getLogger(MU.class);

	/**
	 * @param matrix An array of arrays that is expected to be in matrix form
	 * @return True if all "rows" (ie array elements) have the same length
	 */
	public static boolean isMatrix(float[][] matrix) {
		boolean is = true;
		
		int dim = matrix[0].length;
		for (int i = 1; i < matrix.length && is; i++) {
			if (matrix[i].length != dim) {
				is = false;
			}
		}
		
		return is;
	}
	
	/**
	 * @param matrix Any matrix
	 * @return An identical but independent copy of the given matrix
	 */
	public static float[][] clone(float[][] matrix) {
		float[][] result = new float[matrix.length][];
		
		for (int i = 0; i < matrix.length; i++) {
			result[i] = new float[matrix[i].length];
			System.arraycopy(matrix[i], 0, result[i], 0, matrix[i].length);
		}
		
		return result;
	}
	
	public static float[][][] clone(float[][][] matrix)
	{
		float[][][] result = new float[matrix.length][][];
		for(int i=0; i < matrix.length; i++)
		{
			result[i] = new float[matrix[i].length][];
			for(int j=0; j < matrix[i].length; j++)
			{
				result[i][j] = new float[matrix[i][j].length];
				System.arraycopy(matrix[i][j], 0, result[i][j], 0, matrix[i][j].length);
			}
		}
		
		return result;
	}
	
	/**
	 * @param matrix Any matrix
	 * @return An identical but independent copy of the given matrix
	 */
	public static double[][] clone(double[][] matrix) {
		double[][] result = new double[matrix.length][];
		
		for (int i = 0; i < matrix.length; i++) {
			result[i] = new double[matrix[i].length];
			System.arraycopy(matrix[i], 0, result[i], 0, matrix[i].length);
		}
		
		return result;
	}
	
	/**
	 * Unlike System.arraycopy, this function copies the source matrix into the destination
	 * while preserving the original row length. It copies the full source.
	 * 
	 * @param src - source matrix
	 * @param dest - destination matrix
	 * @param destRowPos - starting target row
	 * @param destColPos - starting target column position
	 * @param length - number of rows to copy
	 * 
	 */
	
	public static void copyInto(float[][] src, float[][] dest, int destRowPos, int destColPos, int length) {
		assert destColPos+dest[0].length>src[0].length;
		
		for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, dest[i + destRowPos], 0 + destColPos, src[0].length);
		}
		return;
	}
	
	
	/**
	 * @param vector Vector to copy from
	 * @param start Index in vector from which to start copying
	 * @param interval Interval separating copied entries in source vector (ie skip over interval-1 entries)
	 * @param end Index in vector at which copying ends
	 * @return Values copied from source vector
	 */
	public static float[] copy(float[] vector, int start, int interval, int end) {
		float[] result = null;
		
		if (interval == 1) {
			result = new float[end-start+1];
			System.arraycopy(vector, start, result, 0, result.length);
		} else {
			result = new float[Math.round((float) (end-start+1) / (float) interval)];
			int i = 0;
			for (int j = start; j < end; j=j+interval) {
				result[i++] = vector[j];
			}
			if (i < result.length-1) {
				float[] trim = new float[i+1];
				System.arraycopy(result, 0, trim, 0, trim.length);
				result = trim;
			}
		}
		
		return result;
	}

	/**
	 * @param matrix Matrix to copy from
	 * @param startRow Row in matrix from which to start copying
	 * @param startCol Col in matrix from which to start copying 
	 * @param lengthRow Number of rows to copy (set to a negative number to copy all the way to the end)
	 * @param lengthCol Number of cols to copy (set to a negative number to copy all the way to the end)
	 * @return Values copied from source vector
	 */
	public static float[][] copy(float[][] matrix, int startRow, int startCol, int lengthRow, int lengthCol) {
		int srcRows = matrix.length;
		int srcCols = matrix[0].length;
		
		if (lengthRow < 0)
			lengthRow = srcRows - startRow;
		
		if (lengthCol < 0)
			lengthCol = srcCols - startCol;
		
		assert startRow + lengthRow <= srcRows && startCol + lengthCol <= srcCols; 
		
		float[][] result = new float[lengthRow][lengthCol];
		
		for (int i = 0; i < lengthRow; i++) {
            System.arraycopy(matrix[i + startRow], 0 + startCol, result[i], 0, lengthCol);
		}
		
		return result;
	}
	
	
	/**
	 * @param X Any vector
	 * @param a Any scalar
	 * @return aX (each element of the vector multiplied by the scalar)
	 */
	public static float[] prod(float[] X, float a) {
		float[] result = new float[X.length];
		for (int i = 0; i < X.length; i++) {
			result[i] = X[i] * a;
		}
		return result;
	}
	
	/**
	 * @param X Any vector
	 * @param Y Any vector of the same length as X
	 * @return X'Y 
	 */
	public static float prod(float[] X, float[] Y) {
		if (X.length != Y.length) {
			throw new IllegalArgumentException("Vectors must have same length");
		}		
		
		float result = 0f;
		for (int i = 0; i < X.length; i++) {
			result += X[i] * Y[i];
		}
		
		return result;
	}
	
	/**
	 * @param A Any matrix 
	 * @param X Any vector with the same number of elements as there are columns in A
	 * @return AX
	 */
	public static float[] prod(float[][] A, float[] X) {
		assert isMatrix(A);
		
		if (A[0].length != X.length) {
			throw new IllegalArgumentException("Dimension mismatch: " + A[0].length + 
					" columns in matrix and " + X.length + " elements in vector");
		}
		
		float[] result = new float[A.length];
		
		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < X.length; j++) {
				result[i] += A[i][j] * X[j];
			}
		}
		
		return result;
	}
	
	/**
	 * @param A Any m x n matrix 
	 * @param B Any n x p matrix 
	 * @return Product of matrices
	 */
	public static float[][] prod(float[][] A, float[][] B) {
		assert isMatrix(A);
		assert isMatrix(B);
		
		if (A[0].length != B.length) {
			throw new IllegalArgumentException("Dimension mismatch: " + A[0].length + 
					" columns in matrix A and " + B.length + " rows in matrix B");
		}
		
		float[][] result = new float[A.length][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[B[0].length];
			for (int j = 0; j < result[i].length; j++) {
				for (int k = 0; k < B.length; k++) {
					result[i][j] += A[i][k] * B[k][j];
				}
			}
		}
		
		return result;
	}
	
	/**
	 * @param A Any vector
	 * @param B Any vector the same length as A
	 * @return A(start:end) The identified subvector from A
	 */
	public static float[] prodElementwise(float[] A, float[] B) {
		assert A.length==B.length;
		
		float[] result = new float[A.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = A[i]*B[i];
		}
		
		return result;
	}
	
	/**
	 * @param A Any matrix
	 * @param B Any matrix the same dimensions as A
	 * @return A .* B
	 */
	public static float[][] prodElementwise(float[][] A, float[][] B) {
		assert A.length==B.length;
		assert A[0].length==B[0].length;
		
		float[][] result = new float[A.length][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[B[0].length];
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = A[i][j]*B[i][j];
			}
		}
		
		return result;
	}
	
	/**
	 * @param A Any matrix
	 * @param a Any scalar
	 * @return aA (each element of matrix multiplied by scalar)
	 */
	public static float[][] prod(float[][] A, float a) {
		assert isMatrix(A);
		
		float[][] result = new float[A.length][];
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[A[i].length];
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = A[i][j] * a;
			}
		}
		
		return result;
	}
	
	/**
	 * @param A Any vector
	 * @param B Any vector
	 * @return A*B (matrix with the outer product of A and B)
	 */
	public static float[][] outerprod(float[] A, float[] B) {
		int Alen = A.length;
		int Blen = B.length;
		float Aval;
		float[][] result = new float[Alen][Blen];
		
		for (int i = 0; i < Alen; i++) {
			Aval = A[i];
			for (int j = 0; j < Blen; j++) {
				result[i][j] = Aval * B[j];
			}
		}
		
		return result;
	}
	
	/**
	 * In-place outer product.
	 * 
	 * @param A Any vector
	 * @param B Any vector
	 * @param result the destination matrix
	 * @param offset row in destination matrix to insert result
	 * @return A*B (matrix with the outer product of A and B)
	 */
	public static float[][] outerprod(float[] A, float[] B, float[][] result, int offset) {
		int Alen = A.length;
		int Blen = B.length;
		float Aval;
		
		for (int i = 0; i < Alen; i++) {
			Aval = A[i];
			for (int j = 0; j < Blen; j++) {
				result[i+offset][j] = Aval * B[j];
			}
		}
		
		return result;
	}
	
	
	/**
	 * @param A Any m x n matrix 
	 * @param B Any m x n matrix
	 * @return The element-wise sum of the given matrices
	 */
	public static float[][] sum(float[][] A, float[][] B) {
		assert isMatrix(A);
		assert isMatrix(B);

		if (A[0].length != B[0].length) {
			throw new IllegalArgumentException("Dimension mismatch: " + A[0].length + 
					" columns in matrix A and " + B[0].length + " columns in matrix B");
		}
		
		if (A.length != B.length) {
			throw new IllegalArgumentException("Dimension mismatch: " + A.length + 
					" rows in matrix A and " + B.length + " rows in matrix B");
		}
		
		float[][] result = new float[A.length][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[A[0].length];
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = A[i][j] + B[i][j];
			}
		}
		
		return result;
	}
	
	/**
	 * @param A Any m x n matrix 
	 * @param B Any m x n matrix
	 * @return The element-wise difference of the given matrices (A-B)
	 */
	public static float[][] difference(float[][] A, float[][] B) {
		assert isMatrix(A);
		assert isMatrix(B);

		if (A[0].length != B[0].length) {
			throw new IllegalArgumentException("Dimension mismatch: " + A[0].length + 
					" columns in matrix A and " + B[0].length + " columns in matrix B");
		}
		
		if (A.length != B.length) {
			throw new IllegalArgumentException("Dimension mismatch: " + A.length + 
					" rows in matrix A and " + B.length + " rows in matrix B");
		}
		
		float[][] result = new float[A.length][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[A[0].length];
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = A[i][j] - B[i][j];
			}
		}
		
		return result;
	}
	
	/**
	 * @param X Any vector 
	 * @param Y Any vector same length as vector X
	 * @return X+Y (element-wise sum) 
	 */
	public static float[] sum(float[] X, float[] Y) {
		if (X.length != Y.length) {
			throw new IllegalArgumentException("Vectors must have same length");
		}
		
		float[] result = new float[X.length];
		for (int i = 0; i < X.length; i++) {
			result[i] = X[i] + Y[i];
		}
		
		return result;
	}

	/**
	 * @param X Any vector 
	 * @param Y Any vector same length as vector X
	 * @return X-Y (element-wise difference) 
	 */
	public static float[] difference(float[] X, float[] Y) {
		if (X.length != Y.length) {
			throw new IllegalArgumentException("Vectors must have same length");
		}
		
		float[] result = new float[X.length];
		for (int i = 0; i < X.length; i++) {
			result[i] = X[i] - Y[i];
		}
		
		return result;
	}
	
	/**
	 * @param X Any vector 
	 * @return X(2:end) - X(1:end-1)
	 */
	public static float[] difference(float[] X) {
		float[] result = new float[X.length - 1];
		
		for (int i = 2; i < X.length; i++) {
			result[i] = X[i] - X[i-1];
		}
		
		return result;
	} 

	/**
	 * @param matrix An array of float arrays (normally a matrix but can have rows of different length)
	 * @param rows Desired number of rows 
	 * @param cols Desired number of columns
	 * @return Matrix with requested numbers of rows and columns drawn from the given matrix, and padded 
	 * 		with zeros if there are not enough values in the original matrix
	 */
	public static float[][] shape(float[][] matrix, int rows, int cols) {
		float[][] result = new float[rows][];
		
		int fromRow = 0;
		int fromCol = -1;
		
		ourLogger.debug(matrix.length + " rows");
		ourLogger.debug(matrix[0].length + " cols");
		
		for (int i = 0; i < rows; i++) {
			result[i] = new float[cols];
			
			
			copyRow : for (int j = 0; j < cols; j++) {
				
				boolean atNextValue = false;
				while (!atNextValue) { //accounts for null or 0-length rows in original
					fromCol++;
					
					if (fromRow == matrix.length) {
						break copyRow;
					} else if (matrix[fromRow] == null || fromCol == matrix[fromRow].length) {
						fromRow = fromRow + 1;
						fromCol = -1;
					} else {
						atNextValue = true;
					} 
				}
				
				result[i][j] = matrix[fromRow][fromCol];
			}
		}
	
		return result;
	}
	
	/**
	 * @param vector Any vector
	 * @return The transpose of the vector (i.e. a column vector instead of a row vector)
	 */
	public static float[][] transpose(float[] vector) {		
		float[][] result = new float[1][vector.length];

        System.arraycopy(vector, 0, result[0], 0, vector.length);
		return result;
	}
	
	/**
	 * @param matrix Any matrix
	 * @return The transpose of the matrix
	 */
	public static float[][] transpose(float[][] matrix) {		
		float[][] result = new float[0][];
		
		if (matrix.length > 0) {
			result = new float[matrix[0].length][];
		}
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[matrix.length];
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] = matrix[j][i];
			}
		}
		return result;
	}
	
	/**
	 * @param entries A list of diagonal entries 
	 * @return A square diagonal matrix with given entries on the diagonal
	 */
	public static float[][] diag(float[] entries) {
		float[][] result = new float[entries.length][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[entries.length];
			result[i][i] = entries[i];
		}
		
		return result;
	}
	
	/**
	 * @param matrix Any matrix
	 * @return Diagonal entries 
	 */
	public static float[] diag(float[][] matrix) {
		float[] result = new float[Math.min(matrix.length, matrix[0].length)];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = matrix[i][i];
		}
		
		return result;
	}

	/**
	 * @param dimension # of rows/columns
	 * @return Identity matrix of specified dimension
	 */
	public static float[][] I(int dimension) {
		float[][] result = new float[dimension][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[dimension];
			result[i][i] = 1f;
		}
		
		return result;
	}

	/**
	 * @param rows Number of rows in the requested matrix 
	 * @param cols Number of columns in the requested matrix
	 * @return Matrix of zeroes with the given dimensions
	 */
	public static float[][] zero(int rows, int cols) {
		float[][] result = new float[rows][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[cols];
		}
		
		return result;
	}
	
	/**
	 * @param rows Number of rows in the requested matrix 
	 * @param cols Number of columns in the requested matrix
	 * @param value Value of each element
	 * @return Matrix with the given dimensions where each entry is the given value
	 */
	public static float[][] uniform(int rows, int cols, float value) {
		float[][] result = new float[rows][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[cols];
			for (int j = 0; j < cols; j++) {
				result[i][j] = value;
			}
		}
		
		return result;
	}
	
	/**
	 * @param rows Number of rows in the requested matrix 
	 * @param cols Number of columns in the requested matrix
	 * @param pdf One-dimensional PDF from which each element is drawn
	 * @return Matrix with the given dimensions where each entry is randomly drawn 
	 * 		from the given PDF
	 */
	public static float[][] random(int rows, int cols, PDF pdf) {
		float[][] result = new float[rows][];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[cols];
			for (int j = 0; j < cols; j++) {
				result[i][j] = pdf.sample()[0];
			}
		}
		
		return result;		
	}

	/**
	 * @param matrix Any float matrix
	 * @return Duplicate with each element cast to double
	 */
	public static double[][] convert(float[][] matrix) {
		double[][] result = new double[matrix.length][];
		
		for (int i = 0; i < matrix.length; i++) {
			result[i] = new double[matrix[i].length];
			for (int j = 0; j < matrix[i].length; j++) {
				result[i][j] = (double) matrix[i][j];
			}
		}
		
		return result;
	}
	
	/**
	 * @param vector Any float vector
	 * @return Duplicate with each element cast to double
	 */
	public static double[] convert(float[] vector) {
		double[] result = new double[vector.length];
		
		for (int i = 0; i < vector.length; i++) {
			result[i] = (double) vector[i];
		}
		
		return result;
	}
	
	/**
	 * @param matrix Any double matrix
	 * @return Duplicate with each element cast to float 
	 */
	public static float[][] convert(double[][] matrix) {
		float[][] result = new float[matrix.length][];
		
		for (int i = 0; i < matrix.length; i++) {
			result[i] = new float[matrix[i].length];
			for (int j = 0; j < matrix[i].length; j++) {
				result[i][j] = (float) matrix[i][j];
			}
		}
		
		return result;		
	}
	
	/**
	 * @param vector Any double vector
	 * @return Duplicate with each element cast to float
	 */
	public static float[] convert(double[] vector) {
		float[] result = new float[vector.length];
		
		for (int i = 0; i < vector.length; i++) {
			result[i] = (float) vector[i];
		}
		
		return result;
	}
	
	/**
	 * @param vector Any vector
	 * @return Minimum of elements
	 */
	public static float min(float[] vector) {
		float result = vector[0];
		
		for (int i = 1; i < vector.length; i++) {
			if (vector[i] < result) result = vector[i];
		}
		
		return result;
	}

	/**
	 * @param vector Any vector
	 * @return Minimum of elements
	 */
	public static float max(float[] vector) {
		float result = vector[0];
		
		for (int i = 1; i < vector.length; i++) {
			if (vector[i] > result) result = vector[i];
		}
		
		return result;
	}
	
	/**
	 * @param matrix Any matrix
	 * @return Minimum of elements
	 */
	public static float min(float[][] matrix) {
		float result = matrix[0][0];
		
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] < result) result = matrix[i][j];				
			}
		}
		
		return result;
	}

	/**
	 * @param matrix Any matrix
	 * @return Minimum of elements
	 */
	public static float max(float[][] matrix) {
		float result = matrix[0][0];
		
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] > result) result = matrix[i][j];				
			}
		}
		
		return result;
	}

	/**
	 * @param vector Any vector
	 * @return Sum of elements
	 */
	public static float sum(float[] vector) {
		float result = 0f;
		
		for (int i = 0; i < vector.length; i++) {
			result += vector[i];
		}
		
		return result;
	}

	/**
	 * @param vector Any vector
	 * @param index Index of last element to include in sum
	 * @return Sum of elements
	 */
	public static float sumToIndex(float[] vector, int index) {
		float result = 0f;
		
		for (int i = 0; i < index+1; i++) {
			result += vector[i];
		}
		
		return result;
	}
	
	/**
	 * @param vector Any vector
	 * @return Mean of vector elements
	 */
	public static float mean(float[] vector) {
		float sum = 0f;
		
		for (int i = 0; i < vector.length; i++) {
			sum += vector[i];
		}
		
		return sum / (float) vector.length;
	}
	
	/**
	 * @param matrix Any matrix
	 * @return Mean of matrix elements
	 */
	public static float mean(float[][] matrix) {
		float sum = 0f;
		float count = 0f;
		
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				sum += matrix[i][j];
				count++;
			}
		}
		
		return sum/count;
	}
	
	/**
	 * @param vector Any vector
	 * @param mean Value around which to take variance, eg MU.mean(vector) or some pre-defined value
	 * @return Bias-corrected variance of vector elements around the given values
	 */
	public static float variance(float[] vector, float mean) {
		float sum = 0f;		
		for (int i = 0; i < vector.length; i++) {
			float deviation = vector[i] - mean;
			sum += deviation*deviation;
		}
				
		return sum / ((float) vector.length - 1);
	}

	/**
	 * @param vector Any vector
	 * @return The vector normalized to 2-norm of 1
	 */
	public static float[] normalize(float[] vector) {
		float[] result = new float[vector.length];
		
		float sum = 0f;
		for (int i = 0; i < vector.length; i++) {
			sum += vector[i]*vector[i];
		}
		float norm = (float) Math.sqrt(sum);
		
		for (int i = 0; i < vector.length; i++) {
			result[i] = vector[i] / norm;
		}
		
		return result;
	}
	
	/**
	 * @param vector Any vector
	 * @param p Degree of p-norm (use -1 for infinity)
	 * @return The p-norm of the vector
	 */
	public static float pnorm(float[] vector, int p) {
		assert p != 0; //undefined
		
		float result = 0;
		
		if (p < 0) { //interpret as infinity-norm
			float max = 0;
			for (int i = 0; i < vector.length; i++) {
				if (Math.abs(vector[i]) > max) max = Math.abs(vector[i]); 
				if ( !(Math.abs(vector[i]) >= 0) ) max = Float.NaN;
			}
			result = max;
		} else {
			double sum = 0;
			for (int i = 0; i < vector.length; i++) {
				sum += Math.pow(Math.abs(vector[i]), (double) p);
			}
			result = (float) Math.pow(sum, 1.0 / (double) p);			
		}
		
		return result;
	}
	
	/**
	 * TODO: handle exponential notation
	 * 
	 * @param matrix Any matrix
	 * @param decimalPlaces number of decimal places to display for float values
	 * @return String representation of matrix with one row per line
	 */
	public static String toString(float[][] matrix, int decimalPlaces) {
		StringBuffer buf = new StringBuffer();
		
		NumberFormat nf = NumberFormat.getInstance();
		if (nf.getMinimumFractionDigits() > decimalPlaces) nf.setMinimumFractionDigits(decimalPlaces);
		nf.setMinimumFractionDigits(decimalPlaces);

		String[][] strings = new String[matrix.length][];
		int maxLength = 0;
		for (int i = 0; i < matrix.length; i++) {
			strings[i] = new String[matrix[i].length];
			for (int j = 0; j < matrix[i].length; j++) {				
				strings[i][j] = nf.format(matrix[i][j]);
				if (strings[i][j].length() > maxLength) maxLength = strings[i][j].length();  
			}
		}
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				appendSpaces(buf, 1 + maxLength - strings[i][j].length());
				buf.append(strings[i][j]);
			}
			buf.append(System.getProperty("line.separator"));
		}
		
		
		return buf.toString();
	}
	
	private static void appendSpaces(StringBuffer buf, int n) {
		for (int i = 0; i < n; i++) {
			buf.append(' ');
		}
	}
	
	/**
	 * @param start Value of first element in vector
	 * @param increment Increment between adjacent elements
	 * @param end Value of last element in vector
	 * @return A vector with elements evenly incremented from <code>start</code> to <code>end</code> 
	 */
	public static float[] makeVector(float start, float increment, float end) {
		int len = 1 + Math.round((end - start) / increment);
		
		float[] result = new float[len];
		for (int i = 0; i < len-1; i++) {
			result[i] = start + (float) i * increment; 
		}
		result[len-1] = end;
		return result;
	}
	
	/**
	 * @param vector A vector
	 * @return Elements rounded to nearest integer
	 */
	public static int[] round(float[] vector) {
		int[] result = new int[vector.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = Math.round(vector[i]);
		}
		
		return result;
	}
	
	/**
	 * A tool for growing vectors (similar to java.util.List). 
	 *    
	 * @author Bryan Tripp
	 */
	public static class VectorExpander {
		
		private static final int ourIncrement = 1000;
		
		private int myIndex;
		private float[] myValues;
		
		public VectorExpander() {
			myIndex = 0;
			myValues = new float[ourIncrement];
		}
		
		/**
		 * @param value New element to append
		 */
		public void add(float value) {
			if (myIndex == myValues.length) {
				float[] newValues = new float[myValues.length + ourIncrement];
				System.arraycopy(myValues, 0, newValues, 0, myValues.length);
				myValues = newValues;
			}
			
			myValues[myIndex++] = value;
		}
		
		/**
		 * @return Array of elements in order appended
		 */
		public float[] toArray() {
			float[] result = new float[myIndex];
			System.arraycopy(myValues, 0, result, 0, myIndex);
			return result;
		}
	}	
	
	/**
	 * A tool for growing matrices (similar to java.util.List). 
	 *    
	 * @author Bryan Tripp
	 */
	public static class MatrixExpander {
		
		private static final int ourIncrement = 1000;
		
		private int myIndex;
		private float[][] myValues;
		
		public MatrixExpander() {
			myIndex = 0;
			myValues = new float[ourIncrement][];
		}
		
		/**
		 * @param value New row to append
		 */
		public void add(float[] value) {
			if (myIndex == myValues.length) {
				float[][] newValues = new float[myValues.length + ourIncrement][];
				System.arraycopy(myValues, 0, newValues, 0, myValues.length);
				myValues = newValues;
			}
			
			myValues[myIndex++] = value;
		}

		/**
		 * @return Array of rows in order appended
		 */
		public float[][] toArray() {
			float[][] result = new float[myIndex][];
			System.arraycopy(myValues, 0, result, 0, myIndex);
			return result;
		}
	}
}
