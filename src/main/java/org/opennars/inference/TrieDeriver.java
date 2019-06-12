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
import org.opennars.language.*;
import org.opennars.main.Parameters;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static org.opennars.inference.InitTrie.initTrie;

public class TrieDeriver {
    // tries which are the roots and are iterated independently
    List<Trie.TrieElement> rootTries = new ArrayList<>();

    public void init() {
        rootTries = initTrie();
        //writeln("TrieDeriver: init with nTries=", rootTries.length);
    }

    public void derive(Sentence leftSentence, Sentence rightSentence, List<Sentence> resultSentences, long time, Parameters narParameters) {
        boolean debugVerbose = false;

        if (debugVerbose) {
            System.out.println("TrieDeriver.derive()");
            System.out.println("   a="+leftSentence);
            System.out.println("   b="+rightSentence);

            int here42 = 6;
        }


        for(Trie.TrieElement iRootTries : rootTries) {
            {   Trie.TrieContext ctx = new Trie.TrieContext();
                ctx.occurrencetimePremiseA = leftSentence.stamp.getOccurrenceTime();
                ctx.occurrencetimePremiseB = rightSentence.stamp.getOccurrenceTime();

                interpretTrieRec(iRootTries, leftSentence, rightSentence, resultSentences, time, ctx, narParameters);
            }
            {   Trie.TrieContext ctx = new Trie.TrieContext();
                ctx.occurrencetimePremiseA = rightSentence.stamp.getOccurrenceTime();
                ctx.occurrencetimePremiseB = leftSentence.stamp.getOccurrenceTime();

                interpretTrieRec(iRootTries, rightSentence, leftSentence, resultSentences, time, ctx, narParameters);
            }
        }
    }

    public static Term walkToBinarySubject(Term root) {
        // TODO< check if this is right >
        return root instanceof Statement ? ((Statement)root).term[0] : null;
    }

    public static Term walkToBinaryPredicate(Term root) {
        // TODO< check if this is right >
        return root instanceof Statement ? ((Statement)root).term[1] : null;
    }

    // returns null if it didn't find it
    // TODO< refactor to recursive function which cuts down the path >
    public static Term walk(String[] path, Term left, Term right) {
       Term current = null;

        for(String iPath: path) {
            if (iPath.equals("a.subject")) {
                current = walkToBinarySubject(left);
            }
            else if (iPath.equals( "a.predicate")) {
                current = walkToBinaryPredicate(left);
            }
            else if (iPath.equals( "b.subject")) {
                current = walkToBinarySubject(right);
            }
            else if (iPath.equals( "b.predicate")) {
                current = walkToBinaryPredicate(right);
            }

            // path in Binary
            else if (iPath.equals("0")) {
                current = walkToBinarySubject(current);
            }
            else if (iPath.equals("1")) {
                current = walkToBinaryPredicate(current);
            }
        }

        return current;
    }

    // function which checks if the expected compound term or binary term is present
    public static boolean checkCompoundOrBinary(Term term, String comparedCompoundType) {
        boolean isStoredAsBinary = // do we need to handle it as a binary,        this is a simplification
            comparedCompoundType.equals("-") ||
                comparedCompoundType.equals("~") ||
                comparedCompoundType.equals("|") ||
                comparedCompoundType.equals("||") ||
                comparedCompoundType.equals("&") ||
                comparedCompoundType.equals("&&") ||
                comparedCompoundType.equals("&/") ||
                comparedCompoundType.equals("&|");

        if (comparedCompoundType.equals("*")) { // product expected
            // TODO< implement special handling for product
            throw new RuntimeException("TODO - not implemented");
        }
        else if (isStoredAsBinary) { // handling for binary
            // must be binary
            Statement binary = (Statement)term;
            if (binary == null) { // must be binary
                return false; // return failure because it found a not expected term
            }

            if (retCopula(binary) != comparedCompoundType) { // binary must be of expected type
                return false;
            }

            return true;
        }
        else { // not implemented case
            throw new RuntimeException("debug - ignorable internal error (checkCompoundOrBinary) for compound/binary=" + comparedCompoundType); // either not implemented or
        }
    }

