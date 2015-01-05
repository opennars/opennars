% inhritance database, should be added on the fly as a theory
inheritance2([a,b,[0.9,1.0]]).
inheritance2([b,a,[0.9,1.0]]).

% idea, find all inheritances in the database, form catesian product,
% iterate over this and do the NARS inference

% the cartesian product and the iteration need some love :)
a(L) :-
	findall(X,inheritance2(X),L),
	cartprod3(L,L2).

% cartesian product
% from http://www.cs.columbia.edu/~fotis/prolog/pr2/cartesian.pl
cartprod3(S, L) :-
   findall(R, cart(S, R), L).

cart([], []).
cart([[A | _] | T], [A | R]) :-
   cart(T, R).

cart([[_ | B] | T], R) :-
   cart([B | T], R).
