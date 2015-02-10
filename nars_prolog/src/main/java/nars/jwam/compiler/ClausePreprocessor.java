package nars.jwam.compiler;

import nars.jwam.RuleHeap;
import nars.jwam.WAM;
import nars.jwam.datastructures.IntArrayList;
import nars.jwam.datastructures.Numbers;
import nars.jwam.datastructures.Strings;
import nars.jwam.datastructures.WAMToString;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This whole class is dedicated to step one of converting heaps to
 * instructions: assigning argument registers and scanning variables for their
 * properties. Initially this class was one method, but for readability I
 * rewrote it into this class. The main thing that happens is that the fact/reason
 * is traversed in breadth-first order per body part (and the head, with which
 * it starts). The first rounds - the arguments of the body parts - get the
 * argument registers. Variables which are permanent also get their registers
 * for register 1, so permanent variables that occur as body part arguments
 * (i.e. the unsafe variables) already have both registers after this point.
 *
 * The scanner also automatically detects anonymous variables which were
 * assigned a name in the source file (the Prolog source, that is).
 *
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class ClausePreprocessor {

    private final Strings strings;
    private final Numbers numbers;
    private final HashMap<Integer, Integer> first_occ; 					// Map from variable name to first body part it occurs in, both for permanent and temporary as you do not know the kind upon the first encounter with the variable
    private final ArrayList<Integer> first_occ_as_body_arg; 				// Array for determining unsafe variables, permanent variables for which the first occurrence is as an argument of a body part are unsafe
    private final ArrayList<Integer> unsafe; 								// Unsafe variables
    private final HashMap<Integer, Integer> last_occ; 						// Last occurrence body part of PERMANENT variables only, needed for environment trimming
    private final HashMap<Integer, ArrayList<Token>> occurrences; 			// Occurrences of variables
    private final HashMap<Integer, Integer> var_registers; 				// X registers of variables
    private int[] heap;
    private Token[] tokens;
    private HashMap<Integer, String> query_vars;					// Vars of query (for string to answer value)
    private final RuleHeap dsm;

    /**
     * Constructor. Requires the compiler's, and thus the WAM's, StringContainer
     * for output when debugging.
     *
     * @param strings
     */
    public ClausePreprocessor(Strings strings, Numbers numbers, RuleHeap dsm) {
        this.strings = strings;										// Instantiate the data
        this.numbers = numbers;
        first_occ = new HashMap<>();
        first_occ_as_body_arg = new ArrayList<>();
        unsafe = new ArrayList<>();
        last_occ = new HashMap<>();
        occurrences = new HashMap<>();
        var_registers = new HashMap<>();
        query_vars = new HashMap<>();
        this.dsm = dsm;
    }

    /**
     * Reset the data to initial state. Saves some initiation of new objects if
     * you re-use an instance of this scanner by resetting it before putting
     * another heap through it.
     */
    public void reset() {
        first_occ.clear();
        first_occ_as_body_arg.clear();
        unsafe.clear();
        last_occ.clear();
        occurrences.clear();
        var_registers.clear();
    }

    /**
     * Main functionality of the scanner: go through the variables and scan them
     * for their properties such as permanence.
     *
     * @param heap Entry heap of a reason/fact.
     * @param tokens Tokens for each of the heap cells.
     * @param isQuery Whether a query is being compiled. For queries we do not
     * take the head and first body part together when determining permanent
     * variables.
     */
    private void scanVarsAndAssignArgRegisters(boolean isQuery) {
        int nr_parts = Compiler.numberOfParts(heap, heap.length - 1, strings); 				// Get the amount of body parts +1 for the head
        int start_of_parts = Compiler.getPartsStart(heap, heap.length - 1, strings);		// Get the start of the body parts
        int end_of_parts = start_of_parts + nr_parts - 1;									// Determine the end
        for (int part = start_of_parts; part <= end_of_parts; part++) { 					// For each part 
            IntArrayList round = new IntArrayList(); 							// Breadth-first overhead: current level 
            Compiler.initialRound(heap, part, round); 									// Add the children of the part for starters
            if (WAM.cell_tag(heap[part]) == WAM.REF) {
                round.add(part);
            }
            IntArrayList next = new IntArrayList(); 							// Breadth-first overhead: next depth level
            int a = 1; 																	// Start at A1
            int max_arg = round.size();													// Maximum of argument registers to distribute
            while (!round.isEmpty()) { 													// While you have not processed the tree's leaves
                next.clear();															// Clear data for the next round
                for (int i = 0; i < round.size(); i++) {									// For each argument
                    Token t = tokens[round.get(i)];								// Get the token
                    if (part == end_of_parts) {
                        t.part_of_last_goal = true; 					// Needed for put_unsafe_value instructions
                    }
                    if (a <= max_arg) {
                        t.register2 = a++; 									// Set second register for arguments 
                    }
                    Compiler.addChildren(heap, t.index, next); 							// Add children to the next round
                    variable_properties(t.index, isQuery, t.register2 != 0, part, start_of_parts); // Determine the properties
                }
                round.clear(); 															// Breadth first overhead: Set up for next depth level
                round.addAll(next);
            }
        }
        determine_Y_registers(start_of_parts, end_of_parts, isQuery);						// Distribute the permanent registers
        if (!isQuery) {
            detect_anonymous_variables(); 										// Filter out all named variables that occur only once
        }
    }

    /**
     * Detection whether a variable is permanent and or unsafe.
     *
     * @param heap
     * @param tokens
     * @param index
     * @param isQuery
     * @param isArg
     * @param partNr
     */
    private void variable_properties(int index, boolean isQuery, boolean isArg, int partNr, int start_of_parts) {
        int var_nr = WAM.cell_value(heap[index]);
        if (WAM.cell_tag(heap[index]) == WAM.REF && var_nr != 0) {
            int occ_part = isQuery ? partNr : (partNr == start_of_parts ? (start_of_parts + 1) : partNr); // If not query then head and first body part are taken together
            if (first_occ.get(var_nr) == null) { 											// First time you encountered this variable 
                if (isArg && (isQuery || partNr > start_of_parts)) {
                    first_occ_as_body_arg.add(var_nr); // First occurence, and part of the body, thus possibly unsafe if the variable is permanent
                }
                first_occ.put(var_nr, occ_part); 										// Remember the bodypart number (for programs the head and first part are numbered 1)
                occurrences.put(var_nr, new ArrayList<>()); 						// Make space for remembering the occurrences
                occurrences.get(var_nr).add(tokens[index]); 							// Remember this occurrence
                if (isQuery) {															// Within a query we keep all variables permanent so we can see after the query execution which values they've got
                    tokens[index].is_perm = true;
                    if (first_occ_as_body_arg.contains(var_nr)) {
                        tokens[index].is_unsafe = true;
                        unsafe.add(var_nr);
                    }
                    last_occ.put(var_nr, partNr);
                }
            } else if (first_occ.get(var_nr) != occ_part && last_occ.get(var_nr) == null) { 	// First encounter permanent variable. Occurred before in another body part, thus this variable is permanent
                ArrayList<Token> this_occs = occurrences.get(var_nr);				 	// Get the occurrences of this variable
                this_occs.add(tokens[index]); 											// Add the child to it
                boolean isUnsafe = first_occ_as_body_arg.contains(var_nr); 				// It is a perm variable that occurs as arg of a body part, thus it is unsafe
                if (isUnsafe) {
                    unsafe.add(var_nr);
                }
                for (Token occurrence : this_occs) {
                    occurrence.is_perm = true; 											// Set all occurrences to permanent
                    if (isUnsafe) {
                        occurrence.is_unsafe = true; 							// If needed, set to unsafe
                    }
                }
                last_occ.put(var_nr, partNr); 											// Remember last occurrence
            } else if (last_occ.get(var_nr) != null) { 									// Second encounter and child is permanent
                tokens[index].is_perm = true; 											// Set the permanent flag
                if (unsafe.contains(var_nr)) {
                    tokens[index].is_unsafe = true; 			// It is an unsafe variable
                }
                occurrences.get(var_nr).add(tokens[index]); 							// Add occurrence
                last_occ.put(var_nr, partNr); 											// Remember last occurrence
            } else {
                occurrences.get(var_nr).add(tokens[index]); 							// Second encounter and child is not permanent
            }
        }
    }

    /**
     * Order the permanent variables and assign them to body parts. Afterwards
     * determine their permanent address. This is needed for environment
     * trimming. If we can reclaim space after a subgoal than we can be more
     * memory efficient.
     *
     * @param tokens
     * @param start_of_parts
     * @param end_of_parts
     */
    private void determine_Y_registers(int start_of_parts, int end_of_parts, boolean isQuery) {
        if (isQuery) {
            query_vars.clear();															// Clear vars
        }
        ArrayList<ArrayList<Integer>> last_occ_per_part = new ArrayList<>(); 	// Make space for computation of Y registers
        for (int part = start_of_parts; part <= end_of_parts; part++) // Initiate administration memory
        {
            last_occ_per_part.add(new ArrayList<>());
        }
        for (Integer key : last_occ.keySet()) // Assign variables to parts where they last occurred
        {
            last_occ_per_part.get(last_occ.get(key) - start_of_parts).add(key);
        }
        int y = last_occ.keySet().size();														// Amount of permanent variables
        for (int part = start_of_parts + 1; part <= end_of_parts; part++) {
            if (tokens[part].is_cut) {
                y++;															// For each deep cut: y++ to reserve a variable
            }
        }
        for (int part = start_of_parts; part <= end_of_parts; part++) {  							// For each part
            for (int i = 0; i < last_occ_per_part.get(part - start_of_parts).size(); i++) {			// For each variable that occurs last in that part
                for (Token occurrence : occurrences.get(last_occ_per_part.get(part - start_of_parts).get(i))) {
                    occurrence.register1 = y; 													// Set the Yi index of the occurrences  
                    String name = dsm.getQueryVars().get(WAM.cell_value(heap[occurrence.index]));
                    if (name != null) {
                        query_vars.put(y, name);										// Store the perm reg for named vars
                    }
                }
                y--;																			// Update the register counter
            }
            tokens[part].perm_vars_afterwards = isQuery ? last_occ.keySet().size() : y;	// Store how many permanent variables occur after this body part (y-1 = amount that had the last occurrence in this part, last_occ.values().size() = total amount of permanent variables
            if (part > 0 & tokens[part].is_cut) { 		// Assign a permanent variable to cuts
                tokens[part].cut_var = y;
                y--;
            }
        }
        if (isQuery) {
            dsm.setQueryVars(query_vars);															// Replace heap indices for perm variables
        }
        query_vars = new HashMap<>();
    }

    /**
     * Filter out all the variables which were given names but only used once.
     * At runtime this saves some time.
     *
     * @param heap Data heap of the clause in which the variables occur.
     */
    private void detect_anonymous_variables() {
        for (Integer key : occurrences.keySet()) // For clauses like: below(P,point(X,Y)):- Y < P. Here X should be anonymous which speeds up execution a bit.
        {
            if (occurrences.get(key).size() == 1) // Only one occurrence in the entire reason, so it is anonymous
            {
                heap[occurrences.get(key).get(0).index] = WAM.newCell(WAM.REF, 0); 	// Make it anonymous  
            }
        }
    }

    public void assign_registers(int[] heap, Token[] tokens, boolean isQuery) {
        this.heap = heap;
        this.tokens = tokens;
        scanVarsAndAssignArgRegisters(isQuery);
        int x = 0;
        int nr_parts = Compiler.numberOfParts(heap, heap.length - 1, strings); 				// Get the amount of body parts +1 for the head
        int start_of_parts = Compiler.getPartsStart(heap, heap.length - 1, strings);		// Get the start of the body parts
        int end_of_parts = start_of_parts + nr_parts - 1;									// Determine the end
        for (int part = start_of_parts; part <= end_of_parts; part++) { 					// For each part 
            IntArrayList round = new IntArrayList(); 							// Breadth-first overhead: current level 
            Compiler.initialRound(heap, part, round); 									// Add the children of the part for starters
            IntArrayList next = new IntArrayList(); 							// Breadth-first overhead: next depth level
            if (isQuery || // In query do not take head and pt1 together
                    (part > start_of_parts + 1) || // Part is number 2 or beyond
                    (part == start_of_parts && start_of_parts == end_of_parts)) {					// Part is one and only of the clause (i.e. fact)
                x = Compiler.numberOfArgs(heap, part) + 1;								// Initiate x beyond the argument registers
            } else if (part == start_of_parts && start_of_parts != end_of_parts) // Part is head of a reason
            {
                x = Math.max(Compiler.numberOfArgs(heap, part), Compiler.numberOfArgs(heap, part + 1)) + 2; // Max of head and first body part
            }
            while (!round.isEmpty()) {
                next.clear();															// Clear data for the next round
                for (int i = 0; i < round.size(); i++) {									// For each argument
                    Token t = tokens[round.get(i)];								// Get the token 
                    Compiler.addChildren(heap, t.index, next);			 				// Add children to the next round  
                    int tag = WAM.cell_tag(heap[t.index]);
                    int value = WAM.cell_value(heap[t.index]);
                    if (tag == WAM.REF && value != 0) // Non-anonymous variables take some extra processing
                    {
                        x = assign_variable(t, value, x);									// Assign X to the variable
                    } else if ((tag == WAM.STR || tag == WAM.LIS) && t.register2 == 0) // Non argument LIS/STR entries get an x register
                    {
                        t.register2 = x++;
                    }
                }
                round.clear(); 															// Breadth first overhead: Set up for next depth level
                round.addAll(next);
            }
            if (isQuery || part > start_of_parts + 1) {
                var_registers.clear();
            }
        }
        //outputResults(); 																// For debugging
    }

    private int assign_variable(Token t, int var_nr, int x) {
        if (t.is_perm && t.register2 == 0) { 													// Permanent non-arg ref needs to move the Y assignment from register 1 to register 2
            t.register2 = t.register1;
            t.register1 = 0;
        } else if (!t.is_perm) { 															// Temporary variable has X1 at register 1 and Ai at register 2 (if it is an argument)
            Integer register = var_registers.get(var_nr); 								// Get or create the register for this variable
            if (register == null) {
                register = x++;
                var_registers.put(var_nr, register);
            }
            if (t.register2 != 0) {
                t.register1 = register;									// If the variable is an argument, then put the register at slot 1
            } else {
                t.register2 = register; 												// Otherwise slot 2 
            }
        }
        return x;
    }

    /**
     * For debugging, should be called �fter assign_registers is performed.
     */
    public void outputResults() {
        System.out.println("/////////// Preprocessing of " + WAMToString.termToString(heap, heap.length - 1, strings, numbers, true) + " heap: " + WAMToString.oneLineHeap(heap, 0, heap.length, strings, numbers));
        System.out.println("/////////// VARIABLE PREPROCESSING");
        for (Integer var : occurrences.keySet()) {
            System.out.println("VAR " + var + " in " + WAMToString.termToString(heap, heap.length - 1, strings, numbers, true) + " heap: " + WAMToString.oneLineHeap(heap, 0, heap.length, strings, numbers));
            for (Token t : occurrences.get(var)) {
                System.out.println('\t' + "Address=" + t.index + " Register 1=" + t.register1 + "  Register 2=" + t.register2 + " Is permanent=" + t.is_perm + " Is unsafe=" + t.is_unsafe + " Was made anonymous=" + (WAM.cell_value(heap[t.index]) == 0));

            }
        }
        System.out.println("/////////// REGISTER ASSIGNMENT");
        System.out.println(WAMToString.registeredTerm(heap, tokens, heap.length - 1, strings, numbers));
    }
}
