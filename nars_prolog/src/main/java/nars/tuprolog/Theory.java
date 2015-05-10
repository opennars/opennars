/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
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

import com.google.common.collect.Lists;
import nars.nal.term.Term;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;


/**
 * This class represents prolog theory which can be provided
 * to a prolog engine.
 *
 * Actually theory incapsulates only textual representation
 * of prolog theories, without doing any check about validity
 *
 * @see Prolog
 *
 */
@SuppressWarnings("serial")
public class Theory implements Serializable, PrologTermIterator {


    
    private String theory;
    private Struct clauseList;

    /**
     * Creates a theory getting its source text from an input stream
     *
     * @param is the input stream acting as source
     */
    public Theory(InputStream is) throws IOException {
        byte[] info = new byte[is.available()];
        is.read(info);
        theory = new String(info);
    }

    /**
     * Creates a theory from its source text
     *
     * @param theory the source text
     * @throws s InvalidTheoryException if theory is null
     */
    public Theory(String theory) throws InvalidTheoryException {
        if (theory == null) {
            throw new InvalidTheoryException();
        }
        this.theory=theory;
    }

    
    
    Theory() {
        this.theory = "";
    }

    /**
     * Creates a theory from a clause list
     *
     * @param clauseList the source text
     * @throws s InvalidTheoryException if clauseList is null or is not a prolog list
     */
    public Theory(Struct clauseList) throws InvalidTheoryException {
        if (clauseList==null || !clauseList.isList()) {
            clauseList = new Struct(new PTerm[] { clauseList } ); //wrap as list
            //throw new InvalidTheoryException();
        }
        this.clauseList = clauseList;
    }

    public Theory(PTerm[] t) throws InvalidTheoryException {
        this(new Struct(t));
    }

    public static Theory parse(Prolog engine, String input) throws InvalidTheoryException {
       Deque<Term> tc = Lists.newLinkedList(new Parser(engine.getOperators(), input));
       return new Theory(new Struct(".", tc));       
    }
    
    @Override
    public Iterator<? extends Term> iterator(Prolog engine) {
        if (isTextual())
            return new Parser(engine.getOperators(), theory).iterator();
        else
            return clauseList.listIterator();
    }


    
    /**
     * Adds (appends) a theory to this.
     *
     * @param th is the theory to be appended
     * @throws s InvalidTheoryException if the theory object are not compatibles (they are
     *  compatibles when both have been built from texts or both from clause lists)
     */
    public void append(Theory th) throws InvalidTheoryException {
        if (th.isTextual() && isTextual()) {
            theory += th.theory;
        } else if (!th.isTextual() && !isTextual()) {
            Struct otherClauseList = th.getClauseListRepresentation();
            if (clauseList.isEmptyList())
                clauseList = otherClauseList;
            else {
                Struct p = clauseList, q;
                while (!(q = (Struct) p.getTermX(1)).isEmptyList())
                    p = q;
                p.setTerm(1, otherClauseList);
            }
        } else if (!isTextual() && th.isTextual()) {
            theory = theory + '\n' + th;
            clauseList = null;
        } else if (isTextual() && !th.isTextual()) {
            theory += th.toString();
        }
        else {
            throw new InvalidTheoryException();
        }
    }

    /**
     * Checks if the theory has been built
     * from a text or a clause list
     *
     */
    boolean isTextual() {
        return theory != null;
    }

    Struct getClauseListRepresentation() {
        return clauseList;
    }

    public String toString() {
        return theory != null ? theory : clauseList.toString();
    }


}