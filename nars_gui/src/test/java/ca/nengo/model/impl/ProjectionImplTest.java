/*
 * Created on 24-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.IdentityFunction;
import ca.nengo.model.*;
import ca.nengo.model.nef.NEFGroup;
import ca.nengo.model.nef.NEFGroupFactory;
import ca.nengo.model.nef.impl.*;
import ca.nengo.util.DataUtils;
import ca.nengo.util.MU;
import ca.nengo.util.Probe;
import junit.framework.TestCase;

/**
 * Unit tests for ProjectionImpl.
 *
 * @author Bryan Tripp
 */
public class ProjectionImplTest extends TestCase {

	private Projection myProjection;
	private Source mySource;
	private Target myTarget;

	@Override
    protected void setUp() throws Exception {
		super.setUp();

		mySource = new MockSource("mock origin", 1);
		myTarget = new MockTarget("mock termination", 1);
		myProjection = new ProjectionImpl(mySource, myTarget, null);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.ProjectionImpl.getOrigin()'
	 */
	public void testGetOrigin() {
		assertEquals(mySource, myProjection.getOrigin());
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.ProjectionImpl.getTermination()'
	 */
	public void testGetTermination() {
		assertEquals(myTarget, myProjection.getTermination());
	}

//	public void testAddBias() throws StructuralException, SimulationException {
//		//TODO: transient dominating error calc
//      //TODO: speed up this test, or something, it takes forever and fails half the time.
//		Network network = new NetworkImpl();
//		FunctionInput input = new FunctionInput("input", new Function[]{new IdentityFunction(1, 0)}, Units.UNK);
//		network.addNode(input);
//		NEFEnsembleFactory ef = new NEFEnsembleFactoryImpl();
//		int n = 200;
//		NEFEnsemble pre = ef.make("pre", n, 1);
//		pre.addDecodedTermination("input", MU.I(1), .005f, false);
//		network.addNode(pre);
//		network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME), pre.getTermination("input"));
//		NEFEnsemble post = ef.make("post", n, 1);
//		network.addNode(post);
//		post.addDecodedTermination("input", MU.I(1), .01f, false);
//		Projection p = network.addProjection(pre.getOrigin(NEFEnsemble.X), post.getTermination("input"));
//
//		DecodedOrigin o = (DecodedOrigin) pre.getOrigin(NEFEnsemble.X);
//		DecodedTermination t = (DecodedTermination) post.getTermination("input");
//		float[][] directWeights = MU.prod(post.getEncoders(), MU.prod(t.getTransform(), MU.transpose(o.getDecoders())));
//		System.out.println("Direct weights: " + MU.min(directWeights) + " to " + MU.max(directWeights));
//
//		Probe probe = network.getSimulator().addProbe(post.getName(), NEFEnsemble.X, true);
//		network.setMode(SimulationMode.CONSTANT_RATE);
//		network.run(-1.5f, 1);
//		network.setMode(SimulationMode.DEFAULT);
//		float[] reference = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//
//		network.run(-1.5f, 1);
////		Plotter.plot(probe.getData(), "mixed weights");
//		float[] mixed = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		getError(reference, mixed);
//
//		p.addBias(300, .005f, .01f, true, false);
//		BiasOrigin bo = (BiasOrigin) pre.getOrigin("post:input");
//		BiasTermination bt = (BiasTermination) post.getTermination("input:bias");
//		assertTrue(MU.min(getNetWeights(directWeights, bo, bt)) > -1e-10);
//		network.run(-1.5f, 1);
////		Plotter.plot(probe.getData(), "positive non-optimal");
//		float[] positiveNonOptimal = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		float error = getError(reference, positiveNonOptimal);
//		assertTrue(error > 1e-10 && error < 5e-3);	// used to be 5e-4, but was
//													// slightly over that
//		p.removeBias();
//
//		p.addBias(300, .005f, .01f, true, true);
//		bo = (BiasOrigin) pre.getOrigin("post:input");
//		bt = (BiasTermination) post.getTermination("input:bias");
//		assertTrue(MU.min(getNetWeights(directWeights, bo, bt)) > -1e-10);
//		network.run(-1.5f, 1);
////		Plotter.plot(probe.getData(), "positive optimal");
//		float[] positiveOptimal = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		float error2 = getError(reference, positiveOptimal);
//		assertTrue(error2 > 1e-10 && error2 < 2.5e-4 && error2 < error);
//		p.removeBias();
//
//		p.addBias(300, .005f, .01f, false, false);
//		bo = (BiasOrigin) pre.getOrigin("post:input");
//		bt = (BiasTermination) post.getTermination("input:bias");
//		assertTrue(MU.min(getNetWeights(directWeights, bo, bt)) < 1e-10);
//		network.run(-1.5f, 1);
////		Plotter.plot(probe.getData(), "negative non-optimal");
//		float[] negativeNonOptimal = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		error = getError(reference, negativeNonOptimal);
//		assertTrue(error > 1e-10 && error < 7e-4);
//		p.removeBias();
//
//		p.addBias(300, .005f, .01f, false, true);
//		bo = (BiasOrigin) pre.getOrigin("post:input");
//		bt = (BiasTermination) post.getTermination("input:bias");
//		assertTrue(MU.min(getNetWeights(directWeights, bo, bt)) < 1e-10);
//		network.run(-1.5f, 1);
////		Plotter.plot(probe.getData(), "negative optimal");
//		float[] negativeOptimal = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		error2 = getError(reference, negativeOptimal);
//		assertTrue(error2 > 1e-10 && error2 < 3.5e-4 && error2 < error);
//	}

	public void testAddBias2D() throws StructuralException, SimulationException {
		Network network = new NetworkImpl();
		FunctionInput input = new FunctionInput("input", new Function[]{new IdentityFunction(1, 0)}, Units.UNK);
		network.addNode(input);
		NEFGroupFactory ef = new NEFGroupFactoryImpl();
		int n = 300;
		NEFGroup pre = ef.make("pre", n, 2);
		pre.addDecodedTermination("input", MU.uniform(2, 1, 1), .005f, false);
		network.addNode(pre);
		network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME), pre.getTermination("input"));
		NEFGroup post = ef.make("post", n, 2);
		network.addNode(post);
		post.addDecodedTermination("input", MU.I(2), .01f, false);
		Projection p = network.addProjection(pre.getOrigin(NEFGroup.X), post.getTermination("input"));

		DecodedSource o = (DecodedSource) pre.getOrigin(NEFGroup.X);
		DecodedTarget t = (DecodedTarget) post.getTermination("input");
		float[][] directWeights = MU.prod(post.getEncoders(), MU.prod(t.getTransform(), MU.transpose(o.getDecoders())));
		System.out.println("Direct weights: " + MU.min(directWeights) + " to " + MU.max(directWeights));

		Probe probe = network.getSimulator().addProbe(post.getName(), NEFGroup.X, true);
		network.setMode(SimulationMode.CONSTANT_RATE);
		network.run(-1.5f, 1);
		network.setMode(SimulationMode.DEFAULT);
		float[] reference = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];

