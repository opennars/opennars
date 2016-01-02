package nars.gui.output;

import nars.Video;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;



/**
 * @author http://stackoverflow.com/questions/19766/how-do-i-make-a-list-with-checkboxes-in-java-swing
 */
public class JCategoryList extends JList<JButton> {

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    /*addMouseListener(new MouseAdapter() {
    public void mousePressed(MouseEvent e) {
    int index = locationToIndex(e.getPoint());
    if (index != -1) {
    JButton checkbox = (JButton)getModel().getElementAt(index);
    repaint();
    }
    }
    });*/
    public JCategoryList() {
        setCellRenderer(new CellRenderer());
        /*addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
        JButton checkbox = (JButton)getModel().getElementAt(index);
        repaint();
        }
        }
        });*/
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public JCategoryList(ListModel<JButton> model) {
        this();
        setModel(model);
    }

    protected static class CellRenderer implements ListCellRenderer<JButton> {

        Font f = Video.fontMono(16.0f);
        private JButton lastCellFocus;

        @Override
        public Component getListCellRendererComponent(JList<? extends JButton> list, JButton value, int index, boolean isSelected, boolean cellHasFocus) {
            JButton b = value;
            b.setContentAreaFilled(false);
            b.setHorizontalTextPosition(JButton.LEFT);
            b.setHorizontalAlignment(JButton.LEFT);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setFont(f);
            if ((cellHasFocus) && (lastCellFocus != b)) {
                b.doClick();
                lastCellFocus = b;
            }
            return b;
        }
    }
}
