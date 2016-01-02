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
package nars.kif;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ***************************************************************
 * @author Adam Pease
 */
public class WordNetUtilities {

    /**
     * POS-prefixed mappings from a new synset number to the old one.
     */
    HashMap mappings = new HashMap();

    /**
     * ***************************************************************
     * Get a SUMO term minus its &% prefix and one character mapping suffix.
     */
    public static String getBareSUMOTerm(String term) {

        return term != null && !term.isEmpty() ? term.substring(2, term.length() - 1) : "";
    }

    /**
     * ***************************************************************
     * Extract the POS from a word_POS_num sense key
     */
    public static String getPOSfromKey(String sense) {

        int firstUS = sense.indexOf('_');
        return sense.substring(firstUS + 1, firstUS + 3);
    }

    /**
     * ***************************************************************
     */
    public static String removeTermPrefixes(String formula) {

        return formula.replaceAll("&%", "");
    }

    /**
     * ***************************************************************
     * Convert a list of Terms in the format "&%term1 &%term2" to an ArrayList
     * of bare term Strings
     */
    public static ArrayList convertTermList(String termList) {

        ArrayList result = new ArrayList();
        String[] list = termList.split(" ");
        for (String aList : list) {
            result.add(getBareSUMOTerm(aList));
        }
        return result;
    }

    /**
     * ***************************************************************
     * Get a SUMO term mapping suffix.
     */
    public static char getSUMOMappingSuffix(String term) {

        return term != null && !term.isEmpty() ? term.charAt(term.length() - 1) : ' ';
    }

    /**
     * ***************************************************************
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static String convertWordNetPointer(String ptr) {

        if ("!".equals(ptr)) {
            ptr = "antonym";
        }
        if ("@".equals(ptr)) {
            ptr = "hypernym";
        }
        if ("@i".equals(ptr)) {
            ptr = "instance hypernym";
        }
        if ("~".equals(ptr)) {
            ptr = "hyponym";
        }
        if ("~i".equals(ptr)) {
            ptr = "instance hyponym";
        }
        if ("#m".equals(ptr)) {
            ptr = "member holonym";
        }
        if ("#s".equals(ptr)) {
            ptr = "substance holonym";
        }
        if ("#p".equals(ptr)) {
            ptr = "part holonym";
        }
        if ("%m".equals(ptr)) {
            ptr = "member meronym";
        }
        if ("%s".equals(ptr)) {
            ptr = "substance meronym";
        }
        if ("%p".equals(ptr)) {
            ptr = "part meronym";
        }
        if ("=".equals(ptr)) {
            ptr = "attribute";
        }
        if ("+".equals(ptr)) {
            ptr = "derivationally related";
        }
        if (";c".equals(ptr)) {
            ptr = "domain topic";
        }
        if ("-c".equals(ptr)) {
            ptr = "member topic";
        }
        if (";r".equals(ptr)) {
            ptr = "domain region";
        }
        if ("-r".equals(ptr)) {
            ptr = "member region";
        }
        if (";u".equals(ptr)) {
            ptr = "domain usage";
        }
        if ("-u".equals(ptr)) {
            ptr = "member usage";
        }
        if ("*".equals(ptr)) {
            ptr = "entailment";
        }
        if (">".equals(ptr)) {
            ptr = "cause";
        }
        if ("^".equals(ptr)) {
            ptr = "also see";
        }
        if ("$".equals(ptr)) {
            ptr = "verb group";
        }
        if ("&".equals(ptr)) {
            ptr = "similar to";
        }
        if ("<".equals(ptr)) {
            ptr = "participle";
        }
        if ("\\".equals(ptr)) {
            ptr = "pertainym";
        }
        return ptr;
    }

    /**
     * ***************************************************************
     */
    public static char posLetterToNumber(char POS) {

        switch (POS) {
            case 'n':
                return '1';
            case 'v':
                return '2';
            case 'a':
                return '3';
            case 'r':
                return '4';
            case 's':
                return '5';
        }
        System.out.println("Error in WordNetUtilities.posLetterToNumber(): bad letter: " + POS);
        return '1';
    }

