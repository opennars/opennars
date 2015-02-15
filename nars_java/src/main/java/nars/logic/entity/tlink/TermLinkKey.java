package nars.logic.entity.tlink;

import nars.logic.entity.Term;


public interface TermLinkKey {
    public Term getTarget();
    public String getPrefix();
}