package nars.nal.meta;

import nars.Global;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;

import java.util.Arrays;
import java.util.List;

/** represents the "program" that the matcher will execute */
public class TermPattern {

    public final PreCondition[] code;
    public final Term term;
    private Op type = null;

//    public TermPattern(Op type, TaskBeliefPair pattern) {
//
//        this.term = pattern;
//        this.type = type;
//
//        List<PreCondition> code = Global.newArrayList();
//
//
//
//        //compile the code
//        compile(pattern, code);
//
//
//        if (code.get(0).toString().equals("TermOpEq{*}")) {
//            code.remove(0);
//        }
//        if (code.get(0).toString().equals("TermSizeEq{2}")) {
//            code.remove(0);
//        }
//
//        Op tt = pattern.term(0).op();
//        if (tt != Op.VAR_PATTERN) {
//            FindSubst.SubOpEquals taskType = new FindSubst.SubOpEquals(0, tt);
//            code.add(0, taskType);
//        }
//
//        Op bt = pattern.term(1).op();
//        if (bt != Op.VAR_PATTERN) {
//            FindSubst.SubOpEquals beliefType = new FindSubst.SubOpEquals(1, bt);
//            code.add(1, beliefType);
//        }
//
//        //code.add(End);
//        code.add(new RuleMatch.Stage(RuleMatch.MatchStage.Post));
//
//        this.code = code.toArray(new PreCondition[code.size()]);
//    }

    public TermPattern(Op type, Term pattern) {

        this.term = pattern;
        this.type = type;

        List<PreCondition> code = Global.newArrayList();


        compile(pattern, code);

        //code.add(new RuleMatch.Stage(RuleMatch.MatchStage.Post));

        this.code = code.toArray(new PreCondition[code.size()]);
    }

    private void compile(Term x, List<PreCondition> code) {


        if (x instanceof TaskBeliefPair) {

            compileTaskBeliefPair((TaskBeliefPair)x, code);

        } else if (x instanceof Compound) {

            //compileCompound((Compound)x, code);
            code.add(new FindSubst.TermOpEquals(x.op())); //interference with (task,belief) pair term

            /*
            if (!Ellipsis.hasEllipsis((Compound)x)) {
                code.add(new FindSubst.TermSizeEquals(x.size()));
            }
            else {
                //TODO get a min bound for the term's size according to the ellipsis type
            }
            */
            code.add(new FindSubst.TermStructure(type, x.structure()));

            code.add(new FindSubst.TermVolumeMin(x.volume()-1));


            if (x instanceof Image) {
                code.add(new FindSubst.ImageIndexEquals(
                        ((Image)x).relationIndex)); //TODO varargs with greaterEqualSize etc
            }



            //if (!x.isCommutative() && Ellipsis.countEllipsisSubterms(x)==0) {
                //ACCELERATED MATCH allows folding of common prefix matches between rules



                //at this point we are certain that the compound itself should match
                //so we proceed with comparing subterms

                //TODO
                //compileCompoundSubterms((Compound)x, code);
            //}
            //else {
                //DEFAULT DYNAMIC MATCH (should work for anything)
                code.add(new FindSubst.MatchTerm(x));
            //}
            //code.add(new FindSubst.MatchCompound((Compound)x));

        } else {
            //an atomic term, use the general entry dynamic match point 'matchTerm'

//            if ((x.op() == type) && (!(x instanceof Ellipsis) /* HACK */)) {
//                code.add(new FindSubst.MatchXVar((Variable)x));
//            }
//            else {
//                //something else
            code.add(new FindSubst.MatchTerm(x));
//            }
        }

    }

    /** compiles a match for the subterms of an ordered, non-commutative compound */
    private void compileCompoundSubterms(Compound x, List<PreCondition> code) {

        //TODO
        //1. test equality. if equal, then skip past the remaining tests
        //code.add(FindSubst.TermEquals);

        code.add(FindSubst.Subterms);

        for (int i = 0; i < x.size(); i++)
            matchSubterm(x, i, code); //eventually this will be fully recursive and can compile not match

        code.add(new FindSubst.ParentTerm(x)); //return to parent/child state

    }

    private void compileTaskBeliefPair(TaskBeliefPair x, List<PreCondition> code) {
        //when derivation begins, frame's parent will be set to the TaskBeliefPair so that a Subterm code isnt necessary
        compileSubterm(x, 0, code);
        compileSubterm(x, 1, code);
        //code.add(FindSubst.Superterm); //
    }

