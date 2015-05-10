package nars.tuprolog;

import nars.nal.term.Term;

import java.util.Iterator;
import java.util.List;

/**
 * @author Matteo Iuliani
 */
public class StageException extends Stage {

    final PTerm catchTerm = PTerm.createTerm("catch(Goal, Catcher, Handler)");
    final PTerm javaCatchTerm = PTerm
            .createTerm("java_catch(Goal, List, Finally)");

    public StageException(Engine c) {
        this.c = c;
        stateName = "Exception";
    }

    void run(Engine.State e) {
        String errorType = e.currentContext.currentGoal.getName();
        if (errorType.equals("throw"))
            prologError(e);
        else
            javaException(e);
    }

    private void prologError(Engine.State e) {
        Term errorTerm = e.currentContext.currentGoal.getTermX(0);
        e.currentContext = e.currentContext.fatherCtx;
        if (e.currentContext == null) {
            // passo nello stato HALT se l?errore non pu? essere gestito (sono
            // arrivato alla radice dell'albero di risoluzione)
            e.nextState = c.END_HALT;
            return;
        }
        while (true) {
            // visito all'indietro l'albero di risoluzione alla ricerca di un
            // subgoal catch/3 il cui secondo argomento unifica con l?argomento
            // dell?eccezione lanciata
            if (e.currentContext.currentGoal.match(catchTerm)
                    && e.currentContext.currentGoal.getTermXP(1).match(errorTerm)) {
                // ho identificato l?ExecutionContext con il corretto subgoal
                // catch/3

                // taglio tutti i punti di scelta generati da Goal
                c.cut();

                // unifico l'argomento di throw/1 con il secondo argomento di
                // catch/3
                List<Var> unifiedVars = e.currentContext.trailingVars
                        .getHead();
                e.currentContext.currentGoal.getTermXP(1).unify(unifiedVars,
                        unifiedVars, errorTerm);

                // inserisco il gestore dell?errore in testa alla lista dei
                // subgoal da eseguire, come definito dal terzo argomento di
                // catch/3. Il gestore deve inoltre essere preparato per
                // l?esecuzione, mantenendo le sostituzioni effettuate durante
                // il processo di unificazione tra l?argomento di throw/1 e il
                // secondo argomento di catch/3
                PTerm handlerTerm = e.currentContext.currentGoal.getTermXP(2);
                Term curHandlerTerm = handlerTerm.getTerm();
                if (!(curHandlerTerm instanceof Struct)) {
                    e.nextState = c.END_FALSE;
                    return;
                }
                // Code inserted to allow evaluation of meta-clause
                // such as p(X) :- X. When evaluating directly terms,
                // they are converted to execution of a call/1 predicate.
                // This enables the dynamic linking of built-ins for
                // terms coming from outside the demonstration context.
                if (handlerTerm != curHandlerTerm)
                    handlerTerm = new Struct("call", curHandlerTerm);
                Struct handler = (Struct) handlerTerm;
                c.identify(handler);
                SubGoalTree sgt = new SubGoalTree();
                sgt.addChild(handler);
                c.pushSubGoal(sgt);
                e.currentContext.currentGoal = handler;

                // passo allo stato GOAL_SELECTION
                e.nextState = c.GOAL_SELECTION;
                return;
            } else {
                // passo all'ExecutionContext successivo
                e.currentContext = e.currentContext.fatherCtx;
                if (e.currentContext == null) {
                    // passo nello stato HALT se l?errore non pu? essere gestito
                    // (sono arrivato alla radice dell'albero di risoluzione)
                    e.nextState = c.END_HALT;
                    return;
                }
            }
        }
    }

