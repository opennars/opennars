/*
  Part of the G4P library for Processing 
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

import automenta.vivisect.swing.PCanvas;
import java.util.Collections;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * DO NOT ATTEMPT TO USE THIS CLASS <br>
 * 
 * Although this class and many of its methods are declared public this is to make 
 * them available through Reflection and means that they should only be used inside
 * the library code. <br> 
 * 
 * This class is used to remember information about a particular applet (i.e. window)
 * and is responsible for forwarding events passed to it from Processing. <br>
 * 
 * It remembers the original transformation matrix to simplify working with 3D renderers
 * and libraries such as PeasyCam.
 * 
 * @author Peter Lager
 *
 */
public class GWindowInfo implements PConstants, GConstants, GConstantsInternal {

	public PApplet app;
	public boolean app_g_3d;
	public PMatrix orgMatrix;
	
	public LinkedList<GAbstractControl> windowControls = new LinkedList<GAbstractControl>();
	// These next two lists are for controls that are to be added or remove since these
	// actions must be performed outside the draw cycle to avoid concurrent modification
	// exceptions when changing windowControls
	public LinkedList<GAbstractControl> toRemove = new LinkedList<GAbstractControl>();
	public LinkedList<GAbstractControl> toAdd = new LinkedList<GAbstractControl>();
	
	// Set this to true if papplet is a GWinApplet objects i.e. part of a 
	// Gwindow object
	boolean isGWindow;
	
	
	/**
	 * Create an applet info object
	 * @param papplet
	 */
	public GWindowInfo (PApplet papplet) {
		app = papplet;
		// Is this applet part of a GWindow object
		isGWindow = (app instanceof GWinApplet);
		app_g_3d = app.g.is3D();
		if(app.g.is3D())
			orgMatrix = papplet.getMatrix((PMatrix3D)null);
//		else
//			orgMatrix = papplet.getMatrix((PMatrix2D)null);
		
		/*
		 * The WinInfo object is responsible for capturing events from Processing
		 * and passing them onto the GWindow objects and the controls.
		 */
		app.registerMethod("draw",this);
		app.registerMethod("mouseEvent",this);
		app.registerMethod("keyEvent",this);
		app.registerMethod("pre",this);
		app.registerMethod("post",this);
	}

	/**
	 * The draw method registered with Processing
	 */
	public void draw(){
		app.pushMatrix();
		if(app_g_3d) {
			app.hint(PConstants.DISABLE_DEPTH_TEST);
			// Load the identity matrix.
			app.resetMatrix();
			// Apply the original Processing transformation matrix.
			app.applyMatrix(orgMatrix);
		}
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & DRAW_METHOD) == DRAW_METHOD )
				control.draw();
		}		
		if(app_g_3d)
			app.hint(PConstants.ENABLE_DEPTH_TEST);
		app.popMatrix();
	}

	/**
	 * The mouse method registered with Processing
	 * 
	 * @param event
	 */
	public void mouseEvent(MouseEvent event){

            
            
		if(isGWindow)
			((GWinApplet)app).mouseEvent(event);
		for(GAbstractControl control : windowControls){
			if((control.registeredMethods & MOUSE_METHOD) == MOUSE_METHOD)
				control.mouseEvent(event);
		}
	}

	/**
	 * The key method registered with Processing
	 */	
	public void keyEvent(KeyEvent event) {
		if(isGWindow)
			((GWinApplet)app).keyEvent(event);
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & KEY_METHOD) == KEY_METHOD)
				control.keyEvent(event);
		}			
	}

	/**
	 * The pre method registered with Processing
	 */
	public void pre(){
//		System.out.println(app);
		if(GAbstractControl.controlToTakeFocus != null && GAbstractControl.controlToTakeFocus.getPApplet() == app){
			GAbstractControl.controlToTakeFocus.setFocus(true);
			GAbstractControl.controlToTakeFocus = null;
		}
		if(isGWindow)
			((GWinApplet)app).pre();
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & PRE_METHOD) == PRE_METHOD)
				control.pre();
		}
	}

	/**
	 * The post method registered with Processing
	 */
	public void post(){
		if(G4P.cursorChangeEnabled){
			if(GAbstractControl.cursorIsOver != null ) //&& GAbstractControl.cursorIsOver.getPApplet() == app)
				app.cursor(GAbstractControl.cursorIsOver.cursorOver);			
			else 
				app.cursor(G4P.mouseOff);
		}
		if(isGWindow)
			((GWinApplet)app).post();
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & POST_METHOD) == POST_METHOD)
				control.post();
		}
		// =====================================================================================================
		// =====================================================================================================
		//  This is where components are removed or added to the window to avoid concurrent access violations 
		// =====================================================================================================
		// =====================================================================================================
		synchronized (this) {
		// Dispose of any unwanted controls
			if(!toRemove.isEmpty()){
				for(GAbstractControl control : toRemove){
					// If the control has focus then lose it
					if(GAbstractControl.focusIsWith == control)
						control.loseFocus(null);
					// Clear control resources
					control.buffer = null;
					if(control.parent != null){
						control.parent.children.remove(control);
						control.parent = null;
					}
					if(control.children != null)
						control.children.clear();
					control.palette = null;
					control.jpalette = null;
					control.eventHandlerObject = null;
					control.eventHandlerMethod = null;
					control.winApp = null;
					windowControls.remove(control);
					System.gc();			
				}
				toRemove.clear();
			}
			if(!toAdd.isEmpty()){
				for(GAbstractControl control : toAdd)
					windowControls.add(control);
				toAdd.clear();
				Collections.sort(windowControls, G4P.zorder);
			}
		}
	}

	/**
	 * Dispose of this WIndow. <br>
	 * First unregister for event handling then clear list of controls
	 * for this window.
	 */
	void dispose(){
		app.noLoop();
		app.unregisterMethod("draw", this);
		app.unregisterMethod("pre", this);
		app.unregisterMethod("post", this);
		app.unregisterMethod("mouseEvent",this);
		app.unregisterMethod("keyEvent",this);
		windowControls.clear();
	}
	
	/**
	 * If a control is to be added to this window then add the control
	 * to the toAdd list. The control will actually be added during the 
	 * post() method
	 * @param control
	 */
	synchronized void addControl(GAbstractControl control){
		// Make sure we avoid duplicates
		if(!windowControls.contains(control) && !toAdd.contains(control))
			toAdd.add(control);
	}

	/**
	 * If a control is to be removed to this window then add the control
	 * to the toAdd list. The control will actually be added during the 
	 * post() method
	 * @param control
	 */
	synchronized void removeControl(GAbstractControl control){
		// Make sure we avoid duplicates
		if(windowControls.contains(control) && !toRemove.contains(control))
			toRemove.add(control);
	}

	void setColorScheme(int cs){
		for(GAbstractControl control : windowControls)
			control.setLocalColorScheme(cs);
	}

	void setAlpha(int alpha){
		for(GAbstractControl control : windowControls)
			control.setAlpha(alpha);
	}


		
}