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
import org.opennars.main.Parameters;

import java.util.ArrayList;
import java.util.List;

public class Trie {
    public static class TrieElement {
        public TrieElement(int type) {
            this.type = type;
        }

        public int type;
        public int side;
        public String checkedString; // can be checked copula
        public String stringPayload;
        public String[] path;

        public String[] pathLeft;
        public String[] pathRight;

        // function which builds the result or returns null on failure
        // trie element is passed to pass some additional data to it
        ///public void function(shared Sentence leftSentence, shared Sentence rightSentence, Sentences resultSentences, shared TrieElement trieElement, TrieContext *trieCtx) fp;
        public DerivableAction fp;

        // interface to call the derivable action from the deriver
        public interface DerivableAction {
            void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, Parameters narParameters);
        }

        public List<TrieElement> children = new ArrayList<>(); // children are traversed if the check was true

        public static class EnumSide {
            public static final int LEFT = -1;
            public static final int RIGHT = 1;
        }

        public static class EnumType {
            public static final int CHECKCOPULA = 0; // check copula of premise which is a binary
            // the trie traversal is not continued if it is not a binary

            //FIN, // terminate processing  - commented because it is implicitly terminated if nothing else matches

            public static final int WALKCOMPARE = 1; // walk left and compare with walk right

            public static final int WALKCHECKCOMPOUND = 2; // walk and check the type of a compound

            public static final int LOADINTERVAL = 3; // load the value of a interval by a path
            public static final int INTERVALPROJECTION = 4; // compute the interval projection

            public static final int PRECONDITION = 5;

            public static final int EXEC = 6; // trie element to run some code with a function
        }
    }


    // context to carry state across the evaluation of trie nodes
    public static class TrieContext {
        Long intervalPremiseT; // used to store the "t" value in the evaluation of the trie
        Long intervalPremiseZ; // used to store the "z" value in the evaluation of the trie

        Long occurrencetimePremiseA;
        Long occurrencetimePremiseB;

        double projectedTruthConfidence = 0.0;
    }

    // add element to trie
    // used to build an efficient trie by the initialization of the trie
    public static void addToTrieRec(List<TrieElement> root, TrieElement added) {
        if (added.type == TrieElement.EnumType.CHECKCOPULA) {
            for(TrieElement iRootElement:root) {
                if (iRootElement.type == TrieElement.EnumType.CHECKCOPULA && iRootElement.side == added.side && iRootElement.checkedString.equals(added.checkedString)) {

                    for(TrieElement iChildren: added.children) {
                        addToTrieRec(iRootElement.children, iChildren);
                    }
                    return;
                }
            }
        }
        else if (added.type == TrieElement.EnumType.WALKCOMPARE) {
            for(TrieElement iRootElement:root) {
                if (iRootElement.type == TrieElement.EnumType.WALKCOMPARE && iRootElement.pathLeft == added.pathLeft && iRootElement.pathRight.equals(added.pathRight)) {

                    for (TrieElement iChildren: added.children) {
                        addToTrieRec(iRootElement.children, iChildren);
                    }
                    return;
                }
            }
        }

        root.add(added);
    }
}
