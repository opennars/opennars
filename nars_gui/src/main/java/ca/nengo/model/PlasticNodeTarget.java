/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PlasticTermination.java". Description:
"Plasticity rules apply to terminations. This interface describes the methods
that a plastic termination must implement."

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
 * Created on May 18, 2006
 */
package ca.nengo.model;

/**
 * Plastic terminations can be ensemble level or node level.
 *
 * This interface describes the methods that a plastic node
 * termination must implement.
 *
 * @author Trevor Bekolay
 */
public interface PlasticNodeTarget extends NTarget<InstantaneousOutput> {

    /**
     * @return List of synaptic weights for each input channel
     */
    public float[] getWeights();

    /**
     * @param weights The new synaptic weights for each input channel
     * @param save Should the weights be saved for resetting purposes?
     */
    public void setWeights(float[] weights, boolean save);
    
    /**
     * @param change The change in the synaptic weights for each input channel
     * @param save Should the weights be saved for resetting purposes?
     */
    public void modifyWeights(float[] change, boolean save);

    /**
     * Save the current state of the weights so it can be reset there
     */
    public void saveWeights();

    /**
     * @return The most recent input to the Termination
     */
    public InstantaneousOutput get();

    /**
     * @return The most recent output of the Termination (after summation and dynamics)
     */
    public float getOutput();

    public PlasticNodeTarget clone() throws CloneNotSupportedException;
    
    public PlasticNodeTarget clone(Node node) throws CloneNotSupportedException;

}
