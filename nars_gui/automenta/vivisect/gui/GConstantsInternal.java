/*
  Part of the GUI for Processing library 
  	http://www.lagers.org.uk/g4p/index.html
	http://gui4processing.googlecode.com/svn/trunk/

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

import java.awt.BasicStroke;

/**
 * Constants that are used internally by the library.
 * 
 * @author Peter Lager
 *
 */
interface GConstantsInternal {
	
	// Constants for GCustomSlider styles
	String SLIDER_STYLES = "|grey_blue|blue18px|green_red20px|purple18px|red_yellow18px|";
	String DEFAULT_SLIDER_STYLE = "grey_blue";

	// Constants for the control methods
	int DRAW_METHOD 			= 0x00000001;
	int MOUSE_METHOD 			= 0x00000002;
	int PRE_METHOD 				= 0x00000004;
	int KEY_METHOD 				= 0x00000008;
	int POST_METHOD 			= 0x00000010;
	int ALL_METHOD 				= 0x0000001f;
	int GROUP_CONTROL_METHOD 	= DRAW_METHOD | MOUSE_METHOD | KEY_METHOD;

	// Event method handler errors
	int MISSING 			= 0xff000001;	// Can't find standard handler
	int NONEXISTANT			= 0xff000002;
	int INVALID_TYPE		= 0xff000003;
	int INVALID_PAPPLET		= 0xff000004;
	int EXCP_IN_HANDLER 	= 0xff000005;	// Exception in event handler

	// Button/slider status values
	int OFF_CONTROL			= 0;
	int OVER_CONTROL		= 1;
	int PRESS_CONTROL		= 2;
	int DRAG_CONTROL		= 3;

	// The tint color used when controls are drawn with transparency
	int TINT_FOR_ALPHA =	255;
	
	// Constants for merging attribute runs
	int I_NONE 		= 0;
	int I_TL 		= 1;
	int I_TR		= 2;
	int I_CL		= 4;
	int I_CR		= 8;
	int I_INSIDE	= 16;
	int I_COVERED	= 32;
	int I_MODES		= 63;

	// Merger action
	int MERGE_RUNS		= 256;
	int CLIP_RUN		= 512;
	int COMBI_MODES		= 768;

	// merger decision grid
	int[][] grid = new int[][] {
			{ I_NONE,	I_TL,		I_CL,		I_COVERED,	I_COVERED },
			{ I_NONE,	I_NONE, 	I_INSIDE, 	I_INSIDE, 	I_COVERED },
			{ I_NONE,	I_NONE, 	I_INSIDE, 	I_INSIDE, 	I_CR },
			{ I_NONE,	I_NONE, 	I_NONE, 	I_NONE, 	I_TR },
			{ I_NONE,	I_NONE, 	I_NONE, 	I_NONE, 	I_NONE }
	};

	// Basic strokes needed when using the Graphics2D object for drawing on the buffer
	BasicStroke pen_1_0 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	BasicStroke pen_2_0 = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	BasicStroke pen_3_0 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	BasicStroke pen_4_0 = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	// Padding for text controls
	int TPAD2	= 2;
	int TPAD6 = 6;

	int TPAD4	= 4;
	int TPAD8	= 8;

	// ### Scroll bar type constants ###
	int SCROLLBAR_VERTICAL 		= GConstants.SCROLLBARS_VERTICAL_ONLY; 		// 1
	int SCROLLBAR_HORIZONTAL 	= GConstants.SCROLLBARS_HORIZONTAL_ONLY;	// 2

	float HORZ_SCROLL_RATE 		= 4f;
	float VERT_SCROLL_RATE 		= 8;

	char EOL	= '\n';

}
