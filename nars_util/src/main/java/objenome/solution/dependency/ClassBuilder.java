package objenome.solution.dependency;

import objenome.AbstractContainer;
import objenome.Phenotainer;
import objenome.Prototainer;
import objenome.util.FindConstructor;
import objenome.util.FindConstructor.NoDeterministicConstruction;
import objenome.util.FindMethod;
import objenome.util.InjectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The implementation of the Configurable Factory.
 *
 * @author sergio.oliveira.jr@gmail.com
 */
public class ClassBuilder implements ConfigurableBuilder {

    private final Prototainer container;

    private final Class<?> klass;

    private Map<String, Object> props = null;

    private List<Object> initValues = null;

    private List<Class<?>> initTypes = null;

    private Constructor<?> constructor = null;

    private Map<String, Method> cache = null;

    private boolean useZeroArgumentsConstructor = false;

    public final Set<ConstructorDependency> constructorDependencies;
    private List<Parameter> initPrimitives;
    private boolean specificInitValue;
    private List<Constructor<?>> possibleConstructors;

    public ClassBuilder(Prototainer container, Class<?> klass) {

        this(container, klass, null);
    }

    public ClassBuilder(Prototainer container, Class<?> klass, Set<ConstructorDependency> constructorDependencies) {

        this.container = container;

        this.klass = klass;

        this.constructorDependencies = constructorDependencies;

    }

    @Override
    public ConfigurableBuilder addPropertyValue(String name, Object value) {

        if (props == null) {

            props = new HashMap<>();

            cache = new HashMap<>();
        }

        props.put(name, value);

        return this;
    }

    public List<Class<?>> getInitTypes() {
        return initTypes;
    }

    public List<Object> getInitValues() {
        return initValues;
    }

    public List<Parameter> getInitPrimitives() {
        return initPrimitives;
    }
    

    @Override
    public ConfigurableBuilder useZeroArgumentConstructor() {

        useZeroArgumentsConstructor = true;

        return this;
    }

    @Override
    public ConfigurableBuilder addPropertyDependency(String property, Object key) {

        String k = InjectionUtils.getKeyName(key);

        return addPropertyValue(property, new DependencyKey(k));
    }

    @Override
    public ConfigurableBuilder addPropertyDependency(String property) {

        return addPropertyDependency(property, property);
    }

    @Override
    public ConfigurableBuilder constructorUse(Object key) {

        String k = InjectionUtils.getKeyName(key);

        Class<?> t = container.type(k);
        
        return addInitValue(new DependencyKey(k), t);
    }

    private ConfigurableBuilder addInitValue(Object value, Class<?> type) {

        if (initValues == null) {

            initValues = new LinkedList<>();

            initTypes = new LinkedList<>();
        }

        initValues.add(value);

        initTypes.add(type);

        return this;
    }

    @Override
    public ConfigurableBuilder addInitValue(Object value) {
        specificInitValue = true;
        return addInitValue(value, value.getClass());
    }

    @Override
    public ConfigurableBuilder addInitPrimitive(Object value) {

        Class<?> primitive = getPrimitiveFrom(value);

        if (primitive == null) {
            throw new IllegalArgumentException("Value is not a primitive: " + value);
        }

        return addInitValue(value, primitive);
    }

    private static List<Class<?>> convertToPrimitives(List<Class<?>> list) {

        if (list == null) {
            return null;
        }

        Iterator<Class<?>> iter = list.iterator();

        List<Class<?>> results = new LinkedList<>();

        while (iter.hasNext()) {

            Class<?> klass = iter.next();

            Class<?> primitive = getPrimitiveFrom(klass);

            if (primitive != null) {

                results.add(primitive);

            } else {

                results.add(klass);
            }
        }

        return results;
    }

    private static Class<?>[] getClasses(List<Class<?>> values) {

        if (values == null) {
            return new Class[0];
        }

        Class<?>[] types = new Class[values.size()];

        return values.toArray(types);
    }

