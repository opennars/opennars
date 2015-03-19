/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "Widget.java". Description: 
"Widgets are models such as Terminations and Origins which can be attached to
  a PNeoNode
  
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

import ca.nengo.model.Network;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.ReversableAction;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.action.UserCancelledException;
import ca.nengo.ui.lib.menu.AbstractMenuBuilder;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.model.UINeoModel;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.tooltip.TooltipBuilder;
import org.piccolo2d.nodes.PText;

import javax.swing.*;
import java.awt.*;

/**
 * Widgets are models such as Terminations and Origins which can be attached to
 * a PNeoNode
 * 
 * @author Shu Wu
 */
public abstract class Widget<M> extends UINeoModel<M> {
	public static final Color EXPOSED_COLOR = Color.yellow;

	private boolean isWidgetVisible = true;
	private ExposedIcon myExposedIcon;

	private UINeoNode parent;

	public Widget(UINeoNode nodeParent, M model) {
		super(model);
		init(nodeParent);
	}

	private void init(final UINeoNode nodeParent) {
		setSelectable(false);

		// Invoke later so that the node parent is set
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getExposedName() != null) {
					setExposed(true);
				} else {
					setExposed(false);
				}
			}
		});

		this.parent = nodeParent;
	}

	protected abstract void setExposed(boolean isExposed);

	@Override
	protected void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);

		if (isWidgetVisible()) {
			menu.addAction(new HideWidgetAction("Hide"));
		} else {
			menu.addAction(new ShowWidgetAction("Show"));
		}

		menu.addSection(getTypeName());

		if (getExposedName() == null) {
			menu.addAction(new ExposeAction());
		} else {
			menu.addAction(new UnExposeAction());
		}
		constructWidgetMenu(menu);
	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);
		tooltips.addProperty("Attached to", parent.name());
		if (getExposedName() != null) {
			tooltips.addProperty("Exposed as", getExposedName());
		}
	}

	/**
	 * Constructs widget-specific menu
	 * 
	 * @param menu
	 */
	protected void constructWidgetMenu(AbstractMenuBuilder menu) {

	}

	protected abstract void exposeModel(UINetwork networkUI, String exposedName);

	/**
	 * Exposes this origin/termination outside the Network
	 * 
	 * @param exposedName
	 *            Name of the newly exposed origin/termination
	 */
	protected void expose(String exposedName) {
		UINetwork networkUI = getNodeParent().getNetworkParent();

		if (networkUI != null) {
			exposeModel(networkUI, exposedName);

			showPopupMessage(this.name() + " is exposed as " + exposedName + " on Network: "
					+ networkUI.name());

		} else {
			UserMessages.showWarning("Cannot expose because no external network is available");
		}
	}

	protected String getExposedName() {
		if (getNodeParent() != null && getNodeParent().getNetworkParent() != null) {
			Network network = getNodeParent().getNetworkParent().node();
			if (network != null) {
				String exposedName = getExposedName(network);
				if (exposedName != null) {
					return exposedName;
				}
			}
		}
		return null;
	}

	protected abstract String getExposedName(Network network);

	protected abstract String getModelName();

	/**
	 * UnExposes this origin/termination outside the Network
	 */
	protected void unExpose() {
		UINetwork networkUI = getNodeParent().getNetworkParent();
		Network network = networkUI.node();
		if (network != null) {
			unExpose(network);
			showPopupMessage(name() + " is UN-exposed on Network: " + network.name());
		} else {
			UserMessages.showWarning("Cannot expose because no external network is available");
		}
	}

	protected abstract void unExpose(Network network);

	public abstract Color getColor();

	public UINeoNode getNodeParent() {
		return parent;
	}

	/**
	 * @return Whether this widget is visible on the parent
	 */
	public boolean isWidgetVisible() {
		return isWidgetVisible;
	}

	@Override
	public void modelUpdated() {

		String name = getModelName();

		String exposedName = getExposedName();
		if (exposedName != null) {
			if (myExposedIcon == null) {
				myExposedIcon = new ExposedIcon(getColor());
                getPNode().addChild(myExposedIcon);
				myExposedIcon.setOffset(getWidth() + 2,
						(getHeight() - myExposedIcon.getHeight()) / 2);
			}

		} else {
			if (myExposedIcon != null) {
				myExposedIcon.removeFromParent();
			}
		}
		setName(name);

	}

	/**
	 * @param isVisible
	 *            Whether the user has marked this widget as hidden
	 */
	public void setWidgetVisible(boolean isVisible) {
		this.isWidgetVisible = isVisible;

		firePropertyChange(Property.WIDGET);

		setVisible(isVisible);
        getPNode().invalidateFullBounds();
	}

	class ExposeAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public ExposeAction() {
			super("Expose outside Network");
		}

		@Override
		protected void action() throws ActionException {

			String name = JOptionPane.showInputDialog(UIEnvironment.getInstance(),
					"Please enter the name to expose this as: ");

			if (name != null && name.compareTo("") != 0) {
				expose(name);
			} else {
				throw new UserCancelledException();
			}

		}
	}

	/**
	 * Action for hiding this widget
	 * 
	 * @author Shu Wu
	 */
	class HideWidgetAction extends ReversableAction {

		private static final long serialVersionUID = 1L;

		public HideWidgetAction(String actionName) {
			super("Hiding " + getTypeName(), actionName);
		}

		@Override
		protected void action() throws ActionException {
			setWidgetVisible(false);
		}

		@Override
		protected void undo() throws ActionException {
			setWidgetVisible(true);

		}
	}

	/**
	 * Action for showing this widget
	 * 
	 * @author Shu Wu
	 */
	class ShowWidgetAction extends ReversableAction {

		private static final long serialVersionUID = 1L;

		public ShowWidgetAction(String actionName) {
			super("Showing " + getTypeName(), actionName);
		}

		@Override
		protected void action() throws ActionException {
			setWidgetVisible(true);
		}

		@Override
		protected void undo() throws ActionException {
			setWidgetVisible(false);
		}
	}

	class UnExposeAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public UnExposeAction() {
			super("Un-expose as " + getExposedName());
		}

		@Override
		protected void action() throws ActionException {
			unExpose();
		}
	}
}

class ExposedIcon extends PText {

	private static final long serialVersionUID = 1L;

	public ExposedIcon(Color color) {
		super("E");

		setTextPaint(color);
		setFont(NengoStyle.FONT_XLARGE);
	}

}
