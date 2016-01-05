package nars.util.utf8;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * from: https://github.com/nitsanw/javanetperf/ Optimizations I tried:
 * 
 * String.getBytes(): utf-8.txt: 0.436665856; single 1 byte string: 0.284596992
 * 
 * encode to a 1 byte per char array first: (0.635079168 s; 0.166008832) encode
 * to temporary array first: (0.423180032; 0.168591872)
 * 
 * Conclusion: while guessing "ASCII text" can be slightly faster for toArray(),
 * the extra copy for the "temp array" approach is pretty cheap, and has the
 * advantage that it works for other scripts as well.
 */

final class StringEncoder {
	static final int CHAR_BUFFER_SIZE = 1024;
	static final int BYTE_BUFFER_SIZE = CHAR_BUFFER_SIZE * 2;
	// Extra "slop" when allocating a new byte buffer: permits the string to
	// contain some extra long UTF-8 characters without needing a new buffer.
	private static final int BUFFER_EXTRA_BYTES = 64;

	// The JDK's java.nio.charset.Charset.maxBytesPerChar() returns 4.0 for
	// UTF-8. This is wrong: the max is 4 bytes per CODEPOINT, but to represent
	// a 4 byte UTF-8 code point, you need 2 Java chars (UTF-16). Thus, the max
	// is 3 bytes per char, and there is a unit test to verify this.
	static final int UTF8_MAX_BYTES_PER_CHAR = 3;

	private final CharBuffer inBuffer = CharBuffer.allocate(CHAR_BUFFER_SIZE);
	private final ByteBuffer byteTemp = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
	private final CharsetEncoder encoder = Utf8.utf8Charset.newEncoder();

	private int readOffset = 0;

