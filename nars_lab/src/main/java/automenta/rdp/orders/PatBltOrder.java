/* PatBltOrder.java
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

public class PatBltOrder extends DestBltOrder {

	private int bgcolor = 0;

	private int fgcolor = 0;

	private Brush brush = null;

	public PatBltOrder() {
		super();
		brush = new Brush();
	}

	public int getBackgroundColor() {
		return this.bgcolor;
	}

	public int getForegroundColor() {
		return this.fgcolor;
	}

	public Brush getBrush() {
		return this.brush;
	}

	public void setBackgroundColor(int bgcolor) {
		this.bgcolor = bgcolor;
	}

	public void setForegroundColor(int fgcolor) {
		this.fgcolor = fgcolor;
	}

	public void reset() {
		super.reset();
		bgcolor = 0;
		fgcolor = 0;
		brush.reset();
	}
}
