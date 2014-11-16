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

import automenta.vivisect.gui.HotSpot.HSrect;

import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * This class is only used by the GDropList, GTextField and GTextArea components to provide
 * a scrollbar. 
 * 
 * @author Peter Lager
 *
 */
class GScrollbar extends GAbstractControl {

	private static final int OFF_FILL = 3;
	private static final int OFF_STROKE = 0;
	private static final int OVER_FILL = 1;
	private static final int OVER_STROKE = 3;
	private static final int TRACK = 5;

	protected RoundRectangle2D lowCap, highCap;

	protected float value = 0.2f;
	protected float filler = .5f;
	protected boolean autoHide = true;
	protected boolean currOverThumb = false;
	protected boolean isValueChanging = false;

	protected float last_ox, last_oy;

	/**
	 * Create the scroll bar
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GScrollbar(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
		hotspots = new HotSpot[]{
				new HSrect(1, 0, 0, 16, height),			// low cap
				new HSrect(2, width - 16, 0, 16, height),	// high cap
				new HSrect(9, 17, 0, width - 17, height)	// thumb track
		};

		lowCap = new RoundRectangle2D.Float(1, 1, 15, height-2, 6, 6);
		highCap = new RoundRectangle2D.Float(width - 15, 1, 14.5f, height-2, 6, 6);

		opaque = false;

		z = Z_SLIPPY;
		registeredMethods = DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		G4P.addControl(this);
	}

	/**
	 * If set to true then the scroll bar is only displayed when needed.
	 * 
	 * @param autoHide
	 */
	public void setAutoHide(boolean autoHide){
		if(this.autoHide != autoHide){
			this.autoHide = autoHide;
			if(this.autoHide && filler > 0.99999f)
				visible = false;
			bufferInvalid = true;
		}
	}
	
	/**
	 * Set the position of the thumb. If the value forces the thumb
	 * past the end of the scrollbar, reduce the filler.
	 * 
	 * @param value  must be in the range 0.0 to 1.0
	 */
	public void setValue(float value){
		if(value + filler > 1)
			filler = 1 - value;
		this.value = value;
		if(autoHide && filler > 0.99999f)
			visible = false;
		else
			visible = true;
		bufferInvalid = true;
	}

	/**
	 * Set the value and the thumb size. Force the value to be valid
	 * depending on filler.
	 * @param value must be in the range 0.0 to 1.0
	 * @param filler must be >0 and <= 1
	 */
	public void setValue(float value, float filler){
		if(value + filler > 1)
			value = 1 - filler;
		this.value = value;
		this.filler = filler;
		if(autoHide && this.filler > 0.99999f)
			visible = false;
		else
			visible = true;
		bufferInvalid = true;
	}

	/**
	 * Get the current value of the scrolbar
	 * @return
	 */
	public float getValue(){
		return value;
	}

	/**
	 * All GUI components are registered for mouseEvents
	 */
	public void mouseEvent(MouseEvent event){
		if(!visible  || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());

		int spot = whichHotSpot(ox, oy);

		// If over the track then see if we are over the thumb
		if(spot >= 9){
			if(isOverThumb(ox, oy))
				spot = 10;
			else
				spot = -1; // Over empty track so ignore
		}
		if(spot != currSpot){
			currSpot = spot;
			bufferInvalid = true;
		}

		if(currSpot >= 0 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(focusIsWith != this && currSpot>= 0 && z > focusObjectZ()){
				dragging = false;
				last_ox = ox; last_oy = oy;
				takeFocus();
			}
			break;
		case MouseEvent.CLICK:
			if(focusIsWith == this){
				switch(currSpot){
				case 1:
					value -= 0.1f;
					if(value < 0)
						value = 0;
					bufferInvalid = true;
					fireEvent(this, GEvent.CHANGED);
					break;
				case 2:
					value += 0.1f;
					if(value + filler > 1.0)
						value = 1 - filler;
					bufferInvalid = true;
					fireEvent(this, GEvent.CHANGED);
					break;
				}
				dragging = false;
				loseFocus(parent);
			}
			break;
		case MouseEvent.RELEASE:
			if(focusIsWith == this && dragging){
				loseFocus(parent);
				dragging = false;
				isValueChanging = false;
				bufferInvalid = true;
			}
			break;
		case MouseEvent.DRAG:
			if(focusIsWith == this){
				float movement = ox - last_ox;
				last_ox = ox;
				float deltaV = movement / (width - 32);
				value += deltaV;
				value = PApplet.constrain(value, 0, 1.0f - filler);
				isValueChanging = true;
				bufferInvalid = true;
				dragging = true;
				fireEvent(this, GEvent.CHANGED);
			}
			break;
		}
	}

	protected boolean isOverThumb(float px, float py){
		float p = (px - 16) / (width - 32);
		boolean over =( p >= value && p < value + filler);
		return over;
	}

	protected void updateBuffer(){
		if(bufferInvalid) {
			bufferInvalid = false;
			Graphics2D g2d = buffer.g2;
			buffer.beginDraw();
			if(opaque) {
				buffer.background(buffer.color(255,0));
				buffer.fill(palette[6]);
				buffer.noStroke();
				buffer.rect(8,0,width-16,height);
			}
			else
				buffer.background(buffer.color(255,0));
			// Draw the track
			buffer.fill(palette[TRACK]);
			buffer.noStroke();
			buffer.rect(8,3,width-8,height-5);
			g2d.setStroke(pen_1_0);

			// Draw the low cap
			buffer.strokeWeight(1.2f);
			if(currSpot == 1){
				g2d.setColor(jpalette[OVER_FILL]);
				g2d.fill(lowCap);
				g2d.setColor(jpalette[OVER_STROKE]);
				g2d.draw(lowCap);
			}
			else {
				g2d.setColor(jpalette[OFF_FILL]);
				g2d.fill(lowCap);
				g2d.setColor(jpalette[OFF_STROKE]);
				g2d.draw(lowCap);
			}
			// Draw the high cap
			if(currSpot == 2){
				g2d.setColor(jpalette[OVER_FILL]);
				g2d.fill(highCap);
				g2d.setColor(jpalette[OVER_STROKE]);
				g2d.draw(highCap);
			}
			else {
				g2d.setColor(jpalette[OFF_FILL]);
				g2d.fill(highCap);
				g2d.setColor(jpalette[OFF_STROKE]);
				g2d.draw(highCap);
			}
			// draw thumb
			float thumbWidth = (width - 32) * filler;
			RoundRectangle2D thumb = new RoundRectangle2D.Float(1,1,thumbWidth-1, height-2,6,6);
			buffer.translate((width - 32) * value + 16, 0);
			if(currSpot == 10 || isValueChanging){
				g2d.setColor(jpalette[OVER_FILL]);
				g2d.fill(thumb);
				g2d.setColor(jpalette[OVER_STROKE]);
				g2d.draw(thumb);
			}
			else {
				g2d.setColor(jpalette[OFF_FILL]);
				g2d.fill(thumb);
				g2d.setColor(jpalette[OFF_STROKE]);
				g2d.draw(thumb);
			}
			buffer.endDraw();
		}
	}

	public void draw(){
		if(!visible) return;
		if(bufferInvalid)
			updateBuffer();

		winApp.pushStyle();
		winApp.pushMatrix();

		applyTransform();
		winApp.imageMode(PApplet.CENTER);
		if(alphaLevel < 255)
			winApp.tint(TINT_FOR_ALPHA, alphaLevel);
		winApp.image(buffer, 0, 0);

		winApp.popMatrix();
		winApp.popStyle();
	}	

}
