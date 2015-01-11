%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Builtin Predicates of Prolog Cafe
% 
% Mutsunori Banbara (banbara@kobe-u.ac.jp)
% Naoyuki Tamura (tamura@kobe-u.ac.jp)
% Kobe University
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- op(1150,  fx, (package)). 
package(_). 
:- package 'jp.ac.kobe_u.cs.prolog.builtin'.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Control constructs
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public true/0, therwise/0.
:- public fail/0, false/0.
%:- public (!)/0, '$get_level'/1, '$neck_cut'/0, '$cut'/1.
:- public (!)/0.
:- public (^)/2.
:- public (',')/2.
:- public (;)/2.
:- public (->)/2.
:- public call/1.

true.
otherwise.

fail :- fail.
false :- fail.

!.
%'$get_level'(X) :- '$get_level'(X).
%'$neck_cut' :- '$neck_cut'.
%'$cut'(X) :- '$cut'(X).

(_ ^ G) :- call(G).

(P, Q) :- call(P), call(Q).

(P; _Q) :- P \= (_ -> _), call(P).
(_P; Q) :- Q \= (_ -> _), call(Q).

(IF -> THEN) :- call(IF), !, call(THEN).

(IF -> THEN; _ELSE) :- call(IF), !, call(THEN).
(_IF -> _THEN; ELSE) :- call(ELSE).

call(Term) :- 
	%'$get_level'(Cut),
	'$get_current_B'(Cut),
	'$meta_call'(Term, user, Cut, 0, interpret).

'$meta_call'(X, _, _, _, _) :- var(X), !, illarg(var, call(X), 1).
'$meta_call'(X, _, _, _, _) :- closure(X), !, '$call_closure'(X).
'$meta_call'(true, _, _, _, _) :- !.
'$meta_call'(trace, _, _, _, _) :- !, trace.
'$meta_call'(debug, _, _, _, _) :- !, debug.
'$meta_call'(notrace, _, _, _, _) :- !, notrace.
'$meta_call'(nodebug, _, _, _, _) :- !, nodebug.
'$meta_call'(spy(L), _, _, _, _) :- !, spy(L).
'$meta_call'(nospy(L), _, _, _, _) :- !, nospy(L).
'$meta_call'(nospyall, _, _, _, _) :- !, nospyall.
'$meta_call'(leash(L), _, _, _, _) :- !, leash(L).
'$meta_call'([X|Xs], _, _, _, _) :- !, consult([X|Xs]).
'$meta_call'(_^X, P, Cut, Depth, Mode) :- !,
	'$meta_call'(X, P, Cut, Depth, Mode).
'$meta_call'(P:X, _, Cut, Depth, Mode) :- !,
	'$meta_call'(X, P, Cut, Depth, Mode).
'$meta_call'(!, _, no, _, _) :- !, illarg(context(if,cut), !, 0).
'$meta_call'(!, _, Cut, _, _) :- !, '$cut'(Cut).
'$meta_call'((X,Y), P, Cut, Depth, Mode) :- !,
	'$meta_call'(X, P, Cut, Depth, Mode),
	'$meta_call'(Y, P, Cut, Depth, Mode).
'$meta_call'((X->Y;Z), P, Cut, Depth, Mode) :- !,
	(   '$meta_call'(X, P, no, Depth, Mode) -> '$meta_call'(Y, P, Cut, Depth, Mode)
        ;   '$meta_call'(Z, P, Cut, Depth, Mode)
        ).
'$meta_call'((X->Y), P, Cut, Depth, Mode) :- !,
	(   '$meta_call'(X, P, no, Depth, Mode) -> '$meta_call'(Y, P, Cut, Depth, Mode)   ).
'$meta_call'((X;Y), P, Cut, Depth, Mode) :- !,
	(   '$meta_call'(X, P, Cut, Depth, Mode) ; '$meta_call'(Y, P, Cut, Depth, Mode)   ).
'$meta_call'(\+(X), P, _, Depth, Mode) :- !,
	\+ '$meta_call'(X, P, no, Depth, Mode).
'$meta_call'(findall(X,Y,Z), P, Cut, Depth, Mode) :- !,
	findall(X, '$meta_call'(Y, P, Cut, Depth, Mode), Z).
'$meta_call'(bagof(X,Y,Z), P, Cut, Depth, Mode) :- !,
	bagof(X, '$meta_call'(Y, P, Cut, Depth, Mode), Z).
'$meta_call'(setof(X,Y,Z), P, Cut, Depth, Mode) :- !,
	setof(X, '$meta_call'(Y, P, Cut, Depth, Mode), Z).
'$meta_call'(once(X), P, Cut, Depth, Mode) :- !,
	once('$meta_call'(X, P, Cut, Depth, Mode)).
'$meta_call'(on_exception(X,Y,Z), P, Cut, Depth, Mode) :- !,
	on_exception(X, '$meta_call'(Y, P, Cut, Depth, Mode), '$meta_call'(Z, P, Cut, Depth, Mode)).
'$meta_call'(catch(X,Y,Z), P, Cut, Depth, Mode) :- !,
	catch('$meta_call'(X, P, Cut, Depth, Mode), Y, '$meta_call'(Z, P, Cut, Depth, Mode)).
%'$meta_call'(freeze(X,Y), P, Cut, Depth, Mode) :- !, ???
%	freeze(X, '$meta_call'(Y, P, Cut, Depth, Mode)).
'$meta_call'(synchronized(X,Y), P, Cut, Depth, Mode) :- !,
	synchronized(X, '$meta_call'(Y, P, Cut, Depth, Mode)).
'$meta_call'(clause(X, Y), P, _, _, _) :- !, clause(P:X, Y).
'$meta_call'(assert(X), P, _, _, _) :- !, assertz(P:X).
'$meta_call'(assertz(X), P, _, _, _) :- !, assertz(P:X).
'$meta_call'(asserta(X), P, _, _, _) :- !, asserta(P:X).
'$meta_call'(retract(X), P, _, _, _) :- !, retract(P:X).
'$meta_call'(abolish(X), P, _, _, _) :- !, abolish(P:X).
'$meta_call'(retractall(X), P, _, _, _) :- !, retractall(P:X).
'$meta_call'(X, P, _, Depth, Mode) :- atom(P), callable(X), !,
	'$meta_call'(Mode, Depth, P, X).
'$meta_call'(X, P, _, _, _) :- 
	illarg(type(callable), call(P:X), 1).

'$meta_call'(trace, Depth, P, X) :- !, 
	functor(X, F, A),
	'$trace_goal'(X, P, F/A, Depth).
'$meta_call'(interpret, Depth, P, X) :- 
	functor(X, F, A),
	'$call_internal'(X, P, F/A, Depth, interpret).

'$call_internal'(X, P, FA, Depth, Mode) :- 
	'$new_internal_database'(P),
	hash_contains_key(P, FA),
	!,
	%'$get_level'(Cut),
	'$get_current_B'(Cut),
	Depth1 is Depth + 1,
	clause(P:X, Body),
	'$meta_call'(Body, P, Cut, Depth1, Mode).
'$call_internal'(X, P, _, _, _) :- '$call'(P, X).


:- public catch/3, throw/1.
:- public on_exception/3.
%:- public raise_exception/1. (written in Java)

catch(Goal, Catch, Recovery) :-
	on_exception(Catch, Goal, Recovery).

throw(Msg) :- raise_exception(Msg).

on_exception(Catch, Goal, Recovery) :- 
	callable(Goal), 
	!,
	'$on_exception'(Catch, Goal, Recovery).
on_exception(Catch, Goal, Recovery) :- 
	illarg(type(callable), on_exception(Catch,Goal,Recovery), 2).

'$on_exception'(_Catch, Goal, _Recovery) :-
	'$set_exception'('$none'),
	'$begin_exception'(L),
	call(Goal),
	'$end_exception'(L).
'$on_exception'(Catch, _Goal, Recovery) :-
        '$get_exception'(Msg),
        Msg \== '$none',
	'$catch_and_throw'(Msg, Catch, Recovery).

'$catch_and_throw'(Msg, Msg, Recovery) :-  !, 
        '$set_exception'('$none'),
	call(Recovery).
'$catch_and_throw'(Msg, _, _) :-  
	raise_exception(Msg).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Term unification
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public (=)/2, '$unify'/2.
:- public (\=)/2, '$not_unifiable'/2.

X = Y :- X = Y.
'$unify'(X, Y) :- '$unify'(X, Y).

X \= Y :- X \= Y.
'$not_unifiable'(X, Y) :- '$not_unifiable'(X, Y).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Type testing
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public var/1, atom/1, integer/1, float/1, atomic/1, compound/1, nonvar/1, number/1.
:- public java/1, java/2, closure/1.
:- public ground/1, callable/1.

var(X) :- var(X).

atom(X) :- atom(X).

integer(X) :- integer(X).

float(X) :- float(X).

atomic(X) :- atomic(X).

nonvar(X) :- nonvar(X).

number(X) :- number(X).

java(X) :- java(X).
java(X, Y) :- java(X, Y).

closure(X) :- closure(X).

ground(X) :- ground(X).

compound(X) :- nonvar(X), functor(X, _, A), A > 0.

callable(X) :- atom(X), !.
callable(X) :- compound(X), !.
callable(X) :- closure(X).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Term comparison
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public (==)/2,  '$equality_of_term'/2.
:- public (\==)/2, '$inequality_of_term'/2.
:- public (@<)/2,  '$before'/2.
:- public (@>)/2,  '$after'/2.
:- public (@=<)/2, '$not_after'/2.
:- public (@>=)/2, '$not_before'/2.
:- public (?=)/2,  '$identical_or_cannot_unify'/2.
:- public compare/3.
% :- public sort/2.    witten in Java
% :- public keysort/2. witten in Java
% :- public merge/3.

X == Y :- X == Y.
'$equality_of_term'(X, Y) :- '$equality_of_term'(X, Y).

X \== Y :- X \== Y.
'$inequality_of_term'(X, Y) :- '$inequality_of_term'(X, Y).

X @< Y :- X @< Y.
'$before'(X, Y) :- '$before'(X, Y).

X @> Y :- X @> Y.
'$after'(X, Y) :- '$after'(X, Y).

X @=< Y :- X @=< Y.
'$not_after'(X, Y) :- '$not_after'(X, Y).

X @>= Y :- X @>= Y.
'$not_before'(X, Y) :- '$not_before'(X, Y).

?=(X, Y) :- ?=(X, Y).
'$identical_or_cannot_unify'(X, Y) :- '$identical_or_cannot_unify'(X, Y).

compare(Op, X, Y) :- '$compare0'(Op0, X, Y), '$map_compare_op'(Op0, Op).

'$compare0'(Op0, X, Y) :-
	'$INSERT_AM'([deref(a(2),a(2)),deref(a(3),a(3))]),
	'$INSERT'(['\tif(! a1.unify(new IntegerTerm(a2.compareTo(a3)), engine.trail))',
	           '\t\treturn engine.fail();']).

'$map_compare_op'(Op0, Op) :- Op0 =:= 0, !, Op = (=).
'$map_compare_op'(Op0, Op) :- Op0 < 0, !, Op = (<).
'$map_compare_op'(Op0, Op) :- Op0 > 0, !, Op = (>).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Term creation and decomposition
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%:- public arg/3.     --> written in Java
%:- public functor/3. --> written in Java
:- public (=..)/2.
:- public copy_term/2.

Term =.. List :- Term =.. List.

copy_term(X, Y) :- copy_term(X, Y).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Arithmetic evaluation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public (is)/2.
:- public '$abs'/2, '$asin'/2, '$acos'/2, '$atan'/2.
:- public '$bitwise_conj'/3, '$bitwise_disj'/3, '$bitwise_exclusive_or'/3, '$bitwise_neg'/2.
:- public '$ceil'/2, '$cos'/2.
:- public '$degrees'/2.
:- public '$exp'/2.
:- public '$float'/2, '$float_integer_part'/2, '$float_fractional_part'/2, '$float_quotient'/3, '$floor'/2.
:- public '$int_quotient'/3.
:- public '$log'/2.
:- public '$max'/3, '$min'/3, '$minus'/3, '$mod'/3, '$multi'/3.
:- public '$plus'/3, '$pow'/3.
:- public '$radians'/2, '$rint'/2, '$round'/2.
:- public '$shift_left'/3, '$shift_right'/3, '$sign'/2, '$sin'/2, '$sqrt'/2.
:- public '$tan'/2, '$truncate'/2.

Z is Y :- Z is Y.

'$abs'(X, Y) :- '$abs'(X, Y).
'$asin'(X, Y) :- '$asin'(X, Y).
'$acos'(X, Y) :- '$acos'(X, Y).
'$atan'(X, Y) :- '$atan'(X, Y).
'$bitwise_conj'(X, Y, Z) :- '$bitwise_conj'(X, Y, Z).
'$bitwise_disj'(X, Y, Z) :- '$bitwise_disj'(X, Y, Z).
'$bitwise_exclusive_or'(X, Y, Z) :- '$bitwise_exclusive_or'(X, Y, Z).
'$bitwise_neg'(X, Y) :- '$bitwise_neg'(X, Y).
'$ceil'(X, Y) :- '$ceil'(X, Y).
'$cos'(X, Y) :- '$cos'(X, Y).
'$degrees'(X, Y) :- '$degrees'(X, Y).
'$exp'(X, Y) :- '$exp'(X, Y).
'$float'(X, Y) :- '$float'(X, Y).
'$float_integer_part'(X, Y) :- '$float_integer_part'(X, Y).
'$float_fractional_part'(X, Y) :- '$float_fractional_part'(X, Y).
'$float_quotient'(X, Y, Z) :- '$float_quotient'(X, Y, Z).
'$floor'(X, Y) :- '$floor'(X, Y).
'$int_quotient'(X, Y, Z) :- '$int_quotient'(X, Y, Z).
'$log'(X, Y) :- '$log'(X, Y).
'$max'(X, Y, Z) :- '$max'(X, Y, Z).
'$min'(X, Y, Z) :- '$min'(X, Y, Z).
'$minus'(X, Y, Z) :- '$minus'(X, Y, Z).
'$mod'(X, Y, Z) :- '$mod'(X, Y, Z).
'$multi'(X, Y, Z) :- '$multi'(X, Y, Z).
'$plus'(X,Y,Z) :- '$plus'(X,Y,Z).
'$pow'(X, Y, Z) :- '$pow'(X, Y, Z).
'$radians'(X, Y) :- '$radians'(X, Y).
'$rint'(X, Y) :- '$rint'(X, Y).
'$round'(X, Y) :- '$round'(X, Y).
'$shift_left'(X, Y, Z) :- '$shift_left'(X, Y, Z).
'$shift_right'(X, Y, Z) :- '$shift_right'(X, Y, Z).
'$sign'(X, Y) :- '$sign'(X, Y).
'$sin'(X, Y) :- '$sin'(X, Y).
'$sqrt'(X, Y) :- '$sqrt'(X, Y).
'$tan'(X, Y) :- '$tan'(X, Y).
'$truncate'(X, Y) :- '$truncate'(X, Y).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Arithmetic comparison
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public (=:=)/2, '$arith_equal'/2.
:- public (=\=)/2, '$arith_not_equal'/2.
:- public (<)/2, '$less_than'/2.
:- public (=<)/2, '$less_or_equal'/2.
:- public (>)/2, '$greater_than'/2.
:- public (>=)/2, '$greater_or_equal'/2.

