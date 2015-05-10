package nars.jwam.compiler;

import javolution.io.CharSequenceReader;
import nars.jwam.RuleHeap;
import nars.jwam.WAM;
import nars.jwam.datastructures.IntArrayList;
import nars.jwam.datastructures.Numbers;
import nars.jwam.datastructures.Strings;
import nars.jwam.parser.ParseException;
import nars.jwam.parser.Parser;
import nars.jwam.parser.ToHeap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * Can compile Prolog to heaps or WAM instructions
 */
public class Compiler {

    // Stores the heap representations
    private final RuleHeap sources;

    // Converts files to heaps
    private final Parser parser;

    // Keeps int->String mapping, needed for toString 
    private final Strings strings;
    // Keeps int->String mapping, needed for toString 
    private final Numbers nums;
    private final SingleClauseCompiler single_clause_compiler;
    private final ClauseSequenceCompiler clause_sequence_compiler;

    public Compiler(RuleHeap sources, Strings strings, Numbers nums, int registerstart) {
        this.sources = sources;
        this.strings = strings;
        this.nums = nums;
        parser = new Parser(new StringReader(""));
        parser.setToHeap(new ToHeap(sources, strings, nums));
        single_clause_compiler = new SingleClauseCompiler(strings, nums, registerstart, sources);
        clause_sequence_compiler = new ClauseSequenceCompiler();
    }

    /**
     * Reset the compiler. Will remove all rule entries as well.
     */
//    Compiler reset() {
//        //for now, do nothing here. any reset will be triggered and managed by the WAM 
//        return this;
//    }
    /**
     * Compiles a file and adds the data to previously compiled rules.
     *
     * @param file File to compile.
     */
    public void compile_file(String file) throws ParseException, FileNotFoundException {

        File f = new File(file);
        FileReader fr = new FileReader(f);
        parser.reset(fr);
        parser.Prolog();

    }

    /**
     * Compiles a String and adds the data to previously compiled rules.
     *
     * @param file File to compile.
     */
    public Compiler compile_string(String str) throws ParseException {        
        parser.reset(new StringReader(str));
        parser.Prolog();
        return this;
    }
    public Compiler compile_string(CharSequence str) throws ParseException {        
        parser.reset(new CharSequenceReader().setInput(str));
        parser.Prolog();
        return this;
    }

    /**
     * Compiles a query.
     *
     */
    public int[] compile_query(String str) throws ParseException {
        String s = str.startsWith("?") ? str : ("? " + str);
        s = s.endsWith(".") ? s : (s + '.');
        parser.reset(new StringReader(s));
        
        // Convert to heap
        parser.Query();
        
        // Convert to instructions
        single_clause_compiler.compile_clause(sources.getQueryHeap(), true, sources);  
        
        //Put query in the WAM
        sources.loadQuery();		
        
        return Arrays.copyOf(sources.getQueryInstructions(), sources.getQueryInstructions().length);

    }

    /**
     * Finalize compilation; Compile the sequences and put the code in the WAM.
     * Should be used after compiling files.
     */
    public Compiler commit() {
        for (Integer key : sources.getHeaps().keySet()) {

            for (int[] clause : sources.heap(key)) {
                single_clause_compiler.compile_clause(clause, false, sources);
            }

            // Compile the sequences 
            int[] r = clause_sequence_compiler.compile_sequence(sources, key);

            // Get the functor start point in the code
            sources.getCallStarts().put(key, sources.getAreas().size());

            // Add code to the code area's
            sources.getAreas().add(r);
        }
        // Transfer the code into the WAM
        sources.injectAllCode();

        return this;
    }

    public Parser getParser() {
        return parser;
    }

    public SingleClauseCompiler getSingleClauseCompiler() {
        return single_clause_compiler;
    }

    public ClauseSequenceCompiler getSequenceCompiler() {
        return clause_sequence_compiler;
    }

