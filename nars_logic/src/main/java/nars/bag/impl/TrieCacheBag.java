package nars.bag.impl;

import nars.concept.Concept;
import nars.term.Term;
import org.magnos.trie.Trie;
import org.magnos.trie.TrieSequencer;

/**
 * Created by me on 7/8/15.
 */
public class TrieCacheBag extends MapCacheBag<Term,Concept> {

    public static class TermTrieSequencer implements TrieSequencer<Term>
    {

        @Override
        public int matches( Term a, int indexA, Term b, int indexB, int count )
        {
            byte[] sequenceA = a.bytes();
            byte[] sequenceB = b.bytes();

            for (int i = 0; i < count; i++)
            {
                if (sequenceA[indexA + i] != sequenceB[indexB + i])
                {
                    return i;
                }
            }

            return count;
        }

        @Override
        public int lengthOf( Term t )
        {
            return t.bytes().length;
        }

        @Override
        public int hashOf( Term t, int i )
        {
            return t.bytes()[i];
        }

    }

    final static TrieSequencer termSequencer = new TermTrieSequencer();

    public TrieCacheBag() {
        super( new Trie(termSequencer) );
    }

}