X =:= Y :- X =:= Y.
'$arith_equal'(X, Y) :- '$arith_equal'(X, Y).

X =\= Y :- X =\= Y.
'$arith_not_equal'(X, Y) :- '$arith_not_equal'(X, Y).

X < Y :- X < Y.
'$less_than'(X, Y) :- '$less_than'(X, Y).

X =< Y :- X =< Y.
'$less_or_equal'(X, Y) :- '$less_or_equal'(X, Y).

X > Y :- X > Y.
'$greater_than'(X, Y) :- '$greater_than'(X, Y).

X >= Y :- X >= Y.
'$greater_or_equal'(X, Y) :- '$greater_or_equal'(X, Y).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Clause retrieval and information
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public clause/2.
:- public (initialization)/2.
:- public '$new_indexing_hash'/3.

clause(Head, B) :-
	'$head_to_term'(Head, H, P:PI, clause(Head,B)),
	'$new_internal_database'(P),
	'$check_procedure_permission'(P:PI, access, private_procedure, clause(Head, B)), 
	'$clause_internal'(P, PI, H, Cl, _),
	%(ground(Cl) -> Cl = (H :- B) ; copy_term(Cl, (H :- B))). ???
	copy_term(Cl, (H :- B)).

% head --> term
'$head_to_term'(H, T, Pkg:F/A, Goal) :- 
	'$head_to_term'(H, T, user, Pkg, Goal),
	functor(T, F, A).

'$head_to_term'(H, _, _, _, Goal) :- var(H), !,
	illarg(var, Goal, 1).
'$head_to_term'(P:H, T, _, Pkg, Goal) :- !,
	'$head_to_term'(H, T, P, Pkg, Goal).
'$head_to_term'(H, H, Pkg, Pkg, _) :- callable(H), atom(Pkg), !.
'$head_to_term'(_, _, _, _, Goal) :- 
	illarg(type(callable), Goal, 1).

% creates an internal database for A if no exists.
'$new_internal_database'(A) :-
	atom(A),
	'$get_hash_manager'(HM),
	'$new_internal_database'(HM, A).

'$new_internal_database'(HM, A) :-
	hash_contains_key(HM, A),
	!.
'$new_internal_database'(_, A) :-
	new_hash(_, [alias(A)]),
	'$init_internal_database'(A).

'$init_internal_database'(A) :-
	'$compiled_predicate'(A, '$init', 0),
	call(A:'$init'),
	!.
'$init_internal_database'(_).

% checks if the internal database of A exists.
'$defined_internal_database'(A) :-
	atom(A),
	'$get_hash_manager'(HM),
	hash_contains_key(HM, A).

% repeatedly finds dynamic clauses.
'$clause_internal'(P, PI, H, Cl, Ref) :-
	hash_contains_key(P, PI),
	'$get_indices'(P, PI, H, RevRefs),
	'$get_instances'(RevRefs, Cls_Refs),
	% ???
	%length(Cls_Refs,N),
	%'$fast_write'([clause_internal,N,for,P,PI]),nl,
	%
	'$clause_internal0'(Cls_Refs, Cl, Ref).

'$clause_internal0'([], _, _) :- fail.
'$clause_internal0'([(Cl,Ref)], Cl, Ref) :- !.
'$clause_internal0'(L, Cl, Ref) :-
	'$builtin_member'((Cl,Ref), L).

'$get_indices'(P, PI, H, Refs) :-
	'$new_indexing_hash'(P, PI, IH),
	'$calc_indexing_key'(H, Key),
	(    hash_contains_key(IH, Key) -> hash_get(IH, Key, Refs)
             ; 
             hash_get(IH, var, Refs)
        ).

% finds the indexing hashtable for P:PI. creates it if no exist.
'$new_indexing_hash'(P, PI, IH) :-
	hash_contains_key(P, PI),
	!,
	hash_get(P, PI, IH).
'$new_indexing_hash'(P, PI, IH) :-
	new_hash(IH),
	hash_put(IH, all, []),
	hash_put(IH, var, []),
	hash_put(IH, lis, []),
	hash_put(IH, str, []),
	hash_put(P, PI, IH).

'$calc_indexing_key'(H, all) :- atom(H), !.
'$calc_indexing_key'(H, Key) :- 
	arg(1, H, A1), 
	'$calc_indexing_key0'(A1, Key).

'$calc_indexing_key0'(A1, all) :- var(A1), !.
'$calc_indexing_key0'(A1, lis) :- A1 = [_|_], !.
'$calc_indexing_key0'(A1, str) :- compound(A1), !.
'$calc_indexing_key0'(A1, Key) :- ground(A1), !, '$term_hash'(A1, Key).
'$calc_indexing_key0'(A1, Key) :- illarg(type(term), '$calc_indexing_key0'(A1,Key), 1).

% checks the permission of predicate P:F/A.
'$check_procedure_permission'(P:F/A, _Operation, _ObjType, _Goal) :- 
	hash_contains_key(P, F/A),
	!.
'$check_procedure_permission'(P:F/A, Operation, ObjType, Goal) :- 
	'$compiled_predicate_or_builtin'(P, F, A),
	!,
        illarg(permission(Operation,ObjType,P:F/A,_), Goal, _).
'$check_procedure_permission'(_, _, _, _).

% checks if predicate P:F/A is compiled or not.
'$compiled_predicate'(P, F, A) :-
	'$INSERT_AM'([deref(a(1),a(1)),deref(a(2),a(2)),deref(a(3),a(3))]),
	'$INSERT'(['\tif(! engine.pcl.definedPredicate(((SymbolTerm)a1).name(), ((SymbolTerm)a2).name(), ((IntegerTerm)a3).intValue()))',
	           '\t\treturn engine.fail();']).

'$compiled_predicate_or_builtin'(P, F, A) :-
	'$INSERT_AM'([deref(a(1),a(1)),deref(a(2),a(2)),deref(a(3),a(3))]),
	'$INSERT'(['\tif(! engine.pcl.definedPredicate(((SymbolTerm)a1).name(), ((SymbolTerm)a2).name(), ((IntegerTerm)a3).intValue()) && ! engine.pcl.definedPredicate("jp.ac.kobe_u.cs.prolog.builtin", ((SymbolTerm)a2).name(), ((IntegerTerm)a3).intValue()))',
	           '\t\treturn engine.fail();']).

% initialize internal databases of given packages.
%initialization([], Goal) :- !, call(Goal).
initialization([], Goal) :- !, once(Goal).
initialization([P|Ps], Goal) :-
	'$new_internal_database'(P),
	initialization(Ps, Goal).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Clause creation and destruction
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public assert/1.
:- public assertz/1.
:- public asserta/1.
:- public retract/1.
:- public abolish/1.
:- public retractall/1.

assert(T) :-assertz(T).

assertz(T) :-
	'$term_to_clause'(T, Cl, P:PI, assertz(T)),
	'$new_internal_database'(P),
	'$check_procedure_permission'(P:PI, modify, static_procedure, assertz(T)),
	copy_term(Cl, NewCl),
	'$insert'(NewCl, Ref),
	%'$fast_write'([intert,NewCl,Ref]), nl, %???
	'$update_indexing'(P, PI, Cl, Ref, 'z'),
	fail.
assertz(_).

asserta(T) :- 
	'$term_to_clause'(T, Cl, P:PI, asserta(T)),
	'$new_internal_database'(P),
	'$check_procedure_permission'(P:PI, modify, static_procedure, asserta(T)),
	copy_term(Cl, NewCl),
	'$insert'(NewCl, Ref),
	%'$fast_write'([insert,NewCl,Ref]), nl, %???
	'$update_indexing'(P, PI, Cl, Ref, 'a'),
	fail.
asserta(_).

abolish(T) :- 
	'$term_to_predicateindicator'(T, P:PI, abolish(T)),
	'$new_internal_database'(P),
	'$check_procedure_permission'(P:PI, modify, static_procedure, abolish(T)),
	'$new_indexing_hash'(P, PI, IH),
	hash_get(IH, all, Refs),
	%'$fast_write'([erase_all,Refs]), nl, %???
	'$erase_all'(Refs),
	hash_remove(P, PI),
	fail.
abolish(_).

retract(Cl) :-
	'$clause_to_term'(Cl, T, P:PI, retract(Cl)),
	'$new_internal_database'(P),
	'$check_procedure_permission'(P:PI, access, static_procedure, retract(Cl)),
	T = (H :- _),
	'$clause_internal'(P, PI, H, Cl0, Ref),
	copy_term(Cl0, T),
	%'$fast_write'([erase,Cl0,Ref]), nl, %???
	'$erase'(Ref),
	'$rehash_indexing'(P, PI, Ref).

retractall(Head) :- 
	'$head_to_term'(Head, H, P:PI, retractall(Head)),
	'$new_internal_database'(P),
	'$check_procedure_permission'(P:PI, access, static_procedure, retractall(Head)),
	'$clause_internal'(P, PI, H, Cl, Ref),
	copy_term(Cl, (H :- _)),
	%'$fast_write'([erase,Cl,Ref]), nl, %???
	'$erase'(Ref),
	'$rehash_indexing'(P, PI, Ref),
	fail.
retractall(_).

% term --> clause (for assert)
'$term_to_clause'(Cl0, Cl, Pkg:F/A, Goal) :- 
	'$term_to_clause'(Cl0, Cl, user, Pkg, Goal),
	Cl = (H :- _),
	functor(H, F, A).

'$term_to_clause'(Cl0, _, _, _, Goal) :- var(Cl0), !,
	illarg(var, Goal, 1).
'$term_to_clause'(_, _, Pkg0, _, Goal) :- var(Pkg0), !,
	illarg(var, Goal, 1).
'$term_to_clause'(P:Cl0, Cl, _, Pkg, Goal) :- !,
	'$term_to_clause'(Cl0, Cl, P, Pkg, Goal).
'$term_to_clause'(_, _, Pkg0, _, Goal) :- \+(atom(Pkg0)), !,
	illarg(type(atom), Goal, 1).
'$term_to_clause'((H0 :- B0), (H :- B), Pkg, Pkg, Goal) :- !,
	'$term_to_head'(H0, H, Pkg, Goal),
	'$term_to_body'(B0, B, Pkg, Goal).
'$term_to_clause'(H0, (H :- true), Pkg, Pkg, Goal) :-
	'$term_to_head'(H0, H, Pkg, Goal).

'$term_to_head'(H, H, _, _) :- atom(H), !.
'$term_to_head'(H, H, _, _) :- compound(H), !.
'$term_to_head'(_, _, _, Goal) :- 
	illarg(type(callable), Goal, 1).

'$term_to_body'(B0, B, Pkg, _) :- 
	'$localize_body'(B0, Pkg, B).

'$localize_body'(G, P, G1) :- var(G), !,
	'$localize_body'(call(G), P, G1).
'$localize_body'(P:G, _, G1) :- !,
	'$localize_body'(G, P, G1).
'$localize_body'((X,Y), P, (X1,Y1)) :- !,
	'$localize_body'(X, P, X1),
	'$localize_body'(Y, P, Y1).
'$localize_body'((X->Y), P, (X1->Y1)) :- !,
	'$localize_body'(X, P, X1),
	'$localize_body'(Y, P, Y1).
'$localize_body'((X;Y), P, (X1;Y1)) :- !,
	'$localize_body'(X, P, X1),
	'$localize_body'(Y, P, Y1).	
'$localize_body'(G, P, G1) :-
	functor(G, F, A),
	'$builtin_meta_predicates'(F, A, M), %???
	!,
	G  =.. [F|As],
	'$localize_args'(M, As, P, As1),
	G1 =.. [F|As1].
'$localize_body'(G, P, call(P:G)) :- var(P), !.
'$localize_body'(G, user, G) :- !.
'$localize_body'(G, _, G) :- system_predicate(G), !.
'$localize_body'(G, P, P:G).

'$localize_args'([], [], _, []) :- !.
'$localize_args'([:|Ms], [A|As], P, [P:A|As1]) :-
	(var(A) ; A \= _:_),
	!,
	'$localize_args'(Ms, As, P, As1).
'$localize_args'([_|Ms], [A|As], P, [A|As1]) :-
	'$localize_args'(Ms, As, P, As1).

'$builtin_meta_predicates'((^), 2, [?,:]).
'$builtin_meta_predicates'(call, 1, [:]).
'$builtin_meta_predicates'(once, 1, [:]).
'$builtin_meta_predicates'((\+), 1, [:]).
'$builtin_meta_predicates'(findall, 3, [?,:,?]).
'$builtin_meta_predicates'(setof, 3, [?,:,?]).
'$builtin_meta_predicates'(bagof, 3, [?,:,?]).
'$builtin_meta_predicates'(on_exception, 3, [?,:,:]).
'$builtin_meta_predicates'(catch, 3, [:,?,:]).
'$builtin_meta_predicates'(synchronized, 2, [?,:]).
'$builtin_meta_predicates'(freeze, 2, [?,:]).

% clause --> term (for retract)
'$clause_to_term'(Cl, T, Pkg:F/A, Goal) :- 
	'$clause_to_term'(Cl, T, user, Pkg, Goal),
	T = (H :- _),
	functor(H, F, A).

'$clause_to_term'(Cl, _, _, _, Goal) :- var(Cl), !,
	illarg(var, Goal, 1).
'$clause_to_term'(_, _, Pkg, _, Goal) :- var(Pkg), !,
	illarg(var, Goal, 1).
'$clause_to_term'(P:Cl, T, _, Pkg, Goal) :- !,
	'$clause_to_term'(Cl, T, P, Pkg, Goal).
'$clause_to_term'(_, _, Pkg, _, Goal) :- \+(atom(Pkg)), !,
	illarg(type(atom), Goal, 1).
