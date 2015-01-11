package nars.jwam.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import nars.jwam.RuleHeap;
import nars.jwam.WAM;
import nars.jwam.datastructures.Numbers;
import nars.jwam.datastructures.IntArrayList;
import nars.jwam.datastructures.Strings;
import nars.jwam.datastructures.WAMToString;

public class SingleClauseCompiler {

    Strings strings;
    Numbers nums;
    HashSet<Integer> built_ins;
    ClausePreprocessor preprocessor;
    int registerstart;
    int[] heap;
    Token[] tokens;
    boolean q; // is query
    boolean deep_cut;
    ArrayList<Integer> enc_perm; // encountered permanent registers
    ArrayList<Integer> enc_temp; // encountered temporary registers
    ArrayList<Integer> explicitly_initialized; // encountered temporary registers
    ArrayList<Integer> sec_enc_perm; // encountered temporary registers
    ArrayList<Integer> instructions;

    public SingleClauseCompiler(Strings strings, Numbers nums, int registerstart, RuleHeap dsm) {
        this.built_ins = new HashSet<Integer>();
        this.strings = strings;
        this.nums = nums;
        this.registerstart = registerstart;
        preprocessor = new ClausePreprocessor(strings, nums, dsm);
        enc_perm = new ArrayList<Integer>(); // encountered permanent registers
        enc_temp = new ArrayList<Integer>(); // encountered temporary registers
        explicitly_initialized = new ArrayList<Integer>(); // encountered temporary registers
        sec_enc_perm = new ArrayList<Integer>(); // encountered temporary registers
        instructions = new ArrayList<Integer>();
    }

    public HashSet<Integer> getBuiltIns() {
        return built_ins;
    }

    public void reset() {
        preprocessor.reset();
        enc_perm.clear();
        enc_temp.clear();
        explicitly_initialized.clear();
        sec_enc_perm.clear();
        instructions.clear();
        deep_cut = false;
        q = false;
    }

    /**
     * Compile a single clause
     *
     * @param heap
     * @param isQuery
     */
    public void compile_clause(int[] heap, boolean isQuery, RuleHeap dsm) {
        int[] program = compile_heap(heap, isQuery, heap.length - 1, dsm);
        if (isQuery) {
            dsm.setQueryInstructions(program);
        } else {
            dsm.addInstructions(Compiler.getTopFN(heap, heap.length - 1, strings), program, false);
        }
    }

    public int[] compile_heap(int[] heap, boolean isQuery, int start, RuleHeap dsm) {
        reset(); 
        
        // Create token array
        this.heap = heap;
        
        // Get the start of the body parts
        int start_of_parts = Compiler.getPartsStart(heap, start, strings);
        if (start_of_parts + 1 >= heap.length) {
            /*throw new RuntimeException("Can't make deep cut for: " + Arrays.toString(tokens) + " at OOB index " + (start_of_parts + 1) + " heap=" + Arrays.toString(heap) + ", start=" + start);*/
        }

        // Reset the data
        tokens = new Token[heap.length]; 	
        
        for (int i = 0; i < tokens.length; i++) { 										// Initialize tokens
            tokens[i] = new Token(i); 													// These will hold information about variables etc 
            if (WAM.cell_tag(heap[i]) == WAM.CON && WAM.cell_value(heap[i]) == strings.getInt("!")) {
                tokens[i].is_cut = true;												// Cuts require some extra data
                if (i == start_of_parts + 1) {
                    tokens[i].is_neck = true;							// Neck cut: a:-!,...
                } else { 
                    // Deep cut: a:-b,...,!,...
                    tokens[start_of_parts + 1].make_get_level = true;						// Signal for get_level instruction
                    tokens[start_of_parts + 1].cut_var = i;
                    // Store token index
                    tokens[i].is_deep = true;
                    // Set is_deep boolean
                }
            }
        }
        preprocessor.assign_registers(heap, tokens, isQuery);								// Assign registers to tokens
        int nr_parts = Compiler.numberOfParts(heap, start, strings); 						// Get the amount of body parts +1 for the head
        int end_of_parts = start_of_parts + nr_parts - 1;									// Determine the end 
        for (int part = start_of_parts; part <= end_of_parts; part++) { 					// For each part 
            if (isQuery || (part > start_of_parts)) // If query or body part: to query instructions
            {
                query_instructions(part);
            } else {
                program_instructions(part);											// Head is program instruction (gets etc)
            }
        }
        fix_clause_end(nr_parts, isQuery);
        int[] program = new int[instructions.size()];
        for (int i = 0; i < program.length; i++) {
            program[i] = instructions.get(i);
        }
        return program;
    }

