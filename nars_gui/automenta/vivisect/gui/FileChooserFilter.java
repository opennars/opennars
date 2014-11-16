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
import javax.swing.filechooser.FileFilter;
import processing.core.PApplet;

class FileChooserFilter extends FileFilter {
	
	private final String[] ftypes;
	private String description = null;
	
	public FileChooserFilter(String types){
		this(types, null);
	}
	
	public FileChooserFilter(String types, String desc){
		ftypes = PApplet.split(types.toLowerCase(), ',');
		for(String e : ftypes)
			e = e.trim();
		description = desc;
	}
	
	@Override
	public boolean accept(File f) {
		String fext = getExtension(f);
		if(fext != null){
			for(String e : ftypes)
				if(fext.equals(e))
					return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
            ext = s.substring(i+1).toLowerCase();
        return ext;
    }
}
