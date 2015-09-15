/*
 * Created on 24-Apr-07
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.AbstractFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.plot.Plotter;
import ca.nengo.util.MU;
import ca.nengo.util.Probe;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;
import junit.framework.TestCase;
import org.junit.Ignore;

//import ca.nengo.math.impl.ConstantFunction;
//import ca.nengo.model.nef.impl.DecodedOrigin;
//import ca.nengo.model.nef.impl.DecodedTermination;

/**
 * Unit tests for NEFEnsembleImpl. 
 * 
 * TODO: this is a functional test with no failures ... convert to unit test
 * TODO: make sure performance optimization works with inhibitory projections
 * 
 * @author Bryan Tripp
 */
@Ignore
public class NEFGroupImplTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void functionalTestAddBiasOrigin() throws StructuralException, SimulationException {
		NEFGroupFactory ef = new NEFGroupFactoryImpl();

		boolean regenerate = false;
		NEFGroup source = ef.make("source", 300, 1, "nefeTest_source", regenerate);
		NEFGroup dest = ef.make("dest", 300, 1, "nefeTest_dest", regenerate);
		
		Function f = new AbstractFunction(1) {
			private static final long serialVersionUID = 1L;
			public float map(float[] from) {
				return from[0] - 1;
			}
		};
		FunctionInput input = new FunctionInput("input", new Function[]{f}, Units.UNK);
//		FunctionInput zero = new FunctionInput("zero", new Function[]{new ConstantFunction(1, 0f)}, Units.UNK);
		
		Network network = new DefaultNetwork();
		network.addNode(input);
		network.addNode(source);
		network.addNode(dest);

		source.addDecodedTermination("input", MU.I(1), .005f, false); //OK
		BiasSource bo = source.addBiasOrigin(source.getSource(NEFGroup.X), 200, "interneurons", true); //should have -ve bias decoders
		network.addNode(bo.getInterneurons()); //should be backwards response functions
//**		bo.getInterneurons().addDecodedTermination("source", MU.I(1), .005f, false);
		
//		Plotter.plot(bo.getInterneurons());
//		Plotter.plot(bo.getInterneurons(), NEFEnsemble.X);
		
//		DecodedTermination t = (DecodedTermination) dest.addDecodedTermination("source", MU.I(1), .005f, false);
//**		BiasTermination[] bt = dest.addBiasTerminations(t, .002f, bo.getDecoders()[0][0], ((DecodedOrigin) source.getOrigin(NEFEnsemble.X)).getDecoders());
//**		bt[1].setStaticBias(-1); //creates intrinsic current needed to counteract interneuron activity at 0
		
//		float[][] weights = MU.prod(dest.getEncoders(), MU.transpose(((DecodedOrigin) source.getOrigin(NEFEnsemble.X)).getDecoders()));
//*		float[][] biasEncoders = MU.transpose(new float[][]{bt[0].getBiasEncoders()});
//*		float[][] biasDecoders = MU.transpose(bo.getDecoders());
//*		float[][] weightBiases = MU.prod(biasEncoders, biasDecoders);
//*		float[][] biasedWeights = MU.sum(weights, weightBiases);
//		Plotter.plot(weights[0], "some weights");
//		Plotter.plot(biasedWeights[0], "some biased weights");
//		Plotter.plot(weights[1], "some more weights");
		
//		Plotter.plot(bt[0].getBiasEncoders(), "bias decoders");
		
		network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), source.getTarget("input"));
		network.addProjection(source.getSource(NEFGroup.X), dest.getTarget("source"));
//*		network.addProjection(bo, bo.getInterneurons().getTermination("source"));
//*		network.addProjection(bo, bt[0]);
//*		network.addProjection(bo.getInterneurons().getOrigin(NEFEnsemble.X), bt[1]);
//		network.addProjection(zero.getOrigin(FunctionInput.ORIGIN_NAME), bt[1]);
		
