package nars.nal;

import nars.nal.meta.ProcTerm;
import nars.nal.meta.ThenFork;

/**
 * reverting fork for use during premise matching
 */
public class PremiseMatchFork extends ThenFork<PremiseMatch> {

    public PremiseMatchFork(ProcTerm<PremiseMatch>[] n) {
        super(n);
    }

    @Override
    public void accept(PremiseMatch m) {
        int revertTime = m.now();
        for (ProcTerm<PremiseMatch> s : terms()) {
            s.accept(m);
            m.revert(revertTime);
        }
    }
}
