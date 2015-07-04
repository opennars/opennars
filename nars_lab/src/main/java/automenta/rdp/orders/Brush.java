/* Brush.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
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
package automenta.rdp.orders;

public class Brush {

	private int xorigin = 0;

	private int yorigin = 0;

	private int style = 0;

	private byte[] pattern = new byte[8];

	public Brush() {
	}

	public int getXOrigin() {
		return this.xorigin;
	}

	public int getYOrigin() {
		return this.yorigin;
	}

	public int getStyle() {
		return this.style;
	}

	public byte[] getPattern() {
		return this.pattern;
	}

	public void setXOrigin(int xorigin) {
		this.xorigin = xorigin;
	}

	public void setYOrigin(int yorigin) {
		this.yorigin = yorigin;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public void setPattern(byte[] pattern) {
		this.pattern = pattern;
	}

	public void reset() {
		xorigin = 0;
		yorigin = 0;
		style = 0;
		pattern = new byte[8];
	}
}
