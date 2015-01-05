/*
  Part of the G4P library for Processing 
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

import processing.core.PApplet;

/**
 * Base class for controls that have 2 variables e.f. GSlider2D
 * 
 * @author Peter Lager
 *
 */
public abstract class GValueControl2D extends GControl {

	static protected int THUMB_SPOT = 1;
	static protected int TRACK_SPOT = 2;

	
	protected float parametricPosX = 0.5f, parametricTargetX = 0.5f;
	protected float parametricPosY = 0.5f, parametricTargetY = 0.5f;
	
	protected float easing  = 1.0f; // must be >= 1.0

	// Offset to between mouse and thumb centre
	protected float offsetH, offsetV;
	
	protected int valueType = DECIMAL;
	protected int precision = 2;

	public GValueControl2D(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
	}

	public void pre(){
		if(Math.abs(parametricTargetX - parametricPosX) > epsilon || Math.abs(parametricTargetY - parametricPosY) > epsilon){
			parametricPosX += (parametricTargetX - parametricPosX) / easing;
			parametricPosY += (parametricTargetY - parametricPosY) / easing;
			updateDueToValueChanging();
			bufferInvalid = true;
			if(Math.abs(parametricTargetX - parametricPosX) > epsilon || Math.abs(parametricTargetY - parametricPosY) > epsilon){
				fireEvent(this, GEvent.VALUE_CHANGING);
			}
			else {
				parametricPosX = parametricTargetX;
				parametricPosY = parametricTargetY;
				fireEvent(this, GEvent.VALUE_STEADY);
			}
		}
	}
	
	/**
	 * This should be overridden in child classes so they can perform any class specific
	 * actions when the value changes.
	 * Override this in GSlider to change the hotshot poaition.
	 */
	protected void updateDueToValueChanging(){
	}

	/**
	 * Make epsilon to match the value of 1 pixel or the precision which ever is the smaller
	 */
	protected void setEpsilon(){
		epsilon = (float) Math.min(0.001, Math.pow(10, -precision));
	}

}
