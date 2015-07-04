package automenta.rdp.crypto;

/**
 * This is a superclass for message digests that operate internally on blocks of
 * data. It is not intended directly for use by application programmers.
 * <p>
 * <b>Copyright</b> &copy; 1995-1997 <a
 * href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the <a
 * href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>
 * All rights reserved.
 * <p>
 * <b>$Revision: #2 $</b>
 * 
 * @author David Hopwood
 * @since Cryptix 2.2.2
 */
public abstract class BlockMessageDigest {
	/**
	 * The buffer used to store the last incomplete block.
	 */
	private byte[] buffer;

	/**
	 * The number of bytes currently stored in <code>buffer</code>.
	 */
	private int buffered;

	private String algorithm;

	/**
	 * The number of bytes that have been input to the digest.
	 */
	private int count;

	private static final int MAX_COUNT = (1 << 28) - 1;

	/**
	 * The length of a data block for this algorithm.
	 */
	private int data_length;

	/**
	 * Constructs a message digest with the specified algorithm name.
	 * 
	 * @param algorithm
	 *            the standard name of the digest algorithm.
	 */
	protected BlockMessageDigest(String algorithm) {
		this.algorithm = algorithm;
		data_length = engineGetDataLength();
		buffer = new byte[data_length];
	}

	/**
	 * @return number of bits hashed so far?
	 */
	protected int bitcount() {
		return count * 8;
	}

	/**
	 * <b>SPI</b>: Resets the digest. Subclasses that override
	 * <code>engineReset</code> should always call this implementation using
	 * <code>super.engineReset()</code>.
	 */
	public void engineReset() {
		buffered = 0;
		count = 0;
	}

	/**
	 * <b>SPI</b>: Updates the message digest with a byte of new data.
	 * 
	 * @param b
	 *            the byte to be added.
	 */
	public void engineUpdate(byte b) throws CryptoException {
		byte[] data = { b };
		engineUpdate(data, 0, 1);
	}

	/**
	 * <b>SPI</b>: Updates the message digest with new data.
	 * 
	 * @param data
	 *            the data to be added.
	 * @param offset
	 *            the start of the data in the array.
	 * @param length
	 *            the number of bytes of data to add.
	 */
	public void engineUpdate(byte[] data, int offset, int length)
			throws CryptoException {
		count += length;
		if (count > MAX_COUNT)
			throw new CryptoException(getAlgorithm()
					+ ": Maximum input length exceeded");

		int datalen = data_length;
		int remainder;

		while (length >= (remainder = datalen - buffered)) {
			System.arraycopy(data, offset, buffer, buffered, remainder);
			engineTransform(buffer);
			length -= remainder;
			offset += remainder;
			buffered = 0;
		}

		if (length > 0) {
			System.arraycopy(data, offset, buffer, buffered, length);
			buffered += length;
		}
	}

	/**
	 * <b>SPI</b>: Calculates the final digest. BlockMessageDigest subclasses
	 * should not usually override this method.
	 * 
	 * @return the digest as a byte array.
	 */
	public byte[] engineDigest() {
		return engineDigest(buffer, buffered);
	}

	//
	// Override int engineDigest(byte[] buf, int offset, int len)
	// from Java 1.2 preview docs? For the time being no - it should work
	// anyway.
	//

	/**
	 * <b>SPI</b> (for BlockMessageDigests only): Calculates the final digest.
	 * <code>data[0..length-1]</code> contains the last incomplete input
	 * block. <i>length</i> will be less than <code>engineDataLength()</code>.
	 * 
	 * @param data
	 *            the last incomplete block.
	 * @param length
	 *            the length in bytes of the last block.
	 * @return the digest as a byte array.
	 */
	public abstract byte[] engineDigest(byte[] data, int length);

	/**
	 * <b>SPI</b> (for BlockMessageDigests only): Performs a transformation on
	 * the given data, which is always one block long.
	 */
	protected abstract void engineTransform(byte[] data);

	/**
	 * <b>SPI</b>: Returns the length of the block that this hash function
	 * operates on.
	 */
	protected abstract int engineGetDataLength();

	protected String getAlgorithm() {
		return this.algorithm;
	}
}
