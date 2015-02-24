/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NEFNode.java". Description: 
"A Node with a distinguished Termination that corresponds to a net  
  effect of input"

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
package ca.nengo.model.nef;

import ca.nengo.model.Node;

/**
 * <p>A Node with a distinguished Termination that corresponds to a net  
 * effect of input. This direct channel is used by NEFEnsembles. NEFEnsembles  
 * run more efficiently by combining and filtering inputs at the Ensemble  
 * level, so that these same combinations and filterings need not be performed  
 * multiple times, for each Node in the Ensemble. Differences in net input to 
 * the different Nodes in an NEFEnsemble are accounted for by encoding vectors
 * (see Eliasmith & Anderson, 2003).</p> 
 * 
 * <p>There can also be additional inputs to an NEFNode, beyond the distinguished  
 * input. The manner in which such inputs are combined with each other 
 * and with the distinguished input is determined by the NEFNode. </p>
 * 
 * @author Bryan Tripp
 */
public interface NEFNode extends Node {

	/**
	 * @param value Value of filtered summary input. This value is typically in the range 
	 * 		[-1 1], and correponds to an inner product of vectors in the space  
	 * 		represented by the NEFEnsemble to which this Node belongs. 
	 */
	public void setRadialInput(float value);
	
}
