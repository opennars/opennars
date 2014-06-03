/*
 * ParameterWindow.java
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Window displaying a system parameter that can be adjusted in run time
 */
public class ParameterWindow extends NarsFrame implements ActionListener, AdjustmentListener {

    /** Display label */
    private Label valueLabel;
    /** Control buttons */
    private Button hideButton, undoButton, defaultButton;
    /** Adjusting bar */
    private Scrollbar valueBar;
    /** parameter values */
    private int defaultValue, previousValue; // , currentValue;
    AtomicInteger currentValue;
    
    /**
     * Constructor
     * @param title Parameter name
     * @param dft The default value of the parameter
     * @param value  
     */
    ParameterWindow(String title, int dft, AtomicInteger currentValue ) {
        super(title);
        defaultValue = dft;
        this.currentValue = currentValue;
        
        previousValue = dft;
        currentValue.set( dft );
        setLayout(new GridLayout(3, 3, 8, 4));
        setBackground(SINGLE_WINDOW_COLOR);
        Label sp1 = new Label("");
        sp1.setBackground(SINGLE_WINDOW_COLOR);
        add(sp1);
        valueLabel = new Label(String.valueOf(dft), Label.CENTER);
        valueLabel.setBackground(SINGLE_WINDOW_COLOR);
        add(valueLabel);
        Label sp2 = new Label("");
        sp2.setBackground(SINGLE_WINDOW_COLOR);
        add(sp2);
        add(new Label("0", Label.RIGHT));
        valueBar = new Scrollbar(Scrollbar.HORIZONTAL, dft, 0, 0, 101);
        valueBar.addAdjustmentListener(this);
        add(valueBar);
        add(new Label("100", Label.LEFT));
        undoButton = new Button("Undo");
        undoButton.addActionListener(this);
        add(undoButton);
        defaultButton = new Button("Default");
        defaultButton.addActionListener(this);
        add(defaultButton);
        hideButton = new Button("Hide");
        hideButton.addActionListener(this);
        add(hideButton);
        this.setBounds(300, 300, 250, 120);
    }

    /**
     * Get the value of the parameter
     * @return The current value
     */
    public int value() {
        return currentValue.get();
    }

    /**
     * Handling button click
     * @param e The ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == defaultButton) {
            currentValue.set( defaultValue );
            valueBar.setValue(currentValue.get() );
            valueLabel.setText(String.valueOf(currentValue));
        } else if (s == undoButton) {
            currentValue.set( previousValue );
            valueBar.setValue(currentValue.get() );
            valueLabel.setText(String.valueOf(currentValue));
        } else if (s == hideButton) {
            close();
        }
    }

    private void close() {
        previousValue = currentValue.get();
        setVisible(false);
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        close();
    }

    /**
     * Handling scrollbar movement
     * @param e The AdjustmentEvent
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == valueBar) {
            int v = valueBar.getValue();
            valueLabel.setText(String.valueOf(v));
            valueBar.setValue(v);
            currentValue.set( v );
        }
    }
}
