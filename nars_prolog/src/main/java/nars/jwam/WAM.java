package nars.jwam;

import nars.jwam.compiler.Compiler;
import nars.jwam.datastructures.IntHashMap;
import nars.jwam.datastructures.Numbers;
import nars.jwam.datastructures.Strings;

import java.util.ArrayList;

public class WAM {

    public static WAM newMedium() {
        //TODO these may not be valid VM parameters; for now, just use Small
        //return new WAM(8192, 80, 2000, 200, 100, 6);        
        return newSmall();
    }
    public static WAM newSmall() {
        return new WAM(5000, 40, 1000, 100, 50, 3);        
    }
    
    
    public static final int STR = 1, REF = 2, PN = 3, CON = 4, LIS = 5, NUM = 6, // Heap types
            PUT_STR = 1, SET_VAR = 2, SET_VAL = 3, GET_STR = 4, UNI_VAR = 5, UNI_VAL = 6, CALL = 7, PROCEED = 8, // All the instructions
            PUT_VAR = 9, PUT_VAL = 10, GET_VAR = 11, GET_VAL = 12, ALLOCATE = 13, DEALLOCATE = 14,
            PUT_VAR_PERM = 15, PUT_VAL_PERM = 16, GET_VAR_PERM = 17, TRY_ME_ELSE = 18, RETRY_ME_ELSE = 19,
            TRUST_ME = 20, PUT_CONSTANT = 21, GET_CONSTANT = 22, SET_CONSTANT = 23, UNIFY_CONSTANT = 24,
            PUT_LIST = 25, GET_LIST = 26, SET_VOID = 27, UNIFY_VOID = 28, END_OF_QUERY = 29,
            SET_VAR_PERM = 30, SET_VAL_PERM = 31, UNI_VAR_PERM = 32, UNI_VAL_PERM = 33, GET_VAL_PERM = 34,
            PUT_VOID = 35, EXECUTE = 36, PUT_UNSAFE_VALUE = 37, UNI_LOCAL_VAL = 38, SET_LOCAL_VAL = 39,
            UNI_LOCAL_VAL_PERM = 40, SET_LOCAL_VAL_PERM = 41, TRY = 42, RETRY = 43, TRUST = 44,
            SWITCH_CON = 45, SWITCH_STR = 46, SWITCH_TERM = 47, DEEP_CUT = 48, NECK_CUT = 49, GET_LEVEL = 50,
            CALL_VAR = 51, CALL_VAR_PERM = 52, EXECUTE_VAR = 53, PUT_NUM = 54, GET_NUM = 55, SET_NUM = 56,
            UNI_NUM = 57, SWITCH_NUM = 58, BUILT_IN_BINARY = 59, D_TRY = 60, D_RETRY = 61, D_TRUST = 62, JUMP = 63,
            JUMPLIST = 64, DYNAMIC_CODE_END = 65, FAIL = 66, RETRACT = 67;

    


    
    private int heap_size;
    private int register_size, unify_pdl_size, trail_size, stack_size, // Memory sizes
            h, s, p, cp, e, b, b0, hb, tr, num_of_args, A1, cca, ca, // Execution globals 
            untilCA = 0, untilP = 0;												// For debugging
    private Strings strings = null; 				 	  					// Management of strings
    private Numbers numbers = null; 				 	   					// Management of doubles
    
    // Code area's
    private ArrayList<int[]> areas = new ArrayList<>();
    // Main data, unification stack and current code area
    private int[] storage, unify_pdl, code;				   					
    
    // Globals for execution and debugging
    private boolean read = true, 
            fail, 
            trace, 
            untilCall, 
            untilPoint; 					
    
    public static int[] call(int x, int y) { return new int[] { x, y }; }
    
    // The call function
    private IntHashMap<int[]> call = null;						   			
    
    // Built in functions, predicates, etc
    private Library library = null;					   					
    
// Can compile Prolog to heaps or WAM instructions
    private Compiler compiler = null;												
    
    // Can edit the rule base
    private RuleHeap rules = null;			
    
    /** execute without time limit */
    public boolean execute() {
        return execute(0);
    }
    
