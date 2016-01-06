package nars.guifx;

import nars.NAR;
import nars.guifx.util.CodeInput;
import nars.task.in.Twenglish;

/**
 * Created by me on 10/13/15.
 */
public class NaturalLanguagePane extends CodeInput {

    final Twenglish te = new Twenglish();
    private final NAR nar;
    float sentenceBudget = 0.5f;

    public NaturalLanguagePane(NAR n) {
        nar = n;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() /*+ "_" + hashCode()*/;
    }

    /**
     * return false to indicate input was not accepted, leaving it as-is.
     * otherwise, return true that it was accepted and the buffer will be cleared.
     */
    @Override
    public boolean onInput(String s) {


        te.parse(toString(), nar, s).forEach(t -> {
            t.getBudget().setPriority((float) sentenceBudget);
            nar.input(t);
        });

//        Collection<Term> tokens = Twenglish.tokenize(s);
//
//        if (tokens == null)
//            return false;
//        else {
//            if (!tokens.isEmpty())
//                nar.believe(
//                        Similarity.make(
//                                Atom.quote(s),
//                                Sequence.makeSequence(
//                                        tokens.toArray(new Term[tokens.size()])
//                                )
//                        )
//                );
        return true;
//        }
    }
}
