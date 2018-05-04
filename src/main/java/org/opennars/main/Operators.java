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
package org.opennars.main;

import org.opennars.operator.NullOperator;
import org.opennars.operator.Operator;
import org.opennars.operator.misc.Add;
import org.opennars.operator.misc.Count;
import org.opennars.operator.mental.Believe;
import org.opennars.operator.mental.Consider;
import org.opennars.operator.mental.Doubt;
import org.opennars.operator.mental.Evaluate;
import org.opennars.operator.mental.FeelBusy;
import org.opennars.operator.mental.FeelSatisfied;
import org.opennars.operator.mental.Hesitate;
import org.opennars.operator.mental.Name;
import org.opennars.operator.mental.Register;
import org.opennars.operator.mental.Remind;
import org.opennars.operator.mental.Want;
import org.opennars.operator.mental.Wonder;
import org.opennars.operator.misc.Reflect;
//import org.opennars.operator.misc.Javascript;


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
