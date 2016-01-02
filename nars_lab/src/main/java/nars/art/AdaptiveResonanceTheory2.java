//
// Translated by CS2J (http://www.cs2j.com)
//
package nars.art;

import java.util.ArrayList;
import java.util.Random;

/**
 * code from https://web.archive.org/web/20120109162743/http://users.visualserver.org/xhudik/art
 * 
 * 
 */
public class AdaptiveResonanceTheory2   
{
    /**
             * Count score (similarity) how similar inst and prot are.
             * The output (similarity) will be their dot product 
             * \param inst it is some instance (example, Ek)
             * \param prot it is some prototype
             **/
    private static float countScore(DynamicVector<Float> prototype, DynamicVector<Float> instance) {
        float score;
        int i;
        score = 0.0f;
        for (i = 0;i < prototype.array.length;i++)
        {
            score += instance.get___idx(i) * prototype.get___idx(i);
        }
        return score;
    }

    /**
             * Add an example (Ek) to a particular cluster.
             * It means that it moves the prototype toward the example.
             * The prototype will be more similar with the example.
             * P'=sqrt( sum_i((1-beta)*Pi + beta*Eki)^2 )
             * \param inst Ek
             * \param prot some prototype
             * \param beta it is given by an user
             * */
    private static void addInstance(DynamicVector<Float> instance, DynamicVector<Float> prototype, float beta) {
        DynamicVector<Float> temp;
        float norm;
        int i;
        //System.Diagnostics.Debug.Assert(beta <= 0.0f && beta <= 1.0f);
        //System.Diagnostics.Debug.Assert(instance.array.Length == prototype.array.Length);
        norm = 0.0f;
        
        try {
            temp = new DynamicVector<>(prototype.array.length);
        }
        catch( Exception ex ) {
            throw new RuntimeException("array ctor exception");
        }
        
        for (i = 0;i < instance.array.length;i++)
        {
            // make vector  tmp=(1-beta)*P + beta*Ek
            temp.set___idx(i,(1.0f - beta) * prototype.get___idx(i) + beta * instance.get___idx(i));
        }
        for (i = 0;i < instance.array.length;i++)
        {
            // count vector norm semi = sqrt(tmp^2)
            norm += temp.get___idx(i) * temp.get___idx(i);
        }
        norm = (float) Math.sqrt(norm);
        //System.Diagnostics.Debug.Assert(norm != 0.0f);
        norm = 1.0f / norm;
        for (i = 0;i < instance.array.length;i++)
        {
            // count prototype
            prototype.set___idx(i,norm * temp.get___idx(i));
        }
    }

    /**
             * Removing an instance(Ek) from the particular prototype.
             * Remove the instance with index 'iinst' in 'sample' from prototype
             * with index 'iprot' in 'prot'. But also remove particular index 
             * from prototype sequence
             **/
    private static void removeInstance(ArrayList<DynamicVector<Float>> sample, int iinst, ArrayList<DynamicVector<Float>> prot, int iprot, ArrayList<ArrayList<Integer>> seq, float beta, float vigilance) {
        int i;
        for (i = 0;i < seq.get(iprot).size();i++)
        {
            // find and erase in the prototype sequence the instance which should be deleted
            if (seq.get(iprot).get(i) == iinst)
            {
                seq.get(iprot).remove(i);
                break;
            }
             
        }
        // if the particular prototype is empty now - delete whole prototype
        // delete also line (prototype) in prototype sequence
        if (seq.get(iprot).isEmpty())
        {
            prot.remove(iprot);
            seq.remove(iprot);
        }
        else
        {
            // if it is not empty - re-create it from the rest examples
            float score;
            // build prototype but without instance which should be deleted
            // at first -- prototype is the first item in the prototype sequence
            prot.set(iprot, sample.get(seq.get(iprot).get(0)));
            // if PE < vigilance -- it won't stop (infinite looping)
            score = countScore(sample.get(seq.get(iprot).get(0)), sample.get(seq.get(iprot).get(0)));
            if (score < vigilance)
            {
                float tmpv = vigilance;
                vigilance = score;
            }
             
            for (i = 1;i < seq.get(iprot).size();i++)
            {
                //cerr << "\nWARNING: vigilance is too high (" << tmpv << "). What means infinite looping!!!\n";
                //cerr << "Vigilance was decreased: vigilance=" << vigilance << endl;
                // continually add others examples
                // it started from 2nd member because the first is already in
                addInstance(sample.get(seq.get(iprot).get(i)), prot.get(iprot), beta);
            }
        } 
    }

