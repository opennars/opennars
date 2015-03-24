/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NodeViewer.java". Description:
"Viewer for looking at NEO Node models

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

package ca.nengo.ui.model.viewer;

import ca.nengo.model.Group;
import ca.nengo.model.Node;
import ca.nengo.ui.lib.action.LayoutAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.object.activity.TrackedStatusMsg;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.elastic.ElasticGround;
import ca.nengo.ui.lib.world.handler.AbstractStatusHandler;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.model.ModelsContextMenu;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.node.UINodeViewable;
import com.google.common.collect.Iterables;
import javolution.util.FastMap;
import org.piccolo2d.activities.PActivity;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.util.PBounds;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;


/**
 * Viewer for looking at NEO Node models
 * 
 * @author Shu
 */
public abstract class NodeViewer extends WorldImpl implements Interactable {

    private MyNodeListener myNodeListener;
    private Boolean justOpened;

    /**
     * Viewer Parent
     */
    private final UINodeViewable parentOfViewer;

    /**
     * Children of NEO nodes
     */
    protected final Map<Node, UINeoNode> neoNodesChildren = new FastMap().atomic(); //Parameters.newHashMap();

    public NodeViewer(UINodeViewable nodeContainer) {
        this(nodeContainer, new ElasticGround());
    }
    /**
     * @param nodeContainer
     *            UI Object containing the Node model
     */
    public NodeViewer(UINodeViewable nodeContainer, WorldGroundImpl ground) {
        super(nodeContainer.name() + ":", ground);
        this.parentOfViewer = nodeContainer;
        this.justOpened = false;

        initialize();
    }

    private void initChildModelListener() {
        myNodeListener = new MyNodeListener();

        getGround().addChildrenListener(new WorldObject.ChildListener() {
            public void childAdded(WorldObject wo) {

                if (wo instanceof UINeoNode) {
                    ((UINeoNode) wo).addModelListener(myNodeListener);
                }
            }

            public void childRemoved(WorldObject wo) {
                if (wo instanceof UINeoNode) {
                    ((UINeoNode) wo).removeModelListener(myNodeListener);
                }
            }
        });
    }

    public Iterator<Node> getNodeModels() {
        return neoNodesChildren.keySet().iterator();
    }

    public Iterable<? extends WorldObject> getWorldObjects() {
        return neoNodesChildren.values();
    }
    public Boolean getJustOpened() {
        return justOpened;
    }

    public void setJustOpened(Boolean justOpened) {
        this.justOpened = justOpened;
    }

    /**
     * @param node
     *            node to be added
     * @param updateModel
     *            if true, the network model is updated. this may be false, if
     *            it is known that the network model already contains this node
     * @param dropInCenterOfCamera
     *            whether to drop the node in the center of the camera
     * @param moveCameraToNode
     *            whether to move the camera to where the node is
     */
    protected UINeoNode addUINode(UINeoNode node, boolean dropInCenterOfCamera, boolean moveCameraToNode) {

        /**
         * Moves the camera to where the node is positioned, if it's not dropped
         * in the center of the camera
         */
        if (moveCameraToNode) {
            getWorld().animateToSkyPosition(node.getOffset().getX(), node.getOffset().getY());
        }

        neoNodesChildren.put(node.node(), node);

        if (dropInCenterOfCamera) {
            getGround().addChildFancy(node, dropInCenterOfCamera);
        } else {
            getGround().addChild(node);
        }

        return node;
    }

    public Point2D localToView(Point2D localPoint) {
        localPoint = getSky().parentToLocal(localPoint);
        localPoint = getSky().localToView(localPoint);
        return localPoint;
    }

    public void doSortByNameLayout(){
        applySortLayout(SortMode.BY_NAME);
    }

    protected abstract boolean canRemoveChildModel(Node node);

    /*   @Override
        protected void constructLayoutMenu(MenuBuilder menu) {
        super.constructLayoutMenu(menu);
        MenuBuilder sortMenu = menu.addSubMenu("Sort");

        sortMenu.addAction(new SortNodesAction(SortMode.BY_NAME));
        sortMenu.addAction(new SortNodesAction(SortMode.BY_TYPE));
    }
     */
    protected void initialize() {
        initChildModelListener();

        setStatusBarHandler(new NodeViewerStatus(this));

        TrackedStatusMsg msg = new TrackedStatusMsg("Building..");

        updateViewFromModel(true);

        msg.finished();

    }

    protected abstract void removeChildModel(Node node);

    /**
     * Called when the model changes. Updates the viewer based on the NEO model.
     */
    protected abstract void updateViewFromModel(boolean isFirstUpdate);

    /**
     * Applies the default layout
     */
    public abstract void applyDefaultLayout();

    /**
     * Applies a square layout which is sorted
     * 
     * @param sortMode
     *            Type of sort layout to use
     */
    public void applySortLayout(SortMode sortMode) {
        NodeViewer.this.sortLayout(this, sortMode);

    }

