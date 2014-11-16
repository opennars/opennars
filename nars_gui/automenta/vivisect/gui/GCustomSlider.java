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
import automenta.vivisect.gui.HotSpot.HSrect;

import java.awt.Graphics2D;
import java.io.File;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Slider that can be customised with user provided graphics. <br>
 * 
 * This class replaces the GWSlider provided in pre v3 editions of this library.
 * <p>
 * The main difference to the GSlider class is the ability to skin the slider with user provided graphics.
 * The library provides a number of skins ready for use. You specify the skin to use when the slider is created 
 * and if the library is unable to load the skin it will print a warning and load the default skin instead. </p>
 * <p>Library skins available</p>
 * <ul>
 * <li>grey_blue (default skin)</li>
 * <li>green_red20px</li>
 * <li>red_yellow18px</li>
 * <li>blue18px</li>
 * <li>purple18px</li>
 * </ul
 * <p>
 * A skin requires 5 image files for different parts of the slider which must be stored in their own 
 * folder (the folder name is also used as the skin name) and this folder should be place inside the 
 * sketch's data folder.</p>
 * <p>The image files have specific names. 
 * <ul>
 * <li>Left end cap of the slider(<b>end_left.???</b>)</li>
 * <li>Right end cap of the slider(<b>end_right.???</b>)</li>
 * <li>An extendible centre segment(<b>centre.???</b>)</li>
 * <li>Draggable thumb (<b>handle.???</b> and <b>handle_mouseover.???</b>)</li>
 * </ul>
 * Where ??? is the image type file extension. The image type can be any that Processing can handle, the
 * most common types will be png, jpg or gif but tga is also permitted</p>
 * 
 * <p>There are very few restrictions about the images you use but when designing the images you should consider
 * the following facts:</p>
 * <ul>
 * <li>the slider will be created to fit the control size (specified in the constructor)</li>
 * <li>the horizontal space allocated for the end-caps will be the same for each end (uses the width or the larger end cap image)</li>
 * <li>the track width will be the height of the centre image</li>
 * <li>the centre image will be tiled along the track length</li>
 * <li>the track will be placed in the horizontal and vertical centre of the control.</li>
 * <li>the end cap images will be placed in the vertical centre of the control and butted against the track.</li>
 * </ul>
 * 
 * 
 * @author Peter Lager
 *
 */
public class GCustomSlider extends GLinearTrackControl {

	protected PImage leftEnd;
	protected PImage thumb;
	protected PImage thumb_mouseover;
	protected PImage rightEnd;
	protected PImage centre;

	/**
	 * Create a custom slider using the default skin.
	 * 
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GCustomSlider(PApplet theApplet, float p0, float p1, float p2, float p3) {
		this(theApplet, p0, p1, p2, p3, null);
	}

	/**
	 * Create a custom slider using the skin specified.
	 * 
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param skin the name of the skin (this is also the name of the folder holding the images)
	 */
	public GCustomSlider(PApplet theApplet, float p0, float p1, float p2, float p3, String skin) {
		super(theApplet, p0, p1, p2, p3);
		skin = (skin == null) ? "grey_blue" : skin.trim();
		setStyle(skin);
		
		// Customise buffer for this control
		buffer.g2.setFont(G4P.numericLabelFont);
		buffer.imageMode(PApplet.CENTER);
		
		hotspots = new HotSpot[]{
				new HSalpha(THUMB_SPOT, width/2 + (parametricPos - 0.5f) * trackLength, height/2, thumb, PApplet.CENTER),  // thumb
				new HSrect(TRACK_SPOT, (width-trackLength)/2, (height-trackWidth)/2, trackLength, trackWidth),		// track
		};
		opaque = false;
		z = Z_SLIPPY;

		epsilon = 0.98f / trackLength;
		ssStartLimit = new StyledString("0.00");
		ssEndLimit = new StyledString("1.00");
		ssValue = new StyledString("0.50");

		// Now register control with applet
		createEventHandler(G4P.sketchApplet, "handleSliderEvents", 
				new Class<?>[]{ GValueControl.class, GEvent.class },
				new String[]{ "slider", "event" }
		);
		registeredMethods = PRE_METHOD | DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		G4P.addControl(this);
	}

