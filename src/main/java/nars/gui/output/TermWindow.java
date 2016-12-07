///*
// * TermWindow.java
// *
// * Copyright (C) 2008  Pei Wang
// *
// * This file is part of Open-NARS.
// *
// * Open-NARS is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2 of the License, or
// * (at your option) any later version.
// *
// * Open-NARS is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.gui.output;
//
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextField;
//import nars.entity.Concept;
//import nars.entity.EntityObserver;
//import nars.gui.NWindow;
//import nars.core.Memory;
//
///**
// * JWindow accept a Term, then display the content of the corresponding Concept
// */
//public class TermWindow extends NWindow implements ActionListener {
//
//    /**
//     * Display label
//     */
//    private final JLabel termLabel;
//    /**
//     * Input field for term name
//     */
//    private final JTextField termField;
//    /**
//     * Control buttons
//     */
//    private final JButton playButton, hideButton;
//    
//    /**
//     * Reference to the memory
//     */
//    private final Memory memory;
//
//    /**
//     * Constructor
//     */
//    public TermWindow(Memory memory) {
//        super("Term Window");
//        this.memory = memory;
//        
//        GridBagLayout gridbag = new GridBagLayout();
//        GridBagConstraints c = new GridBagConstraints();
//        setLayout(gridbag);
//
//        c.ipadx = 3;
//        c.ipady = 3;
//        c.insets = new Insets(5, 5, 5, 5);
//        c.fill = GridBagConstraints.BOTH;
//        c.gridwidth = 1;
//        c.weightx = 0.0;
//        c.weighty = 0.0;
//        termLabel = new JLabel("Term:", JLabel.RIGHT);
//        gridbag.setConstraints(termLabel, c);
//        add(termLabel);
//
//        c.weightx = 1.0;
//        termField = new JTextField("");
//        JScrollPane scrollPane = new JScrollPane(termField);
//        gridbag.setConstraints(scrollPane, c);
//        add(scrollPane);
//
//        c.weightx = 0.0;
//        playButton = new JButton("Show");
//        playButton.addActionListener(this);
//        gridbag.setConstraints(playButton, c);
//        add(playButton);
//
//        hideButton = new JButton("Hide");
//        hideButton.addActionListener(this);
//        gridbag.setConstraints(hideButton, c);
//        add(hideButton);
//
//        setBounds(600, 0, 600, 100);
//    }
//
//    /**
//     * Handling button click
//     *
//     * @param e The ActionEvent
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        JButton b = (JButton) e.getSource();
//        if (b == playButton) {
//            Concept concept = memory.concept(termField.getText().trim());
//            if (concept != null) {
//                EntityObserver entityObserver = new ConceptWindow(concept);
//                concept.startPlay(entityObserver, true);
//            }
//        } else if (b == hideButton) {
//            close();
//        }
//    }
//
//    @Override
//    protected void close() {
//        setVisible(false);
//    }
//
//
//}