    /** Execute instructions. Get the instruction at p and execute it. Instruction methods update p so this method loops until either
     the end of the query is reached (in which case we have success) or if failure occurs.
     * 
     * time is in seconds, if == 0 then no limit
     * 
     * TODO implement timeout with periodic ms or ns check (not every cycle)
     * 
     */
    public boolean execute(double time) {
        int instruction = instruction_code(code[p]);  							// Get the initial instruction code
        while (instruction != END_OF_QUERY) {  											// While not at the end of the query 
            int argument = instruction_arg(code[p]);  							// Get the instruction's direct argument 
            switch (instruction) {													// Look up the correct method
                case GET_STR:
                    get_structure(argument, code[p + 1]);
                    break;
                case PUT_STR:
                    put_structure(argument, code[p + 1]);
                    break;
                case SET_VAR:
                    set_variable(argument);
                    break;
                case SET_VAL:
                    set_value(argument);
                    break;
                case UNI_VAR:
                    unify_variable(argument);
                    break;
                case UNI_VAL:
                    unify_value(argument);
                    break;
                case SET_VAR_PERM:
                    set_variable(e + 2 + argument);
                    break; 					// Most perm instructions only edit the argument
                case SET_VAL_PERM:
                    set_value(e + 2 + argument);
                    break;
                case UNI_VAR_PERM:
                    unify_variable(e + 2 + argument);
                    break;
                case UNI_VAL_PERM:
                    unify_value(e + 2 + argument);
                    break;
                case SET_LOCAL_VAL:
                    set_local_value(argument);
                    break;
                case UNI_LOCAL_VAL:
                    unify_local_value(argument);
                    break;
                case SET_LOCAL_VAL_PERM:
                    set_local_value(e + 2 + argument);
                    break;
                case UNI_LOCAL_VAL_PERM:
                    unify_local_value(e + 2 + argument);
                    break;
                case CALL:
                    call(code[p + 1], true, Integer.MIN_VALUE);
                    break;
                case CALL_VAR:
                    variable_as_goal(code[p + 1], true);
                    break;
                case CALL_VAR_PERM:
                    variable_as_goal(e + 2 + code[p + 1], true);
                    break;
                case EXECUTE:
                    call(code[p + 1], false, Integer.MIN_VALUE);
                    break;
                case EXECUTE_VAR:
                    variable_as_goal(argument, false);
                    break;
                case PROCEED:
                    proceed();
                    break;
                case PUT_VAR:
                    put_variable(argument, code[p + 1]);
                    break;
                case PUT_VAL:
                    put_value(argument, code[p + 1]);
                    break;
                case GET_VAR:
                    get_variable(argument, code[p + 1]);
                    break;
                case GET_VAL:
                    get_value(argument, code[p + 1]);
                    break;
                case DEALLOCATE:
                    deallocate();
                    break;
                case ALLOCATE:
                    allocate();
                    break;
                case PUT_VAR_PERM:
                    put_variable_perm(argument, code[p + 1]);
                    break;
                case PUT_VAL_PERM:
                    put_value(e + 2 + argument, code[p + 1]);
                    break;
                case PUT_UNSAFE_VALUE:
                    put_unsafe_value(argument, code[p + 1]);
                    break;
                case GET_VAR_PERM:
                    get_variable(e + 2 + argument, code[p + 1]);
                    break;
                case GET_VAL_PERM:
                    get_value(e + 2 + argument, code[p + 1]);
                    break;
                case TRY_ME_ELSE:
                    try_me_else(argument);
                    break;
                case RETRY_ME_ELSE:
                    retry_me_else(argument);
                    break;
                case TRUST_ME:
                    trust_me();
                    break;
                case TRY:
                    try_(argument);
                    break;
                case RETRY:
                    retry(argument);
                    break;
                case TRUST:
                    trust(argument);
                    break;
                case D_TRY:
                    d_try(argument, code[p + 1]);
                    break;
                case D_RETRY:
                    d_retry(argument, code[p + 1]);
                    break;
                case D_TRUST:
                    trust(argument);
                    break;									// d_trust and trust are equal
                case SWITCH_CON:
                    switch_on_constant(argument);
                    break;
                case SWITCH_STR:
                    switch_on_structure(argument);
                    break;
                case SWITCH_NUM:
                    switch_on_num(argument);
                    break;
                case SWITCH_TERM:
                    switch_on_term(argument, code[p + 1], code[p + 2], code[p + 3], code[p + 4]);
                    break;
                case PUT_CONSTANT:
                    put_constant(argument, code[p + 1]);
                    break;
                case GET_CONSTANT:
                    get_constant(argument, code[p + 1]);
                    break;
                case SET_CONSTANT:
                    set_constant(argument);
                    break;
                case UNIFY_CONSTANT:
                    unify_constant(argument);
                    break;
                case PUT_NUM:
                    put_num(argument, code[p + 1]);
                    break;
                case GET_NUM:
                    get_num(argument, code[p + 1]);
                    break;
                case SET_NUM:
                    set_num(argument);
                    break;
                case UNI_NUM:
                    unify_num(argument);
                    break;
                case PUT_LIST:
                    put_list(argument);
                    break;
                case GET_LIST:
                    get_list(argument);
                    break;
                case SET_VOID:
                    set_void(argument);
                    break;
                case UNIFY_VOID:
                    unify_void(argument);
                    break;
                case PUT_VOID:
                    put_void(argument, code[p + 1]);
                    break;
                case GET_LEVEL:
                    get_level(argument);
                    break;
                case DEEP_CUT:
                    cut(argument);
                    break;
                case NECK_CUT:
                    neck_cut();
                    break;
                case JUMP:
                    p += argument;
                    break;
                case JUMPLIST:
                    jumplist(argument);
                    break;
                case FAIL:
                    backtrack();
                    break;
                case RETRACT:
                    retract();
                    break;
                case BUILT_IN_BINARY:
                    if (!library.call(code[p + 1])) {
                        backtrack();
                    } else {
                        p += 2;
                    }
                    break;
            }
            if (fail) {
                return false; 													// Break upon failure
            }
            instruction = instruction_code(code[p]);  							// Get next instruction
            if (trace) {
                if ((untilCall && (instruction == CALL || instruction == CALL_VAR || instruction == CALL_VAR_PERM || instruction == EXECUTE || instruction == EXECUTE_VAR)) || (untilPoint && ca == untilCA && p == untilP)) {
                    break;
                }
            }
        }
        return true;
    }
    ////////////////////////////
    ///// PUT INSTRUCTIONS /////
    ////////////////////////////
    //// Put instructions extend the heap by adding constructions, or move around data between registers. 
    //// Putting is generally done by queries.

    // Put a functor cell on the heap, and make a structure cell that points towards.
    private void put_structure(int register, int functor) {
        storage[h] = newCell(PN, functor);	// Make the functor cell, which also holds the arity
        storage[register] = newCell(STR, h);	// Make at the given address (normally a register) a structure cell
        h++;									// Heap is increased by one
        p += 2;									// Register is the direct argument and the functor takes another integer
    }

    // Put a new, and thus unbound, variable (REF cell) on the heap and let the temporary and argument register point to it.
    private void put_variable(int Xn, int Ai) {
        storage[h] = newCell(REF, h); 		// Make the cell
        storage[Xn] = storage[h];				// Temporary register has to point to it
        storage[Ai] = storage[h];				// Also the argument register
        h++;									// Takes one cell integer to make the variable
        p += 2;									// Takes two integers, Xn as direct argument and Ai is another int
    }

    // Put the value of the temporary register Xn into the argument register Ai.
    private void put_value(int Xn, int Ai) {
        storage[Ai] = storage[Xn];				// Copy the cell of temporary register Xn into the argument register Ai
        p += 2;									// Takes two integers, Xn as direct argument and Ai is another int
    }

    // Create a permanent variable in the environment stack and make a argument register point to it.
    private void put_variable_perm(int Yn, int Ai) {
        int addr = e + 2 + Yn;						// Get the environment stack address
        storage[addr] = newCell(REF, addr);   // Create the REF cell
        storage[Ai] = storage[addr];		    // Make argument register point to it
        p += 2;									// Takes two integers, Yn as direct argument and Ai is another int
    }

    // If the address of the permanent variable Yn is older than the current environment, then it is safe to make the argument register
    // Ai point towards it. Otherwise, due to discarding environments with last call optimization, make a new variable on the heap,
    // and bind the permanent and argument variables to it. 
    private void put_unsafe_value(int Yn, int Ai) {
        int addr = deref(e + 2 + Yn);
        if (addr < e) {
            storage[Ai] = storage[addr]; // Permanent value will not be disposed while this argument is used
        } else { 									// Permanent value can be overwritten by a deallocate/allocate combination
            storage[h] = newCell(REF, h); 	// Make unbound variable
            bind(addr, h);						// Bind the permanent value to the unbound variable
            storage[Ai] = storage[h];			// The argument can now safely point towards this variable on the heap
            h++;								// Heap is increased by one
        }
        p += 2;									// Yn is incorporated in instruction integer, for Ai an extra integer is required
    }

    // Create a number cell in a register. Numbers take 1 integer at all times so no need to put them on the heap if they are assigned
    // to a register.
    private void put_num(int n, int register) {
        storage[register] = newCell(NUM, n);	// Make the number cell in the register
        p += 2;									// One integer for the number reference, one integer for the argument register
    }

