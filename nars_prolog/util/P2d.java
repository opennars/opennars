/*
 *   P2d.java
 *
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 *
 * This software is the proprietary information of deis.unibo.it
 * Use is subject to license terms.
 *
 */
package alice.util;

/**
 *
 * 2-dimensional point
 * objects are completely state-less
 *
 */
@SuppressWarnings("serial")
public class P2d implements java.io.Serializable {

    public float x;
	public float y;

    public P2d(float x,float y){
        this.x=x;
        this.y=y;
    }

    public P2d sum(V2d v){
        return new P2d(x+v.x,y+v.y);
    }

    public V2d sub(P2d v){
        return new V2d(x-v.x,y-v.y);
    }

    public String toString(){
        return "P2d("+x+","+y+")";
    }

}
