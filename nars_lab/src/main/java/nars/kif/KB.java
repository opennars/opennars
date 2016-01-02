package nars.kif;

/**
 * This code is copyright Articulate Software (c) 2003. Some portions copyright
 * Teknowledge (c) 2003 and reused under the terms of the GNU license. This
 * software is released under the GNU Public License
 * <http://www.gnu.org/copyleft/gpl.html>. Users of this code also consent, by
 * use of this code, to credit Articulate Software and Teknowledge in any
 * writings, briefings, publications, presentations, or other representations of
 * any software which incorporates, builds on, or uses this code. Please cite
 * the following article in any publication with references:
 *
 * Pease, A., (2003). The Sigma Ontology Development Environment, in Working
 * Notes of the IJCAI-2003 Workshop on Ontology and Distributed Systems, August
 * 9, Acapulco, Mexico.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * *****************************************************************
 * Contains methods for reading, writing knowledge bases and their
 * configurations. Also contains the logic engine process for the knowledge
 * base.
 */
public class KB {

    private static final boolean DEBUG = false;

    /**
     * The name of the knowledge base.
     */
    public String name;

    /**
     * An ArrayList of Strings which are the full path file names of the files
     * which comprise the KB.
     */
    public ArrayList constituents = new ArrayList();

    /**
     * The natural language in which axiom paraphrases should be presented.
     */
    public String language = "en";

    /**
     * The location of preprocessed KIF files, suitable for loading into
     * Vampire.
     */
    public String kbDir = null;

    /**
     * A HashMap of HashSets, which contain all the parent classes of a given
     * class.
     */
    public HashMap parents = new HashMap();

    /**
     * A HashMap of HashSets, which contain all the child classes of a given
     * class.
     */
    public HashMap children = new HashMap();

    /**
     * A HashMap of HashSets, which contain all the disjoint classes of a given
     * class.
     */
    public HashMap disjoint = new HashMap();

    /**
     * A threshold limiting the number of non-ground values that will be added
     * to a single relation cache.
     */
    private static final int MAX_CACHE_SIZE = 1000000;

    /**
     * A List of the names of cached transitive relations.
     */
    public List cachedTransitiveRelationNames = Arrays.asList("subclass",
            "subrelation",
            "subAttribute",
            "subOrganization",
            "subCollection",
            "subProcess",
            "geographicSubregion",
            "geopoliticalSubdivision");

    /**
     * A List of the names of cached reflexive relations.
     */
    public List cachedReflexiveRelationNames = Arrays.asList("subclass",
            "subrelation",
            "subAttribute",
            "subOrganization",
            "subCollection",
            "subProcess");

    /**
     * A List of the names of cached relations.
     */
    public List cachedRelationNames = Arrays.asList("instance", "disjoint");

    /**
     * An ArrayList of RelationCache objects.
     */
    public ArrayList relationCaches = new ArrayList();

    /**
     * The instance of the CELT process.
     */
    //public CELT celt = null;
    /**
     * A Set of Strings, which are all the terms in the KB.
     */
    public TreeSet terms = new TreeSet();

    /**
     * The String constant that is the suffix for files of user assertions.
     */
    public static final String _userAssertionsString = "_UserAssertions.kif";

    /**
     * The String constant that is the suffix for files of cached assertions.
     */
    public static final String _cacheFileSuffix = "_Cache.kif";

    /**
     * A Map of all the Formula objects in the KB. Each key is a String
     * representation of a Formula. Each value is the Formula object
     * corresponding to the key.
     */
    public HashMap formulaMap = new HashMap();

    /**
     * A HashMap of ArrayLists of Formulas, containing all the formulas in the
     * KB. Keys are both the formula itself, and term indexes created in
     * KIF.createKey().
     */
    private final HashMap formulas = new HashMap();

    /**
     * The natural language formatting strings for relations in the KB.
     */
    private HashMap formatMap = null;

    /**
     * The natural language strings for terms in the KB.
     */
    private HashMap termFormatMap = null;

    public KIFInference inferenceEngine; //was vampire