    private void query_instructions(int part) {
        q = true;

        // Make cut related instructions if the part is a cut. When deep cut, the first body part contains in cut_var the index
        // of the token which holds the permanent variable of the cut in its cut_var field.
        if (tokens[part].is_neck) {
            instructions.add(WAM.make_instruction(WAM.NECK_CUT, 0));
        }
        if (tokens[part].make_get_level) {
            instructions.add(WAM.make_instruction(WAM.GET_LEVEL, tokens[tokens[part].cut_var].cut_var));
        }
        if (tokens[part].is_deep) {
            instructions.add(WAM.make_instruction(WAM.DEEP_CUT, tokens[part].cut_var));
        }
        if (!tokens[part].is_cut) {														// For non cut body parts
            IntArrayList grandchildren = new IntArrayList();
            IntArrayList children = new IntArrayList();
            Compiler.addChildren(heap, part, children);
            for (int child = 0; child < children.size(); child++) {						// First process the grand children (non args)
                grandchildren.clear();
                Compiler.addChildren(heap, children.get(child), grandchildren);
                for (int grand = 0; grand < grandchildren.size(); grand++) // For each grand child
                {
                    query_non_arg(grandchildren.get(grand));						// Add its instructions
                }
            }
            for (int child = 0; child < children.size(); child++) // For each child (argument)
            {
                argument_instruction(children.get(child));							// Add its argument instructions
            }
            add_call(part);																// Finish part instructions with a call
        }
    }

    private void query_non_arg(int index) {
        int tag = WAM.cell_tag(heap[index]);
        if (tag == WAM.LIS) {																// For list we can add the children immediately
            int start = Compiler.getArgStart(heap, index);
            query_non_arg(start);
            query_non_arg(start + 1);
            argument_instruction(index);												// Add the argument type instruction
        } else if (tag == WAM.STR) {														// For structures we need to obtain the children
            IntArrayList children = new IntArrayList();
            Compiler.addChildren(heap, index, children);
            for (int child = 0; child < children.size(); child++) {
                query_non_arg(children.get(child)); 								// Add children's instructions
            }
            argument_instruction(index); 												// Add argument type instruction
        }
    }

    private void add_call(int part) {
        Token t = tokens[part];
        int tag = WAM.cell_tag(heap[part]);
        int fn = tag == WAM.CON ? (WAM.cell_tag(heap[part]) << 7) : (WAM.cell_value(heap[WAM.cell_value(heap[part])]));
        if (tag == WAM.REF) {											// We can call variables
            if (t.is_perm) {																// Permanent variable is called
                instructions.add(WAM.make_instruction(WAM.CALL_VAR_PERM, t.perm_vars_afterwards));
                instructions.add(t.register1 == 0 ? t.register2 : t.register1);
            } else {																	// Temporary variable is called
                instructions.add(WAM.make_instruction(WAM.CALL_VAR, t.perm_vars_afterwards));
                instructions.add(registerstart + (t.register1 == 0 ? t.register2 : t.register1));
            }
        } else if (tag == WAM.STR && built_ins.contains(fn)) {						// VM built ins, such as '<' and '>' 
            if (strings.get(fn).equals("retract/1")) { 		// TODO: Added a special retract instruction. It is not very nice to put it here but it works.
                instructions.add(WAM.make_instruction(WAM.RETRACT, t.perm_vars_afterwards));
            } else {
                instructions.add(WAM.make_instruction(WAM.BUILT_IN_BINARY, t.perm_vars_afterwards));
                int cell = heap[WAM.cell_value(heap[part])];
                instructions.add(WAM.cell_value(cell));
            }
        } else {																		// Standard call for predicate (possibly 0-ary)
            instructions.add(WAM.make_instruction(WAM.CALL, t.perm_vars_afterwards));
            int cell = heap[part];
            if (WAM.cell_tag(cell) == WAM.STR) {
                instructions.add(WAM.cell_value(heap[WAM.cell_value(cell)]));
            } else {
                instructions.add(WAM.cell_value(cell) << 7);
            }
        }
    }

