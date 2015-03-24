/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NetworkViewer.java". Description:
"Viewer for peeking into a Network

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

package ca.nengo.ui.model.viewer;

import ca.nengo.model.*;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.UserMessages;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.icon.LoadIcon;
import ca.nengo.ui.lib.world.piccolo.icon.SaveIcon;
import ca.nengo.ui.lib.world.piccolo.icon.ZoomIcon;
import ca.nengo.ui.lib.world.piccolo.object.Button;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.widget.*;
import ca.nengo.util.Probe;
import nars.Global;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.util.PBounds;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Viewer for peeking into a Network
 * 
 * @author Shu Wu
 */
public class NetworkViewer extends GroupViewer<Network,UINetwork> implements NodeContainer {
    private static final boolean ELASTIC_LAYOUT_ENABLED_DEFAULT = false;
    private final File layoutFile;
    private final File backupLayoutFile;
    private ShapeObject layoutArea;
    private Button zoom;
    //private Button feedforward;
    private Button save;
    private Button restore;

    private static final int BUTTON_SIZE = 18;
    private HashSet<NSource> exposedSources;
    private HashSet<NTarget> exposedTargets;

    /**
     * @param pNetwork
     *            Parent Network UI wrapper
     */
    public NetworkViewer(UINetwork pNetwork) {
        this(pNetwork, new WorldGroundImpl());
    }

    public NetworkViewer(UINetwork pNetwork, WorldGroundImpl g) {
        super(pNetwork, g);
        String layoutFileName = "layouts/" + pNetwork.name() + ".layout";
        this.layoutFile = new File(layoutFileName);
        this.backupLayoutFile = new File(layoutFileName + ".bak");
    }

    @Override
    protected boolean canRemoveChildModel(Node node) {
        return true;
    }


    @Override
    protected void initialize() {
        exposedSources = new HashSet<NSource>(getModel().getSources().length);
        exposedTargets = new HashSet<NTarget>(getModel().getTargets().length);

        super.initialize();
        addLayoutButtons();
        updateSimulatorProbes();
    }

    private void addLayoutButtons() {
        zoom = new Button(new ZoomIcon(BUTTON_SIZE),
                new Runnable() {
            public void run() {
                zoomToFit();
            }
        });
        /*
        feedforward = new Button(new ArrowIcon(BUTTON_SIZE),
                new Runnable() {
            public void run() {
                doFeedForwardLayout();
            }
        });
        */
        save = new Button(new SaveIcon(BUTTON_SIZE),
                new Runnable() {
            public void run() {
                saveNodeLayout();
            }
        });
        restore = new Button(new LoadIcon(BUTTON_SIZE),
                new Runnable() {
            public void run() {
                restoreNodeLayout();
            }
        });
        layoutArea = ShapeObject.createRectangle(0, 0, 0.25f, 0.2f);
        layoutArea.setTransparency(0.0f);
        zoom.setTransparency(0.0f);
        //feedforward.setTransparency(0.0f);
        save.setTransparency(0.0f);
        restore.setTransparency(0.0f);
        layoutArea.setPickable(true); // Need this to have event listeners. Ugh.
        layoutArea.addInputEventListener(new ShowHideHandler(
                new WorldObject[]{
                        zoom, /*feedforward, */save, restore
                }));
        this.addChild(layoutArea);
        this.addChild(zoom);
        //this.addChild(feedforward);
        this.addChild(save);
        this.addChild(restore);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();
        layoutArea.setBounds(w - w * 0.255, h - h * 0.205, w * 0.25, h * 0.2);

        double buttonX = w * 0.98 - restore.getWidth();
        double buttonY = h * 0.98 - restore.getHeight();
        restore.setOffset(buttonX, buttonY);
        buttonX -= save.getWidth();
        save.setOffset(buttonX, buttonY);
        //buttonX -= feedforward.getWidth();
        //feedforward.setOffset(buttonX, buttonY);
        buttonX -= zoom.getWidth();
        zoom.setOffset(buttonX, buttonY);
    }

    @Override
    protected void removeChildModel(Node node) {
        getModel().removeNode(node.name());
    }

