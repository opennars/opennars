/*
 * MainWindow.java
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
package nars.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nars.entity.Concept;
import nars.entity.Task;
import nars.io.ExperienceReader;
import nars.io.ExperienceWriter;
import nars.io.IInferenceRecorder;
import nars.io.OutputChannel;
import nars.main.NARS;
import nars.main.Reasoner;
import nars.main_nogui.Parameters;
import nars.main_nogui.NAR;
import nars.storage.Memory;

/**
 * Main window of NARS GUI
 */
public class MainWindow extends NarsFrame implements ActionListener, OutputChannel {

    final int TICKS_PER_TIMER_LABEL_UPDATE = 4*1024; //set to zero for max speed, or a large number to reduce GUI updates

    /**
     * Reference to the reasoner
     */
    private final NAR reasoner;
    /**
     * Reference to the memory
     */
    private final Memory memory;
    /**
     * Reference to the inference recorder
     */
    private IInferenceRecorder record;
    /**
     * Reference to the experience reader
     */
    private ExperienceReader experienceReader;
    /**
     * Reference to the experience writer
     */
    private final ExperienceWriter experienceWriter;
    /**
     * Experience display area
     */
    private final JTextArea ioText;
    /**
     * Control buttons
     */
    private final JButton stopButton, walkButton, runButton, exitButton;
    /**
     * Clock display field
     */
    private final JTextField timerText;
    /**
     * JLabel of the clock
     */
    private final JLabel timerLabel;
    /**
     * System clock - number of cycles since last output
     */
    private long timer;
    /**
     * Whether the experience is saving into a file
     */
    private boolean savingExp = false;

    /**
     * Input experience window
     */
    public InputWindow inputWindow;
    /**
     * JWindow to accept a Term to be looked into
     */
    public TermWindow conceptWin;
    /**
     * Windows for run-time parameter adjustment
     */
    public ParameterWindow forgetTW, forgetBW, forgetCW, silentW;

