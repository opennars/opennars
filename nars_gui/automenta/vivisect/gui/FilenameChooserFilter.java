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

import java.io.File;
import java.io.FilenameFilter;

import processing.core.PApplet;

class FilenameChooserFilter implements FilenameFilter {

	private final String[] ftypes;

	public FilenameChooserFilter(String types){
		ftypes = PApplet.split(types.toLowerCase(), ',');
		for(String e : ftypes)
			e = e.trim();
	}

	public boolean accept(File dir, String name) {
		String fext = null;
		int i = name.lastIndexOf('.');
		if (i > 0 &&  i < name.length() - 1)
			fext = name.substring(i+1).toLowerCase();
		if(fext != null){
			for(String e : ftypes)
				if(fext.equals(e))
					return true;
		}
		return false;
	}

}
