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
import java.util.AbstractMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;

/**
 * This class mantains information about a clause creation
 * (clause copy, final time T after renaming, validity stillValid Flag).
 * These information are necessary to the Theory Manager
 * to use the clause in a consistent way
 *
 */
public class ClauseInfo {
    
    /**
	 * referring clause
	 */
    protected Struct clause;
    
    /**
	 * head of clause
	 */
    private Struct head;
    
    /**
	 * body of clause
	 */
    private SubGoalTree body;
    
    private Struct headCopy;
    
    private SubGoalTree bodyCopy;
    
    /**
	 * if the clause is part of a theory in a lib (null if not)
	 */
    String libName;
    
    
    //usata da Find
    /**
     * building a valid clause with a time stamp = original time stamp + NumVar in clause
     */
    public ClauseInfo(Struct clause_, String lib) {
        clause = clause_;
        head = extractHead(clause);
        body = extractBody(clause.getArg(1));
        libName = lib;
    }
    
    
    /**
     * Gets a clause from a generic Term
     */
    private Struct extractHead(Struct clause) {
        return (Struct)clause.getArg(0);
    }
    
    /**
     * Gets a clause from a generic Term
     */
    static SubGoalTree extractBody(Term body) {
        SubGoalTree r = new SubGoalTree();
        extractBody(r, body);
        return r;
    }
    
    private static void extractBody(SubGoalTree parent, Term body) {
        while (body instanceof Struct && ((Struct)body).getName().equals(",")) {
            Term t = ((Struct)body).getArg(0);
            if (t instanceof Struct && ((Struct)t).getName().equals(",")) {
                extractBody(parent.addChild(),t);
            } else {
                parent.addChild(t);
            }
            body = ((Struct)body).getArg(1);
        }
        parent.addChild(body);
    }
    
    
    /**
     * Gets the string representation
     * recognizing operators stored by
     * the operate manager
     */
    public String toString(OperatorManager op) {
        int p;
        if ((p = op.opPrio(":-","xfx")) >= OperatorManager.OP_LOW) {
            String st=indentPredicatesAsArgX(clause.getArg(1),op,p);
            String head = clause.getArg(0).toStringAsArgX(op,p);
            if (st.equals("true")) {
                return head +".\n";
            } else {
                return (head + " :-\n\t" + st +".\n");
            }
        }
        
        if ((p = op.opPrio(":-","yfx")) >= OperatorManager.OP_LOW) {
            String st=indentPredicatesAsArgX(clause.getArg(1),op,p);
            String head = clause.getArg(0).toStringAsArgY(op,p);
            if (st.equals("true")) {
                return head +".\n";
            } else {
                return (head + " :-\n\t" + st +".\n");
            }
        }
        
        if ((p = op.opPrio(":-","xfy")) >= OperatorManager.OP_LOW) {
            String st=indentPredicatesAsArgY(clause.getArg(1),op,p);
            String head = clause.getArg(0).toStringAsArgX(op,p);
            if (st.equals("true")) {
                return head +".\n";
            } else {
                return (head + " :-\n\t" + st +".\n");
            }
        }
        return (clause.toString());
    }
    
   
    Struct getClause() {
        return clause;
    }
    
   
    Struct getHead() {
        return head;
    }    
    
    
    SubGoalTree getBody() {
        return body;
    }    
    
    String getLibraryName() {
        return libName;
    }
    
    /**
     * Perform copy for assertion operation
     */
    void performCopy() {
        AbstractMap<Var,Var> v = new LinkedHashMap<>();
        clause = (Struct) clause.copy(v, Var.ORIGINAL);
        v = new IdentityHashMap<>();
        head = (Struct)head.copy(v,Var.ORIGINAL);
        body = new SubGoalTree();
        bodyCopy(body,bodyCopy,v,Var.ORIGINAL);
    }
    
    /**
     * Perform copy for use in current engine's demostration
     * @param idExecCtx Current ExecutionContext id
     */
    void performCopy(int idExecCtx) {
        IdentityHashMap<Var,Var> v = new IdentityHashMap<>();
        headCopy = (Struct)head.copy(v,idExecCtx);
        bodyCopy = new SubGoalTree();
        bodyCopy(body,bodyCopy,v,idExecCtx);
    }
    
