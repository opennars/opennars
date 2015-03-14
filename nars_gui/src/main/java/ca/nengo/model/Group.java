/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Ensemble.java". Description:
"A group of Nodes with largely overlapping inputs and outputs.



  There are no strict rules for how to group Nodes into Ensembles, but here are
  some things to consider:


  A group of Nodes that together 'represent' something through a
  population code should be modelled as an Ensemble"

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
 * Created on May 16, 2006
 */
package ca.nengo.model;

import ca.nengo.util.SpikePattern;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * A group of Nodes with largely overlapping inputs and outputs.
 * </p>
 *
 * <p>
 * There are no strict rules for how to group Nodes into Ensembles, but here are
 * some things to consider:
 *
 * <ul>
 * <li>A group of Nodes that together 'represent' something through a
 * population code should be modelled as an Ensemble. (Also consider using
 * NEFEnsemble to make such representation explicit.) </li>
 *
 * <li>Making ensembles that correspond to physical structures (e.g. nuclei)
 * and naming them appropriately will make the model clearer.</li>
 *
 * <li>Outputs from an Ensemble are grouped together and passed to other
 * Ensembles during a simulation, and practical issues may arise from this. For
 * example, putting all your Nodes in a single large ensemble could result in a
 * very large matrix of synaptic weights, which would impair performance. </li>
 * </ul>
 * </p>
 *
 * <p>
 * The membership of an Ensemble is fixed once the Ensemble is created. This
 * means that the Ensemble model doesn't deal explicitly with growth and death
 * of components during simulation (although you can set input/output weights to
 * zero to mimic this). It also means that an Ensemble isn't a good model of a
 * functional "assembly".
 * </p>
 *
 * @author Bryan Tripp
 */
public interface Group<N extends Node> extends Node<N> {


	/**
	 * This method provides a means of efficiently storing the output of an
	 * Ensemble if the component Nodes have Origins that produce SpikeOutput.
	 *
	 * @return A SpikePattern containing a record of spikes, provided
	 *         collectSpikes(boolean) has been set to true
	 */
	public SpikePattern getSpikePattern();

	/**
	 * @param collect
	 *            If true, the spike pattern is recorded in subsequent runs and
	 *            is available through getSpikePattern() (defaults to false)
	 */
	public void collectSpikes(boolean collect);

	/**
	 *
	 * @return true if the spike pattern will be recorded in subsequent runs
	 */
	public boolean isCollectingSpikes();

	/**
	 * Replaces the set of nodes inside the Ensemble
	 * @param nodes New nodes to use
	 */
	public void redefineNodes(Node[] nodes);
	
	public void stopProbing(String stateName);

    default public Iterable<? extends Node> nodes() {
        Node[] t = getNodes();
        if (t.length == 0) return Collections.emptyList();
        return Arrays.asList(t);
    }

}
