/* java.security.Key interface
 * This file is in the public domain.
 */
package automenta.rdp.crypto;

import java.io.Serializable;

public interface Key extends Serializable {
	static long serialVersionUID = 6603384152749567654l;

	String getAlgorithm();

	byte[] getEncoded();

	String getFormat();
}
