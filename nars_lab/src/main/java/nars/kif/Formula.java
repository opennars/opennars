package nars.kif;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
/**
 * Handle operations on an individual formula. This includes formatting for
 * presentation as well as pre-processing for sending to the logic engine.
 */
public class Formula implements Comparable {

    private static final boolean DEBUG = false;

    /**
     * The source file in which the formula appears.
     */
    public String sourceFile;
    /**
     * The line in the file on which the formula starts.
     */
    public int startLine;
    /**
     * The line in the file on which the formula ends.
     */
    public int endLine;
    /**
     * The length of the file in bytes at the position immediately after the end
     * of the formula. This value is used only for formulas entered via
     * KB.tell(). In general, you should not count on it being set to a value
     * other than -1L.
     */
    public long endFilePosition = -1L;
    /**
     * The formula.
     */
    public String theFormula;

    /**
     * A list of TPTP formulas (Strings) that together constitute the
     * translation of theFormula. This member is a List, because predicate
     * variable instantiation and row variable expansion might cause theFormula
     * to expand to several TPTP formulas.
     */
    private ArrayList theTptpFormulas = null;

    /**
     * Returns an ArrayList of the TPTP formulas (Strings) that together
     * constitute the TPTP translation of theFormula.
     *
     * @return An ArrayList of Strings, or an empty ArrayList if no translations
     * have been created or entered.
     */
    public ArrayList getTheTptpFormulas() {
        if (theTptpFormulas == null) {
            theTptpFormulas = new ArrayList();
        }
        return theTptpFormulas;
    }

    /**
     * Clears theTptpFormulas if the ArrayList exists, else does nothing.
     *
     * @return void
     */
    public void clearTheTptpFormulas() {
        if (theTptpFormulas != null) {
            theTptpFormulas.clear();
        }
    }

    /**
     * A list of clausal (resolution) forms generated from this Formula.
     */
    private ArrayList theClausalForm = null;

