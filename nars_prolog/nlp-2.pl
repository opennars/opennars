%%% NLP in NAL example, Step 2 %%%

:- ['nal.pl'].

% Derived sentence 4
represent([rpb(Cs), 'eat', 'fish'], inheritance(product([Cs, fish]), food), [1, 0.45]).

% Derived sentence 5
represent(['cat', 'eat', rpb(Co)], inheritance(product([cat, Co]), food), [1, 0.45]).

% Derived sentence 6
represent([rpb(Cs), 'eat', rpb(Co)], inheritance(product([Cs, Co]), food), [1, 0.29]).

% Given sentence 7
represent(['dog'], dog, [1, 0.9]).

% Given sentence 8
represent(['meat'], meat, [1, 0.9]).

% Supplementary deduction rules

represent([Ws, Wr, Wo], inheritance(product([Cs, Co]), Cr), V) :-
	represent([Ws], Cs, V1), represent([rpb(X), Wr, Wo], inheritance(product([X, Co]), Cr), V2), f_ded(V1, V2, V).

represent([Ws, Wr, Wo], inheritance(product([Cs, Co]), Cr), V) :-
	represent([Wo], Co, V1), represent([Ws, Wr, rpb(X)], inheritance(product([Cs, X]), Cr), V2), f_ded(V1, V2, V).

% To show the results, type "represent(['dog', 'eat', X], Y, V)."
