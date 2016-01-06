package nars.nal;

import nars.term.Term;
import nars.term.compound.Compound;

public interface PremiseAware {
	Term function(Compound args, PremiseMatch r);
}
