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

import static com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler.BUFFER_SIZE;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import nars.entity.Concept;
import nars.entity.Task;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.io.OutputChannel;
import nars.core.Parameters;
import nars.inference.InferenceRecorder;
import nars.storage.Memory;

/**
 * Main window of NARSwing GUI
 */
public class NARWindow extends Window implements ActionListener, OutputChannel, Runnable {

    final int TICKS_PER_TIMER_LABEL_UPDATE = 4*1024; //set to zero for max speed, or a large number to reduce GUI updates

    /**
     * Reference to the reasoner
     */
    private final NARSwing reasoner;
    
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
    private final JButton stopButton, walkButton;

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
     * Windows for run-time parameter adjustment
     */
    public ParameterWindow forgetTW, forgetBW, forgetCW;

    /**
     * To process the next chunk of output data
     *
     * @param lines The text lines to be displayed
     */
    private StringBuffer nextOutput = new StringBuffer();
    private JSlider speedSlider;
    private double currentSpeed = -1;

    private final int GUIUpdatePeriodMS = 768;
    int maxIOTextSize = (int)8E6;
    
    /**
     * Constructor
     *
     * @param reasoner
     * @param title
     */
    public NARWindow(NARSwing reasoner, String title) {
        super(title);
        this.reasoner = reasoner;
        memory = reasoner.getMemory();
        record = memory.getRecorder();
        experienceWriter = new TextOutput(reasoner);
        conceptWin = new TermWindow(memory);
        forgetTW = new ParameterWindow("Task Forgetting Rate", Parameters.TASK_LINK_FORGETTING_CYCLE, memory.getTaskForgettingRate());
        forgetBW = new ParameterWindow("Belief Forgetting Rate", Parameters.TERM_LINK_FORGETTING_CYCLE, memory.getBeliefForgettingRate());
        forgetCW = new ParameterWindow("Concept Forgetting Rate", Parameters.CONCEPT_FORGETTING_CYCLE, memory.getConceptForgettingRate());
        /*silentW = new ParameterWindow("Report Silence Level", Parameters.SILENT_LEVEL, reasoner.getSilenceValue());*/

        record = new InferenceLogger();
        memory.setRecorder(record);

        getContentPane().setBackground(MAIN_WINDOW_COLOR);
        JMenuBar menuBar = new JMenuBar();

        JMenu m = new JMenu("File");
        addJMenuItem(m, "Load Experience");
        addJMenuItem(m, "Save Experience");
        m.addSeparator();
        addJMenuItem(m, "Record Inference");
        m.addActionListener(this);
        menuBar.add(m);

        m = new JMenu("Memory");
        addJMenuItem(m, "Initialize");
        m.addActionListener(this);
        menuBar.add(m);

        m = new JMenu("View");
        addJMenuItem(m, "Concepts");
        addJMenuItem(m, "Buffered Tasks");
        addJMenuItem(m, "Concept Content");
        addJMenuItem(m, "Inference Log");
        addJMenuItem(m, "Input Window");
        m.addActionListener(this);
        menuBar.add(m);

        m = new JMenu("Parameter");
        addJMenuItem(m, "Concept Forgetting Rate");
        addJMenuItem(m, "Task Forgetting Rate");
        addJMenuItem(m, "Belief Forgetting Rate");
        m.addActionListener(this);
        menuBar.add(m);

        m = new JMenu("Help");
        addJMenuItem(m, "Related Information");
        addJMenuItem(m, "About NARS");
        m.addActionListener(this);
        menuBar.add(m);

        setJMenuBar(menuBar);

        setLayout(new BorderLayout());

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
        
        c.weightx = 0.1;
        menu.add(newVolumeSlider(), c);

        c.weightx = 0.0;
        walkButton = new JButton("Walk");
        walkButton.addActionListener(this);
        menu.add(walkButton, c);
                
        
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        menu.add(stopButton, c);
        
        c.weightx = 0.1;
        menu.add(newSpeedSlider(), c);
        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.1;


        c.weightx = 0.4;
        c.gridwidth = GridBagConstraints.REMAINDER;
 
        inputWindow = new InputPanel(reasoner);
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

        while ((s = r.readLine())!=null) {
            ioText.append(s + "\n");            
            new TextInput(reasoner, s); //TODO use a stream to avoid reallocate experiencereader
        }
    }
    
