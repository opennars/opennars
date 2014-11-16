/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2014 Peter Lager

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import processing.core.PApplet;

/**
 * This class allows you to group GUI controls so that they can be treated as a single 
 * entity for common GUI actions. <br>
 * Once grouped a single statement change the enabled status, visibility status, colour 
 * scheme and transparency for all the controls in the group. It is possible to 
 * specify a delay before the action is executed, and in the case of transparency, specify
 * the amount of time the fade occurs.<br>
 * This is particularly useful if your sketch has a number of 'modes' and each mode has its
 * own set of G4P controls. Simply create a GGroup object for each mode then as the mode 
 * changes it is easy to display the controls for the current mode and hide the others. <br>
 * A GGroup is associated with a window (or GWindow) and can only be used with controls
 * displayed on that window.
 *
 * @author Peter Lager
 *
 */
public final class GGroup extends GAbstractControl {
	// The possible group action types
	private static final int INVALID_ACTION	= 0;
	private static final int ALPHA_TO		= 1;
	private static final int ENABLE			= 2;
	private static final int VISIBLE		= 3;
	private static final int COLOR_SCHEME	= 4;

	// Actions still to be processed.
	private List<Action> actions = new LinkedList<Action>();

	private int cTime, lTime, eTime;

	private int currentAlpha = 255;
	private int targetAlpha = 255;
	private float alpha = 255;
	private float speed = 255;

	/**
	 * Create a control group for the indicated PApplet. Only controls
	 * created with this PApplet can be added to this group. <br>
	 * 
	 * All controls added will be initialised as fully opaque. <br>
	 * 
	 * @param theApplet the associated PApplet
	 */
	public GGroup(PApplet theApplet) {
		this(theApplet, 255);
	}

	/**
	 * Create a control group for the indicated PApplet. Only controls
	 * created with this PApplet can be added to this group. <br>
	 * 
	 * All controls added will be initialised as fully opaque. <br>
	 * 
	 * @param theApplet the associated PApplet
	 * @param startAlpha the starting alpha level (must 0-255 incl)
	 */
	public GGroup(PApplet theApplet, int startAlpha) {
		super(theApplet);
		startAlpha &= 0xFF;
		currentAlpha = targetAlpha = startAlpha;
		children = new LinkedList<GAbstractControl>();
		registeredMethods = PRE_METHOD;
		cTime = lTime = eTime = 0;
		G4P.addControl(this);
	}

	/**
	 * Create a control group for the indicated GWindow. Only controls
	 * displayed in this GWindow can be added to this group. <br>
	 * 
	 * All controls added will be initialised as fully opaque. <br>
	 * 
	 * @param theWindow the associated GWindow
	 */
	public GGroup(GWindow theWindow) {
		this(theWindow.papplet, 255);
	}

	/**
	 * Create a control group for the indicated GWindow. Only controls
	 * displayed in this GWindow can be added to this group. <br>
	 * 
	 * All controls added will be initialised as fully opaque. <br>
	 * 
	 * @param theWindow the associated GWindow
	 * @param startAlpha the starting alpha level (must 0-255 incl)
	 */
	public GGroup(GWindow theWindow, int startAlpha) {
		this(theWindow.papplet, 255);
	}


	/**
	 * Fade to invisible. (alpha = 0)
	 * 
	 * @param delay time before starting fade (milli-seconds)
	 * @param duration time to fade over (milli-seconds)
	 */
	public void fadeOut(int delay, int duration){
		actions.add(new Action(ALPHA_TO, delay, new Object[] { 0, duration }));
	}

	/**
	 * Fade to fully opaque. (alpha = 255)
	 * 
	 * @param delay time before starting fade (milli-seconds)
	 * @param duration time to fade over (milli-seconds)
	 */
	public void fadeIn(int delay, int duration){
		actions.add(new Action(ALPHA_TO, delay, new Object[] { 255, duration }));
	}