    private static Object[] getValues(Prototainer container, Constructor c, List<Object> values, Map<Parameter,Object> specific, Collection<DependencyKey> missingDependencies)  {

        if (values == null) {
            return null;
        }

        Parameter[] ps = c.getParameters();
        int size = ps.length;
        
        Object[] array = new Object[size];

        int index = 0;
        int v = 0;
        for (Parameter p : ps) {

            Object specified = specific.get(p);
            if (specified != null)
                array[index] = specified;
            else {

                if (missingDependencies!=null) {
                    missingDependencies.add(new DependencyKey(p, c.toString() + p.toString() ));
                }
                else {
                    Object obj = values.get(v++);
                    if (obj instanceof DependencyKey) {

                        DependencyKey dk = (DependencyKey) obj;

                        Object dependency = null;
                        if (container instanceof AbstractContainer)
                            dependency = ((AbstractContainer)container).get(dk.getKey());
                        array[index] = dependency;


                    } else {

                        array[index] = obj;
                    }
                }
            }
            
            index++;
        }

        return array;
    }

    /*
     * Use reflection to set a property in the bean
     */
    private void setValue(Object bean, String name, Object value) {

        try {

            StringBuilder sb = new StringBuilder(30);
            sb.append("set");
            sb.append(name.substring(0, 1).toUpperCase());

            if (name.length() > 1) {
                sb.append(name.substring(1));
            }

            String methodName = sb.toString();

            if (!cache.containsKey(name)) {

                Method m = null;

                try {

                    m = FindMethod.getMethod(klass, methodName, new Class[]{value.getClass()});

                } catch (Exception e) {

                    // try primitive...
                    Class<?> primitive = getPrimitiveFrom(value);

                    if (primitive != null) {

                        try {

                            m = klass.getMethod(methodName, primitive);

                        } catch (Exception ex) {
                            // not found!
                        }
                    }

                    if (m == null) {

                        throw new InstantiationException("Cannot find method for property: " + name);
                    }
                }

                if (m != null) {

                    cache.put(name, m);

                    m.setAccessible(true);
                }
            }

            Method m = cache.get(name);

            if (m != null) {

                m.invoke(bean, value);
            }

        } catch (Exception e) {

            throw new RuntimeException("Error trying to set a property with reflection: " + name, e);
        }
    }

    private static Class<?> getPrimitiveFrom(Object w) {
        if (w instanceof Boolean) {
            return Boolean.TYPE;
        }
        if (w instanceof Byte) {
            return Byte.TYPE;
        }
        if (w instanceof Short) {
            return Short.TYPE;
        }
        if (w instanceof Character) {
            return Character.TYPE;
        }
        if (w instanceof Integer) {
            return Integer.TYPE;
        }
        if (w instanceof Long) {
            return Long.TYPE;
        }
        if (w instanceof Float) {
            return Float.TYPE;
        }
        if (w instanceof Double) {
            return Double.TYPE;
        }
        return null;
    }

    private static Class<?> getPrimitiveFrom(Class<?> klass) {
        if (klass==(Boolean.class)) {
            return Boolean.TYPE;
        }
        //noinspection IfStatementWithTooManyBranches
        if (klass==(Byte.class)) {
            return Byte.TYPE;
        }
        if (klass==(Short.class)) {
            return Short.TYPE;
        }
        if (klass==(Character.class)) {
            return Character.TYPE;
        }
        if (klass==(Integer.class)) {
            return Integer.TYPE;
        }
        if (klass==(Long.class)) {
            return Long.TYPE;
        }
        if (klass==(Float.class)) {
            return Float.TYPE;
        }
        if (klass==(Double.class)) {
            return Double.TYPE;
        }
        return null;
    }

    public static Map<Parameter,Object> getParameters(Prototainer container) {
        if (container instanceof Phenotainer) {
            return ((Phenotainer)container).parameterValues;
        }
        return Collections.emptyMap();
    }
    
