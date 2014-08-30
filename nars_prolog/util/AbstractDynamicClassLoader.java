package alice.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * 
 * @author Alessio Mercurio
 * 
 * Custom abstract classloader used to add/remove dynamically URLs from it
 * needed by JavaLibrary.
 *
 */

public abstract class AbstractDynamicClassLoader extends ClassLoader
{
	protected ArrayList<URL> listURLs = null;
	protected Hashtable<String, Class<?>> classCache = new Hashtable<String, Class<?>>();
	
	public AbstractDynamicClassLoader()
	{
		super(AbstractDynamicClassLoader.class.getClassLoader());
		listURLs = new ArrayList<URL>();
	}
	
	public AbstractDynamicClassLoader(URL[] urls)
	{
		super(AbstractDynamicClassLoader.class.getClassLoader());
		listURLs = new ArrayList<URL>(Arrays.asList(urls));
	}
	
	public AbstractDynamicClassLoader(URL[] urls, ClassLoader parent)
	{
		super(parent);
		listURLs = new ArrayList<URL>(Arrays.asList(urls));
	}
	
	public Class<?> loadClass(String className) throws ClassNotFoundException {  
        return findClass(className);  
	}
		
	public void addURLs(URL[] urls) throws MalformedURLException
	{
		if(urls == null)
			throw new IllegalArgumentException("Array URLs must not be null.");
		for (URL url : urls) {
			if(!listURLs.contains(url))
				listURLs.add(url);
		}
	}
	
	public void removeURL(URL url) throws IllegalArgumentException
	{
		if(!listURLs.contains(url))
			throw new IllegalArgumentException("URL: " + url + "not found.");
		listURLs.remove(url);
	}
	
	public void removeURLs(URL[] urls) throws IllegalArgumentException
	{
		if(urls == null)
			throw new IllegalArgumentException("Array URLs must not be null.");
		for (URL url : urls) {
			if(!listURLs.contains(url))
				throw new IllegalArgumentException("URL: " + url + "not found.");
			listURLs.remove(url);
		}
	}
	
	public void removeAllURLs()
	{
		if(!listURLs.isEmpty())
			listURLs.clear();
	}

	public URL[] getURLs()
	{
		URL[] result = new URL[listURLs.size()];
		listURLs.toArray(result);
		return result;
	}

	public Class<?>[] getLoadedClasses()
	{
		Class<?>[] result = new Class<?>[classCache.size()];
		int i = 0;
		for (Class<?> aClass : classCache.values()) {
			result[i] = aClass;
		}
		return result;
	}
	
	public void clearCache()
	{
		classCache.clear();
	}

	public void removeClassCacheEntry(String className)
	{
		classCache.remove(className);
		
	}

	public void setClassCacheEntry(Class<?> cls)
	{
		if(classCache.contains(cls))
			classCache.remove(cls.getName());
		classCache.put(cls.getName(), cls);
	}

}
