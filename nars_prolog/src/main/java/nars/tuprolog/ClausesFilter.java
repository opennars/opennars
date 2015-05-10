///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package nars.tuprolog;
//
//import nars.nal.term.Term;
//import nars.tuprolog.util.OneWayList;
//
//import java.util.List;
//
///**
// * ClausesFilter has the aim to reduce the number of clauses
// * that must be verified for goal unification.
// * <p><em>
// * "Since Prolog programmers have a natural tendency to write code in a
// * data structure-directed manner using discriminating patterns as
// * first argument, it is quite accceptable to limit indexing to key on
// * the first argument only"
// * (Warren's Abstract Machine - A tutorial reconstruction, Hassan Aït-Kaci)
// * </em></p>
// * <p>
// * According to the citation above, the actual implementation checks goal's
// * first argument type and uses it to choose clauses.
// * </p>
// *
// * @author Paolo Contessi
// * @since 2.2
// */
//class ClausesFilter {
//    public static boolean OPTIMIZATION_ENABLED = true;
//
//    /**
//     * Iterates familyClauses and select those claues which, in all probability,
//     * could match with given goal.
//     *
//     * @param familyClauses The list of clauses whose head predicate
//     *                      as same name and same arity
//     * @param goal          The goal to be resolved
//     * @return              The list of clauses (subset of
//     *                      <code>familyClauses</code>) which more probably
//     *                      match with <code>goal</code>
//     */
//    public static OneWayList<Clause> filterClauses(List<Clause> familyClauses, PTerm goal){
//        if(OPTIMIZATION_ENABLED && goal instanceof Struct){
//            Struct g = (Struct) goal.getTerm();
//
//            /* If no arguments no optimization can be applied */
//            if(g.size() < 2){
//                return returnAllClauses(familyClauses);
//            }
//
//            /* Retrieves first argument and checks type */
//            Term t = g.getTermX(1).getTerm();
//            if(t instanceof Var){
//                /*
//                 * if first argument is an unbounded variable,
//                 * no reasoning is possible
//                 */
//                return returnAllClauses(familyClauses);
//            } else if(t.isAtomic()){
//                if(t instanceof PNum){
//                    /* selects clauses which has a numeric first argument */
//                    return selectNumeric(familyClauses, (PNum) t);
//                }
//
//                /* selects clauses which has an atomic first argument */
//                return selectConstant(familyClauses, t);
//            } else if(t instanceof Struct){
//                if(isAList(t)){
//                    /* select clauses which has a list as first argument */
//                    return selectList(familyClauses);
//                }
//
//                /* selects clauses which has the same struct
//                 (same functor and same arity) as first argument*/
//                return selectStruct(familyClauses, ((Struct) t).getPredicateIndicator());
//            }
//        }
//
//        /* Default behaviour: no optimization done */
//        return returnAllClauses(familyClauses);
//    }
//
//    /*
//     * Returns all the family clauses, no optimization performed.
//     */
//    private static OneWayList<Clause> returnAllClauses(List<Clause> familyClauses){
//        return OneWayList.transform2(familyClauses);
//    }
//
//    /*
//     * Returns only the clauses whose first argument is a number equal to
//     * the given one (from goal's first argument)
//     */
//    private static OneWayList<Clause> selectNumeric(List<Clause> familyClauses, PNum t) {
//        OneWayList<Clause> result = null;
//        OneWayList<Clause> p = null;
//
//        for(Object obj : familyClauses){
//            Clause clause = (Clause) obj;
//            PTerm arg = clause.getHead().getTermX(0).getTerm();
//
//            if((arg instanceof Var) ||
//                    (arg instanceof PNum && arg.isEqual(t))){
//                OneWayList<Clause> l = new OneWayList<>(clause, null);
//
//                if(result == null){
//                    result = p = l;
//                } else {
//                    p.setTail(l);
//                    p = l;
//                }
//            }
//        }
//
//        return result;
//    }
//
//    /*
//     * Returns only the clauses whose first argument is a struct with a
//     * predicate indicator equal to the given one (from goal's first argument)
//     */
//    private static OneWayList<Clause> selectStruct(List<Clause> familyClauses, String predIndicator) {
//        OneWayList<Clause> result = null;
//        OneWayList<Clause> p = null;
//
//        for(Object obj : familyClauses){
//            Clause clause = (Clause) obj;
//            PTerm arg = clause.getHead().getTermX(0).getTerm();
//
//            if(arg instanceof Var ||
//                    ((Struct) arg).getPredicateIndicator().equals(predIndicator)){
//                OneWayList<Clause> l = new OneWayList<>(clause, null);
//
//                if(result == null){
//                    result = p = l;
//                } else {
//                    p.setTail(l);
//                    p = l;
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private static boolean isAList(PTerm t) {
//        /*
//         * Checks if a Struct is also a list.
//         * A list can be an empty list, or a Struct with name equals to "."
//         * and arity equals to 2.
//         */
//        if(t instanceof Struct){
//            Struct s = (Struct) t;
//            return s.isEmptyList() || (s.getName().equals(".") && s.size() == 2);
//        }
//
//        return false;
//
//    }
//
//    /*
//     * Returns only the clauses whose first argument is a list
//     * (as the goal's first argument)
//     */
//    private static OneWayList<Clause> selectList(List<Clause> familyClauses) {
//        OneWayList<Clause> result = null;
//        OneWayList<Clause> p = null;
//
//        for(Object obj : familyClauses){
//            Clause clause = (Clause) obj;
//            PTerm arg = clause.getHead().getTermX(0).getTerm();
//
//            if(arg instanceof Var || isAList(arg)){
//                OneWayList<Clause> l = new OneWayList<>(clause, null);
//
//                if(result == null){
//                    result = p = l;
//                } else {
//                    p.setTail(l);
//                    p = l;
//                }
//            }
//        }
//
//        return result;
//    }
//
//    /*
//     * Returns only the clauses whose first argument is a constant equals
//     * to the given one (from goal's first argument)
//     */
//    private static OneWayList<Clause> selectConstant(List<Clause> familyClauses, PTerm t) {
//        OneWayList<Clause> result = null;
//        OneWayList<Clause> p = null;
//
//        for(Object obj : familyClauses){
//            Clause clause = (Clause) obj;
//            PTerm arg = clause.getHead().getTermX(0).getTerm();
//
//            if(arg instanceof Var || arg.isAtomic()){
//                OneWayList<Clause> l = new OneWayList<>(clause, null);
//
//                if(result == null){
//                    result = p = l;
//                } else {
//                    p.setTail(l);
//                    p = l;
//                }
//            }
//        }
//
//        return result;
//    }
//
//}
