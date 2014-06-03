/*
 * TermWindow.java
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

import java.awt.*;
import java.awt.event.*;

import nars.entity.Concept;
import nars.entity.EntityObserver;
//import nars.io.StringParser;
import nars.storage.Memory;

/**
 * Window accept a Term, then display the content of the corresponding Concept
 */
public class TermWindow extends NarsFrame implements ActionListener {

    /** Display label */
    private Label termLabel;
    /** Input field for term name */
    private TextField termField;
    /** Control buttons */
    private Button playButton, hideButton;
    /** Reference to the memory */
    private Memory memory;

    /**
     * Constructor
     */
    TermWindow(Memory memory) {
        super("Term Window");
        this.memory = memory;

        setBackground(SINGLE_WINDOW_COLOR);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        c.ipadx = 3;
        c.ipady = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        termLabel = new Label("Term:", Label.RIGHT);
        termLabel.setBackground(SINGLE_WINDOW_COLOR);
        gridbag.setConstraints(termLabel, c);
        add(termLabel);

        c.weightx = 1.0;
        termField = new TextField("");
        gridbag.setConstraints(termField, c);
        add(termField);

        c.weightx = 0.0;
        playButton = new Button("Show");
        playButton.addActionListener(this);
        gridbag.setConstraints(playButton, c);
        add(playButton);

        hideButton = new Button("Hide");
        hideButton.addActionListener(this);
        gridbag.setConstraints(hideButton, c);
        add(hideButton);

        setBounds(400, 0, 400, 100);
    }

    /**
     * Handling button click
     * @param e The ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        Button b = (Button) e.getSource();
        if (b == playButton) {
            Concept concept = memory.nameToConcept(termField.getText().trim());
            if (concept != null) {
                EntityObserver entityObserver = new ConceptWindow(concept);
				concept.startPlay(entityObserver , true);
            }
        } else if (b == hideButton) {
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
}
