//package nars.gui.input;
//
//import nars.NAR;
//
//import java.util.List;
//
///**
// * Created by me on 8/11/15.
// */
//public class NarseseInput implements TextInputPanel.TextInputMode {
//
//    private String input;
//    private NAR nar;
//
//    @Override
//    public void setInputState(NAR nar, String input) {
//        this.input = input;
//        this.nar = nar;
//    }
//
//    @Override
//    public String getInterpretation() {
//        if (input.length() == 0)
//            return null;
//
//        //TEMPORARILY DISABLED WHILE REFACTORING PARSER
//        //ReportingParseRunner rpr = new ReportingParseRunner(p.Input());
//        //ParsingResult r = rpr.run(input);
//
//        /*String s = "";
//        boolean valid = (r.parseErrors.isEmpty());
//        if (!valid) {
//            for (Object e : r.parseErrors) {
//                if (e instanceof InvalidInputError) {
//                    InvalidInputError iie = (InvalidInputError) e;
//                    s += iie.getClass().getSimpleName() + " " + iie.getErrorMessage() + "\n";
//                    s += (" at: " + iie.getStartIndex() + " to " + iie.getEndIndex()) + "\n";
//
//                    for (MatcherPath m : iie.getFailedMatchers()) {
//                        s += ("  ?-> " + m + '\n');
//                    }
//                } else {
//                    s += e.toString();
//                }
//            }
//        } else {*/
//        return "OK. ";
//        //}
//        //return s;
//    }
//
//    public TextInputPanel.InputAction inputDirect = new TextInputPanel.InputAction() {
//
//        @Override
//        public String getLabel() {
//            return "Input";
//        }
//
//        @Override
//        public String getDescription() {
//            return "Direct input into NAR";
//        }
//
//        @Override
//        public String run() {
//            evaluateSeq(input);
//            return "";
//        }
//
//        @Override
//        public double getStrength() {
//            return 1.5;
//        }
//
//    };
//
//    public TextInputPanel.InputAction step = new TextInputPanel.InputAction() {
//
//        @Override
//        public String getLabel() {
//            return "Step";
//        }
//
//        @Override
//        public String getDescription() {
//            return "Compute 1 cycle";
//        }
//
//        @Override
//        public String run() {
//            if (!nar.isRunning())
//                nar.frame(1);
//            return input;
//        }
//
//        @Override
//        public double getStrength() {
//            return 1.0;
//        }
//
//    };
//
//    @Override
//    public void getActions(List<TextInputPanel.InputAction> actionsCollected) {
//        //TODO only allow input if it parses, but while parser is incomplete, allow all
//        if (input.length() > 0)
//            actionsCollected.add(inputDirect);
//
//        actionsCollected.add(step);
//        //TODO reset
//
//
//        /*
//        Other Actions:
//            Ask - direct input a question, and create a solution window to watch for answers
//            Command - direct input a goal, and create a task window to watch progress
//            Parse Tree - show the parse tree for input (but don't clear it)
//        */
//    }
//
//    public void evaluateSeq(String input) {
//        //TODO make sequential evaluation
//        nar.input(input);
//        if (!nar.isRunning())
//            nar.frame(1);
//    }
//
//
// }
