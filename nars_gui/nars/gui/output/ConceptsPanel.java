/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import automenta.vivisect.Video;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.unmodifiableList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.NAR;
import nars.entity.BudgetValue.Budgetable;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.TruthValue.Truthable;
import nars.gui.WrapLayout;

/**
 * Views one or more Concepts
 */
public class ConceptsPanel extends VerticalPanel implements EventObserver, Runnable {

    private final NAR nar;
    private final LinkedHashMap<Concept, ConceptPanel> concept;

    public ConceptsPanel(NAR n, Concept... c) {
        super();

        this.nar = n;

        this.concept = new LinkedHashMap();
        int i = 0;
        for (Concept x : c) {
            ConceptPanel p = new ConceptPanel(x, nar.time());
            addPanel(i++, p);
            concept.put(x, p);
        }

        updateUI();

    }

    @Override
    protected void onShowing(boolean showing) {

        nar.memory.event.set(this, showing,
                Events.ConceptBeliefAdd.class,
                Events.ConceptBeliefRemove.class,
                Events.ConceptQuestionAdd.class,
                Events.ConceptQuestionRemove.class,
                Events.ConceptGoalAdd.class,
                Events.ConceptGoalRemove.class);
    }

    @Override
    public void event(Class event, Object[] args) {

        if (!(args.length > 0) && (args[0] instanceof Concept)) {
            return;
        }
        Concept c = (Concept) args[0];
        ConceptPanel cp = concept.get(c);
        if (cp != null) {
            SwingUtilities.invokeLater(this);
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
        private final JLabel subtitle;

        final int chartWidth = 64;
        final int chartHeight = 64;
        final float titleSize = 24f;
        final float subfontSize = 16f;
        private BeliefTimeline beliefTime;
        
        public ConceptPanel(Concept c, long time) {
            this(c);
            update(time);
        }

        public ConceptPanel(Concept c) {
            super(new BorderLayout());
            this.concept = c;

            JPanel details = new JPanel(new WrapLayout(FlowLayout.LEFT));
            add(details, CENTER);

            details.add(this.beliefChart = new TruthChart(chartWidth, chartHeight));
            details.add(this.questionChart = new PriorityColumn((int)Math.ceil(Math.sqrt(chartWidth)), chartHeight));
            details.add(this.desireChart = new TruthChart(chartWidth, chartHeight));
            //details.add(this.questChart = new PriorityColumn((int)Math.ceil(Math.sqrt(chartWidth)), chartHeight)));
            
            add(this.beliefTime = new BeliefTimeline(chartWidth*3, chartHeight/4), SOUTH);

            JPanel titlePanel = new JPanel(new BorderLayout());
            
            titlePanel.add(this.title = new JTextArea(concept.term.toString()), CENTER);
            this.title.setEditable(false);
            titlePanel.add(this.subtitle = new JLabel(), SOUTH);
            
            details.add(titlePanel);
            
            title.setFont(Video.monofont.deriveFont(titleSize ));

            
        }

        public void update(long time) {

            if (!concept.beliefs.isEmpty()) {
                List<Sentence> bb = concept.getBeliefs();
                beliefChart.update(bb);
                subtitle.setText(bb.get(0).truth.toString());
                
                
                beliefTime.setVisible(
                        beliefTime.update(time, concept.beliefs));
            }
            else {
                if (!concept.questions.isEmpty())
                    subtitle.setText("??");
                beliefTime.setVisible(false);
            }

            if (!concept.questions.isEmpty())
                questionChart.update( unmodifiableList( concept.questions ) );
            
            if (!concept.desires.isEmpty())
                desireChart.update( unmodifiableList( concept.desires ));

            
        
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

        public void update(Iterable<? extends Budgetable> i) {
            Graphics g = g();
            if (g == null) return;
            
            g.setColor(new Color(0.1f, 0.1f, 0.1f));
            g.fillRect(0, 0, getWidth(), getHeight());
            for (Budgetable s : i) {
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

        public BeliefTimeline(int width, int height) {
            super(width, height);
        }

        public boolean update(long time, Collection<Sentence> i) {
            
            long minTime = -1, maxTime = time;
            for (Sentence s : i) {
                if (s.isEternal()) continue;
                long when = s.getOccurenceTime();
                
                if ((minTime == -1) || (minTime > when))
                    minTime = when;                
            }
            
            if (minTime == -1) {
                //no time-bound beliefs
                return false;
            }

            Graphics g = g();
            if (g == null) return false;
            
            int h = 8;                
            float timeFactor = (1f+(float)maxTime - minTime) / ((float)w-h);
            
            g.setColor(new Color(0.1f, 0.1f, 0.1f));
            g.fillRect(0, 0, getWidth(), getHeight());
            for (Sentence s : i) {
                if (s.isEternal()) continue;
                long when = s.getOccurenceTime();
                
                int x = (int)((when - minTime) / timeFactor);

                
                float freq = s.getTruth().getFrequency();
                float conf = s.getTruth().getConfidence();

                float ii = 0.25f + conf * 0.75f;
                float green = freq > 0.5f ? (freq/2f) : 0f;
                
                float red = freq <= 0.5f ? ((1.0f-freq)/2f) : 0;
                g.setColor(new Color(red, green, 1.0f, ii));
                
                g.fillRect(x, 0, h, getHeight());

            }
            g.dispose();        
            return true;
        }
    }
    
    public static class TruthChart extends ImagePanel {

        public TruthChart(int width, int height) {
            super(width, height);
            update(Collections.EMPTY_LIST);
        }

        public void update(Iterable<? extends Truthable> i) {
            Graphics g = g();
            if (g == null) return;
            
            g.setColor(new Color(0.1f, 0.1f, 0.1f));
            g.fillRect(0, 0, (int) getWidth(), (int) getHeight());
            for (Truthable s : i) {
                float freq = s.getTruth().getFrequency();
                float conf = s.getTruth().getConfidence();

                g.setColor(new Color(freq * 1f, conf * 1f, 1f, 0.8f));

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
