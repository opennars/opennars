package nars;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Output;
import nars.io.TextOutput;
import nars.language.Inheritance;
import nars.language.Term;
import nars.prolog.InvalidTheoryException;
import nars.prolog.Struct;

/**
 * Causes a NARProlog to mirror certain activity of a NAR
 */
public class NARPrologMirror implements Output {
    private final NAR nar;
    private final NARProlog prolog;

    public NARPrologMirror(NAR nar, NARProlog prolog) {
        this.nar = nar;
        this.prolog = prolog;
        
        nar.addOutput(this);
    }
   
    @Override
    public void output(final Class channel, final Object o) {        
        if ((channel == IN.class)) {
            if (o instanceof Task) {
                Task task = (Task)o;
                Sentence s = task.sentence;
                if (s.isJudgment()) {
                    try {
                        Struct th = newJudgmentTheory(s);
                        System.out.println("Prolog Theory: " + th.toString());
                        if (th!=null) 
                            prolog.addTheory(th);
                    } catch (InvalidTheoryException ex) {
                        nar.output(ERR.class, ex.toString());
                    }
                }
                else if (s.isQuestion()) {

                }

            }
        }
    }
    
    /** creates a theory from a judgment Statement */
    private Struct newJudgmentTheory(final Sentence judgment) throws InvalidTheoryException {
        //TODO directly construct the theory objects, instead of creating a String        
        return pterm(judgment.content);
    }

    //NARS term -> Prolog term
    public Struct pterm(Term term) {
        
        //CharSequence s = termString(term);
        if (term instanceof Inheritance) {
            Inheritance i = (Inheritance)term;
            return new Struct("inheritance", pterm(i.getSubject()), pterm(i.getPredicate()));
        }
        else if (term instanceof Term) {
            return new Struct(term.name().toString());
        }
        
        return null;        
    }
    


    
    public static void main(String[] args) throws Exception {
        NAR nar = new DefaultNARBuilder().build();
        new TextOutput(nar, System.out);
        
        NARProlog prolog = new NARProlog(nar);
        
        new NARPrologMirror(nar, prolog);
        
        
        nar.addInput("<a --> b>.");        
        nar.finish(3);
        
        //prolog.solve("revision([inheritance(bird, swimmer), [1, 0.8]], [inheritance(bird, swimmer), [0, 0.5]], R).");
        //prolog.solve("inference([inheritance(swan, bird), [0.9, 0.8]], [inheritance(bird, swan), T]).");
        prolog.solve("inheritance(X,b).");
        
    }    

    
    
}
