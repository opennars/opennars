/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "WeightedCostApproximator.java". Description:
"A LinearApproximator in which error is evaluated at a fixed set of points, and
  the cost function that is minimized is a weighted integral of squared error"

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
 * Created on 5-Jun-2006
 */
package ca.nengo.math.impl;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import ca.nengo.math.ApproximatorFactory;
import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;
import ca.nengo.util.MU;
import ca.nengo.util.Memory;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;

/**
 * <p>A LinearApproximator in which error is evaluated at a fixed set of points, and
 * the cost function that is minimized is a weighted integral of squared error.</p>
 *
 * <p>Uses the Moore-Penrose pseudoinverse.</p>
 *
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class WeightedCostApproximator implements LinearApproximator {

	private static final Logger ourLogger = LogManager.getLogger(WeightedCostApproximator.class);
	private static final long serialVersionUID = 1L;

	private float[][] myEvalPoints;
	private final float[][] myValues;
	private float[][] myNoisyValues;
	private Function myCostFunction;
	private int mySignalLength;
	private final boolean myQuiet;

	private double[][] myGammaInverse;

	private static boolean myUseGPU = false;
	private static boolean canUseGPU;
	private static String myGPUErrorMessage;

	static {
		try{
			System.loadLibrary("NengoUtilsGPU");
			canUseGPU = true;

			if(!hasGPU())
			{
				myGPUErrorMessage = "No CUDA-enabled GPU detected.";
				System.out.println(myGPUErrorMessage);
				canUseGPU = false;
			}

		}catch(java.lang.UnsatisfiedLinkError e){
			canUseGPU = false;
			myGPUErrorMessage = "Couldn't load native library NengoUtilsGPU. - Linker error:";
			// This error message confuses many users
			// System.out.println(myGPUErrorMessage);
			// System.out.println(e);
		}
		catch(Exception e){
			canUseGPU = false;
			myGPUErrorMessage = "Couldn't load native library NengoUtilsGPU - General exception:";
			System.out.println(myGPUErrorMessage);
			System.out.println(e.getMessage());
			System.out.println(Arrays.toString(e.getStackTrace()));
		}
	}

	/**
	 * @param use Use the GPU?
	 */
	public static void setUseGPU(boolean use) {
		myUseGPU = use;
	}

	/**
	 * @return Using the GPU?
	 */
	public static boolean getUseGPU() {
		return canUseGPU && myUseGPU;
	}
	
	public static boolean canUseGPU() {
		return canUseGPU;
	}
	
	public static String getGPUErrorMessage() {
		return myGPUErrorMessage;
	}

	private static native boolean hasGPU();
	
	@SuppressWarnings("unused")
	private static native float[][] nativePseudoInverse(float[][] java_matrix, float minSV, int numSV);
	@SuppressWarnings("unused")
	private static native float[][] nativeFindGamma(float[][] noisyValues);

	// the above two functions combined into one to minimize GPU-CPU communication
	private static native float[][] nativeFindGammaPseudoInverse(float[][] noisyValues, float minSV, int numSV);

	/**
	 * @param evaluationPoints Points at which error is evaluated (should be uniformly
	 * 		distributed, as the sum of error at these points is treated as an integral
	 * 		over the domain of interest). Examples include vector inputs to an ensemble,
	 * 		or different points in time	within different simulation regimes.
	 * @param values The values of whatever functions are being combined, at the
	 * 		evaluationPoints. Commonly neuron firing rates. The first dimension makes up
	 * 		the list of functions, and the second the values of these functions at each
	 * 		evaluation point.
	 * @param costFunction A cost function that weights squared error over the domain of
	 * 		evaluation points
	 * @param noise Standard deviation of Gaussian noise to add to values (to reduce
	 * 		sensitivity to simulation noise) as a proportion of the maximum absolute
	 * 		value over all values
	 * @param nSV Number of singular values to keep from the singular value
	 *      decomposition (SVD)
	 * @param quiet Turn off logging?
	 */
	public WeightedCostApproximator(float[][] evaluationPoints, float[][] values, Function costFunction, float noise, int nSV, boolean quiet) {
		assert MU.isMatrix(evaluationPoints);
		assert MU.isMatrix(values);
		assert evaluationPoints.length == values[0].length;

		myEvalPoints = evaluationPoints;
		myValues = MU.clone(values);
		myNoisyValues = MU.clone(values);
		myQuiet = quiet;
		mySignalLength = -1;
		float absNoiseSD = addNoise(myNoisyValues, noise);

		myCostFunction = costFunction;

		calcGamma(absNoiseSD, nSV);
	}
	
	/**
	 * @param evaluationSignals Signals over which error is evaluated. First dimension is for each
	 * 		evaluation signal.  Second dimension is for the dimensions of each signal.  Third dimension is
	 * 		the value of the signal dimension over time.
	 * @param values The values of whatever functions are being combined, over the
	 * 		evaluation signals. Commonly neuron firing rates. The first dimension makes up
	 * 		the list of functions, the second the values of these functions for each evaluation
	 * 		signal, and the third the value of the function over time.
	 * @param costFunction A cost function that weights squared error over the domain of
	 * 		evaluation points
	 * @param noise Standard deviation of Gaussian noise to add to values (to reduce
	 * 		sensitivity to simulation noise) as a proportion of the maximum absolute
	 * 		value over all values
	 * @param nSV Number of singular values to keep from the singular value
	 *      decomposition (SVD)
	 * @param quiet Turn off logging?
	 */
	public WeightedCostApproximator(float[][][] evaluationSignals, float[][][] values, Function costFunction, float noise, int nSV, boolean quiet) {
		//should do some error checking (e.g. make sure all signals are same length)
		
		
		//take all the different signals and arrange them sequentially
		mySignalLength = evaluationSignals[0][0].length;
		myEvalPoints = new float[evaluationSignals.length*mySignalLength][];
		for(int i=0; i < evaluationSignals.length; i++)
		{
			for(int j=0; j < mySignalLength; j++)
			{
				myEvalPoints[i*mySignalLength+j] = new float[evaluationSignals[i].length];
				for(int k=0; k < evaluationSignals[i].length; k++)
				{
					myEvalPoints[i*mySignalLength+j][k] = evaluationSignals[i][k][j];
				}
			}
		}
		
		myValues = new float[values.length][];
		for(int n=0; n < values.length; n++)
		{
			myValues[n] = new float[values[n].length*mySignalLength];
			for(int s=0; s < values[n].length; s++)
			{
                System.arraycopy(values[n][s], 0, myValues[n], s * mySignalLength + 0, values[n][s].length);
			}
		}

		myNoisyValues = MU.clone(myValues);
		myQuiet = quiet;
		float absNoiseSD = addNoise(myNoisyValues, noise);

		myCostFunction = costFunction;

		calcGamma(absNoiseSD, nSV);

	}

	/**
     * @param evaluationPoints Points at which error is evaluated (should be uniformly
     *      distributed, as the sum of error at these points is treated as an integral
     *      over the domain of interest). Examples include vector inputs to an ensemble,
     *      or different points in time within different simulation regimes.
     * @param values The values of whatever functions are being combined, at the
     *      evaluationPoints. Commonly neuron firing rates. The first dimension makes up
     *      the list of functions, and the second the values of these functions at each
     *      evaluation point.
     * @param costFunction A cost function that weights squared error over the domain of
     *      evaluation points
     * @param noise Standard deviation of Gaussian noise to add to values (to reduce
     *      sensitivity to simulation noise) as a proportion of the maximum absolute
     *      value over all values
     * @param nSV Number of singular values to keep from the singular value
     *      decomposition (SVD)
	 */
	public WeightedCostApproximator(float[][] evaluationPoints, float[][] values, Function costFunction, float noise, int nSV) {
		this(evaluationPoints, values, costFunction, noise, nSV, false);
	}
	
	/**
	 * Calculate the gamma matrix.
	 * 
	 * @param absNoiseSD standard deviation of noise that was added to myNoisyValues
	 * @param nSV Number of singular values to keep from the singular value
	 *      decomposition (SVD)
	 */
	private void calcGamma(float absNoiseSD, int nSV) {
		if(!myQuiet) {
            Memory.report("before gamma");
        }

		if(getUseGPU())
		{
			float[][] float_result = new float[myNoisyValues.length][myNoisyValues.length];
			myGammaInverse = new double[myNoisyValues.length][myNoisyValues.length];
			float_result = nativeFindGammaPseudoInverse(myNoisyValues, absNoiseSD*absNoiseSD, nSV);

			for (int i = 0; i < myNoisyValues.length; i++) {
				for (int j = 0; j < myNoisyValues.length; j++) {
					myGammaInverse[i][j] = float_result[i][j];
				}
			}
		}else{
			double[][] gamma = findGamma();
			if(!myQuiet) {
                Memory.report("before inverse");
            }
			
			myGammaInverse = pseudoInverse(gamma, absNoiseSD*absNoiseSD, nSV);
			if(!myQuiet) {
                Memory.report("after inverse");
            }

		}
	}

	private float addNoise(float[][] values, float noise) {
		float maxValue = 0f;
		for (float[] value : values) {
			for (float element : value) {
				if (Math.abs(element) > maxValue) {
                    maxValue = Math.abs(element);
                }
			}
		}

		float SD = noise * maxValue;
		GaussianPDF pdf = new GaussianPDF(0f, SD*SD);

		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				values[i][j] += pdf.sample()[0];
			}
		}

		return SD;
	}
	
	@SuppressWarnings("unused")
	private float addNoise(float[][][] values, float noise) {
		float maxValue = 0f;
		for (float[][] row : values) {
			for (float[] col : row) {
				for (float element : col) {
					if (Math.abs(element) > maxValue) {
	                    maxValue = Math.abs(element);
					}
				}
			}
		}

		float SD = noise * maxValue;
		GaussianPDF pdf = new GaussianPDF(0f, SD*SD);

		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				for (int k = 0; k < values[i][j].length; k++) {
					values[i][j][k] += pdf.sample()[0];
				}
			}
		}

		return SD;
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#getEvalPoints()
	 */
    public float[][] getEvalPoints() {
		return myEvalPoints;
	}

	/**
	 * @see ca.nengo.math.LinearApproximator#getValues()
	 */
    public float[][] getValues() {
		return myValues;
	}

