/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import objenome.solution.dependency.Scope;

/**
 * AbstractContainer which can completely operate to produce or modify objects
 * 
 * "Middle English: from Old French contenir, from Latin continere, from con- altogether + tenere to hold."
 */
public interface AbstractContainer extends Prototainer {

    /**
     * Take a given bean and populate its properties with other beans coming
     * from this container. You basically checking properties of the given bean
     * and looking for values inside the container. And injecting in the given
     * bean, in other words, populating it.
     *
     * @param bean The bean to be populated with other beans from the container.
     */
    <C> C apply(C instance);

    /**
     * Get an instance from the container by using the associated factory.
     *
     * The instance will be fully initialized (through constructor and/or
     * setters) and fully wired (all dependencies will be resolved).
     *
     * @param key The key representing the factory to usable. The name of the bean
    in the container.
     * @return The fully initialized and wired bean.
     */
    <T> T get(Object key);

    /**
     * Construct an instance using beans from the container. A constructor will
     * be chosen that has arguments that can be found inside the container.
     *
     * @param klass The class that should be instantiated.
     * @return An instantiated bean.
     */
    <T> T get(Class<? extends T> klass);
    
    /**
     * Clear all cached instances for that scope. If you have a thread-pool for
 example you will want to remove the THREAD scope when your thread is
 returned to the pool. Because you have a thread pool you will have the
 SAME thread handling different requests and each request will need its
 own instances from the container. Therefore, each time you are done with
 a thread and it is returned to your thread-pool you can call remove to
 release the instances allocated and cached by the container. A web
 container and/or framework can usable this feature to implement a REQUEST
 scope which is nothing more than the THREAD scope with remove. If the web
 container was not using a thread-pool, the THREAD scope would be equal to
 the REQUEST scope as each request would always be handled by a different
 thread.

 It does not make sense to remove a NONE scope (the method returns doing
 nothing). You can remove a SINGLETON scope if necessary.
     *
     * @param scope The scope to be cleared.
     */
    void remove(Scope scope);

    /**
     * Clear a single key from cache and return the instance that was cached.
     *
     * @param key The key representing the bean inside the container.
     * @return The value that was cached and it is not anymore (was cleared) or
     * null if nothing was cleared
     */
    <T> T remove(Object key);


    /**
     * Check whether the container currently has a value for this key. For
     * example, if it is a singleton AND someone has requested it, the container
     * will have it cached. The method is useful to contains for an instance
     * without forcing her creation.
     *
     * @param key The key representing the bean inside the container.
     * @return true if the container has an instance cached in the scope for
     * this key
     */
    boolean contains(Object key);
    

}