    // Similar to put_num but in this case for constants.
    private void put_constant(int c, int register) {
        storage[register] = newCell(CON, c);	 // Make the constant cell in the register
        p += 2;									 // One integer for the number reference, one integer for the argument register
    }

    // Make a LIS cell for starting a list tree an make a register point to it.
    private void put_list(int register) {
        storage[register] = newCell(LIS, h);	// Create the LIS cell to which the register points
        p++; 									// Register is incorporated in the instruction integer
    }

    // Put n unbound variables on the heap and let arguments Ai to A(i+n) point towards them.
    private void put_void(int Ai, int n) {
        for (int i = h; i < h + n; i++) {			// Start at h and end at h+n
            storage[i] = newCell(REF, i);		// Make the unbound variable
            storage[Ai + (i - h)] = storage[i];		// Let an argument register point towards it
        }
        h += n;									// n variables takes n cells
        p += 2;									// Register is direct argument, amount of variables takes another integer
    }

    ////////////////////////////
    ///// GET INSTRUCTIONS /////
    ////////////////////////////
    //// Get instructions generally match an register's content with another address or construction. Get instructions are typically
    //// used by compiled program terms. Get instructions switch the WAM to write mode if the matching meets a unbound variable.
    // See if a functor is present in a register. If the register is variable, then create a functor and structure cell and continue by
    // writing on the heap. Otherwise Start matching the arguments.
    private void get_structure(int register, int functor) {
        int addr = deref(register);									// Go to the end reference of the address
        int tag = cell_tag(storage[addr]);							// Get the cell's tag
        boolean fail = false;										// Init the failure boolean
        if (tag == REF) {												// If the register is a unbound variable
            storage[h] = newCell(STR, h + 1);						// Then make the structure and functor cell
            storage[h + 1] = newCell(PN, functor);
            bind(addr, h);											// Bind the register to the structure cell
            h += 2; 												// Created two cells
            s = h;													// Set the unify pointer to the first argument
            read = false;											// Start writing on the heap instead of reading of the heap
        } else if (tag == STR) {										// If the register is also a structure
            int a = cell_value(storage[addr]);						// Then get the address of its functor cell
            if (cell_value(storage[a]) == functor) {					// Its functor should match
                s = a + 1;											// Set the unify pointer to the first argument
                read = true;										// And continue with reading of the heap
            } else {
                fail = true;										// Otherwise they do not match
            }
        } else {
            fail = true;
        }
        if (fail) {
            backtrack();										// Upon failure: backtrack
        } else {
            p += 2;													// Upon success: move onwards, get_structures uses two integers
        }
    }

    // Opposite of put value: get the value of argument register Ai and put it in the temporary register Xn.
    private void get_variable(int Xn, int Ai) {
        storage[Xn] = storage[Ai];									// Put Ai's value inside Xn
        p += 2;														// Takes two integers, Xn as direct argument and Ai is another int
    }

    // Get the values of temporary register Xn and argument register Ai and try to unify them. Will backtrack upon failure.
    private void get_value(int Xn, int Ai) {
        if (unify(Xn, Ai)) {
            p += 2;										// If they unify, move two integers forward in the code area
        } else {
            backtrack();											// Otherwise backtrack to previous choice point
        }
    }

    // Get the registers value and match it with constant c. Will bind the register if it refers to an unbound variable.
    private void get_constant(int c, int register) {
        int addr = deref(register);									// Go to the bottom of the reference chain
        int tag = cell_tag(storage[addr]);							// Get the cell tag
        boolean fail = false;
        if (tag == REF) {												// If the bottom contains an unbound variable: bind it
            storage[addr] = newCell(CON, c);  					// Create constant cell
            trail(addr); 											// Might be made undone at some later point
        } else if (tag == CON) {
            fail = c != cell_value(storage[addr]);	// If bottom contains a constant, compare it with c
        } else {
            fail = true;
        }
        if (fail) {
            backtrack();										// Upon failure: backtrack
        } else {
            p += 2;													// Instruction has size two, one for c and one for the register
        }
    }

    // Same as get constant but then for numbers. Try to match or bind a register with a number.
    private void get_num(int n, int register) {
        int addr = deref(register);									// Go to the bottom of the reference chain
        int tag = cell_tag(storage[addr]);							// Get the cell tag
        boolean fail = false;
        if (tag == REF) {												// If the bottom contains an unbound variable: bind it
            storage[addr] = newCell(NUM, n);  					// Create number cell
            trail(addr); 											// Might be made undone at some later point
        } else if (tag == NUM) {										// If bottom contains a number, compare it with n
            fail = !num_equality(n, cell_value(storage[addr])); 		// Call number equality method for this
        } else {
            fail = true;
        }
        if (fail) {
            backtrack();										// Upon failure: backtrack
        } else {
            p += 2;													// Instruction has size two, one for n and one for the register
        }
    }

    // Similar to get structure, but lists always have two arguments and need no functor comparison. If the register refers to an
    // unbound variable, then it will be bound to a newly placed list cell. Otherwise matching occurs.
    private void get_list(int register) {
        int addr = deref(register);									// Go to the bottom of the reference chain
        int tag = cell_tag(storage[addr]);							// Get the cell's tag number
        if (tag == REF) {												// If the register is an unbound variable
            storage[h] = newCell(LIS, h + 1);						// Create list cell
            bind(addr, h);											// Bind the register
            h++;													// Update the top-of-heap pointer
            read = false;											// Write the next elements
            p++;													// Instruction size is one
        } else if (tag == LIS) {										// Register refers to a list cell
            s = cell_value(storage[addr]);							// Set the unification pointer to the first argument
            read = true;											// Start unifying rather than writing
            p++;													// Instruction size is one
        } else {
            backtrack();											// Backtrack to the previous choice point upon failure
        }
    }

    ////////////////////////////
    ///// SET INSTRUCTIONS /////
    ////////////////////////////
    //// Set instructions are used to put arguments of structures and lists on the heap. If these arguments are lists or structures,
    //// then they will have a temporary register assigned to them. Thus set instructions only exist for constants, numbers, and 
    //// register copying. Anonymous variables have a specialized instruction (set void) as a way of optimization.
    // Create a new unbound variable on the heap and let a register point to it.
    private void set_variable(int register) {
        storage[h] = newCell(REF, h);								// Create unbound variable
        storage[register] = storage[h];								// Register points to it
        h++;														// Update top-of-heap and next-instruction pointers
        p++;
    }

    // Set the value of a register on the heap.
    private void set_value(int register) {
        storage[h] = storage[register];								// Move the data
        h++;														// Update top-of-heap and next-instruction pointers
        p++;
    }

