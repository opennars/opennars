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

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Base class for different types of hot spot.
 * 
 * @author Peter Lager
 *
 */
abstract class HotSpot implements GConstants, Comparable<HotSpot> {

	public final Integer id;
	public float x, y;

	abstract public boolean contains(float px, float py);

	protected HotSpot(int id){
		this.id = Math.abs(id);
	}

	public void adjust(Object ... arguments){}

	public int compareTo(HotSpot spoto) {
		return id.compareTo(spoto.id);
	}

	/**
	 * Hit is based on being inside a rectangle.
	 * 
	 * @author Peter Lager
	 */
	static class HSrect extends HotSpot {
		public float w, h;

		public HSrect(int id, float x, float y, float w, float h) {
			super(id);
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public boolean contains(final float px, final float py) {
			return (px >= x && py >= y && px <= x + w && py <= y + h);
		}
	}

	/**
	 * Hit based on an arc (pie slice)
	 * 
	 * @author Peter Lager
	 *
	 */
	static class HSarc extends HotSpot {

		public float sa, ea, r, r2;

		public HSarc(int id, float x, float y, float r, float sa, float ea) {
			super(id);
			this.x = x;
			this.y = y;
			this.r = r;
			this.r2 = r * r;
			this.sa = sa;
			this.ea = ea;
		}


		@Override
		public boolean contains(float px, float py) {
			if((px-x)*(px-x) + (py-y)*(py-y) > r2)
				return false;
			// Now check angle
			float a = (float) Math.toDegrees(Math.atan2(py-y, px-x));
			if(a < 0) a += 360;
			if(a < sa) a += 360;
			return (a >= sa && a <= ea);
		}
	}

	/**
	 * Hit is based on being inside a rectangle.
	 * 
	 * @author Peter Lager
	 */
	static class HScircle extends HotSpot {
		public float r, r2;

		public HScircle(int id, float x, float y, float r) {
			super(id);
			this.x = x;
			this.y = y;
			this.r = r;
			this.r2 = r * r;
		}

		@Override
		public boolean contains(final float px, final float py) {
                    float dx = (px-x);
                    float dy = (py-y);
                    dx*=dx;
                    if (dx <= r2) {
                        dy*=dy;
                        if (dy+dx <= r2)
                            return true;
                    }
                    return false;
		}

		/**
		 * If used the parameters must be in the order x, y then r. <br>
		 * 
		 */
		@SuppressWarnings(value={"fallthrough"})
		public void adjust(Object ... arguments){
			switch(arguments.length){
			case 3:
				r = Float.valueOf(arguments[2].toString());
				r2 = r * r;
			case 2:
				y = Float.valueOf(arguments[1].toString());
			case 1:
				x = Float.valueOf(arguments[0].toString());
			}
		}


		public String toString(){
			return "HS circle ["+x+", "+y+"]  radius = "+r;
		}
	}

	/**
	 * Hit depends on the mask image. non-transparent areas are black and
	 * transparent areas are white. <br>
	 * 
	 * It is better this way because scaling the image can change the 
	 * colour white to very nearly white but black is unchanged so is 
	 * easier to test.
	 * 
	 * @author Peter Lager
	 *
	 */
	static class HSmask extends HotSpot {

		private PImage mask = null;

		protected HSmask(int id, PImage mask) {
			super(id);
			this.mask = mask;
		}

		@Override
		public boolean contains(float px, float py) {
			if(mask != null){
				int pixel = mask.get((int)px, (int)py);
				float alpha = (pixel >> 24) & 0xff;
				// A > 0 and RGB = 0 is transparent
				if(alpha > 0 && (pixel & 0x00ffffff) == 0){
					return true;
				}
			}
			return false;
		}

		public String toString(){
			return "HS mask ["+x+", "+y+"]";
		}

	}

	/**
	 * Hit is determined by the alpha channel value.
	 * @author Peter
	 *
	 */
	static class HSalpha extends HotSpot {

		private PImage image = null;

		private int offX, offY;

		protected HSalpha(int id, float x, float y, PImage image, int imageMode) {
			super(id);
			this.image = image;
			this.x = x;
			this.y = y;
			if(imageMode == PApplet.CENTER){
				offX = -image.width/2;
				offY = -image.height/2;
			}
			else
				offX = offY = 0;
		}

		/**
		 * If used the parameters must be in the order x, y then image. <br>
		 */
		@SuppressWarnings(value={"fallthrough"})
		public void adjust(Object ... arguments){
			switch(arguments.length){
			case 3:
				image = (PImage) arguments[2];
			case 2:
				y = Float.valueOf(arguments[1].toString());
			case 1:
				x = Float.valueOf(arguments[0].toString());
			}
		}

		@Override
		public boolean contains(float px, float py) {
			if(image != null){
				int imgX = Math.round(px - x) - offX;
				int imgY = Math.round(py - y) - offY;
				float alpha = (image.get(imgX, imgY) >> 24) & 0xff;
				if(alpha >  ALPHA_PICK)
					return true;
			}
			return false;
		}

		public String toString(){
			return "HS alpha ["+x+", "+y+"]";
		}

	}
}
