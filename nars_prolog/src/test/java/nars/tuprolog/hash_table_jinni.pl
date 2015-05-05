hastable:-
	new_java_class('java.util.Hashtable',C),
	new_java_object(C,void,JavaHashTable),
	table<=JavaHashTable.
	
put_data:-
	table=>T,
	invole_java_method(T,put

test:-
	hashtable(Map),
	class('java.lang.System') <- currentTimeMillis returns T0,
	test(Map,1000),
	class('java.lang.System') <- currentTimeMillis returns T1,
	DT is T1 - T0,
	nl,write(DT),nl.
