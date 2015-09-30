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

import com.google.common.collect.Lists;
import nars.Memory;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Property;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.TemporalRules;
import nars.narsese.InvalidInputException;
import nars.narsese.NarseseParser;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.util.io.Twokenize;
import nars.util.io.Twokenize.Span;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Twitter English - english with additional tags for twitter-like content 
 */
public class Twenglish {

    //public final ArrayList<String> vocabulary = new ArrayList<>();
    
    /** substitutions */
    public final Map<String,String> sub = new HashMap();
    private Memory memory;

    
    boolean languageBooted = true; //set to false to initialize on first twenglish input
    boolean inputProduct = false;
    boolean inputConjSeq = true;
    
    
    public static final Map<String,String> POS = new HashMap(){{
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
        sub.put("go to", "goto");
        //etc..
    }

    public Twenglish(Memory memory) {
        this.memory = memory;
    }

    protected Collection<Task> parseSentence(List<Span> s, NarseseParser narsese, boolean modifyVocabulary) {
        return spansToSentenceTerms(s);
    }
    
    public Collection<Task> spansToSentenceTerms(Collection<Span> s) {
        
        LinkedList<Term> t = new LinkedList();
        Span last = null;
        for (Span c : s) {
            t.add( spanToTerm(c) );
            last = c;
        }
        if (t.isEmpty()) return Collections.EMPTY_LIST;
        
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
                    /*Conjunction*/Product.make(t.toArray(new Term[t.size()]));
            Compound q = Sentence.termOrNull( Instance.make( p, Atom.the(sentenceType,true)) );
            if (q != null) {
                throw new RuntimeException("API Upgrade not finished here:");
                /*tt.add(
                        memory.newTask(q, '.', 1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY)
                );*/
            }
        }
        
        //2. add the 'heard' sequence of just the terms
        if (inputConjSeq) {
            LinkedList<Term> cont = s.stream().map(cp -> lexToTerm(cp.content)).collect(Collectors.toCollection(LinkedList::new));
            //separate each by a duration interval
//cont.add(Interval.interval(memory.duration(), memory));
            cont.removeLast(); //remove trailnig interval term

            Compound con = Sentence.termOrNull(Conjunction.make(cont.toArray(new Term[cont.size()]), TemporalRules.ORDER_FORWARD));
            if (con!=null) {
                throw new RuntimeException("API Upgrade not finished here:");
                /*tt.add(
                        memory.newTask(con, '.', 1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY)
                );*/
            }
        }
        
        return tt;
        
    }



    public static Term spanToTerm(Span c) {
        return spanToTerm(c, false);
    }

    public static Term spanToTerm(Span c, boolean includeWordPOS) {
        if (c.pattern.equals("word")) {
            //TODO support >1 and probabalistic POS
            if (!includeWordPOS) {
                return lexToTerm(c.content);
            }
            else {
                String pos = POS.get(c.content.toLowerCase());
                if (pos != null) {
                    return Property.make(lexToTerm(c.content), tagToTerm(pos));
                }
            }
        }
            
        return Property.make( lexToTerm(c.content), tagToTerm(c.pattern) );
    }
    
    public static Term lexToTerm(String c) {
        return Atom.the(c, true);
    }
    public static Term tagToTerm(String c) {
        c = c.toLowerCase();
        if (c.equals("word")) return Atom.quote(" "); //space surrounded by quotes
        return Atom.the(c, true);
    }
    
    
    /** returns a list of all tasks that it was able to parse for the input */
    public List<Task> parse(Memory m, String s, NarseseParser narsese, boolean modifyVocabulary) throws InvalidInputException {

        
        List<Task> results = new ArrayList();

        List<Span> tokens = Twokenize.twokenize(s);
        
        List<List<Span>> sentences = new ArrayList();
        
        List<Span> currentSentence = new LinkedList();
        for (Span p : tokens) {
            
            currentSentence.add(p);
            
            if (p.pattern.equals("punct")) {
                switch (p.content) {
                    case ".":
                    case "?":
                    case "!":
                        if (!currentSentence.isEmpty()) {
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
                
        if (!results.isEmpty()) {
            if (!languageBooted) {
                
                
                results.add(0, narsese.task(new StringBuilder(
                        "<{word,pronoun,qpronoun,prepos,conjunc} --] symbol>.").toString(), m));
                results.add(0, narsese.task(new StringBuilder(
                        "$0.90;0.90$ <(*,<$a-->[$d]>,<is-->[verb]>,<$b-->[$d]>) =/> <$a <-> $b>>.").toString(), m));
                
                languageBooted = true;
            }
                
        }
        
        return results;
    }

    public static List<Term> tokenize(String msg) {
        List<Twokenize.Span> sp = Twokenize.tokenize(msg);

        List<Term> ll = Lists.transform(sp, x -> Twenglish.spanToTerm(x));
        return ll;
    }
}
