package nars.util.data.id;

/** Lazily calculated dynamic UTF8 */
abstract public class DynamicUTF8Identifier extends UTF8Identifier {

    public DynamicUTF8Identifier() {
        super();
    }

    @Override protected synchronized void ensureNamed() {
        if (!hasName()) {
            name = newName();
            hash = makeHash();
        }
    }

    public boolean hasHash() {
        /** assumes the hash is generated when name is  */
        if (!hasName())
            return false;
        return true;
    }


    @Override
    public int hashCode() {
        ensureNamed();
        return hash;
    }

    /** should return byte[] name, override in subclasses if no constant name is provided at construction  */
    abstract public byte[] newName();

}
