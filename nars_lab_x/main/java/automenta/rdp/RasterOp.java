/* RasterOp.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Set of operations used in displaying raster graphics
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
// Created on 01-Jul-2003
package automenta.rdp;

import org.apache.log4j.Logger;

public class RasterOp {
	static Logger logger = Logger.getLogger(RdesktopCanvas.class);

	private static void ropInvert(WrappedImage biDst, int[] dest, int width, int x,
								  int y, int cx, int cy, int Bpp) {
		int mask = Options.bpp_mask;
		int pdest = (y * width + x);
		for (int i = 0; i < cy; i++) {
			for (int j = 0; j < cx; j++) {
				if (biDst != null) {
					int c = biDst.getRGB(x + j, y + i);
					biDst.setRGB(x + j, y + i, ~c & mask);
				} else
					dest[pdest] = (~dest[pdest]) & mask;
				pdest++;
			}
			pdest += (width - cx);
		}
	}

	private static void ropClear(WrappedImage biDst, int width, int x, int y, int cx,
								 int cy, int Bpp) {

		for (int i = x; i < x + cx; i++) {
			for (int j = y; j < y + cy; j++)
				biDst.setRGB(i, j, 0);
		}
	}

	private static void ropSet(WrappedImage biDst, int width, int x, int y, int cx,
							   int cy, int Bpp) {

		int mask = Options.bpp_mask;

		for (int i = x; i < x + cx; i++) {
			for (int j = y; j < y + cy; j++)
				biDst.setRGB(i, j, mask);
		}

	}

	private static void ropCopy(WrappedImage biDst, int dstwidth, int x, int y,
								int cx, int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {

		if (src == null) { // special case - copy to self
			biDst.getGraphics()
					.copyArea(srcx, srcy, cx, cy, x - srcx, y - srcy);
		} else {
			biDst.setRGB(x, y, cx, cy, src, 0, cx);
		}
	}

	/**
	 * Perform an operation on a rectangular area of a WrappedImage, using an
	 * integer array of colour values as source if necessary
	 * 
	 * @param opcode
	 *            Code defining operation to perform
	 * @param biDst
	 *            Destination image for operation
	 * @param dstwidth
	 *            Width of destination image
	 * @param x
	 *            X-offset of destination area within destination image
	 * @param y
	 *            Y-offset of destination area within destination image
	 * @param cx
	 *            Width of destination area
	 * @param cy
	 *            Height of destination area
	 * @param src
	 *            Source data, represented as an array of integer pixel values
	 * @param srcwidth
	 *            Width of source data
	 * @param srcx
	 *            X-offset of source area within source data
	 * @param srcy
	 *            Y-offset of source area within source data
	 */
	public void do_array(int opcode, WrappedImage biDst, int dstwidth, int x,
			int y, int cx, int cy, int[] src, int srcwidth, int srcx, int srcy) {
		int Bpp = Options.Bpp;
		// int[] dst = null;
		// System.out.println("do_array: opcode = 0x" +
		// Integer.toHexString(opcode) );
		switch (opcode) {
		case 0x0:
			ropClear(biDst, dstwidth, x, y, cx, cy, Bpp);
			break;
		case 0x1:
			ropNor(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0x2:
			ropAndInverted(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx,
					srcy, Bpp);
			break;
		case 0x3: // CopyInverted
			ropInvert(biDst, src, srcwidth, srcx, srcy, cx, cy, Bpp);
			ropCopy(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0x4: // AndReverse
			ropInvert(biDst, null, dstwidth, x, y, cx, cy, Bpp);
			ropAnd(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0x5:
			ropInvert(biDst, null, dstwidth, x, y, cx, cy, Bpp);
			break;
		case 0x6:
			ropXor(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0x7:
			ropNand(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0x8:
			ropAnd(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0x9:
			ropEquiv(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0xa: // Noop
			break;
		case 0xb:
			ropOrInverted(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx,
					srcy, Bpp);
			break;
		case 0xc:
			ropCopy(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy,
					Bpp);
			break;
		case 0xd: // OrReverse
			ropInvert(biDst, null, dstwidth, x, y, cx, cy, Bpp);
			ropOr(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy, Bpp);
			break;
		case 0xe:
			ropOr(biDst, dstwidth, x, y, cx, cy, src, srcwidth, srcx, srcy, Bpp);
			break;
		case 0xf:
			ropSet(biDst, dstwidth, x, y, cx, cy, Bpp);
			break;
		default:
			logger.warn("do_array unsupported opcode: " + opcode);
			// rop_array(opcode,dst,dstwidth,x,y,cx,cy,src,srcwidth,srcx,srcy);
		}
	}

	/**
	 * Perform an operation on a single pixel in a WrappedImage
	 * 
	 * @param opcode
	 *            Opcode defining operation to perform
	 * @param dst
	 *            Image on which to perform the operation
	 * @param x
	 *            X-coordinate of pixel to modify
	 * @param y
	 *            Y-coordinate of pixel to modify
	 * @param color
	 *            Colour to use in operation (unused for some operations)
	 */
	public static void do_pixel(int opcode, WrappedImage dst, int x, int y, int color) {
		int mask = Options.bpp_mask;

		if (dst == null)
			return;

		int c = dst.getRGB(x, y);

		final int nextColor;
		switch (opcode) {
		case 0x0:
			nextColor = 0;
			break;
		case 0x1:
			nextColor = (~(c | color)) & mask;
			break;
		case 0x2:
			nextColor = c & ((~color) & mask);
			break;
		case 0x3:
			nextColor = ((~color) & mask);
			break;
		case 0x4:
			nextColor = ((~c & color) * mask);
			break;
		case 0x5:
			nextColor = ((~c) & mask);
			break;
		case 0x6:
			nextColor = (c ^ ((color) & mask));
			break;
		case 0x7:
			nextColor = ((~c & color) & mask);
			break;
		case 0x8:
			nextColor = (c & (color & mask));
			break;
		case 0x9:
			nextColor = (c ^ (~color & mask));
			break;
		case 0xa: /* Noop */
			return;
		case 0xb:
			nextColor = (c | (~color & mask));
			break;
		case 0xc:
			nextColor = (color);
			break;
		case 0xd:
			nextColor = ((~c | color) & mask);
			break;
		case 0xe:
			nextColor = (c | (color & mask));
			break;
		case 0xf:
			nextColor = (mask);
			break;
		default:
			logger.warn("do_byte unsupported opcode: " + opcode);
			return;
		}

		dst.setRGB(x, y, nextColor);
	}

	private static void ropNor(WrappedImage biDst, int dstwidth, int x, int y, int cx,
							   int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0x1
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);

		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				biDst.setRGB(x + cx, y + cy,
						(~(biDst.getRGB(x + cx, y + cy) | src[psrc])) & mask);
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropAndInverted(WrappedImage biDst, int dstwidth, int x, int y,
									   int cx, int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0x2
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				int c = biDst.getRGB(x + cx, y + cy);
				biDst.setRGB(x + cx, y + cy, c & ((~src[psrc]) & mask));
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropXor(WrappedImage biDst, int dstwidth, int x, int y, int cx,
							   int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0x6
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				int c = biDst.getRGB(x + col, y + row);
				biDst.setRGB(x + col, y + row, c ^ ((src[psrc]) & mask));
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropNand(WrappedImage biDst, int dstwidth, int x, int y,
								int cx, int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0x7
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				int c = biDst.getRGB(x + col, y + row);
				biDst.setRGB(x + col, y + row, (~(c & src[psrc])) & mask);
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropAnd(WrappedImage biDst, int dstwidth, int x, int y, int cx,
							   int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0x8
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				int c = biDst.getRGB(x + col, y + row);
				biDst.setRGB(x + col, y + row, c & ((src[psrc]) & mask));
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropEquiv(WrappedImage biDst, int dstwidth, int x, int y,
								 int cx, int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0x9
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				final int yRow = y + row;
				int c = biDst.getRGB(x + col, yRow);
				biDst.setRGB(x + col, yRow, c ^ ((~src[psrc++]) & mask));
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropOrInverted(WrappedImage biDst, int dstwidth, int x, int y,
									  int cx, int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0xb
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			final int yRow = y + row;
			for (int col = 0; col < cx; col++) {
				int c = biDst.getRGB(x + col, yRow);
				biDst.setRGB(x + col, yRow, c | ((~src[psrc++]) & mask));
			}
			psrc += (srcwidth - cx);
		}
	}

	private static void ropOr(WrappedImage biDst, int dstwidth, int x, int y, int cx,
							  int cy, int[] src, int srcwidth, int srcx, int srcy, int Bpp) {
		// opcode 0xe
		int mask = Options.bpp_mask;
		int psrc = (srcy * srcwidth + srcx);
		for (int row = 0; row < cy; row++) {
			for (int col = 0; col < cx; col++) {
				int c = biDst.getRGB(x + col, y + row);
				biDst.setRGB(x + col, y + row, c | (src[psrc] & mask));
				psrc++;
			}
			psrc += (srcwidth - cx);
		}
	}
}
