package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.CommonVariable;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;

import java.io.Serializable;
import java.util.*;

import static nars.$.$;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until a total solution is found.
the magnitude of a running integer depth metric ("power") serves
as a finite-time AIKR cutoff and its polarity as
returned indicates success value to the callee.  */
public class MatchSubst {


    private static final Map<Term,Term> newDefaultMap() {
        return Global.newHashMap(0);
    }

    public MatchSubst(Random random) {
        this.random = random;
    }

    /** pattern opcodes */
    interface PatternOp extends Serializable {
        /** return -1 to continue but specify an IP (invert the value),
         *  +1 to continue to next
         *  0 to fail
         */
        int run(State f);

//        default int run(State f) {
//            return 1;
//        }
    }

    public abstract static class MatchOp implements PatternOp {

        /** if match not successful, does not cause the execution to
         * terminate but instead sets the frame's match flag */
        abstract public boolean match(State f);

        @Override
        public int run(State f) {
            f.match = match(f);
            return 1;
        }

        @Override
        public String toString() {
            return "MatchOp{}";
        }
    }

    public static final class MatchCompound extends MatchOp {
        public final int size;
        public final Op type;

        public MatchCompound(Op type, int size) {
            this.size = size;
            this.type = type;
        }

        @Override
        public boolean match(State f) {
            return (f.term.op() == type && f.term.size() == size);
        }

        @Override
        public String toString() {
            return "MatchCompound{" +
                    "size=" + size +
                    ", type=" + type +
                    '}';
        }
    }
    public static final class MatchImageIndex extends MatchOp {
        public final int index;

        public MatchImageIndex(int index) {
            this.index = index;
        }
        @Override public boolean match(State f) {
            return ((Image)f.term).relationIndex == index;
        }

        @Override
        public String toString() {
            return "MatchImageIndex{" +
                    "index=" + index +
                    '}';
        }
    }

    /** save power, divide - prepare for descend into subterms of a matchable compound
     *  if insufficient power, terminate
     *  if permute, push restore state
     */
    final static class DescendPush implements PatternOp {
        public final int divisor;
        public final boolean permute;

        public DescendPush(int divisor, boolean permute) {
            this.divisor = divisor;
            this.permute = permute;
        }

        @Override
        public int run(State f) {
            f.parent = ((Compound)f.term);
            if (permute)
                f.save();
            return 1;
        }

        @Override
        public String toString() {
            return "DescendPush{" +
                    "divisor=" + divisor +
                    ", permute=" + permute +
                    '}';
        }
    }

    /** select subterm i */
    final static class SelectSubTerm implements PatternOp {
        public final int index;

        public SelectSubTerm(int index) {
            this.index = index;
        }

        @Override
        public final int run(State f) {
            f.term = f.parent.term(index);
            return 0;
        }

        @Override
        public String toString() {
            return "SelectSubTerm{" +
                    "index=" + index +
                    '}';
        }
    }
    /** select subterm i */
    final static class SelectPermutedSubTerm implements PatternOp {
        public final int index;

        public SelectPermutedSubTerm(int index) {
            this.index = index;
        }

        @Override
        public final int run(State f) {
            f.term = f.parent.term(f.frame.perm.get(index));
            return 0;
        }

        @Override
        public String toString() {
            return "SelectPermutedSubTerm{" +
                    "index=" + index +
                    '}';
        }
    }

    /** subsequence success, restore power minus what was consumed - prepare for pop */
    final static PatternOp SubSuccess = new PatternOp() {
        @Override public final int run(State f) {
            f.term = f.parent; //up
            return 1;
        }

        @Override
        public String toString() {
            return "SubSuccess{}";
        }
    };

    /** if failed to match, terminate */
    final static PatternOp IfFailEnd = new PatternOp() {
        @Override public int run(State f) {
            return (f.match ? 1 : 0);
        }
        @Override
        public String toString() {
            return "IfFailEnd{}";
        }
    };

    /** if failure, restore last save and permute. if no further permutations, terminate */
    final static class IfFailRestoreAndMutate implements PatternOp {
        public final int returnTo; //program instruction pointer to return to

        public IfFailRestoreAndMutate(int returnTo) {
            this.returnTo = returnTo;
        }
        @Override public int run(State f) {
            if (!f.match) {
                if (!f.restoreAndPermuteNext()) {
                    return 0;
                }
            }
            f.match = true;
            return 1;
        }

        @Override
        public String toString() {
            return "IfFailRestoreAndMutate{" +
                    "returnTo=" + returnTo +
                    '}';
        }
    }

    final static class SubstItVar implements PatternOp {
        public final Variable var;

        public SubstItVar(Variable v) {
            this.var = v;
        }
        @Override public int run(State f) {

            Term xSubst = f.resolve(var);
            if (xSubst != null) {
                f.term = xSubst;
            }
            else {
                //final Op xOp = var.op();
                f.frame.putVarX(var, xSubst);
            }
            return 1;
        }

