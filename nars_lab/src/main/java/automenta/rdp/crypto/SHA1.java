package automenta.rdp.crypto;

/**
 * This class implements the SHA-1 message digest algorithm.
 * <p>
 * <b>BUG</b>: The update method is missing.
 * <p>
 * <b>References:</b>
 * <ol>
 * <li> Bruce Schneier, "Section 18.7 Secure Hash Algorithm (SHA),"
 * <cite>Applied Cryptography, 2nd edition</cite>, John Wiley &amp; Sons, 1996
 * <p>
 * <li> NIST FIPS PUB 180-1, "Secure Hash Standard", U.S. Department of
 * Commerce, May 1993.<br>
 * <a href="http://www.itl.nist.gov/div897/pubs/fip180-1.htm">
 * http://www.itl.nist.gov/div897/pubs/fip180-1.htm</a>
 * </ol>
 * <p>
 * <b>Copyright</b> &copy; 1995-1997 <a
 * href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the <a
 * href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>
 * All rights reserved.
 * <p>
 * <b>$Revision: #2 $</b>
 * 
 * @author Systemics Ltd
 * @author David Hopwood
 * @since Cryptix 2.2.2
 */
public final class SHA1 extends BlockMessageDigest implements Cloneable {

	// SHA-1 constants and variables
	// ...........................................................................

	/**
	 * Length of the final hash (in bytes).
	 */
	private static final int HASH_LENGTH = 20;

	/**
	 * Length of a block (i.e. the number of bytes hashed in every transform).
	 */
	private static final int DATA_LENGTH = 64;

	private int[] data;

	private int[] digest;

	private byte[] tmp;

	private int[] w;

	/**
	 * Returns the length of the hash (in bytes).
	 */
	protected static int engineGetDigestLength() {
		return HASH_LENGTH;
	}

	/**
	 * Returns the length of the data (in bytes) hashed in every transform.
	 */
	protected int engineGetDataLength() {
		return DATA_LENGTH;
	}

	/**
	 * Constructs a SHA-1 message digest.
	 */
	public SHA1() {
		super("SHA-1");
		java_init();
		engineReset();
	}

	private void java_init() {
		digest = new int[HASH_LENGTH / 4];
		data = new int[DATA_LENGTH / 4];
		tmp = new byte[DATA_LENGTH];
		w = new int[80];
	}

	/**
	 * This constructor is here to implement cloneability of this class.
	 */
	private SHA1(SHA1 md) {
		this();
		data = (int[]) md.data.clone();
		digest = (int[]) md.digest.clone();
		tmp = (byte[]) md.tmp.clone();
		w = (int[]) md.w.clone();
	}

	/**
	 * Returns a copy of this MD object.
	 */
	public Object clone() {
		return new SHA1(this);
	}

	/**
	 * Initializes (resets) the message digest.
	 */
	public void engineReset() {
		super.engineReset();
		java_reset();
	}

	private void java_reset() {
		digest[0] = 0x67452301;
		digest[1] = 0xefcdab89;
		digest[2] = 0x98badcfe;
		digest[3] = 0x10325476;
		digest[4] = 0xc3d2e1f0;
	}

	/**
	 * Adds data to the message digest.
	 * 
	 * @param data
	 *            The data to be added.
	 * @param offset
	 *            The start of the data in the array.
	 * @param length
	 *            The amount of data to add.
	 */
	protected void engineTransform(byte[] in) {
		java_transform(in);
	}

	private void java_transform(byte[] in) {
		byte2int(in, 0, data, 0, DATA_LENGTH / 4);
		transform(data);
	}

	/**
	 * Returns the digest of the data added and resets the digest.
	 * 
	 * @return the digest of all the data added to the message digest as a byte
	 *         array.
	 */
	public byte[] engineDigest(byte[] in, int length) {
		byte b[] = java_digest(in, length);
		engineReset();
		return b;
	}

	private byte[] java_digest(byte[] in, int pos) {
		if (pos != 0)
			System.arraycopy(in, 0, tmp, 0, pos);

		tmp[pos++] = (byte) 0x80;

		if (pos > DATA_LENGTH - 8) {
			while (pos < DATA_LENGTH)
				tmp[pos++] = 0;

			byte2int(tmp, 0, data, 0, DATA_LENGTH / 4);
			transform(data);
			pos = 0;
		}

		while (pos < DATA_LENGTH - 8)
			tmp[pos++] = 0;

		byte2int(tmp, 0, data, 0, (DATA_LENGTH / 4) - 2);

		// Big endian
		// WARNING: int>>>32 != 0 !!!
		// bitcount() used to return a long, now it's an int.
		int bc = bitcount();
		data[14] = 0;
		data[15] = bc;

		transform(data);

		byte buf[] = new byte[HASH_LENGTH];

		// Big endian
		int off = 0;
		for (int i = 0; i < HASH_LENGTH / 4; ++i) {
			int d = digest[i];
			buf[off++] = (byte) (d >>> 24);
			buf[off++] = (byte) (d >>> 16);
			buf[off++] = (byte) (d >>> 8);
			buf[off++] = (byte) d;
		}
		return buf;
	}

	// SHA-1 transform routines
	// ...........................................................................

