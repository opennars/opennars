package nars.logic;


import nars.event.EventEmitter;
import reactor.event.registry.Registration;

/**
 * Applies a set of Rules to inputs, executing appropriate outputs
 */
public class RuleEngine<X> {

    EventEmitter base;

    public RuleEngine() {
        super();
        base = new EventEmitter();
    }

    public boolean add(LogicRule l) {

        if (l.getRegistration()!=null) {
            /** already added somewhere */
            return false;
        }

        Registration reg = base.on(l.condition(), l);
        l.setRegistration(reg);

        return true;
    }

    public void fire(X x) {
        base.notify(x);
    }

    //void remove(LogicRule l) { ..


}
