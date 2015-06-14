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
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import nars.core.NAR;
import nars.core.Parameters;
import nars.grid2d.TestChamber;
import nars.gui.input.InputPanel;
import nars.gui.output.LogPanel;
import nars.gui.output.MemoryView;
import nars.gui.output.PLineChart;
import nars.gui.output.SentenceTablePanel;
import nars.gui.output.SwingLogPanel;
import nars.gui.output.TermWindow;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.storage.Memory;
import nars.util.NARState;


public class NARControls extends JPanel implements ActionListener, Runnable {

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
    private double currentSpeed = 0;
    private double lastSpeed = 0;
    private final double defaultSpeed = 0.5;

    private final int GUIUpdatePeriodMS = 256;
    private NSlider volumeSlider;

    private List<PLineChart> charts = new ArrayList();
        
    private boolean allowFullSpeed = false;
    public final InferenceLogger logger;

    int chartHistoryLength = 400;
    
    /**
     * Constructor
     *
     * @param nar
     * @param title
     */
    TestChamber chamber=new TestChamber();
    private final JMenuItem internalExperienceItem;
    private final JMenuItem narsPlusItem;
    public NARControls(final NAR nar) {
        super(new BorderLayout());
        
        this.nar = nar;
        memory = nar.memory;        
        
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
            
            JMenuItem mv3 = new JMenuItem("+ Text Input");
            mv3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputPanel inputPanel = new InputPanel(nar);
                    Window inputWindow = new Window("Text Input", inputPanel);                    
                    inputWindow.setSize(800, 200);
                    inputWindow.setVisible(true);        
                }
            });
            m.add(mv3);
            
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

            
            JMenuItem mv = new JMenuItem("+ Memory View");
            mv.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new MemoryView(nar);
                }
            });
            m.add(mv);
            
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
            
            JMenuItem cct = new JMenuItem("+ Concept Content");
            cct.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    conceptWin.setVisible(true);                
                }                
            });
            m.add(cct);
            
            
            
            
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
        
        m = new JMenu("Demos");
        {
            JMenuItem cct2 = new JMenuItem("+ Test Chamber");
            cct2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    chamber.create(nar);
                }                
            });
            m.add(cct2);
            
        }
        menuBar.add(m);
        
        
       

        m = new JMenu("Help");
        //addJMenuItem(m, "Related Information");
        addJMenuItem(m, "About NARS");
        m.addActionListener(this);
        menuBar.add(m);

        add(menuBar, BorderLayout.NORTH);


        JComponent jp = newParameterPanel();
        add(jp, BorderLayout.CENTER);

        init();

        new Thread(this).start();
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
    }

    /**
     * Update timer and its display
     */
    final Runnable _updateGUI = new Runnable() {
        @Override
        public void run() {
            speedSlider.repaint();
            
            long nowTime = nar.getTime();

            if (lastTime != nowTime) {                
                for (PLineChart c : charts) {
                    c.update(new NARState(nar));
                }
            }

            lastTime = nowTime;
            
        }
    };

    protected void updateGUI() {
        SwingUtilities.invokeLater(_updateGUI);
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
                case "Enable NARS+ Ideas":
                    narsPlusItem.setEnabled(false);
                    Parameters.ENABLE_EXPERIMENTAL_NARS_PLUS=!Parameters.ENABLE_EXPERIMENTAL_NARS_PLUS;
                    break;
                case "Enable Internal Experience (NAL9)":
                    internalExperienceItem.setEnabled(false);
                    Parameters.ENABLE_INTERNAL_EXPERIENCE=!Parameters.ENABLE_INTERNAL_EXPERIENCE;
                    break;
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
                    s += " - run " + nar.getMinCyclePeriodMS() + " ms / step";
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
                repaint(); //needed to update when called from outside, as the 'focus' button does
            }

            @Override
            public void onChange(double v) {
                int level = (int) v;
                nar.param().noiseLevel.set(level);
            }

        };

        return s;
    }

    public void setSpeed(double nextSpeed) {
        final double maxPeriodMS = 1024.0;

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

        double logScale = 50;
        if (nextSpeed > 0) {
            int ms = (int) ((1.0 - Math.log(1+nextSpeed*logScale)/Math.log(1+logScale)) * maxPeriodMS);
            if (ms < 1) {
                if (allowFullSpeed)
                    ms = 0;
                else
                    ms = 1;
            }
            stopButton.setText(String.valueOf(FA_StopCharacter));
            nar.setThreadYield(true);
            nar.start(ms);
        } else {
            stopButton.setText(String.valueOf(FA_PlayCharacter));
            nar.stop();
        }
    }

    long lastTime = -1;

    @Override
    public void run() {
        

        updateGUI();
        
        lastTime = nar.getTime();

        while (true) {
            try {
                Thread.sleep(GUIUpdatePeriodMS);
            } catch (InterruptedException ex) {
            }


            updateGUI();

        }
    }
    
    /**
     * button that uses FontAwesome icon as a label
     */
    public static class FAButton extends JButton { 
        static Font ttfReal = null;
        static {
            InputStream in = FAButton.class.getResourceAsStream("FontAwesome.ttf");
            Font ttfBase;
            try {
                ttfBase = Font.createFont(Font.TRUETYPE_FONT, in);
                ttfReal = ttfBase.deriveFont(Font.PLAIN, 14);
            } catch (FontFormatException ex) {
                Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(NARControls.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        public FAButton(char faCode) {
            super();
            setFont(ttfReal);

            setText(String.valueOf(faCode));
        }
    }    
    public static class FAToggleButton extends JToggleButton {
        private final char codeUnselected;
        private final char codeSelected;
        public FAToggleButton(char faCodeUnselected, char faCodeSelected) {
            super();
            this.codeUnselected = faCodeUnselected;
            this.codeSelected = faCodeSelected;
            setFont(FAButton.ttfReal);
            setText(String.valueOf(faCodeUnselected));            
        }

        @Override
        public void setSelected(boolean b) {
            super.setSelected(b);
        }

        
        @Override
        public void paint(Graphics g) {
            if (isSelected()) {
                setText(String.valueOf(codeSelected));
            }
            else {
                setText(String.valueOf(codeUnselected));
            }
            super.paint(g); //To change body of generated methods, choose Tools | Templates.
        }
        
        
        
    }
    
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
                setSpeed(1.0);
                volumeSlider.setValue(20);
                
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

        p.add(newIntSlider(memory.param.taskForgettingRate, "Task Forgetting Rate", 1, 99), c);
        p.add(newIntSlider(memory.param.beliefForgettingRate, "Belief Forgetting Rate", 1, 99), c);
        p.add(newIntSlider(memory.param.conceptForgettingRate, "Concept Forgetting Rate", 1, 99), c);

        JPanel chartPanel = new JPanel(new GridLayout(0,1));
        {
            
            
            PLineChart chart0 = new PLineChart("concepts.Total", chartHistoryLength);
            chartPanel.add(chart0.newPanel());
            charts.add(chart0);
            /*
            ChartPanel chart0 = new ChartPanel("concepts.Total");
            chart0.setPreferredSize(new Dimension(200, 150));
            charts.add(chart0);
            chartPanel.add(chart0);
            */

            PLineChart chart1 = new PLineChart("concepts.Mass", chartHistoryLength);
            chartPanel.add(chart1.newPanel());
            charts.add(chart1);
            PLineChart chart2 = new PLineChart("concepts.AveragePriority", chartHistoryLength);
            chartPanel.add(chart2.newPanel());
            charts.add(chart2);
            
            PLineChart chart3 = new PLineChart("beliefs.Total", chartHistoryLength);
            chartPanel.add(chart3.newPanel());
            charts.add(chart3);
            
            PLineChart chart4 = new PLineChart("questions.Total", chartHistoryLength);
            chartPanel.add(chart4.newPanel());
            charts.add(chart4);

            PLineChart chart5 = new PLineChart("novelTasks.Total", chartHistoryLength);
            chartPanel.add(chart5.newPanel());
            charts.add(chart5);

            PLineChart chart6 = new PLineChart("newTasks.Total", chartHistoryLength);
            chartPanel.add(chart6.newPanel());
            charts.add(chart6);
            
            PLineChart chart7 = new PLineChart("emotion.happy", chartHistoryLength);
            chartPanel.add(chart7.newPanel());
            charts.add(chart7);

            PLineChart chart8 = new PLineChart("emotion.busy", chartHistoryLength);
            chartPanel.add(chart8.newPanel());
            charts.add(chart8);
            
            
        }
        
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;        
        p.add(new JScrollPane(chartPanel), c);

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

    /** if true, then the speed control allows NAR to run() each iteration with 0 delay.  
     *  otherwise, the minimum delay is 1ms */
    public void setAllowFullSpeed(boolean allowFullSpeed) {
        this.allowFullSpeed = allowFullSpeed;
    }

    


}
