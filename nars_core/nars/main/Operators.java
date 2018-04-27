package nars.main;

import nars.main.NAR;
import nars.operator.NullOperator;
import nars.operator.Operator;
import nars.operator.misc.Add;
import nars.operator.misc.Count;
import nars.operator.mental.Believe;
import nars.operator.mental.Consider;
import nars.operator.mental.Doubt;
import nars.operator.mental.Evaluate;
import nars.operator.mental.FeelBusy;
import nars.operator.mental.FeelSatisfied;
import nars.operator.mental.Hesitate;
import nars.operator.mental.Name;
import nars.operator.mental.Register;
import nars.operator.mental.Remind;
import nars.operator.mental.Want;
import nars.operator.mental.Wonder;
import nars.operator.misc.Reflect;
//import nars.operator.misc.Javascript;


public class Operators {
    
    
    /**
     * Default set of Operator's for core functionality.
     * An operator name should contain at least two characters after '^'.
     *     
     */    
    public static Operator[] get(NAR nar) {
        
        //Javascript js=new Javascript();
        //js.setEnabled(nar, true);
        
        return new Operator[] {
            
            //example operators
            new NullOperator("^break"),
            new NullOperator("^drop"),
            new NullOperator("^go-to"),
            new NullOperator("^open"),
            new NullOperator("^pick"),
            new NullOperator("^strike"),
            new NullOperator("^throw"),
            new NullOperator("^activate"),
            new NullOperator("^deactivate"),
            new NullOperator("^lighter"),
            new NullOperator("^reshape"),
            new NullOperator("^escape"),
            new NullOperator("^say"),
            new NullOperator("^right"),
            new NullOperator("^left"),
            new NullOperator("^run"),
            new NullOperator("^feel"),
            //new Wait(),            
            //new Wait(),            
            new NullOperator(),
            new Believe(),  // accept a statement with a default truth-value
            new Want(),     // accept a statement with a default desire-value
            new Wonder(),   // find the truth-value of a statement
            new Evaluate(), // find the desire-value of a statement
            
            //concept operations for internal perceptions
            new Remind(),   // create/activate a concept
            new Consider(),  // find the most active concept            
            new Name(),         // turn a compount term into an atomic term
            //new Abbreviate(),
            new Register(),
            
            // truth-value operations
            new Doubt(),        // decrease the confidence of a belief
            new Hesitate(),      // decrease the confidence of a goal
            

            //Meta
            new Reflect(),
            
            // feeling operations
            new FeelSatisfied(),
            new FeelBusy(),

            // math operations
            new Count(),
            new Add(),
           // new MathExpression(),
                        

           // js,      // javascript evaluation
            
           // new NumericCertainty()
                
        
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
               
            
        };
        
    } 
    
}
