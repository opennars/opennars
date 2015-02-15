package nars.logic.entity.tlink;

import nars.logic.entity.Term;


public interface TermLinkKey {
    public Term getTarget();

    public String getPrefix();

    default public int termLinkHashCode() {
        return 31 * ((31 * getPrefix().hashCode()) + getTarget().hashCode());
    }

    default public boolean termLinkEquals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        TermLinkKey t = (TermLinkKey) obj;
        return getPrefix().equals(t.getPrefix()) && getTarget().equals(t.getTarget());
    }

}