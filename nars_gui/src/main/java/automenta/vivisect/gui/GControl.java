/*
 Part of the GUI library for Processing 
 http://www.lagers.org.uk/g4p/index.html
 http://sourceforge.net/projects/g4p/files/?source=navbar

 Copyright (c) 2008-12 Peter Lager

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General
 Public License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA
 */
package automenta.vivisect.gui;

import automenta.vivisect.Widget;
import automenta.vivisect.swing.PCanvas;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Abstract base class for all GUI controls.
 *
 * @author Peter Lager
 *
 */
public abstract class GControl extends Widget implements PConstants, GConstants, GConstantsInternal {

    /*
     * INTERNAL USE ONLY
     * This holds a reference to the GComponent that currently has the
     * focus.
     * A component loses focus when another component takes focus with the
     * takeFocus() method. The takeFocus method should use focusIsWith.loseFocus()
     * before setting its value to the new component. 
     */
    static GControl focusIsWith = null;

    /*
     * INTERNAL USE ONLY
     * Use by the tab manager to move focus between controls
     * 
     */
    static GControl controlToTakeFocus = null;

    /*
     * INTERNAL USE ONLY
     * Keeps track of the component the mouse is over so the mouse
     * cursor can be changed if we wish.
     */
    static GControl cursorIsOver;

    // Increment to be used if on a GPanel
    final static int Z_PANEL = 1024;

	// Components that don't release focus automatically
    // i.e. GTextField
    final static int Z_STICKY = 0;

	// Components that automatically releases focus when appropriate
    // e.g. GButton
    final static int Z_SLIPPY = 24;

    // Components that expand e.g. GDropList
    final static int Z_SLIPPY_EXPANDS = 48;

    // Reference to the PApplet object that owns this control
    protected PCanvas winApp;

    protected float sx = 1f, sy = 1f;

    /* Used to when components overlap */
    protected int z = Z_STICKY;

    // Set to true when mouse is dragging : set false on button released
    protected boolean dragging = false;

    protected static float epsilon = 0.001f;

    /**
     * Link to the parent panel (if null then it is on main window)
     */
    protected GControl parent = null;

    /*
     * A list of child GComponents added to this component
     * Created and used by GPanel and GDropList classes
     */
    protected List<GControl> children = null;

    protected int localColorScheme = GUI.globalColorScheme;
    protected int[] palette = null;
    protected Color[] jpalette = null;
    protected int alphaLevel = GUI.globalAlpha;

    /**
     * Top left position of component in pixels (relative to parent or absolute
     * if parent is null) (changed form int data type in V3
     */
    protected float x, y;
    /**
     * Width and height of component in pixels for drawing background (changed
     * form int data type in V3
     */
    protected float width, height;
    /**
     * Half sizes reduces programming complexity later
     */
    protected float halfWidth, halfHeight;
    /**
     * The centre of the control
     */
    protected float cx, cy;
    /**
     * The angle to control is rotated (radians)
     */
    protected float rotAngle;
    /**
     * Introduced V3 to speed up AffineTransform operations
     */
    protected double[] temp = new double[2];

	// New to V3 components have an image buffer which is only redrawn if 
    // it has been invalidated
    protected PGraphicsJava2D buffer = null;
    protected boolean bufferInvalid = true;

    /**
     * Whether to show background or not
     */
    protected boolean opaque = false;

	// The cursor image when over a control
    // This should be set in the controls constructor
    protected int cursorOver = HAND;

    /*
     * Position over control corrected for any transformation. <br>
     * [0,0] is top left corner of the control.
     * This is used to determine the mouse position over any 
     * particular control or part of a control.
     */
    protected float ox, oy;

    /* Simple tag that can be used by the user */
    public String tag;

    /* Allows user to specify a number for this component */
    public int tagNo;

    /* Is the component visible or not */
    boolean visible = true;

    /* Is the component enabled to generate mouse and keyboard events */
    boolean enabled = true;

    /* 
     * Is the component available for mouse and keyboard events.
     * This is only used internally to prevent user input being
     * processed during animation.
     * It will preserve enabled and visible flags
     */
    boolean available = true;