//		Probe sourceProbe = network.getSimulator().addProbe("source", NEFEnsemble.X, true);
//		Probe destProbe = network.getSimulator().addProbe("dest", NEFEnsemble.X, true);
//		Probe interProbe = network.getSimulator().addProbe("source_X_bias_interneurons", NEFEnsemble.X, true);
		
		network.run(0, 2);
		
//		Plotter.plot(sourceProbe.getData(), "source");
//		Plotter.plot(destProbe.getData(), "dest");
//		Plotter.plot(interProbe.getData(), "interneurons");
	}
	
	public void functionalTestBiasOriginError() throws StructuralException, SimulationException {
		float tauPSC = .01f;
		
		Network network = new DefaultNetwork();
		
		Function f = new AbstractFunction(1) {
			private static final long serialVersionUID = 1L;
			public float map(float[] from) {
				return from[0] - 1;
			}
		};
		
		FunctionInput input = new FunctionInput("input", new Function[]{f}, Units.UNK);
		network.addNode(input);
		
		NEFGroupFactory ef = new NEFGroupFactoryImpl();
		NEFGroup pre = ef.make("pre", 400, 1, "nefe_pre", false);
		pre.addDecodedTermination("input", MU.I(1), tauPSC, false);
//		DecodedOrigin baseOrigin = (DecodedOrigin) pre.getOrigin(NEFEnsemble.X);
		network.addNode(pre);
		
		NEFGroup post = ef.make("post", 200, 1, "nefe_post", false);
//		DecodedTermination baseTermination = (DecodedTermination) post.addDecodedTermination("pre", MU.I(1), tauPSC, false);
		network.addNode(post);
		
		network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), pre.getTarget("input"));
		Projection projection = network.addProjection(pre.getSource(NEFGroup.X), post.getTarget("pre"));
		
		Probe pPost = network.getSimulator().addProbe("post", NEFGroup.X, true);
		network.run(0, 2);
		TimeSeries ideal = pPost.getData();
		Plotter.plot(pPost.getData(), .005f, "mixed weights result");		
		
		//remove negative weights ... 
		System.out.println("Minimum weight without bias: " + MU.min(projection.getWeights()));
		projection.addBias(100, .005f, tauPSC, true, false);
		System.out.println("Minimum weight with bias: " + MU.min(projection.getWeights()));
		pPost.reset();
		network.run(0, 2);
		TimeSeries diff = new TimeSeriesImpl(ideal.getTimes(), MU.difference(ideal.getValues(), pPost.getData().getValues()), ideal.getUnits()); 
		Plotter.plot(diff, .01f, "positive weights");
		
		projection.removeBias();
		projection.addBias(100, tauPSC/5f, tauPSC, true, true);
		pPost.reset();
		Probe pInter = network.getSimulator().addProbe("post:pre:interneurons", NEFGroup.X, true);
		network.run(0, 2);
		diff = new TimeSeriesImpl(ideal.getTimes(), MU.difference(ideal.getValues(), pPost.getData().getValues()), ideal.getUnits()); 
		Plotter.plot(diff, .01f, "positive weights optimized");
		Plotter.plot(pInter.getData(), .01f, "interneurons");

		
		
