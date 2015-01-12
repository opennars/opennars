package nars.gui.output;

import nars.entity.Sentence;
import nars.entity.TruthValue;
import nars.language.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * 2D scatter plot of belief frequency/certainty
 */
public class BeliefView extends PPanel {

    public static class TermTruthRange {
        public final Term term;
        float minConf, maxConf;
        float minFreq, maxFreq;
        int sentences;

        public TermTruthRange(Term term) {
            this.term = term;
            sentences = 0;
            minFreq = minConf = 1.0f;
            maxFreq = maxConf = 0.0f;
        }
        
        public void include(Sentence s) {
            assert(s.term.equals(term));
            TruthValue t = s.truth;
            if (t!=null) {
                float co = t.getConfidence();
                float fr = t.getFrequency();
                if (co < minConf) minConf = co;
                if (co > maxConf) maxConf = co;
                if (fr < minFreq) minFreq = fr;
                if (fr > maxFreq) maxFreq = fr;
                sentences++;
            }
        }        
    }
    
    public Map<Term, TermTruthRange> termRanges = new HashMap();
    
    public BeliefView() {
        super();
    }


    @Override
    public void setup() {
        super.setup();
        
    }

    
// Draws the chart and a title.
    @Override
    public void draw() {
//        background(0);
//        textSize(9);
//        
//        fill(240f);
//        lineChart.draw(15, 15, width - 30, height - 30);
//
//        // Draw a title over the top of the chart.
//        
//        textSize(16);        
//        text(title, 40, 30);
//        /*textSize(11);
//        text("Gross domestic product measured in inflation-corrected $US",
//                70, 45);*/
        
    }


    /*
    public static void main(String[] args) {
        PLineChart p = new PLineChart("Average",10);
        p.addPoint(2,2);
        p.addPoint(4,4);
        
        Window w = new Window("", p.newPanel());
        
        
        
        w.setSize(400,400);
        w.setVisible(true);
    }
    */

}

