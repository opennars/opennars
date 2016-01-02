package inc.glamdring.vtables;

/**
 * ref class -- approximation of c++ '&'
 * User: jim
 * Date: Sep 20, 2008
 * Time: 12:27:26 AM
 */

public class _ref<Ω> implements _edge<Ω, _ptr> {
    //static final XStream X_STREAM = new XStream();
    Ω l; 
    _ptr r;

    public Ω Ω() {
        return l;
    }

    /**
     * grab left from the incoming, if any,
     * return left in all cases.
     *
     * @param e
     * @return
     */
    @Override
    public Ω demote(_edge<Ω, _ptr> e) {

        if (e != this)
            bind(e.demote(e), e.promote(e));
        return Ω();
    }                              

    public _ptr µ() {
        return r;
    }

    /**
     * grab right from the incoming, if any,
     * and return right in all cases.
     *
     * @param edge
     * @return
     */
    @Override
    public _ptr promote(_edge<Ω, _ptr> edge) {
        if (edge != this) {
            bind(edge.demote(edge), edge.promote(edge));
        }
        return µ();
    }

    @Override
    public _edge<Ω, _ptr> bind(Ω ω, _ptr ptr) {
        return null;
    }

    @Override
    public Ω reify(_ptr void$) {
        return null;
    }


//    /**
//     * bind and write pointer
//     *
//     * @param ?   object
//     * @param ref heap waiting for a write, several
//     * @return ussualy this
//     */
//    public _edge<Ω, _ptr> bind(Ω Ω , _ptr ref) {
//        byte[] bytes = null;
//        l =  Ω;
//
//        if (bytes == null) bytes = X_STREAM.toXML(Ω).getBytes();
//        Integer integer = ref.$r();
//        this.r = (_ptr) ref.bind(ref.demote(ref).putInt(bytes.length).put(bytes), integer);
//        return this;
//    }

//    /**
//     * reads object from the first ptr sent in, or returns the most local version
//     *
//     * @param void$
//     * @return
//     */
//    @SuppressWarnings("unchecked")
//    public Ω reify(_ptr void$) {/*
//        for (voidp voidp : voidp) */
//        {
//            ByteBuffer buffer = void$.l$();
//            Integer integer = void$.$r();
//            buffer.getInt(integer);
//            ByteBuffer buffer1 = (ByteBuffer) buffer.slice().limit(integer);
//            String s = buffer1.asCharBuffer().toString();
//            Ω fromXML = (Ω) X_STREAM.fromXML(s);
//            return fromXML;
//        }
//    }

}
