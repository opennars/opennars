package nars.nal;


import java.util.function.Predicate;

/**
 * A stage in a NARS logical reasoner / inference pipeline.
 * It processes a premise and returns true if the pipeline
 * should be allowed to continue to the next stage, or false
 * to cancel this premise's processing any further.
 */
public interface LogicStage<X> extends Predicate<X> {

    public static final boolean CONTINUE = true;
    public static final boolean STOP = false;

    /** return false to stop subsequent rules for this item; true to continue */
    @Override
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
