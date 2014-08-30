/*
 *   V2d.java
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
 * 2-dimensional vector
 * objects are completely state-less
 *
 */
@SuppressWarnings("serial")
public class V2d implements java.io.Serializable {

    public float x;
	public float y;

    public V2d(float x,float y){
        this.x=x;
        this.y=y;
    }

    public V2d sum(V2d v){
        return new V2d(x+v.x,y+v.y);
    }

    public float abs(){
        return (float)Math.sqrt(x*x+y*y);
    }

    public V2d getNormalized(){
        float module=(float)Math.sqrt(x*x+y*y);
        return new V2d(x/module,y/module);
    }

    public V2d mul(float fact){
        return new V2d(x*fact,y*fact);
    }

    public String toString(){
        return "V2d("+x+","+y+")";
    }
}
