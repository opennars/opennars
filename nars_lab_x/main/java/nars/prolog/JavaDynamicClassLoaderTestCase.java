package prolog;

import nars.tuprolog.util.JavaDynamicClassLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JavaDynamicClassLoader Test Case
 * 
 * @author Alessio Mercurio
 * 
 */
public class JavaDynamicClassLoaderTestCase {
	
	final static int PATHS_NUMBER = 2;
	String[] paths = new String[PATHS_NUMBER];
	
	@Test
	public void ConstructorTest() throws MalformedURLException, IOException, ClassNotFoundException{
		JavaDynamicClassLoader loader = new JavaDynamicClassLoader();
		assertNotNull(loader);
		
		setPath(true);
		URL[] urls = getURLsFromStringArray(paths);
		loader = new JavaDynamicClassLoader(urls, this.getClass().getClassLoader());
		assertEquals(2, loader.getURLs().length);
	}
	
	@Test 
	public void LoadClassTest() throws MalformedURLException, 
		IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException
	{
		JavaDynamicClassLoader loader = null;
		setPath(true);
		URL[] urls = getURLsFromStringArray(paths);
		loader = new JavaDynamicClassLoader(urls, this.getClass().getClassLoader());
		assertEquals(2, loader.getURLs().length);
		
		Class<?> cl = TestCounter.class; //loader.loadClass("TestCounter");
		assertNotNull(cl);
		Method m = cl.getMethod("inc", new Class[]{});
		m.setAccessible(true);
		Object obj = cl.newInstance();
		m.invoke(obj, new Object[]{});
		Method m1 = cl.getMethod("getValue", new Class[]{});
		m1.setAccessible(true);
		Object res_obj = m1.invoke(obj, new Object[]{});
		int res = Integer.parseInt(res_obj.toString());
		assertEquals(1, res);
	}
	
	@Test(expected = ClassNotFoundException.class)
	public void LoadClassNotFoundTest() throws ClassNotFoundException, IOException
	{
		JavaDynamicClassLoader loader = null;
		setPath(true);
		URL[] urls = getURLsFromStringArray(paths);
		loader = new JavaDynamicClassLoader(urls, this.getClass().getClassLoader());
		loader.loadClass("ClassNotFound");
	}
	
	@Test(expected = ClassNotFoundException.class)
	public void InvalidPathTest() throws ClassNotFoundException, IOException
	{
		JavaDynamicClassLoader loader = null;
		URL url = new File(".").toURI().toURL();
		loader = new JavaDynamicClassLoader(new URL[]{url}, this.getClass().getClassLoader());
		loader.loadClass("Counter");
	}
	
	@Test
	public void URLHandling() throws ClassNotFoundException, MalformedURLException, IOException
	{
		JavaDynamicClassLoader loader = null;
		URL url = new File(".").toURI().toURL();
		loader = new JavaDynamicClassLoader(new URL[]{url}, this.getClass().getClassLoader());
		assertEquals(1,  loader.getURLs().length);
		loader.removeURL(url);
		assertEquals(0, loader.getURLs().length);
		setPath(true);
		loader.addURLs(getURLsFromStringArray(paths));
		assertEquals(2,  loader.getURLs().length);
		/*loader.loadClass(TestCounter.class);
		assertEquals(1, loader.getLoadedClasses().length);*/
	}
	
	@Test(expected = ClassNotFoundException.class)
	public void TestNestedPackage() throws ClassNotFoundException, IOException
	{
		JavaDynamicClassLoader loader = null;
		File file = new File(".");
		String tempPath = file.getCanonicalPath()
			+ File.separator + "test"
			+ File.separator + "unit" 
			+ File.separator + "TestURLClassLoaderNestedPackage.jar";
		URL[] urls = getURLsFromStringArray(new String[]{tempPath});
		loader = new JavaDynamicClassLoader(urls, this.getClass().getClassLoader());
		Class<?> cl = loader.loadClass("acme.corp.Counter");
		assertNotNull(cl);
		cl = loader.loadClass("java.lang.String");
		assertNotNull(cl);
		loader.removeAllURLs();
		//cl = loader.loadClass("Counter");
	}
	
	private void setPath(boolean valid) throws IOException
	{
		File file = new File(".");
		// Array paths contains a valid path
		if(valid)
		{
			paths[0] = file.getCanonicalPath()
				+ File.separator + "test"
				+ File.separator + "unit" 
				+ File.separator + "TestURLClassLoader.jar";
		}
		paths[1] = file.getCanonicalPath();
	}
	
	private URL[] getURLsFromStringArray(String[] paths) throws MalformedURLException  
    {
    	URL[] urls = new URL[paths.length];
		
		for (int i = 0; i < paths.length; i++) 
		{
			File directory = new File(paths[i]);
			urls[i] = (directory.toURI().toURL());
		}
		return urls;
    }
}
