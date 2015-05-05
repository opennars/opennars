package nars.op.software;

import nars.nal.term.Compound;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.op.mental.Mental;

import java.util.ArrayList;

// Usage:
// (^numericCertainty, min, max, value, term)
public class NumericCertainty extends Operator implements Mental{
    public NumericCertainty() {
        super("^numericCertainty");
    }
    
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args) {
        if (args.length != 4) {
            // TODO< error report >
            return null;
        }
        
        float min = tryToConvertStringToFloat(args[0].toString());
        float max = tryToConvertStringToFloat(args[1].toString());
        float value = tryToConvertStringToFloat(args[2].toString());
        
        float clampedValue = clamp(min, max, value);
        float resultCertainty = (clampedValue-min)/(max-min);

        // NOTE< no memory output ? >

        ArrayList<Task> results = new ArrayList<>(1);

        Compound resultTerm = Sentence.termOrNull(args[3]);
        if (resultTerm != null) {
            throw new RuntimeException("API Upgrade not finished here:");
            /*results.add(memory.newTask(resultTerm, Symbols.JUDGMENT, 1.0f, resultCertainty, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, Tense.Present));*/
        }
        
        return results;
    }
    
    static private float tryToConvertStringToFloat(String input) {
        return Float.parseFloat(input);
    }
    
    static private float clamp(float min, float max, float value) {
        if (value > max) {
            return max;
        }
        else if (value < min) {
            return min;
        }
        else {
            return value;
        }
    }
}
