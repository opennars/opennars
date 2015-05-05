package nars.tuprolog.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class JavaDynamicClassLoader extends AbstractDynamicClassLoader
{
	public JavaDynamicClassLoader()
	{
		super();
	}
	
	public JavaDynamicClassLoader(URL[] urls)
	{
		super(urls);
	}
	
	public JavaDynamicClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}
	
	public Class<?> findClass(String className) throws ClassNotFoundException 
	{  
	    Class<?> result = null;  
	    String classNameReplaced = className.replace(".", File.separator);
	    
	    result = classCache.get(className);
	    if (result != null)  
	        return result;  
	    try {
			return findSystemClass(className);
		} catch (ClassNotFoundException e) {
			
		} 
	    for (URL aURL : listURLs) {
	    	try {
	    		InputStream is = null;
	    		byte[] classByte = null;
	    		
	    		
	    		if(aURL.toString().endsWith(".jar"))
	    		{
	    			aURL = new URL("jar", "", aURL + "!/" + classNameReplaced + ".class");
	    			is = aURL.openConnection().getInputStream();
	    		}
	    		
	    		if(aURL.toString().indexOf('/', aURL.toString().length() - 1) != -1)
	    		{
	    			aURL = new URL(aURL.toString() + classNameReplaced + ".class");
	    			is = aURL.openConnection().getInputStream();
	    		}
	    		
	    		classByte = getClassData(is);
	            try {
	            	result = defineClass(className, classByte, 0, classByte.length, null);  
	        		classCache.put(className, result);
	        		
				} catch (SecurityException e) {
					result = super.loadClass(className);
				}
	            return result;  
	    	} catch (Exception e) {
//	    		e.printStackTrace();
	    	}
	    }
	    throw new ClassNotFoundException(className);
	}  
	
	private byte[] getClassData(InputStream is) throws IOException
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		int nextValue= is.read();  
        while (-1 != nextValue) {  
            byteStream.write(nextValue);  
            nextValue = is.read();  
        }
        is.close();
        return byteStream.toByteArray();
	}
	
}
