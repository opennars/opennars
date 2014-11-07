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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import nars.core.NAR;
import nars.gui.FileTreeModel;
import nars.gui.NARSwing;
import nars.gui.NPanel;
import nars.gui.NWindow;
import nars.gui.input.TextInputPanel.InputAction;
import nars.gui.input.TextInputPanel.TextInputMode;
import static nars.gui.output.SwingLogPanel.setConsoleFont;
import nars.gui.output.SwingText;
import nars.io.Output.OUT;
import nars.io.TextInput;
import nars.io.narsese.NarseseParser;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;


public class TextInputPanel extends NPanel /*implements ActionListener*/ {
    private ReactionPanel infoPane;
    private final JMenuBar menu;
    private JSplitPane mainSplit;
    private JButton defaultButton;

    public interface InputAction {
        public String getLabel();
        
        /** may be null */
        public String getDescription();
        
        /** perform the action; the returned String, if not null, replaces the current
         * input allowing for actions to transform the input
         */
        public String run();
    
        /** between 0..1 used for sorting and affecting displayed prominence of menu option */
        public double getStrength();

    }
    
    /** each InputMode consists of:
     *      Interpretation - attempts to describe its interpretation (or lack of) of what is currently entered
     *      Actions - possible actions the current input enables, each with description. more than one may be invoked (checkboxes)
     *      Completions - descriptions of possible ways to complete the current input, and their meaning
     * 
     */
    public interface TextInputMode /* extends AbstractInputMode */ {
        
        public void setInputState(NAR nar, String input  /* int cursorPosition */);
        
        /** null if none available */        
        public String getInterpretation();
        
        public void getActions(List<InputAction> actionsCollected);
        
    }
    
    /** provides actions that will be available even if, or only if, input is blank */
    public class NullInput implements TextInputMode {

        private String input;
        private NAR nar;

