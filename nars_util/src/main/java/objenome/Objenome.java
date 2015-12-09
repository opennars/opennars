/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import objenome.solver.IncompleteSolutionException;
import objenome.solver.Solution;
import objenome.solver.Solver;
import org.apache.commons.math3.genetics.InvalidRepresentationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Object Genome; represents a solved plan for being able to construct instances from a Genetainer
 */
public class Objenome {

    public static Objenome solve(Solver s, Object... targets) throws IncompleteSolutionException {
        return new Multitainer().solve(s, targets);
    }
    
    Map<String, Solution> genes = new TreeMap();
    
    public final Multitainer parentContext;
    
    /** generated container, constructed lazily
        TODO different construction policies other than caching a single Phenotainer
        instance in this instance
        */
    private Phenotainer pheno = null;

    public Objenome(Multitainer context, Iterable<Solution> parameters) throws InvalidRepresentationException {

        for (Solution o : parameters)
            genes.put(o.key(), o);

        parentContext = context;
    }
    
    public int getSolutionSize() { return genes.size(); }
    
    /** gets the generated container of this Objenome with respect to the parent container.
        Parent is a Genetainer but the generated container is a Container
        which functions as an ordinary deterministic dependency injection container.     */
    public Phenotainer container() {
        if (pheno!=null)
            return pheno;
        pheno = new Phenotainer(this);        
        return pheno;
        
        //return new Phenotainer(this);        
    }

    /** call after genes have changed to update the container */
    public Phenotainer commit() {
        return container().commit();
    }
    
    public <T> T get(Object key) {
        return container().get(key);
    }
    
    /** list of applied solutions, sorted by key */
    public List<Solution> getSolutions() {
        List<Solution> l = new ArrayList(genes.size());
        for (Map.Entry<String, Solution> stringSolutionEntry : genes.entrySet()) {
            Solution g = stringSolutionEntry.getValue();
            l.add(g);
        }
        return l;
    }
    

    /** mutates this genome's genes, and commits changes to apply to next generated object */
    public Objenome mutate(/* .... mutation opcodes ... */) {
        genes.values().forEach(Solution::mutate);
        
        //invalidate the phenotainer so next time it will be reconstructed
        //TODO find why commit() wasnt sufficient to reset it after a mutate
        pheno = null;
        return this;
    }

    /** fitness function */
    public interface Scoring extends Function<Objenome,Double> {
        
    }

    
}
