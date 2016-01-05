/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open of template in of editor.
 */
package objenome;

import com.google.common.collect.Lists;
import objenome.goal.DecideNumericValue.DecideBooleanValue;
import objenome.goal.DecideNumericValue.DecideDoubleValue;
import objenome.goal.DecideNumericValue.DecideIntegerValue;
import objenome.goal.DevelopMethod;
import objenome.problem.Problem;
import objenome.solution.dependency.*;
import objenome.solution.dependency.ClassBuilder.DependencyKey;
import objenome.solver.IncompleteSolutionException;
import objenome.solver.RandomSolver;
import objenome.solver.Solution;
import objenome.solver.Solver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Dependency-injection Multainer which can be parametrically searched to 
 generate Phenotainer containers
 
 early 20th century: of German Gen, of Pangen, a supposed ultimate unit of heredity (of Greek pan * ‘all’ + genos ‘race, kind, offspring’) + of Latin tenere 'to hold.’"
 */
public class Multitainer extends AbstractPrototainer implements AbstractMultitainer {       
    private int intMinDefault = 0;
    private int intMaxDefault = 1;
    private int doubleMinDefault = 0;
    private int doubleMaxDefault = 1;
    
    public Multitainer(Class... useClasses) {
        this();
        for (Class c : useClasses)
            use(c);
    }
    
    public Multitainer() {
        this(false);        
    }
    
    public Multitainer(boolean concurrent) {
        super(concurrent);
    }
    
    public Multitainer(AbstractPrototainer p) {
        super(p.builders, p.scopes, p.setterDependencies, p.constructorDependencies, p.forConstructMethod);
    }
        
    @Override
    public DecideImplementationClass any(Class abstractClass, Scope scope, Class<?>... klasses) {
        if (abstractClass == null)
            throw new RuntimeException("abstractClass is null");        
        
        return (DecideImplementationClass)usable(abstractClass, scope, 
                new DecideImplementationClass(abstractClass, Lists.newArrayList( klasses ) ));
    }

    public Builder any(Object key, Class<?>[] klasses) {
        Builder b = getBuilder(key);
        if (b instanceof ClassBuilder) {
            return any( b.type(), klasses );
        }
        return null;
    }
    
    
    /**
     * TODO call cb.updateConstructorDependencies(..) fewer times, once should be enough with the right design
     */
    protected List<Problem> getProblems(ClassBuilder cb, List<Object> path, List<Problem> problems) {
        
        //handle primitive value parameters
        {
            cb.updateConstructorDependencies(false);

            if (cb.getInitPrimitives()!=null) {
                for (Parameter p : cb.getInitPrimitives()) {
                    List nextPath = new ArrayList(path);
                    nextPath.add(p);

                    if (p.getType() == int.class) {
                        problems.add(new DecideIntegerValue(p, nextPath, 
                                getIntMinDefault(), getIntMaxDefault()));
                    }
                    else if (p.getType() == double.class) {
                        problems.add(new DecideDoubleValue(p, nextPath, 
                                getDoubleMinDefault(), getDoubleMaxDefault()) );
                    }
                    else if (p.getType() == boolean.class) {
                        problems.add(new DecideBooleanValue(p, nextPath) );    
                    }
                    else {
                        throw new RuntimeException("primitive Parameter " + nextPath + ' ' + p + " not yet supported");
                    }
                }
            }
        }

        
        //Handle Abstract Methods
        Class c = cb.type();
        for (Method m : c.getMethods()) {
            if (Modifier.isAbstract( m.getModifiers() )) {
                problems.add(new DevelopMethod(m) );
            }
        }

        //Handle Constructor Dependencies
        Set<DependencyKey> possibleConstructorDependencies = new HashSet();
        if (cb.getInitValues()!=null) {
            possibleConstructorDependencies.addAll(cb.getInitValues().stream().filter(o -> o instanceof DependencyKey).map(o -> (DependencyKey) o).collect(Collectors.toList()));
        }

        //simultate instancing to find all possible constructor dependencies
        cb.instance(this, possibleConstructorDependencies);

        for (DependencyKey dk : possibleConstructorDependencies) {

            List nextPath = new ArrayList(path);
            nextPath.add(dk);

            Builder bv = getBuilder(dk.key);

            if (bv instanceof DecideImplementationClass) {

                problems.add((Problem)bv);

                //recurse for each choice
                getProblems(((DecideImplementationClass)bv).implementors, nextPath, problems);
            }
            else if (bv!=null) {
                //System.out.println("  Class Builder Init Value Builder: "+ cb + " " + bv);
                getProblems(bv, nextPath, problems);
            }
            else if (dk.param!=null) {

                //this was likely a newly discovered dependency,
                //from possibleConstructorDependencies,
                //so recursively discover its problems:

                Class paramClass = dk.param.getType();
                getProblems(paramClass, nextPath, problems);

            }
        }


        //TODO handle setters, etc


        //System.out.println("Class Builder: "+ path + " " + cb);

        return problems;
    }
    
    protected List<Problem> getProblems(Iterable keys, List<Object> parentPath, List<Problem> problems) {
        for (Object k : keys) {
            problems = getProblems(k, parentPath, problems);
        }
        return problems;
    }
    
