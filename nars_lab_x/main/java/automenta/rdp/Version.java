/* Version.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Stores version information
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

import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Records the current version information of properJavaRDP
 */

public class Version {
	public static String version = "1.1";

	/**
	 * Display the current version of properJavaRDP
	 */
	public static void main(String[] argv) {
		try {
			if (argv.length == 0) {
				System.out.println(version);
			} else {
				String filename = argv[0];
				System.out.println("Writing version information to: "
						+ filename);
				PrintWriter file = new PrintWriter(new FileOutputStream(
						filename), true);

				file.println("product.version=" + version);
				file.close();
			}
		} catch (Exception e) {
			System.err.println("Problem writing version information: " + e);
			e.printStackTrace(System.err);
		}
	}
}
