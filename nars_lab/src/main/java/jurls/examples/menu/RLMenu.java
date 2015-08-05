/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorsten2
 */
public class RLMenu extends JMenu {

    protected class MyAction extends AbstractAction {

        public MyAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            notifyListeners();
        }

    }

    private final List<ActionListener> listeners = new ArrayList<>();

    public RLMenu(String s) {
        super(s);
    }

    @Override
    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void notifyListeners() {
        for (ActionListener l : listeners) {
            l.actionPerformed(null);
        }
    }

}
