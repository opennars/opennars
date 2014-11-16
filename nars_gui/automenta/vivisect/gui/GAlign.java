/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2008-12 Peter Lager

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

/**
 * This class provides an enumeration that is used to control the alignment
 * of text and images.
 * 
 * @author Peter Lager
 *
 */
public enum GAlign {

	INVALID			( -1, "INVALID", "Invalid alignment" ),
	
	// Horizontal alignment constants
	LEFT 			( 0, "LEFT", "Align left" ),
	CENTER 			( 1, "CENTER", "Align centre horizontally" ),
	RIGHT			( 2, "RIGHT", "Align right" ),
	JUSTIFY  		( 3, "JUSTIFY", "Justify text" ),
	
	// Vertical alignment constants
	TOP 			( 16, "TOP", "Align top" ),
	MIDDLE	 		( 17, "MIDDLE", "Align middle vertically" ),
	BOTTOM 			( 18, "BOTTOM", "Align bottom" );

	
	/**
	 * Get an alignment based on its ID number.
	 * 
	 * @param id the id number for this alignment.
	 * @return the alignment or INVALID if not found
	 */
	public static GAlign getFromID(int id){
		switch(id){
		case 0:
			return LEFT;
		case 1:
			return CENTER;
		case 2:
			return RIGHT;
		case 3:
			return JUSTIFY;
		case 16:
			return TOP;
		case 17:
			return MIDDLE;
		case 18:
			return BOTTOM;
		}
		return INVALID;
	}
	
	/**
	 * Get an alignment based on its alignment text.
	 * 
	 * @param text the alignment text.
	 * @return the alignment or INVALID if not found
	 */
	public static GAlign getFromText(String text){
		text = text.toUpperCase();
		if(text.equals("LEFT"))
			return LEFT;
		if(text.equals("CENTER"))
			return CENTER;
		if(text.equals("RIGHT"))
			return RIGHT;
		if(text.equals("JUSTIFY"))
			return JUSTIFY;
		if(text.equals("TOP"))
			return TOP;
		if(text.equals("MIDDLE"))
			return MIDDLE;
		if(text.equals("BOTTOM"))
			return BOTTOM;
		return INVALID;
	}
	
	private int alignID;
	private String alignText;
	private String description;

	/**
	 * A private constructor to prevent alignments being create outside this class.
	 * 
	 * @param id
	 * @param text
	 * @param desc
	 */
	private GAlign(int id, String text, String desc ){
		alignID = id;
		alignText = text;
		description = desc;
	}
	
	/**
	 * Get the id number associated with this alignment
	 * @return the ID associated with this alignment
	 */
	public int getID(){
		return alignID;
	}
	
	/**
	 * Get the text ID associated with this alignment.
	 * 
	 * @return alignment text e.g. "RIGHT"
	 */
	public String getTextID(){
		return alignText;
	}
	
	/**
	 * Get the description of this alignment
	 * 
	 * @return e.g. "Align top"
	 */
	public String getDesc(){
		return description;
	}
	
	/**
	 * Is this a horizontal alignment constant?
	 */
	public boolean isHorzAlign(){
		return alignID >= 0 && alignID <= 8;
	}
	
	/**
	 * Is this a vertical alignment constant?
	 */
	public boolean isVertAlign(){
		return alignID >= 16;
	}
	
	/**
	 * Get the alignment text.
	 */
	public String toString(){
		return alignText;
	}
}
