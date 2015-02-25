/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "BiasOrigin.java". Description:
"Part of a projection in which each of the Nodes making up an Ensemble is a source of only excitatory or inhibitory
  connections"

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
 * Created on 23-Apr-07
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.math.Function;
import ca.nengo.math.PDF;
import ca.nengo.math.impl.*;
import ca.nengo.math.impl.GradientDescentApproximator.Constraints;
import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.impl.LIFNeuronFactory;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.util.MU;
import ca.nengo.util.VectorGenerator;
import ca.nengo.util.impl.RandomHypersphereVG;
import ca.nengo.util.impl.Rectifier;

/**
 * <p>Part of a projection in which each of the Nodes making up an Ensemble is a source of only excitatory or inhibitory
 * connections.</p>
 *
 * <p>The theory is presented in Parisien, Anderson & Eliasmith (2007).</p>
 *
 * <p>Such a projection includes a "base" DecodedOrigin and DecodedTermination (a projection between these may have weights
 * of mixed sign). The projection is expanded with a BiasOrigin a pair of BiasTerminations, and a new NEFEnsemble of
 * interneurons. The make weight signs uniform, a projection is established between the BiasOrigin and BiasTermination,
 * in parallel with the original projection. The effective synaptic weights that arise from the combination of these two
 * projections are of uniform sign. However, the post-synaptic Ensemble receives extra bias current as a result. This bias
 * current is cancelled by a projection from the BiasOrigin through the interneurons, to a second BiasTermination.</p>
 *
 * TODO: account for transformations in the Termination, which can change sign and magnitude of weights
 *
 * @author Bryan Tripp
 */
public class BiasSource extends DecodedSource {

	private static final long serialVersionUID = 1L;

	private NEFGroup myInterneurons;
	private float[][] myConstantOutputs;

	/**
	 * @param ensemble Parent ensemble
	 * @param name Origin name
	 * @param nodes Nodes in ensemble?
	 * @param nodeOrigin Name of origin to use for bias origin
	 * @param constantOutputs ?
	 * @param numInterneurons Number of interneurons to create
	 * @param excitatory Excitatory or inhibitory?
	 * @throws StructuralException if DecodedOrigin can't be created
	 */
	public BiasSource(NEFGroup ensemble, String name, Node[] nodes, String nodeOrigin,
                      float[][] constantOutputs, int numInterneurons, boolean excitatory) throws StructuralException {
		super(ensemble, name, nodes, nodeOrigin,
				new Function[]{new ConstantFunction(ensemble.getDimension(), 0f)},
				getUniformBiasDecoders(constantOutputs, excitatory), 0);

		myInterneurons = createInterneurons(name + " interneurons", numInterneurons, excitatory);
		myConstantOutputs = constantOutputs;
	}

	/**
	 * This method adjusts bias decoders so that the bias function is as flat as possible, without changing the
	 * bias encoders on the post-synaptic ensemble. Distortion can be minimized by calling this method and then
	 * calling optimizeInterneuronDomain().
	 *
	 * @param baseWeights Matrix of synaptic weights in the unbiased projection (ie the weights of mixed sign)
	 * @param biasEncoders Encoders of the bias dimension on the post-synaptic ensemble
	 * @param excitatory If true, weights are to be kept positive (otherwise negative)
	 */
	public void optimizeDecoders(float[][] baseWeights, float[] biasEncoders, boolean excitatory) {
		float[][] evalPoints = MU.transpose(new float[][]{new float[myConstantOutputs[0].length]}); //can use anything here because target function is constant
		GradientDescentApproximator.Constraints constraints = new BiasEncodersMaintained(baseWeights, biasEncoders, excitatory);
		GradientDescentApproximator approximator = new GradientDescentApproximator(evalPoints, MU.clone(myConstantOutputs), constraints, true);
		approximator.setStartingCoefficients(MU.transpose(getDecoders())[0]);
		float[] newDecoders = approximator.findCoefficients(new ConstantFunction(1, 0));
		super.setDecoders(MU.transpose(new float[][]{newDecoders}));
	}

	/**
	 * This method adjusts the interneuron channel so that the interneurons are tuned to the
	 * range of values that is output by the bias function.
	 *
	 * @param interneuronTermination The Termination on getInterneurons() that recieves input from this Origin
	 * @param biasTermination The BiasTermination to which the interneurons project (not the one to which this Origin
	 * 		projects directly)
	 */
	public void optimizeInterneuronDomain(DecodedTarget interneuronTermination, DecodedTarget biasTermination) {
		float[] range = this.getRange();
		range[0] = range[0] - .4f * (range[1] - range[0]); //avoid distorted area near zero in interneurons
		interneuronTermination.setStaticBias(new float[]{-range[0]});
		biasTermination.setStaticBias(MU.sum(biasTermination.getStaticBias(), new float[]{range[0]/(range[1] - range[0])}));
		try {
			interneuronTermination.setTransform(new float[][]{new float[]{1f / (range[1] - range[0])}});
			biasTermination.setTransform(new float[][]{new float[]{-(range[1] - range[0])}});
		} catch (StructuralException e) {
			throw new RuntimeException("Problem parameterizing termination",  e);
		}
	}

	/**
	 * @return Vector of mininum and maximum output of this origin, ie {min, max}
	 */
	public float[] getRange() {
		float[] outputs = MU.prod(MU.transpose(myConstantOutputs), MU.transpose(getDecoders())[0]);
		return new float[]{MU.min(outputs), MU.max(outputs)};
	}