	/**
	 * Fade to an alpha value
	 * 
	 * @param delay time before starting fade (milli-seconds)
	 * @param duration time to fade over (milli-seconds)
	 * @param alpha the target alpha value
	 */
	public void fadeTo(int delay, int duration, int alpha){
		alpha &= 0xFF;
		actions.add(new Action(ALPHA_TO, delay, new Object[] { alpha, duration }));
	}

	/**
	 * Enable / disable the controls.
	 * @param delay delay time before action is performed (milli-seconds)
	 */
	public void setEnabled(int delay, boolean enable){
		actions.add(new Action(ENABLE, delay, new Object[] { enable }));		
	}

	public boolean isFading(){
		return currentAlpha != targetAlpha;
	}
	
	public int timeLeftFading(){
		if(currentAlpha == targetAlpha)
			return 0;
		// calculate approximate time left
		return Math.round((targetAlpha - currentAlpha)/speed);
	}
	
	/**
	 * Enable / disable the controls immediately.
	 */
	public void setEnabled(boolean enable){
		setEnabled(0, enable);	
	}

	/**
	 * Make the control visible or invisible. <br>
	 * If you simply want to change the controls' visibility status then
	 * use this in preference to fadeIn/fadeOut
	 * @param delay delay time before action is performed (milli-seconds)
	 */
	public void setVisible(int delay, boolean visible){
		actions.add(new Action(VISIBLE, delay, new Object[] { visible }));		
	}

	/**
	 * Make the control visible or invisible with immediate effect. <br>
	 * If you simply want to change the controls' visibility status then 
	 * use this in preference to fadeIn/fadeOut
	 */
	public void setVisible(boolean visible){
		setEnabled(0, visible);	
	}
	
	/**
	 * Set the color scheme used by these controls with immediate effect. <br>
	 * @see GConstants for colour constants (e.g. BLUE_SCHEME) to use.
	 * 
	 * @param colScheme the colour scheme (0-15) to be used.
	 */
	public void setLocalColorScheme(int colScheme){
		setLocalColorScheme(0, colScheme);
	}

	/**
	 * Set the color scheme used by these controls after a delay. <br>
	 * @see GConstants for colour constants (e.g. BLUE_SCHEME) to use.
	 * 
	 * @param delay time before colour change (milli-seconds)
	 * @param colScheme the colour scheme (0-15) to be used.
	 */
	public void setLocalColorScheme(int delay, int colScheme){
		colScheme &= 0xf; // constrain to 0-15
		actions.add(new Action(COLOR_SCHEME, delay, new Object[] { colScheme }));
	}
	
	public void pre(){
		if(lTime == 0){ // First call to method
			cTime = lTime = (int) System.currentTimeMillis();
			return;
		}
		// Get elapsed time in millis
		lTime = cTime;
		cTime = (int) System.currentTimeMillis();
		eTime = cTime - lTime;

		if(currentAlpha != targetAlpha)
			peformFading();
		if(!actions.isEmpty())
			processActions();
	}

	/**
	 * Executed
	 */
	private void peformFading(){
		// Calculate the next alpha and constrain to 0-255 incl.
		float nextAlpha = alpha + eTime * speed;
		nextAlpha = (nextAlpha < 0) ? 0 : nextAlpha;
		nextAlpha = (nextAlpha >255) ? 255 : nextAlpha;
		// See if alpha and nextAlpha stradles target
		if((alpha - targetAlpha)*(nextAlpha - targetAlpha) <= 0) 
			currentAlpha = targetAlpha;
		else
			currentAlpha = (int)nextAlpha;
		alphaImpl(currentAlpha);
		alpha = nextAlpha;		
	}

