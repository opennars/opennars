package objenome.util;

import objenome.solution.SetMethodsGPEvolved;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Find constructor with polymorphism! Class.getConstructor only finds an exact
 * match.
 *
 * @author Jon Skeet
 * (http://groups.google.com/group/comp.lang.java.programmer/browse_thread/thread/921ab91865c8cc2e/9e141d3d62e7cb3f)
 */
public enum FindConstructor {
    ;

    /**
     * Finds the most specific applicable constructor
     *
     * @param source Class to find a constructor for
     * @param parameterTypes Parameter types to search for
     */
    public static Constructor<?> getConstructor(Class<?> source,
            Class<?>[] parameterTypes, Map<Parameter,Object> specific)
            throws NoSuchMethodException {
        return internalFind(source.getConstructors(),
                parameterTypes, 
                specific);
    }

    /**
     * Finds the most specific applicable declared constructor
     *
     * @param source Class to find method in
     * @param parameterTypes Parameter types to search for
     */
    public static Constructor<?> getDeclaredConstructor(Class<?> source,
            Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        return internalFind(source.getDeclaredConstructors(),
                parameterTypes,
                Collections.<Parameter, Object>emptyMap());
    }

    
    public static class NoDeterministicConstruction extends NoSuchMethodException {
        
        public final List<Constructor<?>> possibleConstructors;

        public NoDeterministicConstruction(List<Constructor<?>> partiallyApplicableMethods, String s) {
            super(s);
            possibleConstructors = partiallyApplicableMethods;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            //no stack trace
            return this;
        }
    }
    
    /**
     * Internal method to find the most specific applicable method
     * TODO handle dynamic classes with >1 constructor (in the 2nd half of this function)
     */
    private static Constructor<?> internalFind(Constructor<?>[] toTest,
            Class<?>[] parameterTypes, Map<Parameter,Object> specific)
            throws NoSuchMethodException {

        
        
        // First find the applicable methods 
        List<Constructor<?>> applicableMethods = new ArrayList<>();
        List<Constructor<?>> partiallyApplicableMethods = new ArrayList<>();

        int assigned = 0;
        for (Constructor<?> aToTest : toTest) {
            Constructor c = aToTest;
            Constructor actual = c;

            //if dynamic, Find a matching shadow constructor in the parent class to which gene 
            //Parameters will be mapped against
            Class x = c.getDeclaringClass();
            if (x.getName().endsWith(SetMethodsGPEvolved.DYNAMIC_SUFFIX)) {
                Class parent = x.getSuperclass();
                c = parent.getConstructor(c.getParameterTypes());
                if (c == null)
                    continue;
            }

            // Check the parameters match 
            Parameter[] params = c.getParameters();
            Parameter[] paramsActual = actual.getParameters();

            int k = 0;
            for (int j = 0; j < params.length; j++) {
                Object specificValue = specific.get(params[j]);

                if (specificValue != null) {

                    if (c != actual) {
                        //add a duplicated parameters for the actual (dynamic class's) constructor, 
                        //because a parameter was specified for the shadow class's
                        specific.put(paramsActual[j], specificValue);
                    }

                    assigned++;
                }
                //TODO parameterTypes may be out of order or missing holes, so allow that
                else if ((k < parameterTypes.length) && (params[k].getType().isAssignableFrom(parameterTypes[k++]))) {
                    assigned++;
                }


            }

            // If so, add it to the list 
            if (assigned == params.length) {
                applicableMethods.add(actual);
            } else {
                partiallyApplicableMethods.add(actual);
            }
        }

        /* 
         * If we've got one or zero methods, we can finish 
         * the job now. 
         */
        int size = applicableMethods.size();

        if (size == 0) {
            throw new NoDeterministicConstruction(partiallyApplicableMethods, "No valid constructor exists for " + Arrays.toString(parameterTypes) + ", " + specific);
            
        }
        if (size == 1) {
            return applicableMethods.get(0);
        }

        /* 
         * Now find the most specific method. Do this in a very primitive 
         * way - check whether each method is maximally specific. If more 
         * than one method is maximally specific, we'll throw an exception. 
         * For a definition of maximally specific, see JLS section 15.11.2.2. 
         * 
         * I'm sure there are much quicker ways - and I could probably 
         * set the second loop to be from i+1 to size. I'd rather not though, 
         * until I'm sure... 
         */
        int maximallySpecific = -1; // Index of maximally specific method 

        for (int i = 0; i < size; i++) {
            int j;
            // In terms of the JLS, current is T 
            Constructor<?> current = applicableMethods.get(i);
            Parameter[] currentParams = current.getParameters();
            Class<?> currentDeclarer = current.getDeclaringClass();

            for (j = 0; j < size; j++) {
                if (i == j) {
                    continue;
                }
                // In terms of the JLS, test is U 
                Constructor<?> test = applicableMethods.get(j);
                Parameter[] testParams = test.getParameters();
                Class<?> testDeclarer = test.getDeclaringClass();

                // Check if T is a subclass of U, breaking if not 
                if (!testDeclarer.isAssignableFrom(currentDeclarer)) {
                    break;
                }

                // Check if each parameter in T is a subclass of the 
                // equivalent parameter in U 
                int k;

                int l = 0;
                for (k = 0; k < testParams.length; k++) {                    
                    if (
                        ((l++) >= currentParams.length) || 
                        (specific.get(currentParams[l] )==null) ||
                        (!testParams[k].getType().isAssignableFrom(currentParams[l].getType()))
                        ) {
                        break;
                    }
                }
                if (k != testParams.length) {
                    break;
                }
            }
            // Maximally specific! 
            if (j == size) {
                if (maximallySpecific != -1) {
                    throw new NoSuchMethodException("Ambiguous method search - more "
                            + "than one maximally specific method");
                }
                maximallySpecific = i;
            }
        }
        if (maximallySpecific == -1) {
            throw new NoSuchMethodException("No maximally specific method.");
        }
        return applicableMethods.get(maximallySpecific);
    }

}
