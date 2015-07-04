/* DeskSaveOrder.java
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

public class DeskSaveOrder extends BoundsOrder {

	private int offset = 0;

	private int action = 0;

	public DeskSaveOrder() {
		super();
	}

	public int getOffset() {
		return this.offset;
	}

	public int getAction() {
		return this.action;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public void reset() {
		super.reset();
		offset = 0;
		action = 0;
	}
}
