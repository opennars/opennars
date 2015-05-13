package nars.nal.tlink;

import nars.nal.term.Term;
import nars.util.utf8.Utf8;


public interface TermLinkKey {

    public Term getSource();
    public Term getTarget();

    public byte[] getLinkKey();

//    default public int termLinkHashCode() {
//        //TODO cache this value while prefix and target remain the same; it is called more than necessary
//        return Objects.hash(getPrefix(), getTarget());
//    }

    default public boolean termLinkEquals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        TermLinkKey t = (TermLinkKey) obj;

        return Utf8.equals2(getLinkKey(), t.getLinkKey());

        //return getPrefix().equals(t.getPrefix()) && getTarget().equals(t.getTarget());//

        //shouldnt be necessary to compare the source because prefix will contain the necessary information how it relates to target
        // && getSource().equals(t.getSource());
    }


}