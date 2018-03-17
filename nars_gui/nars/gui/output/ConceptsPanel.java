/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NPanel;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.unmodifiableList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.util.Events.CyclesEnd;
import nars.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.gui.WrapLayout;
import nars.inference.TruthFunctions;

/**
 * Views one or more Concepts
 */
public class ConceptsPanel extends NPanel implements EventObserver, Runnable {

    private final NAR nar;
    private final LinkedHashMap<Concept, ConceptPanel> concept;

    public ConceptsPanel(NAR n, Concept... c) {
        super();

        this.nar = n;
            this.concept = new LinkedHashMap();

        if (c.length == 1) {
            ConceptPanel v = new ConceptPanel(c[0], nar.time()); 
            setLayout(new BorderLayout());
            add(v, CENTER);
            concept.put(c[0], v);
        }
        else {
            VerticalPanel v = new VerticalPanel() {

                @Override
                protected void onShowing(boolean showing) {
                    ConceptsPanel.this.onShowing(showing);
                }
                
            };
            add(v, CENTER);
            
            int i = 0;
            for (Concept x : c) {
                if (x==null) continue;

                ConceptPanel p = new ConceptPanel(x, nar.time());
                v.addPanel(i++, p);
                concept.put(x, p);
            }
        }

        updateUI();

    }

    @Override
    protected void onShowing(boolean showing) {

        nar.memory.event.set(this, showing,
                Events.CyclesEnd.class,
                Events.ConceptBeliefAdd.class,
                Events.ConceptBeliefRemove.class,
                Events.ConceptQuestionAdd.class,
                Events.ConceptQuestionRemove.class,
                Events.ConceptGoalAdd.class,
                Events.ConceptGoalRemove.class);
    }

    @Override
    public void event(Class event, Object[] args) {

        if (event == CyclesEnd.class) {
            //SwingUtilities.invokeLater(this);
            run();
        }
    }
    
    @Override public void run() {  
        //TODO only update the necessary concepts
        for (ConceptPanel cp : concept.values())
            cp.update(nar.time()); 
    }

    public static class ConceptPanel extends JPanel {

        private final Concept concept;
        private final TruthChart beliefChart;
        private final TruthChart desireChart;
        private final PriorityColumn questionChart;
        private final JTextArea title;
        private final JTextArea subtitle;

        final int chartWidth = 64;
        final int chartHeight = 64;
        final float titleSize = 16f;
        final float subfontSize = 16f;
        private BeliefTimeline beliefTime;
        private BeliefTimeline desireTime;
       // private final PCanvas syntaxPanel;
        long time = 0;
        
        public ConceptPanel(Concept c, long time) {
            this(c);
            this.time = time;
            update(time);
        }

