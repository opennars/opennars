loop(0,_,_):- !.
loop(N,List,Rnd):-
	invoke_java_method(Rnd,nextInt,X), 
	invoke_java_method(Rnd,nextInt,Y), 
	new_java_object('java.awt.Point'(X,Y), Obj),
	invoke_java_method(List,add(Obj),_),
	N1 is N - 1,
	loop(N1, List, Rnd).

print_elements(List,0):-!.
print_elements(List,N):-
	invoke_java_method(List,get(N),Obj), !, 
	stdout => S,
	invoke_java_method(S,println(Obj),_), 
	N1 is N - 1,
	print_elements(List,N1).

test(N) :-
	ctime(T0),
	new_java_object('java.util.ArrayList', List),
	new_java_object('java.util.Random', Rnd),
	loop(N, List, Rnd),
	invoke_java_method(List,size,Len),
	Len1 is Len - 1, 
	new_java_class('java.lang.System',S),
	get_java_field(S,out,Stdout),
	stdout <= Stdout,
	print_elements(List,Len1),
	ctime(T1),
	DT is T1 - T0, 
	println(DT).