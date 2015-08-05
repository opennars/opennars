/* Glyph.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Represents data for individual glyphs, used for drawing text
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 *          
 */
package automenta.rdp;

// import java.awt.*;
// import java.awt.image.*;

public class Glyph {

	private int font = 0;

	private int character = 0;

	private int offset = 0;

	private int baseline = 0;

	private int width = 0;

	private int height = 0;

	private byte[] fontdata = null;

	/**
	 * Construct a Glyph object
	 * 
	 * @param font
	 *            Font ID for Glyph
	 * @param character
	 *            Character ID for Glyph
	 * @param offset
	 *            x-offset of Glyph data for drawing
	 * @param baseline
	 *            y-offset of Glyph data for drawing
	 * @param width
	 *            Width of Glyph, in pixels
	 * @param height
	 *            Height of Glyph, in pixels
	 * @param fontdata
	 *            Data detailing Glyph's graphical representation
	 */
	public Glyph(int font, int character, int offset, int baseline, int width,
			int height, byte[] fontdata) {
		this.font = font;
		this.character = character;
		this.offset = offset;
		this.baseline = baseline;
		this.width = width;
		this.height = height;
		this.fontdata = fontdata;


	}

	@Override
	public String toString() {
		return "\'" + ((char)this.character) + "\': font=" + this.font + ", " + width + "x" + height;
	}

	/**
	 * Retrieve the font ID for this Glyph
	 * 
	 * @return Font ID
	 */
	public int getFont() {
		return this.font;
	}

	/**
	 * Retrive y-offset of Glyph data
	 * 
	 * @return y-offset
	 */
	public int getBaseLine() {
		return this.baseline;
	}

	/**
	 * Return character ID of this Glyph
	 * 
	 * @return ID of character represented by this Glyph
	 */
	public int getCharacter() {
		return this.character;
	}

	/**
	 * Retrive x-offset of Glyph data
	 * 
	 * @return x-offset
	 */
	public int getOffset() {
		return this.offset;
	}

	/**
	 * Return width of Glyph
	 * 
	 * @return Glyph width, in pixels
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Return height of Glyph
	 * 
	 * @return Glyph height, in pixels
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Graphical data for this Glyph
	 * 
	 * @return Data defining graphical representation of this Glyph
	 */
	public byte[] getFontData() {
		return this.fontdata;
	}
}
