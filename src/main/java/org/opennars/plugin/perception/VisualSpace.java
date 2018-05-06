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

import org.opennars.entity.TruthValue;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.language.Conjunction;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.operator.ImaginationSpace;
import org.opennars.operator.NullOperator;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Patrick
 */
public class VisualSpace implements ImaginationSpace {

    public final float[][] source; //assumed to be set from outside
    public final float[][] cropped; //all elements assumed to be in [0,1] range
    public final int height;
    public final int width;
    public final int px = 0;
    public final int py = 0;
    
    //those are the same for each instance:
    static final NullOperator right = new NullOperator("^right");
    static final NullOperator left = new NullOperator("^left");
    static final NullOperator up = new NullOperator("^up");
    static final NullOperator down = new NullOperator("^down");
    final Set<Operator> ops = new HashSet<>();
    final Nar nar;
    
    public VisualSpace(final Nar nar, final float[][] source, final int py, final int px, final int height, final int width) {
        this.nar = nar;
        this.height = height;
        this.width = width;    
        this.cropped = new float[height][width];
        this.source = new float[source.length][source[0].length];
        for(int i=0;i<source.length;i++) { //"snapshot" from source
            System.arraycopy(source[i], 0, this.source[i], 0, source[0].length);
        }
        //now copy into data
        for(int i=0; i<height; i++) {
            System.arraycopy(source[py + i], px + 0, cropped[i], 0, width);
        }
        nar.addPlugin(right);
        nar.addPlugin(left);
        nar.addPlugin(up);
        nar.addPlugin(down);
        ops.add(right);
        ops.add(left);
        ops.add(up);
        ops.add(down);
    }

    @Override
    public TruthValue AbductionOrComparisonTo(final ImaginationSpace obj, final boolean comparison) {
        if(!(obj instanceof VisualSpace)) {
            return new TruthValue(0.5f, 0.01f);
        }
        final VisualSpace other = (VisualSpace) obj;
        final double kh = ((float) other.height) / ((double) this.height);
        final double kw = ((float) other.width)  / ((double) this.width);
        TruthValue sim = new TruthValue(0.5f, 0.01f);
        for(int i=0; i<this.height; i++) {
            for(int j=0; j<this.width; j++) {
                final int i2 = (int) (((double) i) * kh);
                final int j2 = (int) (((double) j)  * kw);
                final TruthValue t1 = new TruthValue(cropped[i][j], Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                final TruthValue t2 = new TruthValue(other.cropped[i2][j2], Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                final TruthValue t3 = comparison ? TruthFunctions.comparison(t1,t2) : TruthFunctions.abduction(t1,t2);
                sim = TruthFunctions.revision(sim, t3);                
            }
        }
        return sim;
    }

    @Override
    public ImaginationSpace ConstructSpace(final Conjunction program) {
        if(program.isSpatial || program.getTemporalOrder() != TemporalRules.ORDER_FORWARD) {
            return null; //would be a strange program :)
        }
        final Term beginning = program.term[0];
        if(beginning.imagination == null) {
            return null;
        }
        ImaginationSpace cur = beginning.imagination;
        //"execute program":
        for(int i=1; i<program.term.length; i+=1) {
            if(!(program.term[i] instanceof Operation)) {
                return null;
            }
            final Operation oper = (Operation) program.term[i];
            if(!IsOperationInSpace(oper)) {
                return null;
            }
            cur = cur.ProgressSpace(oper, cur);
            i++;
        }
        return null;
    }

    public ImaginationSpace ProgressSpace(final Operation op, final ImaginationSpace b) {
        if(!(b instanceof VisualSpace)) {
            return null; //incompatible
        }
        final VisualSpace B = (VisualSpace) b;
        //construct a space which focus is on both of the focuses of the previous
        //copying the necessary part of source into data and setting width and height
        //for visual space the operation doesn't matter for constructing the compound imagination
        final int minPX = Math.min(this.px, B.px);
        final int maxPX = Math.min(this.px+this.width, B.px+B.width);
        final int minPY = Math.min(this.py, B.py);
        final int maxPY = Math.min(this.py+this.height, B.py+B.height);
        final VisualSpace progressed = new VisualSpace(nar, this.source, minPY, minPX, maxPY, maxPX);
        return progressed;
    }
    
    public boolean IsOperationInSpace(final Operation oper) {
        final Operator op = (Operator) oper.getPredicate();
        return ops.contains(op);
    }
    
    //Needs to be resolved:
    //TODO construct imagination space when a &/ "program" is created (sequence seen)
    //TODO when concept is sampled remember last one
    //so that similarity can be computed and a <-> result entered
    //TODO find how to generate motivations from what question
    //TODO make the used operators working on source so that the programs 
    //(for example conditional eye movements to identify a face) can be learnt
}
