/*
 * Copyright (c) 2002-@year@, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Piccolo was written at the Human-Computer Interaction Laboratory www.cs.umd.edu/hcil by Jesse Grosjean
 * under the supervision of Ben Bederson. The Piccolo website is www.cs.umd.edu/hcil/piccolo.
 */
package ca.nengo.ui.lib.world.handler;

import ca.nengo.ui.lib.action.DragAction;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.WorldSkyImpl;
import ca.nengo.ui.lib.world.piccolo.object.SelectionBorder;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.lib.world.piccolo.primitive.PiccoloNodeInWorld;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.node.UINodeViewable;
import ca.nengo.ui.model.viewer.NodeViewer;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PDragSequenceEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventFilter;
import org.piccolo2d.extras.event.PNotificationCenter;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PDimension;
import org.piccolo2d.util.PNodeFilter;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;


/**
 * <code>PSelectionEventHandler</code> provides standard interaction for
 * selection. Clicking selects the object under the cursor. Shift-clicking
 * allows multiple objects to be selected. Dragging offers marquee selection.
 * Pressing the delete key deletes the selection by default.
 *
 * @author Ben Bederson, modified by Shu Wu
 * @version 1.0
 */
public class SelectionHandler extends PDragSequenceEventHandler {

    ///////////////////////////////////////////////////////////////////////////
    /// Constants
    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION";
    public static final String SELECTION_HANDLER_FRAME_ATTR = "SelHandlerFrame";

    final static int DASH_WIDTH = 5;
    final static int NUM_STROKES = 10;

    ///////////////////////////////////////////////////////////////////////////
    /// Static members

    /// this code allows other classes to be notified when the selection changes
    private static final LinkedHashSet<SelectionListener> selectionListeners = new LinkedHashSet<SelectionListener>();
    // this code keeps track of which selection handler (of all selection handlers)
    // is active (i.e., has changed most recently)
    private static final LinkedHashSet<SelectionHandler> selectionHandlers = new LinkedHashSet<SelectionHandler>();
    private static SelectionHandler activeSelectionHandler = null;
    private final WorldImpl world; // associated world
    private final PanEventHandler panHandler;
    private Collection<WorldObjectImpl> selectedObjects = null; // current selected objects
    private Collection<WorldObjectImpl> allObjects = null;      // used in marquee code
    private DragAction dragAction;
    //private PPath marquee = null;
    private Paint marqueePaint;
    private float marqueePaintTransparency = 1.0f;
    private WorldSkyImpl marqueeParent = null; // Node that marquee is added to
    private WorldObjectImpl pressNode = null; // Node pressed on (or null if none)
    private Point2D presspt = null;
    private Point2D canvasPressPt = null;
    private WorldGroundImpl selectableParent = null; // ground of associated world
    private float strokeNum = 0;
    private Stroke[] strokes = null;

