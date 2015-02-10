package nars.jwam;

import nars.jwam.datastructures.*;

import java.util.*;

// Editable reason base
public class RuleHeap {

    private HashMap<Integer, ArrayList<int[]>> heaps = null;		// f/n -> heap representations of entries
    private HashMap<Integer, ArrayList<int[]>> instructions = null;	// f/n -> instructions of entries
    private ArrayList<int[]> areas = null;							// Code area's
    private HashMap<Integer, Integer> call_starts = null; 			// Functor integer -> code area index
    private final WAM wam;
    private int[] query_heap = null, query_instructions = null;
    private HashMap<Integer, String> query_vars = null;
    private final ArrayList<String> dynamics;
    private final HashSet<Integer> dirty_functors;
    private final HashMap<Integer, IntArrayList> marked_for_removal;

    public RuleHeap(WAM wam) {
        this.wam = wam;
        heaps = new HashMap<>();
        instructions = new HashMap<>();
        areas = new ArrayList<>();
        areas.add(0, new int[0]);
        areas.add(0, new int[0]);			// Reserved space for query
        call_starts = new HashMap<>();
        query_vars = new HashMap<>();
        dynamics = new ArrayList<>();
        dirty_functors = new HashSet<>();
        marked_for_removal = new HashMap<>();
    }

    /**
     * Replace all the code in the WAM with the area's in this source manager.
     * Also refreshes the call function.
     */
    public void injectAllCode() {
        IntHashMap<int[]> call = new IntHashMap<>(IntHashMap.OBJ);
        for (Integer key : call_starts.keySet()) // Make updated call function
        {
            call.putObj(key, WAM.call(0, call_starts.get(key)));
        }
        wam.setCall(call);											// Overwrite the WAM's data
        wam.setCodeAreas(areas);
    }

    /**
     * Remake all the dirty predicates for clean trt code. Quite expensive (O(n)
     * in amount of instructions) so do not use for large data dynamic sets.
     */
    public void cleanDirty() {
        for (int key : dirty_functors) {
            IntArrayList remove = marked_for_removal.get(key);
            if (remove != null) {
                ArrayList<int[]> heap = heaps.get(key);
                ArrayList<int[]> code = instructions.get(key);
                int[] ar = Arrays.copyOf(remove.data, remove.size());
                Arrays.sort(ar);
                for (int i = ar.length - 1; i >= 0; i--) {
                    final int ari = ar[i];
                    heap.remove(ari);
                    removeNums(code.get(ari));
                    code.remove(ari);
                }
            }
            int[] p = wam.getCallFunction().getObj(key);
            if (p[1] < areas.size()) {
                int[] r = wam.getCompiler().getSequenceCompiler().compile_sequence(this, key);
                areas.set(p[1], r);
            }
            p[0] = (heaps.get(key).isEmpty()) ? -1 : 0;
            //wam.getCallFunction().putObj(key, new Point(0,p[1]));
        }
        dirty_functors.clear();
        marked_for_removal.clear();
    }

    private void removeNums(int[] code) {
        // TODO: remove occurring numbers
    }

    public void dynamic_assert(int functor, int[] code, int[] heap, boolean front) {
        int[] p = wam.getCallFunction().getObj(functor);
        int[] current;
        if (p == null) {
            p = WAM.call(0, wam.getCodeAreas().size());
            wam.getCallFunction().putObj(functor, p);
            current = new int[]{WAM.make_instruction(WAM.DYNAMIC_CODE_END, 0)};
            wam.getCodeAreas().add(current);
        } else {
            current = wam.getCodeAreas().get(p[1]);
        }
        dirty_functors.add(functor);
        addHeap(functor, heap, front);
        addInstructions(functor, code, front);
        //System.out.println(p[0] + " "+current.length);
        int size = p[0] < 0 ? 1 : WAM.instruction_arg(current[current.length - 1]); // -1 for deleting end of code instruction
        IntArrayList new_code = new IntArrayList(current, size);
        int first_code = WAM.instruction_code(current[p[0] > 0 ? p[0] : 0]);
        if (p[0] < 0) {	// All entries were retracted
            p[0] = new_code.size(); // So only update p[0] 
        } else if (!front && first_code != WAM.D_TRY) { // only one entry was present
            int ptr = toArg(p[0] - size);
            p[0] = new_code.size();
            new_code.add(WAM.make_instruction(WAM.D_TRY, ptr));
            new_code.add(2); // Else try next entry
            new_code.add(WAM.make_instruction(WAM.D_TRUST, 2 << 1));
            new_code.add(0); // Else try next entry 
        } else if (!front) { // Last d_trust becomes d_retry and add d_trust for new entry
            int last_code_pos = p[0];
            int last_code = WAM.instruction_code(current[last_code_pos]);
            while (last_code != WAM.D_TRUST) {
                last_code_pos += current[last_code_pos + 1];
                last_code = WAM.instruction_code(current[last_code_pos]);
            }
            new_code.data[last_code_pos] = WAM.make_instruction(WAM.D_RETRY, WAM.instruction_arg(current[last_code_pos]));
            new_code.data[last_code_pos + 1] = size - last_code_pos;
            new_code.add(WAM.make_instruction(WAM.D_TRUST, 2 << 1));
            new_code.add(0);
        } else if (front && first_code != WAM.D_TRY) {
            int ptr = toArg(p[0] - size);
            new_code.add(WAM.make_instruction(WAM.D_TRUST, ptr));
            new_code.add(0); // Else try next entry
            p[0] = new_code.size();
            new_code.add(WAM.make_instruction(WAM.D_TRY, 2 << 1));
            new_code.add(-2); // Else try next entry
        } else if (front) {
            new_code.data[p[0]] = WAM.make_instruction(WAM.D_RETRY, WAM.instruction_arg(new_code.data[p[0]]));
            int ptr = p[0] - size;
            p[0] = new_code.size();
            new_code.add(WAM.make_instruction(WAM.D_TRY, 2 << 1));
            new_code.add(ptr); // Else try next entry
        }
        new_code.addAll(new IntArrayList(code, code.length));
        new_code.add(WAM.make_instruction(WAM.DYNAMIC_CODE_END, new_code.size())); // add and shove backwards
        new_code.data[new_code.data.length - 1] = new_code.removeLast();
        wam.getCodeAreas().set(p[1], new_code.data);
        //System.out.println(WAMToString.programToString(new_code.intdata, 0, new_code.size(), wam.regStart(), wam.getStringContainer(), wam.getNums()));
    }

