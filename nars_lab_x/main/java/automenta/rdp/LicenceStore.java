/* LicenceStore.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Handle saving and loading of licences
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

import org.apache.log4j.Logger;

public abstract class LicenceStore {

	static Logger logger = Logger.getLogger(Licence.class);

	/**
	 * Load a licence from a file
	 * 
	 * @return Licence data stored in file
	 */
	public byte[] load_licence() {
		String path = Options.licence_path + "/licence." + Options.hostname;
		byte[] data = null;
		try {
			FileInputStream fd = new FileInputStream(path);
			data = new byte[fd.available()];
			fd.read(data);
		} catch (FileNotFoundException e) {
			logger.warn("Licence file not found!");
		} catch (IOException e) {
			logger.warn("IOException in load_licence");
		}
		return data;
	}

	/**
	 * Save a licence to file
	 * 
	 * @param databytes
	 *            Licence data to store
	 */
	public void save_licence(byte[] databytes) {
		/* set and create the directory -- if it doesn't exist. */
		// String home = "/root";
		String dirpath = Options.licence_path;// home+"/.rdesktop";
		String filepath = dirpath + "/licence." + Options.hostname;

		File file = new File(dirpath);
		file.mkdir();
		try {
			FileOutputStream fd = new FileOutputStream(filepath);

			/* write to the licence file */
			fd.write(databytes);
			fd.close();
			logger.info("Stored licence at " + filepath);
		} catch (FileNotFoundException e) {
			logger.warn("save_licence: file path not valid!");
		} catch (IOException e) {
			logger.warn("IOException in save_licence");
		}
	}

}
