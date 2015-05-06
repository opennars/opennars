/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solver;


import objenome.Phenotainer;

/**
 * Gene of an Objenome; a particular solution to an objenomic problem
 */
public interface Solution {

    /**
     * the DI target instance key that this affects.
     * will consist of alternating items between:
     *  --Class (input target class)
     *  --ClassBuilder (solved dependent Class)
     *  --DependencyKey (which will contain a String key and possible Parameter reference)
     * 
     */


    /** apply the consequences of this gene to an Phenotainer */
    public void apply(Phenotainer c);


//    @Override
//    public default Objene apply(Problem p) { return this; }


    public String key();

    //TODO move this to separate interface: Mutable
    default public void mutate() {
        throw new RuntimeException(this + " does not support mutate() not supported");
    }
    
    
}
