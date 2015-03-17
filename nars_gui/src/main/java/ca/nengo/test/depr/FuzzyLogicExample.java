/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "FuzzyLogicExample.java". Description: 
"Fuzzification is implemented as a function transformation"

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

package ca.nengo.test.depr;

import ca.nengo.math.Function;
import ca.nengo.math.FunctionInterpreter;
import ca.nengo.math.impl.AbstractFunction;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.math.impl.DefaultFunctionInterpreter;
import ca.nengo.model.Network;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.sim.Simulator;

/**
 * Fuzzification is implemented as a function transformation. Inference is done
 * with multidimensional ensembles from which norms and conorms are decoded.
 * Composition is done by projecting high-dimensional fuzzy consequents
 * additively onto the same ensemble, from which the mode is selected by lateral
 * inhibition.
 * 
 * @author Bryan Tripp
 */
public class FuzzyLogicExample {

	public static Network createNetwork() throws StructuralException {
		NetworkImpl net = new DefaultNetwork<>();
		net.setName("FuzzyLogic");
		
		Simulator simulator = net.getSimulator();
		// Rules:
		// 1) if A and (B or C) then 1
		// 2) if D then 2
		FunctionInterpreter fi = new DefaultFunctionInterpreter();

		Function[] functions = new Function[] { fi.parse("x0 < .2", 1),
				new ConstantFunction(1, .5f), new ConstantFunction(1, .2f),
				new ConstantFunction(1, .3f) };
		FunctionInput in = new FunctionInput("input", functions, Units.UNK);

		NEFGroupFactory ef = new NEFGroupFactoryImpl();

		NEFGroup A = ef.make("A", 100, 1, "A", false);
		NEFGroup B = ef.make("B", 100, 1, "B", false);
		NEFGroup C = ef.make("C", 100, 1, "C", false);
		NEFGroup D = ef.make("D", 100, 1, "D", false);

		NEFGroup rule1a = ef.make("rule1a", 500, 2, "rule1a", false);
		NEFGroup rule1b = ef.make("rule1b", 500, 2, "rule1b", false);
		NEFGroup rule2 = ef.make("rule2", 200, 1, "rule2", false);
		rule2.collectSpikes(true);

		rule1a.addDecodedOrigin("OR", new Function[] { new MAX(2) },
				Neuron.AXON);
		rule1b.addDecodedOrigin("AND", new Function[] { new MIN(2) },
				Neuron.AXON);
		rule1a.doneOrigins();
		rule1b.doneOrigins();
		rule2.doneOrigins();

		NEFGroup output = ef.make("output", 500, 5, "fuzzyoutput", false);

		net.addNode(in);
		net.addNode(A);
		net.addNode(B);
		net.addNode(C);
		net.addNode(D);
		net.addNode(rule1a);
		net.addNode(rule1b);
		net.addNode(rule2);
		net.addNode(output);

		A.addDecodedTermination("in", new float[][] { new float[] { 1f, 0f, 0f,
				0f } }, .005f, false);
		B.addDecodedTermination("in", new float[][] { new float[] { 0f, 1f, 0f,
				0f } }, .005f, false);
		C.addDecodedTermination("in", new float[][] { new float[] { 0f, 0f, 1f,
				0f } }, .005f, false);
		D.addDecodedTermination("in", new float[][] { new float[] { 0f, 0f, 0f,
				1f } }, .005f, false);

		net.addProjection(in.getSource(FunctionInput.ORIGIN_NAME), A
				.getTarget("in"));
		net.addProjection(in.getSource(FunctionInput.ORIGIN_NAME), B
				.getTarget("in"));
		net.addProjection(in.getSource(FunctionInput.ORIGIN_NAME), C
				.getTarget("in"));
		net.addProjection(in.getSource(FunctionInput.ORIGIN_NAME), D
				.getTarget("in"));

		rule1a.addDecodedTermination("B", new float[][] { new float[] { 1f },
				new float[] { 0f } }, .005f, false);
		rule1a.addDecodedTermination("C", new float[][] { new float[] { 0f },
				new float[] { 1f } }, .005f, false);
		rule1b.addDecodedTermination("A", new float[][] { new float[] { 1f },
				new float[] { 0f } }, .005f, false);
		rule1b.addDecodedTermination("B or C", new float[][] {
				new float[] { 0f }, new float[] { 1f } }, .005f, false);
		rule2.addDecodedTermination("D", new float[][] { new float[] { 1f } },
				.005f, false);

		net.addProjection(B.getSource(NEFGroup.X), rule1a
				.getTarget("B"));
		net.addProjection(C.getSource(NEFGroup.X), rule1a
				.getTarget("C"));
		net.addProjection(A.getSource(NEFGroup.X), rule1b
				.getTarget("A"));
		net.addProjection(rule1a.getSource("OR"), rule1b
				.getTarget("B or C"));
		net
				.addProjection(D.getSource(NEFGroup.X), rule2
						.getTarget("D"));

		output.addDecodedTermination("rule1", new float[][] {
				new float[] { .4f }, new float[] { .3f }, new float[] { .2f },
				new float[] { .1f }, new float[] { 0f } }, .005f, false);
		output.addDecodedTermination("rule2", new float[][] {
				new float[] { 0f }, new float[] { .1f }, new float[] { .2f },
				new float[] { .3f }, new float[] { .4f } }, .005f, false);

		net.addProjection(rule1b.getSource("AND"), output
				.getTarget("rule1"));
		net.addProjection(rule2.getSource(NEFGroup.X), output
				.getTarget("rule2"));

		float neg = -.3f;
		float pos = .9f;
		float[][] m = new float[][] { new float[] { pos, neg, neg, neg, neg },
				new float[] { neg, pos, neg, neg, neg },
				new float[] { neg, neg, pos, neg, neg },
				new float[] { neg, neg, neg, pos, neg },
				new float[] { neg, neg, neg, neg, pos }, };
		output.addDecodedTermination("recurrent", m, .005f, false);

		Function[] clipped = new Function[] { new Clip(5, 0, 0f, 1f),
				new Clip(5, 1, 0f, 1f), new Clip(5, 2, 0f, 1f),
				new Clip(5, 3, 0f, 1f), new Clip(5, 4, 0f, 1f) };
		output.addDecodedOrigin("recurrent", clipped, Neuron.AXON);

		net.addProjection(output.getSource("recurrent"), output
				.getTarget("recurrent"));

		/*
		 * Add probes
		 */
		try {
			simulator.addProbe(C.name(), "in", true);
			simulator.addProbe(A.name(), "in", true);
			simulator.addProbe(B.name(), "in", true);
			simulator.addProbe(D.name(), "in", true);
		} catch (SimulationException e) {
			e.printStackTrace();
		}

		return net;
	}

	private static class MIN extends AbstractFunction {

		private static final long serialVersionUID = 1L;

		public MIN(int dim) {
			super(dim);
		}

		public float map(float[] from) {
			float result = from[0];

			for (int i = 1; i < from.length; i++) {
				if (from[i] < result)
					result = from[i];
			}

			return result;
		}
	}

	private static class MAX extends AbstractFunction {

		private static final long serialVersionUID = 1L;

		public MAX(int dim) {
			super(dim);
		}

		public float map(float[] from) {
			float result = from[0];

			for (int i = 1; i < from.length; i++) {
				if (from[i] > result)
					result = from[i];
			}

			return result;
		}
	}

	private static class Clip extends AbstractFunction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int myFromDim;
		private final float myMin;
		private final float myMax;

		public Clip(int dim, int fromDim, float min, float max) {
			super(dim);
			myFromDim = fromDim;
			myMin = min;
			myMax = max;
		}

		public float map(float[] from) {
			float result = from[myFromDim];
			if (result < myMin) {
				result = myMin;
			} else if (result > myMax) {
				result = myMax;
			}

			return result;
		}
	}

}
