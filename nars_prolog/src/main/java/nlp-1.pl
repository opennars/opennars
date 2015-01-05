%%% NLP in NAL example, Step 1 %%%

:- ['nal.pl'].

% Given sentence 1
represent(['cat'], cat, [1, 0.9]).

% Given sentence 2
represent(['fish'], fish, [1, 0.9]).

% Given sentence 3
represent(['cat', 'eat', 'fish'], inheritance(product([cat, fish]), food), [1, 0.9]).

% Supplementary induction rules

represent([rpb(X), Wr, Wo], inheritance(product([X, Co]), Cr), V) :-
	represent([Ws], Cs, V1), represent([Ws, Wr, Wo], inheritance(product([Cs, Co]), Cr), V2), f_ind(V1, V2, V).

represent([Ws, Wr, rpb(X)], inheritance(product([Cs, X]), Cr), V) :-
	represent([Wo], Co, V1), represent([Ws, Wr, Wo], inheritance(product([Cs, Co]), Cr), V2), f_ind(V1, V2, V).

% To show the results, type "represent(X, Y, V)."