	private static int f1(final int a, final int b, final int c) {
		return (c ^ (a & (b ^ c))) + 0x5A827999;
	}

	private static int f2(int a, int b, int c) {
		return (a ^ b ^ c) + 0x6ED9EBA1;
	}

	private static int f3(int a, int b, int c) {
		return ((a & b) | (c & (a | b))) + 0x8F1BBCDC;
	}

	private static int f4(int a, int b, int c) {
		return (a ^ b ^ c) + 0xCA62C1D6;
	}

	private void transform(final int[] X) {
		int A = digest[0];
		int B = digest[1];
		int C = digest[2];
		int D = digest[3];
		int E = digest[4];

		int W[] = w;
		System.arraycopy(X, 0, W, 0, 16);
		for (int i = 16; i < 80; i++) {
			int j = W[i - 16] ^ W[i - 14] ^ W[i - 8] ^ W[i - 3];
			W[i] = j;
			W[i] = (j << 1) | (j >>> -1);
		}

		E += ((A << 5) | (A >>> -5)) + f1(B, C, D) + W[0];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f1(A, B, C) + W[1];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f1(E, A, B) + W[2];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f1(D, E, A) + W[3];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f1(C, D, E) + W[4];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f1(B, C, D) + W[5];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f1(A, B, C) + W[6];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f1(E, A, B) + W[7];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f1(D, E, A) + W[8];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f1(C, D, E) + W[9];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f1(B, C, D) + W[10];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f1(A, B, C) + W[11];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f1(E, A, B) + W[12];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f1(D, E, A) + W[13];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f1(C, D, E) + W[14];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f1(B, C, D) + W[15];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f1(A, B, C) + W[16];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f1(E, A, B) + W[17];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f1(D, E, A) + W[18];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f1(C, D, E) + W[19];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f2(B, C, D) + W[20];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f2(A, B, C) + W[21];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f2(E, A, B) + W[22];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f2(D, E, A) + W[23];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f2(C, D, E) + W[24];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f2(B, C, D) + W[25];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f2(A, B, C) + W[26];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f2(E, A, B) + W[27];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f2(D, E, A) + W[28];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f2(C, D, E) + W[29];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f2(B, C, D) + W[30];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f2(A, B, C) + W[31];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f2(E, A, B) + W[32];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f2(D, E, A) + W[33];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f2(C, D, E) + W[34];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f2(B, C, D) + W[35];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f2(A, B, C) + W[36];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f2(E, A, B) + W[37];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f2(D, E, A) + W[38];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f2(C, D, E) + W[39];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f3(B, C, D) + W[40];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f3(A, B, C) + W[41];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f3(E, A, B) + W[42];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f3(D, E, A) + W[43];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f3(C, D, E) + W[44];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f3(B, C, D) + W[45];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f3(A, B, C) + W[46];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f3(E, A, B) + W[47];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f3(D, E, A) + W[48];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f3(C, D, E) + W[49];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f3(B, C, D) + W[50];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f3(A, B, C) + W[51];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f3(E, A, B) + W[52];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f3(D, E, A) + W[53];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f3(C, D, E) + W[54];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f3(B, C, D) + W[55];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f3(A, B, C) + W[56];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f3(E, A, B) + W[57];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f3(D, E, A) + W[58];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f3(C, D, E) + W[59];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f4(B, C, D) + W[60];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f4(A, B, C) + W[61];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f4(E, A, B) + W[62];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f4(D, E, A) + W[63];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f4(C, D, E) + W[64];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f4(B, C, D) + W[65];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f4(A, B, C) + W[66];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f4(E, A, B) + W[67];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f4(D, E, A) + W[68];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f4(C, D, E) + W[69];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f4(B, C, D) + W[70];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f4(A, B, C) + W[71];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f4(E, A, B) + W[72];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f4(D, E, A) + W[73];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f4(C, D, E) + W[74];
		C = ((C << 30) | (C >>> -30));
		E += ((A << 5) | (A >>> -5)) + f4(B, C, D) + W[75];
		B = ((B << 30) | (B >>> -30));
		D += ((E << 5) | (E >>> -5)) + f4(A, B, C) + W[76];
		A = ((A << 30) | (A >>> -30));
		C += ((D << 5) | (D >>> -5)) + f4(E, A, B) + W[77];
		E = ((E << 30) | (E >>> -30));
		B += ((C << 5) | (C >>> -5)) + f4(D, E, A) + W[78];
		D = ((D << 30) | (D >>> -30));
		A += ((B << 5) | (B >>> -5)) + f4(C, D, E) + W[79];
		C = ((C << 30) | (C >>> -30));

		digest[0] += A;
		digest[1] += B;
		digest[2] += C;
		digest[3] += D;
		digest[4] += E;
	}

	// why was this public?
	// Note: parameter order changed to be consistent with System.arraycopy.
	private static void byte2int(byte[] src, int srcOffset, int[] dst,
			int dstOffset, int length) {
		while (length-- > 0) {
			// Big endian
			dst[dstOffset++] = (src[srcOffset++] << 24)
					| ((src[srcOffset++] & 0xFF) << 16)
					| ((src[srcOffset++] & 0xFF) << 8)
					| (src[srcOffset++] & 0xFF);
		}
	}
}
