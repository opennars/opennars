/*
 *   IPAddress.java
 *
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 *
 * This software is the proprietary information of deis.unibo.it
 * Use is subject to license terms.
 *
 */
package alice.util;

/**
 * this class defines services useful for
 * managing of IP extended address:
 *
 * <IPAddress>:<port>
 *
 */
public class IPAddress extends Object {

    static public int getPort(String address){
        int index=address.indexOf(':');
        if (index!=-1){
            try {
                return Integer.parseInt(address.substring(index+1,address.length()));
            } catch (Exception ex){
            }
        }
        return -1;
    }

    static public String getHost(String address){
        int index=address.indexOf(':');
        if (index!=-1){
            return address.substring(0,index);
        }
        return address;
    }

}
