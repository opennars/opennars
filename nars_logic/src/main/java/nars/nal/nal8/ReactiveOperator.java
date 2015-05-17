//package nars.nal.nal8;
//
//import nars.NAR;
//import nars.event.NARReaction;
//import nars.util.event.Reaction;
//
///**
// * ReactiveOperator combines Operator with a Reaction interface to
// * provide a convenient API for defining an Operator that reacts
// * to both its own received invocations (via execute()) and additional specific events.
// */
//abstract public class ReactiveOperator extends Operator {
//
//    private NARReaction reaction;
//    private NAR nar;
//
//    public ReactiveOperator(String name) {
//        super();
//    }
//
//
//    public NAR nar() { return nar; }
//
//    /** events to react to */
//    abstract public Class[] getEvents();
//
//    @Override
//    public boolean setEnabled(NAR n, boolean enabled) {
//
//        this.nar = n;
//
//        if (reaction == null) {
//            reaction = new NARReaction(n, false, getEvents()) {
//
//                @Override public void event(Class event, Object[] args) {
//                    ReactiveOperator.this.event(event, args);
//                }
//            };
//        }
//
//        reaction.setActive(enabled);
//
//        if (enabled) on(n); else off(n);
//
//        return true;
//    }
//
//    protected void on(NAR n) {
//
//    }
//
//    protected void off(NAR n) {
//
//    }
//
//    @Override
//    abstract public void event(Class event, Object[] args);
//
//}