    // Set a constant on the heap.
    private void set_constant(int c) {
        storage[h] = newCell(CON, c);								// Create constant cell
        h++;														// Update top-of-heap and next-instruction pointers
        p++;
    }

    // Set a number on the heap.
    private void set_num(int n) {
        storage[h] = newCell(NUM, n);								// Create number cell
        h++;														// Update top-of-heap and next-instruction pointers
        p++;
    }

    private void set_void(int n) {
        for (int i = h; i < h + n; i++) // Start at h and make variables until h+n
        {
            storage[i] = newCell(REF, i);							// Create unbound variable
        }
        h += n;														// Update top-of-heap and next-instruction pointers
        p++;
    }

    // This specialized instruction takes away the risk of pointing towards a permanent variable that can be cleared away
    // by last call optimization. If an reference is on the heap then act as set value, otherwise create a new variable on the heap.
    private void set_local_value(int Vi) {
        int addr = deref(Vi);										// Go to the bottom of the reference chain
        if (addr < h) {
            storage[h] = storage[addr];						// If the address is already on the heap: set its data
        } else {
            storage[h] = newCell(REF, h);							// Otherwise make new variable
            bind(addr, h);											// And bind it to the referred address
        }
        h++;														// Update top-of-heap and next-instruction pointers
        p++;
    }

    //////////////////////////////
    ///// UNIFY INSTRUCTIONS /////
    //////////////////////////////
    //// Unify instructions are used to unify the arguments of structures and lists with those on the heap. If the WAM is in write mode
    //// then the unify instructions will act as their set counterparts, building new constructions on the heap to which variables can
    //// point.
    // In read mode: move the storage value (can be heap or register) into the register. Otherwise call the set variable counterpart.
    private void unify_variable(int register) {
        if (read) {
            storage[register] = storage[s]; 						// Move the storage value into the register
            p++; 													// Instruction size is one
        } else {
            set_variable(register);								// Otherwise call set variable counterpart
        }
        s++; 														// Move unification pointer one forward to the next argument
    }

    // In read mode the register should unify with the unification pointer. Otherwise the set value counterpart is called upon.
    private void unify_value(int register) {
        if (read) {
            if (!unify(register, s)) {									// If the register and unification pointer do not unify
                s++;												// ?? Don't think this is needed...
                backtrack();										// Then backtrack
                return; 											// Break the method
            }
            p++;													// Otherwise move on
        } else {
            set_value(register);									// In write mode call the set value counterpart
        }
        s++; 														// Move unification pointer one forwards
    }

    // Unify the unification pointer s with constant c. If s points to an unbound variable then it will bind it. In write mode a new
    // constant cell is created on the heap.
    private void unify_constant(int c) {
        if (read) {
            boolean fail = false;
            int addr = deref(s);									// Get the address that is referred to
            int tag = cell_tag(storage[addr]);						// Get its tag
            if (tag == REF) {											// If s points to an unbound variable
                storage[addr] = newCell(CON, c);					// Bind it with a constant cell for c
                trail(addr);										// Might be made undone
            } else if (tag == CON) // If s points to a constant
            {
                fail = c != cell_value(storage[addr]);				// Compare it with c
            } else {
                fail = true;
            }
            if (fail) {
                backtrack();									// Backtrack upon failure
            } else {
                s++;												// Move onwards upon success
                p++;
            }
        } else {													// In write mode just put an new constant cell on the heap
            storage[h] = newCell(CON, c);
            h++;													// Update top-of-heap and instruction pointers
            p++;
        }
    }

    // Same as unify constant but then for numbers.
    private void unify_num(int n) {
        if (read) {
            boolean fail = false;
            int addr = deref(s);									// Get the address that is referred to
            int tag = cell_tag(storage[addr]);						// Get its tag
            if (tag == REF) {											// If s points to an unbound variable
                storage[addr] = newCell(NUM, n);					// Bind it with a number cell for n
                trail(addr);										// Might be made undone
            } else if (tag == NUM) // If s points to a number 
            {
                fail = !num_equality(n, cell_value(storage[addr]));	// Check number equality
            } else {
                fail = true;
            }
            if (fail) {
                backtrack();									// Backtrack upon failure
            } else {
                s++;												// Otherwise move onwards
                p++;
            }
        } else {
            storage[h] = newCell(NUM, n);							// In read mode just put a new number cell on the heap
            h++;
            p++;
        }
    }

    // In read mode we do not need to unify anonymous variables. Their value will never be used anyway. In write mode we can put a 
    // sequence of unbound variables on the heap.
    private void unify_void(int n) {
        if (read) {
            s += n;												// Simply move the unification pointer n forwards
        } else {
            for (int i = h; i < h + n; i++) // Create n new unbound variables on the heap
            {
                storage[i] = newCell(REF, i);
            }
            h += n;													// Update the top-of-heap pointer
        }
        p++;														// Go to next instruction
    }

    // In read mode check if the unification pointer and the (permanent) register unify. Otherwise if Vi does not refer to a 
    // permanent variable address then copy the value of Vi on the heap. Or if Vi does point to a unbound variable on the environment
    // stack, then create a new variable and Vi to it.
    private void unify_local_value(int Vi) {
        if (read) { 													// In read mode:
            if (!unify(Vi, s)) {										// See if Vi and s unify			
                s++;												// ?? needed?
                backtrack();										// If they do not, then backtrack
                return;
            }
        } else {													// In write mode:
            int addr = deref(Vi);									// Get the dereferenced address
            if (addr < h) {
                storage[h] = storage[addr];					// If the address is on the heap, then copy the value to the top
            } else {
                storage[h] = newCell(REF, h);						// Otherwise create a new unbound variable
                bind(addr, h);										// And make Vi's dereferenced value point to it
            }
            h++;													// Either way update the top of the heap pointer
        }
        s++;  														// Move to next argument and instruction
        p++;
    }

    ////////////////////////////////
    ///// CONTROL INSTRUCTIONS /////
    ////////////////////////////////
    //// Control instructions modify the current code area and instruction pointer. We use them to jump to the instructions of a 
    //// specific predicate. Call instructions are meant for in-between goals and execute is for the last goal. Allocate and deallocate
    //// instructions create environments on the environment stack to store permanent variables.
    // Get the code point of a functor, and update the computational state of the WAM. The call instruction should call this method
    // with the arguments functor, true and Integer.MIN_VALUE. The execute instruction uses false as second argument. When a variable
    // is called/executed, then the dereferenced address should be different from Integer.MIN_VALUE.
    private void call(int functor, boolean is_call, int d_address) {
        int[] point = call.getObj(functor); // Get the code point of the functor
        if (point == null || (point != null && point[0] < 0)) {
            backtrack();		// If the predicate is unknown: backtrack
        } else {
            if (is_call) {											// If you want to update the continuation pointer
                cp = p + 2;											// Then update the continuation pointer and code area
                cca = ca;
            }
            num_of_args = numArgs(functor);					// Update the number of arguments global
            b0 = b;													// Set the last choice point pointer (for cuts)
            p = point[0];											// Move to the instruction point of the functor
            ca = point[1];
            code = areas.get(ca);
            if (d_address != Integer.MIN_VALUE) // When executing/calling variables, update the argument registers
            {
                System.arraycopy(storage, d_address + 1, storage, storage.length - register_size + 1, num_of_args);
            }
        }
    }

