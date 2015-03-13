/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NetworkViewerMemoryTest.java". Description: 
"Just a quick check for one type of memory leaks in Network Viewer.
  
  @author Shu"

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
import ca.nengo.model.Network;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.model.node.UINetwork;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Just a quick check for one type of memory leaks in Network Viewer.
 * 
 * @author Shu
 */
public class NetworkViewerMemoryTest {
	private static int i;
	private static AbstractNengo neoGraphics;
	// private static NetworkViewer netView;
	private static final int NUM_OF_LOOPS = 500;
//	private static Window window;

	// private static Window[] windows;
	public static Network createNetwork() throws StructuralException {

		Network network = new DefaultNetwork<>();

		Function f = new ConstantFunction(1, 1f);
		// Function f = new SineFunction();
		FunctionInput input = new FunctionInput("input", new Function[] { f }, Units.UNK);
		network.addNode(input);

		NEFGroupFactory ef = new NEFGroupFactoryImpl();

		NEFGroup integrator = ef.make("integrator", 500, 1, "integrator1", false);
		network.addNode(integrator);
		integrator.collectSpikes(true);

		// Plotter.plot(integrator);
		// Plotter.plot(integrator, NEFEnsemble.X);

		float tau = .05f;

		NTarget interm = integrator.addDecodedTermination("input",
				new float[][] { new float[] { tau } }, tau, false);
		// Termination interm = integrator.addDecodedTermination("input", new
		// float[][]{new float[]{1f}}, tau);
		network.addProjection(input.getSource(FunctionInput.ORIGIN_NAME), interm);

		NTarget fbterm = integrator.addDecodedTermination("feedback",
				new float[][] { new float[] { 1f } }, tau, false);
		network.addProjection(integrator.getSource(NEFGroup.X), fbterm);

		// System.out.println("Network creation: " + (System.currentTimeMillis()
		// - start));
		return network;
	}

	static UINetwork network;

	public static long getApproximateUsedMemory() {
		System.gc();
		System.runFinalization();
		long totalMemory = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		return totalMemory - free;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		printMemoryUsed("Start");
		neoGraphics = new AbstractNengo();

		// Window windows = new Window[NUM_OF_LOOPS];
		for (i = 0; i < NUM_OF_LOOPS; i++) {

			try {

				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {

						try {
							network = new UINetwork(createNetwork());
							neoGraphics.getWorld().getGround().addChild(network);
							network.openViewer();
							// network.openViewer();
							//							
							// netView = new NetworkViewer(network);
							//							
							// window = new Window(
							// .getGround(), network);
						} catch (StructuralException e) {
							e.printStackTrace();
						}

					}
				});
				Thread.sleep(2000);

				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						network.destroy();
					}
				});

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			}
			printMemoryUsed("Loop # " + i);
		}

	}

	public static void printMemoryUsed(String msg) {
		System.out.println("*** " + msg + " ***");
		System.out.println("Approximate used memory: " + getApproximateUsedMemory() / 1024 + " k");
	}

}
