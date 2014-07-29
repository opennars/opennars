package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.gui.InferenceLogger;
import nars.gui.InferenceLogger.LogOutput;
import nars.gui.NARControls;
import nars.gui.NARControls.FAButton;
import nars.gui.NARControls.FAToggleButton;
import nars.gui.NPanel;
import nars.gui.NSlider;
import nars.gui.WrapLayout;
import nars.io.Output;
import nars.io.TextOutput;


public class LogPanel extends NPanel implements Output, LogOutput {
    private final DefaultStyledDocument doc;
    private final JTextPane ioText;
    private final Style mainStyle;
    private final NAR nar;
    int maxIOTextSize = (int) 8E6;
    private boolean showErrors = true;
    private boolean showStamp = false;
    private boolean showQuestions = true;
    private boolean showStatements = true;

    /**
     * the log file
     */
    private PrintWriter logFile = null;
    
    private Collection<String> nextOutput = new ConcurrentLinkedQueue();
    private final InferenceLogger logger;
    private String logFilePath;

    public LogPanel(NARControls c) {
        super();
        setLayout(new BorderLayout());
        
        
        this.nar = c.nar;
        this.logger = c.logger;
        
        
        StyleContext sc = new StyleContext();
        doc = new DefaultStyledDocument(sc);
        
        ioText = new JTextPane(doc);
        ioText.setEditable(false);

        // Create and add the main document style
        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = sc.addStyle("MainStyle", defaultStyle);
        //StyleConstants.setLeftIndent(mainStyle, 16);
        //StyleConstants.setRightIndent(mainStyle, 16);
        //StyleConstants.setFirstLineIndent(mainStyle, 16);
        //StyleConstants.setFontFamily(mainStyle, "serif");
        //StyleConstants.setFontSize(mainStyle, 12);
        doc.setLogicalStyle(0, mainStyle);

        //http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane        
        JPanel ioTextWrap = new JPanel(new BorderLayout());        
        ioTextWrap.add(ioText);
        JScrollPane ioTextScroll = new JScrollPane(ioTextWrap);
        add(ioTextScroll, BorderLayout.CENTER);
        
        

                
        //JPanel menuBottom = new JPanel(new WrapLayout(FlowLayout.RIGHT, 0, 0));
        JPanel menuTop = new JPanel(new WrapLayout(FlowLayout.LEFT,0,0));
        
        //menuBottom.setOpaque(false);
        //menuBottom.setBorder(new EmptyBorder(0,0,0,0));
        menuTop.setOpaque(false);
        menuTop.setBorder(new EmptyBorder(0,0,0,0));

        setConsoleStyle(ioText, true);
        
        ioTextWrap.setBorder(new EmptyBorder(0,0,0,0));
        ioTextScroll.setBorder(new EmptyBorder(0,0,0,0));        
        setBackground(Color.BLACK);
        
        JButton clearButton = new FAButton('\uf016');
        clearButton.setToolTipText("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                ioText.setText("");
            }            
        });
        menuTop.add(clearButton);
        
        final String defaultStreamButtonLabel = "Stream to File..";
        final JToggleButton streamButton = new FAToggleButton('\uf0c7','\uf052');
        streamButton.setToolTipText(defaultStreamButtonLabel);
        streamButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                
                if (streamButton.isSelected()) {
                    if (!openLogFile()) {
                        streamButton.setSelected(false);
                    }
                    else {
                        streamButton.setToolTipText("Streaming...");
                    }
                }
                else {
                    streamButton.setToolTipText(defaultStreamButtonLabel);
                    closeLogFile();
                }
            }            
        });
        menuTop.add(streamButton);
        
        menuTop.add(Box.createHorizontalStrut(4));

        final JToggleButton showStatementsBox = new JToggleButton("Statements");
        showStatementsBox.setSelected(showStatements);
        showStatementsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatements = showStatementsBox.isSelected();
            }
        });
        menuTop.add(showStatementsBox);
        
        final JToggleButton showQuestionsBox = new JToggleButton("Questions");
        showQuestionsBox.setSelected(showQuestions);
        showQuestionsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showQuestions = showQuestionsBox.isSelected();
            }
        });
        menuTop.add(showQuestionsBox);        

        final JToggleButton showErrorBox = new JToggleButton("Errors");
        showErrorBox.setSelected(showErrors);
        showErrorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showErrors = showErrorBox.isSelected();
            }
        });
        menuTop.add(showErrorBox);
        
        
        final JToggleButton showStampBox = new JToggleButton("Stamps");
        showStampBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStamp = showStampBox.isSelected();
            }
        });
        menuTop.add(showStampBox);        

        final JToggleButton showTraceBox = new JToggleButton("Trace");
        showTraceBox.setEnabled(true);
        showTraceBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTrace(showTraceBox.isSelected());
            }
        });
        setTrace(showTraceBox.isSelected());
        menuTop.add(showTraceBox);        
        
        menuTop.add(Box.createHorizontalStrut(4));

        
        final NSlider fontSlider = new NSlider(ioText.getFont().getSize(), 6, 40) {
            @Override public void onChange(double v) {
                ioText.setFont(ioText.getFont().deriveFont((float)v));
            }         
        };        
        fontSlider.setPrefix("Font size: ");
        menuTop.add(fontSlider);
        
        //add(menuBottom, BorderLayout.SOUTH);
        add(menuTop, BorderLayout.NORTH);

        addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
            }
        });

    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing)
            nar.addOutput(this);        
        else
            nar.removeOutput(this);                
    }

    
    @Override
    public void output(final Class c, Object o) {

        if ((!showErrors) && (c == ERR.class)) {
            return;
        }
        if (o instanceof Sentence) {
            Sentence s = (Sentence)o;

            if (s.isQuestion() && !showQuestions)
                return;
            if (s.isJudgment() && !showStatements)
                return;        
        }

        String s = TextOutput.getOutputString(c, o, true, showStamp, nar);
        
        nextOutput.add(s);
        
        if (logFile!=null) {
            logFile.print(s);
        }
        
        SwingUtilities.invokeLater(nextOutputRunnable);

    }

    void limitBuffer(int incomingDataSize) {
        Document doc = ioText.getDocument();
        int overLength = doc.getLength() + incomingDataSize - maxIOTextSize;

        if (overLength > 0) {
            try {
                doc.remove(0, overLength);
            } catch (BadLocationException ex) {
            }
        }
    }

    protected void print(Color c, float size, String text, boolean bold) {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        MutableAttributeSet aset = ioText.getInputAttributes();


        Font f = ioText.getFont();
        StyleConstants.setForeground(aset, c);
        //StyleConstants.setFontSize(aset, (int)(f.getSize()*size));
        StyleConstants.setBold(aset, bold);
        
        try {
            doc.insertString(doc.getLength(), text, null);

            ioText.getStyledDocument().setCharacterAttributes(doc.getLength() - text.length(), text.length(), aset, true);
        } catch (BadLocationException ex) {
            Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    

    public Color getLineColor(String l) {
        l = l.trim();
        if (l.startsWith("OUT:")) {
            return Color.LIGHT_GRAY;
        }
        else if (l.startsWith("IN:")) {
            return Color.WHITE;
        }
        else if (l.startsWith("ERR:")) {
            return Color.ORANGE;
        }
        else //if (l.startsWith("LOG:")) {
            return Color.GRAY;        
    }
    
    private final Runnable nextOutputRunnable = new Runnable() {
        @Override
        public void run() {
            if (nextOutput.size() > 0) {

                try {
                    //limitBuffer(nextOutput.baseLength());
                    limitBuffer(128);

                    for (String o : nextOutput) {
                        print(getLineColor(o), 1.0f, o+'\n', false);
                    }

                    nextOutput.clear();
                }
                catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }
    };


    public static final class LOG {   }
    
    @Override
    public void logAppend(String s) {
        output(LOG.class, s);
    }
    
    public void setTrace(boolean b) {
        if (b) {
            logger.addOutput(this);
        }
        else {
            logger.removeOutput(this);
        }
    }
    
    public boolean openLogFile() {
        FileDialog dialog = new FileDialog((Dialog) null, "Inference Log", FileDialog.SAVE);
        
        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        logFilePath = dialog.getFile();
        if (logFilePath == null) return false;
        
        try {
            boolean append = true;
            boolean autoflush = true;
            logFile = new PrintWriter(new FileWriter(directoryName + logFilePath, append), autoflush);
            output(LOG.class, "Stream opened: " + logFilePath);            
            return true;
        } catch (IOException ex) {
            output(ERR.class, "Log file save: I/O error: " + ex.getMessage());
        }
        
        return false;
    }

    public void closeLogFile() {
        if (logFile!=null) {
            output(LOG.class, "Stream saved: " + logFilePath);
            logFile.close();
            logFile = null;
        }
    }


    public static void setConsoleStyle(JTextComponent c, boolean invert) {
        if (invert) {
            c.setForeground(Color.WHITE);
            c.setCaretColor(Color.WHITE);
            c.setBackground(Color.BLACK);
        }
        else {
            c.setForeground(Color.BLACK);
            c.setCaretColor(Color.BLACK);
            c.setBackground(Color.WHITE);            
            
        }
        c.setBorder(new EmptyBorder(0,0,0,0));
        c.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));        
    }
    
}