    // interprets a trie
    // returns null if it fails - used to propagate control flow
    public boolean interpretTrieRec(
        Trie.TrieElement trieElement,
        Sentence leftSentence,
        Sentence rightSentence,
        List<Sentence> resultSentences,

        long time,
        Trie.TrieContext trieCtx,
        Parameters narParameters
    ) {
        boolean debugVerbose = false;

        if (debugVerbose) System.out.println("interpretTrieRec ENTRY");

        Term left = leftSentence.term;
        Term right = rightSentence.term;




        if (trieElement.type == Trie.TrieElement.EnumType.CHECKCOPULA) {
            if(debugVerbose) System.out.println("interpretTrieRec CHECKCOPULA");

            if (trieElement.side == Trie.TrieElement.EnumSide.LEFT) {
                Statement b = (Statement)left;
                if (b == null) {
                    return false; // we assume that checkcopula checks implicitly for an binary, so it is fine to return
                }
                if (!retCopula(b).equals(trieElement.checkedString)) {
                    return true; // propagate failure
                }
            }
            else { // check right
                Statement b = (Statement) right;
                if (b == null) {
                    return false; // we assume that checkcopula checks implicitly for an binary, so it is fine to return
                }
                if (!retCopula(b).equals(trieElement.checkedString)) {
                    return true; // propagate failure
                }
            }
        }
        else if(trieElement.type == Trie.TrieElement.EnumType.EXEC) {
            if(debugVerbose) System.out.println("interpretTrieRec EXEC");

            trieElement.fp.derive(leftSentence, rightSentence, resultSentences, trieElement, time, trieCtx, narParameters);
        }
        else if(trieElement.type == Trie.TrieElement.EnumType.WALKCOMPARE) {
            if(debugVerbose) System.out.println("interpretTrieRec WALKCOMPARE");

            Term leftElement = walk(trieElement.pathLeft, left, right);
            Term rightElement = walk(trieElement.pathRight, left, right);

            if (leftElement == null || !leftElement.equals(rightElement)) {
                return true; // abort if walk failed or if the walked elements don't match up
            }
        }
        else if(trieElement.type == Trie.TrieElement.EnumType.WALKCHECKCOMPOUND) {
            if(debugVerbose) System.out.println("interpretTrieRec WALKCHECKCOMPOUND");



            if (trieElement.pathLeft.length == 0) { // walk right
                String[] path = trieElement.pathRight;
                Term walkedTerm = walk(path, left, right);

                if (walkedTerm == null) { // doesn't the expected term exist?
                    return false; // return failure if so
                }

                String comparedCompoundType = trieElement.checkedString;

                if (!checkCompoundOrBinary(walkedTerm, comparedCompoundType)) {
                    return false; // propage failure
                }

                // fall through because we want to walk children
            }
            else if(trieElement.pathRight.length == 0) { // walk left
                String[] path = trieElement.pathLeft;
                Term walkedTerm = walk(path, left, right);

                if (walkedTerm == null) { // doesn't the expected term exist?
                    return false; // return failure if so
                }

                String comparedCompoundType = trieElement.checkedString;

                if (!checkCompoundOrBinary(walkedTerm, comparedCompoundType)) {
                    return false; // propage failure
                }

                // fall through because we want to walk children
            }
            else {} // ignore

            // fall through because we want to walk children
        }
        else if (trieElement.type == Trie.TrieElement.EnumType.INTERVALPROJECTION) {
            // compute the TV of the projected interval

            if (trieElement.stringPayload.equals("IntervalProjection(t,z)")) {
                if (trieCtx.intervalPremiseT == null || trieCtx.intervalPremiseZ == null) {
                    return false; // propagate error
                }

                long t = trieCtx.intervalPremiseT, z = trieCtx.intervalPremiseZ;

                trieCtx.projectedTruthConfidence = calcProjectedConf(t, z); // calculate projection
            }
            else {
                return false; // propagate error
            }
        }
        else if(trieElement.type == Trie.TrieElement.EnumType.LOADINTERVAL) {
            // checks and loads interval
            //
            // it is loaded by path into a variable in trie-context

            Term term = walk(trieElement.path, left, right);
            if (!(term instanceof Interval)) { // must be valid interval
                return false; // propagate failure
            }

            long intervalValue = ((Interval)term).time;

            if (trieElement.stringPayload.equals("premiseT")) {
                trieCtx.intervalPremiseT = intervalValue;
            }
            else if (trieElement.stringPayload.equals("premiseZ")) {
                trieCtx.intervalPremiseZ = intervalValue;
            }
            else { // interval name is invalid
                System.out.println("warning - invalid interval name");
            }
        }
        else if(trieElement.type == Trie.TrieElement.EnumType.PRECONDITION) {

            if (trieElement.stringPayload .equals("Time:After(tB,tA)") || trieElement.stringPayload.equals("Time:Parallel(tB,tA)")) {
                if (leftSentence.stamp.isEternal() || rightSentence.stamp.isEternal()) {
                    return false; // no timestamp - precondition failed
                }

                if(trieElement.stringPayload.equals("Time:After(tB,tA)")) {
                    boolean isPreconditionFullfilled = rightSentence.stamp.getOccurrenceTime() > leftSentence.stamp.getOccurrenceTime();

                    if( !isPreconditionFullfilled ) {
                        return false; // propagate failure
                    }
                }
                else if(trieElement.stringPayload.equals("Time:Parallel(tB,tA)")) {
                    if( !occurrenceTimeIsParallel(rightSentence.stamp.getOccurrenceTime(), leftSentence.stamp.getOccurrenceTime())) {
                        return false;
                    }
                }
            }
            else {
                System.out.println("warning - invalid precondition");
                return false; // fail by default
            }
        }

        // we need to iterate children if we are here
        for( Trie.TrieElement iChildren: trieElement.children) {
            boolean recursionResult = interpretTrieRec(iChildren, leftSentence, rightSentence, resultSentences, time, trieCtx, narParameters);
            if (recursionResult ) {
                //return recursionResult;
            }
        }

        return true;
    }

    public static String retCopula(Statement stmt) {
        if (stmt instanceof Inheritance) {
            return "-->";
        }
        else if (stmt instanceof Similarity) {
            return "<->";
        }
        else {
            throw new RuntimeException("TODO - implement!");
        }
    }


    // are the events perceived to occur at the same time?
    public static boolean occurrenceTimeIsParallel(long a, long b) {
        long timeWindow = 50; // TODO< make parameter >
        return abs(a - b) <= timeWindow;
    }

    public static double calcProjectedConf(long timeA, long timeB) {
        long diff = abs(timeA - timeB);

        return pow(2.0, -diff * 0.003);
    }
}
