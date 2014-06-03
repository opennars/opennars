/*
 * InputWindow.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.gui;

import java.awt.Button;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import nars.io.ExperienceReader;
import nars.io.InputChannel;
import nars.main_nogui.ReasonerBatch;

/**
 * Input window, accepting user tasks
 */
public class InputWindow extends NarsFrame implements ActionListener, InputChannel {
    private ReasonerBatch reasoner;
    /** Control buttons */
    private Button okButton, holdButton, clearButton, closeButton;
    /** Input area */
    private TextArea inputText;
    /** Whether the window is ready to accept new input (in fact whether the Reasoner will read the content of {@link #inputText} ) */
    private boolean ready;
    /** number of cycles between experience lines */
    private int timer;

    /**
     * Constructor
     * @param reasoner The reasoner
     * @param title The title of the window
     */
    public InputWindow(ReasonerBatch reasoner, String title) {
        super(title + " - Input Window");
        setBackground(SINGLE_WINDOW_COLOR);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.ipadx = 3;
        c.ipady = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        inputText = new TextArea("");
        gridbag.setConstraints(inputText, c);
        add(inputText);
        c.weighty = 0.0;
        c.gridwidth = 1;
        okButton = new Button("OK");
        okButton.addActionListener(this);
        gridbag.setConstraints(okButton, c);
        add(okButton);
        holdButton = new Button("Hold");
        holdButton.addActionListener(this);
        gridbag.setConstraints(holdButton, c);
        add(holdButton);
        clearButton = new Button("Clear");
        clearButton.addActionListener(this);
        gridbag.setConstraints(clearButton, c);
        add(clearButton);
        closeButton = new Button("Hide");
        closeButton.addActionListener(this);
        gridbag.setConstraints(closeButton, c);
        add(closeButton);
        setBounds(0, 40, 400, 210);
        setVisible(true);

        this.reasoner = reasoner;
    }

    /**
     * Initialize the window
     */
    public void init() {
        ready = false;
        inputText.setText("");
    }

    /**
     * Handling button click
     * @param e The ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        Button b = (Button) e.getSource();
        if (b == okButton) {
            ready = true;
        } else if (b == holdButton) {
            ready = false;
        } else if (b == clearButton) {
            inputText.setText("");
        } else if (b == closeButton) {
            close();
        }
    }

    private void close() {
        setVisible(false);
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        close();
    }

    /**
     * Accept text input in a tick, which can be multiple lines
     * TODO duplicated code with {@link ExperienceReader#nextInput()}
     * @return Whether to check this channel again
     */
    public boolean nextInput() {
        if (timer > 0) {  // wait until the timer
            timer--;
            return true;
        }
        if (!ready) {
            return false;
        }
        String text = inputText.getText().trim();
        String line;    // The next line of text
        int endOfLine;
        // The process steps at a number or no more text
        while ((text.length() > 0) && (timer == 0)) {
        	endOfLine = text.indexOf('\n');
        	if (endOfLine < 0) {	// this code is reached at end of text
        		line = text;
        		text = "";
        	} else {	// this code is reached for ordinary lines
        		line = text.substring(0, endOfLine).trim();
        		text = text.substring(endOfLine + 1);	// text becomes rest of text
        	}

        	try { 	// read NARS language or an integer
        		timer = Integer.parseInt(line);
        		reasoner.walk(timer);
        	} catch (NumberFormatException e) {
        		try {
					reasoner.textInputLine(line);
				} catch (NullPointerException e1) {
					System.out.println("InputWindow.nextInput() - NullPointerException: please correct the input" );
					ready = false;
					return false;
				}
        	}
        	inputText.setText(text);	// update input Text widget to rest of text
        	if (text.isEmpty()) {
        		ready = false;
        	}
        }
        return ((text.length() > 0) || (timer > 0));
    }
}
