/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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


import nars.nal.NALOperator;
import nars.nal.term.Term;
import nars.tuprolog.net.AbstractSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a variable term. Variables are identified by a name
 * (which must starts with an upper case letter) or the anonymous ('_') name.
 *
 * @see PTerm
 *
 */
public class Var implements PTerm {

    final static String ANY = "_";

    // the name identifying the var

    private String name;
    private StringBuilder completeName;     /* Reviewed by Paolo Contessi: String -> StringBuilder */

    private Term link;            /* tlink is used for unification process */

    private long timestamp;        /* timestamp is used for fix vars order */

    private int id;            /* id of ExecCtx owners of this var other for renaming*/


    /**
     * Creates a variable identified by a name.
     *
     * The name must starts with an upper case letter or the underscore. If an
     * underscore is specified as a name, the variable is anonymous.
     *
     * @param n is the name
     * @throws InvalidTermException if n is not a valid Prolog variable name
     */
    public Var(String n) {
        link = null;
        id = -1; //no execCtx owners
        if (n.equals(ANY)) {
            name = null;
            completeName = new StringBuilder(0);
        } else if (Character.isUpperCase(n.charAt(0))
                || (n.startsWith(ANY))) {
            name = n;
            completeName = new StringBuilder(n);
        } else {
            throw new InvalidTermException("Illegal variable name: " + n);
        }
    }



    /**
     * Creates an anonymous variable
     *
     * This is equivalent to build a variable with name _
     */
    public Var() {
        name = null;
        completeName = new StringBuilder(0);
        link = null;
        id = ORIGINAL;
        timestamp = 0;
    }

    /**
     * Creates a internal engine variable.
     *
     * @param n is the name
     * @param id is the id of ExecCtx
     * @param alias code to discriminate external vars
     * @param time is timestamp
     */
    private Var(String n, int id, int alias, long time) {
        name = n;
        completeName = new StringBuilder(0);
        timestamp = time;
        link = null;
        if (id < 0) {
            id = ORIGINAL;
        }
        rename(id, alias);
    }

    /* Identify kind of renaming */
    final static int ORIGINAL = -1;
    final static int PROGRESSIVE = -2;

    /**
     * Rename variable (assign completeName)
     */
    void rename(int idExecCtx, int count) { /* Reviewed by Paolo Contessi: String -> StringBuilder */

        id = idExecCtx;

        completeName.setLength(0);
        if (id > -1) {
            //completeName = name + "_e" + idExecCtx;
            completeName.append(name).append("_e").append(id);
        } else if (id == ORIGINAL) { //completeName = name;
            completeName.append(name);
        } else if (id == PROGRESSIVE) { //completeName = "_"+count;
            completeName.append('_').append(count);
        }

    }

    /**
     * Gets a copy of this variable.
     *
     * if the variable is not present in the list passed as argument, a copy of
     * this variable is returned and added to the list. If instead a variable
     * with the same time identifier is found in the list, then the variable in
     * the list is returned.
     */
    public PTerm copy(Map<Var, Var> vMap, int idExecCtx) {
        Term tt = getTerm();
        if (tt == this) {
            Var v = vMap.get(this);
            if (v == null) {
                //No occurence of v before
                v = new Var(name, idExecCtx, 0, timestamp);
                vMap.put(this, v);
            }
            return v;
        } else {
            if (tt instanceof PTerm)
                return ((PTerm)tt).copy(vMap, idExecCtx);
            throw new RuntimeException("copy resulted in a non-PTerm");
        }
    }

    /**
     * Gets a copy of this variable.
     */
    public PTerm copy(final Map<Var, Var> vMap, final Map<PTerm, Var> substMap) {
        Var v;
        Var temp = vMap.get(this);
        if (temp == null) {
            v = new Var(null, Var.PROGRESSIVE, vMap.size(), timestamp);
                //name,Var.PROGRESSIVE,vMap.size(),timestamp);
            vMap.put(this, v);
        } else {
            v = (Var) temp;
        }
        Term t = getTerm();
        if (t instanceof Var) {
            Object tt = substMap.putIfAbsent((Var)t, v);
            if (tt == null) {
                v.link = null;
            } else {
                v.link = (tt != v) ? (Var) tt : null;
            }
        }
        else if (t instanceof Struct) {
            v.link = ((Struct)t).copy(vMap, substMap);
        }
        else if (t instanceof PNum) {
            v.link = t;
        }
        return v;
    }

    /**
     * De-unify the variable
     */
    public void free() {
        link = null;
    }

