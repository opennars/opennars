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
package nars.tuprolog.lib;

import nars.nal.term.Term;
import nars.tuprolog.Library;
import nars.tuprolog.PTerm;
import nars.tuprolog.PrologError;
import nars.tuprolog.Var;

/**
 * Library for managing DCGs.
 * 
 * Library/Theory dependency: BasicLibrary
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class EDCGLibrary extends Library {

    public EDCGLibrary() {
    }

    public String getTheory() {
        return ":- op(1200, xfx, '==>').\n" 
            +":- op(200, xfx, '\\'). \n" 
            +":- op(200, xfx, ';'). \n"
            +":- op(200, fx, '*'). %Zero or more productions \n"
            +":- op(200, fx, '+'). %One or more productions \n"
            +":- op(200, fx, '?'). %Zero or one production \n"
            +":- op(200, fx, '^'). %Exactly N productions (for parsing only) \n"
            +":- op(200, fx, '#'). %Exactly N productions (for AST generation) \n"
            +"edcg_parse(*(A,_,[]),LO \\ LO). \n"
            +"edcg_parse(*(A,X,[X|L]), LI \\ LO) :- edcg_parse(A, LI \\ L1), edcg_parse(*(A,X,L),L1 \\ LO). \n"
            +"edcg_parse(*(A), LI \\ LO) :- ((edcg_parse(A,LI \\ L1), LI\\=L1, edcg_parse(*A, L1 \\ LO));LI=LO). \n"
            +"edcg_parse(+(A,X,[X|L]), LI \\ LO) :- edcg_parse(A, LI \\ L1), edcg_parse(*(A,X,L),L1 \\ LO). \n" 
            +"edcg_parse(+(A), LI \\ LO) :- (edcg_parse(A,LI \\ L1), LI\\=L1, edcg_parse(*A,L1 \\ LO)). \n"
            +"edcg_parse(?(A,_,E2,E2), LO \\ LO). \n"
            +"edcg_parse(?(A,E1,_,E1), LI \\ LO) :- edcg_parse(A, LI \\ LO). \n" 
            +"edcg_parse(?(A),LI \\ LO) :- edcg_parse(A, LI \\ LO);LI=LO. \n"               
            +"edcg_parse((A;B), Tokens) :- edcg_parse(A, Tokens);edcg_parse(B, Tokens). \n"
            +"edcg_parse(#(A,N,X,L), LI \\ LO) :- edcg_power(#(A,N,0,X,L),LI \\ LO). \n"
            +"edcg_power(#(A,N,N,_,[]),LO \\ LO). \n"
            +"edcg_power(#(A,N,M,X,[X|L]), LI \\ LO) :- M1 is M+1, !,edcg_parse(A, LI \\ L1), edcg_power(#(A,N,M1,X,L),L1 \\ LO). \n"
            +"edcg_parse(^(A,N), LI \\ LO) :- edcg_power(^(A,N,0),LI \\ LO). \n"
            +"edcg_power(^(A,N,N),LO \\ LO). \n" 
            +"edcg_power(^(A,N,M), LI \\ LO) :- M1 is M+1, !,edcg_parse(A, LI \\ L1), edcg_power(^(A,N,M1),L1 \\ LO). \n"
            +"edcg_nonterminal(X) :- list(X), !, fail. \n"
            +"edcg_nonterminal(_). \n"
            +"edcg_terminals(Xs) :- list(Xs). \n"
            +"edcg_phrase(Category, String, Left) :- edcg_parse(Category, String \\ Left). \n"
            +"edcg_phrase(Category, [H | T]) :- edcg_parse(Category, [H | T] \\ []). \n" 
            +"edcg_phrase(Category,[]) :- edcg_parse(Category, [] \\ []). \n"
            +"edcg_parse(A, Tokens) :- edcg_nonterminal(A), (A ==> B), edcg_parse(B, Tokens). \n"
            +"edcg_parse((A, B), Tokens \\ Xs) :- edcg_parse(A, Tokens \\ Tokens1), edcg_parse(B, Tokens1 \\ Xs). \n"
            +"edcg_parse(A, Tokens) :- edcg_terminals(A), edcg_connect(A, Tokens). \n"
            +"edcg_parse({A}, Xs \\ Xs) :- call(A). \n"
            +"edcg_connect([], Xs \\ Xs). \n"
            +"edcg_connect([W | Ws], [W | Xs] \\ Ys) :- edcg_connect(Ws, Xs \\ Ys). \n";
    }

    // Java guards for Prolog predicates

    public boolean phrase_guard_2(Term arg0, PTerm arg1) throws PrologError {
        arg0 = arg0.getTerm();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(engine, 1);
        return true;
    }

    public boolean phrase_guard_3(Term arg0, PTerm arg1, PTerm arg2) throws PrologError {
        arg0 = arg0.getTerm();
        if (arg0 instanceof Var)
            throw PrologError.instantiation_error(engine, 1);
        return true;
    }

}