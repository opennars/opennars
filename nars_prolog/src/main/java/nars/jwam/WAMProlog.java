/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.jwam;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import nars.jwam.datastructures.Convert;
import nars.jwam.parser.ParseException;

/**
 * Main interface to a WAM Prolog context (rulebase, knowledgebase, etc..)
 */
public class WAMProlog {
    final WAM w;

    /** small-sized virtual machine */
    public static WAMProlog newSmall() {        
        return new WAMProlog(WAM.newSmall());
    }
    
    /** medium-sized, about 2x as large for each parameter as "small" */
    public static WAMProlog newMedium() {        
        return new WAMProlog(WAM.newMedium());
    }
    
    public WAMProlog(WAM w) {
        this.w = w;
    }

    /** resets, compiles, and runs a theory (prolog source code string) */
    public WAMProlog setTheory(String theorySource) throws ParseException {        
        if (theorySource!=null && theorySource.length() > 0)
            w.getCompiler().compile_string(theorySource).commit();
        return this;
    }

    public interface Answering {
        /** returning false will cancel this answering process */
        public boolean onNextAnswer(Query q, Answer a);
    }

    public class Query implements Iterable<Answer> {
        
        List<Answer> answers = new ArrayList();
        private int[] compiled;
        private String query;
        
        public Query(String queryString, int[] compiledQuery) {
            this.query = queryString;
            this.compiled = compiledQuery;
        }

        public Answering getAnswers(Answering a, double maxTime) {
            Answer b;
            
            do {
                b = nextAnswer(maxTime);
            } while ((b!=null) && (a.onNextAnswer(this, b)));
            
            return a;
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
                //System.out.println("setting query: " + Arrays.toString(compiled));
                //System.out.println("setting query: " + Arrays.toString(compiled));
                w.setQuery(compiled);
            }
            else {
                //System.out.println("query already exist: " + Arrays.toString(compiled));
            }
            
            
            
            boolean success;
            try {
                success = w.execute(time);
            }
            catch (Exception e) {
                System.err.println(e + " when executing: " + this);
                //is this normal?
                success = false;
            }
                
            
            Answer a = new Answer(this); 
            answers.add(a);
            return a;
        }

        /** iterate all answers saved so-far */
        @Override public Iterator<Answer> iterator() {
            return answers.iterator();
        }    

        @Override
        public String toString() {
            return "Q[" + query + "]";
        }

        
        
    }
    
    public class Answer extends HashMap<String,Object> {
        public final boolean success;
        private final Query query;

        /** create an answer from the current WAM state */
        public Answer(Query question) {
            super(w.rules().getQueryVars().size());
            
            this.query = question;
            this.success = !w.hasFailed();
            
            for(int i : w.rules().getQueryVars().keySet()){
                String key = w.rules().getQueryVars().get(i);
                Object value = Convert.termToObject(w, w.getStorage(), w.getHeapSize()+12+i,true);
                put(key, value);
            }
            
        }
     

        @Override
        public String toString() {
            return "Answer[" + query + "|" + success + "|" + super.toString() + "]";
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
    
    public Query query(String query) throws ParseException {
        w.prepare_for_new_query();
        
        int[] q = w.getCompiler().compile_query(query);       
        
        return new Query(query, q);
    }
    
    public static void printRules(WAM w, PrintStream p) {
        p.println(w.rules().toString(w.strings(),w.numbers(),w.regStart()));       
    }
    
    public void printRules(PrintStream p) {
        printRules(w, p);
    }
        

    public WAM getWAM() {
        return w;
    }
    
}
