package nars.core;

import java.io.IOException;
import nars.entity.AbstractTask;
import nars.entity.Sentence;
import nars.io.narsese.Narsese;


public class Perception {

    final Narsese text;

    public Perception(Narsese textPerception) {
        this.text = textPerception;
    }

    /* Perceive an input object by calling an appropriate perception system according to the object type. */
    public AbstractTask perceive(final Object o, NAR nar) throws IOException {        
        AbstractTask t;
        if (o instanceof String) {
            t = text.perceive((String) o, nar);
        } else if (o instanceof Sentence) {
            //TEMPORARY
            Sentence s = (Sentence) o;
            t = text.perceive(s.content.toString() + s.punctuation + " " + s.truth.toString(),nar);
        } else {
            throw new IOException("Unrecognized input (" + o.getClass() + "): " + o);
        }
        return t;
    }
}