    private void compileSubterm(Compound x, int i, List<PreCondition> code) {
        Term xi = x.term(i);
        code.add(new FindSubst.Subterm(i));
        compile(xi, code);
    }
    private void matchSubterm(Compound x, int i, List<PreCondition> code) {
        code.add(new FindSubst.Subterm(i));
        code.add(new FindSubst.MatchTerm(x.term(i)));
    }

//    private void compileCompound(Compound<?> x, List<PreCondition> code) {
//
//        int s = x.size();
//
//        /** whether any subterms are matchable variables */
//        final boolean constant = !Variable.hasPatternVariable(x);
//        final boolean vararg = constant ? Ellipsis.hasEllipsis(x) : false;
//
//        if (constant) { /*(type == Op.VAR_PATTERN && (*/
//
//            /** allow to compile the structure of the compound
//             *  match statically, including any optimization
//             *  possibilties that foreknowledge of the pattern
//             *  like we have here may provide
//             */
//            //compileConstantCompound(x, code);
//        } else {
//
//        }
//
//
//        code.add(new FindSubst.TermOpEquals(x.op())); //interference with (task,belief) pair term
//
//        //TODO varargs with greaterEqualSize etc
//        //code.add(new FindSubst.TermSizeEquals(c.size()));
//
//        //boolean permute = x.isCommutative() && (s > 1);
//
//        switch (s) {
//            case 0:
//                //nothing to match
//                break;
//
////            case 1:
////                code.add(new FindSubst.MatchTheSubterm(x.term(0)));
////                break;
//
//            default:
//
//                /*if (x instanceof Image) {
//                    code.add(new FindSubst.MatchImageIndex(((Image)x).relationIndex)); //TODO varargs with greaterEqualSize etc
//                }*/
//
//                //TODO this may only be safe if no var-args
//                //code.add(new FindSubst.TermVolumeMin(c.volume()-1));
//
//
//                code.add(new FindSubst.MatchCompound(x));
//
//
////                if (permute) {
////                    code.add(new FindSubst.MatchPermute(c));
////                }
////                else {
////                    compileNonCommutative(code, c);
////                }
//
//            break;
//        }
//    }


    /*private void compileConstantNonCommutiveCompound(Compound<?> x, List<PreCondition> code) {
        //TODO
    }*/


//

//    private void compileNonCommutative(List<PreCondition> code, Compound<?> c) {
//
//        final int s = c.size();
//        TreeSet<SubtermPosition> ss = new TreeSet();
//
//        for (int i = 0; i < s; i++) {
//            Term x = c.term(i);
//            ss.add(new SubtermPosition(x, i, subtermPrioritizer));
//        }
//
//        code.add( FindSubst.Subterms );
//
//        ss.forEach(sp -> { //iterate sorted
//            Term x = sp.term;
//            int i = sp.position;
//
//            compile2(x, code, i);
//            //compile(type, x, code);
//        });
//
//        code.add( FindSubst.Superterm );
//    }

//    private void compile2(Term x, List<PreCondition> code, int i) {
//        //TODO this is a halfway there.
//        //in order for this to work, parent terms need to be stored in a stack or something to return to, otherwise they get a nulll and it crashes:
//
////            code.add(new SelectSubterm(i));
////            compile(x, code);
////
//         if (x instanceof Compound) {
////                //compileCompound((Compound)x, code);
////            /*}
////            else {
//             code.add(new FindSubst.MatchSubterm(x, i));
//         }
//         else {
//             //HACK this should be able to handle atomic subterms without a stack
//             code.add(new FindSubst.SelectSubterm(i));
//             compile(x, code);
//         }
//
//    }

//    final static class SubtermPosition implements Comparable<SubtermPosition> {
//
//        public final int score;
//        public final Term term; //the subterm
//        public final int position; //where it is located
//
//        public SubtermPosition(Term term, int pos, ToIntFunction<Term> scorer) {
//            this.term = term;
//            this.position = pos;
//            this.score = scorer.applyAsInt(term);
//        }
//
//        @Override
//        public int compareTo(SubtermPosition o) {
//            if (this == o) return 0;
//            int p = Integer.compare(o.score, score); //lower first
//            if (p!=0) return p;
//            return Integer.compare(position, o.position);
//        }
//
//        @Override
//        public String toString() {
//            return term + " x " + score + " (" + position + ')';
//        }
//    }
//    /** heuristic for ordering comparison of subterms; lower is first */
//    private ToIntFunction<Term> subtermPrioritizer = (t) -> {
//
//        if (t.op() == type) {
//            return 0;
//        }
//        else if (t instanceof Compound) {
//            if (!t.isCommutative()) {
//                return 1 + (1 * t.volume());
//            } else {
//                return 1 + (2 * t.volume());
//            }
//        }
//        else {
//            return 1; //atomic
//        }
//    };

    @Override
    public String toString() {
        return "TermPattern{" + Arrays.toString(code) + '}';
    }
}
