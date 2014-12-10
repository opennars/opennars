/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.ConceptForget;
import nars.core.Events.ConceptNew;
import nars.core.NAR;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.io.Symbols.NativeOperator;
import nars.language.CompoundTerm;
import nars.language.Term;

/**
 *each of those rows can be a representation of something like a 'multiconcept' or 'aggregated concept' which combines concept data from related concepts
and tasks where the only differ by the top-level operator, tense, freq, conf,etc
 */
public class Idea {
   
    final public Set<Concept> concepts = new HashSet();
    final CharSequence key;

    public static CharSequence getKey(Concept c) {
        return getKey(c.term);
    }

    public static CharSequence getKey(Term t) {
        if (t instanceof CompoundTerm) {
            CompoundTerm ct = (CompoundTerm)t;
            
            if (ct.isVector()) {
                //if not commutative (order matters): key = list of subterms
                return "v" + Arrays.toString(ct.term);
            }            
            else {
                //key = 'set' + sorted list of subterms
                return Term.toSortedSet(ct.term).toString();
            }
        }
        else {
            return t.name();
        }
    }
    private HashSet<NativeOperator> operators;
    
    public Idea(Concept c) {
        super();
        this.key = getKey(c.term);
        add(c);
    }
    
    public Idea(Iterable<Concept> c) {
        super();        
        this.key = getKey(c.iterator().next());
        for (Concept x : c)
            add(x);
    }
    
    public Set<NativeOperator> operators() {
        return operators;
    }
    
    /**
     * includes the concept in this idea.  it's ok to repeat add a 
     * concept again since they are stored as Set
     */
    public boolean add(Concept c) {
        if (Parameters.DEBUG)
            ensureMatchingConcept(c);
  
        boolean b = concepts.add(c);
        
        if (b) {
            //update operators
            operators = new HashSet<NativeOperator>();
            for (Concept x : concepts)
                operators.add(x.term.operator());
        }
        
        return b;
    }
    
    public boolean remove(Concept c) {
        if (Parameters.DEBUG)
            ensureMatchingConcept(c);
        
        return concepts.remove(c);
    }
    
    public CharSequence key() {
        return key;
    }

    protected void ensureMatchingConcept(Concept c) {
        CharSequence ckey = getKey(c.term);
        if (!ckey.equals(key))
            throw new RuntimeException(c + " does not belong in Idea " + key);          }

    @Override
    public String toString() {
        return key() + concepts.toString();
    }
    
    
    public static class IdeaSet extends HashMap<CharSequence,Idea> implements EventObserver {
        private final NAR nar;

        public IdeaSet(NAR n) {
            super();
            this.nar = n;
            enable(true);
        }

        @Override
        public void event(Class event, Object[] args) {
            Concept c = (Concept)args[0];
            
            if (event == ConceptNew.class) {
                add(c);
            }
            else if (event == ConceptForget.class) {
                remove(c);
            }
        }
        
        
        public void enable(boolean enabled) {
            nar.memory.event.set(this, enabled, 
                    ConceptNew.class, ConceptForget.class);            
        }
        
        public Idea get(Concept c) {
            CharSequence k = Idea.getKey(c);
            return get(k);            
        }
        public void add(Concept c) {
            Idea existing = get(c);
            if (existing == null) {
                existing = new Idea(c);
                put(Idea.getKey(c), existing); //calculating getKey() twice can be avoided by caching it when it's uesd to get Idea existing above
            }
            else {
                existing.add(c);
            }
        }
        
        public void remove(Concept c) {
            Idea existing = get(c);
            if (existing != null) {
                existing.remove(c);
            }
        }
        
    }
    
}
