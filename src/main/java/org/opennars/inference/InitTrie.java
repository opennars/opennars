/*
 * The MIT License
 *
 * Copyright 2019 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.inference;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.TruthValue;
import org.opennars.language.Interval;
import org.opennars.language.Term;
import org.opennars.main.Parameters;

import java.util.List;
import java.util.ArrayList;
public class InitTrie {
// AUTOGEN: initializes and fills tries
public static List<Trie.TrieElement> initTrie() {
   List<Trie.TrieElement> rootTries = new ArrayList<>();
// rule         A, B   ['Time:After(tB,tA)']  |-   <A=/>(tB-tA)B>		(Truth:induction)
{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:After(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive0();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}


  return rootTries;
}


public static class derive0 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = DeriverHelpers.makeBinary("&/",a,new Interval(trieCtx.occurrencetimePremiseB-trieCtx.occurrencetimePremiseA));
   Term conclusionPred = b;
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("=/>", conclusionSubj, conclusionPred);
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
      if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
          return; // conclusions with such a low conf are not relevant to the system
      }
      resultSentences.add(new Sentence(conclusionTerm, derivationPunctuation, tv, stamp));
   }
} // method
} // class


public static class derive1 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = DeriverHelpers.makeBinary("&/",a,new Interval(trieCtx.occurrencetimePremiseB-trieCtx.occurrencetimePremiseA));
   Term conclusionPred = b;
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("=/>", conclusionSubj, conclusionPred);
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
      if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
          return; // conclusions with such a low conf are not relevant to the system
      }
      resultSentences.add(new Sentence(conclusionTerm, derivationPunctuation, tv, stamp));
   }
} // method
} // class



// helper
static boolean isSame(Term a, Term b) {
   return a.equals(b);
}
} // class
