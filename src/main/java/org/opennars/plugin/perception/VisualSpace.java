/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.plugin.perception;

import org.opennars.entity.TruthValue;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.language.Conjunction;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.operator.ImaginationSpace;
import org.opennars.operator.NullOperator;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;

/**
 *
 * @author Patrick
 */
public class VisualSpace implements ImaginationSpace {

    public final float[][] source; //assumed to be set from outside
    public final float[][] cropped; //all elements assumed to be in [0,1] range
    public final int height;
    public final int width;
    public int px = 0;
    public int py = 0;
    
    //those are the same for each instance:
    public static final NullOperator move = new NullOperator("^move");
    public static final NullOperator zoom = new NullOperator("^zoom");
    private final Nar nar;
    
    public VisualSpace(final Nar nar, final float[][] source, final int py, final int px, final int height, final int width) {
        this.nar = nar;
        this.height = height;
        this.width = width;    
        this.cropped = new float[height][width];
        this.source = new float[source.length][source[0].length];
        this.py = py;
        this.px = px;
        for(int i=0;i<source.length;i++) { //"snapshot" from source
            System.arraycopy(source[i], 0, this.source[i], 0, source[0].length);
        }
        //now copy into data
        for(int i=0; i<height; i++) {
            int relIndexY = 0; //was py, px but sensory device already does the shifting
            int relIndexX = 0; 
            System.arraycopy(source[relIndexY + i], relIndexX + 0, cropped[i], 0, width);
        }
        nar.addPlugin(move);
        nar.addPlugin(zoom);
    }

    @Override
    public TruthValue AbductionOrComparisonTo(ImaginationSpace obj, boolean comparison) {
        if(!(obj instanceof VisualSpace)) {
            return new TruthValue(1.0f,0.0f, nar.narParameters);
        }
        VisualSpace other = (VisualSpace) obj;
        double kh = ((float) other.height) / ((double) this.height);
        double kw = ((float) other.width)  / ((double) this.width);
        TruthValue bestShiftTruth = new TruthValue(0.5f, 0.01f, nar.narParameters);
        for(int oj=-this.height; oj<this.height; oj++) {
            for(int oi=-this.width; oi<this.width; oi++) {
                TruthValue sim = new TruthValue(0.5f, 0.01f, nar.narParameters);
                for(int i=0; i<this.height; i++) {
                    for(int j=0; j<this.width; j++) {
                        int transi = i+oi;
                        int transj = j+oj;
                        if(transi >= this.width || transj >= this.height || transi < 0 || transj < 0) {
                            continue;
                        }
                        int i2 = (int) (((double) i) * kh);
                        int j2 = (int) (((double) j)  * kw);
                        TruthValue t1 = new TruthValue(cropped[transi][transj], nar.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, nar.narParameters);
                        TruthValue t2 = new TruthValue(other.cropped[i2][j2], nar.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, nar.narParameters);
                        TruthValue t3 = comparison ? TruthFunctions.comparison(t1,t2, nar.narParameters) : TruthFunctions.abduction(t1,t2, nar.narParameters);
                        sim = TruthFunctions.revision(sim, t3, nar.narParameters);                
                    }
                }
                if(sim.getExpectation() > bestShiftTruth.getExpectation()) {
                    bestShiftTruth = sim;
                }
            }
        }
        return bestShiftTruth;
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
        return op.equals(move) || op.equals(zoom);
    }
    
    //Needs to be resolved:
    //TODO construct imagination space when a &/ "program" is created (sequence seen)
    //TODO when concept is sampled remember last one
    //so that similarity can be computed and a <-> result entered
    //TODO find how to generate motivations from what question
    //TODO make the used operators working on source so that the programs 
    //(for example conditional eye movements to identify a face) can be learnt
}
