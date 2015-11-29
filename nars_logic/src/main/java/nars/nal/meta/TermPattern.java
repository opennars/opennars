package nars.nal.meta;

import nars.Global;
import nars.Op;
import nars.nal.RuleMatch;
import nars.nal.nal4.Image;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.FindSubst;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.function.ToIntFunction;

/** represents the "program" that the matcher will execute */
public class TermPattern {

    public final PreCondition[] code;
    public final Term term;
    private Op type = null;

    public TermPattern(Op type, TaskBeliefPair pattern) {

        this.term = pattern;
        this.type = type;

        List<PreCondition> code = Global.newArrayList();



        //compile the code
        compile(pattern, code);


        if (code.get(0).toString().equals("TermOpEq{*}")) {
            code.remove(0);
        }
        if (code.get(0).toString().equals("TermSizeEq{2}")) {
            code.remove(0);
        }

        Op tt = pattern.term(0).op();
        if (tt != Op.VAR_PATTERN) {
            FindSubst.SubOpEquals taskType = new FindSubst.SubOpEquals(0, tt);
            code.add(0, taskType);
        }

        Op bt = pattern.term(1).op();
        if (bt != Op.VAR_PATTERN) {
            FindSubst.SubOpEquals beliefType = new FindSubst.SubOpEquals(1, bt);
            code.add(1, beliefType);
        }

        //code.add(End);
        code.add(new RuleMatch.Stage(RuleMatch.MatchStage.Post));

        this.code = code.toArray(new PreCondition[code.size()]);
    }

    public TermPattern(Op type, Term pattern) {

        this.term = pattern;
        this.type = type;

        List<PreCondition> code = Global.newArrayList();

        //compile the code
        compile(pattern, code);
        //code.add(End);
        code.add(new RuleMatch.Stage(RuleMatch.MatchStage.Post));

        this.code = code.toArray(new PreCondition[code.size()]);
    }

    private void compile(Term t, List<PreCondition> code) {


        boolean constant = false;

        if ((type == Op.VAR_PATTERN && (!Variable.hasPatternVariable(t)))) {
            constant = true;
        }
        if ((type != Op.VAR_PATTERN && !t.hasAny(type))) {
            constant = true;
        }

        if (!constant && (t instanceof Compound)) {
            compileCompound((Compound) t, code);
        }
        else {

            if (constant)
                code.add(new FindSubst.TermEquals(t));
            else {
                if (t.op() == type) {
                    code.add(new FindSubst.MatchXVar((Variable)t));
                }
                else {
                    //something else
                    code.add(new FindSubst.MatchTerm(t));
                }
            }
        }


        //throw new RuntimeException("unknown compile behavior for term: " + t);

        //code.add(new MatchIt(v));
    }

    private void compileCompound(Compound t, List<PreCondition> code) {
        Compound<?> c = t;
        int s = c.size();

        code.add(new FindSubst.TermOpEquals(c.op())); //interference with (task,belief) pair term

        //TODO varargs with greaterEqualSize etc
        code.add(new FindSubst.TermSizeEquals(c.size()));

        boolean permute = c.isCommutative() && (s > 1);

        switch (s) {
            case 0:
                //nothing to match
                break;

            case 1:
                code.add(new FindSubst.MatchTheSubterm(c.term(0)));
                break;

            default:

                if (c instanceof Image) {
                    code.add(new FindSubst.MatchImageIndex(((Image)c).relationIndex)); //TODO varargs with greaterEqualSize etc
                }

                code.add(new FindSubst.TermVolumeMin(c.volume()-1));

                code.add(new FindSubst.TermStructure(type, c.structure()));

                if (permute) {
                    code.add(new FindSubst.MatchPermute(c));
                }
                else {
                    compileNonCommutative(code, c);
                }

            break;
        }
    }


    /** heuristic for ordering comparison of subterms; lower is first */
    private ToIntFunction<Term> subtermPrioritizer = (t) -> {

        if (t.op() == type) {
            return 0;
        }
        else if (t instanceof Compound) {
            if (!t.isCommutative()) {
                return 1 + (1 * t.volume());
            } else {
                return 1 + (2 * t.volume());
            }
        }
        else {
            return 1; //atomic
        }
    };

    private void compileNonCommutative(List<PreCondition> code, Compound<?> c) {

        final int s = c.size();
        TreeSet<SubtermPosition> ss = new TreeSet();

        for (int i = 0; i < s; i++) {
            Term x = c.term(i);
            ss.add(new SubtermPosition(x, i, subtermPrioritizer));
        }

        code.add( FindSubst.Subterms );

        ss.forEach(sp -> { //iterate sorted
            Term x = sp.term;
            int i = sp.position;

            compile2(x, code, i);
            //compile(type, x, code);
        });

        code.add( FindSubst.Superterm );
    }

    private void compile2(Term x, List<PreCondition> code, int i) {
        //TODO this is a halfway there.
        //in order for this to work, parent terms need to be stored in a stack or something to return to, otherwise they get a nulll and it crashes:

//            code.add(new SelectSubterm(i));
//            compile(x, code);
//
         if (x instanceof Compound) {
//                //compileCompound((Compound)x, code);
//            /*}
//            else {
             code.add(new FindSubst.MatchSubterm(x, i));
         }
         else {
             //HACK this should be able to handle atomic subterms without a stack
             code.add(new FindSubst.SelectSubterm(i));
             compile(x, code);
         }

    }

    final static class SubtermPosition implements Comparable<SubtermPosition> {

        public final int score;
        public final Term term; //the subterm
        public final int position; //where it is located

        public SubtermPosition(Term term, int pos, ToIntFunction<Term> scorer) {
            this.term = term;
            this.position = pos;
            this.score = scorer.applyAsInt(term);
        }

        @Override
        public int compareTo(SubtermPosition o) {
            if (this == o) return 0;
            int p = Integer.compare(o.score, score); //lower first
            if (p!=0) return p;
            return Integer.compare(position, o.position);
        }

        @Override
        public String toString() {
            return term + " x " + score + " (" + position + ')';
        }
    }


    @Override
    public String toString() {
        return "TermPattern{" + Arrays.toString(code) + '}';
    }
}
