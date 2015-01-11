package nars.jwam.datastructures;

import java.util.HashMap;
import nars.jwam.WAM;

/**
 * This class is for maintaining String objects. The WAM operates on integers so
 * each String is converted to an integer. Via this class one can get the
 * integer of a String or the String that belongs to an integer.
 *
 * Note that currently all Strings of a WAM accumulate. Future work should
 * include fixing this. When dynamic predicates are retracted we could perform
 * some check to see whether a String is still in use.
 *
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class Strings {

    //TODO use BidiHashmap from Guava
    
    private final HashMap<Integer, String> intToString = new HashMap<Integer, String>(); // The maps that store the Strings and integers
    private final HashMap<String, Integer> stringToInt = new HashMap<String, Integer>();
    
    private int counter = 0;// Keeps track of how many Strings are known

    /**
     * Add a String to the maps if not present already. Returns a f/n integer
     * that contains the functor in the first 25 bits and the argument count in
     * the last 7.
     *
     * @param str The String to add.
     * @param args The amount of arguments.
     * @return A functor integer, first 25 bits is the functor, last 7 the
     * argument count.
     */
    public int add(String str, int args) {
        Integer r = stringToInt.get(str);
        int index;
        if (r == null) {
            index = counter++;
            stringToInt.put(str, index);
            intToString.put(index, str);
        }
        else
            index = r;
        return make_functor_int(index, args);
    }

    /**
     * Given a String, get the integer.
     *
     * @param s The String to search.
     * @return The integer by which the String is identified in the WAM.
     */
    public int getInt(String s) {
        Integer i = stringToInt.get(s);
        if (i == null)
            throw new RuntimeException("Not found: " + s);
        return i.intValue();
    }

    /**
     * Given an f/n integer, get the String in f/n format.
     *
     * @param i The integer to search, in f/n format: 25 bits String identity, 7
     * bits argument count.
     * @return The String which is identified by the integer.
     */
    public String get(int i) {        
        String a = intToString.get(WAM.functNR(i));
        String b = WAM.numArgsSuffix(i); 
        return new StringBuilder(a.length() + b.length()).append(a).append(b).toString();
    }

    /**
     * Given an f/n integer, get the String in f format (no arity included).
     *
     * @param i The integer to search, in f/n format: 25 bits String identity, 7
     * bits argument count.
     * @return The String which is identified by the integer.
     */
    public String getWithoutArity(int i) {
        return intToString.get(WAM.functNR(i));
    }

    /**
     * Reset the container.
     */
    public void reset() {
        intToString.clear();
        stringToInt.clear();
        counter = 0;
    }

    /**
     * Given a String identity and an arity, create a f/n integer. First 25 bits
     * is the String identity, last 7 the arity.
     *
     * @param functor_nr String identity.
     * @param arity Number of arguments (arity).
     * @return Functor integer.
     */
    public final static int make_functor_int(int functor_nr, int arity) {
        return (functor_nr << 7) | arity;
    }

    @Override
    public String toString() {
        return "" + intToString + " " + stringToInt;
    }
}
