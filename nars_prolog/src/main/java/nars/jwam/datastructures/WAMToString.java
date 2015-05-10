package nars.jwam.datastructures;

import nars.jwam.WAM;
import nars.jwam.compiler.Token;

/**
 * This class groups together functionality for transforming WAM constructs to
 * String objects.
 *
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class WAMToString {

    /**
     * Convert a program (WAM instructions) to a String.
     *
     * @param program The instructions.
     * @param start Start index in the array of instructions.
     * @param end End index (inclusive) until where instructions must be
     * outputted.
     * @param registerstart Index in the main WAM memory where argument
     * registers start (used for correct argument numbering).
     * @param strings The integer to String conversion table.
     * @param nums The integer to number conversion table.
     * @return A String representation of a piece of WAM instructions.
     */
    public static String programToString(int[] program, int start, int end, int registerstart, Strings strings, Numbers nums) {
        String r = "";
        int c = start;
        while (c != end) {
            r += c + ":\t" + instructionToString(program, c, registerstart, strings, nums) + "\r\n";
            c += instruction_size(program, c);
        }
        return r;
    }


    /**
     * Convert a term on the heap to a String.
     *
     * @param heap The heap data.
     * @param address The address where the term is stored.
     * @param strings The integer to String conversion table.
     * @param nums The integer to number conversion table.
     * @param no_bindings True if variables in the term might be bound, give
     * false otherwise.
     * @return A string representation of a WAM term.
     */
    public static String termToString(int[] heap, int address, Strings strings, Numbers nums, boolean no_bindings) {
        if (!no_bindings) {
            address = deref(address, heap);
        }
        int tag = WAM.cell_tag(heap[address]);
        int value = WAM.cell_value(heap[address]);
        if (tag == WAM.REF) {
            return value == 0 ? "_" : "V" + value;
        } else if (tag == WAM.STR) {
            int a = value;
            value = WAM.cell_value(heap[a]);
            int args = WAM.numArgs(value);
            String r = "" + strings.get(value);
            r = r.substring(0, r.lastIndexOf('/'));
            if (args > 0) {
                r += "(";
                for (int i = 1; i <= args; i++) {
                    r += i > 1 ? "," : "";
                    r += termToString(heap, a + i, strings, nums, no_bindings);
                }
                r += ")";
            }
            return r;
        } else if (tag == WAM.CON) {
            String r = "" + strings.get(value << 7);
            return r.substring(0, r.lastIndexOf('/'));
        } else if (tag == WAM.NUM) {
            return nums.numToString(value);
        } else if (tag == WAM.LIS) {
            String r = "[";
            r += termToString(heap, value, strings, nums, no_bindings);
            boolean add_comma = true;
            if (WAM.cell_tag(heap[value + 1]) == WAM.REF) {
                r += "|";
                add_comma = false;
            }
            String second = termToString(heap, value + 1, strings, nums, no_bindings);
            if (second.charAt(0) == '[') {
                second = second.substring(1, second.length() - 1);
            }
            if (!second.isEmpty()) {
                r += (add_comma ? "," : "") + second;
            }
            return r + ']';
        }
        return "none";
    }


    /**
     * Convert a single instruction to a String.
     *
     * @param program The instruction array.
     * @param c The index of the to-be-converted instruction.
     * @param registerstart Index in the main WAM memory where argument
     * registers start (used for correct argument numbering).
     * @param strings The integer to String conversion table.
     * @param nums The integer to number conversion table.
     * @return A String representation of a WAM instruction.
     */
    public static String instructionToString(int[] program, int c, int registerstart, Strings strings, Numbers nums) {
        int instruction = WAM.instruction_code(program[c]);
        int argument = WAM.instruction_arg(program[c]);
        String r = "";
        switch (instruction) {
            case WAM.GET_STR:
                return "get_structure " + strings.get(program[c + 1]) + ",X" + (argument - registerstart);
            case WAM.PUT_STR:
                return "put_structure " + strings.get(program[c + 1]) + ",X" + (argument - registerstart);
            case WAM.SET_VAR:
                return "set_variable X" + (argument - registerstart);
            case WAM.SET_VAL:
                return "set_value X" + (argument - registerstart);
            case WAM.UNI_VAR:
                return "unify_variable X" + (argument - registerstart);
            case WAM.UNI_VAL:
                return "unify_value X" + (argument - registerstart);
            case WAM.CALL:
                return "call " + strings.get(program[c + 1]) + ' ' + argument;
            case WAM.CALL_VAR:
                return "call X" + (program[c + 1] - registerstart) + ' ' + argument;
            case WAM.CALL_VAR_PERM:
                return "call Y" + program[c + 1] + ' ' + argument;
            case WAM.EXECUTE:
                return "execute " + strings.get(program[c + 1]);
            case WAM.EXECUTE_VAR:
                return "execute X" + (argument - registerstart);
            case WAM.PROCEED:
                return "proceed";
            case WAM.GET_VAR:
                return "get_variable X" + (argument - registerstart) + ",X" + (program[c + 1] - registerstart);
            case WAM.GET_VAL:
                return "get_value X" + (argument - registerstart) + ",X" + (program[c + 1] - registerstart);
            case WAM.PUT_VAR:
                return "put_variable X" + (argument - registerstart) + ",X" + (program[c + 1] - registerstart);
            case WAM.PUT_VAL:
                return "put_value X" + (argument - registerstart) + ",X" + (program[c + 1] - registerstart);
            case WAM.GET_VAR_PERM:
                return "get_variable_perm Y" + argument + ",X" + (program[c + 1] - registerstart);
            case WAM.PUT_VAR_PERM:
                return "put_variable_perm Y" + argument + ",X" + (program[c + 1] - registerstart);
            case WAM.SET_VAR_PERM:
                return "set_variable_perm Y" + argument;
            case WAM.SET_VAL_PERM:
                return "set_value_perm Y" + argument;
            case WAM.UNI_VAR_PERM:
                return "unify_variable_perm Y" + argument;
            case WAM.UNI_VAL_PERM:
                return "unify_value_perm Y" + argument;
            case WAM.GET_VAL_PERM:
                return "get_variable_perm Y" + argument + ",X" + (program[c + 1] - registerstart);
            case WAM.PUT_VAL_PERM:
                return "put_value_perm Y" + argument + ",X" + (program[c + 1] - registerstart);
            case WAM.PUT_UNSAFE_VALUE:
                return "put_unsafe_value Y" + argument + ",X" + (program[c + 1] - registerstart);
            case WAM.SET_LOCAL_VAL:
                return "set_local_value X" + (argument - registerstart);
            case WAM.UNI_LOCAL_VAL:
                return "unify_local_value X" + (argument - registerstart);
            case WAM.SET_LOCAL_VAL_PERM:
                return "set_local_value_perm Y" + argument;
            case WAM.UNI_LOCAL_VAL_PERM:
                return "unify_local_value_perm Y" + argument;
            case WAM.ALLOCATE:
                return "allocate";
            case WAM.DEALLOCATE:
                return "deallocate";
            case WAM.TRY_ME_ELSE:
                return "try_me_else " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1));
            case WAM.RETRY_ME_ELSE:
                return "retry_me_else " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1));
            case WAM.TRUST_ME:
                return "trust_me";
            case WAM.PUT_CONSTANT:
                return "put_constant " + strings.get(argument << 7) + ", X" + (program[c + 1] - registerstart);
            case WAM.GET_CONSTANT:
                return "get_constant " + strings.get(argument << 7) + ", X" + (program[c + 1] - registerstart);
            case WAM.SET_CONSTANT:
                return "set_constant " + strings.get(argument << 7);
            case WAM.UNIFY_CONSTANT:
                return "unify_constant " + strings.get(argument << 7);
            case WAM.PUT_NUM:
                return "put_num " + numToString(argument, strings, nums) + ", X" + (program[c + 1] - registerstart);
            case WAM.GET_NUM:
                return "get_num " + numToString(argument, strings, nums) + ", X" + (program[c + 1] - registerstart);
            case WAM.SET_NUM:
                return "set_num " + numToString(argument, strings, nums);
            case WAM.UNI_NUM:
                return "unify_num " + numToString(argument, strings, nums);
            case WAM.PUT_LIST:
                return "put_list X" + (argument - registerstart);
            case WAM.GET_LIST:
                return "get_list X" + (argument - registerstart);
            case WAM.SET_VOID:
                return "set_void " + argument;
            case WAM.UNIFY_VOID:
                return "unify_void " + argument;
            case WAM.PUT_VOID:
                return "put_void A" + (argument - registerstart) + ' ' + program[c + 1];
            case WAM.END_OF_QUERY:
                return "end of query";
            case WAM.TRY:
                return "try " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1));
            case WAM.RETRY:
                return "retry " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1));
            case WAM.TRUST:
                return "trust " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1));
            case WAM.D_TRY:
                return "d_try " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1)) + ' ' + program[c + 1];
            case WAM.D_RETRY:
                return "d_retry " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1)) + ' ' + program[c + 1];
            case WAM.D_TRUST:
                return "d_trust " + ((argument & 1) > 0 ? -(argument >>> 1) : (argument >>> 1));
            case WAM.DEEP_CUT:
                return "cut Y" + argument;
            case WAM.NECK_CUT:
                return "neck_cut";
            case WAM.GET_LEVEL:
                return "get_level Y" + argument;
            case WAM.SWITCH_CON:
                return "switch_on_constant " + hashmapToString(c, argument, program, strings, nums, false);
            case WAM.SWITCH_NUM:
                return "switch_on_num " + hashmapToString(c, argument, program, strings, nums, true);
            case WAM.SWITCH_STR:
                return "switch_on_structure " + hashmapToString(c, argument, program, strings, nums, false);
            case WAM.SWITCH_TERM:
                return "switch_on_term V:" + argument + " C:" + program[c + 1] + " L:" + program[c + 2] + " S:" + program[c + 3] + " N:" + program[c + 4];
            case WAM.BUILT_IN_BINARY:
                return "built_in_binary " + strings.get(program[c + 1]);
            case WAM.JUMP:
                return "jump " + argument;
            case WAM.JUMPLIST:
                return "jumplist " + jumpListToString(program, c, argument, strings, nums);
            case WAM.FAIL:
                return "fail";
            case WAM.RETRACT:
                return "retract " + argument;
        }
        return "null";
    }

    /**
     * Get the amount of integers that an instruction takes to be stored.
     *
     * @param program An instruction array.
     * @param c The index of the instruction.
     * @return The number of integers it takes to store the instruction.
     */
    public static int instruction_size(int[] program, int c) {
        int instruction = WAM.instruction_code(program[c]);
        int argument = WAM.instruction_arg(program[c]);
        switch (instruction) {
            case WAM.GET_STR:
                return 2;
            case WAM.PUT_STR:
                return 2;
            case WAM.SET_VAR:
                return 1;
            case WAM.SET_VAL:
                return 1;
            case WAM.UNI_VAR:
                return 1;
            case WAM.UNI_VAL:
                return 1;
            case WAM.CALL:
                return 2;
            case WAM.CALL_VAR:
                return 2;
            case WAM.CALL_VAR_PERM:
                return 2;
            case WAM.EXECUTE:
                return 2;
            case WAM.EXECUTE_VAR:
                return 1;
            case WAM.PROCEED:
                return 1;
            case WAM.GET_VAR:
                return 2;
            case WAM.GET_VAL:
                return 2;
            case WAM.PUT_VAR:
                return 2;
            case WAM.PUT_VAL:
                return 2;
            case WAM.GET_VAR_PERM:
                return 2;
            case WAM.PUT_VAR_PERM:
                return 2;
            case WAM.SET_VAR_PERM:
                return 1;
            case WAM.SET_VAL_PERM:
                return 1;
            case WAM.UNI_VAR_PERM:
                return 1;
            case WAM.UNI_VAL_PERM:
                return 1;
            case WAM.GET_VAL_PERM:
                return 2;
            case WAM.PUT_VAL_PERM:
                return 2;
            case WAM.PUT_UNSAFE_VALUE:
                return 2;
            case WAM.SET_LOCAL_VAL:
                return 1;
            case WAM.UNI_LOCAL_VAL:
                return 1;
            case WAM.SET_LOCAL_VAL_PERM:
                return 1;
            case WAM.UNI_LOCAL_VAL_PERM:
                return 1;
            case WAM.ALLOCATE:
                return 1;
            case WAM.DEALLOCATE:
                return 1;
            case WAM.TRY_ME_ELSE:
                return 1;
            case WAM.RETRY_ME_ELSE:
                return 1;
            case WAM.TRUST_ME:
                return 1;
            case WAM.PUT_CONSTANT:
                return 2;
            case WAM.GET_CONSTANT:
                return 2;
            case WAM.SET_CONSTANT:
                return 1;
            case WAM.UNIFY_CONSTANT:
                return 1;
            case WAM.PUT_NUM:
                return 2;
            case WAM.GET_NUM:
                return 2;
            case WAM.SET_NUM:
                return 1;
            case WAM.UNI_NUM:
                return 1;
            case WAM.PUT_LIST:
                return 1;
            case WAM.GET_LIST:
                return 1;
            case WAM.SET_VOID:
                return 1;
            case WAM.UNIFY_VOID:
                return 1;
            case WAM.PUT_VOID:
                return 2;
            case WAM.END_OF_QUERY:
                return 1;
            case WAM.TRY:
                return 1;
            case WAM.RETRY:
                return 1;
            case WAM.TRUST:
                return 1;
            case WAM.D_TRY:
                return 2;
            case WAM.D_RETRY:
                return 2;
            case WAM.D_TRUST:
                return 2; // d_trust reserves one extra int so it can be transformed to d_(re)try if needed
            case WAM.DEEP_CUT:
                return 1;
            case WAM.NECK_CUT:
                return 1;
            case WAM.GET_LEVEL:
                return 1;
            case WAM.SWITCH_CON:
                return hashmapSpace(program, c, argument);
            case WAM.SWITCH_NUM:
                return hashmapSpace(program, c, argument);
            case WAM.SWITCH_STR:
                return hashmapSpace(program, c, argument);
            case WAM.SWITCH_TERM:
                return 5;
            case WAM.BUILT_IN_BINARY:
                return 2;
            case WAM.JUMP:
                return 1;
            case WAM.JUMPLIST:
                return 1;
            case WAM.FAIL:
                return 1;
            case WAM.RETRACT:
                return 1;
        }
        return 1;
    }

    /**
     * Convert a stored hashmap to String.
     *
     * @param c The cursor of the hashmap.
     * @param entry_space The amount of possible entries in the hashmap.
     * @param source The source where the hashmap is stored.
     * @param strings The integer to String conversion table.
     * @param numbers The integer to number conversion table.
     * @param nums Whether the hashmap stores numbers.
     * @return A string representation of the hashmap.
     */
    public static String hashmapToString(int c, int entry_space, int[] source, Strings strings, Numbers numbers, boolean nums) {
        String r = "{";
        int collisions = 0;
        for (int i = 0; i <= entry_space; i++) {
            if (source[i + 1 + c] != Integer.MIN_VALUE) {
                int e = source[i + 1 + c];
                do {
                    r += '(' + (nums ? numToString(source[c + e + 2], strings, numbers) : strings.get(source[c + e + 2])) + ',' + source[c + e + 3] + ')';
                    e = source[c + e + 1];
                    collisions++;
                } while (e != Integer.MIN_VALUE);
                collisions--;
            }
        }
        return r + "} (" + collisions + " collisions)";
    }

    public static String jumpListToString(int[] source, int c, int start, Strings strings, Numbers nums) {
        String r = "";
        int list_cursor = c + start;
        while (list_cursor != Integer.MIN_VALUE) {
            r += "[";
            r += cellToString(source, list_cursor + 1, strings, nums);
            r += "," + source[list_cursor + 2];
            r += "]->";
            int oldc = list_cursor;
            list_cursor = source[list_cursor] == Integer.MIN_VALUE ? Integer.MIN_VALUE : (list_cursor + source[list_cursor]);
            source[oldc] = 0;
            source[oldc + 1] = 0;
            source[oldc + 2] = 0;
        }
        return r + 'X';
    }

    public static int hashmapSpace(int[] source, int c, int entry_space) {
        int r = 2 + entry_space;
        for (int i = 0; i <= entry_space; i++) {
            if (source[i + 1 + c] != Integer.MIN_VALUE) {
                int e = source[i + 1 + c];
                do {
                    r += 3;
                    e = source[c + e + 1];
                } while (e != Integer.MIN_VALUE);
            }
        }
        return r;
    }

    public static String oneLineHeap(int[] heap, int start, int end, Strings strings, Numbers nums) {
        String r = "";
        for (int i = start; i < end; i++) {
            r += i + ":" + cellToString(heap, i, strings, nums) + ' ';
        }
        return r;
    }

    public static String registeredTerm(int[] heap, Token[] tokens, int address, Strings strings, Numbers nums) {
        int tag = WAM.cell_tag(heap[address]);
        int value = WAM.cell_value(heap[address]);
        String regstr = (tokens[address].register1 != 0 || tokens[address].register2 != 0) ? "{R1:" + tokens[address].register1 + " R2:" + tokens[address].register2 + '}' : "";
        if (tag == WAM.REF) {
            return regstr + (value == 0 ? "_" : "V" + value);
        } else if (tag == WAM.STR) {
            int a = value;
            value = WAM.cell_value(heap[a]);
            int args = WAM.numArgs(value);
            String r = "" + strings.get(value);
            r = r.substring(0, r.lastIndexOf('/'));
            if (args > 0) {
                r += "(";
                for (int i = 1; i <= args; i++) {
                    r += i > 1 ? "," : "";
                    r += registeredTerm(heap, tokens, a + i, strings, nums);
                }
                r += ")";
            }
            return regstr + r;
        } else if (tag == WAM.CON) {
            String r = strings.get(value << 7);
            return regstr + r.substring(0, r.lastIndexOf('/'));
        } else if (tag == WAM.NUM) {
            return regstr + (((value & 4) > 0) ? "-" : "") + (value >>> 3); // TODO: floating point
        } else if (tag == WAM.LIS) {
            String r = "[";
            int a = WAM.cell_value(heap[value]);
            r += registeredTerm(heap, tokens, value, strings, nums);
            boolean add_comma = true;
            if (WAM.cell_tag(heap[value + 1]) == WAM.REF) {
                r += "|";
                add_comma = false;
            }
            String second = registeredTerm(heap, tokens, value + 1, strings, nums);
            if (second.charAt(0) == '[') {
                second = second.substring(1, second.length() - 1);
            }
            if (!second.isEmpty()) {
                r += (add_comma ? "," : "") + second;
            }
            return regstr + r + ']';
        }
        return "none";
    }

    public static String cellToString(int[] source, int c, Strings strings, Numbers nums) {
        String r = "<";
        int tag = WAM.cell_tag(source[c]);
        int value = WAM.cell_value(source[c]);
        switch (tag) {
            case WAM.STR:
                r += "STR," + value;
                break;
            case WAM.REF:
                r += "REF," + value;
                break;
            case WAM.PN:
                r += "PN," + strings.get(value);
                break;
            case WAM.CON:
                r += "CON," + strings.get(value << 7);
                break;
            case WAM.LIS:
                r += "LIS," + value;
                break;
            case WAM.NUM:
                r += "NUM," + numToString(value, strings, nums);
                break;
            case 0:
                r += "null";
                break;
        }
        return r + '>';
    }

    public static int deref(int address, int[] storage) {
        while (true) {
            int value = WAM.cell_value(storage[address]);                    // Get the cell value
            if (WAM.cell_tag(storage[address]) == WAM.REF && value != address) // If tag is REF and is not unbound...
            {
                address = value;
                continue;
            }
            return address;
        }
    }

    public static String numToString(int n, Strings strings, Numbers nums) {
        return nums.numToString(n);
    }
}
