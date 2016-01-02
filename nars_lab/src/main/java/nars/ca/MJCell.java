package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
// e-mail: info@mirekw.com

import java.applet.Applet;
import java.awt.*;

/*
 * 1.51, 23.02.2005
 *  - Source code corrections to make it compilable with Java 1.4
 * 
 * 1.50, 04.10.2000
 *  - New rules family "General binary"
 *  - New rules family "Margolus neighbourhood"
 * 
 * 1.25, 04.07.2000
 *  - Rules definitions were extracted from the compiled applet
 *    and are now specified in an external text file. It's no longer necessary to
 *    recompile the applet just in order to add a new rule.
 *  - New rules:
 *    Generations: Burst, BurstII, Circuit_Genesis, BelZhab Sediment,
 *    FlamingStarbows, Glisserati, Rake, Snake Cyclic CA: Cubism, LavaLamp,
 *    LowBrowFour Larger that Life: ModernArt Life: InverseLife, WalledCities
 *  - New patterns (> 400) for rules: Burst, BurstII, Rake, Snake.
 * 
 * 1.10, 26.05.2000
 *  - Possibility to run the applet also as a Java application
 *    (with Java.exe).
 *  - List of available patterns moved to an external text file.
 *  - Favourities dialog with selected most interesting patterns.
 * 
 * 1.20, 20.06.2000
 *  - New option under the "Animation" menu: Rewind.
 *  - New color palette "8 colors".
 *  - New options in "Color" menu:
 *    - set the active state,
 *    - activate the next state (shortcut '['),
 *    - activate the previous state (shortcut ']').
 *  - "Active state/states count" display in the left panel
 *  - New rule WireWorld, with many patterns.
 *  - New command-line parameters:
 *    ButtonLabel, ViewPanelControls, ViewPanelSeeding, Family, Rule, Pattern
 *  - User rules dialog got a '?' shortcut.
 * 
 * 1.02, 19.03.2000
 *  - Many new rules and patterns in 1D Totalistic, CyclicCA,
 *    Generations, Larger than Life, Neumann binary, and Weighted Life.
 * 
 * 1.01, 05.03.2000
 *  - Patterns file names changed to be compatible with Netscape.
 * 
 * 1.00, 25.02.2000
 *  - Library of patterns for all rules.
 *  - Descriptions of patterns.
 *  - Possibility to define own rules in all rule families.
 *  - Possibility to define any board size.
 *  - New rules family - 1D binary, with 38 rules.
 *  - New rules family - Larger than Life, with 6 rules.
 *  - New rules: Brain 6, Ebb&Flow II (Generations), Busy Brain (Rules tables),
 *    Emergence, Hex Inverse Fire, Jitters, Simple Inverse Fire, Simple Inverse,
 *    Simple, Starbursts (Weighted Life).
 *  - New rules family - User DLL.
 *  - New rules: Rug, Digital Inkblots, Hodges, GreenHast (User DLL).
 *  - Menu View - new option  "Refresh" [F5] That repaints the board.
 * 
 * 0.67, 20.11.1999
 *  - New rules: Sedimental (Generations), Conway--, Conway++,
 *    Conway+-1, Conway+-2, Fleas2, NoFleas2, Hexrule b2o, Starbursts2
 *    (Weighted Life)
 */

//-----------------------------------------------------------------------
public class MJCell extends Applet {
	private static final String VSN_NUM = "1.51";

	MJCellUI mjcUI;

	final Button btnStart = new Button();

	public String sBaseURL; // the URL we started from (has '/' at the end)

	// ----------------------------------------------------------------
	// Default constructor, some VMs require it
	public MJCell() {
    }

