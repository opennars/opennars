/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIProjectionWell.java". Description: 
"LineEndWell for this origin
  
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

import ca.nengo.ui.lib.object.line.LineWell;
import ca.nengo.ui.lib.object.model.ModelObject.ModelListener;

/**
 * LineEndWell for this origin
 * 
 * @author Shu Wu
 */
public class UIProjectionWell extends LineWell {

	private final UISource myOrigin;

	public UIProjectionWell(UISource myOrigin) {
		super();
		this.myOrigin = myOrigin;
	}

	/**
	 * @return new LineEnd created
	 */
	public UIProjection createProjection() {
		UIProjection projection = new UIProjection(this);
		addChild(projection);

		final RemoveProjectionListener removeProjectionListener = new RemoveProjectionListener(
                projection);
		myOrigin.addModelListener(removeProjectionListener);

		/*
		 * Remove the listener when the projection is destroyed
		 */
		projection.addPropertyChangeListener(Property.REMOVED_FROM_WORLD, new Listener() {

			public void propertyChanged(Property event) {
				myOrigin.removeModelListener(removeProjectionListener);
			}
		});

		return projection;
	}

	static class RemoveProjectionListener implements ModelListener {
		private final UIProjection projection;

		public RemoveProjectionListener(UIProjection projection) {
			super();
			this.projection = projection;
		}

		public void modelDestroyed(Object model) {
			projection.disconnectFromTermination();
			projection.destroy();
		}

		public void modelDestroyStarted(Object model) {
			// Do nothing
		}

	}

	protected UISource getOriginUI() {
		return myOrigin;
	}
}
