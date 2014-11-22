/*
  Part of the GUI library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2013 Peter Lager

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
import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * This slider is used to control 2 variables by dragging the thumb over
 * a 2D surface. It has all the features of the standard slider (GSlider)
 * except that it does not have ticks or stick-to-ticks functionality. <br>
 *  
 * If no limits are set then the control will return a value in the range 
 * 0.0 to 1.0 for both the x and the y axis. The setXlimits and setYlimits
 * can be used to set a different range for each axis independently. <b>
 * 
 * The minimum size for this control is 40x40 pixels and this is enforced 
 * when the control is created. If necessary the width and/or height the 
 * rectangle will be increased to 40pixels. <br>
 * 
 * @author Peter Lager
 *
 */
public class GSlider2D extends GValueControl2D {

	// Palette index constants
	static int DBORDER = 1, LBORDER = 2, BACK = 3;
	static int TBORDER = 15, TOFF = 3, TOVER = 11, TDOWN = 14, TDRAG = 6;

	float THUMB_SIZE = 10;
	float HALF_THUMB_SIZE = THUMB_SIZE / 2;
	float BORDER_WIDTH = 2;
	
	// Define the drag area for this control
	protected float dragWidth, dragHeight, dragD;
	
	protected int downHotSpot = -1;
	// Mouse over status
	protected int status = OFF_CONTROL;
	
	protected float startXlimit = 0, endXlimit = 1;
	protected float startYlimit = 0, endYlimit = 1;
	
	/**
	 * Create a 2D slider inside the specified rectangle.
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GSlider2D(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
		// Enforce minimum size constraint
		if(width < 40 || height < 40)
			resize(PApplet.max(Math.round(width),40), PApplet.max(Math.round(height),40));
	
		dragWidth = width - THUMB_SIZE - 2 * BORDER_WIDTH;
		dragHeight = height - THUMB_SIZE - 2 * BORDER_WIDTH;
		dragD = 2 + THUMB_SIZE/2;
		
		hotspots = new HotSpot[]{
				new HSrect(THUMB_SPOT, dragD - HALF_THUMB_SIZE + parametricPosX * dragWidth, 
						dragD - HALF_THUMB_SIZE + parametricPosY * dragHeight, THUMB_SIZE, THUMB_SIZE ),  // thumb
				new HSrect(TRACK_SPOT, dragD, dragD, dragWidth, dragHeight)		// track
		};
		z = Z_SLIPPY;

		epsilon = 0.98f / PApplet.max(dragWidth, dragHeight);
		opaque = true;
		
		// Now register control with applet
		createEventHandler(GUI.applet, "handleSlider2DEvents",
				new Class<?>[]{ GSlider2D.class, GEvent.class },
				new String[]{ "slider2d", "event" }
		);
		registeredMethods = PRE_METHOD | DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		GUI.addControl(this);
	}

	/**
	 * Updates thumb hotspot due changes caused by easing
	 */
	protected void updateDueToValueChanging(){
		hotspots[0].x = dragD - HALF_THUMB_SIZE  + parametricPosX * dragWidth;
		hotspots[0].y = dragD  - HALF_THUMB_SIZE + parametricPosY * dragHeight;	
	}

	
	/**
	 * Set the amount of easing to be used when a value is changing. The default value
	 * is 1 (no easing) values > 1 will cause the value to rush from its starting value
	 * and decelerate towards its final values. In other words it smoothes the movement
	 * of the slider thumb or knob rotation.
	 * 
	 * @param easeBy the easing to set
	 */
	public void setEasing(float easeBy) {
		easing = (easeBy < 1) ? 1 : easeBy;
	}

