package automenta.rdp.crypto;

/**
 * This class is for any unexpected exception in the crypto library.
 * <p>
 * <b>Copyright</b> &copy; 1997 <a href="http://www.systemics.com/">Systemics
 * Ltd</a> on behalf of the <a
 * href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>
 * All rights reserved.
 * <p>
 * <b>$Revision: #2 $</b>
 * 
 * @author David Hopwood
 * @since Cryptix 2.2.2
 */
public class CryptoException extends Exception {
	private static final long serialVersionUID = -5302220269148556116L;

	public CryptoException() {
		super();
	}

	/**
	 * @param reason
	 *            the reason why the exception was thrown.
	 */
	public CryptoException(String reason) {
		super(reason);
	}
}