    // In Prolog we can also call/execute a variable if it is a constant or a structure. For execute use is_call=false.
    private void variable_as_goal(int address, boolean is_call) {
        int cell = storage[deref(address)]; 						// Get the dereferenced cell
        int tag = cell_tag(cell);  									// Get its tag
        if (tag != STR && tag != CON) {
            backtrack();							// Can only call structures or constants
        } else {
            int d_address = cell_value(cell);						// Get the location of the functor cell
            int functor = tag == STR ? cell_value(storage[d_address]) : // Get the functor integer for structure
                    (d_address << 7);  							// Or for constant
            call(functor, is_call, d_address);						// Call the call method
        }
    }

    // Proceed the code by setting the instruction pointer and code area to their continuation pointers.
    private void proceed() {
        p = cp; 													// Continuation instruction
        ca = cca;													// Continuation code area
        code = areas.get(ca);									// Update pointer current code area
    }

    // Create a new environment on the environment/choice point stack. 
    private void allocate() {
        int newE = e > b ? e + instruction_arg(areas.get(cca)[cp - 2]) + 3 : // Reclaim stack space of unused permanent variables
                b + storage[b] + 10; 							// Or if a choice point is at the top, then start after it
        storage[newE] = e;											// Point towards the previous environment
        storage[newE + 1] = cp;										// Store the continuation point
        storage[newE + 2] = cca;
        e = newE;													// Update the top-of-environments pointer
        p++;														// Continue to next instruction
    }

    // Restore continuation point and discard the latest environment.
    private void deallocate() {
        cp = storage[e + 1];											// Restore the continuation point
        cca = storage[e + 2];
        e = storage[e];												// Reclaim memory
        p++;														// Continue to next instruction
    }

    ///////////////////////////////
    ///// CHOICE INSTRUCTIONS /////
    ///////////////////////////////
    //// Choice instructions allow the execution process to try out alternative rules for a predicate if they are present. These 
    //// instructions place and manipulate choice points which protect environments. A choice point stores the computational state of 
    //// the WAM right before a rule was tried.
    // The try_me_else instruction is for initiating a choice point. Assumes that the next instruction is part of the first alternative.
    private void try_me_else(int arg) {
        int l = (arg & 1) > 0 ? -(arg >>> 1) : (arg >>> 1);						// Get the pointer modifier (first bit declares negative number)
        meta_try(l, 1);												// Make the choice point
    }

    // Variation on try_me_else for indexed sequences. Next instruction after this try_ is one integer away, and the next instruction to
    // perform is the instruction pointer plus the argument. Indexed sequences have their try/retry/trust instructions one after another,
    // whereas try_me_else/retry_me_else/trust_me have other instructions between them.
    private void try_(int arg) {
        int l = (arg & 1) > 0 ? -(arg >>> 1) : (arg >>> 1);						// Get the pointer modifier (first bit declares negative number)
        meta_try(1, l);												// Make the choice point
    }

    // Variation for in dynamic predicates. Here the try/retry/trust code is not one after another and neither does the next instruction
    // immediately follow after them. Thus to get those two positions we need two arguments and have a slightly bigger instruction.
    private void d_try(int arg, int next) {
        int l = (arg & 1) > 0 ? (-(arg >>> 1)) : (arg >>> 1);						// Get the pointer modifier (first bit declares negative number)
        meta_try(next, l);											// Make the choice point
    }

    // Used by try instructions to create choice points. Stores the computational state of the WAM prior to choosing an alternative
    // for a rule. Variation differ in the next instruction if the alternative is backtracked and in the next instruction after the try.
    public void meta_try(int stored_p, int delta_p) {
        int newB = e > b ? e + instruction_arg(areas.get(cca)[cp - 2]) + 3 : // Reclaim stack space of unused permanent variables
                b + storage[b] + 10; 										// Or if a choice point is at the top, then start after it 
        storage[newB] = num_of_args;								// Store amount of arguments
        for (int i = 0; i < num_of_args; i++) // Store the argument registers in the choice point
        {
            storage[newB + 1 + i] = storage[A1 + i];
        }
        storage[newB + num_of_args + 1] = e;							// Store the globals
        storage[newB + num_of_args + 2] = cp;
        storage[newB + num_of_args + 3] = b;
        storage[newB + num_of_args + 4] = p + stored_p;					// Try instructions vary on the instruction after try
        storage[newB + num_of_args + 5] = tr;
        storage[newB + num_of_args + 6] = h;
        storage[newB + num_of_args + 7] = b0;
        storage[newB + num_of_args + 8] = cca;
        storage[newB + num_of_args + 9] = ca;
        b = newB;
        hb = h;
        p += delta_p;													// Try instructions also vary on the immediate next instruction
    }

    // Retry instructions come in the same categories as try instructions. They are used for the second up to the second-last
    // alternatives. They update the current choice point.
    private void retry_me_else(int arg) {
        int l = (arg & 1) > 0 ? -(arg >>> 1) : (arg >>> 1); 					// Get the pointer modifier (first bit declares negative number) 
        meta_retry(l, 1);											// Adapt choice point
    }

    private void retry(int arg) {
        int l = (arg & 1) > 0 ? -(arg >>> 1) : (arg >>> 1); 					// Get the pointer modifier (first bit declares negative number)
        meta_retry(1, l);											// Adapt choice point
    }

    private void d_retry(int arg, int next) {
        int l = (arg & 1) > 0 ? (-(arg >>> 1)) : (arg >>> 1); 					// Get the pointer modifier (first bit declares negative number)
        meta_retry(next, l);										// Adapt choice point
    }

