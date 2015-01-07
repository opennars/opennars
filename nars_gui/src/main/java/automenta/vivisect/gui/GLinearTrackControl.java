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

import java.awt.Graphics2D;
import java.awt.font.TextLayout;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * Base class for linear sliders.
 * 
 * This class provides the ability to control the orientation
 * the text should be displayed. It also enables the use of labels
 * for each tick mark.
 *  
 * @author Peter Lager
 *
 */
public abstract class GLinearTrackControl extends GValueControl {

	static protected float TINSET = 2;
	
	static protected int THUMB_SPOT = 1;
	static protected int TRACK_SPOT = 2;

	protected float trackWidth, trackLength, trackDisplayLength;
	protected float trackOffset;

	protected int textOrientation = ORIENT_TRACK;

	protected int downHotSpot = -1;
	// Mouse over status
	protected int status = OFF_CONTROL;
	
	// For labels
	protected StyledString[] labels;
	protected boolean labelsInvalid = true;

	public GLinearTrackControl(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
	}

	/**
	 * Set the text orientation for the display of the limits and value if appropriate. <br>
	 * Acceptable values are G4P.ORIENT_LEFT, G4P.ORIENT_RIGHT or G4P.ORIENT_TRACK <br>
	 * If an invalid value is passed the ORIENT_TRACK is used.
	 * 
	 * @param orient the orientation of the number labels
	 */
	public void setTextOrientation(int orient){
		switch(orient){
		case ORIENT_LEFT:
		case ORIENT_RIGHT:
		case ORIENT_TRACK:
			textOrientation = orient;
			break;
		default:
			textOrientation = ORIENT_TRACK;
		}
		bufferInvalid = true;
	}
	
	/**
	 * This method is used to set the text to appear alongside the tick marks. <br>
	 * The array passed must have a minimum of 2 elements and each label (element)
	 * must have at least 1 character. If these two conditions are not met then
	 * the call to this method will be ignored and no changes are made.
	 * 
	 * @param tickLabels an array of strings for the labels
	 */
	public void setTickLabels(String[] tickLabels){
		if(tickLabels == null || tickLabels.length < 2)
			return;
		for(String s : tickLabels)
			if(s == null || s.length() == 0)
				return;
		labels = new StyledString[tickLabels.length];
		for(int i = 0; i < tickLabels.length; i++)
			labels[i] = new StyledString(tickLabels[i]);
		stickToTicks = true;
		nbrTicks = labels.length;
		startLimit = 0;
		endLimit = nbrTicks - 1;
		valueType = INTEGER;
		showLimits = false;
		showValue = false;
		bufferInvalid = true;			
	}
	
	/**
	 * Set whether the tick marks are to be displayed or not. Then
	 * recalculate the track offset for the value and limit text.
	 * @param showTicks the showTicks to set
	 */
	public void setShowTicks(boolean showTicks) {
		super.setShowTicks(showTicks);
		float newTrackOffset = calcTrackOffset();
		if(newTrackOffset != trackOffset){
			trackOffset = newTrackOffset;
			bufferInvalid = true;
		}
		bufferInvalid = true;
	}

	/**
	 * Calculates the amount of offset for the labels
	 */
	protected float calcTrackOffset(){
		return (showTicks) ? trackWidth + 2 : trackWidth/2 + 2;
	}
	
	/**
	 * The offset is the distance the value/labels are drawn from the 
	 * centre of the track. <br>
	 * You may wish to tweak this value for visual effect.
	 * @param offset
	 */
	public void setTrackOffset(float offset){
		trackOffset = offset;
	}
	
	/**
	 * Get the visual offset for the value/label text.
	 */
	public float getTrackOffset(){
		return trackOffset;
	}
	
	/**
	 * If we are using labels then this will get the label text
	 * associated with the current value. <br>
	 * If labels have not been set then return null
	 */
	public String getValueS(){
		// Use the valueTarget rather than the valuePos since intermediate values
		// have no meaning in this case.
		int idx = Math.round(startLimit + (endLimit - startLimit) * parametricTarget);
		return (labels == null) ? getNumericDisplayString(getValueF()) : labels[idx].getPlainText();
	}

	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		// Normalise ox and oy to the centre of the slider
		ox -= width/2;
		ox /= trackLength;