    protected Double newItemPositionX;
    protected Double newItemPositionY;
    public void setNewItemPosition(Double x, Double y) {
        newItemPositionX=x;
        newItemPositionY=y;
    }

    @Override
    protected void afterViewUpdated(boolean isFirstUpdate) {
        /*
         * Create projection map
         */
        Projection[] projections = getModel().getProjections();

        HashSet<Projection> projectionsToAdd = new HashSet<Projection>();
        Collections.addAll(projectionsToAdd, getModel().getProjections());

        Map<NTarget, Projection> projectionMap = Global.newHashMap(projections.length);

        for (final Projection projection : projectionsToAdd) {
            Projection existing = projectionMap.put(projection.getTarget(), projection);
            Util.Assert(existing == null, "More than one projection found per termination");
        }

        /*
         * Get UI projections
         */
        LinkedList<UIProjection> projectionsToRemove = new LinkedList<UIProjection>();

        for (UINeoNode<?> nodeUI : getUINodes()) {
            for (UITarget<?> terminationUI : nodeUI.getVisibleTargets()) {
                if (terminationUI.getConnector() != null) {
                    UISource originUI = terminationUI.getConnector().getOriginUI();

                    NTarget target = terminationUI.node();
                    NSource source = originUI.node();

                    Projection projection = projectionMap.get(target);
                    if (projection != null && projection.getSource() == source) {
                        /*
                         * Projection already exists
                         */
                        projectionsToAdd.remove(projectionMap.get(target));

                    } else {
                        projectionsToRemove.add(terminationUI.getConnector());
                    }
                }
            }
        }

        /*
         * Destroy unreferenced projections
         */
        for (UIProjection projectionUI : projectionsToRemove) {
            UITarget terminationUI = projectionUI.getTermination();

            projectionUI.destroy();
            if (!isFirstUpdate) {
                terminationUI.showPopupMessage("REMOVED Projection to "
                        + terminationUI.getNodeParent().name() + '.' + terminationUI.name());
            }
        }

        /*
         * Construct projections
         */
        for (Projection projection : projectionsToAdd) {
            NSource source = projection.getSource();
            NTarget term = projection.getTarget();

            UINeoNode nodeOrigin = getUINode(source.getNode());

            UINeoNode nodeTerm = getUINode(term.getNode());

            if (nodeOrigin != null && nodeTerm != null) {
                UISource originUI = nodeOrigin.showSource(source.getName());
                UITarget termUI = nodeTerm.showTarget(term.getName());

                originUI.connectTo(termUI, false);
                if (!isFirstUpdate) {
                    termUI.showPopupMessage("Connected to " + termUI.name() + '.'
                            + name());
                }
            } else {
                if (nodeOrigin == null) {
                    Util.Assert(false, "Could not find a Origin attached to a projection: "
                            + source.getNode().name());
                }
                if (nodeTerm == null) {
                    Util.Assert(false, "Could not find a Termination attached to a projection: "
                            + term.getNode().name());
                }
            }

        }

        updateViewExposed();
    }

    @Override
    protected UINeoNode createUINode(Node node, boolean isFirstUpdate) {
                    /*
                     * Create UI Wrappers here
                     */
        UINeoNode nodeUI = UINeoNode.createNodeUI(node);

        if (nodeUI == null) {
            throw new RuntimeException(node + " did not createUINode");
        }

        if (newItemPositionX != null && newItemPositionY != null) {
            nodeUI.setOffset(newItemPositionX, newItemPositionY);
            neoNodesChildren.put(nodeUI.node(), nodeUI);
            getGround().addChildFancy(nodeUI, false);

        } else {
            boolean centerAndNotify = !isFirstUpdate && isDropEffect();
            UINeoNode u = addUINode(nodeUI, centerAndNotify, false);

            neoNodesChildren.put(node, u);
            if (centerAndNotify) {
                nodeUI.showPopupMessage("Node " + node.name() + " added to Network");
            }
        }

        return nodeUI;
    }

    protected boolean isDropEffect() {
        return true;
    }

