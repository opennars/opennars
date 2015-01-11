package nars.jwam.builtins;

import java.util.HashSet;
import nars.jwam.WAM;

public class Arithmetic {

    private int IS, ARITHUNIFY, ARITHNUNIFY, LTEQ, ISIS, NEQ, NEQISIS, LT, GT, GTEQ;
    private int ADD, SUBTRACT, MULTIPLY, DIVIDE, INTDIVIDE, REMAINDER, MODULO, ABS, SIGN, INTEGERPART, FRACPART, FLOAT, FLOOR, CEIL, CEILING, TRUNCATE, ROUND, STARPOW, POW, SIN, COS, ATAN, EXP, LOG, LOG10, SQRT, SHIFTRIGHT, SHIFTLEFT, BITAND, BITOR, COMPLEMENT, MAX, MIN, INTEGER, XOR, TAN, ASIN, ACOS, PI, E;
    private WAM w = null;
    private HashSet<Integer> covered_built_ins;
    private boolean failed = false;
    private boolean evalIsSimple = true;

    public Arithmetic(WAM w) {
        this.w = w;
        make_built_ins_known();
    }

    public void make_built_ins_known() {
        HashSet<Integer> bi = new HashSet<Integer>();
        bi.add(IS = w.strings().add(" is ", 2));
        bi.add(ARITHUNIFY = w.strings().add("=:=", 2));
        bi.add(ARITHNUNIFY = w.strings().add("=\\=", 2));
        bi.add(LTEQ = w.strings().add("=<", 2));
        bi.add(NEQ = w.strings().add("\\=", 2));
        bi.add(NEQISIS = w.strings().add("\\==", 2));
        bi.add(LT = w.strings().add("<", 2));
        bi.add(GT = w.strings().add(">", 2));
        bi.add(GTEQ = w.strings().add(">=", 2));
        w.getCompiler().getSingleClauseCompiler().getBuiltIns().addAll(bi);
        covered_built_ins = bi;
        ADD = w.strings().add("+", 2);
        SUBTRACT = w.strings().add("-", 2);
        MULTIPLY = w.strings().add("*", 2);
        DIVIDE = w.strings().add("/", 2);
        INTDIVIDE = w.strings().add(" div ", 2);
        REMAINDER = w.strings().add(" rem ", 2);
        MODULO = w.strings().add(" mod ", 2);
        ABS = w.strings().add("abs", 1);
        SIGN = w.strings().add("sign", 1);
        INTEGERPART = w.strings().add("float_integer_part", 1);
        FRACPART = w.strings().add("float_fractional_part", 1);
        FLOAT = w.strings().add("float", 1);
        FLOOR = w.strings().add("floor", 1);
        CEIL = w.strings().add("ceil", 1);
        CEILING = w.strings().add("ceiling", 1);
        TRUNCATE = w.strings().add("truncate", 1);
        ROUND = w.strings().add("round", 1);
        STARPOW = w.strings().add("**", 2);
        POW = w.strings().add("^", 2);
        SIN = w.strings().add("sin", 1);
        COS = w.strings().add("cos", 1);
        ATAN = w.strings().add("atan", 1);
        EXP = w.strings().add("exp", 2);
        LOG = w.strings().add("log", 1);
        LOG10 = w.strings().add("log10", 1);
        SQRT = w.strings().add("sqrt", 1);
        SHIFTRIGHT = w.strings().add(">>", 2);
        SHIFTLEFT = w.strings().add("<<", 2);
        BITAND = w.strings().add("/\\", 2);
        BITOR = w.strings().add("\\/", 2);
        COMPLEMENT = w.strings().add("\\", 1);
        MAX = w.strings().add("max", 2);
        MIN = w.strings().add("min", 2);
        INTEGER = w.strings().add("integer", 1);
        XOR = w.strings().add(" xor ", 2);
        TAN = w.strings().add("tan", 1);
        ASIN = w.strings().add("asin", 1);
        ACOS = w.strings().add("acos", 1);
        PI = w.strings().add("pi", 0);
        E = w.strings().add("e", 0);
    }

    public boolean canHandle(int op) {
        return covered_built_ins.contains(op);
    }

    public boolean handleOperator(int op) {
        if (op == IS) {
            return is();
        } else if (op == LT || op == GT || op == GTEQ || op == LTEQ) {
            return numCompare(op);
        }
        return false;
    }

    public boolean is() {
        int[] heap = w.getStorage();
        int arg1address = w.deref(w.regStart() + 1);
        int tag1 = WAM.cell_tag(heap[arg1address]);
        if (tag1 != WAM.REF && tag1 != WAM.NUM) {
            return false;
        } else {
            int arg2address = w.deref(w.regStart() + 2);
            evalIsSimple = true;
            failed = false;
            double value = evaluate(arg2address);
            if (failed) {
                return false;
            }
            int num = 0;
            if (evalIsSimple) {
                int min = value < 0 ? 4 : 0;
                if (min > 0) {
                    value *= -1;
                }
                num = WAM.make_cell(WAM.NUM, (((int) value) << 3) | min);
            } else {
                num = WAM.make_cell(WAM.NUM, w.numbers().store_temp(value));
            }
            if (tag1 == WAM.REF) {
                heap[arg1address] = num;
                w.trail(arg1address);
                return true;
            } else if (tag1 == WAM.NUM) {
                return w.num_equality(WAM.cell_value(heap[arg1address]), WAM.cell_value(num));
            }
        }
        return false;
    }

    private double evaluate(int address) {
        int[] heap = w.getStorage();
        int tag = WAM.cell_tag(heap[address]);
        if (tag == WAM.NUM) {
            int num = WAM.cell_value(heap[address]);
            if ((num & 3) == 0) {
                return (((num & 4) > 0) ? -1 : 1) * (num >>> 3);
            } else {
                evalIsSimple = false;
                return w.numbers().getDouble(num);
            }
        } else if (tag == WAM.STR) { // TODO constants pi en e lijken mij niet goed te gaan... CON geeft error terug
            int new_address = WAM.cell_value(heap[address]);
            int functor = WAM.cell_value(heap[new_address]);
            int n = functor & 127;
            double arg1 = 0;
            double arg2 = 0;
            if (n > 0) {
                arg1 = evaluate(new_address + 1);
                if (failed) {
                    return 0;
                }
            }
            if (n > 1) {
                arg2 = evaluate(new_address + 2);
                if (failed) {
                    return 0;
                }
            }
            if (functor == ADD) {
                return arg1 + arg2;
            } else if (functor == SUBTRACT) {
                return arg1 - arg2;
            } else if (functor == MULTIPLY) {
                return arg1 * arg2;
            } else if (functor == DIVIDE) {
                return arg1 / arg2;
            }
        } else {
            failed = true;
        }
        return 0.0;
    }

    private boolean numCompare(int op) {
        int[] heap = w.getStorage();
        int arg1address = w.deref(w.regStart() + 1);
        int arg2address = w.deref(w.regStart() + 2);
        int tag1 = WAM.cell_tag(heap[arg1address]);
        int tag2 = WAM.cell_tag(heap[arg2address]);
        if (tag2 != WAM.NUM || tag1 != WAM.NUM) {
            return false;
        } else {
            failed = false;
            double value = evaluate(arg1address);
            if (failed) {
                return false;
            }
            double value2 = evaluate(arg2address);
            if (failed) {
                return false;
            }
            if (op == LT) {
                return value < value2;
            } else if (op == GT) {
                return value > value2;
            } else if (op == GTEQ) {
                return value >= value2;
            } else if (op == LTEQ) {
                return value <= value2;
            }
        }
        return false;
    }
}
