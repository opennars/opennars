/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nar.lab.nalnet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mxgraph.analysis.mxFibonacciHeap.Node;
import java.util.Arrays;
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
        
        //A NALNode can be a term with a certain truth
        public NALNode(Term term, TruthValue truth, long base) {
            this.term = term;
            this.truth = truth;
            this.evidentalBase = new long[] { base };
        }
        //or is a composition of neighbours
        public NALNode(NALNode[] neighbours) {
            this.neighbours = neighbours;
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
                neighbours[i].calculate();
                if(neighbours[i].truth == null) {
                    continue;
                }
                TruthValue t = neighbours[i].truth;
                Term component = null;
                if(t.getFrequency() < 0.5) { //local decision
                    component = Negation.make(neighbours[i].term);
                    t = TruthFunctions.negation(t);
                } else {
                    component = neighbours[i].term;
                }
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
            return this.truth;
        }
        
        @Override
        public String toString() {
            if(this.truth == null || this.term == null) {
                if(calculated) {
                    return "No connection to input evidence";
                } else {
                    return "Node not connected to input";
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
            Task task = new Task(sentence, new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY,
                                                           Parameters.DEFAULT_JUDGMENT_DURABILITY,
                                                           BudgetFunctions.truthToQuality(this.truth)));
            nar.addInput(task);
        }
    }
    
    Random rnd = new Random();
    public class NALNet 
    {    
        public int[] layers;
        float connectChance = 0.0f;
        public NALNet(int[] layers, float connectChance) { //how many layers?
            this.layers = layers;
            this.connectChance = connectChance;
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
            NALNode[][] network = new NALNode[this.layers.length][];
            NALNode[] inputs = new NALNode[this.layers[0]];
            for(int i=0; i<inputs.length; i++) {
                inputs[i] = new NALNode(new Term("input" + (i+1)),input_values[i], i+1);
            }
            network[0] = inputs;
            //ok create the succeeding layers too
            for(int layer = 1; layer < this.layers.length; layer++) {
                network[layer] = new NALNode[this.layers[layer]];
                //whereby each node in next layer is connected to a subset of previous
                for(int i=0; i<network[layer].length; i++) {
                    List<NALNode> toAdd = new LinkedList<NALNode>();
                    for(NALNode previous : network[layer-1]) {
                        if(rnd.nextDouble() < connectChance) {
                            toAdd.add(previous);
                        }
                    }
                    network[layer][i] = new NALNode(toAdd.toArray(new NALNode[0]));
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
        NALNode node2 = new NALNode(new Term("input2"),new TruthValue(0.4f,0.9f),2);
        NALNode node3 = new NALNode(new Term("input3"),new TruthValue(0.6f,0.9f),3);
        NALNode node4 = new NALNode(new Term("input4"),new TruthValue(0.6f,0.9f),4);
        NALNode result = new NALNode(new NALNode[]{node1, node2, node3, node4});
        result.calculate();
        String res = result.toString();
        assert(res.equals("(&|,(--,input2),input1,input3,input4). :|: %0.22;0.66% {1,2,3,4}"));
        System.out.println(res);
    }
    
    //similar as before but with working with layers
    public void demoNALNet() {
        rnd.setSeed(1);
        NALNet nalnet = new NALNet(new int[]{4,1}, 1.0f);
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