    /* The object to handle the event */
    protected Object eventHandlerObject = null;
    /* The method in eventHandlerObject to execute */
    protected Method eventHandlerMethod = null;
    /* the name of the method to handle the event */
    protected String eventHandlerMethodName;

    int registeredMethods = 0;

    /*
     * Specify the PImage that contains the image{s} to be used for the button's state. <br>
     * This image may be a composite of 1 to 3 images tiled horizontally. 
     * @param img
     * @param nbrImages in the range 1 - 3
     */
	//	static PImage[] loadImages(PImage img, int nbrImages){
    //		if(img == null || nbrImages <= 0 || nbrImages > 3)
    //			return null;
    //		PImage[] bimage = new PImage[3];
    //		int iw = img.width / nbrImages;
    //		for(int i = 0; i < nbrImages;  i++){
    //			bimage[i] = new PImage(iw, img.height, ARGB);
    //			bimage[i].copy(img, 
    //					i * iw, 0, iw, img.height,
    //					0, 0, iw, img.height);
    //		}
    //		// If less than 3 images reuse last image in set
    //		for(int i = nbrImages; i < 3; i++)
    //			bimage[i] = bimage[nbrImages - 1];
    //		return bimage;
    //	}
	//	public static String getFocusName(){
    //		if(focusIsWith == null)
    //			return "null";
    //		else
    //			return focusIsWith.toString();
    //	}
    /**
     * Base constructor for ALL control ctors that do not have a visible UI but
     * require access to a PApplet object. <br>
     * As of V3.5 the only class using this constructor is GGroup
     *
     * @param theApplet
     */
    public GControl(PApplet theApplet) {
        super();
        
        // If this is the first control to be created then theAapplet must be the sketchApplet
        if (GUI.applet == null) {
            GUI.applet = theApplet;
        }
        winApp = (PCanvas) theApplet;
        tag = this.getClass().getSimpleName();
    }

    /**
     * Base constructor for ALL control ctors that have a visible UI but whose
     * width and height are determined elsewhere e.g. the size of an image. It
     * will set the position of the control based on controlMode. <br>
     *
     */
    public GControl(PApplet theApplet, float p0, float p1) {
        this(theApplet);

        switch (GUI.control_mode) {
            case CORNER:	// (x,y,w,h)
            case CORNERS:	// (x0,y0,x1,y1)
                x = p0;
                y = p1;
                break;
            case CENTER:	// (cx,cy,w,h)
                cx = p0;
                cy = p1;
                break;
        }
        
        GCScheme.makeColorSchemes(winApp);
        rotAngle = 0;
        z = 0;
        palette = GCScheme.getColor(localColorScheme);
        jpalette = GCScheme.getJavaColor(localColorScheme);
        
    }

    /**
     * Base constructor for ALL control ctors that have a visible UI. It will
     * set the position and size of the control based on controlMode. <br>
     *
     */
    public GControl(PApplet theApplet, float p0, float p1, float p2, float p3) {
        this(theApplet);
        
        setPositionAndSize(p0, p1, p2, p3);
        // Create the buffer (only created with this ctor)
        buffer = (PGraphicsJava2D) winApp.createGraphics((int) width, (int) height, PApplet.JAVA2D);
        buffer.rectMode(PApplet.CORNER);

        GCScheme.makeColorSchemes(winApp);

        rotAngle = 0;
        z = 0;
        palette = GCScheme.getColor(localColorScheme);
        jpalette = GCScheme.getJavaColor(localColorScheme);
        
    }

