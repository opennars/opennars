package nars.jwam.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import nars.jwam.WAM;
import nars.jwam.compiler.Compiler;
import nars.jwam.datastructures.IntArrayList;

public class PredicateDynamics {

    private int ASSERT, ASSERTA, ASSERTZ, RETRACT, RETRACTALL;
    private final WAM wam;
    private final HashSet<Integer> covered_built_ins;
    private final IntArrayList dclist;

    public PredicateDynamics(WAM wam) {
        this.wam = wam;
        dclist = new IntArrayList();
        covered_built_ins = new HashSet<Integer>();

        make_built_ins_known();        
    }

    public void make_built_ins_known() {
        covered_built_ins.clear();
        covered_built_ins.add(ASSERT = wam.strings().add("assert", 1));
        covered_built_ins.add(ASSERTA = wam.strings().add("asserta", 1));
        covered_built_ins.add(ASSERTZ = wam.strings().add("assertz", 1));
        covered_built_ins.add(RETRACT = wam.strings().add("retract", 1));
        covered_built_ins.add(RETRACTALL = wam.strings().add("retractall", 1));
        wam.getCompiler().getSingleClauseCompiler().getBuiltIns().addAll(covered_built_ins);
    }

    public boolean canHandle(int op) {
        return covered_built_ins.contains(op);
    }

    public boolean handleOperator(int op) {
        if (op == ASSERTA) {
            return assertion(true);
        } else if (op == ASSERTZ || op == ASSERT) {
            return assertion(false);
        } else if (op == RETRACT) {
            return retract(false);
        } else if (op == RETRACTALL) {
            return retract(true);
        }
        return false;
    }

    private boolean assertion(boolean front) {
        int[] heap = deep_copy(wam.getStorage(), wam.getA1()); // deep copy heap, temp nums become dynamic nums, (:-(a,b),c) becomes :-(a,b,c) 
        int[] instructions = wam.getCompiler().getSingleClauseCompiler().compile_heap(heap, false, heap.length - 1, wam.rules()); // heap to instructions
        int fn = Compiler.getTopFN(heap, heap.length - 1, wam.strings());
        wam.rules().dynamic_assert(fn, instructions, heap, front); // trt update
        return true;
    }

    public boolean retract(boolean all) {
        int[] storage = wam.getStorage();
        int b = wam.getB();
        boolean is_retry = !all && (storage[b + storage[b] + 4] == wam.getP() && storage[b + storage[b] + 9] == wam.getCA()); // This retract is a retry if there exists a choice point that pointed at the same place as the execution at that time 
        if (is_retry) {
            wam.meta_retry(0, 0);
        }
        int fn = Compiler.getTopFN(wam.getStorage(), wam.getA1(), wam.strings());
        ArrayList<int[]> heaps = wam.rules().heap(fn);
        ArrayList<int[]> instr = wam.rules().instruction(fn);
        if (heaps == null) {
            return all; // entire predicate doesn't exist  
        }
        if (heaps.isEmpty() && is_retry) { // Retrying a retract with no more entries
            wam.trust_me(); // Remove choice point 
            return false;
        } else if (!heaps.isEmpty()) { // if there is an entry 
            int startTR = wam.getTR(); // Remember trail and heap top, the start values are used when making a choicepoint later on 
            int startH = wam.getH();
            int h;// = startH;
            int[] vars = new int[8]; // For copying, need to create right REF values
            int nr = is_retry ? storage[b + 2] : 0; // If it is a retry, continue where ended (note that many asserta's/assertz's can mess this up. It is a little imperfection I am willing to live with. I welcome any proper and fast assert/retract scheme.

            for (; nr < heaps.size(); nr++) { // For each entry 
                h = startH;
                int[] heap = heaps.get(nr);
                for (int i = 0; i < heap.length; i++) { // Add fact heap to heap
                    int cell = heap[i]; // Get the cell
                    int tag = WAM.cell_tag(heap[i]);
                    int v = WAM.cell_value(cell);
                    if (tag == WAM.REF) { // If REF, update the value
                        if (v > vars.length - 1) // Increased space needed
                        {
                            vars = Arrays.copyOf(vars, v * 2);
                        }
                        if (vars[v] == 0) // Var is first time encountered
                        {
                            vars[v] = h; // The ref cannot be at heap[0]
                        }
                        cell = WAM.newCell(WAM.REF, vars[v]); // Make new cell
                    } else if (tag == WAM.STR || tag == WAM.LIS) {
                        cell = WAM.newCell(tag, v + startH);
                    }
                    storage[h] = cell; // Store cell on heap
                    h++; // Update heap
                }
                wam.setH(h); // Update WAM's heap pointer 
//				System.out.println("RUN: "+nr);
//				System.out.println(WAMToString.oneLineHeap(wam.getStorage(), 0, wam.getH(), wam.getStringContainer(), wam.getNums()));
//				System.out.println("A1: "+WAMToString.cellToString(wam.getStorage(), wam.getA1(), wam.getStringContainer(), wam.getNums()));
                if (wam.unify(h - 1, wam.getA1())) { // Unify  
                    wam.rules().dynamic_retract(nr, heaps.size(), fn);// Adapt TRT and point.x
                    heaps.remove(nr); // TODO: clean number registration 
                    instr.remove(nr);
                    if (!all) { // There are more candidates to retract but it is not a retractall call
                        if (nr < heaps.size() - 1) { // Make/update choice point (can backtrack on retract) 
                            if (!is_retry) { // First time the retract is tried, but more entries to retract, so create choice point
                                wam.setNumOfArgs(2);// t o d o?: retract requires amount of perm variables to be stored see:: e>b? e+ --->instruction_arg(areas.get(cca)[cp-2])<--- +3:, EDIT: not needed because cca/cp nothing to do with current instruction :)
                                wam.setH(startH);
                                int tr = wam.getTR();
                                wam.setTR(startTR);
                                wam.meta_try(0, 0); // Creates choice point without moving instruction pointer 
                                wam.setH(h);
                                wam.setTR(tr);
                            }
                            storage[wam.getB() + 2] = nr; // Update choice point with where to continue
                        } else if (is_retry) { // Retried a retract but no more options so remove choice point
                            wam.trust_me(); // Remove choice point, done with this retract
                            wam.setP(wam.getP() - 1); // Trust_me adds to p but the binary_built_in instruction already moves p for retract
                        }
                        return true;
                    } else { // Undo all the unification bindings
                        nr--;
                        wam.unwind_trail(startTR, wam.getTR());
                        wam.setH(startH);
                        wam.setTR(startTR);
                    }
                } else { // If not successful: 
                    wam.unwind_trail(startTR, wam.getTR()); // Undo all the unification bindings
                    wam.setH(startH);
                    wam.setTR(startTR);
                }
            }
        }
        if (is_retry) {
            wam.trust_me(); // Remove choice point, there was one but no more retracts to do
        }
        return all;
    }

