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

package automenta.vnc.viewer.swing;

import automenta.vnc.utils.Strings;
import automenta.vnc.viewer.mvp.Model;

/**
* @author dime at tightvnc.com
*/
public class ConnectionParams implements Model {
	public static final int DEFAULT_SSH_PORT = 22;
	private static final int DEFAULT_RFB_PORT = 5901;

	public String hostName;
	private int portNumber;
	public String sshUserName;
	public String sshHostName;
	private int sshPortNumber;
    public String sshPrivateKey;
    public String sshPublicKey;
    public String sshHostKey;
    public String sshKeyReplaceSymbol;

	private boolean useSsh;

	public ConnectionParams(String hostName,
                            int portNumber,
                            boolean useSsh,
                            String sshHostName,
                            int sshPortNumber,
                            String sshUserName,
                            String sshPrivateKey,
                            String sshPublicKey,
                            String sshHostKey,
                            String sshKeyReplaceSymbol) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.sshUserName = sshUserName;
		this.sshHostName = sshHostName;
		this.sshPortNumber = sshPortNumber;
		this.useSsh = useSsh;
        this.sshPrivateKey = sshPrivateKey;
        this.sshPublicKey = sshPublicKey;
        this.sshHostKey = sshHostKey;
        this.sshKeyReplaceSymbol = sshKeyReplaceSymbol;
	}

	public ConnectionParams(ConnectionParams cp) {
		this.hostName = cp.hostName != null? cp.hostName: "";
		this.portNumber = cp.portNumber;
		this.sshUserName = cp.sshUserName;
		this.sshHostName = cp.sshHostName;
		this.sshPortNumber = cp.sshPortNumber;
		this.useSsh = cp.useSsh;
        this.sshPrivateKey = cp.sshPrivateKey;
        this.sshPublicKey = cp.sshPublicKey;
        this.sshHostKey = cp.sshHostKey;
        this.sshKeyReplaceSymbol = cp.sshKeyReplaceSymbol;
	}

	public ConnectionParams() {
		hostName = "";
        sshUserName = "";
        sshHostName = "";
	}

	public boolean isHostNameEmpty() {
		return Strings.isTrimmedEmpty(hostName);
	}

	public void parseRfbPortNumber(String port) throws WrongParameterException {
		try {
			portNumber = Integer.parseInt(port);
		} catch (NumberFormatException e) {
            portNumber = 0;
            if ( ! Strings.isTrimmedEmpty(port)) {
                throw new WrongParameterException("Wrong port number: " + port + "\nMust be in 0..65535");
            }
        }
        if (portNumber > 65535 || portNumber < 0) throw new WrongParameterException("Port number is out of range: " + port + "\nMust be in 0..65535");
	}
	public void parseSshPortNumber(String port) {
		try {
			sshPortNumber = Integer.parseInt(port);
		} catch (NumberFormatException e) { /*nop*/ }
	}

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public String getHostName() {
        return this.hostName;
    }

    public void setPortNumber(String port) throws WrongParameterException {
        this.parseRfbPortNumber(port);
    }

    public void setPortNumber(int port) {
        this.portNumber = port;
    }

    public int getPortNumber() {
        return 0 == portNumber ? DEFAULT_RFB_PORT : portNumber;
    }

    public void setSshPortNumber(String port) {
        this.parseSshPortNumber(port);
    }

    public void setSshPortNumber(int port) {
        this.sshPortNumber = port;
    }

    public int getSshPortNumber() {
        return 0 == sshPortNumber ? DEFAULT_SSH_PORT: sshPortNumber;
    }

    public void setUseSsh(boolean useSsh) {
        this.useSsh = useSsh;
    }

    public boolean useSsh() {
        return useSsh && ! Strings.isTrimmedEmpty(sshHostName);
    }

    public boolean getUseSsh() {
        return this.useSsh();
    }

    public String getSshUserName() {
        return this.sshUserName;
    }

    public void setSshUserName(String sshUserName) {
        this.sshUserName = sshUserName;
    }

    public String getSshHostName() {
        return this.sshHostName;
    }

    public void setSshHostName(String sshHostName) {
        this.sshHostName = sshHostName;
    }

    public String getSshPrivateKey() {
        return sshPrivateKey;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public String getSshHostKey() {
        return sshHostKey;
    }

    public void setSshHostKey(String sshHostKey) {
        this.sshHostKey = sshHostKey;
    }

    public String getSshKeyReplaceSymbol() {
        return sshKeyReplaceSymbol;
    }

    public void setSshKeyReplaceSymbol(String sshKeyReplaceSymbol) {
        this.sshKeyReplaceSymbol = sshKeyReplaceSymbol;
    }

    public boolean useSshKeyPair(){
        return this.sshPrivateKey != null &&
                !Strings.isTrimmedEmpty(this.sshPrivateKey) &&
                this.sshPublicKey != null &&
                !Strings.isTrimmedEmpty(this.sshPublicKey);
    }

    public void completeEmptyFieldsFrom(ConnectionParams from) {
        if (null == from) return;
		if (Strings.isTrimmedEmpty(hostName) && ! Strings.isTrimmedEmpty(from.hostName)) {
			hostName = from.hostName;
		}
        if ( 0 == portNumber && from.portNumber != 0) {
			portNumber = from.portNumber;
		}
		if (Strings.isTrimmedEmpty(sshUserName) && ! Strings.isTrimmedEmpty(from.sshUserName)) {
			sshUserName = from.sshUserName;
		}
		if (Strings.isTrimmedEmpty(sshHostName) && ! Strings.isTrimmedEmpty(from.sshHostName)) {
			sshHostName = from.sshHostName;
		}
		if ( 0 == sshPortNumber && from.sshPortNumber != 0) {
			sshPortNumber = from.sshPortNumber;
		}
        if (Strings.isTrimmedEmpty(sshPrivateKey) && ! Strings.isTrimmedEmpty(from.sshPrivateKey)) {
            sshPrivateKey = from.sshPrivateKey;
        }
        if (Strings.isTrimmedEmpty(sshPublicKey) && ! Strings.isTrimmedEmpty(from.sshPublicKey)) {
            sshPublicKey = from.sshPublicKey;
        }
        if (Strings.isTrimmedEmpty(sshHostKey) && ! Strings.isTrimmedEmpty(from.sshHostKey)) {
            sshHostKey = from.sshHostKey;
        }
        if (Strings.isTrimmedEmpty(sshKeyReplaceSymbol) && ! Strings.isTrimmedEmpty(from.sshKeyReplaceSymbol)) {
            sshKeyReplaceSymbol = from.sshKeyReplaceSymbol;
        }
		useSsh |= from.useSsh;
	}

	@Override
	public String toString() {
		return hostName != null ? hostName : "";
//        return (hostName != null ? hostName : "") + ":" + portNumber + " " + useSsh + " " + sshUserName + "@" + sshHostName + ":" + sshPortNumber;
    }

    public String toPrint() {
        return "ConnectionParams{" +
                "hostName='" + hostName + '\'' +
                ", portNumber=" + portNumber +
                ", sshUserName='" + sshUserName + '\'' +
                ", sshHostName='" + sshHostName + '\'' +
                ", sshPortNumber=" + sshPortNumber +
                ", useSsh=" + useSsh +
                ", sshPrivateKey=" + sshPrivateKey +
                ", sshPublicKey=" + sshPublicKey +
                ", sshHostKey=" + sshHostKey +
                ", sshKeyReplaceSymbol=" + sshKeyReplaceSymbol +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || ! (obj instanceof ConnectionParams)) return false;
        if (this == obj) return true;
        ConnectionParams o = (ConnectionParams) obj;
        return isEqualsNullable(hostName, o.hostName) && isEqualsNullable(sshPrivateKey, o.sshPrivateKey)
                && getPortNumber() == o.getPortNumber() &&
                useSsh == o.useSsh && isEqualsNullable(sshHostName, o.sshHostName) &&
                getSshPortNumber() == o.getSshPortNumber() && isEqualsNullable(sshUserName, o.sshUserName);
    }

    private boolean isEqualsNullable(String one, String another) {
        //noinspection StringEquality
        return one == another || (null == one? "" : one).equals(null == another? "" : another);
    }

    @Override
    public int hashCode() {
        long hash = (hostName != null? hostName.hashCode() : 0) +
                portNumber * 17 +
                (useSsh ? 781 : 693) +
                (sshHostName != null? sshHostName.hashCode() : 0) * 23 +
                (sshUserName != null? sshUserName.hashCode() : 0) * 37 +
                sshPortNumber * 41 +
                (sshPrivateKey != null? sshPrivateKey.hashCode() : 0) * 45 +
                (sshPublicKey != null? sshPublicKey.hashCode() : 0) * 49+
                (sshHostKey != null? sshHostKey.hashCode() : 0) * 53 +
                (sshKeyReplaceSymbol != null? sshKeyReplaceSymbol.hashCode() : 0) * 57;

        return (int)hash;
    }

    public void clearFields() {
        hostName = "";
        portNumber = 0;
        useSsh = false;
        sshHostName = null;
        sshUserName = null;
        sshPortNumber = 0;
        sshPrivateKey = null;
        sshPublicKey = null;
        sshHostKey = null;
        sshKeyReplaceSymbol = null;
    }
}