    /**
     * Returns a List of the clauses that together constitute the resolution
     * form of this Formula. The list could be empty if the clausal form has not
     * yet been computed.
     *
     * @return ArrayList
     */
    public ArrayList getTheClausalForm() {
        try {
            if (theClausalForm == null) {
                if (isNonEmptyString(theFormula)) {
                    theClausalForm = toNegAndPosLitsWithRenameInfo();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return theClausalForm;
    }

    /**
     * Computes the clauses that together constitute the resolution form of this
     * Formula. The resulting clauses are stored in a List accessed via
     * this.getTheClausalForm().
     *
     * @return void
     */
    public void computeTheClausalForm() {
        getTheClausalForm();
    }

    /**
     * This method clears the list of clauses that together constitute the
     * resolution form of this Formula, and can be used in preparation for
     * recomputing the clauses.
     *
     * @return void
     */
    public void clearTheClausalForm() {
        if (theClausalForm != null) {
            theClausalForm.clear();
        }
        theClausalForm = null;
    }

    /**
     * Returns a List of List objects. Each such object contains, in turn, a
     * pair of List objects. In List object in a pair contains Formula objects.
     * The Formula objects contained in the first List object (0) of a pair
     * represent negative literals (antecedent conjuncts). The Formula objects
     * contained in the second List object (1) of a pair represent positive
     * literals (consequent conjuncts). Taken together, all of the clauses
     * constitute the resolution form of this Formula.
     *
     * @return A List of Lists.
     */
    public ArrayList getClauses() {
        ArrayList clausesWithVarMap = getTheClausalForm();
        if ((clausesWithVarMap == null) || clausesWithVarMap.isEmpty()) {
            return null;
        }
        return (ArrayList) clausesWithVarMap.get(0);
    }

    /**
     * Returns a map of the variable renames that occurred during the
     * translation of this Formula into the clausal (resolution) form accessible
     * via this.getClauses().
     *
     * @return A Map of String (SUO-KIF variable) key-value pairs.
     */
    public HashMap getVarMap() {
        ArrayList clausesWithVarMap = getTheClausalForm();
        if ((clausesWithVarMap == null) || (clausesWithVarMap.size() < 3)) {
            return null;
        }
        return (HashMap) clausesWithVarMap.get(2);
    }

    /**
     * Returns the variable in this Formula that corresponds to the clausal form
     * variable passed as input.
     *
     * @return A SUO-KIF variable (String), which may be just the input
     * variable.
     */
    public String getOriginalVar(String var) {
        Map varmap = getVarMap();
        if (varmap == null) {
            return var;
        }
        return getOriginalVar(var, varmap);
    }

    /**
     * ***************************************************************
     * For any given formula, stop generating new pred var instantiations and
     * row var expansions if this threshold value has been exceeded. The default
     * value is 2000.
     */
    private static final int AXIOM_EXPANSION_LIMIT = 2000;

    /**
     * ***************************************************************
     * This constant indicates the maximum predicate arity supported by the
     * current implementation of Sigma.
     */
    protected static final int MAX_PREDICATE_ARITY = 7;

    /**
     * ***************************************************************
     * Read a String into the variable 'theFormula'.
     */
    public void read(String s) {
        theFormula = s;
    }

    /**
     * ***************************************************************
     * Copy the Formula.
     */
    private Formula copy() {

        Formula result = new Formula();
        if (sourceFile != null) {
            result.sourceFile = sourceFile.intern();
        }
        result.startLine = startLine;
        result.endLine = endLine;
        if (theFormula != null) {
            result.theFormula = theFormula.intern();
        }
        return result;
    }

    /**
     * ***************************************************************
     * Implement the Comparable interface by defining the compareTo method.
     * Formulas are equal if their formula strings are equal.
     */
    @Override
    public int compareTo(Object f) throws ClassCastException {
        if (!"com.articulate.sigma.Formula".equalsIgnoreCase(f.getClass().getName())) {
            throw new ClassCastException("Error in Formula.compareTo(): Class cast exception for argument of class: " + f.getClass().getName());
        }
        return theFormula.compareTo(((Formula) f).theFormula);
    }

    /**
     * ***************************************************************
     * Return the LISP 'car' of the formula - the first element of the list.
     * Note that this operation has no side effect on the Formula.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public String car() {
        try {
            //System.out.println("INFO in formula.car(): theFormula: " + theFormula);
            if (!listP()) {
                return null;
            }
            //System.out.println("INFO in Formula.car: theformula: " + theFormula);
            List quoteChars = Arrays.asList('"', '\'');
            int i = 0;
            while (theFormula.charAt(i) != '(') {
                i++;
            }
            i++;
            while (Character.isWhitespace(theFormula.charAt(i))) {
                i++;
            }
            int start = i;
            if (theFormula.charAt(i) == '(') {
                boolean insideQuote = false;
                char quoteCharInForce = '0';
                int level = 0;
                i++;

                while (insideQuote || theFormula.charAt(i) != ')' || level > 0) {

                    // if (DEBUG) { System.out.print(theFormula.charAt(i)); }
                    if (quoteChars.contains(theFormula.charAt(i))) {
                        if (theFormula.charAt(i - 1) != '\\') {
                            if (quoteCharInForce == '0') {
                                quoteCharInForce = theFormula.charAt(i);
                                insideQuote = true;
                                /*
                                 if (DEBUG) {
                                 System.out.println("|insideQuote == " + insideQuote + "|");
                                 }
                                 */
                            } else if (quoteCharInForce == theFormula.charAt(i)) {
                                quoteCharInForce = '0';
                                insideQuote = false;
                                /*
                                 if (DEBUG) {
                                 System.out.println("|insideQuote == " + insideQuote + "|");
                                 }
                                 */
                            }
                        }
                    }
                    if (!insideQuote) {
                        if (theFormula.charAt(i) == ')') {
                            level--;
                        }
                        if (theFormula.charAt(i) == '(') {
                            level++;
                        }
                    }
                    i++;
                }

                // if (DEBUG) { System.out.println(); }
                i++;
            } else {
                if (quoteChars.contains(theFormula.charAt(i))) {
                    char quoteChar = theFormula.charAt(i);
                    i++;
                    while (((theFormula.charAt(i) != quoteChar
                            || (theFormula.charAt(i) == quoteChar && theFormula.charAt(i - 1) == '\\'))
                            && i < theFormula.length() - 1)) {
                        i++;
                    }
                    i++;
                } else {
                    while (!Character.isWhitespace(theFormula.charAt(i)) && i < theFormula.length() - 1) {
                        i++;
                    }
                }
            }
            return theFormula.substring(start, i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println("INFO in formula.car() end: theFormula: " + theFormula.substring(start,i));
        return null;
    }

    /**
     * ***************************************************************
     * Return the LISP 'cdr' of the formula - the rest of a list minus its first
     * element. Note that this operation has no side effect on the Formula.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public String cdr() {

        //System.out.println("INFO in formula.cdr(): theFormula: " + theFormula);
        try {
            if (!listP()) {
                return null;
            }
            List quoteChars = Arrays.asList('"', '\'');
            int i = 0;
            while (theFormula.charAt(i) != '(') {
                i++;
            }
            i++;
            while (Character.isWhitespace(theFormula.charAt(i))) {
                i++;
            }
            if (theFormula.charAt(i) == '(') {
                boolean insideQuote = false;
                char quoteCharInForce = '0';
                int level = 0;
                i++;

                // if (DEBUG) { System.out.println(); }
                while (insideQuote || theFormula.charAt(i) != ')' || level > 0) {

                    // if (DEBUG) { System.out.print(theFormula.charAt(i)); }
                    if (quoteChars.contains(theFormula.charAt(i))) {
                        if (theFormula.charAt(i - 1) != '\\') {
                            if (quoteCharInForce == '0') {
                                quoteCharInForce = theFormula.charAt(i);
                                insideQuote = true;
                                /*
                                 if (DEBUG) {
                                 System.out.println("|insideQuote == " + insideQuote + "|");
                                 }
                                 */
                            } else if (quoteCharInForce == theFormula.charAt(i)) {
                                quoteCharInForce = '0';
                                insideQuote = false;
                                /*
                                 if (DEBUG) {
                                 System.out.println("|insideQuote == " + insideQuote + "|");
                                 }
                                 */
                            }
                        }
                    }
                    if (!insideQuote) {
                        if (theFormula.charAt(i) == ')') {
                            level--;
                        }
                        if (theFormula.charAt(i) == '(') {
                            level++;
                        }
                    }
                    i++;
                }

                // if (DEBUG) { System.out.println(); }
                i++;
            } else {
                if (theFormula.charAt(i) == '"' || theFormula.charAt(i) == '\'') {
                    char quoteChar = theFormula.charAt(i);
                    i++;
                    while (((theFormula.charAt(i) != quoteChar
                            || (theFormula.charAt(i) == quoteChar && theFormula.charAt(i - 1) == '\\'))
                            && i < theFormula.length() - 1)) {
                        i++;
                    }
                    i++;
                } else {
                    while (!Character.isWhitespace(theFormula.charAt(i)) && i < theFormula.length() - 1) {
                        i++;
                    }
                }
            }
            while ((i < theFormula.length()) && Character.isWhitespace(theFormula.charAt(i))) {
                i++;
            }
            int end = theFormula.lastIndexOf(')');
            return '(' + theFormula.substring(i, end) + ')';
        } catch (Exception ex) {
            System.out.println("\nError in Formula.cdr(" + theFormula + "): " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * ***************************************************************
     * Returns a new Formula which is the result of 'consing' a String into this
     * Formula, similar to the LISP procedure of the same name. This procedure
     * is a little bit of a kluge, since this Formula is treated simply as a
     * LISP object (presumably, a LISP list), and could be degenerate or
     * malformed as a Formula.
     *
     * Note that this operation has no side effect on the original Formula.
     *
     * @param obj The String object that will become the 'car' (or head) of the
     * resulting Formula (list).
     *
     * @return a new Formula, or the original Formula if the cons fails.
     */
    private Formula cons(String obj) {
        Formula ans = this;
        try {
            String fStr = theFormula;
            if (isNonEmptyString(obj) && isNonEmptyString(fStr)) {
                String theNewFormula = null;
                if (listP()) {
                    theNewFormula = empty() ? '(' + obj + ')' : '('
                            + obj
                            + ' '
                            + fStr.substring(1, (fStr.length() - 1))
                            + ')';
                } else {
                    // This should never happen during clausification, but
                    // we include it to make this procedure behave
                    // (almost) like its LISP namesake.
                    theNewFormula = ('(' + obj + " . " + fStr + ')');
                }
                if (theNewFormula != null) {
                    ans = new Formula();
                    ans.read(theNewFormula);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Returns the LISP 'cdr' of the formula as a new Formula, if possible, else
     * returns null.
     *
     * Note that this operation has no side effect on the Formula.
     *
     * @return a Formula, or null.
     */
    public Formula cdrAsFormula() {
        String thisCdr = cdr();
        if (listP(thisCdr)) {
            Formula f = new Formula();
            f.read(thisCdr);
            return f;
        }
        return null;
    }

    /**
     * ***************************************************************
     * Returns the LISP 'cadr' (the second list element) of the formula.
     *
     * Note that this operation has no side effect on the Formula.
     *
     * @return a String, or the empty string if the is no cadr.
     *
     */
    public String cadr() {
        return getArgument(1);
    }

    /**
     * ***************************************************************
     * Returns the LISP 'cddr' of the formula - the rest of the rest, or the
     * list minus its first two elements.
     *
     * Note that this operation has no side effect on the Formula.
     *
     * @return a String, or null.
     *
     */
    public String cddr() {
        Formula fCdr = cdrAsFormula();
        if (fCdr != null) {
            return fCdr.cdr();
        }
        return null;
    }

    /**
     * ***************************************************************
     * Returns the LISP 'cddr' of the formula as a new Formula, if possible,
     * else returns null.
     *
     * Note that this operation has no side effect on the Formula.
     *
     * @return a Formula, or null.
     */
    public Formula cddrAsFormula() {
        String thisCddr = cddr();
        if (listP(thisCddr)) {
            Formula f = new Formula();
            f.read(thisCddr);
            return f;
        }
        return null;
    }

    /**
     * ***************************************************************
     * Returns the LISP 'caddr' of the formula, which is the third list element
     * of the formula.
     *
     * Note that this operation has no side effect on the Formula.
     *
     * @return a String, or the empty string if there is no caddr.
     *
     */
    public String caddr() {
        return getArgument(2);
    }

    /**
     * ***************************************************************
     * Test whether the String is a LISP atom.
     */
    public static boolean atom(String s) {

        boolean ans = false;
        if (isNonEmptyString(s)) {
            String str = s.trim();
            ans = str.length() > 0 && str.charAt(0) == '\"' && str.length() > 0 && str.charAt(str.length() - 1) == '\"' || (str.indexOf(')') == -1)
                    && (str.indexOf('\n') == -1)
                    && (str.indexOf(' ') == -1)
                    && (str.indexOf('\t') == -1);
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Test whether the Formula is a LISP atom.
     */
    public boolean atom() {

        return Formula.atom(theFormula);
    }

    /**
     * ***************************************************************
     * Test whether the Formula is an empty list.
     */
    public boolean empty() {

        return Formula.empty(theFormula);
    }

    /**
     * ***************************************************************
     * Test whether the String is an empty formula.
     */
    public static boolean empty(String s) {
        return (listP(s) && s.matches("\\(\\s*\\)"));
    }

    /**
     * ***************************************************************
     * Test whether the Formula is a list.
     */
    public boolean listP() {

        return Formula.listP(theFormula);
    }

    /**
     * ***************************************************************
     * Test whether the String is a list.
     */
    private static boolean listP(String s) {

        boolean ans = false;
        if (isNonEmptyString(s)) {
            String str = s.trim();
            ans = (str.length() > 0 && str.charAt(0) == '(' && str.length() > 0 && str.charAt(str.length() - 1) == ')');
        }
        return ans;
    }

    /**
     * ***************************************************************
     * @see #validArgs() validArgs below for documentation
     */
    private String validArgsRecurse(Formula f, String filename, Integer lineNo) {

        //System.out.println("INFO in Formula.validArgsRecurse(): Formula: " + f.theFormula);
        if (f.theFormula != null && f.theFormula.isEmpty() || !f.listP() || f.atom() || f.empty()) {
            return "";
        }
        String pred = f.car();
        String rest = f.cdr();
        Formula restF = new Formula();
        restF.read(rest);
        int argCount = 0;
        while (!restF.empty()) {
            argCount++;
            String arg = restF.car();
            Formula argF = new Formula();
            argF.read(arg);
            String result = validArgsRecurse(argF, filename, lineNo);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            restF.theFormula = restF.cdr();
        }
        //noinspection IfStatementWithTooManyBranches
        if ("and".equals(pred) || "or".equals(pred)) {
            if (argCount < 2) {
                return "Too few arguments for 'and' or 'or' in formula: \n" + f + '\n';
            }
        } else if ("forall".equals(pred) || "exists".equals(pred)) {
            if (argCount != 2) {
                return "Wrong number of arguments for 'exists' or 'forall' in formula: \n" + f + '\n';
            } else {
                Formula quantF = new Formula();
                quantF.read(rest);
                if (!listP(quantF.car())) {
                    return "No parenthesized variable list for 'exists' or 'forall' in formula: \n" + f + '\n';
                }
            }
        } else if ("<=>".equals(pred) || "=>".equals(pred)) {
            if (argCount != 2) {
                return "Wrong number of arguments for '<=>' or '=>' in formula: \n" + f + '\n';
            }
        } else if ("equals".equals(pred)) {
            if (argCount != 2) {
                return "Wrong number of arguments for 'equals' in formula: \n" + f + '\n';
            }
        } else if (// !(isVariable(pred)) 
                // && 
                ("yes".equalsIgnoreCase(KBmanager.getMgr().getPref("holdsPrefix"))
                && (argCount > (MAX_PREDICATE_ARITY + 1)))
                || (!"yes".equalsIgnoreCase(KBmanager.getMgr().getPref("holdsPrefix"))
                && (argCount > MAX_PREDICATE_ARITY))) {
            String location = "";
            if ((filename != null) && (lineNo != null)) {
                location = (" near line " + lineNo + " in " + filename);
            }
            KBmanager.getMgr().setError(KBmanager.getMgr().getError()
                    + "\n<br/>Maybe too many arguments"
                    + location + ": "
                    + f
                    + "\n<br/>");
        }
        return "";
    }

    /**
     * ***************************************************************
     * Test whether the Formula uses logical operators and predicates with the
     * correct number of arguments. "equals", "<=>", and "=>" are strictly
     * binary. "or", and "and" are binary or greater. "not" is unary. "forall"
     * and "exists" are unary with an argument list. Warn if we encounter a
     * formula that has more arguments than MAX_PREDICATE_ARITY.
     *
     * @param filename If not null, denotes the name of the file being parsed.
     *
     * @param lineNo If not null, indicates the location of the expression
     * (formula) being parsed in the file being read.
     *
     * @return an empty String if there are no problems or an error message if
     * there are.
     */
    public String validArgs(String filename, Integer lineNo) {

        if (theFormula == null || theFormula.isEmpty()) {
            return "";
        }
        Formula f = new Formula();
        f.read(theFormula);
        String result = validArgsRecurse(f, filename, lineNo);
        //System.out.println("INFO in Formula.validArgs(): result: " + result);
        return result;
    }

    /**
     * ***************************************************************
     * Test whether the Formula uses logical operators and predicates with the
     * correct number of arguments. "equals", "<=>", and "=>" are strictly
     * binary. "or", and "and" are binary or greater. "not" is unary. "forall"
     * and "exists" are unary with an argument list. Warn if we encounter a
     * formula that has more arguments than MAX_PREDICATE_ARITY.
     *
     * @return an empty String if there are no problems or an error message if
     * there are.
     */
    public String validArgs() {
        return validArgs(null, null);
    }

    /**
     * ***************************************************************
     * Not yet implemented! Test whether the Formula has variables that are not
     * properly quantified. The case tested for is whether a quantified variable
     * in the antecedent appears in the consequent or vice versa.
     *
     * @return an empty String if there are no problems or an error message if
     * there are.
     */
    public String badQuantification() {
        return "";
    }

    /**
     * ***************************************************************
     * Parse a String into an ArrayList of Formulas. The String must be a
     * LISP-style list.
     *
     * @return an ArrayList of Formulas
     */
    private ArrayList parseList(String s) {

        //System.out.println("INFO in Formula.parseList(): s " + s);
        ArrayList result = new ArrayList();
        Formula f = new Formula();
        f.read('(' + s + ')');
        if (f.empty()) {
            return result;
        }
        while (!f.empty()) {
            //System.out.println("INFO in Formula.parseList(): f " + f.theFormula);
            String car = f.car();
            f.read(f.cdr());
            Formula newForm = new Formula();
            newForm.read(car);
            result.add(newForm);
        }
        return result;
    }

    /**
     * ***************************************************************
     * Compare two lists of formulas, testing whether they are equal, without
     * regard to order. (B A C) will be equal to (C B A). The method iterates
     * through one list, trying to find a match in the other and removing it if
     * a match is found. If the lists are equal, the second list should be empty
     * once the iteration is complete. Note that the formulas being compared
     * must be lists, not atoms, and not a set of formulas unenclosed by
     * parentheses. So, "(A B C)" and "(A)" are valid, but "A" is not, nor is "A
     * B C".
     */
    private boolean compareFormulaSets(String s) {
        // an ArrayList of Formulas
        ArrayList thisList = parseList(theFormula.substring(1, theFormula.length() - 1));
        ArrayList sList = parseList(s.substring(1, s.length() - 1));
        if (thisList.size() != sList.size()) {
            return false;
        }

        for (Object aThisList : thisList) {
            for (int j = 0; j < sList.size(); j++) {
                if (((Formula) aThisList).logicallyEquals(((Formula) sList.get(j)).theFormula)) {
                    // System.out.println("INFO in Formula.compareFormulaSets(): " + 
                    //       ((Formula) thisList.get(i)).toString() + " equal to " +
                    //       ((Formula) sList.get(j)).theFormula);
                    sList.remove(j);
                    j = sList.size();
                }
            }
        }
        return sList.isEmpty();
    }

    /**
     * ***************************************************************
     * Test if the contents of the formula are equal to the argument at a deeper
     * level than a simple string equals. The only logical manipulation is to
     * treat conjunctions and disjunctions as unordered bags of clauses. So (and
     * A B C) will be logicallyEqual(s) for example, to (and B A C). Note that
     * this is a fairly time-consuming operation and should not generally be
     * used for comparing large sets of formulas.
     */
    public boolean logicallyEquals(String s) {

        if (equals(s)) {
            return true;
        }
        if (Formula.atom(s) && s.compareTo(theFormula) != 0) {
            return false;
        }

        Formula form = new Formula();
        form.read(theFormula);
        Formula sform = new Formula();
        sform.read(s);

        if ("and".equals(form.car().intern()) || "or".equals(form.car().intern())) {
            if (sform.car().intern() == null ? sform.car().intern() != null : !sform.car().intern().equals(sform.car().intern())) {
                return false;
            }
            form.read(form.cdr());
            sform.read(sform.cdr());
            return form.compareFormulaSets(sform.theFormula);
        } else {
            Formula newForm = new Formula();
            newForm.read(form.car());
            Formula newSform = new Formula();
            newSform.read(sform.cdr());
            return newForm.logicallyEquals(sform.car())
                    && newSform.logicallyEquals(form.cdr());
        }
    }

    /**
     * ***************************************************************
     * Test if the contents of the formula are equal to the String argument.
     * Normalize all variables.
     */
    public boolean equals(String s) {

        String f = theFormula;
        Formula form = new Formula();
        Formula sform = new Formula();

        form.theFormula = f;
        s = normalizeVariables(s).intern();
        sform.read(s);
        s = sform.toString().trim().intern();

        form.theFormula = normalizeVariables(theFormula);
        f = form.toString().trim().intern();
        // System.out.println("INFO in Formula.equals(): Comparing " + s + " to " + f);
        return (f == null ? s == null : f.equals(s));
    }

    /**
     * ***************************************************************
     * Test if the contents of the formula are equal to the argument.
     */
    public boolean deepEquals(Formula f) {

        return (f.theFormula.intern() == null ? theFormula.intern() == null : f.theFormula.intern().equals(theFormula.intern()))
                && (f.sourceFile.intern() == null ? sourceFile.intern() == null : f.sourceFile.intern().equals(sourceFile.intern()));
    }

    /**
     * ***************************************************************
     * Return the numbered argument of the given formula. The first element of a
     * formula (i.e. the predicate position) is number 0. Returns the empty
     * string if there is no such argument position.
     */
    public String getArgument(int argnum) {

        String ans = "";
        Formula form = new Formula();
        form.read(theFormula);
        for (int i = 0; form.listP(); i++) {
            ans = form.car();
            if (i == argnum) {
                break;
            }
            form.read(form.cdr());
        }
        if (ans == null) {
            ans = "";
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Returns a non-negative int value indicating the top-level list length of
     * this Formula if it is a proper listP(), else returns -1. One caveat: This
     * method assumes that neither null nor the empty string are legitimate list
     * members in a wff. The return value is likely to be wrong if this
     * assumption is mistaken.
     *
     * @return A non-negative int, or -1.
     */
    public int listLength() {
        int ans = -1;
        if (listP()) {
            int idx = 0;
            while (isNonEmptyString(getArgument(idx))) {
                ans = ++idx;
            }
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Return all the arguments in a simple formula as a list, starting at the
     * given argument. If formula is complex (i.e. an argument is a function or
     * sentence), then return null. If the starting argument is greater than the
     * number of arguments, also return null.
     */
    public ArrayList<String> argumentsToArrayList(int start) {

        if (theFormula.indexOf('(', 1) != -1) {
            return null;
        }
        int index = start;
        ArrayList<String> result = new ArrayList();
        String arg = getArgument(index);
        while (arg != null && !arg.isEmpty() && !arg.isEmpty()) {
            result.add(arg.intern());
            index++;
            arg = getArgument(index);
        }
        if (index == start) {
            return null;
        }
        return result;
    }

    /**
     * ***************************************************************
     * Normalize all variables, so that the first variable in a formula is
     * ?VAR1, the second is ?VAR2 etc. This is necessary so that two formulas
     * can be found equal even if they have different variable names. Variables
     * must be normalized so that (foo ?A ?B) is equal to (foo ?X ?Y) - they
     * both are converted to (foo ?VAR1 ?VAR2) Note that this routine has a
     * significant known bug that variables whose names are a subset of one
     * another will cause problems, for example (foo ?VAR ?VAR1)
     */
    @SuppressWarnings("HardcodedFileSeparator")
    private static String normalizeVariables(String s) {

        int i = 0;
        int varCount = 0;
        int rowVarCount = 0;
        int varstart = 0;

        while (varstart != -1) {
            varstart = s.indexOf('?', i + 1);
            if (varstart != -1) {
                int varend = varstart + 1;
                while (Character.isJavaIdentifierPart(s.charAt(varend)) && varend < s.length()) {
                    varend++;
                }
                String varname = s.substring(varstart + 1, varend);
                s = s.replaceAll("\\?" + varname, "?VAR" + (new Integer(varCount++)));
                i = varstart;
            }
        }

        i = 0;
        while (varstart != -1) {
            varstart = s.indexOf('@', i + 1);
            if (varstart != -1) {
                int varend = varstart + 1;
                while (Character.isJavaIdentifierPart(s.charAt(varend)) && varend < s.length()) {
                    varend++;
                }
                String varname = s.substring(varstart + 1, varend);
                s = s.replaceAll("\\@" + varname, "@ROWVAR" + (new Integer(varCount++)));
                i = varstart;
            }
        }

        return s;
    }

    /**
     * ***************************************************************
     * Translate SUMO inequalities to the typical inequality symbols that the
     * theorem prover requires.
     */
    private String translateInequalities(String s) {

        if ("greaterThan".equalsIgnoreCase(s)) {
            return ">";
        }
        if ("greaterThanOrEqualTo".equalsIgnoreCase(s)) {
            return ">=";
        }
        if ("lessThan".equalsIgnoreCase(s)) {
            return "<";
        }
        if ("lessThanOrEqualTo".equalsIgnoreCase(s)) {
            return "<=";
        }
        return "";
    }

    /**
     * ***************************************************************
     * Collect all the quantified variables in the input, which is the String
     * representation of a Formula.
     *
     * @return An ArrayList of variables (Strings).
     */
    private ArrayList collectQuantifiedVariables(String theFormula) {
        ArrayList quantVariables = new ArrayList();
        int startIndex = -1;
        int tmpIndex = -1;
        int forallIndex = theFormula.indexOf("(forall (?", startIndex);
        int existsIndex = theFormula.indexOf("(exists (?", startIndex);
        int kappaFnIndex = theFormula.indexOf("(KappaFn ?", startIndex);
        while ((forallIndex != -1) || (existsIndex != -1) || (kappaFnIndex != -1)) {
            tmpIndex = (forallIndex < existsIndex && forallIndex != -1) || existsIndex == -1 ? forallIndex : existsIndex;
            startIndex = (tmpIndex < kappaFnIndex && tmpIndex != -1) || kappaFnIndex == -1 ? tmpIndex + 9 : kappaFnIndex + 9;

            int i = startIndex;
            while ((theFormula.charAt(i) != ')')
                    && (theFormula.charAt(i) != '(')
                    && (i < theFormula.length())) {
                i++;
                if (theFormula.charAt(i) == ' ') {
                    if (!quantVariables.contains(theFormula.substring(startIndex, i).intern())) {
                        quantVariables.add(theFormula.substring(startIndex, i));
                    }
                    //System.out.println(theFormula.substring(startIndex,i));
                    startIndex = i + 1;
                }
            }
            //System.out.println(startIndex);
            //System.out.println(i);
            if (i < theFormula.length()) {
                if (!quantVariables.contains(theFormula.substring(startIndex, i).intern())) {
                    quantVariables.add(theFormula.substring(startIndex, i).intern());
                }
                //System.out.println(theFormula.substring(startIndex,i));
                startIndex = i + 1;
            } else {
                startIndex = theFormula.length();
            }
            forallIndex = theFormula.indexOf("(forall (?", startIndex);
            existsIndex = theFormula.indexOf("(exists (?", startIndex);
            kappaFnIndex = theFormula.indexOf("(KappaFn ?", startIndex);
        }
        return quantVariables;
    }

    /**
     * ***************************************************************
     * Collect all the quantified variables in this Formula.
     *
     * @return An ArrayList of variables (Strings).
     */
    private ArrayList collectQuantifiedVariables() {

        return collectQuantifiedVariables(theFormula);
    }

    /**
     * ***************************************************************
     * Collect all the unquantified variables in a formula
     */
    private ArrayList collectUnquantifiedVariables(String theFormula, ArrayList quantVariables) {

        int startIndex = 0;
        ArrayList unquantVariables = new ArrayList();

        while (theFormula.indexOf('?', startIndex) != -1) {
            startIndex = theFormula.indexOf('?', startIndex);
            int spaceIndex = theFormula.indexOf(' ', startIndex);
            int parenIndex = theFormula.indexOf(')', startIndex);
            int i;
            i = (spaceIndex < parenIndex && spaceIndex != -1) || parenIndex == -1 ? spaceIndex : parenIndex;
            if (!quantVariables.contains(theFormula.substring(startIndex, i).intern())
                    && !unquantVariables.contains(theFormula.substring(startIndex, i).intern())) {
                unquantVariables.add(theFormula.substring(startIndex, i).intern());
                //System.out.println(theFormula.substring(startIndex,i));
            }
            startIndex = i;
        }
        return unquantVariables;
    }

    /**
     * ***************************************************************
     * Makes implicit quantification explicit.
     *
     * @param query controls whether to add universal or existential
     * quantification. If true, add existential.
     * @result the formula as a String, with explicit quantification
     */
    public String makeQuantifiersExplicit(boolean query) {

        if (theFormula.indexOf("(documentation") == 0) {
            return theFormula;
        }
        ArrayList quantVariables = collectQuantifiedVariables(theFormula);
        ArrayList unquantVariables = collectUnquantifiedVariables(theFormula, quantVariables);

        if (!unquantVariables.isEmpty()) {       // Quantify all the unquantified variables
            StringBuilder quant = new StringBuilder("(forall (");
            if (query) {
                quant = new StringBuilder("(exists (");
            }
            for (int i = 0; i < unquantVariables.size(); i++) {
                quant = quant.append((String) unquantVariables.get(i));
                if (i < unquantVariables.size() - 1) {
                    quant = quant.append(' ');
                }
            }
            //System.out.println("INFO in Formula.makeQuantifiersExplicit(): result: " + 
            //    quant.toString() + ") " + theFormula + ")");
            return quant + ") " + theFormula + ')';
        } else {
            return theFormula;
        }
    }

    /**
     * ***************************************************************
     *
     * @param kb - The KB used to compute variable arity relations.
     *
     * @return Returns true if this Formula contains any variable arity
     * relations, else returns false.
     */
    protected boolean containsVariableArityRelation(KB kb) {
        boolean ans = false;
        try {
            Set relns = kb.getCachedRelationValues("instance", "VariableArityRelation", 2, 1);
            if (relns != null) {
                String r = null;
                for (Object reln : relns) {
                    r = (String) reln;
                    ans = (theFormula.contains(r));
                    if (ans) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     *
     * @param kb - The KB used to compute variable arity relations.
     *
     * @return Returns true if this Formula contains any variable arity
     * relations, else returns false.
     */
    protected Formula renameVariableArityRelations(KB kb) {
        Formula result = this;
        try {
            if (listP()) {
                StringBuilder sb = new StringBuilder();
                Formula f = new Formula();
                f.read(theFormula);
                int flen = f.listLength();
                String suffix = ("_" + (flen - 1));
                String arg = null;
                sb.append('(');
                for (int i = 0; i < flen; i++) {
                    arg = f.getArgument(i);
                    if (i > 0) {
                        sb.append(' ');
                    }
                    if ((i == 0)
                            && kb.isVariableArityRelation(arg)
                            && !(arg.endsWith(suffix))) {
                        arg += suffix;
                    } else if (listP(arg)) {
                        Formula argF = new Formula();
                        argF.read(arg);
                        arg = argF.renameVariableArityRelations(kb).theFormula;
                    }
                    sb.append(arg);
                }
                sb.append(')');
                f = new Formula();
                f.read(sb.toString());
                result = f;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * ***************************************************************
     * Find all the row variables in an input String.
     *
     * @return a TreeSet of row variable names, without their '@' designator
     *
     */
    private TreeSet findRowVars(String input) {

        Formula f = new Formula();
        f.read(input);
        return f.findRowVars();
    }

    /**
     * ***************************************************************
     * Find all the row variables in this Formula.
     *
     * @return a TreeSet of row variable names, without their '@' designator
     *
     */
    private TreeSet findRowVars() {

        TreeSet result = new TreeSet();
        int i = 0;
        Formula f = new Formula();
        f.read(theFormula);

        while (f.listP() && !f.empty()) {
            String arg = f.car();
            if (arg.charAt(0) == '@') {
                result.add(arg.substring(1));
            } else {
                Formula argF = new Formula();
                argF.read(arg);
                if (argF.listP()) {
                    result.addAll(argF.findRowVars());
                }
            }
            f.read(f.cdr());
        }
        return result;
    }

    /**
     * ***************************************************************
     * Expand row variables, keeping the information about the original source
     * formula. Each variable is treated like a macro that expands to up to
     * seven regular variables. For example
     *
     * (=> (and (subrelation ?REL1 ?REL2) (holds__ ?REL1 @ROW)) (holds__ ?REL2
     *
     * @ROW))
     *
     * would become
     *
     * (=> (and (subrelation ?REL1 ?REL2) (holds__ ?REL1 ?ARG1)) (holds__ ?REL2
     * ?ARG1))
     *
     * (=> (and (subrelation ?REL1 ?REL2) (holds__ ?REL1 ?ARG1 ?ARG2)) (holds__
     * ?REL2 ?ARG1 ?ARG2)) etc.
     *
     * @return an ArrayList of Formulas, or an empty ArrayList.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    protected ArrayList expandRowVars(KB kb) {

        // System.out.println("INFO in Formula.expandRowVars(" + this + " ...)");
        ArrayList resultList = new ArrayList();
        try {

            // This is a kluge, but right here is the best place to
            // perform this check.  Continue only if this is not an
            // obvious tautology.  If the test immediately below
            // fails, this method will just return an empty List.
            if (!isRule() || !getArgument(1).equals(getArgument(2))) {

                Formula f = new Formula();
                f.read(theFormula);
                if (f.theFormula.indexOf('@') == -1) {
                    f.sourceFile = sourceFile;
                    resultList.add(f);
                } else {
                    TreeSet rowVars = f.findRowVars();
                    Iterator it = rowVars.iterator();
                    StringBuilder result = new StringBuilder(f.theFormula);
                    long t1 = 0L;

                    // Iterate through the row variables
                    while (it.hasNext()) {
                        String row = (String) it.next();

                        t1 = System.currentTimeMillis();
                        int[] range = getRowVarExpansionRange(kb, row);
                        // Increment the timer for getRowVarExpansionRange().
                        KB.ppTimers[3] += (System.currentTimeMillis() - t1);

                        boolean hasVariableArityRelation = (range[0] == 0);

                        t1 = System.currentTimeMillis();
                        range[1] = adjustExpansionCount(hasVariableArityRelation, range[1], row);
                        // Increment the timer for adjustExpansionCount().
                        KB.ppTimers[5] += (System.currentTimeMillis() - t1);

                        StringBuilder rowResult = new StringBuilder();
                        StringBuilder rowReplace = new StringBuilder();
                        for (int j = 1; j < range[1]; j++) {
                            if (!rowReplace.toString().isEmpty()) {
                                rowReplace = rowReplace.append(' ');
                            }
                            rowReplace = rowReplace.append('?').append(row).append((new Integer(j)));
                            if (hasVariableArityRelation) {
                                rowResult = rowResult.append(result.toString().replaceAll("\\@" + row, rowReplace.toString())).append('\n');
                            }
                        }
                        if (!hasVariableArityRelation) {
                            rowResult = rowResult.append(result.toString().replaceAll("\\@" + row, rowReplace.toString())).append('\n');
                        }
                        result = new StringBuilder(rowResult.toString());
                    }
                    ArrayList al = parseList(result.toString());
                    // System.out.println("INFO in Formula.expandRowVars(" + this + ")");
                    // System.out.println("  al == " + al);
                    Formula newF = null;
                    for (Object anAl : al) {
                        newF = (Formula) anAl;
                        // Copy the source file information for each expanded formula.
                        newF.sourceFile = sourceFile;
                        resultList.add(newF);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultList;
    }

    /**
     * ***************************************************************
     * This method attempts to revise the number of row var expansions to be
     * done, based on the occurrence of forms such as (<pred>
     *
     * @ROW1 ?ITEM). Note that variables such as ?ITEM throw off the default
     * expected expansion count, and so must be dealt with to prevent
     * unnecessary expansions.
     *
     * @param variableArity Indicates whether the overall expansion count for
     * the Formula is governed by a variable arity relation, or not.
     *
     * @param count The default expected expansion count, possibly to be
     * revised.
     *
     * @param var The row variable to be expanded.
     *
     * @return An int value, the revised expansion count. In most cases, the
     * count will not change.
     *
     */
    private int adjustExpansionCount(boolean variableArity, int count, String var) {

        // System.out.println("INFO in Formula.adjustExpansionCount(" + this + " ...)");
        int revisedCount = count;
        try {
            if (isNonEmptyString(var)) {
                String rowVar = var;
                if (!(var.length() > 0 && var.charAt(0) == '@')) {
                    rowVar = ('@' + var);
                }
                List accumulator = new ArrayList();
                List working = new ArrayList();
                if (listP() && !empty()) {
                    accumulator.add(this);
                }
                while (!(accumulator.isEmpty())) {
                    working.clear();
                    working.addAll(accumulator);
                    accumulator.clear();
                    for (Object aWorking : working) {
                        Formula f = (Formula) aWorking;
                        List literal = f.literalToArrayList();

                        // System.out.println(literal);
                        int len = literal.size();
                        if (literal.contains(rowVar) && !(isVariable(f.car()))) {
                            if (!variableArity && (len > 2)) {
                                revisedCount = (count - (len - 2));
                            } else if (variableArity) {
                                revisedCount = (10 - len);
                            }
                        }
                        if (revisedCount < 2) {
                            revisedCount = 2;
                        }
                        while (!(f.empty())) {
                            String arg = f.car();
                            Formula argF = new Formula();
                            argF.read(arg);
                            if (argF.listP() && !(argF.empty())) {
                                accumulator.add(argF);
                            }
                            f = f.cdrAsFormula();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.println("    -> " + revisedCount);
        return revisedCount;
    }

    /**
     * ***************************************************************
     * Returns a two-place int[] indicating the low and high points of the
     * expansion range (number of row var instances) for the input row var.
     *
     * @param kb A KB required for processing.
     *
     * @param rowVar The row var (String) to be expanded.
     *
     * @return A two-place int[] object. The int[] indicates a numeric range.
     * int[0] holds the start (lowest number) in the range, and int[1] holds the
     * highest number. The default is [1,8]. If the Formula does not contain
     *
     */
    private int[] getRowVarExpansionRange(KB kb, String rowVar) {

        // System.out.println("INFO in Formula.getRowVarExpansionRange(" + this + " ...)");
        int[] ans = new int[2];
        ans[0] = 1;
        ans[1] = 8;
        try {
            if (isNonEmptyString(rowVar)) {
                String var = rowVar;
                if (!(var.length() > 0 && var.charAt(0) == '@')) {
                    var = '@' + var;
                }
                Map minMaxMap = getRowVarsMinMax(kb);
                int[] newArr = (int[]) minMaxMap.get(var);
                if (newArr != null) {
                    ans = newArr;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.print("INFO in getRowVarExpansionRange(" + this + ", " + kb + ", " + rowVar + ")");
        // System.out.println(" -> [" + ans[0] + "," + ans[1] + "]");
        return ans;
    }

    /**
     * ***************************************************************
     * Applied to a SUO-KIF Formula with row variables, this method returns a
     * Map containing an int[] of length 2 for each row var that indicates the
     * minimum and maximum number of row var expansions to perform.
     *
     * @param kb A KB required for processing.
     *
     * @return A Map in which the keys are distinct row variables and the values
     * are two-place int[] objects. The int[] indicates a numeric range. int[0]
     * is the start (lowest number) in the range, and int[1] is the end. If the
     * Formula contains no row vars, the Map is empty.
     *
     */
    private Map getRowVarsMinMax(KB kb) {

        // System.out.println("INFO in Formula.getRowVarsMinMax(" + this + " ...)");
        Map ans = new HashMap();
        try {

            long t1 = System.currentTimeMillis();
            ArrayList clauseData = toNegAndPosLitsWithRenameInfo();
            // Increment the timer for toNegAndPosLitsWithRenameInfo().
            KB.ppTimers[4] += (System.currentTimeMillis() - t1);

            if (!((clauseData instanceof ArrayList) && (clauseData.size() > 2))) {
                return ans;
            }
            /*
             System.out.println();
             System.out.println("clauseData == " + clauseData);
             System.out.println();
             */
            ArrayList clauses = (ArrayList) clauseData.get(0);
            /*
             System.out.println();
             System.out.println("clauses == " + clauses);
             System.out.println("clauses.size() == " + clauses.size());
             System.out.println();
             */
            if (!(clauses instanceof ArrayList) || clauses.isEmpty()) {
                return ans;
            }

            Map varMap = (Map) clauseData.get(2);
            Map rowVarRelns = new HashMap();
            for (Object clause1 : clauses) {
                ArrayList clause = (ArrayList) clause1;

                // System.out.println("clause == " + clause);
                if ((clause != null) && !(clause.isEmpty())) {

                    // First we get the neg lits.  It may be that
                    // we should use *only* the neg lits for this
                    // task, but we will start by combining the neg
                    // lits and pos lits into one list of literals
                    // and see how that works.
                    ArrayList literals = (ArrayList) clause.get(0);
                    ArrayList posLits = (ArrayList) clause.get(1);
                    literals.addAll(posLits);
                    for (Object literal : literals) {
                        Formula litF = (Formula) literal;
                        litF.getRowVarsWithRelations_1(rowVarRelns);
                    }
                }

                // System.out.println("rowVarRelns == " + rowVarRelns);
                if (!(rowVarRelns.isEmpty())) {
                    for (Object o : rowVarRelns.keySet()) {
                        String rowVar = (String) o;
                        String origRowVar = getOriginalVar(rowVar, varMap);
                        int[] minMax = (int[]) ans.get(origRowVar);
                        if (minMax == null) {
                            minMax = new int[2];
                            minMax[0] = 0;
                            minMax[1] = 8;
                            ans.put(origRowVar, minMax);
                        }
                        TreeSet val = (TreeSet) rowVarRelns.get(rowVar);
                        for (Object aVal : val) {
                            String reln = (String) aVal;
                            int arity = kb.getValence(reln);
                            if (arity < 1) {
                                // It's a VariableArityRelation or we
                                // can't find an arity, do nothing.
                            } else {
                                minMax[0] = 1;
                                if ((arity + 1) < minMax[1]) {
                                    minMax[1] = (arity + 1);
                                }
                            }
                            /*
                             System.out.print("minMax == [ ");
                             for (int j = 0 ; j < minMax.length ; j++) {
                             if (j > 0) {
                             System.out.print(", ");
                             }
                             System.out.print(minMax[j]);
                             }
                             System.out.println(" ]");
                             */
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.println("INFO in getRowVarsMinMax(" + kb + ") -> " + ans);
        return ans;
    }

    /**
     * ***************************************************************
     * Finds all the row variables in a literal that occur with a relation that
     * might have a specific arity.
     *
     * @return A Map containing row var data for this literal. The keys are row
     * variables (Strings) and the values are TreeSets containing relations
     * (Strings) that might help to constrain the row var during row var
     * expansion.
     */
    protected Map getRowVarsWithRelations() {
        Map varsToRelns = new HashMap();
        getRowVarsWithRelations_1(varsToRelns);
        return varsToRelns;
    }

    /**
     * ***************************************************************
     * Finds all the row variables in a literal that occur with a relation that
     * might have a specific arity.
     *
     * @see getRowVarsWithRelations()
     *
     * @param varsToRelns A Map for accumulating row var data for one literal.
     * The keys are row variables (Strings) and the values are TreeSets
     * containing relations (Strings) that might help to constrain the row var
     * during row var expansion.
     *
     * @return void
     *
     */
    protected void getRowVarsWithRelations_1(Map varsToRelns) {
        try {
            Formula f = this;
            if (f.listP() && !(f.empty())) {
                String relation = f.car();
                if (!(isVariable(relation) || "SkFn".equals(relation))) {
                    Formula newF = f.cdrAsFormula();
                    while (newF.listP() && !(newF.empty())) {
                        String term = newF.car();
                        if (term.length() > 0 && term.charAt(0) == '@') {
                            TreeSet relns = (TreeSet) varsToRelns.get(term);
                            if (relns == null) {
                                relns = new TreeSet();
                                varsToRelns.put(term, relns);
                            }
                            relns.add(relation);
                        } else {
                            Formula termF = new Formula();
                            termF.read(term);
                            termF.getRowVarsWithRelations_1(varsToRelns);
                        }
                        newF = newF.cdrAsFormula();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * ***************************************************************
     * Convert an ArrayList of Formulas to an ArrayList of Strings.
     */
    private ArrayList formulasToStrings(ArrayList list) {

        ArrayList result = new ArrayList();
        for (Object aList : list) {
            result.add(((Formula) aList).theFormula);
        }
        return result;
    }

    /**
     * ***************************************************************
     * Test whether a Formula is a functional term
     */
    private boolean isFunctionalTerm() {

        if (!listP()) {
            return false;
        }
        String pred = car();
        return (pred.length() >= 2 && pred.endsWith("Fn"));
    }

    /**
     * ***************************************************************
     * Test whether an Object is a variable
     */
    public static boolean isVariable(Object term) {

        return (isNonEmptyString(term)
                && (((String) term).length() > 0 && ((String) term).charAt(0) == '?' || ((String) term).length() > 0 && ((String) term).charAt(0) == '@'));
    }

    /**
     * ***************************************************************
     * Test whether this Formula is a rule
     */
    private boolean isRule() {
        boolean ans = false;
        if (listP()) {
            String head = car();
            ans = ("=>".equals(head) || "<=>".equals(head));
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Test whether a list with a predicate is a quantifier list
     */
    private static boolean isQuantifierList(String listPred, String previousPred) {

        return (("exists".equals(previousPred) || "forall".equals(previousPred))
                && (listPred.length() > 0 && listPred.charAt(0) == '@' || listPred.length() > 0 && listPred.charAt(0) == '?'));
    }

    /**
     * ***************************************************************
     * Test whether a predicate is a logical quantifier
     */
    public static boolean isQuantifier(String pred) {

        return (isNonEmptyString(pred)
                && ("exists".equals(pred)
                || "forall".equals(pred)));
    }

    /**
     * ***************************************************************
     *
     * A static utility method.
     *
     * @param obj Any object, but should be a String.
     *
     * @return true if obj is a SUO-KIF commutative logical operate, else
     * false.
     *
     */
    public static boolean isCommutative(String obj) {

        return (isNonEmptyString(obj)
                && ("and".equals(obj)
                || "or".equals(obj)));
    }

    /**
     * ***************************************************************
     * Test whether a predicate is a logical operate
     */
    public static boolean isLogicalOperator(String pred) {

        String[] logOps = {"and", "or", "not", "=>", "<=>", "forall", "exists", "holds"};
        for (String logOp : logOps) {
            if (logOp.equals(pred)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ***************************************************************
     *
     * @param obj Any object
     *
     * @return true if obj is a non-empty String, else false.
     *
     */
    public static boolean isNonEmptyString(Object obj) {
        return ((obj instanceof String) && !"".equals(obj));
    }

    /**
     * ***************************************************************
     *
     * @return An ArrayList (ordered tuple) representation of the Formula, in
     * which each top-level element of the Formula is either an atom (String) or
     * another list (ArrayList).
     *
     */
    public ArrayList literalToArrayList() {
        ArrayList tuple = new ArrayList();
        try {
            Formula f = this;
            if (f.listP()) {
                while (!f.empty()) {
                    tuple.add(f.car());
                    f = f.cdrAsFormula();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tuple;
    }

    /**
     * ***************************************************************
     * This method returns all SUO-KIF variables that occur in the Formula.
     *
     * @see gatherVariables(TreeSet accumulator)
     *
     * @return A TreeSet containing variables (Strings), or an empty TreeSet if
     * no variables can be found.
     */
    private TreeSet gatherVariables() {
        return gatherVariables(null);
    }

    /**
     * ***************************************************************
     * @see gatherVariables()
     *
     * @param accumulator A TreeSet used for storing variables (Strings).
     *
     * @return A TreeSet containing variables (Strings), or an empty TreeSet if
     * no variables can be found.
     */
    private TreeSet gatherVariables(TreeSet accumulator) {
        if (accumulator == null) {
            accumulator = new TreeSet();
        }
        try {
            if (listP() && !(empty())) {
                String arg0 = car();
                Formula arg0F = new Formula();
                arg0F.read(arg0);
                arg0F.gatherVariables(accumulator);
                cdrAsFormula().gatherVariables(accumulator);
            } else if (isVariable(theFormula)) {
                accumulator.add(theFormula);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return accumulator;
    }

    /**
     * ***************************************************************
     * This Map is used to cache sortal predicate argument type data whenever
     * Formula.findType() or Formula.getTypeList() are going to called hundreds
     * or thousands of times inside KB.preProcess(). The Map is cleared and
     * SORTAL_TYPE_CACHE is set to null after each such use in KB.preProcess().
     */
    private static HashMap SORTAL_TYPE_CACHE = null;

    /**
     * ***************************************************************
     * Clears the Map bound to SORTAL_TYPE_CACHE if the variable is not null,
     * then sets SORTAL_TYPE_CACHE to null.
     *
     * @return void
     */
    protected static void destroySortalTypeCache() {
        if (SORTAL_TYPE_CACHE != null) {
            System.out.println("INFO in Formula.destroySortalTypeCache()");
            System.out.println("  Clearing " + SORTAL_TYPE_CACHE.size() + " entries");
            SORTAL_TYPE_CACHE.clear();
        }
        SORTAL_TYPE_CACHE = null;
    }

    /**
     * ***************************************************************
     * Calls Formula.destroySortalTypeCache() to set SORTAL_TYPE_CACHE to null,
     * then sets SORTAL_TYPE_CACHE to a new, empty HashMap.
     *
     * @return void
     */
    protected static void resetSortalTypeCache() {
        destroySortalTypeCache();
        SORTAL_TYPE_CACHE = new HashMap();
        System.out.println("INFO in Formula.resetSortalTypeCache()");
        System.out.println("  SORTAL_TYPE_CACHE == " + SORTAL_TYPE_CACHE);
    }

    /**
     * ***************************************************************
     * A + is appended to the type if the parameter must be a class
     *
     * @return the type for each argument to the given predicate, where
     * ArrayList element 0 is the result, if a function, 1 is the first
     * argument, 2 is the second etc.
     */
    private ArrayList getTypeList(String pred, KB kb) {

        //System.out.println("INFO in Formula.getTypeList(): pred: " + pred);
        ArrayList result = null;
        try {
            if (SORTAL_TYPE_CACHE != null) {
                String key = "gtl" + pred + kb.name;
                result = ((ArrayList) (SORTAL_TYPE_CACHE.get(key)));
                if (result != null) {
                    return result;
                } else {
                    result = new ArrayList();
                    SORTAL_TYPE_CACHE.put(key, result);
                }
            } else {
                result = new ArrayList();
            }
            int valence = kb.getValence(pred);
            int len = MAX_PREDICATE_ARITY + 1;
            if (valence == 0) {
                len = 2;
            } else if (valence > 0) {
                len = valence + 1;
            }
            String[] r = new String[len];

            ArrayList al = kb.askWithRestriction(0, "domain", 1, pred);
            ArrayList al2 = kb.askWithRestriction(0, "domainSubclass", 1, pred);
            ArrayList al3 = kb.askWithRestriction(0, "range", 1, pred);
            ArrayList al4 = kb.askWithRestriction(0, "rangeSubclass", 1, pred);
            r = addToTypeList(pred, al, r, "");
            r = addToTypeList(pred, al2, r, "+");
            r = addToTypeList(pred, al3, r, "");
            r = addToTypeList(pred, al4, r, "+");
            result.addAll(Arrays.asList(r));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * ***************************************************************
     * A utility helper method for computing predicate data types.
     */
    private String[] addToTypeList(String pred, ArrayList al, String[] result, String classP) {
        try {
            Formula f = null;
            // If the relations in al start with "(range", argnum will
            // be 0, and the arg position of the desired classnames
            // will be 2.
            int argnum = 0;
            int clPos = 2;
            for (int i = 0; i < al.size(); i++) {
                f = (Formula) al.get(i);
                //System.out.println("INFO in addToTypeList(): formula: " + f.theFormula);
                if (f.theFormula.startsWith("(domain")) {
                    argnum = Integer.parseInt(f.getArgument(2));
                    clPos = 3;
                }
                String cl = f.getArgument(clPos);
                String errStr = null;
                String mgrErrStr = null;
                if ((argnum < 0) || (argnum >= result.length)) {
                    errStr = "Possible arity confusion for " + pred;
                    mgrErrStr = KBmanager.getMgr().getError();
                    System.out.println("WARNING in Formula.addToTypeList(): "
                            + errStr
                            + ": al == "
                            + al
                            + ", result.length == "
                            + result.length
                            + ", classP == \""
                            + classP
                            + '"');
                    if (mgrErrStr.isEmpty() || (!mgrErrStr.contains(errStr))) {
                        KBmanager.getMgr().setError(mgrErrStr
                                + "\n<br/>"
                                + errStr
                                + "\n<br/>");
                    }
                } else if ((result[argnum] == null) || result[argnum].isEmpty()) {
                    result[argnum] = cl + classP;
                } else {
                    errStr = ("Multiple types asserted for argument "
                            + argnum
                            + " of "
                            + pred
                            + ": "
                            + cl
                            + ", "
                            + result[argnum]);
                    mgrErrStr = KBmanager.getMgr().getError();
                    System.out.println("Error in Formula.addToTypeList(): " + errStr);
                    if (mgrErrStr.isEmpty() || (!mgrErrStr.contains(errStr))) {
                        KBmanager.getMgr().setError(KBmanager.getMgr().getError()
                                + "\n<br/>"
                                + errStr
                                + "\n<br/>");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * ***************************************************************
     * Find the argument type restriction for a given predicate and argument
     * number that is inherited from one of its super-relations. A "+" is
     * appended to the type if the parameter must be a class. Argument number 0
     * is used for the return type of a Function.
     */
    private String findType(int numarg, String pred, KB kb) {

        // System.out.println("INFO in Formula.findType(" + numarg + ", " + pred + ")");
        if (DEBUG) {
            System.out.println("ENTER findType(" + numarg + ", " + pred + ", " + kb + ')');
        }

        String result = null;
        try {
            boolean cacheResult = false;
            String key = null;
            if (SORTAL_TYPE_CACHE != null) {
                key = "ft" + numarg + pred + kb.name;
                result = ((String) (SORTAL_TYPE_CACHE.get(key)));
                if (result != null) {
                    return result;
                } else {
                    result = "";
                    cacheResult = true;
                }
            } else {
                result = "";
            }
            boolean found = false;
            Set accumulator = new HashSet();
            accumulator.add(pred);
            List parents = new ArrayList();
            Iterator it = null;
            String newPred = null;
            while (!found && !accumulator.isEmpty()) {
                parents.clear();
                parents.addAll(accumulator);
                accumulator.clear();
                List axioms = null;
                Formula f = null;
                it = parents.iterator();
                while (!found && it.hasNext()) {
                    newPred = (String) it.next();
                    if (numarg > 0) {
                        axioms = kb.askWithRestriction(0, "domain", 1, newPred);
                        for (Object axiom1 : axioms) {
                            f = (Formula) axiom1;
                            int argnum = Integer.parseInt(f.getArgument(2));
                            if (argnum == numarg) {
                                result = f.getArgument(3);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            axioms = kb.askWithRestriction(0, "domainSubclass", 1, newPred);
                            for (Object axiom : axioms) {
                                f = (Formula) axiom;
                                int argnum = Integer.parseInt(f.getArgument(2));
                                if (argnum == numarg) {
                                    result = f.getArgument(3) + '+';
                                    found = true;
                                    break;
                                }
                            }
                        }
                    } else if (numarg == 0) {
                        axioms = kb.askWithRestriction(0, "range", 1, newPred);
                        if (!axioms.isEmpty()) {
                            f = (Formula) axioms.get(0);
                            result = f.getArgument(2);
                            found = true;
                        }
                        if (!found) {
                            axioms = kb.askWithRestriction(0, "rangeSubclass", 1, newPred);
                            if (!axioms.isEmpty()) {
                                f = (Formula) axioms.get(0);
                                result = f.getArgument(2) + '+';
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        Set newParents = kb.getCachedRelationValues("subrelation", newPred, 1, 2);
                        if ((newParents != null) && !newParents.isEmpty()) {
                            accumulator.addAll(newParents);
                            accumulator.remove(newPred);
                        }
                    }
                }
            }
            if (cacheResult) {
                SORTAL_TYPE_CACHE.put(key, result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.println("  -> " + result);

        if (DEBUG) {
            System.out.println("EXIT findType(" + numarg + ", " + pred + ", " + kb + ") -> " + result);
        }
        return result;
    }

    /**
     * ***************************************************************
     * This method tries to remove all but the most specific relevant classes
     * from a List of sortal classes.
     *
     * @param types A List of classes (class name Strings) that constrain the
     * value of a SUO-KIF variable.
     *
     * @param kb The KB used to determine if any of the classes in the List
     * types are redundant.
     *
     * @return void
     */
    private void winnowTypeList(List types, KB kb) {

        if (DEBUG) {
            System.out.println("ENTER winnowTypeList(" + types + ", " + kb + ')');
        }

        try {
            if ((types instanceof List) && (types.size() > 1)) {
                Object[] valArr = types.toArray();
                String clX = null;
                String clY = null;
                for (int i = 0; i < valArr.length; i++) {
                    for (int j = 0; j < valArr.length; j++) {
                        if (i != j) {
                            clX = (String) valArr[i];
                            clY = (String) valArr[j];
                            if (kb.isSubclass(clX, clY)) {
                                types.remove(clY);
                                if (types.size() < 2) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (DEBUG) {
            System.out.println("EXIT winnowTypeList(" + types + ", " + kb + ')');
        }

    }

    /**
     * ***************************************************************
     * Does much of the real work for addTypeRestrictions() by recursing through
     * the Formula and collecting type constraint information for the variable
     * var.
     *
     * @param ios A List of classes (class name Strings) of which any binding
     * for var must be an instance.
     *
     * @param scs A List of classes (class name Strings) of which any binding
     * for var must be a subclass.
     *
     * @param var A SUO-KIF variable.
     *
     * @param kb The KB used to determine predicate and variable arg types.
     *
     * @return void
     */
    private void computeTypeRestrictions(List ios, List scs, String var, KB kb) {

        if (DEBUG) {
            System.out.println("ENTER computeTypeRestrictions("
                    + this + ", "
                    + ios + ", "
                    + scs + ", "
                    + var + ", "
                    + kb + ')');
        }

        String pred = null;
        try {
            Formula f = new Formula();
            f.read(theFormula);
            if (!f.listP()) {
                return;
            }
            pred = f.car();
            if (isQuantifier(pred)) {
                Formula nextF = new Formula();
                nextF.read(f.getArgument(2));
                nextF.computeTypeRestrictions(ios, scs, var, kb);
            } else if (isLogicalOperator(pred)) {
                int len = f.listLength();
                for (int i = 1; i < len; i++) {
                    Formula nextF = new Formula();
                    nextF.read(f.getArgument(i));
                    nextF.computeTypeRestrictions(ios, scs, var, kb);
                }
            } else {
                int len = f.listLength();
                int valence = kb.getValence(pred);
                List types = getTypeList(pred, kb);
                int numarg = 0;
                for (int i = 1; i < len; i++) {
                    numarg = i;
                    if (valence == 0) { // pred is a VariableArityRelation
                        numarg = 1;
                    }
                    String arg = f.getArgument(i);
                    if (listP(arg)) {
                        Formula nextF = new Formula();
                        nextF.read(arg);
                        nextF.computeTypeRestrictions(ios, scs, var, kb);
                    } else if (var.equals(arg)) {
                        String type = null;
                        type = numarg >= types.size() ? findType(numarg, pred, kb) : (String) types.get(numarg);
                        if (type == null) {
                            type = findType(numarg, pred, kb);
                        }
                        if (!type.isEmpty() && !type.startsWith("Entity")) {
                            boolean sc = false;
                            while (type.length() > 0 && type.charAt(type.length() - 1) == '+') {
                                sc = true;
                                type = type.substring(0, type.length() - 1);
                            }
                            if (sc) {
                                if (!scs.contains(type)) {
                                    scs.add(type);
                                }
                            } else if (!ios.contains(type)) {
                                ios.add(type);
                            }
                        }
                    }
                }

                String arg1 = null;
                String arg2 = null;
                String term = null;
                String cl = null;

                // Special treatment for equal
                switch (pred) {
                    case "equal":
                        arg1 = f.getArgument(1);
                        arg2 = f.getArgument(2);
                        if (var.equals(arg1)) {
                            term = arg2;
                        } else if (var.equals(arg2)) {
                            term = arg1;
                        }
                        if (isNonEmptyString(term)) {
                            if (listP(term)) {
                                Formula nextF = new Formula();
                                nextF.read(term);
                                if (nextF.isFunctionalTerm()) {
                                    String fn = nextF.car();
                                    List classes = getTypeList(fn, kb);
                                    if (!classes.isEmpty()) {
                                        cl = (String) classes.get(0);
                                    }
                                    if (cl == null) {
                                        cl = findType(0, fn, kb);
                                    }
                                    if (!cl.isEmpty() && !cl.startsWith("Entity")) {
                                        boolean sc = false;
                                        while (cl.length() > 0 && cl.charAt(cl.length() - 1) == '+') {
                                            sc = true;
                                            cl = cl.substring(0, cl.length() - 1);
                                        }
                                        if (sc) {
                                            if (!scs.contains(cl)) {
                                                scs.add(cl);
                                            }
                                        } else if (!ios.contains(cl)) {
                                            ios.add(cl);
                                        }
                                    }
                                }
                            } else {
                                Set instanceOfs = kb.getCachedRelationValues("instance", term, 1, 2);
                                if ((instanceOfs != null) && !instanceOfs.isEmpty()) {
                                    Iterator it = instanceOfs.iterator();
                                    String io = null;
                                    while (it.hasNext()) {
                                        io = (String) it.next();
                                        if (!"Entity".equals(io) && !ios.contains(io)) {
                                            ios.add(io);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "instance":
                    case "subclass":
                        arg1 = f.getArgument(1);
                        arg2 = f.getArgument(2);
                        if (var.equals(arg1) && listP(arg2)) {
                            Formula nextF = new Formula();
                            nextF.read(arg2);
                            if (nextF.isFunctionalTerm()) {
                                String fn = nextF.car();
                                List classes = getTypeList(fn, kb);
                                if (!classes.isEmpty()) {
                                    cl = (String) classes.get(0);
                                }
                                if (cl == null) {
                                    cl = findType(0, fn, kb);
                                }
                                if (!cl.isEmpty() && !cl.startsWith("Entity")) {
                                    while (cl.length() > 0 && cl.charAt(cl.length() - 1) == '+') {
                                        cl = cl.substring(0, cl.length() - 1);
                                    }
                                    if ("subclass".equals(pred)) {
                                        if (!scs.contains(cl)) {
                                            scs.add(cl);
                                        }
                                    } else if (!ios.contains(cl)) {
                                        ios.add(cl);
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in Formula.computeTypeRestrictions("
                    + this + ", "
                    + ios + ", "
                    + scs + ", "
                    + var + ", "
                    + kb + ')');
            System.out.println("  pred == " + pred);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        if (DEBUG) {
            System.out.println("EXIT computeTypeRestrictions("
                    + this + ", "
                    + ios + ", "
                    + scs + ", "
                    + var + ", "
                    + kb + " )");
        }

    }

    /**
     * ***************************************************************
     * When invoked on a Formula that begins with explicit universal
     * quantification, this method returns a String representation of the
     * Formula with type constraints added for the top level quantified
     * variables, if possible. Otherwise, a String representation of the
     * original Formula is returned.
     *
     * @param kb The KB used to determine predicate and variable arg types.
     *
     * @return A String representation of a Formula, with type restrictions
     * added.
     */
    private String insertTypeRestrictionsU(KB kb) {

        if (DEBUG) {
            System.out.println("ENTER insertTypeRestrictionsU(" + this + ", " + kb + ')');
        }

        String result = "";
        try {
            Formula varlistF = new Formula();
            varlistF.read(getArgument(1));
            int vlen = varlistF.listLength();
            Formula nextF = new Formula();
            nextF.read(getArgument(2));
            List constraints = new ArrayList();
            List ios = new ArrayList();
            List scs = new ArrayList();
            Iterator classIt = null;
            String constraint = null;
            String var = null;
            for (int i = 0; i < vlen; i++) {
                ios.clear();
                scs.clear();
                var = varlistF.getArgument(i);
                nextF.computeTypeRestrictions(ios, scs, var, kb);
                if (!ios.isEmpty()) {
                    winnowTypeList(ios, kb);
                    classIt = ios.iterator();
                    while (classIt.hasNext()) {
                        constraint = "(instance " + var + ' ' + classIt.next() + ')';
                        if (!theFormula.contains(constraint)) {
                            constraints.add(constraint);
                        }
                    }
                }
                if (!scs.isEmpty()) {
                    winnowTypeList(scs, kb);
                    classIt = scs.iterator();
                    while (classIt.hasNext()) {
                        constraint = "(subclass " + var + ' ' + classIt.next() + ')';
                        if (!theFormula.contains(constraint)) {
                            constraints.add(constraint);
                        }
                    }
                }
            }
            result += "(forall " + varlistF.theFormula;
            if (constraints.isEmpty()) {
                result += ' ' + nextF.insertTypeRestrictions(kb);
            } else {
                result += " (=>";
                int clen = constraints.size();
                if (clen > 1) {
                    result += " (and";
                }
                for (Object constraint1 : constraints) {
                    result += " " + constraint1;
                }
                if (clen > 1) {
                    result += ")";
                }
                result += ' ' + nextF.insertTypeRestrictions(kb);
                result += ")";
            }
            result += ")";
        } catch (Exception ex) {
            ex.printStackTrace();
            result = theFormula;
        }

        if (DEBUG) {
            System.out.println("EXIT insertTypeRestrictionsU(" + this + ", " + kb + ") -> " + result);
        }

        return result;
    }

    /**
     * ***************************************************************
     * When invoked on a Formula that begins with explicit existential
     * quantification, this method returns a String representation of the
     * Formula with type constraints added for the top level quantified
     * variables, if possible. Otherwise, a String representation of the
     * original Formula is returned.
     *
     * @param kb The KB used to determine predicate and variable arg types.
     *
     * @return A String representation of a Formula, with type restrictions
     * added.
     */
    private String insertTypeRestrictionsE(KB kb) {

        if (DEBUG) {
            System.out.println("ENTER insertTypeRestrictionsE(" + this + ", " + kb + ')');
        }

        String result = "";
        try {
            Formula varlistF = new Formula();
            varlistF.read(getArgument(1));
            int vlen = varlistF.listLength();
            Formula nextF = new Formula();
            nextF.read(getArgument(2));
            List constraints = new ArrayList();
            List ios = new ArrayList();
            List scs = new ArrayList();
            Iterator classIt = null;
            String constraint = null;
            String var = null;
            for (int i = 0; i < vlen; i++) {
                ios.clear();
                scs.clear();
                var = varlistF.getArgument(i);
                nextF.computeTypeRestrictions(ios, scs, var, kb);
                if (!ios.isEmpty()) {
                    winnowTypeList(ios, kb);
                    classIt = ios.iterator();
                    while (classIt.hasNext()) {
                        constraint = "(instance " + var + ' ' + classIt.next() + ')';
                        if (!theFormula.contains(constraint)) {
                            constraints.add(constraint);
                        }
                    }
                }
                if (!scs.isEmpty()) {
                    winnowTypeList(scs, kb);
                    classIt = scs.iterator();
                    while (classIt.hasNext()) {
                        constraint = "(subclass " + var + ' ' + classIt.next() + ')';
                        if (!theFormula.contains(constraint)) {
                            constraints.add(constraint);
                        }
                    }
                }
            }
            result += "(exists " + varlistF.theFormula;
            if (constraints.isEmpty()) {
                result += ' ' + nextF.insertTypeRestrictions(kb);
            } else {
                result += " (and";
                int clen = constraints.size();
                for (Object constraint1 : constraints) {
                    result += " " + constraint1;
                }
                if ("and".equals(nextF.car())) {
                    int nextFLen = nextF.listLength();
                    for (int k = 1; k < nextFLen; k++) {
                        Formula ff = new Formula();
                        ff.read(nextF.getArgument(k));
                        result += ' ' + ff.insertTypeRestrictions(kb);
                    }
                } else {
                    result += ' ' + nextF.insertTypeRestrictions(kb);
                }
                result += ")";
            }
            result += ")";
        } catch (Exception ex) {
            ex.printStackTrace();
            result = theFormula;
        }

        if (DEBUG) {
            System.out.println("EXIT insertTypeRestrictionsE(" + this + ", " + kb + ") -> " + result);
        }

        return result;
    }

    /**
     * ***************************************************************
     * When invoked on a Formula, this method returns a String representation of
     * the Formula with type constraints added for all explicitly quantified
     * variables, if possible. Otherwise, a String representation of the
     * original Formula is returned.
     *
     * @param kb The KB used to determine predicate and variable arg types.
     *
     * @return A String representation of a Formula, with type restrictions
     * added.
     */
    private String insertTypeRestrictions(KB kb) {

        if (DEBUG) {
            System.out.println("ENTER insertTypeRestrictions(" + this + ", " + kb + ')');
        }

        String result = "";
        try {
            Formula f = new Formula();
            f.read(theFormula);
            if (f.listP() && !f.empty()) {
                int len = f.listLength();
                String arg0 = f.car();
                if (isQuantifier(arg0) && (len == 3)) {
                    result += "forall".equals(arg0) ? f.insertTypeRestrictionsU(kb) : f.insertTypeRestrictionsE(kb);
                } else {
                    result += "(";
                    for (int i = 0; i < len; i++) {
                        if (i > 0) {
                            result += " ";
                        }
                        Formula nextF = new Formula();
                        nextF.read(f.getArgument(i));
                        result += nextF.insertTypeRestrictions(kb);
                    }
                    result += ")";
                }
            } else {
                result += theFormula;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            result = theFormula;
        }

        if (DEBUG) {
            System.out.println("EXIT insertTypeRestrictions(" + this + ", " + kb + ") -> " + result);
        }

        return result;
    }

    /**
     * ***************************************************************
     * Add clauses for every variable in the antecedent to restrict its type to
     * the type restrictions defined on every relation in which it appears. For
     * example (=> (foo ?A B) (bar B ?A))
     *
     * (domain foo 1 Z)
     *
     * would result in
     *
     * (=> (instance ?A Z) (=> (foo ?A B) (bar B ?A)))
     */
    private String addTypeRestrictions(KB kb) {

        // System.out.println("INFO in Formula.addTypeRestrictions(" + this + ")");
        if (DEBUG) {
            System.out.println("ENTER addTypeRestrictions(" + this + ", " + kb + ')');
        }

        String result = theFormula;
        try {
            Formula f = new Formula();
            f.read(theFormula);
            f.read(f.makeQuantifiersExplicit(false));
            result = f.insertTypeRestrictions(kb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (DEBUG) {
            System.out.println("EXIT addTypeRestrictions(" + this + ", " + kb + ") -> " + result);
        }

        return result;
    }

    /**
     * ***************************************************************
     * Pre-process a formula before sending it to the theorem prover. This
     * includes ignoring meta-knowledge like documentation strings, translating
     * mathematical operators, quoting higher-order formulas, expanding row
     * variables and prepending the 'holds__' predicate.
     *
     * @return an ArrayList of Formula(s)
     */
    private String preProcessRecurse(Formula f, String previousPred, boolean ignoreStrings,
            boolean translateIneq, boolean translateMath) {

        // System.out.println("INFO in Formula.preProcessRecurse(" + this + ")");
        String[] logOps = {"and", "or", "not", "=>", "<=>", "forall", "exists"};
        String[] matOps = {"equal", "AdditionFn", "SubtractionFn", "MultiplicationFn", "DivisionFn"};
        String[] compOps = {"greaterThan", "greaterThanOrEqualTo", "lessThan", "lessThanOrEqualTo"};
        ArrayList logicalOperators = new ArrayList(Arrays.asList(logOps));
        ArrayList mathOperators = new ArrayList(Arrays.asList(matOps));
        ArrayList comparisonOperators = new ArrayList(Arrays.asList(compOps));

        StringBuilder result = new StringBuilder();
        if (f.theFormula != null && f.theFormula.isEmpty() || !f.listP() || f.atom() || f.empty()) {
            return "";
        }

        String prefix = "";
        String pred = f.car();
        Formula predF = new Formula();
        predF.read(pred);

        if (isQuantifier(pred)) {

            // The list of quantified variables.
            result.append(' ');
            result.append(f.cadr());

            // The formula following the list of variables.
            String next = f.caddr();
            Formula nextF = new Formula();
            nextF.read(next);
            result.append(' ');
            result.append(preProcessRecurse(nextF,
                    "",
                    ignoreStrings,
                    translateIneq,
                    translateMath));
        } else {
            Formula restF = f.cdrAsFormula();
            int argCount = 1;
            while (!restF.empty()) {
                argCount++;
                String arg = restF.car();

                //System.out.println("INFO in preProcessRecurse(): arg: " + arg);
                Formula argF = new Formula();
                argF.read(arg);
                if (argF.listP()) {
                    String res = preProcessRecurse(argF, pred, ignoreStrings, translateIneq, translateMath);
                    result.append(' ');
                    if (!logicalOperators.contains(pred)
                            && !comparisonOperators.contains(pred)
                            && !mathOperators.contains(pred)
                            && !argF.isFunctionalTerm()) {
                        result.append('`');
                    }
                    result.append(res);
                } else {
                    result.append(' ').append(arg);
                }
                restF.theFormula = restF.cdr();
            }

            if ("yes".equals(KBmanager.getMgr().getPref("holdsPrefix"))) {
                if (!logicalOperators.contains(pred) && !isQuantifierList(pred, previousPred)) {
                    prefix = "holds_";
                }
                if (f.isFunctionalTerm()) {
                    prefix = "apply_";
                }
                if ("holds".equals(pred)) {
                    pred = "";
                    argCount--;
                    prefix = prefix + argCount + "__ ";
                } else if (!logicalOperators.contains(pred)
                        && !isQuantifierList(pred, previousPred)
                        && !mathOperators.contains(pred)
                        && !comparisonOperators.contains(pred)) {
                    prefix = prefix + argCount + "__ ";
                } else {
                    prefix = "";
                }
            }
        }

        return '(' + prefix + pred + result + ')';
    }

    /**
     * ***************************************************************
     * Pre-process a formula before sending it to the theorem prover. This
     * includes ignoring meta-knowledge like documentation strings, translating
     * mathematical operators, quoting higher-order formulas, expanding row
     * variables and prepending the 'holds__' predicate.
     *
     * @param query controls whether to add universal or existential
     * quantification. If true, add existential.
     * @return an ArrayList of Formula(s), which could be empty.
     */
    public ArrayList preProcess(boolean query, KB kb) {

        // System.out.println("INFO in Formula.preProcess(" + this + ", " + query + ", " + kb.name + ")");
        ArrayList results = new ArrayList();
        try {

            if (!isNonEmptyString(theFormula)) {
                return results;
            }

            boolean ignoreStrings = false;
            boolean translateIneq = true;
            boolean translateMath = true;
            ArrayList accumulator = new ArrayList();

            boolean addHoldsPrefix = "yes".equalsIgnoreCase(KBmanager.getMgr().getPref("holdsPrefix"));

            long t1 = -1L;
            long tnaplwriVal = KB.ppTimers[4];

            // Do pred var instantiations if we are not adding holds
            // prefixes.
            Formula f = new Formula();
            f.read(theFormula);

            t1 = System.currentTimeMillis();
            ArrayList predVarInstantiations = new ArrayList();
            if (!addHoldsPrefix) {
                predVarInstantiations.addAll(f.instantiatePredVars(kb));
            }

            // If the list of pred var instatiations is empty, add the
            // original formula to the list for further processing below.  
            if (predVarInstantiations.isEmpty()) {
                predVarInstantiations.add(f);
            } else {
                // If the formula contains a pred var that can't be
                // instantiated and so has been marked "reject", don't add
                // anything.
                Object obj0 = predVarInstantiations.get(0);
                if (isNonEmptyString(obj0) && "reject".equalsIgnoreCase((String) obj0)) {
                    predVarInstantiations.clear();
                    System.out.println("WARNING in Formula.preProcess(): No predicate instantiations for\n" + this);
                    String errStr = "No predicate instantiations for <br/>" + htmlFormat(kb);
                    KBmanager.getMgr().setError(KBmanager.getMgr().getError()
                            + ("\n<br/>" + errStr + "\n<br/>"));
                }
            }
            // Increment the timer for pred var instantiation.
            KB.ppTimers[1] += (System.currentTimeMillis() - t1);

            // We do this to avoid adding up time spent in
            // Formula.toNegAndPosLitsWtihRenameInfo() while doing pred
            // var instantiation.  What we really want to know is how much
            // time this method contributes to the total time for row var
            // expansion.
            KB.ppTimers[4] = tnaplwriVal;

            // Iterate over the instantiated predicate formulas, doing row
            // var expansion on each.  If no predicate instantiations can
            // be generated, the ArrayList predVarInstantiations will
            // contain just the original input formula.
            t1 = System.currentTimeMillis();
            int pviN = predVarInstantiations.size();
            Iterator it = null;
            if ((pviN > 0) && (pviN < AXIOM_EXPANSION_LIMIT)) {
                it = predVarInstantiations.iterator();
                ArrayList rowVarExpansions = null;
                while (it.hasNext()) {
                    f = (Formula) it.next();
                    // System.out.println("f == " + f);
                    rowVarExpansions = f.expandRowVars(kb);
                    if (rowVarExpansions != null) {
                        accumulator.addAll(rowVarExpansions);

                        // System.out.println("  accumulator == " + accumulator);
                        if (accumulator.size() > AXIOM_EXPANSION_LIMIT) {
                            break;
                        }
                    }
                }
            }

            // Increment the timer for row var expansion.
            KB.ppTimers[2] += (System.currentTimeMillis() - t1);

            // Iterate over the formulas resulting from row var expansion,
            // passing each to preProcessRecurse for further processing.
            if (!accumulator.isEmpty()) {
                it = accumulator.iterator();
                boolean addSortals = "yes".equalsIgnoreCase(KBmanager.getMgr().getPref("typePrefix"));
                Formula fnew = null;
                String theNewFormula = null;
                while (it.hasNext()) {
                    fnew = (Formula) it.next();

                    t1 = System.currentTimeMillis();
                    String arg0 = getArgument(0);
                    if (addSortals
                            && !query
                            // isLogicalOperator(arg0) ||
                            && (fnew.theFormula.indexOf('?') != -1)) {
                        fnew.read(fnew.addTypeRestrictions(kb));
                    }
                    // Increment the timer for adding type restrictions.
                    KB.ppTimers[0] += (System.currentTimeMillis() - t1);

                    t1 = System.currentTimeMillis();
                    theNewFormula = fnew.preProcessRecurse(fnew,
                            "",
                            ignoreStrings,
                            translateIneq,
                            translateMath);
                    fnew.read(theNewFormula);
                    // Increment the timer for preProcessRecurse().
                    KB.ppTimers[6] += (System.currentTimeMillis() - t1);

                    if (fnew.isOkForInference(query, kb)) {
                        fnew.sourceFile = sourceFile;
                        results.add(fnew);
                    } else {
                        System.out.println("WARNING in Formula.preProcess()");
                        System.out.println("  REJECTING " + theNewFormula);
                        KBmanager.getMgr().setError(KBmanager.getMgr().getError()
                                + "\n<br/>Formula rejected for nal:<br/>"
                                + fnew.htmlFormat(kb)
                                + "<br/>\n");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return results;
    }

    /**
     * ***************************************************************
     * Returns true if this Formula appears not to have any of the
     * characteristics that would cause it to be rejected during translation to
     * TPTP form, or cause problems during logic. Otherwise, returns false.
     *
     * @param query true if this Formula represents a query, else false.
     *
     * @param kb The KB object to be used for evaluating the suitability of this
     * Formula.
     *
     * @return boolean
     */
    private boolean isOkForInference(boolean query, KB kb) {
        boolean pass = false;
        // kb isn't used yet, because the checks below are purely
        // syntactic.  But it probably will be used in the future.
        try {
            pass = !(// (equal ?X ?Y ?Z ...) - equal is strictly binary. 
                    theFormula.matches(".*\\(\\s*equal\\s+\\?\\w+\\s+\\?\\w+\\s+\\?\\w+.*")
                    || (!query
                    && !isLogicalOperator(car())
                    && !theFormula.matches("^\\(\\s*.*\\\".*\\)$")
                    && theFormula.matches("^\\(\\s*.*\\?\\w+.*\\)$")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return pass;
    }

    /**
     * ***************************************************************
     * Compare the given formula to the negated query and return whether they
     * are the same (minus the negation).
     */
    public static boolean isNegatedQuery(String query, String formula) {

        boolean result = false;

        //System.out.println("INFO in Formula.isNegatedQuery(): Comparing |" + query + "| to |" + formula + "|");
        formula = formula.trim();
        if (formula.substring(0, 4).compareTo("(not") != 0) {
            return false;
        }
        formula = formula.substring(5, formula.length() - 1);
        Formula f = new Formula();
        f.read(formula);
        result = f.equals(query);
        //System.out.print("INFO in Formula.isNegatedQuery(): ");
        //System.out.println(result);
        return result;
    }

    /**
     * ***************************************************************
     * Remove the 'holds' prefix wherever it appears.
     */
    public static String postProcess(String s) {

        s = s.replaceAll("holds_\\d__ ", "");
        s = s.replaceAll("apply_\\d__ ", "");
        return s;
    }

    /**
     * ***************************************************************
     * Format a formula for either text or HTML presentation by inserting the
     * proper hyperlink code, characters for indentation and end of line. A
     * standard LISP-style pretty printing is employed where an open parenthesis
     * triggers a new line and added indentation.
     *
     * @param hyperlink - the URL to be referenced to a hyperlinked term.
     * @param indentChars - the proper characters for indenting text.
     * @param eolChars - the proper character for end of line.
     */
    public String format(String hyperlink, String indentChars, String eolChars) {

        boolean inQuantifier = false;
        StringBuilder token = new StringBuilder();
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inToken = false;
        boolean inVariable = false;
        boolean inVarlist = false;
        boolean inComment = false;

        if (isNonEmptyString(theFormula)) {
            theFormula = theFormula.trim();
        }

        for (int i = 0; i < theFormula.length(); i++) {
            // System.out.println("INFO in format(): " + formatted.toString());
            if (!inComment) {
                if (theFormula.charAt(i) == '(' && !inQuantifier && (indentLevel != 0 || i > 1)) {
                    if (i > 0 && Character.isWhitespace(theFormula.charAt(i - 1))) {
                        //System.out.println("INFO in format(): Deleting at end of : |" + formatted.toString() + "|");
                        formatted = formatted.deleteCharAt(formatted.length() - 1);
                    }
                    formatted = formatted.append(eolChars);
                    for (int j = 0; j < indentLevel; j++) {
                        formatted = formatted.append(indentChars);
                    }
                }
                if (theFormula.charAt(i) == '(' && indentLevel == 0 && i == 0) {
                    formatted = formatted.append(theFormula.charAt(0));
                }
                if (Character.isJavaIdentifierStart(theFormula.charAt(i)) && !inToken && !inVariable) {
                    token = new StringBuilder().append(theFormula.charAt(i));
                    inToken = true;
                }
                if ((Character.isJavaIdentifierPart(theFormula.charAt(i)) || theFormula.charAt(i) == '-') && inToken) {
                    token = token.append(theFormula.charAt(i));
                }
                if (theFormula.charAt(i) == '(') {
                    if (inQuantifier) {
                        inQuantifier = false;
                        inVarlist = true;
                        token = new StringBuilder();
                    } else {
                        indentLevel++;
                    }
                }
                if (theFormula.charAt(i) == '"') {
                    inComment = true;    // The next character will be handled in the "else" clause of this primary "if"
                }
                if (theFormula.charAt(i) == ')') {
                    if (!inVarlist) {
                        indentLevel--;
                    } else {
                        inVarlist = false;
                    }
                }
                if (token.toString().compareTo("exists") == 0 || token.toString().compareTo("forall") == 0) {
                    inQuantifier = true;
                }
                if (!Character.isJavaIdentifierPart(theFormula.charAt(i)) && inVariable) {
                    inVariable = false;
                }
                if (theFormula.charAt(i) == '?' || theFormula.charAt(i) == '@') {
                    inVariable = true;
                }
                if (!(Character.isJavaIdentifierPart(theFormula.charAt(i)) || theFormula.charAt(i) == '-') && inToken) {
                    inToken = false;
                    formatted = hyperlink != null && !hyperlink.isEmpty() ? formatted.append("<a href=\"").append(hyperlink).append("&term=").append(token).append("\">").append(token).append("</a>") : formatted.append(token);
                    token = new StringBuilder();
                }
                if (!inToken && i > 0 && !(Character.isWhitespace(theFormula.charAt(i)) && theFormula.charAt(i - 1) == '(')) {
                    if (Character.isWhitespace(theFormula.charAt(i))) {
                        if (!Character.isWhitespace(theFormula.charAt(i - 1))) {
                            formatted = formatted.append(' ');
                        }
                    } else {
                        formatted = formatted.append(theFormula.charAt(i));
                    }
                }
            } else {     // In a comment
                formatted = formatted.append(theFormula.charAt(i));
                if (theFormula.charAt(i) == '"') {
                    inComment = false;
                }
            }
        }
        if (inToken) {    // A term which is outside of parenthesis, typically, a binding.
            formatted = hyperlink != null && !hyperlink.isEmpty() ? formatted.append("<a href=\"").append(hyperlink).append("&term=").append(token).append("\">").append(token).append("</a>") : formatted.append(token);
        }
        return formatted.toString();
    }

    /**
     * ***************************************************************
     * Format a formula for text presentation.
     *
     * @deprecated
     */
    public String textFormat() {

        return format("", "  ", Character.toString((char) 10));
    }

    /**
     * ***************************************************************
     * Format a formula for text presentation.
     */
    @Override
    public String toString() {

        return textFormat();
    }

    /**
     * ***************************************************************
     * Format a formula for HTML presentation.
     */
    public String htmlFormat(String html) {

        return format(html, "&nbsp;&nbsp;&nbsp;&nbsp;", "<br>\n");
    }

    /**
     * ***************************************************************
     * Format a formula for HTML presentation.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public String htmlFormat(KB kb) {
        String hostname = KBmanager.getMgr().getPref("hostname");
        if (!isNonEmptyString(hostname)) {
            hostname = "localhost";
        }
        String port = KBmanager.getMgr().getPref("port");
        if (!isNonEmptyString(port)) {
            port = "8080";
        }
        String kbHref = ("http://" + hostname + ':' + port + "/sigma/Browse.jsp?kb=" + kb.name);

        return format(kbHref, "&nbsp;&nbsp;&nbsp;&nbsp;", "<br>\n");
    }

    /**
     * ***************************************************************
     * Format a formula as a prolog statement. Note that only tuples are
     * converted properly at this time. Statements with any embedded formulas or
     * functions will be rejected with a null return.
     */
    public String toProlog() {

        if (!listP()) {
            System.out.println("INFO in Fomula.toProlog(): Not a formula: " + theFormula);
            return "";
        }
        if (empty()) {
            System.out.println("INFO in Fomula.toProlog(): Empty formula: " + theFormula);
            return "";
        }
        StringBuilder result = new StringBuilder();
        String relation = car();
        Formula f = new Formula();
        f.theFormula = cdr();
        if (!Formula.atom(relation)) {
            System.out.println("INFO in Fomula.toProlog(): Relation not an atom: " + relation);
            return "";
        }
        result.append(relation).append("('");
        while (!f.empty()) {
            String arg = f.car();
            f.theFormula = f.cdr();
            if (!Formula.atom(arg)) {
                System.out.println("INFO in Formula.toProlog(): Argument not an atom: " + arg);
                return "";
            }
            result.append(arg).append('\'');
            if (!f.empty()) {
                result.append(",'");
            } else {
                result.append(").");
            }
        }
        return result.toString();
    }

    private static final String _termMentionSuffix = "_M";
    private static final List renameExceptions = Arrays.asList("en");

    /**
     * ***************************************************************
     * Convert the logical operators and inequalities in SUO-KIF to their TPTP
     * equivalents
     *
     * @param st is the StreamTokenizer_s that contains the current token
     * @return the String that is the translated token
     */
    private static String translateWord(StreamTokenizer_s st, boolean hasArguments) {

        int translateIndex;

        String[] kifOps = {"forall", "exists", "not", "and", "or", "=>", "<=>"};
        String[] tptpOps = {"! ", "? ", "~ ", " & ", " | ", " => ", " <=> "};

        String[] kifPredicates = {"TRUE", "FALSE",
                "equal",
                "<=", "<", ">", ">=",
                "lessThanOrEqualTo", "lessThan", "greaterThan", "greaterThanOrEqualTo"};

        String[] tptpPredicates = {"$true", "$false",
                "equal",
                "lesseq", "less", "greater", "greatereq",
                "lesseq", "less", "greater", "greatereq"};

        String[] kifFunctions = {"MultiplicationFn", "DivisionFn", "AdditionFn", "SubtractionFn"};
        String[] tptpFunctions = {"times", "divide", "plus", "minus"};

        List kifRelations = new ArrayList();
        int i = 0;
        for (i = 0; i < kifPredicates.length; i++) {
            kifRelations.add(kifPredicates[i]);
        }
        for (i = 0; i < kifFunctions.length; i++) {
            kifRelations.add(kifFunctions[i]);
        }

        //DEBUG System.out.println("Translating word " + st.sval + " with hasArguments " + hasArguments);
        // Context creeps back in here whether we want it or not.  We
        // consult the KBmanager to determine if holds prefixing is
        // turned on, or not.  If it is on, then we do not want to add
        // the "mentions" suffix to relation names used as arguments
        // to other relations.
        KBmanager mgr = null;
        boolean holdsPrefixInUse = false;
        String mentionSuffix = _termMentionSuffix;
        try {
            mgr = KBmanager.getMgr();
            holdsPrefixInUse = ((mgr != null) && "yes".equalsIgnoreCase(mgr.getPref("holdsPrefix")));
            if (holdsPrefixInUse && !kifRelations.contains(st.sval)) {
                mentionSuffix = "";
            }
        } catch (Exception ex) {
            //---Be silent if there is a problem getting the KBmanager.
        }

        //----Places single quotes around strings, and replace \n by space
        if (st.ttype == 34) {
            return ('\''
                    + st.sval.replaceAll("[\n\t\r\f]", " ").replaceAll("'", "") + '\'');
        }
        //----Fix variables to have leading V_
        if (st.sval.charAt(0) == '?' || st.sval.charAt(0) == '@') {
            return ("V_" + st.sval.substring(1).replace('-', '_'));
        }
        //----Translate special predicates
        translateIndex = 0;
        while (translateIndex < kifPredicates.length
                && !st.sval.equals(kifPredicates[translateIndex])) {
            translateIndex++;
        }
        if (translateIndex < kifPredicates.length) {
            // return((hasArguments ? "$" : "") + tptpPredicates[translateIndex]);
            return (tptpPredicates[translateIndex] + (hasArguments ? "" : mentionSuffix));
        }
        //----Translate special functions
        translateIndex = 0;
        while (translateIndex < kifFunctions.length
                && !st.sval.equals(kifFunctions[translateIndex])) {
            translateIndex++;
        }
        if (translateIndex < kifFunctions.length) {
            // return((hasArguments ? "$" : "") + tptpFunctions[translateIndex]);
            return (tptpFunctions[translateIndex] + (hasArguments ? "" : mentionSuffix));
        }
        //----Translate operators
        translateIndex = 0;
        while (translateIndex < kifOps.length
                && !st.sval.equals(kifOps[translateIndex])) {
            translateIndex++;
        }
        if (translateIndex < kifOps.length) {
            return (tptpOps[translateIndex]);
        }
        //----Do nothing to numbers
        if (st.ttype == StreamTokenizer.TT_NUMBER
                || (st.sval != null && (Character.isDigit(st.sval.charAt(0))
                || (st.sval.charAt(0) == '-'
                && Character.isDigit(st.sval.charAt(1)))))) {
            return (st.sval);
            //SANITIZE return("n" + st.sval.replace('-','n').replaceAll("[.]","dot"));
        }

        //----Fix other symbols to have leading s_
        // return("s_" + st.sval.substring(1).replace('-','_'));
        String term = st.sval;

        //----Add a "mention" suffix to relation names that occur as arguments
        //----to other relations.
        if (!hasArguments) {
            if ((Character.isLowerCase(st.sval.charAt(0)) && !st.sval.endsWith(mentionSuffix))
                    || st.sval.endsWith("Fn")) {
                if (!renameExceptions.contains(term)) {
                    term += mentionSuffix;
                }
            }
        }
        return ("s_" + term.replace('-', '_'));
    }

    /**
     * ***************************************************************
     * @param st is the StreamTokenizer_s that contains the current token for
     * which the arity is desired
     * @return the integer arity of the given logical operate
     */
    private static int operatorArity(StreamTokenizer_s st) {

        int translateIndex;
        String[] kifOps = {"forall", "exists", "not", "and", "or", "=>", "<=>"};

        translateIndex = 0;
        while (translateIndex < kifOps.length
                && !st.sval.equals(kifOps[translateIndex])) {
            translateIndex++;
        }
        if (translateIndex <= 2) {
            return (1);
        } else {
            return translateIndex < kifOps.length ? 2 : -1;
        }
    }

    /**
     * ***************************************************************
     */
    private static void incrementTOS(Stack countStack) {

        countStack.push((Integer) countStack.pop() + 1);
    }

    /**
     * ***************************************************************
     * Add the current token, if a variable, to the list of variables
     *
     * @param variables is the list of variables
     */
    private static void addVariable(StreamTokenizer_s st, Vector variables) {

        String tptpVariable;

        if (st.sval.charAt(0) == '?' || st.sval.charAt(0) == '@') {
            tptpVariable = translateWord(st, false);
            if (!variables.contains(tptpVariable)) {
                variables.add(tptpVariable);
            }
        }
    }

    /**
     * ***************************************************************
     * Parse a single formula into TPTP format
     */
    public static String tptpParseSUOKIFString(String suoString) {

        StreamTokenizer_s st = null;
        String translatedFormula = null;

        try {
            int parenLevel;
            boolean inQuantifierVars;
            boolean lastWasOpen;
            boolean inHOL;
            int inHOLCount;
            Stack operatorStack = new Stack();
            Stack countStack = new Stack();
            Vector quantifiedVariables = new Vector();
            Vector allVariables = new Vector();
            int index;
            int arity;
            String quantification;

            StringBuilder tptpFormula = new StringBuilder(suoString.length());

            parenLevel = 0;
            countStack.push(0);
            lastWasOpen = false;
            inQuantifierVars = false;
            inHOL = false;
            inHOLCount = 0;

            st = new StreamTokenizer_s(new StringReader(suoString));
            KIF.setupStreamTokenizer(st);

            do {
                st.nextToken();
                //----Open bracket
                //noinspection IfStatementWithTooManyBranches
                if (st.ttype == 40) {
                    if (lastWasOpen) {    //----Should not have ((in KIF
                        System.out.println("ERROR: Double open bracket at " + tptpFormula);
                        throw new ParseException("Parsing error in " + suoString, 0);
                    }
                    //----Track nesting of ()s for hol__, so I know when to close the '
                    if (inHOL) {
                        inHOLCount++;
                    }
                    lastWasOpen = true;
                    parenLevel++;
                    //----Operators
                } else if (st.ttype == StreamTokenizer.TT_WORD
                        && (arity = operatorArity(st)) > 0) {
                    //----Operators must be preceded by a (
                    if (!lastWasOpen) {
                        System.out.println("ERROR: Missing ( before "
                                + st.sval + " at " + tptpFormula);
                        return (null);
                    }
                    //----This is the start of a new term - put in the infix operate if not the
                    //----first term for this operate
                    if ((Integer) (countStack.peek()) > 0) {
                        tptpFormula.append((String) operatorStack.peek());
                    }
                    //----If this is the start of a hol__ situation, quote it all
                    if (inHOL && inHOLCount == 1) {
                        tptpFormula.append('\'');
                    }
                    //----()s around all operate expressions
                    tptpFormula.append('(');
                    //----Output unary as prefix
                    if (arity == 1) {
                        tptpFormula.append(translateWord(st, false));
                        //----Note the new operate (dummy) with 0 operands so far
                        countStack.push(0);
                        operatorStack.push(",");
                        //----Check if the next thing will be the quantified variables
                        if ("forall".equals(st.sval) || "exists".equals(st.sval)) {
                            inQuantifierVars = true;
                        }
                        //----Binary operate
                    } else if (arity == 2) {
                        //----Note the new operate with 0 operands so far
                        countStack.push(0);
                        operatorStack.push(translateWord(st, false));
                    }
                    lastWasOpen = false;
                    //----Back tick - token translation to TPTP. Everything gets ''ed 
                } else if (st.ttype == 96) {
                    //----They may be nested - only start the situation at the outer one
                    if (!inHOL) {
                        inHOL = true;
                        inHOLCount = 0;
                    }
                    //----Quote - Term token translation to TPTP
                } else if (st.ttype == 34
                        || st.ttype == StreamTokenizer.TT_NUMBER
                        || (st.sval != null && (Character.isDigit(st.sval.charAt(0))))
                        || st.ttype == StreamTokenizer.TT_WORD) {
                    //----Start of a predicate or variable list
                    if (lastWasOpen) {
                        //----Variable list
                        if (inQuantifierVars) {
                            tptpFormula.append('[');
                            tptpFormula.append(translateWord(st, false));
                            incrementTOS(countStack);
                            //----Predicate
                        } else {
                            //----This is the start of a new term - put in the infix operate if not the
                            //----first term for this operate
                            if ((Integer) (countStack.peek()) > 0) {
                                tptpFormula.append((String) operatorStack.peek());
                            }
                            //----If this is the start of a hol__ situation, quote it all
                            if (inHOL && inHOLCount == 1) {
                                tptpFormula.append('\'');
                            }
                            //----Predicate or function and (
                            tptpFormula.append(translateWord(st, true));
                            tptpFormula.append('(');
                            //----Note the , for between arguments with 0 arguments so far
                            countStack.push(0);
                            operatorStack.push(",");
                        }
                        //----Argument or quantified variable
                    } else {
                        //----This is the start of a new term - put in the infix operate if not the
                        //----first term for this operate
                        if ((Integer) (countStack.peek()) > 0) {
                            tptpFormula.append((String) operatorStack.peek());
                        }
                        //----Output the word
                        tptpFormula.append(translateWord(st, false));
                        //----Increment counter for this level
                        incrementTOS(countStack);
                    }
                    //----Collect variables that are used and quantified
                    if (isNonEmptyString(st.sval) && (st.sval.charAt(0) == '?' || st.sval.charAt(0) == '@')) {
                        if (inQuantifierVars) {
                            addVariable(st, quantifiedVariables);
                        } else {
                            addVariable(st, allVariables);
                        }
                    }
                    lastWasOpen = false;
                    //----Close bracket.
                } else if (st.ttype == 41) {
                    //----Track nesting of ()s for hol__, so I know when to close the '
                    if (inHOL) {
                        inHOLCount--;
                    }
                    //----End of quantified variable list
                    if (inQuantifierVars) {
                        //----Fake restarting the argument list because the quantified variable list
                        //----does not use the operate from the surrounding expression
                        countStack.pop();
                        countStack.push(0);
                        tptpFormula.append("] : ");
                        inQuantifierVars = false;
                        //----End of predicate or operate list
                    } else {
                        //----Pop off the stacks to reveal the next outer layer
                        countStack.pop();
                        operatorStack.pop();
                        //----Close the expression
                        tptpFormula.append(')');
                        //----If this closes a HOL expression, close the '
                        if (inHOL && inHOLCount == 0) {
                            tptpFormula.append('\'');
                            inHOL = false;
                        }
                        //----Note that another expression has been completed
                        incrementTOS(countStack);
                    }
                    lastWasOpen = false;

                    parenLevel--;
                    //----End of the statement being processed. Universally quantify free variables
                    if (parenLevel == 0) {
                        //findFreeVariables(allVariables,quantifiedVariables);
                        allVariables.removeAll(quantifiedVariables);
                        if (!allVariables.isEmpty()) {
                            quantification = "! [";
                            for (index = 0; index < allVariables.size(); index++) {
                                if (index > 0) {
                                    quantification += ",";
                                }
                                quantification += (String) allVariables.elementAt(index);
                            }
                            quantification += "] : ";
                            tptpFormula.insert(0, "( " + quantification);
                            tptpFormula.append(" )");
                        }
                        if (translatedFormula == null) {
                            translatedFormula = "( " + tptpFormula + " )";
                        } else {
                            translatedFormula += "& ( " + tptpFormula + " )";
                        }
                        if ((Integer) (countStack.pop()) != 1) {
                            System.out.println(
                                    "Error in KIF.tptpParse(): Not one formula");
                        }
                    } else if (parenLevel < 0) {
                        System.out.print("ERROR: Extra closing bracket at "
                                + tptpFormula);
                        throw new ParseException("Parsing error in " + suoString, 0);
                    }
                } else if (st.ttype != StreamTokenizer.TT_EOF) {
                    System.out.println("ERROR: Illegal character '"
                            + (char) st.ttype + "' at " + tptpFormula);
                    throw new ParseException("Parsing error in " + suoString, 0);
                }
            } while (st.ttype != StreamTokenizer.TT_EOF);

            //----Bare word like $false didn't get done by a closing)
            if (translatedFormula == null) {
                translatedFormula = tptpFormula.toString();
            }
        } catch (Exception ex2) {
            System.out.println("Error in Formula.tptpParseSUOKIFString(" + suoString + ')');
            System.out.println("  st.sval == " + st.sval);
            System.out.println("  message == " + ex2.getMessage());
            ex2.printStackTrace();
        }
        return translatedFormula;
    }

    /**
     * ***************************************************************
     * Parse formulae into TPTP format
     */
    public void tptpParse(boolean query, KB kb, List preProcessedForms) {

        if (kb == null) {
            kb = new KB("", KBmanager.getMgr().getPref("kbDir"));
        }

        List processed;
        processed = preProcessedForms != null ? preProcessedForms : preProcess(query, kb);

        //     System.out.println("INFO in Formula.tptpParse(" + this.theFormula + ")");
        //     System.out.println("  processed == " + processed);
        if (processed != null) {
            clearTheTptpFormulas();
            Iterator g = processed.iterator();

            //----Performs function on each current processed axiom
            Formula f;
            while (g.hasNext()) {
                f = (Formula) g.next();
                getTheTptpFormulas().add(tptpParseSUOKIFString(f.theFormula));
            }

            //         System.out.println("INFO in Formula.tptpParse(" + this.theFormula + ")");
            //         System.out.println("  theTptpFormulas == " + this.getTheTptpFormulas());
        }
    }

    /**
     * ***************************************************************
     * Parse formulae into TPTP format
     */
    public void tptpParse(boolean query, KB kb) throws ParseException, IOException {
        tptpParse(query, kb, null);
    }

    ///////////////////////////////////////////////////////
    /*
     START of instantiatePredVars(KB kb) implementation.
     */
    ///////////////////////////////////////////////////////
    /**
     * ***************************************************************
     * Returns an ArrayList of the Formulas that result from replacing all arg0
     * predicate variables in the input Formula with predicate names.
     *
     * @param kb A KB that is used for processing the Formula.
     *
     * @return An ArrayList of Formulas, or an empty ArrayList if no
     * instantiations can be generated.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public ArrayList instantiatePredVars(KB kb) {

        // System.out.println("ENTER instantiatePredVars(" + this + ")");
        ArrayList ans = new ArrayList();

        try {
            if (listP()) {
                String arg0 = getArgument(0);

                // First we do some checks to see if it is worth
                // processing the formula.
                if (isLogicalOperator(arg0)
                        && theFormula.matches(".*\\(\\s*\\?.*")) {

                    // Get all query lits for all pred vars, indexed by
                    // var.
                    List indexedQueryLits = prepareIndexedQueryLiterals(kb);

                    if (indexedQueryLits == null) {
                        ans.add(this);
                    } else {

                        List substForms = new ArrayList();
                        List varQueryTuples = null;
                        List substTuples = null;
                        List litsToRemove = null;

                        // First, gather all substitutions.
                        for (Object indexedQueryLit : indexedQueryLits) {
                            varQueryTuples = (List) indexedQueryLit;
                            substTuples = computeSubstitutionTuples(kb, varQueryTuples);
                            if ((substTuples instanceof List) && !(substTuples.isEmpty())) {
                                if (substForms.isEmpty()) {
                                    substForms.add(substTuples);
                                } else {
                                    int stSize = substTuples.size();
                                    int iSize = -1;
                                    int sfSize = substForms.size();
                                    int sfLast = (sfSize - 1);
                                    for (int i = 0; i < sfSize; i++) {
                                        iSize = ((Collection) substForms.get(i)).size();
                                        if (stSize < iSize) {
                                            substForms.add(i, substTuples);
                                            break;
                                        }
                                        if (i == sfLast) {
                                            substForms.add(substTuples);
                                        }
                                    }
                                }
                            }
                        }

                        if (!substForms.isEmpty()) {

                            // Try to simplify the Formula.
                            Formula f = this;
                            Iterator it1 = substForms.iterator();
                            Iterator it2 = null;
                            while (it1.hasNext()) {
                                substTuples = (List) it1.next();
                                litsToRemove = (List) substTuples.get(0);
                                it2 = litsToRemove.iterator();
                                while (it2.hasNext()) {
                                    List lit = (List) it2.next();
                                    f = f.maybeRemoveMatchingLits(lit);
                                }
                            }

                            // Now generate pred var instantions from the
                            // possibly simplified formula.
                            List templates = new ArrayList();
                            templates.add(f.theFormula);
                            Set accumulator = new HashSet();

                            String template = null;
                            String var = null;
                            String term = null;
                            ArrayList quantVars = null;
                            int i = 0;

                            // Iterate over all var plus query lits forms, getting
                            // a list of substitution literals.
                            it1 = substForms.iterator();
                            while (it1.hasNext()) {
                                substTuples = (List) it1.next();

                                if ((substTuples instanceof List) && !(substTuples.isEmpty())) {

                                    // Iterate over all ground lits ...
                                    // Remove litsToRemove, which we have
                                    // already used above.
                                    litsToRemove = (List) substTuples.remove(0);

                                    // Remove and hold the tuple that
                                    // indicates the variable substitution
                                    // pattern.
                                    List varTuple = (List) substTuples.remove(0);

                                    it2 = substTuples.iterator();
                                    while (it2.hasNext()) {
                                        List groundLit = (List) it2.next();

                                        // Iterate over all formula templates,
                                        // substituting terms from each ground lit
                                        // for vars in the template.
                                        for (Object template1 : templates) {
                                            template = (String) template1;
                                            quantVars = collectQuantifiedVariables(template);
                                            for (i = 0; i < varTuple.size(); i++) {
                                                var = (String) varTuple.get(i);
                                                if (isVariable(var)) {

                                                    term = (String) groundLit.get(i);

                                                    // Don't replace variables that
                                                    // are explicitly quantified.
                                                    if (!quantVars.contains(var)) {

                                                        List patternStrings
                                                                = Arrays.asList("(\\W*\\()(\\s*holds\\s+\\" + var + ")(\\W+)",
                                                                // "(\\W*\\()(\\s*\\" + var + ")(\\W+)",
                                                                "(\\W*)(\\" + var + ")(\\W+)"
                                                        );
                                                        List patterns = new ArrayList();
                                                        for (Object patternString : patternStrings) {
                                                            patterns.add(Pattern.compile((String) patternString));
                                                        }
                                                        Pattern p = null;
                                                        Matcher m = null;
                                                        for (Object pattern : patterns) {
                                                            p = (Pattern) pattern;
                                                            m = p.matcher(template);
                                                            template = m.replaceAll("$1" + term + "$3");
                                                        }
                                                    }
                                                }
                                            }
                                            accumulator.add(template);
                                        }
                                    }
                                    templates.clear();
                                    templates.addAll(accumulator);
                                    accumulator.clear();
                                }
                            }
                            ans.addAll(KB.stringsToFormulas(templates));
                        }
                        if (ans.isEmpty()) {
                            ans.add("reject");
                        }
                    }

                    //         System.out.println("INFO in instantiatePredVars(" + this + ")");
                    //         System.out.println("  -> " 
                    //                     + ((ans.size() > 20) 
                    //                     ? (ans.subList(0, 5) + " and " + (ans.size() - 5) + " more ...")
                    //                     : ans));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Returns the number of SUO-KIF variables (only ? variables, not
     *
     * @ROW variables) in the input query literal.
     *
     * @param queryLiteral A List representing a Formula.
     *
     * @return An int.
     */
    private static int getVarCount(List queryLiteral) {
        int ans = 0;
        if (queryLiteral instanceof List) {
            String term = null;
            for (Object aQueryLiteral : queryLiteral) {
                term = (String) aQueryLiteral;
                if (term.length() > 0 && term.charAt(0) == '?') {
                    ans++;
                }
            }
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method returns an ArrayList of query answer literals. The first
     * element is an ArrayList of query literals that might be used to simplify
     * the Formula to be instantiated. The second element is the query literal
     * (ArrayList) that will be used as a template for doing the variable
     * substitutions. All subsequent elements are ground literals (ArrayLists).
     *
     * @param kb A KB to query for answers.
     *
     * @param queryLits A List of query literals. The first item in the list
     * will be a SUO-KIF variable (String), which indexes the list. Each
     * subsequent item is a query literal (List).
     *
     * @return An ArrayList of literals, or an empty ArrayList of no query
     * answers can be found.
     */
    private static ArrayList computeSubstitutionTuples(KB kb, List queryLits) {

        // System.out.println("ENTER computeSubstitutionTuples(" + kb + ", " + queryLits + ")");
        ArrayList result = new ArrayList();
        try {
            if ((kb instanceof KB)
                    && (queryLits instanceof List)
                    && !(queryLits.isEmpty())) {

                String idxVar = (String) queryLits.get(0);

                int i = 0;
                int j = 0;

                // Sort the query lits by number of variables.
                ArrayList sortedQLits = new ArrayList();
                for (i = 1; i < queryLits.size(); i++) {
                    ArrayList ql = (ArrayList) queryLits.get(i);
                    int varCount = getVarCount(ql);
                    boolean added = false;
                    for (j = 0; j < sortedQLits.size(); j++) {
                        ArrayList ql2 = (ArrayList) sortedQLits.get(j);
                        if (varCount > getVarCount(ql2)) {
                            sortedQLits.add(j, ql);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        sortedQLits.add(ql);
                    }
                }

                // Literals that will be used to try to simplify the
                // formula before pred var instantiation.
                ArrayList simplificationLits = new ArrayList();

                // The literal that will serve as the pattern for
                // extracting var replacement terms from answer
                // literals.
                ArrayList keyLit = null;

                // The list of answer literals retrieved using the
                // query lits, possibly built up via a sequence of
                // multiple queries.
                ArrayList answers = null;

                Set working = new HashSet();
                ArrayList accumulator = null;
                ArrayList ql = null;

                boolean satisfiable = true;

                // The first query lit for which we get an answer is
                // the key lit.
                for (i = 0; (i < sortedQLits.size()) && satisfiable; i++) {
                    ql = (ArrayList) sortedQLits.get(i);
                    accumulator = kb.askWithLiteral(ql);
                    satisfiable = (!((accumulator == null) || accumulator.isEmpty()));

                    // System.out.println(ql + " accumulator == " + accumulator);
                    if (satisfiable) {

                        simplificationLits.add(ql);

                        if (keyLit == null) {
                            keyLit = ql;
                            answers = KB.formulasToArrayLists(accumulator);
                        } else {  // if (accumulator.size() < answers.size()) {
                            accumulator = KB.formulasToArrayLists(accumulator);

                            // Winnow the answers list.
                            working.clear();
                            ArrayList ql2 = null;
                            int varPos = ql.indexOf(idxVar);
                            for (j = 0; j < accumulator.size(); j++) {
                                ql2 = (ArrayList) accumulator.get(j);
                                working.add(ql2.get(varPos));
                            }
                            accumulator.clear();
                            accumulator.addAll(answers);
                            answers.clear();
                            varPos = keyLit.indexOf(idxVar);
                            for (j = 0; j < accumulator.size(); j++) {
                                ql2 = (ArrayList) accumulator.get(j);
                                if (working.contains(ql2.get(varPos))) {
                                    answers.add(ql2);
                                }
                            }
                        }
                    }
                }
                if (satisfiable && (keyLit != null)) {
                    result.add(simplificationLits);
                    result.add(keyLit);
                    result.addAll(answers);
                } else {
                    result.clear();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //     System.out.println("EXIT computeSubstitutionTuples(" + kb + ", " + queryLits + ")");
        //     System.out.println("  -> " + result);
        return result;
    }

    /**
     * ***************************************************************
     * This method returns an ArrayList in which each element is another
     * ArrayList. The head of each element is a variable. The subsequent objects
     * in each element are query literals (ArrayLists).
     *
     * @param kb The KB used for computations involving assertions.
     *
     * @return An ArrayList, or null if the input formula contains no predicate
     * variables.
     */
    private ArrayList prepareIndexedQueryLiterals(KB kb) {

        // System.out.println("ENTER prepareIndexedQueryLiterals(" + this + ")");
        ArrayList ans = null;
        HashMap varsWithTypes = gatherPredVars(kb);
        // System.out.println("vars == " + vars);

        if (!varsWithTypes.isEmpty()) {

            String yOrN = (String) varsWithTypes.get("arg0");

            // If the formula doesn't contain any arg0 pred vars, do
            // nothing.
            if (isNonEmptyString(yOrN) && "yes".equalsIgnoreCase(yOrN)) {

                ans = new ArrayList();

                // Try to simplify the formula.
                ArrayList varWithTypes = null;
                ArrayList indexedQueryLits = null;

                String var = null;
                for (Object o : varsWithTypes.keySet()) {
                    var = (String) o;
                    if (isVariable(var)) {
                        varWithTypes = (ArrayList) varsWithTypes.get(var);
                        indexedQueryLits = gatherPredVarQueryLits(kb, varWithTypes);
                        if (!indexedQueryLits.isEmpty()) {
                            ans.add(indexedQueryLits);
                        }
                    }
                }
            }
        }

        //     System.out.println("EXIT prepareIndexedQueryLiterals(" + this + ")");
        //     System.out.println("  -> " + ans);
        return ans;
    }

    /**
     * ***************************************************************
     * This method collects and returns all predicate variables that occur in
     * the Formula.
     *
     * @param kb The KB to be used for computations involving assertions.
     *
     * @return a HashMap in which the keys are predicate variables, and the
     * values are ArrayLists containing one or more class names that indicate
     * the type constraints tha apply to the variable. If no predicate variables
     * can be gathered from the Formula, the HashMap will be empty. The first
     * element in each ArrayList is the variable itself. Subsequent elements are
     * the types of the variable. If no types for the variable can be
     * determined, the ArrayList will contain just the variable.
     *
     */
    protected HashMap gatherPredVars(KB kb) {

        // System.out.println("ENTER gatherPredVars(" +  this + ")");
        HashMap ans = new HashMap();
        try {
            if (isNonEmptyString(theFormula)) {
                List accumulator = new ArrayList();
                List working = new ArrayList();
                if (listP() && !(empty())) {
                    accumulator.add(this);
                }
                while (!accumulator.isEmpty()) {
                    working.clear();
                    working.addAll(accumulator);
                    accumulator.clear();
                    Formula f = null;
                    String arg0 = null;
                    String arg2 = null;
                    ArrayList vals = null;
                    int len = -1;
                    for (Object aWorking : working) {
                        f = (Formula) aWorking;
                        len = f.listLength();
                        arg0 = f.getArgument(0);
                        //noinspection IfStatementWithTooManyBranches
                        if (isQuantifier(arg0)
                                || "holdsDuring".equals(arg0)
                                || "KappaFn".equals(arg0)) {
                            if (len > 2) {
                                arg2 = f.getArgument(2);

                                Formula newF = new Formula();
                                newF.read(arg2);
                                if (f.listP() && !f.empty()) {
                                    accumulator.add(newF);
                                }
                            } else {
                                System.out.println("INFO in Formula.gatherPredVars(" + this + ')');
                                System.out.println("Is this malformed? " + f.theFormula);
                            }
                        } else if ("holds".equals(arg0)) {
                            accumulator.add(f.cdrAsFormula());
                        } else if (isVariable(arg0)) {
                            vals = (ArrayList) ans.get(arg0);
                            if (vals == null) {
                                vals = new ArrayList();
                                ans.put(arg0, vals);
                                vals.add(arg0);
                            }
                            // Record the fact that we found at least
                            // one variable in the arg0 position.
                            ans.put("arg0", "yes");
                        } else {
                            String argN = null;
                            Formula argF = null;
                            String argType = null;
                            boolean[] signature = kb.getRelnArgSignature(arg0);
                            for (int j = 1; j < len; j++) {
                                argN = f.getArgument(j);
                                if ((signature != null) && signature[j] && isVariable(argN)) {
                                    vals = (ArrayList) ans.get(argN);
                                    if (vals == null) {
                                        vals = new ArrayList();
                                        ans.put(argN, vals);
                                        vals.add(argN);
                                    }
                                    argType = kb.getArgType(arg0, j);
                                    if (!((argType == null) || vals.contains(argType))) {
                                        vals.add(argType);
                                    }
                                } else {
                                    argF = new Formula();
                                    argF.read(argN);
                                    if (argF.listP() && !(argF.empty())) {
                                        accumulator.add(argF);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // System.out.println("INFO in gatherPredVars(" +  this + ")");
        // System.out.println("  -> " + ans);
        return ans;
    }

    /**
     * ***************************************************************
     * This method tries to remove literals from the Formula that match litArr.
     * It is intended for use in simplification of this Formula during predicate
     * variable instantiation, and so only attempts removals that are likely to
     * be safe in that context.
     *
     * @param litArr A List object representing a SUO-KIF atomic formula.
     *
     * @return A new Formula with at least some occurrences of litF removed, or
     * the original Formula if no removals are possible.
     */
    private Formula maybeRemoveMatchingLits(List litArr) {
        Formula f = KB.literalListToFormula(litArr);
        return maybeRemoveMatchingLits(f);
    }

    /**
     * ***************************************************************
     * This method tries to remove literals from the Formula that match litF. It
     * is intended for use in simplification of this Formula during predicate
     * variable instantiation, and so only attempts removals that are likely to
     * be safe in that context.
     *
     * @param litF A SUO-KIF literal (atomic Formula).
     *
     * @return A new Formula with at least some occurrences of litF removed, or
     * the original Formula if no removals are possible.
     */
    private Formula maybeRemoveMatchingLits(Formula litF) {

        // System.out.println("ENTER maybeRemoveMatchingLits(" + litF + ") ");
        Formula result = null;
        try {
            Formula f = this;
            if (f.listP() && !f.empty()) {
                StringBuilder litBuf = new StringBuilder();
                String arg0 = f.car();
                //noinspection IfStatementWithTooManyBranches
                if (f.isRule() // arg0.equals("<=>") ||
                        // arg0.equals("=>") 
                        ) {
                    String arg1 = f.getArgument(1);
                    String arg2 = f.getArgument(2);
                    if (arg1.equals(litF.theFormula)) {
                        Formula arg2F = new Formula();
                        arg2F.read(arg2);
                        litBuf.append(arg2F.maybeRemoveMatchingLits(litF).theFormula);
                    } else if (arg2.equals(litF.theFormula)) {
                        Formula arg1F = new Formula();
                        arg1F.read(arg1);
                        litBuf.append(arg1F.maybeRemoveMatchingLits(litF).theFormula);
                    } else {
                        Formula arg1F = new Formula();
                        arg1F.read(arg1);
                        Formula arg2F = new Formula();
                        arg2F.read(arg2);
                        litBuf.append('(').append(arg0).append(' ').append(arg1F.maybeRemoveMatchingLits(litF).theFormula).append(' ').append(arg2F.maybeRemoveMatchingLits(litF).theFormula).append(')');
                    }
                } else if (isQuantifier(arg0)
                        || "holdsDuring".equals(arg0)
                        || "KappaFn".equals(arg0)) {
                    Formula arg2F = new Formula();
                    arg2F.read(f.caddr());
                    litBuf.append('(').append(arg0).append(' ').append(f.cadr()).append(' ').append(arg2F.maybeRemoveMatchingLits(litF).theFormula).append(')');
                } else if (isCommutative(arg0)) {
                    List litArr = f.literalToArrayList();
                    if (litArr.contains(litF.theFormula)) {
                        litArr.remove(litF.theFormula);
                    }
                    String args = "";
                    int len = litArr.size();
                    for (int i = 1; i < len; i++) {
                        Formula argF = new Formula();
                        argF.read((String) litArr.get(i));
                        args += (' ' + argF.maybeRemoveMatchingLits(litF).theFormula);
                    }
                    args = len > 2 ? '(' + arg0 + args + ')' : args.trim();
                    litBuf.append(args);
                } else {
                    litBuf.append(f.theFormula);
                }
                Formula newF = new Formula();
                newF.read(litBuf.toString());
                result = newF;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (result == null) {
            result = this;
        }

        // System.out.println("EXIT maybeRemoveMatchingLits(" + litF + ")");
        // System.out.println("  -> " + result);
        return result;
    }

    /**
     * ***************************************************************
     * Return true if the input predicate can take relation names a arguments,
     * else returns false.
     */
    private boolean isPossibleRelnArgQueryPred(KB kb, String predicate) {
        return (isNonEmptyString(predicate)
                && ((kb.getRelnArgSignature(predicate) != null)
                || "instance".equals(predicate)));
    }

    /**
     * ***************************************************************
     * This method collects and returns literals likely to be of use as
     * templates for retrieving predicates to be substituted for var.
     *
     * @param varWithTypes A List containing a variable followed, optionally, by
     * class names indicating the type of the variable.
     *
     * @return An ArrayList of literals (Lists) with var at the head. The first
     * element of the ArrayList is the variable (String). Subsequent elements
     * are Lists corresponding to SUO-KIF formulas, which will be used as query
     * templates.
     *
     */
    private ArrayList gatherPredVarQueryLits(KB kb, List varWithTypes) {

        // System.out.println("ENTER gatherPredVarQueryLits(" +  this + ", " + kb + ", " + varWithTypes + ")");
        ArrayList ans = new ArrayList();
        try {
            String var = (String) varWithTypes.get(0);
            Set added = new HashSet();

            // Get the clauses for this Formula.
            StringBuilder litBuf = new StringBuilder();
            List clauses = getClauses();
            Map varMap = getVarMap();
            String qlString = null;
            ArrayList queryLit = null;

            if (clauses != null) {
                Iterator it2 = null;
                Formula f = null;
                for (Object clause1 : clauses) {
                    List clause = (List) clause1;
                    List negLits = (List) clause.get(0);
                    // List poslits = (List) clause.get(1);

                    if (!negLits.isEmpty()) {
                        int flen = -1;
                        String arg = null;
                        String arg0 = null;
                        String term = null;
                        String origVar = null;
                        boolean working = true;
                        for (int ci = 0;
                             ci < 1;
                            // (ci < clause.size()) && ans.isEmpty() ;
                             ci++) {
                            // Try the neglits first.  Then try the poslits only
                            // if there still are no resuls.
                            it2 = ((Iterable) (clause.get(ci))).iterator();
                            while (it2.hasNext()) {
                                f = (Formula) it2.next();
                                if (f.theFormula.matches(".*SkFn\\s+\\d+.*") || f.theFormula.matches(".*Sk\\d+.*")) {
                                    continue;
                                }
                                flen = f.listLength();
                                arg0 = f.getArgument(0);

                                // System.out.println("  var == " + var);
                                // System.out.println("  f.theFormula == " + f.theFormula);
                                // System.out.println("  arg0 == " + arg0);
                                if (isNonEmptyString(arg0)) {

                                    // If arg0 corresponds to var, then var
                                    // has to be of type Predicate, not of
                                    // types Function or List.
                                    if (isVariable(arg0)) {
                                        origVar = getOriginalVar(arg0, varMap);
                                        if (origVar.equals(var) && !varWithTypes.contains("Predicate")) {
                                            varWithTypes.add("Predicate");
                                        }
                                    } else {
                                        queryLit = new ArrayList();
                                        queryLit.add(arg0);
                                        boolean foundVar = false;
                                        for (int i = 1; i < flen; i++) {
                                            arg = f.getArgument(i);
                                            if (!listP(arg)) {
                                                if (isVariable(arg)) {
                                                    arg = getOriginalVar(arg, varMap);
                                                    if (arg.equals(var)) {
                                                        foundVar = true;
                                                    }
                                                }
                                                queryLit.add(arg);
                                            }
                                        }
                                        if (queryLit.size() != flen) {
                                            continue;
                                        }

                                        // If the literal does not start with a
                                        // variable or with "holds" and does not
                                        // contain Skolem terms, but does contain
                                        // the variable in which we're interested,
                                        // it is probably suitable as a query
                                        // template, or might serve as a starting
                                        // place.  Use it, or a literal obtained
                                        // with it.
                                        if (isPossibleRelnArgQueryPred(kb, arg0) && foundVar) {
                                            // || arg0.equals("disjoint")) 
                                            term = "";
                                            if (queryLit.size() > 2) {
                                                term = (String) queryLit.get(2);
                                            }
                                            if (!("instance".equals(arg0) && "Relation".equals(term))) {
                                                if (!added.contains(queryLit.toString().intern())) {
                                                    ans.add(queryLit);
                                                    added.add(queryLit.toString().intern());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If we have previously collected type info for the variable,
            // convert that info query lits now.
            String argType = null;
            int vtLen = varWithTypes.size();
            if (vtLen > 1) {
                for (int j = 1; j < vtLen; j++) {
                    argType = (String) varWithTypes.get(j);
                    if (!"Relation".equals(argType)) {
                        queryLit = new ArrayList();
                        queryLit.add("instance");
                        queryLit.add(var);
                        queryLit.add(argType);
                        qlString = queryLit.toString().intern();
                        if (!added.contains(qlString)) {
                            ans.add(queryLit);
                            added.add(qlString);
                        }
                    }
                }
            }

            // Add the variable to the front of the answer list, if it contains
            // any query literals.
            if (!ans.isEmpty()) {
                ans.add(0, var);
            }

            //         System.out.println("EXIT gatherPredVarQueryLits(" + this + ", " + kb + ", " + varWithTypes + ")");
            //         System.out.println("  -> " + ans);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    ///////////////////////////////////////////////////////
    /*
     END of instantiatePredVars(KB kb) implementation.
     */
    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////
    /*
     START of clausify() implementation.

     The code in the section below implements an algorithm for
     translating SUO-KIF expressions to clausal form.  The public
     methods are:

     public Formula clausify()
     public ArrayList clausifyWithRenameInfo()
     public ArrayList toNegAndPosLitsWithRenameInfo()
     */
    ///////////////////////////////////////////////////////
    /**
     * ***************************************************************
     * This method converts the SUO-KIF Formula to a version of clausal
     * (resolution, conjunctive normal) form with Skolem functions, following
     * the procedure described in Logical Foundations of Artificial
     * Intelligence, by Michael Genesereth and Nils Nilsson, 1987, pp. 63-66.
     *
     * <P>
     * A literal is an atomic formula. (However, because SUO-KIF allows
     * higher-order formulas, not all SUO-KIF literals are really atomic.) A
     * clause is a disjunction of literals that share no variable names with
     * literals in any other clause in the KB. Note that even a relatively
     * simple SUO-KIF formula might generate multiple clauses. In such cases,
     * the Formula returned will be a conjunction of clauses. (A KB is
     * understood to be a conjunction of clauses.) In all cases, the Formula
     * returned by this method should be a well-formed SUO-KIF Formula if the
     * input (original formula) is a well-formed SUO-KIF Formula. Rendering the
     * output in true (LISPy) clausal form would require an additional step, the
     * removal of all commutative logical operators, and the result would not be
     * well-formed SUO-KIF.</P>
     *
     * @see clausifyWithRenameInfo()
     * @ @see toNegAndPosLitsWithRenameInfo()
     *
     * @return A SUO-KIF Formula in clausal form, or null if a clausal form
     * cannot be generated.
     */
    public Formula clausify() {
        Formula ans = null;
        try {
            ans = equivalencesOut();
            ans = ans.implicationsOut();
            ans = ans.negationsIn();
            ans = ans.renameVariables();
            ans = ans.existentialsOut();
            ans = ans.universalsOut();
            ans = ans.disjunctionsIn();
            ans = ans.standardizeApart();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method converts the SUO-KIF Formula to a version of clausal
     * (resolution, conjunctive normal) form with Skolem functions, following
     * the procedure described in Logical Foundations of Artificial
     * Intelligence, by Michael Genesereth and Nils Nilsson, 1987, pp. 63-66.
     *
     * <P>
     * It returns an ArrayList that contains three items: The new clausal-form
     * Formula, the original (input) SUO-KIF Formula, and a Map containing a
     * graph of all the variable substitions done during the conversion to
     * clausal form. This Map makes it possible to retrieve the correspondence
     * between the variables in the clausal form and the variables in the
     * original Formula.</P>
     *
     * @see clausify()
     * @ @see toNegAndPosLitsWithRenameInfo()
     *
     * @return A three-element ArrayList, [<Formula>, <Formula>,
     * <Map>], in which some elements might be null if a clausal form cannot be
     * generated.
     */
    public ArrayList clausifyWithRenameInfo() {
        ArrayList result = new ArrayList();
        Formula ans = null;
        try {
            HashMap topLevelVars = new HashMap();
            HashMap scopedRenames = new HashMap();
            HashMap allRenames = new HashMap();
            HashMap standardizedRenames = new HashMap();
            ans = equivalencesOut();
            ans = ans.implicationsOut();
            ans = ans.negationsIn();
            ans = ans.renameVariables(topLevelVars, scopedRenames, allRenames);
            ans = ans.existentialsOut();
            ans = ans.universalsOut();
            ans = ans.disjunctionsIn();
            ans = ans.standardizeApart(standardizedRenames);
            allRenames.putAll(standardizedRenames);
            result.add(ans);
            result.add(this);
            result.add(allRenames);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * ***************************************************************
     * This method converts the SUO-KIF Formula to an ArrayList of clauses. Each
     * clause is an ArrayList containing an ArrayList of negative literals, and
     * an ArrayList of positive literals. Either the neg lits list or the pos
     * lits list could be empty. Each literal is a Formula object.
     *
     * The first object in the returned ArrayList is an ArrayList of clauses.
     *
     * The second object in the returned ArrayList is the original (input)
     * Formula object (this).
     *
     * The third object in the returned ArrayList is a Map that contains a graph
     * of all the variable substitions done during the conversion of this
     * Formula to clausal form. This Map makes it possible to retrieve the
     * correspondences between the variables in the clausal form and the
     * variables in the original Formula.
     *
     * @see clausify()
     * @see clausifyWithRenameInfo()
     *
     * @return A three-element ArrayList,
     *
     * [
     *   // 1. clauses [ // a clause [ // negative literals [ Formula1, Formula2,
     * ..., FormulaN ], // positive literals [ Formula1, Formula2, ..., FormulaN
     * ] ],
     *
     *     // another clause [ // negative literals [ Formula1, Formula2, ...,
     * FormulaN ], // positive literals [ Formula1, Formula2, ..., FormulaN ] ],
     *
     * ..., ],
     *
     *   // 2.
     * <the-original-Formula>,
     *
     *   // 3. {a-Map-of-variable-renamings},
     *
     * ]
     *
     */
    public ArrayList toNegAndPosLitsWithRenameInfo() {

        // System.out.println("INFO in Formula.toNegAndPosLitsWithRenameInfo(" + this + ")");
        ArrayList ans = new ArrayList();
        try {
            List clausesWithRenameInfo = clausifyWithRenameInfo();
            if (clausesWithRenameInfo.size() == 3) {
                Formula clausalForm = (Formula) clausesWithRenameInfo.get(0);
                ArrayList clauses = clausalForm.operatorsOut();
                if ((clauses != null) && !(clauses.isEmpty())) {

                    // System.out.println("\nclauses == " + clauses);
                    ArrayList newClauses = new ArrayList();
                    ArrayList negLits = null;
                    ArrayList posLits = null;
                    ArrayList literals = null;
                    Formula clause = null;
                    for (Object clause1 : clauses) {
                        negLits = new ArrayList();
                        posLits = new ArrayList();
                        literals = new ArrayList();
                        literals.add(negLits);
                        literals.add(posLits);
                        clause = (Formula) clause1;
                        if (clause.listP()) {
                            while (!(clause.empty())) {
                                boolean isNegLit = false;
                                String lit = clause.car();
                                Formula litF = new Formula();
                                litF.read(lit);
                                if (litF.listP() && "not".equals(litF.car())) {
                                    litF.read(litF.cadr());
                                    isNegLit = true;
                                }
                                if ("FALSE".equals(litF.theFormula)) {
                                    isNegLit = true;
                                }
                                if (isNegLit) {
                                    negLits.add(litF);
                                } else {
                                    posLits.add(litF);
                                }
                                // System.out.println("clause 1 == " + clause);
                                clause = clause.cdrAsFormula();
                                // System.out.println("clause 2 == " + clause);
                            }
                        } else if ("FALSE".equals(clause.theFormula)) {
                            negLits.add(clause);
                        } else {
                            posLits.add(clause);
                        }
                        newClauses.add(literals);
                    }
                    ans.add(newClauses);
                }
                if (ans.size() == 1) {
                    for (int j = 1; j < clausesWithRenameInfo.size(); j++) {
                        ans.add(clausesWithRenameInfo.get(j));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method converts every occurrence of '<=>' in the Formula to a
     * conjunct with two occurrences of '=>'.
     *
     * @return A Formula with no occurrences of '<=>'.
     *
     */
    private Formula equivalencesOut() {
        Formula ans = this;
        try {
            String theNewFormula = null;
            if (listP() && !(empty())) {
                String head = car();
                if (isNonEmptyString(head) && listP(head)) {
                    Formula headF = new Formula();
                    headF.read(head);
                    String newHead = headF.equivalencesOut().theFormula;
                    theNewFormula = cdrAsFormula().equivalencesOut().cons(newHead).theFormula;
                } else if ("<=>".equals(head)) {
                    String second = cadr();
                    Formula secondF = new Formula();
                    secondF.read(second);
                    String newSecond = secondF.equivalencesOut().theFormula;
                    String third = caddr();
                    Formula thirdF = new Formula();
                    thirdF.read(third);
                    String newThird = thirdF.equivalencesOut().theFormula;

                    theNewFormula = ("(and (=> "
                            + newSecond
                            + ' '
                            + newThird
                            + ") (=> "
                            + newThird
                            + ' '
                            + newSecond
                            + "))");
                } else {
                    theNewFormula = cdrAsFormula().equivalencesOut().cons(head).theFormula;
                }
                if (theNewFormula != null) {
                    ans = new Formula();
                    ans.read(theNewFormula);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method converts every occurrence of '(=> LHS RHS' in the Formula to
     * a disjunct of the form '(or (not LHS) RHS)'.
     *
     * @return A Formula with no occurrences of '=>'.
     *
     */
    private Formula implicationsOut() {
        Formula ans = this;
        try {
            String theNewFormula = null;
            if (listP() && !(empty())) {
                String head = car();
                if (isNonEmptyString(head) && listP(head)) {
                    Formula headF = new Formula();
                    headF.read(head);
                    String newHead = headF.implicationsOut().theFormula;
                    theNewFormula = cdrAsFormula().implicationsOut().cons(newHead).theFormula;
                } else if ("=>".equals(head)) {
                    String second = cadr();
                    Formula secondF = new Formula();
                    secondF.read(second);
                    String newSecond = secondF.implicationsOut().theFormula;
                    String third = caddr();
                    Formula thirdF = new Formula();
                    thirdF.read(third);
                    String newThird = thirdF.implicationsOut().theFormula;
                    theNewFormula = ("(or (not " + newSecond + ") " + newThird + ')');
                } else {
                    theNewFormula = cdrAsFormula().implicationsOut().cons(head).theFormula;
                }
                if (theNewFormula != null) {
                    ans = new Formula();
                    ans.read(theNewFormula);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method 'pushes in' all occurrences of 'not', so that each occurrence
     * has the narrowest possible scope, and also removes from the Formula all
     * occurrences of '(not (not ...))'.
     *
     * @see negationsIn_1().
     *
     * @return A Formula with all occurrences of 'not' accorded narrowest scope,
     * and no occurrences of '(not (not ...))'.
     */
    private Formula negationsIn() {
        Formula f = this;
        Formula ans = negationsIn_1();

        // Here we repeatedly apply negationsIn_1() until there are no
        // more changes.
        while (!f.theFormula.equals(ans.theFormula)) {

            /*
             System.out.println();
             System.out.println("f.theFormula == " + f.theFormula);
             System.out.println("ans.theFormula == " + ans.theFormula);
             System.out.println();
             */
            f = ans;
            ans = f.negationsIn_1();
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method is used in negationsIn(). It recursively 'pushes in' all
     * occurrences of 'not', so that each occurrence has the narrowest possible
     * scope, and also removes from the Formula all occurrences of '(not (not
     * ...))'.
     *
     * @see negationsIn().
     *
     * @return A Formula with all occurrences of 'not' accorded narrowest scope,
     * and no occurrences of '(not (not ...))'.
     */
    private Formula negationsIn_1() {
        // System.out.println("INFO in negationsIn_1(" + theFormula + ")");
        try {
            if (listP()) {
                if (empty()) {
                    return this;
                }
                String arg0 = car();
                String arg1 = cadr();
                if ("not".equals(arg0) && listP(arg1)) {
                    Formula arg1F = new Formula();
                    arg1F.read(arg1);
                    String arg0_of_arg1 = arg1F.car();
                    if ("not".equals(arg0_of_arg1)) {
                        String arg1_of_arg1 = arg1F.cadr();
                        Formula arg1_of_arg1F = new Formula();
                        arg1_of_arg1F.read(arg1_of_arg1);
                        return arg1_of_arg1F;
                    }
                    if (isCommutative(arg0_of_arg1)) {
                        String newOp = ("and".equals(arg0_of_arg1) ? "or" : "and");
                        return arg1F.cdrAsFormula().listAll("(not ", ")").cons(newOp);
                    }
                    if (isQuantifier(arg0_of_arg1)) {
                        String vars = arg1F.cadr();
                        String arg2_of_arg1 = arg1F.caddr();
                        String quant = ("forall".equals(arg0_of_arg1) ? "exists" : "forall");
                        arg2_of_arg1 = ("(not " + arg2_of_arg1 + ')');
                        Formula arg2_of_arg1F = new Formula();
                        arg2_of_arg1F.read(arg2_of_arg1);
                        String theNewFormula = ('(' + quant + ' ' + vars + ' '
                                + arg2_of_arg1F.negationsIn_1().theFormula + ')');
                        Formula newF = new Formula();
                        newF.read(theNewFormula);
                        return newF;
                    }
                    String theNewFormula = ("(not " + arg1F.negationsIn_1().theFormula + ')');
                    Formula newF = new Formula();
                    newF.read(theNewFormula);
                    return newF;
                }
                if (isQuantifier(arg0)) {
                    String arg2 = caddr();
                    Formula arg2F = new Formula();
                    arg2F.read(arg2);
                    String newArg2 = arg2F.negationsIn_1().theFormula;
                    String theNewFormula = ('(' + arg0 + ' ' + arg1 + ' ' + newArg2 + ')');
                    Formula newF = new Formula();
                    newF.read(theNewFormula);
                    return newF;
                }
                if (listP(arg0)) {
                    Formula arg0F = new Formula();
                    arg0F.read(arg0);
                    return cdrAsFormula().negationsIn_1().cons(arg0F.negationsIn_1().theFormula);
                }
                return cdrAsFormula().negationsIn_1().cons(arg0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * ***************************************************************
     * This method augments each element of the Formula by concatenating
     * optional Strings before and after the element.
     *
     * Note that in most cases the input Formula will be simply a list, not a
     * well-formed SUO-KIF Formula, and that the output will therefore not
     * necessarily be a well-formed Formula.
     *
     * @param before A String that, if present, is prepended to every element of
     * the Formula.
     *
     * @param after A String that, if present, is postpended to every element of
     * the Formula.
     *
     * @return A Formula, or, more likely, simply a list, with the String values
     * corresponding to before and after added to each element.
     *
     */
    private Formula listAll(String before, String after) {
        Formula ans = this;
        String theNewFormula = null;
        if (listP()) {
            theNewFormula = "";
            Formula f = this;
            while (!(f.empty())) {
                String element = f.car();
                if (isNonEmptyString(before)) {
                    element = (before + element);
                }
                if (isNonEmptyString(after)) {
                    element += after;
                }
                theNewFormula += (' ' + element);
                f = f.cdrAsFormula();
            }
            theNewFormula = ('(' + theNewFormula.trim() + ')');
            if (isNonEmptyString(theNewFormula)) {
                ans = new Formula();
                ans.read(theNewFormula);
            }
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This static variable holds the int value that is used to generate unique
     * variable names.
     */
    private static int VAR_INDEX = 0;

    /**
     * ***************************************************************
     * This static variable holds the int value that is used to generate unique
     * Skolem terms.
     */
    private static int SKOLEM_INDEX = 0;

    /**
     * ***************************************************************
     * This method increments VAR_INDEX and then returns the new int value. If
     * VAR_INDEX is already at Integer.MAX_VALUE, then VAR_INDEX is reset to 0.
     *
     * @return An int value between 0 and Integer.MAX_VALUE inclusive.
     */
    private static int incVarIndex() {
        int oldVal = VAR_INDEX;
        if (oldVal == Integer.MAX_VALUE) {
            VAR_INDEX = 0;
        } else {
            ++VAR_INDEX;
        }
        return VAR_INDEX;
    }

    /**
     * ***************************************************************
     * This method increments SKOLEM_INDEX and then returns the new int value.
     * If SKOLEM_INDEX is already at Integer.MAX_VALUE, then SKOLEM_INDEX is
     * reset to 0.
     *
     * @return An int value between 0 and Integer.MAX_VALUE inclusive.
     */
    private static int incSkolemIndex() {
        int oldVal = SKOLEM_INDEX;
        if (oldVal == Integer.MAX_VALUE) {
            SKOLEM_INDEX = 0;
        } else {
            ++SKOLEM_INDEX;
        }
        return SKOLEM_INDEX;
    }

    /**
     * ***************************************************************
     * This method returns a new SUO-KIF variable String, modifying any digit
     * suffix to ensure that the variable will be unique.
     *
     * @param prefix An optional variable prefix string.
     *
     * @return A new SUO-KIF variable.
     */
    private static String newVar(String prefix) {
        String base = "?X";
        String varIdx = Integer.toString(incVarIndex());
        if (isNonEmptyString(prefix)) {
            List woDigitSuffix = KB.getMatches(prefix, "var_with_digit_suffix");
            //noinspection IfStatementWithTooManyBranches
            if (woDigitSuffix != null) {
                base = (String) woDigitSuffix.get(0);
            } else if (prefix.startsWith("@ROW")) {
                base = "@ROW";
            } else if (prefix.startsWith("?X")) {
                base = "?X";
            } else {
                base = prefix;
            }
            if (!(base.length() > 0 && base.charAt(0) == '?' || base.length() > 0 && base.charAt(0) == '@')) {
                base = ('?' + base);
            }
        }
        return (base + varIdx);
    }

    /**
     * ***************************************************************
     * This method returns a new SUO-KIF variable String, modifying any digit
     * suffix to ensure that the variable will be unique.
     *
     * @return A new SUO-KIF variable.
     */
    private static String newVar() {
        return newVar(null);
    }

    /**
     * ***************************************************************
     * This method returns a new SUO-KIF row variable String, modifying any
     * digit suffix to ensure that the variable will be unique.
     *
     * @return A new SUO-KIF row variable.
     */
    private static String newRowVar() {
        return newVar("@ROW");
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which all variables have been
     * renamed to ensure uniqueness.
     *
     * @see clausify()
     * @see renameVariables(Map topLevelVars, Map scopedRenames)
     *
     * @return A new SUO-KIF Formula with all variables renamed.
     */
    private Formula renameVariables() {
        HashMap topLevelVars = new HashMap();
        HashMap scopedRenames = new HashMap();
        HashMap allRenames = new HashMap();
        return renameVariables(topLevelVars, scopedRenames, allRenames);
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which all variables have been
     * renamed to ensure uniqueness.
     *
     * @see renameVariables().
     *
     * @param topLevelVars A Map that is used to track renames of implicitly
     * universally quantified variables.
     *
     * @param scopedRenames A Map that is used to track renames of explicitly
     * quantified variables.
     *
     * @param allRenames A Map from all new vars in the Formula to their old
     * counterparts.
     *
     * @return A new SUO-KIF Formula with all variables renamed.
     */
    private Formula renameVariables(Map topLevelVars, Map scopedRenames, Map allRenames) {

        try {
            if (listP()) {
                if (empty()) {
                    return this;
                }
                String arg0 = car();
                if (isQuantifier(arg0)) {

                    // Copy the scopedRenames map to protect
                    // variable scope as we descend below this
                    // quantifier.
                    Map newScopedRenames = new HashMap(scopedRenames);

                    String oldVars = cadr();
                    Formula oldVarsF = new Formula();
                    oldVarsF.read(oldVars);
                    String newVars = "";
                    while (!(oldVarsF.empty())) {
                        String oldVar = oldVarsF.car();
                        String newVar = newVar(oldVar);
                        newScopedRenames.put(oldVar, newVar);
                        allRenames.put(newVar, oldVar);
                        newVars += (' ' + newVar);
                        oldVarsF = oldVarsF.cdrAsFormula();
                    }
                    newVars = ('(' + newVars.trim() + ')');
                    String arg2 = caddr();
                    Formula arg2F = new Formula();
                    arg2F.read(arg2);
                    String newArg2 = arg2F.renameVariables(topLevelVars, newScopedRenames, allRenames).theFormula;
                    String theNewFormula = ('(' + arg0 + ' ' + newVars + ' ' + newArg2 + ')');
                    Formula newF = new Formula();
                    newF.read(theNewFormula);
                    return newF;
                }
                Formula arg0F = new Formula();
                arg0F.read(arg0);
                String newArg0 = arg0F.renameVariables(topLevelVars, scopedRenames, allRenames).theFormula;
                String newRest
                        = cdrAsFormula().renameVariables(topLevelVars, scopedRenames, allRenames).theFormula;
                Formula newRestF = new Formula();
                newRestF.read(newRest);
                String theNewFormula = newRestF.cons(newArg0).theFormula;
                Formula newF = new Formula();
                newF.read(theNewFormula);
                return newF;
            }
            if (isVariable(theFormula)) {
                String rnv = (String) scopedRenames.get(theFormula);
                if (!(isNonEmptyString(rnv))) {
                    rnv = (String) topLevelVars.get(theFormula);
                    if (!(isNonEmptyString(rnv))) {
                        rnv = newVar(theFormula);
                        topLevelVars.put(theFormula, rnv);
                        allRenames.put(rnv, theFormula);
                    }
                }
                Formula newF = new Formula();
                newF.read(rnv);
                return newF;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * ***************************************************************
     * This method returns a new, unique skolem term with each invocation.
     *
     * @param vars A sorted TreeSet of the universally quantified variables that
     * potentially define the skolem term. The set may be empty.
     *
     * @return A String. The string will be a skolem functional term (a list) if
     * vars cotains variables. Otherwise, it will be an atomic constant.
     */
    private String newSkolemTerm(TreeSet vars) {
        String ans = "Sk";
        int idx = incSkolemIndex();
        if ((vars != null) && !(vars.isEmpty())) {
            ans += ("Fn " + idx);
            for (Object var1 : vars) {
                String var = (String) var1;
                ans += (' ' + var);
            }
            ans = ('(' + ans + ')');
        } else {
            ans += idx;
        }
        return ans;
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which all existentially quantified
     * variables have been replaced by Skolem terms.
     *
     * @see existentialsOut(Map evSubs, TreeSet iUQVs, TreeSet scopedUQVs)
     * @see collectIUQVars(TreeSet iuqvs, TreeSet scopedVars)
     *
     * @return A new SUO-KIF Formula without existentially quantified variables.
     */
    private Formula existentialsOut() {

        // Existentially quantified variable substitution pairs:
        // var -> skolem term.
        Map evSubs = new HashMap();

        // Implicitly universally quantified variables.
        TreeSet iUQVs = new TreeSet();

        // Explicitly quantified variables.
        TreeSet scopedVars = new TreeSet();

        // Explicitly universally quantified variables.
        TreeSet scopedUQVs = new TreeSet();

        // Collect the implicitly universally qualified variables from
        // the Formula.
        collectIUQVars(iUQVs, scopedVars);

        // Do the recursive term replacement, and return the results.
        return existentialsOut(evSubs, iUQVs, scopedUQVs);
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which all existentially quantified
     * variables have been replaced by Skolem terms.
     *
     * @param evSubs     A Map of variable - skolem term substitution pairs.
     * @param iUQVs      A TreeSet of implicitly universally quantified variables.
     * @param scopedUQVs A TreeSet of explicitly universally quantified
     *                   variables.
     * @return A new SUO-KIF Formula without existentially quantified variables.
     * @see existentialsOut()
     */
    private Formula existentialsOut(Map evSubs, TreeSet iUQVs, TreeSet scopedUQVs) {
        Formula result = this;
        while (true) {
            // System.out.println("INFO in existentialsOut(" + this.theFormula + ", " + evSubs + ", " + iUQVs + ", " + scopedUQVs + ")");
            try {
                if (result.listP()) {
                    if (result.empty()) {
                        return result;
                    }
                    String arg0 = result.car();
                    if ("forall".equals(arg0)) {

                        // Copy the scoped variables set to protect
                        // variable scope as we descend below this
                        // quantifier.
                        TreeSet newScopedUQVs = new TreeSet(scopedUQVs);

                        String varList = result.cadr();
                        Formula varListF = new Formula();
                        varListF.read(varList);
                        while (!(varListF.empty())) {
                            String var = varListF.car();
                            newScopedUQVs.add(var);
                            varListF.read(varListF.cdr());
                        }
                        String arg2 = result.caddr();
                        Formula arg2F = new Formula();
                        arg2F.read(arg2);
                        String theNewFormula = ("(forall "
                                + varList
                                + ' '
                                + arg2F.existentialsOut(evSubs, iUQVs, newScopedUQVs).theFormula + ')');
                        result.read(theNewFormula);
                        return result;
                    }
                    if ("exists".equals(arg0)) {

                        // Collect the relevant universally quantified
                        // variables.
                        TreeSet uQVs = new TreeSet(iUQVs);
                        uQVs.addAll(scopedUQVs);

                        // Collect the existentially quantified
                        // variables.
                        ArrayList eQVs = new ArrayList();
                        String varList = result.cadr();
                        Formula varListF = new Formula();
                        varListF.read(varList);
                        while (!(varListF.empty())) {
                            String var = varListF.car();
                            eQVs.add(var);
                            varListF.read(varListF.cdr());
                        }

                        // For each existentially quantified variable,
                        // create a corresponding skolem term, and
                        // store the pair in the evSubs map.
                        for (Object eQV : eQVs) {
                            String var = (String) eQV;
                            String skTerm = result.newSkolemTerm(uQVs);
                            evSubs.put(var, skTerm);
                        }
                        String arg2 = result.caddr();
                        Formula arg2F = new Formula();
                        arg2F.read(arg2);
                        result = arg2F;
                        continue;
                    }
                    Formula arg0F = new Formula();
                    arg0F.read(arg0);
                    String newArg0 = arg0F.existentialsOut(evSubs, iUQVs, scopedUQVs).theFormula;
                    return result.cdrAsFormula().existentialsOut(evSubs, iUQVs, scopedUQVs).cons(newArg0);
                }
                if (isVariable(theFormula)) {
                    String newTerm = (String) evSubs.get(result.theFormula);
                    if (isNonEmptyString(newTerm)) {
                        result.read(newTerm);
                    }
                    return result;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }

    /**
     * ***************************************************************
     * This method collects all variables in Formula that appear to be only
     * implicitly universally quantified and adds them to the TreeSet iuqvs.
     * Note the iuqvs must be passed in.
     *
     * @param iuqvs A TreeSet for accumulating variables that appear to be
     * implicitly universally quantified.
     *
     * @param scopedVars A TreeSet containing explicitly quantified variables.
     *
     * @return void
     */
    private void collectIUQVars(TreeSet iuqvs, TreeSet scopedVars) {

        // System.out.println("INFO in collectIUQVars(" + this.theFormula + ", " + iuqvs + ", " + scopedVars + ")");
        try {
            if (listP() && !(empty())) {
                String arg0 = car();
                if (isQuantifier(arg0)) {

                    // Copy the scopedVars set to protect variable
                    // scope as we descend below this quantifier.
                    TreeSet newScopedVars = new TreeSet(scopedVars);

                    String varList = cadr();
                    Formula varListF = new Formula();
                    varListF.read(varList);
                    while (!(varListF.empty())) {
                        String var = varListF.car();
                        newScopedVars.add(var);
                        varListF = varListF.cdrAsFormula();
                    }
                    String arg2 = caddr();
                    Formula arg2F = new Formula();
                    arg2F.read(arg2);
                    arg2F.collectIUQVars(iuqvs, newScopedVars);
                } else {
                    Formula arg0F = new Formula();
                    arg0F.read(arg0);
                    arg0F.collectIUQVars(iuqvs, scopedVars);
                    cdrAsFormula().collectIUQVars(iuqvs, scopedVars);
                }
            } else if (isVariable(theFormula)
                    && !(scopedVars.contains(theFormula))) {
                iuqvs.add(theFormula);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which explicit univeral quantifiers
     * have been removed.
     *
     * @return A new SUO-KIF Formula without explicit universal quantifiers.
     * @see clausify()
     */
    private Formula universalsOut() {
        Formula result = this;
        while (true) {
            // System.out.println("INFO in universalsOut(" + this.theFormula + ")");
            try {
                if (result.listP()) {
                    if (result.empty()) {
                        return result;
                    }
                    String arg0 = result.car();
                    if ("forall".equals(arg0)) {
                        String arg2 = result.caddr();
                        result.read(arg2);
                        result = result;
                        continue;
                    }
                    Formula arg0F = new Formula();
                    arg0F.read(arg0);
                    String newArg0 = arg0F.universalsOut().theFormula;
                    return result.cdrAsFormula().universalsOut().cons(newArg0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which nested 'and', 'or', and 'not'
     * operators have been unnested:
     *
     * (not (not <literal> ...)) -> <literal>
     *
     * (and (and <literal-sequence> ...)) -> (and <literal-sequence> ...)
     *
     * (or (or <literal-sequence> ...)) -> (or <literal-sequence> ...)
     *
     * @see clausify()
     * @see nestedOperatorsOut_1()
     *
     * @return A new SUO-KIF Formula in which nested commutative operators and
     * 'not' have been unnested.
     */
    private Formula nestedOperatorsOut() {
        Formula f = this;
        Formula ans = nestedOperatorsOut_1();

        // Here we repeatedly apply nestedOperatorsOut_1() until there are no
        // more changes.
        while (!f.theFormula.equals(ans.theFormula)) {

            /*
             System.out.println();
             System.out.println("f.theFormula == " + f.theFormula);
             System.out.println("ans.theFormula == " + ans.theFormula);
             System.out.println();
             */
            f = ans;
            ans = f.nestedOperatorsOut_1();
        }
        return ans;
    }

    /**
     * ***************************************************************
     *
     * @return A new SUO-KIF Formula in which nested commutative operators and
     * 'not' have been unnested.
     * @see clausify()
     * @see nestedOperatorsOut_1()
     */
    private Formula nestedOperatorsOut_1() {
        Formula result = this;
        nestedOperatorsOut_1:
        while (true) {

            // System.out.println("INFO in nestedOperatorsOut_1(" + this.theFormula + ")");
            try {
                if (result.listP()) {
                    if (result.empty()) {
                        return result;
                    }
                    String arg0 = result.car();
                    if (isCommutative(arg0) || "not".equals(arg0)) {
                        ArrayList literals = new ArrayList();
                        Formula restF = result.cdrAsFormula();
                        while (!(restF.empty())) {
                            String lit = restF.car();
                            Formula litF = new Formula();
                            litF.read(lit);
                            if (litF.listP()) {
                                String litFarg0 = litF.car();
                                if (litFarg0.equals(arg0)) {
                                    if ("not".equals(arg0)) {
                                        String theNewFormula = litF.cadr();
                                        Formula newF = new Formula();
                                        newF.read(theNewFormula);
                                        result = newF;
                                        continue nestedOperatorsOut_1;
                                    }
                                    Formula rest2F = litF.cdrAsFormula();
                                    while (!(rest2F.empty())) {
                                        String rest2arg0 = rest2F.car();
                                        Formula rest2arg0F = new Formula();
                                        rest2arg0F.read(rest2arg0);
                                        literals.add(rest2arg0F.nestedOperatorsOut_1().theFormula);
                                        rest2F = rest2F.cdrAsFormula();
                                    }
                                } else {
                                    literals.add(litF.nestedOperatorsOut_1().theFormula);
                                }
                            } else {
                                literals.add(lit);
                            }
                            restF = restF.cdrAsFormula();
                        }
                        String theNewFormula = ('(' + arg0);
                        for (Object literal : literals) {
                            theNewFormula += (" " + literal);
                        }
                        theNewFormula += ")";
                        Formula newF = new Formula();
                        newF.read(theNewFormula);
                        return newF;
                    }
                    Formula arg0F = new Formula();
                    arg0F.read(arg0);
                    String newArg0 = arg0F.nestedOperatorsOut_1().theFormula;
                    return result.cdrAsFormula().nestedOperatorsOut_1().cons(newArg0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }

    /**
     * ***************************************************************
     * This method returns a new Formula in which all occurrences of 'or' have
     * been accorded the least possible scope.
     *
     * (or P (and Q R)) -> (and (or P Q) (or P R))
     *
     * @see clausify()
     * @see disjunctionsIn_1()
     *
     * @return A new SUO-KIF Formula in which occurrences of 'or' have been
     * 'moved in' as far as possible.
     */
    private Formula disjunctionsIn() {
        Formula f = this;
        Formula ans = nestedOperatorsOut().disjunctionsIn_1();

        // Here we repeatedly apply disjunctionIn_1() until there are no
        // more changes.
        while (!f.theFormula.equals(ans.theFormula)) {

            /* 
             System.out.println();
             System.out.println("f.theFormula == " + f.theFormula);
             System.out.println("ans.theFormula == " + ans.theFormula);
             System.out.println();
             */
            f = ans;
            ans = f.nestedOperatorsOut().disjunctionsIn_1();
        }
        return ans;
    }

    /**
     * ***************************************************************
     *
     * @see clausify()
     * @see disjunctionsIn()
     *
     * @return A new SUO-KIF Formula in which occurrences of 'or' have been
     * 'moved in' as far as possible.
     */
    private Formula disjunctionsIn_1() {

        // System.out.println("INFO in disjunctionsIn_1(" + this.theFormula + ")");
        try {
            if (listP()) {
                if (empty()) {
                    return this;
                }
                String arg0 = car();
                if ("or".equals(arg0)) {
                    List disjuncts = new ArrayList();
                    List conjuncts = new ArrayList();
                    Formula restF = cdrAsFormula();
                    while (!(restF.empty())) {
                        String disjunct = restF.car();
                        Formula disjunctF = new Formula();
                        disjunctF.read(disjunct);
                        if (disjunctF.listP()
                                && "and".equals(disjunctF.car())
                                && conjuncts.isEmpty()) {
                            Formula rest2F = disjunctF.cdrAsFormula().disjunctionsIn_1();
                            while (!(rest2F.empty())) {
                                conjuncts.add(rest2F.car());
                                rest2F = rest2F.cdrAsFormula();
                            }
                        } else {
                            disjuncts.add(disjunct);
                        }
                        restF = restF.cdrAsFormula();
                    }

                    if (conjuncts.isEmpty()) {
                        return this;
                    }

                    Formula resultF = new Formula();
                    resultF.read("()");
                    String disjunctsString = "";
                    for (Object disjunct : disjuncts) {
                        disjunctsString += (" " + disjunct);
                    }
                    disjunctsString = ('(' + disjunctsString.trim() + ')');
                    Formula disjunctsF = new Formula();
                    disjunctsF.read(disjunctsString);
                    for (Object conjunct : conjuncts) {
                        String newDisjuncts
                                = disjunctsF.cons((String) conjunct).cons("or").disjunctionsIn_1().theFormula;
                        resultF = resultF.cons(newDisjuncts);
                    }
                    resultF = resultF.cons("and");
                    return resultF;
                }
                Formula arg0F = new Formula();
                arg0F.read(arg0);
                String newArg0 = arg0F.disjunctionsIn_1().theFormula;
                return cdrAsFormula().disjunctionsIn_1().cons(newArg0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * ***************************************************************
     * This method returns an ArrayList of clauses. Each clause is a LISP list
     * (really, a Formula) containing one or more Formulas. The LISP list is
     * assumed to be a disjunction, but there is no 'or' at the head.
     *
     * @see clausify()
     *
     * @return An ArrayList of LISP lists, each of which contains one or more
     * Formulas.
     */
    private ArrayList operatorsOut() {
        // System.out.println("INFO in operatorsOut(" + this.theFormula + ")");
        ArrayList result = new ArrayList();
        try {
            ArrayList clauses = new ArrayList();
            if (isNonEmptyString(theFormula)) {
                if (listP()) {
                    String arg0 = car();
                    if ("and".equals(arg0)) {
                        Formula restF = cdrAsFormula();
                        while (!(restF.empty())) {
                            String fStr = restF.car();
                            Formula newF = new Formula();
                            newF.read(fStr);
                            clauses.add(newF);
                            restF = restF.cdrAsFormula();
                        }
                    }
                }
                if (clauses.isEmpty()) {
                    clauses.add(this);
                }
                for (Object clause : clauses) {
                    Formula clauseF = new Formula();
                    clauseF.read("()");
                    Formula f = (Formula) clause;
                    if (f.listP()) {
                        if ("or".equals(f.car())) {
                            f = f.cdrAsFormula();
                            while (!(f.empty())) {
                                String lit = f.car();
                                clauseF = clauseF.cons(lit);
                                f = f.cdrAsFormula();
                            }
                        }
                    }
                    if (clauseF.empty()) {
                        clauseF = clauseF.cons(f.theFormula);
                    }
                    result.add(clauseF);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * ***************************************************************
     * This method returns a Formula in which variables for separate clauses
     * have been 'standardized apart'.
     *
     * @see clausify()
     * @see standardizeApart(Map renameMap)
     * @see standardizeApart_1(Map renames, Map reverseRenames)
     *
     * @return A Formula.
     */
    private Formula standardizeApart() {
        HashMap reverseRenames = new HashMap();
        return standardizeApart(reverseRenames);
    }

    /**
     * ***************************************************************
     * This method returns a Formula in which variables for separate clauses
     * have been 'standardized apart'.
     *
     * @see clausify()
     * @see standardizeApart()
     * @see standardizeApart_1(Map renames, Map reverseRenames)
     *
     * @param renameMap A Map for capturing one-to-one variable rename
     * correspondences. Keys are new variables. Values are old variables.
     *
     * @return A Formula.
     */
    private Formula standardizeApart(Map renameMap) {

        Formula result = this;
        try {
            Map reverseRenames = null;
            reverseRenames = renameMap instanceof Map ? renameMap : new HashMap();

            // First, break the Formula into separate clauses, if
            // necessary.
            ArrayList clauses = new ArrayList();
            if (isNonEmptyString(theFormula)) {
                if (listP()) {
                    String arg0 = car();
                    if ("and".equals(arg0)) {
                        Formula restF = cdrAsFormula();
                        while (!(restF.empty())) {
                            String fStr = restF.car();
                            Formula newF = new Formula();
                            newF.read(fStr);
                            clauses.add(newF);
                            restF = restF.cdrAsFormula();
                        }
                    }
                }
                if (clauses.isEmpty()) {
                    clauses.add(this);
                }

                // 'Standardize apart' by renaming the variables in
                // each clause.
                int n = clauses.size();
                for (int i = 0; i < n; i++) {
                    HashMap renames = new HashMap();
                    Formula oldClause = (Formula) clauses.remove(0);
                    clauses.add(oldClause.standardizeApart_1(renames, reverseRenames));
                }

                // Construct the new Formula to return.
                if (n > 1) {
                    String theNewFormula = "(and";
                    for (Object clause : clauses) {
                        Formula f = (Formula) clause;
                        theNewFormula += (' ' + f.theFormula);
                    }
                    theNewFormula += ")";
                    Formula newF = new Formula();
                    newF.read(theNewFormula);
                    result = newF;
                } else {
                    result = (Formula) clauses.get(0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * ***************************************************************
     * This is a helper method for standardizeApart(renameMap). It assumes that
     * the Formula will be a single clause.
     *
     * @see clausify()
     * @see standardizeApart()
     * @see standardizeApart(Map renameMap)
     *
     * @param renames A Map of correspondences between old variables and new
     * variables.
     *
     * @param reverseRenames A Map of correspondences between new variables and
     * old variables.
     *
     * @return A Formula
     */
    private Formula standardizeApart_1(Map renames, Map reverseRenames) {

        try {
            if (listP() && !(empty())) {
                String arg0 = car();
                Formula arg0F = new Formula();
                arg0F.read(arg0);
                arg0F = arg0F.standardizeApart_1(renames, reverseRenames);
                return cdrAsFormula().standardizeApart_1(renames, reverseRenames).cons(arg0F.theFormula);
            }
            if (isVariable(theFormula)) {
                String rnv = (String) renames.get(theFormula);
                if (!(isNonEmptyString(rnv))) {
                    rnv = newVar(theFormula);
                    renames.put(theFormula, rnv);
                    reverseRenames.put(rnv, theFormula);
                }
                Formula rnvF = new Formula();
                rnvF.read(rnv);
                return rnvF;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * ***************************************************************
     * This method finds the original variable that corresponds to a new
     * variable. Note that the clausification algorithm has two variable
     * renaming steps, and that after variables are standardized apart an
     * original variable might correspond to multiple clause variables.
     *
     * @param var A SUO-KIF variable (String)
     *
     * @param varMap A Map (graph) of successive new to old variable
     * correspondences.
     *
     * @return The original SUO-KIF variable corresponding to the input.
     *
     *
     */
    private static String getOriginalVar(String var, Map varMap) {

        // System.out.println("INFO in getOriginalVar(" + var + ", " + varMap + ")");
        String ans = null;
        try {
            String next = null;
            if (isNonEmptyString(var) && (varMap instanceof Map)) {
                ans = var;
                next = (String) varMap.get(ans);
                while (!((next == null) || next.equals(ans))) {
                    ans = next;
                    next = (String) varMap.get(ans);
                }
                if (ans == null) {
                    ans = var;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // System.out.println("  -> " + ans);
        return ans;
    }

    ///////////////////////////////////////////////////////
    /*
     END of clausify() implementation.
     */
    ///////////////////////////////////////////////////////
    /**
     * ***************************************************************
     * A test method.
     */
    public static void main(String[] args) {

        FileWriter fw = null;
        try {
            long t1 = System.currentTimeMillis();
            int count = 0;
            String inpath = args[0];
            String outpath = args[1];
            if (isNonEmptyString(inpath) && isNonEmptyString(outpath)) {
                File infile = new File(inpath);
                if (infile.exists()) {
                    KIF kif = new KIF();
                    kif.setParseMode(KIF.RELAXED_PARSE_MODE);
                    kif.readFile(infile.getCanonicalPath());
                    if (!kif.formulas.isEmpty()) {
                        File outfile = new File(outpath);
                        if (outfile.exists()) {
                            outfile.delete();
                        }
                        fw = new FileWriter(outfile, true);
                        Iterator it = kif.formulas.values().iterator();
                        Iterator it2 = null;
                        Formula f = null;
                        Formula clausalForm = null;
                        while (it.hasNext()) {
                            it2 = ((Iterable) it.next()).iterator();
                            while (it2.hasNext()) {
                                f = (Formula) it2.next();
                                clausalForm = f.clausify();
                                if (clausalForm != null) {
                                    fw.write(clausalForm.theFormula);
                                    fw.write("\n");
                                    count++;
                                }
                            }
                        }
                        fw.close();
                        fw = null;
                    }
                }
            }
            long dur = (System.currentTimeMillis() - t1);
            System.out.println(count
                    + " clausal forms written in "
                    + (dur / 1000.0)
                    + " seconds");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

}  // Formula.java
