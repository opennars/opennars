package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.handler.EventConsumer;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.icon.CloseIcon;
import ca.nengo.ui.lib.world.piccolo.icon.MaximizeIcon;
import ca.nengo.ui.lib.world.piccolo.icon.MinimizeIcon;
import ca.nengo.ui.lib.world.piccolo.icon.RestoreIcon;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventListener;
import org.piccolo2d.extras.nodes.PClip;

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
    public static final int BUTTON_SIZE = 26;


	public static final WindowState WINDOW_STATE_DEFAULT = WindowState.NORMAL;

	private final MenuBar menubar;

	private final Border myBorder;



	private final WorldObjectImpl myContent;
    private final PClip myClippingRectangle;

    private EventConsumer myEventConsumer;

	private final WeakReference<WorldObjectImpl> mySourceRef;

	private RectangularEdge mySourceShadow = null;

	private WindowState myState = WINDOW_STATE_DEFAULT;

	private Rectangle2D savedWindowBounds;

	private Point2D savedWindowOffset;

	private WindowState savedWindowState = WINDOW_STATE_DEFAULT;

    public Window(WorldObjectImpl source, WorldObjectImpl content) {
        this(source, content, true, true, true);
    }


	/**
	 * @param source
	 *            parent Node to attach this Window to
	 * @param content
	 *            Node containing the contents of this Window
	 */
	public Window(WorldObjectImpl source, WorldObjectImpl content, boolean title, boolean minMaxNormalButton, boolean closeButton) {
		super();
		mySourceRef = new WeakReference<WorldObjectImpl>(source);
		setSelectable(true);
		this.myContent = content;

		menubar = new MenuBar(this, title, minMaxNormalButton, closeButton);


		myClippingRectangle = new PClip();
        myClippingRectangle.addChild(content.getPNode());
		//myClippingRectangle.setPaint(NengoStyle.COLOR_BACKGROUND);
        getPNode().addChild(myClippingRectangle);


        myBorder = new Border(this, NengoStyle.COLOR_FOREGROUND);
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
			setPickable(myState!=WindowState.MAXIMIZED);
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
	public String name() {
		return myContent.name();
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
		myContent.setOffset(0, 0 + MENU_BAR_HEIGHT);

        if (myClippingRectangle!=null)
            myClippingRectangle.setBounds((float) getX(), (float) getY(), (float) getWidth(), (float) getHeight());

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
        setWindowState(state, false);
    }
	public void setWindowState(WindowState state, boolean force) {

		if (force || (state != myState)) {

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


	private WorldObject buttonHolder;
	private AbstractButton maximizeButton, minimizeButton, closeButton, normalButton;
	private final Window myWindow;
	private ShapeObject rectangle;

	private Text title;
    private boolean highlighted;

    public MenuBar(Window window, boolean title, boolean minMaxButton, boolean closeButton) {
		super();
		this.myWindow = window;

        if (title) {
            this.title = new Text(myWindow.name());
        }
        if (minMaxButton) {
            normalButton = new Button(new RestoreIcon(Window.BUTTON_SIZE), new Runnable() {
                public void run() {
                    myWindow.setWindowState(Window.WindowState.NORMAL);
                }
            });

            maximizeButton = new Button(new MaximizeIcon(Window.BUTTON_SIZE), new Runnable() {
                public void run() {
                    myWindow.setWindowState(Window.WindowState.MAXIMIZED);
                }
            });

            minimizeButton = new Button(new MinimizeIcon(Window.BUTTON_SIZE), new Runnable() {
                public void run() {
                    myWindow.setWindowState(Window.WindowState.MINIMIZED);
                }
            });
        }
        if (closeButton) {
            this.closeButton = new Button(new CloseIcon(Window.BUTTON_SIZE), new Runnable() {
                public void run() {
                    myWindow.close();
                }
            });
        }


		init();
	}

	private void init() {
		addInputEventListener(this);
		rectangle = ShapeObject.createRectangle(0, 0, 1, 1);
		rectangle.setPaint(NengoStyle.COLOR_BACKGROUND2);

		addChild(rectangle);

        buttonHolder = new WorldObjectImpl();
        addChild(buttonHolder);

        if (title!=null) {
            title.setFont(NengoStyle.FONT_LARGE);
            addChild(title);
        }

        if (canMinMaxNormal()) {
            buttonHolder.addChild(maximizeButton);
            buttonHolder.addChild(normalButton);
            buttonHolder.addChild(minimizeButton);
        }

        if (closeButton!=null) {
            buttonHolder.addChild(closeButton);
        }


		setHighlighted(false);
	}

    private boolean canMinMaxNormal() {
        return normalButton!=null;
    }

    @Override
	public void layoutChildren() {
		super.layoutChildren();

        if (title!=null)
		    title.setBounds(3, 3, getWidth(), getHeight());

        double buttonX= getWidth();
        if (closeButton!=null) {
            buttonX -= closeButton.getWidth();
            closeButton.setOffset(buttonX, 0);
        }
        if (canMinMaxNormal()) {
            buttonX -= maximizeButton.getWidth();
            maximizeButton.setOffset(buttonX, 0);

            buttonX -= normalButton.getWidth();
            normalButton.setOffset(buttonX, 0);

            buttonX -= minimizeButton.getWidth();
            minimizeButton.setOffset(buttonX, 0);
        }

        rectangle.setBounds(getBounds());
	}

	public void processEvent(PInputEvent event, int type) {
		if (type == MouseEvent.MOUSE_CLICKED && canMinMaxNormal() && event.getClickCount() == 2) {
			myWindow.cycleVisibleWindowState();
		}
        //System.out.println(event.getPickedNode());
            //event.setHandled(true);
        //}
        //if (highlighted) event.setHandled(true);
	}

	public void setHighlighted(boolean bool) {
        highlighted = bool;
		if (bool || myWindow.getWindowState() == Window.WindowState.MAXIMIZED) {
			rectangle.setTransparency(1f);
			buttonHolder.setTransparency(1f);
            if (title!=null)
                title.setTransparency(1f);
		} else {
			rectangle.setTransparency(0.2f);
			buttonHolder.setTransparency(0.4f);
            if (title!=null)
			    title.setTransparency(0.6f);
		}
	}

	public void updateButtons() {
        if (canMinMaxNormal()) {
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

    public boolean isHighlighted() {
        return highlighted;
    }
}
