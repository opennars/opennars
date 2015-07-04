// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.viewer.swing;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyboardConvertor {
	private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	private static final String PATTERN_STRING_FOR_SCANCODE = "scancode=(\\d+)";
	private Pattern patternForScancode;
	@SuppressWarnings("serial")
	private static final Map<Integer, CodePair> keyMap = new HashMap<Integer, CodePair>() {{
		put(192 /* Back Quote */, new CodePair('`' /*96*/, '~' /*126*/));
		put(49 /* 1 */, new CodePair('1' /*49*/, '!' /*33*/));
		put(50 /* 2 */, new CodePair('2' /*50*/, '@' /*64*/));
		put(51 /* 3 */, new CodePair('3' /*51*/, '#' /*35*/));
		put(52 /* 4 */, new CodePair('4' /*52*/, '$' /*36*/));
		put(53 /* 5 */, new CodePair('5' /*53*/, '%' /*37*/));
		put(54 /* 6 */, new CodePair('6' /*54*/, '^' /*94*/));
		put(55 /* 7 */, new CodePair('7' /*55*/, '&' /*38*/));
		put(56 /* 8 */, new CodePair('8' /*56*/, '*' /*42*/));
		put(57 /* 9 */, new CodePair('9' /*57*/, '(' /*40*/));
		put(48 /* 0 */, new CodePair('0' /*48*/, ')' /*41*/));
		put(45 /* Minus */, new CodePair('-' /*45*/, '_' /*95*/));
		put(61 /* Equals */, new CodePair('=' /*61*/, '+' /*43*/));
		put(92 /* Back Slash */, new CodePair('\\' /*92*/, '|' /*124*/));

		put(81 /* Q */, new CodePair('q' /*113*/, 'Q' /*81*/));
		put(87 /* W */, new CodePair('w' /*119*/, 'W' /*87*/));
		put(69 /* E */, new CodePair('e' /*101*/, 'E' /*69*/));
		put(82 /* R */, new CodePair('r' /*114*/, 'R' /*82*/));
		put(84 /* T */, new CodePair('t' /*116*/, 'T' /*84*/));
		put(89 /* Y */, new CodePair('y' /*121*/, 'Y' /*89*/));
		put(85 /* U */, new CodePair('u' /*117*/, 'U' /*85*/));
		put(73 /* I */, new CodePair('i' /*105*/, 'I' /*73*/));
		put(79 /* O */, new CodePair('o' /*111*/, 'O' /*79*/));
		put(80 /* P */, new CodePair('p' /*112*/, 'P' /*80*/));
		put(91 /* Open Bracket */, new CodePair('[' /*91*/, '{' /*123*/));
		put(93 /* Close Bracket */, new CodePair(']' /*93*/, '}' /*125*/));

		put(65 /* A */, new CodePair('a' /*97*/, 'A' /*65*/));
		put(83 /* S */, new CodePair('s' /*115*/, 'S' /*83*/));
		put(68 /* D */, new CodePair('d' /*100*/, 'D' /*68*/));
		put(70 /* F */, new CodePair('f' /*102*/, 'F' /*70*/));
		put(71 /* G */, new CodePair('g' /*103*/, 'G' /*71*/));
		put(72 /* H */, new CodePair('h' /*104*/, 'H' /*72*/));
		put(74 /* J */, new CodePair('j' /*106*/, 'J' /*74*/));
		put(75 /* K */, new CodePair('k' /*107*/, 'K' /*75*/));
		put(76 /* L */, new CodePair('l' /*108*/, 'L' /*76*/));
		put(59 /* Semicolon */, new CodePair(';' /*59*/, ':' /*58*/));
		put(222 /* Quote */, new CodePair('\'' /*39*/, '"' /*34*/));

		put(90 /* Z */, new CodePair('z' /*122*/, 'Z' /*90*/));
		put(88 /* X */, new CodePair('x' /*120*/, 'X' /*88*/));
		put(67 /* C */, new CodePair('c' /*99*/, 'C' /*67*/));
		put(86 /* V */, new CodePair('v' /*118*/, 'V' /*86*/));
		put(66 /* B */, new CodePair('b' /*98*/, 'B' /*66*/));
		put(78 /* N */, new CodePair('n' /*110*/, 'N' /*78*/));
		put(77 /* M */, new CodePair('m' /*109*/, 'M' /*77*/));
		put(44 /* Comma */, new CodePair(',' /*44*/, '<' /*60*/));
		put(46 /* Period */, new CodePair('.' /*46*/, '>' /*62*/));
		put(47 /* Slash */, new CodePair('/' /*47*/, '?' /*63*/));

//		put(60 /* Less */, new CodePair('<' /*60*/, '>')); // 105-th key on 105-keys keyboard (less/greather/bar)
		put(KeyEvent.VK_LESS /* Less */, new CodePair('<' /*60*/, '>')); // 105-th key on 105-keys keyboard (less/greather/bar)
//		put(KeyEvent.VK_GREATER /* Greater */, new CodePair('<' /*60*/, '>')); // 105-th key on 105-keys keyboard (less/greather/bar)
	}};

	private static boolean canCheckCapsWithToolkit;

	public KeyboardConvertor() {
		try {
			Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
			canCheckCapsWithToolkit = true;
		} catch (Exception e) {
			canCheckCapsWithToolkit = false;
		}
		if (isWindows) {
			patternForScancode = Pattern.compile(PATTERN_STRING_FOR_SCANCODE);
		}
	}

	public int convert(int keyChar, KeyEvent ev) {
		int keyCode = ev.getKeyCode();
		boolean isShiftDown = ev.isShiftDown();
		CodePair codePair = keyMap.get(keyCode);
		if (null == codePair)
			return keyChar;
		if (isWindows) {
			final Matcher matcher = patternForScancode.matcher(ev.paramString());
			if (matcher.matches()) {
				try {
				int scancode = Integer.parseInt(matcher.group(1));
				if (90 == keyCode && 21 == scancode) { // deutsch z->y
					codePair = keyMap.get(89); // y
				} else if (89 == keyCode && 44 == scancode) { // deutsch y->z
					codePair = keyMap.get(90); // z
				}
				} catch (NumberFormatException e) { /*nop*/ };
			}
		}
		boolean isCapsLock = false;
		if (Character.isLetter(codePair.code)) {
			if (canCheckCapsWithToolkit) {
				try {
					isCapsLock =
						Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
				} catch (Exception ex) { /* nop */ }
			}
		}
		return isShiftDown && ! isCapsLock || ! isShiftDown && isCapsLock ?
				codePair.codeShifted :
				codePair.code;
	}

	private static class CodePair {
		public final int code;
        public final int codeShifted;
		public CodePair(int code, int codeShifted) {
			this.code = code;
			this.codeShifted = codeShifted;
		}
	}

}
