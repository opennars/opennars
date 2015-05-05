/*
 * tuProlog - Copyright (C) 2001-2004  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog.gui.ide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

/**
 * An input field for the Java2 platform using Swing components.
 * 
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.0 - 29-nov-02
 */

public class JavaInputField
    extends JPanel
    implements InputField
{
    
    private static final long serialVersionUID = 1L;

    /**
	 * The input field used in the graphic interface.
	 */
    private JTextField inputField;
    /**
	 * A store for the history of the requested goals.
	 */
    private History history;

    private ConsoleManager console;

    
    public JavaInputField() {
        inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                inputFieldKeyReleased(event);
            }
        });
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                solve();
            }
        });
        
        JButton solveButton = new JButton();
        URL urlImage = ToolBar.getIcon("img/Solve18.png");
        solveButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        solveButton.setPreferredSize(new Dimension(18,18));
        solveButton.setToolTipText("Solve");
        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                solve();
            }
        });

        JButton solveAllButton = new JButton();
        urlImage = ToolBar.getIcon("img/SolveAll18.png");
        solveAllButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        solveAllButton.setPreferredSize(new Dimension(18,18));
        solveAllButton.setToolTipText("Solve All");
        solveAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                solveAll();
            }
        });


        history = new History();

        JPanel inputFieldPanel = new JPanel();
        JPanel buttonsPanel = new JPanel();
        setLayout(new BorderLayout());
        add(new JLabel("?- "), BorderLayout.WEST);
        add(inputFieldPanel,BorderLayout.CENTER);
        add(buttonsPanel,BorderLayout.EAST);
        inputFieldPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputFieldPanel.add(inputField, constraints);
        
        buttonsPanel.add(solveButton);
        buttonsPanel.add(solveAllButton);
    }

    /**
     * Listen to the keys pressed in the input field to provide an intuitive
     * mechanism for navigating through the history of the requested goals
     * using the up and down arrow keys.
     *
     * @param event The <code>java.awt.event.KeyEvent</code> occurred in the
     * input field.
     */
    private void inputFieldKeyReleased(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == 38) // up arrow
            inputField.setText(history.previous());
        else
            if (code == 40) // down arrow
                inputField.setText(history.next());
    }

    /**
	 * Since the solve() method must be placed in this class, I need a reference to the Console where output, solveInfo, tuProlog engine and the ProcessInput thread are placed. This behaviour will change as soon as there will be no need of separate input components for .NET and Java2, i.e. as soon as the AltGr bug in Thinlet, preventing the use of italian keycombo AltGr + '?' and AltGr + '+' to write '[' and ']', will be solved.
	 */
    public void setConsole(ConsoleManager consoleManager)
    {
        this.console = consoleManager;
    }

    /**
     * Solve the goal currently displayed in the input field.
     */
    public void solve()
    {
        if (getGoal().length()>0)//if the goal isn't empty
        {
            addGoalToHistory();
            console.setSolveType(0);
            console.solve();
        }
        else
            console.setStatusMessage("Ready.");
    }

    public void solveAll()
    {
        if (getGoal().length()>0)//if the goal isn't empty
        {
            addGoalToHistory();
            console.setSolveType(1);
            console.solveAll();
        }
        else
            console.setStatusMessage("Ready.");
    }

    public void addGoalToHistory() {
        history.add(getGoal());
    }

    public String getGoal() {
        return inputField.getText();
    }

    public void setFontDimension(int dimension)
    {
        Font font = new Font(inputField.getFont().getName(),inputField.getFont().getStyle(),dimension);
        inputField.setFont(font);
    }

} // end JavaInputField class