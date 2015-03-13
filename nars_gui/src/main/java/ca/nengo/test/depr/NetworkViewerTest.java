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
import ca.nengo.model.Network;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.action.RunSimulatorAction;
import ca.nengo.ui.model.node.UINetwork;

/**
 * Starts Nengo with a network viewer open
 * 
 * @author Shu Wu
 */
public class NetworkViewerTest extends ExampleRunner {

	public static void main(String[] args) {

		try {
			new NetworkViewerTest();
		} catch (StructuralException e) {
			e.printStackTrace();
		}
	}

	public NetworkViewerTest() throws StructuralException {
		super();
	}

    @Override
    public Network getNetwork() {
        try {
            return CreateNetwork();
        } catch (StructuralException e) {
            throw new RuntimeException(e);
        }
    }

    public static Network CreateNetwork() throws StructuralException {
		NetworkImpl network = new DefaultNetwork<>();
		
		Function f = new ConstantFunction(1, 1f);
		FunctionInput input = new FunctionInput("input", new Function[] { f }, Units.UNK);
		
		network .addNode(input);
		
		return network;
	}

	@Override
	protected void doStuff(UINetwork network) {
		(new RunSimulatorAction("Run", network, 0f, 1f, 0.002f)).doAction();


		
	}

}