    /**
     * Calculate all the variables that determine the position and size of the
     * control. This depends on
     * <pre>control_mode</pre>
     *
     */
    private void setPositionAndSize(float n0, float n1, float n2, float n3) {
        switch (GUI.control_mode) {
            case CORNER:	// (x,y,w,h)
                x = n0;
                y = n1;
                width = n2;
                height = n3;
                halfWidth = width / 2;
                halfHeight = height / 2;
                cx = x + halfWidth;
                cy = y + halfHeight;
                break;
            case CORNERS:	// (x0,y0,x1,y1)
                x = n0;
                y = n1;
                width = n2 - n0;
                height = n3 - n1;
                halfWidth = width / 2;
                halfHeight = height / 2;
                cx = x + halfWidth;
                cy = y + halfHeight;
                break;
            case CENTER:	// (cx,cy,w,h)
                cx = n0;
                cy = n1;
                width = n2;
                height = n3;
                halfWidth = width / 2;
                halfHeight = height / 2;
                x = cx - halfWidth;
                y = cy - halfHeight;
                break;
        }
    }

    /**
     * Used internally to enforce minimum size constraints
     *
     * @param w the new width
     * @param h the new height
     */
    protected void resize(int w, int h) {
        width = w;
        height = h;
        halfWidth = width / 2;
        halfHeight = height / 2;
        switch (GUI.control_mode) {
            case CORNER:	// (x,y,w,h)
            case CORNERS:	// (x0,y0,x1,y1)
                cx = x + halfWidth;
                cy = y + halfHeight;
                break;
            case CENTER:	// (cx,cy,w,h)
                x = cx - halfWidth;
                y = cy - halfHeight;
                break;
        }
        buffer = (PGraphicsJava2D) winApp.createGraphics(w, h, PApplet.JAVA2D);
        buffer.rectMode(PApplet.CORNER);
    }

    /**
     * If the component responds to key or mouse input or has a visual
     * representation this it can be part of a group controller.
     *
     * @param control the GUI control we are interested in
     * @return true if it can be added to a group controller
     */
    protected boolean isSuitableForGroupControl(GControl control) {
        return (GROUP_CONTROL_METHOD & registeredMethods) != 0;
    }


    /*
     * These are empty methods to enable polymorphism
     */
    public void draw() {
    }

    @Override
    public boolean draw(PGraphics g) {
        draw();
        return true;
    }

    
    public void mouseEvent(MouseEvent event) {
        
        
    }

    public void keyEvent(KeyEvent e) {
    }

    public void pre() {
    }

    public void post() {
    }

    protected void applyTransform() {
        winApp.translate(cx, cy);
        winApp.rotate(rotAngle);
        winApp.scale(sx, sy);
    }

    /**
     * This will remove all references to this control in the library. <br>
     * The user is responsible for nullifying all references to this control in
     * their sketch code. <br>
     * Once this method is called the control cannot be reused but resources
     * used by the control remain until all references to the control are set to
     * null.
     *
     */
    public void dispose() {
        GUI.removeControl(this);
    }

    /**
     * <b>This is for emergency use only!!!! </b><br>
     * In this version of the library a visual controls is drawn to off-screen
     * buffer and then drawn to the screen by copying the buffer. This means
     * that the computationally expensive routines needed to draw the control
     * (especially text controls) are only done when a change has been noted.
     * This means that single changes need not trigger a full redraw to buffer.
     * <br>
     * It does mean that an error in the library code could result in the buffer
     * not being updated after changes. If this happens then in draw() call this
     * method on the affected control, and report it as an issue
     * <a href = 'http://code.google.com/p/gui4processing/issues/list'>
     * here</a><br>
     * Thanks
     */
    public void forceBufferUpdate() {
        bufferInvalid = true;
    }

    protected HotSpot[] hotspots = null;
    protected int currSpot = -1;

    /**
     * Stop when we are over a hotspot. <br>
     * Hotspots should be listed in order of importance.
     *
     * @param px
     * @param py
     * @return the index for the first hotspot containing px,py else return -1
     */
    protected int whichHotSpot(float px, float py) {
        if (hotspots == null) {
            return -1;
        }
        for (int i = 0; i < hotspots.length; i++) {
            if (hotspots[i].contains(px, py)) {
                return hotspots[i].id;
            }
        }
        return -1;
    }

    protected int getCurrHotSpot() {
        return currSpot;
    }

    /**
     * Determines if a particular pixel position is over the panel.
     *
     * @return true if the position is over.
     */
    public boolean isOver(float x, float y) {
        calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
        currSpot = whichHotSpot(ox, oy);
        return (currSpot >= 0);
    }

