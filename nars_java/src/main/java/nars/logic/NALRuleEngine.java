package nars.logic;

import nars.core.Memory;
import nars.logic.entity.Sentence;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.nal1.LocalRules;
import nars.logic.rule.TaskFireTerm;
import reactor.event.Event;
import reactor.event.selector.ClassSelector;
import reactor.event.selector.Selector;
import reactor.function.Consumer;
import reactor.function.Predicates;
import reactor.rx.action.BatchAction;
import reactor.rx.action.CombineAction;

/**
 * General class which has all NAL Rules
 */
public class NALRuleEngine extends RuleEngine {

    public NALRuleEngine(Memory memory) {
        super();

        add(new FilterEqualSubtermsInRespectToImageAndProduct());

        /** FilterMatchingTaskAndBelief */
        add(new LogicRule(FireConcept.class, new TaskFireTerm() {
            @Override public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {
                Sentence currentBelief = f.getCurrentBelief();
                if ((currentBelief!=null) && (LocalRules.match(taskLink.targetTask, currentBelief, f))) {
                    //Filter this from further processing
                    return false;
                }
                return true;
            }
        }));
/*
        add(new LogicRule(FireConcept.class, new TaskFireTerm() {
            @Override
            public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {

                System.out.println(f + " " + taskLink + " " + termLink);
                return true;
            }
        })); */
    }

    public void reason(FireConcept fireConcept, TaskLink tLink, TermLink bLink) {
        base.notify(FireConcept.class, fireConcept);
    }


    /** this is more like a filter, action wont be necessary */
    public static class FilterEqualSubtermsInRespectToImageAndProduct
    extends LogicRule<FireConcept> implements TaskFireTerm {

        public FilterEqualSubtermsInRespectToImageAndProduct() {
            super(FireConcept.class);
        }

        @Override
        public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink) {
            if(Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(),termLink.getTerm()))
                return false;
            return true;
        }

    }
}
