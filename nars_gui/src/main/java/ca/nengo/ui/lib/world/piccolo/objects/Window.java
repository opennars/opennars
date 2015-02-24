package ca.nengo.ui.lib.world.piccolo.objects;

import ca.nengo.ui.lib.style.NengoStyle;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.handlers.EventConsumer;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.objects.icons.CloseIcon;
import ca.nengo.ui.lib.world.piccolo.objects.icons.MaximizeIcon;
import ca.nengo.ui.lib.world.piccolo.objects.icons.MinimizeIcon;
import ca.nengo.ui.lib.world.piccolo.objects.icons.RestoreIcon;
import ca.nengo.ui.lib.world.piccolo.primitives.Path;
import ca.nengo.ui.lib.world.piccolo.primitives.Text;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventListener;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;


/**
 * A Window which can be minimized, normal, maximized and closed. A Window wraps
 * another world object which contains content displayed in the window.
 *
 * @author Shu Wu
 */
/**
 * @author User
 */
public class Window extends WorldObjectImpl implements Interactable {
	private static final int DEFAULT_HEIGHT = 400;

	private static final int DEFAULT_WIDTH = 600;

	private static final int MENU_BAR_HEIGHT = 27;

	public static final WindowState WINDOW_STATE_DEFAULT = WindowState.NORMAL;

	private final MenuBar menubar;

	private final Border myBorder;

	//private final PClip myClippingRectangle;

	private final WorldObjectImpl myContent;

	private EventConsumer myEventConsumer;

	private final WeakReference<WorldObjectImpl> mySourceRef;

	private RectangularEdge mySourceShadow = null;

	private WindowState myState = WINDOW_STATE_DEFAULT;

	private Rectangle2D savedWindowBounds;

	private Point2D savedWindowOffset;

	private WindowState savedWindowState = WINDOW_STATE_DEFAULT;

	/**
	 * @param source
	 *            parent Node to attach this Window to
	 * @param content
	 *            Node containing the contents of this Window
	 */
	public Window(WorldObjectImpl source, WorldObjectImpl content) {
		super();
		mySourceRef = new WeakReference<WorldObjectImpl>(source);
		setSelectable(true);
		this.myContent = content;

		menubar = new MenuBar(this);


		/*myClippingRectangle = new PClip();
		myClippingRectangle.addChild(content.getPiccolo());
		myClippingRectangle.setPaint(NengoStyle.COLOR_BACKGROUND);*/
		myBorder = new Border(this, NengoStyle.COLOR_FOREGROUND);

		//getPiccolo().addChild(myClippingRectangle);
        addChild(myContent);
		addChild(menubar);
		addChild(myBorder);

		windowStateChanged();

		myContent.addInputEventListener(new MenuBarHandler());

		addInputEventListener(new MenuBarHandler());

		addPropertyChangeListener(Property.PARENTS_BOUNDS, new Listener() {
			public void propertyChanged(Property event) {
				if (myState == WindowState.MAXIMIZED) {
					maximizeBounds();
				}
			}
		});


	}

	protected void maximizeBounds() {
		setOffset(0, 0);
		setBounds(parentToLocal(getParent().getBounds()));
	}

	@Override
	protected void prepareForDestroy() {
		setWindowState(WindowState.MINIMIZED);
		if (mySourceShadow != null) {
			mySourceShadow.destroy();
			mySourceShadow = null;
		}

		myContent.destroy();
		super.prepareForDestroy();
	}

	protected void windowStateChanged() {
		menubar.updateButtons();
		switch (myState) {
		case MAXIMIZED:

			if (mySourceShadow != null) {
				mySourceShadow.destroy();
				mySourceShadow = null;
			}
			myBorder.setVisible(false);
			UIEnvironment.getInstance().addWorldWindow(this);

			if (myEventConsumer == null) {
				myEventConsumer = new EventConsumer();
				addInputEventListener(myEventConsumer);
			}

			maximizeBounds();
			menubar.setHighlighted(true);

			BoundsHandle.removeBoundsHandlesFrom(this);
			break;
		case NORMAL:
			WorldObjectImpl source = mySourceRef.get();

			if (savedWindowBounds != null) {
				setBounds(savedWindowBounds);
				setOffset(savedWindowOffset);
			} else {
				setWidth(DEFAULT_WIDTH);
				setHeight(DEFAULT_HEIGHT);
				if (source != null) {
					setOffset((getWidth() - source.getWidth()) / -2f, source.getHeight() + 20);
				}

			}
			if (source != null) {
				if (myEventConsumer != null) {
					removeInputEventListener(myEventConsumer);
					myEventConsumer = null;
				}

				source.addChild(this);
				myBorder.setVisible(true);

				BoundsHandle.addBoundsHandlesTo(this);
				if (mySourceShadow == null) {

					mySourceShadow = new RectangularEdge(source, this);
					source.addChild(mySourceShadow, 0);
				}
			} else {
				Util.Assert(false, "Window still active after source destroyed");
			}

			break;
		case MINIMIZED:

			if (mySourceShadow != null) {
				mySourceShadow.destroy();
				mySourceShadow = null;
			}
			removeFromParent();
			break;
		}
		if (myState == WindowState.MINIMIZED) {
			setVisible(false);
			setChildrenPickable(false);
			setPickable(false);
		} else {
			setVisible(true);
			setChildrenPickable(true);
			setPickable(true);
		}

		layoutChildren();
	}

	/**
	 * Closes the window
	 */
	public void close() {
		destroy();
	}

