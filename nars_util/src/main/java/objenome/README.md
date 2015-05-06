Evolutionary Dependency Injection
------------------------------

A [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) container that **automatically assembles software** from combinations of components.  Ambiguities in the choice of particular dependencies and parameter constants forms a non-deterministic set of parameters that can be mutated, combined, and optimized to maximize supplied design goals.  And at its core, a **deterministic**, **minimal**, **fluent**, **pure Java**, **no-nonsense** **dependency-injection container**.


![base](https://raw.githubusercontent.com/automenta/objenome/master/objenome.jpg)


Hyperparameter Optimization
---------------------------
[Hyperparameter optimization](https://en.wikipedia.org/wiki/Hyperparameter_optimization) of arbitrary APIs by automatic application of evolutionary, numeric optimization, constraint satisfaction, and other kinds of search, directly to any given set of software components (ex: java classes) through their API.

Solutions to the set of unknown numeric and enumerated parameters involved in a non-deterministic component container (Multitainer) form a plan (genotype) for being able to instantiate desired classes (phenotype) -- even if nothing else is known except the available classes themselves.


How it Works
------------

Objenome is built on a refactored and generalized version of [MentaContainer](http://mentacontainer.soliveirajr.com/mtw/Page/Intro/en/mentacontainer-overview), which provided a minimal and straightforward DI container.

[Apache Commons Math](http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/) provides genetic algorithm, numeric optimization, and other numeric solvers.

An adapted version of the Genetic Programming library [EpochX 2.0](https://github.com/tc33/) is included in the 'evolve' packages.  There are significant architectural differences, including the elimination of all EpochX's need for static classes, and further refactoring to simplify the API. (Currently, this fork does not include EpochX's Context-free-grammar (CFG) packages, though these can be integrated later.)  GP evolution configuration has been modified to use an Objenome dependency-injection container __internally__.  Javassist dynamic bytecode is available to automatically replace unimplemented abstract methods of constructed instances with procedures and expressions evolved to maximize a provided fitness function.


*Container* Dependency Injection
======================

**Basic use() autowiring & get() instantiation**

``` java
Container c = new Container();

c.use(Part.class, Part0.class);

Part p = c.get(Part.class);
```

**Singleton scope: the()**

the() is similar to get() but involves the Singleton scope.

``` java
//1. Using a java literal as a key.  all the(..) lookups with keys equal to it will return the same singleton instance
Part x = c.the("part", new Part0());
Part y = c.the("part", new Part0());
//assertTrue(x==y);

//2. Providing a different key produces a different singleton
Part z = c.the("part2", new Part0());
//assertTrue(x!=z);

//3. Providing no key uses provided instance's class (Part0.class) by default; a different singleton
Part w = c.the(new Part0());
//assertTrue(w!=z);

//4. Accessing by the class gets the default singleton (same as 3).
Part v = c.the(Part0.class);
//assertTrue(v==w);
```

**Multiple dependency levels, mixing use() & usable()**

usable() is similar to use(), but does not involve auto-wiring.  it is like a non-automatic dependency declaration which suggests the availability of an implementation in case a dependent target requires it.

``` java
Container c = new Container();
    
/* public static class JdbcUserDAO implements UserDAO 
      public void setConnection(Connection conn)*/
c.usable(JdbcUserDAO.class);
c.usable(ParameterX.class);
c.use(Connection.class); //wires to setter

/* public ServiceNeedingDAOandParameter(UserDAO userDAO, ParameterX x) */
service = c.get(ServiceNeedingDAOandParameter.class);
```

*Multitainer* injection
=====

**Ambiguous implementation choice with any() & mutate()**

any( AbstractClass.class, of( Impl1.class, Impl2.class) ) defines an ambiguity that must be resolved in the objenome prior to realization.  The default solver starts with random values, and mutate() randomizes them again.

``` java
Multitainer g = new Multitainer();

g.any(Part.class, of(Part0.class, Part1.class));
                
Objenome o = g.solve(Machine.class);

Machine m = o.get(Machine.class);

Machine maybeDifferent = o.mutate().get(Machine.class); 
```

**Ambiguous choice of implementation, and an unknown constant constructor parameter**

``` java
Multitainer g = new Multitainer();

g.any(Part.class, 
    of(Part0.class, Part1.class, PartN.class));
                
Objenome o = g.random(Machine.class);

//assertEquals(2, o.getSolutionSize());
//  1st solution chooses one of three Part implementations
//  2nd solution chooses the 'int' parameter of PartN. (Part0 and Part1 take no parameters as their names suggest)

Machine m = o.get(Machine.class);
```



**Recursive discovery of differently-typed dependencies**

If PartN is chosen, its constructor parameter must be provided.  This creates another "gene" that can be randomized.  The @Before annotation specifies a method parameter's acceptable range.

``` java
Multitainer g = new Multitainer();

/* public PartN( @Between(min=1, max=3) int arg0) { */
g.any(Part.class, of(Part0.class, Part1.class, PartN.class));
g.any(PartWithSubPart.class, of(SubPart0.class, SubPart1.class));
                
Objenome o = g.solve(Machine.class);

```
Note that including Part.class as an additional target, ex: *g.solve(Machine.class, Part.class)* was **NOT** necessary; the Part.class dependency was recursively discovered through reflection.




Numeric Optimization
==================

**Find Constant parameters to fit function zeros (roots)**
``` java
/* public ExampleScalarFunction(@Between(min=-4.0, max=4.0) double constParameter) */
Objenome o = Objenome.solve(new FindZeros(ExampleScalarFunction.class,
        new Function<ExampleScalarFunction, Double>() {            
            public Double apply(ExampleScalarFunction s) {                
                return s.output(0.0) + s.output(0.5) + s.output(1.0);
            }            
}), ExampleScalarFunction.class);

double bestParam = ((Number)o.getSolutions().get(0)).doubleValue();
```
        
**Find constant parameters to maximize a heuristic goal**

*Multivariate optimization* can iteratively search for more optimal sets of 1 or more variables.
Apache Commons Math provides several solvers, with defaults configured for the most general case: non-continuous with no assumption of curve derivatives.

These features be used to optimize a numeric result, a runtime performance metric, an intelligence heuristic, etc.

Once a set of parameters are discovered, they can be saved and re-used.  If after further development the target component has changed, or new features become available, numeric search can be attempted again to find perhaps even better values.

``` java
/* public ExampleMultivariateFunction(@Between(min=-4.0, max=4.0) double a, boolean b)  */
Objenome o = Objenome.solve(new OptimizeMultivariate(ExampleMultivariateFunction.class, 
    new Function<ExampleMultivariateFunction, Double>() {
        public Double apply(ExampleMultivariateFunction s) {      
            double v = s.output(0.0) + s.output(0.5) + s.output(1.0);
            return v;
        }    
}).minimize(), ExampleMultivariateFunction.class);

double bestParam = ((Number)o.getSolutions().get(1)).doubleValue();
```
        




***More documentation coming soon: additional Unit Test examples EXPLAINED***
-----------------------------------------------------------------




Code Evolution with Genetic Programming
=======

**See MethodsGPEvolvedTest and STGP***Test's**


ClassLoader and .JAR Combinatorics
=======

**See PackatainerTest and JartainerTest**


Mutation Testing
=======

**(Possibly planned feature.)**
