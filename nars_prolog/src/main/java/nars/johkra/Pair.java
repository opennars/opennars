/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.johkra;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author me
 */
public class Pair<A,B> implements Serializable {
    
    private A a;
    private B b;
    
    transient private int hash = 0;
    transient private String nameCache = null;
    
    public Pair(A a, B b) {
        setA(a);
        setB(b);
    }
    
    public A a() { return a; }
    public B b() { return b; }
    
    protected void setA(A a) { this.a = a; invalidate(); }
    protected void setB(B b) { this.b = b; invalidate(); }

    protected void invalidate() {
        hash = 0;
        nameCache = null;
    }
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(a, b);
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        Pair p = (Pair)o;
        
        if (hash!=p.hash) return false;
        return a.equals(p.a()) && b.equals(p.b());
    }

    @Override
    public String toString() {
        if (nameCache == null) {
            nameCache = '{' + a.toString() + ',' + b.toString() + '}';
        }
        return nameCache;
    }
    
    
    
    
    
    
}
