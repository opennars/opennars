//package nars.nal.multistep;
//
//import nars.model.impl.Default;
//import nars.Events;
//import nars.NAR;
//import nars.io.out.Output;
//import nars.io.Texts;
//import nars.nal.Sentence;
//import nars.nal.Task;
//
//import java.util.Arrays;
//
//
//
//public class PlanDecisionExperiment {
//
//    public PlanDecisionExperiment() {
//        decision(0.1, 0.9);
//        decision(0.1, 0.1);
//
//        decision(0.9, 0.9);
//        decision(0.5, 0.5);
//        decision(0.9, 0.1);
//
//        decision(0.54, 0.55);
//
//    }
//
//    public void decision(double... confidences) {
//        String i = "";
//
//        for (int n = 0; n < confidences.length; n++) {
//            i += "<<c" + n + " --> decision> =/> goal>." + "\n";
//        }
//        for (int n = 0; n < confidences.length; n++) {
//            i += "<pick(p" + n + ") =/> <c" + n + " --> decision>>." + "\n";
//        }
//        for (int n = 0; n < confidences.length; n++) {
//            double c = confidences[n];
//            i += "<c" + n + " --> decision>. :|: %1.00;" + Texts.n2((float)c) + "%\n";
//        }
//
//        i += "goal!";
//
//        System.out.println(i);
//
//        System.out.println(Arrays.toString(confidences));
//
//        NAR n = new NAR(new Default());
//        n.input(i);
//        //new TextOutput(n, System.out);
//
//        new Output(n) {
//            @Override public void event(Class c, Object... args) {
//                Object o = args[0];
//                /*if (c == IN.class) {
//                    System.out.println("IN: " + n.getTime() + ": " + o);
//                }*/
//                if (c == Events.TaskAdd.class) {
//                    if (o instanceof Task) {
//                        Sentence s = ((Task)o).sentence;
//                        if (s.punctuation == '!') {
//                            System.out.println(o);
//                        }
//                    }
//                }
//                if (c == Events.EXE.class) {
//                    System.out.println("EXE: " + n.time() + ": " + o);
//                }
//            }
//        };
//
//        n.runWhileNewInput(2000);
//
//        System.out.println();
//
//    }
//
//    public static void main(String[] arg) {
//        new PlanDecisionExperiment();
//    }
//}
