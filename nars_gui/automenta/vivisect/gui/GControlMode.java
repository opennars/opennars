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
import processing.core.PConstants;

public enum GControlMode implements PConstants{

	CORNER 		( "X Y W H coordinates", 		"CORNER",	PApplet.CORNER	),
	CORNERS 	( "X0 Y0 X1 Y1 coordinates", 	"CORNERS",	PApplet.CORNERS	),
	CENTER		( "X Y W H coordinates",		"CENTER",	PApplet.CENTER	);

	
	public final String description;
	public final String ps_name;
	public final int mode;
	
	private GControlMode(String desc, String name, int ctrl_mode ){
		description = desc;
		ps_name = name;
		mode = ctrl_mode;
	}
	
	public String toString(){
		return description;
	}
}
