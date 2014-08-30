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
 * managing of IP identifiers
 *
 * <Name>@<IPAddress>:<port>
 *
 */
public class IPIdentifier {

    static public int getPort(String name){
        int index=name.indexOf(':');
        if (index!=-1){
            try {
                return Integer.parseInt(name.substring(index+1,name.length()));
            } catch (Exception ex){
            }
        }
        return -1;
    }

    static public String getHost(String name){
        int index_port=name.indexOf(':');
        int index_at=name.indexOf('@');
        if (index_port!=-1){
            return name.substring(index_at+1,index_port);
        } else if (index_at!=-1) {
            return name.substring(index_at+1,name.length());
        } else {
            // its a name
            return "";
        }
    }

    static public String getName(String name){
        int index_port=name.indexOf(':');
        int index_at=name.indexOf('@');
        if (index_port!=-1){
            if (index_at!=-1){
                return name.substring(0,index_at);
            } else {
                return "";
            }
        } else {
            if (index_at!=-1){
                return name.substring(0,index_at);
            } else {
                return name;
            }
        }
    }

    static public String getAddress(String name){
        int index_at=name.indexOf('@');
        if (index_at==-1){
            return "";
        } else {
            return name.substring(index_at+1,name.length());
        }
    }

    /*
    public static void main(String[] args){
        System.out.println("name: "+IPIdentifier.getName(args[0]));
        System.out.println("host: "+IPIdentifier.getHost(args[0]));
        System.out.println("port: "+IPIdentifier.getPort(args[0]));
    }
     */

}
