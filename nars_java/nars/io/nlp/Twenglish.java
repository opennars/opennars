/*
 * Copyright (C) 2014 tc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.io.nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import nars.core.Memory;
import nars.core.Parameters;
import nars.core.control.AbstractTask;
import nars.entity.Task;
import nars.inference.TemporalRules;
import nars.io.narsese.Narsese;
import nars.io.narsese.Narsese.InvalidInputException;
import nars.io.nlp.Twokenize.Span;
import nars.language.Conjunction;
import nars.language.Instance;
import nars.language.Interval;
import nars.language.Product;
import nars.language.Property;
import nars.language.Term;

/**
 * Twitter English - english with additional tags for twitter-like content 
 */
public class Twenglish {

    public final ArrayList<String> vocabulary = new ArrayList<>();
    
    /** substitutions */
    public final Map<String,String> sub = new HashMap();
    private Memory memory;

    
    boolean languageBooted = true; //set to false to initialize on first twenglish input
    boolean inputProduct = false;
    boolean inputConjSeq = true;
    
    
    public Map<String,String> POS = new HashMap(){{
        //https://www.englishclub.com/grammar/parts-of-speech-table.htm
        
        put("i", "pronoun");
        put("it", "pronoun");
        put("them", "pronoun");
        put("they", "pronoun");
        put("we", "pronoun");
        put("you", "pronoun");
        put("he", "pronoun");
        put("she", "pronoun");
        put("some", "pronoun");
        put("all", "pronoun");
        put("this", "pronoun");
        put("that", "pronoun");
        put("these", "pronoun");
        put("those", "pronoun");
        
        put("is", "verb");

        put("who", "qpronoun");
        put("what", "qpronoun");
        put("where", "qpronoun");
        put("when", "qpronoun");
        put("why", "qpronoun");
        put("which", "qpronoun");
        
        put("to", "prepos");
        put("at", "prepos");
        put("before", "prepos");
        put("after", "prepos");
        put("on", "prepos");
        put("but", "prepos");
        
        put("and", "conjunc");
        put("but", "conjunc");
        put("or", "conjunc");
        put("if", "conjunc");
        put("while", "conjunct");
                
    }};
    
    public Twenglish() {
        //TODO use word tokenization so that word substitutions dont get applied across words.
        sub.put("go to", "go-to");
        //etc..
    }

    public Twenglish(Memory memory) {
        this.memory = memory;
    }

    protected List<AbstractTask> parseSentence(List<Span> s, Narsese narsese, boolean modifyVocabulary) {
        List<AbstractTask> l = new ArrayList();
        
        l.addAll( spansToSentenceTerms(s) );
        
        return l;
    }    
    
    public Collection<Task> spansToSentenceTerms(Collection<Span> s) {
        
        LinkedList<Term> t = new LinkedList();
        Span last = null;
        for (Span c : s) {
            t.add( spanToTerm(c) );
            last = c;
        }
        if (t.size() == 0) return Collections.EMPTY_LIST;
        
        String sentenceType = "fragment";
        if ((last!=null) && (last.pattern.equals("punct"))) {
            switch (last.content) {
                case ".": sentenceType = "judgment"; break;
                case "?": sentenceType = "question"; break;
                case "!": sentenceType = "goal"; break;
            }
        }
        if (!sentenceType.equals("fragment"))
            t.removeLast(); //remove the punctuation, it will be redundant

        List<Task> tt = new ArrayList();
        
        //1. add the logical structure of the sequence of terms
        if (inputProduct) {
            Term p = 
                    /*Conjunction*/Product.make( t.toArray(new Term[t.size()] ));        
            Term q = Instance.make( p, Term.get(sentenceType) );
            tt.add( 
                    memory.newTask(q, '.', 1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY)
            );
        }
        
        //2. add the 'heard' sequence of just the terms
        if (inputConjSeq) {
            LinkedList<Term> cont = new LinkedList();
            for (Span cp : s) {
                cont.add(lexToTerm(cp.content));
                //separate each by a duration interval
                cont.add(Interval.interval(memory.getDuration(), memory));
            }
            cont.removeLast(); //remove trailnig interval term
            Term con = Conjunction.make(cont.toArray(new Term[cont.size()]), TemporalRules.ORDER_FORWARD);

            tt.add( 
                    memory.newTask(con, '.', 1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY)
            );
        }
        
        return tt;
        
    }

    
    
    public Term spanToTerm(Span c) {
        if (c.pattern.equals("word")) {
            //TODO support >1 and probabalistic POS
            String pos = POS.get(c.content.toLowerCase());
            if (pos!=null) {
                return Instance.make( lexToTerm(c.content), tagToTerm(pos) );
            }
        }
            
        return Instance.make( lexToTerm(c.content), tagToTerm(c.pattern) );
    }
    
    public Term lexToTerm(String c) {
        return Term.get(c);
    }
    public Term tagToTerm(String c) {
        return Term.get(c.toLowerCase());
    }
    
    
    /** returns a list of all tasks that it was able to parse for the input */
    public List<AbstractTask> parse(String s, Narsese narsese, boolean modifyVocabulary) throws InvalidInputException {

        
        List<AbstractTask> results = new ArrayList();

        List<Span> tokens = Twokenize.tokenizeRawTweetText(s);
        
        List<List<Span>> sentences = new ArrayList();
        
        List<Span> currentSentence = new LinkedList();
        for (Span p : tokens) {
            
            currentSentence.add(p);
            
            if (p.pattern.equals("punct")) {
                switch (p.content) {
                    case ".":
                    case "?":
                    case "!":
                        if (currentSentence.size() > 0) {
                            sentences.add(currentSentence);
                            currentSentence = new LinkedList();
                            break;
                        }
                }
            }
        }
                
        if (!currentSentence.isEmpty())
            sentences.add(currentSentence);
        
        for (List<Span> x : sentences) {
            results.addAll( parseSentence(x, narsese, modifyVocabulary) );
        }
                
        if (results.size() > 0) {
            if (!languageBooted) {
                
                
                results.add(0, narsese.parseNarsese(new StringBuilder(
                        "<{word,pronoun,qpronoun,prepos,conjunc} --] symbol>.")));
                results.add(0, narsese.parseNarsese(new StringBuilder(
                       "$0.90;0.90$ <(*,<$a-->[$d]>,<is-->[verb]>,<$b-->[$d]>) =/> <$a <-> $b>>.")));
                
                languageBooted = true;
            }
                
        }
        
        return results;
    }
}
