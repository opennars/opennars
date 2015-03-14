/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "IntegratorExample.java". Description: 
"In this example, an Integrator network is constructed
  
  @author Shu Wu"

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
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.model.NTarget;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.util.Probe;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * In this example, an Integrator network is constructed
 * 
 * @author Shu Wu
 */
public class IntegratorExample {
	public static final float tau = .05f;

	private UINetwork network;

	public void createUINetwork(AbstractNengo abstractNengo) {

		network = new UINetwork(new DefaultNetwork());
		abstractNengo.getWorld().getGround().addChild(network);
		network.openViewer();
		//network.getViewer().getGround().setElasticEnabled(true);

		(new Thread(new Runnable() {

			public void run() {
				try {
					buildNetwork(network.node());
				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}

			}

		})).start();

	}

	private void buildNetwork(NetworkImpl network) throws StructuralException,
			InterruptedException, SimulationException {
		network.setName("Integrator");

		Util.debugMsg("Network building started");

		Function f = new ConstantFunction(1, 1f);
		FunctionInput input = new FunctionInput("input", new Function[] { f }, Units.UNK);

		// uiViewer.addNeoNode(uiInput);

		NEFGroupFactory ef = new NEFGroupFactoryImpl();
		NEFGroup integrator = ef.make("integrator", 500, 1, "integrator1", false);
		NTarget interm = integrator.addDecodedTermination("input",
				new float[][] { new float[] { tau } }, tau, false);
		NTarget fbterm = integrator.addDecodedTermination("feedback",
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

		network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), interm);
		Thread.sleep(500);
		network.addProjection(integrator.getSource(NEFGroup.X), fbterm);
		Thread.sleep(500);

		/*
		 * Test removing projections
		 */

		network.removeProjection(interm);
		Thread.sleep(500);
		// add the projection back
		network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), interm);
		Thread.sleep(500);
		/*
		 * Add probes
		 */
		Probe integratorXProbe = network.getSimulator().addProbe("integrator", NEFGroup.X, true);
		Thread.sleep(500);
		/*
		 * Test adding removing probes
		 */
		network.getSimulator().removeProbe(integratorXProbe);
		Thread.sleep(500);
		// add the probe back
		network.getSimulator().addProbe("integrator", NEFGroup.X, true);
		Thread.sleep(500);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				doPostUIStuff();
			}
		});

		Util.debugMsg("Network building finished");

	}

	private void doPostUIStuff() {
		// RunSimulatorAction simulatorRunner = new RunSimulatorAction("Run",
		// network, 0f, 1f, 0.0002f);
		// simulatorRunner.doAction();

		// SwingUtilities.invokeAndWait(new Runnable() {
		// public void run() {
		// // PlotTimeSeries plotAction = new PlotTimeSeries(
		// // integratorProbe.getModel().getData(),
		// // integratorProbe.getName());
		// // plotAction.doAction();
		// }
		// });
		network = null;
	}

	// private UIStateProbe integratorProbe;

	public static void main(String[] args) {
		new IntegratorExample();
	}

	public IntegratorExample() {
		try {
			run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}

	}

	private void run() throws InterruptedException, InvocationTargetException {

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				//try {
					createUINetwork(new AbstractNengo());
                /*
				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}*/
			}
		});

	}

}
