/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UINeoNode.java". Description:
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

package ca.nengo.ui.models;

import ca.nengo.io.FileManager;
import ca.nengo.model.*;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.nef.NEFGroup;
import ca.nengo.model.nef.impl.DecodedSource;
import ca.nengo.model.neuron.Neuron;
import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.actions.*;
import ca.nengo.ui.configurable.ConfigException;
import ca.nengo.ui.configurable.UserDialogs;
import ca.nengo.ui.lib.actions.ActionException;
import ca.nengo.ui.lib.actions.StandardAction;
import ca.nengo.ui.lib.actions.UserCancelledException;
import ca.nengo.ui.lib.objects.activities.TransientStatusMessage;
import ca.nengo.ui.lib.objects.models.ModelObject;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.util.menus.AbstractMenuBuilder;
import ca.nengo.ui.lib.util.menus.PopupMenuBuilder;
import ca.nengo.ui.lib.world.DroppableX;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.models.NodeContainer.ContainerException;
import ca.nengo.ui.models.nodes.*;
import ca.nengo.ui.models.nodes.widgets.*;
import ca.nengo.ui.models.tooltips.TooltipBuilder;
import ca.nengo.ui.models.viewers.GroupViewer;
import ca.nengo.ui.models.viewers.NetworkViewer;
import ca.nengo.ui.models.viewers.NodeViewer;
import ca.nengo.util.Probe;
import ca.nengo.util.VisiblyMutable;
import ca.nengo.util.VisiblyMutable.Event;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * UI Wrapper for a NEO Node Model
 *
 * @author Shu
 */
public abstract class UINeoNode<N extends Node> extends UINeoModel<N> implements DroppableX {


    /**
	 * Factory method which creates a Node UI object around a Node
	 *
	 * @param node
	 *            Node to be wrapped
	 * @return Node UI Wrapper
	 */
	public static UINeoNode createNodeUI(Node node) {

		UINeoNode nodeUI = null;

        if (node instanceof UIBuilder) {
            return ((UIBuilder)node).newUI();
        }
		if (node instanceof Network) {
			nodeUI = new UINetwork((Network) node);
		} else if (node instanceof Group) {
			if (node instanceof NEFGroup) {
				nodeUI = new UINEFGroup((NEFGroup) node);
			} else {
				nodeUI = new UIGroup((Group) node);
			}
		} else if (node instanceof Neuron) {
			nodeUI = new UINeuron((Neuron) node);
		} else if (node instanceof FunctionInput) {
			nodeUI = new UIFunctionInput((FunctionInput) node);
		} else {
			nodeUI = new UIGenericNode(node);
		}
		return nodeUI;
	}

	private ModelUpdateListener myUpdateListener;

	/**
	 * Attached probes
	 */
	private Vector<UIProbe> probes;

	public UINeoNode(N model) {
		super(model);
	}

	/**
	 * Does a linear search of the node's children and returns the result
	 *
	 * @return The Child found matching parameters, null if not found
	 * @param name
	 *            of the Child
	 * @param type
	 *            of the Child
	 */
	private WorldObject getChild(String name, Class<?> type) {

		/*
		 * Linear search used because there tends to be only a small number of
		 * widgets
		 */

		for (WorldObject wo : getChildren()) {
			if (type != null) {
				if (type.isInstance(wo) && (wo.getName().compareTo(name) == 0)) {
					return wo;
				}
			} else if ((wo.getName().compareTo(name) == 0)) {
				return wo;
			}
		}
		return null;
	}

	/**
	 * @param widget
	 *            Widget to be added
	 */
	protected void addWidget(Widget widget) {
		widget.setScale(0.5);
		addChild(widget);
	}

	@SuppressWarnings("unchecked")
	protected void constructDataCollectionMenu(AbstractMenuBuilder menu) {
		/*
		 * Build the "add probe" menu
		 */
		AbstractMenuBuilder probesMenu = menu.addSubMenu("Add probe");
		boolean somethingFound = false;
		if (getModel() instanceof Probeable) {

			Probeable probeable = (Probeable) getModel();
			Properties states = probeable.listStates();

			// Enumeration e = states.elements();
			Iterator<?> it = states.entrySet().iterator();

			while (it.hasNext()) {
				somethingFound = true;
				Entry<String, String> el = (Entry<String, String>) it.next();
				probesMenu.addAction(new AddProbeAction(this, el));

			}
		}

		if (!somethingFound) {
			probesMenu.addLabel("Nothing probeable");
		}

	}

