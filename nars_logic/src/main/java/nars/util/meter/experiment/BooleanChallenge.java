//package nars.util.meter.experiment;
//
//import com.gs.collections.api.tuple.Twin;
//import com.gs.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
//import nars.NAR;
//import nars.Narsese;
//import nars.nal.nal1.Inheritance;
//import nars.nal.nal3.SetExt;
//import nars.nal.nal4.Product;
//import nars.nar.Default;
//import nars.task.Task;
//import nars.task.flow.TaskQueue;
//import nars.term.Term;
//import nars.term.atom.Atom;
//import nars.term.compound.Compound;
//import nars.util.data.random.XORShiftRandom;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static nars.util.Texts.n2;
//import static nars.util.Texts.n4;
//
//public class BooleanChallenge  {
//
//    final float freqThresh = 0.2f; //threshold diff from 0.0 or 1.0 considered too uncertain to count as answer
//    private final double complete;
//    boolean failOnError = false; //exit on the first logical error
//    private boolean correctFeedback = true;
//    boolean ignoreCorrectProvided = false; //if true, scores will only be updated if the answer is was not provided or if it was incorrect (provided or not provided)
//    float confThreshold = 0.01f; //confidence threshold for being counted as an answer
//    float inputConf = 0.99f;
//
//    public static void main(String[] args) {
//        //Global.DEBUG = true;
//        NAR n = new Default(1024, 2, 2, 3).nal(6);
//
//        //new Commander(n, true);
//
//        //NAR n = new NAR(new Discretinuous());
//        //new NARPrologMirror(n, 0.95f, true, true, false);
//        //NAR n = new CurveBagNARBuilder().build();
//
//        //new TraceWriter(n, System.out);
//
//        new BooleanChallenge(n, 2, 35225, 0.1f).getScore();
//
//    }
//
//
//    private final NAR nar;
//    private final double score;
//    int bits;
//    String startChallenge = "<boolean --> challenge>";
//
//    Set<Term> answerProvided = new HashSet();
//    ObjectDoubleHashMap<Term> questionScores = new ObjectDoubleHashMap();
//
//
//
//    private double totalScoreCorrect = 0, totalScoreWrong = 0, totalScoreCorrectIncludingRevised = 0;
//
//    public StringBuilder sb = new StringBuilder();
//
//    public BooleanChallenge(NAR nar, int bits, int cycles, float mystery) {
//
//        this.nar = nar;
//        //nar.log();
//
//        this.bits = bits;
//
////        n.on(new Reaction() {
////
////            @Override
////            public void event(Class event, Object[] arguments) {
////                double p = Math.random();
////                if (p < inputProb) {
////                    p = Math.random();
////                    inputBoolean(bits, (p < questionRatio));
////                }
////
////
////            }
////
////        }, CycleEnd.class);
//
//        inputAxioms();
//
//        int N;
//        switch (bits) {
//            case 1: N = (2*2*2) * 3; break;
//            case 2: N = (4*4*4) *3; break;
//            case 3: N = (8*8*8) * 3; break;
//            default:
//                throw new RuntimeException("didnt count here yet");
//        }
//        int toAsk = (int)Math.ceil(mystery * N);
//
//
//
//        TaskQueue queue = new TaskQueue();
//        while (answerProvided.size() < N - toAsk) {
//            String n = inputBoolean(bits, false);
//            if (n!=null)
//                queue.add(nar.task(n));
//        }
//        queue.input(nar);
//
//        nar.memory.eventAnswer.on((Twin<Task> tp) -> {
//            System.out.println(tp);
//            Task answer = tp.getTwo();
//            Task question = tp.getOne();
//
//            if (!answer.isJudgment())
//                return;
//
//            if (answer.isInput()) {
//                //this is a repetition of something explicitly input
//                return;
//            }
//
////        if (!started) {
////            if (answer.getTerm().toString().equals(startChallenge)) {
////                started = true;
////            }
////            else {
////                return;
////            }
////        }
//
//            Term qterm = answer.getTerm(); //for now, assume the qustion term is answer term
//
//            if (answerProvided.contains(qterm)) {
//                return;
//            }
//
//                /*
//                Task question = answer.getParentTask();
//                if (!question.sentence.isQuestion())
//                    return;
//                if (!questionScores.containsKey(question.getTerm())) {
//                    //this is a response to a question it asked itself
//                    return;
//                }
//                */
//
//            if (answer.getConfidence() < confThreshold)
//                return;
//
//            Term t = answer.getTerm();
//
//
//            if (t instanceof Inheritance) {
//                Term subj = ((Inheritance) t).getSubject();
//                Term pred = ((Inheritance) t).getPredicate();
//
//                if (!(pred instanceof Atom)) return;
//                int[] eq = n(pred, 1);
//                int result = eq[0];
//
//                if ((subj instanceof SetExt) && (((SetExt) subj).size() == 1))
//                    subj = ((Compound) subj).term(0);
//
//                if (subj instanceof Product) {
//                    Product p = (Product) subj;
//                    int[] m = n(p, 2);
//
//                    if (m != null) {
//                        int n[] = new int[] { m[0], m[1], result };
//                        boolean correct = false;
//                        switch (p.term(0).toString()) {
//                            case "and":
//                                correct = evalAnd(n);
//                                break;
//                            case "or":
//                                correct = evalOr(n);
//                                break;
//                            case "xor":
//                                correct = evalXor(n);
//                                break;
//                            //case "not": correct = evalNot(n); break;
//                            default:
//                                return;
//                        }
//
//                        float freq = answer.getFrequency();
//                        float conf = answer.getConfidence();
//
//                        if (freq < freqThresh) {
//                            correct = !correct; //invert
//                            freq = 1f - freq;
//                        }
//
//                        if (freq < (1.0 - freqThresh)) {
//                            //not clear 0 or 1
//                            return;
//                        }
//
//                        addScore(qterm, answer, correct, freq*conf);
//
//                        if (!correct) {
//                            System.err.println(answer.getExplanation());
//
//                            if (failOnError)
//                                System.exit(1);
//
//                            //give correct answer
//                            if (correctFeedback) {
//                                //String c = getTerm(n[0], n[1], pred.toString(), not(n[2])) + ('.');
//                                Term fc = nar.term(getTerm(n[0], n[1], pred.toString(), n[2]));
//                                String c = "(--," + fc + ("). %1.00;" + n2(inputConf) + "%");
//
//                                nar.input(c);
//
//                                answerProvided.add(fc);
//                                questionScores.remove(fc);
//                            }
//
//                        }
//                    }
//                }
//
//            }
//
//        });
//
//        //begin watchign for answers after input is finished
//        //this belief will signal the test to begin
//        nar.input(startChallenge + ".");
//
//        TaskQueue t = new TaskQueue();
//        while (questionScores.size() < toAsk) {
//            String stask = inputBoolean(bits, true);
//            if (stask!=null) {
//                Task task = nar.task(stask);
//                t.add(task);
//            }
//        }
//        t.input(nar);
//
//
//        nar.frame(cycles);
//
//        System.out.println(answerProvided);
//        System.out.println(questionScores);
//
//
//
//        System.out.print(nar.time() + " cycles, ");
//        System.out.print(answerProvided.size() + " provided, ");
//        System.out.println(questionScores.size() + " answerable");
//        System.out.print(totalScoreCorrect + " correct, ");
//        System.out.print((totalScoreCorrectIncludingRevised - totalScoreCorrect) + " revision, ");
//        System.out.println(totalScoreWrong + " wrong, ");
//
//
//        this.complete = (questionScores.sum() / ((double)N));
//        System.out.print(complete + " COMPLETE, ");
//
//        this.score = complete * ((totalScoreCorrect) / (totalScoreCorrect + totalScoreWrong));
//        System.out.println(score + " SCORE");
//
//    }
//
//    public double getScore() {
//        return score;
//    }
//
//    protected static boolean evalAnd(int[] t) { return (t[0] & t[1]) == t[2];        }
//
//    protected static boolean evalOr(int[] t) {
//        return (t[0] | t[1]) == t[2];
//    }
//
//    protected static boolean evalXor(int[] t) {
//        return (t[0] ^ t[1]) == t[2];
//    }
//
//
//
//
//
//
//    public static int not(int i) {
//        if (i == 0) return 1;
//        else return 0;
//    }
//
//    public static char b(int a) {
//        return (char)(a + '0');
//    }
//
//    public static boolean isNumber(String s) {
//        return (s.length() == 1) && (Character.isDigit(s.charAt(0)));
//    }
//
//    public static int[] n(Term p, int count) {
//
//        AtomicInteger i = new AtomicInteger(0);
//        int[] x = new int[count];
//
//        int gi[] = new int[1];
//
//        //extract all numbers from the product recursively, depth first
//        p.recurseTerms((t, parent) -> {
//            if (gi[0] == count) return;
//
//            String ts = t.toString();
//            if (!ts.startsWith("b")) return; //b prefix
//            ts = ts.toString().substring(1);
//
//            if (isNumber(ts.toString())) {
//                gi[0] = i.getAndIncrement();
//                x[gi[0]] = (Integer.parseInt(ts));
//            }
//        });
//        if (gi[0] >= count)
//            return x;
//
//        return null;
//    }
//
//
//
////    public void updateScore() {
////        double s = 0;
////        qanswered = 0;
////        for (Double q : questionScores.values()) {
////            if (q!=null) {
////                s += q;
////                qanswered++;
////            }
////        }
////        //scoreRate = s/ nar.time();
////
////    }
//
//    void inputAxioms() {
////
////        nar.believe("<{or,xor,and} --> operate>", 1f, inputConf);
////        nar.believe("<(a0,a0) <=> b0>");
////        nar.believe("<(a0,a1) <=> b1>");
////        nar.believe("<(a1,a0) <=> b2>");
////        nar.believe("<(a1,a1) <=> b3>");
//
//        String a = "<{";
//        for (int i = 1; i < (1 << bits); i++)
//            a += "b" + i + ",";
//        a += "b0} --> number>";
//        nar.believe(a, 1f, inputConf);
//    }
//
//    void addScore(Term q, Task a, boolean correct, float expectation) {
//
//        if (ignoreCorrectProvided && correct && answerProvided.contains(a.getTerm())) return;
//
//        Term questionTerm = q;
//        long questionTime = a.getParentTask() != null ? a.getParentTask().getCreationTime() : 0;
//        long answerTime = a.getCreationTime();
//        long time = nar.time();
//
//
//        long delay = time - questionTime;
//
//
//        double s;
//        s = expectation; // / ( 1 + delay * delayFactor );
//
////        if (!questionScores.containsKey(questionTerm)) {
////            System.out.println("question not asked: " + questionTerm + " but these are: " + questionScores.keySet());
////        }
//
//        double existingScore = questionScores.getIfAbsent(questionTerm,0);
//        if (correct) {
//
//            if (s > existingScore) {
//
//
//                double ds = s - existingScore;
//                totalScoreCorrectIncludingRevised += s;
//                totalScoreCorrect += ds;
//
//                questionScores.put(questionTerm, Math.min(Math.max(existingScore,s), 1f));
//            }
//
//        }
//        else {
//
//            if (-s < existingScore) {
//                totalScoreWrong += s;
//                questionScores.put(questionTerm, Math.max(Math.min(existingScore, -s), -1f));
//            }
//        }
//
//
//
//
//        System.out.println(questionTime + "," + delay + ": " + a + "  " + n4(s) + "  " +
//                n4(totalScoreCorrect) + "-" + n4(totalScoreWrong) + "=" + n4(totalScoreCorrect - totalScoreWrong));
//
//
//    }
//
//
//    int rand(int bits) {
//        return (int) (XORShiftRandom.global.nextInt(1 << bits));
//    }
//
//    protected String inputBoolean(int bits, boolean question) {
//        int a = rand(bits);
//        int b = rand(bits);
//        int y = rand(bits);
//        int correct;
//        String op;
//        boolean allowNegation = XORShiftRandom.global.nextBoolean();
//        switch (XORShiftRandom.global.nextInt(3)) {
//            case 0:
//                op = "and";
//                correct = a & b;
//                break;
//            case 1:
//                op = "or";
//                correct = a | b;
//                break;
//            case 2:
//            default:
//                op = "xor";
//                correct = a ^ b;
//                break;
//        }
//
//        if (!question && (!allowNegation) && (correct!=y))
//            y = correct;
//
//
//        //String term = "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";
//        String term = getTerm(a, b, op, y);
//        Term t;
//        try {
//            t = nar.term(term);
//        } catch (Narsese.NarseseException ex) {
//            //Logger.getLogger(BooleanChallenge.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
//            return op;
//        }
//
//
//        if (question) {
//            //dont ask alredy answered
//            if (answerProvided.contains(t)) {
//                return null;
//            }
//            //only ask once
//            if (questionScores.containsKey(t))
//                return null;
//            questionScores.put(t, 0);
//        } else {
//            if (questionScores.containsKey(t)) {
//                //dont give answer to a question
//                return null;
//            }
//            answerProvided.add(t);
//        }
//
//        float truth = 1f;
//        if (!question) {
//            truth = (correct == y) ? 1.0f : 0.0f;
//        }
//        String ii = term +
//                (question ? '?' : '.') + " %" + n2(truth) + ";" + n2(inputConf) + "%";
//
//        //System.out.println(ii);
//        //System.out.println(questionScores.keySet());
//        //System.out.println(answerProvided);
//
//        return ii;
//    }
//
//
//    public final String getTerm(int a, int b, String op, int y) {
//
//        sb.setLength(0);
//        sb.append("<{(" + op + ",(");
//            sb.append('b');
//            sb.append(a);
//        sb.append(',');
//            sb.append('b');
//            sb.append(b);
//        sb.append("))} -->");
//            sb.append('b');
//            sb.append(y);
//        sb.append(">");
//        return sb.toString();
//
//
//        //use the { } --> form and not {-- because this is the form answers will return in
//        //return "<{(*," + b(a) + "," + b(b) + "," + b(y) + ")} --> " + op + ">";
//
//        //return "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";
//
//
//        //return "<(&/," + b(a) + "," + b(b) + "," + b(y) + ") --> " + op + ">";
//
//        //return "(&/," + op + "," + b(a) + "," + b(b) + "," + b(y) + ")";
//
//        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) --> " + b(y) + ">";
//
//        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) <=> " + b(y) + ">";
//
//    }
//
//}
