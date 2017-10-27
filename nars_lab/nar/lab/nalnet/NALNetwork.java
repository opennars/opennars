/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nar.lab.nalnet;

import com.google.common.primitives.Longs;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import nars.NAR;
import nars.config.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Negation;
import nars.language.Term;

/**
 *
 * @author Patrick
 */

public class NALNetwork
{
    
    public class NALNode
    {
        public Term term;
        public TruthValue truth;
        public long[] evidentalBase;
        public NALNode[] neighbours; //the "neighbours" the truth calculation is based on
        public boolean negated;
        
        //A NALNode can be a term with a certain truth
        public NALNode(Term term, TruthValue truth, long base) {
            this(term, truth, base, false);
        }
        public NALNode(Term term, TruthValue truth, long base, boolean negated) {
            this.negated = negated;
            if(negated) {
                this.term = Negation.make(term);
                this.truth = TruthFunctions.negation(truth);
            } else {
                this.term = term;
                this.truth = truth;
            }
            this.evidentalBase = new long[] { base };
        }
        //or is a composition of neighbours
        public NALNode(NALNode[] neighbours) {
            this(neighbours, false);
        }
        public NALNode(NALNode[] neighbours, boolean negated) {
            this.neighbours = neighbours;
            this.negated = negated;
        }
        
        boolean calculated = false;
        public TruthValue calculate() {
            calculated = true;
            if(truth != null) {
                return truth;
            }
            List<Term> components = new LinkedList<Term>();
            
            HashSet<Long> evidence_bases = new HashSet<Long>();
            for(int i=0;i<neighbours.length;i++) {
                if(neighbours[i].calculate() == null) {
                    continue;
                }
                TruthValue t = neighbours[i].truth;
                Term component = neighbours[i].term;
                if(truth == null) {
                    truth = t;
                    evidence_bases.addAll(Longs.asList(neighbours[i].evidentalBase));
                    components.add(component);
                } else {
                    if(!Stamp.baseOverlap(Longs.toArray(evidence_bases), neighbours[i].evidentalBase)) {
                        truth = TruthFunctions.intersection(truth, t);
                        evidence_bases.addAll(Longs.asList(neighbours[i].evidentalBase));
                        components.add(component);
                    }
                }
            }
            this.evidentalBase = Longs.toArray(evidence_bases);
            if(this.neighbours.length > 0) {
                try {
                    this.term = Conjunction.make(components.toArray(new Term[0]), TemporalRules.ORDER_CONCURRENT);
                }catch(Exception ex){}
            }
            if(this.negated) {
                this.term = Negation.make(this.term);
                this.truth = TruthFunctions.negation(this.truth);
            }
            return this.truth;
        }
        
        @Override
        public String toString() {
            if(this.truth == null || this.term == null) {
                if(calculated) {
                    return "No connection to input evidence";
                } else {
                    return "Node not evaluated";
                }
            }
            String evidences = "";
            for(long s : this.evidentalBase) {
                evidences += s + ",";
            }
            if(!evidences.isEmpty()) {
                evidences = evidences.substring(0, evidences.length()-1);
            }
            return this.term.toString() + ". :|: " + this.truth.toString() + " {" + evidences + "}";
        }
        
        public void inputInto(NAR nar) {
            calculate();
            Stamp stamp = new Stamp(nar.memory);
            stamp.setOccurrenceTime(nar.memory.time());
            Sentence sentence = new Sentence(this.term, 
                                             Symbols.JUDGMENT_MARK, 
                                             this.truth, 
                                             stamp);
            Task task = new Task(sentence, 
                                 new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,
                                                 Parameters.DEFAULT_JUDGMENT_DURABILITY,
                                                 BudgetFunctions.truthToQuality(this.truth)),
                                 true);
            nar.addInput(task);
        }
    }
    
    Random rnd = new Random();
    public class NALNet 
    {    
        public boolean[][] negated;
        public NALNet(boolean[][] negated) { //how many layers and what amount of nodes per layer?
            this.negated = negated;
        }
        
        public float[] input(float[] input_frequencies) { //default confidence
            TruthValue[] input_values = new TruthValue[input_frequencies.length];
            for(int i=0; i<input_frequencies.length; i++) {
                input_values[i] = new TruthValue(input_frequencies[i], Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
            }
            TruthValue[] output_values = input(input_values);
            float[] output_frequencies = new float[output_values.length];
            for(int i=0; i<output_values.length; i++) {
                output_frequencies[i] = output_values[i].getFrequency();
            }
            return output_frequencies;
        }

        NALNode[] outputs;
        TruthValue[] output_truths;
        Term[] output_terms;
        public TruthValue[] input(TruthValue[] input_values) {
            if(output_truths != null) {
                return output_truths;
            }
            NALNode[][] network = new NALNode[this.negated.length][];
            NALNode[] inputs = new NALNode[this.negated[0].length];
            for(int i=0; i<inputs.length; i++) {
                inputs[i] = new NALNode(new Term("input" + (i+1)),input_values[i], i+1, this.negated[0][i]);
            }
            network[0] = inputs;
            //ok create the succeeding layers too
            for(int layer = 1; layer < this.negated.length; layer++) {
                network[layer] = new NALNode[this.negated[layer].length];
                //whereby each node in next layer is connected to a subset of previous
                for(int i=0; i<network[layer].length; i++) {
                    List<NALNode> toAdd = new LinkedList<NALNode>();
                    for(NALNode previous : network[layer-1]) {
                        toAdd.add(previous);
                    }
                    network[layer][i] = new NALNode(toAdd.toArray(new NALNode[0]), this.negated[layer][i]);
                }
            }
            //ok calculate the outputs now:
            outputs = network[network.length-1];
            output_truths = new TruthValue[outputs.length];
            output_terms = new Term[outputs.length];
            for(int i=0; i<outputs.length; i++) {
                NALNode output_node = outputs[i];
                outputs[i] = output_node;
                output_truths[i] = output_node.calculate();
                if(output_truths[i] == null) { //it wasn't connected to anything
                    output_truths[i] = new TruthValue(0.0f,0.0f);
                }
                output_terms[i] = output_node.term;
            }
            return output_truths;
        }
    }
    
    public void demoNALNode() {
        NALNode node1 = new NALNode(new Term("input1"),new TruthValue(1.0f,0.9f),1);
        NALNode node2 = new NALNode(new Term("input2"),new TruthValue(0.4f,0.9f),2,true);
        NALNode node3 = new NALNode(new Term("input3"),new TruthValue(0.6f,0.9f),3);
        NALNode node4 = new NALNode(new Term("input4"),new TruthValue(0.6f,0.9f),4);
        NALNode result = new NALNode(new NALNode[]{node1, node2, node3, node4}, false);
        result.calculate();
        String res = result.toString();
        assert(res.equals("(&|,(--,input2),input1,input3,input4). :|: %0.22;0.66% {1,2,3,4}"));
        System.out.println(res);
    }
    
    //similar as before but with working with layers
    public void demoNALNet() {
        rnd.setSeed(1);
        NALNet nalnet = new NALNet(new boolean[][]{new boolean[]{false,true,false,false},new boolean[]{false}});
        float[] result = nalnet.input(new float[]{1.0f, 0.4f, 0.6f, 0.6f});
        for(int i=0;i<result.length;i++) {
            System.out.println(nalnet.outputs[i]);
        }
    }

    public static void main(String args[]) {
        NALNetwork nalnet = new NALNetwork();
        nalnet.demoNALNode();
        nalnet.demoNALNet();
    }
}
