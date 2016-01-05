package nars.util.utf8;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

/** from: https://github.com/nitsanw/javanetperf/ */
final class StringDecoder {

	static final int INITIAL_BUFFER_SIZE = 1024;
	private static final int SIZE_ALIGNMENT_BITS = 10; // = 1024
	private static final int SIZE_ALIGNMENT = 1 << SIZE_ALIGNMENT_BITS;
	private static final int SIZE_ALIGNMENT_MASK = (1 << SIZE_ALIGNMENT_BITS) - 1;

	private CharBuffer outBuffer = CharBuffer.allocate(INITIAL_BUFFER_SIZE);

	private final CharsetDecoder decoder = Utf8.utf8Charset.newDecoder();

	public StringDecoder() {
		// This matches the default behaviour for String(byte[], "UTF-8");
		// TODO: Support throwing exceptions on invalid UTF-8?
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
	}

	/**
	 * Reserve space for the next string that will be <= expectedLength
	 * characters long. Must only be called when the buffer is empty.
	 */
	public void reserve(int expectedLength) {
		if (expectedLength < 0) {
			throw new IllegalArgumentException(
					"expectedLength cannot be negative (= " + expectedLength
							+ ')');
		}
		if (outBuffer.position() != 0) {
			throw new IllegalStateException(
					"cannot be called except after finish()");
		}

		if (expectedLength > outBuffer.capacity()) {
			// Allocate a temporary buffer large enough for this string rounded
			// up
			// TODO: Does this size alignment help at all?
			int desiredLength = expectedLength;
			if ((desiredLength & SIZE_ALIGNMENT_MASK) != 0) {
				// round up
				desiredLength = (expectedLength + SIZE_ALIGNMENT)
						& ~SIZE_ALIGNMENT_MASK;
			}
			// assert desiredLength % SIZE_ALIGNMENT == 0;

			outBuffer = CharBuffer.allocate(desiredLength);
		}
		// assert outBuffer.position() == 0;
		// assert expectedLength <= outBuffer.capacity();
	}

	public void decode(byte[] source, int offset, int length) {
		decode(source, offset, length, false);
	}

	private void decode(byte[] source, int offset, int length,
			boolean endOfInput) {
		// TODO: we could cache the input ByteBuffer if source doesn't change
		ByteBuffer input = ByteBuffer.wrap(source, offset, length);

		// Call decode at least once to pass the endOfInput signal through
		do {
			CoderResult result = decoder.decode(input, outBuffer, endOfInput);
			if (result != CoderResult.UNDERFLOW) {
				// Error handling
				if (result == CoderResult.OVERFLOW) {
					// double the buffer size and retry
					CharBuffer next = CharBuffer
							.allocate(outBuffer.capacity() * 2);
					System.arraycopy(outBuffer.array(), 0, next.array(), 0,
							outBuffer.position());
					next.position(outBuffer.position());
					// assert next.remaining() >= outBuffer.capacity();
					outBuffer = next;
				} else {
					// We disable errors in the constructor (replace instead)
					// assert false;
					throw new RuntimeException("error");
					// TODO: Are there any unmappable sequences for UTF-8?
					// assert result.isMalformed();
				}
			}
		} while (input.hasRemaining());
		// assert !input.hasRemaining();
	}

	public char[] newChars(byte[] source, int offset, int length) {
		decodeIt(source, offset, length);

		CharBuffer b = outBuffer;

		// Copy out the string
		return Arrays.copyOf(b.array(), b.position());
	}
	public void appendChars(byte[] source, int offset, int length,
			Appendable target) throws IOException {
		if (target instanceof StringBuilder) {
			// use the stringbuilder version which is optimized for char[] batch
			// append which isnt part of Appendable
			appendChars(source, offset, length, ((StringBuilder) target));
			return;
		}

		decodeIt(source, offset, length);

		CharBuffer b = outBuffer;
		int len = b.position();
		char[] a = b.array();

		for (int i = 0; i < len; i++) {
			target.append(a[i]);
		}

	}
	public void appendChars(byte[] source, int offset, int length,
			StringBuilder target) {
		decodeIt(source, offset, length);

		CharBuffer b = outBuffer;
		int len = b.position();
		char[] a = b.array();
		target.append(a, 0, len);
	}

	public String newString(byte[] source, int offset, int length) {
		decodeIt(source, offset, length);

		CharBuffer b = outBuffer;

		// Copy out the string
		return new String(b.array(), 0, b.position());
	}
	public String newString(char prefix, byte[] source, int offset, int length) {
		decodeIt(source, offset, length);

		CharBuffer b = outBuffer;

		// TODO avoid instantiating new array by preallocating target buffer
		// with prefix at index 0 to set
		char[] suffix = ArrayUtils.subarray(b.array(), 0, b.position());
		char[] n = new char[suffix.length + 1];
		n[0] = prefix;
		System.arraycopy(suffix, 0, n, 1, suffix.length);

		// Copy out the string
		return new String(n);
	}

	public void decodeIt(byte[] source, int offset, int length) {
		// Reset for the next string
		outBuffer.clear();
		decoder.reset();

		decode(source, offset, length, true);

		CoderResult result = decoder.flush(outBuffer);
		if (result == CoderResult.OVERFLOW) {
			throw new RuntimeException("TODO: Handle overflow?");
		} else if (result != CoderResult.UNDERFLOW) {
			throw new RuntimeException("TODO: Handle errors?");
		}
		// assert result == CoderResult.UNDERFLOW;
	}
}