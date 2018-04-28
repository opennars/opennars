package nars.plugin.perception;

import nars.operator.ImaginationSpace;
import java.util.HashSet;
import nars.main.NAR;
import nars.main.Parameters;
import nars.entity.TruthValue;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.language.Conjunction;
import nars.language.Term;
import nars.operator.NullOperator;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 *
 * @author Patrick
 */
public class VisualSpace implements ImaginationSpace {

    public float[][] source; //assumed to be set from outside
    public float[][] cropped; //all elements assumed to be in [0,1] range
    public int height;
    public int width;
    public int px = 0;
    public int py = 0;
    
    //those are the same for each instance:
    static NullOperator right = new NullOperator("^right");
    static NullOperator left = new NullOperator("^left");
    static NullOperator up = new NullOperator("^up");
    static NullOperator down = new NullOperator("^down");
    HashSet<Operator> ops = new HashSet<Operator>();
    NAR nar;
    
    public VisualSpace(NAR nar, float[][] source, int py, int px, int height, int width) {
        this.nar = nar;
        this.height = height;
        this.width = width;    
        this.cropped = new float[height][width];
        this.source = new float[source.length][source[0].length];
        for(int i=0;i<source.length;i++) { //"snapshot" from source
            for(int j=0; j<source[0].length; j++) {
                this.source[i][j] = source[i][j];
            }
        }
        //now copy into data
        for(int i=0; i<height; i++) {
            for(int j=0; j<width; j++) {
                cropped[i][j] = source[py+i][px+j];
            }
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
    public TruthValue AbductionOrComparisonTo(ImaginationSpace obj, boolean comparison) {
        if(!(obj instanceof VisualSpace)) {
            return new TruthValue(1.0f,0.0f);
        }
        VisualSpace other = (VisualSpace) obj;
        double kh = ((float) other.height) / ((double) this.height);
        double kw = ((float) other.width)  / ((double) this.width);
        TruthValue sim = new TruthValue(0.5f, 0.01f);
        for(int i=0; i<this.height; i++) {
            for(int j=0; j<this.width; j++) {
                int i2 = (int) (((double) i) * kh);
                int j2 = (int) (((double) j)  * kw);
                TruthValue t1 = new TruthValue(cropped[i][j], Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                TruthValue t2 = new TruthValue(other.cropped[i2][j2], Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                TruthValue t3 = comparison ? TruthFunctions.comparison(t1,t2) : TruthFunctions.abduction(t1,t2);
                sim = TruthFunctions.revision(sim, t3);                
            }
        }
        return sim;
    }

    @Override
    public ImaginationSpace ConstructSpace(Conjunction program) {
        if(program.isSpatial || program.getTemporalOrder() != TemporalRules.ORDER_FORWARD) {
            return null; //would be a strange program :)
        }
        Term beginning = program.term[0];
        if(beginning.imagination == null) {
            return null;
        }
        ImaginationSpace cur = beginning.imagination;
        //"execute program":
        for(int i=1; i<program.term.length; i+=1) {
            if(!(program.term[i] instanceof Operation)) {
                return null;
            }
            Operation oper = (Operation) program.term[i];
            if(!IsOperationInSpace(oper)) {
                return null;
            }
            cur = cur.ProgressSpace(oper, cur);
            i++;
        }
        return null;
    }

    public ImaginationSpace ProgressSpace(Operation op, ImaginationSpace b) {
        if(!(b instanceof VisualSpace)) {
            return null; //incompatible
        }
        VisualSpace B = (VisualSpace) b;
        //construct a space which focus is on both of the focuses of the previous
        //copying the necessary part of source into data and setting width and height
        //for visual space the operation doesn't matter for constructing the compound imagination
        int minPX = Math.min(this.px, B.px);
        int maxPX = Math.min(this.px+this.width, B.px+B.width);
        int minPY = Math.min(this.py, B.py);
        int maxPY = Math.min(this.py+this.height, B.py+B.height);
        VisualSpace progressed = new VisualSpace(nar, this.source, minPY, minPX, maxPY, maxPX);
        return progressed;
    }
    
    public boolean IsOperationInSpace(Operation oper) {
        Operator op = (Operator) oper.getPredicate();
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