//	private static void testPlot(float[][] evaluationPoints, float[][] values) {
//		XYSeriesCollection dataset = new XYSeriesCollection();
//		for (int i = 0; i < values.length; i++) {
//			XYSeries series = new XYSeries("" + i);
//			for (int j = 0; j < evaluationPoints.length; j++) {
//				series.add(evaluationPoints[j][0], values[i][j]);
//			}
//			dataset.addSeries(series);
//		}
//		JFreeChart chart = ChartFactory.createXYLineChart(
//				"Approximator Activities",
//				"X",
//				"Firing Rate (spikes/s)",
//				dataset,
//				PlotOrientation.VERTICAL,
//				false, false, false
//		);
//		JPanel panel = new ChartPanel(chart);
//		JFrame frame = new JFrame("Approximator");
//		frame.getContentPane().add(panel, BorderLayout.CENTER);
//        frame.pack();
//        frame.setVisible(true);
//	}



	/**
	 * Override this method to use a different pseudoinverse implementation (eg clustered).
	 *
	 * @param matrix Any matrix
	 * @param minSV Hint as to smallest singular value to use
	 * @param nSV Max number of singular values to use
	 * @return The pseudoinverse of the given matrix
	 */
	public double[][] pseudoInverse(double[][] matrix, float minSV, int nSV) {
		double[][] result=null;

		Random random=new Random();

		Runtime runtime=Runtime.getRuntime();
//		int hashCode=java.util.Arrays.hashCode(matrix);
//		int testpos = 0;

		String parent=System.getProperty("user.dir");
		java.io.File path=new java.io.File(parent,"external");
		String filename="matrix_"+random.nextLong();

	 	try {
			// TODO: separate this out into a helper method, so we can do this sort of thing for other calculations as well
	 		java.io.File pinvfile = new java.io.File(path,"pseudoInverse");

			if(pinvfile.exists())
			{
				java.io.File file=new java.io.File(path,filename);
				if (file.canRead()) {
                    file.delete();
                }
				java.io.File file2=new java.io.File(path,filename+".inv");
				if (file2.canRead()) {
                    file2.delete();
                }

				java.nio.channels.FileChannel channel=new java.io.RandomAccessFile(file,"rw").getChannel();
	//			java.nio.ByteBuffer buffer=channel.map(java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, matrix.length*matrix.length*4);
				java.nio.ByteBuffer buffer= java.nio.ByteBuffer.allocate(matrix.length*matrix.length*4);
				buffer.rewind();


				buffer.order(java.nio.ByteOrder.BIG_ENDIAN);
				for (double[] element : matrix) {
					for (int j=0; j<matrix.length; j++) {
						buffer.putFloat((float)(element[j]));
					}
				}
				buffer.rewind();

				channel.write(buffer);
				channel.force(true);
	            channel.close();
	            
	            Process process;
				if (System.getProperty("os.name").startsWith("Windows")) {
					process=runtime.exec("cmd /c pseudoInverse.bat "+filename+ ' ' +filename+".inv"+ ' ' +minSV+ ' ' +nSV,null,path);
					process.waitFor();
				} else {
					process=runtime.exec("external"+java.io.File.separatorChar+"pseudoInverse external/"+filename+" external/"+filename+".inv"+ ' ' +minSV+ ' ' +nSV,null,null);
					process.waitFor();	
				}
				
				java.io.InputStream s=process.getErrorStream();
				if (s.available()>0) {
					System.out.println("external error:");
					while (s.available()>0) {
						System.out.write(s.read());
					}
				}
//				java.io.InputStream s2=process.getInputStream();
//				if (s2.available()>0) {
//					System.out.println("external output:");
//					while (s2.available()>0) {
//						System.out.write(s2.read());
//					}
//				}

				// matrix file cleaned up in finally block
				channel=new java.io.RandomAccessFile(file2,"r").getChannel();
	//			buffer=channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, matrix.length*matrix.length*4);
				buffer= java.nio.ByteBuffer.allocate(matrix.length*matrix.length*4);
				channel.read(buffer);
				buffer.rewind();

				double[][] inv=new double[matrix.length][];

				for (int i=0; i<matrix.length; i++) {
					double[] row=new double[matrix.length];
					for (int j=0; j<matrix.length; j++) {
						row[j]=buffer.getFloat();
					}
					inv[i]=row;
				}
				result=inv;

				// Close all file handles
	            channel.close();

	            // channel file cleaned up in finally block
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e);
		} catch (java.io.IOException e) {
			//e.printStackTrace();
            System.err.println("WeightedCostApproximator.pseudoInverse() - IO Exception: " + e);
		} catch (InterruptedException e) {
            System.err.println("WeightedCostApproximator.pseudoInverse() - Interrupted: " + e);
			//e.printStackTrace();
		} catch (Exception e){
            System.err.println("WeightedCostApproximator.pseudoInverse() - Gen Exception: " + e);
            //e.printStackTrace();
        } finally {
        	java.io.File file=new java.io.File(path,filename);
			if (file.exists()) {
                file.delete();
            }
			java.io.File file2=new java.io.File(path,filename+".inv");
			if (file2.exists()) {
                file2.delete();
            }
        }

		if (result==null) {

			Matrix m = new Matrix(matrix);
			SingularValueDecomposition svd = m.svd();
			Matrix sInv = svd.getS().inverse();

			int i = 0;
			while (i < svd.getS().getRowDimension() && svd.getS().get(i, i) > minSV && (nSV <= 0 || i < nSV)) {
                i++;
            }

			if(!myQuiet) {
                ourLogger.info("Using " + i + " singular values for pseudo-inverse");
            }

			for (int j = i; j < matrix.length; j++) {
				sInv.set(j, j, 0d);
			}

			result = svd.getV().times(sInv).times(svd.getU().transpose()).getArray();

		}

		return result;
	}

	/**
	 * <p>This implementation is adapted from Eliasmith & Anderson, 2003, appendix A.</p>
	 *
	 * <p>It solves PHI = GAMMA" UPSILON, where " denotes pseudoinverse, UPSILON_i = < cost(x) x a_i(x) >,
	 * and GAMMA_ij = < cost(x) a_i(x) a_j(x) >. <> denotes integration (the sum over eval points). </p>
	 *
	 * @see ca.nengo.math.LinearApproximator#findCoefficients(ca.nengo.math.Function)
	 */
    public float[] findCoefficients(Function target) {
    	if(mySignalLength != -1)
    		System.err.println("Warning, finding coefficients using a function on WeightedCostApproximator initialized with signals");
    	
		float[] targetValues = new float[myEvalPoints.length];
		for (int i = 0; i < targetValues.length; i++) {
			targetValues[i] = target.map(myEvalPoints[i]);
		}

		float[] upsilon = new float[myNoisyValues.length];
		for (int i = 0; i < myNoisyValues.length; i++) {
			for (int j = 0; j < myEvalPoints.length; j++) {
				upsilon[i] += myNoisyValues[i][j] * targetValues[j] * myCostFunction.map(myEvalPoints[j]);
			}
			upsilon[i] = upsilon[i] / myEvalPoints.length;
		}

		float[] result = new float[myNoisyValues.length];
		for (int i = 0; i < myNoisyValues.length; i++) {
			for (int j = 0; j < myNoisyValues.length; j++) {
				result[i] += myGammaInverse[i][j] * upsilon[j];
			}
		}

		return result;
	}
    
    /**
     * Similar to findCoefficients(ca.nengo.math.Function), but finds coefficients for a target signal (over time)
     * rather than a target function.
     * 
     * @param targetSignal signal over time that the coefficients should fit to
     * @return coefficients (weights on the output of each neuron)
     */
    public float[] findCoefficients(float[] targetSignal) {
    	if(mySignalLength == -1)
    		System.err.println("Warning, finding coefficients using a signal on WeightedCostApproximator initialized with points");
    	if(targetSignal.length != mySignalLength)
    	{
    		System.err.println("Warning, finding coefficients with a different length target signal than evaluation signals (" + 
    				targetSignal.length + " vs " + mySignalLength + ')');
    		//could do some interpolation/subsampling to match them up, for now we'll just do the rough measure of 
    		//chopping/repeating the end of the target signal
    		float[] newSignal = new float[mySignalLength];
    		for(int i=0; i < mySignalLength; i++)
    			newSignal[i] = targetSignal[Math.min(i,targetSignal.length-1)];
    		targetSignal = newSignal;
    	}
    	
    	//repeat target signal however many times were used to generate the evalPoints (to match the number of evaluation signals)
    	float[] targetValues = new float[myEvalPoints.length];
    	int numRepeat = myEvalPoints.length/mySignalLength;
    	for(int i=0; i < numRepeat; i++)
    	{
            System.arraycopy(targetSignal, 0, targetValues, i * mySignalLength + 0, mySignalLength);
    	}
    	
    	float[] upsilon = new float[myNoisyValues.length];
		for (int i = 0; i < myNoisyValues.length; i++) {
			for (int j = 0; j < myEvalPoints.length; j++) {
				upsilon[i] += myNoisyValues[i][j] * targetValues[j] * myCostFunction.map(myEvalPoints[j]);
			}
			upsilon[i] = upsilon[i] / myEvalPoints.length;
		}

		float[] result = new float[myNoisyValues.length];
		for (int i = 0; i < myNoisyValues.length; i++) {
			for (int j = 0; j < myNoisyValues.length; j++) {
				result[i] += myGammaInverse[i][j] * upsilon[j];
			}
		}

		return result;
    }

	private double[][] findGamma() {

		double[][] result = new double[myNoisyValues.length][];
//		double[][] nativeResult = new double[myNoisyValues.length][];

		for (int i = 0; i < result.length; i++) {
			result[i] = new double[myNoisyValues.length];
			for (int j = 0; j < result[i].length; j++) {
				for (int k = 0; k < myEvalPoints.length; k++) {
					result[i][j] += myNoisyValues[i][k] * myNoisyValues[j][k] * myCostFunction.map(myEvalPoints[k]);
				}
				result[i][j] = result[i][j] / myEvalPoints.length;
			}
		}

		return result;
	}

	@Override
	public LinearApproximator clone() throws CloneNotSupportedException {
		WeightedCostApproximator result = (WeightedCostApproximator) super.clone();

		result.myCostFunction = myCostFunction.clone();
		result.myEvalPoints = MU.clone(myEvalPoints);
		result.myNoisyValues = MU.clone(myNoisyValues);

		result.myGammaInverse = new double[myGammaInverse.length][];
		for (int i = 0; i < myGammaInverse.length; i++) {
			result.myGammaInverse[i] = myGammaInverse[i].clone();
		}

		return result;
	}


	/**
	 * An ApproximatorFactory that produces WeightedCostApproximators.
	 *
	 * @author Bryan Tripp
	 */
	public static class Factory implements ApproximatorFactory {

		private static final long serialVersionUID = -3390244062379730498L;

		private float myNoise;
		private int myNSV;
		private boolean myQuiet;

		/**
		 * @param noise Random noise to add to component functions (proportion of largest value over all functions)
		 */
		public Factory(float noise) {
			this(noise, -1, false);
		}

		/**
		 * @param noise Random noise to add to component functions (proportion of largest value over all functions)
		 * @param quiet Turn off logging?
		 */
		public Factory(float noise, boolean quiet) {
			this(noise, -1, quiet);
		}

		/**
		 * @param noise Random noise to add to component functions (proportion of largest value over all functions)
		 * @param NSV Number of singular values to keep
		 */
		public Factory(float noise, int NSV) {
			this(noise, NSV, false);
		}

		/**
		 * @param noise Random noise to add to component functions (proportion of largest value over all functions)
		 * @param NSV Number of singular values to keep
		 * @param quiet Turn off logging?
		 */
		public Factory(float noise, int NSV, boolean quiet) {
			myNoise = noise;
			myNSV = NSV;
			myQuiet = quiet;
		}


		/**
		 * @return Random noise to add to component functions (proportion of largest value over all functions)
		 */
		public float getNoise() {
			return myNoise;
		}

		/**
		 * @param noise Random noise to add to component functions (proportion of largest value over all functions)
		 */
		public void setNoise(float noise) {
			myNoise = noise;
		}

		/**
		 * @return Maximum number of singular values to use in pseudoinverse of correlation matrix (zero or less means
		 * 		use as many as possible to a threshold magnitude determined by noise).
		 */
		public int getNSV() {
			return myNSV;
		}

		/**
		 * @param nSV Maximum number of singular values to use in pseudoinverse of correlation matrix (zero or less means
		 * 		use as many as possible to a threshold magnitude determined by noise).
		 */
		public void setNSV(int nSV) {
			myNSV = nSV;
		}

		/**
		 * @return Whether or not information will be printed out to console during make process.
		 *
		 */
		public boolean getQuiet() {
			return(myQuiet);
		}

		/**
		 * @param quiet Controls whether or not information will be printed out to console during make process.
		 *
		 */
		public void setQuiet(boolean quiet) {
			myQuiet = quiet;
		}


		/**
		 * @see ca.nengo.math.ApproximatorFactory#getApproximator(float[][], float[][])
		 */
        public LinearApproximator getApproximator(float[][] evalPoints, float[][] values) {
			return new WeightedCostApproximator(evalPoints, values, getCostFunction(evalPoints[0].length), myNoise, myNSV, myQuiet);
		}
        
        /**
         * Similar to getApproximator(float[][], float[][]) but uses evaluation signals and outputs computed over time.
         * 
         * @param evaluationSignals Signals over which component functions are evaluated.  First dimension is the signal, second
         * 				is the dimension, and third is time.
         * @param values values of component functions over the evaluation signals.  First dimension is the component, second
         * 				is the signal, and third is time.
         * @return A LinearApproximator that can be used to approximate new Functions as a weighted sum of the given components.
         */
        public LinearApproximator getApproximator(float[][][] evaluationSignals, float[][][] values) {
        	return new WeightedCostApproximator(evaluationSignals, values, getCostFunction(evaluationSignals[0].length), myNoise, myNSV, myQuiet);
        }

		/**
		 * Note: override to use non-uniform error weighting.
		 *
		 * @param dimension Dimension of the function to be approximated
		 * @return A function over the input space that defines relative importance of error at each point (defaults
		 * 		to a ConstantFunction)
		 */
		public Function getCostFunction(int dimension) {
			return new ConstantFunction(dimension, 1);
		}

		@Override
		public ApproximatorFactory clone() throws CloneNotSupportedException {
			return (ApproximatorFactory) super.clone();
		}

	}

}
