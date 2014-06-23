/*
 * NARWindow.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARSwing.
 *
 * Open-NARSwing is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARSwing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARSwing.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import nars.entity.Concept;
import nars.entity.Task;
import nars.inference.InferenceRecorder;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.storage.Memory;

/**
 * Main window of NARSwing GUI
 */
public class NARWindow extends Window implements ActionListener, Output, Runnable {
    
    final int TICKS_PER_TIMER_LABEL_UPDATE = 4 * 1024; //set to zero for max speed, or a large number to reduce GUI updates

    /**
     * Reference to the reasoner
     */
    private final NARSwing nar;

    /**
     * Reference to the memory
     */
    private final Memory memory;
    /**
     * Reference to the inference recorder
     */
    private InferenceRecorder record;
    /**
     * Reference to the experience reader
     */
    private TextInput experienceReader;
    /**
     * Reference to the experience writer
     */
    private final TextOutput experienceWriter;
    /**
     * Experience display area
     */
    private final JTextArea ioText;
    /**
     * Control buttons
     */
    private JButton stopButton, walkButton;

    /**
     * Whether the experience is saving into a file
     */
    private boolean savingExp = false;

    /**
     * Input experience window
     */
    public InputPanel inputWindow;
    /**
     * JWindow to accept a Term to be looked into
     */
    public TermWindow conceptWin;

    /**
     * To process the next chunk of output data
     *
     * @param lines The text lines to be displayed
     */
    private StringBuffer nextOutput = new StringBuffer();
    private NSlider speedSlider;
    private double currentSpeed = 0;
    private double lastSpeed = 0;
    private double defaultSpeed = 0.5;
    
    private final int GUIUpdatePeriodMS = 768;
    int maxIOTextSize = (int) 8E6;
    private NSlider volumeSlider;
    private boolean showErrors = false;