    private void updateViewExposed() {
        /*
         * Get exposed Origins and Terminations
         */
        HashSet<NSource> exposedOriginsTemp = new HashSet<NSource>(getModel().getSources().length);
        HashSet<NTarget> exposedTerminationsTemp = new HashSet<NTarget>(
                getModel().getTargets().length);

        for (NSource source : getModel().getSources()) {
            if (source instanceof NetworkImpl.SourceWrapper) {
                NetworkImpl.SourceWrapper originWr = (NetworkImpl.SourceWrapper) source;
                exposedOriginsTemp.add(originWr.getWrappedOrigin());
            }
        }

        for (NTarget target : getModel().getTargets()) {
            if (target instanceof NetworkImpl.TargetWrapper) {
                NetworkImpl.TargetWrapper terminationWr = (NetworkImpl.TargetWrapper) target;
                exposedTerminationsTemp.add(terminationWr.getWrappedTermination());
            }
        }

        /*
         * Check to see if terminations have been added or removed
         */
        boolean exposedOriginsChanged = false;
        if (exposedOriginsTemp.size() != exposedSources.size()) {
            exposedOriginsChanged = true;
        } else {
            /*
             * Iterate through origins to see if any have changed
             */
            for (NSource source : exposedOriginsTemp) {
                if (!exposedSources.contains(source)) {
                    break;
                }
                exposedOriginsChanged = true;
            }
        }
        // Copy changed exposed origins if needed
        if (exposedOriginsChanged) {
            exposedSources = exposedOriginsTemp;
        }

        boolean exposedTerminationsChanged = false;
        if (exposedTerminationsTemp.size() != exposedTargets.size()) {
            exposedTerminationsChanged = true;
        } else {
            /*
             * Iterate through Termination to see if any have changed
             */
            for (NTarget target : exposedTerminationsTemp) {
                if (!exposedTargets.contains(target)) {
                    break;
                }
                exposedTerminationsChanged = true;
            }
        }
        // Copy changed exposed terminations if needed
        if (exposedTerminationsChanged) {
            exposedTargets = exposedTerminationsTemp;
        }

        if (exposedTerminationsChanged || exposedOriginsChanged) {
            /*
             * Update exposed terminations and origins
             */
            for (WorldObject wo : getGround().getChildren()) {
                if (wo instanceof UINeoNode) {
                    UINeoNode<?> nodeUI = (UINeoNode) wo;

                    if (exposedOriginsChanged) {
                        for (UISource originUI : nodeUI.getVisibleSources()) {
                            boolean isExposed = exposedSources.contains(originUI.node());
                            originUI.setExposed(isExposed);
                        }
                    }
                    if (exposedTerminationsChanged) {
                        for (UITarget terminationUI : nodeUI.getVisibleTargets()) {
                            boolean isExposed = exposedTargets.contains(terminationUI.node());
                            terminationUI.setExposed(isExposed);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void applyDefaultLayout() {
        if (getUINodes().size() != 0) {
            if (restoreNodeLayout()) {
                return;
            } else {
                applySortLayout(SortMode.BY_NAME);
                // applyJungLayout(KKLayout.class);
            }
        }
        /*
        if (ELASTIC_LAYOUT_ENABLED_DEFAULT) {
            // enable elastic layout for Jung && when no nodes are loaded.
            getGround().setElasticEnabled(true);
        }
        */
    }

    @Override
    public void constructMenu(PopupMenuBuilder menu, Double posX, Double posY) {
        super.constructMenu(menu, posX, posY);

        /*
         * Origins & Terminations
         */
        menu.addSection("Origins and Terminations");
        menu.addAction(new SetOTVisiblityAction("Unhide all", true));
        menu.addAction(new SetOTVisiblityAction("Hide all", false));

        /*
         * Construct simulator menu
         */
        UINetwork.constructSimulatorMenu(menu, getViewerParent());

        /*
         * Create new models
         */
        /*menu.addSection("Add model");
		MenuBuilder createNewMenu = menu.addSubMenu("Create new");

		// Nodes
		for (ConstructableNode constructable : ModelFactory.getNodeConstructables(this)) {
			createNewMenu.addAction(new CreateModelAction(this, constructable));
		}

		MenuBuilder createAdvancedMenu = createNewMenu.addSubMenu("Other");
		for (Class<?> element : ClassRegistry.getInstance().getRegisterableTypes()) {
			if (Node.class.isAssignableFrom(element)) {
				createAdvancedMenu.addAction(new CreateModelAdvancedAction(this, element));
			}
		}

		menu.addAction(new OpenNeoFileAction(this));*/

    }


    @Override
    public Network getModel() {
        return super.getModel();
    }

    @Override
    public UINetwork getViewerParent() {
        return super.getViewerParent();
    }

    /**
     * @return Whether the operation was successful
     */
    public boolean restoreNodeLayout() {
        File fileToOpen = layoutFile;
        boolean loadFromBackup = false;
        boolean readFailed = false;

        if (!layoutFile.exists()) {
            if (!backupLayoutFile.exists()) {
                return false;
            }
            else {
                System.err.println("NetworkViewer.restoreNodeLayout() - Layout file not found, attempting to restore from backup.");
                fileToOpen = backupLayoutFile;
                loadFromBackup = true;
            }
        }

        //getGround().setElasticEnabled(false);
        boolean enableElasticMode = false;

        HashMap<String, Float[]> nodeXY = new HashMap<String, Float[]>();
        String line = null;
        PBounds fullBounds = null;

        while (true) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(fileToOpen));
            } catch (IOException e) {
                System.err.println("NetworkViewer.restoreNodeLayout() - IOException encountered attempting to create BufferedReader: "
                        + e.getMessage());
                readFailed = true;
            }

            try {
                while((line = reader.readLine()) != null) {
                    if (line.length() >= 2 && line.substring(0, 2).equals("# ")) {
                        if (line.contains("elasticmode=")) {
                            enableElasticMode = Boolean.parseBoolean(
                                    line.substring(line.indexOf('=') + 1));
                        } else if (line.contains("viewbounds=")) {
                            float x = Float.parseFloat(line.substring(
                                    line.indexOf("x=") + 2, line.indexOf(',')));
                            float y = Float.parseFloat(line.substring(
                                    line.indexOf("y=") + 2, line.indexOf(',', line.indexOf("y="))));
                            float width = Float.parseFloat(line.substring(
                                    line.indexOf("width=") + 6, line.indexOf(',', line.indexOf("width="))));
                            float height = Float.parseFloat(line.substring(
                                    line.indexOf("height=") + 7, line.indexOf(']', line.indexOf("height="))));
                            // TODO: Hax
                            x += 161.5;
                            y += 100;
                            width -= 323;
                            height -= 200;
                            fullBounds = new PBounds(x, y, width, height);
                        } else {
                            float x = Float.parseFloat(line.substring(
                                    line.indexOf(")=Point2D.Double[") + 17, line.indexOf(',',
                                            line.indexOf(")=Point2D.Double["))));
                            float y = Float.parseFloat(line.substring(
                                    line.indexOf(',', line.indexOf(")=Point2D.Double[")) + 1,
                                    line.indexOf(']', line.indexOf(")=Point2D.Double["))));
                            String fullName = line.substring(2,
                                    line.indexOf("=Point2D.Double["));
                            nodeXY.put(fullName, new Float[]{x, y});
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("NetworkViewer.restoreNodeLayout() - IOException encountered attempting to parse file: " +
                        e.getMessage());
                readFailed = true;
            } catch (StringIndexOutOfBoundsException e) {
                System.err.println("NetworkViewer.restoreNodeLayout() - StringIndexOutOfBoundsException encountered attempting to parse file: " +
                        e.getMessage());
                readFailed = true;
            } catch (NumberFormatException e) {
                System.err.println("NetworkViewer.restoreNodeLayout() - NumberFormatException encountered attempting to parse file: " +
                        e.getMessage());
                readFailed = true;
            }

            if (readFailed) {
                // If backup file does not exists, don't bother trying to load from it.
                if (!backupLayoutFile.exists()) {
                    return false;
                }
                if (!loadFromBackup) {
                    System.err.println("NetworkViewer.restoreNodeLayout() - Attempting to load from backup layout file.");
                    fileToOpen = backupLayoutFile;
                    loadFromBackup = true;
                    readFailed = false;
                }
                else {
                    System.err.println("NetworkViewer.restoreNodeLayout() - Failed loading backup layout file.");
                    return false;
                }
            }
            else {
                // Successful loading of layout file information
                break;
            }
        }

        // Load successful, make a backup copy of the layout file. (only if not loading from backup)
        // TODO: Perhaps we should call the save function here instead of just making a copy? This will ensure that even if
        //       the original file read failed, it will still save a new working version of the layout file?
        if (!loadFromBackup) {
            try {
                Util.copyFile(layoutFile, backupLayoutFile);
            } catch(IOException e) {
                System.err.println("NetworkViewer.restoreNodeLayout() - IOException encountered attempting to copyFile: " +
                        e.getMessage());
            }
        }

        for (UINeoNode node : getUINodes()) {
            Float[] xy = nodeXY.get(node.getFullName());

            if (xy != null) {
                if (!enableElasticMode) {
                    node.animateToPositionScaleRotation(xy[0], xy[1], 1, 0, 700);
                } else {
                    node.setOffset(xy[0], xy[1]);
                }
            }
        }

        if (fullBounds != null) {
            zoomToBounds(fullBounds, 700);
        }

        /*
        if (enableElasticMode) {
            getGround().setElasticEnabled(true);
        }
        */

        return fullBounds != null;
    }

    /**
     * 
     */
    public void saveNodeLayout() {
        StringBuilder newfile = new StringBuilder();
        if (layoutFile.exists()) {
            try {
                Util.copyFile(layoutFile, backupLayoutFile);
            } catch(IOException e) {
                System.err.println("NetworkViewer.saveNodeLayout() - IOException encountered attempting to copyFile: " +
                        e.getMessage());
            }

            try {
                BufferedReader reader = new BufferedReader(new FileReader(backupLayoutFile));
                String line = null;

                while((line = reader.readLine()) != null) {
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        newfile.append(line).append('\n');
                    }
                }
                reader.close();
            } catch(IOException e) {
                System.err.println("NetworkViewer.saveNodeLayout() - IOException encountered attempting to create BufferedReader: " +
                        e.getMessage());
            }
        }

        newfile.append("##############################\n");
        newfile.append("### Nengo Workspace layout ###\n");
        newfile.append("##############################\n");
        //newfile.append("# elasticmode=").append(Boolean.toString(getGround().isElasticMode())).append('\n');
        newfile.append("# viewbounds=").append(getSky().getViewBounds().toString()).append('\n');
        for (UINeoNode object : getUINodes()) {
            newfile.append("# ").append(object.getFullName()).append('=').append(object.getOffset().toString()).append('\n');
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(layoutFile));

            bw.write(newfile.toString());
            bw.close();
        } catch (IOException e) {
            System.err.println("NetworkViewer.saveNodeLayout() - IOException encountered attempting to write to file: " + e.getMessage());
        }
    }

    public void updateSimulatorProbes() {
        /*
         * Construct probes
         */
        Probe[] probesArray = getModel().getSimulator().getProbes();

        /*
         * Hashset of probes
         */
        HashSet<Probe> probeToAdd = new HashSet<Probe>(probesArray.length);
        Collections.addAll(probeToAdd, probesArray);

        /*
         * Get current probes in UI
         */
        LinkedList<UIStateProbe> probesToDestroy = new LinkedList<UIStateProbe>();
        for (UINeoNode<?> nodeUI : getUINodes()) {
            for (UIProbe probeUI : nodeUI.getProbes()) {
                if (probeUI instanceof UIStateProbe) {
                    UIStateProbe stateProbe = (UIStateProbe) probeUI;
                    if (probeToAdd.contains(stateProbe.node())) {
                        probeToAdd.remove(stateProbe.node());
                    } else {
                        probesToDestroy.add(stateProbe);

                    }
                }
            }
        }

        /*
         * Remove probes
         */
        for (UIStateProbe probeUI : probesToDestroy) {
            probeUI.destroy();
        }

        /*
         * Add probes
         */
        for (Probe probe : probeToAdd) {
            Probeable target = probe.getTarget();

            if (!(target instanceof Node)) {
                UserMessages.showError("Unsupported target type for probe");
            } else {

                if (!probe.isInEnsemble()) {

                    Node node = (Node) target;

                    UINeoNode nodeUI = getUINode(node);
                    if (nodeUI != null) {
                        nodeUI.showProbe(probe);
                    } else {
                        Util.debugMsg("There is a dangling probe in the Simulator");
                    }
                }
            }
        }

    }

    /**
     * Action to restore a layout
     * 
     * @author Shu Wu
     */
    class RestoreLayout extends StandardAction {
        private static final long serialVersionUID = 1L;

        final String layoutName;

        public RestoreLayout(String name) {
            super("Restore layout: " + name, name);
            this.layoutName = name;
        }

        @Override
        protected void action() throws ActionException {
            if (!restoreNodeLayout()) {
                throw new ActionException("Could not restore layout");
            }
        }
    }

    /**
     * Action to save a layout
     * 
     * @author Shu Wu
     */
    class SaveLayout extends StandardAction {
        private static final long serialVersionUID = 1L;

        public SaveLayout() {
            super("Save layout");
        }

        @Override
        protected void action() throws ActionException {
            String name = JOptionPane.showInputDialog(UIEnvironment.getInstance(), "Name");

            if (name != null) {
                saveNodeLayout();
            } else {
                throw new ActionException("Could not get layout name", false);
            }

        }

    }

    /**
     * Action to hide all widgets
     * 
     * @author Shu Wu
     */
    class SetOTVisiblityAction extends StandardAction {

        private static final long serialVersionUID = 1L;

        private final boolean visible;

        public SetOTVisiblityAction(String actionName, boolean visible) {
            super(actionName);
            this.visible = visible;
        }

        @Override
        protected void action() throws ActionException {
            setOriginsTerminationsVisible(visible);
        }

    }

    public Node getNodeModel(String name) {
        try {
            return getModel().getNode(name);
        } catch (StructuralException e) {
            // Node does not exist
            return null;
        }
    }



    public UINeoNode addNodeModel(Node node) throws ContainerException {
        return addNodeModel(node, null, null);
    }

    public UINeoNode addNodeModel(Node node, Double posX, Double posY) throws ContainerException {
        try {
            // first, add node to UI
            UINeoNode nodeUI = UINeoNode.createNodeUI(node);

            addNodeModel(nodeUI, posX, posY);

            // second, add node to model. This must be done second, otherwise
            // it updates the view and there is a race to add the UI node
            getModel().addNode(node);

            return nodeUI;
        } catch (StructuralException e) {
            throw new ContainerException(e.toString());
        }
    }

    @Override
    public WorldObject addNodeModel(WorldObject nodeUI, Double posX, Double posY) throws ContainerException {

        UINeoNode u;
        if (posX != null && posY != null) {
            nodeUI.setOffset(posX, posY);
            u = addUINode((UINeoNode) nodeUI, false, false);
        } else {
            u = addUINode((UINeoNode) nodeUI, true, false);
        }
        neoNodesChildren.put(u.node(),u);

        return nodeUI;
    }
}

class ShowHideHandler extends PBasicInputEventHandler {
    private final WorldObject[] toHide;

    public ShowHideHandler(WorldObject[] toHide) {
        this.toHide = toHide;
    }

    @Override
    public void mouseEntered(PInputEvent event) {
        for (WorldObject wo : this.toHide) {
            wo.animateToTransparency(1.0f, 200);
        }
    }

    @Override
    public void mouseExited(PInputEvent event) {
        if (event.getPickedNode().getGlobalBounds().contains(event.getPosition())) {
            return;
        }

        for (WorldObject wo : this.toHide) {
            wo.animateToTransparency(0.0f, 200);
        }
    }
}