	/**
	 * Increases the size of the window through state transitions
	 */
	public void cycleVisibleWindowState() {
		switch (myState) {
		case MAXIMIZED:
			setWindowState(WindowState.NORMAL);
			break;
		case NORMAL:
			setWindowState(WindowState.MAXIMIZED);
			break;
		case MINIMIZED:
			setWindowState(WindowState.NORMAL);
		}

	}

	public JPopupMenu getContextMenu() {
		if (getContents() instanceof Interactable) {
			return ((Interactable) (getContents())).getContextMenu();
		}
		return null;
	}

	@Override
	public String getName() {
		return myContent.getName();
	}

	/**
	 * @return Node representing the contents of the Window
	 */
	public WorldObject getContents() {
		return myContent;
	}

	public WindowState getWindowState() {
		return myState;
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();

		menubar.setBounds(0, 0, getWidth(), MENU_BAR_HEIGHT);

		myContent.setBounds(0, 0, getWidth() - 4, getHeight() - 4 - MENU_BAR_HEIGHT);
		myContent.setOffset(2, 2 + MENU_BAR_HEIGHT);

        //myClippingRectangle.setBounds((float) getX(), (float) getY(), (float) getWidth(), (float) getHeight());

	}

	@Override
	public void moveToFront() {
		super.moveToFront();
		if (mySourceRef.get() != null) {
			mySourceRef.get().moveToFront();
		}
	}

	public void restoreSavedWindow() {
		setWindowState(savedWindowState);
	}

	public void setWindowState(WindowState state) {

		if (state != myState) {

			/*
			 * Saves the window bounds and offset
			 */
			if (myState == WindowState.NORMAL) {
				savedWindowBounds = getBounds();
				savedWindowOffset = getOffset();
			}
			/*
			 * Saves the previous window state
			 */
			if (state == WindowState.MINIMIZED) {
				savedWindowState = myState;
			}

			myState = state;
			windowStateChanged();
		}
	}

	public static enum WindowState {
		MAXIMIZED, MINIMIZED, NORMAL
	}
	
	public boolean isMaximized() {
		return (myState == WindowState.MAXIMIZED);
	}
	public boolean isMinimized() {
		return (myState == WindowState.MINIMIZED);
	}

	class MenuBarHandler extends PBasicInputEventHandler {
		@Override
		public void mouseEntered(PInputEvent arg0) {
			menubar.setHighlighted(true);
		}

		@Override
		public void mouseExited(PInputEvent arg0) {
			menubar.setHighlighted(false);
		}

	}

}

class MenuBar extends WorldObjectImpl implements PInputEventListener {

	private static final int BUTTON_SIZE = 26;

	private WorldObject buttonHolder;
	private AbstractButton maximizeButton, minimizeButton, closeButton, normalButton;
	private final Window myWindow;
	private Path rectangle;

	private Text title;

	public MenuBar(Window window) {
		super();
		this.myWindow = window;
		init();
	}

	private void init() {
		addInputEventListener(this);
		rectangle = Path.createRectangle(0, 0, 1, 1);
		rectangle.setPaint(NengoStyle.COLOR_BACKGROUND2);

		addChild(rectangle);

		title = new Text(myWindow.getName());
		title.setFont(NengoStyle.FONT_LARGE);
		addChild(title);

		normalButton = new Button(new RestoreIcon(BUTTON_SIZE), new Runnable() {
			public void run() {
				myWindow.setWindowState(Window.WindowState.NORMAL);
			}
		});

		maximizeButton = new Button(new MaximizeIcon(BUTTON_SIZE), new Runnable() {
			public void run() {
				myWindow.setWindowState(Window.WindowState.MAXIMIZED);
			}
		});

		minimizeButton = new Button(new MinimizeIcon(BUTTON_SIZE), new Runnable() {
			public void run() {
				myWindow.setWindowState(Window.WindowState.MINIMIZED);
			}
		});

		closeButton = new Button(new CloseIcon(BUTTON_SIZE), new Runnable() {
			public void run() {
				myWindow.close();
			}
		});
		buttonHolder = new WorldObjectImpl();
		addChild(buttonHolder);
		buttonHolder.addChild(maximizeButton);
		buttonHolder.addChild(normalButton);
		buttonHolder.addChild(minimizeButton);
		buttonHolder.addChild(closeButton);

		setHighlighted(false);
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();
		title.setBounds(3, 3, getWidth(), getHeight());

		double buttonX = getWidth() - closeButton.getWidth();
		closeButton.setOffset(buttonX, 0);
		buttonX -= closeButton.getWidth();
		maximizeButton.setOffset(buttonX, 0);
		normalButton.setOffset(buttonX, 0);
		buttonX -= minimizeButton.getWidth();
		minimizeButton.setOffset(buttonX, 0);
		rectangle.setBounds(getBounds());
	}

	public void processEvent(PInputEvent event, int type) {
		if (type == MouseEvent.MOUSE_CLICKED && event.getClickCount() == 2) {
			myWindow.cycleVisibleWindowState();
		}
	}

	public void setHighlighted(boolean bool) {
		if (bool || myWindow.getWindowState() == Window.WindowState.MAXIMIZED) {
			rectangle.setTransparency(1f);
			buttonHolder.setTransparency(1f);
			title.setTransparency(1f);
		} else {
			rectangle.setTransparency(0.2f);
			buttonHolder.setTransparency(0.4f);
			title.setTransparency(0.6f);
		}
	}

	public void updateButtons() {
		boolean isWindowMaximized = (myWindow.getWindowState() == Window.WindowState.MAXIMIZED);

		if (isWindowMaximized) {
			maximizeButton.removeFromParent();
			buttonHolder.addChild(normalButton);
		} else {
			normalButton.removeFromParent();
			buttonHolder.addChild(maximizeButton);
		}

	}

}
