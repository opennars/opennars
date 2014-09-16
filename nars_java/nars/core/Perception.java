package nars.core;

import static com.google.common.collect.Iterators.singletonIterator;
import java.io.IOException;
import java.util.Iterator;
import nars.entity.AbstractTask;
import nars.entity.Sentence;
import nars.io.DefaultTextPerception;
import nars.io.Output.ERR;
import nars.operator.io.Speak;


public class Perception {

    private DefaultTextPerception text = null;
    private NAR nar;

    public void start(NAR n) {
        this.nar = n;
        this.text = new DefaultTextPerception(n);
    }

    /* Perceive an input object by calling an appropriate perception system according to the object type. */
    public Iterator<AbstractTask> perceive(final Object o) {
                
        Exception error;
        try {
            if (o instanceof String) {
                return text.perceive((String) o);
            } else if (o instanceof Sentence) {
                //TEMPORARY
                Sentence s = (Sentence) o;
                return text.perceive(s.content.toString() + s.punctuation + " " + s.truth.toString());
            }
            error = new IOException("Input unrecognized: " + o + " [" + o.getClass() + "]");
        }
        catch (Exception e) {
            error = e;
        }
        
        return singletonIterator( new Speak(ERR.class, error) );
    }

    public DefaultTextPerception getText() {        
        return text;
    }
    
    
}
