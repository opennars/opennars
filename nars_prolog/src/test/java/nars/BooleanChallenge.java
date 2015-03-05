package nars;

import nars.build.Default;
import nars.core.Events.CycleEnd;
import nars.core.Events.OUT;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.Reaction;
import nars.io.TextOutput;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.Narsese;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal1.Inheritance;
import nars.logic.nal3.SetExt;
import nars.logic.nal4.Product;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nars.io.Texts.n2;
import static nars.io.Texts.n4;

/**
 * @author me
 */
public class BooleanChallenge {

    final float freqThresh = 0.20f;
    private final NAR nar;
    private final Narsese parser;
    int bits = 0;
    boolean failOnError = true; //exit on the first logical error
    double inputProb;
    double axiomProb = 1 / 1000.0f;
    float questionRatio = 0.1f; //lower is easier
    boolean ignoreCorrectProvided = false; //if true, scores will only be updated if the answer is was not provided or if it was incorrect (provided or not provided)
    float confThreshold = 0.5f; //confidence threshold for being counted as an answer
    float inputConf = 0.98f;
    int judgments = 0, questions = 0;
    Set<Term> answerProvided = new HashSet();
    Map<Term, Double> questionScores = new HashMap();
    private boolean correctFeedback = true;
    private double totalScoreCorrect = 0, totalScoreWrong = 0;
    public BooleanChallenge(NAR n) {

        this.nar = n;
        this.parser = new Narsese(n);

        n.on(OUT.class, new Reaction() {

            protected boolean evalAnd(int[] t) {
                return (t[0] & t[1]) == t[2];
            }

            protected boolean evalOr(int[] t) {
                return (t[0] | t[1]) == t[2];
            }

            protected boolean evalXor(int[] t) {
                return (t[0] ^ t[1]) == t[2];
            }


            @Override
            public void event(Class event, Object[] arguments) {
                if (!(arguments[0] instanceof Task)) return;

                Task answer = (Task) arguments[0];
                if (!answer.sentence.isJudgment())
                    return;

                if (answer.isInput()) {
                    //this is a repetition of something explicitly input
                    return;
                }


                Term qterm = answer.getTerm(); //for now, assume the qustion term is answer term
                /*
                Task question = answer.getParentTask();
                if (!question.sentence.isQuestion())
                    return;
                if (!questionScores.containsKey(question.getTerm())) {
                    //this is a response to a question it asked itself
                    return;
                }
                */

                if (answer.sentence.truth.getConfidence() < confThreshold)
                    return;

                Term t = answer.getTerm();
                if (t instanceof Inheritance) {
                    Term subj = ((Inheritance) t).getSubject();
                    Term pred = ((Inheritance) t).getPredicate();

                    if ((subj instanceof SetExt) && (((SetExt) subj).size() == 1))
                        subj = ((SetExt) subj).term[0];

                    if (subj instanceof Product) {
                        Product p = (Product) subj;
                        int[] n = n(p.term);
                        if (n != null) {
                            boolean correct = false;
                            switch (pred.toString()) {
                                case "and":
                                    correct = evalAnd(n);
                                    break;
                                case "or":
                                    correct = evalOr(n);
                                    break;
                                case "xor":
                                    correct = evalXor(n);
                                    break;
                                //case "not": correct = evalNot(n); break;
                                default:
                                    return;
                            }

                            float freq = answer.sentence.truth.getFrequency();
                            float conf = answer.sentence.truth.getConfidence();

                            if (freq < freqThresh) correct = !correct; //invert
                            else if (freq > (1.0 - freqThresh)) {
                            } else {
                                //not clear 0 or 1
                                return;
                            }

                            addScore(qterm, answer, correct, conf);

                            if (!correct) {
                                System.err.println(answer.getExplanation());

                                if (failOnError)
                                    System.exit(1);

                                //give correct answer
                                if (correctFeedback) {
                                    String c = getTerm(n[0], n[1], pred.toString(), not(n[2])) + ('.');
                                    nar.addInput(c);
                                }

                            }
                        }
                    }

                }
            }

        });


        n.on(CycleEnd.class, new Reaction() {

            @Override
            public void event(Class event, Object[] arguments) {
                double p = Math.random();
                if (p < inputProb) {
                    p = Math.random();
                    inputBoolean(bits, (p < questionRatio));
                }
                p = Math.random();
                if (p < axiomProb) {
                    inputAxioms();
                }

            }

        });

    }

    public static int not(int i) {
        if (i == 0) return 1;
        else return 0;
    }

    public static String b(int a) {
        return "" + a;
    }

    public static int[] n(Term[] t) {
        int[] x = new int[t.length];
        int i = 0;

        try {
            for (Term v : t) {
                x[i++] = Integer.parseInt(v.toString());
            }
        } catch (NumberFormatException nfe) {
            return null;
        }

        return x;
    }

    public static void main(String[] args) {
        Parameters.DEBUG = true;
        NAR n = new NAR(new Default().setInternalExperience(null));
        //NAR n = new NAR(new Discretinuous());


        //new NARPrologMirror(n, 0.9f, true, true, false);

        //NAR n = new CurveBagNARBuilder().build();

        new TextOutput(n, System.out);

        //new NARSwing(n);

        /*new Thread(new Runnable() {

            @Override
            public void test() { */
        new BooleanChallenge(n).run(1, 5500, 5500);
/*            }

        }).start();*/

    }


//    public void updateScore() {
//        double s = 0;
//        qanswered = 0;
//        for (Double q : questionScores.values()) {
//            if (q!=null) {
//                s += q;
//                qanswered++;
//            }
//        }
//        //scoreRate = s/ nar.time();
//
//    }

