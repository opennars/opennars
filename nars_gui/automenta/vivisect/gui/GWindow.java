/*
  Part of the GUI library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2012 Peter Lager

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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Objects of this class are separate windows which can be used to hold
 GUI GUI components or used for drawing or both combined.
 * <br><br>
 * A number of examples are included in the library and can be found
 * at www.lagers.org.uk
 * 
 * 
 * @author Peter Lager
 *
 */
@SuppressWarnings("serial")
public class GWindow extends Frame implements GConstants, GConstantsInternal {

	protected PApplet app;

	/**
	 * Gives direct access to the PApplet object inside the frame
	 * 
	 */
	public GWinApplet papplet;

	protected String winName;

	public GWinData data;

	protected WindowAdapter winAdapt = null;

	protected int actionOnClose = KEEP_OPEN;


	/** The object to handle the pre event */
	protected Object preHandlerObject = null;
	/** The method in preHandlerObject to execute */
	protected Method preHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String preHandlerMethodName;

	/** The object to handle the draw event */
	protected Object drawHandlerObject = null;
	/** The method in drawHandlerObject to execute */
	protected Method drawHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String drawHandlerMethodName;

	/** The object to handle the key event */
	public Object keyHandlerObject = null;
	/** The method in keyHandlerObject to execute */
	public Method keyHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String keyHandlerMethodName;

	/** The object to handle the mouse event */
	public Object mouseHandlerObject = null;
	/** The method in mouseHandlerObject to execute */
	public Method mouseHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String mouseHandlerMethodName;

	/** The object to handle the post event */
	protected Object postHandlerObject = null;
	/** The method in postHandlerObject to execute */
	protected Method postHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String postHandlerMethodName;

	/** The object to handle the window closing event */
	protected Object closeHandlerObject = null;
	/** The method in closeHandlerObject to execute */
	protected Method closetHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String closetHandlerMethodName;

	/**
	 * Create a window that can be used to hold GUI components or used
 for drawing or both together.
	 * 
	 * @param theApplet
	 * @param name
	 * @param x initial position on the screen
	 * @param y initial position on the screen
	 * @param w width of the drawing area (the frame will be bigger to accommodate border)
	 * @param h height of the drawing area (the frame will be bigger to accommodate border and title bar)
	 * @param noFrame if true then the frame has no border
	 * @param mode JAVA2D / P2D / P3D / OPENGL
	 */
	public GWindow(PApplet theApplet, String name, int x, int y, int w, int h, boolean noFrame, String mode) {
		super(name);
		winName = name;
		windowCtorCore(theApplet, x, y, w, h, null, noFrame, mode, name);
	}

	/**
	 * 
	 * @param theApplet
	 * @param name
	 * @param x initial position on the screen
	 * @param y initial position on the screen
	 * @param image background image (used to size window)
	 * @param noFrame if true then the frame has no border
	 * @param mode JAVA2D / OPENGL
	 */
	public GWindow(PApplet theApplet, String name, int x, int y, PImage image, boolean noFrame, String mode) {
		super(name);
		windowCtorCore(theApplet, x, y, image.width, image.height, image, noFrame, mode, name);
	}

	/**
	 * Core stuff for GWindows ctor
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param noFrame
	 * @param mode
	 */
	private void windowCtorCore(PApplet theApplet, int x, int y, int w, int h, PImage image,  boolean noFrame, String mode, String name){
		// If this is the first control to be created then theAapplet must be the sketchApplet
		if(GUI.sketchApplet == null)
			GUI.sketchApplet = theApplet;
		app = theApplet;
		winName = name;

		if(mode == null || mode.equals(""))
			mode = PApplet.JAVA2D;

		papplet = new GWinApplet(mode);
		papplet.owner = this;
		papplet.frame = this;
		// So we can resize the frame to get the sketch canvas size reqd.
		papplet.frame.setResizable(true);
		// Now set the window width and height
		if(image == null){
			papplet.appWidth = w;
			papplet.appHeight = h;
		} else {
			papplet.bkImage = image;
			papplet.appWidth = image.width;
			papplet.appHeight = image.height;
		}			
		papplet.bkColor = papplet.color(180);

		// Set the papplet size preferences
		papplet.resize(papplet.appWidth, papplet.appHeight);
		papplet.setPreferredSize(new Dimension(papplet.appWidth, papplet.appHeight));
		papplet.setMinimumSize(new Dimension(papplet.appWidth, papplet.appHeight));

		// add the PApplet to the Frame
		setLayout(new BorderLayout());
		add(papplet, BorderLayout.CENTER);

		// ensures that the animation thread is started and
		// that other internal variables are properly set.
		papplet.init();

		// Set the sketch path to the same as the main PApplet object
		papplet.sketchPath = theApplet.sketchPath;

		// Pack the window, position it and make visible
		setUndecorated(noFrame);
		pack();
		setLocation(x,y);
		setVisible(true);

		// Make the window always on top
		setOnTop(true);

		// Make sure we have some data even if not used
		data = new GWinData();
		data.owner = this;

		// Not resizeable if we are using a back image
		super.setResizable(image == null);

		// Make sure GUI knows about this window
		GUI.addWindow(this);
	}

