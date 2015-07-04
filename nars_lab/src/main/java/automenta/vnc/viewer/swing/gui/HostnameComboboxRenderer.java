package automenta.vnc.viewer.swing.gui;

import automenta.vnc.viewer.swing.ConnectionParams;

import javax.swing.*;
import java.awt.*;

/**
 * @author dime at tightvnc.com
 */
public class HostnameComboboxRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String stringValue = renderListItem((ConnectionParams)value);
        setText(stringValue);
        setFont(getFont().deriveFont(Font.PLAIN));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }

    public String renderListItem(ConnectionParams cp) {
        String s = "<html><b>" +cp.hostName + "</b>:" + cp.getPortNumber();
        if (cp.useSsh()) {
            s += " <i>(via ssh://" + cp.sshUserName + '@' + cp.sshHostName + ':' + cp.getSshPortNumber() + ")</i>";
        }
        return s + "</html>";
    }
}
