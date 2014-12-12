/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;

/**
 * Views one or more Concepts
 */
public class ConceptsPanel extends VerticalPanel implements EventObserver {
    
    private final NAR nar;
    private final LinkedHashMap<Concept, ConceptPanel> concept;

    public ConceptsPanel(NAR n, Concept... c) {
        super();
        
        this.nar = n;
        
        this.concept = new LinkedHashMap();
        int i = 0;
        for (Concept x : c) {
            ConceptPanel p = new ConceptPanel(x);
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
        
        if (!(args.length > 0) && (args[0] instanceof Concept))
            return;
        
        Concept c = (Concept)args[0];
        ConceptPanel cp = concept.get(c);
        if (cp!=null)
            cp.update();
    }
    
    public static class ConceptPanel extends JPanel {
        private final Concept concept;
        private final ImagePanel beliefChart;

        public ConceptPanel(Concept c) {
            super(new BorderLayout());
            this.concept = c;

            JLabel title = new JLabel(concept.term.toString());
            add(title, NORTH);
            
            this.beliefChart = new ImagePanel(64,64);
            add(beliefChart, CENTER);
            beliefChart.setVisible(false);
            
            
            update();
        }
        
        public void update() {                        
            
            if (!concept.beliefs.isEmpty()) {
                
                Graphics g = beliefChart.g();
                
                if (g!=null) {
                    float beliefChartWidth = beliefChart.getWidth();
                    float beliefChartHeight = beliefChart.getHeight();
                    g.setColor(new Color(0.1f, 0.1f, 0.1f));
                    g.fillRect(0, 0, (int)beliefChartWidth, (int)beliefChartHeight);
                    for (Sentence s : concept.beliefs) {
                        float freq = s.truth.getFrequency();
                        float conf = s.truth.getConfidence();
                        
                        g.setColor(new Color(freq*1f, conf*1f, 1f, 0.8f));

                        
                        int w = 8;
                        int h = 8;
                        float dw = beliefChartWidth - w*2;
                        float dh = beliefChartHeight - h*2;
                        g.fillOval((int)(freq*dw) + w, (int)((1.0 - conf)*dh) + h, w, h);

                    }
                    g.dispose();
                }
            }
            
            

            
            updateUI();
        }
    }
    
public static class ImagePanel extends JPanel{

    public BufferedImage image;
    final int w, h;
    
    public ImagePanel(int width, int height) {
        super();

        this.w = width;
        this.h = height;
        setSize(width, height);        
        setMinimumSize(new Dimension(width,height));
        setPreferredSize(new Dimension(width, height));
    }
    
    public Graphics g() {
        if (image == null) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        if (image!=null)
            return image.createGraphics();
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

}    
}
