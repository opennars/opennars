package nars.nal;


import java.util.function.Predicate;

/**
 * Base class for NARS logical reasoner / inference rules
 */
public interface LogicRule<X> extends Predicate<X> {

    /** return false to cancel subsequent rules for this object; true to continue */
    abstract public boolean test(X x);

//    public LogicRule setAction(Consumer<Event<X>> action) {
//        this.action = action;
//        return this;
//    }

//    public LogicRule setCondition(Selector condition) {
//        this.condition = condition;
//        return this;
//    }
//
//    public void setCondition(Class c) {
//        setCondition(new ClassSelector(c));
//    }


//
//    @Override
//    public void accept(Event<X> e) {
//        if (action!=null)
//            action.accept(e);
//        else
//            throw new RuntimeException(this + " has no defined action");
//    }


}