    /**
     * Constructor
     *
     * @param reasoner
     * @param title
     */
    public MainWindow(Reasoner reasoner, String title) {
        super(title);
        this.reasoner = reasoner;
        memory = reasoner.getMemory();
        record = memory.getRecorder();
        experienceWriter = new ExperienceWriter(reasoner);
        inputWindow = reasoner.getInputWindow();
        conceptWin = new TermWindow(memory);
        forgetTW = new ParameterWindow("Task Forgetting Rate", Parameters.TASK_LINK_FORGETTING_CYCLE, memory.getTaskForgettingRate());
        forgetBW = new ParameterWindow("Belief Forgetting Rate", Parameters.TERM_LINK_FORGETTING_CYCLE, memory.getBeliefForgettingRate());
        forgetCW = new ParameterWindow("Concept Forgetting Rate", Parameters.CONCEPT_FORGETTING_CYCLE, memory.getConceptForgettingRate());
        silentW = new ParameterWindow("Report Silence Level", Parameters.SILENT_LEVEL, reasoner.getSilenceValue());

        record = new InferenceRecorder();
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
        m.addSeparator();
        addJMenuItem(m, "Report Silence Level");
        m.addActionListener(this);
        menuBar.add(m);

        m = new JMenu("Help");
        addJMenuItem(m, "Related Information");
        addJMenuItem(m, "About NARS");
        m.addActionListener(this);
        menuBar.add(m);

        setJMenuBar(menuBar);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        c.ipadx = 3;
        c.ipady = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;

        ioText = new JTextArea("");
        ioText.setBackground(DISPLAY_BACKGROUND_COLOR);
        ioText.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(ioText);
        gridbag.setConstraints(scrollPane, c);
        add(scrollPane);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;

        runButton = new JButton(" Run ");
        gridbag.setConstraints(runButton, c);
        runButton.addActionListener(this);
        add(runButton);
        walkButton = new JButton(" Walk ");
        gridbag.setConstraints(walkButton, c);
        walkButton.addActionListener(this);
        add(walkButton);
        stopButton = new JButton(" Stop ");
        gridbag.setConstraints(stopButton, c);
        stopButton.addActionListener(this);
        add(stopButton);
        timerLabel = new JLabel("Clock:", JLabel.RIGHT);
        timerLabel.setBackground(MAIN_WINDOW_COLOR);
        gridbag.setConstraints(timerLabel, c);
        add(timerLabel);

        c.weightx = 1.0;
        timerText = new JTextField("");
        timerText.setBackground(DISPLAY_BACKGROUND_COLOR);
        timerText.setEditable(false);
        gridbag.setConstraints(timerText, c);
        add(timerText);

        c.weightx = 0.0;
        exitButton = new JButton(" Exit ");
        gridbag.setConstraints(exitButton, c);
        exitButton.addActionListener(this);
        add(exitButton);

        setBounds(0, 200, 600, 560);
        setVisible(true);

        initTimer();
        nextOutput(null);
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
     * Initialize the system for a new run
     */
    public void init() {
        initTimer();
        ioText.setText("");
        nextOutput(null);
    }

    /**
     * Reset timer and its display
     */
    public void initTimer() {
        timer = 0;
        if (reasoner.isRunning()) {
            if (TICKS_PER_TIMER_LABEL_UPDATE == 0)
                return;
            if (memory.getTime() % TICKS_PER_TIMER_LABEL_UPDATE != 0)
                return;
        }
        SwingUtilities.invokeLater(updateTimer);
    }

    /**
     * Update timer and its display
     */
    final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            timerText.setText(memory.getTime() + " :: " + timer);
        }
    };

    @Override
    public void tickTimer() {
        timer++;
        if (reasoner.isRunning()) {
            if (TICKS_PER_TIMER_LABEL_UPDATE == 0)
                return;
            if (memory.getTime() % TICKS_PER_TIMER_LABEL_UPDATE != 0)
                return;
        }
        SwingUtilities.invokeLater(updateTimer);
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
            if (obj == runButton) {
                nextOutput(null);
                timerLabel.setText("Running..");
                reasoner.start(0);
            } else if (obj == stopButton) {
                nextOutput(null);
                reasoner.stop();
                SwingUtilities.invokeLater(updateTimer);
            } else if (obj == walkButton) {
                nextOutput(null);
                reasoner.walk(1);
                nextOutput(null);
                SwingUtilities.invokeLater(updateTimer);
            } else if (obj == exitButton) {
                close();
            }
        } else if (obj instanceof JMenuItem) {
            String label = e.getActionCommand();
            if (label.equals("Load Experience")) {
                experienceReader = new ExperienceReader(reasoner);
                experienceReader.openLoadFile();
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
                memory.getExportStrings().add("*****RESET*****");
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
                inputWindow.setVisible(true);
            } else if (label.equals("Task Forgetting Rate")) {
                forgetTW.setVisible(true);
            } else if (label.equals("Belief Forgetting Rate")) {
                forgetBW.setVisible(true);
            } else if (label.equals("Concept Forgetting Rate")) {
                forgetCW.setVisible(true);
            } else if (label.equals("Report Silence Level")) {
                silentW.setVisible(true);
            } else if (label.equals("Related Information")) {
//                MessageDialog web = 
                new MessageDialog(this, NARS.WEBSITE);
            } else if (label.equals("About NARS")) {
//                MessageDialog info = 
                new MessageDialog(this, NARS.INFO);
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
     * To process the next chunk of output data
     *
     * @param lines The text lines to be displayed
     */
    private StringBuffer undisplayedExperience = new StringBuffer();

    private boolean _firstOutput = true;

    /**
     *
     * @param lines if null, forces output when updateExperienceOutput is false
     */
    @Override
    public void nextOutput(final ArrayList<String> lines) {
        
        if (lines != null) {
            for (Object line : lines) {
                undisplayedExperience.append(line).append("\n");
            }
        }
        if ((lines == null) || _firstOutput) {
            SwingUtilities.invokeLater(nextOutputRunnable);
        }
    }
    
    private Runnable nextOutputRunnable = new Runnable() {
                @Override
                public void run() {
                    if (undisplayedExperience.length() > 0) {
                        _firstOutput = false;
                        ioText.append(undisplayedExperience.toString());
                        undisplayedExperience.setLength(0);
                    }
                }
            };
    

    /**
     * To get the timer value and then to reset it
     *
     * @return The previous timer value
     */
    public long updateTimer() {
        long i = timer;
        initTimer();
        return i;
    }

    /**
     * @return System clock : number of cycles since last output
     */
    public long getTimer() {
        return timer;
    }
}
