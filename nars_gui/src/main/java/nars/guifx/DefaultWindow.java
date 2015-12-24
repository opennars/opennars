/**
 * DefaultWindowSkin.java
 *
 * Copyright (c) 2011-2015, JFXtras
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package nars.guifx;

import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import jfxtras.labs.scene.control.window.SelectableNode;
import jfxtras.labs.scene.control.window.Window;
import jfxtras.labs.util.WindowUtil;
import jfxtras.scene.control.window.WindowIcon;
import jfxtras.util.NodeUtil;
import nars.guifx.graph2.layout.GraphNode;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class DefaultWindow extends GraphNode implements SelectableNode {

    private double mouseX;
    private double mouseY;
    private double nodeX = 0;
    private double nodeY = 0;
    private boolean dragging = false;
    private boolean zoomable = true;
    private double minScale = 0.1;
    private double maxScale = 10;
    private double scaleIncrement = 0.001;
    private ResizeMode resizeMode;
    private boolean RESIZE_TOP;
    private boolean RESIZE_LEFT;
    private boolean RESIZE_BOTTOM;
    private boolean RESIZE_RIGHT;
    public Node titleBar;
    public final StackPane root = new StackPane();
    private final double contentScale = 0.95f;
    private double oldHeight;
    private Timeline minimizeTimeLine;

    /**
     * Defines whether window is moved to front when user clicks on it
     */
    private boolean moveToFront = true;
    /**
     * Window title property (used by titlebar)
     */
    private final StringProperty titleProperty = new SimpleStringProperty("Title");
    /**
     * Minimize property (defines whether to minimize the window,performed by
     * skin)
     */
    private final BooleanProperty minimizeProperty = new SimpleBooleanProperty();
    /**
     * Resize property (defines whether is the window resizeable,performed by
     * skin)
     */
    private final BooleanProperty resizableProperty = new SimpleBooleanProperty(true);
    /**
     * Resize property (defines whether is the window movable,performed by skin)
     */
    private final BooleanProperty movableProperty = new SimpleBooleanProperty(true);

    /**
     * List of icons shown on the left. TODO replace left/right with more
     * generic position property?
     */
    private final ObservableList<WindowIcon> leftIcons =
            FXCollections.observableArrayList();
    /**
     * List of icons shown on the right. TODO replace left/right with more
     * generic position property?
     */
    private final ObservableList<WindowIcon> rightIcons = FXCollections.observableArrayList();
    /**
     * Defines the width of the border /area where the user can grab the window
     * and resize it.
     */
    private final DoubleProperty resizableBorderWidthProperty = new SimpleDoubleProperty(5);
    /**
     * Defines the titlebar class name. This can be used to define css
     * properties specifically for the titlebar, e.g., background.
     */
    private final StringProperty titleBarStyleClassProperty =
            new SimpleStringProperty("window-titlebar");
    /**
     * defines the action that shall be performed before the window is closed.
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onCloseActionProperty =
            new SimpleObjectProperty<>();
    /**
     * defines the action that shall be performed after the window has been
     * closed.
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onClosedActionProperty =
            new SimpleObjectProperty<>();
    /**
     * defines the transition that shall be played when closing the window.
     */
    private final ObjectProperty<Transition> closeTransitionProperty =
            new SimpleObjectProperty<>();
    /**
     * Selected property (defines whether this window is selected.
     */
    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
    /**
     * Selectable property (defines whether this window is selectable.
     */
    private final BooleanProperty selectableProperty = new SimpleBooleanProperty(true);
    public BorderPane content;


    public DefaultWindow(String title) {

        titleBar = new Label(title);
        //titleBar = new Button(title);

        titleBar.setOnMouseClicked(e-> {
            if (e.getButton() == MouseButton.SECONDARY) {
                Spacegraph s = getSpace();
                if (s!=null) {
                    System.out.println("autozoom to: " + DefaultWindow.this);
                    System.out.println(s.getZoomFactor());



                    Bounds bp = getBoundsInParent();
                    double lx = bp.getMinX();
                    double ly = bp.getMinY();

                    s.startPan(s.getPanX(),s.getPanY());//getLayoutX(), getLayoutY());
                    s.pan(-lx+s.getPanX(), -ly+s.getPanY());
                    s.setZoomFactor(2f);
                    s.endPan();
                }
            }
        });

        titleBar.setCache(true);
        titleBar.setCacheHint(CacheHint.SCALE);
        titleBar.getStyleClass().add("titlebar");


        init2();

    }

    private Spacegraph getSpace() {
        Parent p = getParent();
        while (p!=null && (!(p instanceof Spacegraph))) {
            p = p.getParent();
        }
        if (p instanceof Spacegraph)
            return (Spacegraph) p;
        return null;
    }


    /**
     * Defines whether this window shall be moved to front when a user clicks on
     * the window.
     *
     * @param moveToFront the state to set
     */
    public void setMoveToFront(boolean moveToFront) {
        this.moveToFront = moveToFront;
    }

    /**
     * Indicates whether the window shall be moved to front when a user clicks
     * on the window.
     *
     * @return <code>true</code> if the window shall be moved to front when a
     * user clicks on the window; <code>false</code> otherwise
     */
    public boolean isMoveToFront() {
        return moveToFront;
    }

    /**
     * Returns the window title.
     *
     * @return the title
     */
    public final String getTitle() {
        return titleProperty.get();
    }

    /**
     * Defines the window title.
     *
     * @param title the title to set
     */
    public final void setTitle(String title) {
        titleProperty.set(title);
    }

    /**
     * Returns the window title property.
     *
     * @return the window title property
     */
    public final StringProperty titleProperty() {
        return titleProperty;
    }

    /**
     * Returns a list that contains the icons that are placed on the left side
     * of the titlebar. Add icons to the list to add them to the left side of
     * the window titlebar.
     *
     * @return a list containing the left icons
     *
     * @see #getRightIcons()
     */
    public ObservableList<WindowIcon> getLeftIcons() {
        return leftIcons;
    }

    /**
     * Returns a list that contains the icons that are placed on the right side
     * of the titlebar. Add icons to the list to add them to the right side of
     * the window titlebar.
     *
     * @return a list containing the right icons
     *
     * @see #getLeftIcons()
     */
    public ObservableList<WindowIcon> getRightIcons() {
        return rightIcons;
    }

    /**
     * Defines whether this window shall be minimized.
     *
     * @param v the state to set
     */
    public void setMinimized(boolean v) {
        minimizeProperty.set(v);
    }

    /**
     * Indicates whether the window is currently minimized.
     *
     * @return <code>true</code> if the window is currently minimized;
     * <code>false</code> otherwise
     */
    public boolean isMinimized() {
        return minimizeProperty.get();
    }

    /**
     * Returns the minimize property.
     *
     * @return the minimize property
     */
    public BooleanProperty minimizedProperty() {
        return minimizeProperty;
    }

    /**
     * Defines whether this window shall be resizeable by the user.
     *
     * @param v the state to set
     */
    public void setResizableWindow(boolean v) {
        resizableProperty.set(v);
    }

    /**
     * Indicates whether the window is resizeable by the user.
     *
     * @return <code>true</code> if the window is resizeable; <code>false</code>
     * otherwise
     */
    public boolean isResizableWindow() {
        return resizableProperty.get();
    }

    /**
     * Returns the resize property.
     *
     * @return the minimize property
     */
    public BooleanProperty resizeableWindowProperty() {
        return resizableProperty;
    }

    /**
     * Defines whether this window shall be movable.
     *
     * @param v the state to set
     */
    public void setMovable(boolean v) {
        movableProperty.set(v);
    }

    /**
     * Indicates whether the window is movable.
     *
     * @return <code>true</code> if the window is movable; <code>false</code>
     * otherwise
     */
    public boolean isMovable() {
        return movableProperty.get();
    }

    /**
     * Returns the movable property.
     *
     * @return the minimize property
     */
    public BooleanProperty movableProperty() {
        return movableProperty;
    }

    /**
     * Returns the titlebar style class property.
     *
     * @return the titlebar style class property
     */
    public StringProperty titleBarStyleClassProperty() {
        return titleBarStyleClassProperty;
    }

    /**
     * Defines the CSS style class of the titlebar.
     *
     * @param name the CSS style class name
     */
    public void setTitleBarStyleClass(String name) {
        titleBarStyleClassProperty.set(name);
    }

    /**
     * Returns the CSS style class of the titlebar.
     *
     * @return the CSS style class of the titlebar
     */
    public String getTitleBarStyleClass() {
        return titleBarStyleClassProperty.get();
    }

    /**
     * Returns the resizable border width property.
     *
     * @return the resizable border width property
     *
     * @see #setResizableBorderWidth(double)
     */
    public DoubleProperty resizableBorderWidthProperty() {
        return resizableBorderWidthProperty;
    }

    /**
     * Defines the width of the "resizable border" of the window. The resizable
     * border is usually defined as a rectangular border around the layout
     * bounds of the window where the mouse cursor changes to "resizable" and
     * which allows to resize the window by performing a "dragging gesture",
     * i.e., the user can "grab" the window border and change the size of the
     * window.
     *
     * @param v border width
     */
    public void setResizableBorderWidth(double v) {
        resizableBorderWidthProperty.set(v);
    }

    /**
     * Returns the width of the "resizable border" of the window.
     *
     * @return the width of the "resizable border" of the window
     *
     * @see #setResizableBorderWidth(double)
     */
    public double getResizableBorderWidth() {
        return resizableBorderWidthProperty.get();
    }

    /**
     * Closes this window.
     */
    public void close() {


        // if already closed, we do nothing
        if (getParent() == null) {
            return;
        }

        if (getCloseTransition() != null) {
            getCloseTransition().play();
        } else {
            if (getOnCloseAction() != null) {
                getOnCloseAction().handle(new ActionEvent(this, DefaultWindow.this));
            }
            NodeUtil.removeFromParent(DefaultWindow.this);
            if (getOnClosedAction() != null) {
                getOnClosedAction().handle(new ActionEvent(this, DefaultWindow.this));
            }
        }
    }

    /**
     * Returns the "on-closed-action" property.
     *
     * @return the "on-closed-action" property.
     *
     * @see #setOnClosedAction(EventHandler)
     */
    public ObjectProperty<EventHandler<ActionEvent>> onClosedActionProperty() {
        return onClosedActionProperty;
    }

    /**
     * Defines the action that shall be performed after the window has been
     * closed.
     *
     * @param onClosedAction the action to set
     */
    public void setOnClosedAction(EventHandler<ActionEvent> onClosedAction) {
        onClosedActionProperty.set(onClosedAction);
    }

    /**
     * Returns the action that shall be performed after the window has been
     * closed.
     *
     * @return the action that shall be performed after the window has been
     * closed or <code>null</code> if no such action has been defined
     */
    public EventHandler<ActionEvent> getOnClosedAction() {
        return onClosedActionProperty.get();
    }

    /**
     * Returns the "on-close-action" property.
     *
     * @return the "on-close-action" property.
     *
     * @see #setOnCloseAction(EventHandler)
     */
    public ObjectProperty<EventHandler<ActionEvent>> onCloseActionProperty() {
        return onCloseActionProperty;
    }

    /**
     * Defines the action that shall be performed before the window will be
     * closed.
     *
     * @param onClosedAction the action to set
     */
    public void setOnCloseAction(EventHandler<ActionEvent> onClosedAction) {
        onCloseActionProperty.set(onClosedAction);
    }

    /**
     * Returns the action that shall be performed before the window will be
     * closed.
     *
     * @return the action that shall be performed before the window will be
     * closed or <code>null</code> if no such action has been defined
     */
    public EventHandler<ActionEvent> getOnCloseAction() {
        return onCloseActionProperty.get();
    }

    /**
     * Returns the "close-transition" property.
     *
     * @return the "close-transition" property.
     *
     * @see #setCloseTransition(Transition)
     */
    public ObjectProperty<Transition> closeTransitionProperty() {
        return closeTransitionProperty;
    }

    /**
     * Defines the transition that shall be used to indicate window closing.
     *
     * @param t the transition that shall be used to indicate window closing or
     * <code>null</code> if no transition shall be used.
     */
    public void setCloseTransition(Transition t) {
        closeTransitionProperty.set(t);
    }

    /**
     * Returns the transition that shall be used to indicate window closing.
     *
     * @return the transition that shall be used to indicate window closing or
     * <code>null</code> if no such transition has been defined
     */
    public Transition getCloseTransition() {
        return closeTransitionProperty.get();
    }

    @Override
    public boolean requestSelection(boolean select) {

        if (!select) {
            selectedProperty.set(false);
        }

        if (isSelectable()) {
            selectedProperty.set(select);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the selectableProperty
     */
    public BooleanProperty selectableProperty() {
        return selectableProperty;
    }

    public void setSelectable(Boolean selectable) {
        selectableProperty.set(selectable);
    }

    public boolean isSelectable() {
        return selectableProperty.get();
    }

    /**
     * @return the selectedProperty
     */
    public ReadOnlyBooleanProperty selectedProperty() {
        return selectedProperty;
    }

    /**
     *
     * @return {@code true} if the window is selected; {@code false} otherwise
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    private void init2() {

        content = new BorderPane();



        getChildren().add(content);

        content.setCenter(root);

        content.setTop(titleBar);

        initMouseEventHandlers();




        selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                DropShadow shadow = new DropShadow(20, Color.WHITE);
                Glow effect = new Glow(0.5);
                shadow.setInput(effect);
                setEffect(effect);
            } else {
                setEffect(null);
            }
        });
    }

    private void initMouseEventHandlers() {

        onMousePressedProperty().set((MouseEvent event) -> {
            Node n = this;

            double parentScaleX = n.getParent().
                    localToSceneTransformProperty().getValue().getMxx();
            double parentScaleY = n.getParent().
                    localToSceneTransformProperty().getValue().getMyy();

            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            nodeX = n.getLayoutX() * parentScaleX;
            nodeY = n.getLayoutY() * parentScaleY;

            if (isMoveToFront()) {
                toFront();
            }

            if (isSelected()) {
                selectedWindowsToFront();
            }
        });

        //Event Listener for MouseDragged
        onMouseDraggedProperty().set((MouseEvent event) -> {

            Transform pp = getParent().localToSceneTransformProperty().getValue();
            double parentScaleX = pp.getMxx();
            double parentScaleY = pp.getMyy();

            Transform lp = localToSceneTransformProperty().getValue();
            double scaleX = lp.getMxx();
            double scaleY = lp.getMyy();

            Bounds bil = getBoundsInLocal();
            Bounds boundsInScene = localToScene(bil);

            double sceneX = boundsInScene.getMinX();
            double sceneY = boundsInScene.getMinY();

            double offsetX = event.getSceneX() - mouseX;
            double offsetY = event.getSceneY() - mouseY;

            if (resizeMode == ResizeMode.NONE && isMovable()) {

                nodeX += offsetX;
                nodeY += offsetY;

                double scaledX = nodeX * 1 / parentScaleX;
                double scaledY = nodeY * 1 / parentScaleY;

                double offsetForAllX = scaledX - getLayoutX();
                double offsetForAllY = scaledY - getLayoutY();

                setLayoutX(scaledX);
                setLayoutY(scaledY);

                dragging = true;

                // move all selected windows
                if (isSelected()) {
                    dragSelectedWindows(offsetForAllX, offsetForAllY);
                }

            } else {

                double width = bil.getMaxX() - bil.getMinX();
                double height = bil.getMaxY() - bil.getMinY();

                Insets insets = content.getInsets();
                if (RESIZE_TOP) {
//                        System.out.println("TOP");

                    double insetOffset = insets.getTop() / 2;

                    double yDiff =
                            sceneY / parentScaleY
                            + insetOffset
                            - event.getSceneY() / parentScaleY;

                    double newHeight = content.getPrefHeight() + yDiff;

                    if (newHeight > minHeight(0)) {
                        content.setLayoutY(getLayoutY() - yDiff);
                        content.setPrefHeight(newHeight);
                    }
                }
                Pane cp = root;
                if (RESIZE_LEFT) {
//                        System.out.println("LEFT");

                    double insetOffset = insets.getLeft() / 2;

                    double xDiff = sceneX / parentScaleX
                            + insetOffset
                            - event.getSceneX() / parentScaleX;

                    double newWidth = content.getPrefWidth() + xDiff;

                    if (newWidth > Math.max(minWidth(0),
                            cp.minWidth(0))) {
                        content.setLayoutX(getLayoutX() - xDiff);
                        content.setPrefWidth(newWidth);
                    } else {
                        //
                    }
                }

                if (RESIZE_BOTTOM) {
//                        System.out.println("BOTTOM");

                    double insetOffset = insets.getBottom() / 2;

                    double yDiff = event.getSceneY() / parentScaleY
                            - sceneY / parentScaleY - insetOffset;

                    double newHeight = yDiff;

                    newHeight = Math.max(
                            newHeight, minHeight(0));

                    if (newHeight < maxHeight(0)) {
                        content.setPrefHeight(newHeight);
                    }
                }
                if (RESIZE_RIGHT) {

                    double insetOffset = insets.getRight() / 2;

                    double xDiff = event.getSceneX() / parentScaleX
                            - sceneX / parentScaleY - insetOffset;

                    double newWidth = xDiff;

                    newWidth = Math.max(
                            newWidth,
                            Math.max(cp.minWidth(0),
                            content.minWidth(0)));

                    if (newWidth < maxWidth(0)) {
                        content.setPrefWidth(newWidth);
                    }
                }
            }

            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        onMouseClickedProperty().set((MouseEvent event) -> dragging = false);

        onMouseMovedProperty().set((MouseEvent t) -> {
            if (isMinimized() || !isResizableWindow()) {

                RESIZE_TOP = false;
                RESIZE_LEFT = false;
                RESIZE_BOTTOM = false;
                RESIZE_RIGHT = false;

                resizeMode = ResizeMode.NONE;

                return;
            }

            Node n = this;
            Bounds lb = n.getLayoutBounds();
            Insets insets = content.getInsets();

            Transform lp = n.getParent().localToSceneTransformProperty().getValue();
            double parentScaleX = lp.getMxx();
            double parentScaleY = lp.getMyy();

            Transform tp = n.localToSceneTransformProperty().getValue();
            double scaleX = tp.getMxx();
            double scaleY = tp.getMyy();

            double border = getResizableBorderWidth() * scaleX;


            double diffMinX = Math.abs(lb.getMinX() - t.getX() + insets.getLeft());
            double diffMinY = Math.abs(lb.getMinY() - t.getY() + insets.getTop());
            double diffMaxX = Math.abs(lb.getMaxX() - t.getX() - insets.getRight());
            double diffMaxY = Math.abs(lb.getMaxY() - t.getY() - insets.getBottom());

            boolean left = diffMinX * scaleX < Math.max(border, insets.getLeft() / 2 * scaleX);
            boolean top = diffMinY * scaleY < Math.max(border, insets.getTop() / 2 * scaleY);
            boolean right = diffMaxX * scaleX < Math.max(border, insets.getRight() / 2 * scaleX);
            boolean bottom = diffMaxY * scaleY < Math.max(border, insets.getBottom() / 2 * scaleY);

            RESIZE_TOP = false;
            RESIZE_LEFT = false;
            RESIZE_BOTTOM = false;
            RESIZE_RIGHT = false;

            //noinspection IfStatementWithTooManyBranches
            if (left && !top && !bottom) {
                n.setCursor(Cursor.W_RESIZE);
                resizeMode = ResizeMode.LEFT;
                RESIZE_LEFT = true;
            } else if (left && top && !bottom) {
                n.setCursor(Cursor.NW_RESIZE);
                resizeMode = ResizeMode.TOP_LEFT;
                RESIZE_LEFT = true;
                RESIZE_TOP = true;
            } else if (left && !top && bottom) {
                n.setCursor(Cursor.SW_RESIZE);
                resizeMode = ResizeMode.BOTTOM_LEFT;
                RESIZE_LEFT = true;
                RESIZE_BOTTOM = true;
            } else if (right && !top && !bottom) {
                n.setCursor(Cursor.E_RESIZE);
                resizeMode = ResizeMode.RIGHT;
                RESIZE_RIGHT = true;
            } else if (right && top && !bottom) {
                n.setCursor(Cursor.NE_RESIZE);
                resizeMode = ResizeMode.TOP_RIGHT;
                RESIZE_RIGHT = true;
                RESIZE_TOP = true;
            } else if (right && !top && bottom) {
                n.setCursor(Cursor.SE_RESIZE);
                resizeMode = ResizeMode.BOTTOM_RIGHT;
                RESIZE_RIGHT = true;
                RESIZE_BOTTOM = true;
            } else if (top && !left && !right) {
                n.setCursor(Cursor.N_RESIZE);
                resizeMode = ResizeMode.TOP;
                RESIZE_TOP = true;
            } else if (bottom && !left && !right) {
                n.setCursor(Cursor.S_RESIZE);
                resizeMode = ResizeMode.BOTTOM;
                RESIZE_BOTTOM = true;
            } else {
                n.setCursor(Cursor.DEFAULT);
                resizeMode = ResizeMode.NONE;
            }

            autosize();
        });

//        setOnScroll(new EventHandler<ScrollEvent>() {
//            @Override
//            public void handle(ScrollEvent event) {
//
//                if (!isZoomable()) {
//                    return;
//                }
//
//                double scaleValue =
//                        control.getScaleY() + event.getDeltaY() * getScaleIncrement();
//
//                scaleValue = Math.max(scaleValue, getMinScale());
//                scaleValue = Math.min(scaleValue, getMaxScale());
//
//                control.setScaleX(scaleValue);
//                control.setScaleY(scaleValue);
//
//                event.consume();
//            }
//        });
    }

    // TODO move from skin to behavior class (a lot of other stuff here too)
    private void selectedWindowsToFront() {
        for (SelectableNode sN : WindowUtil.
                getDefaultClipboard().getSelectedItems()) {

            if (sN == this
                    || !(sN instanceof Window)) {
                continue;
            }

            Window selectedWindow = (Window) sN;

            if (getParent().
                    equals(selectedWindow.getParent())
                    && selectedWindow.isMoveToFront()) {

                selectedWindow.toFront();
            }
        } // end for sN
    }

    // TODO move from skin to behavior class (a lot of other stuff here too)
    private void dragSelectedWindows(double offsetForAllX, double offsetForAllY) {
        for (SelectableNode sN : WindowUtil.getDefaultClipboard().getSelectedItems()) {

            if (sN == this
                    || !(sN instanceof Window)) {
                continue;
            }

            Window selectedWindow = (Window) sN;

            if (getParent().
                    equals(selectedWindow.getParent())) {

                selectedWindow.setLayoutX(
                        selectedWindow.getLayoutX()
                        + offsetForAllX);
                selectedWindow.setLayoutY(
                        selectedWindow.getLayoutY()
                        + offsetForAllY);
            }
        } // end for sN
    }

    /**
     * @return the zoomable
     */
    public boolean isZoomable() {
        return zoomable;
    }

    /**
     * @param zoomable the zoomable to set
     */
    public void setZoomable(boolean zoomable) {
        this.zoomable = zoomable;
    }

    /**
     * @return the dragging
     */
    protected boolean isDragging() {
        return dragging;
    }



    /**
     * @return the minScale
     */
    public double getMinScale() {
        return minScale;
    }

    /**
     * @param minScale the minScale to set
     */
    public void setMinScale(double minScale) {
        this.minScale = minScale;
    }

    /**
     * @return the maxScale
     */
    public double getMaxScale() {
        return maxScale;
    }

    /**
     * @param maxScale the maxScale to set
     */
    public void setMaxScale(double maxScale) {
        this.maxScale = maxScale;
    }

    /**
     * @return the scaleIncrement
     */
    public double getScaleIncrement() {
        return scaleIncrement;
    }

    /**
     * @param scaleIncrement the scaleIncrement to set
     */
    public void setScaleIncrement(double scaleIncrement) {
        this.scaleIncrement = scaleIncrement;
    }

}

//class TitleBar extends HBox {
//
//    public static final String DEFAULT_STYLE_CLASS = "window-titlebar";
//    private final Pane leftIconPane;
//    private final Pane rightIconPane;
//    private final Text label = new Text();
//    private final double iconSpacing = 3;
//    DefaultWindow control;
//    // estimated size of "...",
//    // is there a way to find out text dimension without rendering it
//    private final double offset = 40;
//    private double originalTitleWidth;
//
//    public TitleBar(DefaultWindow w) {
//
//        this.control = w;
//
//
//        //getStylesheets().setAll(w.getStylesheets());
//        //getStyleClass().setAll(DEFAULT_STYLE_CLASS);
//
//        setSpacing(8);
//
////        label.setTextAlignment(TextAlignment.CENTER);
////        label.getStyleClass().setAll(DEFAULT_STYLE_CLASS);
//
//        leftIconPane = new IconPane();
//        rightIconPane = new IconPane();
//
//        getChildren().add(leftIconPane);
////        getChildren().add(VFXLayoutUtil.createHBoxFiller());
//        getChildren().add(label);
////        getChildren().add(VFXLayoutUtil.createHBoxFiller());
//        getChildren().add(rightIconPane);
//
//
//        control.boundsInParentProperty().addListener(
//                (ObservableValue<? extends Bounds> ov, Bounds t, Bounds t1) -> {
//            if (control.getTitle() == null
//                    || getLabel().getText() == null
//                    || getLabel().getText().isEmpty()) {
//                return;
//            }
//
//            double maxIconWidth = Math.max(
//                    leftIconPane.getWidth(), rightIconPane.getWidth());
//
//            if (!control.getTitle().equals(getLabel().getText())) {
//                if (originalTitleWidth
//                        + maxIconWidth * 2 + offset < getWidth()) {
//                    getLabel().setText(control.getTitle());
//                }
//            } else if (!"...".equals(getLabel().getText())) {
//                if (originalTitleWidth
//                        + maxIconWidth * 2 + offset >= getWidth()) {
//                    getLabel().setText("...");
//                }
//            }
//        });
//
//    }
//
//    public void setTitle(String title) {
//        getLabel().setText(title);
//
//        originalTitleWidth = getLabel().getBoundsInParent().getWidth();
//
//        double maxIconWidth = Math.max(
//                leftIconPane.getWidth(), rightIconPane.getWidth());
//
//        if (originalTitleWidth
//                + maxIconWidth * 2 + offset >= getWidth()) {
//            getLabel().setText("...");
//        }
//    }
//
//    public String getTitle() {
//        return getLabel().getText();
//    }
//
//    public void addLeftIcon(Node n) {
//        leftIconPane.getChildren().add(n);
//    }
//
//    public void addRightIcon(Node n) {
//        rightIconPane.getChildren().add(n);
//    }
//
//    public void removeLeftIcon(Node n) {
//        leftIconPane.getChildren().remove(n);
//    }
//
//    public void removeRightIcon(Node n) {
//        rightIconPane.getChildren().remove(n);
//    }
//
//    @Override
//    protected double computeMinWidth(double h) {
//        double result = super.computeMinWidth(h);
//
//        double iconWidth =
//                Math.max(
//                leftIconPane.prefWidth(h),
//                rightIconPane.prefWidth(h)) * 2;
//
//        result = Math.max(result,
//                iconWidth
//                //                + getLabel().prefWidth(h)
//                + getInsets().getLeft()
//                + getInsets().getRight());
//
//        return result + iconSpacing * 2 + offset;
//    }
//
//    @Override
//    protected double computePrefWidth(double h) {
//        return computeMinWidth(h);
//    }
//
//
//
//    /**
//     * @return the label
//     */
//    public final Text getLabel() {
//        return label;
//    }
//
//    private static class IconPane extends Pane {
//
//        private final double spacing = 2;
//
//        public IconPane() {
//            //
//            setPrefWidth(USE_COMPUTED_SIZE);
//            setMinWidth(USE_COMPUTED_SIZE);
//        }
//
//        @Override
//        protected void layoutChildren() {
//
//            int count = 0;
//
//            double width = getHeight();
//            double height = getHeight();
//
//            for (Node n : getManagedChildren()) {
//
//                double x = (width + spacing) * count;
//
//                n.resizeRelocate(x, 0, width, height);
//
//                count++;
//            }
//        }
//
//        @Override
//        protected double computeMinWidth(double h) {
//            return getHeight() * getChildren().size()
//                    + spacing * (getChildren().size() - 1);
//        }
//
//        @Override
//        protected double computeMaxWidth(double h) {
//            return computeMinWidth(h);
//        }
//
//        @Override
//        protected double computePrefWidth(double h) {
//            return computeMinWidth(h);
//        }
//    }
//}


// TODO do we still need this enum?
enum ResizeMode {

    NONE,
    TOP,
    LEFT,
    BOTTOM,
    RIGHT,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}