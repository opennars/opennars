package nars.util.graph;

import java.util.Map;
import java.util.WeakHashMap;
import nars.core.Memory;
import nars.entity.Item;
import nars.entity.Sentence;

/**
 * SentenceGraph subclass that stores a Map<Sentence,Concept> of where the sentence
 * originated
 */
abstract public class SentenceItemGraph extends SentenceGraph {
    
    public final Map<Sentence,Item> concepts;

    public SentenceItemGraph(Memory memory) {
        super(memory);
        concepts = new WeakHashMap();
    }

    
    @Override
    public boolean add(final Sentence s, final Item c) {
        
        
       // if(specialAdd) {
       //     return false;
       // }
        
        if (super.add(s, c)) {
            concepts.put(s, c);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(final Sentence s) {
        if (super.remove(s)) {
            concepts.remove(s);
            //System.out.println(System.identityHashCode(this) + ": " +vertexSet().size() + ":" + edgeSet().size() + " add: " + s + " ");
            
            return true;
        }
        return false;
    }
    
    
    
    
}