    @Override
    public <T> T instance(Prototainer context, Collection<DependencyKey> simulateAndAddExtraProblemsHere) {

        Object[] values = null;

        synchronized (container) {
            
            Map<Parameter,Object> specificParameters = getParameters(context);

            if (constructor == null) {

                if (!useZeroArgumentsConstructor) {

                    if (simulateAndAddExtraProblemsHere != null) {
                        //reset
                        initTypes = null;
                        initValues = null;
                    }
                    
                    updateConstructorDependencies();

                } else {

                    if (initTypes != null) {
                        initTypes = null; // just in case client did something stupid...
                    }
                    if (initValues != null) {
                        initValues = null; // just in case client did something stupid...
                    }
                }

                
                
                try {
                    
                    constructor = FindConstructor.getConstructor(klass, getClasses(initTypes), specificParameters);
                    
                } catch (Exception e) {

                    // try primitives...
                    try {
                        
                       constructor = FindConstructor.getConstructor(klass, getClasses(convertToPrimitives(initTypes)), specificParameters);

                    } 
                    catch (NoDeterministicConstruction ndc) {
                        
                        if (simulateAndAddExtraProblemsHere!=null)
                            possibleConstructors = ndc.possibleConstructors;
                        else
                            throw new RuntimeException("Missing constructor for class: " + klass, ndc);
                    }
                    catch (Exception ee) {
                        throw new RuntimeException("Missing constructor for class: " + klass, ee);
                    }
                    
                }
            }
            
            if (initValues == null)  {
                initValues = new ArrayList();                    
            }

            if (simulateAndAddExtraProblemsHere==null) {

                if (constructor == null) {
                    throw new RuntimeException("No constructors");
                }
                
                //throws a detailed RuntimeException if it fails:
                values = getValues(context, constructor, initValues, specificParameters, null);
                
            }
            else {
                //try all potential constructors, getting all missnig dependencies which are added as problems to simulateAndAddExtraProblemsHere collection
                if ((possibleConstructors!=null) || (constructor!=null)) {
                    Set<DependencyKey> missingDependencies = new HashSet();
                    
                    if (constructor!=null)
                        getValues(context, constructor, initValues, specificParameters, missingDependencies);
                    
                    if (possibleConstructors!=null) 
                        for (Constructor c : possibleConstructors) {
                            getValues(context, c, initValues, specificParameters, missingDependencies);
                        }

                    simulateAndAddExtraProblemsHere.addAll(missingDependencies.stream().collect(Collectors.toList()));
                }
            }
        }
        
        if (simulateAndAddExtraProblemsHere!=null) {            
            //finished
            return null;
        }

        Object obj = null;
        try {

            obj = constructor.newInstance(values);

        } catch (Exception e) {
            
            throw new RuntimeException("Cannot create instance of " + this + " with constructor: " + constructor + ": " + e + " with values=" + Arrays.toString(values), e);
        }

        //set Bean properties
        if (props != null && !props.isEmpty()) {

            //TODO use entrySet
            for (Map.Entry<String, Object> stringObjectEntry : props.entrySet()) {
                Object value = stringObjectEntry.getValue();

                if (value instanceof DependencyKey) {
                    DependencyKey dk = (DependencyKey) value;
                    value = ((AbstractContainer)context).get(dk.getKey());
                }

                setValue(obj, stringObjectEntry.getKey(), value);
            }
        }

        return (T) obj;
    }

    private static boolean betterIsAssignableFrom(Class<?> klass1, Class<?> klass2) {

        // with autoboxing both ways...
        if (klass1.isAssignableFrom(klass2)) {
            return true;
        }

        Class<?> k1 = klass1.isPrimitive() ? klass1 : getPrimitiveFrom(klass1);
        Class<?> k2 = klass2.isPrimitive() ? klass2 : getPrimitiveFrom(klass2);

        if (k1 == null || k2 == null) {
            return false;
        }

        return k1.isAssignableFrom(k2);
    }

    public void updateConstructorDependencies() {
        updateConstructorDependencies(true);
    }
    
