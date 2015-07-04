/* JRdpLoader.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Launch ProperJavaRDP with settings from a config file
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
package automenta.rdp.loader;

import automenta.rdp.Rdesktop;
import automenta.rdp.RdesktopException;
import automenta.rdp.rdp.Utilities_Localised;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class JRdpLoader {

	// Set of identifiers to be found within the launch file
	private static String[] identifiers = { "--user", "--password", "--domain",
			"--fullscreen", "--geometry", "--use_rdp5" };

	// Set of command-line options mapping to the launch file identifiers
	private static String[] pairs = { "-u", "-p", "-d", "-f", "-g",
			"--use_rdp5" };

	public static void main(String args[]) {

		if (args.length <= 0) {
			System.err.println("Expected usage: JRdpLoader launchFile");
			System.exit(-1);
		}

		String launchFile = args[0];

		String server = "";
		String port = "";

		try {
			String outArgs = "";

			// Open the file specified at the command-line
			FileInputStream fstream = new FileInputStream(launchFile);
			DataInputStream in = new DataInputStream(fstream);
			while (in.available() != 0) {
				String line = in.readLine();
				StringTokenizer stok = new StringTokenizer(line);
				if (stok.hasMoreTokens()) {
					String identifier = stok.nextToken();
					String value = "";
					while (stok.hasMoreTokens()) {
						value += stok.nextToken();
						if (stok.hasMoreTokens())
							value += " ";
					}

					if (identifier.equals("--server"))
						server = value;
					else if (identifier.equals("--port"))
						port = value;
					else {
						String p = getParam(identifier);
						if (p != null)
							outArgs += p + ' ' + value + ' ';
					}
				}
			}

			if (server != null && server != "") {
				outArgs += server;
				if (port != null && port != "")
					outArgs += ':' + port;

				// String[] finArgs = outArgs.split(" ");
				String[] finArgs = Utilities_Localised.split(outArgs, " ");

				Rdesktop.main(finArgs);
				in.close();
			} else {
				System.err.println("No server name provided");
				System.exit(0);
			}

		} catch (IOException ioe) {
			System.err.println("Launch file could not be read: "
					+ ioe.getMessage());
			System.exit(-1);
		} catch (RdesktopException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static String getParam(String identifier) {
		for (int i = 0; i < identifiers.length; i++) {
			if (identifier.equals(identifiers[i])) {
				return pairs[i];
			}
		}

		return null;
	}

}