    /**
     * Initialize the system for a new run
     */
    public void init() {
        setSpeed(0);
        updateTimer();
        ioText.setText("");
        nextOutput(null);
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
                reasoner.walk(1, true);
                updateTimer();
            }
        } else if (obj instanceof JMenuItem) {
            String label = e.getActionCommand();
            if (label.equals("Load Experience")) {
                experienceReader = new TextInput(reasoner);
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
            } else if (label.equals("Initialize")) {
                /// TODO mixture of modifier and reporting
                reasoner.reset();
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
            } else if (label.equals("Input Window")) {
                inputWindow.setVisible(!inputWindow.isVisible());
            } else if (label.equals("Task Forgetting Rate")) {
                forgetTW.setVisible(true);
            } else if (label.equals("Belief Forgetting Rate")) {
                forgetBW.setVisible(true);
            } else if (label.equals("Concept Forgetting Rate")) {
                forgetCW.setVisible(true);
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
    public void nextOutput(final ArrayList<String> lines) {
            
        if (lines != null) {
            for (Object line : lines) {
                nextOutput.append(line).append("\n");
            }
        }
        
        SwingUtilities.invokeLater(nextOutputRunnable);
    }
    void limitBuffer(int incomingDataSize)    {
       Document doc = ioText.getDocument();
       int overLength = doc.getLength() + incomingDataSize - maxIOTextSize;

       if (overLength > 0)       {
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
    



    private JSlider newSpeedSlider() {
        //Create the slider
        final double RANGE = 100.0;
        final JSlider s = new JSlider(JSlider.HORIZONTAL, 0, (int)100, 0) {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(Color.BLACK);
                String s = "@" + memory.getTime();
                if (currentSpeed == 0)
                    s += " - pause";
                else if (currentSpeed == 1.0)
                    s += " - run max speed";
                else
                    s += " - run " + reasoner.getMinTickPeriodMS() + " ms / tick";
                g.drawString(s, 4, 14);

            }
          
        };
        s.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                double speed = ((double)s.getValue())/RANGE;
                setSpeed(speed);
            }
            
        });

        s.setMajorTickSpacing((int)(RANGE/4));
        s.setPaintTicks(true);
        
        //Create the label table
        
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( 0 ), new JLabel("Paused") );
        labelTable.put( new Integer( 100 ), new JLabel("Fast") );
        s.setLabelTable( labelTable );
        

        s.setPaintLabels(true);
        
        this.speedSlider = s;
        
        return s;
    }
    
    private JSlider newVolumeSlider() {
        final JSlider s = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        s.setMajorTickSpacing(25);        
        s.setSize(new Dimension(50, 50));

        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( 0 ), new JLabel("Silent") );
        labelTable.put( new Integer( 100 ), new JLabel("Loud") );
        s.setLabelTable( labelTable );
        
        s.setPaintTicks(true);

        s.setPaintLabels(true);
        s.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int level = 100 - s.getValue();
                reasoner.setSilenceValue(level);
            }
            
        });
           
        return s;
    }
    
    public void setSpeed(final double s) {
        final double maxPeriodMS = 256.0;
        
        if (currentSpeed == s)
            return;
        
        currentSpeed = s;
        
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (s == 0) {
                    reasoner.stop();
                    speedSlider.setValue(0);
                    stopButton.setEnabled(false);
                }
                else {
                    stopButton.setEnabled(true);
                    int ms = (int)((1.0 - (s)) * maxPeriodMS);
                    if (ms < 1) ms = 0;
                    reasoner.start(ms);
                }
                
                speedSlider.repaint();
            }

        });
        
    }

    @Override
    public void run() {
        while (true) {
            speedSlider.repaint();
            
            try {
                Thread.sleep(GUIUpdatePeriodMS);
            } catch (InterruptedException ex) {            }
        }
    }
}
