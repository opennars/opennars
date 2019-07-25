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
import org.opennars.control.DerivationContext;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.TruthValue;
import org.opennars.language.Interval;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.main.Parameters;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
public class InitTrie {
// AUTOGEN: initializes and fills tries
public static List<Trie.TrieElement> initTrie() {
   List<Trie.TrieElement> rootTries = new ArrayList<>();
// rule         A, B   ['Time:After(tB,tA)']  |-   <A=/>(tB-tA)B>		(Truth:induction)	Introduce$#
{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:After(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive0();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}


// rule         A, B   ['Time:After(tB,tA)']  |-   <A&/(tB-tA)B>		(Truth:intersection)	Introduce#
/*{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:After(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive1();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}*/


// rule         A, B   ['Time:Parallel(tB,tA)']  |-   <A=|>B>		(Truth:induction)	Introduce$#
{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:Parallel(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive2();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}


// rule         A, B   ['Time:After(tB,tA)']  |-   <A&/B>		(Truth:intersection)	Introduce#
/*{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:After(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive3();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}*/


// rule         A, B   ['Time:Parallel(tB,tA)']  |-   <A&|B>		(Truth:intersection)	Introduce#
{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:Parallel(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive4();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}


// rule         A, B   ['Time:After(tB,tA)']  |-   <B=\>(tA-tB)A>		(Truth:induction)	Introduce$#
{
    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);
    te0.stringPayload = "Time:After(tB,tA)";
    
    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);
    teX.fp = new derive5();
    te0.children.add(teX);
    
    Trie.addToTrieRec(rootTries, te0);
}


  return rootTries;
}


public static class derive0 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, DerivationContext nal, Parameters narParameters) {
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
      conclusionTerm = DeriverHelpers.derivePredImplConclusionTerm(conclusionTerm, aSentence, bSentence);
      if(conclusionTerm == null) {
          return;
      }
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      { // add conclusion without introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
         if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
            return; // conclusions with such a low conf are not relevant to the system
         }
         Sentence resultSentence = new Sentence(conclusionTerm, derivationPunctuation, tv, stamp);
         synchronized (resultSentences) { resultSentences.add(resultSentence); }
      }
      { // add conclusion with introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
         // introduce vars
         // "Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
             for(boolean subjectIntro : new boolean[]{true, false}) {
                 Set<Pair<Term,Float>> termWithIntroVarsAndPenality = CompositionalRules.introduceVariables(nal, conclusionTerm, subjectIntro);
                 for(Pair<Term,Float> iTermWithIntroVarsAndPenality : termWithIntroVarsAndPenality) { // ok we applied it, all we have to do now is to use it
                     final Term conclusionTerm2 = iTermWithIntroVarsAndPenality.getLeft();
                     final float penality = iTermWithIntroVarsAndPenality.getRight();
                     tv.mulConfidence(penality);
                     if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
                        continue; // conclusions with such a low conf are not relevant to the system
                     }
                     if (conclusionTerm2 != null) { // check is necessary because conclusion may be invalid
                        Sentence resultSentence = new Sentence(conclusionTerm2, derivationPunctuation, tv, stamp);
                        synchronized (resultSentences) { resultSentences.add(resultSentence); }
                     }
                     else {
                        int debugHere=6;
                     }
                 }
             }
         }
   }
} // method
} // class


public static class derive1 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, DerivationContext nal, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = a;
   Term conclusionPred = b;
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("&/", conclusionSubj, conclusionPred);
      if(conclusionTerm == null) {
          return;
      }
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      { // add conclusion without introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, aSentence.truth, bSentence.truth, narParameters) : null;
         if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
            return; // conclusions with such a low conf are not relevant to the system
         }
         Sentence resultSentence = new Sentence(conclusionTerm, derivationPunctuation, tv, stamp);
         synchronized (resultSentences) { resultSentences.add(resultSentence); }
      }
      { // add conclusion with introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, aSentence.truth, bSentence.truth, narParameters) : null;
         // introduce vars
         // "Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
             for(boolean subjectIntro : new boolean[]{true, false}) {
                 Set<Pair<Term,Float>> termWithIntroVarsAndPenality = CompositionalRules.introduceVariables(nal, conclusionTerm, subjectIntro);
                 for(Pair<Term,Float> iTermWithIntroVarsAndPenality : termWithIntroVarsAndPenality) { // ok we applied it, all we have to do now is to use it
                     final Term conclusionTerm2 = iTermWithIntroVarsAndPenality.getLeft();
                     final float penality = iTermWithIntroVarsAndPenality.getRight();
                     tv.mulConfidence(penality);
                     if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
                        continue; // conclusions with such a low conf are not relevant to the system
                     }
                     if (conclusionTerm2 != null) { // check is necessary because conclusion may be invalid
                        Sentence resultSentence = new Sentence(conclusionTerm2, derivationPunctuation, tv, stamp);
                        synchronized (resultSentences) { resultSentences.add(resultSentence); }
                     }
                     else {
                        int debugHere=6;
                     }
                 }
             }
         }
   }
} // method
} // class


