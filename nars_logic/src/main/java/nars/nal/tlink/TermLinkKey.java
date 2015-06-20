package nars.nal.tlink;

import nars.nal.term.Term;
import nars.nal.term.Termed;
import nars.util.data.Util;
import nars.util.utf8.Utf8;


public interface TermLinkKey  {

    public Term getTarget();

    public byte[] prefix();



//    default public int termLinkHashCode() {
//        //TODO cache this value while prefix and target remain the same; it is called more than necessary
//        return Objects.hash(getPrefix(), getTarget());
//    }

    default public boolean termLinkEquals(final Object obj) {
        if (this == obj) return true;
        if (obj instanceof TermLinkKey) {
            TermLinkKey tl = (TermLinkKey) obj;
            return Utf8.equals2(prefix(), tl.prefix()) && getTarget().equals(tl.getTarget());
        }
        return false;
    }

//    /** the result of this should be cached */
//    default int hash() {
//        return hash(prefix(), getTarget());
//    }

    public static int hash(byte[] prefix, Term target) {
        return (int)Util.ELFHash(prefix, target.hashCode());
    }

//    /** key + target */
//    default public boolean termLinkEquals(Object obj, boolean testHash) {
//        if (obj == null) return false;
//        if (obj == this) return true;
//
//        TermLinkKey t = (TermLinkKey) obj;
//
//        if (Global.DEBUG) {
//            if (hashCode() == t.hashCode() && !getTarget().equals(t.getTarget())) {
//                System.err.println("hash collision but apparently not equal, may be problem: " + this + " ==hash== " + obj);
//            }
//        }
//
//
//
//
//        if (testHash && hashCode()!=t.hashCode()) return false;
//
//        if (getLinkKey().equals(t.getLinkKey()) && getTarget().equals(t.getTarget())) {
////            if (!getSource().equals(t.getSource())) {
////                System.out.println("nonequal " + this + " " + obj);
////
////            }
//            return true;
//        }
//        return false;
//
//        //return getPrefix().equals(t.getPrefix()) && getTarget().equals(t.getTarget());//
//
//        //shouldnt be necessary to compare the source because prefix will contain the necessary information how it relates to target
//        // && getSource().equals(t.getSource());
//    }


}