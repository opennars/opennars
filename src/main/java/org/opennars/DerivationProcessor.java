package org.opennars;

import org.opennars.entity.Sentence;
import org.opennars.inference.TemporalRules;
import org.opennars.language.Conjunction;
import org.opennars.language.Implication;
import org.opennars.language.Term;

public class DerivationProcessor {

    // process a program on two sentences for some temporal inference to derive a conclusion
    // /param condA condition
    // /param condB condition
    // /param program program to get processed instruction by instruction
    // /param a premise a
    // /param b premise b
    public void processProgramForTemporal(String condA, String condB, Instr[] program, Sentence a, Sentence b) {
        if (!checkCondition(condA, a.term) || !checkCondition(condB, b.term)) {
            return; // ignore because any condition didn't match up
        }

        // TODO< keep track of result sorted by occurence time >

        // TODO< interpret program instruction by instruction and keep track of indices in premises >
    }

    // encoding of condition
    // "F" temporal forward implication
    // "S" temporal sequence
    // "" always true
    private static boolean checkCondition(String condition, Term term) {
        if(condition.equals("F")) {
            return term instanceof Implication && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        }
        else if(condition.equals("S")) {
            return term instanceof Conjunction && term.getTemporalOrder() == TemporalRules.ORDER_FORWARD;
        }
        else if(condition.equals("")) {
            return true;
        }
        return false; // not defined - default value!
    }


    // instructions:
    // startIdxA : set idx of a at the beginning
    ///// startIdxB : set idx of b at the beginning
    // scanNextA : scan for next non interval in a and keep track of occurence time
    ///// incIdxB : increment idx of b
    // storeSeqSortedA : store event at current idx of a into the by time sorted list of events



    static class Instr {
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

    static {
        { // program to combine two sequences into one ordered one
            new Instr("label", "label_readA");// label: read A

            new Instr("startIdxA");

            new Instr("checkEndA");
            new Instr("jmpTrue", "label_readB");

            new Instr("scanNextA");
            new Instr("storeSeqSortedA");

            new Instr("jmp", -5);


            new Instr("label", "label_readB");// label: read B

            new Instr("startIdxB");

            new Instr("checkEndB");
            new Instr("jmpTrue", "label_buildConclusion");

            new Instr("scanNextB");
            new Instr("storeSeqSortedB");

            new Instr("jmp", -5);



            new Instr("label", "label_buildConclusion");// label: build conclusion

            // TODO< read out result array and write to conclusion sequence conjunction >
        }




    }
}