	class RenameNodeAction extends StandardAction {
		private static final long serialVersionUID = 1L;

		public RenameNodeAction(String description) {
			super(description);
		}

		@Override
		protected void action() throws ActionException {
			try {
				String newName = UserDialogs.showDialogString("Enter name", getName());

				getModel().setName(newName);
			} catch (ConfigException e) {
				throw new UserCancelledException();
			} catch (StructuralException e) {
				UserMessages.showWarning("Could not rename: " + e.getMessage());
			}
		}

	}

	@Override
	protected void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);

		Collection<UINeoNode> arrayOfMe = new ArrayList<UINeoNode>();
		arrayOfMe.add(this);
		
		menu.addAction(new CopyAction("Copy", arrayOfMe));
		menu.addAction(new CutAction("Cut", arrayOfMe));

		SimulationMode mode = ((UINeoNode) arrayOfMe.toArray()[0]).getModel().getMode();

		int selected = -1;
		if (mode == SimulationMode.DEFAULT) {
			selected = 0;
		} else if (mode == SimulationMode.RATE) {
			selected = 1;
		} else if (mode == SimulationMode.DIRECT) {
			selected = 2;
		}
		
		if (selected >= 0) {
			AbstractMenuBuilder modeMenu = menu.addSubMenu("Mode");
			modeMenu.addActionsRadio(new StandardAction[]{
					new DefaultModeAction("Spiking", arrayOfMe),
					new RateModeAction("Rate", arrayOfMe),
					new DirectModeAction("Direct", arrayOfMe)
				}, selected);
		}

//		menu.addSection("File");
//		menu.addAction(new SaveNodeAction(this));
//		menu.addAction(new RenameNodeAction("Rename"));

		menu.addSection("View");
