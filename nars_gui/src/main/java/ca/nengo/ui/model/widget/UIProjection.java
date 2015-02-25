/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIProjection.java". Description: 
"Line Ends for this origin
  
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

package ca.nengo.ui.model.widget;

import ca.nengo.ui.lib.object.line.ILineTermination;
import ca.nengo.ui.lib.object.line.LineConnector;
import ca.nengo.ui.lib.world.piccolo.primitive.PXEdge;
import ca.nengo.ui.model.UINeoNode;

/**
 * Line Ends for this origin
 * 
 * @author Shu Wu
 */
public class UIProjection extends LineConnector {

	public UIProjection(UIProjectionWell well) {
		super(well);
	}

	/**
	 * Sets whether the type of connection represented by this Line End is
	 * recursive (if it origin and termination are on the same model).
	 * 
	 * @param isRecursive
	 *            Whether this connection is recurisve
	 */
	private void setRecursive(boolean isRecursive) {
		if (isRecursive) {
			/*
			 * Recursive connections are represented by an upward arcing edge
			 */
			UINeoNode nodeParent = getOriginUI().getNodeParent();
			getEdge().setLineShape(PXEdge.EdgeShape.UPWARD_ARC);
			getEdge().setMinArcRadius(nodeParent.getBounds().getWidth());
			setPointerVisible(false);
		} else {
			getEdge().setLineShape(PXEdge.EdgeShape.STRAIGHT);
			setPointerVisible(true);
		}

	}

	@Override
	protected void disconnectFromTermination() {
		if (getTermination() != null) {
			setRecursive(false);
			getTermination().disconnect();
		}
	}

	@Override
	protected boolean initTarget(ILineTermination target, boolean modifyModel) {
		if (!(target instanceof UITarget)) {
			return false;
		}

		UITarget term = ((UITarget) target);

		if (term.isModelBusy()) {
			return false;
		}

		if (term.connect(getOriginUI(), modifyModel)) {
			if (term.getNodeParent() == getOriginUI().getNodeParent()) {
				setRecursive(true);
			}

			return true;
		} else {
			return false;
		}
	}

	public UISource getOriginUI() {
		return ((UIProjectionWell) getWell()).getOriginUI();
	}

	@Override
	public UITarget getTermination() {
		return (UITarget) super.getTermination();
	}

}
