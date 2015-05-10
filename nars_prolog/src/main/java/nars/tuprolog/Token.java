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
import java.io.Serializable;

/**
 * This class represents a token read by the prolog term tokenizer
 *
 *
 *
 */
@SuppressWarnings("serial")
class Token implements Serializable {
    // token textual representation
    final String seq;
    // token type and attribute
    final int type;
    
    public Token(String seq_,int type_) {
        seq = seq_;
        type = type_;
    }
    
    public int getType() {
        return(type & Tokenizer.TYPEMASK);
    }

    /**
     * attribute could be EOF or ERROR
     */
    public int getAttribute() {
        return type & Tokenizer.ATTRMASK;
    }

    public String getValue(){
        return seq;
    }

    public boolean isOperator(boolean commaIsEndMarker) {
        if (commaIsEndMarker && ",".equals(seq))
            return false;
        return getAttribute() == Tokenizer.OPERATOR;
    }

    public boolean isFunctor() {
        return getAttribute() == Tokenizer.FUNCTOR;
    }
    
    public boolean isNumber() {
        return type == Tokenizer.INTEGER || type == Tokenizer.FLOAT;
    }

    boolean isEOF() {
        return getAttribute() == Tokenizer.EOF;
    }

    boolean isType(int type) {
        return getType() == type;
    }
}