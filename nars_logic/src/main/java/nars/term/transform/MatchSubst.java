package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.*;
import nars.util.data.random.XorShift1024StarRandom;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

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
        abstract public boolean match(Term f);

        @Override
        public int run(State f) {
            f.match = match(f.frame.term);
            return 1;
        }

        @Override
        public String toString() {
            return "MatchOp{}";
        }
    }

    public static final class MatchAtom extends MatchOp {
        public final AbstractAtomic a;

        public MatchAtom(AbstractAtomic a) {
            this.a = a;
        }

        @Override
        public boolean match(Term t) {
            return a.equals(t);
        }

        @Override
        public String toString() {
            return "MatchAtom{" +  a +  '}';
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
        public boolean match(Term t) {
            return (t.op() == type && t.size() == size);
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
        @Override public boolean match(Term t) {
            return ((Image)t).relationIndex == index;
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
    final static class Push implements PatternOp {
        public final int divisor;
        public final boolean permute;

        public Push(int divisor, boolean permute) {
            this.divisor = divisor;
            this.permute = permute;
        }

        @Override
        public int run(State f) {
            Term t = f.frame.term;
            if (!(t instanceof Compound)) {
                f.match = false; //requires compound to descend into
            }
            else {
                f.pushIn(permute);
            }
            return 1;
        }

        @Override
        public String toString() {
            return "Push{" +
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
            Frame ff = f.frame;
            ff.term = ff.parent.term(index);
            return 1;
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
            Frame ff = f.frame;
            ff.term = ff.term.term(ff.perm.get(index));
            return 1;
        }

        @Override
        public String toString() {
            return "SelectPermutedSubTerm{" +
                    "index=" + index +
                    '}';
        }
    }

    /** subsequence success, restore power minus what was consumed - prepare for pop */
    final static PatternOp Pop = new PatternOp() {
        @Override public final int run(State s) {
            s.popOut(); //up
            return 1;
        }

        @Override
        public String toString() {
            return "Pop{}";
        }
    };

    /** subsequence success, restore power minus what was consumed - prepare for pop */
    final static PatternOp EndAfterIfMatchSuccess = new PatternOp() {
        @Override public final int run(State f) {
            if (f.match) {
                f.onSuccess.accept(f);
            }
            return 0; //end
        }

        @Override
        public String toString() {
            return "End{}";
            //return "EndAfterIfMatchSuccess{}";
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

            final Frame ff = f.frame;
            final Term xSubst = f.resolve(var);

            if (xSubst != null) {
                ff.term = xSubst;
            }
            else {
                ff.putVarX(var, ff.term);
            }
            return 1;
        }

        @Override
        public String toString() {
            return "SubstItVar{" +  var + '}';
        }
    }

//    final static class SubstItTerm extends SubstIt {
//
//
//    }


    /** represents the "program" that the matcher will execute */
    public static class TermPattern {

        final PatternOp[] code;

        public TermPattern(Op type, Term pattern) {

            List<PatternOp> code = Global.newArrayList();

            //compile the code
            compile(type, pattern, code);
            code.add(EndAfterIfMatchSuccess);

            this.code = code.toArray(new PatternOp[code.size()]);
        }

        private void compile(Op type, Term t, List<PatternOp> code) {
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
                code.add(new Push(s, permute));

                //TODO 2-phase match
                int i = 0;
                for (Term x : c) {
                    if (!permute)
                        code.add(new SelectSubTerm(i));
                    else
                        code.add(new SelectPermutedSubTerm(i));

                    compile(type, x, code);

                    if (!permute)
                        code.add(IfFailEnd);
                    else
                        code.add(new IfFailRestoreAndMutate(savePoint));

                    i++;
                }
                code.add(Pop);
                return;
            }
            else if (t instanceof AbstractAtomic) {
                code.add(new MatchAtom((AbstractAtomic)t));
            }

            //code.add(new MatchIt(v));
        }

        @Override
        public String toString() {
            return "TermPattern{" + Arrays.toString(code) + '}';
        }
    }


    private final Random random;


    public static class Frame {

        /** X var -> Y term mapping */
        public final Map<Term, Term> xy;

        public Term term; //mutable

        public final Random rng;
        private boolean xyChanged = false;

        /** Y var -> X term mapping */
        public final Map<Term, Term> yx;
        private boolean yxChanged = false;

        ShuffleTermVector perm;
        public Compound parent;

        Frame(Random rng) {
            this.rng = rng;

            term = null;
            perm = null;
            xy = Global.newHashMap();
            yx = Global.newHashMap();
        }

        public Frame(Random rng, Term nonCompound) {
            this(rng);
            this.parent = null;
            this.term = nonCompound;
        }

        public Frame(Random rng, Compound parent) {
            this(rng);

            this.parent = parent;
        }

        public Frame(Frame frame, boolean permute) {
            this.parent = (Compound)frame.term;
            this.rng = frame.rng;
            xy = Global.newHashMap(frame.xy);
            yx = Global.newHashMap(frame.yx);
            if (permute) {
                perm = new ShuffleTermVector(frame.rng, term);
            }
            else {
                perm = null;
            }
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

        @Override
        public String toString() {
            return "{" + term + " in " + parent + ": " + xy + ", " + yx + '}';
        }
    }

    public static class State /* extends Frame? */ {

        /** match state */
        boolean match = true;

        final Deque<Frame> stack = new ArrayDeque();

        /** current frame */
        public Frame frame;

        final public Consumer<State> onSuccess;

        public State(Random rng, Term term, Consumer<State> onSuccess) {
            this.frame = new Frame(rng, term);
            this.onSuccess = onSuccess;
        }

        @Override
        public String toString() {
            return "State{" + stack.size() + ", " +
                    match +
                    ", frame=" + frame +
                    '}';
        }

        public void pushIn(boolean permute) {
            Frame innerFrame = new Frame(frame, permute);
            stack.push(frame);
            this.frame = innerFrame;
            System.out.println("\nPUSH  " + frame.term + " IN " + frame.parent + "\n");        }

        /** pops and returns the parent term */
        public final Frame popOut() {
            if (stack.isEmpty()) return null;
            Frame popped = stack.pop();

            frame.parent = (Compound) popped.parent;
            System.out.println("\nPOP  " + frame.term + " IN " + frame.parent + "\n");

            return popped;
        }

        /** returns true if there exist further permutations */
        public final boolean restoreAndPermuteNext() {
            //Frame inner = this.frame;
            Frame outer = popOut(); //previously pushed outer to restore to
//            if (inner.xyChanged)
//                outer.xy.clear(); outer.xy.putAll(inner.xy);
//            if (inner.yxChanged)
//                outer.yx.clear(); outer.yx.putAll(inner.yx);
            return outer.perm.hasNextThenNext();
        }

        public final Term resolve(Term xVar) {
            return frame.xy.get(xVar);
        }

    }

    public static final void next(Random rng, Op type, final Term pattern, final Term y, int power, Consumer<State> onSuccess) {
        TermPattern p = new TermPattern(type, pattern);
        System.out.println(pattern + "\n" + p);

        next(rng, p, y, power, onSuccess);
    }

    /** find substitutions, returning the success state.
     * this method should be used only from the outside.
     * all internal purposes should use the find() method
     * in order to manage decrease in power correctly */
    public static final boolean next(Random rng, final TermPattern x, final Term y, int power, Consumer<State> success) {

        State s = new State(rng, y, success);

        final PatternOp code[] = x.code;
        int ip = 0; //instruction pointer

        do {
            PatternOp o = code[ip];



            final int result = o.run(s);

            System.out.println(ip + "\t" + o + " -> " + result);
//            System.out.println("\t\t" + s);
//            System.out.println("\n");

            switch (result) {
                case 0: ip = -1; break; //failure
                case 1: ip++;  break;
                default: ip = -result;  break;
            }
        } while (ip >= 0);

        return s.match;
    }

    public static void main(String[] args) {
        MatchSubst.next(new XorShift1024StarRandom(1),
            Op.VAR_PATTERN,
            $("<%x --> y>"), $("<a --> y>"),
            100,
            (s) -> {
                System.out.println(s);
            }
        );
    }

}