/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;


import com.google.common.collect.Iterators;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.nal.NALOperator;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.util.data.Utf8;

import java.util.*;

/**
 * Struct class represents both compound prolog term
 * and atom term (considered as 0-arity compound).
 */
public class Struct implements PTerm, Compound {


    /**
     * name of the structure
     */
    private String name;
    /**
     * args array
     */
    private Term[] term;
    /**
     * arity
     */
    private int arity;
    /**
     * to speedup hash map operation
     */
    private String predicateIndicator;
    /**
     * primitive behaviour
     */
    private transient PrimitiveInfo primitive;
    /**
     * it indicates if the term is resolved
     */
    private boolean resolved = false;

    /**
     * Builds a Struct representing an atom
     */
    public Struct(String f) {
        this(f, 0);
    }

    /**
     * Builds a compound, with one argument
     */
    public Struct(String f, Term at0) {
        this(f, new Term[]{at0});
    }

    /**
     * Builds a compound, with two arguments
     */
    public Struct(String f, Term at0, Term at1) {
        this(f, new Term[]{at0, at1});
    }

    /**
     * Builds a compound, with three arguments
     */
    public Struct(String f, Term at0, Term at1, Term at2) {
        
        this(f, new Term[]{at0, at1, at2});
    }

    /**
     * Builds a compound, with four arguments
     */
    public Struct(String f, Term at0, Term at1, Term at2, Term at3) {
        this(f, new Term[]{at0, at1, at2, at3});
    }

    /**
     * Builds a compound, with five arguments
     */
    public Struct(String f, Term at0, Term at1, Term at2, Term at3, Term at4) {
        this(f, new Term[]{at0, at1, at2, at3, at4});
    }

    /**
     * Builds a compound, with six arguments
     */
    public Struct(String f, Term at0, Term at1, Term at2, Term at3, Term at4, Term at5) {
        this(f, new Term[]{at0, at1, at2, at3, at4, at5});
    }

    /**
     * Builds a compound, with seven arguments
     */
    public Struct(String f, Term at0, Term at1, Term at2, Term at3, Term at4, Term at5, Term at6) {
        this(f, new Term[]{at0, at1, at2, at3, at4, at5, at6});
    }

    /**
     * Builds a compound, with an array of arguments
     */
    public Struct(String f, Term[] argList) {
        this(f, argList.length);
        for (int i = 0; i < argList.length; i++)
            if (argList[i] == null)
                throw new InvalidTermException("Arguments of a Struct cannot be null");
            else
                term[i] = argList[i];
    }


    /**
     * Builds a structure representing an empty list
     */
    public Struct() {
        this("[]", 0);
        resolved = true;
    }


    /**
     * Builds a list providing head and tail
     */
    public Struct(Term h, Term t) {
        this(".", 2);
        term[0] = h;
        term[1] = t;
    }

    @Override
    public Iterator<Term> iterator() {
        return Iterators.forArray(term);
    }

    /**
     * Builds a list specifying the elements
     */
    public Struct(Term[] argList) {
        this(argList, 0);
    }

    private Struct(Term[] argList, int index) {
        this(".", 2);
        if (index < argList.length) {
            term[0] = argList[index];
            term[1] = new Struct(argList, index + 1);
        } else {
            // build an empty list
            name = "[]";
            arity = 0;
            term = null;
        }
    }

    /**
     * Builds a compound, with a linked list of arguments
     */
    public Struct(String f, Collection<? extends Term> al) {
        name = f;
        arity = al.size();
        if (arity > 0) {
            term = new Term[arity];
            Iterator<? extends Term> ali = al.iterator();
            for (int c = 0; c < arity; c++)
                term[c] = ali.next();
        }

        predicateIndicator = getPredicateString(name, arity);
        resolved = false;
    }

    private Struct(int arity_) {
        arity = arity_;
        term = new PTerm[arity];
    }


    private Struct(final String name_, final int arity_) {
        if (name_ == null)
            throw new InvalidTermException("The functor of a Struct cannot be null");
        if (name_.length() == 0 && arity_ > 0)
            throw new InvalidTermException("The functor of a non-atom Struct cannot be an empty string");
        arity = arity_;
        if (arity > 0) {
            term = new PTerm[arity];
        }
        setName(name_);
    }


