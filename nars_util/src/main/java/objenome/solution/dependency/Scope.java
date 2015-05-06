package objenome.solution.dependency;

public enum Scope {

    /**
     * The container calls factory.getInstance on every request for the bean,
     * returning always new instances.
     */
    NONE,
    /**
     * The container calls factory.getInstance only once and caches the value,
     * returning always the same instance.
     */
    SINGLETON,
    /**
     * The container calls factory.getInstance and caches the value on a thread
     * local, so the same thread will always get the same instance, at least
     * until the scope is cleared by the client.
     */
    THREAD
}
