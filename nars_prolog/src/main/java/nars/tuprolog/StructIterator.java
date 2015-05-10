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

import nars.nal.term.Term;

import java.util.NoSuchElementException;

/**
 * This class represents an iterator through the arguments of a Struct list.
 *
 * @see Struct
 */
@SuppressWarnings("serial")
class StructIterator implements java.util.Iterator<Term>, java.io.Serializable {
    
    Struct list;
    
    StructIterator(Struct t) {
        this.list = t;
    }
    
    public boolean hasNext() {
        return !list.isEmptyList();
    }


    public Term next() {
        final Struct list = this.list;
        if (list.isEmptyList())
            throw new NoSuchElementException();
        // Using Struct#getTerm(int) instead of Struct#listHead and Struct#listTail
        // to avoid redundant Struct#isList calls since it is only possible to get
        // a StructIterator on a Struct instance which is already a list.
        Term head = list.getTerm(0);
        this.list = (Struct) list.getTerm(1);
        return head;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}