    public void replaceSubtermWithVariable(int subterm) {
        Struct s = ((Struct) getTerm(subterm));
        setTerm(subterm, new Var("X"));
    }

    public void setName(String name_) {
        name = name_;
        predicateIndicator = getPredicateString(name, arity);
        resolved = false;
    }

    public static String getPredicateString(String name, int arity) {
        return new StringBuilder(name.length() + 1 + 4).append(name).append('/').append(arity).toString();
    }

    /**
     * @return
     */
    String getPredicateIndicator() {
        return predicateIndicator;
    }

    /**
     * Gets the number of elements of this structure ("arity")
     */
    public int size() {
        return arity;
    }

    public byte[] name() {
        return Utf8.toUtf8(getName());
    }

    /**
     * Gets the functor name  of this structure
     */
    public String getName() {
        return name;
    }

    public Term[] getTerms() {
        return term;
    }


    /**
     * Gets the i-th element of this structure
     * <p>
     * No bound check is done
     */
    public Term getTermX(final int index) {
        return term[index];
    }
    /** same as getTermX, but returns null if is not an instanceof PTerm */
    public PTerm getTermXP(final int index) {
        Term x = term[index];
        if (x instanceof PTerm)
            return ((PTerm)x);
        return null;
    }

    /**
     * Sets the i-th element of this structure
     * <p>
     * (Only for internal service)
     */
    void setTerm(final int index, final Term argument) {
        term[index] = argument;
    }

    void setTerm(final Term[] newArgs) {
        this.arity = newArgs.length;
        this.term = newArgs;
    }


    /**
     * Gets the i-th element of this structure
     * <p>
     * No bound check is done. It is equivalent to
     * <code>getTerm(index).getTerm()</code>
     */
    public Term getTerm(final int index) {
        if (!(term[index] instanceof Var))
            return term[index];
        return term[index].getTerm();
    }


    // checking type and properties of the Term

    /**
     * is this term a prolog numeric term?
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * is this term a struct
     */
    public boolean isStruct() {
        return true;
    }

    /**
     * is this term a variable
     */
    public boolean isVar() {
        return false;
    }


    // check type services

    public boolean isCompound() {
        return arity > 0;
    }

    public boolean isAtom() {
        return (size() == 0 || isEmptyList());
    }

    @Override
    public boolean isList() {
        return (size() == 2 && getName().equals(".") && term[1].isList()) || ((size() == 0) && (getName().equals("[]")));
    }

