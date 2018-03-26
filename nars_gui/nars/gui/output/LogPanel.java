package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import nars.main.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import automenta.vivisect.swing.AwesomeToggleButton;
import automenta.vivisect.swing.AwesomeButton;
import automenta.vivisect.swing.NPanel;
import automenta.vivisect.swing.NSlider;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import nars.gui.InferenceLogger;
import nars.gui.InferenceLogger.LogOutput;
import nars.gui.NARControls;
import nars.gui.WrapLayout;
import nars.io.handlers.EventHandler;
import nars.io.handlers.OutputHandler;
import nars.io.handlers.OutputHandler.ERR;
import nars.io.handlers.OutputHandler.EXE;
import nars.io.handlers.TextOutputHandler;

abstract public class LogPanel extends NPanel implements LogOutput {

    static CharSequence getText(Class c, Object o, boolean showStamp, NAR nar) {
        return TextOutputHandler.getOutputString(c, o, showStamp, nar);
    }

    protected final NAR nar;

    private EventHandler out;
    
    public static final int maxIOTextSize = (int) 3E5;
    public static final int clearMargin = (int) 3E4;
    
    protected boolean showErrors = true;
    protected boolean showStamp = false;
    protected boolean showQuestions = true;
    protected boolean showStatements = true;
    protected boolean showExecutions = true;

    /**
     * the log file
     */
    protected PrintWriter logFile = null;

    private final InferenceLogger logger;
    private String logFilePath;

    public static final Class[] outputEvents = OutputHandler.DefaultOutputEvents;

    
    
    public LogPanel(NARControls c) {
        this(c, outputEvents);
    }
    
