package ca.nengo.ui.test.depr;

import ca.nengo.math.Function;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Target;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.nef.NEFGroup;
import ca.nengo.model.nef.NEFGroupFactory;
import ca.nengo.model.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.util.Probe;


public class TestUtil {
	public static final float tau = .05f;
	public static void buildNetwork(NetworkImpl network) throws StructuralException,
			InterruptedException, SimulationException {
		network.setName("Integrator");

		Util.debugMsg("Network building started");

		Function f = new ConstantFunction(1, 1f);
		FunctionInput input = new FunctionInput("input", new Function[] { f },
				Units.UNK);

		// uiViewer.addNeoNode(uiInput);

		NEFGroupFactory ef = new NEFGroupFactoryImpl();
		NEFGroup integrator = ef.make("integrator", 500, 1, "integrator1",
				false);
		Target interm = integrator.addDecodedTermination("input",
				new float[][] { new float[] { tau } }, tau, false);
		Target fbterm = integrator.addDecodedTermination("feedback",
				new float[][] { new float[] { 1f } }, tau, false);

		network.addNode(integrator);
		Thread.sleep(1000);
		network.addNode(input);
		Thread.sleep(1000);
		// UINEFEnsemble uiIntegrator = new UINEFEnsemble(integrator);
		// uiViewer.addNeoNode(uiIntegrator);
		// uiIntegrator.collectSpikes(true);

		// UITermination uiInterm =
		// uiIntegrator.showTermination(interm.getName());
		// UITermination uiFbterm =
		// uiIntegrator.showTermination(fbterm.getName());

		network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME),
				interm);
		Thread.sleep(500);
		network.addProjection(integrator.getOrigin(NEFGroup.X), fbterm);
		Thread.sleep(500);

		/*
		 * Add probes
		 */
		Probe integratorXProbe = network.getSimulator().addProbe("integrator",
				NEFGroup.X, true);
		Thread.sleep(500);
		/*
		 * Test adding removing probes
		 */
		network.getSimulator().removeProbe(integratorXProbe);
		Thread.sleep(500);
		// add the probe back
		network.getSimulator().addProbe("integrator", NEFGroup.X, true);
		Thread.sleep(500);

		

		Util.debugMsg("Network building finished");

	}
}