    /**
     * ***************************************************************
     */
    public static char posNumberToLetter(char POS) {

        switch (POS) {
            case '1':
                return 'n';
            case '2':
                return 'v';
            case '3':
                return 'a';
            case '4':
                return 'r';
            case '5':
                return 's';
        }
        System.out.println("Error in WordNetUtilities.posLetterToNumber(): bad number: " + POS);
        return 'n';
    }

    /**
     * ***************************************************************
     * Convert a part of speech number to the two letter format used by the
     * WordNet sense index code. Defaults to noun "NN".
     */
    public static String posNumberToLetters(String pos) {

        if ("1".equalsIgnoreCase(pos)) {
            return "NN";
        }
        if ("2".equalsIgnoreCase(pos)) {
            return "VB";
        }
        if ("3".equalsIgnoreCase(pos)) {
            return "JJ";
        }
        if ("4".equalsIgnoreCase(pos)) {
            return "RB";
        }
        if ("5".equalsIgnoreCase(pos)) {
            return "JJ";
        }
        System.out.println("Error in WordNetUtilities.posNumberToLetters(): bad number: " + pos);
        return "NN";
    }

    /**
     * ***************************************************************
     * Convert a part of speech number to the two letter format used by the
     * WordNet sense index code. Defaults to noun "NN".
     */
    public static String posLettersToNumber(String pos) {

        if ("NN".equalsIgnoreCase(pos)) {
            return "1";
        }
        if ("VB".equalsIgnoreCase(pos)) {
            return "2";
        }
        if ("JJ".equalsIgnoreCase(pos)) {
            return "3";
        }
        if ("RB".equalsIgnoreCase(pos)) {
            return "4";
        }
        System.out.println("Error in WordNetUtilities.posNumberToLetters(): bad letters: " + pos);
        return "1";
    }

    /**
     * ***************************************************************
     * Take a WordNet sense identifier, and return the integer part of speech
     * code.
     */
    public static int sensePOS(String sense) {

        if (sense.contains("_NN_")) {
            return WordNet.NOUN;
        }
        if (sense.contains("_VB_")) {
            return WordNet.VERB;
        }
        if (sense.contains("_JJ_")) {
            return WordNet.ADJECTIVE;
        }
        if (sense.contains("_RB_")) {
            return WordNet.ADVERB;
        }
        System.out.println("Error in WordNetUtilities.sensePOS(): Unknown part of speech type in sense code: " + sense);
        return 0;
    }

    /**
     * ***************************************************************
     */
    public static String mappingCharToName(char mappingType) {

        String mapping = "";
        switch (mappingType) {
            case '=':
                mapping = "equivalent";
                break;
            case ':':
                mapping = "anti-equivalent";
                break;
            case '+':
                mapping = "subsuming";
                break;
            case '[':
                mapping = "negated subsuming";
                break;
            case '@':
                mapping = "instance";
                break;
            case ']':
                mapping = "negated instance";
                break;
        }
        return mapping;
    }

    /**
     * ***************************************************************
     * A utility function that mimics the functionality of the perl substitution
     * feature (s/match/replacement/). Note that only one replacement is made,
     * not a global replacement.
     *
     * @param result is the string on which the substitution is performed.
     * @param match is the substring to be found and replaced.
     * @param subst is the string replacement for match.
     * @return is a String containing the result of the substitution.
     */
    public static String subst(String result, String match, String subst) {

        Pattern p = Pattern.compile(match);
        Matcher m = p.matcher(result);
        if (m.find()) {
            result = m.replaceFirst(subst);
        }
        return result;
    }

    /**
     * ***************************************************************
     * A utility function that mimics the functionality of the perl substitution
     * feature (s/match/replacement/) but rather than returning the result of
     * the substitution, just tests whether the result is a key in a hashtable.
     * Note that only one replacement is made, not a global replacement.
     *
     * @param result is the string on which the substitution is performed.
     * @param match is the substring to be found and replaced.
     * @param subst is the string replacement for match.
     * @param hash is a hashtable to be checked against the result.
     * @return is a boolean indicating whether the result of the substitution
     * was found in the hashtable.
     */
    public static boolean substTest(String result, String match, String subst, Hashtable hash) {

        Pattern p = Pattern.compile(match);
        Matcher m = p.matcher(result);
        if (m.find()) {
            result = m.replaceFirst(subst);
            return hash.containsKey(result);
        } else {
            return false;
        }
    }

    /**
     * ***************************************************************
     */
    private static boolean isVowel(char c) {

        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }

