/*
 * The MIT License
 *
 * Copyright 2019 OpenNARS.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.plugin.mental;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opennars.entity.Task;
import org.opennars.io.Narsese;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.TaskAdd;
import org.opennars.language.CompoundTerm;
import static org.opennars.language.CompoundTerm.addComponentsRecursively;
import org.opennars.language.Interval;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.plugin.Plugin;

/**
 *
 * @author tc
 */
public class ConceptNet implements Plugin {
    
    Task lastAdded = null;
    int richness = 20;
    
    public ConceptNet(int richness, int uniquenessBufferSize) {
        this.richness = richness;
        AlreadyQueried = new String[uniquenessBufferSize];
    }
    
    EventObserver obs = new EventObserver()
    {
        @Override
        public void event(Class ev, Object[] args) {
            if(ev == TaskAdd.class) {
                Task t = (Task) args[0];
                String reason = (String) args[1];
                if(t.isInput() && reason.equals("Perceived")) {
                    Set<Term> M = addComponentsRecursively(t.sentence.term, null);
                    for(Term term : M) {
                        if(!(term instanceof CompoundTerm) && !(term instanceof Interval) && !(term instanceof CompoundTerm)) {
                            try {
                                List<String> qu = queryMeaningOnce(term.toString(), richness);
                                if(qu.size() > 0) {
                                    System.out.println("queried for " + term);
                                }
                                for(String s : qu) {
                                    Task parseTask = new Narsese(n).parseTask(s + ".");
                                    n.memory.addNewTask(parseTask, "ConceptNet");
                                }
                            } catch (Exception ex) {
                                System.out.println("ERROR in ConceptNet plugin in EventObserver, unable to parse task");

                            }
                            
                        }
                    }
                }
            }
        }
    };
    
    static Nar n = null;
    
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        ConceptNet.n = n;
        n.memory.event.set(obs, enabled, Events.TaskAdd.class);
        return true;
    }
    
    public enum Side {
        EXTENSION,
        INTENSION
    }

    public static class RawStatement {

        String subject;
        String predicate;
        String relation;

        public RawStatement(String subject, String relation, String predicate) {
            this.subject = subject;
            this.relation = relation;
            this.predicate = predicate;
        }

        @Override
        public String toString() {
            return subject + " " + relation + " " + predicate;
        }

        public String ToNarsese() {
            if (this.relation.equals("IsA")) {
                return "<" + this.subject + " --> " + this.predicate + ">";
            }
            if (this.relation.equals("InstanceOf")) {
                return "<{" + this.subject + "} --> " + this.predicate + ">";
            }
            if (this.relation.equals("HasProperty")) {
                return "<" + this.subject + " --> [" + this.predicate + "]>";
            }
            if (this.relation.equals("DistinctFrom")) {
                return "(--,<" + this.subject + " <-> " + this.predicate + ">)";
            }
            if (this.relation.equals("SimilarTo")) {
                return "<" + this.subject + " <-> " + this.predicate + ">";
            }
            return "<(*," + this.subject + "," + this.predicate + ") --> " + this.relation + ">";
        }
    }

    public static RawStatement unwrap(String rel) {
        String unw = rel.split("\\[")[1].split("\\]")[0].replace("/c/en/", "").replace("/n/", "").replace("/r/", "").replace("/", "");
        String[] parts = unw.split(",");
        return new RawStatement(parts[1], parts[0], parts[2]);
    }

    public static List<RawStatement> queryConceptNet(int amount, Side extension, String term, String relation) {
        try {
            // url containing the word to be indexed
            //String url= "http://api.conceptnet.io/query?node=/c/en/human&rel=/r/IsA";
            //String url= "http://api.conceptnet.io/query?node=/c/en/human&rel=/r/IsA";
            String ext = extension == Side.EXTENSION ? "end" : "start";

            String url = "http://api.conceptnet.io/query?" + ext + "=/c/en/" + term + "&rel=/r/" + relation + "&limit=" + amount;
            // open HttpURLConnection
            HttpURLConnection hp = (HttpURLConnection) new URL(url)
                    .openConnection();
            // set to request method to get
            // not required since default
            hp.setRequestMethod("GET");
            // get the inputstream in the json format
            hp.setRequestProperty("Accept", "application/json");
            // get inputstream from httpurlconnection
            InputStream is = hp.getInputStream();
            // get text from inputstream using IOUtils
            String jsonText = IOUtils.toString(is, Charset.forName("UTF-8"));
            // get json object from the json String
            JSONObject json = new JSONObject(jsonText);
            // get the edges array from the JSONObject which contains all
            // content
            JSONArray edges = json.getJSONArray("edges");
            // goes through the edges array
            List<RawStatement> ret = new ArrayList<>();
            for (int x = 0; x < edges.length(); x++) {
                // convert the first object of the json array into a jsonobject
                // once it is a jsonobject you can use getString or getJSONArray
                // to continue in getting info
                JSONObject obj = edges.getJSONObject(x);
                //List<String> hasToContain = new ArrayList<String>() {"HasA", "HasProperty"  };
                //if(obj.get("@id"))
                ret.add(unwrap(edges.getJSONObject(x).get("@id").toString()));
            }
            is.close();
            return ret;
        } catch (IOException | JSONException e) {
            System.out.println(e);
        }
        return null;
    }

    public static List<String> queryMeaning(String term, int richness) {
        List<String> ret = new ArrayList<>();
        String[] conceptNetRelations = new String[]{"IsA", "PartOf", "HasA", "Causes", "HasProperty", "DistinctFrom", "SimilarTo", "MadeOf", "InstanceOf"};
        for (String rel : conceptNetRelations) {
            for (Side side : new Side[]{Side.INTENSION, Side.EXTENSION}) {
                int maxAmount = richness;
                for (RawStatement s : queryConceptNet(maxAmount, side, term, rel)) {
                    ret.add(s.ToNarsese());
                }
            }
        }
        return ret;
    }

    static String[] AlreadyQueried = new String[1000]; //save last 1000 queries
    static int QueryIndex = 0;

    public static List<String> queryMeaningOnce(String term, int richness) {
        for (String AlreadyQueried1 : AlreadyQueried) {
            if (term.equals(AlreadyQueried1)) {
                return new ArrayList<>();
            }
        }
        AlreadyQueried[QueryIndex] = term;
        QueryIndex++;
        if (QueryIndex >= AlreadyQueried.length) {
            QueryIndex = 0;
        }
        return queryMeaning(term, richness);
    }
}
