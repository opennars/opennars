package nars.nal.tlink;

import nars.Global;
import nars.nal.term.Term;
import nars.util.utf8.Utf8;


public interface TermLinkKey {

    public Term getTarget();

    public byte[] getLinkKey();

//    default public int termLinkHashCode() {
//        //TODO cache this value while prefix and target remain the same; it is called more than necessary
//        return Objects.hash(getPrefix(), getTarget());
//    }

    default public boolean termLinkEquals(Object obj) {
        return termLinkEquals(obj, false);
    }

    default public boolean termLinkEquals(Object obj, boolean testHash) {
        if (obj == null) return false;
        if (obj == this) return true;

        TermLinkKey t = (TermLinkKey) obj;

        if (Global.DEBUG) {
            if (hashCode() == t.hashCode() && !getTarget().equals(t.getTarget())) {
                System.err.println("hash collision but apparently not equal, may be problem: " + this + " ==hash== " + obj);
            }
        }

        if (testHash && hashCode()!=t.hashCode()) return false;

        if (Utf8.equals2(getLinkKey(), t.getLinkKey()) && getTarget().equals(t.getTarget())) {
//            if (!getSource().equals(t.getSource())) {
//                System.out.println("nonequal " + this + " " + obj);
//
//            }
            return true;
        }
        return false;

        //return getPrefix().equals(t.getPrefix()) && getTarget().equals(t.getTarget());//

        //shouldnt be necessary to compare the source because prefix will contain the necessary information how it relates to target
        // && getSource().equals(t.getSource());
    }


}