	/**
	 * X (horz) limits
	 * Sets the range of values to be returned. This method will
	 * assume that you want to set the valueType to INTEGER
	 * 
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setXlimits(int start, int end){
		startXlimit = start;
		endXlimit = end;
		setEpsilon();
		valueType = INTEGER;
		bufferInvalid = true;
	}
	
	/**
	 * X (horz) limits
	 * Sets the initial value and the range of values to be returned. This 
	 * method will assume that you want to set the valueType to INTEGER.
	 * 
	 * @param initValue the initial value
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimitsX(int initValue, int start, int end){
		startXlimit = start;
		endXlimit = end;
		valueType = INTEGER;
		setEpsilon();
		bufferInvalid = true;
		setValueX(initValue);
		updateDueToValueChanging();
	}
	
	/**
	 * X (horz) limits
	 * Sets the range of values to be returned. This method will
	 * assume that you want to set the valueType to DECIMAL
	 * 
	 * @param start
	 * @param end
	 */
	public void setLimitsX(float start, float end){
		startXlimit = start;
		endXlimit = end;
		if(valueType == INTEGER){
			valueType = DECIMAL;
			setPrecision(1);
		}
		setEpsilon();
		bufferInvalid = true;
	}
	
	/**
	 * X (horz) limits
	 * Sets the initial value and the range of values to be returned. This 
	 * method will assume that you want to set the valueType to DECIMAL.
	 * 
	 * @param initValue the initial value
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimitsX(float initValue, float start, float end){
		startXlimit = start;
		endXlimit = end;
		initValue = PApplet.constrain(initValue, start, end);
		if(valueType == INTEGER){
			valueType = DECIMAL;
			setPrecision(1);
		}
		setEpsilon();
		bufferInvalid = true;
		setValueX(initValue);
		updateDueToValueChanging();
	}

	/**
	 * Y (vert) limits
	 * Sets the range of values to be returned. This method will
	 * assume that you want to set the valueType to INTEGER
	 * 
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimitsY(int start, int end){
		startYlimit = start;
		endYlimit = end;
		setEpsilon();
		valueType = INTEGER;
		bufferInvalid = true;
	}
	
	/**
	 * Y (vert) limits
	 * Sets the initial value and the range of values to be returned. This 
	 * method will assume that you want to set the valueType to INTEGER.
	 * 
	 * @param initValue the initial value
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimitsY(int initValue, int start, int end){
		startYlimit = start;
		endYlimit = end;
		valueType = INTEGER;
		setEpsilon();
		bufferInvalid = true;
		setValueY(initValue);
		updateDueToValueChanging();
	}
	
	/**
	 * Y (vert) limits
	 * Sets the range of values to be returned. This method will
	 * assume that you want to set the valueType to DECIMAL
	 * 
	 * @param start
	 * @param end
	 */
	public void setLimitsY(float start, float end){
		startYlimit = start;
		endYlimit = end;
		if(valueType == INTEGER){
			valueType = DECIMAL;
			setPrecision(1);
		}
		setEpsilon();
		bufferInvalid = true;
	}
	
	/**
	 * Y (vert) limits
	 * Sets the initial value and the range of values to be returned. This 
	 * method will assume that you want to set the valueType to DECIMAL.
	 * 
	 * @param initValue the initial value
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimitsY(float initValue, float start, float end){
		startYlimit = start;
		endYlimit = end;
		initValue = PApplet.constrain(initValue, start, end);
		if(valueType == INTEGER){
			valueType = DECIMAL;
			setPrecision(1);
		}
		setEpsilon();
		bufferInvalid = true;
		setValueY(initValue);
		updateDueToValueChanging();
	}

	/**
	 * Set the X (horz) value for the slider. <br>
	 * The value supplied will be constrained to the current limits.
	 * @param v the new value
	 */
	public void setValueX(float v){
		if(valueType == INTEGER)
			v = Math.round(v);
		float t = (v - startXlimit) / (endXlimit - startXlimit);
		t = PApplet.constrain(t, 0.0f, 1.0f);
		parametricTargetX = t;
	}
	
	/**
	 * Set the Y (vert) value for the slider. <br>
	 * The value supplied will be constrained to the current limits.
	 * @param v the new value
	 */
	public void setValueY(float v){
		if(valueType == INTEGER)
			v = Math.round(v);
		float t = (v - startYlimit) / (endYlimit - startYlimit);
		t = PApplet.constrain(t, 0.0f, 1.0f);
		parametricTargetY = t;
	}
	