    /**
     * ***************************************************************
     * Return the plural form of the verb. Handle multi-word phrases to modify
     * only the first word.
     */
    public static String verbPlural(String verb) {

        String word = verb;
        String remainder = "";
        if (verb.indexOf('_') > 0) {
            word = verb.substring(0, verb.indexOf('_'));
            remainder = verb.substring(verb.indexOf('_'), verb.length());
        }

        // if (exceptionVerbPluralHash.containsKey(word))                  Note that there appears to be no WordNet exception list for verb plurals, just tenses
        //    word = (String) exceptionVerbPluralHash.get(word);
        if (word.matches(".*y$") && !isVowel(word.charAt(word.length() - 2))) {
            word = WordNetUtilities.subst(word, "y$", "ies");
        } else {
            if (word.matches(".*s$") || word.matches(".*x$") || word.matches(".*ch$")
                    || word.matches(".*sh$") || word.matches(".*z$") || "go".equals(word)) {
                word += "es";
            } else if ("be".equals(word)) {
                word = "are";
            } else {
                word += "s";
            }
        }
        return word + remainder;
    }

    /**
     * ***************************************************************
     * HTML format a TreeMap of word senses and their associated synset
     */
    public static String formatWords(TreeMap words, String kbName) {

        StringBuilder result = new StringBuilder();
        int count = 0;
        Iterator it = words.keySet().iterator();
        while (it.hasNext() && count < 50) {
            String word = (String) it.next();
            String synset = (String) words.get(word);
            result.append("<a href=\"WordNet.jsp?word=");
            result.append(word);
            result.append("&POS=");
            result.append(synset.substring(0, 1));
            result.append("&kb=");
            result.append(kbName);
            result.append("&synset=");
            result.append(synset.substring(1, synset.length()));
            result.append("\">").append(word).append("</a>");
            count++;
            if (it.hasNext() && count < 50) {
                result.append(", ");
            }
        }
        if (it.hasNext() && count >= 50) {
            result.append("...");
        }
        return result.toString();
    }

    /**
     * ***************************************************************
     * HTML format a TreeMap of ArrayLists word senses
     */
    public static String formatWordsList(TreeMap words, String kbName) {

        StringBuilder result = new StringBuilder();
        int count = 0;
        Iterator it = words.keySet().iterator();
        while (it.hasNext() && count < 50) {
            String word = (String) it.next();
            ArrayList synsetList = (ArrayList) words.get(word);
            for (int i = 0; i < synsetList.size(); i++) {
                String synset = (String) synsetList.get(i);
                result.append("<a href=\"WordNet.jsp?word=");
                result.append(word);
                result.append("&POS=");
                result.append(synset.substring(0, 1));
                result.append("&kb=");
                result.append(kbName);
                result.append("&synset=");
                result.append(synset.substring(1, synset.length()));
                result.append("\">").append(word).append("</a>");
                count++;
                if (i < synsetList.size() - 1) {
                    result.append(", ");
                }
            }
            if (it.hasNext() && count < 50) {
                result.append(", ");
            }
        }
        if (it.hasNext() && count >= 50) {
            result.append("...");
        }
        return result.toString();
    }

