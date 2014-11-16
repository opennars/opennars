/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2008 Peter Lager

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

import automenta.vivisect.gui.HotSpot.HSrect;
import automenta.vivisect.gui.StyledString.TextLayoutInfo;

import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * This class is the Button component.
 * 
 * The button face can have either text or an image or both just
 * pick the right constructor.
 * 
 * Three types of event can be generated :-  <br>
 * <b> PRESSED  RELEASED  CLICKED </b><br>
 * 
 * To simplify event handling the button only fires off CLICKED events 
 * if the mouse button is pressed and released over the button face 
 * (the default behaviour). <br>
 * 
 * Using <pre>button1.fireAllEvents(true);</pre> enables the other 2 events
 * for button <b>button1</b>. A PRESSED event is created if the mouse button
 * is pressed down over the button face, the CLICKED event is then generated 
 * if the mouse button is released over the button face. Releasing the 
 * button off the button face creates a RELEASED event. <br>
 * 
 * The image file can either be a single image which is used for 
 * all button states, or be a composite of 3 images (tiled horizontally)
 * which are used for the different button states OFF, OVER and DOWN 
 * in which case the image width should be divisible by 3. <br>
 * A number of setImages(...) methods exist to set button state images, these
 * can be used once the button is created.<br>
 * 
 * 
 * @author Peter Lager
 *
 */
public class GButton extends GTextIconAlignBase {

	// Mouse over status
	protected int status = 0;

	// Only report CLICKED events
	protected boolean reportAllButtonEvents = false;
	
	public GButton(PApplet theApplet, float p0, float p1, float p2, float p3) {
		this(theApplet, p0, p1, p2, p3, "");
	}
	
	public GButton(PApplet theApplet, float p0, float p1, float p2, float p3, String text) {
		super(theApplet, p0, p1, p2, p3);
		hotspots = new HotSpot[]{
				new HSrect(1, 0, 0, width, height)		// control surface
		};
		setText(text);
		z = Z_SLIPPY;
		// Now register control with applet
		createEventHandler(G4P.sketchApplet, "handleButtonEvents", 
				new Class<?>[]{ GButton.class, GEvent.class }, 
				new String[]{ "button", "event" } 
		);
		registeredMethods = DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		G4P.addControl(this);
	}
	
	/**
	 * If the parameter is true all 3 event types are generated, if false
	 * only CLICKED events are generated (default behaviour).
	 * @param all
	 */
	public void fireAllEvents(boolean all){
		reportAllButtonEvents = all;
	}

	/**
	 * Enable or disable the ability of the component to generate mouse events.<br>
	 * If the control is to be disabled when it is clicked then this will force the
	 * mouse off button image is used.
	 * @param enable true to enable else false
	 */
	public void setEnabled(boolean enable){
		super.setEnabled(enable);
		if(!enable)
			status = OFF_CONTROL;
	}
	
	/**
	 * 
	 * When a mouse button is clicked on a GButton it generates the GEvent.CLICKED event. If
	 * you also want the button to generate GEvent.PRESSED and GEvent.RELEASED events
	 * then you need the following statement.<br>
	 * <pre>btnName.fireAllEvents(true); </pre><br>
	 * 
	 * <pre>
	 * void handleButtonEvents(void handleButtonEvents(GButton button, GEvent event) {
	 *	  if(button == btnName && event == GEvent.CLICKED){
	 *        // code for button click event
	 *    }
	 * </pre> <br>
	 * Where <pre><b>btnName</b></pre> is the GButton identifier (variable name) <br><br>
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
				bufferInvalid = true;
			}
			break;
		case MouseEvent.CLICK:
			// No need to test for isOver() since if the component has focus
			// and the mouse has not moved since MOUSE_PRESSED otherwise we 
			// would not get the Java MouseEvent.MOUSE_CLICKED event
			if(focusIsWith == this){
				status = OFF_CONTROL;
				bufferInvalid = true;
				loseFocus(null);
				dragging = false;
				fireEvent(this, GEvent.CLICKED);
			}
			break;
		case MouseEvent.RELEASE:	
			// if the mouse has moved then release focus otherwise
			// MOUSE_CLICKED will handle it
			if(focusIsWith == this && dragging){
				if(reportAllButtonEvents)
					fireEvent(this, GEvent.RELEASED);
				dragging = false;
				loseFocus(null);
				status = OFF_CONTROL;
				bufferInvalid = true;
			}
			break;
		case MouseEvent.MOVE:
			int currStatus = status;
			// If dragged state will stay as PRESSED
			if(currSpot >= 0)
				status = OVER_CONTROL;
			else
				status = OFF_CONTROL;
			if(currStatus != status)
				bufferInvalid = true;
			break;
		case MouseEvent.DRAG:
			dragging = (focusIsWith == this);
			break;
		}
	}
	
	public void draw(){
		if(!visible) return;

		// Update buffer if invalid
		updateBuffer();
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
		winApp.image(buffer, 0, 0);	
		winApp.popMatrix();		
		winApp.popStyle();
	}
	
	protected void updateBuffer(){
		if(bufferInvalid) {
			Graphics2D g2d = buffer.g2;
			// Get the latest lines of text
			LinkedList<TextLayoutInfo> lines = stext.getLines(g2d);	
			bufferInvalid = false;
			buffer.beginDraw();
			// Back ground colour
			switch(status){
			case OVER_CONTROL:
				buffer.background(palette[6]);
				break;
			case PRESS_CONTROL:
				buffer.background(palette[14]);
				break;
			default:
				buffer.background(palette[4]);
			}
			g2d.setColor(jpalette[3]);
			g2d.setStroke(pen_1_0);
			g2d.drawRect(0, 0, (int)width-1, (int)height-1);
			// Calculate text and icon placement
			calcAlignment();
			// If there is an icon draw it
			if(iconW != 0)
				buffer.image(bicon[status], siX, siY);
			float wrapWidth = stext.getWrapWidth();
			float sx = 0, tw = 0;
			buffer.translate(stX, stY);
			for(TextLayoutInfo lineInfo : lines){
				TextLayout layout = lineInfo.layout;
				buffer.translate(0, layout.getAscent());
				switch(textAlignH){
				case CENTER:
					tw = layout.getVisibleAdvance();
					tw = (tw > wrapWidth) ? tw - wrapWidth : tw;
					sx = (wrapWidth - tw)/2;
					break;
				case RIGHT:
					tw = layout.getVisibleAdvance();
					tw = (tw > wrapWidth) ? tw - wrapWidth : tw;
					sx = wrapWidth - tw;
					break;
				case LEFT:
				case JUSTIFY:
				default:
					sx = 0;		
				}
				// display text
				g2d.setColor(jpalette[2]);
				lineInfo.layout.draw(g2d, sx, 0);
				buffer.translate(0, layout.getDescent() + layout.getLeading());
			}
			buffer.endDraw();
		}	
	}
}
