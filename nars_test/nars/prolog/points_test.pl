loop(0,_,_):- !.
loop(N,List,Rnd):-
	Rnd <- nextInt returns X, 
	Rnd <- nextInt returns Y,
	java_object('java.awt.Point', [X,Y], Obj),
	List <- add(Obj), N1 is N - 1,
	loop(N1, List, Rnd).

print_elements(Iterator):-
	Iterator <- hasNext returns true, !, 
	Iterator <- next returns Obj,
	stdout <- println(Obj), 
	print_elements(Iterator).
print_elements(_).

test(N) :-
	class('java.lang.System') <- currentTimeMillis returns T0,
	java_object('java.util.ArrayList', [], List),
	java_object('java.util.Random', [], Rnd),
	loop(N, List, Rnd),
	List <- iterator returns Iterator, 
	print_elements(Iterator),
	class('java.lang.System') <- currentTimeMillis returns T1,
	DT is T1 - T0, 
	stdout <- println(DT).