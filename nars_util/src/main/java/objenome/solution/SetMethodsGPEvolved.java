/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solution;

import com.google.common.collect.Sets;
import objenome.Phenotainer;
import objenome.goal.DevelopMethod;
import objenome.solver.Solution;

import java.util.Set;

/**
 * Uses a dynamically generated expression to complete an abstract or interface method
 * TODO
 */
public class SetMethodsGPEvolved implements Solution {
    
    public static String DYNAMIC_SUFFIX = "$$D";
    private final Set<DevelopMethod> methods;
    
    

    
    public SetMethodsGPEvolved(DevelopMethod... m) {
        methods = Sets.newHashSet(m);
    }

    @Override
    public void apply(Phenotainer p) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    
    

    @Override
    public String key() {
        return "implement(" + methods + ')';
    }

    

    @Override
    public String toString() {
        return key();
    }

    public void addMethodToDevelop(DevelopMethod m) {
        methods.add(m);
    }
    
    
}
