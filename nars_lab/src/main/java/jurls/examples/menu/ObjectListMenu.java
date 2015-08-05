/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.menu;

import javax.swing.*;

/**
 *
 * @author thorsten2
 */
public class ObjectListMenu extends RLMenu {

    private final Object[] xs;
    private final JRadioButtonMenuItem[] buttons;

    public ObjectListMenu(String text, int selectedIndex, Object... xs) {
        super(text);
        this.xs = xs;

        buttons = new JRadioButtonMenuItem[xs.length];
        ButtonGroup bg = new ButtonGroup();
        for (int i = 0; i < xs.length; ++i) {
            buttons[i] = new JRadioButtonMenuItem(new MyAction(xs[i].toString()));
            bg.add(buttons[i]);
            add(buttons[i]);
        }

        buttons[selectedIndex].setSelected(true);
    }

    public Object getObject() {
        for (int i = 0; i < buttons.length; ++i) {
            if (buttons[i].isSelected()) {
                return xs[i];
            }
        }
        return null;
    }
}
