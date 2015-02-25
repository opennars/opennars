//package ca.nengo.model.plasticity.impl;

//import ca.nengo.TestUtil;
//import ca.nengo.math.Function;
//import ca.nengo.math.impl.AbstractFunction;
//import ca.nengo.math.impl.ConstantFunction;
//import ca.nengo.math.impl.IndicatorPDF;
//import ca.nengo.math.impl.PiecewiseConstantFunction;
//import ca.nengo.model.Ensemble;
//import ca.nengo.model.InstantaneousOutput;
//import ca.nengo.model.Network;
//import ca.nengo.model.Node;
//import ca.nengo.model.SimulationException;
//import ca.nengo.model.SimulationMode;
//import ca.nengo.model.StructuralException;
//import ca.nengo.model.Units;
//import ca.nengo.model.impl.EnsembleImpl;
//import ca.nengo.model.impl.FunctionInput;
//import ca.nengo.model.impl.NetworkImpl;
//import ca.nengo.model.impl.NodeFactory;
//import ca.nengo.model.impl.RealOutputImpl;
//import ca.nengo.model.impl.SpikeOutputImpl;
//import ca.nengo.model.neuron.Neuron;
//import ca.nengo.model.neuron.SpikeGenerator;
//import ca.nengo.model.neuron.impl.LIFNeuronFactory;
//import ca.nengo.model.neuron.impl.ExpandableSpikingNeuron;
//import ca.nengo.model.plasticity.PlasticityRule;
//import ca.nengo.model.plasticity.impl.SpikePlasticityRule;
//import ca.nengo.util.MU;
//import junit.framework.TestCase;
//
///**
// * Unit tests for SpikePlasticityRule. 
// * 
// * @author Bryan Tripp
// */
//public class SpikePlasticityRuleTest extends TestCase {
//
//	private static final String ORIGIN = Neuron.AXON;
//	private static final String MOD_TERMINATION = "mod";
//        private static final String TERM_NAME = "test";
//	private Function myOnInSpike;
//	private Function myOnOutSpike;
//	private PlasticityRule myRule;
//	
//	protected void setUp() throws Exception {
//		super.setUp();
//
//		myOnOutSpike = new STDP(true, .005f); 
//		myOnInSpike = new STDP(false, .005f);
//		myRule = new SpikePlasticityRule(myOnInSpike, myOnOutSpike, ORIGIN, MOD_TERMINATION);
//	}
//
//	public void testGetDerivative() throws StructuralException {
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0f), 0f);
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, .001f), .001f);
//		myRule.setTerminationState(MOD_TERMINATION, new RealOutputImpl(new float[]{1f}, Units.SPIKES_PER_S, .001f), .001f);
//		float[][] d = myRule.getDerivative(MU.I(1), new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, .001f), .001f);
//		TestUtil.assertClose(d[0][0], 0, .00001f); //should be no effect with post-synaptic spike only
//		
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 1f), 1f);
//		myRule.setTerminationState(MOD_TERMINATION, new RealOutputImpl(new float[]{1f}, Units.SPIKES_PER_S, 1f), 1f);
//		d = myRule.getDerivative(MU.I(1), new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 1f), 1f);
//		TestUtil.assertClose(d[0][0], 0, .00001f); //positive and negative effects should cancel
//		
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 2f), 2f);
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 2.005f), 2.005f);
//		myRule.setTerminationState(MOD_TERMINATION, new RealOutputImpl(new float[]{1f}, Units.SPIKES_PER_S, 2.005f), 2.005f);
//		d = myRule.getDerivative(MU.I(1), new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 2.005f), 2.005f);
//		TestUtil.assertClose(d[0][0], -0.0003679f, .00001f); 
//		
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 3f), 3f);
//		myRule.getDerivative(MU.I(1), new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 3f), 3f);
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 3.005f), 3.005f);
//		myRule.setTerminationState(MOD_TERMINATION, new RealOutputImpl(new float[]{1f}, Units.SPIKES_PER_S, 3.005f), 3.005f);
//		d = myRule.getDerivative(MU.I(1), new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 3.005f), 3.005f);
//		TestUtil.assertClose(d[0][0], 0.0003679f, .00001f); 
//
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 4f), 4f);
//		myRule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 4.005f), 4.005f);
//		myRule.setTerminationState(MOD_TERMINATION, new RealOutputImpl(new float[]{0f}, Units.SPIKES_PER_S, 4.005f), 4.005f);
//		d = myRule.getDerivative(MU.I(1), new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 4.005f), 4.005f);
//		TestUtil.assertClose(d[0][0], 0f, .00001f); //modulatory input is zero 
//	}
//	
//	public void testMultiDim() {
//		int inDim = 2;
//		int outDim = 3;
//		PlasticityRule rule = new SpikePlasticityRule(TERM_NAME, ORIGIN, MOD_TERMINATION, 0, myOnInSpike, myOnOutSpike, inDim, outDim);
//		
//		rule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{false, true, false}, Units.SPIKES, 2f), 2f);
//		rule.setOriginState(ORIGIN, new SpikeOutputImpl(new boolean[]{false, false, false}, Units.SPIKES, 2.005f), 2.005f);
//		float[][] transform = new float[][]{new float[]{0, 0}, new float[]{0, 0}, new float[]{0, 0}};
//		rule.setTerminationState(MOD_TERMINATION, new RealOutputImpl(new float[]{1f}, Units.SPIKES_PER_S, 2.005f), 2.005f);
//		float[][] d = rule.getDerivative(transform, new SpikeOutputImpl(new boolean[]{true, false}, Units.SPIKES, 2.005f), 2.005f);
//		TestUtil.assertClose(d[0][0], 0, .00001f); 
//		TestUtil.assertClose(d[1][0], -0.0003679f, .001f); 
//		TestUtil.assertClose(d[2][0], 0, .00001f); 
//		TestUtil.assertClose(d[0][1], 0, .00001f); 
//		TestUtil.assertClose(d[1][1], 0, .00001f); 
//		TestUtil.assertClose(d[2][1], 0, .00001f); 
//	}
//	
//	public void testInNetwork() throws StructuralException, SimulationException {
//		Network network = new NetworkImpl();
//		
//		FunctionInput mod = new FunctionInput("input", new Function[]{new ConstantFunction(1, 1)}, Units.UNK);
//		network.addNode(mod);
//		FunctionInput earlyStep = new FunctionInput("early", new Function[]{new PiecewiseConstantFunction(new float[]{0.1f, .110f}, new float[]{-2, 1, -2})}, Units.UNK);
//		network.addNode(earlyStep);
//		FunctionInput lateStep = new FunctionInput("late", new Function[]{new PiecewiseConstantFunction(new float[]{0.110f, .120f}, new float[]{-2, 1, -2})}, Units.UNK);
//		network.addNode(lateStep);
//		
//		NodeFactory nf = new LIFNeuronFactory(.02f, .002f, new IndicatorPDF(300, 400), new IndicatorPDF(-1, -.9f));
//		PlasticExpandableSpikingNeuron pre = (PlasticExpandableSpikingNeuron) nf.make("pre");
//		pre.addTermination("input", MU.I(1), .005f, false);
//		
//		PlasticExpandableSpikingNeuron post = (PlasticExpandableSpikingNeuron) nf.make("post");
//		post.addTermination(MOD_TERMINATION, MU.I(1), .005f, true);
//		float[][] transform = new float[][]{new float[1]};
//		post.addTermination("pre", transform, .005f, false);
//		post.setPlasticityRule("pre", myRule);
//		post.addTermination("input", MU.I(1), .005f, false);
//		
//		Ensemble ensemble = new EnsembleImpl("ensemble", new Node[]{pre, post});
//		ensemble.collectSpikes(true);
//		network.addNode(ensemble);
//		
//		network.addProjection(earlyStep.getOrigin(FunctionInput.ORIGIN_NAME), pre.getTermination("input"));
//		network.addProjection(lateStep.getOrigin(FunctionInput.ORIGIN_NAME), post.getTermination("input"));
//		network.addProjection(mod.getOrigin(FunctionInput.ORIGIN_NAME), post.getTermination(MOD_TERMINATION));
//		network.addProjection(pre.getOrigin(Neuron.AXON), post.getTermination("pre"));
//		
//		network.run(0, 0.2f);
////		Plotter.plot(ensemble.getSpikePattern());
////		System.out.println(MU.toString(transform, 10));
//		
//		assertTrue(transform[0][0] > .0001f);
//	}
//	
//	public void testInEnsemble() throws StructuralException, SimulationException {
//		NetworkImpl network = new NetworkImpl();
//		
//		NodeFactory nf = new LIFNeuronFactory(.02f, .002f, new IndicatorPDF(50, 100), new IndicatorPDF(-1, -.9f));
//		
//		FunctionInput mod = new FunctionInput("mod", new Function[]{new ConstantFunction(1, 1)}, Units.UNK);
//		network.addNode(mod);
//		
//		Node[] preNodes = new Node[2];
//		for (int i = 0; i < preNodes.length; i++) {
//			preNodes[i] = nf.make(""+i);
//		}
//		EnsembleImpl pre = new EnsembleImpl("pre", preNodes); //these will fire with intrinsic bias current 
//		pre.collectSpikes(true);
//		network.addNode(pre); 
//		
//		Node[] postNodes = new Node[2];
//		for (int i = 0; i < postNodes.length; i++) {
//			postNodes[i] = nf.make(""+i);
//		}
//		EnsembleImpl post = new EnsembleImpl("post", postNodes);
//		post.collectSpikes(true);
//		float[][] weights = new float[][]{new float[]{0, 0}, new float[]{0, 0}};
//		post.addTermination("in", weights, .005f, false);
//		SpikePlasticityRule rule = new SpikePlasticityRule(TERM_NAME, Neuron.AXON, MOD_TERMINATION, 0, myOnInSpike, myOnOutSpike, 2, 1); //output dimension matches neuron
//		post.setPlasticityRule("in", rule);
//		post.addTermination("mod", new float[][]{new float[]{1}, new float[]{1}}, .005f, true);
//		network.addNode(post);
//		
//		network.addProjection(mod.getOrigin(FunctionInput.ORIGIN_NAME), post.getTermination("mod"));
//		network.addProjection(pre.getOrigin(Neuron.AXON), post.getTermination("in"));
//		
//		network.setStepSize(.001f);
//		network.run(0, 1f);
//		
//		assertTrue(Math.abs(weights[0][0]) > .000001);
//		assertTrue(Math.abs(weights[0][1]) > .000001);
//		assertTrue(Math.abs(weights[1][0]) > .000001);
//		assertTrue(Math.abs(weights[1][1]) > .000001);
//		
////		Plotter.plot(pre.getSpikePattern());
////		Plotter.plot(post.getSpikePattern());
////		System.out.println(MU.toString(weights, 10)); //TODO: this seems usually negative but in/out spikes are aligned in same timestep, so not sure where bias arises
//	}
//	
//	/**
//	 * Spikes at specified times (note: not using this presently). 
//	 * 
//	 * @author Bryan Tripp
//	 */
////	private static class TimedSpikeGenerator implements SpikeGenerator {
////
////		private static final long serialVersionUID = 1L;
////		
////		private float[] mySpikeTimes;
////		private float myTolerance;
////		
////		public TimedSpikeGenerator(float[] spikeTimes, float tolerance) {
////			mySpikeTimes = spikeTimes;
////			myTolerance = tolerance;
////		}
////		
////		public InstantaneousOutput run(float[] time, float[] current) {
////			boolean spiking = false;
////			search : for (int i = 0; i < mySpikeTimes.length; i++) {
////				for (int j = 0; j < time.length; j++) {
////					if (Math.abs(mySpikeTimes[i] - time[j]) <= myTolerance) {
////						spiking = true;
////						break search;
////					}
////				}
////			}
////			return new SpikeOutputImpl(new boolean[]{spiking}, Units.SPIKES, time[time.length]);
////		}
////
////		public void reset(boolean randomize) {
////		}
////
////		public SimulationMode getMode() {
////			return SimulationMode.DEFAULT;
////		}
////
////		public void setMode(SimulationMode mode) {
////		}
////		
////	}
//	
//	//exponential function for STDP modulated by another input
//	public static class STDP extends AbstractFunction {
//
//		private static final long serialVersionUID = 1L;
//		
//		private float mySign;
//		private float myTau;
//		
//		public STDP(boolean positive, float tau) {			
//			super(3);
//			setPositive(positive);
//			myTau = tau;
//		}
//		
//		public boolean getPositive() {
//			return mySign > 1;
//		}
//		
//		public void setPositive(boolean positive) {
//			mySign = positive ? 1 : -1;			
//		}
//		
//		public float getTau() {
//			return myTau;
//		}
//		
//		public void setTau(float tau) {
//			myTau = tau;
//		}
//		
//		@Override
//		public float map(float[] from) {
//			double stdp = from[0] > .0001 ? Math.exp(-from[0]/myTau) : 0; //avoid large numbers with small floating-point differences 
//			return .001f * mySign * from[2] * (float) stdp;
//		}
//		
//	}
//	
//	public static void main(String[] args) {
//		SpikePlasticityRuleTest test = new SpikePlasticityRuleTest();
//		try {
//			test.setUp();
////			test.testInNetwork();
//			test.testInEnsemble();
//		} catch (StructuralException e) {
//			e.printStackTrace();
//		} catch (SimulationException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//}