        String filter = "";
        public ConceptPanel(Concept c) {
            super(new BorderLayout());
            this.concept = c;

            this.setPreferredSize(new Dimension(600,600));
            JPanel overlay = new JPanel(new BorderLayout());
            
            JPanel details = new JPanel(new WrapLayout(FlowLayout.LEFT));
            details.setOpaque(false);

            details.add(new JLabel("Beliefs"));
            details.add(this.beliefChart = new TruthChart(chartWidth, chartHeight));
            details.add(new JLabel("Questions"));
            details.add(this.questionChart = new PriorityColumn((int)Math.ceil(Math.sqrt(chartWidth)), chartHeight));
            details.add(new JLabel("Desires"));
            details.add(this.desireChart = new TruthChart(chartWidth, chartHeight));
            //details.add(this.questChart = new PriorityColumn((int)Math.ceil(Math.sqrt(chartWidth)), chartHeight)));
            

            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            
            titlePanel.add(this.title = new JTextArea(concept.term.toString()), CENTER);
            
            JTextField jfilter = new JTextField("");
            jfilter.setPreferredSize(new Dimension(255,20));
            details.add(jfilter,SOUTH);
            jfilter.setBackground(Color.DARK_GRAY);
            jfilter.setForeground(Color.WHITE);
            
            ConceptPanel THIS = this;
            jfilter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                THIS.update(time);
                THIS.filter = jfilter.getText();
            }

            });
            
            title.setEditable(false);
            title.setOpaque(false);
            subtitle = new JTextArea("");
            subtitle.setEditable(false);
            titlePanel.add(subtitle, SOUTH);
            
            details.add(titlePanel);
            
            title.setFont(Video.monofont.deriveFont(titleSize ));

            overlay.add(details, CENTER);
            JPanel timepanels = new JPanel();
            timepanels.setLayout(new BoxLayout(timepanels,BoxLayout.PAGE_AXIS));
            timepanels.setOpaque(false);
            timepanels.setPreferredSize(new Dimension(250,100));
            timepanels.add(new JLabel("belief-events:"));
            timepanels.add(this.beliefTime = new BeliefTimeline(chartWidth*6, chartHeight/3));
            timepanels.add(new JLabel("desire-events:"));
            timepanels.add(this.desireTime = new BeliefTimeline(chartWidth*6, chartHeight/3));
            
            overlay.add(timepanels, SOUTH);
            
            
            
           /* TermSyntaxVis tt = new TermSyntaxVis(c.term);
            syntaxPanel = new PCanvas(tt);
            syntaxPanel.setZoom(10f);
            
            syntaxPanel.noLoop();
            syntaxPanel.redraw();

            
                        

            add(syntaxPanel);*/
            add(overlay, NORTH);  
            //setComponentZOrder(overlay, 1);
            //syntaxPanel.setBounds(0,0,400,400);
            
            
        }

        public void update(long time) {

             StringBuilder conceptstr = new StringBuilder(); //concept.toStringLong().replaceAll("\n", "<br/>");
                
            /*if(concept.beliefs.size()>0) {
                conceptstr.append("\nBeliefs:\n");
                for(Task tl : concept.beliefs) {
                    conceptstr.append(tl.sentence.toString());
                    conceptstr.append("\n");
                }
            }

            if(concept.desires.size()>0) {
                conceptstr.append("\nDesires:\n");
                for(Task tl : concept.desires) {
                    conceptstr.append(tl.sentence.toString());
                    conceptstr.append("\n");
                }
            }

            if(concept.questions.size()>0) {
                conceptstr.append("\nQuestions:\n");
                for(Task tl : concept.questions) {
                    conceptstr.append(tl.sentence.toString());
                    conceptstr.append("\n");
                }
            }

            if(concept.quests.size()>0) {
                conceptstr.append("\nQuests:\n");
                for(Task tl : concept.quests) {
                    conceptstr.append(tl.sentence.toString());
                    conceptstr.append("\n");
                }
            }*/

            if(concept.taskLinks.size()>0) {
                conceptstr.append("TaskLinks:\n");
                for(TaskLink tl : concept.taskLinks) {
                    String s = tl.targetTask.sentence.toString()+ " priority:" + tl.getBudget().getPriority();
                    if(s.contains(filter)) {
                        conceptstr.append(s);
                        conceptstr.append("\n");
                    }
                }
            }

            /*if(concept.termLinks.size()>0) {
                conceptstr.append("\nTermLinks:\n");
                for(TermLink tl : concept.termLinks) {
                    conceptstr.append(tl.getTerm().toString());
                    conceptstr.append("\n");
                }
            }*/
            
            this.time = time;
            String sub = "";

            if (!concept.beliefs.isEmpty()) {
                List<Task> bbT = concept.getBeliefs();
                List<Sentence> bb=new ArrayList<Sentence>();
                for(Task ts : bbT) {
                    bb.add(ts.sentence);
                }
                
                beliefChart.update(time, bb);
                sub+="truth: " + bb.get(0).truth.toString();
                
                beliefTime.setVisible(
                        beliefTime.update(time, bb));
            }
            else {
                subtitle.setText("");
                if (!concept.questions.isEmpty() || !concept.quests.isEmpty())
                    subtitle.setText("?(question)");
                beliefTime.setVisible(false);
            }

            if (!concept.questions.isEmpty() || !concept.quests.isEmpty()) {
                ArrayList<Task> qu = new ArrayList<Task>();
                qu.addAll(concept.questions);
                qu.addAll(concept.quests);
                questionChart.update( unmodifiableList( qu ) );
            }
            
            if (!concept.desires.isEmpty()) {
                List<Task> ddT = concept.getDesires();
                List<Sentence> dd=new ArrayList<Sentence>();
                for(Task ts : ddT) {
                    dd.add(ts.sentence);
                }
                
                String s=sub;
                sub = (s.equals("") ? "" : s+" ")+"  desire: "+concept.desires.get(0).sentence.truth.toString();
                ArrayList<Sentence> desir=new ArrayList();
                for(Task ts: concept.desires) {
                    desir.add(ts.sentence);
                }
                desireChart.update( time, unmodifiableList( desir ));
                
                desireTime.setVisible(
                        desireTime.update(time, dd));
            } else {
                desireTime.setVisible(false);
            }
            String finalstr = sub+"\n\n"+conceptstr.toString();
            subtitle.setText(finalstr);

            updateUI();
        }
    }

    public static class ImagePanel extends JPanel {

        public BufferedImage image;
        final int w, h;

        public ImagePanel(int width, int height) {
            super();

            this.w = width;
            this.h = height;
            setSize(width, height);
            setMinimumSize(new Dimension(width, height));
            setPreferredSize(new Dimension(width, height));
        }

        public Graphics g() {
            if (image == null) {
                image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            if (image != null) {
                return image.createGraphics();
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }

    }
    public static class PriorityColumn extends ImagePanel {

        public PriorityColumn(int width, int height) {
            super(width, height);
            update(Collections.EMPTY_LIST);
        }

        public void update(Iterable<Task> i) {
            Graphics g = g();
            if (g == null) return;
            
            g.setColor(new Color(0.1f, 0.1f, 0.1f));
            g.fillRect(0, 0, getWidth(), getHeight());
            for (Task s : i) {
                float pri = s.getBudget().getPriority();
                float dur = s.getBudget().getDurability();

                float ii = 0.1f + pri * 0.9f;
                g.setColor(new Color(ii, ii, ii, 0.5f + 0.5f * dur));

                int h = 8;
                int y = (int)((1f - pri) * (getHeight() - h));
                
                g.fillRect(0, y-h/2, getWidth(), h);

            }
            g.dispose();            
        }
    }
    
    /** normalized to entire history of non-eternal beliefs;
     * displayed horizontally
     */
    public static class BeliefTimeline extends ImagePanel {

        float minTime, maxTime;
        private float timeFactor;
        
        public BeliefTimeline(int width, int height) {
            super(width, height);
        }

        public int getX(long when) {
            return (int)Math.round((when - minTime) / timeFactor);    
        }
        
        public boolean update(long time, Collection<Sentence> i) {
            
            minTime = maxTime = time;
            for (Sentence s : i) {
                if (s.isEternal()) continue;
                long when = s.getOccurenceTime();
                                
                if (minTime > when)
                    minTime = when;                
                if (maxTime < when)
                    maxTime = when;
            }
            
            if (minTime == maxTime) {
                //no time-distinct beliefs
                return false;
            }

            Graphics g = g();
            if (g == null) return false;
            
            int thick = 4;                
            timeFactor = ((float)maxTime - minTime) / ((float)w-thick);
            
            g.setColor(new Color(0.1f, 0.1f, 0.1f));
            g.fillRect(0, 0, getWidth(), getHeight());
            for (Sentence s : i) {
                if (s.isEternal()) continue;
                long when = s.getOccurenceTime();
                
                int x = getX(when);
                
                
                float freq = s.getTruth().getFrequency();
                float conf = s.getTruth().getConfidence();

                int y = (int)((1.0f - freq) * (this.h - thick));
                        
                
                g.setColor(getColor(freq, conf, 1.0f));
                
                g.fillRect(x, y, thick, thick);

            }
            
            // "now" axis            
            g.setColor(Color.WHITE);
            g.fillRect(getX(time), 0, 1, getHeight());
            
            g.dispose();        
            return true;
        }
    }
    
    public static Color getColor(float freq, float conf, float factor) {
        float ii = 0.25f + (factor * conf) * 0.75f;
       // float green = freq > 0.5f ? (freq/2f) : 0f;
        //float red = freq <= 0.5f ? ((1.0f-freq)/2f) : 0;
        
        float evidence = TruthFunctions.c2w(conf);
        float positive_evidence_in_0_1 = TruthFunctions.w2c(evidence*freq);
        float negative_evidence_in_0_1 = TruthFunctions.w2c(evidence*(1.0f-freq));        
        return new Color(positive_evidence_in_0_1,0.0f, negative_evidence_in_0_1, ii);
    }
    
    public static class TruthChart extends ImagePanel {

        public TruthChart(int width, int height) {
            super(width, height);
        }

        public void update(long now, Iterable<Sentence> i) {
            Graphics g = g();
            if (g == null) return;
            
            g.setColor(new Color(0.1f, 0.1f, 0.1f));
            g.fillRect(0, 0, (int) getWidth(), (int) getHeight());
            for (Sentence s : i) {
                float freq = s.getTruth().getFrequency();
                float conf = s.getTruth().getConfidence();

                float factor = 1.0f;
                if (s instanceof Sentence) {
                    Sentence ss = (Sentence)s;
                    if (!ss.isEternal()) {
                        //float factor = TruthFunctions.temporalProjection(now, ss.getOccurenceTime(), now);
                        factor = 1.0f / (1f + Math.abs(ss.getOccurenceTime() - now)  );                        
                    }
                }
                g.setColor(getColor(freq, conf, factor));

                int w = 8;
                int h = 8;
                float dw = getWidth() - w;
                float dh = getHeight() - h;
                g.fillRect((int) (freq * dw), (int) ((1.0 - conf) * dh), w, h);

            }
            g.dispose();

        }
    }
    
    
}