    // Long method but that's due to six different tag types.
    private void argument_instruction(int index) {
        Token t = tokens[index];
        int type = WAM.cell_tag(heap[index]);
        if (type == WAM.LIS) { // List puts or gets the LIS type and then has two consecutive arguments
            IntArrayList children = new IntArrayList();
            Compiler.addChildren(heap, index, children);
            instructions.add(WAM.make_instruction(q ? WAM.PUT_LIST : WAM.GET_LIST, registerstart + t.register2));
            if (!enc_temp.contains(t.register2)) {
                enc_temp.add(t.register2);
            }
            register1_instruction(children.get(0));
            register1_instruction(children.get(1));
        } else if (type == WAM.STR) { // STR looks like LIS but has a functor/arg combi plus it has 1 or more arguments
            instructions.add(WAM.make_instruction(q ? WAM.PUT_STR : WAM.GET_STR, registerstart + t.register2));
            int cell = heap[WAM.cell_value(heap[index])];
            instructions.add(WAM.cell_value(cell));
            if (!enc_temp.contains(t.register2)) {
                enc_temp.add(t.register2);
            }
            IntArrayList children = new IntArrayList();
            Compiler.addChildren(heap, index, children);
            for (int child = 0; child < children.size(); child++) {
                register1_instruction(children.get(child));
            }
        } else if (type == WAM.CON) { // CON's are a form of optimization in replacement of STR's with 0 arguments. 
            instructions.add(WAM.make_instruction(q ? WAM.PUT_CONSTANT : WAM.GET_CONSTANT, WAM.cell_value(heap[index])));
            instructions.add(registerstart + t.register2);
            if (!enc_temp.contains(t.register2)) {
                enc_temp.add(t.register2);
            }
        } else if (type == WAM.NUM) { // NUM's are a form of optimization in replacement of CON's, you do not want to parse strings all the time for numbers.
            instructions.add(WAM.make_instruction(q ? WAM.PUT_NUM : WAM.GET_NUM, WAM.cell_value(heap[index])));
            instructions.add(registerstart + t.register2);
            if (!enc_temp.contains(t.register2)) {
                enc_temp.add(t.register2);
            }
        } else if (type == WAM.REF && WAM.cell_value(heap[index]) != 0) { // Non-anonymous variables
            if (t.is_perm) {
                if (enc_perm.contains(t.register1)) { // Not the first time you encountered this register
                    int i = 0;
                    if (q && t.is_unsafe && t.part_of_last_goal && !sec_enc_perm.contains(t.register1) && !explicitly_initialized.contains(t.register1)) {
                        sec_enc_perm.add(t.register1); // Second time you encounter this register
                        explicitly_initialized.add(t.register1);
                        i = WAM.PUT_UNSAFE_VALUE;
                    } else {
                        i = q ? WAM.PUT_VAL_PERM : WAM.GET_VAL_PERM;
                    }
                    instructions.add(WAM.make_instruction(i, t.register1));
                } else { // The first time you encountered this register 
                    enc_perm.add(t.register1);
                    instructions.add(WAM.make_instruction(q ? WAM.PUT_VAR_PERM : WAM.GET_VAR_PERM, t.register1));
                }
            } else {
                if (enc_temp.contains(t.register1)) { // Not the first time you encountered this register
                    instructions.add(WAM.make_instruction(q ? WAM.PUT_VAL : WAM.GET_VAL, registerstart + t.register1));
                } else { // The first time you encountered this register
                    enc_temp.add(t.register1);
                    instructions.add(WAM.make_instruction(q ? WAM.PUT_VAR : WAM.GET_VAR, registerstart + t.register1));
                    if (q) {
                        explicitly_initialized.add(-t.register1); // if q, then this temporary variable (hence the -1) is explicitly initialized (needed for detecting set_local_value and unify_local_value)
                    }
                }
            }
            instructions.add(registerstart + t.register2); // REF's in arguments have two registers
        } else if (q && type == WAM.REF && WAM.cell_value(heap[index]) == 0) {
            int last_instr = 0;
            if (instructions.size() > 1) // There is a possible put_void instruction before you
            {
                last_instr = WAM.instruction_code(instructions.get(instructions.size() - 2)); // Put voids can be done in sequence: p :- p(_,_,_). => put_void A1 3; call p/3;
            }
            int arg = registerstart + t.register2;
            int n = 1;
            if (last_instr == WAM.PUT_VOID) {
                n = instructions.remove(instructions.size() - 1) + 1;
                arg = WAM.instruction_arg(instructions.remove(instructions.size() - 1)); // So if the last instruction was also put void, then add this one to it by taking the argument to start and the amount of variables to put
            }
            instructions.add(WAM.make_instruction(WAM.PUT_VOID, arg)); // By replacing it with the same argument + 1
            instructions.add(n);
        }
    }

