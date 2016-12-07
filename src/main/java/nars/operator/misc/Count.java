/*
 * Copyright (C) 2014 peiwang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.operator.misc;

import nars.language.CompoundTerm;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Term;
import nars.operator.SynchronousFunctionOperator;
import nars.storage.Memory;

/**
 * Count the number of elements in a set
 * 

'INVALID
(^count,a)!
(^count,a,b)!
(^count,a,#b)!

'VALID: 
(^count,[a,b],#b)!

 * 
 */
public class Count extends SynchronousFunctionOperator {

    public Count() {
        super("^count");
    }

    final static String requireMessage = "Requires 1 SetExt or SetInt argument";
    
    final static Term counted = Term.get("counted");
    
    
    @Override
    protected Term function(Memory memory, Term[] x) {
        if (x.length!=1) {
            throw new RuntimeException(requireMessage);
        }

        Term content = x[0];
        if (!(content instanceof SetExt) && !(content instanceof SetInt)) {
            throw new RuntimeException(requireMessage);
        }       
        
        int n = ((CompoundTerm) content).size();
        return Term.get(n);
    }

    @Override
    protected Term getRange() {
        return counted;
    }


    
}
