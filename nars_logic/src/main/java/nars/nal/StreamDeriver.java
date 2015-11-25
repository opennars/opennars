package nars.nal;

import nars.task.Task;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** uses Java8 Stream API to apply rules to derivations
 *  NOT ready
 * */
public class StreamDeriver extends SimpleDeriver {

    public final Stream<Task> run(RuleMatch m, final List<TaskRule> u, final int maxNAL) {
        return run(m, u.stream(), maxNAL);
    }

    public Stream<Task> run(RuleMatch m, final Stream<TaskRule> rules, final int maxNAL) {

        //Predicate<Level> pcFilter = Level.maxFilter(maxNAL);

        return rules.
                //filter( /* filter the entire rule */ pcFilter).
                        map(r -> m.run(r)).
                        flatMap(p ->
                                        (p != null) ? Stream.of(p) : Stream.empty()
                        ).
                //filter( /* filter each rule postcondition */ pcFilter).
                        map(p -> m.apply(p)).
                        filter(t -> t != null).flatMap(l -> l.stream()).filter(s -> s!=null);
    }



//        public Stream<Task> forEachRule(final RuleMatch match) {
//            //return forEachRuleExhaustive(match);
//            return forEachRuleByType(match);
//        }

//        public Stream<Task> forEachRuleExhaustive(final RuleMatch match) {
//            return match.run(rules, match.premise.nal());
//        }

}
