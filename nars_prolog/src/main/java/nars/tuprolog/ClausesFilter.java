/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nars.tuprolog;

import nars.tuprolog.util.OneWayList;

import java.util.List;

/**
 * ClausesFilter has the aim to reduce the number of clauses
 * that must be verified for goal unification.
 * <p><em>
 * "Since Prolog programmers have a natural tendency to write code in a
 * data structure-directed manner using discriminating patterns as
 * first argument, it is quite accceptable to limit indexing to key on
 * the first argument only"
 * (Warren's Abstract Machine - A tutorial reconstruction, Hassan Aït-Kaci)
 * </em></p>
 * <p>
 * According to the citation above, the actual implementation checks goal's
 * first argument type and uses it to choose clauses.
 * </p>
 *
 * @author Paolo Contessi
 * @since 2.2
 */
class ClausesFilter {
    public static boolean OPTIMIZATION_ENABLED = true;

    /**
     * Iterates familyClauses and select those claues which, in all probability,
     * could match with given goal.
     *
     * @param familyClauses The list of clauses whose head predicate
     *                      as same name and same arity
     * @param goal          The goal to be resolved
     * @return              The list of clauses (subset of
     *                      <code>familyClauses</code>) which more probably
     *                      match with <code>goal</code>
     */
    public static OneWayList<ClauseInfo> filterClauses(List<ClauseInfo> familyClauses, Term goal){
        if(OPTIMIZATION_ENABLED && goal instanceof Struct){
            Struct g = (Struct) goal.getTerm();

            /* If no arguments no optimization can be applied */
            if(g.getArity() < 2){
                return returnAllClauses(familyClauses);
            }

            /* Retrieves first argument and checks type */
            Term t = g.getArg(1).getTerm();
            if(t instanceof Var){
                /* 
                 * if first argument is an unbounded variable,
                 * no reasoning is possible
                 */
                return returnAllClauses(familyClauses);
            } else if(t.isAtomic()){
                if(t instanceof Number){
                    /* selects clauses which has a numeric first argument */
                    return selectNumeric(familyClauses, (Number) t);
                }

                /* selects clauses which has an atomic first argument */
                return selectConstant(familyClauses, t);
            } else if(t instanceof Struct){
                if(isAList(t)){
                    /* select clauses which has a list as first argument */
                    return selectList(familyClauses);
                }

                /* selects clauses which has the same struct
                 (same functor and same arity) as first argument*/
                return selectStruct(familyClauses, ((Struct) t).getPredicateIndicator());
            }
        }

        /* Default behaviour: no optimization done */
        return returnAllClauses(familyClauses);
    }

    /*
     * Returns all the family clauses, no optimization performed.
     */
    private static OneWayList<ClauseInfo> returnAllClauses(List<ClauseInfo> familyClauses){
        return OneWayList.transform2(familyClauses);
    }

    /*
     * Returns only the clauses whose first argument is a number equal to
     * the given one (from goal's first argument)
     */
    private static OneWayList<ClauseInfo> selectNumeric(List<ClauseInfo> familyClauses, Number t) {
        OneWayList<ClauseInfo> result = null;
        OneWayList<ClauseInfo> p = null;

        for(Object obj : familyClauses){
            ClauseInfo clause = (ClauseInfo) obj;
            Term arg = clause.getHead().getArg(0).getTerm();

            if((arg instanceof Var) ||
                    (arg instanceof Number && arg.isEqual(t))){
                OneWayList<ClauseInfo> l = new OneWayList<>(clause, null);
            
                if(result == null){
                    result = p = l;
                } else {
                    p.setTail(l);
                    p = l;
                }
            }
        }

        return result;
    }

    /*
     * Returns only the clauses whose first argument is a struct with a
     * predicate indicator equal to the given one (from goal's first argument)
     */
    private static OneWayList<ClauseInfo> selectStruct(List<ClauseInfo> familyClauses, String predIndicator) {
        OneWayList<ClauseInfo> result = null;
        OneWayList<ClauseInfo> p = null;

        for(Object obj : familyClauses){
            ClauseInfo clause = (ClauseInfo) obj;
            Term arg = clause.getHead().getArg(0).getTerm();

            if(arg instanceof Var ||
                    ((Struct) arg).getPredicateIndicator().equals(predIndicator)){
                OneWayList<ClauseInfo> l = new OneWayList<>(clause, null);

                if(result == null){
                    result = p = l;
                } else {
                    p.setTail(l);
                    p = l;
                }
            }
        }

        return result;
    }

    private static boolean isAList(Term t) {
        /*
         * Checks if a Struct is also a list.
         * A list can be an empty list, or a Struct with name equals to "."
         * and arity equals to 2.
         */
        if(t instanceof Struct){
            Struct s = (Struct) t;
            return s.isEmptyList() || (s.getName().equals(".") && s.getArity() == 2);
        }

        return false;

    }
    
    /*
     * Returns only the clauses whose first argument is a list
     * (as the goal's first argument)
     */
    private static OneWayList<ClauseInfo> selectList(List<ClauseInfo> familyClauses) {
        OneWayList<ClauseInfo> result = null;
        OneWayList<ClauseInfo> p = null;

        for(Object obj : familyClauses){
            ClauseInfo clause = (ClauseInfo) obj;
            Term arg = clause.getHead().getArg(0).getTerm();

            if(arg instanceof Var || isAList(arg)){
                OneWayList<ClauseInfo> l = new OneWayList<>(clause, null);

                if(result == null){
                    result = p = l;
                } else {
                    p.setTail(l);
                    p = l;
                }
            }
        }

        return result;
    }

    /*
     * Returns only the clauses whose first argument is a constant equals
     * to the given one (from goal's first argument)
     */
    private static OneWayList<ClauseInfo> selectConstant(List<ClauseInfo> familyClauses, Term t) {
        OneWayList<ClauseInfo> result = null;
        OneWayList<ClauseInfo> p = null;

        for(Object obj : familyClauses){
            ClauseInfo clause = (ClauseInfo) obj;
            Term arg = clause.getHead().getArg(0).getTerm();

            if(arg instanceof Var || arg.isAtomic()){
                OneWayList<ClauseInfo> l = new OneWayList<>(clause, null);

                if(result == null){
                    result = p = l;
                } else {
                    p.setTail(l);
                    p = l;
                }
            }
        }

        return result;
    }
    
}