    // Adapt a choice point and retrieve the computational state.
    public void meta_retry(int stored_p, int delta_p) {
        int n = storage[b];											// Get amount of arguments
        for (int i = 0; i < n; i++) // Put the original argument assignments back in the registers
        {
            storage[A1 + i] = storage[b + i + 1];
        }
        e = storage[b + n + 1];											// Restore environment 
        cp = storage[b + n + 2];										// Restore continuation code point
        cca = storage[b + n + 8];
        storage[b + n + 4] = p + stored_p;								// Update the next instruction when backtracking
        unwind_trail(storage[b + n + 5], tr);							// Undo bindings between now and last try/retry
        tr = storage[b + n + 5];										// Update trail top pointer
        h = storage[b + n + 6]; 										// Update heap top pointer
        hb = h;														// Update backtrack heap point
        p += delta_p;													// Go to next instruction 
    }

    // Trust instructions are the last of a series of alternatives. They are largely the same as retry instructions but discard the 
    // choice point afterwards. 
    public void trust_me() {
        meta_retry(0, 1);											// Reload state from choice point
        b = storage[b + storage[b] + 3];								// Remove choice point
    }

    // Trust and d_trust are the same. They take the delta p value from the instruction's argument.
    private void trust(int arg) {
        int l = (arg & 1) > 0 ? (-(arg >>> 1)) : (arg >>> 1); 					// Get the pointer modifier (first bit declares negative number)
        meta_retry(0, l);											// Reload state from choice point 
        b = storage[b + storage[b] + 3];								// Remove choice point
    }

    /////////////////////////////////
    ///// INDEXING INSTRUCTIONS /////
    /////////////////////////////////
    //// Static Prolog code can easily be optimized by using simple indexing algorithms. Indexing allows a choice of rules to be made
    //// based upon the first argument of the predicate. Predicates with STR or CON types for their first arguments are further indexed
    //// by hashmaps. With indexing one can skip alternatives.
    // Jump to a code point based on the type of the first argument. 
    private void switch_on_term(int V, int C, int L, int S, int N) {
        switch (cell_tag(storage[deref(A1)])) {						// Switch on the dereferenced argument cell tag
            case REF:
                p += V;
                break;										// Case for variables
            case CON:
                p += C;
                break;					// Case for constants
            case LIS:
                p += L;
                break;					// Case for lists
            case STR:
                p += S;
                break;					// Case for structures
            case NUM:
                p += N;
                break;					// Case for numbers
        }
    }

    // Search a constant in a hashmap that ties constants to instruction pointer updates.
    private void switch_on_constant(int N) {
        int cell = storage[deref(A1)]; 								// Dereference the address of the first argument
        int newP = search(cell_value(cell) << 7, N);					// Search the constant
        if (newP == Integer.MIN_VALUE) {
            backtrack(); 					// Constant was not found, so backtrack
        } else {
            p += newP;												// Otherwise update the instruction pointer
        }
    }

    // Search a number in a hashmap that given a number returns a pointer update.
    private void switch_on_num(int N) {
        int cell = storage[deref(A1)];  							// Dereference the address of the first argument
        int newP = search(cell_value(cell) << 4, N);					// Search the number
        if (newP == Integer.MIN_VALUE) {
            backtrack(); 					// Number was not found, so backtrack
        } else {
            p += newP;												// Otherwise update the instruction pointer
        }
    }

    // Search a functor integer in a hashmap that stores the pointer updates that belong to those integers.
    private void switch_on_structure(int N) {
        int cell = storage[cell_value(storage[deref(A1)])];  		// Dereference the first argument and get the functor cell
        int newP = search(cell_value(cell), N);						// Search the functor in the hashmap
        if (newP == Integer.MIN_VALUE) {
            backtrack(); 					// If not found: backtrack
        } else {
            p += newP;												// Else: update the instruction pointer
        }
    }

    // Jumplist are an alternative to hashmaps. They have a higher complexity (O(n), rather than hashmap's expected O(1)) but are
    // easer to maintain in dynamic memory if facts are retracted and asserted. A jumplist is a node list with for each node a pointer
    // to the next, an integer with the key, and an integer with where to go to if the key was searched. So a jumplist linearly searches
    // through the keys instead of using a hash system.
    private void jumplist(int start) {
        int cell = storage[deref(A1)]; 								// Get the dereferenced cell of argument one
        if (cell_tag(cell) == STR) {
            cell = storage[cell_value(cell)]; 	// In case of a structure we take its functor cell
        }
        int newP = Integer.MIN_VALUE;								// Init the pointer modifier
        int cursor = p + start;										// Go to the start of the list
        while (cursor >= 0) {											// While entries exist
            if (code[cursor + 1] == cell) {							// Check whether the key matches
                newP = code[cursor + 2];							// If so: get the pointer modifier
                cursor = -1;
            } else // Else:
            {
                cursor = code[cursor] == Integer.MIN_VALUE ? -1 : // If we reached the end of the list, set cursor to -1
                        cursor + code[cursor];					// Otherwise continue with next entry
            }
        }
        if (newP == Integer.MIN_VALUE) {
            backtrack(); 					// If element was not found: backtrack
        } else {
            p += newP;												// Otherwise continue execution
        }
    }

    ////////////////////////////
    ///// CUT INSTRUCTIONS /////
    ////////////////////////////
    //// Prolog allows for cuts. A cut removes the choice points between the latest point when the rule with the cut is tried and the
    //// latest point when the cut is reached as a subgoal of the rule.
    // A neck cut: a:-!... can be executed immediately. If there is a call/execute then b0 is set to b. So if b is bigger then b0 then
    // for the rule that contains the neck cut we have a choice point which now can be omitted (because try/retry/trust edit b).
    private void neck_cut() {
        if (b > b0) { 												// There is a choice point to delete
            b = b0;													// Remove the choice point (i.e. allow it to be overwritten)
            tidy_trail();											// Remove trail bindings which cannot be made undone due to the cut
        }
        p++;														// Continue execution
    }

    // Deep cuts: a:-b,!... Need to store the latest choice point in a permanent variable before executing preceeding subgoals. A
    // corresponding CUT instruction will do the actual cut.
    private void get_level(int Yn) {
        storage[e + 2 + Yn] = b0;										// Store the latest choice points
        p++;														// Continue execution
    }

    private void cut(int Yn) {
        if (b > storage[e + 2 + Yn]) {										// If new choice points occurred in the mean time
            b = storage[e + 2 + Yn];									// Then make those available to be overwritten
            tidy_trail();											// And clear the trail bindings which cannot be made undone anymore
        }
        p++;														// Continue execution
    }

    /////////////////////////
    // RETRACT INSTRUCTION //
    /////////////////////////
    private void retract() {
        cp = p + 2;													// Update the continuation pointer and code area
        cca = ca;
        b0 = b;
        if (!library.getPredDynamics().retract(false)) {
            backtrack();
        } else {
            p++;
        }
    }

