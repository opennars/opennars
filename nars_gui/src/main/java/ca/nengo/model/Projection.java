/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Projection.java". Description:
"A connection between an Origin and a Termination"

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
 * Created on May 5, 2006
 */
package ca.nengo.model;

import ca.nengo.util.ScriptGenException;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A connection between an Origin and a Termination.
 *
 * @author Bryan Tripp
 */
public interface Projection extends Serializable {

	/**
	 * @return Origin of this Projection (where information comes from)
	 */
	public NSource getSource();

	/**
	 * @return Termination of this Projection (where information goes)
	 */
	public NTarget getTarget();

	/**
	 * @return The Network to which this Projection belongs
	 */
	public Network getNetwork();

	/**
	 * Makes all the synaptic weights in the projection either positive or negative, so that the projection
	 * accords with Dale's principle. This introduces a bias current postsynaptically, which is a function
	 * of presynaptic activity. This bias is removed by projecting the same function through an ensemble
	 * of interneurons. See Parisien, Anderson & Eliasmith, 2007, Neural Computation for more detail.
	 *
	 * @param numInterneurons Number of interneurons through which bias function is projected
	 * @param tauInterneurons Time constant of post-synaptic current in projection from presynaptic ensemble to interneurons (typically short)
	 * @param tauBias Time constant of post-synaptic current in projection from interneurons to postsynaptic ensemble
	 * @param excitatory If true, synapses in main projection are made excitatory; if false, inhibitory
	 * @param optimize If true, performs optimizations to minimize distortion in the parallel projection through interneurons
	 * @throws StructuralException if bias can't be added
	 */
	public void addBias(int numInterneurons, float tauInterneurons, float tauBias, boolean excitatory, boolean optimize) throws StructuralException;

	/**
	 * Deletes bias-related interneurons, projections, origins, and terminations.
	 */
	public void removeBias();

	/**
	 * @param enable If true, and initializeBias(...) has been called, then bias is enabled; if false it is disabled (default true)
	 */
	public void enableBias(boolean enable);

	/**
	 * @return true if bias is enabled
	 */
	public boolean biasIsEnabled();

	/**
	 * @return Matrix of weights in this Projection (if there are neurons on each end, then these are synaptic weights)
	 */
	public float[][] getWeights();
	
	
	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException;
}
