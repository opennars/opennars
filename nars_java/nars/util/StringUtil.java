package nars.util;

import java.lang.reflect.Field;
import nars.util.rope.Rope;



public class StringUtil {
    
    /** Half-way between a String and a Rope; concatenates a list of strings into an immutable CharSequence which is either:
     *  If a component is null, it is ignored.
     *  if total non-null components is 0, returns null
     *  if total non-null components is 1, returns that component.
     *  if the combined length <= maxLen, creates a StringBuilder appending them all.
     *  if the combined length > maxLen, creates a Rope appending them all.     
     */
    public static CharSequence yarn(int maxLen, CharSequence... components) {
        int totalLen = 0;
        int total = 0;
        CharSequence lastNonNull = null;
        for (final CharSequence s : components)
            if (s!=null)  {
                totalLen += s.length();
                total++;
                lastNonNull = s;
            }
        
        if (total == 0)
            return null;
        
        if (total == 1) {
            //System.err.println("        same: " + lastNonNull);
            return lastNonNull;
        }
        
        if (totalLen <= maxLen) {
            StringBuilder sb = new StringBuilder(totalLen);
            for (final CharSequence s : components)
                if (s!=null)
                    sb.append(s);
            //System.err.println("stringbuffer: " + sb);
            return sb;
        }
        else {
            Rope r = Rope.catFast(components);
            //System.err.println("        rope: " + r);
            return r;
        }
    }
    
    static final Field val, sbval;
    static {
        Field sv = null, sbv = null;
        try {
            sv = String.class.getDeclaredField("value"); 
            //o = String.class.getDeclaredField("offset");
            sbv = StringBuilder.class.getSuperclass().getDeclaredField("value");
            
            sv.setAccessible(true); 
            sbv.setAccessible(true);
            //o.setAccessible(true);         
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        val = sv;        
        sbval = sbv;
    }
    
    /** Change the first min(|s|, |t|) characters of s to t
        TODO must reset the hashcode field
        TODO test
      */
    public static void overwrite(String s, String t) {
        try {
            //int offset   = off.getInt(s);
            char[] value = (char[]) val.get(s);
            for (int i = 0; i < Math.min(s.length(), t.length()); i++) 
                value[i] = t.charAt(i);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    } 

    /**
     * Warning: don't modify the return char[] because it will beinconsistent with s.hashCode()
     * @param String to invade
     * @return the private char[] field in String class
     */
    public static char[] getCharArray(String s) {
         try {            
            return (char[]) val.get(s);
        } catch (Exception ex) {
            ex.printStackTrace();            
        }   
        return null;
    }

    public static char[] getCharArray(StringBuilder s) {
         try {            
            return (char[])sbval.get(s);
        } catch (Exception ex) {
            ex.printStackTrace();            
        }   
        return null;
    }
    
    
/*
    public static void main(String[] args) { 
        String s = "Immutable"; 
        String t = "Notreally"; 

        mutate(s, t);
        StdOut.println(t); 

        // strings are interned so this doesn't even print "Immutable" (!)
        StdOut.println("Immutable");
    } 
*/
    
    /**
     * @author http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     */
    public static int levenshteinDistance(final String s0, final String s1) {
        int len0 = s0.length() + 1;                                                     
        int len1 = s1.length() + 1;                                                     

        // the array of distances                                                       
        int[] cost = new int[len0];                                                     
        int[] newcost = new int[len0];                                                  

        // initial cost of skipping prefix in String s0                                 
        for (int i = 0; i < len0; i++) 
            cost[i] = i;                                     

        // dynamicaly computing the array of distances                                  

        // transformation cost for each letter in s1                                    
        for (int j = 1; j < len1; j++) {                                                
            // initial cost of skipping prefix in String s1                             
            newcost[0] = j;                                                             

            // transformation cost for each letter in s0                                
            for(int i = 1; i < len0; i++) {                                             
                // matching current letters in both strings                             
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;             

                // computing cost for each transformation                               
                int cost_replace = cost[i - 1] + match;                                 
                int cost_insert  = cost[i] + 1;                                         
                int cost_delete  = newcost[i - 1] + 1;                                  

                // keep minimum cost                                                    
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }                                                                           

            // swap cost/newcost arrays                                                 
            int[] swap = cost; cost = newcost; newcost = swap;                          
        }                                                                               

        // the distance is the cost for transforming all letters in both strings        
        return cost[len0 - 1];                                                          
    }    
}