	private static float[][] getUniformBiasDecoders(float[][] constantOutputs, boolean excitatory) {
		float[][] result = new float[constantOutputs.length][];
		float decoder = getBiasDecoder(constantOutputs, excitatory);
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[]{decoder};
		}
		return result;
	}

	private static float getBiasDecoder(float[][] constantOutputs, boolean excitatory) {
		//iterate over evaluation points to find max of sum(constantOutputs)
		float max = 0;
		for (int i = 0; i < constantOutputs[0].length; i++) {
			float sum = 0;
			for (float[] constantOutput : constantOutputs) {
				sum += constantOutput[i];
			}
			if (sum > max) {
                max = sum;
            }
		}

		return excitatory ? 1f / max : -1f / max; //this makes the bias function peak at 1 (or -1)
	}

	private NEFGroup createInterneurons(String name, int num, boolean excitatoryProjection) throws StructuralException {
		final Function f;
		if (excitatoryProjection) {
			f = new IdentityFunction(1, 0);
		} else {
			f = new AbstractFunction(1) {
				private static final long serialVersionUID = 1L;
				public float map(float[] from) {
					return 1 + from[0];
				}
			};
		}

		NEFGroupFactory ef = new NEFGroupFactoryImpl() {
			private static final long serialVersionUID = 1L;
			protected void addDefaultOrigins(NEFGroup ensemble) {} //wait until some neurons are adjusted
		};
		ef.setEncoderFactory(new Rectifier(ef.getEncoderFactory(), true));
		ef.setEvalPointFactory(new BiasedVG(new RandomHypersphereVG(false, 0.5f, 0f), 0, excitatoryProjection ? .5f : -.5f));

//		PDF interceptPDF = excitatoryProjection ? new IndicatorPDF(-.5f, .75f) : new IndicatorPDF(-.99f, .35f);
		PDF interceptPDF = excitatoryProjection ? new IndicatorPDF(-.15f, .9f) : new IndicatorPDF(-1.2f, .1f); //was -.5f, .75f for excitatory
		PDF maxRatePDF = excitatoryProjection ? new IndicatorPDF(200f, 500f) : new IndicatorPDF(400f, 800f);
		ef.setNodeFactory(new LIFNeuronFactory(.02f, .0001f, maxRatePDF, interceptPDF));
		ef.setApproximatorFactory(new GradientDescentApproximator.Factory(
				new GradientDescentApproximator.CoefficientsSameSign(true), false));

		NEFGroup result = ef.make(name, num, 1);

		//TODO: bounding neurons for inhibitory projection
		//set intercepts of first few neurons to 1 (outside normal range)
		int n = 10;
		for (int i = 0; i < n; i++) {
			SpikingNeuron neuron = (SpikingNeuron) result.getNodes()[i];
			neuron.setBias(1-neuron.getScale());
		}

		DecodedSource o = (DecodedSource) result.addDecodedOrigin(NEFGroup.X, new Function[]{f}, Neuron.AXON);

		float[][] decoders = o.getDecoders();
		for (int i = 0; i < n; i++) {
			decoders[i] = new float[]{1f / n / 300};
		}
		o.setDecoders(decoders);

		return result;
	}

	/**
	 * @return An ensemble of interneurons through which this Origin must project (in parallel with its
	 * 		direct projection) to compensate for the bias introduced by making all weights the same sign.
	 */
	public NEFGroup getInterneurons() {
		return myInterneurons;
	}

	private static class BiasEncodersMaintained implements GradientDescentApproximator.Constraints {

		private static final long serialVersionUID = 1L;

		private double[][] myBaseWeights;
		private double[] myBiasEncoders;
		private final boolean myExcitatory;

		public BiasEncodersMaintained(float[][] baseWeights, float[] biasEncoders, boolean excitatory) {
			myBaseWeights = MU.convert(baseWeights);
			myBiasEncoders = MU.convert(biasEncoders);
			myExcitatory = excitatory;
		}

		public boolean correct(float[] coefficients) {
			boolean allCorrected = true;

			for (int i = 0; i < coefficients.length; i++) {
				boolean corrected = false;

				if (myExcitatory && coefficients[i] < 0) { //next correction will fail
					coefficients[i] = Float.MIN_VALUE;
					corrected = true;
				} else if (!myExcitatory && coefficients[i] > 0) {
					coefficients[i] = - Float.MIN_VALUE;
					corrected = true;
				}

				for (int j = 0; j < myBiasEncoders.length; j++) {
					if ( - myBaseWeights[j][i] / coefficients[i] > myBiasEncoders[j] )  {
						coefficients[i] = - (float) (myBaseWeights[j][i] / myBiasEncoders[j]);
						corrected = true;
					}
				}

				if (!corrected) {
                    allCorrected = false;
                }
			}

			return allCorrected;
		}

		@Override
		public Constraints clone() throws CloneNotSupportedException {
			BiasEncodersMaintained result = (BiasEncodersMaintained) super.clone();
			result.myBaseWeights = MU.clone(myBaseWeights);
			result.myBiasEncoders = myBiasEncoders.clone();
			return result;
		}

	}

	/**
	 * Adds a specified bias to a specified dimension of vectors that are made by an underlying generator.
	 *
	 * @author Bryan Tripp
	 */
	private static class BiasedVG implements VectorGenerator, java.io.Serializable {
		private static final long serialVersionUID = 1L;

		private final VectorGenerator myVG;
		private final int myDim;
		private final float myBias;

		public BiasedVG(VectorGenerator vg, int dim, float bias) {
			myVG = vg;
			myDim = dim;
			myBias = bias;
		}

		/**
		 * @see ca.nengo.util.VectorGenerator#genVectors(int, int)
		 */
		public float[][] genVectors(int number, int dimension) {
			float[][] result = myVG.genVectors(number, dimension);
			for (int i = 0; i < result.length; i++) {
				result[i][myDim] += myBias;
			}
			return result;
		}
	}

}
