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

import automenta.vivisect.gui.HotSpot.HSalpha;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

/**
 * Buttons created from this class have 2 or more toggle states. If the number of states
 * is N then the button's value will be in the range 0 to N-1. Most toggle buttons will 
 * have just two states and these have values 0 and 1. <br>
 * Clicking on the button advances the state by one, restarting at zero after the last 
 * state. <br> 
 * Each state must have its own 'picture' and the user must supply these as a tiled image 
 * where the pictures are tiled in 1D or 2D arrangement without 'empty space' around the 
 * tiles. <br>
 * If for any reason the library is unable to use the specified graphics then it will 
 * provide a default two state toggle switch. <br>
 * It is also possible to provide an over-button image set for when the mouse moves 
 * over the button - this is optional. <br>
 * The button control will always be resized to suit the state picture size (tile size). <br>
 * The mouse is considered to be over the button it its position is over an opaque pixel 
 * in the state picture. Since transparent pixels are not included then the button shape 
 * can be different for each state. <br>
 * 
 * 
 * 
 * Three types of event can be generated :-  <br>
 * <b> GEvent.PRESSED  GEvent.RELEASED  GEvent.CLICKED </b><br>
 * 
 * To simplify event handling the button only fires off CLICKED events 
 * when the mouse button is pressed and released over the button face 
 * (the default behaviour). <br>
 * 
 * Using <pre>button1.fireAllEvents(true);</pre> enables the other 2 events
 * for button <b>button1</b>. A PRESSED event is created if the mouse button
 * is pressed down over the button face, the CLICKED event is then generated 
 * if the mouse button is released over the button face. Releasing the 
 * button off the button face creates a RELEASED event. This is included for 
 * completeness since it is unlikely you will need to detect these events 
 * for this type of control. <br>
 * 
 * 
 * @author Peter Lager
 *
 */
public class GImageToggleButton extends GAbstractControl {

	private static PImage toggle = null;
	private static final String TOGGLE = "toggle.png";
	
	protected int nbrStates = 2;
	protected int stateValue = 0;

	protected PImage[] offImage;
	protected PImage[] overImage;

	protected int status;
	protected boolean reportAllButtonEvents = false;


	/**
	 * Create the library default image-toggle-button at the stated position. <br>
	 * 
	 * @param theApplet
	 * @param p0 horizontal position of the control
	 * @param p1 vertical position of the control
	 */
	public GImageToggleButton(PApplet theApplet, float p0, float p1){
		this(theApplet, p0, p1, null, null, 1, 1);
	}

	/**
	 * Create an image-toggle-button. <br>
	 * Single row of tiles.
	 * 
	 * @param theApplet
	 * @param p0 horizontal position of the control
	 * @param p1 vertical position of the control
	 * @param offPicture the filename of bitmap containing toggle state pictures
	 * @param nbrCols number of tiles horizontally
	 */
	public GImageToggleButton(PApplet theApplet, float p0, float p1, String offPicture, int nbrCols){
		this(theApplet, p0, p1, offPicture, null, nbrCols, 1);
	}

	/**
	 * Create an image-toggle-button. <br>
	 * 
	 * @param theApplet
	 * @param p0 horizontal position of the control
	 * @param p1 vertical position of the control
	 * @param offPicture the filename of bitmap containing toggle state pictures
	 * @param nbrCols number of tiles horizontally
	 * @param nbrRows number of tiles vertically
	 */
	public GImageToggleButton(PApplet theApplet, float p0, float p1, String offPicture, int nbrCols, int nbrRows){
		this(theApplet, p0, p1, offPicture, null, nbrCols, nbrRows);
	}

	/**
	 * Create an image-toggle-button. <br>
	 * Single row of tiles.
	 * 
	 * @param theApplet
	 * @param p0 horizontal position of the control
	 * @param p1 vertical position of the control
	 * @param offPicture the filename of bitmap containing toggle state pictures
	 * @param overPicture the filename of bitmap containing mouse-over button toggle state pictures
	 * @param nbrCols number of tiles horizontally
	 */
	public GImageToggleButton(PApplet theApplet, float p0, float p1, String offPicture, String overPicture, int nbrCols){
		this(theApplet, p0, p1, offPicture, overPicture, nbrCols, 1);
	}

	/**
	 * Create an image-toggle-button. <br>
	 * 
	 * @param theApplet
	 * @param p0 horizontal position of the control
	 * @param p1 vertical position of the control
	 * @param offPicture the filename of bitmap containing toggle state pictures
	 * @param overPicture the filename of bitmap containing mouse-over button toggle state pictures
	 * @param nbrCols number of tiles horizontally
	 * @param nbrRows number of tiles vertically
	 */
	public GImageToggleButton(PApplet theApplet, float p0, float p1, String offPicture, String overPicture, int nbrCols, int nbrRows){
		super(theApplet, p0, p1);
		// Attempt to get off-control image data
		PImage temp = null;
		if(nbrCols < 1 || nbrRows < 1 || offPicture == null || null == (temp = ImageManager.loadImage(winApp, offPicture))){
			// Invalid data use default
			nbrStates = 2;
			if(toggle == null)
				toggle = ImageManager.loadImage(winApp, TOGGLE);
			offImage = ImageManager.makeTiles1D(winApp, toggle, 2, 1);
		}
		else {
			// Off-control image data valid
			nbrStates = nbrCols * nbrRows;
			offImage = ImageManager.makeTiles1D(winApp, temp, nbrCols, nbrRows);
			// Now check for over-control image data
			if(overPicture != null && null != (temp = ImageManager.loadImage(winApp, overPicture))){
				overImage = ImageManager.makeTiles1D(winApp, temp, nbrCols, nbrRows);
			}
		}
		// The control will always be resized to match the image size
		resize(offImage[0].width, offImage[0].height);

		//========================================================================
		// Setup the hotspots
		hotspots = new HotSpot[]{
				new HSalpha(1, 0, 0, offImage[stateValue], PApplet.CORNER)
		};

		//========================================================================

		z = Z_SLIPPY;
		// Now register control with applet
		createEventHandler(G4P.sketchApplet, "handleToggleButtonEvents",
				new Class<?>[]{ GImageToggleButton.class, GEvent.class }, 
				new String[]{ "button", "event" } 
		);
		registeredMethods = DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		G4P.addControl(this);
	}

