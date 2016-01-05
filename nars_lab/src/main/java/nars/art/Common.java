//
// Translated by CS2J (http://www.cs2j.com)
//
package nars.art;

import java.util.ArrayList;

/**
     * code from https://web.archive.org/web/20120109162743/http://users.visualserver.org/xhudik/art
     * 
     */
public enum Common
{
    ;

    /**
             * 
             * returns -1 if it was not found
             */
    public static int instanceInSequence(ArrayList<ArrayList<Integer>> prototypesSequence, int instanceI) {
        int i, j, prototypeNumber;
        prototypeNumber = -1;
        for (i = 0;i < prototypesSequence.size();i++)
        {
            for (j = 0;j < prototypesSequence.get(i).size();j++)
            {
                // every prototype is compound of different number of instances
                if (prototypesSequence.get(i).get(j) == instanceI)
                {
                    prototypeNumber = i;
                    break;
                }
                 
            }
            if (prototypeNumber != -1)
            {
                break;
            }
             
        }
        return prototypeNumber;
    }

    /**
             * find a particular instance(example Ek) or prototype in a sequence
             * and give as a result the index of instance(prototype) in the sequence
             * item means instance or prototype.
             * If must_find is set up as true and the example (item) has not been found -- write error 
             * message and stop the program.
             * It is used by ART 1, ART 2A and ART 2A-C algorithms
             **/
    public static int findItem(ArrayList<DynamicVector<Float>> samples, DynamicVector<Float> instance, boolean mustFind) {
        int index, i;
        index = -1;
        for (i = 0;i < samples.size();i++)
        {
            if (samples.get(i).equals(instance))
            {
                index = i;
                break;
            }
             
        }
        if (!mustFind)
        {
            return index;
        }
        else
        {
            if (index != -1)
            {
                return index;
            }
            else
            {
                throw new RuntimeException("sample ... was not found in sequence.");
            } 
        } 
    }

}
