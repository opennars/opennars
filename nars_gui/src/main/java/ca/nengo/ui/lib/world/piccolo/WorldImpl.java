package ca.nengo.ui.lib.world.piccolo;


import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.action.PasteAction;
import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.RemoveObjectsAction;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.action.ZoomToFitAction;
import ca.nengo.ui.lib.menu.PopupMenuBuilder;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.handler.*;
import ca.nengo.ui.lib.world.piccolo.object.TooltipWrapper;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.lib.world.piccolo.primitive.PXGrid;
import ca.nengo.ui.lib.world.piccolo.primitive.PXLayer;
import ca.nengo.ui.model.NodeContainer;
import ca.nengo.ui.util.NengoClipboard;
import com.google.common.collect.Iterables;
import org.piccolo2d.PRoot;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.util.PBounds;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of World. World holds World Objects and has navigation and
 * interaction handlers.
 * 
 * @author Shu Wu
 */
public class WorldImpl extends WorldObjectImpl implements World, Interactable {

	/**
	 * Padding to use around objects when zooming in on them
	 */
	private static final double OBJECT_ZOOM_PADDING = 100;

	/**
	 * Whether tooltips are enabled
	 */
	private static boolean tooltipsEnabled = true;

	public static Rectangle2D getObjectBounds(Collection<WorldObject> objects) {
		double startX = Double.POSITIVE_INFINITY;
		double startY = Double.POSITIVE_INFINITY;
		double endX = Double.NEGATIVE_INFINITY;
		double endY = Double.NEGATIVE_INFINITY;

		for (WorldObject wo : objects) {
			Point2D position = wo.localToGlobal(new Point2D.Double(0, 0));
			Rectangle2D bounds = wo.localToGlobal(wo.getBounds());

			double x = position.getX();
			double y = position.getY();

			if (x < startX) {
				startX = x;
			}
			if (x + bounds.getWidth() > endX) {
				endX = x + bounds.getWidth();
			}

			if (y < startY) {
				startY = y;
			}
			if (y + bounds.getHeight() > endY) {
				endY = y + bounds.getHeight();
			}

		}

		if (objects.size() > 0) {
			return new Rectangle2D.Double(startX, startY, endX - startX, endY - startY);
		} else {
			throw new InvalidParameterException("no objects");
		}

	}

	public static boolean isTooltipsVisible() {
		return tooltipsEnabled;
	}

	public static void setTooltipsVisible(boolean tooltipsVisible) {
		WorldImpl.tooltipsEnabled = tooltipsVisible;
	}

	/**
	 * Layer attached to the camera which shows the zoomable grid
	 */
	private final PXGrid gridLayer;

	/**
	 * If true, then selection mode. If false, then navigation mode.
	 */
	private boolean isSelectionMode;

	private final KeyboardHandler keyboardHandler;

	/**
	 * PLayer which holds the ground layer
	 */
	private final PXLayer layer;

	/**
	 * Ground which can be zoomed and navigated
	 */
	private final WorldGroundImpl myGround;

	/**
	 * Sky, which looks at the ground and whose position and scale remains
	 * static
	 */
	private final WorldSkyImpl mySky;

	/**
	 * Panning handler
	 */
	private final PanEventHandler panHandler;

	/**
	 * Selection handler
	 */
	protected final SelectionHandler selectionEventHandler;

	/**
	 * Status bar handler
	 */
	private PBasicInputEventHandler statusBarHandler;

	/**
	 * @param name
	 * @param ground
	 */
	public WorldImpl(String name, WorldGroundImpl ground) {
		this(name, new WorldSkyImpl(), ground);
	}

	/**
	 * Default constructor
	 * 
	 * @param name
	 *            Name of this world
	 */
	public WorldImpl(String name, WorldSkyImpl sky, WorldGroundImpl ground) {
		super(name);

		/*
		 * Create layer
		 */
		layer = new PXLayer();
		getPRoot().addChild(layer);

		/*
		 * Create ground
		 */
		ground.setWorld(this);
		myGround = ground;
        layer.addChild(myGround.getPNode());

		/*
		 * Create sky
		 */
		mySky = sky;
		mySky.addLayer(layer);
		addChild(mySky);

		/*
		 * Create handlers
		 */
		panHandler = new PanEventHandler();
		keyboardHandler = new KeyboardHandler();
		mySky.getCamera().addInputEventListener(keyboardHandler);
		mySky.getCamera().addInputEventListener(new TooltipPickHandler(this, 1000, 0));
		mySky.getCamera().addInputEventListener(new MouseHandler(this));


		selectionEventHandler = new SelectionHandler(this, panHandler);
		selectionEventHandler.setMarqueePaint(NengoStyle.COLOR_BORDER_SELECTED);
		selectionEventHandler.setMarqueeStrokePaint(NengoStyle.COLOR_BORDER_SELECTED);
		selectionEventHandler.setMarqueePaintTransparency(0.1f);


        getPNode().addInputEventListener(new EventConsumer());
		setStatusBarHandler(new RootWorldStatusHandler(this));

		/*
		 * Set position and scale
		 */
		// animateToSkyPosition(0, 0);
		getSky().setViewScale(0.7f);

		/*
		 * Create the grid
		 */


		gridLayer = PXGrid.createGrid(getSky().getCamera(), UIEnvironment.getInstance()
                .getUniverse().getRoot(), NengoStyle.COLOR_DARKBORDER, 1500);

		/*
		 * Let the top canvas have a handle on this world
		 */
		UIEnvironment.getInstance().getUniverse().addWorld(this);

		/*
		 * Miscellaneous
		 */
		setBounds(0, 0, 1200, 800);

		initSelectionMode();

	}