	public void draw(){
		if(!visible) return;

		// Update buffer if invalid
		//updateBuffer();
		winApp.pushStyle();

		winApp.pushMatrix();
		// Perform the rotation
		applyTransform();
		// Move matrix to line up with top-left corner
		winApp.translate(-halfWidth, -halfHeight);
		// Draw buffer
		winApp.imageMode(PApplet.CORNER);
		if(alphaLevel < 255)
			winApp.tint(TINT_FOR_ALPHA, alphaLevel);
		if(status == OVER_CONTROL && overImage != null)
			winApp.image(overImage[stateValue], 0, 0);
		else
			winApp.image(offImage[stateValue], 0, 0);

		winApp.popMatrix();		
		winApp.popStyle();
	}


	/**
	 * 
	 * When a mouse button is clicked on a GImageToggleButton it generates the GEvent.CLICKED event. If
	 * you also want the button to generate GEvent.PRESSED and GEvent.RELEASED events
	 * then you need the following statement.<br>
	 * <pre>btnName.fireAllEvents(true); </pre><br>
	 * <pre>
	 * void handleButtonEvents(void handleToggleButtonEvents(GImageToggleButton button, GEvent event) {
	 *	  if(button == btnName && event == GEvent.CLICKED){
	 *        int buttonState = btnName.stateValue();
	 *    }
	 * </pre> <br>
	 * Where <pre><b>btnName</b></pre> is the GImageToggleButton identifier (variable name) <br><br>
	 * 
	 */
	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		if(currSpot >= 0 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(focusIsWith != this && currSpot >= 0  && z > focusObjectZ()){
				dragging = false;
				status = PRESS_CONTROL;
				takeFocus();
				if(reportAllButtonEvents)
					fireEvent(this, GEvent.PRESSED);
			}
			break;
		case MouseEvent.CLICK:
			// No need to test for isOver() since if the component has focus
			// and the mouse has not moved since MOUSE_PRESSED otherwise we 
			// would not get the Java MouseEvent.MOUSE_CLICKED event
			if(focusIsWith == this){
				status = OFF_CONTROL;
				loseFocus(null);
				dragging = false;
				nextState();
				fireEvent(this, GEvent.CLICKED);
			}
			break;
		case MouseEvent.RELEASE:	
			// if the mouse has moved then release focus otherwise
			// MOUSE_CLICKED will handle it
			if(focusIsWith == this && dragging){
				if(currSpot >= 0){
					nextState();
					fireEvent(this, GEvent.CLICKED);
				}
				else {
					if(reportAllButtonEvents){
						fireEvent(this, GEvent.RELEASED);
					}
				}
				dragging = false;
				loseFocus(null);
				status = OFF_CONTROL;
			}
			break;
		case MouseEvent.MOVE:
			// If dragged state will stay as PRESSED
			if(currSpot >= 0)
				status = OVER_CONTROL;
			else
				status = OFF_CONTROL;
			break;
		case MouseEvent.DRAG:
			dragging = (focusIsWith == this);
			break;
		}
	}

	/**
	 * Advance to the next state and adjust the hotspot to use the current image
	 */
	private void nextState(){
		stateValue++;
		stateValue %= nbrStates;
		hotspots[0].adjust(0,0,offImage[stateValue]);
	}

	/**
	 * Get the current state value of the button.
	 * @deprecated use getState()
	 */
	@Deprecated
	public int stateValue(){
		return stateValue;
	}
	
	/**
	 * Get the current state value of the button.
	 */
	public int getState(){
		return stateValue;
	}
	
	/**
	 * Change the current toggle state. <br>
	 * If the parameter is not a valid toggle state value then it
	 * is ignored and the button's state value is unchanged.
	 * @deprecated use setState(int)
	 * @param newState
	 */
	@Deprecated
	public void stateValue(int newState){
		if(newState >= 0 && newState < nbrStates && newState != stateValue){
			stateValue = newState;
			hotspots[0].adjust(0,0,offImage[stateValue]);
			bufferInvalid = true;
		}
	}
	
	/**
	 * Change the current toggle state. <br>
	 * If the parameter is not a valid toggle state value then it
	 * is ignored and the button's state value is unchanged.
	 * @param newState
	 */
	public void setState(int newState){
		if(newState >= 0 && newState < nbrStates && newState != stateValue){
			stateValue = newState;
			hotspots[0].adjust(0,0,offImage[stateValue]);
			bufferInvalid = true;
		}
	}
	
	/**
	 * If the parameter is true all 3 event types are generated, if false
	 * only CLICKED events are generated (default behaviour). <br>
	 * For this toggle control I can't see the need for anything but
	 * CLICKED events
	 * @param all
	 */
	public void fireAllEvents(boolean all){
		reportAllButtonEvents = all;
	}
	 
}
