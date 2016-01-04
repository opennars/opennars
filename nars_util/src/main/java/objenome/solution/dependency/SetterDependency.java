package objenome.solution.dependency;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import objenome.util.FindMethod;
import objenome.util.InjectionUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A simple implementation of the Dependency interface.
 *
 * @author sergio.oliveira.jr@gmail.com
 */
public class SetterDependency {

    private final String targetProperty;

    private final String sourceFromContainer;

    private final Class<?> sourceType;

    private final Map<String, Method> cache = new UnifiedMap(0);

    public SetterDependency(String targetProperty, String sourceFromContainer, Class<?> sourceType) {

        this.targetProperty = targetProperty;

        this.sourceFromContainer = sourceFromContainer;

        this.sourceType = sourceType;
    }

    public static Method findMethodToInject(Class<?> target, String name, Class<?> source) {

        StringBuilder sb = new StringBuilder(128);

        sb.append("set").append(name.substring(0, 1).toUpperCase());

        if (name.length() > 1) {
            sb.append(name.substring(1));
        }

        String methodName = sb.toString();

        Method m = null;

        try {

            m = FindMethod.getMethod(target, methodName, new Class[]{source});

        } catch (Exception e) {
        }

        if (m == null) {

            Class<?> primitive = InjectionUtils.getPrimitiveFrom(source);

            if (primitive != null) {

                try {

                    m = target.getMethod(methodName, primitive);

                } catch (Exception e) {
                }

            }
        }

        if (m != null) {
            m.setAccessible(true);
        }

        return m;

    }

    public String getTarget() {

        return targetProperty;
    }

    public String getSource() {

        return sourceFromContainer;
    }

    @Override
    public int hashCode() {

        return targetProperty.hashCode() * 31 + sourceFromContainer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;

        if (!(obj instanceof SetterDependency)) {
            return false;
        }

        SetterDependency d = (SetterDependency) obj;

        if (!d.targetProperty.equals(targetProperty)) {
            return false;
        }

        return d.sourceFromContainer.equals(sourceFromContainer);
    }

    public Method check(Class<?> targetClass) {

        String className = targetClass.getName();

        // first check cache...
        Method m = null;

        synchronized (cache) {

            m = cache.get(className);

        }

        if (m == null && cache.containsKey(className)) {
            return null; // it is null...
        }
        if (m != null) {
            return m;
        }

        m = findMethodToInject(targetClass, targetProperty, sourceType);

        /*
         try {
        	
         Method[] methods = targetClass.getMethods();
        	
         StringBuilder sb = new StringBuilder(128);
         sb.append("set");
         sb.append(targetProperty.substring(0, 1).toUpperCase());
         if (targetProperty.length() > 1) sb.append(targetProperty.substring(1));
        	
         String methodName = sb.toString();
        	
         for(Method theMethod : methods) {
        		
         if (theMethod.getName().equals(methodName)) {
        			
         // check type...
        			
         Class<?>[] params = theMethod.getParameterTypes();
        			
         if (params == null || params.length != 1) continue;
        			
         if (params[0].isAssignableFrom(sourceType)) {
        				
         m = theMethod;
        				
         break;
         }
         }
         }
        	
         } catch(Exception e) {
        	
         e.printStackTrace();
         }
         */
        if (m != null) {

            synchronized (cache) {

                cache.put(className, m );

            }

            return m;
        }

        synchronized (cache) {

            // save null to indicate there is no method here... (so you don't do again to find null !!!
            cache.put(className, null);

        }

        return null;
    }
}
