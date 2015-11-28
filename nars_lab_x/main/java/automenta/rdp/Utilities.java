/* Utilities.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Provide replacements for useful methods that were unavailable prior to
 *          Java 1.4 (Java 1.1 compliant).
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

import java.awt.datatransfer.DataFlavor;
import java.util.StringTokenizer;

public class Utilities {

	/**
	 * Replaces each substring of this string that matches the given regular
	 * expression with the given replacement.
	 * 
	 * @param in
	 *            Input string
	 * @param regex
	 *            Regular expression describing patterns to match within input
	 *            string
	 * @param replace
	 *            Patterns matching regular expression within input are replaced
	 *            with this string
	 * @return
	 */
	public static String strReplaceAll(String in, String regex, String replace) {
		String[] finArgs = null;
		StringTokenizer tok = new StringTokenizer(in, regex);
		for (Object[] obj = { tok, finArgs = new String[tok.countTokens()],
				new int[] { 0 } }; ((StringTokenizer) obj[0]).hasMoreTokens(); ((String[]) obj[1])[((int[]) obj[2])[0]++] = ((StringTokenizer) obj[0])
				.nextToken()) {
		}
		StringBuilder out = new StringBuilder(finArgs[0]);
		for (int i = 1; i < finArgs.length; i++) {
			out.append(replace).append(finArgs[i]);
		}
		return out.toString();
	}

	/**
	 * Split a string into segments separated by a specified substring
	 * 
	 * @param in
	 *            Input string
	 * @param splitWith
	 *            String with which to split input string
	 * @return Array of separated string segments
	 */
	public static String[] split(String in, String splitWith) {
		String[] out = null;
		StringTokenizer tok = new StringTokenizer(in, splitWith);
		for (Object[] obj = { tok, out = new String[tok.countTokens()],
				new int[] { 0 } }; ((StringTokenizer) obj[0]).hasMoreTokens(); ((String[]) obj[1])[((int[]) obj[2])[0]++] = ((StringTokenizer) obj[0])
				.nextToken()) {
		}
		return out;
	}

	public static DataFlavor imageFlavor = new DataFlavor(java.awt.Image.class,
			"image/x-java-image");

}