    void inputAxioms() {

        nar.believe("<(||,or,xor,and) --> operator>", inputConf);

        String a = "<(||,";
        for (int i = 1; i < (1 << bits); i++)
            a += i + ",";
        a += "0) --> number>";
        nar.believe(a, inputConf);
    }

    void addScore(Term q, Task a, boolean correct, float confidence) {

        if (ignoreCorrectProvided && correct && answerProvided.contains(a.getTerm())) return;

        Term questionTerm = q;
        long questionTime = a.getParentTask() != null ? a.getParentTask().getCreationTime() : 0;
        long answerTime = a.getCreationTime();
        long time = nar.time();


        if (questionTime > answerTime) {
            //answer already known previously

        }

        long delay = time - questionTime;


        double s;
        if (correct) {
            s = confidence; // / ( 1 + delay * delayFactor );
            totalScoreCorrect += s;
        } else {
            s = -confidence;
            totalScoreWrong += -s;
        }

//        if (!questionScores.containsKey(questionTerm)) {
//            System.out.println("question not asked: " + questionTerm + " but these are: " + questionScores.keySet());
//        }

        Double existingScore = questionScores.get(questionTerm);
        if (existingScore == null) existingScore = 0d;

        questionScores.put(questionTerm, existingScore + s);

        System.out.println(questionTime + "," + delay + ": " + a.sentence + "  " + n4(s) + "  " +
                n4(totalScoreCorrect) + "-" + n4(totalScoreWrong) + "=" + n4(totalScoreCorrect - totalScoreWrong));


        //System.out.println("  " + getNALStack());
    }

    public List<String> getNALStack() {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();

        String prefix = "";

        boolean tracing = false;
        String prevMethodID = null;

        List<String> path = new ArrayList();
        int i;
        for (i = 0; i < s.length; i++) {
            StackTraceElement e = s[i];

            String className = e.getClassName();
            String methodName = e.getMethodName();


            if (tracing) {

                //Filter conditions
                if (className.contains("reactor."))
                    continue;
                if (className.contains("EventEmitter"))
                    continue;
                if ((className.equals("NAL") || className.equals("Memory")) && methodName.equals("emit"))
                    continue;

                int cli = className.lastIndexOf(".") + 1;
                if (cli != -1)
                    className = className.substring(cli, className.length()); //class's simpleName

                String methodID = className + '_' + methodName;
                String sm = prefix + '_' + methodID;

                path.add(sm);

                prevMethodID = methodID;


                //Termination conditions
                if (className.contains("ConceptFireTask") && methodName.equals("accept"))
                    break;
                if (className.contains("ImmediateProcess") && methodName.equals("reason"))
                    break;
                if (className.contains("ConceptFire") && methodName.equals("reason"))
                    break;
            } else if (className.endsWith(".NAL") && methodName.equals("deriveTask")) {
                tracing = true; //begins with next stack element
            }

        }

        if (i >= s.length - 1) {
            System.err.println("Stack not clipped: " + Arrays.toString(s));
        }

        return path;

    }

    int rand(int bits) {
        return (int) (Math.random() * (1 << bits));
    }

    protected void inputBoolean(int bits, boolean question) {
        int a = rand(bits);
        int b = rand(bits);
        int y = rand(bits);
        int correct;
        String op;
        float truth = 1.0f;
        switch ((int) (Math.random() * 3)) {
            case 0:
                op = "and";
                correct = a & b;
                break;
            case 1:
                op = "or";
                correct = a | b;
                break;
            case 2:
            default:
                op = "xor";
                correct = a ^ b;
                break;
        }


        //String term = "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";
        String term = getTerm(a, b, op, y);
        Term t;
        try {
            t = parser.parseTerm(term);
        } catch (InvalidInputException ex) {
            Logger.getLogger(BooleanChallenge.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }


        if (question) {
            if (answerProvided.contains(t)) {
                //dont ask alredy answered
                return;
            }
            questionScores.putIfAbsent(t, null);
        } else {
            if (questionScores.containsKey(t)) {
                //dont give answer to a question
                return;
            }
            answerProvided.add(t);
        }

        if (!question) {
            truth = 1f;
            //truth = (correct == y) ? 1.0f : 0.0f;
            if (correct != y) {
                term = "(--," + term + ")";
            }
        }
        String ii = term +
                (question ? '?' : '.') + " %" + n2(truth) + ";" + n2(inputConf) + "%";

        //System.out.println(ii);
        //System.out.println(questionScores.keySet());
        //System.out.println(answerProvided);

        nar.addInput(ii);

    }

    public void run(int level, int bit1Iterations, int bit2Thinking) {
        bits = level;


        System.out.println("start");
        inputAxioms();


        inputProb = 0.10;

        for (int i = 0; i < bit1Iterations; i++)
            nar.step(1);

        inputProb = 0.0;

        for (int i = 0; i < bit2Thinking; i++)
            nar.step(1);

    }

    public String getTerm(int a, int b, String op, int y) {

        //use the { } --> form and not {-- because this is the form answers will return in
        return "<{(*," + b(a) + "," + b(b) + "," + b(y) + ")} --> " + op + ">";

        //return "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";        


        //return "<(&/," + b(a) + "," + b(b) + "," + b(y) + ") --> " + op + ">";        

        //return "(&/," + op + "," + b(a) + "," + b(b) + "," + b(y) + ")";

        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) --> " + b(y) + ">";

        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) <=> " + b(y) + ">";

    }

}
