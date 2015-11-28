/* DataBlob.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Encapsulates Blobs of byte data, of arbitrary size (although 
 *          data cannot be changed once set)
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

public class DataBlob {

	private byte[] data = null;

	private int size = 0;

	/**
	 * Construct a DataBlob with a givne size and content. Once constructed, the
	 * DataBlob cannot be modified
	 * 
	 * @param size
	 *            Size of data
	 * @param data
	 *            Array of byte data to store in blob
	 */
	public DataBlob(int size, byte[] data) {
		this.size = size;
		this.data = data;
	}

	/**
	 * Retrieve size of data stored in this DataBlob
	 * 
	 * @return Size of stored data
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Retrieve data stored in this DataBlob
	 * 
	 * @return Stored data
	 */
	public byte[] getData() {
		return this.data;
	}
}
