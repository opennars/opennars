package nars.term;


abstract public class ImmutableAtom extends AbstractAtomic {

    private final byte[] id;
    private final int hash;


    public ImmutableAtom(byte[] id, int hash) {
        this.id = id;
        this.hash = hash;
    }

    @Override
    public final byte[] bytes() {
        return id;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final void setBytes(byte[] b) {
        throw new RuntimeException("immutable");
    }
}