		network.run(-1.5f, 1);
//		Plotter.plot(probe.getData(), "mixed weights");
		float[] mixed = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
		getError(reference, mixed);

		p.addBias(300, .005f, .01f, true, false);
		BiasSource bo = (BiasSource) pre.getOrigin("post_input");
		BiasTarget bt = (BiasTarget) post.getTermination("input (bias)");
		assertTrue(MU.min(getNetWeights(directWeights, bo, bt)) > -1e-10);
		network.run(-1.5f, 1);
//		Plotter.plot(probe.getData(), "positive non-optimal");
//		float[] positiveNonOptimal = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		float error = getError(reference, positiveNonOptimal);
//		assertTrue(error > 1e-10 && error < 5e-4);
		p.removeBias();

		p.addBias(300, .005f, .01f, true, true);
		bo = (BiasSource) pre.getOrigin("post_input");
		bt = (BiasTarget) post.getTermination("input (bias)");
		assertTrue(MU.min(getNetWeights(directWeights, bo, bt)) > -1e-10);
		network.run(-1.5f, 1);
//		Plotter.plot(probe.getData(), "positive optimal");
//		float[] positiveOptimal = MU.transpose(DataUtils.filter(probe.getData(), .01f).getValues())[0];
//		float error2 = getError(reference, positiveOptimal);
//		assertTrue(error2 > 1e-10 && error2 < 2.5e-4 && error2 < error);
		p.removeBias();
	}

	private static float getError(float[] reference, float[] data) {
		int start = Math.round(reference.length/5); //avoid transient in error calculation
		int length = reference.length - start;
		float[] difference = new float[length];
		System.arraycopy(MU.difference(data, reference), start, difference, 0, length);

		float result = MU.variance(difference, 0);
//		Plotter.plot(difference, "error variance: " + result);
		System.out.println("error" + result);
		return result;
	}

	private static float[][] getNetWeights(float[][] directWeights, BiasSource bo, BiasTarget bt) {
		float[][] biasWeights = MU.prod(MU.transpose(new float[][]{bt.getBiasEncoders()}), MU.transpose(bo.getDecoders()));
		System.out.println("Bias weights: " + MU.min(biasWeights) + " to " + MU.max(biasWeights));

		float[][] netWeights = MU.sum(directWeights, biasWeights);
		System.out.println("Net weights: " + MU.min(netWeights) + " to " + MU.max(netWeights) + " mean " + MU.mean(netWeights));

		return netWeights;
	}

	public static class MockSource implements Source {

		private static final long serialVersionUID = 1L;

		private String myName;
		private int myDimensions;

		public MockSource(String name, int dimensions) {
			myName = name;
			myDimensions = dimensions;

		}

		public String getName() {
			return myName;
		}

		public void setName(String name) {
			myName = name;
		}

		public int getDimensions() {
			return myDimensions;
		}

		public void setDimensions(int dim) {
			myDimensions = dim;
		}

		public InstantaneousOutput get() {
			throw new RuntimeException("not implemented");
		}
		
		public  void accept(InstantaneousOutput val) {
			throw new RuntimeException("not implemented");
		}


		public Node getNode() {
			return null;
		}
		
		public void setRequiredOnCPU(boolean val){
		}
		    
		public boolean getRequiredOnCPU(){
		    return false;
		}

		@Override
		public Source clone() throws CloneNotSupportedException {
			return (Source) super.clone();
		}
		
		public Source clone(Node node) throws CloneNotSupportedException {
			return this.clone();
		}
	}

	public static class MockTarget implements Target {

		private static final long serialVersionUID = 1L;

		private final String myName;
		private final int myDimensions;

		public MockTarget(String name, int dimensions) {
			myName = name;
			myDimensions = dimensions;
		}

		public String getName() {
			return myName;
		}

		public int getDimensions() {
			return myDimensions;
		}

		public void setValues(InstantaneousOutput values) throws SimulationException {
			throw new RuntimeException("not implemented");
		}

		public void propertyChange(String propertyName, Object newValue) {
			throw new RuntimeException("not implemented");
		}

		public Node getNode() {
			return null;
		}

		public boolean getModulatory() {
			return false;
		}

		public float getTau() {
			return 0;
		}

		public void setModulatory(boolean modulatory) {
		}

		public void setTau(float tau) throws StructuralException {
		}
		
		public InstantaneousOutput get() {
			throw new RuntimeException("not implemented");
		}

		/**
		 * @see ca.nengo.model.Resettable#reset(boolean)
		 */
		public void reset(boolean randomize) {
		}

		@Override
		public MockTarget clone() throws CloneNotSupportedException {
			return this.clone(null);
		}
		
		public MockTarget clone(Node node) throws CloneNotSupportedException {
			return (MockTarget)super.clone();
		}

	}

	public static void main(String[] args) {
		ProjectionImplTest test = new ProjectionImplTest();
		try {
			test.testAddBias2D();
		} catch (StructuralException e) {
			e.printStackTrace();
		} catch (SimulationException e) {
			e.printStackTrace();
		}
	}
}