//		//remove negative weights ... 
//		BiasOrigin bo = pre.addBiasOrigin(baseOrigin, 100, "interneurons", true);
//		BiasTermination[] bt = post.addBiasTerminations(baseTermination, tauPSC, bo.getDecoders()[0][0], baseOrigin.getDecoders());
//		DecodedTermination it = (DecodedTermination) bo.getInterneurons().addDecodedTermination("bias", MU.I(1), tauPSC/5f, false);
//		network.addNode(bo.getInterneurons());
//		network.addProjection(bo, bt[0]);
//		network.addProjection(bo, bo.getInterneurons().getTermination("bias"));
//		network.addProjection(bo.getInterneurons().getOrigin(NEFEnsemble.X), bt[1]);
//		Plotter.plot(MU.transpose(bo.getDecoders())[0], "bias decoders");
////		Plotter.plot(bo.getInterneurons(), NEFEnsemble.X);
//		
//		pPost.reset();
//		network.run(0, 2);
//		TimeSeries diff = new TimeSeriesImpl(ideal.getTimes(), MU.difference(ideal.getValues(), pPost.getData().getValues()), ideal.getUnits()); 
//		Plotter.plot(diff, .01f, "positive weights");
////		Plotter.plot(ideal, pPost.getData(), .005f, "positive weights result");
//		
//		//narrow bias range ... 
////		Plotter.plot(pre, bo.getName());
//		float[][] baseWeights = MU.prod(post.getEncoders(), MU.prod(baseTermination.getTransform(), MU.transpose(baseOrigin.getDecoders())));
//		float[] encodersBeforeTweak = findBiasEncoders(baseWeights, MU.transpose(bo.getDecoders())[0]);
//		bo.optimizeDecoders(baseWeights, bt[0].getBiasEncoders());
////		Plotter.plot(pre, bo.getName());
//		float[] encodersAfterTweak = findBiasEncoders(baseWeights, MU.transpose(bo.getDecoders())[0]);
//		TestUtil.assertClose(MU.sum(MU.difference(encodersBeforeTweak, encodersAfterTweak)), 0, .0001f);
//		Plotter.plot(MU.transpose(bo.getDecoders())[0], "narrow bias decoders");
//		
//		pPost.reset();		
//		network.run(0, 2);
//		diff = new TimeSeriesImpl(ideal.getTimes(), MU.difference(ideal.getValues(), pPost.getData().getValues()), ideal.getUnits()); 
//		Plotter.plot(diff, .01f, "narrowed bias"); 		
////		Plotter.plot(ideal, pPost.getData(), .005f, "narrowed bias result");
//		
//		//optimize interneuron range ... 
//		float[] range = bo.getRange();
//		System.out.println(range[0] + " to " + range[1]);
//		range[0] = range[0] - .25f * (range[1] - range[0]); //avoid distorted area near zero in interneurons 
//		it.setStaticBias(new float[]{-range[0]});
//		it.getTransform()[0][0] = 1f / (range[1] - range[0]);
//		bt[1].setStaticBias(new float[]{range[0]/(range[1] - range[0])});
//		bt[1].getTransform()[0][0] = -(range[1] - range[0]);		
//		
//		pPost.reset();
//		network.run(0, 2);
//		diff = new TimeSeriesImpl(ideal.getTimes(), MU.difference(ideal.getValues(), pPost.getData().getValues()), ideal.getUnits()); 
//		Plotter.plot(diff, .01f, "optimized interneuron"); 				
////		Plotter.plot(ideal, pPost.getData(), .005f, "optimized interneuron result");
//		
////		Probe pBias = network.getSimulator().addProbe("pre", bo.getName(), true);
////		Probe pInter = network.getSimulator().addProbe(bo.getInterneurons().getName(), NEFEnsemble.X, true);
////		Probe pBT0 = network.getSimulator().addProbe("post", bt[0].getName(), true);
////		Probe pBT1 = network.getSimulator().addProbe("post", bt[1].getName(), true);
////		Probe pT = network.getSimulator().addProbe("post", "pre", true);
////		
////		network.run(0, 2);
////		Plotter.plot(pPost.getData(), .005f, "post");
////		Plotter.plot(pBias.getData(), .005f, "bias");
////		Plotter.plot(pInter.getData(), .005f, "interneurons");
////		Plotter.plot(pBT0.getData(), .005f, "BT0");
////		Plotter.plot(pBT1.getData(), .005f, "BT1");
////		Plotter.plot(pT.getData(), .005f, "base termination");
	}
	
