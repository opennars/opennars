package alice.util;

import java.net.URL;

public class AndroidDynamicClassLoader extends AbstractDynamicClassLoader
{
	private String dexPath;
	private ClassLoader classLoader;
	
	public AndroidDynamicClassLoader()
	{
		super();
	}
	
	public AndroidDynamicClassLoader(URL[] urls)
	{
		super(urls);
	}
	
	public AndroidDynamicClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}
	
	private String createPathString()
	{
		String path;
		
		if(listURLs.isEmpty())
		{
			path = "";
		}
		else
		{
			path = listURLs.get(0).getPath();
			
			for (int i=1; i<listURLs.size(); i++)
			{
				path = path.concat(":" + listURLs.get(i).getPath());
			}
		}
		
		return path;
	}
	
	private void setDexPath(String dexPath)
	{
		this.dexPath = dexPath;
	}

	public Class<?> findClass(String className) throws ClassNotFoundException
	{		
		setDexPath(createPathString());
		
		try {
			/**
			 * More informations on the use of reflection here can be found in the class LibraryManager
			 * in the method loadLibrary(Strin, String[]).
			 */
			classLoader = (ClassLoader) Class.forName("dalvik.system.DexClassLoader")
											 .getConstructor(String.class, String.class, String.class, ClassLoader.class)
											 .newInstance(dexPath, "/data/data/alice.tuprologx.android/app_dex", null, getParent());
		} catch (Exception e) {
			throw new ClassNotFoundException(className);
		}
		
		return classLoader.loadClass(className);
	}

	
}