    protected List<Problem> getProblems(Object k, List<Object> parentPath, List<Problem> problems) {
        if (problems == null) problems = new ArrayList();
            
        //TODO lazily calculate as needed, not immediately because it may not be used
        List<Object> path;
        path = parentPath == null ? new ArrayList() : new ArrayList(parentPath);

        Object previousPathElement = !path.isEmpty() ? path.get(path.size()-1) : null;

        Builder b = (k instanceof Builder) ? (Builder)k : getBuilder(k);

        //System.err.println(k + " --> " + b);
        //System.out.println(parentPath);
        
        if (b == null) {
            ClassBuilder cb = getClassBuilder(k.getClass() instanceof Class ? (Class)k : k.getClass());
            if (cb.equals(previousPathElement))
                throw new RuntimeException("Cyclic dependency: " + path + " -> " + cb);

            //System.out.println(k + ">" + cb + " " + problems);
                    
            path.add(cb);                
            getProblems(cb, path, problems);
            
            //System.out.println(k + "<" + cb + " " + problems);
            
        }
        else {
            
        
            if (b instanceof DecideImplementationClass) {
                DecideImplementationClass mcb = (DecideImplementationClass)b;
                path.add(mcb);

                problems.add(mcb);
                
                return getProblems(mcb.implementors, path, problems);
            }
            else if (b instanceof ClassBuilder) {                      
                if (b.equals(previousPathElement)) {
                    return problems;
                    //throw new RuntimeException("Cyclic dependency: " + path + " -> " + b);
                }

                path.add(b);
                
                if (k!=b.type())
                    getProblems( b.type(), path, problems);
                else
                    getProblems( (ClassBuilder)b, path, problems);
                return problems;
                
            }
            else {
                throw new RuntimeException("decide what this means: Builder=" + b);                
                /*if (b instanceof Parameterized) {
                    genes.addAll( ((Parameterized)b).getGenes(path) );
                } */               
            }
        }
        
        return problems;
        
    }

    
    public List<Object> getKeyClasses() {
        //TODO use setter dependencies also?
        return getConstructorDependencies().stream().map(ConstructorDependency::getContainerKey).filter(k -> k!=null).collect(toList());
    }
    
    /** creates a new random objosome,
  analogous to AbstractContainer.genome(Object key) except this represents of set of desired
  keys for which to evolve a set of Objosomes can be evolved to generate
     */
    public Objenome random(Object... keys) {
        try {
            return solve(new RandomSolver(), keys);
        } catch (IncompleteSolutionException ex) {
            throw new RuntimeException(ex.toString(), ex);
        }
    }
    
    
    
    
    
    
    
    public Objenome solve(Solver solver, Object... keys) throws IncompleteSolutionException {
        return solve(Lists.newArrayList(solver), keys);
    }
    
    public Objenome solve(Iterable<Solver> solvers, Object... targets) throws IncompleteSolutionException {
        List<? extends Object> k;
        k = targets.length == 0 ? getKeyClasses() : Lists.newArrayList(targets);
        
        List<Problem> p = getProblems(k, null, null);
        
        Map<Problem,Solution> problemSolutions = new HashMap();
        for (Problem x : p) {
            problemSolutions.put(x, null);
        }
        
        Map<Problem,Solution> remainingProblems = null;
        for (Solver solver : solvers) {
            if (remainingProblems == null)
                remainingProblems = new HashMap(problemSolutions.size());
            else
                remainingProblems.clear();
            
            //extract the keys with missing solutions to add to the solver
            for (Map.Entry<Problem, Solution> e : problemSolutions.entrySet()) {
                if (e.getValue() == null)
                    remainingProblems.put(e.getKey(), null);
            }
            
            if (remainingProblems.isEmpty())
                break;
            
            solver.solve(this, remainingProblems, targets);
            
            //merge the new results with the main problems/solutions map
            problemSolutions.putAll(remainingProblems);
        }
       
        return solve(targets, problemSolutions);
    }

    public Objenome solve(Object[] targets, Map<Problem,Solution> problemSolutions) throws IncompleteSolutionException {
        List<Problem> missing = problemSolutions.entrySet().stream().filter(e -> e.getValue() == null).map(Map.Entry::getKey).collect(Collectors.toList());

        if (!missing.isEmpty())
            throw new IncompleteSolutionException(missing, targets, this);
        
        Set<Solution> g = new HashSet();
        for (Map.Entry<Problem, Solution> e : problemSolutions.entrySet()) {                        
            Solution s = e.getValue();
            if (s == null) {
                missing.add(e.getKey());
                throw new IncompleteSolutionException(missing, targets, this);
            }
            g.add(s);
        }
        
        return new Objenome(this, g);        
    }
    
    /** realize of phenotype of a chromosome */
    public AbstractContainer build(Objenome objsome, Object[] keys) {

        //populate a new DefaultContext as configured by this Objenome and of static parameters provided in Genetainer parent container
        return null;
    }    

    /** used if a parameter annotation is not present on primitive parameters */
    public int getIntMinDefault() {  return intMinDefault;    }
    /** used if a parameter annotation is not present on primitive parameters */
    public int getIntMaxDefault() {  return intMaxDefault;    }
    /** used if a parameter annotation is not present on primitive parameters */
    public int getDoubleMinDefault() {  return doubleMinDefault;    }
    /** used if a parameter annotation is not present on primitive parameters */
    public int getDoubleMaxDefault() {  return doubleMaxDefault;    }

    public void setIntMaxDefault(int intMaxDefault) {
        this.intMaxDefault = intMaxDefault;
    }

    public void setIntMinDefault(int intMinDefault) {
        this.intMinDefault = intMinDefault;
    }

    public void setDoubleMaxDefault(int doubleMaxDefault) {
        this.doubleMaxDefault = doubleMaxDefault;
    }

    public void setDoubleMinDefault(int doubleMinDefault) {
        this.doubleMinDefault = doubleMinDefault;
    }

//    /** returns an error string summarizing why a list of genes would be invalid
// with respect to this container; or null if there is no error     */
//    public String getChromosomeError(List<Solution> genes) {
//        return null;
//    }


}