    // Temp nums become dynamic nums, (:-(a,b),c) becomes :-(a,b,c)
    private int[] deep_copy(int[] store, int cursor) {
        dclist.clear();
        int address = wam.deref(cursor);
        int tag = WAM.cell_tag(store[address]);
        if (tag == WAM.CON) {
            return new int[]{store[address]};
        } else if (tag == WAM.STR) {
            int pn_address = WAM.cell_value(store[address]);
            int pn = WAM.cell_value(store[pn_address]);
            if (wam.string(pn).startsWith(",/")) {
                if (WAM.cell_tag(store[pn_address + 1]) == WAM.STR) {
                    int pn2_address = WAM.cell_value(store[pn_address + 1]);
                    int pn2 = WAM.cell_value(store[pn2_address]);
                    if (wam.string(pn2).equals(":-/2")) {
                        IntArrayList children = new IntArrayList();
                        deep_copy_child(store, pn2_address + 1);
                        children.add(dclist.removeLast());
                        deep_copy_child(store, pn2_address + 2);
                        children.add(dclist.removeLast());
                        int args = WAM.numArgs(pn);
                        for (int i = 1; i < args; i++) {
                            deep_copy_child(store, pn_address + 1 + i);
                            children.add(dclist.removeLast());
                        }
                        int start = dclist.size();
                        dclist.add(WAM.newCell(WAM.PN, wam.strings().add(":-", args + 1)));
                        for (int i = 0; i < children.size(); i++) {
                            dclist.add(children.data[i]);
                        }
                        dclist.add(WAM.newCell(WAM.STR, start));
                    } else {
                        System.out.println("ERROR in PredicateDynamics.deepcopy 1");
                    }
                } else {
                    System.out.println("ERROR in PredicateDynamics.deepcopy 2");
                }
            } else {
                deep_copy_child(store, address);
            }
        }
        return Arrays.copyOf(dclist.data, dclist.size());
    }

    private void deep_copy_child(int[] store, int cursor) {
        int address = wam.deref(cursor);
        int tag = WAM.cell_tag(store[address]);
        int value = WAM.cell_value(store[address]);
        if (tag == WAM.REF) {
            dclist.add(WAM.newCell(WAM.REF, value + 1)); // Removes anonymous if accidently at heap[0] a ref is placed
        } else if (tag == WAM.CON) {
            dclist.add(store[address]);
        } else if (tag == WAM.NUM) {
            if ((value & 3) > 0) {
                dclist.add(WAM.newCell(WAM.NUM, wam.numbers().add_num(value)));
            } else {
                dclist.add(store[address]);
            }
        } else if (tag == WAM.LIS) {
            deep_copy_child(store, value);
            int c1 = dclist.removeLast();
            deep_copy_child(store, value + 1);
            int c2 = dclist.removeLast();
            int start = dclist.size();
            dclist.add(c1);
            dclist.add(c2);
            dclist.add(WAM.newCell(WAM.LIS, start));
        } else if (tag == WAM.STR) {
            int pn = WAM.cell_value(store[value]);
            int arg = WAM.numArgs(pn);
            IntArrayList children = new IntArrayList(new int[arg], 0);
            for (int i = 0; i < arg; i++) {
                deep_copy_child(store, value + i + 1);
                children.add(dclist.removeLast());
            }
            int start = dclist.size();
            dclist.add(WAM.newCell(WAM.PN, pn));
            for (int i = 0; i < children.size(); i++) {
                dclist.add(children.data[i]);
            }
            dclist.add(WAM.newCell(WAM.STR, start));
        }
    }
}
