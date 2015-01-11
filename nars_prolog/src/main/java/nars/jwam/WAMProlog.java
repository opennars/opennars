/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.jwam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nars.jwam.datastructures.Convert;

/**
 * Main interface to a WAM Prolog context (rulebase, knowledgebase, etc..)
 */
public class WAMProlog {
    final WAM w;

    /** small-sized virtual machine */
    public static WAMProlog newSmall() {
        WAM small = new WAM(5000, 40, 1000, 100, 50, 3);        
        return new WAMProlog(small);
    }
    
    /** medium-sized, about 2x as large for each parameter as "small" */
    public static WAMProlog newMedium() {
        WAM medium = new WAM(8192, 80, 2000, 200, 100, 6);        
        return new WAMProlog(medium);
    }
    
    public WAMProlog(WAM w) {
        this.w = w;
    }

    /** resets, compiles, and runs a theory (prolog source code string) */
    public WAMProlog setTheory(String theorySource) {        
        w.getCompiler().compile_string(theorySource).commit();
        return this;
    }

    public class Query implements Iterable<Answer> {
        
        List<Answer> answers = new ArrayList();
        private int[] compiled;
        private String query;
        
        public Query(String queryString, int[] compiledQuery) {
            this.query = queryString;
            this.compiled = compiledQuery;
        }

        /** get next answer, without time limit */
        public Answer nextAnswer() {
            return nextAnswer(0);
        }
        
        /** get next answer */
        public Answer nextAnswer(double time) {
            
            if (!Arrays.equals(w.getQuery(), compiled)) {
                //need to (re-)execute because a different query was previously set
                //TODO optionally through an exception if interleaved queries
                w.setQuery(compiled);
            }
            
            boolean success = w.execute(time);
            
            Answer a = new Answer(); 
            answers.add(a);
            return a;
        }

        /** iterate all answers saved so-far */
        @Override public Iterator<Answer> iterator() {
            return answers.iterator();
        }    

        
    }
    
    public class Answer extends HashMap<String,Object> {
        public final boolean success;

        /** create an answer from the current WAM state */
        public Answer() {
            super(w.rules().getQueryVars().size());
            
            this.success = !w.hasFailed();
            
            for (Map.Entry<Integer, String> e : w.rules().getQueryVars().entrySet()) {  
                int i = e.getKey();
                String var = e.getValue();
                Object value = Convert.cellToObject(w, w.getStorage(), w.getHeapSize());
                put(var, value);
            }
        }

        
        /**
        * Obtain the result of a query from the WAM.
        *
        * @param w The WAM.
        * @return The result of the last query.
        */
        public String toString(WAM w) {
            StringBuilder r = new StringBuilder();
            //int[] instr = w.getRuleHeap().getQueryInstructions();
            //int amount_perm = WAM.instruction_arg(instr[instr.length - 3]);
            for (Entry<String, Object> e : entrySet()) {
                String var = e.getKey();
                Object val = e.getValue();
                r.append( var );
                r.append( ":\t" );
                r.append( val );
                r.append( "\r\n" );
            }
            return r.append(Boolean.toString(success)).toString();
        }    
    
    }
    
    public Query query(String query) {
        w.prepare_for_new_query();
        
        int[] q = w.getCompiler().compile_query(query);       
        
        return new Query(query, q);
    }
    
   public static void printRules(WAM w) {
        System.out.println(w.rules().toString(w.strings(),w.numbers(),w.regStart()));          
    }
        
    
}
