/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.plugin.perception;

import org.opennars.entity.Concept;
import org.opennars.entity.Task;
import org.opennars.io.Narsese;
import org.opennars.language.Term;
import org.opennars.main.NAR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SensoryChannel implements Serializable {
    private Collection<SensoryChannel> reportResultsTo;
    public NAR nar; //for top-down influence of concept budgets
    public final List<Task> results = new ArrayList<>();
    public int height = 0; //1D channels have height 1
    public int width = 0;
    public int duration = -1;
    public SensoryChannel(){}
    public SensoryChannel(final NAR nar, final Collection<SensoryChannel> reportResultsTo, final int width, final int height, final int duration) {
        this.reportResultsTo = reportResultsTo;
        this.nar = nar;
        this.width = width;
        this.height = height;
        this.duration = duration;
    }
    public SensoryChannel(final NAR nar, final SensoryChannel reportResultsTo, final int width, final int height, final int duration) {
        this(nar, Arrays.asList(reportResultsTo), width, height, duration);
    }
    public void addInput(final String text) {
        try {
            final Task t = new Narsese(nar).parseTask(text);
            this.addInput(t);
        } catch (final Narsese.InvalidInputException ex) {
            Logger.getLogger(SensoryChannel.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Could not parse input", ex);
        }
    }
    public abstract NAR addInput(final Task t);
    public void step_start(){} //needs to put results into results and call step_finished when ready
    public void step_finished() {
        for(final SensoryChannel ch : reportResultsTo) {
            for(final Task t : results) {
                ch.addInput(t);
            }
        }
        results.clear();
    }
    
    public double topDownPriority(final Term t) {
        double prioritysum = 0.0f;
        int k=0;
        for(final SensoryChannel chan : reportResultsTo) {
            prioritysum += chan.priority(t);
            k++;
        }
        return prioritysum / (double) reportResultsTo.size();
    }
    
    public double priority(final Term t) {
        if(this instanceof NAR) { //on highest level it is simply the concept priority
            final Concept c = ((NAR)this).memory.concept(t);
            if(c != null) {
                return c.getPriority();
            }
        }
        return 0.0;
    }
}
