/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UIEnsemble.java". Description: 
"UI Wrapper for an Ensemble
  
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

package ca.nengo.ui.models.nodes;

import ca.nengo.model.Group;
import ca.nengo.ui.lib.actions.ActionException;
import ca.nengo.ui.lib.actions.ReversableAction;
import ca.nengo.ui.lib.util.menus.AbstractMenuBuilder;
import ca.nengo.ui.models.icons.GroupIcon;
import ca.nengo.ui.models.nodes.widgets.UISpikeProbe;
import ca.nengo.ui.models.viewers.GroupViewer;
import ca.nengo.ui.models.viewers.NodeViewer;

/**
 * UI Wrapper for an Ensemble
 * 
 * @author Shu
 */
public class UIGroup extends UINodeViewable {

	private UISpikeProbe spikeCollector;

	public UIGroup(Group model) {
		super(model);
		init();
	}

	/**
	 * Initializes this instance
	 */
	private void init() {
		setIcon(new GroupIcon(this));
	}

	@Override
	protected void constructDataCollectionMenu(AbstractMenuBuilder menu) {
		super.constructDataCollectionMenu(menu);

		if (spikeCollector == null || spikeCollector.isDestroyed()) {
			menu.addAction(new StartCollectSpikes());
		} else {
			menu.addAction(new StopCollectSpikes());
		}

	}

	@Override
	protected NodeViewer createViewerInstance() {
		return new GroupViewer(this);
	}

	public void collectSpikes(boolean collect) {
		if (collect) {
			if (spikeCollector == null || spikeCollector.isDestroyed()) {
				spikeCollector = new UISpikeProbe(this);
				newProbeAdded(spikeCollector);
			}
		} else {
			if (spikeCollector != null) {
				spikeCollector.destroyModel();
				spikeCollector = null;
			}
		}
	}

	@Override
	public Group getModel() {
		return (Group) super.getModel();
	}

	@Override
	public int getNodesCount() {
		if (getModel() != null) {
			return getModel().getNodes().length;
		} else
			return 0;
	}

	@Override
	public int getDimensionality() {
		// Ensembles are not guaranteed to be NEFEnsembles, and may not have dimensionality
		return -1;
	}
	
	@Override
	public String getTypeName() {

		return "Ensemble";
	}

	@Override
	public void saveContainerConfig() {
		/*
		 * Do nothing here. Ensemble configuration cannot be saved yet
		 */
	}

	@Override
	protected void modelUpdated() {
		super.modelUpdated();
		if (getModel().isCollectingSpikes()) {
			collectSpikes(true);
		}
	}

	/**
	 * Action to enable Spike Collection
	 * 
	 * @author Shu Wu
	 */
	class StartCollectSpikes extends ReversableAction {

		private static final long serialVersionUID = 1L;

		public StartCollectSpikes() {
			super("Collect Spikes");
		}

		@Override
		protected void action() throws ActionException {
			if (getModel().isCollectingSpikes())
				throw new ActionException("Already collecting spikes");
			else
				collectSpikes(true);
		}

		@Override
		protected void undo() {
			collectSpikes(false);

		}

	}

	/**
	 * Action to Stop Collecting Spikes
	 * 
	 * @author Shu Wu
	 */
	class StopCollectSpikes extends ReversableAction {
		private static final long serialVersionUID = 1L;

		public StopCollectSpikes() {
			super("Stop Collecting Spikes");
		}

		@Override
		protected void action() throws ActionException {
			if (!getModel().isCollectingSpikes())
				throw new ActionException("Already not collecting spikes");
			else
				collectSpikes(false);

		}

		@Override
		protected void undo() throws ActionException {
			collectSpikes(true);
		}

	}
}
