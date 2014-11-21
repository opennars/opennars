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

import automenta.vivisect.gui.HotSpot.HScircle;
import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * This control simulates a digital joystick and is designed to give more
 * intuitive control in game scenarios where you might use the keyboard 
 * e.g. WASD keys for movement. <br>
 * 
 * The joystick has two modes - in the default mode the joystick just 
 * responds to movement in 4 directions (left,right, up and down) the 
 * second mode allows for diagonals so recognises 8 directions. <br>
 * 
 * As in a real joystick you have a dead zone near the centre which 
 * does not generate signals, thus avoiding jitter. This area is shown 
 * graphically. <br>
 * 
 * The direction of the joystick is represented by an integer in the 
 * range 0-7 and -1 when in the dead zone. <br>
 * <pre>
 *     5   6   7
 * 		\  |  /
 *       \ | /
 *   4 --- + --- 0         + is the dea zone so -1
 *       / | \
 *      /  |  \
 *     3   2   1
 * </pre> <br>
 * As well as the direction there are two useful methods to decode these 
 * into X and Y directions - <pre>getStickX()</pre> and <pre>getStickY()</pre>
 * which give the values. <br>
 * <pre>
 * X= -1   0   +1 
 * 		\  |  /  -1
 *       \ | /
 *     --- + ---  0
 *       / | \
 *      /  |  \
 *           Y=  +1
 * </pre> <br>
 * The stick will auto-center when released. <br>
 * 
 * The minimum size for this control is 40x40 pixels and this is enforced 
 * when the control is created. If necessary the width and/or height the 
 * rectangle will be increased to 40pixels.
 * 
 * @author Peter Lager
 *
 */
public class GStick extends GControl {
	// palette index constants
	protected static final int BORDERS = 0;
	protected static final int LED_INACTIVE = 1;
	protected static final int LED_ACTIVE = 14;
	protected static final int STICK = 0;
	protected static final int STICK_TOP = 3;
	protected static final int STICK_TOP_OVER = 11;
	protected static final int STICK_TOP_PRESS = 14;
	protected static final int STICK_TOP_DRAG = 15;
	protected static final int OUTERRING = 6;
	protected static final int ACTIONRING = 5;
	protected static final int BACK = 6;
	protected static final int ROD = 1;
	//angle constants
	protected static final float RAD90 = PApplet.radians(90);
	protected static final float RAD45 = PApplet.radians(45);
	protected static final float RAD22_5 = PApplet.radians(22.5f);


	protected static final int[] posMap = new int[] { 0x01, 0x07, 0x04, 0x1c, 0x10, 0x70, 0x40, 0xc1 };
	protected static final int[] posX = new int[] {  1,  1,  0, -1, -1, -1,  0,  1 };
	protected static final int[] posY= new int[] {  0,  1,  1,  1,  0, -1, -1, -1 };
	

	protected final float ledWidth, ledHeight;
	protected float ledRingRad;

	protected float actionRad, actionRadLimit, gripRad, rodRad;
	protected float rodLength = 0, stickAngle;
	
	protected int position = -1;
	protected int mode = X4; // X4 or X8
	protected int status = OFF_CONTROL;