//	private float[] findBiasEncoders(float[][] baseWeights, float[] biasDecoders) {
//		float[] biasEncoders = new float[baseWeights.length];
//		
//		for (int j = 0; j < biasEncoders.length; j++) {
//			float max = 0;
//			for (int i = 0; i < biasDecoders.length; i++) {
//				float x = - baseWeights[j][i] / biasDecoders[i];
//				if (x > max) max = x;
//			}			
//			biasEncoders[j] = max;
//		}
//		
//		return biasEncoders;
//	}
	
	public void testClone() throws StructuralException, CloneNotSupportedException {
		NEFGroupFactory ef = new NEFGroupFactoryImpl();
		NEFGroup ensemble = ef.make("test", 100, 1);
		long startTime = System.currentTimeMillis();
		ensemble.clone();
		System.out.println(System.currentTimeMillis() - startTime);
	}
	
	public static void main(String[] args) {
		NEFGroupImplTest test = new NEFGroupImplTest();
		try {
//			test.testAddBiasOrigin();
//			test.functionalTestBiasOriginError();
			test.testClone();
		} catch (StructuralException e) {
			e.printStackTrace();
//		} catch (SimulationException e) {
//			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	public void testKillNeurons() throws StructuralException
	{
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl nef1 = (NEFGroupImpl)ef.make("nef1", 1000, 1);
		
		nef1.killNeurons(0.0f,true);
		int numDead = countDeadNeurons(nef1);
		if(numDead != 0) {
			fail("Number of dead neurons outside expected range");
		}
		
		nef1.killNeurons(0.5f,true);
		numDead = countDeadNeurons(nef1);
		if(numDead < 400 || numDead > 600) {
			fail("Number of dead neurons outside expected range");
		}
		
		nef1.killNeurons(1.0f,true);
		numDead = countDeadNeurons(nef1);
		if(numDead != 1000) {
			fail("Number of dead neurons outside expected range");
		}
		
		NEFGroupImpl nef2 = (NEFGroupImpl)ef.make("nef2", 1, 1);
		nef2.killNeurons(1.0f,true);
		numDead = countDeadNeurons(nef2);
		if(numDead != 0)
			fail("Relay protection did not work");
		nef2.killNeurons(1.0f,false);
		numDead = countDeadNeurons(nef2);
		if(numDead != 1)
			fail("Number of dead neurons outside expected range");

	}
	private int countDeadNeurons(NEFGroupImpl pop)
	{
		Node[] neurons = pop.getNodes();
		int numDead = 0;
		
		for(int i = 0; i < neurons.length; i++)
		{
			SpikingNeuron n = (SpikingNeuron)neurons[i];
			if(n.getBias() == 0.0f && n.getScale() == 0.0f)
				numDead++;
		}
		
		return numDead;
	}
	
	public void testAddDecodedSignalOrigin() throws StructuralException
	{
		NEFGroupFactoryImpl ef = new NEFGroupFactoryImpl();
		NEFGroupImpl ensemble = (NEFGroupImpl)ef.make("test", 5, 1);
		float[][] vals = new float[2][1];
		vals[0][0] = 1;
		vals[1][0] = 1;
		TimeSeriesImpl targetSignal = new TimeSeriesImpl(new float[]{0,1}, vals, new Units[]{Units.UNK});
		TimeSeriesImpl[] evalSignals = new TimeSeriesImpl[1];
		
		//test the per-dimension eval signals
		evalSignals[0] = new TimeSeriesImpl(new float[]{0,1}, vals, new Units[]{Units.UNK});
		ensemble.addDecodedSignalOrigin("test1", targetSignal, evalSignals, "AXON");
		if(ensemble.getSource("test1") == null)
			fail("Error creating per-dimension signal origin");
		
		//test the per-node eval signals
		vals[0] = new float[]{1, 1, 1, 1, 1};
		vals[1] = new float[]{1, 1, 1, 1, 1};
		evalSignals[0] = new TimeSeriesImpl(new float[]{0,1}, vals, new Units[]{Units.UNK,Units.UNK,Units.UNK,Units.UNK,Units.UNK});
		ensemble.addDecodedSignalOrigin("test2", targetSignal, evalSignals, "AXON");
		if(ensemble.getSource("test2") == null)
			fail("Error creating per-node signal origin");
	}

}