    /**
     * Set the local colour scheme for this control. Children are ignored.
     *
     * @param cs the colour scheme to use
     */
    public void setLocalColorScheme(int cs) {
        cs = Math.abs(cs) % 16; // Force into valid range
        if (localColorScheme != cs || palette == null) {
            localColorScheme = cs;
            palette = GCScheme.getColor(localColorScheme);
            jpalette = GCScheme.getJavaColor(localColorScheme);
            bufferInvalid = true;
        }
    }

    /**
     * Set the local colour scheme for this control. Children are ignored. If
     * required include the children and their children.
     *
     * @param cs the colour scheme to use
     * @param includeChildren if do do the same for all descendants
     */
    public void setLocalColorScheme(int cs, boolean includeChildren) {
        cs = Math.abs(cs) % 16; // Force into valid range
        if (localColorScheme != cs || palette == null) {
            localColorScheme = cs;
            palette = GCScheme.getColor(localColorScheme);
            jpalette = GCScheme.getJavaColor(localColorScheme);
            bufferInvalid = true;
            if (includeChildren && children != null) {
                for (GControl c : children) {
                    c.setLocalColorScheme(cs, true);
                }
            }
        }
    }

    /**
     * Get the local color scheme ID number.
     *
     */
    public int getLocalColorScheme() {
        return localColorScheme;
    }

    /**
     * Set the transparency of the component and make it unavailable to mouse
     * and keyboard events if below the threshold. Child controls are ignored?
     *
     * @param alpha value in the range 0 (transparent) to 255 (opaque)
     */
    public void setAlpha(int alpha) {
        alpha = Math.abs(alpha) % 256;
        if (alphaLevel != alpha) {
            alphaLevel = alpha;
            available = (alphaLevel >= ALPHA_BLOCK);
            bufferInvalid = true;
        }
    }

    /**
     * Set the transparency of the component and make it unavailable to mouse
     * and keyboard events if below the threshold. Child controls are ignored?
     * <br>
     * If required include the children and their children.
     *
     * @param alpha value in the range 0 (transparent) to 255 (opaque)
     * @param includeChildren if do do the same for all descendants
     */
    public void setAlpha(int alpha, boolean includeChildren) {
        setAlpha(alpha);
        if (includeChildren && children != null) {
            for (GControl c : children) {
                c.setAlpha(alpha, true);
            }
        }
    }

    /**
     * Get the parent control. If null then this is a top-level component
     */
    public GControl getParent() {
        return parent;
    }

    /**
     * Get the PApplet that manages this component
     */
    public PApplet getPApplet() {
        return winApp;
    }

	// Used by composite control i.e. ones that have scrollbars, buttons etc. but not
    // GWindow and GPanel
    protected PGraphics getBuffer() {
        return buffer;
    }

    /**
     * Support UTF8 encoding
     *
     * @param ascii UTF8 code
     * @return true if the character can be displayed
     */
    protected boolean isDisplayable(int ascii) {
        return !(ascii < 32 || ascii == 127);
    }

    /**
     * This method should be used sparingly since it is heavy on resources.
     *
     * @return a PGraphics object showing current state of the control (ignoring
     * rotation)
     */
    public PGraphics getSnapshot() {
        if (buffer != null) {
            updateBuffer();
            PGraphicsJava2D snap = (PGraphicsJava2D) winApp.createGraphics(buffer.width, buffer.height, PApplet.JAVA2D);
            snap.beginDraw();
            snap.image(buffer, 0, 0);
            return snap;
        }
        return null;
    }

    /*
     * Empty method at the moment make abstract
     * in final version
     */
    protected void updateBuffer() {
    }