public static class derive2 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, DerivationContext nal, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = a;
   Term conclusionPred = b;
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("=|>", conclusionSubj, conclusionPred);
      if(conclusionTerm == null) {
          return;
      }
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      { // add conclusion without introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
         if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
            return; // conclusions with such a low conf are not relevant to the system
         }
         Sentence resultSentence = new Sentence(conclusionTerm, derivationPunctuation, tv, stamp);
         synchronized (resultSentences) { resultSentences.add(resultSentence); }
      }
      { // add conclusion with introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
         // introduce vars
         // "Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
             for(boolean subjectIntro : new boolean[]{true, false}) {
                 Set<Pair<Term,Float>> termWithIntroVarsAndPenality = CompositionalRules.introduceVariables(nal, conclusionTerm, subjectIntro);
                 for(Pair<Term,Float> iTermWithIntroVarsAndPenality : termWithIntroVarsAndPenality) { // ok we applied it, all we have to do now is to use it
                     final Term conclusionTerm2 = iTermWithIntroVarsAndPenality.getLeft();
                     final float penality = iTermWithIntroVarsAndPenality.getRight();
                     tv.mulConfidence(penality);
                     if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
                        continue; // conclusions with such a low conf are not relevant to the system
                     }
                     if (conclusionTerm2 != null) { // check is necessary because conclusion may be invalid
                        Sentence resultSentence = new Sentence(conclusionTerm2, derivationPunctuation, tv, stamp);
                        synchronized (resultSentences) { resultSentences.add(resultSentence); }
                     }
                     else {
                        int debugHere=6;
                     }
                 }
             }
         }
   }
} // method
} // class


public static class derive3 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, DerivationContext nal, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = a;
   Term conclusionPred = b;
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("&/", conclusionSubj, conclusionPred);
      if(conclusionTerm == null) {
          return;
      }
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      { // add conclusion without introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, aSentence.truth, bSentence.truth, narParameters) : null;
         if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
            return; // conclusions with such a low conf are not relevant to the system
         }
         Sentence resultSentence = new Sentence(conclusionTerm, derivationPunctuation, tv, stamp);
         synchronized (resultSentences) { resultSentences.add(resultSentence); }
      }
      { // add conclusion with introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, aSentence.truth, bSentence.truth, narParameters) : null;
         // introduce vars
         // "Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
             for(boolean subjectIntro : new boolean[]{true, false}) {
                 Set<Pair<Term,Float>> termWithIntroVarsAndPenality = CompositionalRules.introduceVariables(nal, conclusionTerm, subjectIntro);
                 for(Pair<Term,Float> iTermWithIntroVarsAndPenality : termWithIntroVarsAndPenality) { // ok we applied it, all we have to do now is to use it
                     final Term conclusionTerm2 = iTermWithIntroVarsAndPenality.getLeft();
                     final float penality = iTermWithIntroVarsAndPenality.getRight();
                     tv.mulConfidence(penality);
                     if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
                        continue; // conclusions with such a low conf are not relevant to the system
                     }
                     if (conclusionTerm2 != null) { // check is necessary because conclusion may be invalid
                        Sentence resultSentence = new Sentence(conclusionTerm2, derivationPunctuation, tv, stamp);
                        synchronized (resultSentences) { resultSentences.add(resultSentence); }
                     }
                     else {
                        int debugHere=6;
                     }
                 }
             }
         }
   }
} // method
} // class