    /**
             * Create a new prototype and also create a new sequence in prot_seq
             * One line in prot_seq = one cluster represented by a prototype
             * \param inst set of all examples
             * \param iinst ID of particular Ek
             * \param prot set of prototypes
             * \param prot_seq set of all prototypes with indexes of member's Ek
             * \param vigilance it is set by an user
             * */
    private static void createPrototype(ArrayList<DynamicVector<Float>> inst, int iinst, ArrayList<DynamicVector<Float>> prot, ArrayList<ArrayList<Integer>> prot_seq, float vigilance) {
        float score;
        ArrayList<Integer> new_seq = new ArrayList<>();
        // if PE < vigilance -- it won't stop
        score = countScore(inst.get(iinst), inst.get(iinst));
        if (score < vigilance)
        {
            float tmpv;
            if ((score - vigilance) < 0.0001f)
            {
                tmpv = vigilance;
                vigilance = vigilance - (0.0001f + 0.0001f * vigilance);
            }
            else
            {
                tmpv = vigilance;
                vigilance = score;
            } 
            //    cerr << "\nWARNING: vigilance is too high (" << tmpv << "). What means infinite looping!!!\n";
            //    cerr << "Vigilance was decreased: vigilance=" << vigilance << endl;
            int x = 0;
        }
         
        // for breakpoint
        // create a new prototype
        prot.add(inst.get(iinst));
        // create a new prototype sequence and insert the first index of instance
        new_seq = new ArrayList<>();
        new_seq.add(iinst);
        prot_seq.add(new_seq);
    }

    /** 
             * Returns a prototype with highest similarity (score) -- which was not used yet.
             * The score is counted for a particular instance Ek and all the prototypes.
             * If it is returned empty prototype -- was not possible (for some rule) to find the best
             * @param inst example Ek
             * @param prot set of prototypes
             * @param used set of already tested prototypes
             **/
    private DynamicVector<Float> bestPrototype2A(DynamicVector<Float> inst, ArrayList<DynamicVector<Float>> prot, ArrayList<DynamicVector<Float>> used) {
        // prototypes with the same score
        ArrayList<DynamicVector<Float>> sameScore = new ArrayList<>();
        DynamicVector<Float> empty;
        int usize;
        int psize;
        float[] score;
        int i, j;
        float higher;
        sameScore = new ArrayList<>();
        empty = new DynamicVector<>(0);
        // ASK< is size 0 right? >
        usize = used.size();
        psize = prot.size();
        // if the number of already used prototypes and the number of
        //  prototypes are the same return empty protot. (no best protot.)
        if (used.size() == prot.size())
        {
            return empty;
        }
         
        score = new float[psize];
        for (i = 0;i < psize;i++)
        {
            // setting initial value(the minimum for type double for this particular architecture) for scoring prototypes
            score[i] = Float.MIN_VALUE;
        }
        for (i = 0;i < psize;i++)
        {
            // set score for every prototype
            boolean usedb;
            // search if prototype is not among already used prototypes
            usedb = false;
            for (j = 0;j < usize;j++)
            {
                if (prot.get(i).equals(used.get(j)))
                {
                    usedb = true;
                    break;
                }
                 
            }
            // is proto[i] among the used ??
            if (usedb)
            {
            }
            else
            {
                // if not count it's score
                score[i] = countScore(prot.get(i), inst);
            } 
        }
        //find prototype with highest score
        higher = Float.MIN_VALUE;
        for (i = 0;i < psize;i++)
        {
            if (score[i] == higher)
            {
                sameScore.add(prot.get(i));
            }
            else
            {
                if (score[i] > higher)
                {
                    // erase the old list
                    sameScore.clear();
                    sameScore.add(prot.get(i));
                    higher = score[i];
                }
                 
            } 
        }
        if (sameScore.isEmpty())
        {
            return empty;
        }
        else // the result is an empty prototype
        if (sameScore.size() == 1)
        {
            return sameScore.get(0);
        }
        else
        {
            // the result is the only one possible best prototype
            int index;
            // if there is more best prototypes with the same score -- random choosing
            index = random.nextInt(sameScore.size());
            return sameScore.get(index);
        }  
    }

    /**
         * In the structure Clust are stored all the results.<br>
         * More specifically: prototypes, fluctuation (error) and sequence
         * of all examples for each prototype
         **/
    public static class Clust   
    {
        /** 
                 * proto is a set of created prototypes
                 */
        public ArrayList<DynamicVector<Float>> proto = new ArrayList<>();
        /** 
                * proto_seq it is a sequence of sequences (a matrix). Where each line
                * represents one prototype and each column in the line represents some
                * example's ID.<br>
                * Example:<br>
                * 1 2 4<br>
                * 7 3 5
                *<br><br>
                * The first prototype consists of the ID's examples: 1, 2 and 3<br>
                * The second cluster consist of the following examples: 7, 3 and 5<br>
                * An example with ID 5 is a vector. In fact, it is an input line
                */
        public ArrayList<ArrayList<Integer>> proto_seq = new ArrayList<>();
        /** 
                * How many examples
                * were re-assign (they are in a different cluster then they were before) 
                * */
        public float fluctuation;
    }

