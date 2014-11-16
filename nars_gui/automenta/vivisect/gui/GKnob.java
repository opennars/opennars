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

import automenta.vivisect.gui.HotSpot.HSarc;
import automenta.vivisect.gui.HotSpot.HScircle;
import processing.core.PApplet;
import processing.event.MouseEvent;


/**
 * The provides an extremely configurable GUI knob controller. GKnob
 * inherits from GValueControl so you should read the documentation 
 * for that class as it also applies to GKnob. <br><br>
 * 
 * Configurable options <br>
 *  Knob size but it must be circular <br>
 *  Start and end of rotation arc. <br>
 *  Bezel width with tick marks <br>
 *  User defined value limits (i.e. the range of values returned <br>
 *  <br>
 *  Range of values associated with rotating the knob <br>
 *  Rotation is controlled by mouse movement -  3 modes available <br>
 *  (a) angular -  drag round knob center <br>
 *  (b) horizontal - drag left or right <br>
 *  (c) vertical - drag up or down <br>
 *  User can specify mouse sensitivity for modes (b) and (c)
 *  Use can specify easing to give smoother rotation
 *  
 * 	<b>Note</b>: Angles are measured clockwise starting in the positive x direction i.e.
 * <pre>
 *         270
 *          |
 *    180 --+-- 0
 *          |
 *          90
 * </pre>
 * 
 * @author Peter Lager
 *
 */
public class GKnob extends GValueControl {


	protected float startAng = 110, endAng = 70;

	protected int mode = CTRL_HORIZONTAL;

	protected boolean showTrack = true;

	protected float bezelRadius, bezelWidth, gripRadius;
	protected boolean overIncludesBezel = true;
	protected float sensitivity = 1.0f;

	protected boolean drawArcOnly = false;
	protected boolean mouseOverArcOnly = false;

	protected float startMouseX, startMouseY;
	protected float lastMouseAngle, mouseAngle;

	// corresponds to target and current values
	//				parametricTarget 
	protected float angleTarget, lastAngleTarget;

	/**
	 * Will create the a circular knob control that fits the rectangle define by
	 * the values passed as parameters. <br>
	 * The knob has two zones the outer bezel and the inner gripper. The radius of 
	 * the outer bezel is calculated from <br>
	 * <pre>bezel radius = min(width, height)/2 - 2<pre><br>
	 * The radius of the inner griper radius is calculated from the bezel radius 
	 * and the last parameter. <br>
	 * <pre>grip radius = bezel radiius * gripAmount </pre><br>
	 * The gripAmount should be in te range 0.0 to 1.0 inclusive. The actual value 
	 * will be constrained to that range. <br>
	 *  
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param gripAmount must be >=0.0 and <=1.0
	 */
	public GKnob(PApplet theApplet, float p0, float p1, float p2, float p3, float gripAmount) {
		super(theApplet, p0, p1, p2, p3);
		bezelRadius = Math.min(width, height) / 2 - 2;
		setGripAmount(gripAmount);
		setTurnRange(startAng, endAng);
		// valuePos and valueTarget will start at 0.5;
		lastAngleTarget = angleTarget = scaleValueToAngle(parametricTarget);
		hotspots = new HotSpot[]{
				new HScircle(1, width/2, height/2, gripRadius)
		};
		z = Z_SLIPPY;

		epsilon = 0.98f / (endAng - startAng);
		showTicks = true;
		
		// Now register control with applet
		createEventHandler(G4P.sketchApplet, "handleKnobEvents",
				new Class<?>[]{ GValueControl.class, GEvent.class }, 
				new String[]{ "knob", "event" } 
		);
		registeredMethods = PRE_METHOD | DRAW_METHOD | MOUSE_METHOD ;
		cursorOver = HAND;
		G4P.addControl(this);
	}

	/**
	 * The radius of the inner griper radius is calculated from the bezel radius 
	 * and the parameter gripAmount using <br>
	 * <pre>grip radius = bezel radiius * gripAmount </pre><br>
	 * The gripAmount should be in te range 0.0 to 1.0 inclusive. The actual value 
	 * will be constrained to that range. <br>
	 * 
	 * @param gripAmount must be >=0.0 and <=1.0
	 */
	public void setGripAmount(float gripAmount){
		gripAmount = PApplet.constrain(gripAmount, 0.0f, 1.0f);
		gripRadius = bezelRadius * gripAmount;
		if(gripRadius < 2.0f) gripRadius = 0.0f;
		bezelWidth = bezelRadius - gripRadius;
		bufferInvalid = true;
	}
	
