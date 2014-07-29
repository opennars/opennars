/*
 * InputPanel.java
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
package nars.gui.input;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import nars.core.NAR;
import nars.gui.FileTreeModel;
import nars.gui.NPanel;
import static nars.gui.output.LogPanel.setConsoleStyle;
import nars.io.Output.OUT;
import nars.io.TextInput;


public class InputPanel extends NPanel implements ActionListener {

    private final NAR reasoner;
    /**
     * Control buttons
     */
    private final JButton okButton, holdButton, clearButton;
    /**
     * Input area
     */
    private final JTextArea inputText;
    /**
     * Whether the window is ready to accept new addInput (in fact whether the
 Reasoner will read the content of {@link #inputText} )
     */
    private boolean ready;
    /**
     * number of cycles between experience lines
     */
    private int timer;
    private final JPanel centerPanel;
    private final JTree fileTree;
    private final JButton okSeqButton;

    /**
     * Constructor
     *
     * @param reasoner The reasoner
     * @param title The title of the window
     */
    public InputPanel(final NAR reasoner) {
        super(new BorderLayout());
        
        centerPanel = new JPanel(new BorderLayout());
                
        JPanel menu = new JPanel();
        menu.setLayout(new FlowLayout(FlowLayout.RIGHT,0,0));

        menu.setOpaque(false);
        setBackground(Color.BLACK);
        


        final JComboBox modeSelect = new JComboBox();
        modeSelect.addItem("Text");
        modeSelect.addItem("Files");
        modeSelect.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateMode(modeSelect.getSelectedIndex());
            }
        });
        menu.add(modeSelect);
                
        okButton = new JButton("Evaluate");
        okButton.setDefaultCapable(true);
        okButton.setToolTipText("Input the text, each line executed in the same clock cycle.  (Ctrl-enter)");
        okButton.addActionListener(this);
        menu.add(okButton);
        
        okSeqButton = new JButton("Eval Seq");
        okSeqButton.setDefaultCapable(true);
        okSeqButton.setToolTipText("Input the text, each line executed in successive clock cycles.");
        okSeqButton.addActionListener(this);
        menu.add(okSeqButton);
        
        holdButton = new JButton("Hold");
        holdButton.addActionListener(this);
        menu.add(holdButton);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        menu.add(clearButton);
        
        
        

        
        TreeModel model = new FileTreeModel(new File("./nal"));
        fileTree = new JTree(model);
        fileTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                    } else if (e.getClickCount() == 2) {
                        //DoubleClick
                        File f = (File) selPath.getLastPathComponent();

                        if (!f.isDirectory()) {
                            try {
                                new TextInput(reasoner, f);
                                reasoner.output(OUT.class, "Loaded file: " + f.getAbsolutePath());
                            } catch (IOException ex) {
                                System.err.println(ex);
                            }
                        }
                    }
                }
            }
        });

        inputText = new JTextArea("");
        inputText.setRows(3);        
        inputText.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                //control-enter evaluates
                if (e.isControlDown())
                    if (e.getKeyCode()==10) {
                        okButton.doClick();
                    }
            }           
        });
        setConsoleStyle(inputText, true);

        updateMode(0);
        
        add(centerPanel, BorderLayout.CENTER);
        add(menu, BorderLayout.SOUTH);
        
        
        this.reasoner = reasoner;
    }

    private void updateMode(int selectedIndex) {
        centerPanel.removeAll();
        System.out.println(selectedIndex);
        if (selectedIndex == 0) {
            centerPanel.add(inputText, BorderLayout.CENTER);
        }
        else if (selectedIndex == 1) {
            centerPanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);
        }        
        centerPanel.validate();
        layout();
        repaint();
    }

    /**
     * Initialize the window
     */
    public void init() {
        ready = false;
        inputText.setText("");
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {
            
        }
        else {
        
        }
    }
    
    /**
     * Handling button click
     *
     * @param e The ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        if (b == okButton) {
            ready = true;
            evaluate(inputText.getText());
            inputText.setText("");
        } else if (b == okSeqButton) {
            ready = true;
            evaluateSeq(inputText.getText());
            inputText.setText("");
        } else if (b == holdButton) {
            ready = false;
        } else if (b == clearButton) {
            inputText.setText("");
        }
    }

    public void evaluate(String input) {
        reasoner.addInput(input);
        reasoner.step(1);
    }
    public void evaluateSeq(String input) {
        //TODO make sequential evaluation
        reasoner.addInput(input);
        reasoner.step(1);
    }
   
    private void close() {
        setVisible(false);
    }

    

//    /**
//     * Accept text addInput in a tick, which can be multiple lines TODO some
// duplicated code with {@link ExperienceReader#nextInput()}
//     *
//     * @return Whether to check this channel again
//     */
//    public boolean nextInput() {
//        if (timer > 0) {  // wait until the timer
//            timer--;
//            return true;
//        }
//        if (!ready) {
//            return false;
//        }
//        String text = inputText.getText().trim();
//        String line;    // The next line of text
//        int endOfLine;
//        // The process steps at a number or no more text
//        while ((text.length() > 0) && (timer == 0)) {
//            endOfLine = text.indexOf('\n');
//            if (endOfLine < 0) {	// this code is reached at end of text
//                line = text;
//                text = "";
//            } else {	// this code is reached for ordinary lines
//                line = text.substring(0, endOfLine).trim();
//                text = text.substring(endOfLine + 1);	// text becomes rest of text
//            }
//            
//            reasoner.addInput(line);
//            
//            inputText.setText(text);	// update addInput Text widget to rest of text
//            if (text.isEmpty()) {
//                ready = false;
//            }
//        }
//        return ((text.length() > 0) || (timer > 0));
//    }
//
  
}