        public final InputAction clear = new InputAction() {
            @Override public String getLabel() {
                return "Clear";
            }

            @Override public String getDescription() {
                return "Empty the input area";
            }

            @Override public String run() {
                return "";
            }                

            @Override
            public double getStrength() {
                return 0.25;
            }            
        };
        public final InputAction library = new InputAction() {
            private JTree fileTree = null;
            
            @Override public String getLabel() {
                return "Library";
            }

            @Override public String getDescription() {
                return "Browse the file library for experiences to load";
            }

            @Override public String run() {
                
                TreeModel model = new FileTreeModel(new File("./nal"));
                /*if (fileTree==null)*/ {
                    fileTree = new JTree(model);
                    fileTree.addMouseListener(new MouseAdapter() {
                        @Override
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
                                            nar.addInput(new TextInput(f));
                                            nar.emit(OUT.class, "Loaded file: " + f.getAbsolutePath());
                                        } catch (IOException ex) {
                                            System.err.println(ex);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    new NWindow("Experience Library", new JScrollPane(fileTree)).show(400, 200);
                }

                return "";
            }                

            @Override
            public double getStrength() {                
                return (input.length() == 0) ? 0.5 : 0.15;
            }
            
        };

        
        @Override
        public void setInputState(NAR nar, String input) {
            this.input = input;
            this.nar = nar;
        }

        @Override
        public String getInterpretation() {
            return null;
        }

        @Override
        public void getActions(List<InputAction> actionsCollected) {
            if (input.length() > 0) {
                actionsCollected.add(clear);
                //TODO concept search
                //TODO operator search
                
            }
            actionsCollected.add(library);
        }
    }
    
    public class NarseseInput implements TextInputMode {
        public final NarseseParser p = NarseseParser.newParser();
        
        private String input;
        private NAR nar;

        @Override
        public void setInputState(NAR nar, String input) {
            this.input = input;
            this.nar = nar;
        }

        @Override
        public String getInterpretation() {
            if (input.length() == 0)
                return null;
            
            ReportingParseRunner rpr = new ReportingParseRunner(p.Input());
            ParsingResult r = rpr.run(input);

            String s = "";
            boolean valid = (r.parseErrors.isEmpty());
            if (!valid) {
                for (Object e : r.parseErrors) {
                    if (e instanceof InvalidInputError) {
                        InvalidInputError iie = (InvalidInputError) e;
                        s += iie.getClass().getSimpleName() + " " + iie.getErrorMessage() + "\n";
                        s += (" at: " + iie.getStartIndex() + " to " + iie.getEndIndex()) + "\n";

                        for (MatcherPath m : iie.getFailedMatchers()) {
                            s += ("  ?-> " + m + '\n');
                        }
                    } else {
                        s += e.toString();
                    }
                }
            } else {
                s = "OK. ";
            }
            return s;
        }

        public InputAction inputDirect = new InputAction() {

            @Override
            public String getLabel() {
                return "Input";
            }

            @Override
            public String getDescription() {
                return "Direct input into NAR";
            }

            @Override
            public String run() {
                evaluateSeq(input);
                return "";
            }

            @Override
            public double getStrength() {
                return 1.5;
            }
                
        };
        
        public InputAction step = new InputAction() {

            @Override
            public String getLabel() {
                return "Step";
            }

            @Override
            public String getDescription() {
                return "Compute 1 cycle";
            }

            @Override
            public String run() {
                nar.step(1);
                return input;
            }

            @Override
            public double getStrength() {
                return 1.0;
            }
                
        };
        
        @Override
        public void getActions(List<InputAction> actionsCollected) {
            //TODO only allow input if it parses, but while parser is incomplete, allow all
            if (input.length() > 0)
                actionsCollected.add(inputDirect);
            
            actionsCollected.add(step);
            //TODO reset
            
            
            /*
            Other Actions:
                Ask - direct input a question, and create a solution window to watch for answers
                Command - direct input a goal, and create a task window to watch progress
                Parse Tree - show the parse tree for input (but don't clear it)
            */
        }
        
        public void evaluateSeq(String input) {
            //TODO make sequential evaluation
            nar.addInput(input);
            nar.step(1);
        }

        
    }
    
    /*
    public class EnglishInput implements TextInputMode {
        private NAR nar;
        private String input = "";
        private Englisch englisch;
        private List<AbstractTask> nextTasks;

        @Override
        public void setInputState(NAR nar, String input) {
            this.nar = nar;
            this.englisch = nar.perception.getText().englisch;

            input = input.trim();
            if (!this.input.equals(input)) {
                this.input = input;

                if (input.length() == 0) {
                    this.nextTasks = null;
                    return;
                }

                try {
                    this.nextTasks = englisch.parse(input, nar.perception.getText().narsese, false);
                    if (nextTasks.isEmpty())
                        nextTasks = null;
                } catch (Narsese.InvalidInputException ex) {
                    
                }
            }
        }

        @Override
        public String getInterpretation() {
            if (nextTasks!=null) {
                return nextTasks.toString();
            }
            return null;
        }

        @Override
        public void getActions(List<InputAction> actionsCollected) {
            
            
            //Actions:
            //    interpret
            

        }
    }*/
    
    private final NAR nar;

    /**
     * Input area
     */
    private JTextArea inputText;
    
//    /**
//     * Whether the window is ready to accept new addInput (in fact whether the
//     * Reasoner will read the content of {@link #inputText} )
//     */
//    private boolean ready;
    
    private final JPanel centerPanel;
    private final JComponent textInput;
    
    public List<TextInputMode> modes = new ArrayList();
    

    /**
     * Constructor
     *
     * @param nar The reasoner
     * @param title The title of the window
     */
    public TextInputPanel(final NAR nar) {
        super(new BorderLayout());

        this.nar = nar;

        modes.add(new NarseseInput());
        //modes.add(new EnglishInput());
        modes.add(new NullInput());
        
        centerPanel = new JPanel(new BorderLayout());

        menu = new JMenuBar();

        menu.setOpaque(false);
        setBackground(Color.BLACK);

        textInput = newTextInput();
        centerPanel.add(textInput);

        add(centerPanel, BorderLayout.CENTER);
        add(menu, BorderLayout.SOUTH);

    }

//    private void updateMode(int selectedIndex) {
//        centerPanel.removeAll();
//        if (selectedIndex == 0) {
//            centerPanel.add(narseseInput, BorderLayout.CENTER);
//        } else if (selectedIndex == 1) {
//            centerPanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);
//        }
//        centerPanel.validate();
//        repaint();
//    }

    public class ReactionPanel extends JPanel {
        private SwingText comments;
        

        /**
         * List with buttons (instant invoke) and checkboxes with 'All' at the top when two or more are selected
         */
        //private final JPanel list;

        public ReactionPanel() {
            super(new BorderLayout());

            //list = new JPanel(new GridBagLayout());
            //add(new JScrollPane(list), BorderLayout.CENTER);
            comments = new SwingText();
            comments.setEditable(false);
            setConsoleFont(comments, 12);
            
            /*JPanel pj = new JPanel(new BorderLayout());
            pj.add(j, BorderLayout.CENTER);*/
            add(new JScrollPane(comments), BorderLayout.CENTER);
        }

        public void update() {

            List<String[]> interpretations = new ArrayList();
            List<InputAction> actions = new ArrayList();
            
            String input = inputText.getText();
            
            for (final TextInputMode t : modes) {
                t.setInputState(nar, input);
                
                String interp = t.getInterpretation();
                if (interp!=null) {
                    interpretations.add(new String[] { t.getClass().getSimpleName(), interp } );
                }
                
                t.getActions(actions);                
            }
            
            /*
            GridBagConstraints gc = new GridBagConstraints();
            gc.weightx = 1.0;
            gc.weighty = 0.0;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridx = 1;
            gc.gridy = 1;
            */
            
            
            menu.removeAll();
            
            
            comments.setText("");
            for (String[] i : interpretations) {
                Color c = NARSwing.getColor(i[0], 0.7f, 0.6f);
                comments.print(Color.WHITE, c, i[0] + ":\n");
                Color c2 = NARSwing.getColor(i[0], 0.5f, 0.3f);
                comments.print(Color.WHITE, c2, i[1] + "\n\n");
            }


            
            if (comments.getText().length() > 0) {
                if (!isVisible()) {
                    int ll = mainSplit.getLastDividerLocation();
                    if (ll <= 0)
                        ll = (int)(getWidth() * 0.75);
                    if (getWidth() == 0) {
                        //component hasnt been instantiated yet, guessing at size
                        //TODO use actual planned size
                        ll = 500;
                    }
                    mainSplit.setDividerLocation(ll);
                    mainSplit.setLastDividerLocation(ll);
                    setVisible(true);
                }                
            }
            else {                
                if (isVisible()) {                    
                    mainSplit.setLastDividerLocation(mainSplit.getDividerLocation());
                    setVisible(false);
                }
            }
            
            defaultButton = null;
            double maxStrength = 0;
            for (InputAction a : actions) {
                JButton b = new JButton(a.getLabel());
                
                double strength = a.getStrength();
                if (strength > maxStrength) {
                    defaultButton = b;
                    maxStrength = strength;
                }
                b.setFont(b.getFont().deriveFont((float)(b.getFont().getSize() * (0.5f + 0.5f * strength))));
                        
                b.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                //TODO catch error around run() ?
                                String result = a.run();
                                if (result!=null) {
                                    inputText.setText(result);
                                }
                            }
                        });
                    }
                });
                                
                menu.add(b);
            }

            menu.validate();
            menu.repaint();
            
            validate();
            repaint();

        }

    }

    //TODO move this to its own class
    public JComponent newTextInput() {
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);        
        
        infoPane = new ReactionPanel();

        inputText = new JTextArea("");
        inputText.setRows(3);

        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                updated(documentEvent);
            }

            public void insertUpdate(DocumentEvent documentEvent) {
                updated(documentEvent);
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                updated(documentEvent);
            }

            private void updated(DocumentEvent documentEvent) {
                String t = inputText.getText();
                
                boolean valid = true, show;
//                if (t.length() > 0) {
//                    infoPane.update(t);
//                    show = true; //!valid;
//                    
//                }
//                else {
//                    show = false;
//                }
                //show = true;
                
                
                updateContext();
            }
        };
        inputText.getDocument().addDocumentListener(documentListener);

        inputText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                //control-enter evaluates
                if (e.isControlDown()) {
                    if (e.getKeyCode() == 10) {
                        runDefault();
                    }
                }
            }
        });
        setConsoleFont(inputText, 20);

        mainSplit.add(new JScrollPane(inputText), 0);

        infoPane.setVisible(false);
        mainSplit.add(infoPane, 1);

        updateContext();
        
        return mainSplit;
    }

    protected void updateContext() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                infoPane.update();
            }            
        });
    }

    /**
     * Initialize the window
     */
    public void init() {
        inputText.setText("");
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {

        } else {

        }
    }
    
    protected void runDefault() {
        if (defaultButton!=null)
            defaultButton.doClick();
    }

//    /**
//     * Handling button click
//     *
//     * @param e The ActionEvent
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        JButton b = (JButton) e.getSource();
//        if (b == eval) {
//            ready = true;
//            evaluateSeq(inputText.getText());
//            inputText.setText("");
////        } else if (b == evalAll) {
////            ready = true;
////            evaluateAll(inputText.getText());
////            inputText.setText("");
//        } else if (b == holdButton) {
//            ready = false;
//        } else if (b == clearButton) {
//            inputText.setText("");
//        }
//    }

//    public void evaluateAll(String input) {
//        reasoner.addInput(input);
//        reasoner.step(1);
//    }

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
