/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIOrigin.java". Description: 
"@author Shu"

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

import ca.nengo.model.*;
import ca.nengo.neural.nef.impl.DecodedSource;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.tooltip.TooltipBuilder;

import java.awt.*;

/**
 * UI Wrapper for an Origin
 * 
 * @author Shu Wu
 * 
 */
/**
 * @author Shu
 */
public abstract class UISource extends Widget {

	/**
	 * Factory method for creating a UI Wrapper around a origin
	 * 
	 * @param uiNodeParent
	 *            UINeoNode to attach the UITermination object to the right
	 *            parent.
	 * @param source
	 * @return UI Origin Wrapper
	 */
	public static UISource createOriginUI(UINeoNode uiNodeParent, NSource source) {

		if (source instanceof DecodedSource) {
			return new UIDecodedSource(uiNodeParent, (DecodedSource) source);
		} else {
			return new UIGenericSource(uiNodeParent, source);
		}
	}

	private static final String typeName = "Source";

	private boolean isExposed = false;

	private UIProjectionWell lineWell;

	private Color lineWellDefaultColor;

	protected UISource(UINeoNode nodeParent, NSource source) {
		super(nodeParent, source);

		init();
	}

	private void init() {
		lineWell = new UIProjectionWell(this);
		lineWellDefaultColor = lineWell.getColor();
		ModelIcon icon = new ModelIcon(this, lineWell);
		icon.configureLabel(false);
        icon.getLabel().setFont(NengoStyle.FONT_TINY);

		setIcon(icon);

		attachViewToModel();
	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);

		tooltips.addProperty("Dimensions", String.valueOf(node().getDimensions()));
//
//		//try {
//			Object value = getModel().get();
//            if (value!=null)
//                tooltips.addProperty("Value: ", value.toString());
//
//			//tooltips.addProperty("Time: ", String.valueOf(value.getTime()));
//			//tooltips.addProperty("Units: ", String.valueOf(value.getUnits()));
//
//		/*} catch (SimulationException e) {
//		}*/

	}

	/**
	 * Destroys the Origin model
	 */
	protected abstract void destroyOriginModel();

	@Override
	protected void exposeModel(UINetwork networkUI, String exposedName) {
		networkUI.node().exposeOrigin(node(), exposedName);
		networkUI.showSource(exposedName);
	}

	@Override
	protected String getExposedName(Network network) {
		return network.getExposedOriginName(node());
	}

	@Override
	protected String getModelName() {
		return node().getName();
	}

	@Override
	protected final void prepareToDestroyModel() {
		destroyOriginModel();
		super.prepareToDestroyModel();
	}

	@Override
	protected void unExpose(Network network) {

		try
		{
			String exposedName = getExposedName();
			if (exposedName != null)
			{
				//remove the origin from UI
				getNodeParent().getNetworkParent().hideSource(exposedName);
				
				//remove the origin from actual network
				network.hideOrigin(exposedName);
			}
			else
				UserMessages.showWarning("Could not unexpose this origin");
		}
		catch(StructuralException se)
		{
			UserMessages.showWarning("Could not unexpose this origin");
		}
				
		
	}

	/**
	 * Connect to a Termination
	 * 
	 * @param term
	 *            Termination to connect to
	 */
	public void connectTo(UITarget term) {
		connectTo(term, true);
	}

	/**
	 * @param term
	 *            Termination to connect to
	 * @param modifyModel
	 *            if true, the Network model will be updated to reflect this
	 *            connection
	 */
	public void connectTo(UITarget term, boolean modifyModel) {
		/*
		 * Check if we're already connection to that termination
		 */
		if (term.getConnector() != null && term.getConnector().getOriginUI() == this) {
			return;
		}

		UIProjection lineEnd = lineWell.createProjection();

		if (!lineEnd.tryConnectTo(term, modifyModel)) {
			UserMessages.showWarning("Could not connect");
		}
	}

	public Color getColor() {
		return lineWell.getColor();
	}

    public void setColor(Color c) {
        lineWell.setColor(c);
    }

	@Override
	public NSource node() {
		return (NSource) super.node();
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public void setExposed(boolean isExposed) {
		if (this.isExposed == isExposed) {
			return;
		}
		this.isExposed = isExposed;

		if (isExposed) {
			lineWell.setColor(Widget.EXPOSED_COLOR);
		} else {
			lineWell.setColor(lineWellDefaultColor);
		}

	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);

		lineWell.setVisible(isVisible);
	}
}

class UIGenericSource extends UISource {

	protected UIGenericSource(UINeoNode nodeParent, NSource source) {
		super(nodeParent, source);
	}

	@Override
	protected boolean showRemoveModelAction() {
		return false;
	}

	@Override
	protected void destroyOriginModel() {
		/*
		 * Do nothing
		 */
	}

}