	// ----------------------------------------------------------------
	// The main initialization
	@Override
	@SuppressWarnings("HardcodedFileSeparator")
	public void init() {
		boolean isApplet = true; // applet or application?

		mjcUI = new MJCellUI(this); // the user interface class

		// detect the location we started from
		try {
			sBaseURL = getCodeBase().toString();
			if (sBaseURL.length() > 0 && sBaseURL.charAt(sBaseURL.length() - 1) == '.')
				sBaseURL = sBaseURL.substring(0, sBaseURL.length() - 1);
			if (!(sBaseURL.length() > 0 && sBaseURL.charAt(sBaseURL.length() - 1) == '/'))
				sBaseURL += "/";
			//System.out.println("Base: " + sBaseURL);
		} catch (Exception e) {
			isApplet = false; // no CodeBase, we run as an application
			sBaseURL = "./";
		}

		// interprete parameters
		if (isApplet) {
			// button
			String sBtnLabel = getParameter("ButtonLabel");
			if (sBtnLabel == null)
				sBtnLabel = "Start MJCell";
			btnStart.setLabel("  " + sBtnLabel + "  ");

			// background
			String sColor = getParameter("Background");
			if (sColor != null)
				setBackground(Color.decode(sColor));

			// family
			String sFamily = getParameter("Family");
			if (sFamily != null)
				mjcUI.sInitGame = sFamily;

			// rule
			String sRule = getParameter("Rule");
			if (sRule != null)
				mjcUI.sInitRule = sRule;

			// pattern
			String sPattern = getParameter("Pattern");
			if (sPattern != null)
				mjcUI.sInitPatt = sPattern;

			// UI items visibility
			String sInitPanelLeft = getParameter("ViewPanelControls");
			if (sInitPanelLeft != null)
				mjcUI.bInitPanelLeft = sInitPanelLeft.compareTo("1") == 0;

			String sInitPanelBotm = getParameter("ViewPanelSeeding");
			if (sInitPanelBotm != null)
				mjcUI.bInitPanelBotm = sInitPanelBotm.compareTo("1") == 0;
		} else {
			btnStart.setLabel("  Start MJCell  ");
		}

		add(btnStart);

		setSize(200, 100); // interpreted only when run from IDE
		resize(200, 100);
	}

	// ----------------------------------------------------------------
	//
	@Override
	public boolean action(Event e, Object arg) {
		if (e.target == btnStart) {
			mjcUI.build();
			mjcUI.show();
			mjcUI.Init();
		}
		return true;
	}

	// ----------------------------------------------------------------
	// allow this Applet to run as an application as well
	//static public void main(String args[])
	//{
	//  //System.out.println("main()");
	//  final MJCell theApplet = new MJCell();
	//  Frame frame = new Frame("MJCell");
	//  frame.setVisible(false);
	//  frame.setSize(200,100);
	//  theApplet.init();
	//  frame.add(theApplet);
	//  frame.validate();
	//  frame.setVisible(true);
	//  theApplet.start();
	//  frame.addWindowListener(new java.awt.event.WindowAdapter()
	//        {
	//          public void windowClosing(WindowEvent e)
	//          {
	//            theApplet.stop();
	//            theApplet.destroy();
	//            System.exit(0);
	//          } // end WindowClosing
	//        } // end anonymous class
	//      ); // end addWindowListener line
	//} // end main
	// ----------------------------------------------------------------
	public String getAppletVersion() {
		return VSN_NUM;
	}

	// ----------------------------------------------------------------
	public String getAppletName() {
		return "Mirek's Java Cellebration v." + getAppletVersion();
	}

	// ----------------------------------------------------------------
	@Override
	@SuppressWarnings("HardcodedFileSeparator")
	public String getAppletInfo() {
		return getAppletName() + "\n\n"
				+ "Copyright (C) Mirek Wojtowicz, 1999..2005\n"
				+ "e-mail: info@mirekw.com\n" + "http://www.mirekw.com\n\n"
				+ "Special thanks go to:\n"
				+ "John Elliott - for error reports and useful comments,\n"
				+ "Linda Bamer - for Macintosh tests.\n\n"
				+ "This applet is loosely based on 'Cellular' applet\n"
				+ "by K.S. Mueller & Thad Brown\n"
				+ "http://www.missouri.edu/~polsksm/";
	}
	// ----------------------------------------------------------------

}