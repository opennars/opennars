package nars.logic;

import org.drools.AbstractConsequence;
import org.drools.spi.RuleComponent;

/**
 * Base class for NARS logical reasoner / inference rules
 */
public interface LogicRule<X> extends AbstractConsequence<X> {

    public Object condition();

}