    public PXGrid getGridLayer() {
        return gridLayer;
    }

    private PRoot getPRoot() {
		/*
		 * This world's root is always to top-level root associated with the
		 * canvas
		 */
		return UIEnvironment.getInstance().getUniverse().getRoot();
	}

	private void initSelectionMode() {
		isSelectionMode = false;
        mySky.getCamera().addInputEventListener(selectionEventHandler);
        mySky.getCamera().addInputEventListener(panHandler);
	}

	/**
	 * Create context menu
	 * 
	 * @return Menu builder
	 */
	protected void constructMenu(PopupMenuBuilder menu, Double posX, Double posY) {
		NengoClipboard clipboard = AbstractNengo.getInstance().getClipboard();
		if (clipboard.hasContents()) {
			ArrayList<String> clipboardNames = clipboard.getContentsNames();
			String selectionName = "";
			if (clipboardNames.size() == 1) {
				selectionName = clipboardNames.get(0);
			} else {
				selectionName = "selection";
			}
			PasteAction pasteAction = new PasteAction("Paste '" + selectionName + "' here", (NodeContainer)this, false);
			pasteAction.setPosition(posX, posY);
			menu.addAction(pasteAction);
		}
		menu.addAction(new ZoomToFitAction("Zoom to fit", this));
		/*MenuBuilder windowsMenu = menu.addSubMenu("Windows");
		windowsMenu.addAction(new CloseAllWindows("Close all"));
		windowsMenu.addAction(new MinimizeAllWindows("Minimize all"));*/
	}

	protected void constructSelectionMenu(Collection<WorldObject> selection, PopupMenuBuilder menu) {
		menu.addAction(new ZoomToFitAction("Zoom to fit", this));
		menu.addAction(new RemoveObjectsAction(selection, "Remove selected"));
	}

	@Override
	protected void prepareForDestroy() {
		UIEnvironment.getInstance().getUniverse().removeWorld(this);

		keyboardHandler.destroy();
		gridLayer.removeFromParent();
		layer.removeFromParent();

		getGround().destroy();
		getSky().destroy();

		super.prepareForDestroy();
	}

	/**
	 * Sets the view position of the sky, and animates to it.
	 * 
	 * @param x
	 *            X Position relative to ground
	 * @param y
	 *            Y Position relative to ground
	 */
	public void animateToSkyPosition(double x, double y) {
		Rectangle2D newBounds = new Rectangle2D.Double(x, y, 0, 0);

		mySky.animateViewToCenterBounds(newBounds, false, 600);
	}

	/**
	 * Closes all windows which exist in this world
	 */
	public void closeAllWindows() {
		for (Window window : getWindows()) {
			window.close();
		}

	}

	/**
	 * @return A collection of all the windows in this world
     * TODO replace with iterator to avoid copying a new collection
	 */
	public Iterable<Window> getWindows() {
        return Iterables.concat(getSky().getWindows(), getGround().getWindows());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.shu.ui.lib.handlers.Interactable#showContextMenu(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public JPopupMenu getContextMenu() {
		PopupMenuBuilder menu = new PopupMenuBuilder(name());
		constructMenu(menu, null, null);

		return menu.toJPopupMenu();
	}
	
	public JPopupMenu getContextMenu(double posX, double posY) {
		PopupMenuBuilder menu = new PopupMenuBuilder(name());
		constructMenu(menu, posX, posY);

		return menu.toJPopupMenu();
	}

	/**
	 * @return ground
	 */
	public WorldGroundImpl getGround() {
		return myGround;
	}

	/**
	 * @return Selection Currently Selected nodes
	 */
	public Collection<WorldObject> getSelection() {
		return selectionEventHandler.getSelection();
	}
	
	public SelectionHandler getSelectionHandler() {
		return selectionEventHandler;
	}

	/**
	 * @return Context menu for currently selected items, null is none is to be
	 *         shown
	 */
	public final JPopupMenu getSelectionMenu(Collection<WorldObject> selection) {

		if (selection.size() > 1) {

			PopupMenuBuilder menu = new PopupMenuBuilder(selection.size() + " Objects selected");

			constructSelectionMenu(selection, menu);

			return menu.toJPopupMenu();
		} else {
			return null;
		}

	}

