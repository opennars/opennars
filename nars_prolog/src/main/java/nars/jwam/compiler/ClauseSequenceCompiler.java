package nars.jwam.compiler;

import nars.jwam.RuleHeap;
import nars.jwam.WAM;
import nars.jwam.datastructures.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ClauseSequenceCompiler {

    Strings strings = null;

    public int[] compile_sequence(RuleHeap r, int functor) {
        this.strings = r.getWAM().strings();
        ArrayList<int[]> entries = r.instruction(functor); 	// The instructions of the individual clauses
        int[] prim_result = null;
        if (entries.size() == 1) {
            prim_result = Arrays.copyOf(entries.get(0), entries.get(0).length + 1); 				// Only one entry, return its instructions
            prim_result[prim_result.length - 1] = WAM.make_instruction(WAM.DYNAMIC_CODE_END, prim_result.length - 1);
        } else {
            ArrayList<Integer> result = new ArrayList<Integer>();
            if (r.isDynamic(functor)) {
                addDynamicTRT(result, entries, 0, entries.size());
                result.add(WAM.make_instruction(WAM.DYNAMIC_CODE_END, result.size()));
            } else if (WAM.numArgs(functor) == 0) {
                addTRT(result, entries, 0, entries.size()); // If dynamic or no arguments: simply do TRT
            } else {
                ArrayList<int[]> sequences = toSequences(r, functor);		// Determine the sequences
                if (sequences.size() == 1) {
                    prim_result = Arrays.copyOf(sequences.get(0), sequences.get(0).length + 1); 	// Only one sequence 
                } else {
                    addTRT(result, sequences, 0, sequences.size()); 			// TRT surrounding sequences  
                }
            }
            if (prim_result == null) { 											// Convert the result array to primitive integers
                prim_result = new int[result.size()];
                for (int i = 0; i < result.size(); i++) {
                    prim_result[i] = result.get(i);
                }
            }
        }
        return prim_result;
    }

    private void addTRT(ArrayList<Integer> instructions, ArrayList<int[]> entries, int start, int end) {
        for (int i = start; i < end; i++) { 									// Add TRT (try retry trust) instructions
            if (i == start) {
                instructions.add(WAM.make_instruction(WAM.TRY_ME_ELSE, (entries.get(start).length + 1) << 1));
            } else if (i == end - 1) {
                instructions.add(WAM.make_instruction(WAM.TRUST_ME, 0));
            } else {
                instructions.add(WAM.make_instruction(WAM.RETRY_ME_ELSE, (entries.get(i).length + 1) << 1));
            }
            for (int j = 0; j < entries.get(i).length; j++) // Append the entry's code
            {
                instructions.add(entries.get(i)[j]);
            }
        }
    }

    private void addDynamicTRT(ArrayList<Integer> instructions, ArrayList<int[]> entries, int start, int end) {
        for (int i = start; i < end; i++) { 									// Add TRT (try retry trust) instructions
            if (i == start) {
                instructions.add(WAM.make_instruction(WAM.D_TRY, 2 << 1));
            } else if (i == end - 1) {
                instructions.add(WAM.make_instruction(WAM.D_TRUST, 2 << 1));
            } else {
                instructions.add(WAM.make_instruction(WAM.D_RETRY, 2 << 1));
            }
            instructions.add(entries.get(i).length + 2);
            for (int j = 0; j < entries.get(i).length; j++) // Append the entry's code
            {
                instructions.add(entries.get(i)[j]);
            }
        }
    }

    private ArrayList<int[]> toSequences(RuleHeap dsm, int functor) {
        ArrayList<int[]> r = new ArrayList<int[]>();						 // The result instructions
        ArrayList<int[]> clauses = dsm.instruction(functor);		 // The instructions of the individual clauses
        ArrayList<int[]> heaps = dsm.getHeaps().get(functor);				 // The heap data of each instruction
        boolean appendToSequence = false;									 // Boolean for whether a new sequence is needed or not
        ArrayList<int[]> sequence = new ArrayList<int[]>();
        ArrayList<int[]> heap_sequence = new ArrayList<int[]>();
        for (int i = 0; i < clauses.size(); i++) {
            int[] c = clauses.get(i);
            int[] h = heaps.get(i);
            int type = WAM.cell_tag(h[Compiler.getArgStart(h, h.length - 1)]); // Get the type of the first argument
            if (type == WAM.REF) { 												 // Variables form their personal sequences
                if (appendToSequence && sequence.size() > 0) // Was still building a sequence and had to add to it
                {
                    r.add(sequence.size() == 1 ? sequence.get(0) : // Current sequence had size of 1 so just add it
                            sequenceToInstructions(sequence, heap_sequence));	 // Current sequence larger than one, so do indexing
                }
                r.add(c); 													 // Add this clause
                appendToSequence = false;									 // Next element starts new sequence
            } else {														 // No REF as first argument
                if (!appendToSequence) {										 // New sequence needed
                    appendToSequence = true;								 // Next element is added to this sequence
                    sequence = new ArrayList<int[]>();
                    heap_sequence = new ArrayList<int[]>();
                }
                sequence.add(c);											 // Add this clause to the sequence
                heap_sequence.add(h);										 // Add this heap to the sequence
            }
        }
        if (appendToSequence && sequence.size() > 0) // Done, might need to finish the current sequence
        {
            r.add(sequence.size() == 1 ? sequence.get(0)
                    : sequenceToInstructions(sequence, heap_sequence));
        }
        return r;
    }

    private int[] sequenceToInstructions(ArrayList<int[]> sequence, ArrayList<int[]> heap_sequence) {
        ArrayList<Integer> r = new ArrayList<Integer>();
        HashMap<int[], Integer> start = new HashMap<int[], Integer>();
        HashMap<Integer, ArrayList<int[]>> constants = new HashMap<Integer, ArrayList<int[]>>();
        HashMap<Integer, ArrayList<int[]>> structures = new HashMap<Integer, ArrayList<int[]>>();
        HashMap<Integer, ArrayList<int[]>> lists = new HashMap<Integer, ArrayList<int[]>>();
        HashMap<Integer, ArrayList<int[]>> nums = new HashMap<Integer, ArrayList<int[]>>();
        r.add(0);
        r.add(0);
        r.add(0);
        r.add(0);
        r.add(0); 							// Reserved space for switch_on_term
        boolean hasVarEntry = false;
        int v_start = r.size(); 												// Start for clauses with first-arg = REF
        for (int i = 0; i < sequence.size(); i++) {
            int[] c = sequence.get(i);
            int[] h = heap_sequence.get(i);
            int key = h[Compiler.getArgStart(h, Compiler.getPartsStart(h, h.length - 1, strings))]; 					// Cell is key: tag & value
            int type = WAM.cell_tag(key);
            switch (type) {
                case WAM.CON:
                    if (constants.get(key) == null) {
                        constants.put(key, new ArrayList<int[]>());
                    }
                    constants.get(key).add(c);
                    break;
                case WAM.NUM:
                    if (nums.get(key) == null) {
                        nums.put(key, new ArrayList<int[]>());
                    }
                    nums.get(key).add(c);
                    break;
                case WAM.STR:
                    System.out.println("blabla " + WAM.cell_value(h[WAM.cell_value(key)]) + " " + WAM.cell_tag(h[WAM.cell_value(key)]));
                    key = h[WAM.cell_value(key)]; 									// Get the PN cell
                    if (structures.get(key) == null) {
                        structures.put(key, new ArrayList<int[]>());
                    }
                    structures.get(key).add(c);
                    break;
                case WAM.LIS:
                    if (lists.get(0) == null) {
                        lists.put(0, new ArrayList<int[]>());
                    }
                    lists.get(0).add(c);
                    break;
                case WAM.REF:
                    hasVarEntry = true;
            }
            start.put(c, r.size());
            r.add(i == 0 ? WAM.make_instruction(WAM.TRY_ME_ELSE, (c.length + 1) << 1) : (i == sequence.size() - 1 ? WAM.make_instruction(WAM.TRUST_ME, 0)
                    : WAM.make_instruction(WAM.RETRY_ME_ELSE, (c.length + 1) << 1))
            );
            for (int j = 0; j < c.length; j++) {
                r.add(c[j]);
            }
        }

        // Make normal switch
        int c_start = 0;								// Standard pointer is 0, no entry for this argument type
        int l_start = 0;
        int s_start = 0;
        int n_start = 0;
        if (hasVarEntry) {
            c_start = l_start = s_start = n_start = 5;  // However if one of the first arguments is variable then always start
        }
        c_start = constants.isEmpty() ? c_start : addSimpleTRT(r, constants, start);
        l_start = lists.isEmpty() ? l_start : addSimpleTRT(r, lists, start);
        s_start = structures.isEmpty() ? s_start : addSimpleTRT(r, structures, start);
        n_start = nums.isEmpty() ? n_start : addSimpleTRT(r, nums, start);
        if (c_start < 0) {
            c_start = r.size();
            makeHashMap(r, WAM.SWITCH_CON, constants, start);
        }
        if (l_start < 0) {
            l_start = start.get(lists.values().iterator().next().get(0)); // Lists jump to their try trust retry instructions
        }
        if (s_start < 0) {
            s_start = r.size();// make switch on constant
            makeHashMap(r, WAM.SWITCH_STR, structures, start);
        }
        if (n_start < 0) {
            n_start = r.size();
            makeHashMap(r, WAM.SWITCH_NUM, nums, start);
        }
        if (c_start != 5 || l_start != 5 || s_start != 5 || n_start != 5) {				// If a main switch is needed
            r.set(0, WAM.make_instruction(WAM.SWITCH_TERM, v_start)); 	// Make main switch
            r.set(1, c_start);
            r.set(2, l_start);
            r.set(3, s_start);
            r.set(4, n_start);
        } else {
            for (int i = 0; i < 5; i++) {
                r.remove(0);					// Otherwise remove the reserved space
            }
        }
        int[] result = new int[r.size()]; 								// Convert and return
        for (int i = 0; i < r.size(); i++) {
            result[i] = r.get(i);
        }
        return result;
    }

    private int addSimpleTRT(ArrayList<Integer> instructions, HashMap<Integer, ArrayList<int[]>> entries, HashMap<int[], Integer> start) {
        int startPos = -1;
        for (Integer key : entries.keySet()) {
            ArrayList<int[]> occs = entries.get(key);
            if (entries.size() == 1 && occs.size() == 1) // Only one occurrence
            {
                return start.get(occs.get(0)) + 1;							// So no TRT
            } else if (occs.size() > 1) {
                int newStart = instructions.size() - 1;						// New start of the clause
                for (int i = 0; i < occs.size(); i++) {						// For each occurrence
                    int l = start.get(occs.get(i)) + 1 - instructions.size();	// Instruction pointer update
                    l = l > 0 ? l << 1 : (((-l) << 1) | 1);								// Small encoding for negative update
                    instructions.add(i == 0 ? WAM.make_instruction(WAM.TRY, l) : ( // Add TRT instruction
                            i == occs.size() - 1 ? WAM.make_instruction(WAM.TRUST, l)
                                    : WAM.make_instruction(WAM.RETRY, l)));
                    start.put(occs.get(i), newStart); 						// They all point now towards the start of their third level indexing
                }
            }
            if (entries.size() == 1) // Only one TRT sequence
            {
                return start.get(occs.get(0)) + 1; 							// So return its start
            }
        }
        return startPos;
    }

    public void makeHashMap(ArrayList<Integer> r, int instruction, HashMap<Integer, ArrayList<int[]>> map, HashMap<int[], Integer> start) {
        ModPrimIntKeyHashMap hashmap = new ModPrimIntKeyHashMap();
        for (Integer key : map.keySet()) {
            int[] first_clause = map.get(key).get(0);
            if (instruction == WAM.SWITCH_NUM) {
                hashmap.put(WAM.cell_value(key), start.get(first_clause) - r.size() + 1);
            } else if (instruction == WAM.SWITCH_CON) {
                hashmap.put(WAM.cell_value(key) << 7, start.get(first_clause) - r.size() + 1);
            } else {
                hashmap.put(WAM.cell_value(key), start.get(first_clause) - r.size() + 1);
            }
        }
        r.add(WAM.make_instruction(instruction, hashmap.entry_space)); // entry space is needed for the key
        r.addAll(hashmap.toArrayList());
    }
}