	/**
	 * Set both the XY values for the slider. <br>
	 * The values supplied will be constrained to the appropriate current limits.
	 * @param vx the new X (horz) value
	 * @param vy the new Y (vert) value
	 */
	public void setValueXY(float vx, float vy){
		setValueX(vx);
		setValueY(vy);
	}
	
	/**
	 * Get the current X value as a float
	 */
	public float getValueXF(){
		return startXlimit + (endXlimit - startXlimit) * parametricPosX;
	}

	/**
	 * Get the current X value as an integer. <br>
	 * DECIMAL and EXPONENT value types will be rounded to the nearest integer.
	 */
	public int getValueXI(){
		return Math.round(getValueXF());
	}
	
	/**
	 * Get the current X value as a string taking into account the number format. <br>
	 */
	public String getValueXS(){
		return getNumericDisplayString(getValueXF());
	}
	
	/**
	 * Get the current Y value as a float
	 */
	public float getValueYF(){
		return startYlimit + (endYlimit - startYlimit) * parametricPosY;
	}

	/**
	 * Get the current Y value as an integer. <br>
	 * DECIMAL and EXPONENT value types will be rounded to the nearest integer.
	 */
	public int getValueYI(){
		return Math.round(getValueYF());
	}
	
	/**
	 * Get the current Y value as a string taking into account the number format. <br>
	 */
	public String getValueYS(){
		return getNumericDisplayString(getValueYF());
	}
	
	/**
	 * Used to format the number into a string for display.
	 * @param number
	 * @return the number formated as a string
	 */
	protected String getNumericDisplayString(float number){
		String s = "";
		switch(valueType){
		case INTEGER:
			s = String.format("%d", Math.round(number));
			break;
		case DECIMAL:
			s = String.format("%." + precision + "f", number);
			break;
		case EXPONENT:
			s = String.format("%." + precision + "e", number);
			break;
		}
		return s.trim();
	}

	/**
	 * For DECIMAL values this sets the number of decimal places to 
	 * be displayed.
	 * @param nd must be >= 1 otherwise will use 1
	 */
	public void setPrecision(int nd){
		nd = PApplet.constrain(nd, 1, 5);
		if(nd < 1)
			nd = 1;
		if(nd != precision){
			precision = nd;
			setEpsilon();
			bufferInvalid = true;
		}
	}
	
	/**
	 * Set the numberFormat and precision in one go. <br>
	 * Valid number formats are INTEGER, DECIMAL, EXPONENT <br>
	 * Precision must be >= 1 and is ignored for INTEGER.
	 * 
	 * @param numberFormat GUI.INTEGER, GUI.DECIMAL orG4P. EXPONENT
	 * @param precision must be >= 1
	 */
	public void setNumberFormat(int numberFormat, int precision){
		switch(numberFormat){
		case INTEGER:
		case DECIMAL:
		case EXPONENT:
			this.valueType = numberFormat;
			break;
		default:
			valueType = DECIMAL;
		}
		setPrecision(precision);
		bufferInvalid = true;
	}
		
	/**
	 * Set the numberFormat and precision in one go. <br>
	 * Valid number formats are INTEGER, DECIMAL, EXPONENT <br>
	 * Precision must be >= 1 and is ignored for INTEGER.
	 * 
	 * @param numberFormat GUI.INTEGER, GUI.DECIMAL or GUI.EXPONENT
	 */
	public void setNumberFormat(int numberFormat){
		switch(numberFormat){
		case INTEGER:
		case DECIMAL:
		case EXPONENT:
			this.valueType = numberFormat;
			break;
		default:
			valueType = DECIMAL;
		}
		bufferInvalid = true;
	}
	
	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		// Make ox,oy relative to top-left of drag area
		ox -= dragD;
		oy -= dragD;
	                