//		AbstractMenuBuilder docMenu = menu.addSubMenu("Documentation");
//		docMenu.addAction(new SetDocumentationAction("Set"));
//		docMenu.addAction(new ViewDocumentationAction("View"));
		constructViewMenu(menu);

		menu.addSection("Data Collection");
		constructDataCollectionMenu(menu);

	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);

		if (getModel().getDocumentation() != null) {
			tooltips.addProperty("Documentation",
					Util.truncateString(getModel().getDocumentation(), 100));
		}
		tooltips.addProperty("Simulation mode", getModel().getMode().toString());

	}

	protected void constructViewMenu(AbstractMenuBuilder menu) {

		AbstractMenuBuilder originsAndTerminations = menu.addSubMenu("Origins and terminations");

		/*
		 * Build the "show origins" menu
		 */
		Source[] sources = getModel().getOrigins();
		if (sources.length > 0) {

			AbstractMenuBuilder originsMenu = originsAndTerminations.addSubMenu("Show origin");

			for (Source element : sources) {
				originsMenu.addAction(new ShowOriginAction(element.getName()));
			}

		}

		/*
		 * Build the "show origins" menu
		 */
		Target[] targets = getModel().getTerminations();
		if (targets.length > 0) {

			AbstractMenuBuilder terminationsMenu = originsAndTerminations.addSubMenu("Show termination");

			for (Target element : targets) {
				terminationsMenu.addAction(new ShowTerminationAction(element.getName()));
			}

		}
		originsAndTerminations.addAction(new ShowAllOandTAction("Show all"));
		originsAndTerminations.addAction(new HideAllOandTAction("Hide all"));

	}

	@Override
	protected void initialize() {
		super.initialize();
		probes = new Vector<UIProbe>();
		myUpdateListener = new ModelUpdateListener();
	}

	@Override
	protected void modelUpdated() {
		super.modelUpdated();

		Source[] modelSources = getModel().getOrigins();
		HashSet<Source> modelSourceSet = new HashSet<Source>(modelSources.length);
        Collections.addAll(modelSourceSet, modelSources);

		Target[] modelTargets = getModel().getTerminations();
		HashSet<Target> modelTargetSet = new HashSet<Target>(
				modelTargets.length);
        Collections.addAll(modelTargetSet, modelTargets);

		for (WorldObject wo : getChildren()) {
			if (wo instanceof ModelObject) {
				Object model = ((ModelObject) wo).getModel();

				if (model instanceof Target) {
					if (!modelTargetSet.contains(model)) {
						wo.destroy();
						this.showPopupMessage("Termination removed: " + wo.getName());
					} else {
						modelTargetSet.remove(model);
					}
				}
				if (wo instanceof Source) {
					if (!modelSourceSet.contains(model)) {
						wo.destroy();
						this.showPopupMessage("Origin removed: " + wo.getName());
					} else {
						modelSourceSet.remove(model);
					}
				}
			}

		}
		
		// Ensure that any new origins and terminations are shown
		for (Target term: modelTargetSet) {
			this.showTermination(term.getName());
		}
		for (Source source : modelSourceSet) {
			String name= source.getName();
			
			// don't automatically show these two origins for NEFEnsembles
			if (this instanceof UINEFGroup) {
				if (name.equals("AXON") || name.equals("current")) {
					continue;				
				}
			}
			this.showOrigin(source.getName());
		}
	}

	/**
	 * Called when a new probe is added
	 *
	 * @param probeUI
	 *            New probe that was just added
	 */
	protected void newProbeAdded(UIProbe probeUI) {

		addChild(probeUI);
		probes.add(probeUI);

		/*
		 * Assign the probe to a Origin / Termination
		 */

		WorldObject probeHolder = null;

		Source source = null;
		try {
			source = getModel().getOrigin(probeUI.getName());

		} catch (StructuralException e1) {
            e1.printStackTrace();
		}

		if (source != null) {
			probeHolder = showOrigin(source.getName());
		} else if (source == null) {
			Target term = null;
			try {
				term = getModel().getTermination(probeUI.getName());

			} catch (StructuralException e) {
                e.printStackTrace();
			}
			if (term != null) {
                probeHolder = showTermination(term.getName());
            }
		}

		if (probeHolder != null) {
			probeUI.setOffset(0, probeHolder.getHeight() / 2);
			probeHolder.addChild(probeUI);

		} else {
			addChild(probeUI);
		}
	}

	@Override
	protected void prepareToDestroyModel() {
		super.prepareToDestroyModel();

		for (WorldObject wo : getChildren()) {
			if (wo instanceof UITermination) {
				UITermination term = (UITermination) wo;
				term.disconnect();
			}
		}

	}

	/**
	 * Creates a new probe and adds the UI object to the node
	 *
	 * @param stateName
	 *            The name of the state variable to probe
	 */
	public UIStateProbe addProbe(String stateName) throws SimulationException {
		UIStateProbe probeUI = new UIStateProbe(this, stateName);
		newProbeAdded(probeUI);
		return probeUI;
	}

	@Override
	public void attachViewToModel() {
		super.attachViewToModel();
		if (getModel() instanceof VisiblyMutable) {
			VisiblyMutable visiblyMutable = getModel();
			visiblyMutable.addChangeListener(myUpdateListener);
		}
	}

	@Override
	public void detachViewFromModel() {
		super.detachViewFromModel();
		if (getModel() instanceof VisiblyMutable) {
			VisiblyMutable visiblyMutable = getModel();

			Util.Assert(myUpdateListener != null);
			visiblyMutable.removeChangeListener(myUpdateListener);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ca.shu.ui.lib.world.DroppableX#droppedOnTargets(java.util.Collection)
	 */
	public void droppedOnTargets(Collection<WorldObject> targets) throws UserCancelledException {
		// look through all containers, move to the first NodeContainer we find
		for (WorldObject wo : targets) {
			if (wo instanceof NodeContainer) {
				NodeContainer nodeContainer = (NodeContainer) wo;

				try {
					CreateModelAction.ensureNonConflictingName(getModel(), nodeContainer); // throws UserCancelledException
					
					Node node;
					try {
						node = getModel().clone();
					} catch (CloneNotSupportedException e) {
						throw new ContainerException("Could not clone node: " + e.getMessage());
					}
					Point2D newPosition = localToGlobal(new Point2D.Double(0, 0));
					newPosition = wo.globalToLocal(newPosition);
					newPosition = nodeContainer.localToView(newPosition);

					// destroy the old model
					destroyModel();
					
					// add the new model
					nodeContainer.addNodeModel(node,
							newPosition.getX(),
							newPosition.getY());

				} catch (ContainerException e) {
					UserMessages.showWarning("Could not drop into container: " + e.getMessage());
				}

				return;
			}
		}
	}

	/**
	 * @return The default file name for this node
	 */
	public String getFileName() {
		return this.getName() + '.' + AbstractNengo.NEONODE_FILE_EXTENSION;
	}

	@Override
	public N getModel() {
		return super.getModel();
	}

	@Override
	public String getName() {
		if (getModel() != null) {
			return getModel().getName();
		} else {
			return "Model not constructed";
		}
	}

    /**
     * @return The Network model the Node is attached to
     */
    public UINetwork getNetworkParent() {
        UINeoNode other = this;
        while (true) {
            NodeViewer viewer = other.getParentViewer();

		/*
         * Can only access parent network if the Node is inside a Network Viewer
		 */
            if (viewer instanceof NetworkViewer) {
                return ((NetworkViewer) viewer).getViewerParent();
            } else if (viewer != null) {
                // Found the parent viewer, but it's not a network viewer
                // Recursively iterate up the view graph until we find the NetworkViewer or not
                //
                WorldObject viewerParent = viewer.getViewerParent();

                if (viewerParent instanceof UINeoNode) {
                    other = ((UINeoNode) viewerParent);
                    continue;
                }
            }

            return null;
        }
    }

	/**
	 * @return The viewer the node is contained in, this may be a regular world
	 *         or a specialized viewer such as a NetworkViewer or EnsembleViewer
	 */
	public NodeViewer getParentViewer() {

		WorldImpl viewer = getWorld();
		if (viewer != null && viewer instanceof NodeViewer) {
			return (GroupViewer) viewer;
		} else {
			return null;
		}
	}

	public Vector<UIProbe> getProbes() {
		return probes;
	}

	public Collection<UIOrigin> getVisibleOrigins() {
		List<UIOrigin> origins = new ArrayList(getChildrenCount());

		for (WorldObject wo : getChildren()) {
			if (wo instanceof UIOrigin) {
				origins.add((UIOrigin) wo);
			}
		}
		return origins;
	}

	public Collection<UITermination> getVisibleTerminations() {
		List<UITermination> terminations = new ArrayList(getChildrenCount());

		for (WorldObject wo : getChildren()) {
			if (wo instanceof UITermination) {
				terminations.add((UITermination) wo);
			}
		}
		return terminations;
	}

	/**
	 * Hides all origins and terminations
	 */
	public void hideAllOandT() {
		for (WorldObject wo : getChildren()) {
			if (wo instanceof Widget && (wo instanceof UITermination || wo instanceof UIOrigin)) {
				((Widget) wo).setWidgetVisible(false);
			}
		}
		layoutChildren();
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();

		/*
		 * layout widgets such as Origins and Terminations
		 */
		Rectangle2D bounds = getIcon().localToParent(getIcon().getBounds());

		double offsetX = bounds.getX();
		double offsetY = bounds.getY();

		double centerX = offsetX + bounds.getWidth() / 2f;
		double centerY = offsetY + bounds.getHeight() / 2f;

		double termX = -20 + bounds.getX();
		double termY = getIcon().getHeight() + offsetY;

		double originX = getIcon().getWidth() + 5 + offsetX;
		double originY = termY;

		double probeY = 0;

		/*
		 * Lays out origin objects
		 */
		for (WorldObject wo : getChildren()) {

			if (wo instanceof UIProbe) {
				UIProbe probe = (UIProbe) wo;

				probe.setOffset(getWidth() * (1f / 4f), probeY + getHeight() * (1f / 4f));
				probeY += probe.getHeight() + 5;

			} else if (wo instanceof Widget) {
				Widget widget = (Widget) wo;
				if (widget.getParent() == null) {
					/*
					 * Check to see that the origin has not been removed from
					 * the world
					 */

				} else {

					double scale = widget.getScale();

					if (!(widget).isWidgetVisible()) {
						double x = centerX - widget.getWidth() * scale / 2f;
						double y = centerY - widget.getHeight() * scale / 2f;

						widget.setOffset(x, y);

						widget.setVisible(false);
						widget.setPickable(false);
						widget.setChildrenPickable(false);

					} else {
						widget.setVisible(true);
						widget.setPickable(true);
						widget.setChildrenPickable(true);

						if (widget instanceof UIOrigin) {
							originY -= scale * widget.getHeight() + 8;
							widget.setOffset(originX, originY);

						} else if (widget instanceof UITermination) {
							termY -= scale * widget.getHeight() + 8;
							widget.setOffset(termX, termY);
						}
					}
				}

			}
		}

	}

	/**
	 * Removes a Probe UI object from node
	 *
	 * @param probe
	 *            to be removed
	 */
	public void removeProbe(UIProbe probe) {
		probes.remove(probe);
		probe.destroy();

	}

	/**
	 * @param file
	 *            File to be saved in
	 * @throws IOException
	 *             if model cannot be saved to file
	 */
	public void saveModel(File file) throws IOException {
		FileManager fm = new FileManager();

		fm.save(this.getModel(), file);
		new TransientStatusMessage(this.getFullName() + " was saved to " + file.toString(), 2500);
	}
	
	public void generateScript(File file) throws IOException {
		FileManager fm = new FileManager();

		fm.generate(this.getModel(), file.toString());
		new TransientStatusMessage(this.getFullName() + " generated script " + file.toString(), 2500);
	}

	@Override
	public final void setName(String name) {
		/*
		 * Set name is disabled, the Name is automatically retrieved from model
		 */
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the visibility of widgets
	 */
	public void setWidgetsVisible(boolean visible) {
		for (WorldObject wo : getChildren()) {
			if (wo instanceof Widget) {
				((Widget) wo).setWidgetVisible(visible);
			}
		}
		layoutChildren();
	}

	/**
	 * Shows all the origins on the Node model
	 */
	public void showAllOrigins() {

		Source[] sources = getModel().getOrigins();

		for (Source element : sources) {
			UIOrigin originUI = showOrigin(element.getName());
			originUI.setWidgetVisible(true);
		}
		layoutChildren();
	}

	/**
	 * Shows all the origins on the Node model
	 */
	public void showAllDecodedOrigins() {

		Source[] sources = getModel().getOrigins();

		for (Source element : sources) {
			if (element instanceof DecodedSource) {
				UIOrigin originUI = showOrigin(element.getName());
				originUI.setWidgetVisible(true);
			}
		}
		layoutChildren();
	}


	/**
	 * Shows all the terminations on the Node model
	 */
	public void showAllTerminations() {

		Target[] targets = getModel().getTerminations();

		for (Target element : targets) {
			UITermination termUI = showTermination(element.getName());
			termUI.setWidgetVisible(true);
		}
		layoutChildren();
	}

	/**
	 *            Name of an Origin on the Node model
	 * @return the POrigin shown
	 */
	public UIOrigin showOrigin(String originName) {

		UIOrigin originUI;

		// Try to find if the origin has already been created
		originUI = (UIOrigin) getChild(originName, UIOrigin.class);
		if (originUI == null) {
			// try to create it
			try {
				Source sourceModel = getModel().getOrigin(originName);
				if (sourceModel != null) {
					originUI = UIOrigin.createOriginUI(this, sourceModel);
					addWidget(originUI);
				} else {
					Util.Assert(false, "Could not find origin: " + originName);
				}

			} catch (StructuralException e) {
				UserMessages.showError(e.toString());
			}
		}

		if (originUI != null) {
			originUI.setWidgetVisible(true);
		}
		return originUI;

	}

	/**
	 *            Name of an Origin on the Node model
	 * @return the POrigin hidden
	 */
	public UIOrigin hideOrigin(String originName) {

		UIOrigin originUI;

		originUI = (UIOrigin) getChild(originName, UIOrigin.class);

		if (originUI != null) {
			originUI.setWidgetVisible(false);
		}
		return originUI;

	}

	/**
	 * Call this function if the probe already exists in the simulator and only
	 * needs to be shown
	 *
	 * @param probe
	 *            To be shown
	 * @return Probe UI Object
	 */
	public UIProbe showProbe(Probe probe) {
		/*
		 * Check if the probe is already shown
		 */
		for (UIProbe probeUI : probes) {
			if (probeUI.getModel() == probe) {
				return probeUI;
			}
		}

		UIStateProbe probeUI = new UIStateProbe(this, probe);
		newProbeAdded(probeUI);
		return probeUI;
	}

	/**
	 *            Name of an Termination on the Node model
	 * @return
	 */
	public UITermination showTermination(String terminationName) {
		UITermination termUI;

		// Try to find if the origin has already been created
		termUI = (UITermination) getChild(terminationName, UITermination.class);
		if (termUI == null) {
			// Otherwise try to create it
			try {

				Target termModel = getModel().getTermination(terminationName);
				if (termModel != null) {
					termUI = UITermination.createTerminationUI(this, termModel);
					addWidget(termUI);
				} else {
					Util.Assert(false, "Could not find termination: " + terminationName);
				}

			} catch (StructuralException e) {
				UserMessages.showError(e.toString());
			}
		}
		if (termUI != null) {
			termUI.setWidgetVisible(true);
		}
		return termUI;

	}

	private class ModelUpdateListener implements VisiblyMutable.Listener {
		private boolean modelUpdatePending = false;

		public void changed(Event e) {
			if (!modelUpdatePending) {
				modelUpdatePending = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						modelUpdatePending = false;
						firePropertyChange(Property.MODEL_CHANGED);
						if (getModel() != null) {
							modelUpdated();
						}
					}
				});
			}
		}
	}

	/**
	 * Action for hiding all origins and terminations
	 *
	 * @author Shu Wu
	 */
	class HideAllOandTAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public HideAllOandTAction(String actionName) {
			super("Hide all origins and terminations", actionName);
		}

		@Override
		protected void action() throws ActionException {
			setWidgetsVisible(false);
		}
	}

	/**
	 * Action for setting the documentation of the node
	 *
	 * @author Shu Wu
	 */
	/*class SetDocumentationAction extends ReversableAction {

		private static final long serialVersionUID = 1L;

		String prevDoc;

		public SetDocumentationAction(String actionName) {
			super("Set documentation on " + getName(), actionName);

		}

		@Override
		protected void action() throws ActionException {
			prevDoc = getModel().getDocumentation();

			JTextArea editor = new JTextArea(30, 50);
			editor.setText(prevDoc);

			int rtnValue = JOptionPane.showOptionDialog(UIEnvironment.getInstance(),
					new JScrollPane(editor),
					getName() + " - Documenation Editor",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					null);

			if (rtnValue == JOptionPane.OK_OPTION) {
				String text = editor.getText();

				if (text != null) {

					getModel().setDocumentation(text);
					showPopupMessage("Documentation changed");
				} else {
					throw new UserCancelledException();
				}
			}

		}

		@Override
		protected void undo() throws ActionException {
			getModel().setDocumentation(prevDoc);
			showPopupMessage("Documentation changed");
		}
	}*/

	/**
	 * Action for showing all origins and terminations
	 *
	 * @author Shu Wu
	 */
	class ShowAllOandTAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public ShowAllOandTAction(String actionName) {
			super("Show all origins and terminations", actionName);
		}

		@Override
		protected void action() throws ActionException {
			showAllOrigins();
			showAllTerminations();
		}
	}

	/**
	 * Action for showing a specific origin
	 *
	 * @author Shu Wu
	 */
	class ShowOriginAction extends StandardAction {

		private static final long serialVersionUID = 1L;
		final String originName;

		public ShowOriginAction(String originName) {
			super(originName);
			this.originName = originName;
		}

		@Override
		protected void action() throws ActionException {
			showOrigin(originName);
		}
	}

	/**
	 * Action for showing a specific termination
	 *
	 * @author Shu Wu
	 */
	class ShowTerminationAction extends StandardAction {

		private static final long serialVersionUID = 1L;
		final String termName;

		public ShowTerminationAction(String termName) {
			super(termName);
			this.termName = termName;
		}

		@Override
		protected void action() throws ActionException {
			showTermination(termName);
		}
	}

	/**
	 * Action for viewing the node's documentation
	 *
	 * @author Shu Wu
	 */
	/*class ViewDocumentationAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public ViewDocumentationAction(String actionName) {
			super("View documentation on " + getName(), actionName);

		}

		@Override
		protected void action() throws ActionException {
			UserMessages.showTextDialog(getName() + " - Documentation Viewer",
					getModel().getDocumentation());
		}

	}*/
}
