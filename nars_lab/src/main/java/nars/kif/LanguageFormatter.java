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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ***************************************************************
 * A class that handles the generation of natural language from logic.
 *
 * @author Adam Pease - apease [at] articulatesoftware [dot] com, with thanks to
 * Michal Sevcenko - sevcenko@vc.cvut.cz for development of the formatting
 * language.
 */
public enum LanguageFormatter {
    ;

    private static HashMap keywordMap;

    /**
     * ***************************************************************
     */
    private static String getKeyword(String englishWord, String language) {

        String ans = "";
        HashMap hm = (HashMap) keywordMap.get(englishWord);
        if (hm != null) {
            ans = (String) hm.get(language);
            if (ans == null) {
                ans = "";
            }
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Format a list of variables which are not enclosed by parens
     */
    private static String formatList(String list, String language) {

        StringBuilder result = new StringBuilder();
        String[] ar = list.split(" ");
        for (int i = 0; i < ar.length; i++) {
            if (i == 0) {
                result.append(transliterate(ar[0], language));
            }
            if (i > 0 && i < ar.length - 1) {
                result.append(", ").append(transliterate(ar[i], language));
            }
            if (i == ar.length - 1) {
                result.append(' ').append(getKeyword("and", language)).append(' ').append(transliterate(ar[i], language));
            }
        }
        return result.toString();
    }

    /**
     * ***************************************************************
     */
    private static boolean logicalOperator(String word) {

        return "if".equals(word) || "then".equals(word) || "=>".equals(word)
                || "and".equals(word) || "or".equals(word)
                || "<=>".equals(word) || "not".equals(word)
                || "forall".equals(word) || "exists".equals(word)
                || "holds".equals(word);
    }

    /**
     * ***************************************************************
     */
    private static String transliterate(String word, String language) {

        if (word.charAt(0) != '?') {
            return word;
        } else if ("ar".equals(language)) {
            StringBuilder result = new StringBuilder();
            result.append('?');
            for (int i = 1; i < word.length(); i++) {
                switch (word.charAt(i)) {
                    case 'A':
                        result.append("\u0627\u0654");
                        break;
                    case 'B':
                        result.append('\uFE8F');
                        break;
                    case 'C':
                        result.append('\uFED9');
                        break;
                    case 'D':
                        result.append('\uFEA9');
                        break;
                    case 'E':
                        result.append('\u0650');
                        break;
                    case 'F':
                        result.append('\uFED1');
                        break;
                    case 'G':
                        result.append('\uFE9D');
                        break;
                    case 'H':
                        result.append('\uFEE9');
                        break;
                    case 'I':
                        result.append('\u0650');
                        break;
                    case 'J':
                        result.append('\uFE9D');
                        break;
                    case 'K':
                        result.append('\uFED9');
                        break;
                    case 'L':
                        result.append('\uFEDD');
                        break;
                    case 'M':
                        result.append('\uFEE1');
                        break;
                    case 'N':
                        result.append('\uFEE5');
                        break;
                    case 'O':
                        result.append('\u064F');
                        break;
                    case 'P':
                        result.append('\uFE8F');
                        break;
                    case 'Q':
                        result.append('\uFED5');
                        break;
                    case 'R':
                        result.append('\uFEAD');
                        break;
                    case 'S':
                        result.append('\uFEB1');
                        break;
                    case 'T':
                        result.append('\uFE95');
                        break;
                    case 'U':
                        result.append('\u064F');
                        break;
                    case 'V':
                        result.append('\uFE8F');
                        break;
                    case 'W':
                        result.append('\uFEED');
                        break;
                    case 'X':
                        result.append('\uFEAF');
                        break;
                    case 'Y':
                        result.append('\uFEF1');
                        break;
                    case 'Z':
                        result.append('\uFEAF');
                        result.append(word.charAt(i));
                }
            }
            return result.toString();
        } else {
            return word;
        }
    }

    /**
     * ***************************************************************
     * Read a set of standard words and phrases in several languages. Each
     * phrase must appear on a new line with alternatives separated by '|'. The
     * first entry should be a set of two letter language identifiers.
     *
     * @return a HashMap of HashMaps where the first HashMap has a key of the
     * English phrase, and the interior HashMap has a key of the two letter
     * language identifier.
     */
    public static HashMap readKeywordMap(String dir) {

        // System.out.println("INFO in LanguageFormatter.readKeywordMap()");
        // System.out.println("  dir == " + dir);
        if (keywordMap == null) {
            keywordMap = new HashMap();
        }
        if (keywordMap.isEmpty()) {

            System.out.println("INFO in LanguageFormatter.readKeywordMap(): filling keywordMap");

            String fname = null;
            String line;
            HashMap newLine;
            ArrayList languageKeyArray = new ArrayList();
            String key;
            int i;
            int count;
            BufferedReader br;
            try {
                File dirFile = new File(dir);
                File file = new File(dirFile, "language.txt");
                fname = file.getCanonicalPath();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(fname), "UTF-8"));
            } catch (IOException ioe) {
                System.out.println("Error in LanguageFormatter.readKeywordMap(): Error opening file " + fname);
                // System.out.println(System.getProperty("user.dir"));
                return keywordMap;
            }

            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(fname), "UTF-8"));
                // lnr = new LineNumberReader(new FileReader(fname));
                do {
                    line = br.readLine();
                    if (line != null) {
                        //noinspection IfStatementWithTooManyBranches
                        if (line.startsWith("en|")) { // The language key line.
                            i = 0;
                            while (line.indexOf('|', i) > 0) {
                                languageKeyArray.add(line.substring(i, line.indexOf('|', i)));
                                i = line.indexOf('|', i) + 1;
                            }
                            languageKeyArray.add(line.substring(i, i + 2));
                        } else if (line.length() > 0 && line.charAt(0) == ';' || line.isEmpty()) {  // ignore comment lines
                        } else if (line.indexOf('|') > -1) { // Line with phrase alternates in different languages.
                            newLine = new HashMap();
                            key = line.substring(0, line.indexOf('|'));
                            i = 0;
                            count = 0;
                            while (line.indexOf('|', i) > 0) {
                                newLine.put(languageKeyArray.get(count), line.substring(i, line.indexOf('|', i)));
                                i = line.indexOf('|', i) + 1;
                                count++;
                            }
                            newLine.put(languageKeyArray.get(count), line.substring(i, line.length()));
                            // System.out.println("INFO in LanguageFormatter.keywordMap(): key: " + key + " value: " + newLine);
                            keywordMap.put(key.intern(), newLine);
                        } else {
                            System.out.println("INFO in LanguageFormatter.keywordMap(): Unrecognized line in language.txt: " + line);
                        }
                    }
                } while (line != null);
            } catch (IOException ioe) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error in LanguageFormatter.keywordMap(): Error closing file " + fname);
                }
                return (keywordMap);
            }
            try {
                br.close();
            } catch (IOException e) {
                System.out.println("Error  in LanguageFormatter.readKeywordMap(): Error closing file " + fname);
            }
        }
        return (keywordMap);
    }

    /**
     * ***************************************************************
     *
     */
    private static String processAtom(String atom, Map termMap, String language) {

        if (atom.charAt(0) == '?') {
            return transliterate(atom, language);
        }
        if (termMap.containsKey(atom)) {
            return (String) termMap.get(atom);
        }
        return atom;
    }

    /**
     * ***************************************************************
     * For debugging ...
     */
    private static void printSpaces(int depth) {
        for (int i = 0; i <= depth; i++) {
            System.out.print("  ");
        }
        System.out.print(depth + ":");
    }

    /**
     * ***************************************************************
     * Create a natural language paraphrase of a logical statement. This is the
     * entry point for this function, but kifExprPara does most of the work.
     *
     * @param stmt The statement to be paraphrased.
     * @param isNegMode Whether the statement is negated.
     * @param phraseMap An association list of relations and their natural
     * language format statements.
     * @param termMap An association list of terms and their natural language
     * format statements.
     * @return A String, which is the paraphrased statement.
     */
    public static String nlStmtPara(String stmt,
            boolean isNegMode,
            Map phraseMap,
            Map termMap,
            String language,
            int depth) {

        /*
         System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
         printSpaces( depth );
         System.out.println( "stmt == " + stmt );
         */
        if (stmt == null || stmt.length() < 1) {
            System.out.println("Error in LanguageFormatter.nlStmtPara(): stmt is empty");
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "a:return == \"\"" );
             */
            return "";
        }
        if ((phraseMap == null) || phraseMap.isEmpty()) {
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "b:return == \"\"" );
             */
            return "";
        }
        if ((termMap == null) || termMap.isEmpty()) {
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "c:return == \"\"" );
             */
            return "";
        }
        StringBuilder result = new StringBuilder();
        String ans = null;
        Formula f = new Formula();
        f.read(stmt);
        if (f.atom()) {
            ans = processAtom(stmt, termMap, language);
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "d:return == " + ans );
             */
            return ans;
        }
        if (!f.listP()) {
            System.out.println("Error in LanguageFormatter.nlStmtPara(): Statement is not an atom or a list: " + stmt);
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "e:return == \"\"" );
             */
            return "";
        }
        /*
         if (phraseMap == null) {
         System.out.println("Error in LanguageFormatter.nlStmtPara(): phrase map is null.");
         phraseMap = new HashMap();
         }
         */
        String pred = f.car();
        if (!Formula.atom(pred)) {
            System.out.println("Error in LanguageFormatter.nlStmtPara(): statement: " + stmt + " has a formula in the predicate position.");
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "f:return == " + stmt );
             */
            return stmt;
        }
        if (logicalOperator(pred)) {
            ans = paraphraseLogicalOperator(stmt, isNegMode, phraseMap, termMap, language, depth + 1);
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "g:return == " + ans );
             */
            return ans;
        }
        if (phraseMap.containsKey(pred)) {
            ans = paraphraseWithFormat(stmt, isNegMode, phraseMap, termMap, language);
            /*
             System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "h:return == " + ans );
             */
            return ans;
        }
        // predicate has no paraphrase
        if (pred.charAt(0) == '?') {
            result.append(transliterate(pred, language));
        } else {
            if (termMap.containsKey(pred)) {
                result.append((String) termMap.get(pred));
            } else {
                result.append(pred);
            }
        }
        f.read(f.cdr());
        while (!f.empty()) {
            String arg = f.car();
            f.read(f.cdr());
            if (!Formula.atom(arg)) {
                result.append(' ').append(nlStmtPara(arg, isNegMode, phraseMap, termMap, language, depth + 1));
            } else {
                result.append(' ').append(translateWord(termMap, arg, language));
            }
        }
        ans = result.toString();
        /*
         System.out.println( "INFO in LanguageFormatter.nlStmtPara( " + depth + " ):" );
         printSpaces( depth );
         System.out.println( "i:return == " + ans );
         */
        return ans;
    }

    /**
     * ***************************************************************
     * Return the NL format of an individual word.
     */
    private static String translateWord(Map termMap, String word, String language) {

        if (termMap != null && termMap.containsKey(word)) {
            return ((String) termMap.get(word));
        } else {
            return word.charAt(0) == '?' ? transliterate(word, language) : word;
        }
    }

    /**
     * ***************************************************************
     * Create a natural language paraphrase for statements involving the logical
     * operators.
     *
     * @param pred the logical predicate in the expression
     * @param isNegMode is the expression negated?
     * @param words the expression as an ArrayList of tokens
     * @return the natural language paraphrase as a String, or null if the
     * predicate was not a logical operate.
     */
    private static String paraphraseLogicalOperator(String stmt,
            boolean isNegMode,
            Map phraseMap,
            Map termMap,
            String language,
            int depth) {

        //System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator(): stmt == " + stmt);
	/*
         System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
         printSpaces( depth );
         System.out.println( "stmt == " + stmt);
         */
        if (keywordMap == null) {
            System.out.println("Error in LanguageFormatter.paraphraseLogicalOperator(): keywordMap is null");
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "a:return == null" );
             */
            return null;
        }
        ArrayList args = new ArrayList();
        Formula f = new Formula();
        f.read(stmt);
        String pred = f.getArgument(0);
        f.read(f.cdr());

        String ans = null;
        if ("not".equals(pred)) {
            ans = nlStmtPara(f.car(), true, phraseMap, termMap, language, depth + 1);
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "b:return == " + ans );
             */
            return ans;
        }

        while (!f.empty()) {
            String arg = f.car();
            String result = nlStmtPara(arg, false, phraseMap, termMap, language, depth + 1);

            if (result != null && !result.isEmpty() && !result.isEmpty()) {
                args.add(result);
            } else {
                System.out.println("INFO in LanguageFormatter.paraphraseLogicalOperators(): bad result for: " + arg);
                arg = " ";
            }

            // System.out.println("INFO in LanguageFormatter.paraphraseLogicalOperators(): adding argument: " + ((String) args.get(args.size()-1)));
            f.read(f.cdr());
        }
        String IF = getKeyword("if", language);
        String THEN = getKeyword("then", language);
        String AND = getKeyword("and", language);
        String OR = getKeyword("or", language);
        String IFANDONLYIF = getKeyword("if and only if", language);
        String NOT = getKeyword("not", language);
        String FORALL = getKeyword("for all", language);
        String EXISTS = getKeyword("there exists", language);
        String EXIST = getKeyword("there exist", language);
        String NOTEXIST = getKeyword("there don't exist", language);
        String NOTEXISTS = getKeyword("there doesn't exist", language);
        String HOLDS = getKeyword("holds", language);
        String SOTHAT = getKeyword("so that", language);
        String SUCHTHAT = getKeyword("such that", language);
        if (!Formula.isNonEmptyString(SUCHTHAT)) {
            SUCHTHAT = SOTHAT;
        }

        StringBuilder sb = new StringBuilder();

        if ("=>".equalsIgnoreCase(pred)) {
            if (isNegMode) {
                sb.append(args.get(1)).append(' ').append(AND).append(' ').append("~{").append(args.get(0)).append('}');
            } else {
                sb.append("<ul><li>").append(IF).append(' ').append(args.get(0)).append(",<li>").append(THEN).append(' ').append(args.get(1)).append("</ul>");
            }
            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "c:return == " + ans );
             */
            return ans;
        }
        if ("and".equalsIgnoreCase(pred)) {
            if (isNegMode) {
                for (int i = 0; i < args.size(); i++) {
                    if (i != 0) {
                        sb.append(' ').append(OR).append(' ');
                    }
                    sb.append("~{ ");
                    sb.append(translateWord(termMap, (String) args.get(i), language));
                    sb.append(" }");
                }
            } else {
                for (int i = 0; i < args.size(); i++) {
                    if (i != 0) {
                        sb.append(' ').append(AND).append(' ');
                    }
                    sb.append(translateWord(termMap, (String) args.get(i), language));
                }
            }
            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "d:return == " + ans );
             */
            return ans;
        }
        if ("holds".equalsIgnoreCase(pred)) {

            for (int i = 0; i < args.size(); i++) {
                if (i != 0) {
                    if (isNegMode) {
                        sb.append(' ').append(NOT);
                    }
                    sb.append(' ').append(HOLDS).append(' ');
                }
                sb.append(translateWord(termMap, (String) args.get(i), language));
            }

            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "e:return == " + ans );
             */
            return ans;
        }
        if ("or".equalsIgnoreCase(pred)) {
            for (int i = 0; i < args.size(); i++) {
                if (i != 0) {
                    if (isNegMode) {
                        sb.append(' ').append(AND).append(' ');
                    } else {
                        sb.append(' ').append(OR).append(' ');
                    }
                }
                sb.append(translateWord(termMap, (String) args.get(i), language));
            }
            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "f:return == " + ans );
             */
            return ans;
        }
        if ("<=>".equalsIgnoreCase(pred)) {
            if (isNegMode) {
                sb.append(translateWord(termMap, (String) args.get(1), language));
                sb.append(' ').append(OR).append(' ');
                sb.append("~{ ");
                sb.append(translateWord(termMap, (String) args.get(0), language));
                sb.append(" }");
                sb.append(' ').append(OR).append(' ');
                sb.append(translateWord(termMap, (String) args.get(0), language));
                sb.append(' ').append(OR).append(' ');
                sb.append("~{ ");
                sb.append(translateWord(termMap, (String) args.get(1), language));
                sb.append(" }");
            } else {
                sb.append(translateWord(termMap, (String) args.get(0), language));
                sb.append(' ').append(IFANDONLYIF).append(' ');
                sb.append(translateWord(termMap, (String) args.get(1), language));
            }
            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "g:return == " + ans );
             */
            return ans;
        }
        if ("forall".equalsIgnoreCase(pred)) {
            if (isNegMode) {
                sb.append(' ').append(NOT).append(' ');
            }
            sb.append(FORALL).append(' ');
            if (((String) args.get(0)).indexOf(' ') == -1) {
                // If just one variable ...
                sb.append(translateWord(termMap, (String) args.get(0), language));
            } else {
                // If more than one variable ...
                sb.append(translateWord(termMap, formatList((String) args.get(0), language), language));
            }
            sb.append(' ');
            // sb.append(" "+HOLDS+": ");
            sb.append(translateWord(termMap, (String) args.get(1), language));
            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "h:return == " + ans );
             */
            return ans;
        }
        if ("exists".equalsIgnoreCase(pred)) {
            if (((String) args.get(0)).indexOf(' ') == -1) {

		// If just one variable ...
                // NS: The section immediately below seems to be just
                // wrong, so I've commented it out.
		/*
                 if (args.size() != 3) {
                 for (int i = args.size()-1; i >= 0; i--) {
                 sb.append(translateWord(termMap,(String) args.get(i),language));
                 sb.append(" ");
                 }
                 ans = sb.toString();
                 System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
                 printSpaces( depth );
                 System.out.println( "i:return == " + ans );
                 return ans;  // not the right english format
                 }
                 */
                if (isNegMode) {
                    sb.append(NOTEXISTS).append(' ');
                } else {
                    sb.append(EXISTS).append(' ');
                }
                sb.append(translateWord(termMap, (String) args.get(0), language));
            } else {

                // If more than one variable ...
                if (isNegMode) {
                    sb.append(NOTEXIST).append(' ');
                } else {
                    sb.append(EXIST).append(' ');
                }
                sb.append(translateWord(termMap, formatList((String) args.get(0), language), language));
            }
            sb.append(' ').append(SUCHTHAT).append(' ');
            sb.append(translateWord(termMap, (String) args.get(1), language));
            ans = sb.toString();
            /*
             System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
             printSpaces( depth );
             System.out.println( "j:return == " + ans );
             */
            return ans;
        }
        /*
         System.out.println( "INFO in LanguageFormatter.paraphraseLogicalOperator( " + depth + " ):" );
         printSpaces( depth );
         System.out.println( "l:return == \"\"" );
         */
        return "";
    }

    /**
     * ***************************************************************
     * Create a natural language paraphrase of a logical statement, where the
     * predicate is not a logical operate. Use a printf-like format string to
     * generate the paraphrase.
     *
     * @param stmt the statement to format
     * @param isNegMode whether the statement is negated, and therefore
     * requiring special formatting.
     * @return the paraphrased statement.
     */
    private static String paraphraseWithFormat(String stmt, boolean isNegMode, Map phraseMap,
            Map termMap, String language) {

        // System.out.println("INFO in LanguageFormatter.paraphraseWithFormat(): Statement: " + stmt);
        // System.out.println("neg mode: " + isNegMode);
        Formula f = new Formula();
        f.read(stmt);
        String pred = f.car();
        String strFormat = (String) phraseMap.get(pred);
        // System.out.println("str format: " + strFormat);
        int index;

        if (strFormat.contains("&%")) {                   // setup the term hyperlink
            strFormat = strFormat.replace("&%", "&%" + pred + '$');
        }
        if (isNegMode) {                                    // handle negation
            if (!strFormat.contains("%n")) {
                strFormat = getKeyword("not", language) + ' ' + strFormat;
            } else {
                if (!strFormat.contains("%n{")) {
                    strFormat = strFormat.replace("%n", getKeyword("not", language));
                } else {
                    int start = strFormat.indexOf("%n{") + 3;
                    int end = strFormat.indexOf('}', start);
                    strFormat = (strFormat.substring(0, start - 3)
                            + strFormat.substring(start, end)
                            + strFormat.substring(end + 1, strFormat.length()));
                }
            }
            // delete all the unused positive commands
            isNegMode = false;
            // strFormat = strFormat.replace("%p ","");
            // strFormat = strFormat.replaceAll(" %p\\{[\\w\\']+\\} "," ");
            // strFormat = strFormat.replaceAll("%p\\{[\\w\\']+\\} "," ");
            strFormat = strFormat.replaceAll(" %p\\{.+?\\} ", " ");
            strFormat = strFormat.replaceAll("%p\\{.+?\\} ", " ");
        } else {
            // delete all the unused negative commands          
            strFormat = strFormat.replace(" %n ", " ");
            strFormat = strFormat.replace("%n ", " ");
            // strFormat = strFormat.replaceAll(" %n\\{[\\w\\']+\\} "," ");
            // strFormat = strFormat.replaceAll("%n\\{[\\w\\']+\\} "," ");
            strFormat = strFormat.replaceAll(" %n\\{.+?\\} ", " ");
            strFormat = strFormat.replaceAll("%n\\{.+?\\} ", " ");
            if (strFormat.contains("%p{")) {
                int start = strFormat.indexOf("%p{") + 3;
                int end = strFormat.indexOf('}', start);
                strFormat = (strFormat.substring(0, start - 3)
                        + strFormat.substring(start, end)
                        + strFormat.substring(end + 1, strFormat.length()));
            }
        }

        strFormat = expandStar(f, strFormat, language);

        int num = 1;                                          // handle arguments
        String argPointer = '%' + (new Integer(num)).toString();
        while (strFormat.contains(argPointer)) {
            // System.out.println("INFO in LanguageFormatter.paraphraseWithFormat(): Statement: " + f.theFormula);
            // System.out.println("arg: " + f.getArgument(num));
            // System.out.println("num: " + num);
            // System.out.println("str: " + strFormat);
            strFormat = strFormat.replace(argPointer, nlStmtPara(f.getArgument(num), isNegMode, phraseMap, termMap, language, 1));
            num++;
            argPointer = '%' + (new Integer(num)).toString();
        }

        // System.out.println("str: " + strFormat);
        return strFormat;
    }

    /**
     * ***************************************************************
     * This method expands all "star" (asterisk) directives in the input format
     * string, and returns a new format string with individually numbered
     * argument pointers.
     *
     * @param f The Formula being paraphrased.
     *
     * @param strFormat The format string that contains the patterns and
     * directives for paraphrasing f.
     *
     * @param lang A two-character string indicating the language into which f
     * should be paraphrased.
     *
     * @return A format string with all relevant argument pointers expanded.
     */
    private static String expandStar(Formula f, String strFormat, String lang) {

        String result = strFormat;
        ArrayList problems = new ArrayList();
        try {
            int flen = f.listLength();
            if (Formula.isNonEmptyString(strFormat) && (flen > 1)) {
                int p1 = 0;
                int p2 = strFormat.indexOf("%*");
                if (p2 != -1) {
                    int slen = strFormat.length();
                    String lb = null;
                    String rb = null;
                    int lbi = -1;
                    int rbi = -1;
                    String ss = null;
                    String range = null;
                    String[] rangeArr = null;
                    String[] rangeArr2 = null;
                    String lowStr = null;
                    String highStr = null;
                    int low = -1;
                    int high = -1;
                    String delim = " ";
                    boolean isRange = false;
                    boolean[] argsToPrint = new boolean[flen];
                    int nArgsSet = -1;
                    StringBuilder sb = new StringBuilder();
                    while ((p1 < slen) && (p2 >= 0) && (p2 < slen)) {
                        sb.append(strFormat.substring(p1, p2));
                        p1 = (p2 + 2);
                        for (int k = 0; k < argsToPrint.length; k++) {
                            argsToPrint[k] = false;
                        }
                        lowStr = null;
                        highStr = null;
                        low = -1;
                        high = -1;
                        delim = " ";
                        nArgsSet = 0;
                        lb = null;
                        lbi = p1;
                        if (lbi < slen) {
                            lb = strFormat.substring(lbi, (lbi + 1));
                        }
                        while ((lb != null) && ("{".equals(lb) || "[".equals(lb))) {
                            rb = "]";
                            if ("{".equals(lb)) {
                                rb = "}";
                            }
                            rbi = strFormat.indexOf(rb, lbi);
                            if (rbi == -1) {
                                problems.add("Error in format \"" + strFormat + "\": missing \"" + rb + '"');
                                break;
                            }
                            p1 = (rbi + 1);
                            ss = strFormat.substring((lbi + 1), rbi);
                            if ("{".equals(lb)) {
                                range = ss.trim();
                                rangeArr = range.split(",");
                                // System.out.println( "INFO in LanguageFormatter.expandStar(): rangeArr == " + rangeArr );
                                for (String aRangeArr : rangeArr) {
                                    if (Formula.isNonEmptyString(aRangeArr)) {
                                        isRange = (aRangeArr.indexOf('-') != -1);
                                        rangeArr2 = aRangeArr.split("-");
                                        lowStr = rangeArr2[0].trim();
                                        try {
                                            low = Integer.parseInt(lowStr);
                                        } catch (Exception e1) {
                                            problems.add("Error in format \"" + strFormat + "\": bad value in \"" + ss + '"');
                                            low = 1;
                                        }
                                        // System.out.println( "INFO in LanguageFormatter.expandStar(): low == " + low );
                                        high = low;
                                        if (isRange) {
                                            if (rangeArr2.length == 2) {
                                                highStr = rangeArr2[1].trim();
                                                try {
                                                    high = Integer.parseInt(highStr);
                                                } catch (Exception e2) {
                                                    problems.add("Error in format \"" + strFormat + "\": bad value in \"" + ss + '"');
                                                    high = (flen - 1);
                                                }
                                            } else {
                                                high = (flen - 1);
                                            }
                                        }
                                        // System.out.println( "INFO in LanguageFormatter.expandStar(): high == " + high );
                                        for (int j = low; (j <= high) && (j < argsToPrint.length); j++) {
                                            argsToPrint[j] = true;
                                            nArgsSet++;
                                        }
                                    }
                                }
                            } else {
                                delim = ss;
                            }
                            lb = null;
                            lbi = p1;
                            if (lbi < slen) {
                                lb = strFormat.substring(lbi, (lbi + 1));
                            }
                        }
                        String AND = getKeyword("and", lang);
                        if (!Formula.isNonEmptyString(AND)) {
                            AND = "+";
                        }
                        int nAdded = 0;
                        boolean addAll = (nArgsSet == 0);
                        int nToAdd = (addAll ? (argsToPrint.length - 1) : nArgsSet);
                        for (int i = 1; i < argsToPrint.length; i++) {
                            if (addAll || (argsToPrint[i])) {
                                if (nAdded >= 1) {
                                    if (nToAdd == 2) {
                                        sb.append(' ').append(AND).append(' ');
                                    } else {
                                        sb.append(delim);
                                    }
                                    if ((nToAdd > 2) && ((nAdded + 1) == nToAdd)) {
                                        sb.append(AND).append(' ');
                                    }
                                }
                                sb.append('%').append(i);
                                nAdded++;
                            }
                        }
                        if (p1 < slen) {
                            p2 = strFormat.indexOf("%*", p1);
                            if (p2 == -1) {
                                sb.append(strFormat.substring(p1, slen));
                                break;
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        result = sb.toString();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!problems.isEmpty()) {
            String errStr = KBmanager.getMgr().getError();
            String str = null;
            if (errStr == null) {
                errStr = "";
            }
            for (Object problem : problems) {
                str = (String) problem;
                System.out.println("Error in LanguageFormatter.expandStar(): ");
                System.out.println("  " + str);
                errStr += ("\n<br/>" + str + "\n<br/>");
            }
            KBmanager.getMgr().setError(errStr);
        }
        return result;
    }

    /**
     * **************************************************************
     * Hyperlink terms in a natural language format string. This assumes that
     * terms to be hyperlinked are in the form &%termName$termString , where
     * termName is the name of the term to be browsed in the knowledge base and
     * termString is the text that should be displayed hyperlinked.
     *
     * @param href the anchor string up to the term= parameter, which this
     * method will fill in.
     * @param stmt the KIF statement that will be passed to nlStmtPara for
     * formatting.
     * @param phraseMap the set of NL formatting statements that will be passed
     * to nlStmtPara.
     * @param termMap the set of NL statements for terms that will be passed to
     * nlStmtPara.
     * @param language the natural language in which the paraphrase should be
     * generated.
     */
    public static String htmlParaphrase(String href, String stmt, Map phraseMap, Map termMap, String language) {

        int end;
        int start = -1;
        String nlFormat = nlStmtPara(stmt, false, phraseMap, termMap, language, 1);
        if (nlFormat != null) {
            while (nlFormat.contains("&%")) {

                start = nlFormat.indexOf("&%", start + 1);
                int word = nlFormat.indexOf('$', start);
                end = word == -1 ? start + 2 : word + 1;
                while (end < nlFormat.length() && Character.isJavaIdentifierPart(nlFormat.charAt(end))) {
                    end++;
                }
                nlFormat = word == -1 ? nlFormat.substring(0, start)
                        + "<a href=\""
                        + href
                        + "&term="
                        + nlFormat.substring(start + 2, end)
                        + "\">"
                        + nlFormat.substring(start + 1, end)
                        + "</a>"
                        + nlFormat.substring(end, nlFormat.length()) : nlFormat.substring(0, start)
                        + "<a href=\""
                        + href
                        + "&term="
                        + nlFormat.substring(start + 2, word)
                        + "\">"
                        + nlFormat.substring(word + 1, end)
                        + "</a>"
                        + nlFormat.substring(end, nlFormat.length());
            }
        } else {
            nlFormat = "";
        }
        /*
         if ( Formula.isNonEmptyString(nlFormat) ) {
         StringBuilder sb = new StringBuilder( nlFormat );
         sb.append( "." );
         char ch = sb.charAt( 0 );
         if ( Character.isLowerCase(ch) ) {
         sb.setCharAt( 0, Character.toUpperCase(ch) );
         }
         nlFormat = sb.toString();
         }
         */
        return nlFormat;
    }

    /**
     * **************************************************************
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static void main(String[] args) {

        readKeywordMap("C:\\Program Files\\Apache Software Foundation\\Tomcat 5.5\\KBs");
        HashMap phraseMap = new HashMap();
        phraseMap.put("foo", "%1 is %n{nicht} a &%foo of %2");
        HashMap termMap = new HashMap();
        System.out.println(htmlParaphrase("", "(=> (exists (?FOO ?BAR) (foo ?FOO ?BAR)) (bar ?BIZ ?BONG))", phraseMap, termMap, "en"));

    }
}