    /**
     * Given an heap entry for rules, get the amount of arguments for a
     * predicate/list.
     *
     * @param heap Source heap.
     * @param cursor Cursor where the top structure starts.
     * @return Number of parts of the clause (body parts + 1 for the head).
     */
    public static int numberOfArgs(int[] heap, int cursor) {
        int tag = WAM.cell_tag(heap[cursor]);
        if (tag == WAM.STR) {
            int cell = heap[WAM.cell_value(heap[cursor])]; 							// Dereference STR cell to PN cell
            return WAM.numArgs(WAM.cell_value(cell)); 						// Return the arity of the PN cell
        } else if (tag == WAM.LIS) {
            return 2; 											// Lists have two parts always
        }
        return 0;
    }

    /**
     * Given an heap entry for rules, get the amount of body parts plus one.
     *
     * @param heap Source heap.
     * @param cursor Cursor where the top structure starts.
     * @return Number of parts of the clause (body parts + 1 for the head).
     */
    public static int numberOfParts(int[] heap, int cursor, Strings strings) {
        int tag = WAM.cell_tag(heap[cursor]);
        if (tag == WAM.CON) {
            return 1;
        } else if (tag == WAM.STR) {
            int f_int = WAM.cell_value(heap[WAM.cell_value(heap[cursor])]); 		// Get functor int
            String functor = strings.get(f_int); 								// Get functor
            if (functor.startsWith(":-/")) {
                return WAM.numArgs(f_int);			// Rule with a body, so get :-'s arity
            } else {
                return 1; 															// Otherwise a fact, is its own and only bodypart 
            }
        }
        return 0;
    }

    public static int getTopFN(int[] heap, int cursor, Strings strings) {
        while (true) {
            int tag = WAM.cell_tag(heap[cursor]);
            if (tag == WAM.STR) {
                int addr = WAM.cell_value(heap[cursor]);                                // Get functor address
                int r = WAM.cell_value(heap[addr]);
                String functor = strings.get(r);                                    // Get functor
                if (functor.startsWith(":-/")) {
                    cursor = addr + 1;
                } else {
                    return r;                                                            // Otherwise a fact, is its own and only bodypart
                }
            } else if (tag == WAM.CON) {
                return WAM.cell_value(heap[cursor]) << 7;
            } else {
                return Integer.MIN_VALUE;
            }
        }
    }

    public static int getPartsStart(int[] heap, int cursor, Strings strings) {
        int tag = WAM.cell_tag(heap[cursor]);
        if (tag == WAM.STR) {
            int addr = WAM.cell_value(heap[cursor]); 								// Get functor address
            String functor = strings.get(WAM.cell_value(heap[addr])); 		// Get functor
            if (functor.startsWith(":-/")) {
                return addr + 1; 							// Rule with a body, so start at first argument
            } else {
                return cursor; 													// Otherwise a fact, is its own and only bodypart
            }
        } else if (tag == WAM.LIS) {
            return WAM.cell_value(heap[cursor]); 					// List directly points to first
        } else {
            return cursor; 														// All others start on their own position
        }
    }

    public static int getArgStart(int[] heap, int cursor) {
        int tag = WAM.cell_tag(heap[cursor]);
        if (tag == WAM.STR) {
            return WAM.cell_value(heap[cursor]) + 1; 					// Dereference STR cell to PN cell
        } else if (tag == WAM.LIS) {
            return WAM.cell_value(heap[cursor]);	 				// List directly points to first
        } else {
            return cursor; 														// All others start on their own position
        }
    }

    public static void addChildren(int[] heap, int cursor, IntArrayList list) {
        int tag = WAM.cell_tag(heap[cursor]);
        if (tag == WAM.STR || tag == WAM.LIS) {
            int nr_args = numberOfArgs(heap, cursor); 								// Works also for arguments of constructions
            int start_of_args = getArgStart(heap, cursor); 							// Get the address of the first argument
            for (int i = start_of_args; i < start_of_args + nr_args; i++) {
                list.add(i); 													// Next round's addresses
            }
        }
    }

    public static void initialRound(int[] heap, int cursor, IntArrayList list) {
        int nr_args = numberOfArgs(heap, cursor); 									// Works also for arguments of constructions 
        int start_of_args = getArgStart(heap, cursor); 								// Get the address of the first argument
        for (int i = start_of_args; i < start_of_args + nr_args; i++) {
            list.add(i); 	// Next round's addresses
        }
    }
}
