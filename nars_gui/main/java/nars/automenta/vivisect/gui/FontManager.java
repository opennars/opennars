/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2014 Peter Lager

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

import java.awt.Font;
//import java.awt.GraphicsEnvironment;
//import java.util.Arrays;

/**
 * 
 * This class is used to access system fonts. <br>
 * 
 * Only basic functionality is available in 3.5 but will be extended to 
 * 
 * Introduced v3.5
 * 
 * 
 * @author Peter Lager
 *
 */
public class FontManager {

	
//	private static String[] ffnames;
	
//	static {
//		ffnames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//		Arrays.sort(ffnames); // Should have been sorted but just in case
//		// listFontFamilyNames();
//	}
//	
//	public static void listFontFamilyNames(){
//		System.out.println("\n----------- FontManagerFonts   ------------------------");
//		for (String f : ffnames) 
//			System.out.println(f);
//		System.out.println("\n## Number of family names = " + ffnames.length);
//		System.out.println("----------------------------------------------------------\n");
//		
//	}
	
	/** Default list of font family names in priority order */
	private static String[] pfnames = {"Arial", "Trebuchet MS", "Tahoma", "Helvetica", "Verdana" };
	

	/**
	 * Get a system font that matches one the font family names with the style and size. If 
	 * it can't find a suitable system font from the font family it returns a logical
	 * font (Dialog) of the specified style and size.
	 * 
	 * @param familyFontNamnes user defined list of family font names or null to use default list
	 * @param style Font.PLAIN, Font.BOLD, Font.Italic 
	 * @param size font size
	 * @return a system font, or if none found a logical font
	 */
	public static Font getPriorityFont(String[] familyFontNamnes, int style, int size){
		Font font = null;
		String[] names = (familyFontNamnes == null || familyFontNamnes.length == 0)
				? pfnames : familyFontNamnes;
		for(String name : names){
			font = getFont(name, style, size);
			if(font != null) return font;
		}
		return getFont("Dialog", style, size);
	}
	
	/**
	 * Get a system font that matches the font family name, style and size. If 
	 * it can't find a system font from the font family it returns null.
	 * 
	 * @param familyName font family name e.g. "Arial", "Trebuchet MS" ...
	 * @param style Font.PLAIN, Font.BOLD, Font.Italic 
	 * @param size font size
	 * @return a system font from the specified family name, if not found returns null
	 */
	public static Font getFont(String familyName, int style, int size){
		Font font = new Font(familyName, style, size);
		if(familyName.equalsIgnoreCase(font.getFamily()))
			return font;
		return null;
	}
	
}
