package nars.logic;


import nars.logic.rule.TaskFireTerm;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.ClassSelector;
import reactor.event.selector.PredicateSelector;
import reactor.event.selector.Selector;
import reactor.function.Consumer;
import reactor.function.Predicate;

/**
 * Base class for NARS logical reasoner / inference rules
 */
public class LogicRule<X> implements Consumer<Event<X>> {

    private Selector condition;
    private Consumer<Event<X>> action;
    private Registration registration;

    public LogicRule(Selector condition, Consumer<Event<X>> action) {
        super();
        this.condition = condition;
        this.action = action != null ? action : this;
    }

    public LogicRule(Predicate<Object> p, Consumer<Event<X>> action) {
        this(new PredicateSelector(p), action);
    }

    public LogicRule(Class<? extends X> x, Consumer<Event<X>> action) {
        this(new ClassSelector(x), action);
    }


    //TODO infer the generic argument type via reflection
    @Deprecated public LogicRule(Class<? extends X> c) {
        this(c, null);
    }


    public LogicRule setAction(Consumer<Event<X>> action) {
        this.action = action;
        return this;
    }

    public LogicRule setCondition(Selector condition) {
        this.condition = condition;
        return this;
    }

    public void setCondition(Class c) {
        setCondition(new ClassSelector(c));
    }


    public Selector condition() { return condition; }

    @Override
    public void accept(Event<X> e) {
        if ((action!=null) && (action!=this))
            action.accept(e);
    }


    void setRegistration(Registration reg) {
        this.registration = reg;
    }

    public Registration getRegistration() {
        return registration;
    }
}
