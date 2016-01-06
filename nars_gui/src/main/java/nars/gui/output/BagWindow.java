package nars.gui.output;

///*
// * BagWindow.java
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
//import java.awt.BorderLayout;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.Scrollbar;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.AdjustmentEvent;
//import java.awt.event.AdjustmentListener;
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollBar;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
//import nars.core.Parameters;
//import nars.logic.entity.Item;
//import nars.gui.NWindow;
//import nars.storage.AbstractBag;
//import Bag;
//import nars.storage.BagObserver;
//
///**
// * JWindow display the priority distribution of items within a given bag
// */
//@Deprecated public class BagWindow<E extends Item> extends NWindow implements ActionListener, AdjustmentListener,
//        BagObserver<E> {
//
//    /**
//     * The bag to be displayed
//     */
//    private Bag<?> bag;
//    /**
//     * Control buttons
//     */
//    private JButton playButton, stopButton, closeButton;
//    /**
//     * Display area
//     */
//    private JTextArea text;
//    /**
//     * Display label
//     */
//    private JLabel valueLabel;
//    /**
//     * Adjustable display level
//     */
//    private JScrollBar valueBar;
//    /**
//     * The location of the display area, shifted according to the number of
//     * windows opened
//     */
//    private static int counter;
//    /**
//     * whether this bag window is active
//     */
//    private boolean showing;
//
//    public BagWindow() {
//        super("Concepts");
//
//    }
//
//    private void applyBorderLayout(JScrollPane textScrollPane) {
//        setLayout(new BorderLayout());
//        add(textScrollPane, BorderLayout.CENTER);
//        JPanel bottomPanel = new JPanel();
//        add(bottomPanel, BorderLayout.SOUTH);
//        bottomPanel.add(valueLabel);
//        bottomPanel.add(valueBar);
//        bottomPanel.add(playButton);
//        bottomPanel.add(stopButton);
//        bottomPanel.add(closeButton);
//    }
//
//    private void adjustLabelAndCursor(int showLevel) {
//        String valueText = String.valueOf(showLevel);
//        // always occupy 3 characters (padding):
//        valueText = showLevel > 9 ? "0" + valueText : "00" + valueText;
//        valueText = showLevel > 99 ? "" + showLevel : valueText;
//        valueLabel.setText(valueText);
//        valueBar.setValue(showLevel);
//    }
//
//    @Override
//    public void post(String str) {
//        showing = true;
//        text.setText(str);
//    }
//
//    /**
//     * Handling button click
//     *
//     * @param e The ActionEvent
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        Object source = e.getSource();
////        if (source == playButton) {
////            bag.play();
////        } else if (source == stopButton) {
////            bag.stop();
////        } else if (source == closeButton) {
////            close();
////        }
//    }
//
//    /**
//     * Close the window
//     */
//    @Override
//    protected void close() {
////        bag.stop();
//        dispose();
//        counter--;
//    }
//
//    /**
//     * Handling scrollbar movement
//     *
//     * @param e The AdjustmentEvent
//     */
//    @Override
//    public void adjustmentValueChanged(AdjustmentEvent e) {
//        if (e.getSource() == valueBar) {
//            int showLevel = valueBar.getValue();
//            adjustLabelAndCursor(showLevel);
////            bag.setShowLevel(showLevel);
////            bag.play();
//        }
//    }
//
//    @Override
//    public void setBag(AbstractBag<E> _bag) {
//        //TODO support AbstractBag
//        if (!(bag instanceof Bag)) {
//            return;
//        }
//        
//        
//        this.bag = bag;
//
//        removeAll();
//        /* The lowest level displayed -- will need adjusted for variable bag sizes*/
//        int showLevel = (int) (Parameters.BAG_THRESHOLD * bag.levels);
//
//        text = new JTextArea("");
//        text.setEditable(false);
//        JScrollPane textScrollPane = new JScrollPane(text);
//        valueLabel = new JLabel("00", JLabel.RIGHT);
//        valueBar = new JScrollBar(Scrollbar.HORIZONTAL, showLevel, 0, 1, bag.levels);
//        valueBar.addAdjustmentListener(this);
//        stopButton = new JButton(NWindow.OFF_LABEL);
//        stopButton.addActionListener(this);
//        playButton = new JButton(NWindow.ON_LABEL);
//        playButton.addActionListener(this);
//        closeButton = new JButton("Close");
//        closeButton.addActionListener(this);
//
//        applyBorderLayout(textScrollPane);
//
//        setBounds(600, 60 + counter * 40, 600, 300);
//        counter++;
//        adjustLabelAndCursor(showLevel);
//        setVisible(true);
//
//    }
//
//    @Override
//    public void refresh(String message) {
//        if (showing) {
//            post(message);
//        }
//    }
//
//    @Override
//    public void stop() {
//        showing = false;
//    }
//
//    @SuppressWarnings("unused")
//    private void applyGridBagLayout(JScrollPane textScrollPane) {
//        GridBagLayout gridbag = new GridBagLayout();
//        GridBagConstraints c = new GridBagConstraints();
//        setLayout(gridbag);
//        c.ipadx = 3;
//        c.ipady = 3;
//        c.insets = new Insets(5, 5, 5, 5);
//        c.fill = GridBagConstraints.BOTH;
//        c.gridwidth = GridBagConstraints.REMAINDER;
//        c.weightx = 1.0;
//        c.weighty = 1.0;
//        gridbag.setConstraints(textScrollPane, c);
//        add(textScrollPane);
//        c.weighty = 0.0;
//        c.gridwidth = 1;
//        gridbag.setConstraints(valueLabel, c);
//        add(valueLabel);
//        gridbag.setConstraints(valueBar, c);
//        add(valueBar);
//        gridbag.setConstraints(playButton, c);
//        add(playButton);
//        gridbag.setConstraints(stopButton, c);
//        add(stopButton);
//        gridbag.setConstraints(closeButton, c);
//        add(closeButton);
//    }
// }