	/**
	 * Change the skin used for the slider.
	 * @param skin the name of the folder holding the graphics for this slider
	 */
	public void setStyle(String skin){
		loadSkin(skin);
		float maxEndLength = Math.max(leftEnd.width, rightEnd.width);
		maxEndLength = Math.max(maxEndLength, 10); // make sure we have enough to show limits value
		trackLength = Math.round(width - 2 * maxEndLength - TINSET);
		trackDisplayLength = trackLength + 2 * Math.min(leftEnd.width, rightEnd.width);
		trackWidth = centre.height;
		trackOffset = calcTrackOffset();
		extendCentreImage();
		bufferInvalid = true;
	}

	/**
	 * Calculates the amount of offset for the labels
	 */
	protected float calcTrackOffset(){
		float adjustedTrackOffset = (showTicks) ? trackWidth: trackWidth/2;
		adjustedTrackOffset = Math.max(adjustedTrackOffset, thumb.height/2) + 2;
		if(adjustedTrackOffset != trackOffset){
			bufferInvalid = true;
		}
		return adjustedTrackOffset;
	}


	protected void updateDueToValueChanging(){
		hotspots[0].x = (width/2  + (parametricPos - 0.5f) * trackLength);
	}

	protected void updateBuffer(){
		if(bufferInvalid) {
			Graphics2D g2d = buffer.g2;
			bufferInvalid = false;
			buffer.beginDraw();

			// Back ground colour
			buffer.background(opaque ? palette[6] : palette[2] & 0xFFFFFF);
			// Draw track, thumb, ticks etc.
			buffer.pushMatrix();
			buffer.translate(width/2, height/2);
			// draw ticks
			if(showTicks){
				float delta = 1.0f / (nbrTicks - 1);
				for(int i = 0; i < nbrTicks; i++){
					float tickx = Math.round((i * delta - 0.5f) * trackLength);
					buffer.strokeWeight(2);
					buffer.stroke(255);
					buffer.line(tickx+1, -trackWidth, tickx+1, trackWidth);
					buffer.strokeWeight(1.0f);
					buffer.stroke(0);
					buffer.line(tickx, -trackWidth, tickx, trackWidth);
				}
			}
			buffer.image(centre,0,0);
			buffer.image(leftEnd, -(trackLength + leftEnd.width)/2, 0);
			buffer.image(rightEnd, (trackLength + rightEnd.width)/2, 0);
			switch(status){
			case OFF_CONTROL:
				buffer.image(thumb,(parametricPos - 0.5f) * trackLength, 0);
				break;
			case OVER_CONTROL:
			case PRESS_CONTROL:
			case DRAG_CONTROL:
				buffer.image(thumb_mouseover,(parametricPos - 0.5f) * trackLength, 0);
				break;
			}		
			// Display slider values
			g2d.setColor(jpalette[2]);
			if(labels != null){
				drawLabels();
			}
			else {

				if(showLimits)
					drawLimits();
				if(showValue)
					drawValue();
			}
			buffer.popMatrix();
			buffer.endDraw();
		}
	}

	private void extendCentreImage(){
		int tl = (int)trackLength;
		PGraphics pg = winApp.createGraphics(tl, centre.height, JAVA2D);
		int rem = tl % centre.width;
		int n = tl / centre.width;
		n = (rem == 0) ? n : n + 1;
		int px = (tl - centre.width * n)/2;
		pg.beginDraw();
		pg.background(winApp.color(255,0));
		pg.imageMode(CORNER);

		while(px < tl){
			pg.image(centre, px, 0);
			px += centre.width;
		}

		pg.endDraw();
		centre = pg;
	}

