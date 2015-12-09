package objenome.solution.dependency;

import objenome.Prototainer;
import objenome.solution.dependency.ClassBuilder.DependencyKey;
import objenome.util.FindMethod;

import java.lang.reflect.Method;
import java.util.Collection;

public class GenericBuilder<E> implements Builder, Interceptor<E> {

    private final Object factory;

    private final Method method;

    private final Class<?> type;

    private Interceptor<E> interceptor = null;

    public GenericBuilder(Object factory, String methodName) {

        this.factory = factory;

        try {

            method = FindMethod.getMethod(factory.getClass(), methodName, new Class[]{});

            method.setAccessible(true);

            type = method.getReturnType();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public void setInterceptor(Interceptor<E> interceptor) {

        this.interceptor = interceptor;
    }

    @Override
    public void onCreated(E createdObject) {
        if (interceptor != null) {
            interceptor.onCreated(createdObject);
        }
    }

    @Override
    public void onRemoved(E clearedObject) {
        if (interceptor != null) {
            interceptor.onRemoved(clearedObject);
        }
    }

    @Override
    public <T> T instance(Prototainer context, Collection<DependencyKey> simulateAndAddExtraProblemsHere) {

        if (simulateAndAddExtraProblemsHere==null) {
            try {

                return (T) method.invoke(factory, (Object[]) null);

            } catch (Exception e) {

                throw new RuntimeException("Cannot invoke method: " + method, e);

            }
        }
        
        return null;
    }

    @Override
    public Class<?> type() {
        return type;
    }
}
