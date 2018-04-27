package nars.plugin.perception;

import nars.io.Narsese;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.main.NAR;
import nars.entity.Concept;
import nars.entity.Task;
import nars.language.Term;

public abstract class SensoryChannel implements Serializable {
    private Collection<SensoryChannel> reportResultsTo;
    public NAR nar; //for top-down influence of concept budgets
    public List<Task> results = new ArrayList<Task>();
    public int height = 0; //1D channels have height 1
    public int width = 0;
    public int duration = -1;
    public SensoryChannel(){}
    public SensoryChannel(NAR nar, Collection<SensoryChannel> reportResultsTo, int width, int height, int duration) {
        this.reportResultsTo = reportResultsTo;
        this.nar = nar;
        this.width = width;
        this.height = height;
        this.duration = duration;
    }
    public SensoryChannel(NAR nar, SensoryChannel reportResultsTo, int width, int height, int duration) {
        this(nar, Arrays.asList(reportResultsTo), width, height, duration);
    }
    public void addInput(final String text) {
        try {
            Task t = new Narsese(nar).parseTask(text);
            this.addInput(t);
        } catch (Narsese.InvalidInputException ex) {
            Logger.getLogger(SensoryChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public abstract NAR addInput(final Task t);
    public void step_start(){} //needs to put results into results and call step_finished when ready
    public void step_finished() {
        for(SensoryChannel ch : reportResultsTo) {
            for(Task t : results) {
                ch.addInput(t);
            }
        }
        results.clear();
    }
    
    public double topDownPriority(Term t) {
        double prioritysum = 0.0f;
        int k=0;
        for(SensoryChannel chan : reportResultsTo) {
            prioritysum += chan.priority(t);
            k++;
        }
        return prioritysum / (double) reportResultsTo.size();
    }
    
    public double priority(Term t) {
        if(this instanceof NAR) { //on highest level it is simply the concept priority
            Concept c = ((NAR)this).memory.concept(t);
            if(c != null) {
                return c.getPriority();
            }
        }
        return 0.0;
    }
}
