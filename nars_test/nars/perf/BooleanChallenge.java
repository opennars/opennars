package nars.perf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.NARPrologMirror;
import nars.core.EventEmitter.Observer;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.core.build.Discretinuous;
import nars.entity.Task;
import nars.io.Output.OUT;
import nars.io.TextOutput;
import static nars.io.Texts.n2;
import nars.io.narsese.Narsese;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;

/**
 *
 * @author me
 */
public class BooleanChallenge {
    
    int bits = 0;
    
    double inputProb;

    float questionRatio = 0.5f;
    
    float delayFactor = 0.1f; //how important quick answers are
    
    final float freqThresh = 0.3f;
    
    private final NAR nar;
    
    int judgments = 0, questions = 0;
    

            public static int not(int i) {
                if (i == 0) return 1;
                else return 0;
            }
    
    Set<Term> answered = new HashSet();
    Map<Term,Double> questionScores = new HashMap();
    
    private final Narsese parser;
    private double score;
    private double qanswered;
    private double scoreRate;
    
    public BooleanChallenge(NAR n) {
        
        this.nar = n;        
        this.parser = new Narsese(n);
                
        n.on(OUT.class, new Observer() {

            protected boolean evalAnd(int[] t) {
                return (t[0] & t[1]) == t[2];
            }
            protected boolean evalOr(int[] t) {
                return (t[0] | t[1]) == t[2];
            }
            protected boolean evalXor(int[] t) {
                return (t[0] ^ t[1]) == t[2];
            }
            
            
            @Override public void event(Class event, Object[] arguments) {
                if (!(arguments[0] instanceof Task)) return;
                                            
                Task answer = (Task) arguments[0];
                if (!answer.sentence.isJudgment())
                    return;                
                if (answer.isInput()) {
                    //this is a repetition of something explicitly input
                    return;
                }
                
                Task question = answer.parentTask;
                if (!question.sentence.isQuestion())
                    return;
                if (!questionScores.containsKey(question.getContent())) {
                    //this is a response to a question it asked itself
                    return;
                }
                        
                        
                Term t = answer.getContent();
                if (t instanceof Inheritance) {
                    Term subj = ((Inheritance)t).getSubject();
                    Term pred = ((Inheritance)t).getPredicate();
                    if (subj instanceof Product) {
                        Product p = (Product)subj;
                        int[] n = n(p.term);
                        if (n!=null) {
                            boolean correct = false;
                            switch (pred.toString()) {
                                case "and": correct = evalAnd(n); break;
                                case "or": correct = evalOr(n); break;
                                case "xor": correct = evalXor(n); break;
                                //case "not": correct = evalNot(n); break;
                                default:
                                    return;
                            }
  
                            float freq = answer.sentence.truth.getFrequency();
                            float conf = answer.sentence.truth.getConfidence();
                            
                            if (freq < freqThresh) correct = !correct;
                            else if (freq > (1.0 - freqThresh)) { }
                            else {
                                //not clear 0 or 1
                                return;
                            }
                            
                            addScore(question, answer, correct, conf);
                            
                            if (!correct) {
                                //give correct answer
                                String c = getTerm(n[0], n[1], pred.toString(), not(n[2])) + ('.');  
                                nar.addInput(c);
                            }
                        }
                    }
                    
                }
            }
            
        });
        
        
        n.on(CycleEnd.class, new Observer() {

            @Override public void event(Class event, Object[] arguments) {
                double p = Math.random();
                if (p < inputProb) {
                    p = Math.random();                    
                    inputBoolean(bits, (p < questionRatio));
                }

            }
            
        });
                
    }
    
    void inputAxioms() {
        String a = "<{or,xor,and} <-> operator>.\n" +
                "<{";
        for (int i = 1; i < (1 << bits); i++)
            a += i + ",";
        a += "0} <-> number>.\n";
        nar.addInput(a);
    }
    
    void addScore(Task q, Task a, boolean correct, float confidence) {        

        Term questionTerm = q.getContent();
        long questionTime = q.getCreationTime();
        long answerTime = a.getCreationTime();
        long time = nar.time();
        
        
        if (questionTime > answerTime) {
            //answer already known previously
            
        }
        
        long delay = time - questionTime;
        
        
        double s;
        if (correct) {
            s = confidence / ( 1 + delay * delayFactor );            
        }
        else {
            s = -confidence;
        }
        
        Double existingScore = questionScores.get(questionTerm);
        if (existingScore == null) {
           questionScores.put(questionTerm, s); 
        }
        else {
           questionScores.put(questionTerm, Math.max(s, existingScore));
        }

        updateScore();
        System.out.println(questionTime + "," + delay + ": " + q.sentence + " | " + a.sentence.truth + "  +/-(" + s + ")     " + score + " of " + qanswered + "/" + questionScores.size() + " <" + answered.size() + ">" );
    }
    
    
    public void updateScore() {
        double s = 0;
        qanswered = 0;
        for (Double q : questionScores.values()) {
            if (q!=null) {
                s += q;
                qanswered++;
            }
        }
        score = s;
        scoreRate = s/ nar.time();
        
    }
    
    int rand(int bits) { 
        return (int)(Math.random() * (1 << bits)); 
    }

    
    protected void inputBoolean(int bits, boolean question) {
        int a = rand(bits);
        int b = rand(bits);               
        int y = rand(bits);
        int correct;
        String op;
        float truth = 1.0f;
        switch ((int)(Math.random() * 3)) {
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
        if (!question) {
            truth = (correct == y) ? 1.0f : 0.0f;
        }
        
        float conf = 0.9f;
        //String term = "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";
        String term = getTerm(a, b, op, y);
        Term t;
        try {
            t = parser.parseTerm(term);
        } catch (Narsese.InvalidInputException ex) {
            Logger.getLogger(BooleanChallenge.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        String ii = term + 
                (question ? '?' : '.')  + " %" + n2(truth) + ";" + n2(conf) + "%";
        
        //System.out.println(ii);
                        
        if (question) {
            if (answered.contains(t)) {
                //dont ask alredy answered
                return;
            }
            questionScores.putIfAbsent(t, null);            
        }
        else {
           if (questionScores.containsKey(t)) {
               //dont give answer to a question
               return;
           }
           answered.add(t);
        }
        
        nar.addInput( ii );
        
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
        }
        catch (NumberFormatException nfe) { return null;        }
        
        return x;
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

            
    public static void main(String[] args) {
        //NAR n = new DefaultNARBuilder().build();
        NAR n = new Discretinuous().build();

        
        new NARPrologMirror(n, 0.9f, true);
        
        //NAR n = new CurveBagNARBuilder().build();

        //new TextOutput(n, System.out, 0.9f);
        
        //new NARSwing(n);
        
        /*new Thread(new Runnable() {

            @Override
            public void run() { */
                new BooleanChallenge(n).run(1, 1500000,1500000);
/*            }
            
        }).start();*/
        
    }

    public String getTerm(int a, int b, String op, int y) {
        
        return "<(*," + b(a) + "," + b(b) + "," + b(y) + ") --> " + op + ">";        
        
        //return "<(*,(*," + b(a) + "," + b(b) + ")," + b(y) + ") --> " + op + ">";        
        
        
        
        
        //return "<(&/," + b(a) + "," + b(b) + "," + b(y) + ") --> " + op + ">";        
        
        //return "(&/," + op + "," + b(a) + "," + b(b) + "," + b(y) + ")";
        
        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) --> " + b(y) + ">";
        
        //return "<(*," + op + ", (*," + b(a) + "," + b(b) + ")) <=> " + b(y) + ">";
        
    }
    
}