    private void register1_instruction(int index) {
        Token t = tokens[index];
        int type = WAM.cell_tag(heap[index]);
        if (type == WAM.REF && t.is_perm) { // Permanent variables are handled differently 
            if (!enc_perm.contains(t.register2)) { // The first time you encountered this register
                enc_perm.add(t.register2);
                instructions.add(WAM.make_instruction(q ? WAM.SET_VAR_PERM : WAM.UNI_VAR_PERM, t.register2));
                explicitly_initialized.add(t.register2); // This permanent variable is now explicitly initialized
            } else {
                if (explicitly_initialized.contains(t.register2)) {
                    instructions.add(WAM.make_instruction(q ? WAM.SET_VAL_PERM : WAM.UNI_VAL_PERM, t.register2)); // Not the first time you encountered this register 
                } else {
                    instructions.add(WAM.make_instruction(q ? WAM.SET_LOCAL_VAL_PERM : WAM.UNI_LOCAL_VAL_PERM, t.register2));
                    explicitly_initialized.add(t.register2);
                }
            }
        } else if (type == WAM.CON) { // Constants have special instructions 
            instructions.add(WAM.make_instruction(q ? WAM.SET_CONSTANT : WAM.UNIFY_CONSTANT, WAM.cell_value(heap[index]))); // Constants do not have a register2, instead they immediately use their name
        } else if (type == WAM.NUM) { // Constants have special instructions
            instructions.add(WAM.make_instruction(q ? WAM.SET_NUM : WAM.UNI_NUM, WAM.cell_value(heap[index]))); // Numbers do not have a register2, instead they immediately use their name
        } else if (type == WAM.REF && WAM.cell_value(heap[index]) == 0) {
            int last_instr = WAM.instruction_code(instructions.get(instructions.size() - 1)); // Set and unify voids can be done in sequence
            int arg = 1;
            if (last_instr == (q ? WAM.SET_VOID : WAM.UNIFY_VOID)) {
                arg = WAM.instruction_arg(instructions.remove(instructions.size() - 1)) + 1; // So if the last instruction was also set/unify void, then add this one to it
            }
            instructions.add(WAM.make_instruction((q ? WAM.SET_VOID : WAM.UNIFY_VOID), arg)); // By replacing it with the same argument + 1
        } else { // All other types (LIS, STR, temp REF) are handled equally
            if (enc_temp.contains(t.register2)) { // Later occurrence
                if (explicitly_initialized.contains(-t.register2)) {
                    instructions.add(WAM.make_instruction(q ? WAM.SET_VAL : WAM.UNI_VAL, registerstart + t.register2));
                } else {
                    explicitly_initialized.add(-t.register2);
                    instructions.add(WAM.make_instruction(q ? WAM.SET_LOCAL_VAL : WAM.UNI_LOCAL_VAL, registerstart + t.register2));
                }
            } else { // First occurrence
                enc_temp.add(t.register2);
                instructions.add(WAM.make_instruction(q ? WAM.SET_VAR : WAM.UNI_VAR, registerstart + t.register2));
                explicitly_initialized.add(-t.register2); // This temporary variable (hence the -) is now explicitly initialized
            }
        }

    }