    private void bodyCopy(SubGoalTree source, SubGoalTree destination, AbstractMap<Var,Var> map, int id) {
        for(AbstractSubGoalTree s: source){
            if (s.isLeaf()) {
                SubGoalElement l = (SubGoalElement)s;
                Term t = l.getValue().copy(map,id);
                destination.addChild(t);
            } else {
                SubGoalTree src  = (SubGoalTree)s; 
                SubGoalTree dest = destination.addChild();
                bodyCopy(src,dest,map,id);
            }
        }
    }
    
    
    Struct getHeadCopy() {
        return headCopy;
    }
    
    
    SubGoalTree getBodyCopy() {
        return bodyCopy;
    }
    
    
    
    /**
     * Gets the string representation with default operate representation
     */
    public String toString() {
        // default prio: xfx
        String st=indentPredicates(clause.getArg(1));
        return( clause.getArg(0).toString() + " :-\n\t"+st+".\n");
    }
    
    static private String indentPredicates(Term t) {
        if (t instanceof Struct) {
            Struct co=(Struct)t;
            if (co.getName().equals(",")){
                return co.getArg(0).toString()+",\n\t"+indentPredicates(co.getArg(1));
            } else {
                return t.toString();
            }
        } else {
            return t.toString();
        }
    }
    
    /*commented by Roberta Calegari fixed following issue 20 Christian Lemke suggestion
     * static private String indentPredicatesAsArgX(Term t,OperatorManager op,int p) {
        if (t instanceof Struct) {
            Struct co=(Struct)t;
            if (co.getName().equals(",")) {
                return co.getArg(0).toStringAsArgX(op,p)+",\n\t"+
                "("+indentPredicatesAsArgX(co.getArg(1),op,p)+")";
            } else {
                return t.toStringAsArgX(op,p);
            }
        } else {
            return t.toStringAsArgX(op,p);
        }
        
    }
    
    static private String indentPredicatesAsArgY(Term t,OperatorManager op,int p) {
        if (t instanceof Struct) {
            Struct co=(Struct)t;
            if (co.getName().equals(",")) {
                return co.getArg(0).toStringAsArgY(op,p)+",\n\t"+
                "("+indentPredicatesAsArgY(co.getArg(1),op,p)+")";
            } else {
                return t.toStringAsArgY(op,p);
            }
        } else {
            return t.toStringAsArgY(op,p);
        }
    }*/
    
    static private String indentPredicatesAsArgX(Term t,OperatorManager op,int p) {
        if (t instanceof Struct) {
            Struct co=(Struct)t;
            if (co.getName().equals(",")) {
               int prio = op.opPrio(",","xfy");
               StringBuilder sb = new StringBuilder(prio >= p ? "(" : "");
               sb.append(co.getArg(0).toStringAsArgX(op,prio));
               sb.append(",\n\t");
               sb.append(indentPredicatesAsArgY(co.getArg(1),op,prio));
               if (prio >= p) sb.append(')');

               return sb.toString();

           } else {
               return t.toStringAsArgX(op,p);
           }
       } else {
           return t.toStringAsArgX(op,p);
       }
    }

    static private String indentPredicatesAsArgY(Term t,OperatorManager op,int p) {
        if (t instanceof Struct) {
            Struct co=(Struct)t;
            if (co.getName().equals(",")) {
               int prio = op.opPrio(",","xfy");
               StringBuilder sb = new StringBuilder(prio > p ? "(" : "");
               sb.append(co.getArg(0).toStringAsArgX(op,prio));
               sb.append(",\n\t");
               sb.append(indentPredicatesAsArgY(co.getArg(1),op,prio));
               if (prio > p) sb.append(')');

               return sb.toString();
            } else {
               return t.toStringAsArgY(op,p);
            }
        } else {
           return t.toStringAsArgY(op,p);
        }
    }
    
    
}