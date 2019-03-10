package org.opennars.derivation;

import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.TruthValue;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.language.*;
import org.opennars.main.Parameters;

import java.util.*;

public class DerivationProcessor {

    // TODO< convert all instructions to static methods which can easily get called with bytecode >

    // process a program on two sentences for some temporal inference to derive a conclusion
    // /param condA condition
    // /param condB condition
    // /param program program to get processed instruction by instruction
    // /param a premise a
    // /param b premise b
    public static Sentence processProgramForTemporal(String condA, String condB, Instr[] program, Sentence a, Sentence b, List<Sentence> derivedSentences, Timable time, Parameters reasonerParameters) {
        if (!checkCondition(condA, a) || !checkCondition(condB, b)) {
            return null; // ignore because any condition didn't match up
        }

        InstructionsAndContext.Context ctx = new InstructionsAndContext.Context();
        ctx.a = a;
        ctx.b = b;
        ctx.derivedSentences = derivedSentences;
        ctx.time = time;
        ctx.reasonerParameters = reasonerParameters;

        // interpret program instruction by instruction and keep track of indices in premises

        for(;;) {
            if(ctx.ip>=program.length) {
                break;
            }
            Instr currentInstr = program[ctx.ip];
            ctx.ip++;

            if(currentInstr.mnemonic.equals("startIdxA")) {
                InstructionsAndContext.startIdxA(ctx);
            }
            else if(currentInstr.mnemonic.equals("startIdxB")) {
                InstructionsAndContext.startIdxB(ctx);
            }
            else if(currentInstr.mnemonic.equals("checkEndA")) {
                InstructionsAndContext.checkEndA(ctx);
            }
            else if(currentInstr.mnemonic.equals("checkEndB")) {
                InstructionsAndContext.checkEndB(ctx);
            }
            else if(currentInstr.mnemonic.equals("jmpTrue")) {
                if(ctx.flag) {
                    String labelName = currentInstr.arg0;

                    // search for label
                    for(int idx=0;idx<program.length;idx++) {
                        if(program[idx].mnemonic.equals("label") && program[idx].arg0.equals(labelName)) {
                            ctx.ip = idx;
                            break;
                        }
                    }
                }
            }
            else if(currentInstr.mnemonic.equals("jmp")) {
                InstructionsAndContext.jmp(currentInstr.arg0Int, ctx);
            }
            else if(currentInstr.mnemonic.equals("scanNextA")) {
                InstructionsAndContext.scanNextA(ctx);
            }
            else if(currentInstr.mnemonic.equals("scanNextB")) {
                InstructionsAndContext.scanNextB(ctx);
            }
            else if(currentInstr.mnemonic.equals("storeSeqSortedA")) {
                InstructionsAndContext.storeSeqSortedA(ctx);
            }
            else if(currentInstr.mnemonic.equals("storeSeqSortedB")) {
                InstructionsAndContext.storeSeqSortedB(ctx);
            }

            else if(currentInstr.mnemonic.equals("readA")) {
                InstructionsAndContext.readA(ctx);
            }
            else if(currentInstr.mnemonic.equals("readB")) {
                InstructionsAndContext.readB(ctx);
            }

            else if(currentInstr.mnemonic.equals("calcTruthUnionAB")) {
                InstructionsAndContext.calcTruthUnionAB(ctx);
            }
            else if(currentInstr.mnemonic.equals("calcTruthIdA")) {
                InstructionsAndContext.calcTruthIdA(ctx);
            }

            // MACRO to write out the sorted list as a conjunction
            else if(currentInstr.mnemonic.equals("m_writeConjunction")) {
                return InstructionsAndContext.m_writeConjunction(ctx);
            }
            else if(currentInstr.mnemonic.equals("m_writeWindowedConjunction")) {
                return InstructionsAndContext.m_writeWindowedConjunction(ctx);
            }
            else if(currentInstr.mnemonic.equals("label")) {
                // ignore
            }
            else {
                int here = 5;
            }
        }

        return null; // program was invalid if we are here - ignore
    }

    public static class TermWithOccurrenceTime {
        public Term term;
        public long occurrenceTime;

