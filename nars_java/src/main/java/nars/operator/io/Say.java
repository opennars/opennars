/*
 * Sample.java
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
package nars.operator.io;

import nars.core.Memory;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;
import nars.operator.data.Flat;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Say extends Operator {

//    boolean rejectEmpty = true;
//    boolean rejectHasVariables = true;    
    
    public Say() {
        super("^say");
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {

//        if (rejectEmpty && args.length == 1) {
//            //SELF argument by itself is not worth speaking
//            throw NegativeFeedback.ignore("Said nothing");
//        }
//                
//        if (rejectEmpty && Terms.containsVariables(args)) {
//            throw NegativeFeedback.ignore("Said variables");
//        }
                
//        List<Term> spoken = Lists.newArrayList(args).subList(0, args.length-1);
//        List<Term> spoke2=new ArrayList<Term>();
//        for(Term t: spoken) {
//            if(t instanceof Product) {
//                CompoundTerm cn=(CompoundTerm) t;
//                for(Term k : cn) {
//                    String s=k.toString();
//                    if(s.startsWith("word-")) {
//                        spoke2.add(new Term(s.replace("word-", "")));
//                    } else {
//                        spoke2.add(k);
//                    }
//                }
//
//            } else {
//                return null;
//            }
//        }
        List<Term> spoke2 = Flat.collect(args, new ArrayList());
        memory.emit(Say.class, spoke2);
        
        return null;
    }

}
