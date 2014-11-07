package nars.core.build;

import nars.operator.NullOperator;
import nars.operator.Operator;
import nars.operator.io.Say;
import nars.operator.math.Add;
import nars.operator.math.Count;
import nars.operator.math.Expression;
import nars.operator.mental.Anticipate;
import nars.operator.mental.Believe;
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
import nars.operator.software.NumericCertainty;


public class DefaultOperators {
    
    
    /**
     * Default set of Operator's for core functionality.
     * An operator name should contain at least two characters after '^'.
     *     
     */    
    public static Operator[] get() {
        
        return new Operator[] {
            //new Wait(),            
            new NullOperator(),
            new Believe(),  // accept a statement with a default truth-value
            new Want(),     // accept a statement with a default desire-value
            new Wonder(),   // find the truth-value of a statement
            new Evaluate(), // find the desire-value of a statement
            
            //concept operations for internal perceptions
            new Remind(),   // create/activate a concept
            //new Consider(nal),  // find the most active concept            
            new Name(),         // turn a compount term into an atomic term
            //new Abbreviate(),
            new Register(),
            
            // truth-value operations
            new Doubt(),        // decrease the confidence of a belief
            new Hesitate(),      // decrease the confidence of a goal
            
            new Anticipate(),      // expect an event

            // feeling operations
            new FeelHappy(),
            new FeelBusy(),

            // math operations
            new Count(),
            new Add(),
            new Expression(),

            new Javascript(),      // javascript evaluation
            
            new NumericCertainty(),
            
            //io operations
            new Say(),
                
         /* 
+         *          I/O operations under consideration
+         * observe          // get the most active input (Channel ID: optional?)
+         * anticipate       // get the input matching a given statement with variables (Channel ID: optional?)
+         * tell             // output a judgment (Channel ID: optional?)
+         * ask              // output a question/quest (Channel ID: optional?)
+         * demand           // output a goal (Channel ID: optional?)
+         */
                
//        new Wait()              // wait for a certain number of clock cycle
        
        
        /*
         * -think            // carry out a working cycle
         * -do               // turn a statement into a goal
         *
         * possibility      // return the possibility of a term
         * doubt            // decrease the confidence of a belief
         * hesitate         // decrease the confidence of a goal
         *
         * feel             // the overall happyness, average solution quality, and predictions
         * busy             // the overall business
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
         * -???              // rememberAction the history of the system? excutions of operatons?
         */
                
        /* operators for testing examples */
//        table.put("^go-to", new GoTo("^go-to"));
//        table.put("^pick", new Pick("^pick"));
//        table.put("^open", new Open("^open"));
//        table.put("^break", new Break("^break"));
//        table.put("^drop", new Drop("^drop"));
//        table.put("^throw", new Throw("^throw"));
//        table.put("^strike", new Strike("^strike"));
            
        };
        
    } 
    
}