    /**
     * *************************************************************
     * Constructor which takes the name of the KB and the location where KBs
     * preprocessed for Vampire should be placed.
     */
    public KB(String n, String dir) {

        name = n;
        kbDir = dir;
        initRelationCaches();
        KBmanager mgr = KBmanager.getMgr();
        if (mgr != null) {
            String loadCelt = mgr.getPref("loadCELT");
            if ((loadCelt != null) && "yes".equalsIgnoreCase(loadCelt)) {
                //celt = new CELT();
            }
        }
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * Get an ArrayList of Strings containing the language identifiers
//     * of available natural language formatting templates.
//     *
//     * @return an ArrayList of Strings containing the language identifiers
//     */
//    public ArrayList availableLanguages() {
//
//        ArrayList al = new ArrayList();
//        ArrayList col = ask("arg", 0, "format");
//        if (col != null) {
//            for (int i = 0; i < col.size(); i++) {
//                String lang = ((Formula) col.get(i)).theFormula;
//                int langStart = lang.indexOf(' ');
//                int langEnd = lang.indexOf(' ',langStart+1);
//                lang = lang.substring(langStart+1, langEnd);
//                if (!al.contains(lang.intern()))
//                    al.add(lang.intern());
//            }
//        }
//        return al;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     * Returns a list of the names of cached relations.
     *
     * @return An ArrayList of relation names (Strings).
     */
    private ArrayList getCachedRelationNames() {
        ArrayList relationNames = new ArrayList(cachedTransitiveRelationNames);
        relationNames.addAll(cachedRelationNames);
        return relationNames;
    }

    /**
     * *************************************************************
     * Returns an ArrayList of RelationCache objects.
     *
     * @return An ArrayList of RelationCache objects.
     */
    protected ArrayList getRelationCaches() {
        return relationCaches;
    }

    /**
     * *************************************************************
     * Initializes all RelationCaches. Creates the RelationCache objects if they
     * do not yet exist, and clears all existing RelationCache objects.
     *
     * @return void
     */
    protected void initRelationCaches() {
        // System.out.println("INFO in initRelationCaches()");
        Iterator it;
        if (relationCaches.isEmpty()) {
            it = getCachedRelationNames().iterator();
            String relname;
            while (it.hasNext()) {
                relname = (String) it.next();
                relationCaches.add(new RelationCache(relname, 1, 2));

                // Since disjoint is symmetric, we put all
                // disjointness entries in one table.  All transitive
                // binary relations are cached in two RelationCaches,
                // one that looks "upward" from the keys, and another
                // that looks "downward" from the keys.
                if (!"disjoint".equals(relname)) {
                    relationCaches.add(new RelationCache(relname, 2, 1));
                }
            }
        } else {
            RelationCache cache;
            it = relationCaches.iterator();
            while (it.hasNext()) {
                cache = (RelationCache) it.next();
                cache.clear();
            }
        }

        // We still set these legacy variables.  Eventually, they
        // should be removed.
        parents = getRelationCache("subclass", 1, 2);
        children = getRelationCache("subclass", 2, 1);
        disjoint = getRelationCache("disjoint", 1, 2);

    }

    /**
     * *************************************************************
     * Returns the RelationCache object identified by the input arguments:
     * relation name, key argument position, and value argument position.
     *
     * @param relName The name of the cached relation.
     *
     * @param keyArg An int value that indicates the argument position of the
     * cache keys.
     *
     * @param valueArg An int value that indicates the argument position of the
     * cache values.
     *
     * @return a RelationCache object, or null if there is no cache
     * corresponding to the input arguments.
     */
    private RelationCache getRelationCache(String relName, int keyArg, int valueArg) {
        if (Formula.isNonEmptyString(relName)) {
            Iterator it = getRelationCaches().iterator();
            RelationCache cache;
            while (it.hasNext()) {
                cache = (RelationCache) it.next();
                if (cache.getRelationName().equals(relName)
                        && (cache.getKeyArgument() == keyArg)
                        && (cache.getValueArgument() == valueArg)) {
                    return cache;
                }
            }
        }
        return null;
    }

    /**
     * *************************************************************
     * Writes the cache .kif file, and then calls addConstituent() so that the
     * file can be processed and loaded by the logic engine.
     *
     * @return a String indicating any errors, or the empty string if there were
     * no errors.
     */
    public String cache() {

//        System.out.println("ENTER KB.cache()");
//        String result = "";
//        FileWriter fr = null;
//        try {
//            boolean closureComputed = false;
//            List caches = getRelationCaches();
//            Iterator it = null;
//            Iterator it2 = null;
//            Iterator it3 = null;
//            String relation = null;
//            String arg1 = null;
//            String arg2 = null;
//            Set valSet = null;
//            String tuple = null;
//            RelationCache rc = null;
//            if (caches != null) {
//                it = caches.iterator();
//                while (it.hasNext()) {
//                    rc = (RelationCache) it.next();
//                    if (rc.getIsClosureComputed()) {
//                        closureComputed = true;
//                        break;
//                    }
//                }
//
//                // Don't bother writing the cache file if we have not
//                // at least partially computed the closure of the
//                // various cached relations.
//                if (closureComputed) {
//                    File dir = new File(kbDir);
//                    File f = new File(dir, (this.name + _cacheFileSuffix));
//                    System.out.println("INFO in KB.cache()");
//                    System.out.println("  User cache file == " + f.getCanonicalPath());
//                    if (f.exists()) {
//                        System.out.println("  Deleting " + f.getCanonicalPath());
//                        f.delete();
//                        if (f.exists()) {
//                            System.out.println("  Could not delete " + f.getCanonicalPath());
//                        }
//                    }
//                    String filename = f.getCanonicalPath();
//                    fr = new FileWriter(f, true);
//                    System.out.println("  Appending statements to " + f.getCanonicalPath());
//                    it = caches.iterator();
//                    while (it.hasNext()) {
//                        rc = (RelationCache) it.next();
//                        if (rc.getKeyArgument() == 1) {
//                            relation = rc.getRelationName();
//
//                            // Unfortunately, there are just too many
//                            // disjoint classes to consider writing
//                            // them to a file, or to consider having
//                            // Vampire try to load the assertions.
//                            if (! relation.equals("disjoint")) {
//                                it2 = rc.keySet().iterator();
//                                while (it2.hasNext()) {
//                                    arg1 = (String) it2.next();
//                                    valSet = (Set) rc.get(arg1);
//                                    it3 = valSet.iterator();
//                                    while (it3.hasNext()) {
//                                        arg2 = (String) it3.next();
//                                        tuple = ("(" + relation + " " + arg1 + " " + arg2 + ")");
//                                        if (! formulaMap.containsKey(tuple.intern())) {
//                                            fr.write(tuple);
//                                            fr.write("\n");
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if (fr != null) {
//                        fr.close();
//                        fr = null;
//                    }
//                    constituents.remove(filename);
//                    System.out.println("INFO in KB.cache()");
//                    System.out.println("  Adding " + filename);
//                    result = addConstituent(filename, false, false);
//                    KBmanager.getMgr().writeConfiguration();
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        finally {
//            if (fr != null) {
//                try {
//                    fr.close();
//                }
//                catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//            System.out.println("EXIT KB.cache()");
//            return result;
//        }
        return "";
    }

    /**
     * *************************************************************
     * Adds one value to the cache, indexed under keyTerm.
     *
     * @param cache The RelationCache object to be updated.
     *
     * @param keyTerm The String that is the key for this entry.
     *
     * @param valueTerm The String that is the value for this entry.
     *
     * @return The int value 1 if a new entry is added, else 0.
     */
    private int addRelationCacheEntry(RelationCache cache, String keyTerm, String valueTerm) {
        int count = 0;
        if ((cache != null) && (keyTerm != null) && (valueTerm != null)) {
            Set valueSet = (Set) cache.get(keyTerm);
            if (valueSet == null) {
                valueSet = new HashSet();
                cache.put(keyTerm, valueSet);
            }
            if (valueSet.add(valueTerm)) {
                count++;
            }
        }
        return count;
    }

    /**
     * *************************************************************
     * Returns the HashSet indexed by term in the RelationCache identified by
     * relation, keyArg, and valueArg.
     *
     * @param relation A String, the name of a relation.
     *
     * @param term A String (key) that indexes a HashSet.
     *
     * @param keyArg An int value that, with relation and valueArg, identifies a
     * RelationCache.
     *
     * @param valueArg An int value that, with relation and keyArg, identifies a
     * RelationCache.
     *
     * @return A HashSet, or null if no HashSet corresponds to term.
     */
    public HashSet getCachedRelationValues(String relation, String term, int keyArg, int valueArg) {
        RelationCache cache = getRelationCache(relation, keyArg, valueArg);
        if (cache != null) {
            return (HashSet) cache.get(term);
        }
        return null;
    }

    /**
     * *************************************************************
     * This method computes the transitive closure for the relation identified
     * by relationName. The results are stored in the RelationCache object for
     * the relation and "direction" (looking from the arg1 keys toward arg2
     * parents, or looking from the arg2 keys toward arg1 children).
     *
     * @param relationName The name of a relation.
     *
     * @return void
     */
    private void computeTransitiveCacheClosure(String relationName) {

        System.out.println("INFO in KB.computeTransitiveCacheClosure(" + relationName + ')');

        try {
            long t1 = System.currentTimeMillis();
            int count = 0;
            if (cachedTransitiveRelationNames.contains(relationName)) {
                RelationCache c1 = getRelationCache(relationName, 1, 2);
                RelationCache c2 = getRelationCache(relationName, 2, 1);
                RelationCache inst1 = null;
                RelationCache inst2 = null;
                boolean isSubrelationCache = "subrelation".equals(relationName);
                if (isSubrelationCache) {
                    inst1 = getRelationCache("instance", 1, 2);
                    inst2 = getRelationCache("instance", 2, 1);
                }
                Set c1Keys = c1.keySet();
                Iterator it1;
                Iterator it2;
                String keyTerm;
                String valTerm;
                Set valSet;
                Set valSet2;
                Object[] valArr;
                boolean changed = true;
                while (changed) {
                    changed = false;
                    it1 = c1Keys.iterator();
                    while (it1.hasNext()) {
                        keyTerm = (String) it1.next();
                        if ((keyTerm == null) || keyTerm.isEmpty()) {
                            System.out.println("Error in KB.computeTransitiveCacheClosure(" + relationName + ')');
                            System.out.println("  keyTerm == " + ((keyTerm == null) ? null : '"' + keyTerm + '"'));
                        } else {
                            valSet = (Set) c1.get(keyTerm);
                            valArr = valSet.toArray();
                            for (Object aValArr : valArr) {
                                valTerm = (String) aValArr;

                                valSet2 = (Set) c1.get(valTerm);
                                if (valSet2 != null) {
                                    it2 = valSet2.iterator();
                                    while (it2.hasNext() && (count < MAX_CACHE_SIZE)) {
                                        if (valSet.add(it2.next())) {
                                            changed = true;
                                            count++;
                                        }
                                    }
                                }

                                if (count < MAX_CACHE_SIZE) {
                                    valSet2 = (Set) c2.get(valTerm);
                                    if (valSet2 == null) {
                                        valSet2 = new HashSet();
                                        c2.put(valTerm, valSet2);
                                    }
                                    if (valSet2.add(keyTerm)) {
                                        changed = true;
                                        count++;
                                    }
                                }
                            }
                            // Here we try to make sure that every Relation
                            // has at least some entry in the "instance"
                            // caches, since this information is sometimes
                            // considered redundant and so could be left out
                            // of .kif files.
                            valTerm = "Relation";
                            if (keyTerm.endsWith("Fn")) {
                                valTerm = "Function";
                            } else if (Character.isLowerCase(keyTerm.charAt(0)) && (keyTerm.indexOf('(') == -1)) {
                                valTerm = "Predicate";
                            }
                            addRelationCacheEntry(inst1, keyTerm, valTerm);
                            addRelationCacheEntry(inst2, valTerm, keyTerm);
                        }
                    }
                    if (changed) {
                        c1.setIsClosureComputed(true);
                        c2.setIsClosureComputed(true);
                    }
                }
            }
            System.out.println("  "
                    + count
                    + ' '
                    + relationName
                    + " entries computed in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");

            /*
             if (relationName.equals("subclass")) {
             printParents();
             printChildren();
             }
             */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************************
     * This method computes the closure for the cache of the instance relation,
     * in both directions.
     *
     * @return void
     */
    private void computeInstanceCacheClosure() {

        System.out.println("INFO in KB.computeInstanceCacheClosure()");

        try {
            long t1 = System.currentTimeMillis();
            RelationCache ic1 = getRelationCache("instance", 1, 2);
            RelationCache ic2 = getRelationCache("instance", 2, 1);
            RelationCache sc1 = getRelationCache("subclass", 1, 2);
            Set ic1KeySet = ic1.keySet();
            Iterator it1 = ic1KeySet.iterator();
            Iterator it2;
            String ic1KeyTerm;
            Set ic1ValSet;
            Object[] ic1ValArr;
            String ic1ValTerm;
            Set sc1ValSet;
            Set ic2ValSet;

            int count = 0;
            while (it1.hasNext()) {
                ic1KeyTerm = (String) it1.next();
                ic1ValSet = (Set) ic1.get(ic1KeyTerm);
                ic1ValArr = ic1ValSet.toArray();
                for (Object anIc1ValArr : ic1ValArr) {
                    ic1ValTerm = (String) anIc1ValArr;
                    if (ic1ValTerm != null) {
                        sc1ValSet = (Set) sc1.get(ic1ValTerm);
                        if (sc1ValSet != null) {
                            it2 = sc1ValSet.iterator();
                            while (it2.hasNext() && (count < MAX_CACHE_SIZE)) {
                                if (ic1ValSet.add(it2.next())) {
                                    count++;
                                }
                            }
                        }
                    }
                }
                if (count < MAX_CACHE_SIZE) {
                    it2 = ic1ValSet.iterator();
                    while (it2.hasNext()) {
                        ic1ValTerm = (String) it2.next();
                        ic2ValSet = (Set) ic2.get(ic1ValTerm);
                        if (ic2ValSet == null) {
                            ic2ValSet = new HashSet();
                            ic2.put(ic1ValTerm, ic2ValSet);
                        }
                        if (ic2ValSet.add(ic1KeyTerm)) {
                            count++;
                        }
                    }
                }
            }

            ic1.setIsClosureComputed(true);
            ic2.setIsClosureComputed(true);
            System.out.println("  "
                    + count
                    + " instance entries computed in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************************
     * This method computes the closure for the cache of the disjoint relation.
     *
     * @return void
     */
    private void computeDisjointCacheClosure() {

        System.out.println("INFO in KB.computeDisjointCacheClosure()");

        try {
            long t1 = System.currentTimeMillis();
            RelationCache dc1 = getRelationCache("disjoint", 1, 2);
            RelationCache sc2 = getRelationCache("subclass", 2, 1);
            Set dc1KeySet;
            Object[] dc1KeyArr;
            String dc1KeyTerm;
            Set dc1ValSet;
            Object[] dc1ValArr;
            String dc1ValTerm;
            Set sc2ValSet;
            Iterator it;
            String sc2ValTerm;
            Set dc1ValSet2;

            int count = -1;
            int passes = 0;
            boolean changed;

            // One pass is sufficient.
            // while (changed) {
            dc1KeySet = dc1.keySet();
            dc1KeyArr = dc1KeySet.toArray();
            changed = false;
            for (int i = 0; (i < dc1KeyArr.length) && (count < MAX_CACHE_SIZE); i++) {

                dc1KeyTerm = (String) dc1KeyArr[i];
                dc1ValSet = (Set) dc1.get(dc1KeyTerm);
                dc1ValArr = dc1ValSet.toArray();
                for (Object aDc1ValArr : dc1ValArr) {
                    dc1ValTerm = (String) aDc1ValArr;
                    sc2ValSet = (Set) sc2.get(dc1ValTerm);
                    if (sc2ValSet != null) {
                        if (dc1ValSet.addAll(sc2ValSet)) {
                            changed = true;
                        }
                    }
                }

                sc2ValSet = (Set) sc2.get(dc1KeyTerm);
                if (sc2ValSet != null) {
                    it = sc2ValSet.iterator();
                    while (it.hasNext()) {
                        sc2ValTerm = (String) it.next();
                        dc1ValSet2 = (Set) dc1.get(sc2ValTerm);
                        if (dc1ValSet2 == null) {
                            dc1ValSet2 = new HashSet();
                            dc1.put(sc2ValTerm, dc1ValSet2);
                        }
                        if (dc1ValSet2.addAll(dc1ValSet)) {
                            changed = true;
                        }
                    }
                }

                it = dc1.values().iterator();
                count = 0;
                while (it.hasNext()) {
                    dc1ValSet = (Set) it.next();
                    count += dc1ValSet.size();
                }
            }

            if (changed) {
                dc1.setIsClosureComputed(true);
            }

            // System.out.println("  " + count + " disjoint entries after pass " + ++passes);
            // }
            System.out.println("  "
                    + count
                    + " disjoint entries computed in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");

            // printDisjointness();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HashMap relnsWithRelnArgs = null;

    /**
     * *************************************************************
     * This method builds a cache of all Relations in the current KB for which
     * at least one argument must be filled by a relation name (or a variable
     * denoting a relation name). This method should be called only after the
     * subclass cache has been built.
     */
    private void cacheRelnsWithRelnArgs() {

        System.out.println("INFO in KB.cacheRelnsWithRelnArgs()");

        try {
            long t1 = System.currentTimeMillis();
            if (relnsWithRelnArgs == null) {
                relnsWithRelnArgs = new HashMap();
            }
            relnsWithRelnArgs.clear();
            Set relnClasses = getCachedRelationValues("subclass", "Relation", 2, 1);

            // System.out.println("  relnClasses == " + relnClasses);
            if (relnClasses != null) {
                ArrayList formulas;
                Iterator it = relnClasses.iterator();
                Iterator it2;
                String relnClass;
                Formula f;
                String reln;
                int argPos;
                int valence;
                boolean[] signature;
                while (it.hasNext()) {
                    relnClass = (String) it.next();
                    formulas = askWithRestriction(3, relnClass, 0, "domain");

                    // System.out.println("  formulas == " + formulas);
                    if (formulas != null) {
                        it2 = formulas.iterator();
                        while (it2.hasNext()) {
                            f = (Formula) it2.next();
                            reln = f.getArgument(1);
                            valence = getValence(reln);
                            if (valence < 1) {
                                valence = Formula.MAX_PREDICATE_ARITY;
                            }
                            signature = (boolean[]) relnsWithRelnArgs.get(reln);
                            if (signature == null) {
                                signature = new boolean[valence + 1];
                                for (int j = 0; j < signature.length; j++) {
                                    signature[j] = false;
                                }
                                relnsWithRelnArgs.put(reln, signature);
                            }
                            argPos = Integer.parseInt(f.getArgument(2));
                            try {
                                signature[argPos] = true;
                            } catch (Exception e1) {
                                System.out.println("Error in KB.cacheRelnsWithRelnArgs():");
                                System.out.println("  reln == " + reln
                                        + ", argPos == " + argPos
                                        + ", signature == " + Arrays.toString(signature));
                                throw e1;
                            }
                        }
                    }
                }
                // This is a kluge.  "format" (and "termFormat", which
                // is not directly relevant here) should be defined as
                // predicates (meta-predicates) in Merge.kif, or in
                // some language-independent paraphrase scaffolding
                // .kif file.
                signature = (boolean[]) relnsWithRelnArgs.get("format");
                if (signature == null) {
                    signature = new boolean[4];
                    // signature = { false, false, true, false };
                    for (int i = 0; i < signature.length; i++) {
                        signature[i] = (i == 2);
                    }
                    relnsWithRelnArgs.put("format", signature);
                }
            }
            System.out.println("  "
                    + relnsWithRelnArgs.size()
                    + " relation argument entries computed in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************************
     * Returns a boolean[] if the input relation has at least one argument that
     * must be filled by a relation name.
     *
     */
    protected boolean[] getRelnArgSignature(String relation) {
        if (relnsWithRelnArgs != null) {
            return (boolean[]) relnsWithRelnArgs.get(relation);
        }
        return null;
    }

    private final HashMap relationValences = new HashMap();

    private void cacheRelationValences() {
        System.out.println("INFO in KB.cacheRelationValences()");
        try {
            long t1 = System.currentTimeMillis();
            Set relations = getCachedRelationValues("instance", "Relation", 2, 1);
            if (relations != null) {
                RelationCache ic1 = getRelationCache("instance", 1, 2);
                RelationCache ic2 = getRelationCache("instance", 2, 1);
                String reln;
                String className;
                int valence;
                for (Object relation : relations) {
                    reln = (String) relation;

                    // Here we evaluate getValence() to build the
                    // relationValences cache, and use its return
                    // value to fill in any info that might be missing
                    // from the "instance" cache.
                    valence = getValence(reln);
                    className = null;
                    if (valence >= 0) {
                        switch (valence) {
                            case 0:
                                className = "VariableArityRelation";
                                break;
                            case 1:
                                if (reln.endsWith("Fn")) {
                                    className = "UnaryFunction";
                                }
                                break;
                            case 2:
                                className = "BinaryRelation";
                                break;
                            case 3:
                                className = "TernaryRelation";
                                break;
                            case 4:
                                className = "QuaternaryRelation";
                                break;
                            case 5:
                                className = "QuintaryRelation";
                                break;
                            default:
                                break;
                        }
                    }
                    if (className != null) {
                        addRelationCacheEntry(ic1, reln, className);
                        addRelationCacheEntry(ic2, className, reln);
                    }
                }
            }
            System.out.println("  "
                    + relationValences.size()
                    + " relation valence entries computed in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected String getArgType(String reln, int argPos) {
        String className = null;
        try {
            List formulas = askWithRestriction(1, reln, 0, "domain");
            if (!((formulas == null) || formulas.isEmpty())) {
                String argN;
                Formula f;
                for (Object formula : formulas) {
                    f = (Formula) formula;
                    argN = f.getArgument(2);
                    if (Integer.parseInt(argN) == argPos) {
                        className = f.getArgument(3);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return className;
    }

    public boolean isVariableArityRelation(String relnName) {
        boolean ans = false;
        try {
            ans = (getValence(relnName) == 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    protected ArrayList listRelnsWithRelnArgs() {
        if (relnsWithRelnArgs != null) {
            return new ArrayList(relnsWithRelnArgs.keySet());
        }
        return null;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    protected boolean containsRelnWithRelnArg(String input) {
//        try {
//            if (Formula.isNonEmptyString(input)) {
//                List relns = listRelnsWithRelnArgs();
//                if (relns != null) {
//                    int len = relns.size();
//                    String reln = null;
//                    for (int i = 0 ; i < len ; i++) {
//                        reln = (String) relns.get(i);
//                        if (input.indexOf(reln) >= 0) {
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return false;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * debugging utility
//     */
//    private void printParents() {
//
//        System.out.println("INFO in printParents():  Printing parents.");
//        System.out.println();
//        Iterator it = parents.keySet().iterator();
//        while (it.hasNext()) {
//            String parent = (String) it.next();
//            System.out.print(parent + " ");
//            System.out.println(parents.get(parent));
//        }
//        System.out.println();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * debugging utility
//     */
//    private void printChildren() {
//
//        System.out.println("INFO in printChildren():  Printing children.");
//        System.out.println();
//        Iterator it = children.keySet().iterator();
//        while (it.hasNext()) {
//            String child = (String) it.next();
//            System.out.print(child + " ");
//            System.out.println(children.get(child));
//        }
//        System.out.println();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * debugging utility
//     */
//    private void printDisjointness() {
//
//        System.out.println("INFO in printDisjointness():  Printing disjoint.");
//        System.out.println();
//        Iterator it = disjoint.keySet().iterator();
//        while (it.hasNext()) {
//            String term = (String) it.next();
//            System.out.print(term + " is disjoint with ");
//            System.out.println(disjoint.get(term));
//        }
//        System.out.println();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     * Determine whether a particular term is an immediate instance, which has a
     * statement of the form (instance term otherTerm). Note that this does not
     * count for terms such as Attribute(s) and Relation(s), which may be
     * defined as subAttribute(s) or subrelation(s) of another instance. If the
     * term is not an instance, return an empty ArrayList. Otherwise, return an
     * ArrayList of the Formula(s) in which the given term is defined as an
     * instance.
     *
     * @param term A String.
     *
     * @return An ArrayList.
     */
    public ArrayList instancesOf(String term) {

        //System.out.println("INFO in KB.instancesOf()");
        return askWithRestriction(1, term, 0, "instance");
    }

    /**
     * *************************************************************
     * Determine whether a particular class or instance "child" is a child of
     * the given "parent".
     *
     * @param child A String, the name of a term.
     *
     * @param parent A String, the name of a term.
     *
     * @return true if child and parent constitute an actual or implied relation
     * in the current KB, else false.
     */
    public boolean childOf(String child, String parent) {

        if (child.equals(parent)) {
            return true;
        }
        HashSet childs = (HashSet) children.get(parent);
        if (childs != null && childs.contains(child)) {
            return true;
        }
        ArrayList al = instancesOf(child);
        for (Object anAl : al) {
            Formula form = (Formula) anAl;
            Formula f = new Formula();
            f.read(form.theFormula);
            f.read(f.cdr());
            f.read(f.cdr());
            String superTerm = f.car();
            if (superTerm.equals(parent)) {
                return true;
            }
            if (childs != null && childs.contains(superTerm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * *************************************************************
     * Returns true if the subclass cache supports the conclusion that c1 is a
     * subclass of c2, else returns false.
     *
     * @param c1 A String, the name of a SetOrClass.
     *
     * @param c2 A String, the name of a SetOrClass.
     *
     * @return boolean
     */
    public boolean isSubclass(String c1, String c2) {
        boolean ans = false;
        try {
            if (Formula.isNonEmptyString(c1) && Formula.isNonEmptyString(c2)) {
                Set classNames = getCachedRelationValues("subclass", c1, 1, 2);
                ans = ((classNames != null) && classNames.contains(c2));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * *************************************************************
     * Builds all of the relation caches for the current KB.
     */
    public void buildRelationCaches() {

        try {
            long t1 = System.currentTimeMillis();
            initRelationCaches();
            cacheGroundAssertions();

            Iterator it = cachedTransitiveRelationNames.iterator();
            String relationName;
            while (it.hasNext()) {
                relationName = (String) it.next();
                computeTransitiveCacheClosure(relationName);
            }

            computeInstanceCacheClosure();
            computeDisjointCacheClosure();
            cacheRelnsWithRelnArgs();
            cacheRelationValences();

            System.out.println("Total elapsed time to build all relation caches: "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************************
     * Populates all caches with ground assertions, from which closures can be
     * computed.
     */
    private void cacheGroundAssertions() {

        System.out.println("INFO in KB.cacheGroundAssertions(): ");

        // System.out.println("formulas == " + formulas.toString());
        try {
            long t1 = System.currentTimeMillis();
            String relation;
            String arg1;
            String arg2;
            List forms;
            Formula formula;
            RelationCache c1;
            RelationCache c2;

            Iterator formsIt;
            Iterator it = getCachedRelationNames().iterator();
            int total = 0;
            int count;
            while (it.hasNext()) {

                count = 0;
                relation = (String) it.next();
                forms = ask("arg", 0, relation);
                // System.out.println(forms.size() + " " + relation + " assertions retrieved");

                if (forms != null) {

                    // System.out.print(relation);
                    c1 = getRelationCache(relation, 1, 2);
                    c2 = getRelationCache(relation, 2, 1);
                    formsIt = forms.iterator();
                    while (formsIt.hasNext()) {
                        formula = (Formula) formsIt.next();
                        if ((formula.theFormula.indexOf('(', 2) == -1)
                                && !(formula.sourceFile.endsWith(_cacheFileSuffix))) {

                            arg1 = formula.getArgument(1).intern();
                            arg2 = formula.getArgument(2).intern();
                            if (Formula.isNonEmptyString(arg1) && Formula.isNonEmptyString(arg2)) {
                                count += addRelationCacheEntry(c1, arg1, arg2);
                                count += addRelationCacheEntry(c2, arg2, arg1);

                                // Special cases.
                                if (cachedReflexiveRelationNames.contains(relation)) {
                                    count += addRelationCacheEntry(c1, arg1, arg1);
                                    count += addRelationCacheEntry(c1, arg2, arg2);
                                    count += addRelationCacheEntry(c2, arg1, arg1);
                                    count += addRelationCacheEntry(c2, arg2, arg2);
                                } else if ("disjoint".equals(relation)) {
                                    count += addRelationCacheEntry(c1, arg2, arg1);
                                }
                            }
                        }
                    }
                }

                // More ways of collecting implied disjointness
                // assertions.
                if ("disjoint".equals(relation)) {
                    List partitions = ask("arg", 0, "partition");
                    List decompositions = ask("arg", 0, "disjointDecomposition");
                    forms = new ArrayList();
                    if (partitions != null) {
                        forms.addAll(partitions);
                    }
                    if (decompositions != null) {
                        forms.addAll(decompositions);
                    }
                    c1 = getRelationCache(relation, 1, 2);
                    List arglist;
                    formsIt = forms.iterator();
                    while (formsIt.hasNext()) {
                        formula = (Formula) formsIt.next();
                        if ((formula.theFormula.indexOf('(', 2) == -1)
                                && !(formula.sourceFile.endsWith(_cacheFileSuffix))) {

                            arglist = formula.argumentsToArrayList(2);
                            for (int i = 0; i < arglist.size(); i++) {
                                for (int j = 0; j < arglist.size(); j++) {
                                    if (i != j) {
                                        arg1 = ((String) arglist.get(i)).intern();
                                        arg2 = ((String) arglist.get(j)).intern();
                                        if (Formula.isNonEmptyString(arg1) && Formula.isNonEmptyString(arg2)) {
                                            count += addRelationCacheEntry(c1, arg1, arg2);
                                            count += addRelationCacheEntry(c1, arg2, arg1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println("  " + count + " cache entries added for " + relation);
                total += count;
            }
            System.out.println("  "
                    + total
                    + " ground assertions cached in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * *************************************************************
     * Converts all Formula objects in the input List to ArrayList tuples.
     *
     * @param formulaList A list of Formulas.
     *
     * @return An ArrayList of formula tuples (ArrayLists), or an empty
     * ArrayList.
     */
    public static ArrayList formulasToArrayLists(List formulaList) {

        ArrayList ans = new ArrayList();
        try {
            if (formulaList instanceof List) {
                Iterator it = formulaList.iterator();
                Formula f;
                while (it.hasNext()) {
                    f = (Formula) it.next();
                    ans.add(f.literalToArrayList());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * *************************************************************
     * Converts all Strings in the input List to Formula objects.
     *
     * @param strings A list of Strings.
     *
     * @return An ArrayList of Formulas, or an empty ArrayList.
     */
    public static ArrayList stringsToFormulas(List strings) {

        ArrayList ans = new ArrayList();
        try {
            if (strings instanceof List) {
                for (Object string : strings) {
                    Formula f = new Formula();
                    f.read((String) string);
                    ans.add(f);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * *************************************************************
     * Converts a literal (List object) to a String.
     *
     * @param literal A List representing a SUO-KIF formula.
     *
     * @return A String representing a SUO-KIF formula.
     */
    public static String literalListToString(List literal) {
        StringBuilder b = new StringBuilder();
        try {
            if (literal instanceof List) {
                b.append('(');
                for (int i = 0; i < literal.size(); i++) {
                    if (i > 0) {
                        b.append(' ');
                    }
                    b.append((String) literal.get(i));
                }
                b.append(')');
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b.toString();
    }

    /**
     * *************************************************************
     * Converts a literal (List object) to a Formula.
     *
     * @param literal A List representing a SUO-KIF formula.
     *
     * @return A SUO-KIF Formula object, or null if no Formula can be created.
     */
    public static Formula literalListToFormula(List lit) {
        Formula f = null;
        try {
            String theFormula = literalListToString(lit);
            if (Formula.isNonEmptyString(theFormula)) {
                f = new Formula();
                f.read(theFormula);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return f;
    }

    /**
     * *************************************************************
     * Returns an ArrayList of Formulas in which the two terms provided appear
     * in the indicated argument positions. If there are no Formula(s) matching
     * the given terms and respective argument positions, return an empty
     * ArrayList.
     *
     * @return ArrayList
     */
    public ArrayList askWithRestriction(int argnum1, String term1, int argnum2, String term2) {

        ArrayList partial = ask("arg", argnum1, term1);
        ArrayList result = new ArrayList();
        if (partial != null) {
            for (Object aPartial : partial) {
                Formula f = (Formula) aPartial;
                if (f.getArgument(argnum2).equals(term2)) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    /**
     * *************************************************************
     * Returns an ArrayList containing the Formulas that match the request.
     *
     * @param kind - May be one of "ant", "cons", "stmt", or "arg"
     * @see KIF.createKey()
     * @param term - The term that appears in the statements being requested.
     * @param argnum - The argument position of the term being asked for. The
     * first argument after the predicate is "1". This parameter is ignored if
     * the kind is "ant", "cons" or "stmt".
     * @return an ArrayList of Formula(s), or null if no match found.
     */
    public ArrayList ask(String kind, int argnum, String term) {

        return kind.compareTo("arg") == 0 ? (ArrayList) formulas.get(kind + '-' + (new Integer(argnum)).toString() + '-' + term) : (ArrayList) formulas.get(kind + '-' + term);
    }

    /**
     * *************************************************************
     * Merges a KIF object containing a single formula into the current KB.
     *
     * @param kif A KIF object.
     *
     * @param pathname The full, canonical pathname string of the constituent
     * file in which the formula will be saved, if known.
     *
     * @return If any of the formulas are already present, returns an ArrayList
     * containing the old (existing) formulas, else returns an empty ArrayList.
     */
    private ArrayList merge(KIF kif, String pathname) {

        ArrayList formulasPresent = new ArrayList();
        try {
            // Add all the terms from the new formula into the KB's current list
            terms.addAll(kif.terms);

            Set keys = kif.formulas.keySet();
            for (Object key1 : keys) {
                String key = (String) key1;
                ArrayList newFormulas = new ArrayList((Collection) kif.formulas.get(key));
                if (formulas.containsKey(key)) {
                    ArrayList oldFormulas = (ArrayList) formulas.get(key);
                    for (Object newFormula1 : newFormulas) {
                        Formula newFormula = (Formula) newFormula1;
                        if (pathname != null) {
                            newFormula.sourceFile = pathname;
                        }

                        boolean found = false;
                        for (Object oldFormula1 : oldFormulas) {
                            Formula oldFormula = (Formula) oldFormula1;
                            if (newFormula.theFormula.equals(oldFormula.theFormula)) {
                                found = true;
                                formulasPresent.add(oldFormula);
                                // System.out.println("INFO in KB.merge)");
                                // System.out.println("  newFormula == " + newFormula);
                                // System.out.println("  oldFormula == " + oldFormula);
                            }
                        }
                        if (!found) {
                            // value.computeTheClausalForm();
                            oldFormulas.add(newFormula);
                            formulaMap.put(newFormula.theFormula.intern(), newFormula);
                        }
                    }
                } else {
                    formulas.put(key, newFormulas);
                    Iterator it2 = newFormulas.iterator();
                    Formula f;
                    while (it2.hasNext()) {
                        f = (Formula) it2.next();
                        if (Formula.isNonEmptyString(f.theFormula)) {
                            // f.computeTheClausalForm();
                            formulaMap.put(f.theFormula.intern(), f);
                        }
                    }
                }
            }
            /* collectParents();
             if (KBmanager.getMgr().getPref("cache") != null &&
             KBmanager.getMgr().getPref("cache").equalsIgnoreCase("yes"))
             cache();  */     // caching is too slow to perform for just one formula
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return formulasPresent;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     *  Writes a list of Formulas to a file.
//     * @param formulas an AraryList of Strings.
//     * @param fname The fully qualified file name.
//     * @return void
//     */
//    private void writeFormulas(ArrayList formulas, String fname) throws IOException {
//
//        FileWriter fr = null;
//
//        try {
//            fr = new FileWriter(fname,true);
//            for (int i = 0; i < formulas.size(); i++) {
//                fr.write((String) formulas.get(i));
//                fr.write("\n");
//            }
//        }
//        catch (java.io.IOException e) {
//            System.out.println("Error writing file " + fname);
//        }
//        finally {
//            if (fr != null)
//                fr.close();
//        }
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     * Writes a single user assertion (String) to the end of a file.
     *
     * @param formula A String representing a SUO-KIF Formula.
     * @param fname A String denoting the pathname of the target file.
     * @return A long value indicating the number of bytes in the file after the
     * formula has been written. A value of 0L means that the file does not
     * exist, and so could not be written for some rule. A value of -1
     * probably means that some error occurred.
     */
    private long writeUserAssertion(String formula, String fname) throws IOException {

        long flen = -1L;
        FileWriter fr = null;

        try {
            File file = new File(fname);
            fr = new FileWriter(file, true);
            fr.write(formula);
            fr.write("\n");
            flen = file.length();
        } catch (IOException e) {
            System.out.println("Error writing file " + fname);
        } finally {
            if (fr != null) {
                fr.close();
            }
        }
        return flen;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * Adds a formula to the knowledge base.  Returns an XML formatted
//     * String that contains the response of the logic engine.  It
//     * should be of the form "<assertionResponse>...</assertionResponse>"
//     * where the body should be " Formula has been added to the session
//     * database" if all went well.
//     *
//     * @param input The String representation of a SUO-KIF Formula.
//     *
//     * @return A String indicating the status of the tell operation.
//     */
//    public String tell(String input) {
//
//        System.out.println("INFO in KB.tell(" + input + ")");
//
//        String result = "The formula could not be added";
//        try {
//            // 1. Parse the input string.
//            KIF kif = new KIF();
//            String msg = kif.parseStatement(input);
//            if (msg != null) {
//                result = "Error parsing \"" + input + "\": " + msg;
//            }
//            else if (kif.formulaSet.isEmpty()) {
//                result = "The input could not be parsed";
//            }
//            else {
//                // Make the pathname of the user assertions file.
//                File dir = new File(this.kbDir);
//                File file = new File(dir, (this.name + _userAssertionsString));
//                String filename = file.getCanonicalPath();
//                List formulasAlreadyPresent = merge(kif, filename);
//                if (!formulasAlreadyPresent.isEmpty()) {
//                    String sf = ((Formula)formulasAlreadyPresent.get(0)).sourceFile;
//                    result = "The formula was already added from " + sf;
//                }
//                else {
//                    ArrayList parsedFormulas = new ArrayList();
//                    String fstr = null;
//                    Formula parsedF = null;
//                    Iterator it = kif.formulaSet.iterator();
//                    boolean go = true;
//                    while (go && it.hasNext()) {
//                        // 2. Confirm that the input has been converted into
//                        // 2. at least one Formula object and stored in
//                        // 2. this.formulaMap.
//                        fstr = (String) it.next();
//                        parsedF = (Formula) this.formulaMap.get(fstr.intern());
//                        if (parsedF == null) {
//                            go = false;
//                        }
//                        else {
//                            parsedFormulas.add(parsedF);
//                        }
//                    }
//                    System.out.println("INFO in KB.tell()");
//                    System.out.println("  parsedFormulas == " + parsedFormulas);
//                    if (go && !parsedFormulas.isEmpty()) {
////                        if (!constituents.contains(filename)) {
////                            // System.out.println("INFO in KB.tell():
////                            // Adding file: " + filename + " to: " +
////                            // constituents.toString());
////
////                            // 3. If the assertions file exists, delete it.
////                            if (file.exists())
////                                file.delete();
////                            constituents.add(filename);
////                            KBmanager.getMgr().writeConfiguration();
////                        }
//                        it = parsedFormulas.iterator();
//                        while (it.hasNext()) {
//                            parsedF = (Formula) it.next();
//                            // 4. Write the formula to the user assertions file.
//                            parsedF.endFilePosition = writeUserAssertion(parsedF.theFormula, filename);
//                            parsedF.sourceFile = filename;
//                        }
//
//                        result = "The formula has been added for browsing";
//
//                        boolean allAdded = (inferenceEngine != null);
//                        ArrayList processedFormulas = new ArrayList();
//                        it = parsedFormulas.iterator();
//                        while (it.hasNext()) {
//                            processedFormulas.clear();
//                            parsedF = (Formula) it.next();
//                            // 5. Preproccess the formula.
//                            processedFormulas.addAll(parsedF.preProcess(false, this));
//                            if (processedFormulas.isEmpty()) {
//                                allAdded = false;
//                            }
//                            else {
//                                // 6. If TPTP != no, translate to TPTP.
//                                if (!KBmanager.getMgr().getPref("TPTP").equalsIgnoreCase("no")) {
//                                    parsedF.tptpParse(false, this, processedFormulas);
//
//                                    System.out.println("INFO in KB.tell()");
//                                    System.out.println("  theTptpFormulas == " + parsedF.getTheTptpFormulas());
//                                }
//                                // 7. If there is an logic engine,
//                                // 7. assert the formula to the
//                                // 7. logic engine's database.
//                                if (inferenceEngine != null) {
//                                    String ieResult = null;
//                                    Formula processedF = null;
//                                    Iterator it2 = processedFormulas.iterator();
//                                    while (it2.hasNext()) {
//                                        processedF = (Formula) it2.next();
//                                        System.out.println("ENTER Vampire.assertFormula(" + processedF.theFormula + ")");
//                                        ieResult = inferenceEngine.assertFormula(processedF.theFormula);
//                                        System.out.println("EXIT Vampire.assertFormula(" + processedF.theFormula + ")");
//                                        System.out.println("  " + ieResult);
//                                        if (ieResult.indexOf("Formula has been added") < 0) {
//                                            allAdded = false;
//                                        }
//                                    }
//                                }
//                            }
//
//                            // System.out.println("INFO in KB.tell(" + input + ")");
//                            // System.out.println("  parsedF == " + parsedF);
//                            // System.out.println("  formulaMap.get(parsedF.theFormula) == "
//                            //                    + formulaMap.get(parsedF.theFormula));
//                            // System.out.println("  parsedF.sourceFile == " + parsedF.sourceFile);
//                            // System.out.println("  parsedF.endFilePosition == " + parsedF.endFilePosition);
//                            // System.out.println("  parsedF.theTptpFormulas == " + parsedF.getTheTptpFormulas());
//
//                        }
//                        result += (allAdded ? " and logic" : " but not for local logic");
//                    }
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//            // System.out.println("Error in KB.tell(): " + ioe.getMessage());
//        }
//        /* collectParents();
//           if (KBmanager.getMgr().getPref("cache") != null &&
//           KBmanager.getMgr().getPref("cache").equalsIgnoreCase("yes"))
//           cache();        */   // caching is currently not efficient enough to invoke it after every assertion
//        System.out.println("INFO in KB.tell(" + input + ")");
//        System.out.println("  -> " + result);
//        return result;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * Submits a query to the logic engine.  Returns an XML
//     * formatted String that contains the response of the logic
//     * engine.  It should be in the form
//     * "<queryResponse>...</queryResponse>".
//     *
//     * @param suoKifFormula The String representation of the SUO-KIF
//     * query.
//     *
//     * @param timeout The number of seconds after which the logic
//     * engine should give up.
//     *
//     * @param maxAnswers The maximum number of answers (binding sets)
//     * the logic engine should return.
//     *
//     * @return A String indicating the status of the ask operation.
//     */
//    public String ask(String suoKifFormula, int timeout, int maxAnswers) {
//
//        System.out.println("INFO in KB.ask(" + suoKifFormula + ", " + timeout + ", " + maxAnswers + ")");
//
//        String result = "";
//        try {
//
//            // Start by assuming that the ask is futile.
//            result = "<queryResponse>\n<answer result=\"no\" number=\"0\">\n</answer>\n<summary proofs=\"0\"/>\n</queryResponse>\n";
//            result = result.replaceAll("&lt;","<");
//            result = result.replaceAll("&gt;",">");
//
//            if (Formula.isNonEmptyString(suoKifFormula)) {
//                Formula query = new Formula();
//                query.read(suoKifFormula);
//                ArrayList processedStmts = query.preProcess(true, this);
//
//                System.out.println("  processedStmts == " + processedStmts);
//
//                if (!processedStmts.isEmpty()) {
//                    result = this.inferenceEngine.submitQuery(((Formula)processedStmts.get(0)).theFormula,
//                                                              timeout,
//                                                              maxAnswers);
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return result;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * Takes a term and returns true if the term occurs in the KB.
     *
     * @param term A String.
     * @return true or false.
     */
    public boolean containsTerm(String term) {

        return terms.contains(term.intern());
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Takes a formula string and returns true if the corresponding
//     * Formula occurs in the KB.
//     *
//     * @param formula A String.
//     * @return true or false.
//     */
//    public boolean containsFormula(String formula) {
//
//        return formulaMap.containsKey(formula.intern());
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Count the number of terms in the knowledge base in order to
//     * present statistics to the user.
//     *
//     * @return The int(eger) number of terms in the knowledge base.
//     */
//    public int getCountTerms() {
//
//        return terms.size();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *  Count the number of relations in the knowledge base in order to
//     *  present statistics to the user.
//     *
//     *  @return The int(eger) number of relations in the knowledge base.
//     */
//    public int getCountRelations() {
//
//        return this.getAllInstances("Relation").size();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *  Count the number of formulas in the knowledge base in order to
//     *  present statistics to the user.
//     *
//     *  @return The int(eger) number of formulas in the knowledge base.
//     */
//    public int getCountAxioms() {
//
//        return formulaMap.size();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * An accessor providing a TreeSet of un-preProcessed String representations
     * of Formulas.
     *
     * @return A TreeSet of Strings.
     */
    public TreeSet getFormulas() {

        TreeSet newFormulaSet = new TreeSet(formulaMap.keySet());
        return newFormulaSet;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *  Count the number of rules in the knowledge base in order to
//     *  present statistics to the user. Note that the number of rules
//     *  is a subset of the number of formulas.
//     *
//     *  @return The int(eger) number of rules in the knowledge base.
//     */
//    public int getCountRules() {
//
//        List symbols = Arrays.asList("=>", "<=>");
//        int count = 0;
//        Formula f = null;
//        String arg0 = null;
//        Iterator it = formulaMap.values().iterator();
//        while (it.hasNext()) {
//            f = (Formula) it.next();
//            arg0 = f.car();
//            if (symbols.contains(arg0)) {
//                count++;
//            }
//        }
//        return count;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * Create an ArrayList of the specific size, filled with empty strings.
     */
    private ArrayList arrayListWithBlanks(int size) {

        ArrayList al = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            al.add("");
        }
        return al;
    }

    /**
     * ***************************************************************
     * Get the alphabetically nearest terms to the given term, which is not in
     * the KB. Elements 0-14 should be alphabetically lesser and 15-29
     * alphabetically greater. If the term is at the beginning or end of the
     * alphabet, fill in blank items with the empty string: "".
     */
    private ArrayList getNearestTerms(String term) {

        ArrayList al = arrayListWithBlanks(30);
        Object[] t = terms.toArray();
        int i = 0;
        while (i < t.length && ((Comparable<String>) t[i]).compareTo(term) < 0) {
            i++;
        }
        int lower = i;
        while (i - lower < 15 && lower > 0) {
            lower--;
            al.set(15 - (i - lower), t[lower]);
        }
        int upper = i - 1;
        System.out.println(t.length);
        while (upper - i < 14 && upper < t.length - 1) {
            upper++;
            al.set(15 + (upper - i), t[upper]);
        }
        return al;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Get the neighbors of this initial uppercase term (class or function).
//     */
//    public ArrayList getNearestRelations(String term) {
//
//        term = Character.toUpperCase(term.charAt(0)) + term.substring(1,term.length());
//        return getNearestTerms(term);
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Get the neighbors of this initial lowercase term (relation).
//     */
//    public ArrayList getNearestNonRelations(String term) {
//
//        term = Character.toLowerCase(term.charAt(0)) + term.substring(1,term.length());
//        return getNearestTerms(term);
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * Repopulates the format maps for lang.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    private void reloadFormatMaps(String lang) {
        try {

            String lingua;

            if (formatMap == null) {
                formatMap = new HashMap();
            }
            formatMap.clear();
            if (termFormatMap == null) {
                termFormatMap = new HashMap();
            }
            termFormatMap.clear();

            // System.out.println("INFO in KB.reloadFormatMaps(): Reading the format maps for " + lang);
            lingua = lang == null ? language : lang;
            long t1 = System.currentTimeMillis();
            ArrayList col = ask("arg", 0, "format");
            if ((col == null) || col.isEmpty()) {
                // System.out.println("Error in KB.reloadFormatMaps(): No relation formatting file loaded for language " + lang);
                return;
            }
            //System.out.println("Number of format statements: " + (new Integer(col.size())).toString());
            Iterator ite = col.iterator();
            Formula f;
            String arg1;
            String key;
            String format;
            while (ite.hasNext()) {
                f = (Formula) ite.next();
                arg1 = f.getArgument(1);
                if (arg1.equalsIgnoreCase(lingua)) {
                    key = f.getArgument(2);
                    format = f.getArgument(3);
                    if (format.length() > 0 && format.charAt(0) == '\"') {
                        format = format.substring(1);
                    }
                    if (format.length() > 0 && format.charAt(format.length() - 1) == '\"') {
                        format = format.substring(0, format.length() - 1);
                    }
                    if (format.indexOf('$') < 0) {
                        format = format.replaceAll("\\x26\\x25", "\\&\\%" + key + "\\$");
                    }
                    formatMap.put(key.intern(), format);
                }
            }
            System.out.println("INFO in KB.reloadFormatMaps(): "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds to build KB.formatMap");

            t1 = System.currentTimeMillis();
            col = ask("arg", 0, "termFormat");
            if ((col == null) || col.isEmpty()) {
                // System.out.println("Error in KB.reloadFormatMaps(): No term formatting file loaded for language: " + lang);
                return;
            }
            //System.out.println("Number of format statements: " + (new Integer(col.size())).toString());
            ite = col.iterator();
            while (ite.hasNext()) {
                f = (Formula) ite.next();
                arg1 = f.getArgument(1);
                if (arg1.equalsIgnoreCase(lingua)) {
                    key = f.getArgument(2);
                    format = f.getArgument(3);
                    if (format.length() > 0 && format.charAt(0) == '\"') {
                        format = format.substring(1);
                    }
                    if (format.length() > 0 && format.charAt(format.length() - 1) == '\"') {
                        format = format.substring(0, format.length() - 1);
                    }
                    //if (format.indexOf("$") < 0)
                    //    format = format.replaceAll("\\x26\\x25", "\\&\\%"+key+"\\$");
                    termFormatMap.put(key.intern(), format);
                }
            }
            System.out.println("INFO in KB.reloadFormatMaps(): "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds to build KB.termFormatMap");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        language = lang;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *  This method creates an association list (Map) of the natural
//     *  language format string and the relation name for which that
//     *  format string applies.  If the map has already been built and
//     *  the language hasn't changed, just return the existing map.
//     *  This is a case of "lazy evaluation".
//     *
//     *  @return An instance of Map where the keys are relation names
//     *  and the values are format strings.
//     */
//    public HashMap getFormatMap(String lang) {
//
//        if ((formatMap == null) || formatMap.isEmpty() || (!lang.equalsIgnoreCase(language))) {
//
//            // This is here to make sure LanguageFormatter.keywordMap
//            // is initialized.
//            LanguageFormatter.readKeywordMap(KBmanager.getMgr().getPref("kbDir"));
//
//            reloadFormatMaps(lang);
//        }
//        return formatMap;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * Deletes the user assertions file, and then reloads the KB.
     */
//    public void deleteUserAssertions() {
//
//        String cname = null;
//        for (int i = 0 ; i < constituents.size() ; i++) {
//            cname = (String) constituents.get(i);
//            if (cname.endsWith(_userAssertionsString)) {
//                try {
//                    constituents.remove(i);
//                    KBmanager.getMgr().writeConfiguration();
//                    reload();
//                }
//                catch (IOException ioe) {
//                    System.out.println("Error in KB.deleteUserAssertions: Error writing configuration: " + ioe.getMessage());
//                }
//            }
//        }
//        return;
//    }
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *  This method creates an association list (Map) of the natural
//     *  language string and the term for which that format string
//     *  applies.  If the map has already been built and the language
//     *  hasn't changed, just return the existing map.  This is a case
//     *  of "lazy evaluation".
//     *
//     *  @return An instance of Map where the keys are terms and the
//     *  values are format strings.
//     */
//    public HashMap getTermFormatMap(String lang) {
//
//        //System.out.println("INFO in KB.getTermFormatMap(): Reading the format map for " + lang + " with language=" + language);
//        if ((termFormatMap == null) || termFormatMap.isEmpty() || (!lang.equalsIgnoreCase(language))) {
//
//            // This is here to make sure LanguageFormatter.keywordMap
//            // is initialized.
//            LanguageFormatter.readKeywordMap(KBmanager.getMgr().getPref("kbDir"));
//
//            reloadFormatMaps(lang);
//        }
//        return termFormatMap;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * Add a new KB constituent by reading in the file, and then
//     * merging the formulas with the existing set of formulas.  All
//     * assertion caches are rebuilt, the current Vampire process is
//     * destroyed, and a new one is created.
//     *
//     * @param filename - the full path of the file being added.
//     */
//    public String addConstituent(String filename) {
//        return addConstituent(filename, true,  true);
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     * Add a new KB constituent by reading in the file, and then merging the
     * formulas with the existing set of formulas.
     *
     * @param filename - The full path of the file being added.
     * @param buildCachesP - If true, forces the assertion caches to be rebuilt.
     * @param loadVampireP - If true, destroys the old Vampire process and
     * starts a new one.
     */
    public String addConstituent(String filename, boolean buildCachesP, boolean loadVampireP) {

        System.out.println("ENTER KB.addConstituent(" + filename + ", " + buildCachesP + ", " + loadVampireP + ')');
        long t1 = System.currentTimeMillis();
        StringBuilder result = new StringBuilder();

        try {
            File constituent = new File(filename);
            String canonicalPath = constituent.getCanonicalPath();
            Iterator it;
            Iterator it2;
            KIF file = new KIF();
            String key;
            String internedFormula;
            ArrayList list;
            ArrayList newList;
            Formula f;

            if (constituents.contains(canonicalPath)) {
                return "Error: " + canonicalPath + " already loaded.";
            }
            System.out.println("INFO in KB.addConstituent(" + filename + ", " + buildCachesP + ", " + loadVampireP + ')');
            System.out.println("  Adding " + canonicalPath);
            try {
                file.readFile(canonicalPath);
            } catch (Exception ex1) {
                result.append(ex1.getMessage());
                if (ex1 instanceof ParseException) {
                    result.append(" at line ").append(((ParseException) ex1).getErrorOffset());
                }
                result.append(" in file ").append(canonicalPath);
                return result.toString();
            }

            System.out.println("INFO in KB.addConstituent(" + filename + ", " + buildCachesP + ", " + loadVampireP + ')');
            System.out.println("  Parsed file "
                    + canonicalPath
                    + " of size "
                    + file.formulas.keySet().size());
            it = file.formulas.keySet().iterator();
            int count = 0;
            while (it.hasNext()) {
                // Iterate through the formulas in the file, adding them to the KB, at the appropriate key.
                key = (String) it.next();
                // Note that this is a slow operation that needs to be improved
                // System.out.println("INFO KB.addConstituent(): Key " + key);
                if ((count++ % 100) == 1) {
                    System.out.print(".");
                }
                list = (ArrayList) formulas.get(key);
                if (list == null) {
                    list = new ArrayList();
                    formulas.put(key, list);
                }
                newList = (ArrayList) file.formulas.get(key);
                it2 = newList.iterator();
                while (it2.hasNext()) {
                    f = (Formula) it2.next();
                    internedFormula = f.theFormula.intern();
                    if (!list.contains(f)) {
                        list.add(f);
                        formulaMap.put(internedFormula, f);

                        // Force translation to clausal form, since it
                        // will be used multiple times later.
                        // f.computeTheClausalForm();
                    } else {
                        result.append("Warning: Duplicate axiom in ");
                        result.append(f.sourceFile).append(" at line ").append(f.startLine).append("<BR>");
                        result.append(f.theFormula).append("<P>");
                        Formula existingFormula = (Formula) formulaMap.get(internedFormula);
                        result.append("Warning: Existing formula appears in ");
                        result.append(existingFormula.sourceFile).append(" at line ").append(existingFormula.startLine).append("<BR>");
                        result.append("<P>");
                    }

                    /*
                     boolean found = false;
                     for (int j = 0; j < list.size(); j++) {         
                     if (j % 10000 == 1) System.out.print("!");            
                     if (f.deepEquals((Formula) list.get(j))) 
                     found = true;
                     }
                     if (!found) 
                     list.add(newArrayList.get(i));
                     */
                }
            }
            System.out.println("x");

            terms.addAll(file.terms);

            if (!constituents.contains(canonicalPath)) {
                constituents.add(canonicalPath);
            }

            System.out.println("INFO in KB.addConstituent(" + filename + ", " + buildCachesP + ", " + loadVampireP + ')');
            System.out.println("  File "
                    + canonicalPath
                    + " loaded in "
                    + ((System.currentTimeMillis() - t1) / 1000.0)
                    + " seconds");

            if (buildCachesP && !canonicalPath.endsWith(_cacheFileSuffix)) {
                buildRelationCaches();
            }

            if (loadVampireP) {
                loadVampire();
            }
        } catch (Exception ex) {
            result.append(ex.getMessage());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("EXIT KB.addConstituent(" + filename + ", " + buildCachesP + ", " + loadVampireP + ')');
        return result.toString();
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Reload all the KB constituents.
//     */
//    public String reload() {
//
//        System.out.println("ENTER KB.reload()");
//        StringBuilder result = new StringBuilder();
//        try {
//            ArrayList newConstituents = new ArrayList();
//            Iterator ci = constituents.iterator();
//            String cName = null;
//            while (ci.hasNext()) {
//                cName = (String) ci.next();
//
//                // Don't reuse the same cached data.  Instead, recompute
//                // it.
//                if (!(cName.endsWith(_cacheFileSuffix))) {
//                    newConstituents.add(cName);
//                }
//            }
//            constituents.clear();
//            language = "en";
//            formulas.clear();
//            formulaMap.clear();
//            terms.clear();
//            if (formatMap != null) {
//                formatMap.clear();
//            }
//            if (termFormatMap != null) {
//                termFormatMap.clear();
//            }
//
//            ci = newConstituents.iterator();
//            while (ci.hasNext()) {
//                cName = (String) ci.next();
//                System.out.println("INFO in KB.reload()");
//                System.out.println("  constituent == " + cName);
//                result.append(addConstituent(cName, false, false));
//            }
//
//            // Rebuild the in-memory relation caches.
//            buildRelationCaches();
//
//            // If cache == yes, write the cache file.
//            if (KBmanager.getMgr().getPref("cache").equalsIgnoreCase("yes")) {
//                result.append(this.cache());
//            }
//
//            // At this point, we have reloaded all constituents, have
//            // rebuilt the relation caches, and, if cache == yes, have
//            // written out the _Cache.kif file.  Now we reload the
//            // inferfence engine.
//            loadVampire();
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        System.out.println("EXIT KB.reload()");
//        return result.toString();
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Write a KIF file.
//     * @param fname - the name of the file to write, including full path.
//     */
//    public void writeFile(String fname) throws IOException {
//
//        FileWriter fr = null;
//        PrintWriter pr = null;
//        Iterator it;
//        HashSet formulaSet = new  HashSet();
//        ArrayList formulaArray;
//        String key;
//        ArrayList list;
//        Formula f;
//        String s;
//
//        try {
//            fr = new FileWriter(fname);
//            pr = new PrintWriter(fr);
//
//            it = formulas.keySet().iterator();
//            while (it.hasNext()) {
//                key = (String) it.next();
//                list = (ArrayList) formulas.get(key);
//                for (int i = 0; i < list.size(); i++) {
//                    f = (Formula) list.get(i);
//                    s = f.toString();
//                    pr.println(s);
//                    pr.println();
//                }
//            }
//        }
//        catch (java.io.IOException e) {
//            throw new IOException("Error writing file " + fname);
//        }
//        finally {
//            if (pr != null) {
//                pr.close();
//            }
//            if (fr != null) {
//                fr.close();
//            }
//        }
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     * A HashMap for holding compiled regular expression patterns. The map is
     * initialized by calling compilePatterns().
     */
    private static HashMap REGEX_PATTERNS = null;

    /**
     * ***************************************************************
     * This method returns a compiled regular expression Pattern object indexed
     * by key.
     *
     * @param key A String that is the retrieval key for a compiled regular
     * expression Pattern.
     *
     * @return A compiled regular expression Pattern instance.
     */
    public static Pattern getCompiledPattern(String key) {
        if (Formula.isNonEmptyString(key) && (REGEX_PATTERNS != null)) {
            ArrayList al = (ArrayList) REGEX_PATTERNS.get(key);
            if (al != null) {
                return (Pattern) al.get(0);
            }
        }
        return null;
    }

    /**
     * ***************************************************************
     * This method returns the int value that identifies the regular expression
     * binding group to be returned when there is a match.
     *
     * @param key A String that is the retrieval key for the binding group index
     * associated with a compiled regular expression Pattern.
     *
     * @return An int that indexes a binding group.
     */
    public static int getPatternGroupIndex(String key) {
        if (Formula.isNonEmptyString(key) && (REGEX_PATTERNS != null)) {
            ArrayList al = (ArrayList) REGEX_PATTERNS.get(key);
            if (al != null) {
                return ((Number) al.get(1)).intValue();
            }
        }
        return -1;
    }

    /**
     * ***************************************************************
     * This method compiles and stores regular expression Pattern objects and
     * binding group indexes as two cell ArrayList objects. Each ArrayList is
     * indexed by a String retrieval key.
     *
     * @return void
     */
    @SuppressWarnings("HardcodedFileSeparator")
    private static void compilePatterns() {
        if (REGEX_PATTERNS == null) {
            REGEX_PATTERNS = new HashMap();
            String[][] patternArray
                    = {{"row_var", "\\@ROW\\d*", "0"},
                    // { "open_lit", "\\(\\w+\\s+\\?\\w+\\s+.\\w+\\s*\\)", "0" },
                    {"open_lit", "\\(\\w+\\s+\\?\\w+[a-zA-Z_0-9-?\\s]+\\)", "0"},
                    {"pred_var_1", "\\(holds\\s+(\\?\\w+)\\W", "1"},
                    {"pred_var_2", "\\((\\?\\w+)\\W", "1"},
                    {"var_with_digit_suffix", "(\\D+)\\d*", "1"}
                    };
            String pName;
            Pattern p;
            Integer groupN;
            ArrayList pVal;
            for (String[] aPatternArray : patternArray) {
                pName = aPatternArray[0];
                p = Pattern.compile(aPatternArray[1]);
                groupN = new Integer(aPatternArray[2]);
                pVal = new ArrayList();
                pVal.add(p);
                pVal.add(groupN);
                REGEX_PATTERNS.put(pName, pVal);
            }
        }
    }

    /**
     * ***************************************************************
     * This method finds regular expression matches in an input string using a
     * compiled Pattern and binding group index retrieved with patternKey. If
     * the ArrayList accumulator is provided, match results are added to it and
     * it is returned. If accumulator is not provided (is null), then a new
     * ArrayList is created and returned if matches are found.
     *
     * @param input The input String in which matches are sought.
     *
     * @param patternKey A String used as the retrieval key for a regular
     * expression Pattern object, and an int index identifying a binding group.
     *
     * @param accumulator An optional ArrayList to which matches are added. Note
     * that if accumulator is provided, it will be the return value even if no
     * new matches are found in the input String.
     *
     * @return An ArrayList, or null if no matches are found and an accumulator
     * is not provided.
     */
    public static ArrayList getMatches(String input, String patternKey, ArrayList accumulator) {
        ArrayList ans = null;
        if (accumulator != null) {
            ans = accumulator;
        }
        if (REGEX_PATTERNS == null) {
            KB.compilePatterns();
        }
        if (Formula.isNonEmptyString(input) && Formula.isNonEmptyString(patternKey)) {
            Pattern p = KB.getCompiledPattern(patternKey);
            if (p != null) {
                Matcher m = p.matcher(input);
                int gidx = KB.getPatternGroupIndex(patternKey);
                if (gidx >= 0) {
                    while (m.find()) {
                        String rv = m.group(gidx);
                        if (Formula.isNonEmptyString(rv)) {
                            if (ans == null) {
                                ans = new ArrayList();
                            }
                            if (!(ans.contains(rv))) {
                                ans.add(rv);
                            }
                        }
                    }
                }
            }
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method finds regular expression matches in an input string using a
     * compiled Pattern and binding group index retrieved with patternKey, and
     * returns the results, if any, in an ArrayList.
     *
     * @param input The input String in which matches are sought.
     *
     * @param patternKey A String used as the retrieval key for a regular
     * expression Pattern object, and an int index identifying a binding group.
     *
     * @return An ArrayList, or null if no matches are found.
     */
    public static ArrayList getMatches(String input, String patternKey) {
        return KB.getMatches(input, patternKey, null);
    }

    /**
     * ***************************************************************
     * This method retrieves Formulas by asking the query expression queryLit,
     * and returns the results, if any, in an ArrayList.
     *
     * @param queryLit The query, which is assumed to be a List (atomic literal)
     * consisting of a single predicate and its arguments. The arguments could
     * be variables, constants, or a mix of the two, but only the first constant
     * encountered in a left to right sweep over the literal will be used in the
     * actual query.
     *
     * @return An ArrayList of Formula objects, or an empty ArrayList if no
     * answers are retrieved.
     */
    public ArrayList askWithLiteral(List queryLit) {
        ArrayList ans = new ArrayList();
        if ((queryLit instanceof List) && !(queryLit.isEmpty())) {
            String pred = (String) queryLit.get(0);
            if ("instance".equals(pred)
                    && isVariable((String) queryLit.get(1))
                    && !(isVariable((String) queryLit.get(2)))) {
                String className = (String) queryLit.get(2);
                String inst;
                String fStr;
                Formula f;
                Set ai = getAllInstances(className);
                for (Object anAi : ai) {
                    inst = (String) anAi;
                    fStr = ("(instance " + inst + ' ' + className + ')');
                    f = new Formula();
                    f.read(fStr);
                    ans.add(f);
                }
            } else if ("valence".equals(pred)
                    && isVariable((String) queryLit.get(1))
                    && isVariable((String) queryLit.get(2))) {
                String inst;
                String fStr;
                Formula f;
                Set ai = getAllInstances("Relation");
                Iterator it = ai.iterator();
                int valence;
                while (it.hasNext()) {
                    inst = (String) it.next();
                    valence = getValence(inst);
                    if (valence > 0) {
                        fStr = ("(valence " + inst + ' ' + valence + ')');
                        f = new Formula();
                        f.read(fStr);
                        ans.add(f);
                    }
                }
            } else {
                String constant = null;
                int cidx = -1;
                int qlLen = queryLit.size();
                String term;
                for (int i = 1; i < qlLen; i++) {
                    term = (String) queryLit.get(i);
                    if (Formula.isNonEmptyString(term)
                            && !(isVariable(term))) {
                        constant = term;
                        cidx = i;
                        break;
                    }
                }
                ans = constant != null ? askWithRestriction(cidx, constant, 0, pred) : ask("arg", 0, pred);
            }
        }
        return ans;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * This method retrieves formulas by asking the query expression
//     * queryLit, and returns the results, if any, in an ArrayList.
//     *
//     * @param queryLit The query, which is assumed to be an atomic
//     * literal consisting of a single predicate and its arguments.
//     * The arguments could be variables, constants, or a mix of the
//     * two, but only the first constant encountered in a left to right
//     * sweep over the literal will be used in the actual query.
//     *
//     * @return An ArrayList of Formula objects, or an empty ArrayList
//     * if no answers are retrieved.
//     */
//    public ArrayList askWithLiteral(Formula queryLit) {
//        List input = queryLit.literalToArrayList();
//        return askWithLiteral(input);
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * This method retrieves the upward transitive closure of all Class
//     * names contained in the input set.  The members of the input set are
//     * not included in the result set.
//     *
//     * @param classNames A Set object containing SUO-KIF class names
//     * (Strings).
//     *
//     * @return A Set of SUO-KIF class names, which could be empty.
//     */
//    private Set getAllSuperClasses(Set classNames) {
//        Set ans = new HashSet();
//        try {
//            if ((classNames instanceof Set) && !(classNames.isEmpty())) {
//                List accumulator = new ArrayList();
//                List working = new ArrayList();
//                String arg2 = null;
//                working.addAll(classNames);
//                while (!(working.isEmpty())) {
//                    for (int i = 0 ; i < working.size() ; i++) {
//                        List nextLits = askWithRestriction(1,
//                                                           (String) working.get(i),
//                                                           0,
//                                                           "subclass");
//
//                        if (nextLits != null) {
//                            for (int j = 0 ; j < nextLits.size() ; j++) {
//                                Formula f = (Formula) nextLits.get(j);
//                                arg2 = f.getArgument(2);
//                                if (! working.contains(arg2)) {
//                                    accumulator.add(arg2);
//                                }
//                            }
//                        }
//                    }
//                    ans.addAll(accumulator);
//                    working.clear();
//                    working.addAll(accumulator);
//                    accumulator.clear();
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return ans;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * This method retrieves the downward transitive closure of all Class
//     * names contained in the input set.  The members of the input set are
//     * not included in the result set.
//     *
//     * @param classNames A Set object containing SUO-KIF class names
//     * (Strings).
//     *
//     * @return A Set of SUO-KIF class names, which could be empty.
//     */
//    private Set getAllSubClasses(Set classNames) {
//        Set ans = new HashSet();
//        try {
//            if ((classNames instanceof Set) && !(classNames.isEmpty())) {
//                List accumulator = new ArrayList();
//                List working = new ArrayList();
//                String arg1 = null;
//                working.addAll(classNames);
//                while (!(working.isEmpty())) {
//                    for (int i = 0 ; i < working.size() ; i++) {
//                        List nextLits = askWithRestriction(2,
//                                                           (String) working.get(i),
//                                                           0,
//                                                           "subclass");
//
//                        if (nextLits != null) {
//                            for (int j = 0 ; j < nextLits.size() ; j++) {
//                                Formula f = (Formula) nextLits.get(j);
//                                arg1 = f.getArgument(1);
//                                if (! working.contains(arg1)) {
//                                    accumulator.add(arg1);
//                                }
//                            }
//                        }
//                    }
//                    ans.addAll(accumulator);
//                    working.clear();
//                    working.addAll(accumulator);
//                    accumulator.clear();
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return ans;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * This method retrieves all instances of the classes named in the input
     * set.
     *
     * @param classNames A Set object containing SUO-KIF class names (Strings).
     *
     * @return A TreeSet, possibly empty, containing SUO-KIF constant names.
     */
    protected TreeSet getAllInstances(Set classNames) {
        // System.out.println("ENTER KB.getAllInstances(" + classNames + ")");
        TreeSet ans = new TreeSet();
        try {
            if ((classNames instanceof Set) && !(classNames.isEmpty())) {
                Set partial;
                String name;
                for (Object className : classNames) {
                    name = (String) className;
                    partial = getCachedRelationValues("instance", name, 2, 1);
                    if (partial != null) {
                        ans.addAll(partial);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.println("EXIT KB.getAllInstances(" + classNames + ")");
        // System.out.println("=> " + ans);
        return ans;
    }

    /**
     * ***************************************************************
     * This method retrieves all instances of the class named in the input
     * String.
     *
     * @param className The name of a SUO-KIF Class.
     *
     * @return A TreeSet, possibly empty, containing SUO-KIF constant names.
     */
    public TreeSet getAllInstances(String className) {
        if (Formula.isNonEmptyString(className)) {
            TreeSet input = new TreeSet();
            input.add(className);
            return getAllInstances(input);
        }
        return new TreeSet();
    }

    /**
     * ***************************************************************
     * This method tries to find or compute a valence for the input relation.
     *
     * @param relnName A String, the name of a SUO-KIF Relation.
     *
     * @return An int value. -1 means that no valence value could be found. 0
     * means that the relation is a VariableArityRelation. 1-5 are the standard
     * SUO-KIF valence values.
     */
    public int getValence(String relnName) {

        // System.out.println("INFO in KB.getValence(" + relnName + ")");
        int ans = -1;
        try {
            if (Formula.isNonEmptyString(relnName)) {

                // First, see if the valence has already been cached.
                if (relationValences != null) {
                    int[] rv = (int[]) relationValences.get(relnName);
                    if (rv != null) {
                        ans = rv[0];
                        return ans;
                    }
                }

                // Grab all of the superrelations too, since we have
                // already computed them.
                Set relnSet = getCachedRelationValues("subrelation", relnName, 1, 2);
                if (relnSet == null) {
                    relnSet = new HashSet();
                }
                relnSet.add(relnName);

                Iterator it = relnSet.iterator();
                List literals;
                String relation;
                while (it.hasNext() && (ans < 0)) {

                    relation = (String) it.next();

                    // First, check to see if the KB actually contains an
                    // explicit valence value.  This is unlikely.
                    literals = askWithRestriction(1, relation, 0, "valence");
                    if ((literals != null) && !(literals.isEmpty())) {
                        Formula f = (Formula) literals.get(0);
                        String digit = f.getArgument(2);
                        if (Formula.isNonEmptyString(digit)) {
                            ans = Integer.parseInt(digit);
                            if (ans >= 0) {
                                break;
                            }
                        }
                    }

                    // See which valence-determining class the
                    // relation belongs to.
                    Set classNames = getCachedRelationValues("instance", relation, 1, 2);

                    // System.out.println("classNames == " + classNames);
                    if (classNames != null) {
                        String[][] tops = {{"VariableArityRelation", "0"},
                        {"UnaryFunction", "1"},
                        {"BinaryRelation", "2"},
                        {"TernaryRelation", "3"},
                        {"QuaternaryRelation", "4"},
                        {"QuintaryRelation", "5"},};

                        for (int i = 0; (ans < 0) && (i < tops.length); i++) {

                            if (classNames.contains(tops[i][0])) {
                                ans = Integer.parseInt(tops[i][1]);

                                // Sigh.  It's never simple.  The
                                // kluge below is to deal with the
                                // fact that a function, by
                                // definition, has a valence one less
                                // than the corresponding predicate.
                                // An instance of TernaryRelation that
                                // is also an instance of Function has
                                // a valence of 2, not 3.
                                if ((i > 1)
                                        && (relation.endsWith("Fn") || classNames.contains("Function"))
                                        && !(tops[i][0]).endsWith("Function")) {
                                    --ans;
                                }
                                break;
                            }
                        }
                    }
                }
                // Cache the answer, if there is one.
                if (ans >= 0) {
                    int[] rv = new int[1];
                    rv[0] = ans;
                    relationValences.put(relnName, rv);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.println("INFO in getValence(" + relnName + ") => " + ans);
        return ans;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *
//     * @return an ArrayList containing all predicates in this KB.
//     *
//     */
//    public ArrayList collectPredicates() {
//        ArrayList ans = new ArrayList();
//        Set preds = getCachedRelationValues("instance", "Predicate", 2, 1);
//        if (preds != null) {
//            ans.addAll(preds);
//        }
//        // System.out.println("Found " + ans.size() + " predicates");
//        return ans;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *
//     * @param obj Any object
//     *
//     * @return true if obj is a String representation of a LISP empty
//     * list, else false.
//     *
//     */
//    public static boolean isEmptyList(Object obj) {
//        return ((obj instanceof String) && Formula.empty((String) obj));
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *
//     * A utility method.
//     *
//     * @param objList A list of anything.
//     *
//     * @param label An optional label (String), or null.
//     *
//     * @return void
//     *
//     */
//    public static void printAll(List objList, String label) {
//        if (objList instanceof List) {
//            Iterator it = objList.iterator();
//            while (it.hasNext()) {
//                if (Formula.isNonEmptyString(label)) {
//                    System.out.println(label + ": " + it.next());
//                }
//                else {
//                    System.out.println(it.next());
//                }
//            }
//        }
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     *
     * A static utility method.
     *
     * @param obj Presumably, a String.
     *
     * @return true if obj is a SUO-KIF variable, else false.
     *
     */
    public static boolean isVariable(String obj) {
        if (Formula.isNonEmptyString(obj)) {
            return (obj.length() > 0 && obj.charAt(0) == '?' || obj.length() > 0 && obj.charAt(0) == '@');
        }
        return false;
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *
//     * A static utility method.
//     *
//     * @param obj A String.
//     *
//     * @return true if obj is a SUO-KIF logical quantifier, else
//     * false.
//     *
//     */
//    public static boolean isQuantifier(String obj) {
//
//        return (Formula.isNonEmptyString(obj)
//                && (obj.equals("forall") || obj.equals("exists")));
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     *
//     * A static utility method.
//     *
//     * @param obj Presumably, a String.
//     *
//     * @return true if obj is a SUO-KIF commutative logical operate,
//     * else false.
//     *
//     */
//    public static boolean isCommutative(String obj) {
//
//        return (Formula.isNonEmptyString(obj)
//                && (obj.equals("and") || obj.equals("or")));
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * Hyperlink terms identified with '&%' to the URL that brings up
//     * that term in the browser.  Handle (and ignore) suffixes on the term.
//     * For example "&%Processes" would get properly linked to the term "Process",
//     * if present in the knowledge base.
//     */
//    public String formatDocumentation(String href, String documentation) {
//
//        int i;
//        int j;
//        String term = "";
//        String newFormula = documentation;
//
//        while (newFormula.indexOf("&%") != -1) {
//            i = newFormula.indexOf("&%");
//            j = i + 2;
//            while (Character.isJavaIdentifierPart(newFormula.charAt(j)) && j < newFormula.length())
//                j++;
//            //System.out.println("Candidate term: " + newFormula.substring(i+2,j));
//
//            while (!containsTerm(newFormula.substring(i+2,j)) && j > i + 2)
//                j--;
//            term = newFormula.substring(i+2,j);
//            if (!"".equals(term) && containsTerm(newFormula.substring(i+2,j))) {
//                newFormula = newFormula.substring(0,i) +
//                    "<a href=\"" + href + "&term=" + term + "\">" + term + "</a>" +
//                    newFormula.substring(j,newFormula.length());
//            }
//            else
//                newFormula = newFormula.substring(0,i) + newFormula.substring(j,newFormula.length());
//        }
//        return newFormula;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     *  Pull all the formulas into one TreeSet of Strings.
//     */
//    private TreeSet collectAllFormulas(HashMap forms) {
//
//        TreeSet ts = new TreeSet();
//        ArrayList al = new ArrayList(forms.values());
//
//        for (int i = 0; i < al.size(); i++) {
//            ArrayList al2 = (ArrayList) al.get(i);
//            for (int j = 0; j < al2.size(); j++)
//                ts.add(((Formula) al2.get(j)).theFormula);
//        }
//        return ts;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     *  Pull all the formulas in an ArrayList into one TreeSet of Strings.
//     */
//    private TreeSet collectFormulasFromList(ArrayList forms) {
//
//        TreeSet ts = new TreeSet();
//        for (int j = 0; j < forms.size(); j++)
//            ts.add(((Formula) forms.get(j)).theFormula);
//        return ts;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     * Save the contents of the current KB to a file.
     */
    public String writeInferenceEngineFormulas(TreeSet forms) {

        // System.out.println("file separator == " + File.separator);
        FileWriter fr = null;
        PrintWriter pr = null;
        String filename = null;
        try {
            String inferenceEngine = KBmanager.getMgr().getPref("inferenceEngine");
            File executable;
            if (Formula.isNonEmptyString(inferenceEngine)) {
                executable = new File(inferenceEngine);
                if (DEBUG || executable.exists()) {
                    File dir = executable.getParentFile();
                    File file = new File(dir, (name + "-v.kif"));
                    filename = file.getCanonicalPath();

                    // System.out.println("filename == " + filename);
                    fr = new FileWriter(filename);
                    pr = new PrintWriter(fr);
                    for (Object form : forms) {
                        pr.println((String) form);
                        pr.println();
                    }
                    if (!file.exists()) {
                        filename = null;
                        throw new Exception("Error writing " + file.getCanonicalPath());
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in KB.writeInferenceEngineFormulas(): " + ex.getMessage());
            ex.printStackTrace();
        }
        if (pr != null) {
            try {
                pr.close();
            } catch (Exception e1) {
            }
        }
        if (fr != null) {
            try {
                fr.close();
            } catch (Exception e2) {
            }
        }
        return filename;
    }

    /**
     * *************************************************************
     * Starts Vampire and collects, preprocesses and loads all of the
     * constituents into it.
     */
    public void loadVampire() {

        // System.out.println("INFO in KB.loadVampire()");
        try {
            if (!formulaMap.isEmpty()) {

                System.out.println("INFO in KB.loadVampire(): preprocessing " + formulaMap.size() + " formulas");

                TreeSet forms = preProcess(formulaMap.keySet());
                String filename = writeInferenceEngineFormulas(forms);
                boolean vFileSaved = Formula.isNonEmptyString(filename);
                if (vFileSaved) {
                    System.out.println("INFO in KB.loadVampire(): " + forms.size() + " formulas saved to " + filename);
                } else {
                    System.out.println("INFO in KB.loadVampire(): new -v.kif file not written");
                }

                inferenceEngine = null;

                if (Formula.isNonEmptyString(KBmanager.getMgr().getPref("inferenceEngine")) && vFileSaved) {
                    //System.out.println("INFO in KB.loadVampire(): getting new logic engine");
                    //inferenceEngine = Vampire.getNewInstance(filename);
                    inferenceEngine = new KIFInference() {

                        @Override
                        public String assertFormula(String formula) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public String submitQuery(String formula, int timeLimit, int bindingsLimit) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void terminate() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    };
                }
                System.out.println("INFO in KB.loadVampire(): inferenceEngine == " + inferenceEngine);
            }
        } catch (Exception e) {
            System.out.println("Error in KB.loadVampire(): " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * A utility array for profiling subtasks in KB.preProcess().
     */
    protected static long[] ppTimers = {0L, // type pred (sortal) computation
        0L, // pred var instantion
        0L, // row var expansion
        0L, // Formula.getRowVarExpansionRange()
        0L, // Formula.toNegAndPosLitsWithRenameInfo()
        0L, // Formula.adjustExpansionCount()
        0L // Formula.preProcessRecurse()
};

    /**
     * ***************************************************************
     * Preprocess the knowledge base to work with Vampire. This includes "holds"
     * prefixing, ticking nested formulas, expanding row variables, and
     * translating mathematical relation operators.
     *
     * @return a TreeSet of Strings.
     */
    public TreeSet preProcess(Set forms) {
        // System.out.println("INFO in kb.preProcess()");
        TreeSet newTreeSet = new TreeSet();
        try {
            for (int i = 0; i < ppTimers.length; i++) {
                ppTimers[i] = 0L;
            }

            Formula.resetSortalTypeCache();

            boolean tptpParseP = "yes".equalsIgnoreCase(KBmanager.getMgr().getPref("TPTP"));
            long t1 = System.currentTimeMillis();
            String form;
            Formula f;
            // Formula newFormula = null;
            ArrayList processed;         // An ArrayList of Formula(s).
            // If the Formula which is to be preprocessed does not contain row
            // variables, then this list will have only one element.
            for (Object form1 : forms) {
                form = (String) form1;
                f = (Formula) formulaMap.get(form);
                // newFormula = new Formula();
                // newFormula.theFormula = new String(f.theFormula);

                // System.out.println("preProcess " + newFormula);
                // processed = newFormula.preProcess(false,this);   // not queries
                processed = f.preProcess(false, this);   // not queries

                if (tptpParseP) {
                    //try {
                    f.tptpParse(false, this, processed);   // not a query
                        /*
                     } catch (ParseException pe) {
                     String er = "Error in KB.preProcess(): " + pe.getMessage() + " for formula in file " + f.sourceFile + " at line " + f.startLine;
                     KBmanager.getMgr().setError(KBmanager.getMgr().getError() + "\n<br/>" + er + "\n<br/>");
                     System.out.println(er);
                     } catch (IOException ioe) {
                     System.out.println("Error in KB.preProcess: " + ioe.getMessage());
                     }*/
                }

                for (Object aProcessed : processed) {
                    Formula p = (Formula) aProcessed;
                    if (p.theFormula != null) {
                        newTreeSet.add(p.theFormula);
                    }
                }
            }
            long dur = (System.currentTimeMillis() - t1);
            System.out.println("INFO in KB.preProcess()");
            System.out.println("  "
                    + (dur / 1000.0)
                    + " seconds total to produce "
                    + newTreeSet.size()
                    + " formulas");
            System.out.println("    "
                    + (ppTimers[1] / 1000.0)
                    + " seconds instantiating predicate variables");
            System.out.println("    "
                    + (ppTimers[2] / 1000.0)
                    + " seconds expanding row variables");
            System.out.println("      "
                    + (ppTimers[3] / 1000.0)
                    + " seconds in Formula.getRowVarExpansionRange()");
            System.out.println("        "
                    + (ppTimers[4] / 1000.0)
                    + " seconds in Formula.toNegAndPosLitsWithRenameInfo()");
            System.out.println("      "
                    + (ppTimers[5] / 1000.0)
                    + " seconds in Formula.adjustExpansionCount()");
            System.out.println("    "
                    + (ppTimers[0] / 1000.0)
                    + " seconds adding type predicates");
            System.out.println("    "
                    + (ppTimers[6] / 1000.0)
                    + " seconds in Formula.preProcessRecurse()");
            for (int i = 0; i < ppTimers.length; i++) {
                ppTimers[i] = 0L;
            }

            if (tptpParseP) {
                int goodCount = 0;
                int badCount = 0;
                List badList = new ArrayList();
                for (Object o : formulaMap.values()) {
                    f = (Formula) o;
                    if (f.getTheTptpFormulas().isEmpty()) {
                        badCount++;
                        if (badCount < 11) {
                            badList.add(f);
                        }
                    } else {
                        goodCount++;
                        if (goodCount < 10) {
                            System.out.println("Sample TPTP translation: " + f.getTheTptpFormulas().get(0));
                        }
                    }
                }
                System.out.println("INFO in KB.preProcess(): TPTP translation succeeded for "
                        + goodCount
                        + " formula"
                        + ((goodCount == 1) ? "" : "s"));
                boolean someAreBad = (badCount > 0);
                System.out.println("INFO in KB.preProcess(): TPTP translation failed for "
                        + badCount
                        + " formula"
                        + ((badCount == 1) ? "" : "s")
                        + (someAreBad ? ":" : ""));
                if (someAreBad) {
                    Iterator it = badList.iterator();
                    for (int i = 1; it.hasNext(); i++) {
                        f = (Formula) it.next();
                        System.out.println("[" + i + "]: " + f);
                    }
                    if (badCount > 10) {
                        System.out.println("  " + (badCount - 10) + " more ...");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Formula.destroySortalTypeCache();

        return newTreeSet;
    }

    /**
     * *************************************************************
     */
    private void writePrologFormulas(ArrayList forms, PrintWriter pr) {

        TreeSet ts = new TreeSet(forms);
        if (forms != null) {
            int i = 0;
            for (Object t : ts) {
                Formula formula = (Formula) t;
                String result = formula.toProlog();
                if (result != null && !result.isEmpty()) {
                    pr.println(result);
                }
                if (i % 100 == 1) {
                    System.out.print(".");
                }
            }
        }
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     */
//    public String writePrologFile(String fname) {
//
//        File file = null;
//        FileWriter fr = null;
//        PrintWriter pr = null;
//        String result = null;
//
//        try {
//            file = new File(fname);
//
//            System.out.println("INFO in KB.writePrologFile(): Writing " + file.getCanonicalPath());
//
//            if ((WordNet.wn != null) && WordNet.wn.wordFrequencies.isEmpty()) {
//                WordNet.wn.readWordFrequencies();
//            }
//            fr = new FileWriter(file);
//            pr = new PrintWriter(fr);
//            pr.println("% Copyright (c) 2006 Articulate Software Incorporated");
//            pr.println("% This software released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.");
//            pr.println("% This is a very lossy translation to prolog of the KIF ontologies available at www.ontologyportal.org\n");
//
//            pr.println("% subAttribute");
//            writePrologFormulas(ask("arg",0,"subAttribute"),pr);
//            pr.println("\n% subrelation");
//            writePrologFormulas(ask("arg",0,"subrelation"),pr);
//            pr.println("\n% disjoint");
//            writePrologFormulas(ask("arg",0,"disjoint"),pr);
//            pr.println("\n% partition");
//            writePrologFormulas(ask("arg",0,"partition"),pr);
//            pr.println("\n% instance");
//            writePrologFormulas(ask("arg",0,"instance"),pr);
//            pr.println("\n% subclass");
//            writePrologFormulas(ask("arg",0,"subclass"),pr);
//            System.out.println(" ");
//
//            pr.flush();
//            result = file.getCanonicalPath();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Error in KB.writePrologFile(): " + e.getMessage());
//        }
//        if (pr != null) {
//            try {
//                pr.close();
//            }
//            catch (Exception e1) {
//            }
//        }
//        if (fr != null) {
//            try {
//                fr.close();
//            }
//            catch (Exception e2) {
//            }
//        }
//        return result;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** *************************************************************
//     * This method translates the entire KB to TPTP format, storing
//     * the translation for each Formula in the List identified by the
//     * private member Formula.theTptpFormulas.  Use
//     * Formula.getTheTptpFormulas() to accesss the TPTP sentences
//     * (Strings) that constitute the translation for a single SUO-KIF
//     * Formula.
//     *
//     * @return An int indicating the number of Formulas that were
//     * successfully translated.
//     */
//    public int tptpParse() {
//        int goodCount = 0;
//        try {
//            int badCount = 0;
//            ArrayList badList = new ArrayList();
//            Formula f = null;
//            Iterator it = this.formulaMap.values().iterator();
//            while (it.hasNext()) {
//                f = (Formula) it.next();
//                f.tptpParse(false, this);
//                if (f.getTheTptpFormulas().isEmpty()) {
//                    badCount++;
//                    if (badList.size() < 11) {
//                        badList.add(f);
//                    }
//                }
//                else {
//                    goodCount++;
//                }
//            }
//            System.out.println("INFO in KB.tptpParse(): TPTP translation succeeded for "
//                               + goodCount
//                               + " formula"
//                               + ((goodCount == 1) ? "" : "s"));
//            boolean someAreBad = (badCount > 0);
//            System.out.println("INFO in KB.tptpParse(): TPTP translation failed for "
//                               + badCount
//                               + " formula"
//                               + ((badCount == 1) ? "" : "s")
//                               + (someAreBad ? ":" : ""));
//            if (someAreBad) {
//                it = badList.iterator();
//                for (int i = 1 ; it.hasNext() ; i++) {
//                    f = (Formula) it.next();
//                    System.out.println("[" + i + "]: " + f);
//                }
//                if (badCount > 10) {
//                    System.out.println("  " + (badCount - 10) + " more ...");
//                }
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return goodCount;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * *************************************************************
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public String writeTPTPFile(String fileName,
                                Formula conjecture,
                                boolean onlyPlainFOL,
                                String reasoner) {

        String result = null;
        String sanitizedKBName;
        File outputFile;
        PrintWriter pr = null;
        int axiomIndex = 1;
        TreeSet orderedFormulae;
        String theTPTPFormula;
        boolean sanitizedFormula;
        boolean commentedFormula;

        sanitizedKBName = name.replaceAll("\\W", "_");
        try {

            System.out.println("INFO in KB.writeTPTPFile(\""
                    + fileName
                    + "\", "
                    + conjecture
                    + ", "
                    + onlyPlainFOL
                    + ", \""
                    + reasoner
                    + "\")");

            //----If file name is a directory, create filename therein
            if (fileName == null) {
                outputFile = File.createTempFile(sanitizedKBName, ".p", null);
                //----Delete temp file when program exits.
                outputFile.deleteOnExit();
            } else {
                outputFile = new File(fileName);
            }
            String canonicalPath = outputFile.getCanonicalPath();
            System.out.println("INFO in KB.writeTPTPFile(): Writing " + canonicalPath);

            pr = new PrintWriter(new FileWriter(outputFile));
            pr.println("% Copyright 2007 Articulate Software Incorporated");
            pr.println("% This software released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.");
            pr.println("% This is a translation to TPTP of KB "
                    + sanitizedKBName + '\n');

            orderedFormulae = new TreeSet((o1, o2) -> {
                Formula f1 = (Formula) o1;
                Formula f2 = (Formula) o2;
                int fileCompare = f1.sourceFile.compareTo(f2.sourceFile);
                if (fileCompare == 0) {
                    fileCompare = (new Integer(f1.startLine)).compareTo(f2.startLine);
                    if (fileCompare == 0) {
                        fileCompare = (new Long(f1.endFilePosition)).compareTo(f2.endFilePosition);
                    }
                }
                return fileCompare;
            });
            orderedFormulae.addAll(formulaMap.values());

            if (onlyPlainFOL) {
                Formula.resetSortalTypeCache();
            }

            Iterator ite = orderedFormulae.iterator();
            List tptpFormulas;
            Iterator tptpIt;
            Formula f;
            while (ite.hasNext()) {
                f = (Formula) ite.next();
                tptpFormulas = f.getTheTptpFormulas();

                // System.out.println("  1 : tptpFormulas == " + tptpFormulas);
                //----If we are writing "sanitized" tptp, aka onlyPlainFOL,
                //----here we rename all VariableArityRelations so that each
                //----relation name has a numeric suffix corresponding to the
                //----number of the relation's arguments.  This is required
                //----for some provers, such as E and EP.
                if (onlyPlainFOL
                        && !tptpFormulas.isEmpty()
                        && !"yes".equalsIgnoreCase(KBmanager.getMgr().getPref("holdsPrefix"))
                        && f.containsVariableArityRelation(this)) {
                    Formula tmpF = new Formula();
                    tmpF.read(f.theFormula);
                    List processed = tmpF.preProcess(false, this);
                    List withRelnRenames;
                    if (!processed.isEmpty()) {
                        withRelnRenames = new ArrayList();
                        Iterator it2 = processed.iterator();
                        Formula f2;
                        while (it2.hasNext()) {
                            f2 = (Formula) it2.next();
                            withRelnRenames.add(f2.renameVariableArityRelations(this));
                        }
                        tmpF.tptpParse(false, this, withRelnRenames);
                        tptpFormulas = tmpF.getTheTptpFormulas();
                        // System.out.println("  2 : tptpFormulas == " + tptpFormulas);
                    }
                }

                tptpIt = tptpFormulas.iterator();
                while (tptpIt.hasNext()) {
                    theTPTPFormula = (String) tptpIt.next();
                    commentedFormula = false;
                    if (onlyPlainFOL) {
                        //----Remove interpretations of arithmetic
                        theTPTPFormula = theTPTPFormula.
                                replaceAll("[$]less", "dollar_less").replaceAll("[$]greater", "dollar_greater").
                                replaceAll("[$]time", "dollar_times").replaceAll("[$]divide", "dollar_divide").
                                replaceAll("[$]plus", "dollar_plus").replaceAll("[$]minus", "dollar_minus");
                        //----Don't output ""ed ''ed and numbers
                        if (theTPTPFormula.indexOf('\'') >= 0
                                || theTPTPFormula.indexOf('"') >= 0
                                || theTPTPFormula.matches(".*[(,]-?[0-9].*")) {
                            pr.print("%FOL ");
                            commentedFormula = true;
                        }
                        if ("Equinox---1.0b".equals(reasoner) && f.theFormula.indexOf("equal") > 2) {
                            Formula f2 = new Formula();
                            f2.read(f.cdr());
                            f2.read(f.car());
                            if ("equals".equals(f2.theFormula)) {
                                pr.print("%FOL ");
                                commentedFormula = true;
                            }
                        }
                    }
                    pr.println("fof(kb_" + sanitizedKBName + '_' + axiomIndex++
                            + ",axiom,(" + theTPTPFormula + ")).");
                    // if (commentedFormula) {
                    pr.println();
                    // }
                }
                if (f.getTheTptpFormulas().isEmpty()) {
                    String addErrStr = "No TPTP formula for <br/>" + f.htmlFormat(this);
                    KBmanager.getMgr().setError(KBmanager.getMgr().getError()
                            + "<br/>\n" + addErrStr + "\n<br/>");
                    System.out.println("INFO in KB.writeTPTPFile(): No TPTP formula for\n" + f);
                }
            }
            //----Print conjecture if one has been supplied
            if (conjecture != null) {
                tptpIt = conjecture.getTheTptpFormulas().iterator();
                // conjecture.getTheTptpFormulas() should return a
                // List containing only one String, so the iteration
                // below is probably unnecessary.  I don't know if the
                // provers on the target server can even handle
                // multiple conjectures.
                while (tptpIt.hasNext()) {
                    theTPTPFormula = (String) tptpIt.next();
                    pr.println("fof(prove_from_" + sanitizedKBName
                            + ",conjecture,(" + theTPTPFormula + ")).");
                }
            }
            result = canonicalPath;
        } catch (Exception e) {
            System.out.println("Error in KB.writeTPTPFile(): " + e.getMessage());
            e.printStackTrace();
        }

        if (onlyPlainFOL) {
            Formula.destroySortalTypeCache();
        }

        if (pr != null) {
            try {
                pr.close();
            } catch (Exception e1) {
            }
        }

        return result;
    }

    /**
     * *************************************************************
     * Instances of RelationCache hold the cached extensions and, when possible,
     * the computed closures, of selected relations. Canonical examples are the
     * caches for subclass and instance.
     *
     */
    static class RelationCache extends HashMap {

        private String relationName = "";

        public String getRelationName() {
            return relationName;
        }

        private int keyArgument = -1;

        public int getKeyArgument() {
            return keyArgument;
        }

        private int valueArgument = -1;

        public int getValueArgument() {
            return valueArgument;
        }

        private boolean isClosureComputed = false;

// --Commented out by Inspection START (8/15/14 2:38 AM):
//        public boolean getIsClosureComputed() {
//            return isClosureComputed;
//        }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
        public void setIsClosureComputed(boolean computed) {
            isClosureComputed = computed;
        }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//        private RelationCache() {
//        }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
        public RelationCache(String predName, int keyArg, int valueArg) {
            relationName = predName;
            keyArgument = keyArg;
            valueArgument = valueArg;
        }

        @SuppressWarnings("CloneReturnsClassType")
        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    /**
     * *************************************************************
     * This method currently takes one command-line argument, which should be
     * the absolute pathname of the directory in which the source Merge,kif file
     * is located. The resulting tptp file will be named TPTP-TEST-KB.tptp, and
     * will be written to the same directory.
     */
    public static void main(String[] args) {

        try {
            if (args[0] == null) {
                System.out.println("Usage: java -classpath <path> com.articulate.sigma.KB <kb-dir>");
                System.exit(1);
            }

            KBmanager mgr = KBmanager.getMgr();

            // These three parameters, along with the consituent
            // (.kif) files loaded, determine the set of SUO-KIF
            // assertions that will serve as the source for a
            // translation of the KB to TPTP.
            mgr.setPref("holdsPrefix", "no");
            mgr.setPref("typePrefix", "no");
            mgr.setPref("cache", "no");

            // This parameter determines if the entire KB will be
            // translated to TPTP as part of the loading and
            // processing of the .kif constituent files.
            mgr.setPref("TPTP", "yes");

            mgr.setPref("inferenceEngine", null);

            mgr.setPref("kbDir", args[0]);

            mgr.kbs.clear();

            mgr.addKB("TPTP-TEST-KB");

            KB kb = mgr.getKB("TPTP-TEST-KB");

            kb.constituents.clear();

            File kbDir = new File(mgr.getPref("kbDir"));

            File kifFileToLoad = new File(kbDir, "Merge.kif");

            kb.addConstituent(kifFileToLoad.getCanonicalPath(),
                    // Compute caches of "virtual" assertions,
                    true,
                    // Don't write a file of processed
                    // SUO-KIF formulas for the logic
                    // engine, and don't try to start an
                    // logic engine process.
                    false
            );

            kb.preProcess(kb.getFormulas());

            File tptpFile = new File(kbDir, kb.name + ".tptp");

            String fileWritten = kb.writeTPTPFile(tptpFile.getCanonicalPath(), null, false, "none");

            if (Formula.isNonEmptyString(fileWritten)) {
                System.out.println("File written: " + fileWritten);
            } else {
                System.out.println("Could not write " + tptpFile.getCanonicalPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
