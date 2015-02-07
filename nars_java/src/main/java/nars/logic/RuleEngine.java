package nars.logic;

import org.drools.AbstractConsequence;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.rule.*;
import org.drools.spi.FieldConstraint;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Applies a set of Rules to inputs, executing appropriate outputs
 */
public class RuleEngine<X> {

    final org.drools.leaps.LeapsRuleBase base = new org.drools.leaps.LeapsRuleBase();

    final Map<FieldConstraint, Column> cols = new HashMap();

    /** represents a singleton fact that can be used for fast firing, not thread safe yet */
    private FactHandle nowHandle = null;

    //TODO separate state from the rulebase so that multiple states can use the same rulebase simultaneously
    public WorkingMemory state;

    public RuleEngine() {
        super();
    }

    public void add(LogicRule<X> l) {
        final LRule r = new LRule( l );
        base.addRule(r);
    }

    public WorkingMemory start() {
        state = base.newWorkingMemory();
        return state;
    }

    //ruleBase.addPackage( this.pkg );

//    /** modifies the default FactHandle object, should be faster than assert/retract
//     * return whether the object was fired
//     * */
//    public void now(X o) {
//
//        //System.out.println(state.getObjects());
//
//        if (nowHandle == null)
//            nowHandle = state.assertObject(o);
//        else {
//            state.modifyObject(nowHandle, o);
//        }
//
//        state.fireAllRules();
//
//    }

    public void fire(X o) {

        FactHandle fh = state.assertObject(o);
        state.fireAllRules();
        state.retractObject(fh);

    }

    class LRule extends Rule {

        public final LogicRule logic;

        public LRule(LogicRule l) {
            super(l.toString());
            this.logic = l;

            setConsequence(l);

            Object cond = l.condition();
            if (cond instanceof EvalCondition) {
                addPattern(cond);
            }
            else if (cond instanceof FieldConstraint) {
                addPattern(getColumn((FieldConstraint)cond));
            }
            else if (cond instanceof Object[]) {
                Object[] a = (Object[])cond;
                for (Object x : a) {
                    //TODO make this process constraint columns if necessary
                    addPattern(x);
                }
            }
            else if (cond instanceof GroupElement) {
                GroupElement g = (GroupElement)cond;
                List cc = g.getChildren();
                for (int i = 0; i < cc.size(); i++) {
                    Object o = cc.get(i);

                    //ensure that Column exists
                    if (o instanceof FieldConstraint) {
                        cc.set(i, getColumn((FieldConstraint)o));
                    }
                }

                addPattern(cond);
            }

        }
    }

    protected Column getColumn(FieldConstraint f) {
        Column c = cols.get(f);
        if (c == null) {
            c = new Column(0 /*cols.size()*/);
            c.addConstraint(f);
            cols.put(f, c);
        }
        return c;
    }

}