'$clause_to_term'((H0 :- B), (H :- B), Pkg, Pkg, Goal) :- !,
	'$head_to_term'(H0, H, _, Goal).
	%'$body_to_term'(B0, B, Goal).
'$clause_to_term'(H0, (H :- true), Pkg, Pkg, Goal) :-
	'$head_to_term'(H0, H, _, Goal).

% term --> predicate indicator (for abolish)
'$term_to_predicateindicator'(T, Pkg:PI, Goal) :- 
	'$term_to_predicateindicator'(T, PI, user, Pkg, Goal).

'$term_to_predicateindicator'(T, _, _, _, Goal) :- var(T), !,
	illarg(var, Goal, 1).
'$term_to_predicateindicator'(_, _, Pkg, _, Goal) :- var(Pkg), !,
	illarg(var, Goal, 1).
'$term_to_predicateindicator'(P:T, PI, _, Pkg, Goal) :- !,
	'$term_to_predicateindicator'(T, PI, P, Pkg, Goal).
'$term_to_predicateindicator'(T, _, _, _, Goal) :- T \= _/_, !,
	illarg(type('predicate_indicator'), Goal, 1).
'$term_to_predicateindicator'(F/_, _, _, _, Goal) :- \+ atom(F), !,
	illarg(type(atom), Goal, 1).
'$term_to_predicateindicator'(_/A, _, _, _, Goal) :- \+ integer(A), !,
	illarg(type(integer), Goal, 1).
'$term_to_predicateindicator'(T, T, Pkg, Pkg, _).

'$update_indexing'(P, PI, Cl, Ref, A_or_Z) :-
	'$new_indexing_hash'(P, PI, IH),
	'$gen_indexing_keys'(Cl, IH, Keys),
	%'$fast_write'([update_indexing,P,PI,Cl,Ref,Keys]), nl, %???
	'$update_indexing_hash'(A_or_Z, Keys, IH, Ref).

'$gen_indexing_keys'((H :- _), _, [all]) :- atom(H), !.
'$gen_indexing_keys'((H :- _), IT, Keys) :- 
	arg(1, H, A1), 
	'$gen_indexing_keys0'(A1, IT, Keys).

'$gen_indexing_keys0'(A1, IT,      Keys) :- var(A1), !, hash_keys(IT, Keys).
'$gen_indexing_keys0'(A1,  _, [all,lis]) :- A1 = [_|_], !.
'$gen_indexing_keys0'(A1,  _, [all,str]) :- compound(A1), !.
'$gen_indexing_keys0'(A1, IT, [all,Key]) :- ground(A1),  !,
	'$term_hash'(A1, Key), % get the hash code of A1
	(    hash_contains_key(IT, Key) -> true
             ;
             hash_get(IT, var, L), hash_put(IT, Key, L)
        ).
'$gen_indexing_keys0'(A1, IT, Keys) :- 
	illarg(type(term), '$gen_indexing_keys0'(A1,IT,Keys), 1).

'$update_indexing_hash'(a, Keys, IH, Ref) :- !, '$hash_addz_all'(Keys, IH, Ref).
'$update_indexing_hash'(z, Keys, IH, Ref) :- !, '$hash_adda_all'(Keys, IH, Ref).

'$hash_adda_all'([], _, _) :- !.
'$hash_adda_all'([K|Ks], H, X) :- 
	'$hash_adda'(H, K, X), 
	'$hash_adda_all'(Ks, H, X).

'$hash_addz_all'([], _, _) :- !.
'$hash_addz_all'([K|Ks], H, X) :-
	'$hash_addz'(H, K, X),
	'$hash_addz_all'(Ks, H, X).

'$erase_all'([]) :- !.
'$erase_all'([R|Rs]) :- '$erase'(R), '$erase_all'(Rs).

'$rehash_indexing'(P, PI, Ref) :-
	'$new_indexing_hash'(P, PI, IH),
	hash_keys(IH, Keys),
	%'$fast_write'([rehash_indexing,P,PI,Keys]), nl, %???
	'$remove_index_all'(Keys, IH, Ref).

'$remove_index_all'([], _, _) :- !.
'$remove_index_all'([K|Ks], IH, Ref) :-
	'$hash_remove_first'(IH, K, Ref),
	'$remove_index_all'(Ks, IH, Ref).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% All solutions
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public findall/3.
:- public bagof/3.
:- public setof/3.

% findall/3
findall(Template, Goal, Instances) :- callable(Goal), !,
	new_hash(H),
	'$findall'(H, Template, Goal, Instances).
findall(Template, Goal, Instances) :- 
	illarg(type(callable), findall(Template,Goal,Instances), 2).

'$findall'(H, Template, Goal, _) :-
	call(Goal), 
	copy_term(Template, CT),
	'$hash_adda'(H, '$FINDALL', CT),
	fail.
'$findall'(H, _, _, Instances) :-
	hash_get(H, '$FINDALL', Vs),
	'$builtin_reverse'(Vs, Instances).

% bagof/3 & setof/3
bagof(Template, Goal, Instances) :- callable(Goal), !,	
	'$bagof'(Template, Goal, Instances).
bagof(Template, Goal, Instances) :- 
	illarg(type(callable), bagof(Template,Goal,Instances), 2).

setof(Template, Goal, Instances) :- callable(Goal), !,	
	'$bagof'(Template, Goal, Instances0),
	sort(Instances0, Instances).
setof(Template, Goal, Instances) :- 
	illarg(type(callable), setof(Template,Goal,Instances), 2).

'$bagof'(Template, Goal, Instances) :- 
	'$free_variables_set'(Goal, Template, FV),
	%write('Goal = '), write(Goal), nl,
	%write('Free variables set = '), write(FV), nl,
	FV \== [],
	!,
	Witness =.. ['$witness'|FV],
	findall(Witness+Template, Goal, S),
	'$bagof_instances'(S, Witness, Instances0),
	Instances = Instances0.
'$bagof'(Template, Goal, Instances) :- 
	findall(Template, Goal, Instances),
	Instances \== [].

'$bagof_instances'([], _Witness, _Instances) :- fail.
'$bagof_instances'(S0, Witness, Instances) :-
	S0 = [W+T|S],
	'$variants_subset'(S, W, WT_list, T_list, S_next),
	'$bagof_instances0'(S_next, Witness, Instances, [W+T|WT_list], [T|T_list]).

'$bagof_instances0'(_, Witness, Instances, WT_list, T_list) :-
	'$unify_witness'(WT_list, Witness),
	Instances = T_list.
'$bagof_instances0'(S_next, Witness, Instances, _, _) :-
	'$bagof_instances'(S_next, Witness, Instances).

'$variants_subset'([], _W, [], [], []) :- !.
'$variants_subset'([W0+T0|S], W, [W0+T0|WT_list], [T0|T_list], S_next) :-
	'$term_variant'(W, W0),
	!,
	'$variants_subset'(S, W, WT_list, T_list, S_next).
'$variants_subset'([WT|S], W, WT_list, T_list, [WT|S_next]) :-
	'$variants_subset'(S, W, WT_list, T_list, S_next).

'$term_variant'(X, Y) :- new_hash(Hash), '$term_variant'(X, Y, Hash).

'$term_variant'(X, Y, Hash) :- var(X), !,
	(   hash_contains_key(Hash, X) ->
	    hash_get(Hash, X, V), Y == V
	    ;
	    var(Y), hash_put(Hash, X, Y)
	).
'$term_variant'(X, Y, _) :- ground(X), !,
	X == Y.
'$term_variant'(_, Y, _) :- var(Y), !,
	fail.
'$term_variant'([X|Xs], [Y|Ys], Hash) :- !,
	'$term_variant'(X, Y, Hash),
	'$term_variant'(Xs, Ys, Hash).
'$term_variant'(X, Y, Hash) :- 
	X =.. Xs,
	Y =.. Ys,
	'$term_variant'(Xs, Ys, Hash).

'$unify_witness'([], _) :- !.
'$unify_witness'([W+_|WT_list], W) :-
	'$unify_witness'(WT_list, W).

% Variable set of a term
'$variables_set'(X, Vs) :- '$variables_set'(X, [], Vs).

'$variables_set'(X, Vs, Vs      ) :- var(X), '$builtin_memq'(X, Vs), !.
'$variables_set'(X, Vs, [X|Vs]  ) :- var(X), !.
'$variables_set'(X, Vs0, Vs0    ) :- atomic(X), !.
'$variables_set'([X|Xs], Vs0, Vs) :- !, 
	'$variables_set'(X, Vs0, Vs1), 
	'$variables_set'(Xs, Vs1, Vs).
'$variables_set'(X, Vs0, Vs     ) :- 
	X =.. Xs, 
	'$variables_set'(Xs, Vs0, Vs).

'$builtin_memq'(X, [Y|_])  :- X==Y, !.
'$builtin_memq'(X, [_|Ys]) :- '$builtin_memq'(X, Ys).

% Existential variables set of a term
'$existential_variables_set'(X, Vs) :- 
	'$existential_variables_set'(X, [], Vs).

'$existential_variables_set'(X, Vs, Vs) :- var(X), !.
'$existential_variables_set'(X, Vs, Vs) :- atomic(X), !.
'$existential_variables_set'(_:X, Vs0, Vs) :- !,
	'$existential_variables_set'(X, Vs0, Vs).
%'$existential_variables_set'((X;Y), Vs0, Vs) :- !,
%	'$existential_variables_set'(X, Vs0, Vs1),
%	'$existential_variables_set'(Y, Vs1, Vs).
%'$existential_variables_set'((X->Y), Vs0, Vs) :- !,
%	'$existential_variables_set'(X, Vs0, Vs1),
%	'$existential_variables_set'(Y, Vs1, Vs).
%'$existential_variables_set'((X,Y), Vs0, Vs) :- !,	
%	'$existential_variables_set'(X, Vs0, Vs1),
%	'$existential_variables_set'(Y, Vs1, Vs).
'$existential_variables_set'(^(V,G), Vs0, Vs) :- !,
	'$variables_set'(V, Vs0, Vs1),
	'$existential_variables_set'(G, Vs1, Vs).
'$existential_variables_set'('$meta_call'(G,_,_,_,_), Vs0, Vs) :- !, %???
	'$existential_variables_set'(G, Vs0, Vs).
'$existential_variables_set'(_, Vs, Vs).

% Free variables set of a term
'$free_variables_set'(T, V, FV) :- 
	'$variables_set'(T, TV),
	'$variables_set'(V, VV),
	'$existential_variables_set'(T, VV, BV),
	'$builtin_set_diff'(TV, BV, FV),
	!.

'$builtin_set_diff'(L1, L2, L) :-
	sort(L1, SL1),
	sort(L2, SL2),
	'$builtin_set_diff0'(SL1, SL2, L).

'$builtin_set_diff0'([], _, []) :- !.
'$builtin_set_diff0'(L1, [], L1) :- !.
'$builtin_set_diff0'([X|Xs], [Y|Ys], L) :- X == Y, !,
	'$builtin_set_diff0'(Xs, Ys, L).
'$builtin_set_diff0'([X|Xs], [Y|Ys], [X|L]) :- X @< Y, !,
	'$builtin_set_diff0'(Xs, [Y|Ys], L).
'$builtin_set_diff0'([X|Xs], [Y|Ys], [Y|L]) :- 
	'$builtin_set_diff0'([X|Xs], Ys, [Y|L]).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Stream selection and control
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%:- public current_input/1  (written in Java)
%:- public current_output/1 (written in Java)
%:- public set_input/1, set_output/1. (written in Java)
%:- public open/4 (written in Java)
:- public open/3.
%:- public close/2 (written in Java)
:- public close/1.
%:- public flush_output/1.(written in Java)
:- public flush_output/0.
:- public stream_property/2.

open(Source_sink, Mode, Stream) :- open(Source_sink, Mode, Stream, []).

close(S_or_a) :- close(S_or_a, []).

flush_output :- 
    current_output(S),
    flush_output(S).

stream_property(Stream, Stream_property) :- 
	var(Stream_property), 
	!,
	'$stream_property'(Stream, Stream_property).
stream_property(Stream, Stream_property) :- 
	'$stream_property_specifier'(Stream_property), 
	!,
	'$stream_property'(Stream, Stream_property).
stream_property(Stream, Stream_property) :- 
	illarg(domain(term,stream_proeprty), stream_property(Stream, Stream_property), 2).

'$stream_property'(Stream, Stream_property) :- 
	var(Stream),
	!,
	'$get_stream_manager'(SM),
	hash_map(SM, Map),
	'$builtin_member'((Stream,Vs), Map),
	java(Stream),
	'$builtin_member'(Stream_property, Vs).
'$stream_property'(Stream, Stream_property) :- 
	java(Stream),
	!,
	'$get_stream_manager'(SM),
	hash_get(SM, Stream, Vs),
	'$builtin_member'(Stream_property, Vs).
'$stream_property'(Stream, Stream_property) :- 
	illarg(domain(stream,stream), stream_property(Stream, Stream_property), 1).

