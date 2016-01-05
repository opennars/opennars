package nars.util.data.id;

/**
 * An identifier which consists of a reference to another UTF8Identifier and a
 * start, stop interval representing a subsequence, TODO
 */
public abstract class SlicedUTF8Identifier extends UTF8Identifier {

	final UTF8Identifier source;
	final short start;
	final short length;

	public SlicedUTF8Identifier(UTF8Identifier source, short start, short end) {
		this.source = source;
		this.start = start;
		length = (short) (end - start);
	}

	public int getEnd() {
		return start + length;
	}

}