	/*
	 * Loop through all the actions waiting to be processed reducing the time 
	 * before they need to be implemented. If the action is ready remove it 
	 * from the list and implement it.
	 */
	private void processActions(){
		Iterator<Action> iter = actions.iterator();
		while(iter.hasNext()){
			Action a = iter.next();
			a.delay -= eTime;
			if(a.delay <= 0){
				iter.remove();
				switch(a.type){
				case VISIBLE:
					visibleImpl(a.bool);
					break;
				case ENABLE:
					enableImpl(a.bool);
					break;
				case COLOR_SCHEME:
					colorImpl(a.target);
					break;
				case ALPHA_TO:
					if(a.duration <= 0)
						alphaImpl(a.target);
					else {
						speed = ((float)(a.target - currentAlpha))/(float) a.duration;
						alpha = currentAlpha;
						targetAlpha = a.target;
					}
					break;
				}
			}
		}
	}

	private void alphaImpl(int alpha){
		for(GAbstractControl control : children)
			control.setAlpha(alpha, true);		
	}

	private void colorImpl(int col){
		col &= 0xff; // make 0 -15
		for(GAbstractControl control : children)
			control.setLocalColorScheme(col,  true);		
	}

	private void enableImpl(boolean enable){
		for(GAbstractControl control : children)
			control.setEnabled(enable);		
	}

	private void visibleImpl(boolean visible){
		for(GAbstractControl control : children)
			control.setVisible(visible);		
	}


	/**
	 * Add one or more G4P controls to this group. <br>
	 * If a control is not a valid type for group control, or if the control and group are for different 
	 * windows then the control is not added and a warning message is displayed.
	 * 
	 * @param controls comma separated list of G4P controls to add to this group
	 */
	public void addControls(GAbstractControl... controls){
		if(controls != null)
			for(GAbstractControl control : controls)
				addControl(control);
	}

	/**
	 * A single G4P control to add to this group. <br>
	 * If a control is not a valid type for group control, or if the control and group are for different 
	 * windows then the control is not added and a warning message is displayed.
	 * 
	 * @param control a G4P control to add to this group
	 */
	public void addControl(GAbstractControl control){
		if(control != null){
			if(!control.isSuitableForGroupControl(control)){
				GMessenger.message(INVALID_TYPE, new Object[] { this, control } );
				return;				
			}
			if(this.winApp != control.winApp){
				GMessenger.message(INVALID_PAPPLET, new Object[] { this, control } );
				return;								
			}
			control.setAlpha(currentAlpha, true);
			children.add(control);
		}
	}

	/**
	 * Remove one or more G4P controls from this group. <br>
	 * 
	 * @param controls comma separated list of G4P controls to remove from this group
	 */
	public void removeControls(GAbstractControl... controls){
		if(controls != null)
			for(GAbstractControl control : controls)
				removeControl(control);
	}

	/**
	 * Remove a single G4P control from this group. <br>
	 * 
	 * @param control a G4P control to remove from this group
	 */
	public void removeControl(GAbstractControl control){
		if(control != null)
			children.remove(control);
	}


	private class Action {
		int type;
		long delay = 0;
		// Extras
		int duration = 0;
		boolean bool = false;
		int target = 0;

		/**
		 * Create an action
		 * 
		 * @param type
		 * @param delay
		 * @param extra
		 */
		public Action(int type, long delay, Object... extra) {
			super();
			this.type = type;
			this.delay = delay;
			switch(type){
			case VISIBLE:
			case ENABLE:
				bool = (Boolean)extra[0];
				break;
			case COLOR_SCHEME:
				target = (Integer)extra[0];
				break;
			case ALPHA_TO:
				target = (Integer)extra[0];
				duration = (Integer)extra[1];
				break;
			default:
				this.type = INVALID_ACTION;
			}
		}

		public String toString(){
			StringBuilder sb = new StringBuilder();
			switch(type){
			case VISIBLE:
				sb.append("VISIBLE       ");
				break;
			case ENABLE:
				sb.append("ENABLE        ");
				break;
			case COLOR_SCHEME:
				sb.append("COLOR_SCHEME  ");
				break;
			case ALPHA_TO:
				sb.append("ALPHA_TO   over  ");
				sb.append(duration + " millis  ");
				break;
			default:
				sb.append("INVALID       ");
				break;
			}
			return sb.toString();
		}
	}


}