    public boolean isGround() {
        for (int i = 0; i < arity; i++) {
            Term ti = term[i];
            if (ti instanceof PTerm && ((PTerm) ti).isGround()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check is this struct is clause or directive
     */
    public boolean isClause() {
        return (name.equals(":-") && arity > 1 && term[0].getTerm() instanceof Struct);
        //return(name.equals(":-") && arity == 2 && arg[0].getTerm() instanceof Struct);
    }

    @Override
    public Term getTerm() {
        return this;
    }


    /**
     * Gets an argument inside this structure, given its name
     *
     * @param name name of the structure
     * @return the argument or null if not found
     */
    public Struct getTerms(final String name) {
        if (arity == 0) {
            return null;
        }

        //TODO combine these into one loop
        for (int i = 0; i < term.length; i++) {
            if (term[i] instanceof Struct) {
                Struct s = (Struct) term[i];
                if (s.getName().equals(name)) {
                    return s;
                }
            }
        }
        for (int i = 0; i < term.length; i++) {
            if (term[i] instanceof Struct) {
                Struct s = (Struct) term[i];
                Struct sol = s.getTerms(name);
                if (sol != null) {
                    return sol;
                }
            }
        }
        return null;
    }


    //

    /**
     * Test if a term is greater than other
     */
    public boolean isGreater(Term t) {
        t = t.getTerm();
        if (!(t instanceof Struct)) {
            return true;
        } else {
            Struct ts = (Struct) t;
            int tarity = ts.arity;
            if (arity > tarity) {
                return true;
            } else if (arity == tarity) {
                final int nc = name.compareTo(ts.name);
                if (nc > 0) {
                    return true;
                } else if (nc == 0) {
                    for (int c = 0; c < arity; c++) {
                        Term tc = term[c];
                        Term tstc = ts.term[c];
                        if ((tc instanceof PTerm) && (tstc instanceof PTerm)) {
                            PTerm ptc = (PTerm) tc;
                            if (ptc.isGreater((PTerm) tstc)) {
                                return true;
                            } else if (!ptc.isEqual((PTerm) tstc)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isGreaterRelink(Term t, ArrayList<String> vorder) {
        t = t.getTerm();
        if (!(t instanceof Struct)) {
            return true;
        } else {
            Struct ts = (Struct) t;
            int tarity = ts.arity;
            if (arity > tarity) {
                return true;
            } else if (arity == tarity) {
                //System.out.println("Compare di "+name+" con "+ts.name);
                int nc = name.compareTo(ts.name);
                if (nc > 0) {
                    return true;
                } else if (nc == 0) {
                    for (int c = 0; c < arity; c++) {
                        if (!(term[c] instanceof PTerm) && !(ts.term[c] instanceof PTerm)) continue;
                        PTerm ptc = (PTerm) term[c];
                        PTerm pvt = (PTerm) ts.term[c];

                        //System.out.println("Compare di "+arg[c]+" con "+ts.arg[c]);
                        if (ptc.isGreaterRelink(pvt, vorder)) {
                            return true;
                        } else if (ptc.isEqual(pvt)) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Test if a term is equal to other
     */
    public boolean isEqual(Term t) {
        if (this == t) return true;
        //TODO use a hashcode for the arguments to compare quickly
        t = t.getTerm();
        if (t instanceof Struct) {
            Struct ts = (Struct) t;
            if (arity == ts.arity && name.equals(ts.name)) {
                for (int c = 0; c < arity; c++) {

                    if (!term[c].equals(ts.term[c])) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //


    /**
     * Gets a copy of this structure
     *
     * @param vMap is needed for register occurence of same variables
     */
    public PTerm copy(Map<Var, Var> vMap, int idExecCtx) {
        Struct t = new Struct(arity);
        t.resolved = resolved;
        t.name = name;
        t.predicateIndicator = predicateIndicator;
        t.primitive = primitive;
        for (int c = 0; c < arity; c++) {
            Term tc = term[c];
            if (tc instanceof PTerm)
                t.term[c] = ((PTerm) tc).copy(vMap, idExecCtx);
            else
                t.term[c] = tc.clone();
        }
        return t;
    }


    /**
     * Gets a copy of this structure
     *
     * @param vMap is needed for register occurence of same variables
     */
    public Struct copy(Map<Var, Var> vMap, Map<PTerm, Var> substMap) {

        Struct t = new Struct(arity);
        t.resolved = false;
        t.name = name;
        t.predicateIndicator = predicateIndicator;
        t.primitive = null;
        for (int c = 0; c < arity; c++) {
            Term tc = term[c];
            if (tc instanceof PTerm)
                t.term[c] = ((PTerm) tc).copy(vMap, substMap);
            else
                t.term[c] = tc.clone();
        }
        return t;
    }


    /**
     * resolve term
     */
    public long resolveTerm(long count) {
        if (resolved) {
            return count;
        } else {
            return resolveTerm(new UnifiedMap(), count);
        }
    }


    /**
     * Resolve name of terms
     *
     * @param vl    list of variables resolved
     * @param count start timestamp for variables of this term
     * @return next timestamp for other terms
     */
    public long resolveTerm(final Map<String, Var> vl, final long count) {
        long newcount = count;
        final Term[] arg = this.term;
        /*if (hasVar())*/
        {
            for (int c = 0; c < arity; c++) {
                Term term = arg[c];
                if (term != null) {
                    //--------------------------------
                    // we want to resolve only not linked variables:
                    // so linked variables must get the linked term
                    term = term.getTerm();
                    //--------------------------------
                    if (term instanceof Var) {
                        Var t = (Var) term;
                        t.setTimestamp(newcount++);
                        if (!t.isAnonymous()) {
                            // searching a variable with the same name
                            Var found = vl.putIfAbsent(t.getName(), t);
                            if (found != null)
                                arg[c] = found;
                        }
                    } else if (term instanceof Struct) {
                        newcount = ((Struct) term).resolveTerm(vl, newcount);
                    }
                }
            }
        }
        resolved = true;
        return newcount;
    }

    // services for list structures



    /**
     * Gets the head of this structure, which is supposed to be a list.
     * <p>
     * <p>
     * Gets the head of this structure, which is supposed to be a list.
     * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
     * </p>
     */
    public Term listHead() {
        if (!isList())
            throw new UnsupportedOperationException("The structure " + this + " is not a list.");
        return term[0].getTerm();
    }

    @Override
    public boolean isConstant() {
        return !hasVar();
    }

    @Override
    public Term replaceTerm(int index, Term t) {
        term[index] = t;
        return this;
    }

    /**
     * Gets the tail of this structure, which is supposed to be a list.
     * <p>
     * <p>
     * Gets the tail of this structure, which is supposed to be a list.
     * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
     * </p>
     */
    public Struct listTail() {
        if (!isList())
            throw new UnsupportedOperationException("The structure " + this + " is not a list.");
        return (Struct) term[1].getTerm();
    }

    /**
     * Gets the number of elements of this structure, which is supposed to be a list.
     * <p>
     * <p>
     * Gets the number of elements of this structure, which is supposed to be a list.
     * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
     * </p>
     */
    public int listSize() {
        if (!isList())
            throw new UnsupportedOperationException("The structure " + this + " is not a list.");
        Struct t = this;
        int count = 0;
        while (!t.isEmptyList()) {
            count++;
            t = (Struct) t.term[1].getTerm();
        }
        return count;
    }

    /**
     * Gets an iterator on the elements of this structure, which is supposed to be a list.
     * <p>
     * <p>
     * Gets an iterator on the elements of this structure, which is supposed to be a list.
     * If the callee structure is not a list, throws an <code>UnsupportedOperationException</code>
     * </p>
     */
    public Iterator<? extends Term> listIterator() {
        if (!isList())
            throw new UnsupportedOperationException("The structure " + this + " is not a list.");
        return new StructIterator(this);
    }

    // hidden services

    /**
     * Gets a list Struct representation, with the functor as first element.
     */
    public Struct toList() {
        Struct t = new Struct();
        for (int c = arity - 1; c >= 0; c--) {
            t = new Struct(term[c].getTerm(), t);
        }
        return new Struct(new Struct(name), t);
    }


    /**
     * Gets a flat Struct from this structure considered as a List
     * <p>
     * If this structure is not a list, null object is returned
     */
    Struct fromList() {
        Term ft = term[0].getTerm();
        if (!ft.isAtom()) {
            return null;
        }
        Struct at = (Struct) term[1].getTerm();
        List<Term> al = new ArrayList<>();
        while (!at.isEmptyList()) {
            if (!at.isList()) {
                return null;
            }
            al.add(at.getTerm(0));
            at = (Struct) at.getTerm(1);
        }
        return new Struct(((Struct) ft).name, al);
    }


    /**
     * Appends an element to this structure supposed to be a list
     */
    public void append(Term t) {
        if (isEmptyList()) {
            name = ".";
            arity = 2;
            predicateIndicator = name + '/' + arity; /* Added by Paolo Contessi */
            term = new Term[arity];
            term[0] = t;
            term[1] = new Struct();
        } else if (term[1].isList()) {
            ((Struct) term[1]).append(t);
        } else {
            term[1] = t;
        }
    }


    /**
     * Inserts (at the head) an element to this structure supposed to be a list
     */
    void insert(PTerm t) {
        Struct co = new Struct();
        co.term[0] = term[0];
        co.term[1] = term[1];
        term[0] = t;
        term[1] = co;
    }

    //

    /**
     * Try to unify two terms
     *
     * @param t   the term to unify
     * @param vl1 list of variables unified
     * @param vl2 list of variables unified
     * @return true if the term is unifiable with this one
     */
    public boolean unify(final List<Var> vl1, final List<Var> vl2, Term t) {
        // In fase di unificazione bisogna annotare tutte le variabili della struct completa.
        t = t.getTerm();

        final int ari = this.arity;
        Term[] argg = this.term;


        if (t instanceof Struct) {
            Struct ts = (Struct) t;
            if (ari == ts.arity && name.equals(ts.name)) {
                for (int c = 0; c < ari; c++) {
                    if (argg[c] instanceof PTerm) {
                        if (!((PTerm) argg[c]).unify(vl1, vl2, ts.getTerm(c))) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else if (t instanceof Var) {
            return ((Var)t).unify(vl2, vl1, this);
        }
        return false;
    }


    /**
     * dummy method
     */
    public void free() {
    }

    //

    /**
     * Set primitive behaviour associated at structure
     */
    void setPrimitive(PrimitiveInfo b) {
        primitive = b;
    }

    /**
     * Get primitive behaviour associated at structure
     */
    public PrimitiveInfo getPrimitive() {
        return primitive;
    }

    /**
     * Check if this term is a primitive struct
     */
    public boolean isPrimitive() {
        return primitive != null;
    }

    //

    public static final String atomEscape(final CharSequence n) {
        return "\'" + n + '\'';
    }

    @Override
    public PTerm clone() {
        return this;
    }

    @Override
    public Compound cloneDeep() {
        return this;
    }

    /**
     * Gets the string representation of this structure
     * <p>
     * Specific representations are provided for lists and atoms.
     * Names starting with upper case letter are enclosed in apices.
     */
    public String toString() {
        // empty list case
        if (isEmptyList()) return "[]";
        // list case

        if (name.equals(".") && arity == 2) {
            return ('[' + toString0() + ']');
        } else if (name.equals("{}")) {
            return ('{' + toString0_bracket() + '}');
        } else {
            CharSequence s = (Parser.isAtom(name) ? name : atomEscape(name));
            //TODO use StringBuilder
            if (arity > 0) {
                s = s + "(";
                for (int c = 1; c < arity; c++) {
                    if (!(term[c - 1] instanceof Var)) {
                        s = s + term[c - 1].toString() + ',';
                    } else {
                        s = s + ((Var) term[c - 1]).toStringFlattened() + ',';
                    }
                }
                if (!(term[arity - 1] instanceof Var)) {
                    s = s + term[arity - 1].toString() + ')';
                } else {
                    s = s + ((Var) term[arity - 1]).toStringFlattened() + ')';
                }
            }
            return s.toString();
        }
    }

    @Override
    public int hashCode() {
        return getPredicateIndicator().hashCode();
    }

    @Override
    public void share(Compound equivalent) {

    }


    private String toString0() {
        Term h = term[0].getTerm();
        Term t = term[1].getTerm();
        if (t instanceof Struct && ((Struct) t).isList()) {
            Struct tl = (Struct) t;
            if (tl.isEmptyList()) {
                return h.toString();
            }
            if (h instanceof Var) {
                return (((Var) h).toStringFlattened() + ',' + tl.toString0());
            } else {
                return (h.toString() + ',' + tl.toString0());
            }
        } else {
            String h0;
            String t0;
            if (h instanceof Var) {
                h0 = ((Var) h).toStringFlattened();
            } else {
                h0 = h.toString();
            }
            if (t instanceof Var) {
                t0 = ((Var) t).toStringFlattened();
            } else {
                t0 = t.toString();
            }
            return (h0 + '|' + t0);
        }
    }

    private String toString0_bracket() {
        if (arity == 0) {
            return "";
        } else if (arity == 1 && !((term[0] instanceof Struct) && ((Struct) term[0]).getName().equals(","))) {
            return term[0].getTerm().toString();
        } else {
            // comma case
            Struct stt = ((Struct) term[0]);
            Term head = stt.getTerm(0);
            Term tail = stt.getTerm(1);
            StringBuilder buf = new StringBuilder(head.toString());
            while (tail instanceof Struct && ((Struct) tail).getName().equals(",")) {
                head = ((Struct) tail).getTerm(0);
                buf.append(',').append(head.toString());
                tail = ((Struct) tail).getTerm(1);
            }
            buf.append(',').append(tail.toString());
            return buf.toString();
            //    return arg[0]+","+((Struct)arg[1]).toString0_bracket();
        }
    }

    public String toStringAsList(Operators op) {
        Term h = term[0];
        Term t = term[1].getTerm();
        if (h instanceof PTerm) {
            PTerm ph = (PTerm)h;

            if (t.isList()) {
                Struct tl = (Struct) t;
                if (tl.isEmptyList()) {
                    return ph.toStringAsArgY(op, 0);
                }
                return (ph.toStringAsArgY(op, 0) + ',' + tl.toStringAsList(op));
            } else {
                if (t instanceof PTerm) {
                    PTerm pt = (PTerm) t;
                    return (ph.toStringAsArgY(op, 0) + '|' + pt.toStringAsArgY(op, 0));
                }
            }
        }

        return toString();
    }

    public String toStringAsArg(Operators op, int prio, boolean x) {
        int p = 0;
        String v = "";

        if (name.equals(".") && arity == 2) {
            if (term[0].isEmptyList()) {
                return ("[]");
            } else {
                return ('[' + toStringAsList(op) + ']');
            }
        } else if (name.equals("{}")) {
            return ('{' + toString0_bracket() + '}');
        }

        Term t0 = term[0];
        Term t1 = term[1];
        if (!(t0 instanceof PTerm && t1 instanceof PTerm)) {
            return toString();
        }

        PTerm p0 = (PTerm)t0;
        PTerm p1 = (PTerm)t1;

        if (arity == 2) {
            if ((p = op.opPrio(name, "xfx")) >= Operators.OP_LOW) {
                return (
                        (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                p0.toStringAsArgX(op, p) +
                                ' ' + name + ' ' +
                                p1.toStringAsArgX(op, p) +
                                (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
            }
            if ((p = op.opPrio(name, "yfx")) >= Operators.OP_LOW) {
                return (
                        (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                p0.toStringAsArgY(op, p) +
                                ' ' + name + ' ' +
                                p1.toStringAsArgX(op, p) +
                                (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
            }
            if ((p = op.opPrio(name, "xfy")) >= Operators.OP_LOW) {
                if (!name.equals(",")) {
                    return (
                            (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                    p0.toStringAsArgX(op, p) +
                                    ' ' + name + ' ' +
                                    p1.toStringAsArgY(op, p) +
                                    (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
                } else {
                    return (
                            (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                    p0.toStringAsArgX(op, p) +
                                    //",\n\t"+
                                    ',' +
                                    p1.toStringAsArgY(op, p) +
                                    (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
                }
            }
        } else if (arity == 1) {
            if ((p = op.opPrio(name, "fx")) >= Operators.OP_LOW) {
                return (
                        (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                name + ' ' +
                                p0.toStringAsArgX(op, p) +
                                (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
            }
            if ((p = op.opPrio(name, "fy")) >= Operators.OP_LOW) {
                return (
                        (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                name + ' ' +
                                p0.toStringAsArgY(op, p) +
                                (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
            }
            if ((p = op.opPrio(name, "xf")) >= Operators.OP_LOW) {
                return (
                        (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                p0.toStringAsArgX(op, p) +
                                ' ' + name + ' ' +
                                (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
            }
            if ((p = op.opPrio(name, "yf")) >= Operators.OP_LOW) {
                return (
                        (((x && p >= prio) || (!x && p > prio)) ? "(" : "") +
                                p0.toStringAsArgY(op, p) +
                                ' ' + name + ' ' +
                                (((x && p >= prio) || (!x && p > prio)) ? ")" : ""));
            }
        }
        v = (Parser.isAtom(name) ? name : '\'' + name + '\'');
        if (arity == 0) {
            return v;
        }
        v += "(";
        for (p = 1; p < arity; p++) {
            v = v + term[p - 1].toString() /*toStringAsArgY(op, 0)*/ + ',';
        }
        v += term[arity - 1].toString() /*toStringAsArgY(op, 0) */;
        v += ")";
        return v;
    }

    public PTerm iteratedGoalTerm() {
        if (name.equals("^") && arity == 2) {
            Term goal = getTerm(1);
            if (goal instanceof PTerm) {
                return ((PTerm) goal).iteratedGoalTerm();
            }
        }

        return this;
        //return super.iteratedGoalTerm();
    }

    @Override
    public void accept(TermVisitor tv) {
        tv.visit(this);
    }


    @Override
    public NALOperator operator() {
        if (isAtom()) {
            return NALOperator.ATOM;
        } else {
            return NALOperator.PRODUCT;
        }
    }

    @Override
    public short getComplexity() {
        //TODO this is a minimum estimate. 1 + arity = complexity
        return (short) (size() + 1);
    }

    /** renamed from the misleading 'isAtomic' */
    public boolean isAtomic() {
        return size() == 0;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PTerm)
            return isEqual((PTerm) obj);
        return false;
    }

    @Override
    public Term clone(Term[] replaced) {
        return null;
    }
}