    /**
     * Creates a selection event handler.
     *
     * @param marqueeParent    The node to which the event handler dynamically adds a marquee
     *                         (temporarily) to represent the area being selected.
     * @param selectableParent The node whose children will be selected by this event
     *                         handler.
     */
    public SelectionHandler(WorldImpl world, PanEventHandler panHandler) {
        this.world = world;
        this.marqueeParent = world.getSky();
        this.selectableParent = world.getGround();
        this.panHandler = panHandler;
        panHandler.setSelectionHandler(this);
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));

        float[] dash = {DASH_WIDTH, DASH_WIDTH};
        strokes = new Stroke[NUM_STROKES];
        for (int i = 0; i < NUM_STROKES; i++) {
            strokes[i] = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, i);
        }

        selectedObjects = new LinkedHashSet();
        allObjects = new LinkedHashSet();
        //marqueeObjects = new LinkedHashSet();

        addSelectionHandler(this);
    }

    public static void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Private members

    public static void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(listener);
    }

    public static void selectionChanged(Collection<WorldObject> objs) {
        for (SelectionListener listener : selectionListeners) {
            listener.selectionChanged(objs);
        }
    }
    ///private Collection<WorldObjectImpl> marqueeObjects = null;  // used in marquee code

    private static void addSelectionHandler(SelectionHandler s) {
        selectionHandlers.add(s);
    }

    private static void removeSelectionHandler(SelectionHandler s) {
        selectionHandlers.remove(s);
    }

    public static SelectionHandler getActiveSelectionHandler() {
        return activeSelectionHandler;
    }

    /**
     * @return the selected objects in the active selection handler
     */
    public static List<WorldObject> getActiveSelection() {
        if (getActiveSelectionHandler() == null)
            return Collections.emptyList();
        else
            return getActiveSelectionHandler().getSelection();
    }
    //private Paint marqueeStrokePaint;

    /**
     * @return the last element in the list of active selected objects (or
     * null if no object selected)
     */
    public static WorldObject getActiveObject() {
        List<WorldObject> s = getActiveSelection();
        if (!s.isEmpty())
            return s.get(s.size() - 1); // return last item
        else
            return null;
    }

    public static Object getActiveModel() {
        WorldObject obj = getActiveObject();
        while (obj != null)
            if (obj instanceof ModelObject)
                return ((ModelObject) obj).node();
            else
                obj = obj.getParent();

        return null;
    }

    public static NodeViewer getActiveViewer() {
        WorldObject wo = getActiveObject();
        NodeViewer viewer = null;
        if (wo instanceof UINodeViewable) {
            UINodeViewable node = (UINodeViewable) wo;
            if (node.isViewerWindowVisible())
                viewer = node.getViewer();
        }
        if (viewer == null)
            viewer = getParentViewer(wo);

        return viewer;
    }

    public static UINetwork getActiveNetwork(boolean toplevel) {
        WorldObject wo = getActiveObject();
        UINetwork net = null;
        if (wo instanceof UINetwork) {
            net = (UINetwork) wo;
        } else {
            net = getParentNetwork(wo);
        }

        if (toplevel && net != null) {
            UINetwork netparent = net.getNetworkParent();
            while (netparent != null) {
                net = netparent;
                netparent = net.getNetworkParent();
            }
        }

        return net;
    }

    /**
     * Find the NodeViewer parent of a WorldObject
     *
     * @param wo the world object to start the search from
     * @return the NodeViewer parent of the world object, null if it does not exist
     */
    protected static NodeViewer getParentViewer(WorldObject wo) {
        if (wo instanceof NodeViewer)
            return (NodeViewer) wo;

        while (wo != null) {
            if (wo instanceof WorldGroundImpl) {
                World world = wo.getWorld();
                if (world instanceof NodeViewer) {
                    return (NodeViewer) world;
                }
            }
            wo = wo.getParent();
        }
        return null;
    }

    /**
     * Find the UINetwork parent of a WorldObject
     *
     * @param wo the world object to start the search from
     * @return the UINetwork parent of the world object, null if it does not exist
     */
    protected static UINetwork getParentNetwork(WorldObject wo) {
        if (wo instanceof UINetwork)
            return ((UINetwork) wo).getNetworkParent();

        while (wo != null) {
            if (wo instanceof UINetwork) {
                return (UINetwork) wo;
            } else if (wo instanceof WorldGroundImpl) {
                World world = wo.getWorld();
                if (world instanceof NodeViewer) {
                    wo = ((NodeViewer) world).getViewerParent();
                    continue;
                }
            }
            wo = wo.getParent();
        }
        return null;
    }

    // moves a WorldObject, and all the Windows it lies in, to the front
    protected static void moveStackToFront(WorldObject wo) {
        wo.moveToFront();

        // move all parent network windows to the front
        UINetwork pnet = getParentNetwork(wo);
        while (pnet != null) {
            if (pnet.isViewerWindowVisible()) {
                pnet.moveViewerWindowToFront();
            }
            pnet = getParentNetwork(pnet);
        }
    }

    public void finalize() {

        removeSelectionHandler(this);
        try {
            super.finalize();
        } catch (Throwable throwable) {

        }
    }

    private boolean isActiveSelectionHandler() {
        return (this == getActiveSelectionHandler());
    }

    private static void setActiveSelectionHandler(SelectionHandler activeS) {
        activeSelectionHandler = activeS;
        for (SelectionHandler s : selectionHandlers) {
            if (s != activeS)
                s.unselectAll();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Selection changed

    private void activeSelectionChanged() {
        setActiveSelectionHandler(this);
        PNotificationCenter.defaultCenter().postNotification(SELECTION_CHANGED_NOTIFICATION, this);
        selectionChanged(getSelection());
    }

    private void passiveSelectionChanged() {
        PNotificationCenter.defaultCenter().postNotification(SELECTION_CHANGED_NOTIFICATION, this);
        if (isActiveSelectionHandler())
            selectionChanged(getSelection());
    }

    protected void initializeSelection(PInputEvent pie) {
        canvasPressPt = pie.getCanvasPosition();
        presspt = pie.getPosition();

        pressNode = null;
        for (PNode node = pie.getPath().getPickedNode(); node != null; node = node.getParent()) {
            if (node instanceof PiccoloNodeInWorld) {
                WorldObjectImpl wo = (WorldObjectImpl) ((PiccoloNodeInWorld) node).getWorldObject();

                if (wo != null && wo.isSelectable()) {
                    if (!(wo instanceof Window && ((Window) wo).isMaximized())) {
                        // this object is not a maximized window, so move to front
                        moveStackToFront(wo);
                    }

                    if (wo instanceof Window) {
                        // select the parent (Network, ensemble, etc.) of the clicked window
                        WorldObjectImpl parent = (WorldObjectImpl) wo.getParent();
                        if (parent != null && parent.getWorld() != null) {
                            parent.getWorld().getSelectionHandler().unselectAll();
                            parent.getWorld().getSelectionHandler().select(parent);
                        }
                    }

                    pie.setHandled(true);
                    pressNode = wo;
                    return;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Selection

    /**
     * Determine if the specified node is selectable (i.e., if it is a child of
     * the one the list of selectable parents.
     */
    protected boolean isSelectable(WorldObject node) {
        return (node != null && node.getVisible() && selectableParent.isAncestorOf(node));
    }

    protected void startStandardOptionSelection(PInputEvent pie) {
        // Option indicator is down, toggle selection
        if (isSelectable(pressNode)) {
            if (isSelected(pressNode)) {
                unselect(pressNode);
            } else {
                select(pressNode);
            }
        }
        pie.setHandled(true);
    }

    protected void startStandardSelection(PInputEvent pie) {
        // Option indicator not down - clear selection, and start fresh
        if (!isSelected(pressNode)) {
            unselectAll();

            if (isSelectable(pressNode)) {
                select(pressNode);
            }
        }
        pie.setHandled(true);
    }

    public boolean isOptionSelection(PInputEvent pie) {
        return pie.isShiftDown();
    }

    public boolean isSelected(WorldObjectImpl node) {
        return (node != null && selectedObjects.contains(node));
    }

    public void decorateSelectedNode(WorldObjectImpl node) {
        SelectionBorder frame = new SelectionBorder(world, node);

        node.setSelected(true);
        node.getPNode().addAttribute(SELECTION_HANDLER_FRAME_ATTR, frame);
    }

    public void undecorateSelectedNode(WorldObjectImpl node) {
        SelectionBorder frame = (SelectionBorder) node.getPNode().getAttribute(SELECTION_HANDLER_FRAME_ATTR);
        if (frame != null && frame instanceof SelectionBorder) {
            frame.destroy();
        }
        node.setSelected(false);
        node.getPNode().addAttribute(SELECTION_HANDLER_FRAME_ATTR, null);
    }

    /**
     * Returns a copy of the currently selected nodes.
     */
    public ArrayList<WorldObject> getSelection() {
        ArrayList<WorldObject> sel = new ArrayList<WorldObject>(selectedObjects);

        boolean changes = false;
        for (int i = sel.size() - 1; i >= 0; i--) {
            WorldObject wo = sel.get(i);
            if (!wo.getVisible()) {
                //internalUnselect(sel.get(i));
                selectedObjects.remove(wo);
                sel.remove(i);
                changes = true;
            }
        }
        if (changes)
            passiveSelectionChanged();

        return sel;
    }

    private boolean internalSelect(WorldObjectImpl node) {
        if (!isSelected(node)) {
            selectedObjects.add(node);
            decorateSelectedNode(node);
            return true;
        } else
            return false;
    }

    public void select(Collection<WorldObjectImpl> items) {
        boolean changes = false;
        Iterator<WorldObjectImpl> itemIt = items.iterator();
        while (itemIt.hasNext()) {
            WorldObjectImpl node = itemIt.next();
            changes |= internalSelect(node);
        }
        if (changes) {
            activeSelectionChanged();
        }
    }

    public void select(WorldObjectImpl node) {
        select(Arrays.asList(new WorldObjectImpl[]{node}));
    }

    private boolean internalUnselect(WorldObjectImpl node) {
        if (isSelected(node)) {
            undecorateSelectedNode(node);
            selectedObjects.remove(node);
            return true;
        } else
            return false;
    }

    public void unselect(Collection<WorldObjectImpl> items) {
        boolean changes = false;
        Iterator<WorldObjectImpl> itemIt = items.iterator();
        while (itemIt.hasNext()) {
            WorldObjectImpl node = itemIt.next();
            changes |= internalUnselect(node);
        }
        if (changes) {
            passiveSelectionChanged();
        }
    }

    public void unselect(WorldObjectImpl node) {
        unselect(Arrays.asList(new WorldObjectImpl[]{node}));
    }

    public void unselectAll() {
        // Because unselect() removes from selection, we need to
        // take a copy of it first so it isn't changed while we're iterating
        unselect(new ArrayList<WorldObjectImpl>(selectedObjects));
    }

    protected void startDrag(PInputEvent e) {


        super.startDrag(e);


        initializeSelection(e);

        if (shouldStartMarqueeMode()) {
            initializeMarquee(e);

            if (!isOptionSelection(e)) {
                startMarqueeSelection(e);
            } else {
                startOptionMarqueeSelection(e);
            }
        } else {
            if (!isOptionSelection(e)) {
                startStandardSelection(e);
            } else {
                startStandardOptionSelection(e);
            }

            Collection<WorldObject> nodes = getSelection();
            if (nodes.size() > 0) {
                dragAction = new DragAction(nodes);
                panHandler.setInverted(true);
            }

        }


    }

    ///////////////////////////////////////////////////////////////////////////
    /// Dragging, including overridden methods from PDragSequenceEventHandler

    protected void drag(PInputEvent e) {

        super.drag(e);


		/*if (shouldStartMarqueeMode() && marquee != null) {
            updateMarquee(e);

			if (!isOptionSelection(e)) {
				computeMarqueeSelection(e);
			} else {
				computeOptionMarqueeSelection(e);
			}
		} else */


        Iterator<WorldObjectImpl> selectionEn = selectedObjects.iterator();

        if (selectionEn.hasNext()) {
            try {
                PDimension d = e.getDeltaRelativeTo(selectableParent.getPNode());

                final long now = System.currentTimeMillis();

                while (selectionEn.hasNext()) {
                    WorldObjectImpl node = selectionEn.next();
                    PDimension gDist = new PDimension();
                    gDist.setSize(d);

                    node.localToParent(node.globalToLocal(gDist));

                    node.dragOffset(gDist.getWidth(), gDist.getHeight());

                    if (node.isAnimating(now)) {
                        double newX = node.getPNode().getOffset().getX();
                        double newY = node.getPNode().getOffset().getY();
                        node.animateToPosition(newX, newY, 0);
                    }


                }
            } catch (RuntimeException ee) {

/*
Exception in thread "AWT-EventQueue-0" java.lang.RuntimeException: Node could not be found on pick path
at org.piccolo2d.util.PPickPath.getPathTransformTo(PPickPath.java:320)
at org.piccolo2d.util.PPickPath.canvasToLocal(PPickPath.java:393)
at org.piccolo2d.event.PInputEvent.getDeltaRelativeTo(PInputEvent.java:569)
*/
                return;
            }
        }


    }

    /**
     * This gets called continuously during the drag, and is used to animate the
     * marquee
     */
    protected void dragActivityStep(PInputEvent aEvent) {

		/*if (marquee != null) {
			float origStrokeNum = strokeNum;

			// Increment by partial steps to slow down animation
			strokeNum = (strokeNum + 0.5f) % NUM_STROKES;
			if ((int) strokeNum != (int) origStrokeNum) {
				marquee.setStroke(strokes[(int) strokeNum]);
			}
		}*/
    }

    protected void endDrag(PInputEvent e) {
        super.endDrag(e);
        panHandler.setInverted(false);
        endSelection(true);
    }

    public void endSelection(boolean unselect) {
		/*if (marquee != null) {
			// Remove marquee
			marquee.removeFromParent();
			marquee = null;
		}*/
        if (!shouldStartMarqueeMode()) {
            // store the parent, in case pressNode is destroyed and we want to select it
            WorldObject parent = null;
            if (pressNode != null)
                parent = pressNode.getParent();

            // end the drag action
            if (dragAction != null) {
                dragAction.setFinalPositions();
                dragAction.doAction();
                dragAction = null;
            }

            // if pressNode is destroyed, unselect it and try to find a valid parent to select
            if (pressNode != null && pressNode.isDestroyed()) {
                while (parent != null && (parent.isDestroyed() || !isSelectable(parent))) {
                    parent = parent.getParent();
                }
                if (parent instanceof WorldObjectImpl) {
                    internalUnselect(pressNode);
                    select((WorldObjectImpl) parent);
                } else {
                    unselect(pressNode);
                }
            }

            // if a window was dragged, deselect it
            if (unselect && (pressNode == null || pressNode instanceof Window)) {
                unselectAll();
            }

            // cleanup
            endStandardSelection();
        }
    }

    protected void endStandardSelection() {
        pressNode = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Marquee
    protected void initializeMarquee(PInputEvent e) {
		/*marquee = PPath.createRectangle((float) presspt.getX(), (float) presspt.getY(), 0, 0);
		marquee.setPaint(marqueePaint);
		marquee.setTransparency(marqueePaintTransparency);
		marquee.setStrokePaint(marqueeStrokePaint);
		marquee.setStroke(strokes[0]);

		marqueeParent.getPiccolo().addChild(marquee);
		marqueeObjects.clear();*/
    }

    protected PBounds getMarqueeBounds() {
		/*if (marquee != null) {
			return marquee.getBounds();
		}*/
        return new PBounds();
    }

    /**
     * Indicates the color used to paint the marquee.
     *
     * @return the paint for interior of the marquee
     */
    public Paint getMarqueePaint() {
        return marqueePaint;
    }

    /**
     * Sets the color used to paint the marquee.
     *
     * @param paint the paint color
     */
    public void setMarqueePaint(Paint paint) {
        this.marqueePaint = paint;
    }

    /**
     * Indicates the transparency level for the interior of the marquee.
     *
     * @return Returns the marquee paint transparency, zero to one
     */
    public float getMarqueePaintTransparency() {
        return marqueePaintTransparency;
    }

    /**
     * Sets the transparency level for the interior of the marquee.
     *
     * @param marqueePaintTransparency The marquee paint transparency to set.
     */
    public void setMarqueePaintTransparency(float marqueePaintTransparency) {
        this.marqueePaintTransparency = marqueePaintTransparency;
    }

    public void setMarqueeStrokePaint(Paint marqueeStrokePaint) {
        //this.marqueeStrokePaint = marqueeStrokePaint;
    }

    @SuppressWarnings("unchecked")
    protected void updateMarquee(PInputEvent pie) {
        PBounds b = new PBounds();

        if (marqueeParent.getPNode() instanceof PCamera) {
            b.add(canvasPressPt);
            b.add(pie.getCanvasPosition());
        } else {
            b.add(presspt);
            b.add(pie.getPosition());
        }

        b.reset();
        b.add(presspt);
        b.add(pie.getPosition());

        PBounds marqueeBounds = (PBounds) b.clone();

        selectableParent.globalToLocal(marqueeBounds);
        marqueeParent.viewToLocal(marqueeBounds);
        // marquee.globalToLocal(b);


		/*marquee.setBounds((float) marqueeBounds.x, (float) marqueeBounds.y, (float) marqueeBounds.width,
				(float) marqueeBounds.height);*/

        allObjects.clear();
        PNodeFilter filter = new BoundsFilter(b);

        Collection<PNode> items;

        items = selectableParent.getPNode().getAllNodes(filter, null);

        Iterator<PNode> itemsIt = items.iterator();
        while (itemsIt.hasNext()) {
            PNode next = itemsIt.next();
            if (next instanceof PiccoloNodeInWorld) {
                WorldObjectImpl wo = (WorldObjectImpl) ((PiccoloNodeInWorld) next).getWorldObject();
                if (wo.getVisible())
                    allObjects.add(wo);
            }

        }

    }

    protected void computeMarqueeSelection(PInputEvent pie) {
        ArrayList<WorldObjectImpl> unselectList = new ArrayList<WorldObjectImpl>();

        // Make just the items in the list selected
        // Do this efficiently by first unselecting things not in the list
        Iterator<WorldObjectImpl> selectionEn = selectedObjects.iterator();
        while (selectionEn.hasNext()) {
            WorldObjectImpl node = selectionEn.next();
            if (!node.getVisible() || !allObjects.contains(node)) {
                unselectList.add(node);
            }
        }
        unselect(unselectList);

        // Then select the rest
        selectionEn = allObjects.iterator();
        while (selectionEn.hasNext()) {
            WorldObjectImpl node = selectionEn.next();
			/*if (!selectedObjects.contains(node) && !marqueeObjects.contains(node) && isSelectable(node)) {
				marqueeObjects.add(node);
			} else */
            if (!isSelectable(node)) {
                selectionEn.remove();
            }
        }

        select(allObjects);
    }

    protected void computeOptionMarqueeSelection(PInputEvent pie) {
        ArrayList<WorldObjectImpl> unselectList = new ArrayList<WorldObjectImpl>();

        Iterator<WorldObjectImpl> selectionEn = selectedObjects.iterator();
        while (selectionEn.hasNext()) {
            WorldObjectImpl node = selectionEn.next();
            if (!allObjects.contains(node) /*&& marqueeObjects.contains(node)*/) {
                //marqueeObjects.remove(node);
                unselectList.add(node);
            }
        }
        unselect(unselectList);

        // Then select the rest
        selectionEn = allObjects.iterator();
        while (selectionEn.hasNext()) {
            WorldObjectImpl node = selectionEn.next();
			/*if (!selectedObjects.contains(node) && !marqueeObjects.contains(node) && isSelectable(node)) {
				marqueeObjects.add(node);
			} else */
            if (!isSelectable(node)) {
                selectionEn.remove();
            }
        }

        select(allObjects);
    }

    protected boolean shouldStartMarqueeMode() {
        return false;
        //return ((pressNode == null || pressNode instanceof Window) && world.isSelectionMode());
    }

    protected void startMarqueeSelection(PInputEvent e) {
        unselectAll();
    }

    protected void startOptionMarqueeSelection(PInputEvent e) {
        unselectAll();
    }

    public static interface SelectionListener {
        public void selectionChanged(Collection<WorldObject> obj);
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Inner classes

    protected class BoundsFilter implements PNodeFilter {
        final PBounds bounds;
        final PBounds localBounds = new PBounds();

        protected BoundsFilter(PBounds bounds) {
            this.bounds = bounds;
        }

        public boolean accept(PNode node) {
            localBounds.setRect(bounds);
            node.globalToLocal(localBounds);

            boolean boundsIntersects = node.intersects(localBounds);
            //boolean isMarquee = (node == marquee);
            boolean isMarquee = false;

            if (node instanceof PiccoloNodeInWorld) {
                WorldObject wo = ((PiccoloNodeInWorld) node).getWorldObject();

                if (wo.isSelectable()) {
                    return (node.getPickable() && boundsIntersects && !isMarquee && !(wo == selectableParent));
                }
            }
            return false;

        }

        public boolean acceptChildrenOf(PNode node) {
            return node == selectableParent.getPNode();
        }

    }


}