        @Override
        public String toString() {
            return "SubstItVar{" +
                    "var=" + var +
                    '}';
        }
    }

//    final static class SubstItTerm extends SubstIt {
//
//
//    }


    /** represents the "program" that the matcher will execute */
    public static class TermPattern {
        final Op type;
        final PatternOp[] code;

        public TermPattern(Op type, Term pattern) {
            this.type = type;

            List<PatternOp> code = Global.newArrayList();

            //compile the code
            compile(pattern, code);

            this.code = code.toArray(new PatternOp[code.size()]);
        }

        private void compile(Term t, List<PatternOp> code) {
            if (t instanceof Variable) {
                Variable v = (Variable)t;
                if (t.op() == type) {
                    code.add(new SubstItVar(v));
                    return;
                }
            } else if (t instanceof Compound) {
                Compound<?> c = (Compound)t;

                int s = c.size();
                code.add(new MatchCompound(c.op(), s)); //TODO varargs with greaterEqualSize etc
                if (c instanceof Image) {
                    code.add(new MatchImageIndex(((Image)c).relationIndex)); //TODO varargs with greaterEqualSize etc
                }

                boolean permute = c.isCommutative();

                int savePoint = code.size();
                code.add(new DescendPush(s, permute));

                //TODO 2-phase match
                int i = 0;
                for (Term x : c) {
                    if (!permute)
                        code.add(new SelectSubTerm(i));
                    else
                        code.add(new SelectPermutedSubTerm(i));

                    compile(x, code);

                    if (!permute)
                        code.add(IfFailEnd);
                    else
                        code.add(new IfFailRestoreAndMutate(savePoint));

                    i++;
                }
                code.add(SubSuccess);
                return;
            }

            //code.add(new MatchIt(v));
        }

        @Override
        public String toString() {
            return "TermPattern{" + type +
                    ", code=" + Arrays.toString(code) +
                    '}';
        }
    }


    private final Random random;


    public static class Frame {

        /** X var -> Y term mapping */
        public final Map<Term, Term> xy;
        private boolean xyChanged = false;

        /** Y var -> X term mapping */
        public final Map<Term, Term> yx;
        private boolean yxChanged = false;

        ShuffleTermVector perm;

        public Frame() {
            perm = null;
            xy = Global.newHashMap();
            yx = Global.newHashMap();
        }

        public Frame(Frame frame) {
            perm = frame.perm;
            xy = Global.newHashMap(frame.xy);
            yx = Global.newHashMap(frame.yx);
        }


        private final void putVarX(final Term /* variable */ x, final Term y) {
            xyPut(x, y);
            if (x instanceof CommonVariable) {
                yxPut(x, y);
            }
        }


        private void putCommon(final Variable x, final Variable y) {
            final Variable commonVar = CommonVariable.make(x, y);
            xyPut(x, commonVar);
            yxPut(y, commonVar);
            //return true;
        }

        private final void yxPut(Term y, Term x) {
            yxChanged|= (yx.put(y, x)!=x);
        }

        private final void xyPut(Term x, Term y) {
            xyChanged|= (xy.put(x, y)!=y);
        }


    }

    public static class State {

        /** match state */
        boolean match = true;

        final Deque<Frame> stack = new ArrayDeque();

        /** current frame */
        Frame frame = new Frame();

        /** current term */
        public Term term;

        /** current parent compound */
        public Compound parent;

        public void save() {
            stack.push(new Frame(frame));
        }

        /** returns true if there exist further permutations */
        public boolean restoreAndPermuteNext() {
            Frame popped = stack.pop();
            frame.xy.clear(); frame.xy.putAll(popped.xy);
            frame.yx.clear(); frame.yx.putAll(popped.yx);
            return ((frame.perm = popped.perm).hasNextThenNext());
        }

        public final Term resolve(Term xVar) {
            return frame.xy.get(xVar);
        }

    }

    public static final boolean next(Op type, final Term pattern, final Term y, int power) {
        TermPattern p = new TermPattern(type, pattern);
        System.out.println(pattern + "\n" + p);
        return next(p, y, power);
    }

    /** find substitutions, returning the success state.
     * this method should be used only from the outside.
     * all internal purposes should use the find() method
     * in order to manage decrease in power correctly */
    public static final boolean next(final TermPattern x, final Term y, int power /*, Consumer<MatchSubst> onMatch*/) {

        final PatternOp code[] = x.code;
        int ip = 0; //instruction pointer

        State frame = new State();

        do {
            PatternOp o = code[ip];
            int result = o.run(frame);
            switch (result) {
                case 0: ip = -1; //failure
                case 1: ip++;
                default: ip = -result;
            }
        } while (ip >= 0);

        return frame.match;
    }

    public static void main(String[] args) {
        MatchSubst.next(Op.VAR_PATTERN, $("<%x --> y>"), $("<a --> y>"), 100);
    }

}