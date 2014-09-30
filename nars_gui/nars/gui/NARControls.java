/*
 * NARControls.java
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
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.Observer;
import nars.core.Events.FrameEnd;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.sense.MultiSense;
import nars.gui.input.TextInputPanel;
import nars.gui.input.image.SketchPointCloudPanel;
import nars.gui.output.LogPanel;
import nars.gui.output.SentenceTablePanel;
import nars.gui.output.SwingLogPanel;
import nars.gui.output.TermWindow;
import nars.gui.output.chart.BubbleChart;
import nars.gui.output.chart.ChartsPanel;
import nars.gui.output.face.NARFacePanel;
import nars.gui.output.graph.ConceptGraphCanvas;
import nars.gui.output.graph.ConceptGraphCanvas2;
import nars.gui.output.graph.ImplicationGraphCanvas;
import nars.gui.output.graph.ProcessingGraphPanel;
import nars.gui.output.graph.SentenceGraphCanvas;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.util.graph.InheritanceGraph;


public class NARControls extends JPanel implements ActionListener, Observer {

    final int TICKS_PER_TIMER_LABEL_UPDATE = 4 * 1024; //set to zero for max speed, or a large number to reduce GUI updates

    /**
     * Reference to the reasoner
     */
    public final NAR nar;

    /**
     * Reference to the memory
     */
    private final Memory memory;
    
    /**
     * Reference to the experience writer
     */
    private final TextOutput experienceWriter;


    /**
     * Control buttons
     */
    private JButton stopButton, walkButton;

    /**
     * Whether the experience is saving into a file
     */
    private boolean savingExp = false;


    /**
     * JWindow to accept a Term to be looked into
     */
    public TermWindow conceptWin;

    /**
     * To process the next chunk of output data
     *
     * @param lines The text lines to be displayed
     */
    private NSlider speedSlider;
    private float currentSpeed = 0f;
    private float lastSpeed = 0f;
    private final float defaultSpeed = 0.5f;

    private final int GUIUpdatePeriodMS = 50;
    private NSlider volumeSlider;

    private boolean allowFullSpeed = true;
    public final InferenceLogger logger;

    int chartHistoryLength = 128;
    
    /**
     * Constructor
     *
     * @param nar
     * @param title
     */
    private final JMenuItem internalExperienceItem;
    private final JMenuItem narsPlusItem;
    private final JMenuItem fullInternalExp;
    private ChartsPanel chart;
    private final MultiSense senses;
    public NARControls(final NAR nar) {
        super(new BorderLayout());
        
        this.nar = nar;
        memory = nar.memory;        
        
        senses = new MultiSense(memory.logic, memory.resource);
        senses.setActive(true);
        senses.update(memory);
        
        experienceWriter = new TextOutput(nar);
        conceptWin = new TermWindow(memory);
        
        logger = new InferenceLogger();
        nar.memory.setRecorder(logger);
        
        JMenuBar menuBar = new JMenuBar();

        JMenu m = new JMenu("Memory");
        addJMenuItem(m, "Reset");
        m.addSeparator();
        addJMenuItem(m, "Load Experience");
        addJMenuItem(m, "Save Experience");
        m.addSeparator();
        
        internalExperienceItem = addJMenuItem(m, "Enable Internal Experience (NAL9)");
        fullInternalExp = addJMenuItem(m, "Enable Full Internal Experience");
        narsPlusItem = addJMenuItem(m, "Enable NARS+ Ideas");
        m.addActionListener(this);
        menuBar.add(m);

        /*m = new JMenu("Input");
        {
            JMenuItem mv = new JMenuItem("+ Text Input");
            mv.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputPanel inputPanel = new InputPanel(nar);
                    Window inputWindow = new Window("Text Input", inputPanel);                    
                    inputWindow.setSize(800, 200);
                    inputWindow.setVisible(true);        
                }
            });
            m.add(mv);
            
        }
        menuBar.add(m);*/
        
        m = new JMenu("Windows");
        {
            
            JMenuItem mv3 = new JMenuItem("+ Input");
            mv3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TextInputPanel inputPanel = new TextInputPanel(nar);
                    Window inputWindow = new Window("Input", inputPanel);                    
                    inputWindow.setSize(800, 200);
                    inputWindow.setVisible(true);        
                }
            });
            m.add(mv3);
            
            JMenuItem cct4 = new JMenuItem("+ Input Drawing");
            cct4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Window w = new Window("Sketch", new SketchPointCloudPanel(nar));
                    w.setSize(500,500);
                    w.setVisible(true);
                }                
            });
            m.add(cct4);
            
            JMenuItem ml = new JMenuItem("+ Log");
            ml.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LogPanel p = new SwingLogPanel(NARControls.this);
                    Window w = new Window("Log", p);
                    w.setSize(500, 300);
                    w.setVisible(true);      
                }
            });
            m.add(ml);

            
            JMenuItem mv = new JMenuItem("+ Concept Graph");
            mv.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Window("Concept Graph", new ProcessingGraphPanel(nar, new ConceptGraphCanvas(nar))).show(500, 500);
                }
            });
            m.add(mv);

            JMenuItem mv2 = new JMenuItem("+ Concept Graph 2");
            mv2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Window("Concept Graph", new ProcessingGraphPanel(nar, new ConceptGraphCanvas2(nar))).show(500, 500);
                }
            });
            m.add(mv2);

            
            JMenuItem imv = new JMenuItem("+ Implication Graph");
            imv.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //new Window("Implication Graph", new SentenceGraphPanel(nar, nar.memory.executive.graph.implication)).show(500, 500);
                    new Window("Implication Graph", 
                            new ProcessingGraphPanel(nar, 
                                    new ImplicationGraphCanvas(
                                            nar.memory.executive.graph))).show(500, 500);
                }
            });
            m.add(imv);

            JMenuItem sg = new JMenuItem("+ Inheritance / Similarity Graph");
            sg.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Window("Inheritance Graph", 
                            new ProcessingGraphPanel(nar, 
                                    new SentenceGraphCanvas(
                                            new InheritanceGraph(nar)))).show(500, 500);
                }
            });
            m.add(sg);
            
            
            
            JMenuItem st = new JMenuItem("+ Sentence Table");
            st.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SentenceTablePanel p = new SentenceTablePanel(nar);
                    Window w = new Window("Sentence Table", p);
                    w.setSize(500, 300);
                    w.setVisible(true);                    
                }
            });
            m.add(st);

            JMenuItem fc = new JMenuItem("+ Freq. vs Confidence");
            fc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BubbleChart bc = new BubbleChart(nar);
                    Window wbc = new Window("Freq vs. Conf", bc);
                    wbc.setSize(250,250);
                    wbc.setVisible(true);
                }
            });
            m.add(fc);
            
            JMenuItem hf = new JMenuItem("+ Humanoid Face");
            hf.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NARFacePanel f = new NARFacePanel(nar);
                    Window w = new Window("Face", f);
                    w.setSize(250,400);
                    w.setVisible(true);
                }
            });
            m.add(hf);
            
            
            /*JMenuItem ct = new JMenuItem("+ Concepts");
            ct.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // see design for Bag and {@link BagWindow} in {@link Bag#startPlay(String)} 
                    memory.conceptsStartPlay(new BagWindow<Concept>(), "Active Concepts");                    
                }
            });
            m.add(ct);
            
            JMenuItem bt = new JMenuItem("+ Buffered Tasks");
            bt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    memory.taskBuffersStartPlay(new BagWindow<Task>(), "Buffered Tasks");
                }
            });
            m.add(bt);*/
            
            /*JMenuItem cct = new JMenuItem("+ Concept Content");
            cct.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    conceptWin.setVisible(true);                
                }                
            });
            m.add(cct);*/
            
            
            
            
            /*
            JMenuItem it = new JMenuItem("+ Inference Log");
            it.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    record.show();
                    record.play();
                }                
            });
            m.add(it);            
            */
        }
        menuBar.add(m);
        