    /**
         * In this structure are stored all input parameters important for run every art algorithm. 
         * They can be given by user or they are set as default
         **/
    public static class in_param   
    {
        /** 
                * Input parameter beta (-b) in ART 1 is a small positive integer. It influences a number of created clusters. The higher value the 
                * higher number of created clusters. The default is 1.<br>
                * For another ART implementations (based on real value input) <em>beta</em> is a learning constant. It has a range [0, 1]. The default
                * is 0.5
                * */
        public float beta;
        /**
               * positive integer or 0 - skip the last n columns (in input examples) -- default value:0
               * */
        int skip;
        /**
                * Input parameter vigilance (-v) together with alpha (ART for real numbers input) or beta (ART 1) set up a similarity threshold. 
                * This threshold influence a minimum similarity under which an example Ek will be accepted by prototype. The higher value 
                * the higher number of clusters. It has a range [0,1]. The default is 0.1.
                * */
        public float vigilance;
        /** 
                * Input parameter theta (-t) denoising parameter. If a value Ek_i (Ek is a example and
                * it's ith column) is lower than theta then the number will be changed to 0. It is used only by ART 2A. It's range:
                * [0,1/dim^-0.5]. Default 0.00001
                * */
        float theta = 0.00001f;
        /** 
                * Input parameter alpha (-a)  is used by real value ART algorithms. Together with vigilance set up a similarity threshold. 
                * This threshold influences a minimum similarity which is necessary for the example Ek to be accepted by the prototype. 
                * The range: [0,1/sqrt(dim)] where dim is a number of dimensions. The default is 1/sqrt(dim) * 1/2
                * */
        public float alpha;
        /** 
                * Input parameter distance (-d) set up a distance measure:
                *   <ol><li> Euclidean distance
                *    <li>Modified Euclidean distance -- it is in a testing mode. Euclidean distance use equation 1 - E/dim where E is Euclidean distance
                *    and dim is a number of dimensions. Modified Euclidean use equation log(dim^2) - E. This distance in some cases can achieve a better
                *    performance than standard Euclidean distance. However, it is recommended to use standard Euclidean distance. DO NOT USE IT
                *    <li> Manhattan distance
                *    <li> Correlation distance
                *    <li> Minkowski distance
                *   </ol>
                *    It works only for art_distance. Default Euclidean distance measure
                **/
        int distance;
        /** 
                * Input parameter power (-p) it is used only for Minkowski distance in art_distance. It set up the power for Minkowski 
                * distance measure. The default is 3. Minkowski with the power 1 is Manhattan distance. Minkowski with power 2 is 
                * Euclidean distance
                * */
        int power;
        /**
                * An input parameter --  a number of passes (-E), it is a maximum number of how many times an example Ek 
                * can be re-assigned. If it reach this number the program will stop. The default is 100
                * */
        public int pass;
        /** 
                * An input parameter -- fluctuation (-e), it is a highest possible error rate (%). It means a maximum
                * number (in %) of how many instances can be re-assign. If the real fluctuatio is lower than -e
                * then program will stop. Default is 5% examples.
                * */
        public float error;
    }

