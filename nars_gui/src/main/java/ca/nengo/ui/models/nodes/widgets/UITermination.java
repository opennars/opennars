/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UITermination.java". Description: 
"UI Wrapper for a Termination
  
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

package ca.nengo.ui.models.nodes.widgets;

import ca.nengo.model.Network;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Target;
import ca.nengo.model.nef.impl.DecodedTarget;
import ca.nengo.ui.lib.actions.ActionException;
import ca.nengo.ui.lib.actions.StandardAction;
import ca.nengo.ui.lib.objects.lines.ILineTermination;
import ca.nengo.ui.lib.objects.lines.LineConnector;
import ca.nengo.ui.lib.objects.lines.LineTerminationIcon;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.icons.ModelIcon;
import ca.nengo.ui.models.nodes.UINetwork;
import ca.nengo.ui.models.tooltips.TooltipBuilder;

import java.awt.*;

/**
 * UI Wrapper for a Termination
 * 
 * @author Shu Wu
 */
public abstract class UITermination<T extends Target> extends Widget<T> implements ILineTermination {

	/**
	 * Factory method for creating a UI Wrapper around a termination
	 * 
	 * @param uiNodeParent
	 *            UINeoNode to attach the UITermination object to the right
	 *            parent.
	 * @param target
	 * @return UI Termination Wrapper
	 */
	public static UITermination createTerminationUI(UINeoNode uiNodeParent, Target target) {
	    if (uiNodeParent instanceof UINetwork) {
	        return new UINetworkTermination((UINetwork)uiNodeParent, target);
	    } else if (target instanceof DecodedTarget) {
			return new UIDecodedTermination(uiNodeParent, (DecodedTarget) target);
		} else {
			return new UIGenericTermination(uiNodeParent, target);
		}
	}

	private boolean isExposed = false;

	private LineTerminationIcon myIcon;

	private Color myIconDefaultColor;

	protected UITermination(UINeoNode nodeParent, T term) {
		super(nodeParent, term);
		setName(term.getName());
		init();

	}

	private void init() {
		myIcon = new LineTerminationIcon();
		myIconDefaultColor = myIcon.getColor();
		ModelIcon iconWr = new ModelIcon(this, myIcon);
		iconWr.configureLabel(false);
		
		setSelectable(true);

		setIcon(iconWr);
	}

	/**
	 *            Target to be connected with
	 * @return true is successfully connected
	 */
	protected boolean connect(UIOrigin source, boolean modifyModel) {
		if (getConnector() != null) {
			/*
			 * Cannot connect if already connected
			 */
			return false;
		}

		boolean successful = false;
		if (modifyModel) {

			try {
				if (getNodeParent().getNetworkParent() == null) {
					throw new StructuralException(
							"Can't create projection because termination is not within the scope of a Network");
				}

				getNodeParent().getNetworkParent().getModel().addProjection(source.getModel(),
						getModel());
				getNodeParent().showPopupMessage(
						"NEW Projection to " + getNodeParent().getName() + '.' + getName());
				successful = true;
			} catch (StructuralException e) {
				disconnect();
				UserMessages.showWarning("Could not connect: " + e.getMessage());
			}
		} else {
			successful = true;
		}

		return successful;
	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);

		tooltips.addProperty("Dimensions", String.valueOf(getModel().getDimensions()));

		// tooltips.addTitle("Configuration");
		tooltips.addProperty("Time Constant", String.valueOf(getModel().getTau()));
		tooltips.addProperty("Modulatory", String.valueOf(getModel().getModulatory()));
	}

	/*@Override
	protected void constructWidgetMenu(AbstractMenuBuilder menu) {
		super.constructWidgetMenu(menu);

		if (getConnector() != null) {
			menu.addAction(new DisconnectAction("Disconnect"));
		}
	}*/

	/**
	 * Destroys the termination model
	 */
	protected abstract void destroyTerminationModel();

	@Override
	protected void exposeModel(UINetwork networkUI, String exposedName) {
		networkUI.getModel().exposeTermination(getModel(), exposedName);
		networkUI.showTermination(exposedName);
	}

	@Override
	protected String getExposedName(Network network) {
		return network.getExposedTerminationName(getModel());
	}

	@Override
	protected String getModelName() {
		return getModel().getName();
	}

	@Override
	protected final void prepareToDestroyModel() {
		disconnect();
		destroyTerminationModel();
		super.prepareToDestroyModel();
	}

	@Override
	protected void unExpose(Network network) {
		if (getExposedName() != null) {
			network.hideTermination(getExposedName());
		} else {
			UserMessages.showWarning("Could not unexpose this termination");
		}
	}

	/**
	 *            Termination to be disconnected from
	 * @return True if successful
	 */
	public void disconnect() {
		if (getConnector() != null) {
			try {
				getNodeParent().getNetworkParent().getModel().removeProjection(getModel());
				getNodeParent().showPopupMessage(
						"REMOVED Projection to " + getNodeParent().getName() + '.' + getName());

				getConnector().destroy();
			} catch (StructuralException e) {
				UserMessages.showWarning("Problem trying to disconnect: " + e.toString());
			}
		} else {
			/*
			 * Not connected
			 */
		}
	}

	public Color getColor() {
		return myIcon.getColor();
	}

	public UIProjection getConnector() {
		for (WorldObject wo : getChildren()) {
			if (wo instanceof LineConnector) {
				if (wo instanceof UIProjection) {
					return (UIProjection) wo;
				} else {
					Util.Assert(false, "Unexpected projection type");
				}
			}
		}
		return null;
	}

	@Override
	public String getTypeName() {
		return "Termination";
	}

	/**
	 * @return Termination weights matrix
	 */
	// public float[][] getWeights() {
	// return (float[][])
	// getModel().getConfiguration().getProperty(Termination.WEIGHTS);
	// }
	@Override
	public void setExposed(boolean isExposed) {
		if (this.isExposed == isExposed) {
			return;
		}
		this.isExposed = isExposed;
		if (isExposed) {
			myIcon.setColor(Widget.EXPOSED_COLOR);
			myIcon.moveToFront();
		} else {
			myIcon.setColor(myIconDefaultColor);
			myIcon.moveToBack();
		}
	}

	/**
	 * Action for removing attached connection from the termination
	 * 
	 * @author Shu Wu
	 */
	class DisconnectAction extends StandardAction {
		private static final long serialVersionUID = 1L;

		public DisconnectAction(String actionName) {
			super("Remove connection from Termination", actionName);
		}

		@Override
		protected void action() throws ActionException {
			disconnect();
		}
	}
}

class UIGenericTermination extends UITermination implements ILineTermination {

	protected UIGenericTermination(UINeoNode nodeParent, Target term) {
		super(nodeParent, term);
	}

	@Override
	protected boolean showRemoveModelAction() {
		return false;
	}

	@Override
	protected void destroyTerminationModel() {
		/*
		 * Do nothing
		 */
	}

}
