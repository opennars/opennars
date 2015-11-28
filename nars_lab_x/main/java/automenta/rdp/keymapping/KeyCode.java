/* KeyCode.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
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
package automenta.rdp.keymapping;

public class KeyCode {
	/**
	 * X scancodes for the printable keys of a standard 102 key MF-II Keyboard
	 */
	public static final int SCANCODE_EXTENDED = 0x80;

	private final int[] main_key_scan_qwerty = { 0x29, 0x02, 0x03, 0x04, 0x05,
			0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x10, 0x11, 0x12,
			0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1E, 0x1F,
			0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x2B, 0x2C,
			0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x56 };

	private static final String[] main_key_US = { "`~", "1!", "2@", "3#", "4$",
			"5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ", "wW", "eE",
			"rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}", "aA", "sS",
			"dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "''\"", // added '
																	// to \"
			"\\|", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?" };

	/** * United States keyboard layout (phantom key version) */
	/* (XFree86 reports the <> key even if it's not physically there) */
	private static final String[] main_key_US_phantom = { "`~", "1!", "2@",
			"3#", "4$", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "'\"",
			"\\|", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?",
			"<>" /* the phantom key */
	};

	/** * British keyboard layout */
	private static final String[] main_key_UK = { "`�|", "1!", "2\"", "3�",
			"4$�", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}", "aA",
			"sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "'@", "#~",
			"zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?", "\\|" };

	/** * French keyboard layout (contributed by Eric Pouech) */
	private static final String[] main_key_FR = { "�", "&1", "�2~", "\"3#",
			"'4{", "(5[", "-6|", "�7", "_8\\", "�9^�", "�0@", ")�]", "=+}",
			"aA", "zZ", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "^�",
			"$��", "qQ", "sS�", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "mM",
			"�%", "*�", "wW", "xX", "cC", "vV", "bB", "nN", ",?", ";.", ":/",
			"!�", "<>" };

	/** * Icelandic keyboard layout (contributed by R�khar�ur Egilsson) */
	private static final String[] main_key_IS = { "�", "1!", "2\"", "3#", "4$",
			"5%", "6&", "7/{", "8([", "9)]", "0=}", "��\\", "-_", "qQ@", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "��", "'?~", "aA",
			"sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "�^", "+*`",
			"zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "��", "<>|" };

	/** * German keyboard layout (contributed by Ulrich Weigand) */
	private static final String[] main_key_DE = { "^�", "1!", "2\"�", "3��",
			"4$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "�?\\", "'`", "qQ@",
			"wW", "eE�", "rR", "tT", "zZ", "uU", "iI", "oO", "pP", "��", "+*~",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "��",
			"#�", "yY", "xX", "cC", "vV", "bB", "nN", "mM�", ",;", ".:", "-_",
			"<>|" };

	/** * German keyboard layout without dead keys */
	private static final String[] main_key_DE_nodead = { "^�", "1!", "2\"",
			"3�", "4$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "�?\\", "�",
			"qQ", "wW", "eE", "rR", "tT", "zZ", "uU", "iI", "oO", "pP", "��",
			"+*~", "aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��",
			"��", "#'", "yY", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:",
			"-_", "<>" };

	/** * Swiss German keyboard layout (contributed by Jonathan Naylor) */
	private static final String[] main_key_SG = { "��", "1+|", "2\"@", "3*#",
			"4�", "5%", "6&�", "7/�", "8(�", "9)", "0=", "'?�", "^`~", "qQ",
			"wW", "eE", "rR", "tT", "zZ", "uU", "iI", "oO", "pP", "��[", "�!]",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "��{",
			"$�}", "yY", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_",
			"<>\\" };

	/** * Swiss French keyboard layout (contributed by Philippe Froidevaux) */
	private static final String[] main_key_SF = { "��", "1+|", "2\"@", "3*#",
			"4�", "5%", "6&�", "7/�", "8(�", "9)", "0=", "'?�", "^`~", "qQ",
			"wW", "eE", "rR", "tT", "zZ", "uU", "iI", "oO", "pP", "��[", "�!]",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "��{",
			"$�}", "yY", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_",
			"<>\\" };

	/** * Norwegian keyboard layout (contributed by Ove K�ven) */
	private static final String[] main_key_NO = { "|�", "1!", "2\"@", "3#�",
			"4�$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "+?", "\\`�", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "��", "�^~",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "��",
			"'*", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_",
			"<>" };

	/** * Danish keyboard layout (contributed by Bertho Stultiens) */
	private static final String[] main_key_DA = { "��", "1!", "2\"@", "3#�",
			"4�$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "+?", "�`|", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "��", "�^~",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "��",
			"'*", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_",
			"<>\\" };

	/** * Swedish keyboard layout (contributed by Peter Bortas) */
	private static final String[] main_key_SE = { "��", "1!", "2\"@", "3#�",
			"4�$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "+?\\", "�`", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "��", "�^~",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "��",
			"'*", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_",
			"<>|" };

	/** * Canadian French keyboard layout */
	private static final String[] main_key_CF = { "#|\\", "1!�", "2\"@", "3/�",
			"4$�", "5%�", "6?�", "7&�", "8*�", "9(�", "0)�", "-_�", "=+�",
			"qQ", "wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO�", "pP�",
			"^^[", "��]", "aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL",
			";:~", "``{", "<>}", "zZ", "xX", "cC", "vV", "bB", "nN", "mM",
			",'-", ".", "��", "���" };

	/** * Portuguese keyboard layout */
	private static final String[] main_key_PT = { "\\�", "1!", "2\"@", "3#�",
			"4$�", "5%", "6&", "7/{", "8([", "9)]", "0=}", "'?", "��", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "+*\\�",
			"\\'\\`", "aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL",
			"��", "��", "\\~\\^", "zZ", "xX", "cC", "vV", "bB", "nN", "mM",
			",;", ".:", "-_", "<>" };

	/** * Italian keyboard layout */
	private static final String[] main_key_IT = { "\\|", "1!�", "2\"�", "3��",
			"4$�", "5%�", "6&�", "7/{", "8([", "9)]", "0=}", "'?`", "�^~",
			"qQ@", "wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO�", "pP�",
			"��[", "+*]", "aA", "sS�", "dD�", "fF", "gG", "hH", "jJ", "kK",
			"lL", "��@", "�#", "��", "zZ", "xX", "cC", "vV", "bB", "nN",
			"mM�", ",;", ".:�", "-_", "<>|" };

	/** * Finnish keyboard layout */
	private static final String[] main_key_FI = { "", "1!", "2\"@", "3#", "4$",
			"5%", "6&", "7/{", "8([", "9)]", "0=}", "+?\\", "\'`", "qQ", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "", "\"^~", "aA",
			"sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "", "", "'*", "zZ",
			"xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_", "<>|" };

	/** * Russian keyboard layout (contributed by Pavel Roskin) */
	private static final String[] main_key_RU = { "`~", "1!", "2@", "3#", "4$",
			"5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ��", "wW��",
			"eE��", "rR��", "tT��", "yY��", "uU��", "iI��", "oO��", "pP��",
			"[{��", "]}��", "aA��", "sS��", "dD��", "fF��", "gG��", "hH��",
			"jJ��", "kK��", "lL��", ";:��", "'\"��", "\\|", "zZ��", "xX��",
			"cC��", "vV��", "bB��", "nN��", "mM��", ",<��", ".>��", "/?" };

	/** * Russian keyboard layout KOI8-R */
	private static final String[] main_key_RU_koi8r = { "()", "1!", "2\"",
			"3/", "4$", "5:", "6,", "7.", "8;", "9?", "0%", "-_", "=+", "��",
			"��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��",
			"��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "��",
			"\\|", "��", "��", "��", "��", "��", "��", "��", "��", "��", "/?",
			"<>" /* the phantom key */
	};

	/** * Spanish keyboard layout (contributed by Jos� Marcos L�pez) */
	private static final String[] main_key_ES = { "��\\", "1!|", "2\"@", "3�#",
			"4$", "5%", "6&�", "7/", "8(", "9)", "0=", "'?", "��", "qQ", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "`^[", "+*]", "aA",
			"sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "'�{", "��}",
			"zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_", "<>" };

	/** * Belgian keyboard layout ** */
	private static final String[] main_key_BE = { "", "&1|", "�2@", "\"3#",
			"'4", "(5", "�6^", "�7", "!8", "�9{", "�0}", ")�", "-_", "aA",
			"zZ", "eE�", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "^�[",
			"$*]", "qQ", "sS�", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "mM",
			"�%�", "��`", "wW", "xX", "cC", "vV", "bB", "nN", ",?", ";.", ":/",
			"=+~", "<>\\" };

	/** * Hungarian keyboard layout (contributed by Zolt�n Kov�cs) */
	private static final String[] main_key_HU = { "0�", "1'~", "2\"�", "3+^",
			"4!�", "5%�", "6/�", "7=`", "8(�", "9)�", "�ֽ", "�ܨ", "�Ӹ",
			"qQ\\", "wW|", "eE", "rR", "tT", "zZ", "uU", "iI�", "oO�", "pP",
			"���", "���", "aA", "sS�", "dD�", "fF[", "gG]", "hH", "jJ�", "kK�",
			"lL�", "��$", "���", "�ۤ", "yY>", "xX#", "cC&", "vV@", "bB{",
			"nN}", "mM", ",?;", ".:�", "-_*", "��<" };

	/** * Polish (programmer's) keyboard layout ** */
	private static final String[] main_key_PL = { "`~", "1!", "2@", "3#", "4$",
			"5%", "6^", "7&�", "8*", "9(", "0)", "-_", "=+", "qQ", "wW",
			"eE��", "rR", "tT", "yY", "uU", "iI", "oO��", "pP", "[{", "]}",
			"aA��", "sS��", "dD", "fF", "gG", "hH", "jJ", "kK", "lL��", ";:",
			"'\"", "\\|", "zZ��", "xX��", "cC��", "vV", "bB", "nN��", "mM",
			",<", ".>", "/?", "<>|" };

	/** * Croatian keyboard layout specific for me <jelly@srk.fer.hr> ** */
	private static final String[] main_key_HR_jelly = { "`~", "1!", "2@", "3#",
			"4$", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{��", "]}��",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:��",
			"'\"��", "\\|��", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<",
			".>", "/?", "<>|" };

	/** * Croatian keyboard layout ** */
	private static final String[] main_key_HR = { "��", "1!", "2\"�", "3#^",
			"4$�", "5%�", "6&�", "7/`", "8(�", "9)�", "0=�", "'?�", "+*�",
			"qQ\\", "wW|", "eE", "rR", "tT", "zZ", "uU", "iI", "oO", "pP",
			"���", "���", "aA", "sS", "dD", "fF[", "gG]", "hH", "jJ", "kK�",
			"lL�", "��", "���", "���", "yY", "xX", "cC", "vV@", "bB{", "nN}",
			"mM�", ",;", ".:", "-_/", "<>" };

	/** * Japanese 106 keyboard layout ** */
	private static final String[] main_key_JA_jp106 = { "1!", "2\"", "3#",
			"4$", "5%", "6&", "7'", "8(", "9)", "0~", "-=", "^~", "\\|", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "@`", "[{",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";+", ":*",
			"]}", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?",
			"\\_", };

	/** * Japanese pc98x1 keyboard layout ** */
	private static final String[] main_key_JA_pc98x1 = { "1!", "2\"", "3#",
			"4$", "5%", "6&", "7'", "8(", "9)", "0", "-=", "^`", "\\|", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "@~", "[{",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";+", ":*",
			"]}", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?",
			"\\_", };

	/** * Brazilian ABNT-2 keyboard layout (contributed by Raul Gomes Fernandes) */
	private static final String[] main_key_PT_br = { "'\"", "1!", "2@", "3#",
			"4$", "5%", "6\"", "7&", "8*", "9(", "0)", "-_", "=+", "qQ", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "'`", "[{", "aA",
			"sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "~^", "]}",
			"zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?" };

	/**
	 * * US international keyboard layout (contributed by Gustavo Noronha
	 * (kov@debian.org))
	 */
	private static final String[] main_key_US_intl = { "`~", "1!", "2@", "3#",
			"4$", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "\\|", "qQ",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "'\"",
			"zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?" };

	/**
	 * * Slovak keyboard layout (see cssk_ibm(sk_qwerty) in xkbsel) -
	 * dead_abovering replaced with degree - no symbol in iso8859-2 - brokenbar
	 * replaced with bar
	 */
	private static final String[] main_key_SK = { ";�`'", "+1", "�2", "�3",
			"�4", "�5", "�6", "�7", "�8", "�9", "�0)", "=%", "", "qQ\\", "wW|",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "�/�", "�(�", "aA",
			"sS�", "dD�", "fF[", "gG]", "hH", "jJ", "kK�", "lL�", "�\"$",
			"�!�", "�)�", "zZ>", "xX#", "cC&", "vV@", "bB{", "nN}", "mM",
			",?<", ".:>", "-_*", "<>\\|" };

	/**
	 * * Slovak and Czech (programmer's) keyboard layout (see
	 * cssk_dual(cs_sk_ucw))
	 */
	private static final String[] main_key_SK_prog = { "`~", "1!", "2@", "3#",
			"4$", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ��",
			"wW��", "eE��", "rR��", "tT��", "yY��", "uU��", "iI��", "oO��",
			"pP��", "[{", "]}", "aA��", "sS��", "dD��", "fF��", "gG��", "hH��",
			"jJ��", "kK��", "lL��", ";:", "'\"", "\\|", "zZ��", "xX�", "cC��",
			"vV��", "bB", "nN��", "mM��", ",<", ".>", "/?", "<>" };

	/** * Czech keyboard layout (see cssk_ibm(cs_qwerty) in xkbsel) */
	private static final String[] main_key_CS = { ";", "+1", "�2", "�3", "�4",
			"�5", "�6", "�7", "�8", "�9", "�0�)", "=%", "", "qQ\\", "wW|",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "�/[{", ")(]}",
			"aA", "sS�", "dD�", "fF[", "gG]", "hH", "jJ", "kK�", "lL�", "�\"$",
			"�!�", "�'", "zZ>", "xX#", "cC&", "vV@", "bB{", "nN}", "mM", ",?<",
			".:>", "-_*", "<>\\|" };

	/** * Latin American keyboard layout (contributed by Gabriel Orlando Garcia) */
	private static final String[] main_key_LA = { "|��", "1!", "2\"", "3#",
			"4$", "5%", "6&", "7/", "8(", "9)", "0=", "'?\\", "��", "qQ@",
			"wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "��", "+*~",
			"aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "{[^",
			"}]`", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_",
			"<>" };

	/** * Lithuanian (Baltic) keyboard layout (contributed by Nerijus Bali�nas) */
	private static final String[] main_key_LT_B = { "`~", "��", "��", "��",
			"��", "��", "��", "��", "��", "((", "))", "-_", "��", "qQ", "wW",
			"eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}", "aA",
			"sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "'\"", "\\|",
			"zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?" };

	/** * Turkish keyboard Layout */
	private static final String[] main_key_TK = { "\"�", "1!", "2'", "3^#",
			"4+$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "*?\\", "-_", "qQ@",
			"wW", "eE", "rR", "tT", "yY", "uU", "�I�", "oO", "pP", "��", "��~",
			"aA�", "sS�", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "��", "i�",
			",;`", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", "��", "��", ".:" };
}