    /**
     * Attempt to create the default event handler for the component class. The
     * default event handler is a method that returns void and has a single
     * parameter of the same type as the component class generating the event
     * and a method name specific for that class.
     *
     * @param handlerObj the object to handle the event
     * @param methodName the method to execute in the object handler class
     * @param param_classes the parameter classes.
     * @param param_names that names of the parameters (used for error messages
     * only)
     */
    @SuppressWarnings("rawtypes")
    protected void createEventHandler(Object handlerObj, String methodName, Class[] param_classes, String[] param_names) {
        try {
            eventHandlerMethod = handlerObj.getClass().getMethod(methodName, param_classes);
            eventHandlerObject = handlerObj;
            eventHandlerMethodName = methodName;
        } catch (Exception e) {
            GMessenger.message(MISSING, new Object[]{this, methodName, param_classes, param_names});
            eventHandlerObject = null;
        }
    }

    /**
     * Attempt to create the default event handler for the component class. The
     * default event handler is a method that returns void and has a single
     * parameter of the same type as the component class generating the event
     * and a method name specific for that class.
     *
     * @param obj the object to handle the event
     * @param methodName the method to execute in the object handler class
     */
    public void addEventHandler(Object obj, String methodName) {
        try {
            eventHandlerObject = obj;
            eventHandlerMethodName = methodName;
            eventHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[]{this.getClass(), GEvent.class});
        } catch (Exception e) {
            GMessenger.message(NONEXISTANT, new Object[]{this, methodName, new Class<?>[]{this.getClass(), GEvent.class}});
            eventHandlerObject = null;
            eventHandlerMethodName = "";
        }
    }

    /**
     * Attempt to fire an event for this component.
     *
     * The method called must have a single parameter which is the object firing
     * the event. If the method to be called is to have different parameters
     * then it should be overridden in the child class The method
     */
    protected void fireEvent(Object... objects) {
        if (eventHandlerMethod != null) {
            try {
                eventHandlerMethod.invoke(eventHandlerObject, objects);
            } catch (Exception e) {
                GMessenger.message(EXCP_IN_HANDLER,
                        new Object[]{eventHandlerObject, eventHandlerMethodName, e});
            }
        }
    }

    /**
     * Set the rotation to apply when displaying this control. The center of
     * rotation is determined by the control_mode attribute.
     *
     * @param angle clockwise angle in radians
     */
    public void setRotation(float angle) {
        setRotation(angle, GUI.control_mode);
    }

    /**
     * Set the rotation to apply when displaying this control. The center of
     * rotation is determined by the mode parameter parameter.
     *
     * @param angle clockwise angle in radians
     * @param mode PApplet.CORNER / CORNERS / CENTER
     */
    public void setRotation(float angle, GControlMode mode) {
        rotAngle = angle;
        AffineTransform aff = new AffineTransform();
        aff.setToRotation(angle);
        switch (mode) {
            case CORNER:
            case CORNERS:
                // Rotate about top corner
                temp[0] = halfWidth;
                temp[1] = halfHeight;
                aff.transform(temp, 0, temp, 0, 1);
                cx = (float) temp[0] + x;// - halfWidth;
                cy = (float) temp[1] + y;// - halfHeight;
                break;
            case CENTER:
            default:
                // Rotate about centre
                temp[0] = -halfWidth;
                temp[1] = -halfHeight;
                aff.transform(temp, 0, temp, 0, 1);
                x = cx + (float) temp[0];
                y = cy + (float) temp[1];  			// should this be minus?? I don't think so
                break;
        }
    }

    /**
     * Move the control to the given position based on the mode. <br>
     *
     * The position is not constrained to the screen area. <br>
     *
     * The current control mode determines whether we move the corner or the
     * center of the control to px,py <br>
     *
     * @param px the horizontal position to move to
     * @param py the vertical position to move to
     */
    public void moveTo(float px, float py) {
        moveTo(px, py, GUI.control_mode);
    }

    /**
     * Move the control to the given position based on the mode. <br>
     *
     * Unlike when dragged the position is not constrained to the screen area.
     * <br>
     *
     * The mode determines whether we move the corner or the center of the
     * control to px,py <br>
     *
     * @param px the horizontal position to move to
     * @param py the vertical position to move to
     * @param mode the control mode
     */
    public void moveTo(float px, float py, GControlMode mode) {
        GControl p = parent;
        if (p != null) {
            px -= p.width / 2.0f;
            py -= p.height / 2.0f;
        }
        switch (mode) {
            case CORNER:
            case CORNERS:
                cx += (px - x);
                cy += (py - y);
                x = cx - width / 2.0f;
                y = cy - height / 2.0f;
                break;
            case CENTER:
                cx = px;
                cy = py;
                x = cx - width / 2.0f;
                y = cy - height / 2.0f;
                break;
        }
    }

    /**
     * Get the left position of the control. <br>
     * If the control is on a panel then the value returned is relative to the
     * top-left corner of the panel otherwise it is relative to the sketch
     * window display. <br>
     *
     */
    public float getX() {
        if (parent != null) {
            return x + parent.width / 2.0f;
        } else {
            return x;
        }
    }

    /**
     * Get the top position of the control. <br>
     * If the control is on a panel then the value returned is relative to the
     * top-left corner of the panel otherwise it is relative to the sketch
     * window display. <br>
     *
     */
    public float getY() {
        if (parent != null) {
            return y + parent.height / 2.0f;
        } else {
            return y;
        }
    }

    /**
     * Get the centre x position of the control. <br>
     * If the control is on a panel then the value returned is relative to the
     * top-left corner of the panel otherwise it is relative to the sketch
     * window display. <br>
     */
    public float getCX() {
        if (parent != null) {
            return x + (parent.width + width) / 2.0f;
        } else {
            return cx;
        }
    }

    /**
     * Get the centre y position of the control. <br>
     * If the control is on a panel then the value returned is relative to the
     * top-left corner of the panel otherwise it is relative to the sketch
     * window display. <br>
     *
     */
    public float getCY() {
        if (parent != null) {
            return x + (parent.width + width) / 2.0f;
        } else {
            return cy;
        }
    }

    /**
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    /**
     *
     * @param visible the visibility to set
     */
    public void setVisible(boolean visible) {
        // If we are making it invisible and it has focus give up the focus
        if (!visible && focusIsWith == this) {
            loseFocus(null);
        }
        this.visible = visible;
        // Only available if alpha level is high enough
        if (visible) {
            available = (alphaLevel > ALPHA_BLOCK);
        } else {
            available = false;
        }
		// If this control has children than make them available if this control
        // is visible and unavailable if invisible
        if (children != null) {
            for (GControl c : children) {
                c.setAvailable(this.visible);
            }
        }
    }

    /**
     * @return the component's visibility
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * The availability flag is used by the library code to determine whether a
     * control should be considered for drawing and mouse/key input. <br>
     * It perits an internal control that does not affect the visible and
     * enabled state of the control, which are set by the programmer.
     *
     * If a control and its children are made unavailable it will still be drawn
     * but it not respond to user input.
     *
     * @param avail
     */
    protected void setAvailable(boolean avail) {
        available = avail;
        if (children != null) {
            for (GControl c : children) {
                c.setAvailable(avail);
            }
        }
    }

    /**
     * Is this control available?
     */
    protected boolean isAvailable() {
        return available;
    }

    /**
     * Determines whether to show the back colour or not. Only applies to some
     * components
     *
     * @param opaque
     */
    public void setOpaque(boolean opaque) {
		// Ensure that we dont't go from true >> false otherwise 
        // it will validate an invalid buffer
        bufferInvalid |= (opaque != this.opaque);
        this.opaque = opaque;
    }

    /**
     * Find out if the component is opaque
     *
     * @return true if the background is visible
     */
    public boolean isOpaque() {
        return opaque;
    }

    public boolean isDragging() {
        return dragging;
    }

    /**
     * Enable or disable the ability of the component to generate mouse
     * events.<br>
     * GTextField - it also controls key press events <br>
     * GPanel - controls whether the panel can be moved/collapsed/expanded <br>
     *
     * @param enable true to enable else false
     */
    public void setEnabled(boolean enable) {
        enabled = enable;
        if (children != null) {
            for (GControl c : children) {
                c.setEnabled(enable);
            }
        }
    }

    /**
     * Is this component enabled
     *
     * @return true if the component is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Give the focus to this component but only after allowing the current
     * component with focus to release it gracefully. <br>
     * Always cancel the keyFocusIsWith irrespective of the component type. If
     * the component needs to retain keyFocus then override this method in that
     * class e.g. GCombo
     */
    protected void takeFocus() {
        if (focusIsWith != null && focusIsWith != this) {
            focusIsWith.loseFocus(this);
        }
        focusIsWith = this;
    }

    /**
     * For most components there is nothing to do when they loose focus.
     * Override this method in classes that need to do something when they loose
     * focus eg TextField
     */
    protected void loseFocus(GControl grabber) {
        if (cursorIsOver == this) {
            cursorIsOver = null;
        }
        focusIsWith = grabber;
    }

    /**
     * Determines whether this component is to have focus or not
     *
     * @param focus
     */
    public void setFocus(boolean focus) {
        if (focus) {
            takeFocus();
        } else {
            loseFocus(null);
        }
    }

    /**
     * Does this component have focus
     *
     * @return true if this component has focus else false
     */
    public boolean hasFocus() {
        return (this == focusIsWith);
    }

    /**
     * Get the Z order value for the object with focus.
     */
    protected static int focusObjectZ() {
        return (focusIsWith == null) ? -1 : focusIsWith.z;
    }

    /**
     * This will set the rotation of the control to angle overwriting any
     * previous rotation set. Then it calculates the centre position so that the
     * original top left corner of the control will be the position indicated by
     * x,y with respect to the top left corner of parent. <br>
     *
     * The added control will have its position calculated relative to the
     * centre of the parent control. <br>
     *
     * All overloaded methods call this one. <br>
     *
     * @param c the control to add.
     * @param x the leftmost or centre position depending on controlMode
     * @param y the topmost or centre position depending on controlMode
     * @param angle the rotation angle (replaces any the angle specified in
     * control)
     */
    public void add(GControl c, float x, float y, float angle) {
        // Ignore if children are not allowed.
        if (children == null) {
            return;
        }
        c.rotAngle = angle;
        // In child control reset the control so it centred about the origin
        AffineTransform aff = new AffineTransform();
        aff.setToRotation(angle);
        /*
         * The following code should result in the x,y and cx,cy coordinates of
         * the added control (c) added being measured relative to the centre of  
         * this control.
         */
        switch (GUI.control_mode) {
            case CORNER:
            case CORNERS:
                // Rotate about top corner
                c.x = x;
                c.y = y;
                c.temp[0] = c.halfWidth;
                c.temp[1] = c.halfHeight;
                aff.transform(c.temp, 0, c.temp, 0, 1);
                c.cx = (float) c.temp[0] + x - halfWidth;
                c.cy = (float) c.temp[1] + y - halfHeight;
                c.x = c.cx - c.halfWidth;
                c.y = c.cy - c.halfHeight;
                break;
            case CENTER:
                // Rotate about centre
                c.cx = x;
                c.cy = y;
                c.temp[0] = -c.halfWidth;
                c.temp[1] = -c.halfHeight;
                aff.transform(c.temp, 0, c.temp, 0, 1);
                c.x = c.cx + (float) c.temp[0] - halfWidth;
                c.y = c.cy - (float) c.temp[1] - halfHeight;
                c.cx -= halfWidth;
                c.cy -= halfHeight;
                break;
        }
        c.rotAngle = angle;
        // Add to parent
        c.parent = this;
        c.setZ(z);
        // Parent will now be responsible for drawing
        c.registeredMethods &= (ALL_METHOD - DRAW_METHOD);
        if (children == null) {
            children = new ArrayList<GControl>();
        }
        children.add(c);
        Collections.sort(children, new Z_Order());
        // Does the control being added have to do anything extra
        c.addToParent(this);
    }

    /**
     * Add a control at the given position with zero rotation angle.
     *
     * @param c the control to add.
     * @param x the leftmost or centre position depending on controlMode
     * @param y the topmost or centre position depending on controlMode
     */
    public void add(GControl c, float x, float y) {
        if (children == null) {
            return;
        }
        GControl.this.add(c, x, y, 0);
    }

    /**
     * Add a control at the position and rotation specified in the control.
     *
     * @param c the control to add
     */
    public void add(GControl c) {
        if (children == null) {
            return;
        }
        switch (GUI.control_mode) {
            case CORNER:
            case CORNERS:
                GControl.this.add(c, c.x, c.y, c.rotAngle);
                break;
            case CENTER:
                GControl.this.add(c, c.cx, c.cy, c.rotAngle);
                break;
        }
    }

    /**
     * Add several control at the position and rotation specified in each
     * control.
     *
     * @param controls comma separated list of controls
     */
    public void add(GControl... controls) {
        if (children == null) {
            return;
        }
        for (GControl c : controls) {
            switch (GUI.control_mode) {
                case CORNER:
                case CORNERS:
                    GControl.this.add(c, c.x, c.y, c.rotAngle);
                    break;
                case CENTER:
                    GControl.this.add(c, c.cx, c.cy, c.rotAngle);
                    break;
            }
        }
    }

    /**
     * Changes that need to be made to child when added
     *
     * @param p the parent
     */
    protected void addToParent(GControl p) {
    }

    /**
     * Get the shape type when the cursor is over a control
     *
     * @return shape type
     */
    public int getCursorOver() {
        return cursorOver;
    }

    /**
     * Set the shape type to use when the cursor is over a control
     *
     * @param cursorOver the shape type to use
     */
    public void setCursorOver(int cursorOver) {
        this.cursorOver = cursorOver;
    }

    /**
     * Get an affine transformation that is the compound of all transformations
     * including parents
     *
     * @param aff
     */
    protected AffineTransform getTransform(AffineTransform aff) {
        if (parent != null) {
            aff = parent.getTransform(aff);
        }
        aff.translate(cx, cy);
        aff.rotate(rotAngle);
        aff.scale(sx, sy);
        return aff;
    }

    /**
     * This method takes a position px, py and calculates the equivalent
     * position [ox,oy] as if no transformations have taken place and the origin
     * is the top-left corner of the control.
     *
     * @param px
     * @param py
     */
    protected void calcTransformedOrigin(float px, float py) {
        AffineTransform aff = new AffineTransform();
        aff = getTransform(aff);
        temp[0] = px;
        temp[1] = py;
        try {
            aff.inverseTransform(temp, 0, temp, 0, 1);
            ox = (float) temp[0] + halfWidth;
            oy = (float) temp[1] + halfHeight;
        } catch (NoninvertibleTransformException e) {
        }
    }

    /**
     * Recursive function to set the priority of a component. This is used to
     * determine who gets focus when components overlap on the screen e.g. when
     * a combobo expands it might cover a button. <br>
     * It is used where components have childen e.g. GCombo and GPaneln It is
     * used when a child component is added.
     *
     * @param component
     * @param parentZ
     */
    protected void setZ(int parentZ) {
        z += parentZ;
        if (children != null) {
            for (GControl c : children) {
                c.setZ(parentZ);
            }
        }
    }

    /**
     * If the control is permanently no longer required then call this method to
     * remove it and free up resources. <br>
     * The variable identifier used to create this control should be set to
     * null. <br>
     * For example if you want to dispose of a button called
     * <pre>btnDoThis</pre> then to remove the button use the statements <br>
     * <pre>
     * btnDoThis.dispose(); <br>
     * btnDoThis = null; <br></pre>
     */
    public void markForDisposal() {
        GUI.removeControl(this);
    }

    public String toString() {
        if (tag == null) {
            return this.getClass().getSimpleName();
        } else {
            return tag;
        }
    }

    /**
     * Comparator used for controlling the order components are drawn
     *
     * @author Peter Lager
     */
    public static class Z_Order implements Comparator<GControl> {

        public int compare(GControl c1, GControl c2) {
            if (c1.z != c2.z) {
                return new Integer(c1.z).compareTo(new Integer(c2.z));
            } else {
                return new Integer((int) -c1.y).compareTo(new Integer((int) -c2.y));
            }
        }

    } // end of comparator class

    public void scale(float sx, float sy) {
        this.sx = sx;
        this.sy = sy;
    }
}
