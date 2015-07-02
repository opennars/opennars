package nars.task;

import nars.term.Compound;
import nars.term.Termed;

/**
 * Created by me on 5/15/15.
 */
public interface Sentenced<T2 extends Compound> extends Termed {
    public Sentence<T2> getSentence();
}