	protected void calculateHotSpot(){
		float overRad = (this.overIncludesBezel) ? bezelRadius : gripRadius;
		if(mouseOverArcOnly)				
			hotspots[0] = new HSarc(1, width/2 , height/2, overRad, startAng, endAng);  // over grip
		else
			hotspots[0] = new HScircle(1, width/2, height/2, overRad);
	}
	
	/**
	 * For a particular normalised value calculate the angle (degrees)
	 * 
	 * @param v
	 * @return the needle angle for the given value
	 */
	protected float scaleValueToAngle(float v){
		float a = startAng + v * (endAng - startAng);
		return a;		
	}

	/**
	 * Calculates the knob angle based on the normalised value.
	 * 
	 * @param a
	 */
	protected float calcAngletoValue(float a){
		if(a < startAng)
			a += 360;
		float v = (a - startAng) / (endAng - startAng);
		return v;
	}

	/**
	 * Set the value for the slider. <br>
	 * The user must ensure that the value is valid for the slider range.
	 * @param v
	 */
	public void setValue(float v){
		super.setValue(v);
		angleTarget = scaleValueToAngle(parametricTarget);
	}

	/**
	 * Whether or not to show the circular progress bar.
	 * @param showTrack true for visible
	 */
	public void setShowTrack(boolean showTrack){
		if(this.showTrack != showTrack){
			this.showTrack = showTrack;
			bufferInvalid = true;
		}
	}

	/**
	 * Are we showing the the value track bar.
	 */
	public boolean isShowTrack(){
		return showTrack;
	}
	
	/**
	 * Whether to include the bezel when deciding when the mouse is over.
	 * @param overBezel true if bezel inclded.
	 */
	public void setIncludeOverBezel(boolean overBezel){
		overIncludesBezel = overBezel;
		calculateHotSpot();
	}
	
	/**
	 * Is the bezel included when considering when the mouse is over.
	 * @return true if included.
	 */
	public boolean isIncludeOverBezel(){
		return overIncludesBezel;
	}
	
	/**
	 * Decides when the knob will respond to the mouse buttons. If set to true
	 * it will only respond when ver the arc made by the start and end angles. If
	 * false it will be the full circle.
	 * @param arcOnly
	 */
	public void setOverArcOnly(boolean arcOnly){
		mouseOverArcOnly = arcOnly;
		calculateHotSpot();
	}

	/**
	 * Does the mouse only respond when over the arc?
	 * @return true = yes
	 */
	public boolean isOverArcOnly(){
		return mouseOverArcOnly;
	}
	
	/**
	 * Convenience method to set both the show and the mouse over arc only properties
	 * for this knob
	 * @param over_arc_only mouse over arc only?
	 * @param draw_arc_only draw arc only?
	 * @param overfullsize include bezel in mouse over calculations?
	 */
	public void setArcPolicy(boolean over_arc_only, boolean draw_arc_only, boolean overfullsize){
		this.mouseOverArcOnly = over_arc_only;
		setShowArcOnly(draw_arc_only);
		overIncludesBezel = overfullsize;
		calculateHotSpot();
	}
	
	/**
	 * This will decide whether the knob is draw as a full circle or as an arc.
	 * 
	 * @param arcOnly true for arc only
	 */
	public void setShowArcOnly(boolean arcOnly){
		if(drawArcOnly != arcOnly){
			drawArcOnly = arcOnly;
			bufferInvalid = true;
		}
	}
	
