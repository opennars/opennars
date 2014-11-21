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

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Display area for user generated graphics. <br>
 * 
 * This control will display a PGraphics object created and updated by the user.
 * If the size of the users graphic is different from the control the output will be 
 * rescaled to fit the control size irrespective of the aspect ratio.
 * 
 * @author Peter Lager
 *
 */
public class GSketchPad extends GControl {

	// Scale graphic should be set to true if the grpahics object
	// and this sketch pad object are of different sizes.
	protected boolean scaleGraphic = false;

	protected PGraphics pad = null;

	public GSketchPad(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
		cursorOver = GUI.mouseOff; // does not change
		registeredMethods = DRAW_METHOD;
		GUI.addControl(this);
	}
	
	public void setGraphic(PGraphics pg){
		if(pg == null)
			return;
		pad = pg;
		scaleGraphic = (int)width != pg.width || (int)height != pg.height;
	}
	
	public void draw(){
		if(!visible) return;

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
		if(pad != null){
			try {
				if(scaleGraphic)
					winApp.image(pad, 0, 0, width, height);
				else
					winApp.image(pad, 0, 0);
//				System.out.println("Graphic updated with alpha " + alphaLevel);
			}
			catch(Exception excp){ /* Do nothing */	}
		}
//		winApp.noFill();
//		winApp.stroke(palette[3]);
//		winApp.strokeWeight(1.5f);
//		winApp.rect(0, 0, width, height);
		winApp.popMatrix();	
		winApp.popStyle();
	}

}
