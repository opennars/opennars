/* ClipBMP.java
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
package automenta.rdp.rdp5.cliprdr;

import automenta.rdp.Input;
import org.apache.log4j.Logger;

import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ClipBMP extends Component {

	private static final long serialVersionUID = -756738379924520867L;

	protected static Logger logger = Logger.getLogger(Input.class);

	// --- Private constants
	private final static int BITMAPFILEHEADER_SIZE = 14;

	private final static int BITMAPINFOHEADER_SIZE = 40;

	// --- Private variable declaration
	// --- Bitmap file header
	private byte bitmapFileHeader[] = new byte[14];

	private byte bfType[] = { 'B', 'M' };

	private int bfSize = 0;

	private int bfReserved1 = 0;

	private int bfReserved2 = 0;

	private int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;

	// --- Bitmap info header
	private byte bitmapInfoHeader[] = new byte[40];

	private int biSize = BITMAPINFOHEADER_SIZE;

	private int biWidth = 0;

	private int biHeight = 0;

	private int biPlanes = 1;

	private int biBitCount = 24;

	private int biCompression = 0;

	private int biSizeImage = 0x030000;

	private int biXPelsPerMeter = 0x0;

	private int biYPelsPerMeter = 0x0;

	private int biClrUsed = 0;

	private int biClrImportant = 0;

	// --- Bitmap raw data
	private int bitmap[];

	// --- File section
	private OutputStream fo;

	// --- Default constructor
	public ClipBMP() {
	}

	public byte[] getBitmapAsBytes(Image parImage, int parWidth, int parHeight) {
		try {
			fo = new ByteArrayOutputStream();
			save(parImage, parWidth, parHeight);
			fo.close();
		} catch (Exception saveEx) {
			saveEx.printStackTrace();
		}
		return ((ByteArrayOutputStream) fo).toByteArray();
	}

	public void saveBitmap(String parFilename, Image parImage, int parWidth,
			int parHeight) {
		try {
			fo = new FileOutputStream(parFilename);
			save(parImage, parWidth, parHeight);
			fo.close();
		} catch (Exception saveEx) {
			saveEx.printStackTrace();
		}
	}

	/*
	 * The saveMethod is the main method of the process. This method will call
	 * the convertImage method to convert the memory image to a byte array;
	 * method writeBitmapFileHeader creates and writes the bitmap file header;
	 * writeBitmapInfoHeader creates the information header; and writeBitmap
	 * writes the image.
	 * 
	 */
	private void save(Image parImage, int parWidth, int parHeight) {
		try {
			convertImage(parImage, parWidth, parHeight);
			// writeBitmapFileHeader();
			writeBitmapInfoHeader();
			writeBitmap();
		} catch (Exception saveEx) {
			saveEx.printStackTrace();
		}
	}

	/*
	 * convertImage converts the memory image to the bitmap format (BRG). It
	 * also computes some information for the bitmap info header.
	 * 
	 */
	private boolean convertImage(Image parImage, int parWidth, int parHeight) {
		int pad;
		bitmap = new int[parWidth * parHeight];
		PixelGrabber pg = new PixelGrabber(parImage, 0, 0, parWidth, parHeight,
				bitmap, 0, parWidth);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return (false);
		}
		pad = (4 - ((parWidth * 3) % 4)) * parHeight;
		biSizeImage = ((parWidth * parHeight) * 3) + pad;
		bfSize = biSizeImage + BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
		biWidth = parWidth;
		biHeight = parHeight;
		return (true);
	}

	/*
	 * writeBitmap converts the image returned from the pixel grabber to the
	 * format required. Remember: scan lines are inverted in a bitmap file!
	 * 
	 * Each scan line must be padded to an even 4-byte boundary.
	 */
	private void writeBitmap() {
		int size;
		int value;
		int j;
		int i;
		int rowCount;
		int rowIndex;
		int lastRowIndex;
		int pad;
		int padCount;
		byte rgb[] = new byte[3];
		size = (biWidth * biHeight) - 1;
		pad = 4 - ((biWidth * 3) % 4);
		if (pad == 4) // <==== Bug correction
			pad = 0; // <==== Bug correction
		rowCount = 1;
		padCount = 0;
		rowIndex = size - biWidth;
		lastRowIndex = rowIndex;
		try {
			for (j = 0; j < size; j++) {
				value = bitmap[rowIndex];
				rgb[0] = (byte) (value & 0xFF);
				rgb[1] = (byte) ((value >> 8) & 0xFF);
				rgb[2] = (byte) ((value >> 16) & 0xFF);
				fo.write(rgb);
				if (rowCount == biWidth) {
					padCount += pad;
					for (i = 1; i <= pad; i++) {
						fo.write(0x00);
					}
					rowCount = 1;
					rowIndex = lastRowIndex - biWidth;
					lastRowIndex = rowIndex;
				} else
					rowCount++;
				rowIndex++;
			}
			// --- Update the size of the file
			bfSize += padCount - pad;
			biSizeImage += padCount - pad;
		} catch (Exception wb) {
			wb.printStackTrace();
		}
	}

	/*
	 * writeBitmapFileHeader writes the bitmap file header to the file.
	 * 
	 */
	// private void writeBitmapFileHeader() {
	// try {
	// fo.write(bfType);
	// fo.write(intToDWord(bfSize));
	// fo.write(intToWord(bfReserved1));
	// fo.write(intToWord(bfReserved2));
	// fo.write(intToDWord(bfOffBits));
	// } catch (Exception wbfh) {
	// wbfh.printStackTrace();
	// }
	// }
	
	/*
	 * 
	 * writeBitmapInfoHeader writes the bitmap information header to the file.
	 * 
	 */
	private void writeBitmapInfoHeader() {
		try {
			fo.write(intToDWord(biSize));
			fo.write(intToDWord(biWidth));
			fo.write(intToDWord(biHeight));
			fo.write(intToWord(biPlanes));
			fo.write(intToWord(biBitCount));
			fo.write(intToDWord(biCompression));
			fo.write(intToDWord(biSizeImage));
			fo.write(intToDWord(biXPelsPerMeter));
			fo.write(intToDWord(biYPelsPerMeter));
			fo.write(intToDWord(biClrUsed));
			fo.write(intToDWord(biClrImportant));
		} catch (Exception wbih) {
			wbih.printStackTrace();
		}
	}

	/*
	 * 
	 * intToWord converts an int to a word, where the return value is stored in
	 * a 2-byte array.
	 * 
	 */
	private static byte[] intToWord(int parValue) {
		byte retValue[] = new byte[2];
		retValue[0] = (byte) (parValue & 0x00FF);
		retValue[1] = (byte) ((parValue >> 8) & 0x00FF);
		return (retValue);
	}

	/*
	 * 
	 * intToDWord converts an int to a double word, where the return value is
	 * stored in a 4-byte array.
	 * 
	 */
	private static byte[] intToDWord(int parValue) {
		byte retValue[] = new byte[4];
		retValue[0] = (byte) (parValue & 0x00FF);
		retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
		retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
		retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);
		return (retValue);
	}

	/**
	 * loadbitmap() method converted from Windows C code. Reads only
	 * uncompressed 24- and 8-bit images. Tested with images saved using
	 * Microsoft Paint in Windows 95. If the image is not a 24- or 8-bit image,
	 * the program refuses to even try. I guess one could include 4-bit images
	 * by masking the byte by first 1100 and then 0011. I am not really
	 * interested in such images. If a compressed image is attempted, the
	 * routine will probably fail by generating an IOException. Look for
	 * variable ncompression to be different from 0 to indicate compression is
	 * present.
	 * 
	 * Arguments: sdir and sfile are the result of the FileDialog()
	 * getDirectory() and getFile() methods.
	 * 
	 * Returns: Image Object, be sure to check for (Image)null !!!!
	 * 
	 */
	public static Image loadbitmap(InputStream fs) {
		Image image;
		try {
			// int bflen = 14; // 14 byte BITMAPFILEHEADER
			// byte bf[] = new byte[bflen];
			// fs.read(bf, 0, bflen);
			int bilen = 40; // 40-byte BITMAPINFOHEADER
			byte bi[] = new byte[bilen];
			fs.read(bi, 0, bilen);

			// Interperet data.
			// int nsize = (((int) bf[5] & 0xff) << 24)
			// | (((int) bf[4] & 0xff) << 16)
			// | (((int) bf[3] & 0xff) << 8) | (int) bf[2] & 0xff;
			// System.out.println("Size of file is :" + nsize);

			// int nbisize = (((int) bi[3] & 0xff) << 24)
			// | (((int) bi[2] & 0xff) << 16)
			// | (((int) bi[1] & 0xff) << 8) | (int) bi[0] & 0xff;
			// System.out.println("Size of bitmapinfoheader is :" + nbisize);

			int nwidth = (((int) bi[7] & 0xff) << 24)
					| (((int) bi[6] & 0xff) << 16)
					| (((int) bi[5] & 0xff) << 8) | (int) bi[4] & 0xff;
			// System.out.println("Width is :" + nwidth);

			int nheight = (((int) bi[11] & 0xff) << 24)
					| (((int) bi[10] & 0xff) << 16)
					| (((int) bi[9] & 0xff) << 8) | (int) bi[8] & 0xff;
			// System.out.println("Height is :" + nheight);

			// int nplanes = (((int) bi[13] & 0xff) << 8) | (int) bi[12] & 0xff;
			// System.out.println("Planes is :" + nplanes);

			int nbitcount = (((int) bi[15] & 0xff) << 8) | (int) bi[14] & 0xff;
			// System.out.println("BitCount is :" + nbitcount);

			// Look for non-zero values to indicate compression
			// int ncompression = (((int) bi[19]) << 24) | (((int) bi[18]) <<
			// 16)
			// | (((int) bi[17]) << 8) | (int) bi[16];
			// System.out.println("Compression is :" + ncompression);

			int nsizeimage = (((int) bi[23] & 0xff) << 24)
					| (((int) bi[22] & 0xff) << 16)
					| (((int) bi[21] & 0xff) << 8) | (int) bi[20] & 0xff;
			// System.out.println("SizeImage is :" + nsizeimage);

			// int nxpm = (((int) bi[27] & 0xff) << 24)
			// | (((int) bi[26] & 0xff) << 16)
			// | (((int) bi[25] & 0xff) << 8) | (int) bi[24] & 0xff;
			// System.out.println("X-Pixels per meter is :" + nxpm);

			// int nypm = (((int) bi[31] & 0xff) << 24)
			// | (((int) bi[30] & 0xff) << 16)
			// | (((int) bi[29] & 0xff) << 8) | (int) bi[28] & 0xff;
			// System.out.println("Y-Pixels per meter is :" + nypm);

			int nclrused = (((int) bi[35] & 0xff) << 24)
					| (((int) bi[34] & 0xff) << 16)
					| (((int) bi[33] & 0xff) << 8) | (int) bi[32] & 0xff;
			// System.out.println("Colors used are :" + nclrused);

			// int nclrimp = (((int) bi[39] & 0xff) << 24)
			// | (((int) bi[38] & 0xff) << 16)
			// | (((int) bi[37] & 0xff) << 8) | (int) bi[36] & 0xff;
			// System.out.println("Colors important are :" + nclrimp);

			if (nbitcount == 24) {
				// No Palatte data for 24-bit format but scan lines are
				// padded out to even 4-byte boundaries.
				int npad = (nsizeimage / nheight) - nwidth * 3;
				int ndata[] = new int[nheight * nwidth];
				byte brgb[] = new byte[(nwidth + npad) * 3 * nheight];
				fs.read(brgb, 0, (nwidth + npad) * 3 * nheight);
				int nindex = 0;
				for (int j = 0; j < nheight; j++) {
					for (int i = 0; i < nwidth; i++) {
						ndata[nwidth * (nheight - j - 1) + i] = (255 & 0xff) << 24
								| ((brgb[nindex + 2] & 0xff) << 16)
								| ((brgb[nindex + 1] & 0xff) << 8)
								| brgb[nindex] & 0xff;
						// System.out.println("Encoded Color at ("
						// +i+","+j+")is:"+nrgb+" (R,G,B)= (" +((int)(brgb[2]) &
						// 0xff)+"," +((int)brgb[1]&0xff)+","
						// +((int)brgb[0]&0xff)+")";
						nindex += 3;
					}
					nindex += npad;
				}

				image = Toolkit.getDefaultToolkit()
						.createImage(
								new MemoryImageSource(nwidth, nheight, ndata,
										0, nwidth));
			} else if (nbitcount == 16) {
				// Have to determine the number of colors, the clrsused
				// parameter is dominant if it is greater than zero. If
				// zero, calculate colors based on bitsperpixel.
				int nNumColors = 0;
				if (nclrused > 0) {
					nNumColors = nclrused;
				} else {
					nNumColors = (1 & 0xff) << nbitcount;
				}
				// System.out.println("The number of Colors is " + nNumColors);

				// Some bitmaps do not have the sizeimage field calculated
				// Ferret out these cases and fix 'em.
				if (nsizeimage == 0) {
					nsizeimage = ((((nwidth * nbitcount) + 31) & ~31) >> 3);
					nsizeimage *= nheight;
					// System.out.println("nsizeimage (backup) is " +
					// nsizeimage);
				}

				// Read the palatte colors.
				int npalette[] = new int[nNumColors];
				byte bpalette[] = new byte[nNumColors * 4];
				fs.read(bpalette, 0, nNumColors * 4);
				int nindex8 = 0;
				for (int n = 0; n < nNumColors; n++) {
					npalette[n] = (255 & 0xff) << 24
							| (((int) bpalette[nindex8 + 2] & 0xff) << 16)
							| (((int) bpalette[nindex8 + 1] & 0xff) << 8)
							| (int) bpalette[nindex8] & 0xff;
					// System.out.println ("Palette Color "+n +"
					// is:"+npalette[n]+" (res,R,G,B)=
					// ("+((int)(bpalette[nindex8+3]) & 0xff)+","
					// +((int)(bpalette[nindex8+2]) & 0xff)+","
					// +((int)bpalette[nindex8+1]&0xff)+","
					// +((int)bpalette[nindex8]&0xff)+")");
					nindex8 += 4;
				}

				// Read the image data (actually indices into the palette)
				// Scan lines are still padded out to even 4-byte
				// boundaries.
				int npad8 = (nsizeimage / nheight) - nwidth;
				// System.out.println("nPad is:" + npad8);

				int ndata8[] = new int[nwidth * nheight];
				byte bdata[] = new byte[(nwidth + npad8) * nheight];
				fs.read(bdata, 0, (nwidth + npad8) * nheight);
				nindex8 = 0;
				for (int j8 = 0; j8 < nheight; j8++) {
					for (int i8 = 0; i8 < nwidth; i8++) {
						ndata8[nwidth * (nheight - j8 - 1) + i8] = npalette[((int) bdata[nindex8] & 0xff)]
								| npalette[((int) bdata[nindex8 + 1] & 0xff)] << 8;
						nindex8 += 2;
					}
					nindex8 += npad8;
				}

				image = Toolkit.getDefaultToolkit().createImage(
						new MemoryImageSource(nwidth, nheight, ndata8, 0,
								nwidth));
			} else if (nbitcount == 8) {
				// Have to determine the number of colors, the clrsused
				// parameter is dominant if it is greater than zero. If
				// zero, calculate colors based on bitsperpixel.
				int nNumColors = 0;
				if (nclrused > 0) {
					nNumColors = nclrused;
				} else {
					nNumColors = (1 & 0xff) << nbitcount;
				}
				// System.out.println("The number of Colors is " + nNumColors);

				// Some bitmaps do not have the sizeimage field calculated
				// Ferret out these cases and fix 'em.
				if (nsizeimage == 0) {
					nsizeimage = ((((nwidth * nbitcount) + 31) & ~31) >> 3);
					nsizeimage *= nheight;
					// System.out.println("nsizeimage (backup) is " +
					// nsizeimage);
				}

				// Read the palatte colors.
				int npalette[] = new int[nNumColors];
				byte bpalette[] = new byte[nNumColors * 4];
				fs.read(bpalette, 0, nNumColors * 4);
				int nindex8 = 0;
				for (int n = 0; n < nNumColors; n++) {
					npalette[n] = (255 & 0xff) << 24
							| (((int) bpalette[nindex8 + 2] & 0xff) << 16)
							| (((int) bpalette[nindex8 + 1] & 0xff) << 8)
							| (int) bpalette[nindex8] & 0xff;
					// System.out.println ("Palette Color "+n +"
					// is:"+npalette[n]+" (res,R,G,B)=
					// ("+((int)(bpalette[nindex8+3]) & 0xff)+","
					// +((int)(bpalette[nindex8+2]) & 0xff)+","
					// +((int)bpalette[nindex8+1]&0xff)+","
					// +((int)bpalette[nindex8]&0xff)+")");
					nindex8 += 4;
				}

				// Read the image data (actually indices into the palette)
				// Scan lines are still padded out to even 4-byte
				// boundaries.
				int npad8 = (nsizeimage / nheight) - nwidth;
				// System.out.println("nPad is:" + npad8);

				int ndata8[] = new int[nwidth * nheight];
				byte bdata[] = new byte[(nwidth + npad8) * nheight];
				fs.read(bdata, 0, (nwidth + npad8) * nheight);
				nindex8 = 0;
				for (int j8 = 0; j8 < nheight; j8++) {
					for (int i8 = 0; i8 < nwidth; i8++) {
						ndata8[nwidth * (nheight - j8 - 1) + i8] = npalette[((int) bdata[nindex8] & 0xff)];
						nindex8++;
					}
					nindex8 += npad8;
				}

				image = Toolkit.getDefaultToolkit().createImage(
						new MemoryImageSource(nwidth, nheight, ndata8, 0,
								nwidth));
			} else if (nbitcount == 4) {
				// Have to determine the number of colors, the clrsused
				// parameter is dominant if it is greater than zero. If
				// zero, calculate colors based on bitsperpixel.
				int nNumColors = 0;
				if (nclrused > 0) {
					nNumColors = nclrused;
				} else {
					nNumColors = (1 & 0xff) << nbitcount;
				}
				// System.out.println("The number of Colors is " + nNumColors);

				// Some bitmaps do not have the sizeimage field calculated
				// Ferret out these cases and fix 'em.
				if (nsizeimage == 0) {
					nsizeimage = ((((nwidth * nbitcount) + 31) & ~31) >> 3);
					nsizeimage *= nheight;
					// System.out.println("nsizeimage (backup) is " +
					// nsizeimage);
				}

				// Read the palatte colors.
				int npalette[] = new int[nNumColors + 1];
				byte bpalette[] = new byte[nNumColors * 4];
				fs.read(bpalette, 0, nNumColors * 4);
				int nindex8 = 0;
				for (int n = 0; n < nNumColors; n++) {
					npalette[n] = (255 & 0xff) << 24
							| (((int) bpalette[nindex8 + 2] & 0xff) << 16)
							| (((int) bpalette[nindex8 + 1] & 0xff) << 8)
							| (int) bpalette[nindex8] & 0xff;
					nindex8 += 4;
				}

				// Read the image data (actually indices into the palette)
				// Scan lines are still padded out to even 4-byte
				// boundaries.
				int npad8 = (nsizeimage * 2 / nheight) - nwidth;
				// System.out.println("nPad is:" + npad8);
				if (npad8 == 4)
					npad8 = 0;

				int ndata8[] = new int[nwidth * nheight];
				byte bdata[] = new byte[(nwidth / 2 + npad8) * nheight];
				fs.read(bdata, 0, (nwidth / 2 + npad8) * nheight);// (nwidth)
				// *
				// nheight);
				nindex8 = 0;
				// System.out.println("nwidth = " + nwidth + ", nheight = " +
				// nheight);
				for (int j8 = 0; j8 < nheight; j8++) {
					for (int i8 = 0; i8 < (nwidth) - 1; i8 += 2) {
						ndata8[nwidth * (nheight - j8 - 1) + i8] = npalette[((int) (bdata[nindex8] & 0x0f))];
						ndata8[nwidth * (nheight - j8 - 1) + i8 + 1] = npalette[((int) (bdata[nindex8] & 0xf0) / 0xf)];
						System.out.print("1:" + (bdata[nindex8] & 0x0f) + '\t');
						System.out.print("2:" + ((bdata[nindex8] & 0xf0) / 0xf)
								+ '\t');
						// System.out.print(nindex8 + "/" + nsizeimage + "\t");
						// ndata8[nwidth * j8 + i8] = npalette[((int)
						// (bdata[nindex8] & 0x0f))];
						// ndata8[nwidth * j8 + i8 + 1] = npalette[((int)
						// (bdata[nindex8] & 0xf0) / 0xf)];
						// System.out.print("\t" + (nheight * j8 + i8) + "=(" +
						// npalette[((int) (bdata[nindex8] & 0x0f))] + ")");
						// System.out.print("\t" + (nheight * j8 + i8 + 1) +
						// "=(" + npalette[((int) (bdata[nindex8] & 0xf0) /
						// 0xf)] + ")");
						nindex8++;
					}
					// nindex8 += npad8;
				}

				// image = null;
				image = Toolkit.getDefaultToolkit().createImage(
						new MemoryImageSource(nwidth, nheight, ndata8, 0,
								nwidth));
			} else {
				logger
						.warn("Not a 24-bit or 8-bit Windows Bitmap, aborting...");
				image = (Image) null;
			}

			fs.close();
			return image;
		} catch (Exception e) {
			// System.out.println("\nCaught exception in loadbitmap: " +
			// e.getMessage() + " " + e.getClass().getName());
		}
		return (Image) null;
	}

}