    /**
     * De-unify the variables of list
     */
    public final static void free(final List<Var> varsUnified) {
        for (final Var v : varsUnified) {
            v.free();
        }
    }
    /** faster version for arraylist which doesnt involve iterator */
    public final static void free(final ArrayList<Var> varsUnified) {
        final int size = varsUnified.size();
        for (int i = 0; i < size; i++) {
            varsUnified.get(i).free();
        }
    }

    /**
     * Gets the name of the variable
     */
    public String getName() {
        if (name != null) {
            return completeName.toString();
        } else {
            //return ANY+timestamp;
            return ANY;
        }
    }

    /**
     * Gets the name of the variable
     */
    public String getOriginalName() {
        if (name != null) {
            return name;
        } else {
            //return ANY+timestamp;
            return ANY + hashCode();
        }
    }

    /**
     * Gets the term which is referred by the variable.
     *
     * For unbound variable it is the variable itself, while for bound variable
     * it is the bound term.
     */
    public Term getTerm() {
        Term tt = this;
        Term t = link;
        while (t != null) {
            tt = t;
            if (t instanceof Var) {
                t = ((Var) t).link;
            } else {
                break;
            }
        }
        return tt;
    }



    /**
     * Gets the term which is direct referred by the variable.
     */
    public Term getLink() {
        return link;
    }

    /**
     * Set the term which is direct bound
     */
    public void setLink(Term l) {
        link = l;
    }

    /**
     * Set the timestamp
     */
    void setTimestamp(long t) {
        timestamp = t;
    }

	//
    public boolean isNumber() {
        return false;
    }

    public boolean isStruct() {
        return false;
    }

    public boolean isVar() {
        return true;
    }

    public boolean isEmptyList() {
        Term t = getTerm();
        if (t == this) {
            return false;
        } else {
            return t.isEmptyList();
        }
    }

    public boolean isAtomic() {
        Term t = getTerm();
        if (t == this) {
            return false;
        } else {
            return t.isAtomic();
        }
    }

    public boolean isCompound() {
        Term t = getTerm();
        if (t == this) {
            return false;
        } else {
            return t.isCompound();
        }
    }

    public boolean isAtom() {
        Term t = getTerm();
        if (t == this) {
            return false;
        } else {
            return t.isAtom();
        }
    }

    public boolean isList() {
        Term t = getTerm();
        if (t == this) {
            return false;
        } else {
            return t.isList();
        }
    }

    public boolean isGround() {
        Term t = getTerm();
        if (t == this) {
            return false;
        } else {
            return (t instanceof PTerm) && ((PTerm)t).isGround();
        }
    }

	//
    /**
     * Tests if this variable is ANY
     */
    public boolean isAnonymous() {
        return name == null;
    }

    /**
     * Tests if this variable is bound
     *
     */
    public boolean isBound() {
        return link != null;
    }

    /**
     * finds var occurence in a Struct, doing occur-check. (era una findIn)
     *
     * @param vl TODO
     */
    private boolean occurCheck(final List<Var> vl, final Struct t) {
        int arity = t.size();
        for (int c = 0; c < arity; c++) {
            Term at = t.getTerm(c);
            if (at instanceof Struct) {
                if (occurCheck(vl, (Struct) at)) {
                    return true;
                }
            } else if (at instanceof Var) {
                Var v = (Var) at;
                if (v.link == null) {
                    vl.add(v);
                }
                if (this == v) {
                    return true;
                }
            }
        }
        return false;

    }

	 //
    /**
     * Resolve the occurence of variables in a Term
     */
    public long resolveTerm(long count) {
        Term tt = getTerm();
        if ((tt != this) && (tt instanceof PTerm)) {
            return ((PTerm)tt).resolveTerm(count);
        } else {
            timestamp = count;
            return count++;
        }
    }

