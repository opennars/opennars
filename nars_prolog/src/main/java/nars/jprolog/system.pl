%:- op(1150,  fx, (package)). 
%package(_). 
:- package 'jp.ac.kobe_u.cs.prolog.builtin'.
:- public system_predicate/1.

system_predicate(system_predicate(_)).
% Control constructs
system_predicate(true).
system_predicate(therwise).
system_predicate(fail).
system_predicate(false).
system_predicate((!)).
system_predicate('$get_level'(_)).
system_predicate('$neck_cut').
system_predicate('$cut'(_)).
system_predicate((_ ^ _)).
system_predicate((_ , _)).
system_predicate((_ ; _)).
system_predicate((_ -> _)).
system_predicate(call(_)).
system_predicate(catch(_,_,_)).
system_predicate(throw(_)).
system_predicate(on_exception(_,_,_)).
system_predicate(raise_exception(_)).
% Term unification
system_predicate((_ = _)).
system_predicate('$unify'(_,_)).
system_predicate((_ \= _)).
system_predicate('$not_unifiable'(_,_)).
% Type testing
system_predicate(var(_)).
system_predicate(atom(_)).
system_predicate(integer(_)).
system_predicate(float(_)).
system_predicate(atomic(_)).
system_predicate(compound(_)).
system_predicate(nonvar(_)).
system_predicate(number(_)).
system_predicate(java(_)).
system_predicate(java(_,_)).
system_predicate(closure(_)).
system_predicate(ground(_)).
system_predicate(callable(_)).
% Term comparison
system_predicate((_ == _)).
system_predicate('$equality_of_term'(_,_)).
system_predicate((_ \== _)).
system_predicate('$inequality_of_term'(_,_)).
system_predicate((_ @< _)).
system_predicate('$before'(_,_)).
system_predicate((_ @> _)).
system_predicate('$after'(_,_)).
system_predicate((_ @=< _)).
system_predicate('$not_after'(_,_)).
system_predicate((_ @>= _)).
system_predicate('$not_before'(_,_)).
system_predicate(?=(_,_)).
system_predicate('$identical_or_cannot_unify'(_,_)).
system_predicate(compare(_,_,_)).
system_predicate(sort(_,_)).
system_predicate(keysort(_,_)).
%system_predicate(merge(_,_,_)).
% Term creation and decomposition
system_predicate(arg(_,_,_)).
system_predicate(functor(_,_,_)).
system_predicate((_ =.. _)).
system_predicate('$univ'(_,_)).
system_predicate(copy_term(_,_)).
% Arithmetic evaluation
system_predicate(is(_,_)).
system_predicate('$abs'(_,_)).
system_predicate('$asin'(_,_)).
system_predicate('$acos'(_,_)).
system_predicate('$atan'(_,_)).
system_predicate('$bitwise_conj'(_,_,_)).
system_predicate('$bitwise_disj'(_,_,_)).
system_predicate('$bitwise_exclusive_or'(_,_,_)).
system_predicate('$bitwise_neg'(_,_)).
system_predicate('$ceil'(_,_)).
system_predicate('$cos'(_,_)).
system_predicate('$degrees'(_,_)).
system_predicate('$exp'(_,_)).
system_predicate('$float'(_,_)).
system_predicate('$float_integer_part'(_,_)).
system_predicate('$float_fractional_part'(_,_)).
system_predicate('$float_quotient'(_,_,_)).
system_predicate('$floor'(_,_)).
system_predicate('$int_quotient'(_,_,_)).
system_predicate('$log'(_,_)).
system_predicate('$max'(_,_,_)).
system_predicate('$min'(_,_,_)).
system_predicate('$minus'(_,_,_)).
system_predicate('$mod'(_,_,_)).
system_predicate('$multi'(_,_,_)).
system_predicate('$plus'(_,_,_)).
system_predicate('$pow'(_,_,_)).
system_predicate('$radians'(_,_)).
system_predicate('$rint'(_,_)).
system_predicate('$round'(_,_)).
system_predicate('$shift_left'(_,_,_)).
system_predicate('$shift_right'(_,_,_)).
system_predicate('$sign'(_,_)).
system_predicate('$sin'(_,_)).
system_predicate('$sqrt'(_,_)).
system_predicate('$tan'(_,_)).
system_predicate('$truncate'(_,_)).
% Arithmetic comparison
system_predicate((_ =:= _)).
system_predicate('$arith_equal'(_,_)).
system_predicate((_ =\= _)).
system_predicate('$arith_not_equal'(_,_)).
system_predicate((_ < _)).
system_predicate('$less_than'(_,_)).
system_predicate((_ =< _)).
system_predicate('$less_or_equal'(_,_)).
system_predicate((_ > _)).
system_predicate('$greater_than'(_,_)).
system_predicate((_ >= _)).
system_predicate('$greater_or_equal'(_,_)).
% Clause retrieval and information
system_predicate(clause(_,_)).
system_predicate(initialization(_,_)).
system_predicate('$new_indexing_hash'(_,_,_)).
% Clause creation and destruction
system_predicate(assert(_)).
system_predicate(assertz(_)).
system_predicate(asserta(_)).
system_predicate(retract(_)).
system_predicate(abolish(_)).
system_predicate(retractall(_)).
% All solutions
system_predicate(findall(_,_,_)).
system_predicate(bagof(_,_,_)).
system_predicate(setof(_,_,_)).
% Stream selection and control
system_predicate(current_input(_)).
system_predicate(current_output(_)).
system_predicate(set_input(_)).
system_predicate(set_output(_)).
system_predicate(open(_,_,_)).
system_predicate(open(_,_,_,_)).
system_predicate(close(_)).
system_predicate(close(_,_)).
system_predicate(flush_output(_)).
system_predicate(flush_output).
system_predicate(stream_property(_,_)).
% Character input/output
system_predicate(get_char(_)).
system_predicate(get_char(_,_)).
system_predicate(get_code(_)).
system_predicate(get_code(_,_)).
system_predicate(peek_char(_)).
system_predicate(peek_char(_,_)).
system_predicate(peek_code(_)).
system_predicate(peek_code(_,_)).
system_predicate(put_char(_)).
system_predicate(put_char(_,_)).
system_predicate(put_code(_)).
system_predicate(put_code(_,_)).
system_predicate(nl).
system_predicate(nl(_)).
system_predicate(get0(_)).
system_predicate(get0(_,_)).
system_predicate(get(_)).
system_predicate(get(_,_)).
system_predicate(put(_)).
system_predicate(put(_,_)).
system_predicate(tab(_)).
system_predicate(tab(_,_)).
system_predicate(skip(_)).
system_predicate(skip(_,_)).
% Byte input/output
system_predicate(get_byte(_)).
system_predicate(get_byte(_,_)).
system_predicate(peek_byte(_)).
system_predicate(peek_byte(_,_)).
system_predicate(put_byte(_)).
system_predicate(put_byte(_,_)).
% Term input/output
system_predicate(read(_)).
system_predicate(read(_,_)).
system_predicate(read_with_variables(_,_)).
system_predicate(read_with_variables(_,_,_)).
system_predicate(read_line(_)).
system_predicate(read_line(_,_)).
system_predicate(write(_)).
system_predicate(write(_,_)).
system_predicate(writeq(_)).
system_predicate(writeq(_,_)).
system_predicate(write_canonical(_)).
system_predicate(write_canonical(_,_)).
system_predicate(write_term(_,_)).
system_predicate(write_term(_,_,_)).
system_predicate(op(_,_,_)).
system_predicate(current_op(_,_,_)).
% Logic and control
system_predicate(\+(_)).
system_predicate(once(_)).
system_predicate(repeat).
% Atomic term processing
system_predicate(atom_length(_,_)).
system_predicate(atom_concat(_,_,_)).
system_predicate(sub_atom(_,_,_,_,_)).
system_predicate(atom_chars(_,_)).
system_predicate(atom_codes(_,_)).
system_predicate(char_code(_,_)).
system_predicate(number_chars(_,_)).
system_predicate(number_codes(_,_)).
system_predicate(name(_,_)).
% Implementation defined hooks
system_predicate(set_prolog_flag(_,_)).
system_predicate(current_prolog_flag(_,_)).
system_predicate(halt).
system_predicate(halt(_)).
system_predicate(abort).
% DCG
system_predicate('C'(_,_,_)).
system_predicate(expand_term(_,_)).
% Hash creation and control
system_predicate(new_hash(_)).
system_predicate(new_hash(_,_)).
system_predicate(hash_clear(_)).
system_predicate(hash_contains_key(_,_)).
system_predicate(hash_get(_,_,_)).
system_predicate(hash_is_empty(_)).
system_predicate(hash_keys(_,_)).
system_predicate(hash_map(_,_)).
system_predicate(hash_put(_,_,_)).
system_predicate(hash_remove(_,_)).
system_predicate(hash_size(_,_)).
system_predicate('$get_hash_manager'(_)).
% Java interoperation
system_predicate(java_constructor0(_,_)).
system_predicate(java_constructor(_,_)).
system_predicate(java_declared_constructor0(_,_)).
system_predicate(java_declared_constructor(_,_)).
system_predicate(java_method0(_,_,_)).
system_predicate(java_method(_,_,_)).
system_predicate(java_declared_method0(_,_,_)).
system_predicate(java_declared_method(_,_,_)).
system_predicate(java_get_field0(_,_,_)).
system_predicate(java_get_field(_,_,_)).
system_predicate(java_get_declared_field0(_,_,_)).
system_predicate(java_get_declared_field(_,_,_)).
system_predicate(java_set_field0(_,_,_)).
system_predicate(java_set_field(_,_,_)).
system_predicate(java_set_declared_field0(_,_,_)).
system_predicate(java_set_declared_field(_,_,_)).
system_predicate(synchronized(_,_)).
system_predicate(java_conversion(_,_)).
% Prolog interpreter
system_predicate(cafeteria).
system_predicate(consult(_)).
system_predicate(trace).
system_predicate(notrace).
system_predicate(debug).
system_predicate(nodebug).
system_predicate(leash(_)).
system_predicate(spy(_)).
system_predicate(nospy(_)).
system_predicate(nospyall).
system_predicate(listing).
system_predicate(listing(_)).
% Misc
system_predicate(length(_,_)).
system_predicate(numbervars(_,_,_)).
system_predicate(statistics(_,_)).
% END
