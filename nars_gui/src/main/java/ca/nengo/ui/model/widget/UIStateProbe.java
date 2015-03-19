/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIStateProbe.java". Description: 
"UI Wrapper for a Simulator Probe
  
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

import ca.nengo.model.*;
import ca.nengo.ui.data.ProbePlotHelper;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.object.activity.TrackedAction;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.menu.MenuBuilder;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINodeViewable;
import ca.nengo.ui.model.tooltip.TooltipBuilder;
import ca.nengo.ui.model.viewer.GroupViewer;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.Probe;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;

/**
 * UI Wrapper for a Simulator Probe
 * 
 * @author Shu Wu
 */
public class UIStateProbe extends UIProbe {

	/**
	 * 
	 */
	private static Probe createProbe(UINeoNode nodeAttachedTo, String state)
			throws SimulationException {

		/*
		 * Creates the probe
		 */
		Node node = nodeAttachedTo.node();
		Probe probe;
		try {
			if (nodeAttachedTo.getParentViewer() instanceof GroupViewer) {
				NodeViewer groupViewer = nodeAttachedTo.getParentViewer();

				UINodeViewable ensemble = groupViewer.getViewerParent();
				Network network = ensemble.getNetworkParent().node();

				probe = network.getSimulator().addProbe(ensemble.name(),
						(Probeable) node,
						state,
						true);

			} else if (nodeAttachedTo.getNetworkParent() != null) {
				probe = nodeAttachedTo.getNetworkParent().getSimulator().addProbe(node.name(),
						state,
						true);
			} else {
				throw new SimulationException(
						"Cannot add a probe to a node that is not inside a Network");
			}
			
			nodeAttachedTo.showPopupMessage("Probe (" + state + ") added");

		} catch (SimulationException exception) {
			// nodeAttachedTo.popupTransientMsg("Could not add Probe (" + state
			// + ") added to Simulator");
			throw exception;
		}
		return probe;
	}

	public UIStateProbe(UINeoNode nodeAttachedTo, Probe probeModel) {
		super(nodeAttachedTo, probeModel);
	}

	public UIStateProbe(UINeoNode nodeAttachedTo, String state) throws SimulationException {
		super(nodeAttachedTo, createProbe(nodeAttachedTo, state));
	}

	@Override
	protected void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);

		menu.addSection("Probe");
		MenuBuilder plotMenu = menu.addSubMenu("plot");

		Collection<StandardAction> actions = ProbePlotHelper.getInstance().getPlotActions(node().getData(),
				name());

		for (StandardAction action : actions) {
			plotMenu.addAction(action);
		}

		MenuBuilder exportMenu = menu.addSubMenu("export data");
		exportMenu.addAction(new ExportToMatlabAction());

	}

	@Override
	public void doubleClicked() {
		ProbePlotHelper.getInstance().getDefaultAction(node(), name()).doAction();
	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);
		tooltips.addProperty("Attached to", node().getStateName());
	}

	@Override
	protected void prepareToDestroyModel() {
		try {
			getProbeParent().getNetworkParent().getSimulator().removeProbe(node());
			getProbeParent().showPopupMessage("Probe removed from Simulator");
			
			Probe model = node();
			if(model.isInEnsemble()){
				Group target = (Group) model.getTarget();
				target.stopProbing(model.getStateName());
			}
		} catch (SimulationException e) {
			UserMessages.showError("Could not remove probe: " + e.getMessage());
		}

		super.prepareToDestroyModel();
	}

	/**
	 * @param name
	 *            prefix of the fileName to be exported to
	 * @throws IOException
	 */
	public void exportToMatlab(String name) {
//		MatlabExporter me = new MatlabExporter();
//		me.add(getName(), getModel().getData());
//		try {
//			me.write(new File(name + ".mat"));
//		} catch (IOException e) {
//			UserMessages.showError("Could not export file: " + e.toString());
//		}
	}

	@Override
	public Probe node() {
		return (Probe) super.node();
	}

	@Override
	public String getTypeName() {
		return "State Probe";
	}

	@Override
	public void modelUpdated() {
		setName(node().getStateName());
	}

	/**
	 * Action for exporting to MatLab
	 * 
	 * @author Shu Wu
	 */
	class ExportToMatlabAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		String name;

		public ExportToMatlabAction() {
			super("Matlab");
		}

		@Override
		protected void action() throws ActionException {

			name = JOptionPane.showInputDialog("Enter name of file to export to: ");

			(new TrackedAction("Exporting to matlab") {

				private static final long serialVersionUID = 1L;

				@Override
				protected void action() throws ActionException {
					exportToMatlab(name);
				}

			}).doAction();

		}
	}
}