'$stream_property_specifier'(input).
'$stream_property_specifier'(output).
'$stream_property_specifier'(alias(_)).
'$stream_property_specifier'(mode(_)).
'$stream_property_specifier'(type(_)).
'$stream_property_specifier'(file_name(_)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Character input/output
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%:- public get_char/2, get_code/2.   (written in Java)
%:- public peek_char/2, peek_code/2. (written in Java)
%:- public put_char/2, put_code/2.   (written in Java)
%:- public nl/0.                     (written in Java)

:- public get_char/1, get_code/1.
:- public peek_char/1, peek_code/1.
:- public put_char/1, put_code/1.
:- public nl/1.

get_char(Char)  :- current_input(S), get_char(S, Char).
get_code(Code)  :- current_input(S), get_code(S, Code).

peek_char(Char) :- current_input(S), peek_char(S, Char).
peek_code(Code) :- current_input(S), peek_code(S, Code).

put_char(Char)  :- current_output(S), put_char(S, Char).
put_code(Code)  :- current_output(S), put_code(S, Code).

nl(S) :- put_char(S, '\n').

:- public get0/1, get0/2.
:- public get/1.
%:- public get/2.  (written in Java)
:- public put/1, put/2.
:- public tab/1.
%:- public tab/2.  (written in Java)
:- public skip/1.
%:- public skip/2. (written in Java)

get0(Code)  :- current_input(S), get_code(S, Code).
get0(S_or_a, Code)  :- get_code(S_or_a, Code).

get(Code)  :- current_input(S), get(S, Code).

put(Exp)  :- current_output(S), put(S, Exp).
put(S_or_a, Exp)  :- Code is Exp, put_code(S_or_a, Code).

tab(N) :- current_output(S), tab(S, N).

skip(N) :- current_input(S), skip(S, N).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Byte input/output
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public get_byte/1, peek_byte/1, put_byte/1.
%:- public get_byte/2.  % written in java
%:- public peek_byte/2. % written in java
%:- public put_byte/2.  % written in java

get_byte(Byte) :-
    current_input(S),
    get_byte(S, Byte).

peek_byte(Byte) :-
    current_input(S),
    peek_byte(S, Byte).

put_byte(Byte) :-
    current_output(S),
    put_byte(S, Byte).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Term input/output (read)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public read/1, read/2.
:- public read_with_variables/2, read_with_variables/3.
:- public read_line/1.
%:- public read_line/2. (written in Java)
:- dynamic '$tokens'/1. 

read(X) :- current_input(S), read(S, X).

read(S_or_a, X) :-
	read_tokens(S_or_a, Tokens, _),
	parse_tokens(X, Tokens),
	!.

read_with_variables(X, Vs) :- 
	current_input(S), 
	read_with_variables(S, X, Vs).

read_with_variables(S_or_a, X, Vs) :-
	read_tokens(S_or_a, Tokens, Vs),
	parse_tokens(X, Tokens),
	!.

read_line(X) :- current_input(S), read_line(S, X).

% read_token(S_or_a, Token) reads one token from the input, 
% and unifies Token with:
%   error(Atom), 
%   end_of_file, 
%   '.', ' ', '(', ')', '[', ']', '{', '}', ',', '|', 
%   number(Integer_or_Float),
%   atom(Atom), 
%   var(Atom), 
%   string(CharCodeList)

%read_token(Token) :- current_input(S), read_token(S, Token).

read_token(S_or_a, Token) :-
	'$read_token0'(S_or_a, Type, Token0),
	'$read_token1'([Type], Token0, Token).

'$read_token1'([-2], T, error(T))  :- !. % error('message')
'$read_token1'("I",  T, number(T)) :- !. % number(intvalue)
'$read_token1'("D",  T, number(T)) :- !. % number(floatvalue)
'$read_token1'("A",  T, atom(T))   :- !. % atom('name')
'$read_token1'("V",  T, var(T))    :- !. % var('name')
'$read_token1'("S",  T, string(T)) :- !. % string("chars")
'$read_token1'(_,    T, T)         :- !. % others

% read_tokens(Tokens, Vs) reads tokens from the input 
% until full-stop-mark ('.') or end_of_file, 
% unifies Tokens with a list of tokens.
% Token for a variable has a form of var(Name,Variable).
% Vs is a list of Name=Variable pairs.

%read_tokens(Tokens, Vs) :-
%	current_input(Stream),
%	'$read_tokens'(Stream, Tokens, Vs, []),
%	!.

read_tokens(Stream, Tokens, Vs) :-
	'$read_tokens'(Stream, Tokens, Vs, []),
	!.

'$read_tokens'(Stream, Tokens, Vs, VI) :-
	read_token(Stream, Token),
	'$read_tokens1'(Stream, Token, Tokens, Vs, VI).

'$read_tokens1'(Stream, error(Message), [], _, _) :-  !,
	write('{SYNTAX ERROR}'), nl, 
	write('** '), write(Message), write(' **'), nl,
	'$read_tokens_until_fullstop'(Stream),
	fail.
'$read_tokens1'(_Stream, end_of_file, [end_of_file,'.'], [], _) :- !.
'$read_tokens1'(_Stream, '.', ['.'], [], _) :- !.
'$read_tokens1'(Stream, var('_'), [var('_',V)|Tokens], ['_'=V|Vs], VI0) :- !,
	'$read_tokens'(Stream, Tokens, Vs, ['_'=V|VI0]).
'$read_tokens1'(Stream, var(Name), [var(Name,V)|Tokens], Vs, VI) :-
	'$mem_pair'(Name=V, VI), !,
	'$read_tokens'(Stream, Tokens, Vs, VI).
'$read_tokens1'(Stream, var(Name), [var(Name,V)|Tokens], [Name=V|Vs], VI0) :- !,
	'$read_tokens'(Stream, Tokens, Vs, [Name=V|VI0]).
'$read_tokens1'(Stream, Token, [Token|Tokens], Vs, VI) :-
	'$read_tokens'(Stream, Tokens, Vs, VI).

'$mem_pair'(X1=V1, [X2=V2|_]) :- X1 == X2, !, V1 = V2.
'$mem_pair'(X, [_|L]) :- '$mem_pair'(X, L).
%'$mem_pair'(X, [_|L]) :- member(X, L).

'$read_tokens_until_fullstop'(Stream) :-
	read_token(Stream, Token),
	'$read_tokens_until_fullstop'(Stream, Token).

'$read_tokens_until_fullstop'(_Stream, end_of_file) :- !.
'$read_tokens_until_fullstop'(_Stream, '.') :- !.
'$read_tokens_until_fullstop'(Stream, _) :-
	read_token(Stream, Token),
	'$read_tokens_until_fullstop'(Stream, Token).

parse_tokens(X, Tokens) :-
	retractall('$tokens'(_)),
	assertz('$tokens'(Tokens)),
	'$parse_tokens'(X, 1201, Tokens, ['.']),
	retract('$tokens'(Tokens)),
	!.

% '$parse_tokens'(X, Prec) parses the input whose precedecence =< Prec.
'$parse_tokens'(X, Prec0) -->
	'$parse_tokens_skip_spaces',
	'$parse_tokens1'(Prec0, X1, Prec1),
	!,
	'$parse_tokens_skip_spaces',
	'$parse_tokens2'(Prec0, X1, Prec1, X, _Prec),
	!.

'$parse_tokens1'(Prec0, X1, Prec1) -->
	'$parse_tokens_peep_next'(Next),
	{'$parse_tokens_is_starter'(Next)},
	!,
	'$parse_tokens_before_op'(Prec0, X1, Prec1).
'$parse_tokens1'(_, _, _) -->
	'$parse_tokens_peep_next'(Next),
	'$parse_tokens_error'([Next,cannot,start,an,expression]).

'$parse_tokens2'(Prec0, X, Prec, X, Prec) -->
	'$parse_tokens_peep_next'(Next),
	{'$parse_tokens_is_terminator'(Next)},
	{Prec =< Prec0},
	!.
'$parse_tokens2'(Prec0, X1, Prec1, X, Prec) -->
	'$parse_tokens_peep_next'(Next),
	{'$parse_tokens_is_post_in_op'(Next)},
	!,
	'$parse_tokens_post_in_ops'(Prec0, X1, Prec1, X, Prec).
'$parse_tokens2'(_, _, _, _, _) --> 
	'$parse_tokens_error'([operator,expected,after,expression]).

% '$parse_tokens_before_op'(Prec0, X, Prec)
% parses the input until infix or postfix operator,
% and returns X and Prec
'$parse_tokens_before_op'(Prec0, X, Prec) --> [' '], !,
	'$parse_tokens_before_op'(Prec0, X, Prec).
'$parse_tokens_before_op'(_, end_of_file, 0) --> [end_of_file], !.
'$parse_tokens_before_op'(_, N, 0) --> [number(N)], !.
'$parse_tokens_before_op'(_, N, 0) -->
	[atom('-')], [number(N0)], !, {N is -N0}.
'$parse_tokens_before_op'(_, V, 0) --> [var(_,V)], !.
'$parse_tokens_before_op'(_, S, 0) --> [string(S)], !.
'$parse_tokens_before_op'(_, X, 0) --> ['('], !,
	'$parse_tokens'(X, 1201),
	'$parse_tokens_expect'(')').
'$parse_tokens_before_op'(_, X, 0) --> ['{'], !,
	'$parse_tokens_skip_spaces',
	'$parse_tokens_brace'(X).
'$parse_tokens_before_op'(_, X, 0) --> ['['], !,
	'$parse_tokens_skip_spaces',
	'$parse_tokens_list'(X).
'$parse_tokens_before_op'(_, X, 0) -->
	[atom(F)], ['('],
	!,
	'$parse_tokens_skip_spaces',
	'$parse_tokens_args'(Args),
	{X =.. [F|Args]}.
'$parse_tokens_before_op'(Prec0, X, PrecOp) -->
	[atom(F)], {current_op(PrecOp,fx,F)}, {PrecOp =< Prec0},
	'$parse_tokens_skip_spaces',
	'$parse_tokens_peep_next'(Next),
	{'$parse_tokens_is_starter'(Next)},
	{\+ '$parse_tokens_is_post_in_op'(Next)},
	!,
        {Prec1 is PrecOp - 1},
        '$parse_tokens'(Arg, Prec1),
	{functor(X, F, 1)},
	{arg(1, X, Arg)}.
'$parse_tokens_before_op'(Prec0, X, PrecOp) -->
	[atom(F)], {current_op(PrecOp,fy,F)}, {PrecOp =< Prec0},
	'$parse_tokens_skip_spaces',
	'$parse_tokens_peep_next'(Next),
	{'$parse_tokens_is_starter'(Next)},
	{\+ '$parse_tokens_is_post_in_op'(Next)},
	!,
        '$parse_tokens'(Arg, PrecOp),
	{functor(X, F, 1)},
	{arg(1, X, Arg)}.
'$parse_tokens_before_op'(_, A, 0) -->
	[atom(A)].

'$parse_tokens_brace'('{}') --> ['}'], !.
'$parse_tokens_brace'(X) -->
	'$parse_tokens'(X1, 1201),
	'$parse_tokens_expect'('}'),
	{X = {X1}}.

'$parse_tokens_list'('[]') --> [']'], !.
'$parse_tokens_list'([X|Xs]) -->
	'$parse_tokens'(X, 999),
	'$parse_tokens_skip_spaces',
	'$parse_tokens_list_rest'(Xs).

'$parse_tokens_list_rest'(Xs) --> ['|'], !,
	'$parse_tokens'(Xs, 999),
	'$parse_tokens_expect'(']').
'$parse_tokens_list_rest'([X|Xs]) --> [','], !,
	'$parse_tokens'(X, 999),
	'$parse_tokens_skip_spaces',
	'$parse_tokens_list_rest'(Xs).
'$parse_tokens_list_rest'('[]') -->
	'$parse_tokens_expect'(']').

'$parse_tokens_args'('[]') --> [')'], !.
'$parse_tokens_args'([X|Xs]) -->
	'$parse_tokens'(X, 999),
	'$parse_tokens_skip_spaces',
	'$parse_tokens_args_rest'(Xs).

'$parse_tokens_args_rest'([X|Xs]) --> [','], !,
	'$parse_tokens'(X, 999),
	'$parse_tokens_skip_spaces',
	'$parse_tokens_args_rest'(Xs).
'$parse_tokens_args_rest'('[]') -->
	'$parse_tokens_expect'(')').

% '$parse_tokens_post_in_op'(Prec0, X1, Prec1, X, Prec)
% parses the input beginning from infix or postfix operator,
% and returns X and Prec
'$parse_tokens_post_in_ops'(Prec0, X1, Prec1, X, Prec) -->
	'$parse_tokens_skip_spaces',
	[Op],
	'$parse_tokens_op'(Op, Prec0, X1, Prec1, X2, Prec2),
	'$parse_tokens_post_in_ops'(Prec0, X2, Prec2, X, Prec).
'$parse_tokens_post_in_ops'(Prec0, X, Prec, X, Prec) --> 
	{Prec =< Prec0}.

'$parse_tokens_op'(',', Prec0, X1, Prec1, X, PrecOp) --> !,
	'$parse_tokens_op'(atom(','), Prec0, X1, Prec1, X, PrecOp).
'$parse_tokens_op'('|', Prec0, X1, Prec1, X, PrecOp) --> !,
	'$parse_tokens_op'(atom(';'), Prec0, X1, Prec1, X, PrecOp).
'$parse_tokens_op'(atom(Op), Prec0, X1, Prec1, X, PrecOp) -->
	{current_op(PrecOp, xf, Op)}, {PrecOp =< Prec0},
	{Prec1 < PrecOp},
	{functor(X, Op, 1)},
	{arg(1, X, X1)}.
'$parse_tokens_op'(atom(Op), Prec0, X1, Prec1, X, PrecOp) -->
	{current_op(PrecOp, yf, Op)}, {PrecOp =< Prec0},
	{Prec1 =< PrecOp},
	{functor(X, Op, 1)},
	{arg(1, X, X1)}.
'$parse_tokens_op'(atom(Op), Prec0, X1, Prec1, X, PrecOp) -->
	{current_op(PrecOp, xfx, Op)}, {PrecOp =< Prec0},
	{Prec1 < PrecOp},
	{Prec2 is PrecOp - 1},
	'$parse_tokens'(X2, Prec2),
	!,
	{functor(X, Op, 2)},
	{arg(1, X, X1)},
	{arg(2, X, X2)}.
'$parse_tokens_op'(atom(Op), Prec0, X1, Prec1, X, PrecOp) -->
	{current_op(PrecOp, xfy, Op)}, {PrecOp =< Prec0},
	{Prec1 < PrecOp},
	{Prec2 is PrecOp},
	'$parse_tokens'(X2, Prec2),
	!,
	{functor(X, Op, 2)},
	{arg(1, X, X1)},
	{arg(2, X, X2)}.
'$parse_tokens_op'(atom(Op), Prec0, X1, Prec1, X, PrecOp) -->
	{current_op(PrecOp, yfx, Op)}, {PrecOp =< Prec0},
	{Prec1 =< PrecOp},
	{Prec2 is PrecOp - 1},
	'$parse_tokens'(X2, Prec2),
	!,
	{functor(X, Op, 2)},
	{arg(1, X, X1)},
	{arg(2, X, X2)}.

'$parse_tokens_is_starter'(end_of_file).
'$parse_tokens_is_starter'('(').
'$parse_tokens_is_starter'('[').
'$parse_tokens_is_starter'('{').
'$parse_tokens_is_starter'(number(_)).
'$parse_tokens_is_starter'(atom(_)).
'$parse_tokens_is_starter'(var(_,_)).
'$parse_tokens_is_starter'(string(_)).

'$parse_tokens_is_terminator'(')').
'$parse_tokens_is_terminator'(']').
'$parse_tokens_is_terminator'('}').
'$parse_tokens_is_terminator'('.').

'$parse_tokens_is_post_in_op'(',') :- !.
'$parse_tokens_is_post_in_op'('|') :- !.
'$parse_tokens_is_post_in_op'(atom(Op)) :-
	current_op(_, Type, Op),
	'$parse_tokens_post_in_type'(Type),
	!.

'$parse_tokens_post_in_type'(xfx).
'$parse_tokens_post_in_type'(xfy).
'$parse_tokens_post_in_type'(yfx).
'$parse_tokens_post_in_type'(xf).
'$parse_tokens_post_in_type'(yf).

'$parse_tokens_expect'(Token) -->
	'$parse_tokens_skip_spaces',
	[Token],
	!.
'$parse_tokens_expect'(Token) -->
	'$parse_tokens_error'([Token,expected]).

'$parse_tokens_skip_spaces' --> [' '], !, '$parse_tokens_skip_spaces'.
'$parse_tokens_skip_spaces' --> [].

'$parse_tokens_peep_next'(Next, S, S) :- S = [Next|_].

'$parse_tokens_error'(Message, S0, _S) :-
	write('{SYNTAX ERROR}'), nl, write('** '),
	'$parse_tokens_write_message'(Message), write(' **'), nl,
	'$parse_tokens_error1'([], S0),
	clause('$tokens'(Tokens), _),
	'$parse_tokens_error1'(Tokens, S0),
	fail.

'$parse_tokens_error1'([], _) :- !.
'$parse_tokens_error1'(Tokens, S0) :- Tokens == S0, !,
	nl, write('** here **'), nl,
	'$parse_tokens_error1'(Tokens, []), nl.
'$parse_tokens_error1'([Token|Tokens], S0) :-
	'$parse_tokens_error2'(Token),
	'$parse_tokens_error1'(Tokens, S0).

'$parse_tokens_error2'(number(X)) :- !, write(X).
'$parse_tokens_error2'(atom(X)) :- !, writeq(X).
'$parse_tokens_error2'(var(X,_)) :- !, write(X).
'$parse_tokens_error2'(string(X)) :- !,
	write('"'), '$parse_tokens_write_string'(X), write('"').
'$parse_tokens_error2'(X) :- write(X).

'$parse_tokens_write_string'([]).
'$parse_tokens_write_string'([C|Cs]) :- [C] = """", !,
	put_code(C), put_code(C), '$parse_tokens_write_string'(Cs).
'$parse_tokens_write_string'([C|Cs]) :- 
	put_code(C), '$parse_tokens_write_string'(Cs).

'$parse_tokens_write_message'([]).
'$parse_tokens_write_message'([X|Xs]) :-
	write(X), write(' '), '$parse_tokens_write_message'(Xs).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Term input/output (write)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public write/1, write/2.
:- public writeq/1, writeq/2.
:- public write_canonical/1, write_canonical/2.
:- public write_term/2, write_term/3.

write(Term) :-
	current_output(S),
	write_term(S, Term, [numbervars(true)]).

write(S_or_a, Term) :-
	write_term(S_or_a, Term, [numbervars(true)]).

writeq(Term) :-
	current_output(S),
	write_term(S, Term, [quoted(true),numbervars(true)]).

writeq(S_or_a, Term) :-
	write_term(S_or_a, Term, [quoted(true),numbervars(true)]).

write_canonical(Term) :-
	current_output(S),
	write_term(S, Term, [quoted(true),ignore_ops(true)]).

write_canonical(S_or_a, Term) :-
	write_term(S_or_a, Term, [quoted(true),ignore_ops(true)]).

write_term(Term, Options) :-
	current_output(S),
	write_term(S, Term, Options).

write_term(S_or_a, Term, Options) :-
	'$write_term'(S_or_a, Term, Options),
	fail.
write_term(_, _, _).

'$write_term'(S_or_a, Term, Options) :-
	'$write_term0'(Term, 1200, punct, _, Options, S_or_a),
	!.

'$write_term0'(Term, _Prec, Type0, alpha, _, S_or_a) :- 
	var(Term), 
	!,
	'$write_space_if_needed'(Type0, alpha, S_or_a),
	'$fast_write'(S_or_a, Term).
'$write_term0'(Term, _Prec, Type0, alpha, _, S_or_a) :- 
	java(Term), 
	!,
	'$write_space_if_needed'(Type0, alpha, S_or_a),
	'$fast_write'(S_or_a, Term).
'$write_term0'(Term, _Prec, Type0, alpha, Style, S_or_a) :-
	Term = '$VAR'(VN), integer(VN), VN >= 0,
	'$builtin_member'(numbervars(true), Style),
	!,
	'$write_space_if_needed'(Type0, alpha, S_or_a),
	'$write_VAR'(VN, S_or_a).
'$write_term0'(Term, _Prec, Type0, alpha, _, S_or_a) :- 
	number(Term), Term < 0, 
	!,
	'$write_space_if_needed'(Type0, symbol, S_or_a),
	'$fast_write'(S_or_a, Term).
'$write_term0'(Term, _Prec, Type0, alpha, _, S_or_a) :- 
	number(Term), 
	!,
	'$write_space_if_needed'(Type0, alpha, S_or_a),
	'$fast_write'(S_or_a, Term).
%'$write_term0'(Term, Prec, Type0, punct, _, S_or_a) :-
%	atom(Term), 
%	current_op(PrecOp, OpType, Term),
%	(OpType = fx ; OpType = fy),
%	PrecOp =< Prec,
%	!,
%	'$write_space_if_needed'(Type0, punct, S_or_a),
%	put_char(S_or_a, '('),
%	'$write_atom'(Term, punct, _, _, S_or_a),
%	put_char(S_or_a, ')').
'$write_term0'(Term, _Prec, Type0, Type, Style, S_or_a) :- 
	atom(Term), 
	!,
	'$write_atom'(Term, Type0, Type, Style, S_or_a).
'$write_term0'(Term, Prec, Type0, Type, Style, S_or_a) :-
	\+ '$builtin_member'(ignore_ops(true), Style),
	'$write_is_operator'(Term, Op, Args, OpType),
	!,
	'$write_term_op'(Op, OpType, Args, Prec, Type0, Type, Style, S_or_a).
'$write_term0'(Term, _Prec, Type0, punct, Style, S_or_a) :-
	Term = [_|_],
	\+ '$builtin_member'(ignore_ops(true), Style),
	!,
	'$write_space_if_needed'(Type0, punct, S_or_a),
	put_char(S_or_a, '['),
	'$write_term_list_args'(Term, punct, _, Style, S_or_a),
	put_char(S_or_a, ']').
'$write_term0'(Term, _Prec, Type0, _Type, Style, S_or_a) :-
	Term = {Term1},
	\+ '$builtin_member'(ignore_ops(true), Style),
	!,
	'$write_space_if_needed'(Type0, punct, S_or_a),
	put_char(S_or_a, '{'),
	'$write_term0'(Term1, 1200, punct, _, Style, S_or_a),
	put_char(S_or_a, '}').
'$write_term0'(Term, _Prec, Type0, punct, Style, S_or_a) :-
	Term =.. [F|Args],
	'$write_atom'(F, Type0, _, Style, S_or_a),
	put_char(S_or_a, '('),
	'$write_term_args'(Args, punct, _, Style, S_or_a),
	put_char(S_or_a, ')').

'$write_space_if_needed'(punct, _,     _     ) :- !.
'$write_space_if_needed'(X,     X,     S_or_a) :- !, put_char(S_or_a, ' ').
'$write_space_if_needed'(other, alpha, S_or_a) :- !, put_char(S_or_a, ' ').
'$write_space_if_needed'(_,     _,     _     ).

'$write_VAR'(VN, S_or_a) :- VN < 26, !,
	Letter is VN mod 26 + "A",
	put_code(S_or_a, Letter).
'$write_VAR'(VN, S_or_a) :-
	Letter is VN mod 26 + "A",
	put_code(S_or_a, Letter),
	Rest is VN//26,
	'$fast_write'(S_or_a, Rest).

'$write_atom'(Atom, Type0, Type, Style, S_or_a) :- 
	'$builtin_member'(quoted(true), Style), 
	!,
	'$atom_type'(Atom, Type), 
	'$write_space_if_needed'(Type0, Type, S_or_a),
	'$fast_writeq'(S_or_a, Atom).
'$write_atom'(Atom, Type0, Type, _, S_or_a) :-
	'$atom_type'(Atom, Type), 
	'$write_space_if_needed'(Type0, Type, S_or_a),
	'$fast_write'(S_or_a, Atom).

'$atom_type'(X, alpha ) :- '$atom_type0'(X, 0), !.
'$atom_type'(X, symbol) :- '$atom_type0'(X, 1), !.
'$atom_type'(X, punct ) :- '$atom_type0'(X, 2), !.
'$atom_type'(X, other ) :- '$atom_type0'(X, 3), !.

'$write_is_operator'(Term, Op, Args, OpType) :-
	functor(Term, Op, Arity),
	'$write_op_type'(Arity, OpType),
	current_op(_, OpType, Op),
	Term =.. [_|Args],
	!.

'$write_op_type'(1, fx).
'$write_op_type'(1, fy).
'$write_op_type'(1, xf).
'$write_op_type'(1, yf).
'$write_op_type'(2, xfx).
'$write_op_type'(2, xfy).
'$write_op_type'(2, yfx).

'$write_term_op'(Op, OpType, Args, Prec, Type0, punct, Style, S_or_a) :-
	current_op(PrecOp, OpType, Op), 
	PrecOp > Prec,
	!,
	'$write_space_if_needed'(Type0, punct, S_or_a),
	put_char(S_or_a, '('),
	'$write_term_op1'(Op, OpType, Args, PrecOp, punct, _, Style, S_or_a),
	put_char(S_or_a, ')').
'$write_term_op'(Op, OpType, Args, _Prec, Type0, Type, Style, S_or_a) :-
	current_op(PrecOp, OpType, Op),
	'$write_term_op1'(Op, OpType, Args, PrecOp, Type0, Type, Style, S_or_a).

'$write_term_op1'(Op, fx, [A1], PrecOp, Type0, Type, Style, S_or_a) :- !,
	'$write_atom'(Op, Type0, Type1, Style, S_or_a),
	Prec1 is PrecOp - 1,
	'$write_term0'(A1, Prec1, Type1, Type, Style, S_or_a).
'$write_term_op1'(Op, fy, [A1], PrecOp, Type0, Type, Style, S_or_a) :- !,
	'$write_atom'(Op, Type0, Type1, Style, S_or_a),
	Prec1 is PrecOp,
	'$write_term0'(A1, Prec1, Type1, Type, Style, S_or_a).
'$write_term_op1'(Op, xf, [A1], PrecOp, Type0, Type, Style, S_or_a) :- !,
	Prec1 is PrecOp - 1,
	'$write_term0'(A1, Prec1, Type0, Type1, Style, S_or_a),
	'$write_atom'(Op, Type1, Type, Style, S_or_a).
'$write_term_op1'(Op, yf, [A1], PrecOp, Type0, Type, Style, S_or_a) :- !,
	Prec1 is PrecOp,
	'$write_term0'(A1, Prec1, Type0, Type1, Style, S_or_a),
	'$write_atom'(Op, Type1, Type, Style, S_or_a).
'$write_term_op1'(Op, xfx, [A1,A2], PrecOp, Type0, Type, Style, S_or_a) :- !,
	Prec1 is PrecOp - 1,
	Prec2 is PrecOp - 1,
	'$write_term0'(A1, Prec1, Type0, Type1, Style, S_or_a),
	'$write_term_infix_op'(Op, Type1, Type2, Style, S_or_a),
	'$write_term0'(A2, Prec2, Type2, Type, Style, S_or_a).
'$write_term_op1'(Op, xfy, [A1,A2], PrecOp, Type0, Type, Style, S_or_a) :- !,
	Prec1 is PrecOp - 1,
	Prec2 is PrecOp,
	'$write_term0'(A1, Prec1, Type0, Type1, Style, S_or_a),
	'$write_term_infix_op'(Op, Type1, Type2, Style, S_or_a),
	'$write_term0'(A2, Prec2, Type2, Type, Style, S_or_a).
'$write_term_op1'(Op, yfx, [A1,A2], PrecOp, Type0, Type, Style, S_or_a) :- !,
	Prec1 is PrecOp,
	Prec2 is PrecOp - 1,
	'$write_term0'(A1, Prec1, Type0, Type1, Style, S_or_a),
	'$write_term_infix_op'(Op, Type1, Type2, Style, S_or_a),
	'$write_term0'(A2, Prec2, Type2, Type, Style, S_or_a).
	
'$write_term_infix_op'(',', Type0, punct, _, S_or_a) :- !,
	'$write_space_if_needed'(Type0, punct, S_or_a),
	put_char(S_or_a, ',').
'$write_term_infix_op'(Op, Type0, Type, Style, S_or_a) :-
	'$write_atom'(Op, Type0, Type, Style, S_or_a).

'$write_term_list_args'([A|As], Type0, Type, Style, S_or_a) :- 
	nonvar(As), As = [_|_], 
	!,
	'$write_term0'(A, 999, Type0, Type1, Style, S_or_a),
	'$write_space_if_needed'(Type1, punct, S_or_a),
	put_char(S_or_a, ','),
	'$write_term_list_args'(As, punct, Type, Style, S_or_a).

'$write_term_list_args'([A|As], Type0, Type, Style, S_or_a) :- 
	nonvar(As), As = [], 
	!,
	'$write_term0'(A, 999, Type0, Type, Style, S_or_a).

'$write_term_list_args'([A|As], Type0, Type, Style, S_or_a) :-
	'$write_term0'(A, 999, Type0, Type1, Style, S_or_a),
	'$write_space_if_needed'(Type1, punct, S_or_a),
	put_char(S_or_a, '|'),
	'$write_term0'(As, 999, punct, Type, Style, S_or_a).

'$write_term_args'([], Type, Type, _, _) :- !.
'$write_term_args'([A], Type0, Type, Style, S_or_a) :- !,
	'$write_term0'(A, 999, Type0, Type, Style, S_or_a).
'$write_term_args'([A|As], Type0, Type, Style, S_or_a) :- !,
	'$write_term0'(A, 999, Type0, Type1, Style, S_or_a),
	'$write_space_if_needed'(Type1, punct, S_or_a),
	put_char(S_or_a, ','),
	'$write_term_args'(As, punct, Type, Style, S_or_a).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Term input/output (others)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public op/3.
:- public current_op/3.
:- dynamic '$current_operator'/3.

op(Priority, Op_specifier, Operator) :-
	integer(Priority), 
	0 =<Priority, Priority =<1200,
	!,
	'$op1'(Priority, Op_specifier, Operator).
op(Priority, Op_specifier, Operator) :-
        illarg(domain(integer,0-1200), op(Priority,Op_specifier,Operator), 1).

'$op1'(Priority, Op_specifier, Operator) :- 
	nonvar(Op_specifier),
	'$op_specifier'(Op_specifier, _),
	!,
	'$op2'(Priority, Op_specifier, Operator).
'$op1'(Priority, Op_specifier, Operator) :- 
        findall(X, '$op_specifier'(X,_), Domain), 
	illarg(domain(term,Domain), op(Priority,Op_specifier,Operator), 2).

'$op2'(Priority, Op_specifier, Operator) :- 
	atom(Operator), 
	!,
	'$add_operators'([Operator], Priority, Op_specifier).
'$op2'(Priority, Op_specifier, Operator) :- 
	'$op_atom_list'(Operator, Atoms),
	!,
	'$add_operators'(Atoms, Priority, Op_specifier).
'$op2'(Priority, Op_specifier, Operator) :- 
        illarg(type(list(atom)), op(Priority,Op_specifier,Operator), 3).

'$add_operators'([], _, _) :- !.
'$add_operators'([A|As], Priority, Op_specifier) :- 
	'$add_op'(A, Priority, Op_specifier),
	'$add_operators'(As, Priority, Op_specifier).

'$add_op'(',', Priority, Op_specifier) :- !,
	illarg(permission(modify,operator,',',_), op(Priority,Op_specifier,','), 3).
'$add_op'(A, _, Op_specifier) :-
	clause('$current_operator'(_,Op_specifier0,A), _),
	'$op_specifier'(Op_specifier, Class),
	'$op_specifier'(Op_specifier0, Class0),
	Class = Class0,
	retract('$current_operator'(_,Op_specifier0,A)),
	fail.
'$add_op'(_, 0, _) :- !.
'$add_op'(A, Priority, Op_specifier) :-
	assertz('$current_operator'(Priority,Op_specifier,A)).

'$op_specifier'( fx, prefix).
'$op_specifier'( fy, prefix).
'$op_specifier'(xfx, infix).
'$op_specifier'(xfy, infix).
'$op_specifier'(yfx, infix).
'$op_specifier'( xf, postfix).
'$op_specifier'( yf, postfix).

'$op_atom_list'(X, _) :- var(X), !, fail.
'$op_atom_list'([], []) :- !.
'$op_atom_list'([X|Xs], [X|As]) :- atom(X), !,
	'$op_atom_list'(Xs, As).

current_op(Priority, Op_specifier, Operator) :-
	clause('$current_operator'(Priority,Op_specifier,Operator), _).

'$current_operator'( 1200, xfx, (:-)).
'$current_operator'( 1200, xfx, (-->)).
'$current_operator'( 1200,  fx, (:-)).
'$current_operator'( 1200,  fx, (?-)).
'$current_operator'( 1150,  fx, (package)).
'$current_operator'( 1150,  fx, (import)).
'$current_operator'( 1150,  fx, (public)).
'$current_operator'( 1150,  fx, (dynamic)).
'$current_operator'( 1150,  fx, (meta_predicate)).
'$current_operator'( 1150,  fx, (mode)).
'$current_operator'( 1150,  fx, (multifile)).
'$current_operator'( 1150,  fx, (block)).
'$current_operator'( 1100, xfy, (;)).
'$current_operator'( 1050, xfy, (->)).
'$current_operator'( 1000, xfy, (',')).
'$current_operator'(  900,  fy, (\+)).
'$current_operator'(  700, xfx, (=)).
'$current_operator'(  700, xfx, (\=)).
'$current_operator'(  700, xfx, (==)).
'$current_operator'(  700, xfx, (\==)).
'$current_operator'(  700, xfx, (@<)).
'$current_operator'(  700, xfx, (@>)).
'$current_operator'(  700, xfx, (@=<)).
'$current_operator'(  700, xfx, (@>=)).
'$current_operator'(  700, xfx, (=..)).
'$current_operator'(  700, xfx, (is)).
'$current_operator'(  700, xfx, (=:=)).
'$current_operator'(  700, xfx, (=\=)).
'$current_operator'(  700, xfx, (<)).
'$current_operator'(  700, xfx, (>)).
'$current_operator'(  700, xfx, (=<)).
'$current_operator'(  700, xfx, (>=)).
'$current_operator'(  550, xfy, (:)).
'$current_operator'(  500, yfx, (+)).
'$current_operator'(  500, yfx, (-)).
'$current_operator'(  500, yfx, (#)).
'$current_operator'(  500, yfx, (/\)).
'$current_operator'(  500, yfx, (\/)).
'$current_operator'(  500,  fx, (+)).
'$current_operator'(  400, yfx, (*)).
'$current_operator'(  400, yfx, (/)).
'$current_operator'(  400, yfx, (//)).
'$current_operator'(  400, yfx, (mod)).
'$current_operator'(  400, yfx, (rem)).
'$current_operator'(  400, yfx, (<<)).
'$current_operator'(  400, yfx, (>>)).
'$current_operator'(  300, xfx, (~)).
'$current_operator'(  200, xfx, (**)).
'$current_operator'(  200, xfy, (^)).
'$current_operator'(  200,  fy, (\)).
'$current_operator'(  200,  fy, (-)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Logic and control
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public (\+)/1.
:- public once/1.
:- public repeat/0.

\+(G) :- call(G), !, fail.
\+(_).

repeat.
repeat :- repeat.

once(G) :- call(G), !.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Atomic term processing
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%:- public atom_length/2.                  written in Java
%:- public atom_concat/3.                  written in Java
:- public sub_atom/5.
%:- public atom_chars/2, atom_codes/2.     written in Java
%:- public char_code/2.                    written in Java
%:- public number_chars/2, number_codes/2. written in Java
:- public name/2.

sub_atom(Atom, Before, Length, After, Sub_atom) :-
    atom_concat(AtomL, X, Atom),
    atom_length(AtomL, Before),
    atom_concat(Sub_atom, AtomR, X),
    atom_length(Sub_atom, Length),
    atom_length(AtomR, After).

name(Constant, Chars) :-
	nonvar(Constant),
	(   number(Constant) -> number_codes(Constant, Chars)
	;   atomic(Constant) -> atom_codes(Constant, Chars)
	;   illarg(type(atomic), name(Constant,Chars), 1)
	).
name(Constant, Chars) :-
	var(Constant),
	(   number_codes(Constant0, Chars) -> Constant = Constant0
	;   atom_codes(Constant0, Chars) -> Constant = Constant0
	;   illarg(type(list(char)), name(Constant,Chars), 2)
	).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Implementation defined hooks
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public set_prolog_flag/2.
:- public current_prolog_flag/2.

set_prolog_flag(Flag, Value) :- var(Flag), !, 
	illarg(var, set_prolog_flag(Flag,Value), 1).
set_prolog_flag(Flag, Value) :- var(Value), !, 
	illarg(var, set_prolog_flag(Flag,Value), 2).
set_prolog_flag(Flag, Value) :- atom(Flag), !, 
	'$set_prolog_flag0'(Flag, Value).
set_prolog_flag(Flag, Value) :- 
	illarg(type(atom), set_prolog_flag(Flag,Value), 1).

'$set_prolog_flag0'(Flag, Value) :- 
	'$prolog_impl_flag'(Flag, Mode, changeable(YN)),
	!,
	'$set_prolog_flag0'(YN, Flag, Value, Mode).
'$set_prolog_flag0'(Flag, Value) :- 
	illarg(domain(atom,prolog_flag), set_prolog_flag(Flag,Value), 1).

'$set_prolog_flag0'(no, Flag, Value, _) :- !,
        illarg(permission(modify,flag,Flag,_), set_prolog_flag(Flag,Value), _).
'$set_prolog_flag0'(_, Flag, Value, Mode) :-
	'$builtin_member'(Value, Mode),
	!,
	'$set_prolog_impl_flag'(Flag, Value).
'$set_prolog_flag0'(_, Flag, Value, _) :-
	illarg(domain(atom,flag_value), set_prolog_flag(Flag,Value), 2).

current_prolog_flag(Flag, Term) :- var(Flag), !,
	'$prolog_impl_flag'(Flag, _, _),
	'$get_prolog_impl_flag'(Flag, Term).
current_prolog_flag(Flag, Term) :- atom(Flag), !,
	(   '$prolog_impl_flag'(Flag, _, _) -> '$get_prolog_impl_flag'(Flag, Term)
        ;   illarg(domain(atom,prolog_flag), current_prolog_flag(Flag,Term), 1)
        ).
current_prolog_flag(Flag, Term) :- 
	illarg(type(atom), current_prolog_flag(Flag,Term), 1).

% '$prolog_impl_flag'(bounded,     _, changeable(no)). 
'$prolog_impl_flag'(max_integer, _, changeable(no)).
'$prolog_impl_flag'(min_integer, _, changeable(no)).
% '$prolog_impl_flag'(integer_rounding_function, [down,toward_zero], changeable(no)).
% '$prolog_impl_flag'(char_conversion, [on,off], changeable(no)).
'$prolog_impl_flag'(debug, [on,off], changeable(yes)).
'$prolog_impl_flag'(max_arity, _, changeable(no)).
'$prolog_impl_flag'(unknown, [error,fail,warning], changeable(yes)).
'$prolog_impl_flag'(double_quotes, [chars,codes,atom], changeable(no)).
'$prolog_impl_flag'(print_stack_trace, [on,off], changeable(yes)).

:- public halt/0.
%:- public halt/1. (written in Java)
:- public abort/0.

halt :- halt(1).

abort :- raise_exception('Execution aborted').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% DCG
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public 'C'/3, expand_term/2.

'C'([X|S], X, S).

expand_term(Dcg, Cl) :- var(Dcg), !, Dcg = Cl.
expand_term(Dcg, Cl) :- '$dcg_expansion'(Dcg, Cl0), !, Cl0 = Cl.
expand_term(Dcg, Dcg).

'$dcg_expansion'(Dcg, Cl) :- var(Dcg), !, Dcg = Cl.
'$dcg_expansion'((Head --> B), (H1 :- G1, G2)) :-
	nonvar(Head),
	Head = (H, List),
	List = [_|_],
	!,
	'$dcg_translation_atom'(H, H1, S0, S1),
	'$dcg_translation'(B, G1, S0, S),
	'$dcg_translation'(List, G2, S1, S).
'$dcg_expansion'((H --> B), (H1 :- B1)) :-
	'$dcg_translation_atom'(H, H1, S0, S),
	'$dcg_translation'(B, B1, S0, S).

'$dcg_translation_atom'(X, phrase(X,S0,S), S0, S) :- 
	var(X),
	!.
'$dcg_translation_atom'(M:X, M:X1, S0, S) :- !,
	'$dcg_translation_atom'(X, X1, S0, S).
'$dcg_translation_atom'(X, X1, S0, S) :-
	X =.. [F|As],
	'$builtin_append'(As, [S0,S], As1),
	X1 =.. [F|As1].

'$dcg_translation'(X, Y, S0, S) :-
	'$dcg_trans'(X, Y0, T, S0, S),
	'$dcg_trans0'(Y0, Y, T, S0, S).

'$dcg_trans0'(Y, Y, T, S0, T) :- T \== S0, !.
'$dcg_trans0'(Y0, Y, T, _, S) :- '$dcg_concat'(Y0, S=T, Y).

'$dcg_concat'(X, Y, Z) :- X == true, !, Z = Y.
'$dcg_concat'(X, Y, Z) :- Y == true, !, Z = X.
'$dcg_concat'(X, Y, (X,Y)).

'$dcg_trans'(X, X1, S, S0, S) :- var(X), !,
	'$dcg_translation_atom'(X, X1, S0, S).
'$dcg_trans'(M:X, M:Y, T, S0, S) :- !,
	'$dcg_trans'(X, Y, T, S0, S).
'$dcg_trans'([], true, S0, S0, _) :- !.
'$dcg_trans'([X|Y], Z, T, S0, S) :- !,
	'$dcg_trans'(Y, Y1, T, S1, S),
	'$dcg_concat'('C'(S0,X,S1), Y1, Z).
'$dcg_trans'(\+X, (X1 -> fail; S=S0), S, S0, S) :- !,
	'$dcg_trans'(X, X1, S1, S0, S1).
'$dcg_trans'((X,Y), Z, T, S0, S) :- !,
	'$dcg_trans'(X, X1, S1, S0, S1),
	'$dcg_trans'(Y, Y1, T, S1, S),
	'$dcg_concat'(X1, Y1, Z).
'$dcg_trans'((X->Y), (X1->Y1), T, S0, S) :- !,
	'$dcg_trans'(X, X1, S1, S0, S1),
	'$dcg_trans'(Y, Y1, T, S1, S).
'$dcg_trans'((X;Y), (X1;Y1), S, S0, S) :- !,
	'$dcg_translation'(X, X1, S0, S),
	'$dcg_translation'(Y, Y1, S0, S).
'$dcg_trans'(!, !, S0, S0, _) :- !.
'$dcg_trans'({G}, call(G), S0, S0, _) :- var(G), !.
'$dcg_trans'({G}, G, S0, S0, _) :- !.
'$dcg_trans'(X, X1, S, S0, S) :-
	'$dcg_translation_atom'(X, X1, S0, S).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Hash creation and control
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public new_hash/1.
%:- public new_hash/2.          written in Java
%:- public hash_clear/1.        written in Java
%:- public hash_contains_key/2. written in Java
%:- public hash_get/3.          written in Java
%:- public hash_is_empty/1.     written in Java
%:- public hash_keys/2.         written in Java
:- public hash_map/2.
%:- public hash_put/3.          written in Java
%:- public hash_remove/2.       written in Java
%:- public hash_size/2.         written in Java
%:- public '$get_hash_manager'/1.  written in Java

new_hash(Hash) :- new_hash(Hash, []).

hash_map(H_or_a, List) :-
	hash_keys(H_or_a, Ks0),
	sort(Ks0, Ks),
	hash_map(Ks, List, H_or_a).

hash_map([], [], _) :- !.
hash_map([K|Ks], [(K,V)|Ls], H_or_a) :-
	hash_get(H_or_a, K, V),
	hash_map(Ks, Ls, H_or_a).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Java interoperation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%:- public java_constructor0/2.            (written in Java)
%:- public java_declared_constructor0/2.   (written in Java)
%:- public java_method0/3.                 (written in Java)
%:- public java_declared_method0/3.        (written in Java)
%:- public java_get_field0/3.              (written in Java)
%:- public java_get_declared_field0/3.     (written in Java)
%:- public java_set_field0/3.              (written in Java)
%:- public java_set_declared_field0/3.     (written in Java)
%:- public java_conversion/2.              (written in Java)
:- public java_constructor/2.
:- public java_declared_constructor/2.
:- public java_method/3.
:- public java_declared_method/3.
:- public java_get_field/3.
:- public java_get_declared_field/3.
:- public java_set_field/3.
:- public java_set_declared_field/3.
:- public synchronized/2.

java_constructor(Constr, Instance) :-
	Constr =.. [F|As],
	builtin_java_convert_args(As, As1),
	Constr1 =.. [F|As1],
	java_constructor0(Constr1, Instance1),
	Instance = Instance1.

java_declared_constructor(Constr, Instance) :-
	Constr =.. [F|As],
	builtin_java_convert_args(As, As1),
	Constr1 =.. [F|As1],
	java_declared_constructor0(Constr1, Instance1),
	Instance = Instance1.

java_method(Class_or_Instance, Method, Value) :- 
	Method =.. [F|As],
	builtin_java_convert_args(As, As1),
	Method1 =.. [F|As1],
	java_method0(Class_or_Instance, Method1, Value1),
	java_conversion(Value2, Value1),
	Value = Value2.

java_declared_method(Class_or_Instance, Method, Value) :-
	Method =.. [F|As],
	builtin_java_convert_args(As, As1),
	Method1 =.. [F|As1],
	java_declared_method0(Class_or_Instance, Method1, Value1),
	java_conversion(Value2, Value1),
	Value = Value2.

java_get_field(Class_or_Instance, Field, Value) :-
	java_get_field0(Class_or_Instance, Field, Value1),
	java_conversion(Value2, Value1),
	Value = Value2.

java_get_declared_field(Class_or_Instance, Field, Value) :-
	java_get_declared_field0(Class_or_Instance, Field, Value1),
	java_conversion(Value2, Value1),
	Value = Value2.

java_set_field(Class_or_Instance, Field, Value) :-
	java_conversion(Value, Value1),
	java_set_field0(Class_or_Instance, Field, Value1).

java_set_declared_field(Class_or_Instance, Field, Value) :-
	java_conversion(Value, Value1),
	java_set_declared_field0(Class_or_Instance, Field, Value1).

builtin_java_convert_args([], []) :- !.
builtin_java_convert_args([X|Xs], [Y|Ys]) :-
	java_conversion(X, Y),
	builtin_java_convert_args(Xs, Ys).

synchronized(Object, Goal) :- 
	'$begin_sync'(Object, Ref), 
	call(Goal), 
	'$end_sync'(Ref).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Prolog interpreter
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- op(1170, xfx, (:-)).
:- op(1170, xfx, (-->)).
:- op(1170,  fx, (:-)).
:- op(1170,  fx, (?-)).

:- op(1150,  fx, (package)).
:- op(1150,  fx, (import)).
:- op(1150,  fx, (public)).
:- op(1150,  fx, (dynamic)).
:- op(1150,  fx, (meta_predicate)).
:- op(1150,  fx, (mode)).
:- op(1150,  fx, (multifile)).
:- op(1150,  fx, (block)).

:- public cafeteria/0.
:- public consult/1.
:- public trace/0, notrace/0.
:- public debug/0, nodebug/0.
:- public leash/1.
:- public spy/1, nospy/1, nospyall/0.
:- public listing/0.
:- public listing/1.

:- dynamic '$current_leash'/1.
:- dynamic '$current_spypoint'/3.
:- dynamic '$leap_flag'/1.
:- dynamic '$consulted_file'/1.
:- dynamic '$consulted_package'/1.
:- dynamic '$consulted_predicate'/3.

%%% Main
cafeteria :-
	'$cafeteria_init',
	repeat,
	    '$toplvel_loop',
	    on_exception(Msg, '$cafeteria'(Goal), print_message(error, Msg)),
	    Goal == end_of_file,
	    !,
	nl, '$fast_write'(bye), nl.

'$cafeteria_init' :- 
	retractall('$leap_flag'(_)),
	retractall('$current_leash'(_)),
	retractall('$current_spypoint'(_,_,_)),
	retractall('$consulted_file'(_)),
	retractall('$consulted_package'(_)),
	retractall('$consulted_predicate'(_,_,_)),
	assertz('$leap_flag'(no)),
	assertz('$current_leash'(call)),
	assertz('$current_leash'(exit)),
	assertz('$current_leash'(redo)),
	assertz('$current_leash'(fail)),
	!.

'$toplvel_loop' :-
	current_prolog_flag(debug, Mode),
	(Mode == off -> true ; print_message(info,[debug])),
	'$fast_write'('| ?- '), 
	flush_output.

'$cafeteria'(Goal) :-
	read_with_variables(Goal, Vars),
	'$process_order'(Goal, Vars).

'$process_order'(G,               _) :- var(G), !, illarg(var, (?- G), 1).
'$process_order'(end_of_file,     _) :- !.
'$process_order'([File|Files],    _) :- !, consult([File|Files]).
'$process_order'(G,            Vars) :- 
	current_prolog_flag(debug, Mode),
	(   Mode == off -> call(user:G) ; '$trace_goal'(user:G)   ), nl, 
	'$rm_redundant_vars'(Vars, Vars1),	
	'$give_answers_with_prompt'(Vars1),
	!, 
	'$fast_write'(yes), nl.
'$process_order'(_, _) :- nl, '$fast_write'(no), nl.

'$rm_redundant_vars'([], []) :- !.
'$rm_redundant_vars'(['_'=_|Xs], Vs)  :- !,
	'$rm_redundant_vars'(Xs, Vs).
'$rm_redundant_vars'([X|Xs], [X|Vs]) :-
	'$rm_redundant_vars'(Xs, Vs).

'$give_answers_with_prompt'([]) :- !.
'$give_answers_with_prompt'(Vs) :-
	'$give_an_answer'(Vs),
	'$fast_write'(' ? '), flush_output, 
	read_line(Str), 
	Str \== ";", 
	nl.

'$give_an_answer'([])  :- !, '$fast_write'(true).
'$give_an_answer'([X]) :- !, '$print_an answer'(X).
'$give_an_answer'([X|Xs]) :-
	'$print_an answer'(X), '$fast_write'(','), nl,
	'$give_an_answer'(Xs).

'$print_an answer'(N = V) :- 
	write(N), '$fast_write'(' = '), writeq(V).

%%% Read Program
consult(Files) :- var(Files), !, illarg(var, consult(Files), 1).
consult([]) :- !.
consult([File|Files]) :- !, consult(File), consult(Files).
consult(File) :- atom(File), !, '$consult'(File).

'$consult'(F) :-
	'$prolog_file_name'(F, PF),
	open(PF, read, In),
	stream_property(In, file_name(File)),
	print_message(info, [consulting,File,'...']),
	statistics(runtime, _),
	'$consult_init'(File),
	repeat,
	    read(In, Cl),
	    '$consult_clause'(Cl),
	    Cl == end_of_file,
	    !,
	statistics(runtime, [_,T]),
	print_message(info, [File,'consulted,',T,msec]),
	close(In).

%'$prolog_file_name'(File,  File) :- sub_atom(File, _, 3, 0, '.pl'), !.
%'$prolog_file_name'(File,  File) :- sub_atom(File, _, 4, 0, '.pro'), !.
'$prolog_file_name'(File,  File) :- sub_atom(File, _, _, After, '.'), After > 0, !.
'$prolog_file_name'(File0, File) :- atom_concat(File0, '.pl', File).

'$consult_init'(File) :-
	retractall('$consulted_file'(_)),
	retractall('$consulted_package'(_)),
	retract('$consulted_predicate'(P,PI,File)),
	abolish(P:PI),
	fail.
'$consult_init'(File) :-
	assertz('$consulted_file'(File)),
	assertz('$consulted_package'(user)).

'$consult_clause'(end_of_file          ) :- !.
'$consult_clause'((:- module(P,_))     ) :- !, '$assert_consulted_package'(P).
'$consult_clause'((:- package P)       ) :- !, '$assert_consulted_package'(P).
'$consult_clause'((:- import _)        ) :- !.
'$consult_clause'((:- dynamic _)       ) :- !.
'$consult_clause'((:- public _)        ) :- !.
'$consult_clause'((:- meta_predicate _)) :- !.
'$consult_clause'((:- mode _)          ) :- !.
'$consult_clause'((:- multifile _)     ) :- !.
'$consult_clause'((:- block _)         ) :- !.
%'$consult_clause'((:- G)               ) :- !, clause('$consulted_package'(P), _), call(P:G).
'$consult_clause'((:- G)               ) :- !, clause('$consulted_package'(P), _), once(P:G).
'$consult_clause'(Clause0) :- 
	'$consult_preprocess'(Clause0, Clause), 
	'$consult_cls'(Clause).

'$assert_consulted_package'(P) :-
	clause('$consulted_package'(P), _),
	!.
'$assert_consulted_package'(P) :-
	retractall('$consulted_package'(_)),
	assertz('$consulted_package'(P)).

'$consult_preprocess'(Clause0, Clause) :-
	expand_term(Clause0, Clause).

'$consult_cls'((H :- G)) :- !, '$assert_consulted_clause'((H :- G)).
'$consult_cls'(H) :- '$assert_consulted_clause'((H :- true)).

'$assert_consulted_clause'(Clause) :-
	Clause = (H :- _),
	functor(H, F, A),
	clause('$consulted_file'(File), _),
	clause('$consulted_package'(P), _),
	assertz(P:Clause),
	assertz('$consulted_predicate'(P,F/A,File)),
	!.

%%% Trace
trace :- current_prolog_flag(debug, on), !.
trace :- 
	set_prolog_flag(debug, on),
	'$trace_init',
	'$fast_write'('{Small debugger is switch on}'),
	nl, !.

'$trace_init' :- 
	retractall('$leap_flag'(_)),
	retractall('$current_leash'(_)),
	retractall('$current_spypoint'(_,_,_)),
	assertz('$leap_flag'(no)),
	assertz('$current_leash'(call)),
	assertz('$current_leash'(exit)),
	assertz('$current_leash'(redo)),
	assertz('$current_leash'(fail)),
	!.

notrace :- current_prolog_flag(debug, off), !.
notrace :-
	set_prolog_flag(debug, off),
	'$fast_write'('{Small debugger is switch off}'), 
	nl, !.

debug :- trace.
nodebug :- notrace.

%%% Spy-Points
spy(T) :- 
	'$term_to_predicateindicator'(T, PI, spy(T)),
	trace,
	'$assert_spypoint'(PI),
	'$set_debug_flag'(leap, yes),
	!.

'$assert_spypoint'(P:F/A) :- 
	clause('$current_spypoint'(P,F,A), _), 
	print_message(info, [spypoint,P:F/A,is,already,added]),
	!.
'$assert_spypoint'(P:F/A) :- 
	clause('$consulted_predicate'(P,F/A,_), _),
	assertz('$current_spypoint'(P,F,A)),
	print_message(info, [spypoint,P:F/A,is,added]),
	!.
'$assert_spypoint'(P:F/A) :- 
	print_message(warning, [no,matching,predicate,for,spy,P:F/A]).

nospy(T) :- 
	'$term_to_predicateindicator'(T, PI, nospy(T)),
	'$retract_spypoint'(PI),
	'$set_debug_flag'(leap, no),
	!.

'$retract_spypoint'(P:F/A) :-
	retract('$current_spypoint'(P,F,A)),
	print_message(info, [spypoint,P:F/A,is,removed]),
	!.
'$retract_spypoint'(_).

nospyall :-  
	retractall('$current_spypoint'(_,_,_)),
	'$set_debug_flag'(leap, no).

%%% Leash
leash(L) :- nonvar(L), '$leash'(L), !.
leash(L) :- illarg(type('leash_specifier'), leash(L), 1).

'$leash'([]) :- !, 
	retractall('$current_leash'(_)),
	print_message(info, [no,leashing]).
'$leash'(Ms) :-
	retractall('$current_leash'(_)),
	'$assert_leash'(Ms),
	print_message(info,[leashing,stopping,on,Ms]).

'$assert_leash'([]) :- !.
'$assert_leash'([X|Xs]) :- 
	'$leash_specifier'(X),
	assertz('$current_leash'(X)), 
	'$assert_leash'(Xs).

'$leash_specifier'(call).
'$leash_specifier'(exit).
'$leash_specifier'(redo).
'$leash_specifier'(fail).
%'$leash_specifier'(exception).

%%% Trace a Goal
'$trace_goal'(Term) :- 
	'$set_debug_flag'(leap, no),
	'$get_level'(Cut), 
	'$meta_call'(Term, user, Cut, 0, trace).

'$trace_goal'(X, P, FA, Depth) :- 
	print_procedure_box(call, X, P, FA, Depth),
	'$call_internal'(X, P, FA, Depth, trace),
	print_procedure_box(exit, X, P, FA, Depth),
	redo_procedure_box(X, P, FA, Depth).
'$trace_goal'(X, P, FA, Depth) :- 
	print_procedure_box(fail, X, P, FA, Depth),
	fail.

print_procedure_box(Mode, G, P, F/A, Depth) :- 
	clause('$current_spypoint'(P, F, A), _),
	!,
	'$builtin_message'(['+',Depth,Mode,':',P:G]), 
	'$read_blocked'(print_procedure_box(Mode,G,P,F/A,Depth)).
print_procedure_box(Mode, G, P, FA, Depth) :- 
	clause('$leap_flag'(no), _),
	!,
	'$builtin_message'([' ',Depth,Mode,':',P:G]), 
	(    clause('$current_leash'(Mode), _) 
             -> 
	     '$read_blocked'(print_procedure_box(Mode,G,P,FA,Depth))
	     ; 
	     nl
	 ).
print_procedure_box(_, _, _, _, _).

redo_procedure_box(_, _, _, _).
redo_procedure_box(X, P, FA, Depth) :- 
	print_procedure_box(redo, X, P, FA, Depth),
	fail.

'$read_blocked'(G) :-
	'$fast_write'(' ? '),
	flush_output, 
	read_line(C),
	(C == [] -> DOP = 99 ; C = [DOP|_]),
	'$debug_option'(DOP, G).

'$debug_option'(97,  _) :- !, notrace, abort.               % a for abort
'$debug_option'(99,  _) :- !, '$set_debug_flag'(leap, no).  % c for creep
'$debug_option'(108, _) :- !, '$set_debug_flag'(leap, yes). % l for leap
'$debug_option'(43,  print_procedure_box(Mode,G,P,FA,Depth)) :- !, % + for spy this
	spy(P:FA),
	call(print_procedure_box(Mode,G,P,FA,Depth)).
'$debug_option'(45,  print_procedure_box(Mode,G,P,FA,Depth)) :- !, % - for nospy this
	nospy(P:FA),
	call(print_procedure_box(Mode,G,P,FA,Depth)).
'$debug_option'(63,  G) :- !, '$show_debug_option', call(G).
'$debug_option'(104, G) :- !, '$show_debug_option', call(G).
'$debug_option'(_, _).

'$show_debug_option' :-
	tab(4), '$fast_write'('Debuggin options:'), nl,
	tab(4), '$fast_write'('a      abort'), nl,
	tab(4), '$fast_write'('RET    creep'), nl,
	tab(4), '$fast_write'('c      creep'), nl,
	tab(4), '$fast_write'('l      leap'), nl,
	tab(4), '$fast_write'('+      spy this'), nl,
	tab(4), '$fast_write'('-      nospy this'), nl,
	tab(4), '$fast_write'('?      help'), nl,
	tab(4), '$fast_write'('h      help'), nl.

'$set_debug_flag'(leap, Flag) :-
	clause('$leap_flag'(Flag), _),
	!.
'$set_debug_flag'(leap, Flag) :-
	retractall('$leap_flag'(_)),
	assertz('$leap_flag'(Flag)).

%%% Listing
listing :- '$listing'(_, user).

listing(T) :- var(T), !, illarg(var, listing(T), 1).
listing(P) :- atom(P), !, '$listing'(_, P).
listing(F/A) :- !, '$listing'(F/A, user).
listing(P:PI) :- atom(P), !, '$listing'(PI, P).
listing(T) :- illarg(type(predicate_indicator), listing(T), 1).

'$listing'(PI, P) :- var(PI), !,
	'$listing_dynamic_clause'(P, _).
'$listing'(F/A, P) :- atom(F), integer(A), !,
	'$listing_dynamic_clause'(P, F/A).
'$listing'(PI, P) :- illarg(type(predicate_indicator), listing(P:PI), 1).

'$listing_dynamic_clause'(P, PI) :- 
	'$new_internal_database'(P),
	hash_keys(P, Keys),
	'$builtin_member'(PI, Keys),
	PI = F/A,
	functor(H, F, A),
	'$clause_internal'(P, PI, H, Cl, _),
	'$write_dynamic_clause'(P, Cl),
	fail.
'$listing_dynamic_clause'(_, _).

'$write_dynamic_clause'(_, Cl) :- var(Cl), !, fail.
'$write_dynamic_clause'(P, (H :- true)) :- !,
	numbervars(H, 0, _),
	'$write_dynamic_head'(P, H),
	write('.'), nl.
'$write_dynamic_clause'(P, (H :- B)) :- !,
	numbervars((H :- B), 0, _),
	'$write_dynamic_head'(P, H),
	write(' :-'), nl,
	'$write_dynamic_body'(B, 8),
	write('.'), nl.

'$write_dynamic_head'(user, H) :- !, writeq(H).
'$write_dynamic_head'(P, H) :- 
	write(P), write(':'), writeq(H).

'$write_dynamic_body'((G1,G2), N) :- !,
	'$write_dynamic_body'(G1, N), write(','), nl,
	'$write_dynamic_body'(G2, N).
'$write_dynamic_body'((G1;G2), N) :- !,
	N1 is N+4,
	tab(N), write('('), nl,
	'$write_dynamic_body'(G1, N1), nl,
	tab(N), write(';'), nl,
	'$write_dynamic_body'(G2, N1), nl,
	tab(N), write(')').
'$write_dynamic_body'((G1->G2), N) :- !,
	N1 is N+4,
	tab(N), write('('), nl,
	'$write_dynamic_body'(G1, N1), nl,
	tab(N), write('->'), nl,
	'$write_dynamic_body'(G2, N1), nl,
	tab(N), write(')').
'$write_dynamic_body'(B, N) :-
	tab(N), writeq(B).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Misc
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
:- public length/2.
:- public numbervars/3.
:- public statistics/2.

length(L, N) :- var(N), !, '$length'(L, 0, N).
length(L, N) :- '$length0'(L, 0, N).

'$length'([], I, I).
'$length'([_|L], I0, I) :- I1 is I0+1, '$length'(L, I1, I).

'$length0'([], I, I) :- !.
'$length0'([_|L], I0, I) :- I0 < I, I1 is I0+1, '$length0'(L, I1, I).

numbervars(X, VI, VN) :- 
	integer(VI), VI >= 0, 
	!,
	'$numbervars'(X, VI, VN).

'$numbervars'(X, VI, VN) :- var(X), !,
	X = '$VAR'(VI),	  % This structure is checked in write
	VN is VI + 1.
'$numbervars'(X, VI, VI) :- atomic(X), !.
'$numbervars'(X, VI, VI) :- java(X), !.
'$numbervars'(X, VI, VN) :-
	functor(X, _, N),
	'$numbervars_str'(1, N, X, VI, VN).

'$numbervars_str'(I, I, X, VI, VN) :- !,
	arg(I, X, A),
	'$numbervars'(A, VI, VN).
'$numbervars_str'(I, N, X, VI, VN) :-
	arg(I, X, A),
	'$numbervars'(A, VI, VN1),
	I1 is I + 1,
	'$numbervars_str'(I1, N, X, VN1, VN).

statistics(Key, Value) :- 
	nonvar(Key), 
	'$statistics_mode'(Key), 
	!,
	'$statistics'(Key, Value).
statistics(Key, Value) :- 
	findall(M, '$statistics_mode'(M), Domain),
	illarg(domain(atom,Domain), statistics(Key,Value), 1).

'$statistics_mode'(runtime).
'$statistics_mode'(trail).
'$statistics_mode'(choice).

print_message(Type, Message) :- var(Type), !,
	illarg(var, print_message(Type,Message), 1).
print_message(error, Message) :- !, 
	'$error_message'(Message).
print_message(info,  Message) :- !,
	'$fast_write'('{'), 
	'$builtin_message'(Message), 
	'$fast_write'('}'), nl.
print_message(warning, Message) :- !,
	'$fast_write'('{WARNING: '), 
	'$builtin_message'(Message), 
	'$fast_write'('}'), nl.

'$error_message'(instantiation_error(Goal,0)) :- !,
	'$fast_write'('{INSTANTIATION ERROR: '), 
	'$write_goal'(Goal), 
	'$fast_write'('}'), nl.
'$error_message'(instantiation_error(Goal,ArgNo)) :- !,
	'$fast_write'('{INSTANTIATION ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo), 
	'$fast_write'('}'), nl.
'$error_message'(type_error(Goal,ArgNo,Type,Culprit)) :- !,
	'$fast_write'('{TYPE ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo), 
	'$fast_write'(': expected '), '$fast_write'(Type),
	'$fast_write'(', found '), write(Culprit),
	'$fast_write'('}'), nl.
'$error_message'(domain_error(Goal,ArgNo,Domain,Culprit)) :- !,
	'$fast_write'('{DOMAIN ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo),
	'$fast_write'(': expected '), '$fast_write'(Domain),
	'$fast_write'(', found '), write(Culprit),
	'$fast_write'('}'), nl.
'$error_message'(existence_error(_Goal,0,ObjType,Culprit,_Message)) :- !,
	'$fast_write'('{EXISTENCE ERROR: '),
	'$fast_write'(ObjType), '$fast_write'(' '), write(Culprit), '$fast_write'(' does not exist'),
	'$fast_write'('}'), nl.
'$error_message'(existence_error(Goal,ArgNo,ObjType,Culprit,_Message)) :- !,
	'$fast_write'('{EXISTENCE ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo),
	'$fast_write'(': '), 
	'$fast_write'(ObjType), '$fast_write'(' '), write(Culprit), '$fast_write'(' does not exist'),
	'$fast_write'('}'), nl.
'$error_message'(permission_error(Goal,Operation,ObjType,Culprit,Message)) :- !, 
	'$fast_write'('{PERMISSION ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - can not '), '$fast_write'(Operation), '$fast_write'(' '), 
	'$fast_write'(ObjType), '$fast_write'(' '), write(Culprit), 
	'$fast_write'(': '), '$fast_write'(Message),
	'$fast_write'('}'), nl.
'$error_message'(representation_error(Goal,ArgNo,Flag)) :- !, 
	'$fast_write'('{REPRESENTATION ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo), 
	'$fast_write'(': limit of '), '$fast_write'(Flag), '$fast_write'(' is breached'),
	'$fast_write'('}'), nl.
'$error_message'(evaluation_error(Goal,ArgNo,Type)) :- !, 
	'$fast_write'('{EVALUATION ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo), 
	'$fast_write'(', found '), '$fast_write'(Type),
	'$fast_write'('}'), nl.
'$error_message'(syntax_error(Goal,ArgNo,Type,Culprit,_Message)) :- !,
	'$fast_write'('{SYNTAX ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo), 
	'$fast_write'(': expected '), '$fast_write'(Type),
	'$fast_write'(', found '), write(Culprit),
	'$fast_write'('}'), nl.
'$error_message'(system_error(Message)) :- !,
	'$fast_write'('{SYSTEM ERROR: '), write(Message), '$fast_write'('}'), nl.
'$error_message'(internal_error(Message)) :- !,
	'$fast_write'('{INTERNAL ERROR: '), write(Message), '$fast_write'('}'), nl.
'$error_message'(java_error(Goal,ArgNo,Exception)) :- !,
	'$fast_write'('{JAVA ERROR: '),
	'$write_goal'(Goal), 
	'$fast_write'(' - arg '), '$fast_write'(ArgNo),
	'$fast_write'(', found '), '$write_goal'(Exception),
	'$fast_write'('}'), nl,
	'$print_stack_trace'(Exception).
'$error_message'(Message) :- 
	'$fast_write'('{'), write(Message), '$fast_write'('}'), nl.

'$write_goal'(Goal) :- java(Goal), !, 
	current_output(S), '$write_toString'(S, Goal).
'$write_goal'(Goal) :- write(Goal).

illarg(Msg, Goal, ArgNo) :- var(Msg), !,
	illarg(var, Goal, ArgNo).
illarg(var, Goal, ArgNo) :-
	raise_exception(instantiation_error(Goal, ArgNo)).
illarg(type(Type), Goal, ArgNo) :- 
	arg(ArgNo, Goal, Arg),
	(  nonvar(Arg) -> 
	   Error = type_error(Goal,ArgNo,Type,Arg)
	;  Error = instantiation_error(Goal,ArgNo)
	),
	raise_exception(Error).
illarg(domain(Type,ExpDomain), Goal, ArgNo) :-
	arg(ArgNo, Goal, Arg),
	(  '$match_type'(Type, Arg) ->
	   Error = domain_error(Goal,ArgNo,ExpDomain,Arg)
	;  nonvar(Arg) ->
	   Error = type_error(Goal,ArgNo,Type,Arg)
	;  Error = instantiation_error(Goal,ArgNo)
	),
	raise_exception(Error).
illarg(existence(ObjType,Culprit,Message), Goal, ArgNo) :-
	raise_exception(existence_error(Goal,ArgNo,ObjType,Culprit,Message)).
illarg(permission(Operation, ObjType, Culprit, Message), Goal, _) :-
	raise_exception(permission_error(Goal,Operation,ObjType,Culprit,Message)).
illarg(representation(Flag), Goal, ArgNo) :- 
	raise_exception(representation_error(Goal,ArgNo,Flag)).
illarg(evaluation(Type), Goal, ArgNo) :- 
	raise_exception(evaluation_error(Goal,ArgNo,Type)).
illarg(syntax(Type,Culprit,Message), Goal, ArgNo) :- 
	raise_exception(syntax_error(Goal,ArgNo,Type,Culprit,Message)).
illarg(system(Message), _, _) :- 
	raise_exception(system_error(Message)).
illarg(internal(Message), _, _) :- 
	raise_exception(internal_error(Message)).
illarg(java(Exception), Goal, ArgNo) :- 
	raise_exception(java_error(Goal,ArgNo,Exception)).
illarg(Msg, _, _) :- raise_exception(Msg).

'$match_type'(term,         _).
'$match_type'(variable,     X) :- var(X).
'$match_type'(atom,         X) :- atom(X).
'$match_type'(atomic,       X) :- atomic(X).
'$match_type'(byte,         X) :- integer(X), 0 =< X, X =< 255.
'$match_type'(in_byte,      X) :- integer(X), -1 =< X, X =< 255.
'$match_type'(character,    X) :- atom(X), atom_length(X, 1).
'$match_type'(in_character, X) :- (X == 'end_of_file' ; '$match_type'(character,X)).
'$match_type'(number,       X) :- number(X).
'$match_type'(integer,      X) :- integer(X).
'$match_type'(float,        X) :- float(X).
'$match_type'(callable,     X) :- callable(X).
'$match_type'(compound,     X) :- compound(X).
'$match_type'(list,         X) :- nonvar(X), (X = [] ; X = [_|_]).
'$match_type'(java,         X) :- java(X).
'$match_type'(stream,       X) :- (java(X, 'java.io.PushbackReader') ; java(X, 'java.io.PrintWriter')).
'$match_type'(stream_or_alias, X) :- (atom(X) ; '$match_type'(stream, X)).
'$match_type'(hash,         X) :- java(X, 'jp.ac.kobe_u.cs.prolog.lang.HashtableOfTerm').
'$match_type'(hash_or_alias,X) :- (atom(X) ; '$match_type'(hash, X)).
'$match_type'(predicate_indicator, X) :- 
	nonvar(X), 
	X = P:F/A, 
	atom(P),
	atom(F),
	integer(A).
%'$match_type'(evaluable,    X).
%'$match_type'('convertible to java',  X).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Utilities
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
'$builtin_append'([], Zs, Zs).
'$builtin_append'([X|Xs], Ys, [X|Zs]) :- '$builtin_append'(Xs, Ys, Zs).

'$builtin_member'(X, [X|_]).
'$builtin_member'(X, [_|L]) :- '$builtin_member'(X, L).	

'$builtin_reverse'(Xs, Zs) :- '$builtin_reverse'(Xs, [], Zs).
'$builtin_reverse'([], Zs, Zs).
'$builtin_reverse'([X|Xs], Ys, Zs) :- '$builtin_reverse'(Xs, [X|Ys], Zs).

'$builtin_message'([]) :- !.
'$builtin_message'([M]) :- !, write(M).
'$builtin_message'([M|Ms]) :- write(M), '$fast_write'(' '), '$builtin_message'(Ms).

'$member_in_reverse'(X, [_|L]) :- '$member_in_reverse'(X, L).
'$member_in_reverse'(X, [X|_]).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% END

