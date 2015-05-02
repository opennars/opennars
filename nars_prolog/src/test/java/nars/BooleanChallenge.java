package nars;

import nars.Events.CycleEnd;
import nars.Events.OUT;
import nars.event.Reaction;
import nars.io.Texts;
import nars.io.narsese.InvalidInputException;
import nars.nal.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.prototype.Default;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    int bits = 0;
    boolean failOnError = false; //exit on the first logical error
    double negationProb = 0;
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
        n.on(new Reaction() {

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
                        subj = ((Compound) subj).term[0];

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
                                    //String c = getTerm(n[0], n[1], pred.toString(), not(n[2])) + ('.');
                                    String c = "(--," + getTerm(n[0], n[1], pred.toString(), n[2]) + ("). %1.00;" + Texts.n2(inputConf) + "%");
                                    nar.input(c);
                                }

                            }
                        }
                    }

                }
            }

        }, OUT.class);


        n.on(new Reaction() {

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

        }, CycleEnd.class);

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
        Global.DEBUG = true;
        NAR n = new NAR(new Default().setInternalExperience(null));
        //NAR n = new NAR(new Discretinuous());
        //new NARPrologMirror(n, 0.9f, true, true, false);
        //NAR n = new CurveBagNARBuilder().build();

        //new TraceWriter(n, System.out);
        //new TextOutput(n, System.out);
        /*
        {

            String lastOutput = "";
            @Override
            public String process(Class c, Object[] o) {
                String p = super.process(c, o);

                if (p.equals(lastOutput)) {
                    System.err.println("DUPLICATED OUTPUT");
                    new Exception().printStackTrace();
                }
                lastOutput = p;
                return p;
            }
        };*/

        //new NARSwing(n);

        /*new Thread(new Runnable() {

            @Override
            public void test() { */
        new BooleanChallenge(n).run(1, 6550000, 6550000);
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

        nar.believe("<{or,xor,and} --> operate>", inputConf);

        String a = "<{";
        for (int i = 1; i < (1 << bits); i++)
            a += i + ",";
        a += "0} --> number>";
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


    int rand(int bits) {
        return (int) (Math.random() * (1 << bits));
    }

    protected void inputBoolean(int bits, boolean question) {
        int a = rand(bits);
        int b = rand(bits);
        int y = rand(bits);
        int correct;
        String op;
        boolean allowNegation = Math.random() < negationProb;
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

        if (!question && (!allowNegation) && (correct!=y))
            y = correct;


        //String term = "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";
        String term = getTerm(a, b, op, y);
        Term t;
        try {
            t = nar.term(term);
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

        float truth = 1f;
        if (!question) {
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

        nar.input(ii);

    }

    public void run(int level, int bit1Iterations, int bit2Thinking) {
        bits = level;


        System.out.println("start");
        inputAxioms();


        inputProb = 0.10;

        for (int i = 0; i < bit1Iterations; i++)
            nar.frame(1);

        inputProb = 0.0;

        for (int i = 0; i < bit2Thinking; i++)
            nar.frame(1);

    }

    public String getTerm(int a, int b, String op, int y) {

        //use the { } --> form and not {-- because this is the form answers will return in
        return "<(*," + b(a) + "," + b(b) + "," + b(y) + ") --> " + op + ">";

        //return "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";        


        //return "<(&/," + b(a) + "," + b(b) + "," + b(y) + ") --> " + op + ">";        

        //return "(&/," + op + "," + b(a) + "," + b(b) + "," + b(y) + ")";

        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) --> " + b(y) + ">";

        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) <=> " + b(y) + ">";

    }

}
