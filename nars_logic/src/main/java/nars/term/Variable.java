/*
 * Variable.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.term;


import nars.Op;
import nars.Symbols;
import nars.util.data.Util;
import nars.util.utf8.Utf8;

import java.io.IOException;

/**
 * A variable term, which does not correspond to a concept
 */
abstract public class Variable extends Atom {

    protected Variable(final byte[] n) {
        super(n);
    }

//    Variable(final String name) {
//        super(Utf8.toUtf8(name));
//    }

    Variable() {
        super();
    }

    public static Variable the(Op varType, byte[] baseName) {
        return the(varType.ch, baseName);
    }

    public static Variable the(char varType, String baseName) {
        return the(varType, Utf8.toUtf8(baseName));
    }

    public static Variable the(char ch, byte[] name) {
         switch (ch) {
            case Symbols.VAR_DEPENDENT:
                return new VarDep(name);
            case Symbols.VAR_INDEPENDENT:
                return new VarIndep(name);
            case Symbols.VAR_QUERY:
                return new VarQuery(name);
            case Symbols.VAR_PATTERN:
                return new VarPattern(name);
            default:
                throw new RuntimeException("invalid variable type: " + ch);
        }

    }

    public static Variable the(Op type, int counter) {
        return the(type, Util.intAsByteArray(counter));
    }

    //TODO replace this with a generic counting method of how many subterms there are present
    public static int numPatternVariables(Term t) {
        final int[] has = {0};
        t.recurseTerms((t1, superterm) -> {
            if (t1.op() == Op.VAR_PATTERN)
                has[0]++;
        });
        return has[0];
    }

    /** necessary because VAR_PATTERN are hidden from substructure */
    public static boolean hasPatternVariable(Term t) {
        final boolean[] has = {false};
        t.recurseTerms((t1, superterm) -> {
            if (!has[0]) {
                if (t1.op() == Op.VAR_PATTERN)
                    has[0] = true;
            }
        });
        return has[0];
    }

    @Override
    public final void append(Appendable w, boolean pretty) throws IOException {
        w.append(op().ch);
        super.append(w, pretty);
    }

    @Override
    public final String toString() {
        return Utf8.fromUtf8toString(op().ch, bytes());
    }

    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public final int complexity() {       return 0;   }

    @Override public final boolean hasVar() { return true;     }


    public static final class VarDep extends Variable {

        public VarDep(byte[] name)  { super(name);        }

        @Override public final int structure() { return 1 << Op.VAR_DEPENDENT.ordinal();        }

        @Override public final Op op() { return Op.VAR_DEPENDENT; }

        @Override public final int vars() { return 1; }

        @Override public final boolean hasVarDep() { return true;  }
        @Override public final int varDep() {  return 1;        }

        @Override public final boolean hasVarIndep() { return false;  }
        @Override public final int varIndep() { return 0;}

        @Override public final boolean hasVarQuery() { return false;  }
        @Override public final int varQuery() { return 0; }

    }

    public static final class VarIndep extends Variable {

        public VarIndep(byte[] name) { super(name);         }

        @Override public final int structure() {  return 1 << Op.VAR_INDEPENDENT.ordinal();        }

        @Override public final Op op() { return Op.VAR_INDEPENDENT; }

        @Override public final int vars() { return 1; }

        @Override public final boolean hasVarDep() { return false;  }
        @Override public final int varDep() {  return 0;        }

        @Override public final boolean hasVarIndep() { return true;  }
        @Override public final int varIndep() { return 1;}

        @Override public final boolean hasVarQuery() { return false;  }
        @Override public final int varQuery() { return 0; }
    }

    public static final class VarQuery extends Variable {

        public VarQuery(byte[] name) {  super(name);         }

        @Override public final int structure() {  return 1 << Op.VAR_QUERY.ordinal();        }

        @Override public final Op op() { return Op.VAR_QUERY; }

        @Override public final int vars() { return 1; }

        @Override public final boolean hasVarDep() { return false;  }
        @Override public final int varDep() {  return 0;        }

        @Override public final boolean hasVarIndep() { return false;  }
        @Override public final int varIndep() { return 0;}

        @Override public final boolean hasVarQuery() { return true;  }
        @Override public final int varQuery() { return 1; }
    }


    public static final class VarPattern extends Variable {

        public VarPattern(byte[] name) {  super(name);         }

        @Override public final int structure() { return 0;        }

        @Override public final Op op() { return Op.VAR_PATTERN; }

        /** pattern variable hidden in the count 0 */
        @Override public final int vars() { return 0; }

        @Override public final boolean hasVarDep() { return false;  }
        @Override public final int varDep() {  return 0;        }

        @Override public final boolean hasVarIndep() { return false;  }
        @Override public final int varIndep() { return 0;}

        @Override public final boolean hasVarQuery() { return false;  }
        @Override public final int varQuery() { return 0; }
    }
}