    public int dynamic_retract(int nr, int nrEntries, int functor) {
        int[] p = wam.getCallFunction().getObj(functor); // Get the relevant code point
        dirty_functors.add(functor);
        int[] current = wam.getCodeAreas().get(p[1]);
//		System.out.println(WAMToString.programToString(current, 0, current.length, wam.regStart(), wam.getStringContainer(), wam.getNums()));
//		System.out.println("$$$$$$$$$$$$");
        int size = WAM.instruction_arg(current[current.length - 1]); // -1 for deleting end of code instruction
        IntArrayList new_code = new IntArrayList(current, size);
        //d_try/d_retry: eerste arg is eigen code, tweede arg is modificatie voor volgende code
        if (nrEntries == 1) {
            current[p[0]] = WAM.make_instruction(WAM.FAIL, 0);
            p[0] = -1; // Only one entry which is removed, so p[0] points to -1 
        } else {
            int own_trt = p[0];
            int next_trt = p[0] + current[p[0] + 1];
            int last_trt = 0;
            int second_last_trt = 0;
            for (int i = 0; i < nr; i++) {
                second_last_trt = last_trt;
                last_trt = own_trt;
                own_trt = next_trt;
                next_trt += current[next_trt + 1];
            }
            int own_code = WAM.instruction_arg(current[own_trt]);
            own_code = own_trt + ((own_code & 1) > 0 ? (-(own_code >>> 1)) : (own_code >>> 1));
            current[own_code] = WAM.make_instruction(WAM.FAIL, 0); // Change own first instruction to fail.
            if (nr == 0) { // First in the series is removed
                current[own_trt] = WAM.make_instruction(WAM.D_RETRY, WAM.instruction_arg(current[own_trt]));// Change try to retry
                int next_code = WAM.instruction_arg(current[next_trt]); // Get the actual start of the next piece of code
                next_code = next_trt + ((next_code & 1) > 0 ? (-(next_code >>> 1)) : (next_code >>> 1));
                if (nrEntries == 2) { // There is only one entry left, so p[0] has to point to its actual start (not its trt)
                    p[0] = next_code; // Let p[0] point to it
                } else { // The next code is a retry, but has to become a try. However due to pointers to the retry instruction icw backtracking we have to add a new try instruction at the end
                    p[0] = new_code.size();
                    int arg = next_code - p[0];
                    arg = arg > 0 ? arg << 1 : (((-arg) << 1) | 1);
                    new_code.add(WAM.make_instruction(WAM.D_TRY, arg));
                    new_code.add((next_trt + current[next_trt + 1]) - p[0]);
                }
            } else if (nr == (nrEntries - 1)) { // Last entry is removed
                int last_code = WAM.instruction_arg(current[last_trt]);
                last_code = last_trt + ((last_code & 1) > 0 ? (-(last_code >>> 1)) : (last_code >>> 1));
                if (nr == 1) { // Second of two is removed
                    p[0] = last_code; // Now start to point to start of first entry code
                } else { // Last of |entries|>2 is removed
                    int arg = last_code - new_code.size();
                    arg = arg > 0 ? arg << 1 : (((-arg) << 1) | 1);
                    current[second_last_trt + 1] = new_code.size(); // Let second last trt point towards the new trust instruction
                    new_code.add(WAM.make_instruction(WAM.D_TRUST, arg)); // Make the new trust instruction
                    new_code.add(0);
                }
            } else { // A middle entry is removed
                current[last_trt + 1] = next_trt - last_trt; // Let the last trt point to the next trt
            }
        }
        // go to trt position
        // *get first real instruction, change it to fail
        // *if nr = 0, change it to retry
        // *if nr = 0 and |entries| = 2, point.x of fn goes to next item start
        // *if nr = 0 and |entries| = 0 p[0] is -1
        // *if nr = 0 and |entries| > 2, create new try instruction which points to next instructions, make point.x point to it
        // *if nr = |entries| and nr = 2, point.x goes to first item real start 
        // *if nr = |entries| and nr > 2, add trust instruction and make it point to second last instruction series, let the TRT of the third last entry point to the new trust instruction
        // *if nr < |entries| and nr > 0, make previous TRT instruction point to the next one
        // 
        new_code.add(WAM.make_instruction(WAM.DYNAMIC_CODE_END, new_code.size())); // add and shove backwards
        new_code.data[new_code.data.length - 1] = new_code.removeLast();
        wam.getCodeAreas().set(p[1], new_code.data);
//		System.out.println(WAMToString.programToString(new_code.intdata, 0, new_code.size(), wam.regStart(), wam.getStringContainer(), wam.getNums()));
        return 0;
    }