//        m = new JMenu("Demos");
//        {
//            JMenuItem cct2 = new JMenuItem("+ Test Chamber");
//            cct2.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    
//                    chamber.create(nar);
//                }                
//            });
//            m.add(cct2);
//        }
//        menuBar.add(m);

        m = new JMenu("Help");
        //addJMenuItem(m, "Related Information");
        addJMenuItem(m, "About NARS");
        m.addActionListener(this);
        menuBar.add(m);

        add(menuBar, BorderLayout.NORTH);


        JComponent jp = newParameterPanel();
        add(jp, BorderLayout.CENTER);

        init();
        
    }

    /**
     * @param m
     * @param item
     */
    private JMenuItem addJMenuItem(JMenu m, String item) {
        JMenuItem menuItem = new JMenuItem(item);
        m.add(menuItem);
        menuItem.addActionListener(this);
        return menuItem;
    }

    /**
     * Open an addInput experience file with a FileDialog
     */
    public void openLoadFile() {
        FileDialog dialog = new FileDialog((Dialog) null, "Load experience", FileDialog.LOAD);
        dialog.setVisible(true);
        String directoryName = dialog.getDirectory();
        String fileName = dialog.getFile();
        String filePath = directoryName + fileName;

        try {
            nar.addInput(new TextInput(new File(filePath)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Initialize the system for a new finish
     */
    public void init() {
        setSpeed(0);
        setSpeed(0);        //call twice to make it start as paused
        updateGUI();
        nar.memory.event.on(FrameEnd.class, this);
    }

    final Runnable updateGUIRunnable = new Runnable() {
        @Override public void run() {
            updateGUI();
        }
    };
    
    /** in ms */
    long lastUpdateTime = -1;
    
    /** in memory cycles */
    long lastUpdateCycle = -1;
    
    AtomicBoolean updateScheduled = new AtomicBoolean(false);
    
    protected void updateGUI() {
        
        speedSlider.repaint();

        long nowTime = nar.getTime();

        if (lastUpdateCycle != nowTime) {       
            chart.update(true);
            
            lastUpdateCycle = nowTime;
            updateScheduled.set(false);
        }


    }
    

    @Override
    public void event(final Class event, final Object... arguments) {
        if (event == FrameEnd.class) {
            
            long now = System.currentTimeMillis();
            long deltaTime = now - lastUpdateTime;
            
            if ((deltaTime >= GUIUpdatePeriodMS) || (!updateScheduled.get())) {
                
                lastUpdateTime = System.currentTimeMillis();
                
                senses.update(memory);
                
                SwingUtilities.invokeLater(updateGUIRunnable);
                updateScheduled.set(true);
            }
        }
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
                updateGUI();
            } else if (obj == walkButton) {
                nar.stop();
                nar.step(1);
                updateGUI();
            }
        } else if (obj instanceof JMenuItem) {
            String label = e.getActionCommand();
            switch (label) {
                case "Enable Full Internal Experience":
                    fullInternalExp.setEnabled(false);
                    Parameters.INTERNAL_EXPERIENCE_FULL=true;
                    //Parameters.ENABLE_EXPERIMENTAL_NARS_PLUS=!Parameters.ENABLE_EXPERIMENTAL_NARS_PLUS;
                    break;
                    
//                case "Enable NARS+ Ideas":
//                    narsPlusItem.setEnabled(false);
//                    nar.memory.param.experimentalNarsPlus.set(true);
//                    break;
//                case "Enable Internal Experience (NAL9)":
//                    internalExperienceItem.setEnabled(false);
//                    nar.memory.param.internalExperience.set(true);
//                    break;
                    
                case "Load Experience":
                    openLoadFile();
                    break;
                case "Save Experience":
                    if (savingExp) {
                        experienceWriter.closeSaveFile();
                    } else {
                        FileDialog dialog = new FileDialog((Dialog) null, "Save experience", FileDialog.SAVE);
                        dialog.setVisible(true);
                        String directoryName = dialog.getDirectory();
                        String fileName = dialog.getFile();
                        String path = directoryName + fileName;
                        experienceWriter.openSaveFile(path);
                    }
                    savingExp = !savingExp;
                    break;
                case "Reset":
                    /// TODO mixture of modifier and reporting
                    narsPlusItem.setEnabled(true);
                    internalExperienceItem.setEnabled(true);
                    nar.reset();
                    break;
                case "Related Information":
//                MessageDialog web =
                    new MessageDialog(NARSwing.WEBSITE); 
                    break;
                case "About NARS":
//                MessageDialog info =
                    new MessageDialog(NARSwing.INFO+"\n\n"+NARSwing.WEBSITE);
                    break;
            }
        }
    }


    
    private NSlider newSpeedSlider() {
        final NSlider s = new NSlider(0f, 0f, 1.0f) {

            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }
                
                String s = "@" + memory.getTime();

                if (currentSpeed == 0) {
                    s += " - pause";
                } else if (currentSpeed == 1.0) {
                    s += " - run max speed";
                } else {
                    s += " - run " + nar.getMinCyclePeriodMS() + " ms / step";
                }
                return s;
            }

            @Override
            public void onChange(float v) {
                setSpeed(v);
            }

        };
        this.speedSlider = s;

        return s;
    }

    private NSlider newVolumeSlider() {
        final NSlider s = this.volumeSlider = new NSlider(100f, 0, 100f) {

            @Override
            public String getText() {
                if (value == null) {
                    return "";
                }

                float v = value();
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
            public void setValue(float v) {
                super.setValue(Math.round(v));
                repaint(); //needed to update when called from outside, as the 'focus' button does
            }

            @Override
            public void onChange(float v) {
                int level = (int) v;
                nar.param().noiseLevel.set(level);
            }

        };

        return s;
    }

    public void setSpeed(float nextSpeed) {
        final float maxPeriodMS = 1024.0f;

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
        stopButton.setText(String.valueOf(FA_PlayCharacter));

        /*if (currentSpeed == s)
         return;*/
        speedSlider.setValue(nextSpeed);
        currentSpeed = nextSpeed;

        float logScale = 50f;
        if (nextSpeed > 0) {
            int ms = (int) ((1.0 - Math.log(1+nextSpeed*logScale)/Math.log(1+logScale)) * maxPeriodMS);
            if (ms < 1) {
                if (allowFullSpeed)
                    ms = 0;
                else
                    ms = 1;
            }
            stopButton.setText(String.valueOf(FA_StopCharacter));
            //nar.setThreadYield(true);
            nar.start(ms, nar.getCyclesPerFrame());
        } else {
            stopButton.setText(String.valueOf(FA_PlayCharacter));
            nar.stop();
        }
    }


    
//
//    @Override
//    public void run() {
//        
//
//        updateGUI();
//        
//        lastTime = nar.getTime();
//
//        while (true) {
//            try {
//                Thread.sleep(GUIUpdatePeriodMS);
//            } catch (InterruptedException ex) {
//            }
//
//
//            updateGUI();
//
//        }
//    }
    
    
    //http://astronautweb.co/snippet/font-awesome/
    private final char FA_PlayCharacter = '\uf04b';
    private final char FA_StopCharacter = '\uf04c';
    private final char FA_FocusCharacter = '\uf11e';

    private JComponent newParameterPanel() {
        JPanel p = new JPanel();

        JPanel pc = new JPanel();

        pc.setLayout(new GridLayout(1, 0));

        stopButton = new FAButton(FA_StopCharacter);
        stopButton.addActionListener(this);
        pc.add(stopButton);

        walkButton = new FAButton('\uf051');
        walkButton.setToolTipText("Walk 1 Cycle");
        walkButton.addActionListener(this);
        pc.add(walkButton);

        JButton focusButton = new FAButton(FA_FocusCharacter);
        focusButton.setToolTipText("Focus");
        focusButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setSpeed(1.0f);
                volumeSlider.setValue(0.0f);
                
            }

        });
        pc.add(focusButton);
        
        
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.ipady = 8;

        p.add(pc, c);
        
        NSlider vs = newVolumeSlider();
        vs.setFont(vs.getFont().deriveFont(Font.BOLD));
        p.add(vs, c);

        NSlider ss = newSpeedSlider();
        ss.setFont(vs.getFont());
        p.add(ss, c);


        c.ipady = 4;

        p.add(newIntSlider(memory.param.taskCyclesToForget, "Task Forgetting Rate", 1, 99), c);
        p.add(newIntSlider(memory.param.beliefCyclesToForget, "Belief Forgetting Rate", 1, 99), c);
        p.add(newIntSlider(memory.param.conceptCyclesToForget, "Concept Forgetting Rate", 1, 99), c);

        //JPanel chartPanel = new JPanel(new GridLayout(0,1));
        {
            this.chart = new ChartsPanel(senses, chartHistoryLength);
            //chartPanel.add(chart);
                        
        }
        
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;        
        //p.add(new JScrollPane(chartPanel), c);
        p.add(chart, c);

        /*c.fill = c.BOTH;
        p.add(Box.createVerticalBox(), c);*/
        

        return p;
    }

    private NSlider newIntSlider(final AtomicInteger x, final String prefix, int min, int max) {
        final NSlider s = new NSlider(x.intValue(), min, max) {

            @Override
            public String getText() {
                return prefix + ": " + super.getText();
            }

            @Override
            public void setValue(float v) {
                int i = (int) Math.round(v);
                super.setValue(i);
                x.set(i);
            }

            @Override
            public void onChange(float v) {
            }
        };

        return s;
    }

    /** if true, then the speed control allows NAR to run() each iteration with 0 delay.  
     *  otherwise, the minimum delay is 1ms */
    public void setAllowFullSpeed(boolean allowFullSpeed) {
        this.allowFullSpeed = allowFullSpeed;
    }

    


}
