package nars.util.data.id;

/**
 * Wrapper class applying deterministic lossless compression
 * to form the internal representation of an uncompressed UTF8 byte[] sequence
 * (which could be from another UTF8Identifier).
 *
 * It does not store a reference to the original data since it can
 * be regenerated via decommpression on-demand.
 *
 * This mangles lexicographic ordinality but it shouldn't matter. :D
 *
 * Default method: Run-length-encoding (RLE)
 */
abstract public class CompressedUTF8Identifier extends UTF8Identifier {

    public CompressedUTF8Identifier(byte[] source) {
        super();

    }

    public CompressedUTF8Identifier(UTF8Identifier uncompressed) {
        this(uncompressed.bytes());
    }
}
