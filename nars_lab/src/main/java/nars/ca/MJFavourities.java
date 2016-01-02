package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// Favourite patterns list

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

//import java.io.*;

public class MJFavourities extends Dialog implements ActionListener {
	private final Button btnLoad;
    private final Button btnCcl;
	private final Label lblPrompt;
	private final List LstFiles = new List(10, false);
	private final MJCellUI mjUI;

	// ----------------------------------------------------------------
	// Constructor
	MJFavourities(Frame frame, MJCellUI mjui) {
		super(frame, "Favourite patterns", false);
		mjUI = mjui;
		setLayout(new BorderLayout());
		lblPrompt = new Label("Select the pattern:");
		add("North", lblPrompt);
		add("Center", LstFiles);

		Panel pnlButtons = new Panel();
		pnlButtons.setLayout(new FlowLayout());
		pnlButtons.add(btnLoad = new Button(" Load "));
		btnLoad.addActionListener(this);
		pnlButtons.add(btnCcl = new Button("Cancel"));
		btnCcl.addActionListener(this);
		add("South", pnlButtons);

		Dimension d = getToolkit().getScreenSize();
		setLocation(d.width / 4, d.height / 3);
		setSize(d.width / 6, d.height / 3);
		setVisible(false);
		InitList();
		pack();
	}

	// ----------------------------------------------------------------
	// Fill the list with patterns from the current rule
	@SuppressWarnings("HardcodedFileSeparator")
	public void InitList() {
		MJTools mjT;
		Vector vLines;
		int i, iGame;

		LstFiles.clear(); // no items

		vLines = new Vector();
		mjT = new MJTools();
		if (mjT.LoadResTextFile("fav.txt", vLines)) {
			for (i = 0; i < vLines.size(); i++) {
				if (!((String) vLines.elementAt(i)).startsWith("//"))
					LstFiles.add((String) vLines.elementAt(i));
			}
		}
	}

	// ----------------------------------------------------------------
	// Load the currently selected pattern
	@SuppressWarnings("HardcodedFileSeparator")
	private void LoadCurrentPattern() {
		String sGameName = "";
		String sRuleName = "";
		String sPattName = "";

		if (LstFiles.getSelectedIndex() >= 0) {
			String sItem = LstFiles.getSelectedItem();
			int whereSlash = sItem.lastIndexOf('/');
			if (whereSlash > 0) {
				sPattName = sItem.substring(whereSlash + 1); // part after '/'

				sItem = sItem.substring(0, whereSlash); // part before '/'
				int whereSep = sItem.lastIndexOf('|');
				if (whereSep > 0) {
					sGameName = sItem.substring(0, whereSep); // part before '|'
					sRuleName = sItem.substring(whereSep + 1); // part after '|'

					sGameName = mjUI.mjr.GetGameName(mjUI.mjr
							.GetGameIndex(sGameName));
					mjUI.ActivateGame(sGameName);
					mjUI.ActivateRule(sRuleName);
				}
			}

			if ((!sGameName.isEmpty()) && (!sRuleName.isEmpty())) {
				lblPrompt.setText("Please wait...");
				try {
					mjUI.mjo.OpenFile(sGameName + '/' + sRuleName + '/'
							+ sPattName);
					lblPrompt.setText("Select the pattern:");
				} catch (Exception exc) {
					lblPrompt.setText("Error loading pattern!");
				}
			}
		}
	}

	// ----------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnLoad) {
			LoadCurrentPattern();
		} else if (ae.getSource() == btnCcl) {
			setVisible(false);
		}
	}

	// ----------------------------------------------------------------
	@Override
	public boolean action(Event evt, Object arg) {
		if (evt.target.equals(LstFiles)) {
			LoadCurrentPattern();
		} else
			return super.action(evt, arg);
		return true;
	}

	// ----------------------------------------------------------------
	@Override
	public boolean handleEvent(Event evt) {
		if (evt.id == Event.WINDOW_DESTROY)
			setVisible(false);
		else
			return super.handleEvent(evt);
		return true;
	}
	// ----------------------------------------------------------------

}