	/**
	 * Create the stick inside the specified rectangle. 
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GStick(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
		// Enforce minimum size constraint
		if(width < 40 || height < 40)
			resize(PApplet.max(Math.round(width),40), PApplet.max(Math.round(height),40));

		// Customise buffer for this control
		buffer.ellipseMode(PApplet.CORNER);
		opaque = false;
		// Calculate stick metrics
		float stickSize = PApplet.min(width, height);
		float mag = stickSize/50;
		ledWidth = 6 * mag;
		ledHeight = 1.6f * ledWidth;
		ledRingRad = (stickSize - ledWidth - 3)/2;
		actionRad = 0.50f * ledRingRad;
		gripRad = 4.0f * mag;
		rodRad = 3.0f * mag;	
		actionRadLimit = ledRingRad - gripRad - ledWidth/2;
		
		hotspots = new HotSpot[]{
				new HScircle(1, width/2, height/2, gripRad)
		};
		z = Z_SLIPPY;

		// Now register control with applet
		createEventHandler(GUI.sketchApplet, "handleStickEvents",
				new Class<?>[]{ GStick.class, GEvent.class }, 
				new String[]{ "stick", "event" } 
		);
		registeredMethods = DRAW_METHOD | MOUSE_METHOD ;
		cursorOver = HAND;
		GUI.addControl(this);
	}

	/**
	 * Sets the stick mode to either 4 or 8 directions. <br>
 If the mode parameter should be either GUI.X4 or GUI.X8 any
 other value will be silently ignored
	 * 
	 * @param m the new mode
	 */
	public void setMode(int m){
		if(m != mode && m == X4 || m == X8){
			mode = m;
			bufferInvalid = true;
		}
	}
	
	/**
	 * Get the current mode
	 */
	public int getMode(){
		return mode;
	}
	
	/**
	 * Returns the current position of the stick based on <br>
	 * <pre>
	 *     5   6   7
	 * 		\  |  /
	 *       \ | /
	 *   4 --- + --- 0         + is the dea zone so -1
	 *       / | \
	 *      /  |  \
	 *     3   2   1
	 * </pre> <br>
	 * @return current stick direction
	 */
	public int getPosition(){
		return position;
	}
	
	/**
	 * Get the X position of the stick from <br>
	 *  * <pre>
	 * X= -1   0   +1 
	 * 		\  |  /  -1
	 *       \ | /
	 *     --- + ---  0
	 *       / | \
	 *      /  |  \
	 *           Y=  +1
	 * </pre> <br>
	 * @return the X value (-1, 0 or 1)
	 */
	public int getStickX(){
		return (position < 0) ? 0 : posX[position];
	}
	
	/**
	 * Get the Y position of the stick from <br>
	 *  * <pre>
	 * X= -1   0   +1 
	 * 		\  |  /  -1
	 *       \ | /
	 *     --- + ---  0
	 *       / | \
	 *      /  |  \
	 *           Y=  +1
	 * </pre> <br>
	 * @return the Y value (-1, 0 or 1)
	 */
	public int getStickY(){
		return (position < 0) ? 0 : posY[position];
	}

	/**
	 * Calculate the angle to the knob centre making sure it is in
	 * the range 0-360
	 * @param px relative to centre
	 * @param py relative to centre
	 * @return the angle made by the stick
	 */
	protected float calcStickAngle(float px, float py){
		float a = PApplet.atan2(py, px);
		if(a < 0)
			a += PApplet.TWO_PI;
		return a;	
	}
	
	/**
	 * Calculate the position (mode dependent) from an angle in the range 0-2PI 
	 * @param a the angle 0-2PI
	 * @return direction (0-7)
	 */
	protected int getPositionFromAngle(float a){
		int newState;
		if(mode == 1){
			a = (a + RAD45) % PApplet.TWO_PI;
			newState = 2 * (int)(a / RAD90);
		}
		else {
			a = (a + RAD22_5) % PApplet.TWO_PI;
			newState = (int)(a / RAD45);
		}
		return newState % 8;
	}

	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		// Make ox and oy relative to the centre of the stick
		ox -= width/2;
		oy -= height/2;

