package inc.glamdring.vtables;

import java.nio.ByteBuffer;

/**
 * pointer class -- approximation of c++ '*'
 *
 * @author jim
 */
public class _ptr implements _edge<ByteBuffer, Integer> {
    private ByteBuffer l;


    public ByteBuffer demote(_edge<ByteBuffer, Integer> e) {
        if (this != e) bind(e.demote(e), e.promote(e));
        return l$();
    }

    public Integer promote(_edge<ByteBuffer, Integer> e) {
        if (this != e) 
            bind(e.demote(e), e.promote(e));
        return $r();
    }

    public _edge<ByteBuffer, Integer> bind(ByteBuffer byteBuffer, Integer r) {

        l = (ByteBuffer) byteBuffer.duplicate().position(r);
        return this;
    }

    public ByteBuffer reify(_ptr ptr1) {
        return ptr1.l$();

    }

    public ByteBuffer l$() {
        return l;
    }

    public Integer $r() {
        return demote(this).position();
    }
}