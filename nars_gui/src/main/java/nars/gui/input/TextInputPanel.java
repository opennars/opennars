///*
// * InputPanel.java
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
//package nars.gui.input;
//
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.NWindow;
//import nars.NAR;
//import nars.Video;
//import nars.gui.FileTreeModel;
//import nars.gui.output.SwingText;
//import nars.op.io.echo;
//import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
//import org.fife.ui.rtextarea.RTextScrollPane;
//
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.tree.TreeModel;
//import javax.swing.tree.TreePath;
//import java.awt.*;
//import java.awt.event.*;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static nars.gui.output.SwingLogPanel.setConsoleFont;
//
//
//public class TextInputPanel extends NPanel /*implements ActionListener*/ {
//    private ReactionPanel infoPane;
//    private final JMenuBar menu;
//    //private JSplitPane mainSplit;
//    private JButton defaultButton;
//
//    public interface InputAction {
//        public String getLabel();
//
//        /** may be null */
//        public String getDescription();
//
//        /** perform the action; the returned String, if not null, replaces the current
//         * input allowing for actions to transform the input
//         */
//        public String run();
//
//        /** between 0..1 used for sorting and affecting displayed prominence of menu option */
//        public double getStrength();
//
//    }
//
//    /** each InputMode consists of:
//     *      Interpretation - attempts to describe its interpretation (or lack of) of what is currently entered
//     *      Actions - possible actions the current input enables, each with description. more than one may be invoked (checkboxes)
//     *      Completions - descriptions of possible ways to complete the current input, and their meaning
//     *
//     */
//    public interface TextInputMode /* extends AbstractInputMode */ {
//
//        public void setInputState(NAR nar, String input  /* int cursorPosition */);
//
//        /** null if none available */
//        public String getInterpretation();
//
//        public void getActions(List<InputAction> actionsCollected);
//
//    }
//
//    /** provides actions that will be available even if, or only if, input is blank */
//    public class NullInput implements TextInputMode {
//
//        private String input;
//        private NAR nar;
//
//        public final InputAction clear = new InputAction() {
//            @Override public String getLabel() {
//                return "Clear";
//            }
//
//            @Override public String getDescription() {
//                return "Empty the input area";
//            }
//
//            @Override public String run() {
//                return "";
//            }
//
//            @Override
//            public double getStrength() {
//                return 0.25;
//            }
//        };
//        public final InputAction library = new InputAction() {
//            private JTree fileTree = null;
//
//            @Override public String getLabel() {
//                return "Library";
//            }
//
//            @Override public String getDescription() {
//                return "Browse the file library for experiences to load";
//            }
//
//            @Override public String run() {
//
//                TreeModel model = new FileTreeModel(new File("..//nal"));
//                /*if (fileTree==null)*/ {
//                    fileTree = new JTree(model);
//                    fileTree.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mousePressed(MouseEvent e) {
//                            int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
//                            TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
//                            if (selRow != -1) {
//                                if (e.getClickCount() == 1) {
//                                } else if (e.getClickCount() == 2) {
//                                    //DoubleClick
//                                    File f = (File) selPath.getLastPathComponent();
//
//                                    if (!f.isDirectory()) {
//                                        try {
//                                            nar.input(f);
//                                            nar.emit(echo.class, "Loaded file: " + f.getAbsolutePath());
//                                        } catch (IOException ex) {
//                                            System.err.println(ex);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    });
//                    new NWindow("Experience Library", new JScrollPane(fileTree)).show(400, 200);
//                }
//
//                return "";
//            }
//
//            @Override
//            public double getStrength() {
//                return (input.length() == 0) ? 0.5 : 0.15;
//            }
//
//        };
//
//
//        @Override
//        public void setInputState(NAR nar, String input) {
//            this.input = input;
//            this.nar = nar;
//        }
//
//        @Override
//        public String getInterpretation() {
//            return null;
//        }
//
//        @Override
//        public void getActions(List<InputAction> actionsCollected) {
//            if (input.length() > 0) {
//                actionsCollected.add(clear);
//                //TODO concept search
//                //TODO operate search
//
//            }
//            actionsCollected.add(library);
//        }
//    }
//
//
//    /*
//    public class EnglishInput implements TextInputMode {
//        private NAR nar;
//        private String input = "";
//        private Englisch englisch;
//        private List<AbstractTask> nextTasks;
//
//        @Override
//        public void setInputState(NAR nar, String input) {
//            this.nar = nar;
//            this.englisch = nar.perception.getText().englisch;
//
//            input = input.trim();
//            if (!this.input.equals(input)) {
//                this.input = input;
//
//                if (input.length() == 0) {
//                    this.nextTasks = null;
//                    return;
//                }
//
//                try {
//                    this.nextTasks = englisch.parse(input, nar.perception.getText().narsese, false);
//                    if (nextTasks.isEmpty())
//                        nextTasks = null;
//                } catch (Narsese.InvalidInputException ex) {
//
//                }
//            }
//        }
//
//        @Override
//        public String getInterpretation() {
//            if (nextTasks!=null) {
//                return nextTasks.toString();
//            }
//            return null;
//        }
//
//        @Override
//        public void getActions(List<InputAction> actionsCollected) {
//
//
//            //Actions:
//            //    interpret
//
//
//        }
//    }*/
//
//    private final NAR nar;
//
//    /**
//     * Input area
//     */
//    private RSyntaxTextArea inputText;
//
////    /**
////     * Whether the window is ready to accept new addInput (in fact whether the
////     * Reasoner will read the content of {@tlink #inputText} )
////     */
////    private boolean ready;
//
//    private final JPanel centerPanel;
//    private final JComponent textInput;
//
//    public List<TextInputMode> modes = new ArrayList();
//
//
//    /**
//     * Constructor
//     *
//     * @param nar The reasoner
//     */
//    public TextInputPanel(final NAR nar) {
//        super(new BorderLayout());
//
//        this.nar = nar;
//
//        modes.add(new NarseseInput());
//        //modes.add(new EnglishInput());
//        modes.add(new NullInput());
//
//        centerPanel = new JPanel(new BorderLayout());
//
//        menu = new JMenuBar();
//
//        menu.setOpaque(false);
//        menu.setBackground(Color.BLACK);
//        setBackground(Color.BLACK);
//
//        textInput = newTextInput();
//        centerPanel.add(textInput);
//
//        add(centerPanel, BorderLayout.CENTER);
//        add(menu, BorderLayout.SOUTH);
//        menu.setVisible(true);
//
//    }
//
////    private void updateMode(int selectedIndex) {
////        centerPanel.removeAll();
////        if (selectedIndex == 0) {
////            centerPanel.add(narseseInput, BorderLayout.CENTER);
////        } else if (selectedIndex == 1) {
////            centerPanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);
////        }
////        centerPanel.validate();
////        repaint();
////    }
//
//    public class ReactionPanel extends JPanel {
//        private SwingText comments;
//
//
//        /**
//         * List with buttons (instant invoke) and checkboxes with 'All' at the top when two or more are selected
//         */
//        //private final JPanel list;
//
//        public ReactionPanel() {
//            super(new BorderLayout());
//
//            //list = new JPanel(new GridBagLayout());
//            //add(new JScrollPane(list), BorderLayout.CENTER);
//            comments = new SwingText();
//            comments.setEditable(false);
//            setConsoleFont(comments, 12);
//
//            /*JPanel pj = new JPanel(new BorderLayout());
//            pj.add(j, BorderLayout.CENTER);*/
//            add(new JScrollPane(comments), BorderLayout.CENTER);
//        }
//
//        public void update() {
//
//            List<String[]> interpretations = new ArrayList();
//            List<InputAction> actions = new ArrayList();
//
//            String input = inputText.getText();
//
//            for (final TextInputMode t : modes) {
//                t.setInputState(nar, input);
//
//                String interp = t.getInterpretation();
//                if (interp!=null) {
//                    interpretations.add(new String[] { t.getClass().getSimpleName(), interp } );
//                }
//
//                t.getActions(actions);
//            }
//
//            /*
//            GridBagConstraints gc = new GridBagConstraints();
//            gc.weightx = 1.0;
//            gc.weighty = 0.0;
//            gc.fill = GridBagConstraints.HORIZONTAL;
//            gc.gridx = 1;
//            gc.gridy = 1;
//            */
//
//
//            menu.removeAll();
//
//
//            comments.setText("");
//            for (String[] i : interpretations) {
//                Color c = Video.getColor(i[0], 0.7f, 0.6f);
//                comments.print(Color.WHITE, c, i[0] + ":\n", null);
//                Color c2 = Video.getColor(i[0], 0.5f, 0.3f);
//                comments.print(Color.WHITE, c2, i[1] + "\n\n", null);
//            }
//
//
//
////            if (comments.getText().length() > 0) {
////                if (!isVisible()) {
////                    int ll = mainSplit.getLastDividerLocation();
////                    if (ll <= 0)
////                        ll = (int)(getWidth() * 0.75);
////                    if (getWidth() == 0) {
////                        //component hasnt been instantiated yet, guessing at size
////                        //TODO use actual planned size
////                        ll = 500;
////                    }
////                    mainSplit.setDividerLocation(ll);
////                    mainSplit.setLastDividerLocation(ll);
////                    setVisible(true);
////                }
////            }
////            else {
////                if (isVisible()) {
////                    mainSplit.setLastDividerLocation(mainSplit.getDividerLocation());
////                    setVisible(false);
////                }
////            }
//
//            defaultButton = null;
//            double maxStrength = 0;
//            for (InputAction a : actions) {
//                JButton b = new JButton(a.getLabel());
//
//                double strength = a.getStrength();
//                if (strength > maxStrength) {
//                    defaultButton = b;
//                    maxStrength = strength;
//                }
//                b.setFont(b.getFont().deriveFont((float)(b.getFont().getSize() * (0.5f + 0.5f * strength))));
//
//                b.setForeground(Color.WHITE);
//                b.setBackground(Color.DARK_GRAY);
//                b.addActionListener(new ActionListener() {
//                    @Override public void actionPerformed(ActionEvent e) {
//                        SwingUtilities.invokeLater(new Runnable() {
//                            @Override public void run() {
//                                //TODO catch error around run() ?
//                                String result = a.run();
//                                if (result!=null) {
//                                    inputText.setText(result);
//                                }
//                            }
//                        });
//                    }
//                });
//
//                menu.add(b);
//            }
//
//            menu.validate();
//            menu.repaint();
//
//            validate();
//            repaint();
//
//        }
//
//    }
//
//    //TODO move this to its own class
//    public JComponent newTextInput() {
//        //mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//
//        infoPane = new ReactionPanel();
//
//
//
//        inputText = new RSyntaxTextArea();
//        //inputText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LISP);
//
//        //http://fifesoft.com/rsyntaxtextarea/examples/example5.php
//        //http://fifesoft.com/rsyntaxtextarea/doc/CustomSyntaxHighlighting.html
//        /*((RSyntaxDocument)inputText.getDocument()).setSyntaxStyle(new TokenMaker() {        });*/
////        try {
////            Theme theme = Theme.load(getClass().getResourceAsStream("/text_input_theme_dark.xml"));
////            theme.apply(inputText);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        inputText.setBackground(Color.BLACK);
//        inputText.setCurrentLineHighlightColor(new Color(0.1f, 0.1f, 0.1f));
//        inputText.setForeground(Color.WHITE);
//        inputText.setMarkOccurrencesColor(Color.ORANGE);
//        inputText.setMarkAllHighlightColor(Color.ORANGE);
//
//
//        inputText.setCodeFoldingEnabled(false);
//        inputText.setAntiAliasingEnabled(true);
//
//        RTextScrollPane sp = new RTextScrollPane(inputText);
//        sp.setLineNumbersEnabled(false);
//        sp.setFoldIndicatorEnabled(false);
//        //mainSplit.add(sp, 0);
//
//        this.inputText.setRows(3);
//
//        DocumentListener documentListener = new DocumentListener() {
//            public void changedUpdate(DocumentEvent documentEvent) {
//                updated(documentEvent);
//            }
//
//            public void insertUpdate(DocumentEvent documentEvent) {
//                updated(documentEvent);
//            }
//
//            public void removeUpdate(DocumentEvent documentEvent) {
//                updated(documentEvent);
//            }
//
//            private void updated(DocumentEvent documentEvent) {
//                String t = TextInputPanel.this.inputText.getText();
//
//                boolean valid = true, show;
////                if (t.length() > 0) {
////                    infoPane.update(t);
////                    show = true; //!valid;
////
////                }
////                else {
////                    show = false;
////                }
//                //show = true;
//
//
//                updateContext();
//            }
//        };
//        this.inputText.getDocument().addDocumentListener(documentListener);
//
//        this.inputText.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent e) {
//                //control-enter evaluates
//                if (e.isControlDown()) {
//                    if (e.getKeyCode() == 10) {
//                        runDefault();
//                    }
//                }
//            }
//        });
//        setConsoleFont(this.inputText, 20);
//
//
//
//        infoPane.setVisible(false);
//        //mainSplit.add(infoPane, 1);
//
//        updateContext();
//
//        //return mainSplit;
//        return sp;
//    }
//
//    protected void updateContext() {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override public void run() {
//                infoPane.update();
//            }
//        });
//    }
//
//    /**
//     * Initialize the window
//     */
//    public void init() {
//        inputText.setText("");
//    }
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//        if (appearedOrDisappeared) {
//
//        } else {
//
//        }
//    }
//
//    protected void runDefault() {
//        if (defaultButton!=null)
//            defaultButton.doClick();
//    }
//
////    /**
////     * Handling button click
////     *
////     * @param e The ActionEvent
////     */
////    @Override
////    public void actionPerformed(ActionEvent e) {
////        JButton b = (JButton) e.getSource();
////        if (b == eval) {
////            ready = true;
////            evaluateSeq(inputText.getText());
////            inputText.setText("");
//////        } else if (b == evalAll) {
//////            ready = true;
//////            evaluateAll(inputText.getText());
//////            inputText.setText("");
////        } else if (b == holdButton) {
////            ready = false;
////        } else if (b == clearButton) {
////            inputText.setText("");
////        }
////    }
//
////    public void evaluateAll(String input) {
////        reasoner.addInput(input);
////        reasoner.step(1);
////    }
//
//    private void close() {
//        setVisible(false);
//    }
//
////    /**
////     * Accept text addInput in a tick, which can be multiple lines TODO some
//// duplicated code with {@tlink ExperienceReader#nextInput()}
////     *
////     * @return Whether to check this channel again
////     */
////    public boolean nextInput() {
////        if (timer > 0) {  // wait until the timer
////            timer--;
////            return true;
////        }
////        if (!ready) {
////            return false;
////        }
////        String text = inputText.getText().trim();
////        String line;    // The next line of text
////        int endOfLine;
////        // The process steps at a number or no more text
////        while ((text.length() > 0) && (timer == 0)) {
////            endOfLine = text.indexOf('\n');
////            if (endOfLine < 0) {	// this code is reached at end of text
////                line = text;
////                text = "";
////            } else {	// this code is reached for ordinary lines
////                line = text.substring(0, endOfLine).trim();
////                text = text.substring(endOfLine + 1);	// text becomes rest of text
////            }
////
////            reasoner.addInput(line);
////
////            inputText.setText(text);	// update addInput Text widget to rest of text
////            if (text.isEmpty()) {
////                ready = false;
////            }
////        }
////        return ((text.length() > 0) || (timer > 0));
////    }
////
// }