public static class derive4 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, DerivationContext nal, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = a;
   Term conclusionPred = b;
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("&|", conclusionSubj, conclusionPred);
      if(conclusionTerm == null) {
          return;
      }
      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps
      { // add conclusion without introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, aSentence.truth, bSentence.truth, narParameters) : null;
         if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
            return; // conclusions with such a low conf are not relevant to the system
         }
         Sentence resultSentence = new Sentence(conclusionTerm, derivationPunctuation, tv, stamp);
         synchronized (resultSentences) { resultSentences.add(resultSentence); }
      }
      { // add conclusion with introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INTERSECTION, aSentence.truth, bSentence.truth, narParameters) : null;
         // introduce vars
         // "Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
             for(boolean subjectIntro : new boolean[]{true, false}) {
                 Set<Pair<Term,Float>> termWithIntroVarsAndPenality = CompositionalRules.introduceVariables(nal, conclusionTerm, subjectIntro);
                 for(Pair<Term,Float> iTermWithIntroVarsAndPenality : termWithIntroVarsAndPenality) { // ok we applied it, all we have to do now is to use it
                     final Term conclusionTerm2 = iTermWithIntroVarsAndPenality.getLeft();
                     final float penality = iTermWithIntroVarsAndPenality.getRight();
                     tv.mulConfidence(penality);
                     if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
                        continue; // conclusions with such a low conf are not relevant to the system
                     }
                     if (conclusionTerm2 != null) { // check is necessary because conclusion may be invalid
                        Sentence resultSentence = new Sentence(conclusionTerm2, derivationPunctuation, tv, stamp);
                        synchronized (resultSentences) { resultSentences.add(resultSentence); }
                     }
                     else {
                        int debugHere=6;
                     }
                 }
             }
         }
   }
} // method
} // class


public static class derive5 implements Trie.TrieElement.DerivableAction {
public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, DerivationContext nal, Parameters narParameters) {
   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : "Invalid derivation : question-question";
   
   
   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());
   
   char derivationPunctuation = aSentence.punctuation;
   if (aSentence.isQuestion() || bSentence.isQuestion()) {
       derivationPunctuation = '?';
   }
   
   Term a = aSentence.term;
   Term b = bSentence.term;
   
   Term conclusionSubj = b;
   Term conclusionPred = DeriverHelpers.makeBinary("&/",a,new Interval(/*negate because tA-tB is a convention but leads to negative timing*/- (trieCtx.occurrencetimePremiseA-trieCtx.occurrencetimePremiseB)));
   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL
      Term conclusionTerm = DeriverHelpers.makeBinary("=\\>", conclusionSubj, conclusionPred);
      if(conclusionTerm == null) {
          return;
      }
      Stamp stamp = new Stamp(bSentence.stamp, aSentence.stamp, time, narParameters); // merge stamps
      { // add conclusion without introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
         if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
            return; // conclusions with such a low conf are not relevant to the system
         }
         Sentence resultSentence = new Sentence(conclusionTerm, derivationPunctuation, tv, stamp);
         synchronized (resultSentences) { resultSentences.add(resultSentence); }
      }
      { // add conclusion with introduced variables
         TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute(TruthFunctions.EnumType.INDUCTION, aSentence.truth, bSentence.truth, narParameters) : null;
         // introduce vars
         // "Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
             for(boolean subjectIntro : new boolean[]{true, false}) {
                 Set<Pair<Term,Float>> termWithIntroVarsAndPenality = CompositionalRules.introduceVariables(nal, conclusionTerm, subjectIntro);
                 for(Pair<Term,Float> iTermWithIntroVarsAndPenality : termWithIntroVarsAndPenality) { // ok we applied it, all we have to do now is to use it
                     final Term conclusionTerm2 = iTermWithIntroVarsAndPenality.getLeft();
                     final float penality = iTermWithIntroVarsAndPenality.getRight();
                     tv.mulConfidence(penality);
                     if(hasConclusionTruth && tv.getConfidence() < 0.0001) {
                        continue; // conclusions with such a low conf are not relevant to the system
                     }
                     if (conclusionTerm2 != null) { // check is necessary because conclusion may be invalid
                        Sentence resultSentence = new Sentence(conclusionTerm2, derivationPunctuation, tv, stamp);
                        synchronized (resultSentences) { resultSentences.add(resultSentence); }
                     }
                     else {
                        int debugHere=6;
                     }
                 }
             }
         }
   }
} // method
} // class



// helper
static boolean isSame(Term a, Term b) {
   return a.equals(b);
}
} // class
