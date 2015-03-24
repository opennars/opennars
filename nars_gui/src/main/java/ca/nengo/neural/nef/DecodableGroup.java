/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DecodableEnsemble.java". Description:
"An Ensemble that produces output signals that mean something when taken together"

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
 * Created on 20-Feb-07
 */
package ca.nengo.neural.nef;

import ca.nengo.math.Function;
import ca.nengo.model.*;
import ca.nengo.neural.nef.impl.DecodedSource;
import ca.nengo.neural.nef.impl.DecodedTarget;
import ca.nengo.neural.plasticity.PlasticGroup;
import ca.nengo.util.Probe;

/**
 * <p>An Ensemble that produces output signals that mean something when taken together. This meaning
 * can be decoded, as a scalar or vector, through linear combination of the outputs.</p>
 *
 * <p>Note that NEFEnsemble is a paricularly powerful and efficient special case of DecodableEnsemble.
 * However NEFEnsemble makes some assumptions that can be relaxed by using DecodableEnsemble instead:
 * <ol><li>It assumes that its Nodes can run in the SimulationMode CONSTANT_RATE</li>
 * <li>It assumes that activity arises from cosine-tuning to preferred input vectors (all Nodes must be
 * NEFNodes for this rule).</li></ol>
 * </p>
 *
 * @author Bryan Tripp
 */
public interface DecodableGroup extends PlasticGroup, Probeable {

	/**
	 * Adds an Origin that corresponds to a decoding of the activities of Nodes in this Ensemble. The decoding
	 * is found by running the Ensemble within a Network, and using its output to approximate a vector function
	 * of time.
	 *
	 * @param name Name of decoding
	 * @param functions 1D Functions of time which represent the meaning of the Ensemble output when it runs
	 * 		in the Network provided (see environment arg)
	 * @param nodeOrigin The name of the Node-level Origin to decode
	 * @param environment A Network in which the Ensemble runs (may include inputs, feedback, etc)
	 * @param probe A Probe that is connected to the named Node-level Origin
	 * @param startTime Simulation time at which to start
	 * @param endTime Simulation time at which to finish
	 * @return An Origin that approximates the given Functions as a linear combination of output from the given
	 * 		nodeOrigin
	 * @throws StructuralException May arise in instantiating the Origin
	 * @throws SimulationException If there is a problem running the simulation
	 */
	public NSource addDecodedOrigin(String name, Function[] functions, String nodeOrigin, Network environment,
			Probe probe, float startTime, float endTime) throws StructuralException, SimulationException;

	/**
	 * Adds an Origin that corresponds to a decoding of the activities of Nodes in this Ensemble. The decoding
	 * is found by running the Ensemble repeatedly with different inputs, and using the steady-state output
	 * for each input to approximate a vector function of the input. Input is applied to a caller-defined
	 * Termination which may or may not be directly onto the Ensemble.
	 *
	 * @param name Name of decoding
	 * @param functions Functions of input that represent the meaning of Ensemble output when it runs in the
	 * 		Network provided (see environment arg)
	 * @param nodeOrigin The name of the Node-level Origin to decode
	 * @param environment A Network in which the Ensemble runs (may include inputs, feedback, etc)
	 * @param probe A Probe that is connected to the named Node-level Origin
	 * @param target The Termination through which input is to be applied to the Ensemble
	 * @param evalPoints The set of vector inputs that are to be applied at the above Termination
	 * @param transientTime The amount of time the Network is to run with each input, so that transients die away
	 * 		(output is averaged over the last 10% of each simulation)
	 * @return An Origin that approximates the given Functions as a linear combination of output from the given
	 * 		nodeOrigin
	 * @throws StructuralException May arise in instantiating the Origin
	 * @throws SimulationException If there is a problem running the simulations
	 */
	public NSource addDecodedOrigin(String name, Function[] functions, String nodeOrigin, Network environment,
			Probe probe, NTarget target, float[][] evalPoints, float transientTime) throws StructuralException, SimulationException;

	/**
	 * This method can optionally be called after all decoded Origins have been added, in order to free
	 * resources that are needed for adding new decodings.
	 */
	public void doneOrigins();

	/**
	 * @param name Name of an existing decoding to remove
	 * @return The removed DecodedOrigin
	 * @throws StructuralException if DecodedOrigin doesn't exist
	 */
	public DecodedSource removeDecodedOrigin(String name) throws StructuralException;

	/**
     * @param name Name of an existing termination to remove
	 * @return The removed DecodedTermination
	 * @throws StructuralException if DecodedTermination doesn't exist
     */
    public DecodedTarget removeDecodedTermination(String name) throws StructuralException;

}
