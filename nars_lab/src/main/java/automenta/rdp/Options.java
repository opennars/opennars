/* Options.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Global static storage of user-definable options
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

import java.awt.image.DirectColorModel;

public class Options {

	public static final int DIRECT_BITMAP_DECOMPRESSION = 0;
	public static final int BUFFEREDIMAGE_BITMAP_DECOMPRESSION = 1;
	public static final int INTEGER_BITMAP_DECOMPRESSION = 2;

	public static int bitmap_decompression_store =
			//DIRECT_BITMAP_DECOMPRESSION;
			INTEGER_BITMAP_DECOMPRESSION;
			//BUFFEREDIMAGE_BITMAP_DECOMPRESSION; //seems slow


	// disables bandwidth saving tcp packets
	public static boolean low_latency = true;

	public static int keylayout = 0x409; // UK by default

	public static String username = ""; // -u username

	public static String domain = ""; // -d domain

	public static String password = ""; // -p password

	public static String hostname = ""; // -n hostname

	public static String command = ""; // -s command

	public static String directory = ""; // -d directory

	public static String windowTitle = "RDP"; // -T windowTitle

	public static int width = 1280; // -g widthxheight

	public static int height = 1024; // -g widthxheight

	public static int port = 3389; // -t port

	public static boolean fullscreen = false;

	public static boolean built_in_licence = false;

	public static boolean load_licence = false;

	public static boolean save_licence = false;

	public static String licence_path = "./";

	public static boolean debug_keyboard = false;

	public static boolean debug_hexdump = false;

	public static boolean enable_menu = true;

	// public static boolean paste_hack = true;

	public static boolean altkey_quiet = false;

	public static boolean caps_sends_up_and_down = true;

	public static boolean remap_hash = true;

	public static boolean useLockingKeyState = true;

	public static boolean use_rdp5 = true;

	public static int server_bpp = 16; // Bits per pixel

	public static int Bpp = (server_bpp + 7) / 8; // Bytes per pixel

	// Correction value to ensure only the relevant number of bytes are used for
	// a pixel
	public static int bpp_mask = 0xFFFFFF >> 8 * (3 - Bpp);

	public static int imgCount = 0;

	public static DirectColorModel colour_model = new DirectColorModel(24,
			0xFF0000, 0x00FF00, 0x0000FF);

	/**
	 * Set a new value for the server's bits per pixel
	 * 
	 * @param server_bpp
	 *            New bpp value
	 */
	public static void set_bpp(int server_bpp) {
		Options.server_bpp = server_bpp;
		Options.Bpp = (server_bpp + 7) / 8;
		if (server_bpp == 8)
			bpp_mask = 0xFF;
		else
			bpp_mask = 0xFFFFFF;

		colour_model = new DirectColorModel(24, 0xFF0000, 0x00FF00, 0x0000FF);
	}

	public static int server_rdp_version;

	public static int win_button_size = 0; /* If zero, disable single app mode */

	public static boolean bitmap_compression = true;

	public static boolean persistent_bitmap_caching = false;

	public static boolean bitmap_caching = true;

	public static boolean precache_bitmaps = true;

	public static boolean polygon_ellipse_orders = true;

	public static boolean sendmotion = true;

	public static boolean orders = true;

	public static boolean encryption = false;

	public static boolean packet_encryption = false;

	public static boolean desktop_save = true;

	public static boolean grab_keyboard = true;

	public static boolean hide_decorations = false;

	public static boolean console_session = false;

	public static boolean owncolmap;

	public static boolean use_ssl = false;

	public static boolean map_clipboard = true;

	public static int rdp5_performanceflags = Rdp.RDP5_NO_CURSOR_SHADOW
			| Rdp.RDP5_NO_CURSORSETTINGS | Rdp.RDP5_NO_FULLWINDOWDRAG
			| Rdp.RDP5_NO_MENUANIMATIONS /*| Rdp.RDP5_NO_THEMING*/
			| Rdp.RDP5_NO_WALLPAPER ;

	public static boolean save_graphics = false;

}
