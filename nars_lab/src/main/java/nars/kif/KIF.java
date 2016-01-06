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
import java.text.ParseException;
import java.util.*;

/**
 * *****************************************************************
 * A class designed to read a file in SUO-KIF format into memory. See
 * <http://suo.ieee.org/suo-kif.html> for a language specification. readFile()
 * and writeFile() and the only public methods.
 *
 * @author Adam Pease
 */
public class KIF {



    /**
     * A numeric constant denoting normal parse mode, in which syntax
     * constraints are enforced.
     */
    public static final int NORMAL_PARSE_MODE = 1;

    /**
     * A numeric constant denoting relaxed parse mode, in which fewer syntax
     * constraints are enforced than in NORMAL_PARSE_MODE.
     */
    public static final int RELAXED_PARSE_MODE = 2;

    private int parseMode = NORMAL_PARSE_MODE;
    private List<Formula> allFormulas;

    public KIF() {
    }

    public KIF(String filename) throws Exception {
        readFile(filename);

        allFormulas = new LinkedList();
        for (List<Formula> l : formulas.values()) {
            allFormulas.addAll(l);
        }
    }

    public List<Formula> getFormulas() {
        return allFormulas;
    }

    /**
     * @return int Returns an integer value denoting the current parse mode.
     */
    public int getParseMode() {
        return parseMode;
    }

    /**
     * Sets the current parse mode to the input value mode.
     *
     * @param mode An integer value denoting a parsing mode.
     *
     * @return void
     */
    public void setParseMode(int mode) {
        parseMode = mode;
    }

    /**
     * The set of all terms in the knowledge base. This is a set of Strings.
     */
    public TreeSet terms = new TreeSet();

    /**
     * A HashMap of ArrayLists of Formulas. @see KIF.createKey for key format.
     */
    public HashMap<String, List<Formula>> formulas = new HashMap();

    /**
     * A "raw" HashSet of unique Strings which are the formulas from the file
     * without any further processing, in the order which they appear in the
     * file.
     */
    public LinkedHashSet formulaSet = new LinkedHashSet();