    private static int toArg(int x) {
        return x < 0 ? (((-x) << 1) | 1) : (x << 1);
    }

    /**
     * Put the query code of the last compiled query in the WAM.
     */
    public void loadQuery() {
        wam.setQuery(query_instructions);
    }

    /**
     * Add a heap to the reason base.
     *
     * @param functor Functor it belongs to
     * @param heap The heap containing the new entry.
     * @param front Whether to put it in front of the other entries
     */
    public void addHeap(int functor, int[] heap, boolean front) {
        if (heaps.get(functor) == null) {
            heaps.put(functor, new ArrayList<>());
        }
        if (front) {
            heaps.get(functor).add(0, heap);
        } else {
            heaps.get(functor).add(heap);
        }
    }

    /**
     * Add a heap to the reason base.
     *
     * @param functor Functor it belongs to
     * @param heap The heap containing the new entry.
     * @param front Whether to put it in front of the other entries
     */
    public void addInstructions(int functor, int[] instr, boolean front) {
        if (instructions.get(functor) == null) {
            instructions.put(functor, new ArrayList<>());
        }
        if (front) {
            instructions.get(functor).add(0, instr);
        } else {
            instructions.get(functor).add(instr);
        }
    }

    public HashMap<Integer, ArrayList<int[]>> getHeaps() {
        return heaps;
    }

    public HashMap<Integer, ArrayList<int[]>> getInstructions() {
        return instructions;
    }
    
    public ArrayList<int[]> instruction(int i) {
        return instructions.get(i);
    }

    public ArrayList<int[]> getAreas() {
        return areas;
    }

    public HashMap<Integer, Integer> getCallStarts() {
        return call_starts;
    }

    public void setQueryInstructions(int[] q) {
        query_instructions = q;
    }

    public int[] getQueryInstructions() {
        return query_instructions;
    }

    public void setQueryHeap(int[] q) {
        query_heap = q;
    }

    public int[] getQueryHeap() {
        return query_heap;
    }

    public WAM getWAM() {
        return wam;
    }

    public HashMap<Integer, String> getQueryVars() {
        return query_vars;
    }

    public void setQueryVars(HashMap<Integer, String> qv) {
        query_vars = qv;
    }

    public void addDynamic(String s) {
        if (!dynamics.contains(s)) {
            dynamics.add(s);
        }
    }

    public boolean isDynamic(int functor) {
        return dynamics.contains(wam.string(functor));
    }

    public boolean isDynamic(String functor) {
        return dynamics.contains(functor);
    }

    public ArrayList<int[]> heap(int fn) {
        return heaps.get(fn);
    }
    
    
    public static class Rule { /* TODO */ }
    
    public Iterator<Rule> getRules() {
        //TODO wrap rules in an accessible object
        return null;
    }

    public String toString(Strings strings, Numbers nums, int registerstart) {
        String r = "";
        for (Integer key : call_starts.keySet()) {							// For each functor with rules
            r += strings.get(key) + ":\r\n";					// Get its name
            int[] instr = areas.get(call_starts.get(key));
            ArrayList<int[]> heap = heaps.get(key);
            for (int i = 0; i < heap.size(); i++) {					// Show each of the entered heaps
                int[] h = heap.get(i);
                r += '\t' + WAMToString.termToString(h, h.length - 1, strings, nums, true) + "\r\n";
            }
            r += '\t' + "Instructions:\r\n";
            r += "\t\t" + WAMToString.programToString(instr, 0, instr.length, registerstart, strings, nums).replaceAll("\r\n", "\r\n\t\t") + "\r\n";
        }
        if (query_instructions != null) {
            String q = WAMToString.termToString(query_heap, query_heap.length - 1, strings, nums, true);
            q = q.substring(3, q.length() - 1);
            r += "Query: " + q + "\r\n";
            r += '\t' + "Instructions:\r\n";
            r += "\t\t" + WAMToString.programToString(query_instructions, 0, query_instructions.length, registerstart, strings, nums).replaceAll("\r\n", "\r\n\t\t") + "\r\n";
        }
        return r;
    }
}