    public static void sortLayout(WorldImpl nodeViewer, SortMode sortMode) {
        //getGround().setElasticEnabled(false);

        List<WorldObject> nodes = new ArrayList();
        Iterables.addAll(nodes, nodeViewer.getChildren());


        switch (sortMode) {
            case BY_NAME:
                Collections.sort(nodes, new Comparator<WorldObject>() {

                    public int compare(WorldObject o1, WorldObject o2) {
                        return (o1.name().compareToIgnoreCase(o2.name()));

                    }

                });

                break;
            case BY_TYPE:
                Collections.sort(nodes, new Comparator<WorldObject>() {

                    public int compare(WorldObject o1, WorldObject o2) {
                        if (o1.getClass() != o2.getClass()) {

                            return o1.getClass().getSimpleName().compareToIgnoreCase(
                                    o2.getClass().getSimpleName());
                        } else {
                            return (o1.name().compareToIgnoreCase(o2.name()));
                        }

                    }

                });

                break;
        }

        /*
         * basic rectangle layout variables
         */
        double x = 0;
        double y = 0;

        int numberOfColumns = (int) Math.sqrt(nodes.size());
        int columnCounter = 0;

        double startX = Double.MAX_VALUE;
        double startY = Double.MAX_VALUE;
        double maxRowHeight = 0;
        double endX = Double.MIN_VALUE;
        double endY = Double.MIN_VALUE;

        if (nodes.size() > 0) {
            for (WorldObject node : nodes) {


                node.animateToPosition(x, y, 1000);

                if (x < startX) {
                    startX = x;
                } else if (x + node.getWidth() > endX) {
                    endX = x + node.getWidth();
                }

                if (y < startY) {
                    startY = y;
                } else if (y + node.getHeight() > endY) {
                    endY = y + node.getHeight();
                }

                Rectangle2D fb = node.getFullBoundsReference();
                if (fb.getHeight() > maxRowHeight) {
                    maxRowHeight = fb.getHeight();
                }

                x += fb.getWidth() + 50;

                if (++columnCounter > numberOfColumns) {
                    x = 0;
                    y += maxRowHeight + 50;
                    maxRowHeight = 0;
                    columnCounter = 0;
                }
            }

        }


        PBounds fullBounds = new PBounds(startX, startY, endX - startX, endY - startY);
        nodeViewer.zoomToBounds(fullBounds);


    }

    //    @Override
    //    public void constructMenu(PopupMenuBuilder menu, Double posX, Double posY) {
    //        super.constructMenu(menu, posX, posY);
    //
    //        // File menu
    //        //	menu.addSection("File");
    //        //	menu.addAction(new SaveNodeAction(getViewerParent()));
    //
    //    }

    /**
     * @return NEO Model represented by the viewer
     */
    public Group getModel() {
        return (Group) parentOfViewer.node();
    }

    /**
     * @return A collection of NEO Nodes contained in this viewer
     */
    public Collection<UINeoNode> getUINodes() {
    	//return new ArrayList<UINeoNode>(neoNodesChildren.values());
        return neoNodesChildren.values();
    }

    public UINeoNode getUINode(Node node) {
        return neoNodesChildren.get(node);
    }

    @Override
    protected void constructSelectionMenu(Collection<WorldObject> selection, PopupMenuBuilder menu) {
        super.constructSelectionMenu(selection, menu);
        ArrayList<ModelObject> models = new ArrayList<ModelObject>(selection.size());

        for (WorldObject object : selection) {
            if (object instanceof ModelObject) {
                models.add((ModelObject) object);
            }
        }

        ModelsContextMenu.constructMenu(menu, models);

    }

    /**
     * @return Parent of this viewer
     */
    public UINodeViewable getViewerParent() {
        return parentOfViewer;
    }

    public void setOriginsTerminationsVisible(boolean visible) {
        for (UINeoNode node : getUINodes()) {
            node.setWidgetsVisible(visible);
        }
    }

    public void updateViewFromModel() {
        updateViewFromModel(false);
    }

    private class MyNodeListener implements ModelObject.ModelListener {

        public void modelDestroyed(Object model) {
            removeChildModel((Node) model);
        }

        public void modelDestroyStarted(Object model) {
            if (!canRemoveChildModel((Node) model)) {
                throw new UnsupportedOperationException("Removing nodes not supported here");
            }

        }

    }

    /**
     * Supported types of sorting allowed in layout
     * 
     * @author Shu Wu
     */
    public static enum SortMode {
        BY_NAME("Name"), BY_TYPE("Type");

        private final String name;

        SortMode(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }
    }

    /**
     * Action to apply a sorting layout
     * 
     * @author Shu Wu
     */
    class SortNodesAction extends LayoutAction {

        private static final long serialVersionUID = 1L;
        final SortMode sortMode;

        public SortNodesAction(SortMode sortMode) {
            super(NodeViewer.this, "Sort nodes by " + sortMode.getName(), sortMode.getName());
            this.sortMode = sortMode;
        }

        @Override
        protected void applyLayout() {
            applySortLayout(sortMode);
        }

    }

    /**
     * Zooms the viewer to optimally fit all nodes
     * 
     * @author Shu Wu
     */
    class ZoomToFitActivity extends PActivity {

        public ZoomToFitActivity() {
            super(0);
            UIEnvironment.getInstance().addActivity(this);
        }

        @Override
        protected void activityStarted() {
            zoomToFit();
        }

    }

}

/**
 * Handler which updates the status bar of NeoGraphics to display information
 * about the node which the mouse is hovering over.
 * 
 * @author Shu Wu
 */
class NodeViewerStatus extends AbstractStatusHandler {

    public NodeViewerStatus(NodeViewer world) {
        super(world);
    }

    StringBuilder statusStr = new StringBuilder(200);

    @Override
    protected String getStatusMessage(PInputEvent event) {
        statusStr.setLength(0);

        ModelObject wo = (ModelObject) Util.getNodeFromPickPath(event, ModelObject.class);

        /*if (getWorld().getGround().isElasticMode()) {
            statusStr.append("(Elastic) | ");
        }*/
        statusStr.append(getWorld().getViewerParent().getFullName()).append(" -> ");

        if (getWorld().getSelection().size() > 1) {
            statusStr.append(getWorld().getSelection().size()).append(" objects selected");

        } else {

            if (wo != null) {
                statusStr.append(wo.getFullName());
            } else {
                return "Nothing selected";
            }
        }
        return statusStr.toString();
    }

    @Override
    protected NodeViewer getWorld() {
        return (NodeViewer) super.getWorld();
    }

}
