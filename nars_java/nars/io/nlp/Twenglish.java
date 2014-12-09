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
import java.util.HashMap;
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
import nars.language.Product;
import nars.language.Term;

/**
 * Twitter English - english with additional tags for twitter-like content 
 */
public class Twenglish {

    public final ArrayList<String> vocabulary = new ArrayList<>();
    
    /** substitutions */
    public final Map<String,String> sub = new HashMap();
    private Memory memory;

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
        
        l.add( spansToProduct(s) );
        
        return l;
    }    
    
    public Task spansToProduct(Collection<Span> s) {
        
        List<Term> t = new ArrayList();
        for (Span c : s) {
            t.add( spanToTerm(c) );
        }
        
        Term p = Conjunction.make( t.toArray(new Term[t.size()] ), TemporalRules.ORDER_FORWARD );
        return memory.newTask(p, '.', 1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY);
        
    }

    
    
    public Term spanToTerm(Span c) {
        return Product.make( lexToTerm(c.content), tagToTerm(c.pattern) );
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
        sentences.add(tokens);
        
        for (List<Span> x : sentences) {
            results.addAll( parseSentence(x, narsese, modifyVocabulary) );
        }
                
        
        return results;
    }
}
