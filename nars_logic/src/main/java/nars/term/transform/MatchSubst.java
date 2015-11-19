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

            /* DEBUG */ if (!f.frame.match) throw new RuntimeException("should have terminated already");

            f.frame.match = match(f.frame.term);
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

        public Push(int divisor) {
            this.divisor = divisor;
        }

        @Override
        public int run(State f) {
            if (!f.frame.match) return 0; //terminate

            f.pushIn();
            return 1;
        }

        @Override
        public String toString() {
            return "Push{" +
                    "divisor=" + divisor +
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

            /* DEBUG */ if (!ff.parent.isCommutative()) throw new RuntimeException("only commutative");

            ff.term = ff.parent.term(ff.perm.get(index));
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

            return s.popOut()!=null ? 1 : 0;
        }

        @Override
        public String toString() {
            return "Pop{}";
        }
    };

    /** subsequence success, restore power minus what was consumed - prepare for pop */
    final static PatternOp EndAfterSuccessSupply = new PatternOp() {
        @Override public final int run(State f) {
            if (f.frame.match) {
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

    abstract public static class Breakable implements PatternOp {

        /** target where to break to */
        public int failTo; //TODO make final

    }

    /** if failed to match, terminate */
    final static class IfFailBreak extends Breakable {

        public IfFailBreak() {

        }

        @Override public int run(State f) {
            if (f.frame.match) return 1;
            return -failTo;
        }

        @Override public String toString() {
            return "IfFailBreak{}";
        }
    };

    /** if failure, restore last save and permute. if no further permutations, terminate */
    final static class IfFailPermute extends Breakable {

        public final int returnTo; //program instruction pointer to return to repeat permute


        public IfFailPermute(int returnTo) {
            this.returnTo = returnTo;
        }
        @Override public int run(State f) {
            if (!f.frame.match) {

                if (!f.restoreAndPermuteNext()) {
                    return -failTo;
                }
                else {
                    return -returnTo;
                }

            }

            return 1;
        }

        @Override
        public String toString() {
            return "IfFailPermute{}";
        }
    }

    final static class SaveAs implements PatternOp {
        public final Variable var;

        public SaveAs(Variable v) {
            this.var = v;
        }
        @Override public int run(State f) {

            final Frame ff = f.frame;
            final Term xSubst = f.resolve(var);

            if (xSubst != null) {
                if (!ff.term.equals(xSubst)) {
                    ff.match = false;
                }
            }
            else {
                ff.putVarX(var, ff.term);
            }
            return 1;
        }

        @Override
        public String toString() {
            return "SaveAs{" +  var + '}';
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
            code.add(EndAfterSuccessSupply);

            this.code = code.toArray(new PatternOp[code.size()]);
        }

        private void compile(Op type, Term t, List<PatternOp> code) {
            if (t instanceof Variable) {
                Variable v = (Variable)t;
                if (t.op() == type) {
                    code.add(new SaveAs(v));
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

                code.add(new Push(s));

                int savePoint = code.size(); //+1?

                //TODO 2-phase match
                List<Breakable> breakables = Global.newArrayList(s);

                int i = 0;
                for (Term x : c) {
                    if (!permute)
                        code.add(new SelectSubTerm(i));
                    else
                        code.add(new SelectPermutedSubTerm(i));

                    compile(type, x, code);


                    final Breakable b;
                    if (!permute) {
                        b = new IfFailBreak();
                    }
                    else {
                        b = new IfFailPermute(savePoint);
                    }
                    breakables.add(b);
                    code.add(b);

                    i++;
                }

                //exit: //TODO save this codepoint and replace in the above that reference
                int endCompound = code.size();
                code.add(Pop);

                breakables.forEach(b -> b.failTo = endCompound);

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

        boolean match = true;


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
        public int ip;

        Frame(Random rng) {
            this.rng = rng;

            term = null;
            perm = null;
            xy = Global.newHashMap();
            yx = Global.newHashMap();
        }

        public Frame(Random rng, Term root) {
            this(rng);
            this.parent = null;
            this.term = root;
        }

        Frame(Random rng, Compound parent, Map<Term, Term> xyToClone, Map<Term, Term> yxToClone, int ip) {
            this.rng = rng;
            this.xy = Global.newHashMap(xyToClone);
            this.yx = Global.newHashMap(yxToClone);
            this.ip = ip;

            init(parent);

        }

        final private void init(Compound parent) {
            this.parent = parent;
            if (parent!=null && parent.isCommutative()) {
                perm = new ShuffleTermVector(rng, parent);
            }
        }

        public Frame(Frame frame) {
            this(frame.rng, (Compound)frame.term, frame.xy, frame.yx, frame.ip);
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
            return "{" + term + " in " + parent + ": " + match + ", " + xy + ", " + yx + '}';
        }
    }

    public static class State /* extends Frame? */ {

        /** match state */

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
            return "State[" + stack.size() + "]" + frame;
        }

        public final void pushIn() {

            Frame outer = this.frame;
            stack.push(outer);
            this.frame = new Frame(outer);

            System.out.println("PUSH  ->" + outer.term + " IN " + outer.parent);
            System.out.println("PUSH  <-" + frame.term + " IN " + frame.parent + "\n");
        }

        /** pops and returns the parent term */
        public final Frame popOut() {
            //if (stack.isEmpty()) return null;
            Frame inner = this.frame;
            Frame outer = stack.pop();

//            //frame.perm = outer.perm;
//            frame.parent = (Compound) outer.term;
            if (!inner.match)
                outer.match = false; //propagate failure upwards
            else {
                outer.xy.clear();
                outer.xy.putAll(inner.xy);
                outer.yx.clear();
                outer.yx.putAll(inner.yx);
            }

            System.out.println("POP  <- " + inner.term + " IN " + inner.parent);
            System.out.println("POP  -> " + outer.term + " IN " + outer.parent + "\n");

            outer.ip = inner.ip;

            return this.frame = outer;
        }

        /** returns true if there exist further permutations */
        public final boolean restoreAndPermuteNext() {
            Frame previous = stack.getFirst(); //previously pushed outer to restore to

            final Frame f = this.frame;

            if (f.xyChanged) {
                f.xy.clear();
                f.xy.putAll(previous.xy);
                f.xyChanged = false;
            }
            if (f.yxChanged) {
                f.yx.clear();
                f.yx.putAll(previous.yx);
                f.yxChanged = false;
            }

            return f.match = f.perm.hasNextThenNext();
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
        int ip = s.frame.ip; //instruction pointer

        do {


            PatternOp o = code[ip];


            System.out.println(ip + "\t" + o);

            final int result = o.run(s);

            ip = s.frame.ip; //read ip

            System.out.println("\t\t" + s);

            switch (result) {
                case 0: ip = -1; break; //failure
                case 1: ip++;  break;
                default: {
                    if (result < 0)
                        ip = -result;
                    else
                        throw new RuntimeException("?");
                    break;
                }
            }

            s.frame.ip = ip; //store ip

        } while (ip >= 0);

        return s.frame.match;
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