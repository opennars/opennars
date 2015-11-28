/* TriBltOrder.java
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

public class TriBltOrder extends PatBltOrder {

	private int color_table = 0;

	private int cache_id = 0;

	private int cache_idx = 0;

	private int srcx = 0;

	private int srcy = 0;

	private int unknown = 0;

	public TriBltOrder() {
		super();
	}

	public int getColorTable() {
		return this.color_table;
	}

	public int getCacheID() {
		return this.cache_id;
	}

	public int getCacheIDX() {
		return this.cache_idx;
	}

	public int getSrcX() {
		return this.srcx;
	}

	public int getSrcY() {
		return this.srcy;
	}

	public int getUnknown() {
		return this.unknown;
	}

	public void setColorTable(int color_table) {
		this.color_table = color_table;
	}

	public void setCacheID(int cache_id) {
		this.cache_id = cache_id;
	}

	public void setCacheIDX(int cache_idx) {
		this.cache_idx = cache_idx;
	}

	public void setSrcX(int srcx) {
		this.srcx = srcx; // corrected
	}

	public void setSrcY(int srcy) {
		this.srcy = srcy; // corrected
	}

	public void setUnknown(int unknown) {
		this.unknown = unknown;
	}

	public void reset() {
		super.reset();
		color_table = 0;
		cache_id = 0;
		cache_idx = 0;
		srcx = 0;
		srcy = 0;
		unknown = 0;
	}
}
