package nars.operator;

import java.util.ArrayList;
import nars.operator.math.Add;
import nars.operator.mental.Abbreviate;
import nars.operator.mental.Believe;
import nars.operator.mental.Consider;
import nars.operator.mental.Doubt;
import nars.operator.mental.Evaluate;
import nars.operator.mental.FeelBusy;
import nars.operator.mental.FeelHappy;
import nars.operator.mental.Hesitate;
import nars.operator.mental.Name;
import nars.operator.mental.Register;
import nars.operator.mental.Remind;
import nars.operator.mental.Want;
import nars.operator.mental.Wonder;
import nars.operator.software.Javascript;
import nars.operators.math.Count;


public class DefaultOperators {
    /**
     * Default set of Operator's for core functionality.
     * An operator name should contain at least two characters after '^'.
     *
     */
    public static Operator[] get() {
        ArrayList<Operator> operators = new ArrayList<Operator>();
        
        //new Wait(),                    // wait for a certain number of clock cycle
        operators.add(new NullOperator());
        operators.add(new Believe());    // accept a statement with a default truth-value
        operators.add(new Want());       // accept a statement with a default desire-value
        operators.add(new Wonder());     // find the truth-value of a statement
        operators.add(new Evaluate());   // find the desire-value of a statement
            
        // concept operations for internal perceptions
        operators.add(new Remind());     // create/activate a concept
        operators.add(new Consider());   // find the most active concept            
        operators.add(new Name());       // turn a compount term into an atomic term
        operators.add(new Abbreviate());
        operators.add(new Register());
            
        // truth-value operations
        operators.add(new Doubt());        // decrease the confidence of a belief
        operators.add(new Hesitate());     // decrease the confidence of a goal

        // feeling operations
        operators.add(new FeelHappy());    // the overall happyness, average solution quality, and predictions
        operators.add(new FeelBusy());     // the overall business

        // math operations
        operators.add(new Count());
        operators.add(new Add());

        operators.add(new Javascript());   // javascript evaluation
                
        
        return operators.toArray(new Operator[operators.size()]);

        /* 
         *          I/O operations under consideration
         * observe          // get the most active input (Channel ID: optional?)
         * anticipate       // get the input matching a given statement with variables (Channel ID: optional?)
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)
         */
        
        /*
         * -think            // carry out a working cycle
         * -do               // turn a statement into a goal
         *
         * possibility      // return the possibility of a term
         * doubt            // decrease the confidence of a belief
         * hesitate         // decrease the confidence of a goal
         *
         *
        
        
         * do               // to turn a judgment into a goal (production rule) ??
        
         *
         * count            // count the number of elements in a set
         * arithmatic       // + - * /
         * comparisons      // < = >
         * inference        // binary inference
         *

         * -assume           // local assumption ???
         * 
         * observe          // get the most active input (Channel ID: optional?)
         * anticipate       // get input of a certain pattern (Channel ID: optional?)
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)        
        
         * name             // turn a compount term into an atomic term ???
         * -???              // rememberAction the history of the system? excutions of operations?
         */
                
        /* operators for testing examples */
//        table.put("^go-to", new GoTo("^go-to"));
//        table.put("^pick", new Pick("^pick"));
//        table.put("^open", new Open("^open"));
//        table.put("^break", new Break("^break"));
//        table.put("^drop", new Drop("^drop"));
//        table.put("^throw", new Throw("^throw"));
//        table.put("^strike", new Strike("^strike"));
    }
}
