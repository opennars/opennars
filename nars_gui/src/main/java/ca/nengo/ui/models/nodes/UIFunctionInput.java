/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIFunctionInput.java". Description: 
"UI Wrapper of FunctionInput
  
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

package ca.nengo.ui.models.nodes;

import ca.nengo.model.impl.FunctionInput;
import ca.nengo.ui.actions.PlotFunctionNodeAction;
import ca.nengo.ui.lib.util.menus.PopupMenuBuilder;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.icons.FunctionInputIcon;
import ca.nengo.ui.models.tooltips.TooltipBuilder;

/**
 * UI Wrapper of FunctionInput
 * 
 * @author Shu Wu
 */
public class UIFunctionInput extends UINeoNode {

	public static final String typeName = "Function Input";

	public UIFunctionInput(FunctionInput model) {
		super(model);
		init();
	}

	private void init() {
		setIcon(new FunctionInputIcon(this));
		showAllOrigins();
	}

	@Override
	protected void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);
		// MenuBuilder plotMenu = menu.createSubMenu("Plot");
		menu.addSection("Function");

		menu.addAction(new PlotFunctionNodeAction(getName(), "Plot function", getModel()));
	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);

		tooltips.addProperty("Dimensions", String.valueOf(getModel().getFunctions().length));
	}

	@Override
	public FunctionInput getModel() {

		return (FunctionInput) super.getModel();
	}

	@Override
	public String getTypeName() {
		return typeName;
	}
}