		if(currSpot >= 0 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(focusIsWith != this && currSpot > -1 && z >= focusObjectZ()){
				downHotSpot = currSpot;
				status = (downHotSpot == THUMB_SPOT) ? PRESS_CONTROL : OFF_CONTROL;
				offsetH = ox - parametricPosX * dragWidth; // normalised
				offsetV = oy - parametricPosY * dragHeight; // normalised
				takeFocus();
				bufferInvalid = true;
			}
			break;
		case MouseEvent.CLICK:
			if(focusIsWith == this){
				parametricTargetX = ox / dragWidth;
				parametricTargetY = oy / dragHeight;
				dragging = false;
				status = OFF_CONTROL;
				loseFocus(null);
				bufferInvalid = true;
			}
			break;
		case MouseEvent.RELEASE:
			if(focusIsWith == this && dragging){
				if(downHotSpot == THUMB_SPOT){
					mouseUpdateTargets();
				}
				status = OFF_CONTROL;
				bufferInvalid = true;
				loseFocus(null);				
			}
			dragging = false;
			break;
		case MouseEvent.DRAG:
			if(focusIsWith == this){
				status = DRAG_CONTROL;
				dragging = true;
				if(downHotSpot == THUMB_SPOT){
					mouseUpdateTargets();
					bufferInvalid = true;
				}
			}
			break;
		case MouseEvent.MOVE:
			int currStatus = status;
			// If dragged state will stay as PRESSED
			if(currSpot == THUMB_SPOT)
				status = OVER_CONTROL;
			else
				status = OFF_CONTROL;
			if(currStatus != status)
				bufferInvalid = true;
			break;			
		}
	}

	/**
	 * Convenience method called during mouse event handling
	 */
	private void mouseUpdateTargets(){
		parametricTargetX = ox / dragWidth;
		if(parametricTargetX < 0){
			parametricTargetX = 0;
			offsetH = 0;
		}
		else if(parametricTargetX > 1){
			parametricTargetX = 1;
			offsetH = 0;
		}
		parametricTargetY = oy / dragHeight;
		if(parametricTargetY < 0){
			parametricTargetY = 0;
			offsetV = 0;
		}
		else if(parametricTargetY > 1){
			parametricTargetY = 1;
			offsetV = 0;
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
		winApp.pushMatrix();
		// Move matrix to line up with top-left corner
		winApp.translate(-halfWidth, -halfHeight);
		// Draw buffer
		winApp.imageMode(PApplet.CORNER);
		if(alphaLevel < 255)
			winApp.tint(TINT_FOR_ALPHA, alphaLevel);
		winApp.image(buffer, 0, 0);	
		winApp.popMatrix();
		winApp.popMatrix();

		winApp.popStyle();
	}
	
	protected void updateBuffer(){
		if(bufferInvalid) {
			bufferInvalid = false;
			buffer.beginDraw();

			buffer.ellipseMode(PApplet.CENTER);
			buffer.rectMode(PApplet.CENTER);
			// Back ground colour
			if(opaque == true)
				buffer.background(palette[BACK]);
			else
				buffer.background(buffer.color(255,0));

			buffer.pushMatrix();
			// Draw thumb cursor lines
			float tx = dragD + parametricPosX * dragWidth;
			float ty = dragD + parametricPosY * dragHeight;
			buffer.stroke(palette[TBORDER]);
			buffer.strokeWeight(1);
			buffer.line(0, ty, width, ty);
			buffer.line(tx, 0, tx, height);
			switch(status){
			case OFF_CONTROL:
				buffer.fill(palette[TOFF]);
				break;
			case OVER_CONTROL:
				buffer.fill(palette[TOVER]);
				break;
			case PRESS_CONTROL:
				buffer.fill(palette[TDOWN]);
				break;
			case DRAG_CONTROL:
				buffer.fill(palette[TDRAG]);
				break;
			}
			buffer.rect(tx, ty, THUMB_SIZE, THUMB_SIZE);
			
			// Draw control border
			buffer.rectMode(PApplet.CORNERS);
			buffer.noFill();
			buffer.stroke(palette[LBORDER]);
			buffer.strokeWeight(2);
			buffer.rect(0,0,width-1,height-1);
			buffer.stroke(palette[DBORDER]);
			buffer.strokeWeight(1);
			buffer.rect(1,1,width-1,height-1);
			
			buffer.popMatrix();
			buffer.endDraw();
		}
	}
}
