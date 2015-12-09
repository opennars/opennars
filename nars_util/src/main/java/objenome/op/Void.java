/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op;

/**
 * Void type
 */
public class Void extends Node {
    
    protected static Void the = new Void();
    
    private Void() {
    }
    
    public static Void get() {
        return the;
    }

    @Override
    public Object evaluate() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return "void";
    }
}
