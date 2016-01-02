package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// General-purpose single line input box

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputBox extends Dialog implements ActionListener {
	boolean isAccepted = false;
	final Button btnOk;
    final Button btnCcl;
	final TextField txtFld;

	InputBox(Frame frame, String sDeafult, String sTitle, String sPrompt) {
		super(frame, sTitle, true);
		setLayout(new BorderLayout());
		txtFld = new TextField(sDeafult);
		add("West", new Panel()); // some margins
		add("East", new Panel());
		add("North", new Label(sPrompt));
		add("Center", txtFld);

		Panel pnlButtons = new Panel();
		pnlButtons.setLayout(new FlowLayout());
		pnlButtons.add(btnOk = new Button("  Ok  "));
		btnOk.addActionListener(this);
		pnlButtons.add(btnCcl = new Button("Cancel"));
		btnCcl.addActionListener(this);
		add("South", pnlButtons);

		Dimension d = getToolkit().getScreenSize();
		setLocation(d.width / 4, d.height / 3);
		pack();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnOk) {
			isAccepted = true;
			setVisible(false);
		} else if (ae.getSource() == btnCcl) {
			setVisible(false);
		}
	}

	@Override
	public boolean handleEvent(Event evt) {
		if (evt.id == Event.WINDOW_DESTROY)
			dispose();
		else
			return super.handleEvent(evt);
		return true;
	}
}