package nars.operator;

import nars.operator.mental.Believe;

public class DefaultOperators {
    
    
    /**
     * Default set of Operator's for core functionality.
     * An operator name should contain at least two characters after '^'.
     *     
     */    
    public static Operator[] get() {
        
        return new Operator[] {
            //new Wait(),            
            new Sample(),
            new Believe(), //accept a statement with a default truth-value
//        table.put("^want", new Want("^want"));              // accept a statement with a default desire-value
//        table.put("^wonder", new Wonder("^wonder"));        // find the truth-value of a statement
//        table.put("^assess", new Assess("^assess"));        // find the desire-value of a statement
//        /* operators for internal perceptions */
//        table.put("^consider", new Consider("^consider"));  // find the most active concept
//        table.put("^remind", new Remind("^remind"));        // create/activate a concept
//        table.put("^wait", neSampleit("^wait"));              // wait for a certain number of clock cycle
        /*
         * observe          // process a new task (Channel ID: optional?)
         * think            // carry out a working cycle
         * do               // turn a statement into a goal
         *
         * possibility      // return the possibility of a term
         * doubt            // decrease the confidence of a belief
         * hesitate         // decrease the confidence of a goal
         *
         * feel             // the overall happyness, average solution quality, and predictions
         * busy             // the overall business
         *
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)
         *
         * count            // count the number of elements in a set
         * arithmatic       // + - * /
         * comparisons      // < = >
         * inference        // binary inference
         *
         * assume           // local assumption ???
         * name             // turn a compount term into an atomic term ???
         * ???              // rememberAction the history of the system? excutions of operatons?
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
