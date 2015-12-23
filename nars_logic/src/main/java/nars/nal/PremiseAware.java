package nars.nal;

import nars.term.Term;
import nars.term.compound.Compound;


public interface PremiseAware {
    public Term function(Compound args, RuleMatch r);
}
