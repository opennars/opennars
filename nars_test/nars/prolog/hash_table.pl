hashtable(Map):-
	java_object('java.util.HashMap',[],Map).
	
put_data(Map,Key,Data):-
	Map <- put(Key,Data).
	
get_data(Map,Key,Data):-
	Map <- get(Key) returns Data.

remove_data(Map,Key):-
	Map <- remove(Key).



test(Map,0):-!.
test(Map,N):-
	java_object('java.lang.Integer',[N],Data),
	put_data(Map,key(N),Data),
	get_data(Map,key(N),_),
	remove_data(Map,key(N)),
	N1 is N - 1,
	test(Map,N1).


test(N):-
	hashtable(Map),
	class('java.lang.System') <- currentTimeMillis returns T0,
	test(Map,N),
	class('java.lang.System') <- currentTimeMillis returns T1,
	DT is T1 - T0,
	nl,write(DT),nl.

	
	
test2(Map,0):-!.
test2(Map,N):-
	java_object('java.lang.Integer',[N],Data),
	dict_put(Map,key(N),Data),
	dict_get(Map,key(N),_),
	dict_remove(Map,key(N)),
	N1 is N - 1,
	test2(Map,N1).


test2(N):-
	new_dict(Map),
	class('java.lang.System') <- currentTimeMillis returns T0,
	test2(Map,N),
	class('java.lang.System') <- currentTimeMillis returns T1,
	DT is T1 - T0,
	nl,write(DT),nl.
	