    /////////////////////////
    // ANCILLARY FUNCTIONS //
    ///////////////////////// 
    // Follow a trail of references until either the variable is bound or you reach an unbound variable.
    public int deref(int address) {
        while (true) {
            int value = cell_value(storage[address]);                    // Get the cell value
            if (cell_tag(storage[address]) == REF && value != address) // If tag is REF and is not unbound...
            {
                address = value;
                continue;
            }
            return address;                                                // Otherwise return the argument address
        }
    }

    // Follow a trail of references until either the variable is bound or you reach an unbound variable.
    public static int external_deref(int[] source, int address) {
        while (true) {
            int value = cell_value(source[address]);                    // Get the cell value
            if (cell_tag(source[address]) == REF && value != address) // If tag is REF and is not unbound...
            {
                address = value;
                continue;
            }
            return address;                                                // Otherwise return the argument address
        }
    }

    // Bind two addresses, one of them has to be an unbound REF. This is assumed in the method so check for this property before calling.
    private void bind(int a, int b) {
        if (cell_tag(storage[a]) == REF && (cell_tag(storage[b]) != REF || b < a)) { // If 'a' is a variable and 'b' is not or older ...
            storage[a] = storage[b];									   // ... then 'a' is bound to 'b'
            trail(a);													   // Remember the binding
        } else {
            storage[b] = storage[a];									   // Otherwise bind 'b' to 'a'
            trail(b);													   // Remember the binding
        }
    }