    public void updateConstructorDependencies(boolean ignorePrimitives) {

        Constructor<?>[] constructors = klass.getConstructors();

        for (Constructor<?> c : constructors) {

            LinkedList<Class<?>> providedInitTypes = null;

            providedInitTypes = initTypes != null ? new LinkedList<>(initTypes) : new LinkedList<>();

            LinkedList<Object> providedInitValues = null;

            providedInitValues = initValues != null ? new LinkedList<>(initValues) : new LinkedList<>();

            List<Class<?>> newInitTypes = new LinkedList();
            List<Object> newInitValues = new LinkedList();
            List<Parameter> newInitPrimitives = new LinkedList();

            Set<ConstructorDependency> constructorDependencies = this.constructorDependencies != null ? this.constructorDependencies : container.getConstructorDependencies();

            Set<ConstructorDependency> dependencies = new HashSet<>(constructorDependencies);

                        
            Parameter[] constructorParams = c.getParameters();

            if (constructorParams == null || constructorParams.length == 0) {
                //Default constructor
                if (!specificInitValue) {
                    initTypes = newInitTypes; //use empty lists to indicate this
                    initValues = newInitValues;
                    initPrimitives = newInitPrimitives;
                }
                continue; 
            }
            for (Parameter p : constructorParams) {
                Class<?> pc = p.getType();

                // first see if it was provided...
                Class<?> provided = providedInitTypes.isEmpty() ? null : providedInitTypes.getFirst();

                if (provided != null && pc.isAssignableFrom(provided)) {

                    // matched this one, so remove...
                    System.out.println(c);
                    System.out.println(pc + " " + provided + ' ' + providedInitTypes + ' ' + providedInitValues);
                    
                    newInitTypes.add(providedInitTypes.removeFirst()); 
                    newInitValues.add(providedInitValues.removeFirst());

                    continue;

                } 
                                
                /*else*/

                //check for a Phenotainer-supplied get
                //TODO move this to a method in phenotainer
                if (container instanceof Phenotainer) {
                    Phenotainer pheno = (Phenotainer)container;
                    Object v = pheno.get(p);

                    //TODO check for assignability?
                    if (v!=null) {
                        newInitTypes.add(p.getType());
                        newInitValues.add(v);
                        continue;
                    }

                }


                boolean foundMatch = false;

                // contains auto-wiring...
                Iterator<ConstructorDependency> iter = dependencies.iterator();
                while (iter.hasNext()) {

                    ConstructorDependency d = iter.next();

                    if (betterIsAssignableFrom(pc, d.getSourceType())) {


                        //if phenotyping, first check that it can be instantiated by attempting it. if it works, save it into the context for the parameter
                        if (container instanceof Phenotainer) {
                            Phenotainer pheno = (Phenotainer)container;
                            try {
                                Object subinstance = pheno.get(d.getSourceType());
                                if (subinstance == null)
                                    continue;

                                //use this subinstance
                                pheno.use(p, subinstance);
                                newInitValues.add(subinstance);
                            }
                            catch (RuntimeException e) {
                                //not instantiable, try next dependency
                                continue;

                            }
                        }


                        iter.remove();

                        newInitTypes.add(d.getSourceType());

                        newInitValues.add(new DependencyKey(p, d.getSource()));

                        foundMatch = true;

                        break;

                    }
                }

                if (foundMatch)
                    continue; // next constructor param...


                if (!ignorePrimitives) {
                    //record primitives in constructor
                    if (pc.equals(double.class) || pc.equals(int.class) || pc.equals(boolean.class) || pc.equals(long.class) || pc.equals(short.class) ) {
                        newInitPrimitives.add(p);
                        newInitTypes.add(pc);
                    }
                    else {
                        //System.out.println("Missing: " + p + " " + pc.getName());
                        break;
                    }
                }
                else
                    break; // no param... next constructor...
            }

            // done, contains if found...
            int capableSize = newInitTypes.size();
                //requirePrimitives ?  newInitTypes.size() : (newInitPrimitives.size() + newInitTypes.size());
                    
            if (constructorParams.length == capableSize && providedInitTypes.isEmpty()) {

                initTypes = newInitTypes;
                initPrimitives = newInitPrimitives;
                initValues = newInitValues;
            }
        }
        
        //return missing;
    }

    public static final class DependencyKey {

        public final String key;
        public final Parameter param;

        public DependencyKey(String key) {
            this(null, key);            
        }
        
        public DependencyKey(Parameter param, String key) {
            this.param = param;
            this.key = key;
        }

        private String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return param != null ? param + " (" + key + ')' : key;
        }
        
        
    }

    @Override
    public Class<?> type() {
        return klass;
    }

    @Override
    public String toString() {
        return "ClassBuilder[" + type() + ']';
    }
    
    
}
