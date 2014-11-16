loop(0,_,_):- !.
loop(N,List,Rnd):-
	Rnd <- nextInt returns X, 
	Rnd <- nextInt returns Y,
	java_object('java.awt.Point', [X,Y], Obj),
	List <- add(Obj), N1 is N - 1,
	loop(N1, List, Rnd).

print_elements(List,0):-!.
print_elements(List,N):-!.
	List <- get(N) returns Obj, !, 
	stdout <- println(Obj), 
	N1 is N - 1,
	print_elements(List,N1).
	
test(N) :-
	class('java.lang.System') <- currentTimeMillis returns T0,
	java_object('java.util.ArrayList', [], List),
	java_object('java.util.Random', [], Rnd),
	loop(N, List, Rnd),
	List <- size returns Len,
	Len1 is Len - 1, 
	print_elements(List,Len1),
	class('java.lang.System') <- currentTimeMillis returns T1,
	DT is T1 - T0, 
	stdout <- println(DT).