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

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Defines a number of color schemes for the GUI components. <br>
 * 
 * It loads an image file with all the colors used by the various colour schemes. <br>
 * It will search for a file for a user defined scheme (user_gui_palette.png) and
 * if it can't find it it will use the library default scheme (default_gui_palette.png).
 * 
 * @author Peter Lager
 *
 */
public class GCScheme implements GConstants {

	private static int[][] palettes = null;
	private static Color[][] jpalettes = null;

	/**
	 * Set the color scheme to one of the preset schemes
	 * BLUE / GREEN / RED /  PURPLE / YELLOW / CYAN / BROWN
	 * or if you have created your own schemes following the instructions
	 * at gui4processing.lagers.org.uk/colorscheme.html then you can enter
	 * the appropriate numeric value of the scheme.
	 * 
	 * @param schemeNo
	 * @return the color scheme based on the scheme number
	 */
	public static int[] getColor(int schemeNo){
		schemeNo = Math.abs(schemeNo) % 16;
		return palettes[schemeNo];
	}

	/**
	 * Set the color scheme to one of the preset schemes
	 * BLUE / GREEN / RED /  PURPLE / YELLOW / CYAN / BROWN
	 * or if you have created your own schemes following the instructions
	 * at gui4processing.lagers.org.uk/colorscheme.html then you can enter
	 * the appropriate numeric value of the scheme.
	 * 
	 * @param schemeNo
	 * @return the color scheme based on the scheme number
	 */
	public static Color[] getJavaColor(int schemeNo){
		schemeNo = Math.abs(schemeNo) % 16;
		return jpalettes[schemeNo];
	}

	/**
	 * Called every time we create a control. The palettes will be made when 
	 * the first control is created.
	 * 
	 * @param app
	 */
	public static void makeColorSchemes(PApplet app) {
		// If the palettes have not been created then create them
		// otherwise do nothing
		if(palettes != null)
			return;
		// Load the image
		PImage image = null;
                
		/*InputStream is = app.createInput("user_gui_palette.png");
		if(is != null){
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			image = app.loadImage("user_gui_palette.png");
			GMessenger.message(USER_COL_SCHEME, null);
		}
		else*/ {
			// User image not provided
			image = app.loadImage("data/default_gui_palette.png");
			// Added to 3.4 to hopefully fix problem with OpenProcessing
			if(image == null)
				image = new PImage((new javax.swing.ImageIcon(new GCScheme().getClass().getResource("/data/default_gui_palette.png"))).getImage());
		}
		// Now make the palletes
		palettes = new int[16][16];
		jpalettes = new Color[16][16];
		for(int p = 0; p < 16; p++)
			for(int c = 0; c < 16; c++){
				int col =  image.get(c * 16 + 8, p * 16 + 8);
				palettes[p][c] = col;
				jpalettes[p][c] = new Color((col >> 16) & 0xff, (col >> 8) & 0xff, col & 0xff);
			}
	}

	/**
	 * This method is called by the G4P GUI Builder tool when there is no
	 * sketch = no PApplet object to use
	 */
	public static void makeColorSchemes() {
		// If the palettes have not been created then create them
		// otherwise do nothing
		if(palettes != null)
			return;
		// Load the image
		PImage image = new PImage((new javax.swing.ImageIcon(new GCScheme().getClass().getResource("/data/default_gui_palette.png"))).getImage());
		// Now make the palletes
		palettes = new int[16][16];
		jpalettes = new Color[16][16];
		for(int p = 0; p < 16; p++)
			for(int c = 0; c < 16; c++){
				int col =  image.get(c * 16 + 8, p * 16 + 8);
				palettes[p][c] = col;
				jpalettes[p][c] = new Color((col >> 16) & 0xff, (col >> 8) & 0xff, col & 0xff);
			}
	}

}