        public TermWithOccurrenceTime(Term term, long occurrenceTime) {
            this.term = term;
            this.occurrenceTime = occurrenceTime;
        }
    }

    // encoding of condition
    // "F" temporal forward implication
    // "S" temporal sequence
    // "E" single event
    // "" always true
    private static boolean checkCondition(String condition, Sentence sentence) {
        if(condition.equals("")) {
            return true;
        }

        Term term = sentence.term;

        if(condition.equals("F")) {
            return term instanceof Implication && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        }
        else if(condition.equals("S")) {
            return term instanceof Conjunction && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        }
        else if(condition.equals("E")) {
            if(sentence.isEternal()) {
                return false;
            }

            return term instanceof Similarity || term instanceof Inheritance;
        }

        return false; // not defined - default value!
    }


    // instructions:
    // startIdxA : set idx of a at the beginning
    ///// startIdxB : set idx of b at the beginning
    // scanNextA : scan for next non interval in a and keep track of occurence time
    ///// incIdxB : increment idx of b
    // storeSeqSortedA : store event at current idx of a into the by time sorted list of events

    // checkEndA : check if idx is at end


    public static class Instr {
        public Instr(String mnemonic) {
            this.mnemonic = mnemonic;
            arg0 = null;
            arg0Int=0;
        }
        public Instr(String mnemonic, String arg0) {
            this.mnemonic = mnemonic;
            this.arg0 = arg0;
            arg0Int=0;
        }
        public Instr(String mnemonic, long arg0) {
            this.mnemonic = mnemonic;
            this.arg0Int = arg0;
            this.arg0 = null;
        }
        public final String mnemonic;
        public final String arg0;
        public final long arg0Int;
    }

    public static Instr[] programCombineSequenceAndEvent;
    public static Instr[] programCombineEventAndEvent;
    public static Instr[] programTranslateSequenceToWindowedSequence; // translate sequence in a to a sequences with parallel windowed events


    static {
        { // program to combine a sequences and a event into one ordered sequence
            programCombineSequenceAndEvent = new Instr[]{
                new Instr("label", "label_readA"),// label: read A

                new Instr("startIdxA"),

                new Instr("scanNextA"),
                new Instr("checkEndA"),
                new Instr("jmpTrue", "label_readB"),

                new Instr("storeSeqSortedA"),

                new Instr("jmp", -5),


                new Instr("label", "label_readB"),// label: read B

                new Instr("readB"),
                new Instr("storeSeqSortedB"),

                //new Instr("startIdxB"),

                //new Instr("checkEndB"),
                //new Instr("jmpTrue", "label_buildConclusion"),

                //new Instr("scanNextB"),
                //new Instr("storeSeqSortedB"),

                //new Instr("jmp", -5),


                new Instr("label", "label_buildConclusion"),// label: build conclusion


                new Instr("calcTruthUnionAB"), // compute the truth by taking the union of input A and input B

                // read out result array and write to conclusion sequence conjunction
                new Instr("m_writeConjunction")
            };
        }



        { // program to combine a event and a event into one ordered sequence
            programCombineEventAndEvent = new Instr[]{
                new Instr("readA"),
                new Instr("storeSeqSortedA"),

                new Instr("readB"),
                new Instr("storeSeqSortedB"),

                new Instr("calcTruthUnionAB"), // compute the truth by taking the union of input A and input B

                // read out result array and write to conclusion sequence conjunction
                new Instr("m_writeConjunction")
            };
        }

        { // program to combine a sequences and a event into one ordered sequence
            programTranslateSequenceToWindowedSequence = new Instr[]{
                new Instr("label", "label_readA"),// label: read A

                new Instr("startIdxA"),

                new Instr("scanNextA"),
                new Instr("checkEndA"),
                new Instr("jmpTrue", "label_buildConclusion"),

                new Instr("storeSeqSortedA"),

                new Instr("jmp", -5),

                new Instr("label", "label_buildConclusion"),// label: build conclusion


                new Instr("calcTruthIdA"), // compute the truth by taking the truth of input A

                // read out result array and write to conclusion sequence conjunction
                new Instr("m_writeWindowedConjunction")
            };
        }

    }
}