    /**
             * ART 2A algorithm, inputs: examples and input parameters given by an user
             * How exactly it is working can be found at www.fi.muni.cz/~xhudik/art/drafts
             * \param sample  set if input examples (Eks)
             * \param par all input parameters set by an user or default
             **/
    public void art2A(ArrayList<DynamicVector<Float>> sample, in_param param, Clust results) {
        // prototype with highest score
        DynamicVector<Float> P;
        // list of all prototypes
        ArrayList<DynamicVector<Float>> prot = new ArrayList<>();
        // the best representation of the prototypes of the whole history
        ArrayList<DynamicVector<Float>> prot_best = new ArrayList<>();
        // sequences of samples Ek from which prototype has been created
        // it is possible to reconstruct a prototype from the sequence
        // defined in art_common.h
        ArrayList<ArrayList<Integer>> prot_seq = new ArrayList<>();
        // the best representation of the prototypes of the whole history
        ArrayList<ArrayList<Integer>> prot_seq_best = new ArrayList<>();
        // list of prototypes which were used already
        ArrayList<DynamicVector<Float>> used = new ArrayList<>();
        used = new ArrayList<>();
        prot = new ArrayList<>();
        prot_seq = new ArrayList<>();
        prot_best = new ArrayList<>();
        prot_seq_best = new ArrayList<>();
        float fluctuation = 100.0f;
        // the lowest error of the whole history
        // it is initialized as some impossible number(higher than 100% can't be), to avoid problems with first iteration
        float fluctuation_best = 120.0f;
        // how many times it run throughout the samples
        int pass = 0;
        // how many Ek's has been reassign to other cluster (prototype) in a previous pass (run)
        ArrayList<Boolean> changed = new ArrayList<>();
        int i, j;
        changed = new ArrayList<>();
        for (i = 0;i < sample.size();i++)
        {
            changed.add(true);
        }
        while ((pass < param.pass) && (fluctuation > param.error))
        {
            // do cycle while error is higher than the parameter -e  or a number of passes is lower than the parameter -E
            int number_changed;
            for (i = 0;i < sample.size();i++)
            {
                // nullifying changed values
                changed.set(i, false);
            }
            for (i = 0;i < sample.size();i++)
            {
                // cycle for instances
                // zeroing 'used' prototypes
                used.clear();
                do
                {
                    float score;
                    float alphaSum;
                    // find the best prototype for current Ek
                    P = bestPrototype2A(sample.get(i), prot, used);
                    // if there is no best prototype
                    if (P.array.length == 0)
                    {
                        int prototypeIndex;
                        //check if the instance is not included already in some other prototype
                        prototypeIndex = Common.instanceInSequence(prot_seq, i);
                        if (prototypeIndex != -1)
                        {
                            //if so, remove it (recreate prototype--without the instance)
                            removeInstance(sample, i, prot, prototypeIndex, prot_seq, param.beta, param.vigilance);
                        }
                         
                        createPrototype(sample, i, prot, prot_seq, param.vigilance);
                        changed.set(i, true);
                        break;
                    }
                     
                    // add P among 'used'
                    used.add(P);
                    //count similarity between P and Ek (it is called "score") and alpha*sum_i Eki
                    score = countScore(P, sample.get(i));
                    alphaSum = 0.0f;
                    for (j = 0;j < sample.get(i).array.length;j++)
                    {
                        alphaSum += param.alpha * sample.get(i).get___idx(j);
                    }
                    // if similarity is sufficient -- sample[i] is member of the P
                    if (score >= alphaSum)
                    {
                        if (score >= param.vigilance)
                        {
                            int prot_index;
                            int Pindex;
                            // if the example Ek is already included in some prototype -- find it
                            prot_index = Common.instanceInSequence(prot_seq, i);
                            if (prot_index != -1)
                            {
                                // test if the found prototype is not actual one (P) in that case try - go for another Ek
                                if (prot.get(prot_index).equals(P))
                                {
                                    break;
                                }
                                else
                                {
                                    // re-build prototype - without the sample
                                    removeInstance(sample, i, prot, prot_index, prot_seq, param.beta, param.vigilance);
                                } 
                            }
                             
                            // find an index of P in prototypes
                            Pindex = Common.findItem(prot, P, true);
                            // add instance to the current prototype
                            addInstance(sample.get(i), prot.get(Pindex), param.beta);
                            prot_seq.get(Pindex).add(i);
                            changed.set(i, true);
                            break;
                        }
                        else
                        {
                        }
                    }
                    else
                    {
                        // try other best P
                        //score=>alphaSize
                        int prot_index;
                        // if prototype is not enough similar to the example(sample[i]) then create a new prototype
                        // check if the instance is not already in some other prototype
                        prot_index = Common.instanceInSequence(prot_seq, i);
                        if (prot_index != -1)
                        {
                            // if so, remove it (recreate prototype--without the instance)
                            removeInstance(sample, i, prot, prot_index, prot_seq, param.beta, param.vigilance);
                        }
                         
                        createPrototype(sample, i, prot, prot_seq, param.vigilance);
                        changed.set(i, true);
                        break;
                    } 
                }
                while (prot.size() != sample.size());
            }
            // for sample
            //count statistics for this pass
            number_changed = 0;
            for (j = 0;j < changed.size();j++)
            {
                if (changed.get(j))
                {
                    number_changed++;
                }
                 
            }
            fluctuation = ((float)number_changed / sample.size()) * 100;
            pass++;
            //cout << "Pass: " << pass <<", fluctuation: " << fluctuation << "%" << ", clusters: " << prot.size() << endl;
            //test if this iteration has not lower error
            if (fluctuation < fluctuation_best)
            {
                //if it is so - assign the new best results
                prot_best = prot;
                prot_seq_best = prot_seq;
                fluctuation_best = fluctuation;
            }
             
        }
        // while
        // create results
        results.proto = prot_best;
        results.proto_seq = prot_seq_best;
        results.fluctuation = fluctuation_best;
    }

    private final Random random = new Random();
}


