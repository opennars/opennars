/*
  Part of the GUI for Processing library 
  	http://www.lagers.org.uk/g4p/index.html
	http://gui4processing.googlecode.com/svn/trunk/

  Copyright (c) 2008-09 Peter Lager

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

import java.awt.font.TextAttribute;

import javax.swing.JOptionPane;

/**
 * 
 * These constants can all be available to the Processor.
 * 
 * @author Peter Lager
 *
 */
public interface GConstants {

	int RED_SCHEME 		= 0;
	int GREEN_SCHEME 	= 1;
	int YELLOW_SCHEME	= 2;
	int PURPLE_SCHEME	= 3;
	int ORANGE_SCHEME 	= 4;
	int CYAN_SCHEME 	= 5;
	int BLUE_SCHEME 	= 6;
	int GOLD_SCHEME 	= 7;	
	int SCHEME_8		= 8;
	int SCHEME_9		= 9;
	int SCHEME_10		= 10;
	int SCHEME_11		= 11;
	int SCHEME_12		= 12;
	int SCHEME_13		= 13;
	int SCHEME_14		= 14;
	int SCHEME_15		= 15;

	// Keyboard values not covered by Processing
	char HOME		= java.awt.event.KeyEvent.VK_HOME;
	char END		= java.awt.event.KeyEvent.VK_END;
		

	// Configuration constants
	// GRoundControl
	int CTRL_ANGULAR 		= 0x00000501;
	int CTRL_HORIZONTAL 	= 0x00000502;
	int CTRL_VERTICAL 		= 0x00000503;

	// GWindow
	int EXIT_APP 			= 0x00000f01;
	int CLOSE_WINDOW 		= 0x00000f02;
	int KEEP_OPEN 			= 0x00000f03;

	// ### GUI build constants ###
	int USER_COL_SCHEME 	= 0x00010102;

	// The min alpha level for a control to respond to mouse and keyboard
	int ALPHA_BLOCK					= 128;
	// The min alpha before a pixel is considered for a hot spot
	int ALPHA_PICK					= 48;

	// ### Scroll bar policy constants ###
	/** Do not create or display any scrollbars for the text control. */
	int SCROLLBARS_NONE 			= 0x0000;
	/** Create and display vertical scrollbar only. */
	int SCROLLBARS_VERTICAL_ONLY 	= 0x0001;
	/** Create and display horizontal scrollbar only. */
	int SCROLLBARS_HORIZONTAL_ONLY 	= 0x0002;
	/** Create and display both vertical and horizontal scrollbars. */
	int SCROLLBARS_BOTH 			= 0x0003;
	/** whether to hide when not required */
	int SCROLLBARS_AUTOHIDE 		= 0x1000;

	// Slider / numeric display types
	int INTEGER 		= 0;
	int DECIMAL 		= 1;
	int EXPONENT 		= 2;

	// Text orientation for sliders
	int ORIENT_LEFT 	= -1;
	int ORIENT_TRACK 	=  0;
	int ORIENT_RIGHT	=  1;
	
	// Stick mode
	int X4 				= 1;
	int X8 				= 2;

	// Modal dialog messages
	// Message types
	int PLAIN 			= JOptionPane.PLAIN_MESSAGE;
	int ERROR			= JOptionPane.ERROR_MESSAGE;
	int INFO			= JOptionPane.INFORMATION_MESSAGE;
	int WARNING			= JOptionPane.WARNING_MESSAGE;
	int QUERY			= JOptionPane.QUESTION_MESSAGE;
	
	// Option types
	int YES_NO			= JOptionPane.YES_NO_OPTION;
	int YES_NO_CANCEL	= JOptionPane.YES_NO_CANCEL_OPTION;
	int OK_CANCEL		= JOptionPane.OK_CANCEL_OPTION;
	
	// Replies to option types
	int OK				= JOptionPane.OK_OPTION;
	int YES				= JOptionPane.YES_OPTION;		// Has same int value as OK
	int NO				= JOptionPane.NO_OPTION;
	int CANCEL			= JOptionPane.CANCEL_OPTION;
	int CLOSED			= JOptionPane.CLOSED_OPTION;	
	
	// Attribute:- fontface   Value Type:- String font family name e.g. "Times New Roman"
	TextAttribute FAMILY = TextAttribute.FAMILY;

	// Attribute:- font weight   Value Type:- Float in range (0.5 to 2.75)
	TextAttribute WEIGHT = TextAttribute.WEIGHT;
	// Predefined constants for font weight
	Float WEIGHT_EXTRA_LIGHT 	= new Float(0.5f);
	Float WEIGHT_LIGHT 			= new Float(0.75f);
	Float WEIGHT_DEMILIGHT 		= new Float(0.875f);
	Float WEIGHT_REGULAR 		= new Float(1.0f);
	Float WEIGHT_SEMIBOLD 		= new Float(1.25f);
	Float WEIGHT_MEDIUM 		= new Float(1.5f);
	Float WEIGHT_DEMIBOLD 		= new Float(1.75f);
	Float WEIGHT_BOLD 			= new Float(2.0f);
	Float WEIGHT_HEAVY 			= new Float(2.25f);
	Float WEIGHT_EXTRABOLD 		= new Float(2.5f);
	Float WEIGHT_ULTRABOLD 		= new Float(2.75f);

	// Attribute:- font width   Value Type:- Float in range (0.75 to 1.5)
	TextAttribute WIDTH 		= TextAttribute.WIDTH;
	// Predefined constants for font width
	Float WIDTH_CONDENSED 		= new Float(0.75f);
	Float WIDTH_SEMI_CONDENSED 	= new Float(0.875f);
	Float WIDTH_REGULAR 		= new Float(1.0f);
	Float WIDTH_SEMI_EXTENDED 	= new Float(1.25f);
	Float WIDTH_EXTENDED 		= new Float(1.5f);

	// Attribute:- font posture   Value Type:- Float in range (0.0 to 0.20)
	TextAttribute POSTURE 		= TextAttribute.POSTURE;
	// Predefined constants for font posture (plain or italic)
	Float POSTURE_REGULAR 		= new Float(0.0f);
	Float POSTURE_OBLIQUE 		= new Float(0.20f);

	// Attribute:- font size   Value Type:- Float
	TextAttribute SIZE 			= TextAttribute.SIZE;

	// Attribute:- font superscript   Value Type:- Integer (1 : super or -1 subscript)
	TextAttribute SUPERSCRIPT 	= TextAttribute.SUPERSCRIPT;
	// Predefined constants for font super/subscript
	Integer SUPERSCRIPT_SUPER 	= new Integer(1);
	Integer SUPERSCRIPT_SUB 	= new Integer(-1);
	Integer SUPERSCRIPT_OFF 	= new Integer(0);

	// Attribute:- font foreground snd bsckground colour   Value Type:- Color
	TextAttribute FOREGROUND 	= TextAttribute.FOREGROUND;
	TextAttribute BACKGROUND 	= TextAttribute.BACKGROUND;

	// Attribute:- font strike through   Value:- Boolean
	TextAttribute STRIKETHROUGH = TextAttribute.STRIKETHROUGH;
	// Predefined constants for font strike through on/off
	Boolean STRIKETHROUGH_ON 	= new Boolean(true);
	Boolean STRIKETHROUGH_OFF 	= new Boolean(false);

	//	TextAttribute JUSTIFICATION = TextAttribute.JUSTIFICATION;
	//	Float JUSTIFICATION_FULL = new Float(1.0f);
	//	Float JUSTIFICATION_NONE = new Float(0.0f);
	
}