    private void program_instructions(int part) {
        IntArrayList round = new IntArrayList();
        IntArrayList next = new IntArrayList();
        IntArrayList children = new IntArrayList();
        Compiler.addChildren(heap, part, children);
        for (int child = 0; child < children.size(); child++) { // First add the arguments with a special call (for REF's and CON's)
            argument_instruction(children.get(child));
            Compiler.addChildren(heap, children.get(child), round);
        }
        while (!round.isEmpty()) { // BF through nodes
            next.clear();
            for (int desc = 0; desc < round.size(); desc++) {
                Token t = tokens[round.get(desc)];
                int tag = WAM.cell_tag(heap[t.index]);
                if (tag == WAM.LIS || tag == WAM.STR) // Only LIS and STR trees now add instructions
                {
                    argument_instruction(t.index);
                }
                Compiler.addChildren(heap, t.index, next);
            }
            round.clear(); // BF overhead
            round.addAll(next);
        }
    }

    private void fix_clause_end(int nr_parts, boolean isQuery) {
        if (isQuery) {
            int instr = WAM.instruction_arg(instructions.get(instructions.size() - 2));
            if (instr > 0) {
                instructions.add(0, WAM.make_instruction(WAM.ALLOCATE, 0));
            }
            instructions.add(WAM.make_instruction(WAM.END_OF_QUERY, 0));
        } else if (!isQuery && nr_parts > 1) {
            int arg = instructions.get(instructions.size() - 1);
            int instr = WAM.instruction_code(instructions.get(instructions.size() - 2));
            if (nr_parts > 2) {
                instructions.add(0, WAM.make_instruction(WAM.ALLOCATE, 0));
            }
            if (instr == WAM.CALL) {
                instructions.remove(instructions.size() - 1);
                instructions.remove(instructions.size() - 1);
                if (nr_parts > 2) {
                    instructions.add(WAM.make_instruction(WAM.DEALLOCATE, 0));
                }
                instructions.add(WAM.make_instruction(WAM.EXECUTE, 0));// if ends with call => deallocate execute
                instructions.add(arg);
            } else if (instr == WAM.CALL_VAR) {
                instructions.remove(instructions.size() - 1);
                instructions.remove(instructions.size() - 1);
                if (nr_parts > 2) {
                    instructions.add(WAM.make_instruction(WAM.DEALLOCATE, 0));
                }
                instructions.add(WAM.make_instruction(WAM.EXECUTE_VAR, arg));
            } else if (instr == WAM.CALL_VAR_PERM) {
                if (nr_parts > 2) {
                    instructions.add(WAM.make_instruction(WAM.DEALLOCATE, 0));
                }
                instructions.add(WAM.make_instruction(WAM.PROCEED, 0));
            } else if (instr == WAM.BUILT_IN_BINARY || instr == WAM.RETRACT) {
                instructions.add(WAM.make_instruction(WAM.PROCEED, 0));
            } else if (nr_parts > 2) {
                instructions.add(WAM.make_instruction(WAM.DEALLOCATE, 0));
            }
        } else {
            instructions.add(WAM.make_instruction(WAM.PROCEED, 0));
        }
    }

    private void outputResult() {
        System.out.println("/////////// INSTRUCTIONS: ");
        int[] program = new int[instructions.size()];
        for (int i = 0; i < program.length; i++) {
            program[i] = instructions.get(i);
        }
        System.out.println(WAMToString.programToString(program, 0, instructions.size(), registerstart, strings, nums));
    }
}
