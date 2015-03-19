/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UINeoModel.java". Description: 
"UI Wrapper for a NEO Node Model
  
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

package ca.nengo.ui.model;

import ca.nengo.ui.action.PropertiesAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.object.model.ModelObject;

/**
 * UI Wrapper for a NEO Node Model
 * 
 * @author Shu
 */
public abstract class UINeoModel<M> extends ModelObject<M> {
	public UINeoModel(M model) {
		super(model);
	}

	protected final void afterModelCreated() {
		/*
		 * Remove this funciton after
		 */
	}

	@Override
	protected void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);
		menu.addAction(new PropertiesAction("Properties", node()));
	}

	@Override
	public void altClicked() {
		(new PropertiesAction("Inspector", node())).doAction();
	}
}
