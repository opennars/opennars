package nars.jwam.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import nars.jwam.RuleHeap;
import nars.jwam.WAM;
import nars.jwam.datastructures.Numbers;
import nars.jwam.datastructures.IntArrayList;
import nars.jwam.datastructures.Strings;

/**
 * This class is used by the parser. By calling what is encountered during
 * parsing (such as numbers, variables etc.) objects of this class can construct
 * a WAM based heap representation of the prolog code. Clauses are stored as
 * :-/n structures, with n being the amount of bodypars plus one (the head).
 *
 * After heap representations are build, the compiler can turn them into
 * instructions. We also store the heap representations of dynamic predicates in
 * the WAM because this allows for fast retraction (you can easily see if a
 * clause matches the to-be-retracted item). Note that with agents in mind this
 * extra storage of the heap representation is quite small. For instance a fact
 * like has(agent0,gold) only takes four integers (which is less than the
 * required instructions of building this structure on the heap).
 *
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class ToHeap {

    private Strings strings = null; 						// Storage of String identities
    private Numbers nums = null;
    private IntArrayList heap = null; 							// The current heap
    private RuleHeap rules = null;						// Stores the already present rules
    private HashMap<String, Integer> var_ids = null; 				// Maps variable names to variable numbers
    private ArrayList<IntArrayList> argrefs = null; 			// Stack for references to predicate arguments
    private boolean isQuery = false;

    /**
     * Constructor. The StringContainer of the argument should be tied to a
     * single compiler and WAM engine. Otherwise the String id's might be
     * inconsistent.
     *
     * @param strings Container that keeps the String to Integer conversion.
     */
    public ToHeap(RuleHeap rules, Strings strings, Numbers nums) {
        this.strings = strings;										// Strings and initial rules are given
        this.nums = nums;
        this.rules = rules;
        heap = new IntArrayList();								// The rest is initialized
        var_ids = new HashMap<String, Integer>();
        argrefs = new ArrayList<IntArrayList>();
    }

    /**
     * Announce the start of a new predicate or list. Will make sure that the
     * next arguments are added to this structures data.
     */
    public void initiatePrologStructure() {
        argrefs.add(new IntArrayList());						// Add an argument reference list to the stack
    }

    /**
     * Finish a predicate.
     *
     * @param functorname The functor name of the predicate. E.g. p(x) has as
     * functor name "p".
     */
    public void finishStructure(String functorname) {
        IntArrayList args = argrefs.remove(argrefs.size() - 1);	// Get the argument references
        if (args.isEmpty()) { 										// Actually a constant if there are no arguments
            addConstant(functorname);
        } else {
            int index = heap.size();								// Otherwise create the functor cell <PN,functor>
            heap.add(WAM.make_cell(WAM.PN, strings.add(functorname, args.size())));
            for (int i = 0; i < args.size(); i++) {
                heap.add(args.get(i)); 						// Add the arguments in consecutive order
            }
            if (argrefs.isEmpty()) // Returned to rule level
            {
                heap.add(WAM.make_cell(WAM.STR, index)); 		// So store the start of the action in the heap itself 
            } else // Otherwise store the STR cell in the parent's arguments
            {
                argrefs.get(argrefs.size() - 1).add(WAM.make_cell(WAM.STR, index));
            }
        }
    }

    /**
     * Finish the construction of a list.
     */
    public void finishList() {
        if (argrefs.get(argrefs.size() - 1).size() == 1) // Only one argument was given at the end of the list
        {
            addConstant("[]"); 										// So provide the [] constant as a tail
        }
        IntArrayList args = argrefs.remove(argrefs.size() - 1);	// Get the argument references (should be two)
        if (args.isEmpty()) {
            addConstant("[]"); 						// No arguments, so the list is the constant []
        } else {   													// Now we must have two arguments from which the second is the tail
            int index = heap.size();
            heap.add(args.get(0)); 							// Add list element
            heap.add(args.get(1)); 							// Add tail
            argrefs.get(argrefs.size() - 1).add(WAM.make_cell(WAM.LIS, index)); // Add LIS cell to parent
        }
    }

    /**
     * Add a number to the list.
     *
     * @param str String representation of the number. Ends with "f" if floating
     * point, "s" if simple. If no character is provided, then type depends on
     * whether the number contains a floating point.
     */
    public void addNum(String str) {
        int value = nums.new_number(str);
        argrefs.get(argrefs.size() - 1).add(WAM.make_cell(WAM.NUM, value)); // Otherwise create the functor cell <PN,functor>
    }

    /**
     * Adds a variable to the administration. If it is a new variable it gets a
     * new identification number.
     *
     * @param name Name of the variable. "_" always gets id 0.
     */
    public void addVar(String name) {
        if (!var_ids.containsKey(name)) // If the variable is not known
        {
            var_ids.put(name, var_ids.size());						// Assign id
        }
        Integer var_index = var_ids.get(name); 						// Get the id and add to parent's arguments 
        if (isQuery) {
            rules.getQueryVars().put(var_index, name);		// Store the last reference to a variable in a query
        }
        argrefs.get(argrefs.size() - 1).add(WAM.make_cell(WAM.REF, var_index));
    }

    /**
     * Add a constant to the heap.
     *
     * @param name Name of the constant.
     */
    public void addConstant(String name) {
        if (argrefs.isEmpty()) // Top level construction (entry of constant as fact, e.g. "a.")
        {
            heap.add(WAM.make_cell(WAM.CON, strings.add(name, 0) >> 7));
        } else // Otherwise add to parent's arguments
        {
            argrefs.get(argrefs.size() - 1).add(WAM.make_cell(WAM.CON, strings.add(name, 0) >> 7));
        }
    }

    /**
     * Announce the start of a rule. Will clear the heap, variable
     * administration and argument references.
     */
    public void startRule() {
        heap.clear(); 												// Make room for the next rule
        var_ids.clear();
        var_ids.put("_", 0); 										// Reserve anonymous variable
        argrefs.clear();											// Clear the argument references
    }

    /**
     * Announce the end of a rule. Will add the rule to the rule base. If the
     * rule has a body, then it will look on the stack like
     * ":-(head,bodypt1,...,bodyptn)". The functor under which the rule will
     * then be stored is the functor of the head (first argument).
     */
    public void finishRule() {
        int functor_cell = heap.getLast();						// Get the cell containing the functor
        int functor_int = WAM.cell_value(functor_cell) << 7; 			// Assume for the moment that we have a constant
        int tag = WAM.cell_tag(functor_cell);  						// Get the tag of the functor cell
        if (tag == WAM.STR) {											// If the tag is STR then the value points to the PN cell
            int functor_address = WAM.cell_value(functor_cell);		// So get that address
            functor_cell = heap.get(functor_address); 			// Go to PN cell
            functor_int = WAM.cell_value(functor_cell);				// Grab the real functor integer
            if (strings.get(functor_int).startsWith(":-/")) { 	// If it was a body rule
                functor_cell = heap.get(functor_address + 1); 	// Go to the head and get its functor number
                if (WAM.cell_tag(functor_cell) == WAM.CON) // Again mind the CON or STR situation
                {
                    functor_int = WAM.cell_value(functor_cell) << 7;
                } else {
                    functor_int = WAM.cell_value(heap.get(WAM.cell_value(functor_cell)));
                }
            }
        }
        rules.addHeap(functor_int, Arrays.copyOf(heap.data, heap.size()), false); // Finally add the heap to the rule base 
    }

    /**
     * Announce the end of a query. Will store the heap in the rule database
     * (overwrites the last query).
     */
    public void startQuery() {
        rules.getQueryVars().clear();								 // Make space for variables
        isQuery = true;
        startRule(); 												 // Also start a new rule
    }

    /**
     * Announce the end of a query. Will store the heap in the rule database
     * (overwrites the last query).
     */
    public void finishQuery() {
        rules.setQueryHeap(Arrays.copyOf(heap.data, heap.size()));
        isQuery = false;
    }

    /**
     * Process a directive. Not implemented yet.
     *
     * @param arguments Everything after ":-" until "."
     */
    public void addDirective(ArrayList<String> arguments) {
        if (arguments.get(0).equals("dynamic") && arguments.size() == 2) {  // Declaration of a dynamic predicate
            rules.addDynamic(arguments.get(1));
        }
    }
}