    /**
     * Constructor
     *
     * @param nar
     * @param title
     */
    public NARWindow(final NARSwing nar, String title) {
        super(title);
        this.nar = nar;
        memory = nar.getMemory();
        record = memory.getRecorder();
        experienceWriter = new TextOutput(nar);
        conceptWin = new TermWindow(memory);

        
        record = new InferenceLogger();
        memory.setRecorder(record);
        
        getContentPane().setBackground(MAIN_WINDOW_COLOR);
        JMenuBar menuBar = new JMenuBar();
        
        
        JMenu m = new JMenu("Memory");
        addJMenuItem(m, "Reset");
        m.addSeparator();        
        addJMenuItem(m, "Load Experience");
        addJMenuItem(m, "Save Experience");
        m.addSeparator();
        addJMenuItem(m, "Record Inference");
        m.addActionListener(this);
        menuBar.add(m);
        
        m = new JMenu("View");
        {
            JMenuItem mv = new JMenuItem("Memory View");        
            mv.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new MemoryView(nar.memory);
                }                
            });
            m.add(mv);
        }
        
        addJMenuItem(m, "Concepts");
        addJMenuItem(m, "Buffered Tasks");
        addJMenuItem(m, "Concept Content");
        addJMenuItem(m, "Inference Log");
        addJMenuItem(m, "Input Window");
        m.addActionListener(this);
        menuBar.add(m);        
        
        m = new JMenu("Help");
        addJMenuItem(m, "Related Information");
        addJMenuItem(m, "About NARS");
        m.addActionListener(this);
        menuBar.add(m);
        
        setJMenuBar(menuBar);
        
        setLayout(new BorderLayout());
        
        JComponent jp = newParameterPanel();
        jp.setPreferredSize(new Dimension(250, 120));
        add(jp, BorderLayout.WEST);
        
        GridBagConstraints c = new GridBagConstraints();
        ioText = new JTextArea("");        
        ioText.setBackground(DISPLAY_BACKGROUND_COLOR);
        ioText.setEditable(false);        
        JScrollPane scrollPane = new JScrollPane(ioText);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel menu = new JPanel(new GridBagLayout());        
        menu.setOpaque(false);
        add(menu, BorderLayout.SOUTH);
        
        c.ipadx = 2;
        c.ipady = 2;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        c.gridheight = 10;
        
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.VERTICAL;        
        
        c.gridheight = GridBagConstraints.REMAINDER;
        c.weightx = 0.0;        
        c.weighty = 0.0;        
        c.fill = GridBagConstraints.BOTH;        
        
        c.weightx = 0.0;
        
        menu.add(newControlPanel(), c);
        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.1;
        
        c.weightx = 0.4;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        inputWindow = new InputPanel(nar);
        menu.add(inputWindow, c);
        
        setBounds(0, 200, 810, 600);
        setVisible(true);
        
        init();
        
        new Thread(this).start();
    }

    /**
     * @param m
     * @param item
     */
    private void addJMenuItem(JMenu m, String item) {
        JMenuItem menuItem = new JMenuItem(item);
        m.add(menuItem);
        menuItem.addActionListener(this);
    }

    /**
     * Open an input experience file with a FileDialog
     */
    public void openLoadFile() {
        FileDialog dialog = new FileDialog((FileDialog) null, "Load experience", FileDialog.LOAD);
        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        String fileName = dialog.getFile();
        String filePath = directoryName + fileName;
        
        try {
            loadFile(filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void loadFile(String filePath) throws IOException, FileNotFoundException {        
        BufferedReader r = new BufferedReader(new FileReader(filePath));
        String s;
        
        while ((s = r.readLine()) != null) {
            ioText.append(s + "\n");            
            new TextInput(nar, s); //TODO use a stream to avoid reallocate experiencereader
        }
    }

    /**
     * Initialize the system for a new run
     */
    public void init() {
        setSpeed(0);
        setSpeed(0);        //call twice to make it start as paused
        updateTimer();
        ioText.setText("");
    }

    /**
     * Update timer and its display
     */
    final Runnable _updateTimer = new Runnable() {
        @Override
        public void run() {
            speedSlider.repaint();
        }
    };
    
    protected void updateTimer() {
        SwingUtilities.invokeLater(_updateTimer);        
    }

    /**
     * Handling button click
     *
     * @param e The ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj instanceof JButton) {
            if (obj == stopButton) {
                setSpeed(0);
                updateTimer();
            } else if (obj == walkButton) {
                nar.walk(1, true);
                updateTimer();
            }
        } else if (obj instanceof JMenuItem) {
            String label = e.getActionCommand();
            if (label.equals("Load Experience")) {
                experienceReader = new TextInput(nar);
                openLoadFile();
            } else if (label.equals("Save Experience")) {
                if (savingExp) {
                    ioText.setBackground(DISPLAY_BACKGROUND_COLOR);
                    experienceWriter.closeSaveFile();
                } else {
                    ioText.setBackground(SAVING_BACKGROUND_COLOR);
                    experienceWriter.openSaveFile();
                }
                savingExp = !savingExp;
            } else if (label.equals("Record Inference")) {
                if (record.isLogging()) {
                    record.closeLogFile();
                } else {
                    record.openLogFile();
                }
            } else if (label.equals("Reset")) {
                /// TODO mixture of modifier and reporting
                nar.reset();
                ioText.setText("");
            } else if (label.equals("Concepts")) {
                /* see design for Bag and {@link BagWindow} in {@link Bag#startPlay(String)} */
                memory.conceptsStartPlay(new BagWindow<Concept>(), "Active Concepts");
            } else if (label.equals("Buffered Tasks")) {
                memory.taskBuffersStartPlay(new BagWindow<Task>(), "Buffered Tasks");
            } else if (label.equals("Concept Content")) {
                conceptWin.setVisible(true);
            } else if (label.equals("Inference Log")) {
                record.show();
                record.play();
            } else if (label.equals("Related Information")) {
//                MessageDialog web = 
                new MessageDialog(this, NARSwing.WEBSITE);
            } else if (label.equals("About NARS")) {
//                MessageDialog info = 
                new MessageDialog(this, NARSwing.INFO);
            } else {
//                MessageDialog ua = 
                new MessageDialog(this, UNAVAILABLE);
            }
        }
    }

    /**
     * Close the whole system
     */
    private void close() {
        setVisible(false);
        System.exit(0);
    }
    
    @Override
    public void windowClosing(WindowEvent arg0) {
        close();
    }

    /**
     *
     * @param lines if null, forces output when updateExperienceOutput is false
     */
    @Override
    public void output(final Channel c, final Object o) {
        
        if ((!showErrors) && (c == Channel.ERR)) {
            return;
        }
        
        final String line = c.toString() + ": " + o.toString() + "\n";
        
        nextOutput.append(line);        
        
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
    
    private Runnable nextOutputRunnable = new Runnable() {
        @Override
        public void run() {
            if (nextOutput.length() > 0) {
                
                limitBuffer(nextOutput.length());                
                ioText.append(nextOutput.toString());
                nextOutput.setLength(0);
            }
        }
    };
    
    private NSlider newSpeedSlider() {
        final NSlider s = new NSlider(0, 0, 1.0) {
            
            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }
                
                double v = value();
                
                String s = "@" + memory.getTime();
                
                if (currentSpeed == 0) {
                    s += " - pause";
                } else if (currentSpeed == 1.0) {
                    s += " - run max speed";
                } else {
                    s += " - run " + nar.getMinTickPeriodMS() + " ms / tick";
                }
                return s;
            }
            
            @Override
            public void onChange(double v) {
                setSpeed(v);
            }
            
        };
        this.speedSlider = s;
        
        return s;        
    }
    
    private NSlider newVolumeSlider() {
        final NSlider s = this.volumeSlider = new NSlider(100, 0, 100) {
            
            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }
                
                double v = value();
                String s = "Volume: " + super.getText() + " (";
                
                if (v == 0) {
                    s += "Silent";
                } else if (v < 25) {
                    s += "Quiet";
                } else if (v < 75) {
                    s += "Normal";
                } else {
                    s += "Loud";
                }
                
                s += ")";
                return s;
            }
            
            @Override
            public void setValue(double v) {
                super.setValue(Math.round(v));                
            }
            
            @Override
            public void onChange(double v) {
                int level = 100 - (int) v;
                nar.setSilenceValue(level);
            }
            
        };
        
        return s;
    }
    
    public void setSpeed(double nextSpeed) {
        final double maxPeriodMS = 256.0;
        
        if (nextSpeed == 0) {
            if (currentSpeed == 0) {
                if (lastSpeed == 0) {
                    lastSpeed = defaultSpeed;
                }
                nextSpeed = lastSpeed;
            } else {
            }
            
        }
        lastSpeed = currentSpeed;
        speedSlider.repaint();
        stopButton.setText("Resume");

        /*if (currentSpeed == s)
         return;*/
        speedSlider.setValue(nextSpeed);
        currentSpeed = nextSpeed;
        
        if (nextSpeed > 0) {
            int ms = (int) ((1.0 - (nextSpeed)) * maxPeriodMS);
            if (ms < 1) {
                ms = 0;
            }
            stopButton.setText("Stop");
            nar.start(ms);            
        } else {
            stopButton.setText("Resume");
            nar.stop();            
        }
    }
    
    @Override
    public void run() {
        while (true) {
            speedSlider.repaint();
            
            try {
                Thread.sleep(GUIUpdatePeriodMS);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private JComponent newParameterPanel() {
        JPanel p = new JPanel();
        
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.ipady = 16;
        
        NSlider vs = newVolumeSlider();
        vs.setFont(vs.getFont().deriveFont(Font.BOLD));
        p.add(vs, c);        
        
        NSlider ss = newSpeedSlider();
        ss.setFont(vs.getFont());
        p.add(ss, c);
        
        
        TreeModel model = new FileTreeModel(new File("./nal"));
        final JTree fileTree = new JTree(model);        
        fileTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                    } else if (e.getClickCount() == 2) {
                        //DoubleClick
                        File f = (File)selPath.getLastPathComponent();
                        
                        if (!f.isDirectory()) {
                            try {
                                loadFile(f.getAbsolutePath());
                            } catch (IOException ex) {
                                System.err.println(ex);
                            }
                        }
                    }
                }
            }
        });
        
        c.ipady = 4;
        
        p.add(newIntSlider(memory.getTaskForgettingRate(), "Task Forgetting Rate", 1, 99), c);
        p.add(newIntSlider(memory.getBeliefForgettingRate(), "Belief Forgetting Rate", 1, 99), c);
        p.add(newIntSlider(memory.getConceptForgettingRate(), "Concept Forgetting Rate", 1, 99), c);
        
        final JCheckBox showErrorBox = new JCheckBox("Show Errors");
        showErrorBox.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                showErrors = showErrorBox.isSelected();
            }
            
        });
        p.add(showErrorBox, c);
        

        c.fill = c.BOTH;
        c.weighty = 1.0;
        p.add(Box.createVerticalBox(), c);
        
        JTabbedPane t = new JTabbedPane();
        t.addTab("Options", new JScrollPane(p));
        t.addTab("Files", new JScrollPane(fileTree));
        return t;
    }
    
    private NSlider newIntSlider(final AtomicInteger x, final String prefix, int min, int max) {
        final NSlider s = new NSlider(x.intValue(), min, max) {
            
            @Override
            public String getText() {
                return prefix + ": " + super.getText();                
            }
            
            @Override
            public void setValue(double v) {
                int i = (int) Math.round(v);
                super.setValue(i);                
                x.set(i);
            }
            
            @Override
            public void onChange(double v) {
            }            
        };
        
        return s;        
    }
    
    private Component newControlPanel() {
        JPanel p = new JPanel();
        
        p.setLayout(new GridLayout(0, 1));
        
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        p.add(stopButton);
        
        walkButton = new JButton("Walk");
        walkButton.addActionListener(this);
        p.add(walkButton);
        
        JButton focusButton = new JButton("Focus");
        focusButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeed(1.0);
                volumeSlider.setValue(20);
            }
            
        });
        p.add(focusButton);
        
        return p;
    }
    
}