    private void javaException(Engine.State e) {
        Term exceptionTerm = e.currentContext.currentGoal.getTermX(0);
        e.currentContext = e.currentContext.fatherCtx;
        if (e.currentContext == null) {
            // passo nello stato HALT se l?errore non pu? essere gestito (sono
            // arrivato alla radice dell'albero di risoluzione)
            e.nextState = c.END_HALT;
            return;
        }
        while (true) {
            // visito all'indietro l'albero di risoluzione alla ricerca di un
            // subgoal java_catch/3 che abbia un catcher unificabile con
            // l'argomento dell'eccezione lanciata
            if (e.currentContext.currentGoal.match(javaCatchTerm)
                    && javaMatch(e.currentContext.currentGoal.getTermXP(1),
                            exceptionTerm)) {
                // ho identificato l?ExecutionContext con il corretto subgoal
                // java_catch/3

                // taglio tutti i punti di scelta generati da JavaGoal
                c.cut();

                // unifico l'argomento di java_throw/1 con il catcher
                // appropriato e recupero l'handler corrispondente
                List<Var> unifiedVars = e.currentContext.trailingVars
                        .getHead();
                PTerm handlerTerm = javaUnify(e.currentContext.currentGoal
                        .getTermXP(1), exceptionTerm, unifiedVars);
                if (handlerTerm == null) {
                    e.nextState = c.END_FALSE;
                    return;
                }

                // inserisco il gestore e il finally (se presente) in testa alla
                // lista dei subgoal da eseguire. I due predicati devono inoltre
                // essere preparati per l?esecuzione, mantenendo le sostituzioni
                // effettuate durante il processo di unificazione tra
                // l'eccezione e il catcher
                Term curHandlerTerm = handlerTerm.getTerm();
                if (!(curHandlerTerm instanceof Struct)) {
                    e.nextState = c.END_FALSE;
                    return;
                }
                PTerm finallyTerm = e.currentContext.currentGoal.getTermXP(2);
                Term curFinallyTerm = finallyTerm.getTerm();
                // verifico se c'? il blocco finally
                boolean isFinally = true;
                if (curFinallyTerm instanceof Int) {
                    Int finallyInt = (Int) curFinallyTerm;
                    if (finallyInt.intValue() == 0)
                        isFinally = false;
                    else {
                        // errore di sintassi, esco
                        e.nextState = c.END_FALSE;
                        return;
                    }
                } else if (!(curFinallyTerm instanceof Struct)) {
                    e.nextState = c.END_FALSE;
                    return;
                }
                // Code inserted to allow evaluation of meta-clause
                // such as p(X) :- X. When evaluating directly terms,
                // they are converted to execution of a call/1 predicate.
                // This enables the dynamic linking of built-ins for
                // terms coming from outside the demonstration context.
                if (handlerTerm != curHandlerTerm)
                    handlerTerm = new Struct("call", curHandlerTerm);
                if (finallyTerm != curFinallyTerm)
                    finallyTerm = new Struct("call", curFinallyTerm);

                Struct handler = (Struct) handlerTerm;
                c.identify(handler);
                SubGoalTree sgt = new SubGoalTree();
                sgt.addChild(handler);
                if (isFinally) {
                    Struct finallyStruct = (Struct) finallyTerm;
                    c.identify(finallyStruct);
                    sgt.addChild(finallyStruct);
                }
                c.pushSubGoal(sgt);
                e.currentContext.currentGoal = handler;

                // passo allo stato GOAL_SELECTION
                e.nextState = c.GOAL_SELECTION;
                return;

            } else {
                // passo all'ExecutionContext successivo
                e.currentContext = e.currentContext.fatherCtx;
                if (e.currentContext == null) {
                    // passo nello stato HALT se l?errore non pu? essere gestito
                    // (sono arrivato alla radice dell'albero di risoluzione)
                    e.nextState = c.END_HALT;
                    return;
                }
            }
        }
    }

    // verifica se c'? un catcher unificabile con l'argomento dell'eccezione
    // lanciata
    private boolean javaMatch(PTerm arg1, Term exceptionTerm) {
        if (!arg1.isList())
            return false;
        Struct list = (Struct) arg1;
        if (list.isEmptyList())
            return false;
        Iterator<? extends Term> it = list.listIterator();
        while (it.hasNext()) {
            Term nextTerm = it.next();
            if (!nextTerm.isCompound())
                continue;
            Struct element = (Struct) nextTerm;
            if (!element.getName().equals(","))
                continue;
            if (element.size() != 2)
                continue;
            if (element.getTermXP(0).match(exceptionTerm)) {
                return true;
            }
        }
        return false;
    }

    // unifica l'argomento di java_throw/1 con il giusto catcher e restituisce
    // l'handler corrispondente
    private PTerm javaUnify(Term arg1, Term exceptionTerm, List<Var> unifiedVars) {
        Struct list = (Struct) arg1;
        Iterator<? extends Term> it = list.listIterator();
        while (it.hasNext()) {
            Term nextTerm = it.next();
            if (!nextTerm.isCompound())
                continue;
            Struct element = (Struct) nextTerm;
            if (!element.getName().equals(","))
                continue;
            if (element.size() != 2)
                continue;
            if (element.getTermXP(0).match(exceptionTerm)) {
                element.getTermXP(0)
                        .unify(unifiedVars, unifiedVars, exceptionTerm);
                return element.getTermXP(1);
            }
        }
        return null;
    }
}