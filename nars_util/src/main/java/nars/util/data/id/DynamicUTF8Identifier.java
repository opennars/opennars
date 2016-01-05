//package nars.util.data.id;
//
///** Lazily calculated dynamic UTF8 for constructing compact INTERNAL REPRESENTATIONS */
//abstract public class DynamicUTF8Identifier extends LiteralUTF8Identifier {
//
//    public DynamicUTF8Identifier() {
//        super();
//    }
//
//    @Override
//    public char[] chars(final boolean pretty) {
//        return charsFromWriter(pretty);
//    }
//
//
//    protected void ensureNamed() {
//        if (!hasName()) {
//            setData(init());
//        }
//    }
//
////    public boolean hasHash() {
////        /** assumes the hash is generated when name is  */
////        if (!hasName())
////            return false;
////        return true;
////    }
//
//
//    @Override
//    public int hashCode() {
//        ensureNamed();
//        return hash;
//    }
//
//    /** should return byte[] name, override in subclasses if no constant name is provided at construction
//     * implementations should call ByteBuffer.add() sometimes instead of ByteBuf.append()
//     * when the exact total size of the resulting byte[] is known
//     */
//    abstract public byte[] init();
//
// }
