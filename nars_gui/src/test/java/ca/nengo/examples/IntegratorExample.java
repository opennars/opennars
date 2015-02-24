///*
// * Created on 14-Jun-2006
// */
//package ca.nengo.examples;
//
//import java.io.File;
//import java.io.IOException;
//
//import ca.nengo.io.MatlabExporter;
//import ca.nengo.math.Function;
//import ca.nengo.math.impl.ConstantFunction;
//import ca.nengo.model.Ensemble;
//import ca.nengo.model.Network;
//import ca.nengo.model.SimulationException;
////import ca.nengo.model.SimulationMode;
//import ca.nengo.model.StructuralException;
//import ca.nengo.model.Termination;
//import ca.nengo.model.Units;
//import ca.nengo.model.impl.FunctionInput;
//import ca.nengo.model.impl.NetworkImpl;
//import ca.nengo.model.nef.NEFEnsemble;
//import ca.nengo.model.nef.NEFEnsembleFactory;
//import ca.nengo.model.nef.impl.NEFEnsembleFactoryImpl;
//import ca.nengo.plot.Plotter;
//import ca.nengo.sim.Simulator;
////import ca.nengo.sim.impl.LocalSimulator;
//import ca.nengo.util.Probe;
//import ca.nengo.util.TimeSeries;
//
//public class IntegratorExample {
//
//	public static Network createNetwork() throws StructuralException {
//
//		Network network = new NetworkImpl();
//
//		Function f = new ConstantFunction(1, 1f);
////		Function f = new SineFunction();
//		FunctionInput input = new FunctionInput("input", new Function[]{f}, Units.UNK);
//		network.addNode(input);
//
//		NEFEnsembleFactory ef = new NEFEnsembleFactoryImpl();
//
//		NEFEnsemble integrator = ef.make("integrator", 500, 1, "integrator1", true);
//		network.addNode(integrator);
//		integrator.collectSpikes(true);
//
//		Plotter.plot(integrator);
//		Plotter.plot(integrator, NEFEnsemble.X);
//
//		float tau = .05f;
//
//		Termination interm = integrator.addDecodedTermination("input", new float[][]{new float[]{tau}}, tau, false);
////		Termination interm = integrator.addDecodedTermination("input", new float[][]{new float[]{1f}}, tau);
//		network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME), interm);
//
//		Termination fbterm = integrator.addDecodedTermination("feedback", new float[][]{new float[]{1f}}, tau, false);
//		network.addProjection(integrator.getOrigin(NEFEnsemble.X), fbterm);
//
//		//System.out.println("Network creation: " + (System.currentTimeMillis() - start));
//		return network;
//	}
//
//
//	public static void main(String[] args) {
//
//		try {
//			Network network = createNetwork();
//			Simulator simulator = network.getSimulator();
//
//			Probe inputRecorder = simulator.addProbe("input", "input", true);
//			Probe integratorRecorder = simulator.addProbe("integrator", NEFEnsemble.X, true);
//			Probe neuronRecorder = simulator.addProbe("integrator", 0, "V", true);
//
//			long startTime = System.currentTimeMillis();
//			simulator.run(0f, 1f, .0002f);
//			System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000f) );
//
//			TimeSeries integratorData = integratorRecorder.getData();
//			integratorData.getLabels()[0] = "decoded output";
//
//			Plotter.plot(inputRecorder.getData(), "Input");
//			Plotter.plot(integratorData, .005f, "Integrator");
//			Plotter.plot(neuronRecorder.getData(), "Neuron #0");
//
//			Plotter.plot(((Ensemble) network.getNode("integrator")).getSpikePattern());
//
//			MatlabExporter me = new MatlabExporter();
//			me.add("input", inputRecorder.getData());
//			me.add("integrator", integratorRecorder.getData(), .01f);
//			me.add("neuron", neuronRecorder.getData());
//			me.write(new File("export.mat"));
//
//		} catch (SimulationException e) {
//			e.printStackTrace();
//		} catch (StructuralException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//}
