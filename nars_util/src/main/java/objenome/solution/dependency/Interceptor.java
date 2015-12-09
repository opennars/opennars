package objenome.solution.dependency;

/**
 * Some factories can also implement this interface to perform some cleanup when
 * the instance is created or cleared. For example, a connection pool will want
 * to know when the connection instance is cleared so it can return it to the
 * pool.
 *
 * It makes more sense to use this interface for factories that will be placed
 * in the THREAD scope, but you can also use it with other scopes as well.
 *
 * This is particular useful for the THREAD scope for dealing with thread pools,
 * so when the thread is returned to the thread pool you will want to clear the
 * THREAD scope. That's pretty much how web containers work: one thread per
 * request coming from a thread pool.
 *
 * @author sergio.oliveira.jr@gmail.com
 *
 * @param <E>
 */
public interface Interceptor<E> {

    /**
     * This method will be called right before the getInstance() method return a
     * new instance created by the factory.
     *
     * @param createdObject The object that was just created.
     */
    void onCreated(E createdObject);

    /**
     * This method will be called right before the object is cleared from the
     * scope.
     *
     * @param clearedObject The object being cleared.
     */
    void onRemoved(E clearedObject);
}