	public StringEncoder() {
		// set the buffer to "filled" so it gets filled by encode()
		inBuffer.position(inBuffer.limit());
		// Needed for U+D800 - U+DBFF = High Surrogate; U+DC00 - U+DFFF = Low
		// Surrogates
		// Maybe others in the future? This is what the JDK does for
		// String.getBytes().
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		// Not actually needed for UTF-8, but can't hurt
		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	private void readInputChunk(String source) {
		// assert inBuffer.remaining() <= 1;
		// assert readOffset < source.length();

		CharBuffer inBuffer = this.inBuffer;
		char[] inChars = inBuffer.array();

		int readOffset = this.readOffset;
		// We need to get a chunk from the string: Compute the chunk length
		int readLength = source.length() - readOffset;
		int inCharsLen = inChars.length;
		if (readLength > inCharsLen) {
			readLength = inCharsLen;
		}

		// Copy the chunk from the string into our temporary buffer
		source.getChars(readOffset, readOffset + readLength, inChars, 0);
		inBuffer.position(0);
		inBuffer.limit(readLength);
		this.readOffset += readLength;
	}

	/**
	 * Encodes string into destination. This must be called multiple times with
	 * the same string until it returns true. When this returns false, it must
	 * be called again with larger destination buffer space. It is possible that
	 * there are a few bytes of space remaining in the destination buffer, even
	 * though it must be refreshed. For example, if a UTF-8 3 byte sequence
	 * needs to be written, but there is only 1 or 2 bytes of space, this will
	 * leave the last couple bytes unused.
	 * 
	 * @param destination
	 *            a ByteBuffer that will be filled with data.
	 * @return false if more output buffer space is needed, true if encoding is
	 *         complete.
	 */
	public boolean encode(String source, ByteBuffer destination) {
		// We need to special case the empty string
		if (source.isEmpty()) {
			return true;
		}

		// read data in, if needed
		if (!inBuffer.hasRemaining() && readOffset < source.length()) {
			readInputChunk(source);
		}

		int slen = source.length();

		CharBuffer inBuffer = this.inBuffer;
		CharsetEncoder encoder = this.encoder;
		// if flush() overflows the destination, skip the encode loop and re-try
		// the flush()
		if (inBuffer.hasRemaining()) {
			while (true) {
				// assert inBuffer.hasRemaining();
				boolean endOfInput = readOffset == source.length();
				CoderResult result = encoder.encode(inBuffer, destination,
						endOfInput);
				if (result == CoderResult.OVERFLOW) {
					// NOTE: destination could space remaining, in case of a
					// multi-byte sequence
					// assert destination.remaining() <
					// encoder.maxBytesPerChar();
					return false;
				}
				// assert result == CoderResult.UNDERFLOW;

				// If we split a surrogate char (inBuffer.remaining() == 1),
				// back up and re-copy
				// from the source. avoid a branch by always subtracting
				// assert inBuffer.remaining() <= 1;
				readOffset -= inBuffer.remaining();
				// assert readOffset > 0;

				// If we are done, break. Otherwise, read the next chunk
				if (readOffset == slen)
					break;
				readInputChunk(source);
			}
		}
		// assert !inBuffer.hasRemaining();
		// assert readOffset == source.length();

		CoderResult result = encoder.flush(destination);
		if (result == CoderResult.OVERFLOW) {
			// I don't think this can happen. If it does, assert so we can
			// figure it out
			// assert false;
			throw new RuntimeException("error");
			// We attempt to handle it anyway
			// return false;
		}
		// assert result == CoderResult.UNDERFLOW;

		// done!
		return true;
	}

	private int getCharsConverted() {
		int charsConverted = readOffset - inBuffer.remaining();
		// assert 0 <= charsConverted && charsConverted <= readOffset;
		return charsConverted;
	}

	/**
	 * Returns a ByteBuffer containing the UTF-8 version of source. The position
	 * of the ByteBuffer will be 0, the limit is the length of the string. The
	 * capacity of the ByteBuffer may be larger than the string.
	 */
	public ByteBuffer toNewByteBuffer(String source) {
		// Optimized for 1 byte per character strings (ASCII)
		ByteBuffer buffer = ByteBuffer.allocate(source.length()
				+ BUFFER_EXTRA_BYTES);

		boolean done;
		do {
			done = encode(source, buffer);
			if (!done) {
				// need a larger buffer
				// estimate the average bytes per character from the current
				// sample
				int charsConverted = getCharsConverted();
				double bytesPerChar;
				bytesPerChar = charsConverted > 0 ? buffer.position()
						/ (double) charsConverted : encoder
						.averageBytesPerChar();

				int charsRemaining = source.length() - charsConverted;
				// assert charsRemaining > 0;
				int bytesRemaining = (int) (charsRemaining * bytesPerChar + 0.5);
				ByteBuffer next = ByteBuffer.allocate(buffer.position()
						+ bytesRemaining + BUFFER_EXTRA_BYTES);

				// Copy the current chunk
				// TODO: Use a list of ByteBuffers to avoid copies?
				System.arraycopy(buffer.array(), 0, next.array(), 0,
						buffer.position());
				next.position(buffer.position());
				buffer = next;
			}
		} while (!done);

		// Set the buffer for reading and finish
		buffer.flip();
		return buffer;
	}

	/**
	 * Returns a new byte array containing the UTF-8 version of source. The
	 * array will be exactly the correct size for the string.
	 */
	public byte[] toNewArray(String source) {
		reset();

		ByteBuffer byteTemp = this.byteTemp;

		// Optimized for short strings
		// assert byteTemp.remaining() == byteTemp.capacity();
		boolean done = encode(source, byteTemp);
		if (done) {
			// copy the exact correct bytes out
			byte[] out = new byte[byteTemp.position()];
			System.arraycopy(byteTemp.array(), 0, out, 0, byteTemp.position());
			byteTemp.clear();
			// ~ good += 1;
			return out;
		}

		// Worst case: assume max bytes per remaining character.
		int charsRemaining = source.length() - getCharsConverted();
		ByteBuffer remaining = ByteBuffer.allocate(charsRemaining
				* UTF8_MAX_BYTES_PER_CHAR);
		// noinspection UnusedAssignment
		encode(source, remaining);
		// assert done;

		// Combine everything and return it
		byte[] out = new byte[byteTemp.position() + remaining.position()];
		System.arraycopy(byteTemp.array(), 0, out, 0, byteTemp.position());
		System.arraycopy(remaining.array(), 0, out, byteTemp.position(),
				remaining.position());
		byteTemp.clear();
		// ~ worst += 1;
		return out;
	}

	public void reset() {
		readOffset = 0;
		// reset inBuffer in case we are in the middle of an operation
		inBuffer.position(0);
		inBuffer.limit(0);
		encoder.reset();
	}
}