    /**
     * ***************************************************************
     * Routine called by mergeUpdates which does the bulk of the work. Should
     * not be called during normal interactive running of Sigma.
     */
    private static void processMergers(HashMap hm, String fileName, String pattern, String posNum) throws IOException {

        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            KB kb = KBmanager.getMgr().getKB("SUMO");
            fw = new FileWriter(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName + "-new.txt");
            pw = new PrintWriter(fw);

            FileReader r = new FileReader(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName + ".txt");
            LineNumberReader lr = new LineNumberReader(r);
            String line;
            while ((line = lr.readLine()) != null) {
                if (lr.getLineNumber() % 1000 == 0) {
                    System.out.print('.');
                }
                Pattern p = Pattern.compile(pattern);
                line = line.trim();
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String oldTerm = m.group(4);
                    String bareOldTerm = getBareSUMOTerm(oldTerm);
                    String mapType = oldTerm.substring(oldTerm.length() - 1);
                    String synset = posNum + m.group(1);
                    String newTerm = (String) hm.get(synset);
                    if (!bareOldTerm.contains("&%") && newTerm != null && !newTerm.isEmpty() && !newTerm.equals(bareOldTerm) && kb.childOf(newTerm, bareOldTerm)) {
                        pw.println(m.group(1) + m.group(2) + "| " + m.group(3) + " &%" + newTerm + mapType);
                        System.out.println("INFO in WordNet.processMergers(): synset, oldTerm, newterm: "
                                + synset + ' ' + oldTerm + ' ' + newTerm);
                    } else {
                        pw.println(m.group(1) + m.group(2) + "| " + m.group(3) + ' ' + m.group(4));
                    }
                } else {
                    pw.println(line.trim());
                }
            }
        } catch (IOException e) {
            throw new IOException("Error writing file " + fileName + '\n' + e.getMessage());
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                fw.close();
            }
        }
    }

    /**
     * ***************************************************************
     * Read in a file with a nine-digit synset number followed by a space and a
     * SUMO term. If the term is more specific than the current mapping for that
     * synset, replace the old term. This is a utility that is not normally
     * called from the interactive Sigma system.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static void mergeUpdates() throws IOException {

        HashMap hm = new HashMap();

        String dir = "/Program Files/Apache Software Foundation/Tomcat 5.5/KBs";
        FileReader r = new FileReader(dir + File.separator + "newMappings20.dat");
        LineNumberReader lr = new LineNumberReader(r);
        String line;
        while ((line = lr.readLine()) != null) {
            if (line.length() > 11) {
                String synset = line.substring(0, 9);
                String SUMOterm = line.substring(10);
                hm.put(synset, SUMOterm);
            }
        }

        String fileName = "WordNetMappings-nouns";
        String pattern = "^([0-9]{8})([\\S\\s]+)\\|\\s([\\S\\s]+?)\\s(\\&\\%\\S+[\\S\\s]+)$";
        String posNum = "1";
        processMergers(hm, fileName, pattern, posNum);
        fileName = "WordNetMappings-verbs";
        pattern = "^([0-9]{8})([^\\|]+)\\|\\s([\\S\\s]+?)\\s(\\&\\%\\S+[\\S\\s]+)$";
        posNum = "2";
        processMergers(hm, fileName, pattern, posNum);
        fileName = "WordNetMappings-adj";
        pattern = "^([0-9]{8})([\\S\\s]+)\\|\\s([\\S\\s]+?)\\s(\\&\\%\\S+[\\S\\s]+)$";
        posNum = "3";
        processMergers(hm, fileName, pattern, posNum);
        fileName = "WordNetMappings-adv";
        pattern = "^([0-9]{8})([\\S\\s]+)\\|\\s([\\S\\s]+)\\s(\\&\\%\\S+[\\S\\s]+)$";
        posNum = "4";
        processMergers(hm, fileName, pattern, posNum);
    }

    /**
     * ***************************************************************
     * Given a POS-prefixed synset that is not mapped to SUMO, go up the
     * hypernym links to try to find a synset that is linked. Return the SUMO
     * term with its mapping type suffix and &% prefix. Note that in cases where
     * there are multiple hpernyms, When the first hypernym doesn't yield a good
     * SUMO term, the routine does a depth first search (although going "up" the
     * tree of hypernyms) to find a good term.
     */
    private static String findMappingFromHypernym(String synset) {

        ArrayList rels = (ArrayList) WordNet.wn.relations.get(synset);   // relations requires prefixes
        if (rels != null) {
            for (Object rel : rels) {
                AVPair avp = (AVPair) rel;
                if ("hypernym".equals(avp.attribute) || "instance hypernym".equals(avp.attribute)) {
                    String mappingChar;
                    mappingChar = "instance hypernym".equals(avp.attribute) ? "@" : "+";
                    String targetSynset = avp.value;
                    String targetSUMO = WordNet.wn.getSUMOMapping(targetSynset);
                    if (targetSUMO != null && !targetSUMO.isEmpty()) {
                        if (targetSUMO.charAt(targetSUMO.length() - 1) == '[') {
                            mappingChar = "[";
                        }
                        if (Character.isUpperCase(targetSUMO.charAt(2))) // char 2 is start of actual term after &%
                        {
                            return "&%" + getBareSUMOTerm(targetSUMO) + mappingChar;
                        } else {
                            String candidate = findMappingFromHypernym(targetSynset);
                            if (candidate != null && !candidate.isEmpty()) {
                                return candidate;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * ***************************************************************
     * This is a utility routine that should not be called during normal Sigma
     * operation. It does most of the actual work for deduceMissingLinks()
     */
    public static void processMissingLinks(String fileName, String pattern, String posNum) throws IOException {

        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            KB kb = KBmanager.getMgr().getKB("SUMO");
            fw = new FileWriter(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName + "-new.txt");
            pw = new PrintWriter(fw);

            FileReader r = new FileReader(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName + ".txt");
            LineNumberReader lr = new LineNumberReader(r);
            String line;
            while ((line = lr.readLine()) != null) {
                if (lr.getLineNumber() % 1000 == 0) {
                    System.out.print('.');
                }
                Pattern p = Pattern.compile(pattern);
                line = line.trim();
                Matcher m = p.matcher(line);
                if (line.contains("&%")) {
                    pw.println(line.trim());
                } else {
                    if (m.matches()) {
                        String synset = posNum + m.group(1);
                        String newTerm = findMappingFromHypernym(synset);
                        if (newTerm != null && !newTerm.isEmpty()) {
                            pw.println(m.group(1) + m.group(2) + "| " + m.group(3) + ' ' + newTerm);
//                            System.out.println("INFO in WordNet.processMissingLinks(): synset, newterm: " + 
//                                               synset + " " + " " + newTerm);
                        } else {
                            pw.println(line.trim());
                            System.out.println("INFO in WordNet.processMissingLinks(): No term found for synset"
                                    + synset);
                        }
                    } else {
                        pw.println(line.trim());
                    }
                }
                m = p.matcher(line);
            }
        } catch (IOException e) {
            throw new IOException("Error writing file " + fileName + '\n' + e.getMessage());
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                fw.close();
            }
        }
    }

    /**
     * ***************************************************************
     * Use the WordNet hyper-/hypo-nym links to deduce a likely tlink for a SUMO
     * term that has not yet been manually linked. This is a utility routine
     * that should not be called during normal Sigma operation.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static void deduceMissingLinks() throws IOException {

        String fileName = "WordNetMappings-nouns";
        String pattern = "^([0-9]{8})([\\S\\s_]+)\\|\\s([\\S\\s]+?)\\s*$";
        String posNum = "1";
        processMissingLinks(fileName, pattern, posNum);
        fileName = "WordNetMappings-verbs";
        pattern = "^([0-9]{8})([^\\|]+)\\|\\s([\\S\\s]+?)\\s*$";
        posNum = "2";
        processMissingLinks(fileName, pattern, posNum);
        fileName = "WordNetMappings-adj";
        pattern = "^([0-9]{8})([\\S\\s]+)\\|\\s([\\S\\s]+?)\\s*$";
        posNum = "3";
        processMissingLinks(fileName, pattern, posNum);
        fileName = "WordNetMappings-adv";
        pattern = "^([0-9]{8})([\\S\\s]+)\\|\\s([\\S\\s]+)\\s*$";
        posNum = "4";
        processMissingLinks(fileName, pattern, posNum);
    }

    /**
     * ***************************************************************
     * This is a utility routine that should not be called during normal Sigma
     * operation. It does most of the actual work for updateWNversion(). The
     * output is a set of WordNet data files with a "-new" suffix.
     */
    public void updateWNversionProcess(String fileName, String pattern, String posNum) throws IOException {

        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            KB kb = KBmanager.getMgr().getKB("SUMO");
            fw = new FileWriter(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName + "-new");
            pw = new PrintWriter(fw);

            FileReader r = new FileReader(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName);
            LineNumberReader lr = new LineNumberReader(r);
            String line;
            while ((line = lr.readLine()) != null) {
                if (lr.getLineNumber() % 1000 == 0) {
                    System.out.print('.');
                }
                Pattern p = Pattern.compile(pattern);
                line = line.trim();
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String newsynset = posNum + m.group(1);
                    String oldsynset = (String) mappings.get(newsynset);
                    if (oldsynset != null && !oldsynset.isEmpty()) {
                        String term = "";
                        oldsynset = oldsynset.substring(1);
                        switch (posNum.charAt(0)) {
                            case '1':
                                term = (String) WordNet.wn.nounSUMOHash.get(oldsynset);
                                break;
                            case '2':
                                term = (String) WordNet.wn.verbSUMOHash.get(oldsynset);
                                break;
                            case '3':
                                term = (String) WordNet.wn.adjectiveSUMOHash.get(oldsynset);
                                break;
                            case '4':
                                term = (String) WordNet.wn.adverbSUMOHash.get(oldsynset);
                                break;
                        }
                        if (term == null) {
                            pw.println(line.trim());
                            System.out.println("Error in WordNetUtilities.updateWNversionProcess(): No term for synsets (old, new): "
                                    + posNum + oldsynset + ' ' + posNum + newsynset);
                        } else {
                            pw.println(line + ' ' + term);
                        }
                    } else {
                        pw.println(line.trim());
                        System.out.println("Error in WordNetUtilities.updateWNversionProcess(): No mapping for synset: " + newsynset);
                    }
                } else {
                    pw.println(line.trim());
                }
            }
        } catch (IOException e) {
            throw new IOException("Error writing file " + fileName + '\n' + e.getMessage());
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                fw.close();
            }
        }
    }

    /**
     * ***************************************************************
     * Read the version mapping files and store in the HashMap called
     * "mappings".
     */
    public void updateWNversionReading(String fileName, String pattern, String posNum) throws IOException {

        try {
            FileReader r = new FileReader(KBmanager.getMgr().getPref("kbDir") + File.separator + fileName);
            LineNumberReader lr = new LineNumberReader(r);
            String line;
            while ((line = lr.readLine()) != null) {
                if (lr.getLineNumber() % 1000 == 0) {
                    System.out.print('.');
                }
                Pattern p = Pattern.compile(pattern);
                line = line.trim();
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String newsynset = posNum + m.group(1);
                    String oldsynset = posNum + m.group(2);
                    mappings.put(newsynset, oldsynset);
                } else {
                    System.out.println("INFO in WordNetUtilities.updateWNversionReading(): no match for line: " + line);
                }
            }
        } catch (IOException e) {
            throw new IOException("Error writing file " + fileName + '\n' + e.getMessage());
        }
    }

    /**
     * ***************************************************************
     * Port the mappings from one version of WordNet to another. It calls
     * updateWNversionReading to do most of the work. It assumes that the
     * mapping file has the new synset first and the old one second. File names
     * are for the new WordNet version, which will need to have different names
     * from the old version that WordNet.java needs to read in order to get the
     * existing mappings. This is a utility which should not be called during
     * normal Sigma operation. Mapping files are in a simple format produced by
     * University of Catalonia and available at
     * http://www.lsi.upc.edu/~nlp/web/index.php?option=com_content&task=view&id=21&Itemid=57
     * If that address changes you may also start at
     * http://www.lsi.upc.edu/~nlp/web/ and go to Resources and then an item on
     * WordNet mappings.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public void updateWNversion() throws IOException {

        String fileName = "wn30-21.noun";
        String pattern = "^(\\d+) (\\d+) .*$";
        String posNum = "1";
        updateWNversionReading(fileName, pattern, posNum);
        fileName = "wn30-21.verb";
        pattern = "^(\\d+) (\\d+) .*$";
        posNum = "2";
        updateWNversionReading(fileName, pattern, posNum);
        fileName = "wn30-21.adj";
        pattern = "^(\\d+) (\\d+) .*$";
        posNum = "3";
        updateWNversionReading(fileName, pattern, posNum);
        fileName = "wn30-21.adv";
        pattern = "^(\\d+) (\\d+) .*$";
        posNum = "4";
        updateWNversionReading(fileName, pattern, posNum);

        fileName = "data3.noun";
        pattern = "^([0-9]{8}) .+$";
        posNum = "1";
        updateWNversionProcess(fileName, pattern, posNum);
        fileName = "data3.verb";
        pattern = "^([0-9]{8}) .+$";
        posNum = "2";
        updateWNversionProcess(fileName, pattern, posNum);
        fileName = "data3.adj";
        pattern = "^([0-9]{8}) .+$";
        posNum = "3";
        updateWNversionProcess(fileName, pattern, posNum);
        fileName = "data3.adv";
        pattern = "^([0-9]{8}) .+$";
        posNum = "4";
        updateWNversionProcess(fileName, pattern, posNum);
    }

}
