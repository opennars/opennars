// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.rfb.protocol.auth;

import automenta.vnc.exceptions.CryptoException;
import automenta.vnc.exceptions.FatalException;
import automenta.vnc.exceptions.TransportException;
import automenta.vnc.rfb.CapabilityContainer;
import automenta.vnc.rfb.IPasswordRetriever;
import automenta.vnc.transport.Reader;
import automenta.vnc.transport.Writer;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class VncAuthentication extends AuthHandler {
	@Override
	public SecurityType getType() {
		return SecurityType.VNC_AUTHENTICATION;
	}

	@Override
	public boolean authenticate(Reader reader,
			Writer writer, CapabilityContainer authCaps, IPasswordRetriever passwordRetriever)
	throws TransportException, FatalException {
		byte [] challenge = reader.readBytes(16);
		String password = passwordRetriever.getPassword();
        if (null == password) return false;
		byte [] key = new byte[8];
        System.arraycopy(password.getBytes(), 0, key, 0, Math.min(key.length, password.getBytes().length));
	    writer.write(encrypt(challenge, key));
	    return false;
	}

  /**
	 * Encript challenge by key using DES
	 * @return encripted bytes
	 * @throws CryptoException on problem with DES algorithm support or smth about
	 */
	public byte[] encrypt(byte[] challenge, byte... key) throws CryptoException {
		try {
		    DESKeySpec desKeySpec = new DESKeySpec(mirrorBits(key));
		    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		    SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		    Cipher desCipher = Cipher.getInstance("DES/ECB/NoPadding");
		    desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return desCipher.doFinal(challenge);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
			throw new CryptoException("Cannot encrypt challenge", e);
		}
    }

	private static byte[] mirrorBits(byte... k) {
		byte[] key = new byte[8];
		for (int i = 0; i < 8; i++) {
			byte s = k[i];
			s = (byte) (((s >> 1) & 0x55) | ((s << 1) & 0xaa));
	        s = (byte) (((s >> 2) & 0x33) | ((s << 2) & 0xcc));
	        s = (byte) (((s >> 4) & 0x0f) | ((s << 4) & 0xf0));
	        key[i] = s;
		}
		return key;
	}

}