		// currSpot == 1 for text display area
		if(currSpot >= 0 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(focusIsWith != this && currSpot > -1 && z > focusObjectZ()){
				status = PRESS_CONTROL;
				position = -1;
				rodLength = PApplet.sqrt(ox*ox + oy*oy);
				stickAngle = calcStickAngle(ox, oy);
				dragging = false;
				takeFocus();
				bufferInvalid = true;
			}
			break;
		case MouseEvent.RELEASE:
			if(focusIsWith == this){
				loseFocus(null);
			}
			// If we are not already near the centre then make it so
			// and fire an event
			if(position != -1){
				position = -1;
				fireEvent(this, GEvent.CHANGED);
			}
			hotspots[0].adjust(width/2, height/2);
			rodLength = stickAngle = 0;
			dragging = false;
			status = OFF_CONTROL;
			bufferInvalid = true;
			break;
		case MouseEvent.DRAG:
			if(focusIsWith == this){
				status = DRAG_CONTROL;
				dragging = true;
				rodLength = PApplet.sqrt(ox*ox + oy*oy);
				stickAngle = calcStickAngle(ox, oy);
				int newPosition = -1;
				if(rodLength >= actionRad){
					newPosition = getPositionFromAngle(stickAngle);
				}
				if(rodLength > actionRadLimit){
					ox = actionRadLimit * PApplet.cos(stickAngle);
					oy = actionRadLimit * PApplet.sin(stickAngle);
					rodLength = actionRadLimit;
				}
				hotspots[0].adjust(ox + width/2, oy + height/2);	
				if(newPosition != position){
					position = newPosition;
					fireEvent(this, GEvent.CHANGED);
				}
				bufferInvalid = true;
			}
			break;
		case MouseEvent.MOVE:
			int currStatus = status;
			// If dragged state will stay as PRESSED
			if(currSpot == 1)
				status = OVER_CONTROL;
			else
				status = OFF_CONTROL;
			if(currStatus != status)
				bufferInvalid = true;
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
			bufferInvalid = false;
			buffer.beginDraw();
			// Back ground colour
			if(opaque == true)
				buffer.background(palette[BACK]);
			else
				buffer.background(buffer.color(255,0));
			// Move origin to centre

			buffer.translate(width/2, height/2);

			buffer.fill(palette[OUTERRING]);
			buffer.stroke(palette[BORDERS]);
			buffer.strokeWeight(1.0f);
			buffer.ellipse(0,0,2*ledRingRad, 2*ledRingRad);
			buffer.ellipse(0,0,2*actionRad, 2*actionRad);
			// Draw everything except the stick
			buffer.pushMatrix();
			int led = 0x00000001, delta = 2/mode;
			for(int i = 0; i < 8; i += delta){
				buffer.stroke(palette[BORDERS]);
				buffer.strokeWeight(1.0f);
				buffer.line(0,0,ledRingRad,0);
				// Only draw LEDs on even directions
				if(i%2 == 0){
					buffer.noStroke();
					if(position >= 0 && (posMap[position] & led) == led)
						buffer.fill(palette[LED_ACTIVE]);
					else
						buffer.fill(palette[LED_INACTIVE]);
					buffer.ellipse(ledRingRad,0,ledWidth,ledHeight);
				}
				led <<= delta;
				buffer.rotate(delta * RAD45);
			}
			buffer.popMatrix();
			
			// Draw the inactive area near the centre of the 
			buffer.fill(palette[ACTIONRING]);
			buffer.stroke(palette[BORDERS]);
			buffer.strokeWeight(1.0f);
			buffer.ellipse(0,0,2*actionRad, 2*actionRad);

			// Draw the rod and button
			buffer.pushMatrix();
			buffer.rotate(stickAngle);
			buffer.noStroke();
			buffer.fill(palette[ROD]);
			buffer.ellipse(0,0,2*rodRad,2*rodRad);
			buffer.rect(0,-rodRad,rodLength,2*rodRad);
			buffer.strokeWeight(1);
			buffer.stroke(palette[ROD]);
			buffer.fill(palette[STICK_TOP_DRAG]);
			// Draw thumb
			switch(status){
			case OFF_CONTROL:
				buffer.fill(palette[STICK_TOP]);
				break;
			case OVER_CONTROL:
				buffer.fill(palette[STICK_TOP_OVER]);
				break;
			case PRESS_CONTROL:
				buffer.fill(palette[STICK_TOP_PRESS]);
				break;
			case DRAG_CONTROL:
				buffer.fill(palette[STICK_TOP_DRAG]);
				break;
			}
			buffer.ellipse(rodLength,0,2*gripRad, 2*gripRad);		
			buffer.popMatrix();
			buffer.endDraw();
		}	
	}
}