		if(currSpot >= 0 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(focusIsWith != this && currSpot > -1 && z >= focusObjectZ()){
				downHotSpot = currSpot;
				status = (downHotSpot == THUMB_SPOT) ? PRESS_CONTROL : OFF_CONTROL;
				offset = ox + 0.5f - parametricPos; // normalised
				takeFocus();
				bufferInvalid = true;
			}
			break;
		case MouseEvent.CLICK:
			if(focusIsWith == this ){
				parametricTarget = ox + 0.5f;
				if(stickToTicks)
					parametricTarget = findNearestTickValueTo(parametricTarget);
				dragging = false;
				status = OFF_CONTROL;
				loseFocus(null);
				bufferInvalid = true;
			}
			break;
		case MouseEvent.RELEASE:
			if(focusIsWith == this && dragging){
				if(downHotSpot == THUMB_SPOT){
					parametricTarget = (ox - offset) + 0.5f;
					if(parametricTarget < 0){
						parametricTarget = 0;
						offset = 0;
					}
					else if(parametricTarget > 1){
						parametricTarget = 1;
						offset = 0;
					}
					if(stickToTicks)
						parametricTarget = findNearestTickValueTo(parametricTarget);
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
					parametricTarget = (ox - offset) + 0.5f;
					if(parametricTarget < 0){
						parametricTarget = 0;
						offset = 0;
					}
					else if(parametricTarget > 1){
						parametricTarget = 1;
						offset = 0;
					}
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

	protected void drawValue(){
		Graphics2D g2d = buffer.g2;
		float px, py;
		TextLayout line;
		ssValue = new StyledString(getNumericDisplayString(getValueF()));
		line = ssValue.getLines(g2d).getFirst().layout;
		float advance = line.getVisibleAdvance();
		switch(textOrientation){
		case ORIENT_LEFT:
			px = (parametricPos - 0.5f) * trackLength + line.getDescent();
			py = -trackOffset;
			buffer.pushMatrix();
			buffer.translate(px, py);
			buffer.rotate(-PI/2);
			line.draw(g2d, 0, 0 );
			buffer.popMatrix();
			break;
		case ORIENT_RIGHT:
			px = (parametricPos - 0.5f) * trackLength - line.getDescent();
			py = - trackOffset - advance;
			buffer.pushMatrix();
			buffer.translate(px, py);
			buffer.rotate(PI/2);
			line.draw(g2d, 0, 0 );
			buffer.popMatrix();
			break;
		case ORIENT_TRACK:
			px = (parametricPos - 0.5f) * trackLength - advance /2;
			if(px < -trackDisplayLength/2)
				px = -trackDisplayLength/2;
			else if(px + advance > trackDisplayLength /2)
				px = trackDisplayLength/2 - advance;
			py = -trackOffset - line.getDescent();
			line.draw(g2d, px, py );
			line = ssEndLimit.getLines(g2d).getFirst().layout;	
			break;
		}
	}
	
	protected void drawLimits(){
		Graphics2D g2d = buffer.g2;
		float px, py;
		TextLayout line;
		if(limitsInvalid){
			ssStartLimit = new StyledString(getNumericDisplayString(startLimit));
			ssEndLimit = new StyledString(getNumericDisplayString(endLimit));
			limitsInvalid = false;
		}
		switch(textOrientation){
		case ORIENT_LEFT:
			line = ssStartLimit.getLines(g2d).getFirst().layout;	
			px = -trackLength/2 + line.getDescent();
			py = trackOffset + line.getVisibleAdvance();
			buffer.pushMatrix();
			buffer.translate(px, py);
			buffer.rotate(-PI/2);
			line.draw(g2d, 0, 0 );
			buffer.popMatrix();
			line = ssEndLimit.getLines(g2d).getFirst().layout;	
			px = trackLength/2  + line.getDescent();
			py = trackOffset + line.getVisibleAdvance();
			buffer.pushMatrix();
			buffer.translate(px, py);
			buffer.rotate(-PI/2);
			line.draw(g2d, 0, 0 );
			buffer.popMatrix();
			break;
		case ORIENT_RIGHT:
			line = ssStartLimit.getLines(g2d).getFirst().layout;	
			px = -trackLength/2 - line.getDescent();
			py = trackOffset;
			buffer.pushMatrix();
			buffer.translate(px, py);
			buffer.rotate(PI/2);
			line.draw(g2d, 0, 0 );
			buffer.popMatrix();
			line = ssEndLimit.getLines(g2d).getFirst().layout;	
			px = trackLength/2  - line.getDescent();
			py = trackOffset;
			buffer.pushMatrix();
			buffer.translate(px, py);
			buffer.rotate(PI/2);
			line.draw(g2d, 0, 0 );
			buffer.popMatrix();
			break;
		case ORIENT_TRACK:
			line = ssStartLimit.getLines(g2d).getFirst().layout;	
			px = -(trackLength + trackWidth)/2;
			py = trackOffset + line.getAscent();
			line.draw(g2d, px, py );
			line = ssEndLimit.getLines(g2d).getFirst().layout;	
			px = (trackLength + trackWidth)/2 - line.getVisibleAdvance();
			py = trackOffset + line.getAscent();
			line.draw(g2d, px, py );
			break;
		}	
	}

	protected void drawLabels(){
		Graphics2D g2d = buffer.g2;
		float px, py;
		TextLayout line;
		if(labelsInvalid){
			ssStartLimit = new StyledString(getNumericDisplayString(startLimit));
			ssEndLimit = new StyledString(getNumericDisplayString(endLimit));
			limitsInvalid = false;
		}
		float deltaX = 1.0f / (nbrTicks - 1);
		switch(textOrientation){
		case ORIENT_LEFT:
			for(int i = 0; i < labels.length; i++){
				line = labels[i].getLines(g2d).getFirst().layout;	
				px = (i * deltaX - 0.5f)*trackLength + line.getDescent();
				py = trackOffset + line.getVisibleAdvance();
				buffer.pushMatrix();
				buffer.translate(px, py);
				buffer.rotate(-PI/2);
				line.draw(g2d, 0, 0 );
				buffer.popMatrix();
			}
			break;
		case ORIENT_RIGHT:
			for(int i = 0; i < labels.length; i++){
				line = labels[i].getLines(g2d).getFirst().layout;	
				px = (i * deltaX - 0.5f)*trackLength - line.getDescent();
				py = trackOffset;
				buffer.pushMatrix();
				buffer.translate(px, py);
				buffer.rotate(PI/2);
				line.draw(g2d, 0, 0 );
				buffer.popMatrix();
			}
			break;
		case ORIENT_TRACK:
			for(int i = 0; i < labels.length; i++){
				line = labels[i].getLines(g2d).getFirst().layout;	
				px = (i * deltaX - 0.5f)*trackLength - 0.5f * line.getVisibleAdvance();
				py = trackOffset + line.getAscent();
				line.draw(g2d, px, py );
			}
			break;
		}	
	}
	
}