	/**
	 * Are we showing arc only?
	 * @return true = yes
	 */
	public boolean isShowArcOnly(){
		return drawArcOnly;
	}
	
	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		// Normalise ox and oy to the centre of the knob
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
				startMouseX = ox;
				startMouseY = oy;
				lastMouseAngle = mouseAngle = getAngleFromUser(ox, oy);
				offset = scaleValueToAngle(parametricTarget) - mouseAngle;
				takeFocus();
			}
			break;
		case MouseEvent.RELEASE:
			if(focusIsWith == this){
				loseFocus(null);
			}
			// Correct for sticky ticks if needed
			if(stickToTicks)
				parametricTarget = findNearestTickValueTo(parametricTarget);
			dragging = false;
			break;
		case MouseEvent.DRAG:
			if(focusIsWith == this){
				mouseAngle = getAngleFromUser(ox, oy);
				if(mouseAngle != lastMouseAngle){
					float deltaMangle = mouseAngle - lastMouseAngle;
					// correct when we go over zero degree position
					if(deltaMangle < -180)
						deltaMangle += 360;
					else if(deltaMangle > 180)
						deltaMangle -= 360;
					// Calculate and adjust new needle angle so it is in the range aLow >>> aHigh
					angleTarget = constrainToTurnRange(angleTarget + deltaMangle);
					parametricTarget = calcAngletoValue(angleTarget);
					// Update offset for use with angular mouse control
					offset += (angleTarget - lastAngleTarget - deltaMangle);
					// Remember target needle and mouse angles
					lastAngleTarget = angleTarget;
					lastMouseAngle = mouseAngle;
				}
			}
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
                
		winApp.pushMatrix();
		// Move matrix to line up with top-left corner
		winApp.translate(-halfWidth, -halfHeight);
		// Draw buffer
		winApp.imageMode(PApplet.CORNER);
		if(alphaLevel < 255)
			winApp.tint(TINT_FOR_ALPHA, alphaLevel);
		winApp.image(buffer, 0, 0);	
		winApp.popMatrix();
		// Value labels
		if(children != null){
			for(GAbstractControl c : children)
				c.draw();
		}
		winApp.popMatrix();

		winApp.popStyle();
	}

	protected void updateBuffer(){
		double a, sina, cosa;
		float tickLength;
		if(bufferInvalid) {
			bufferInvalid = false;
			buffer.beginDraw();
			buffer.ellipseMode(PApplet.CENTER);
			// Back ground colour
			// Back ground colour
			if(opaque == true)
				buffer.background(palette[6]);
			else
				buffer.background(buffer.color(255,0));
			buffer.translate(width/2, height/2);
			buffer.noStroke();
			float anglePos = scaleValueToAngle(parametricPos);
			if(bezelWidth > 0){
				// Draw bezel, track,  ticks etc
				buffer.noStroke();
				buffer.fill(palette[5]);
				if(drawArcOnly)
					buffer.arc(0,0,2*bezelRadius, 2*bezelRadius, PApplet.radians(startAng), PApplet.radians(endAng));
				else
					buffer.ellipse(0,0,2*bezelRadius, 2*bezelRadius);
				// Since we have a bezel test for ticks
				if(showTicks){
					buffer.noFill();
					buffer.strokeWeight(1.6f);
					buffer.stroke(palette[3]);
					float deltaA = (endAng - startAng)/(nbrTicks - 1);
					for(int t = 0; t < nbrTicks; t++){
						tickLength = gripRadius + ((t == 0 || t == nbrTicks - 1) ? bezelWidth : bezelWidth * 0.8f); 
						a =  Math.toRadians(startAng + t * deltaA);
						sina = Math.sin(a);
						cosa = Math.cos(a);
						buffer.line((float)(gripRadius * cosa), (float)(gripRadius * sina), (float)(tickLength * cosa), (float)(tickLength * sina));
					}
				}
				// draw track?
				if(showTrack){
					buffer.noStroke();
					buffer.fill(palette[14]);
					buffer.arc(0,0, 2*(gripRadius + bezelWidth * 0.5f), 2*(gripRadius + bezelWidth * 0.5f), PApplet.radians(startAng), PApplet.radians(anglePos));					
				}
			}

			// draw grip (inner) part of knob
			buffer.strokeWeight(1.6f);
			buffer.stroke(palette[2]); // was 14
			buffer.fill(palette[2]);
			if(drawArcOnly)
				buffer.arc(0,0,2*gripRadius, 2*gripRadius, PApplet.radians(startAng), PApplet.radians(endAng));
			else
				buffer.ellipse(0,0,2*gripRadius, 2*gripRadius);

			// Draw needle
			buffer.noFill();
			buffer.stroke(palette[14]);
			buffer.strokeWeight(3);
			a = Math.toRadians(anglePos);
			sina = Math.sin(a);
			cosa = Math.cos(a);
			buffer.line(0, 0, (float)(gripRadius * cosa), (float)(gripRadius * sina));
			buffer.endDraw();
		}
	}

	
	/**
	 * Get the current mouse controller mode possible values are <br>
	 * GKnob.CTRL_ANGULAR or GKnob.CTRL_HORIZONTAL) orGKnob.CTRL_VERTICAL
	 * @return the mode
	 */
	public int getTurnMode() {
		return mode;
	}

	/**
	 * Set the mouse control mode to use, acceptable values are <br>
	 * GKnob.CTRL_ANGULAR or GKnob.CTRL_HORIZONTAL) orGKnob.CTRL_VERTICAL any
	 * other value will be ignored.
	 * @param mode the mode to set
	 */
	public void setTurnMode(int mode) {
		switch(mode){
		case CTRL_ANGULAR:
		case CTRL_HORIZONTAL:
		case CTRL_VERTICAL:
			this.mode = mode;			
		}
	}

	/**
	 * This gets the sensitivity to be used in modes CTRL_HORIZONTAL and CTRL_VERTICAL
	 * @return the sensitivity
	 */
	public float getSensitivity() {
		return sensitivity;
	}

	/**
	 * This gets the sensitivity to be used in modes CTRL_HORIZONTAL and CTRL_VERTICAL <br>
	 * A value of 1 is 1 degree per pixel and a value of 2 is 2 degrees per pixel. <br>
	 * @param sensitivity the sensitivity to set
	 */
	public void setSensitivity(float sensitivity) {
		this.sensitivity = (sensitivity < 0.1f) ? 0.1f : sensitivity;
	}

	/**
	 * Calculates the 'angle' from the current mouse position based on the type
	 * of 'controller' set.
	 * @param px the distance from the knob centre in the x direction
	 * @param py the distance from the knob centre in the y direction
	 * @return the unconstrained angle
	 */
	protected float getAngleFromUser(float px, float py){
		float degs = 0;
		switch(mode){
		case CTRL_ANGULAR:
			degs = calcRealAngleFromXY(ox, oy);
			break;
		case CTRL_HORIZONTAL:
			degs = sensitivity * (px - startMouseX);
			break;
		case CTRL_VERTICAL:
			degs = sensitivity * (py - startMouseY);
			break;
		}
		return degs;
	}

	/**
	 * Set the limits for the range of valid rotation angles for the knob.
	 * 
	 * @param start the range start angle in degrees
	 * @param end the range end angle in degrees
	 */
	public void setTurnRange(float start, float end){
		start = constrain360(start);
		end = constrain360(end);
		startAng = start;
		endAng = (startAng >= end) ? end + 360 : end;
		setValue(getValueF());
//		anglePos = angleTarget;
		bufferInvalid = true;
	}

	/**
	 * Determines whether an angle is within the knob
	 * rotation range.
	 * @param a the angle in degrees
	 * @return true is angle is within rotation range else false
	 */
	protected boolean isInTurnRange(float a){
		a = constrain360(a);
		if(a < startAng)
			a += 360;
		return (a >= startAng && a <= endAng);
	}

	/**
	 * Accept an angle and constrain it to the knob angle range.
	 * 
	 * @param a
	 * @return the constrained angle
	 */
	protected float constrainToTurnRange(float a){
		if(a < startAng)
			a = startAng;
		else if(a > endAng)
			a = endAng;
		return a;
	}
	
	/**
	 * Accept an angle and constrain it to the range 0-360
	 * @param a
	 * @return the constrained angle
	 */
	protected float constrain360(float a){
		while(a < 0)
			a += 360;
		while(a > 360)
			a -= 360;
		return a;
	}

	/**
	 * Calculate the angle to the knob centre making sure it is in
	 * the range 0-360
	 * @param px
	 * @param py
	 * @return the angle from the knob centre to the specified point.
	 */
	protected float calcRealAngleFromXY(float px, float py){
		float a = (float) Math.toDegrees(Math.atan2(py, px));
		if(a < 0)
			a += 360;
		return a;	
	}
}