	/**
	 * Attempt to create the on-close-window event handler for this GWindow. 
	 * The default event handler is a method that returns void and has a single
	 * parameter of type GWindow (this will be a reference to the window that is
	 * closing) <br/>
	 * 
	 * The handler will <b>not be called</> if the setActionOnClose flag is set 
	 * to EXIT_APP <br/>
	 * If the flag is set to CLOSE_WINDOW then the handler is called when the window
	 * is closed by clicking on the window-close-icon or using either the close or 
	 * forceClose methods. <br/>
	 * If the flag is set to KEEP_OPEN the window can only be closed using the
	 * forceClose method. In this case the handler will be called.
	 * 
	 * 
	 * @param obj the object to handle the on-close-window event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addOnCloseHandler(Object obj, String methodName){
		try{
			closeHandlerObject = obj;
			closetHandlerMethodName = methodName;
			closetHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[] {GWindow.class } );
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GWindow.class } } );
			closeHandlerObject = null;
			closetHandlerMethodName = "";
		}
	}

	/**
	 * This method will be called by this windows GWindowCloser object
	 */
	void onClose(){
		if(closeHandlerObject != null){
			try {
				closetHandlerMethod.invoke(closeHandlerObject, new Object[] { this } );
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER,  
						new Object[] {closeHandlerObject, closetHandlerMethod, e } );
			}
		}		
	}

	/**
	 * Add an object that holds the data this window needs to use.
	 * 
	 * Note: the object can be of any class that extends GWinData.
	 * 
	 * @param data
	 */
	public void addData(GWinData data){
		this.data = data;
		this.data.owner = this;
	}

	/**
	 * Always make this window appear on top of other windows (or not). <br>
	 * This will not work when run from a remote server (ie Applet over the web)
	 * for security reasons. In this situation a call to this method is ignored
	 * and a warning is generated. 
	 * 
	 * @param onTop
	 */
	public void setOnTop(boolean onTop){
		try{
			setAlwaysOnTop(onTop);
		} catch (Exception e){
			if(GUI.showMessages)
				System.out.println("Warning: setOnTop() method will not work when the sketch is run from a remote location.");
		}
	}

	/**
	 * Sets the location of the window. <br>
 (Already available from the Frame class - helps visibility 
 of method in GUI reference)
	 */
	public void setLocation(int x, int y){
		super.setLocation(x,y);
	}

	/**
	 * Sets the visibility of the window <br>
 (Already available from the Frame class - helps visibility 
 of method in GUI reference)
	 */
	public void setVisible(boolean visible){
		super.setVisible(visible);
	}

	/**
	 * Determines whether the window is resizabale or not. <br>
	 * This cannot be set to true if a background image is used.
	 */
	public void setResizable(boolean resizable){
		if(resizable == false)
			super.setResizable(false);
		else {
			if(papplet.bkImage == null)
				super.setResizable(true);
		}
	}

	/**
	 * Set the background image to be used instead of a plain color background <br>
	 * The window will resize to accommodate the image. This will also turn on autoClear.
	 * @param image
	 */
	public void setBackground(PImage image){
		papplet.noLoop();
		papplet.bkImage = null;
		super.setResizable(true);
		papplet.resize(image.width, image.height);
		papplet.bkImage = image;
		papplet.appWidth = image.width;
		papplet.appHeight = image.height;
		papplet.setPreferredSize(new Dimension(papplet.appWidth, papplet.appHeight));
		papplet.setMinimumSize(new Dimension(papplet.appWidth, papplet.appHeight));
		pack();
		super.setResizable(false);
		papplet.autoClear = true;
		papplet.loop();
	}

	/**
	 * Set the background color for the window. This will also turn on autoClear.
	 * 
	 * @param col
	 */
	public void setBackground(int col){
		papplet.bkColor = col;
		papplet.autoClear = true;
	}

	/**
	 * Like the draw() method in the main sketch tab the user must include
	 * the background(...) statement to clear the background. It autoClear is set 
	 * to true then this is done automatically otherwise you may want to add your own
	 * call to background in the draw handler method. <br>
	 * If you have called one of the background(...) methods autoClear will be set to true. 
	 * 
	 * @param auto_clear whether to call the background() method or not
	 */
	public void setAutoClear(boolean auto_clear){
		papplet.autoClear = auto_clear;
	}

	/**
	 * This sets what happens when the users attempts to close the window. <br>
	 * There are 3 possible actions depending on the value passed. <br>
	 * GWindow.KEEP_OPEN - ignore attempt to close window (default action) <br>
	 * GWindow.CLOSE_WINDOW - close this window, if it is the main window it causes the app to exit <br>
	 * GWindow.EXIT_APP - exit the app, this will cause all windows to close. <br>
	 * @param action the required close action
	 */
	public void setActionOnClose(int action){
		switch(action){
		case KEEP_OPEN:
			removeWindowListener(winAdapt);
			winAdapt = null;
			actionOnClose = action;
			break;
		case CLOSE_WINDOW:
		case EXIT_APP:
			if(winAdapt == null){
				winAdapt = new GWindowAdapter(this);
				addWindowListener(winAdapt);
			} // end if
			actionOnClose = action;
			break;
		} // end switch
	}

	/**
	 * Get the action to be performed when the user attempts to close
	 * the window.
	 * @return actionOnClose
	 */
	public int getActionOnClose(){
		return actionOnClose;
	}

	/**
	 * This method will fire a WindowClosing event to be captured by the 
	 * GWindow$GWindowAdapter object. <br>
	 * There are 3 possible actions depending on the value passed. <br>
	 * GWindow.KEEP_OPEN - ignore attempt to close window (default action) <br>
	 * GWindow.CLOSE_WINDOW - close this window <br>
	 * GWindow.EXIT_APP - exit the app, this will cause all windows to close. <br>
	 */
	public void close(){
		getToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * This method guarantees that the window is closed by overriding the KEEP_OPEN action-on-close
	 * and will fire a WindowClosing event to be captured by the GWindow$GWindowAdapter object. <br>
	 * There are 2 possible actions depending on the currently specified action-on-close. <br>
	 * GWindow.KEEP_OPEN - close this window <br>
	 * GWindow.CLOSE_WINDOW - close this window <br>
	 * GWindow.EXIT_APP - exit the app, this will cause all windows to close. <br>
	 */
	public void forceClose(){
		if(actionOnClose == KEEP_OPEN)
			setActionOnClose(CLOSE_WINDOW);
		getToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Attempt to add the 'draw' handler method. 
	 * The default event handler is a method that returns void and has two
	 * parameters PApplet and GWinData
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addDrawHandler(Object obj, String methodName){
		try{
			drawHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[] {GWinApplet.class, GWinData.class } );
			drawHandlerObject = obj;
			drawHandlerMethodName = methodName;
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GWinApplet.class, GWinData.class } } );
		}
	}

	/**
	 * Attempt to add the 'pre' handler method. 
	 * The default event handler is a method that returns void and has two
	 * parameters GWinApplet and GWinData
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addPreHandler(Object obj, String methodName){
		try{
			preHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[] {GWinApplet.class, GWinData.class } );
			preHandlerObject = obj;
			preHandlerMethodName = methodName;
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GWinApplet.class, GWinData.class } } );
		}
	}

	/**
	 * Attempt to add the 'mouse' handler method. 
	 * The default event handler is a method that returns void and has three
	 * parameters GWinApplet, GWinData and a MouseEvent
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addMouseHandler(Object obj, String methodName){
		try{
			mouseHandlerMethod = obj.getClass().getMethod(methodName, 
					new Class<?>[] {GWinApplet.class, GWinData.class, MouseEvent.class } );
			mouseHandlerObject = obj;
			mouseHandlerMethodName = methodName;
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GWinApplet.class, GWinData.class, MouseEvent.class } } );
		}
	}

	/**
	 * Attempt to add the 'key' handler method. 
	 * The default event handler is a method that returns void and has three
	 * parameters GWinApplet, GWinData and a KeyEvent
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addKeyHandler(Object obj, String methodName){
		try{
			keyHandlerMethod = obj.getClass().getMethod(methodName, 
					new Class<?>[] {GWinApplet.class, GWinData.class, KeyEvent.class } );
			keyHandlerObject = obj;
			keyHandlerMethodName = methodName;
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GWinApplet.class, GWinData.class, KeyEvent.class } } );
		}
	}

	/**
	 * Attempt to add the 'post' handler method. 
	 * The default event handler is a method that returns void and has two
	 * parameters GWinApplet and GWinData
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addPostHandler(Object obj, String methodName){
		try{
			postHandlerMethod = obj.getClass().getMethod(methodName, 
					new Class<?>[] {GWinApplet.class, GWinData.class } );
			postHandlerObject = obj;
			postHandlerMethodName = methodName;
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { GWinApplet.class, GWinData.class } } );
		}
	}

	/**
	 * Window adapter class that remembers the window it belongs to so
	 * it can be used to mark it for closure if required.
	 * 
	 * @author Peter Lager
	 */
	public class GWindowAdapter extends WindowAdapter {
		GWindow window;

		public GWindowAdapter(GWindow window){
			this.window = window;
		}

		public void windowClosing(WindowEvent evt) {
			switch(actionOnClose){
			case CLOSE_WINDOW:
				window.papplet.noLoop();
				GUI.markWindowForClosure(window);
				break;
			case EXIT_APP:
				System.exit(0);
				break;
			}
		}
	}

}