	/**
	 * Load a skin
	 * @param style
	 */
	private void loadSkin(String style){
		// Remember the skin we want to use
		String style_used = style;
		boolean found = false;
		// First check for user defined skin
		// See if we are running in a browser or running locally
		if(winApp.sketchPath("").length() == 0)
			found = loadSkin_AppletInBrowser(style);	// browser
		else
			found = loadStyle_FromSketch(style);		// local
		// If not found load it from the library
		if(!found)
			style_used = loadStyle_FromG4P(style);
		// See if we have had to use a different skin. If true then 
		// the original skin could not be found so say so 
		if(!style.equalsIgnoreCase(style_used))
			System.out.println("Unable to load the skin " + style + " using default '" + DEFAULT_SLIDER_STYLE + "' style instead");
	}

	/**
	 * If no user defined skin has been specified then load a style from the G4P library.
	 * If the style does not exist it will use the default style.
	 * @param style
	 */
	private String loadStyle_FromG4P(String style) {
		boolean found = (SLIDER_STYLES.indexOf("|"+style+"|") >= 0);
		// If not found use the default grey_blue
		if(!found)
			style = DEFAULT_SLIDER_STYLE;

		// All the library styles use png graphics
		leftEnd = winApp.loadImage(style + "/end_left.png");
		rightEnd = winApp.loadImage(style + "/end_right.png");
		thumb = winApp.loadImage(style +"/handle.png");
		thumb_mouseover = winApp.loadImage(style +"/handle_mouseover.png");
		//	will be stretched before use
		centre = winApp.loadImage(style + "/centre.png");

		return style;	
	}

	/**
	 * Load a skin when run as an application or run locally.
	 * 
	 * @param styleFolder
	 * @param style
	 * @return true if the style loaded successfully
	 */
	private boolean loadStyle_FromSketch(String style) {
		// First attempt to locate the style inside the sketch or sketch data folders
		File styleFolder = new File(winApp.dataPath(style));
		if(!styleFolder.exists())
			styleFolder = new File(winApp.sketchPath(style));
		// If the style is in the sketch then attempt to load the style
		// and if successful we are done
		if(!styleFolder.exists())
			return false;

		int fcount = 0;
		String[] names = new String[] { "centre.", "end_left.", "end_right.", "handle.", "handle_mouseover." };
		PImage[] images = new PImage[names.length];
		File[] fileList = styleFolder.listFiles();
		for(int i = 0; i < names.length; i++){
			for(File f : fileList){
				String filename = f.getName();
				if(filename.startsWith(names[i])){
					images[i] = winApp.loadImage(style + "/" + filename);
					fcount ++;
				}
			}
		}
		if(fcount != names.length)
			return false;

		centre = images[0];
		leftEnd = images[1];
		rightEnd = images[2];
		thumb = images[3];
		thumb_mouseover = images[4];
		return true;		
	}

	/**
	 * Load a skin when run as an applet inside a browser.
	 * 
	 * Note: sketchPath() is null when inside a browswer.
	 * 
	 * @param style
	 * @return true if the style loaded successfully
	 */
	private boolean loadSkin_AppletInBrowser(String style){
		leftEnd = winApp.loadImage(style + "/end_left.png");
		if(leftEnd == null)
			leftEnd = winApp.loadImage(style + "/end_left.jpg");
		rightEnd = winApp.loadImage(style + "/end_right.png");
		if(rightEnd == null)
			rightEnd = winApp.loadImage(style + "/end_right.jpg");
		thumb = winApp.loadImage(style +"/handle.png");
		if(thumb == null)
			thumb = winApp.loadImage(style +"/handle.jpg");
		thumb_mouseover = winApp.loadImage(style +"/handle_mouseover.png");
		if(thumb_mouseover == null)
			thumb_mouseover = winApp.loadImage(style +"/handle_mouseover.jpg");
		//	will be stretched before use
		centre = winApp.loadImage(style + "/centre.png");
		if(centre == null)
			centre = winApp.loadImage(style + "/centre.jpg");

		boolean found = !(leftEnd == null || rightEnd == null || thumb == null || thumb_mouseover == null || centre == null);

		// See if we have problems with the skin files
		if(!found){
			System.out.println("Unable to load the skin " + style + " check the ");
			System.out.println("skin name used and ensure all the image files are present.");
			System.out.println("Reverting to default 'grey_blue' style");
			loadSkin("grey_blue");
		}
		return found;
	}



}
