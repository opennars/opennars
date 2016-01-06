///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.nal;
//
//import nars.model.impl.Default;
//import nars.Events.Answer;
//import nars.Events.OUT;
//import nars.NAR;
//import nars.NARSeed;
//import nars.event.NARReaction;
//import nars.narsese.InvalidInputException;
//import nars.nal.term.Term;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//
///**
// *
// * @author me
// */
//public class TuneTuffy {
//
//    public static class SolutionMonitor extends NARReaction {
//        private final Term term;
//        Sentence mostConfident = null;
//
//        public SolutionMonitor(NAR n, String term) throws InvalidInputException {
//            super(n, true, OUT.class, Answer.class);
//
//            Term t = n.term(term);
//            this.term = t;
//
//            n.input(t.toString() + "?");
//        }
//
//        @Override
//        public void event(Class event, Object[] args) {
//            if ((event == Answer.class) || (event == OUT.class)) {
//                Task task = (Task)args[0];
//                Term content = task.sentence.term;
//                if (task.sentence.isJudgment()) {
//                    if (content.equals(term)) {
//                        onJudgment(task.sentence);
//                    }
//                }
//            }
//        }
//
//        public void onJudgment(Sentence s) {
//            if (mostConfident == null)
//                mostConfident = s;
//            else {
//                float existingConf = mostConfident.truth.getConfidence();
//                if (existingConf < s.truth.getConfidence())
//                    mostConfident = s;
//            }
//        }
//
//        @Override
//        public String toString() {
//            return term + "? " + mostConfident;
//        }
//
//    }
//
//    public static void main(String[] args) throws FileNotFoundException, InvalidInputException {
//        NARSeed b = new Default().
//                setInternalExperience(null);
//
//
//        NAR n = new NAR(b);
//        n.input(new File("nal/use_cases/tuffy.smokes.nal"));
//
//        //new TextOutput(n, System.out, 0.95f);
//
//        n.run(0);
//
//        SolutionMonitor anna0 = new SolutionMonitor(n, "<Anna <-> [Smokes]>");
//        SolutionMonitor bob0 = new SolutionMonitor(n, "<Bob --> [Smokes]>");
//        SolutionMonitor edward0 = new SolutionMonitor(n, "<Edward --> [Smokes]>");
//        SolutionMonitor frank0 = new SolutionMonitor(n, "<Frank --> [Smokes]>");
//
//        SolutionMonitor anna = new SolutionMonitor(n, "<Anna <-> [Cancer]>");
//        SolutionMonitor bob = new SolutionMonitor(n, "<Bob --> [Cancer]>");
//        SolutionMonitor edward = new SolutionMonitor(n, "<Edward --> [Cancer]>");
//        SolutionMonitor frank = new SolutionMonitor(n, "<Frank --> [Cancer]>");
//
//
//        n.run(15000);
//
//        //first number is the expected Tuffy probability result
//        System.out.println("0.75? " + edward);
//        System.out.println("0.65? " + anna);
//        System.out.println("0.50? " + bob);
//        System.out.println("0.45? " + frank);
//    }
// }