    private String filename;

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    public String getFilename() {
//        return this.filename;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    private File file;
    private int totalLinesForComments = 0;

    /**
     * ***************************************************************
     * This routine sets up the StreamTokenizer_s so that it parses SUO-KIF. = <
     * > are treated as word characters, as are normal alphanumerics. ; is the
     * line comment character and " is the quote character.
     */
    public static void setupStreamTokenizer(StreamTokenizer_s st) {

        st.whitespaceChars(0, 32);
        st.ordinaryChars(33, 44);   // !"#$%&'()*+,
        st.wordChars(45, 46);       // -.
        st.ordinaryChar(47);       // /
        st.wordChars(48, 57);       // 0-9
        st.ordinaryChars(58, 59);   // :;
        st.wordChars(60, 64);       // <=>?@
        st.wordChars(65, 90);       // A-Z
        st.ordinaryChars(91, 94);   // [\]^
        st.wordChars(95, 95);       // _
        st.ordinaryChar(96);       // `
        st.wordChars(97, 122);      // a-z
        st.ordinaryChars(123, 127); // {|}~
        // st.parseNumbers();
        st.quoteChar('"');
        st.commentChar(';');
        st.eolIsSignificant(true);
    }

    /**
     * ***************************************************************
     */
    /*private void display(StreamTokenizer_s st,
     boolean inRule,
     boolean inAntecedent,
     boolean inConsequent,
     int argumentNum,
     int parenLevel,
     String key) {

     System.out.print (inRule);
     System.out.print ("\t");
     System.out.print (inAntecedent);
     System.out.print ("\t");
     System.out.print (inConsequent);
     System.out.print ("\t");
     System.out.print (st.ttype);
     System.out.print ("\t");
     System.out.print (argumentNum);
     System.out.print ("\t");
     System.out.print (parenLevel);
     System.out.print ("\t");
     System.out.print (st.sval);
     System.out.print ("\t");
     System.out.print (st.nval);
     System.out.print ("\t");
     System.out.print (st.toString());
     System.out.print ("\t");
     System.out.println (key);
     }
     */
    /**
     * ***************************************************************
     */
    private void parse(Reader r) {

        int mode = getParseMode();

        /*
         System.out.println("INFO in KIF.parse()");
         System.out.println("  filename == " + this.getFilename());
         System.out.println("  parseMode == "
         + ((mode == RELAXED_PARSE_MODE) ? "RELAXED_PARSE_MODE" : "NORMAL_PARSE_MODE"));
         */
        String key = null;
        ArrayList keySet;
        StringBuilder expression = new StringBuilder(40);
        StreamTokenizer_s st;
        int parenLevel;
        boolean inRule;
        int argumentNum;
        boolean inAntecedent;
        boolean inConsequent;
        int lastVal;
        int lineStart;
        boolean isEOL;
        String com;
        Formula f = new Formula();
        ArrayList list;
        TreeSet warningSet = new TreeSet();

        if (r == null) {
            /*System.err.println("No Input Reader Specified");
             System.out.println("EXIT KIF.parse(" + r + ")");*/
            return;
        }
        try {
            st = new StreamTokenizer_s(r);
            KIF.setupStreamTokenizer(st);
            parenLevel = 0;
            inRule = false;
            argumentNum = -1;
            inAntecedent = false;
            inConsequent = false;
            keySet = new ArrayList();
            lineStart = 0;
            isEOL = false;
            do {
                lastVal = st.ttype;
                st.nextToken();

                // check the situation when multiple KIF statements read as one
                // This relies on extra blank line to seperate KIF statements
                if (st.ttype == StreamTokenizer.TT_EOL) {
                    if (isEOL) { // two line seperators in a row, shows a new KIF statement is to start.
                        // check if a new statement has already been generated, otherwise report error
                        if (!keySet.isEmpty() || expression.length() > 0) {
                            //System.out.print("INFO in KIF.parse(): Parsing Error:");
                            //System.out.println(new Integer(lineStart + totalLinesForComments).toString());
                            throw new ParseException("Parsing error in " + filename + ": possible missing close parenthesis.", f.startLine);
                        }
                        continue;
                    } else {                                            // Found a first end of line character.
                        isEOL = true;                                 // Turn on flag, to watch for a second consecutive one.
                        continue;
                    }
                }
                if (isEOL) {
                    isEOL = false;                                    // Turn off isEOL if a non-space token encountered
                }
                //noinspection IfStatementWithTooManyBranches
                if (st.ttype == 40) {                                   // open paren
                    if (parenLevel == 0) {
                        lineStart = st.lineno();
                        f = new Formula();
                        f.startLine = st.lineno() + totalLinesForComments;
                        f.sourceFile = filename;
                    }
                    parenLevel++;
                    if (inRule && !inAntecedent && !inConsequent) {
                        inAntecedent = true;
                    } else {
                        if (inRule && inAntecedent && (parenLevel == 2)) {
                            inAntecedent = false;
                            inConsequent = true;
                        }
                    }
                    if ((parenLevel != 0) && (lastVal != 40) && (expression.length() > 0)) { // add back whitespace that ST removes
                        expression.append(' ');
                    }
                    expression.append('(');
                } else if (st.ttype == 41) {                                      // )  - close paren
                    parenLevel--;
                    expression.append(')');
                    if (parenLevel == 0) {                                    // The end of the statement...
                        f.theFormula = expression.toString().intern();
                        //if (KBmanager.getMgr().getPref("TPTP").equals("yes"))                       
                        //f.tptpParse(false,null);   // not a query
                        if (formulaSet.contains(f.theFormula)) {
                            String warning = "Duplicate formula at line " + f.startLine + ": " + expression;
                            // lineStart + totalLinesForComments + expression;
                            warningSet.add(warning);
                        }
                        // Check argument validity ONLY if we are in
                        // NORMAL_PARSE_MODE.
                        if (mode == NORMAL_PARSE_MODE) {
                            String validArgs = f.validArgs((file != null ? file.getName() : null),
                                    (file != null ? f.startLine : null));
                            if (validArgs == null || validArgs.isEmpty()) {
                                validArgs = f.badQuantification();
                            }
                            if (validArgs != null && !validArgs.isEmpty()) {
                                throw new ParseException("Parsing error in " + filename + ".\n Invalid number of arguments. " + validArgs, f.startLine);
                            }
                        }
                        // formulaList.add(expression.intern());

                        keySet.add(f.theFormula);           // Make the formula itself a key
                        f.endLine = st.lineno() + totalLinesForComments;
                        for (Object aKeySet : keySet) {             // Add the expression but ...
                            if (formulas.containsKey(aKeySet)) {
                                if (!formulaSet.contains(f.theFormula)) {  // don't add keys if formula is already present
                                    list = (ArrayList) formulas.get(aKeySet);
                                    if (!list.contains(f)) {
                                        list.add(f);
                                    }
                                }
                            } else {
                                list = new ArrayList();
                                list.add(f);
                                formulas.put((String) aKeySet, list);
                            }
                        }
                        formulaSet.add(f.theFormula);

                        inConsequent = false;
                        inRule = false;
                        argumentNum = -1;
                        lineStart = st.lineno() + 1;                            // start next statement from next line
                        expression.delete(0, expression.length());
                        keySet.clear();
                    } else if (parenLevel < 0) {
                        throw new ParseException("Parsing error in " + filename + ": Extra closing paranthesis found.", f.startLine);
                    }
                } else if (st.ttype == 34) {                                      // " - it's a string
                    if (lastVal != 40) // add back whitespace that ST removes
                    {
                        expression.append(' ');
                    }
                    expression.append('"');
                    com = st.sval;
                    totalLinesForComments += countChar(com, (char) 0X0A);
                    expression.append(com);
                    expression.append('"');
                } else if ((st.ttype == StreamTokenizer.TT_NUMBER)
                        || (st.sval != null && (Character.isDigit(st.sval.charAt(0))))) {                  // number
                    if (lastVal != 40) // add back whitespace that ST removes
                    {
                        expression.append(' ');
                    }
                    if (st.nval == 0) {
                        expression.append(st.sval);
                    } else {
                        expression.append(Double.toString(st.nval));
                    }
                    if (parenLevel < 2) // Don't care if parenLevel > 1
                    {
                        argumentNum += 1;                // RAP - added on 11/27/04 
                    }
                } else if (st.ttype == StreamTokenizer.TT_WORD) {                  // a token
                    if ((st.sval.compareTo("=>") == 0 || st.sval.compareTo("<=>") == 0) && parenLevel == 1) // RAP - added parenLevel clause on 11/27/04 to 
                    // prevent implications embedded in statements from being rules
                    {
                        inRule = true;
                    }
                    if (parenLevel < 2) // Don't care if parenLevel > 1
                    {
                        argumentNum += 1;
                    }
                    if (lastVal != 40) // add back whitespace that ST removes
                    {
                        expression.append(' ');
                    }
                    expression.append(st.sval);
                    if (expression.length() > 64000) {
                        //System.out.print("Error in KIF.parse(): Parsing error: Sentence Over 64000 characters.");
                        //System.out.println(new Integer(lineStart + totalLinesForComments).toString());
                        throw new ParseException("Parsing error in " + filename + ": Sentence Over 64000 characters.", f.startLine);
                    }
                    // Build the terms list and create special keys
                    // ONLY if we are in NORMAL_PARSE_MODE.
                    if ((mode == NORMAL_PARSE_MODE)
                            && (st.sval.charAt(0) != '?')
                            && (st.sval.charAt(0) != '@')) {   // Variables are not terms
                        terms.add(st.sval);                  // collect all terms
                        key = createKey(st.sval, inAntecedent, inConsequent, argumentNum, parenLevel);
                        keySet.add(key);                     // Collect all the keys until the end of
                    }                                        // the statement is reached.
                } else if ((mode == RELAXED_PARSE_MODE) && (st.ttype == 96)) {

                    // AB: 5/2007
                    // allow '`' in relaxed parse mode.
                    expression.append(' ');
                    expression.append('`');
                } else if (st.ttype != StreamTokenizer.TT_EOF) {
                    key = null;
                    // System.out.println( "st.ttype == " + st.ttype );
                    //System.out.print("Error in KIF.parse(): Parsing Error: Illegal character at line: ");
                    //System.out.println(new Integer(lineStart + totalLinesForComments).toString());
                    throw new ParseException("Parsing error in "
                            + filename
                            + ": Illegal character near line "
                            + f.startLine,
                            f.startLine);
                }
                // if (key != null)
                //    display(st,inRule,inAntecedent,inConsequent,argumentNum,parenLevel,key);
            } while (st.ttype != StreamTokenizer.TT_EOF);
            if (!keySet.isEmpty() || expression.length() > 0) {
                //System.out.println("Error in KIF.parse(): Parsing error: ");
                //System.out.println("Kif ends before parsing finishes.  Missing closing parenthesis.");
                throw new ParseException("Parsing error in "
                        + filename
                        + ": Missing closing paranthesis near line "
                        + f.startLine, f.startLine);
            }
        } catch (Exception ex) {
            warningSet.add("Error in KIF.parse(): " + ex.getMessage());
            System.err.println("Error in KIF.parse(): " + ex.getMessage());
            ex.printStackTrace();
        }

        if (!warningSet.isEmpty()) {
            Iterator it = warningSet.iterator();
            StringBuilder warnings = new StringBuilder();
            while (it.hasNext()) {
                String w = (String) it.next();
                System.err.println((w.startsWith("Error") ? w : "Warning in KIF.parse(): " + w));
                warnings.append("\n<br/>").append(w).append("<br/>\n");
            }
            System.err.println(warnings);
        }

    }

    /**
     * ***************************************************************
     * This routine creates a key that relates a token in a logical statement to
     * the entire statement. It prepends to the token a string indicating its
     * position in the statement. The key is of the form type-[num]-term, where
     * [num] is only present when the type is "arg", meaning a statement in
     * which the term is nested only within one pair of parentheses. The other
     * possible types are "ant" for rule antecedent, "cons" for rule consequent,
     * and "stmt" for cases where the term is nested inside multiple levels of
     * parentheses. An example key would be arg-0-instance for a appearance of
     * the term "instance" in a statement in the predicate position.
     *
     * @param sval - the token such as "instance", "Human" etc.
     * @param inAntecedent - whether the term appears in the antecedent of a
     * rule.
     * @param inConsequent - whether the term appears in the consequent of a
     * rule.
     * @param argumentNum - the argument position in which the term appears. The
     * predicate position is argument 0. The first argument is 1 etc.
     * @param parenLevel - if the paren level is > 1 then the term appears
     * nested in a statement and the argument number is ignored.
     */
    private String createKey(String sval,
            boolean inAntecedent,
            boolean inConsequent,
            int argumentNum,
            int parenLevel) {

        if (sval == null) {
            sval = "null";
        }
        String key = "";
        if (inAntecedent) {
            key = key + "ant-";
            key = key + sval;
        }

        if (inConsequent) {
            key = key + "cons-";
            key = key + sval;
        }

        if (!inAntecedent && !inConsequent && (parenLevel == 1)) {
            key = key + "arg-";
            key = key + argumentNum;
            key = key + '-';
            key = key + sval;
        }
        if (!inAntecedent && !inConsequent && (parenLevel > 1)) {
            key = key + "stmt-";
            key = key + sval;
        }
        return (key);
    }

    /**
     * ***************************************************************
     * Count the number of appearences of a certain character in a string.
     *
     * @param str - the string to be tested.
     * @param c - the character to be counted.
     */
    private int countChar(String str, char c) {

        int len = 0;
        char[] cArray = str.toCharArray();
        for (char aCArray : cArray) {
            if (aCArray == c) {
                len++;
            }
        }
        return len;
    }

    /**
     * ***************************************************************
     * Read a KIF file.
     *
     * @param fname - the full pathname of the file.
     */
    public void readFile(String fname) throws Exception {

        FileReader fr = null;
        Exception exThr = null;
        try {
            file = new File(fname);
            filename = file.getCanonicalPath();
            fr = new FileReader(file);
            parse(fr);
        } catch (Exception ex) {
            exThr = ex;
            String er = ex.getMessage() + ((ex instanceof ParseException)
                    ? " at line " + ((ParseException) ex).getErrorOffset()
                    : "");
            System.err.println("ERROR in KIF.readFile(\"" + fname + "\") " + er);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception ex2) {
                }
            }
        }
        if (exThr != null) {
            throw exThr;
        }
    }

// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Write a KIF file.
//     * @param fname - the name of the file to write, including full path.
//     */
//    public void writeFile(String fname) {
//
//        System.out.println("ENTER KIF.writeFile(\"" + fname + "\")");
//        System.out.println("  number of formulas == " + formulaSet.size());
//
//        FileWriter fr = null;
//        PrintWriter pr = null;
//        Iterator it;
//        ArrayList formulaArray;
//        try {
//            fr = new FileWriter(fname);
//            pr = new PrintWriter(fr);
//
//            it = formulaSet.iterator();
//            while (it.hasNext())
//                pr.println((String) it.next());
//        }
//        catch (Exception ex) {
//            System.out.println("ERROR in KIF.writeFile(\"" + fname + "\")");
//            System.out.println("  " + ex.getMessage());
//            ex.printStackTrace();
//        }
//        finally {
//            try {
//                if (pr != null) {
//                    pr.close();
//                }
//                if (fr != null) {
//                    fr.close();
//                }
//            }
//            catch (Exception ex2) {
//            }
//        }
//        System.out.println("EXIT KIF.writeFile(\"" + fname + "\")");
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
// --Commented out by Inspection START (8/15/14 2:38 AM):
//    /** ***************************************************************
//     * Parse a single formula.
//     */
//    public String parseStatement(String formula) {
//
//        StringReader r = new StringReader(formula);
//        try {
//            parse(r);
//        }
//        catch (Exception e) {
//            return e.getMessage();
//        }
//        return null;
//    }
// --Commented out by Inspection STOP (8/15/14 2:38 AM)
    /**
     * ***************************************************************
     * Test method for this class. Currently, it writes the TPTP output to a
     * file.
     */
    public static void main(String[] args) throws IOException {

        Iterator it;
        KIF kifp = new KIF();
        Formula f;
        String form;
        ArrayList list;
        int axiomCount = 0;
        File toFile;
        FileWriter fw;
        PrintWriter pw;

        try {
            System.out.println("Loading from " + args[0]);
            kifp.readFile(args[0]);
        } catch (Exception e1) {
            String msg = e1.getMessage();
            if (e1 instanceof ParseException) {
                msg += (" in statement starting at line "
                        + ((ParseException) e1).getErrorOffset());
            }
            System.out.println(msg);
        }
        /*
         it = kifp.formulaSet.iterator();
         while (it.hasNext()) {
         form = (String) it.next();
         System.out.println (form);
         }
         */
        System.out.println("");

        fw = null;
        pw = null;
        File outfile = new File(args[0] + ".tptp");

        try {
            fw = new FileWriter(outfile);
            pw = new PrintWriter(fw);

            it = kifp.formulaSet.iterator();
            while (it.hasNext()) {
                axiomCount++;
                form = (String) it.next();
                form = Formula.tptpParseSUOKIFString(form);
                form = "fof(axiom" + axiomCount + ",axiom,(" + form + ")).";
                if (form.indexOf('"') < 0 && form.indexOf('\'') < 0) {
                    pw.println(form + '\n');
                }
            }
        } catch (Exception ex) {
            System.out.println("Error writing " + outfile.getCanonicalPath() + ": " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception e3) {
            }
        }
    }
}
