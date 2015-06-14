package nars.core;

import java.io.IOException;
import nars.entity.AbstractTask;
import nars.entity.Sentence;
import nars.io.TextPerception;


public class Perception {

    final TextPerception text;

    public Perception(TextPerception textPerception) {
        this.text = textPerception;
    }

    /* Perceive an input object by calling an appropriate perception system according to the object type. */
    public AbstractTask perceive(final Object o) throws IOException {        
        AbstractTask t;
        if (o instanceof String) {
            t = text.perceive((String) o);
        } else if (o instanceof Sentence) {
            //TEMPORARY
            Sentence s = (Sentence) o;
            t = text.perceive(s.content.toString() + s.punctuation + " " + s.truth.toString());
        } else {
            throw new IOException("Unrecognized input (" + o.getClass() + "): " + o);
        }
        return t;
    }
}
