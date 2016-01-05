package objenome.solution.dependency;

/**
 * An IoC factory that knows how to create instances and can be configured by
 * accepting values for its constructor and properties (setters). It can also be
 * intercepted right after it creates an instance and right before it releases
 * an instance through the Interceptor interface.
 * 
 * It also supports constructor and property values that are dependencies, in
 * other words, they come from the container itself.
 * 
 * @author sergio.oliveira.jr@gmail.com
 * @see Interceptor
 */
public interface ConfigurableBuilder extends Builder {

	/**
	 * Add a constructor parameter to be used when the bean is instantiated. It
	 * can be called more than once to use constructors with more than one
	 * argument.
	 * 
	 * @param value
	 *            A parameter value to be used by a constructor.
	 * @return The factory itself. (Fluent API)
	 */
	ConfigurableBuilder addInitValue(Object value);

	/**
	 * Add a constructor parameter that is a primitive to be used when the bean
	 * is instantiated. It can be called more than once to use constructors with
	 * more than one argument.<br/>
	 * <br/>
	 * Note: This method is seldom necessary and should be used if and only if
	 * you have a constructor that mixes Wrappers and primitives, like below:<br/>
	 * <br/>
	 * 
	 * <pre>
	 * public AObject(int x, Integer i, Boolean b, boolean f) { ... }
	 * </pre>
	 * 
	 * @param value
	 *            A parameter value to be used by a constructor. Must be a
	 *            primitive that will be autoboxed.
	 * @return The factory itself. (Fluent API)
	 */
	ConfigurableBuilder addInitPrimitive(Object value);

	/**
	 * Add a constructor parameter that is a dependency, in other words, it gets
	 * its value from the container. When the object is created the dependency
	 * will be obtained from the container.
	 * 
	 * @param key
	 *            The key used to get an instance from the container
	 * @return The set of possible builders itself. (Fluent API)
	 */
	ConfigurableBuilder constructorUse(Object key);

	/**
	 * In case you want to force the use of a zero argument constructor and
	 * avoid any ambiguity when choosing the constructor to use.
	 * 
	 * This method is seldom necessary and should be used in the rare cases that
	 * the container cannot correctly determine the constructor you want to use
	 * due to auto-wiring.
	 * 
	 * @return The factory itself. (Fluent API)
	 */
	ConfigurableBuilder useZeroArgumentConstructor();

	/**
	 * Add a property to be injected through a setter when the factory
	 * instantiates an object.
	 * 
	 * @param name
	 *            The property name.
	 * @param value
	 *            The property value.
	 * @return The factory itself. (Fluent API)
	 */
	ConfigurableBuilder addPropertyValue(String name, Object value);

	/**
	 * Add a setter property that is a dependency, in other words, its value
	 * will be obtained from the container.
	 * 
	 * The property name and the dependency name are the same. If they are
	 * different you can use the other version of addPropertyDependency that
	 * accepts both values.
	 * 
	 * @param property
	 *            The dependency name which is equal to the property name.
	 * @return The factory itself. (Fluent API)
	 */
	ConfigurableBuilder addPropertyDependency(String property);

	/**
	 * Add a setter property that is a dependency, in other words, its value
	 * will be obtained from the container.
	 * 
	 * The property name and the dependency name are different.
	 * 
	 * @param property
	 *            The property that will be injected by the container.
	 * @param key
	 *            The dependency name, in other words, the key used to get a
	 *            bean from the container.
	 * @return The factory itself. (Fluent API)
	 */
	ConfigurableBuilder addPropertyDependency(String property, Object key);
}