    public boolean unify(int a, int b) {
        int tag1 = cell_tag(storage[a]);
        int tag2 = cell_tag(storage[b]);
        if (tag1 == REF || tag2 == REF) {
            unifynon_deref(deref(a), deref(b));
            return true;
        } else if (tag1 == tag2) {
            int value1 = cell_value(storage[a]);
            int value2 = cell_value(storage[b]);
            switch (tag2) {
                case CON:
                    return value1 == value2;
                case LIS:
                    return unify(value1, value2) && unify(value1 + 1, value2 + 1);
                case NUM:
                    return num_equality(value1, value2);
                case STR:
                    int f_n = cell_value(storage[value2]);
                    if (cell_value(storage[value1]) == f_n) {		// ... predicate and arity must be equal
                        int args = numArgs(f_n);		    // If equal, push the arguments and continue
                        for (int i = 1; i <= args; i++) {
                            if (!unify(value1 + i, value2 + i)) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
            }
        }
        return false;
    }

    private boolean unifynon_deref(int a, int b) {
        int tag1 = cell_tag(storage[a]);
        int tag2 = cell_tag(storage[b]);
        if (tag1 == REF || tag2 == REF) {
            bind(a, b);
            return true;
        } else if (tag1 == tag2) {
            int value1 = cell_value(storage[a]);
            int value2 = cell_value(storage[b]);
            switch (tag2) {
                case CON:
                    return value1 == value2;
                case LIS:
                    return unify(value1, value2) && unify(value1 + 1, value2 + 1);
                case NUM:
                    return num_equality(value1, value2);
                case STR:
                    int f_n = cell_value(storage[value2]);
                    if (cell_value(storage[value1]) == f_n) {		// ... predicate and arity must be equal
                        int args = numArgs(f_n);		    // If equal, push the arguments and continue
                        for (int i = 1; i <= args; i++) {
                            if (!unify(value1 + i, value2 + i)) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
            }
        }
        return false;
    }

    // Backtrack to a previous choice point. Note that this does not clear the heap. Thus one might want to use a garbage collection 
    // algorithm on top of this. For small Prolog queries (as is common in APL's) this would probably just slow the execution down.
    public void backtrack() {
        if (b == heap_size) { 				// Bottom of choice point stack reached, so overall failure occurs
            fail = true;
        } else {							// Otherwise restore the instruction point
            p = storage[b + storage[b] + 4];
            cca = storage[b + storage[b] + 8];
            ca = storage[b + storage[b] + 9];
            code = areas.get(ca);
            b0 = storage[b + storage[b] + 7];
        }
    }

    // Check whether to store a bound address, and store it if it has to be.
    public void trail(int a) {
        if (a < hb || (h < a && a < b)) { // Store if the variable was created before the heap part after the last choice point or ...
            storage[tr] = a;			// ... if the variable was on the environment/choice stack before the latest choice point...
            tr++;						// ... (i.e. any variable older than the latest choice point)
        }
    }

    // Undo part of the variable bindings by replacing them with unbound REF cells.
    public void unwind_trail(int a1, int a2) {
        for (int i = a1; i < a2; i++) {
            storage[storage[i]] = newCell(REF, storage[i]);
        }
    }

    // Clear the trail of bindings after the last cut choice point. These bindings will never be made undone to try other alternatives.
    private void tidy_trail() {
        int i = b == heap_size ? (heap_size + stack_size) : storage[b + storage[b] + 5]; // Get the correct trail point out of the choice points (if any)
        while (i < tr) {														 // For the entire trail	
            if (storage[i] < hb || ((h < storage[i]) && (storage[i] < b))) // Skip bindings from before the cut choice point
            {
                i++;
            } else {
                storage[i] = storage[tr - 1];									 // Overwrite a binding with the last binding in the trail
                tr--;
            }
        }
    }

    // Search the value of a key in a hashmap. Note that for these hashmaps the hash function is the identity function.
    private int search(int key, int N) {
        int e = p + 1 + ((key >>> 7) & N); 									  // Get the bucket array in the hashmap
        if (code[e] == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE; // Not even a chain at this index so does not exist
        }
        e = code[e]; 											  // Move to first entry of the chain
        do {
            if (code[p + e + 2] == key) {
                return code[p + e + 3];		  // If the key is found, return the value
            } else {
                e = code[p + e + 1];								  // Else move to the next entry in the bucket array
            }
        } while (e != Integer.MIN_VALUE);								  // Will reach MIN_VALUE if key does not exist
        return Integer.MIN_VALUE;									  // Return MIN_VALUE as equivalent of null
    }

    // Check whether two NUM cell value's are equal. If they are not simple numbers, then the number container will be called upon.
    public boolean num_equality(int v1, int v2) {
        return (v1 & 3) == 0 ? ((v2 & 3) == 0 && (v2 == v1)) : numbers.are_equal(v1, v2);
    }

    // Cell (de-)composition:
    public static final int newCell(int tag, int value) {
        return (value << 3) | tag;
    }

    public static final int cell_value(int cell) {
        return cell >>> 3;
    }

    public static final int cell_tag(int cell) {
        return cell & 7;
    }

    // Instruction (de-)composition:
    public static final int make_instruction(int instruction, int arg) {
        return (arg << 7) | instruction;
    }

    public static final int instruction_arg(int instruction) {
        return instruction >>> 7;
    }

    public static final int instruction_code(int instruction) {
        return instruction & 127;
    }

    // Functor integer decomposition:
    public static final int functNR(int functor_int) {
        return functor_int >>> 7;
    }

    public static final int numArgs(int functor_int) {
        return functor_int & 127;
    }

    /**
     * Append a compiled WAM query to the engine. This will overwrite the
     * previous query.
     *
     * @param query The query (compiled WAM code) to append to the code area's.
     */
    public void setQuery(int[] query) {
        int[] prevCode = areas.set(1, query);									// Overwrite the query area
        code = query; //areas.get(1);										// Set the engine's current code area to the query code
        ca = 1;															// Set the execution point to the query area at position 1
        p = 0;
    }
    
    /** get current query (compiled) code */
    public int[] getQuery() {
        return code;
    }

    /////////////////////////////
    // GETTER & SETTER METHODS //
    ///////////////////////////// 
    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getCP() {
        return cp;
    }

    public void setCP(int cp) {
        this.cp = cp;
    }

    public int getE() {
        return e;
    }

    public void setE(int e) {
        this.e = e;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getB0() {
        return b0;
    }

    public void setB0(int b0) {
        this.b0 = b0;
    }

    public int getHB() {
        return hb;
    }

    public void setHB(int hb) {
        this.hb = hb;
    }

    public int getTR() {
        return tr;
    }

    public void setTR(int tr) {
        this.tr = tr;
    }

    public int getCA() {
        return ca;
    }

    public void setCA(int ca) {
        this.ca = ca;
    }

    public int getCCA() {
        return cca;
    }

    public void setCCA(int cca) {
        this.cca = cca;
    }

    public int getNumOfArgs() {
        return num_of_args;
    }

    public void setNumOfArgs(int num_of_args) {
        this.num_of_args = num_of_args;
    }

    public int getHeapSize() {
        return heap_size;
    }

    public void setHeapSize(int heap_size) {
        this.heap_size = heap_size;
    }

    public String string(int x) {
        return strings.get(x);
    }
    public Strings strings() {
        return strings;
    }

    public void setStringContainer(Strings c) {
        this.strings = c;
    }

    public int getRegisterSize() {
        return register_size;
    }

    public void setRegisterSize(int register_size) {
        this.register_size = register_size;
    }

    public int getPDLSize() {
        return unify_pdl_size;
    }

    public void setPDLSize(int pdl_size) {
        this.unify_pdl_size = pdl_size;
    }

    public int getStackSize() {
        return stack_size;
    }

    public void setStackSize(int stack_size) {
        this.stack_size = stack_size;
    }

    public int getTrailSize() {
        return trail_size;
    }

    public void setTrailSize(int trail_size) {
        this.trail_size = trail_size;
    }

    public int[] getStorage() {
        return storage;
    }

    public void setStorage(int[] storage) {
        this.storage = storage;
    }

    public int[] getPDL() {
        return unify_pdl;
    }

    public void setPDL(int[] pdl) {
        this.unify_pdl = pdl;
    }

    public int[] getCodeArea() {
        return code;
    }

    public void setCodeArea(int[] code) {
        this.code = code;
        areas.add(code);
    }

    public ArrayList<int[]> getCodeAreas() {
        return areas;
    }

    public void setCodeAreas(ArrayList<int[]> areas) {
        this.areas = areas;
    }

    public IntHashMap<int[]> getCallFunction() {
        return call;
    }

    public void setCall(IntHashMap<int[]> call) {
        this.call = call;
    }

    public boolean getTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean getUntilCall() {
        return untilCall;
    }

    public void setUntilCall(boolean b) {
        this.untilCall = b;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean hasFailed() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public Library getLibrary() {
        return library;
    }

    public void setBuiltIns(Library bi) {
        library = bi;
    }

    public Numbers numbers() {
        return numbers;
    }

    public void setNumbers(Numbers n) {
        numbers = n;
    }

    public nars.jwam.compiler.Compiler getCompiler() {
        return compiler;
    }


    public RuleHeap rules() {
        return rules;
    }

    public void setSrcManager(RuleHeap d) {
        rules = d;
    }

    public int regStart() {
        return storage.length - register_size;
    }

    public int getA1() {
        return A1;
    }

    /////////////////////
    // MISC. FUNCTIONS //
    /////////////////////
    /**
     * Make everything ready to receive a new query.
     */
    public void prepare_for_new_query() {
        h = s = p = cp = num_of_args = hb = 0;
        read = true;
        trace = fail = false;
        e = heap_size;
        tr = e + stack_size;
        b = e;
        b0 = b;
        cca = ca = 0;
        A1 = regStart() + 1;
        numbers.reset_temps(); // Remove temporary numbers
        rules.cleanDirty(); // Remake try retry trust code of previously changed predicates
    }

    /**
     * Constructor for the WAM.
     */
    public WAM(int heap_size, int register_size, int stack_size, int trail_size, int unify_pdl_size, int query_space) {
        setGlobals(heap_size, register_size, stack_size, trail_size, unify_pdl_size, query_space);
        clear();									// Initialize the memory
    }

    /**
     * Recreate all used memory.
     */
    public void clear() {
        storage = new int[heap_size + register_size + stack_size + trail_size];
        unify_pdl = new int[unify_pdl_size + 1];
        areas = new ArrayList<>();
        areas.add(new int[0]); 						// Reserve for static code
        areas.add(new int[0]); 						// Reserve for query
        code = areas.get(0);
        cca = ca = 0;
        call = new IntHashMap<>(IntHashMap.OBJ);
        strings = new Strings();
        strings.add("!", 0);
        numbers = new Numbers();
        rules = new RuleHeap(this);
        compiler = new nars.jwam.compiler.Compiler(rules, strings, numbers, regStart());
        library = new Library(this);
        A1 = regStart() + 1;
    }

    /**
     * Set the various globals.
     */
    public void setGlobals(int heap_size, int register_size, int stack_size, int trail_size, int unify_pdl_size, int query_space) {
        this.heap_size = heap_size;
        this.register_size = register_size;
        this.unify_pdl_size = unify_pdl_size - 1;
        this.trail_size = trail_size;
        this.stack_size = stack_size;
        storage = new int[heap_size + register_size + stack_size + trail_size];
        e = heap_size;
        tr = e + stack_size;
        b = e;
        b0 = b;
        unify_pdl = new int[unify_pdl_size];
        A1 = regStart() + 1;
        cca = ca = 0;
        areas = new ArrayList<>();
        areas.add(new int[0]);
        areas.add(new int[0]);
    }

    public WAM shallow_clone() {
        return null; // TODO Clone of computational state, but no cloning of (dynamic) code area's, String and number container
    }

    public static String numArgsSuffix(int f) {
        return '/' + Integer.toString(numArgs(f));
    }
}
