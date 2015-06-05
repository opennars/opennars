package nars.nal.stamp;

import nars.nal.Sentence;
import nars.nal.term.Compound;

import java.util.function.Consumer;

/**
 * Indicates that this can be used to "stamp" a sentence
 */
public interface IStamp<C extends Compound>  {

    public void stamp(Sentence<C> compoundSentence);

}
