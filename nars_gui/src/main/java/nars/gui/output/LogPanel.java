//package nars.gui.output;
//
//import automenta.vivisect.swing.AwesomeButton;
//import automenta.vivisect.swing.AwesomeToggleButton;
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.NSliderSwing;
//import nars.Events.ERR;
//import nars.Events.EXE;
//import nars.NAR;
//import nars.Symbols;
//import nars.event.NARReaction;
//import nars.gui.WrapLayout;
//import nars.io.TraceWriter;
//import nars.io.TraceWriter.LogOutput;
//import nars.io.out.Output;
//import nars.task.Sentence;
//import nars.task.Task;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ContainerEvent;
//import java.awt.event.ContainerListener;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//abstract public class LogPanel extends NPanel {
//
//    protected final NAR nar;
//    protected final JPanel menu;
//
//    private NARReaction out;
//
//    public static final int maxIOTextSize = (int) 3E5;
//    public static final int clearMargin = (int) 3E4;
//
//    protected boolean showErrors = true;
//    protected boolean showStamp = false;
//    protected boolean showQuestions = true;
//    protected boolean showStatements = true;
//    protected boolean showExecutions = true;
//
//    /**
//     * the log file
//     */
//    protected PrintWriter logFile = null;
//
//    private final TraceWriter logger;
//    private String logFilePath;
//
//    public static final Class[] outputEvents = Output.DefaultOutputEvents;
//
//
//
//    public LogPanel(NAR c) {
//        this(c, outputEvents);
//    }
//
//    public LogPanel(NAR n, Class... events) {
//        super();
//        setLayout(new BorderLayout());
//
//        this.nar = n;
//
//        this.logger = new TraceWriter(nar, false);
//
//        out = new NARReaction(nar, false, events) {
//            @Override public void event(final Class event, final Object[] arguments) {
//                LogPanel.this.output(event, arguments.length > 1 ? arguments : arguments[0]);
//            }
//        };
//
//        //JPanel menuBottom = new JPanel(new WrapLayout(FlowLayout.RIGHT, 0, 0));
//        menu = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));
//
//        //menuBottom.setOpaque(false);
//        //menuBottom.setBorder(new EmptyBorder(0,0,0,0));
//        menu.setOpaque(false);
//        menu.setBorder(new EmptyBorder(0, 0, 0, 0));
//
//
//        JButton clearButton = new AwesomeButton('\uf016');
//        clearButton.setToolTipText("Clear");
//        clearButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                clearLog();
//            }
//        });
//        menu.add(clearButton);
//
//        final String defaultStreamButtonLabel = "Stream to File..";
//        final JToggleButton streamButton = new AwesomeToggleButton('\uf0c7', '\uf052');
//        streamButton.setToolTipText(defaultStreamButtonLabel);
//        streamButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                if (streamButton.isSelected()) {
//                    if (!openLogFile()) {
//                        streamButton.setSelected(false);
//                    } else {
//                        streamButton.setToolTipText("Streaming...");
//                    }
//                } else {
//                    streamButton.setToolTipText(defaultStreamButtonLabel);
//                    closeLogFile();
//                }
//            }
//        });
//        menu.add(streamButton);
//
//        menu.add(Box.createHorizontalStrut(4));
//
//        final JToggleButton showStatementsBox = new JToggleButton(".");
//        showStatementsBox.setToolTipText("Show Statements");
//        showStatementsBox.setSelected(showStatements);
//        showStatementsBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showStatements = showStatementsBox.isSelected();
//            }
//        });
//        menu.add(showStatementsBox);
//
//        final JToggleButton showQuestionsBox = new JToggleButton("?");
//        showQuestionsBox.setToolTipText("Show Questions");
//        showQuestionsBox.setSelected(showQuestions);
//        showQuestionsBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showQuestions = showQuestionsBox.isSelected();
//            }
//        });
//        menu.add(showQuestionsBox);
//
//        final JToggleButton showExecutionsBox = new JToggleButton("!");
//        showExecutionsBox.setToolTipText("Show Goals & Executions");
//        showExecutionsBox.setSelected(showExecutions);
//        showExecutionsBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showExecutions = showExecutionsBox.isSelected();
//            }
//        });
//        menu.add(showExecutionsBox);
//
//        final JToggleButton showErrorBox = new JToggleButton("Errors");
//        showErrorBox.setSelected(showErrors);
//        showErrorBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showErrors = showErrorBox.isSelected();
//            }
//        });
//        menu.add(showErrorBox);
//
//        final JToggleButton showStampBox = new JToggleButton("Stamp");
//        showStampBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                setShowStamp(showStampBox.isSelected());
//            }
//        });
//        menu.add(showStampBox);
//
//        final JToggleButton showTraceBox = new JToggleButton("Trace");
//        showTraceBox.setEnabled(true);
//        showTraceBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                setTrace(showTraceBox.isSelected());
//            }
//        });
//        setTrace(showTraceBox.isSelected());
//        menu.add(showTraceBox);
//
//        menu.add(Box.createHorizontalStrut(4));
//
//
//        final NSliderSwing fontSlider = new NSliderSwing(12f, 6f, 40f) {
//
//            @Override
//            public void onChange(float v) {
//                setFontSize(v);
//            }
//
//        };
//        fontSlider.setPrefix("Font size: ");
//        menu.add(fontSlider);
//
//
//        //add(menuBottom, BorderLayout.SOUTH);
//        add(menu, BorderLayout.NORTH);
//
//        addContainerListener(new ContainerListener() {
//
//            @Override
//            public void componentAdded(ContainerEvent e) {
//            }
//
//            @Override
//            public void componentRemoved(ContainerEvent e) {
//            }
//        });
//
//    }
//
//    abstract public void setFontSize(float v);
//    abstract protected void clearLog();
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//
//        if (!appearedOrDisappeared) {
//
//        }
//
//        out.setActive(appearedOrDisappeared);
//    }
//
//    public void output(final Class c, Object o) {
//
//        if (!isShowing()) {
//            if (out.isActive()) out.off();
//            return;
//        }
//
//        if ((c == ERR.class) && (!showErrors)) {
//            return;
//        }
//        if ((c == EXE.class) && !showExecutions) {
//            return;
//        }
//
//        if (o instanceof Task) {
//
//            Sentence s = ((Task) o);
//            if (s!=null) {
//                if (s.isQuestion() && !showQuestions) {
//                    return;
//                }
//                if (s.isJudgment() && !showStatements) {
//                    return;
//                }
//                if (s.isGoal()&& !showExecutions) {
//                    return;
//                }
//            }
//
//        }
//
//        print(c, o);
//
//    }
//
//    abstract void print(Class c, Object o);
//
//    abstract void limitBuffer(int incomingDataSize);
//
////
////    public static Color getChannelColor(Class c) {
////
////        switch (c.getSimpleName()) {
////            case "OUT":
////                return Color.GREEN;
////            case "IN":
////                return Color.YELLOW;
////            case "ERR":
////                return Color.ORANGE;
////        }
////
////        return Color.GRAY;
////    }
//
//    final static Color getPriorityColor(final float priority) {
//        return new Color(priority, priority, priority);
//    }
//    final static Color getFrequencyColor(final float frequency) {
//        return new Color(1.0f - frequency, frequency, 0);
//    }
//    final static Color getConfidenceColor(final float confidence) {
//        return new Color(0,0,confidence);
//    }
//
//    final static Color getStatementColor(final char punctuation, final float priority) {
//
//        float r = 1f, g = 1f, b = 1f;
//        switch (punctuation) {
//            case Symbols.GOAL: r = 1f; g = 0.75f; b = 0f; break;
//            case Symbols.QUESTION: b = 1f; r = 0.3f; g = 0f; break;
//            case Symbols.QUEST: r = 0.2f; g = 1f; b = 0.2f; break; //solution
//            case Symbols.JUDGMENT: break;
//
//        }
//        r *= 0.4f + 0.6f*priority;
//        g *= 0.4f + 0.6f*priority;
//        b *= 0.4f + 0.6f*priority;
//        return new Color(r, g, b);
//    }
//
//
//    public static final class LOG {
//    }
//
//
//    @Override
//    public void traceAppend(Object channel, String s) {
//        output(LOG.class, channel + ": " + s);
//    }
//
//    public void setTrace(boolean b) {
//        if (b) {
//            logger.addOutput(this);
//            logger.setActive(true);
//        } else {
//            logger.setActive(false);
//            logger.removeOutput(this);
//        }
//    }
//
//    public boolean openLogFile() {
//        FileDialog dialog = new FileDialog((Dialog) null, "Inference Log", FileDialog.SAVE);
//
//        dialog.setVisible(true);
//        String directoryName = dialog.getDirectory();
//        logFilePath = dialog.getFile();
//        if (logFilePath == null) {
//            return false;
//        }
//
//        try {
//            boolean append = true;
//            boolean autoflush = true;
//            logFile = new PrintWriter(new FileWriter(directoryName + logFilePath, append), autoflush);
//            output(LOG.class, "Stream opened: " + logFilePath);
//            return true;
//        } catch (IOException ex) {
//            output(ERR.class, "Log file save: I/O error: " + ex.getMessage());
//        }
//
//        return false;
//    }
//
//    public void closeLogFile() {
//        if (logFile != null) {
//            output(LOG.class, "Stream saved: " + logFilePath);
//            logFile.close();
//            logFile = null;
//        }
//    }
//
//    public void setShowStamp(boolean showStamp) {
//        this.showStamp = showStamp;
//    }
//
//
//
// }
