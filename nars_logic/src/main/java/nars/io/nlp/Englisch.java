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

import nars.narsese.InvalidInputException;
import nars.narsese.NarseseParser;
import nars.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nars.io.Texts.levenshteinDistance;

/**
 * "Englisch" (nearly-English) Input Perception To keep NLP overhead as small as
 * possible we will here use a heuristic which directly translates to Narsese
 * whenever it can, and use general sentence form
 *
 * @author tc
 */
public class Englisch {

    public final ArrayList<String> vocabulary = new ArrayList<>();
    
    /** substitutions */
    public final Map<String,String> sub = new HashMap();

    public Englisch() {
        //TODO use word tokenization so that word substitutions dont get applied across words.
        sub.put("go to", "goto");
        //etc..
    }

    
    protected void parseSentence(String s, NarseseParser narsese, List<String> statements, boolean modifyVocabulary) {
        s = s.trim();
        
        String punct = ".";

        if (s.endsWith("?")) {
            punct = "?";
        } else if (s.endsWith("!")) {
            punct = "!";
        }

        s = s.replace("go to", "go-to").replace("should be", "is");

        s = s.replace("?", "").replace("!", "").replace(".", "").replace(",", "");
        s = s.replace("who ", "?1 ").replace("what ", "?1 ").replace("when ", "?1 ").replace("where ", "?1 ");
        s = s.replace("somewhere", "#1").replace("something", "#1");

        String[] words = s.split(" ");
        ArrayList<String> realwords = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            if (!(words[i].equals("a") || words[i].equals("an") || words[i].equals("the"))) {
                realwords.add(words[i]);
                if (modifyVocabulary) {
                    if (!vocabulary.contains(words[i])) {
                        vocabulary.add(words[i]);
                    }
                }
            }
        }
        words = realwords.toArray(new String[realwords.size()]);

        for (String word : words) { //now add the similar words
            for (String mword : vocabulary) {
                if (word.equals(mword)) {
                    continue;
                }
                int difference = levenshteinDistance(word.replace("ing", ""), mword.replace("ing", ""));
                //get longer word:
                int longerword = Math.max(word.length(), mword.length());
                double perc = ((double) difference) / ((double) longerword);
                if (perc < 0.3) {
                    statements.add("<" + word + " <-> " + mword + ">.");
                }
            }
        }

        String sentence = "";

        if (words.length == 2) {
            sentence = "(^" + words[0] + "," + words[1] + ")" + punct + " :|:";
        } else if (words.length == 3 || (words.length > 3 && (words[3].equals("at") || words[3].equals("on") || words[3].equals("in")))) {
            if ("is".equals(words[1])) {
                sentence = "<" + words[0] + " --> " + words[2] + ">" + punct + " :|:";
            } else {
                sentence = "<(*," + words[0] + "," + words[2] + ") --> " + words[1] + ">" + punct + " :|:";
            }
        } else {
            boolean contains_is = false;
            for (int i = 0; i < words.length; i++) {
                if (words[i].equals("is")) {
                    contains_is = true;
                    String left = "<(*";
                    String right = "(*";
                    int leftcnt = 0;
                    int rightcnt = 0;
                    for (int j = 0; j < i; j++) {
                        if (words[j].equals("at") || words[j].equals("on") || words[j].equals("in")) {
                            break;
                        }
                        leftcnt++;
                        left += "," + words[j];
                    }
                    for (int j = i + 1; j < words.length; j++) {
                        if (words[j].equals("at") || words[j].equals("on") || words[j].equals("in")) {
                            break;
                        }
                        rightcnt++;
                        right += "," + words[j];
                    }
                    left += ")";
                    right += ")>";
                    if (rightcnt == 1) {
                        right = right.replace("(*,", "");
                        right = right.replace(")", "");
                    }
                    if (leftcnt == 1) {
                        left = left.replace("(*,", "");
                        left = left.replace(")", "");
                    }
                    sentence = left + " --> " + right + punct + " :|:";
                }
            }

            if (!contains_is) {
                sentence = "<(*";
                for (int i = 0; i < words.length; i++) {
                    sentence += "," + words[i];
                }
                sentence += ") --> sentence>" + punct + " :|:";
            }
        }
        statements.add(sentence);
        
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals("at") || words[i].equals("on") || words[i].equals("in")) {
                statements.add("<" + words[i + 1] + " --> place>.");
            }
        }
        //int wu=levenshteinDistance("","");        
        
    }
    
    /** returns a list of all tasks that it was able to parse for the input */
    public List<Task> parse(String s, NarseseParser narsese, boolean modifyVocabulary) throws InvalidInputException {

        List<String> statements = new ArrayList();


        String[] sentences = s.split("(?<=[.?!])\\s+(?=[a-zA-Z])");
        for (String x : sentences)
            parseSentence(x, narsese, statements, modifyVocabulary);
        
        
        List<Task> results = new ArrayList();
        for (String i : statements) {
            Task t = narsese.task(new StringBuilder(i).toString());
            if (t!=null)
                results.add(t);
        }
        //System.out.println("english: " + statements + "\n  " + results);
        return results;
    }
}