    String filter = "";
    public LogPanel(NARControls c, Class... events) {
        super();
        setLayout(new BorderLayout());

        this.nar = c.nar;
        this.logger = c.logger;

        out = new EventHandler(nar, false, events) {
            @Override public void event(final Class event, final Object[] arguments) {
                LogPanel.this.output(event, arguments.length > 1 ? arguments : arguments[0]);
            }
        };
                
        //JPanel menuBottom = new JPanel(new WrapLayout(FlowLayout.RIGHT, 0, 0));
        JPanel menuTop = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));

        //menuBottom.setOpaque(false);
        //menuBottom.setBorder(new EmptyBorder(0,0,0,0));
        menuTop.setOpaque(false);
        menuTop.setBorder(new EmptyBorder(0, 0, 0, 0));

        
        JButton clearButton = new AwesomeButton('\uf016');
        clearButton.setForeground(Color.WHITE); 
        clearButton.setBackground(Color.DARK_GRAY);
        clearButton.setToolTipText("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
        menuTop.add(clearButton);

        final String defaultStreamButtonLabel = "Stream to File..";
        final JToggleButton streamButton = new AwesomeToggleButton('\uf0c7', '\uf052');
        streamButton.setForeground(Color.WHITE); 
        streamButton.setBackground(Color.DARK_GRAY);
        streamButton.setToolTipText(defaultStreamButtonLabel);
        streamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (streamButton.isSelected()) {
                    if (!openLogFile()) {
                        streamButton.setSelected(false);
                    } else {
                        streamButton.setToolTipText("Streaming...");
                    }
                } else {
                    streamButton.setToolTipText(defaultStreamButtonLabel);
                    closeLogFile();
                }
            }
        });
        menuTop.add(streamButton);

        menuTop.add(Box.createHorizontalStrut(4));

        final JToggleButton showStatementsBox = new JToggleButton(".");
        showStatementsBox.setForeground(Color.WHITE); 
        showStatementsBox.setBackground(Color.DARK_GRAY);
        showStatementsBox.setToolTipText("Show Statements");
        showStatementsBox.setSelected(showStatements);
        showStatementsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatements = showStatementsBox.isSelected();
            }
        });
        menuTop.add(showStatementsBox);

        final JToggleButton showQuestionsBox = new JToggleButton("?");
        showQuestionsBox.setForeground(Color.WHITE); 
        showQuestionsBox.setBackground(Color.DARK_GRAY);
        showQuestionsBox.setToolTipText("Show Questions");
        showQuestionsBox.setSelected(showQuestions);
        showQuestionsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showQuestions = showQuestionsBox.isSelected();
            }
        });
        menuTop.add(showQuestionsBox);

        final JToggleButton showExecutionsBox = new JToggleButton("!");
        showExecutionsBox.setForeground(Color.WHITE); 
        showExecutionsBox.setBackground(Color.DARK_GRAY);
        showExecutionsBox.setToolTipText("Show Goals & Executions");
        showExecutionsBox.setSelected(showExecutions);
        showExecutionsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showExecutions = showExecutionsBox.isSelected();
            }
        });
        menuTop.add(showExecutionsBox);
        
        final JToggleButton showErrorBox = new JToggleButton("Errors");
        showErrorBox.setForeground(Color.WHITE); 
        showErrorBox.setBackground(Color.DARK_GRAY);
        showErrorBox.setSelected(showErrors);
        showErrorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showErrors = showErrorBox.isSelected();
            }
        });
        menuTop.add(showErrorBox);

        final JToggleButton showStampBox = new JToggleButton("Stamp");
        showStampBox.setForeground(Color.WHITE); 
        showStampBox.setBackground(Color.DARK_GRAY);
        showStampBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setShowStamp(showStampBox.isSelected());
            }
        });
        menuTop.add(showStampBox);

        final JToggleButton showTraceBox = new JToggleButton("Trace");
        showTraceBox.setForeground(Color.WHITE); 
        showTraceBox.setBackground(Color.DARK_GRAY);
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

        
        final NSlider fontSlider = new NSlider(12f, 6f, 40f) {

            @Override
            public void onChange(float v) {
                setFontSize(v);
            }
            
        };
        fontSlider.setPrefix("Font size: ");
        menuTop.add(fontSlider);
        
        final JTextField filterBox = new JTextField("");
        filterBox.setPreferredSize(new Dimension(255,20));
        
        filterBox.setForeground(Color.WHITE); 
        filterBox.setBackground(Color.DARK_GRAY);
        filterBox.setEnabled(true);
        filterBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                filter = filterBox.getText();
            }
        
        });
        menuTop.add(filterBox);


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
    
    abstract public void setFontSize(float v);
    abstract protected void clearLog();

    @Override
    protected void onShowing(boolean showing) {

        if (showing) {
            out.setActive(true);
        } else {
            out.setActive(false);
        }
    }

    public void output(final Class c, Object o) {

        if ((c == ERR.class) && (!showErrors)) {
            return;
        }
        if ((c == EXE.class) && !showExecutions) {
            return;
        }
        
        if (o instanceof Task) {
            
            Sentence s = ((Task) o).sentence;
            if (s!=null) {
                if (s.isQuestion() && !showQuestions) {
                    return;
                }
                if (s.isJudgment() && !showStatements) {
                    return;
                }
                if (s.isGoal()&& !showExecutions) {
                    return;
                }
            }
            
        }

        print(c, o);

    }
    
    abstract void print(Class c, Object o);
    
    abstract void limitBuffer(int incomingDataSize);


    public static Color getChannelColor(Class c) {
        
        switch (c.getSimpleName()) {
            case "OUT":                
                return Color.GREEN;
            case "IN":
                return Color.YELLOW;
            case "ERR":
                return Color.ORANGE;
        }
        
        return Color.GRAY;
    }

    final static Color getPriorityColor(final float val) {
        return new Color(val, val, val);
    }
    final static Color getNegativeEvidenceColor(final float val) {
        return new Color(0, 0, val);
    }
    final static Color getPositiveEvidenceColor(final float val) {
        return new Color(val,0,0);
    }

    final static Color getStatementColor(final char punctuation, final float priority) {
        
        float r = 1f, g = 1f, b = 1f;
        /*switch (punctuation) {
            case '!': r = 1f; g = 0.75f; b = 0f; break;
            case '?': b = 1f; r = 1.0f; g = 1f; break;
            case '=': r = 0.0f; g = 0f; b = 0.0f; break; //solution
            case '.': break;
                
        }        
        r *= 0.25f + 0.75f*priority;
        g *= 0.25f + 0.75f*priority;
        b *= 0.25f + 0.75f*priority;*/
        return new Color(r, g, b);
    }
    

    public static final class LOG {
    }

    
    @Override
    public void traceAppend(Class channel, String s) {
        output(LOG.class, channel.getSimpleName() + ": " + s);
    }

    public void setTrace(boolean b) {
        if (b) {
            logger.setActive(true);
            logger.addOutput(this);
        } else {
            logger.setActive(false);
            logger.removeOutput(this);
        }
    }

    public boolean openLogFile() {
        FileDialog dialog = new FileDialog((Dialog) null, "Inference Log", FileDialog.SAVE);

        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        logFilePath = dialog.getFile();
        if (logFilePath == null) {
            return false;
        }

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
        if (logFile != null) {
            output(LOG.class, "Stream saved: " + logFilePath);
            logFile.close();
            logFile = null;
        }
    }

    public void setShowStamp(boolean showStamp) {
        this.showStamp = showStamp;
    }

    
 
}