	/**
	 * @return sky
	 */
	public WorldSkyImpl getSky() {
		return mySky;
	}

	public boolean isAncestorOf(WorldObject wo) {
		if (wo == this)
			return true;

		if (getGround().isAncestorOf(wo)) {
			return true;
		} else {
			return super.isAncestorOf(wo);
		}
	}

	/**
	 * @return if true, selection mode is enabled. if false, navigation mode is
	 *         enabled instead.
	 */
	public boolean isSelectionMode() {
		return isSelectionMode;
	}

	/**
	 * Minimizes all windows that exist in this world
	 */
	public void minimizeAllWindows() {
		for (Window window : getWindows()) {
			window.setWindowState(Window.WindowState.MINIMIZED);
		}
	}

	public boolean setBounds(double x, double y, double w, double h) {
		mySky.setBounds(x, y, w, h);
		return super.setBounds(x, y, w, h);
	}

	/**
	 * @param enabled
	 *            True if selection mode is enabled, False if navigation
	 */
	public void setSelectionMode(boolean enabled) {
		if (isSelectionMode != enabled) {
			isSelectionMode = enabled;
			mySky.getCamera().removeInputEventListener(selectionEventHandler);
			selectionEventHandler.endSelection(false);
			if (!isSelectionMode) {
				initSelectionMode();
			} else {
				mySky.getCamera().removeInputEventListener(panHandler);
				mySky.getCamera().addInputEventListener(selectionEventHandler);
			}

			// layoutChildren();
		}
	}

	/**
	 * Set the status bar handler, there can be only one.
	 * 
	 * @param statusHandler
	 *            New Status bar handler
	 */
	public void setStatusBarHandler(AbstractStatusHandler statusHandler) {
		if (statusBarHandler != null) {
			getSky().getCamera().removeInputEventListener(statusBarHandler);
		}

		statusBarHandler = statusHandler;

		if (statusBarHandler != null) {
			getSky().getCamera().addInputEventListener(statusBarHandler);
		}
	}

	/**
	 * @param objectSelected
	 *            Object to show the tooltip for
	 * @return Tooltip shown
	 */
	public TooltipWrapper showTooltip(WorldObject objectSelected) {

		TooltipWrapper tooltip = new TooltipWrapper(getSky(), objectSelected.getTooltip(),
				objectSelected);

		tooltip.fadeIn();

		return tooltip;

	}

	/**
	 * @param position
	 *            Position in sky
	 * @return Position on ground
	 */
	public Point2D skyToGround(Point2D position) {
		mySky.localToView(position);

		return position;
	}

	public void zoomToBounds(Rectangle2D bounds) {
		zoomToBounds(bounds, 1000);
	}

	/**
	 * Animate the sky to look at a portion of the ground at bounds
	 * 
	 * @param bounds
	 *            Bounds to look at
	 * @return Reference to the activity which is animating the zoom and
	 *         positioning
	 */
	public void zoomToBounds(Rectangle2D bounds, long time) {
		PBounds biggerBounds = new PBounds(bounds.getX() - OBJECT_ZOOM_PADDING, bounds.getY()
				- OBJECT_ZOOM_PADDING, bounds.getWidth() + OBJECT_ZOOM_PADDING * 2, bounds
				.getHeight()
				+ OBJECT_ZOOM_PADDING * 2);

		getSky().animateViewToCenterBounds(biggerBounds, true, time);

	}

	public void zoomToFit() {
		if (getSelection().size() > 0) {
			Rectangle2D bounds = WorldImpl.getObjectBounds(getSelection());
			zoomToBounds(bounds);
		} else {
            zoomToBounds(getGround().getPNode().getUnionOfChildrenBounds(null));
		}

	}

	/**
	 * @param object
	 *            Object to zoom to
	 * @return reference to animation activity
	 */
	public void zoomToObject(WorldObject object) {
		Rectangle2D bounds = object.getParent().localToGlobal(object.getFullBoundsReference());

		zoomToBounds(bounds);
	}

	/**
	 * Action to close all windows
	 * 
	 * @author Shu Wu
	 */
	class CloseAllWindows extends StandardAction {

		private static final long serialVersionUID = 1L;

		public CloseAllWindows(String actionName) {
			super("Close all windows", actionName);
		}

		@Override
		protected void action() throws ActionException {
			closeAllWindows();

		}

	}

	/**
	 * Action to minimize all windows
	 * 
	 * @author Shu Wu
	 */
	class MinimizeAllWindows extends StandardAction {

		private static final long serialVersionUID = 1L;

		public MinimizeAllWindows(String actionName) {
			super("Minimize all windows", actionName);
		}

		@Override
		protected void action() throws ActionException {
			minimizeAllWindows();

		}

	}

}