	 //
    /**
     * var unification.
     * <p>
     * First, verify the Term eventually already unified with the same Var if
     * the Term exist, unify var with that term, in order to handle situation as
     * (A = p(X) , A = p(1)) which must produce X/1.
     * <p>
     * If instead the var is not already unified, then:
     * <p>
     * if the Term is a var bound to X, then try unification with X so for
     * example if A=1, B=A then B is unified to 1 and not to A (note that it's
     * coherent with chronological backtracking: the eventually backtracked A
     * unification is always after backtracking of B unification.
     * <p>
     * if are the same Var, unification must succeed, but without any new
     * bindings (to avoid cycles for extends in A = B, B = A)
     * <p>
     * if the term is a number, then it's a success and new tlink is created
     * (retractable by means of a code)
     * <p>
     * if the term is a compound, then occur check test is executed: the var
     * must not appear in the compound ( avoid X=p(X), or p(X,X)=p(Y,f(Y)) ); if
     * occur check is ok then it's success and a new tlink is created
     * (retractable by a code)
     */
    public boolean unify(List<Var> vl1, List<Var> vl2, Term t) {
        Term tt = getTerm();
        if (tt == this) {
            t = t.getTerm();
            if (t instanceof Var) {
                if (this == t) {
                    //try {
                        vl1.add(this);
                    /* vl1==null mean nothing intresting for the caller */
                    /*} catch (NullPointerException e) {

                    }*/
                    return true;
                }
            } else if (t instanceof Struct) {
                // occur-check
                if (occurCheck(vl2, (Struct) t)) {
                    return false;
                }
            } else if (!(t instanceof PNum) && !(t instanceof AbstractSocket)) {
                return false;
            }
            link = t;
            //try {
                vl1.add(this);
            /* vl1==null mean nothing intresting for the caller */
            /*} catch (NullPointerException e) {

            }*/
            //System.out.println("VAR "+name+" BOUND to "+tlink+" - time: "+time+" - mark: "+mark);
            return true;
        } else {
            return (tt instanceof PTerm) && ( ((PTerm)tt).unify(vl1, vl2, t));
        }
    }

    /**
     * Gets a copy of this variable
     */
    /*    public Term copy(int idExecCtx) {
     Term tt = getTerm();
     if(tt == this) {
     if(idExecCtx > 0 && id > 0) thisCopy++;
     return new Var(name,idExecCtx,thisCopy,antialias,timestamp);
     } else {
     return (tt.copy(idExecCtx));
     }
     }
     */
    public boolean isGreater(Term t) {
        Term tt = getTerm();
        if (tt == this) {
            t = t.getTerm();
            if (!(t instanceof Var)) {
                return false;
            }
            return timestamp > ((Var) t).timestamp;
        } else {
            return (tt instanceof PTerm) && ((PTerm)tt).isGreater(t);
        }
    }

    public boolean isGreaterRelink(Term t, ArrayList<String> vorder) {
        Term tt = getTerm();
        if (tt == this) {
            t = t.getTerm();
            if (!(t instanceof Var)) {
                return false;
            }
			 //System.out.println("Compare di tt "+tt+" con t "+t);
            //System.out.println("vorder "+vorder);
            //System.out.println("indice tt "+vorder.indexOf(((Var)tt).getName())+" indice t "+vorder.indexOf(((Var)t).getName()));
            //return timestamp > ((Var)t).timestamp;
            return vorder.indexOf(((Var) tt).getName()) > vorder.indexOf(((Var) t).getName());
        } else {
            return (tt instanceof PTerm) && ((PTerm)tt).isGreaterRelink(t, vorder);
        }
    }

    public boolean isEqual(Term t) {
        Term tt = getTerm();
        if (tt == this) {
            t = t.getTerm();
            return (t instanceof Var && timestamp == ((Var) t).timestamp);
        } else {
            return (tt instanceof PTerm) && (t instanceof PTerm) && ((PTerm)tt).isEqual((PTerm)t);
        }
    }

    public void setName(String s) {
        this.name = s;
    }

    @Override
    public PTerm clone() {
        return new Var();
    }

    @Override
    public Term cloneDeep() {
        return null;
    }

    /**
     * Gets the string representation of this variable.
     *
     * For bounded variables, the string is <Var Name>/<bound Term>.
     */
    @Override
    public String toString() {
        Term tt = getTerm();
        if (name != null) {
            if (tt == this) {
                return completeName.toString();
            } else {
                return (name + " / " + tt.toString());
            }
        } else {
            if (tt == this) {
                return ANY + hashCode();
            } else {
                return tt.toString();
            }
        }
    }

    /**
     * Gets the string representation of this variable, providing the string
     * representation of the linked term in the case of bound variable
     *
     */
    public String toStringFlattened() {
        Term tt = getTerm();
        if (name != null) {
            if (tt == this) {
                return completeName.toString();
            } else {
                return tt.toString();
            }
        } else {
            if (tt == this) {
                return ANY + hashCode();
            } else {
                return tt.toString();
            }
        }
    }

    /*Castagna 06/2011*/
    @Override
    public void accept(TermVisitor tv) {
        tv.visit(this);
    }
    /**/

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object t) {
        if (t instanceof PTerm)
            return isEqual((PTerm)t);
        return false;
    }

    @Override
    public NALOperator operator() {
        return NALOperator.PVAR;
    }

    @Override
    public short getComplexity() {
        return 1;
    }

    @Override
    public void recurseSubterms(nars.nal.term.TermVisitor v, Term parent) {
        //do nothing
    }
}
