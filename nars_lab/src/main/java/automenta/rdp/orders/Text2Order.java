/* Text2Order.java
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

public class Text2Order implements Order {

	private int flags = 0;

	private int mixmode = 0;

	private int fgcolor = 0;

	private int bgcolor = 0;

	private int x = 0;

	private int y = 0;

	private int unknown = 0;

	private int font = 0;

	private int clipleft = 0;

	private int cliptop = 0;

	private int clipright = 0;

	private int clipbottom = 0;

	private int boxleft = 0;

	private int boxtop = 0;

	private int boxright = 0;

	private int boxbottom = 0;

	private int opcode = 0;

	private int length = 0;

	byte[] text = new byte[256];

	public Text2Order() {
	}

	public int getFont() {
		return this.font;
	}

	public int getClipLeft() {
		return this.clipleft;
	}

	public int getClipRight() {
		return this.clipright;
	}

	public int getClipTop() {
		return this.cliptop;
	}

	public int getClipBottom() {
		return this.clipbottom;
	}

	public int getBoxLeft() {
		return this.boxleft;
	}

	public int getBoxRight() {
		return this.boxright;
	}

	public int getBoxTop() {
		return this.boxtop;
	}

	public int getBoxBottom() {
		return this.boxbottom;
	}

	public int getX() {
		return this.x;
	}

	public int getUnknown() {
		return this.unknown;
	}

	public int getY() {
		return this.y;
	}

	public int getFlags() {
		return this.flags;
	}

	public int getMixmode() {
		return this.mixmode;
	}

	public int getForegroundColor() {
		return this.fgcolor;
	}

	public int getBackgroundColor() {
		return this.bgcolor;
	}

	public int getLength() {
		return this.length;
	}

	public byte[] getText() {
		return this.text;
	}

	public void setFont(int font) {
		this.font = font;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setUnknown(int unknown) {
		this.unknown = unknown;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setMixmode(int mixmode) {
		this.mixmode = mixmode;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void setForegroundColor(int fgcolor) {
		this.fgcolor = fgcolor;
	}

	public void setBackgroundColor(int bgcolor) {
		this.bgcolor = bgcolor;
	}

	public void setClipLeft(int clipleft) {
		this.clipleft = clipleft;
	}

	public void setClipRight(int clipright) {
		this.clipright = clipright;
	}

	public void setClipTop(int cliptop) {
		this.cliptop = cliptop;
	}

	public void setClipBottom(int clipbottom) {
		this.clipbottom = clipbottom;
	}

	public void setBoxLeft(int boxleft) {
		this.boxleft = boxleft;
	}

	public void setBoxRight(int boxright) {
		this.boxright = boxright;
	}

	public void setBoxTop(int boxtop) {
		this.boxtop = boxtop;
	}

	public void setBoxBottom(int boxbottom) {
		this.boxbottom = boxbottom;
	}

	public void setText(byte[] text) {
		this.text = text;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void reset() {
		font = 0;
		flags = 0;
		mixmode = 0;
		unknown = 0;
		fgcolor = 0;
		bgcolor = 0;
		clipleft = 0;
		cliptop = 0;
		clipright = 0;
		clipbottom = 0;
		boxleft = 0;
		boxtop = 0;
		boxright = 0;
		boxbottom = 0;
		x = 0;
		y = 0;
		length = 0;
		opcode = 0;
		text = new byte[256];
	}

	public int getOpcode() {
		return opcode;
	}

	public void setOpcode(int name) {
		opcode = name;
	}
}
