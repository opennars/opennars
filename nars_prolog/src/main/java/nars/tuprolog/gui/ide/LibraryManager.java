/*
 * tuProlog - Copyright (C) 2001-2004  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog.gui.ide;

import nars.tuprolog.InvalidLibraryException;
import nars.tuprolog.Library;
import nars.tuprolog.Prolog;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * A dynamic manager for tuProlog libraries.
 *
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.1 - 27-may-05
 */

public final class LibraryManager
{

    /**
	 * The Prolog engine referenced by the Library Manager. 
	 */
    private Prolog engine;
    /**
	 * Stores classnames for managed libraries.
	 */
    private ArrayList<String> libraries;
    private Hashtable<String, URL> externalLibraries = new Hashtable<>();

    public LibraryManager() {
    	libraries = new ArrayList<>();
    }

    /**
	 * Set the engine to be referenced by the library manager.
	 * @param engine  The engine to be referenced by the library manager.
	 */
    public void setEngine(Prolog engine) {
        this.engine = engine;
        initialize();
    }
    
    /**
     * Initialize the repository for managed libraries using the
     * standard libraries which come loaded with the tuProlog engine.
     */
    void initialize() {
            String[] loadedLibraries = engine.getCurrentLibraries();
            for (int i = loadedLibraries.length - 1; i >= 0; i--)
            libraries.add(loadedLibraries[i]);
    }

    /**
	 * Get the engine referenced by the library manager.
	 * @return  the engine referenced by the library manager.
	 */
    public Prolog getEngine() {
        return engine;
    }

    /**
     * Check if a library is loaded into the Prolog engine.
     *
     * @param libraryClassname The complete name of the library class to check.
     * @return true if the library is loaded into the engine, false otherwise.
     */
    public boolean isLibraryLoaded(String libraryClassname) {
        return (engine.getLibrary(libraryClassname) != null);
    }

    /**
     * Add a library to the manager.
     *
     * @param libraryClassname The name of the .class of the library to be added.
     * @throws ClassNotFoundException if the library class cannot be found.
     * @throws InvalidLibraryException if the library is not a valid tuProlog library.
     */
    public void addLibrary(String libraryClassname) throws ClassNotFoundException, InvalidLibraryException {
        if (libraryClassname.isEmpty())
            throw new ClassNotFoundException();
        /** 
         * check for classpath without uppercase at the first char of the last word
         */
        StringTokenizer st=new StringTokenizer(libraryClassname,".");
        String str=null;
        while(st.hasMoreTokens())
            str=st.nextToken();
        if ((str.charAt(0)>'Z') ||(str.charAt(0)<'A'))
            throw new ClassNotFoundException();

//        Class<?> library = getClass().getClassLoader().loadClass(libraryClassname);
//        if (library.getSuperclass().equals(nars.tuprolog.library.class))
//            libraries.add(libraryClassname);
//        else
//            throw new InvalidLibraryException(libraryClassname,-1,-1);
        
        Library lib = null;
        try
        {
        	lib = (Library) Class.forName(libraryClassname).newInstance();
        	libraries.add(lib.getName());
        }
        catch(Exception ex)
        {
        	throw new InvalidLibraryException(libraryClassname,-1,-1);
        }
    }

        /**
	     * Add a library to the manager.
	     *
	     * @param libraryClassname The name of the .class of the library to be added.
	     * @param path The path where is contained the library.
	     * @throws ClassNotFoundException if the library class cannot be found.
	     * @throws InvalidLibraryException if the library is not a valid tuProlog library.
	     */
		public void addLibrary(String libraryClassname, File file) throws ClassNotFoundException, InvalidLibraryException {
	        if (libraryClassname.isEmpty())
	            throw new ClassNotFoundException();
	        /** 
	         * check for classpath without uppercase at the first char of the last word
	         */
	        Library lib = null;
	        try
	        {
	        	String path = file.getPath();
	        	
	        	if(path.contains(".class"))
	        		file = new File(file.getPath().substring(0, file.getPath().lastIndexOf(File.separator) + 1));
	        	
	        	URL url = file.toURI().toURL();
	        	ClassLoader loader = null;
	        	
//	        	// .NET
//	        	if(System.getProperty("java.vm.name").equals("IKVM.NET"))
//	        	{
//	        		Assembly asm = Assembly.LoadFrom(file.getPath());
//	        		loader = new AssemblyCustomClassLoader(asm, new URL[]{url});
//	        		libraryClassname = "cli." + libraryClassname.substring(0, 
//	        				libraryClassname.indexOf(",")).trim();
//	        	}
//	        	// JVM
//	        	else
	        	{
	        		loader = URLClassLoader.newInstance(
	        				new URL[]{ url } ,
	        				getClass().getClassLoader());
	        	}	
	        	
				lib = (Library) Class.forName(libraryClassname, true, loader).newInstance();
				libraries.add(lib.getName());
				externalLibraries.put(libraryClassname, getClassResource(lib.getClass()));
	        }
	        catch(Exception ex)
	        {
	        	throw new InvalidLibraryException(libraryClassname,-1,-1);
	        }
	    }
    
    /**
     * Remove a library to the manager.
     *
     * @param libraryClassname The name of the .class of the library to be removed.
     * @throws ClassNotFoundException if the library class cannot be found.
     * @throws InvalidLibraryException if the library is not a valid tuProlog library.
     */
    public void removeLibrary(String libraryClassname) throws InvalidLibraryException
    {
             libraries.remove(libraryClassname);
    }

    /**
     * Get the libraries managed by the library manager.
     *
     * @return The libraries managed by the library manager as an array of
     * <code>Object</code>s.
     */
    public Object[] getLibraries() {
        return libraries.toArray();
    }

    public void setLibraries(ArrayList<String> libraries)
    {
        this.libraries=libraries;
    }

    public void resetLibraries()
    {
        this.libraries=new ArrayList<>();
    }

    public String toString()
    {
        String result = "";
        Object[] array = getLibraries();
        for (int i=0;i<array.length;i++)
        {
            result=result+array[i]+ '\n';
        }
        return result;
    }

    /**
     * Load a library from the Library Manager into the engine.
     *
     * @param library The library to be loaded into the engine.
     * @throws InvalidLibraryException
     */
    public void loadLibrary(String library) throws InvalidLibraryException {
        engine.loadLibrary(library);
    }
    
    /**
     * Load a library from the Library Manager into the engine.
     *
     * @param library The library to be loaded into the engine.
     * @param path The library path where is contained the library.
     * @throws InvalidLibraryException
     */
    public void loadLibrary(String library, File file) throws InvalidLibraryException {
        engine.loadLibrary(library, new String[] { file.getAbsolutePath()});
    }
    
    

    /**
     * Unload a library from the Library Manager out of the engine.
     *
     * @param library The library to be unloaded out of the engine.
     * @throws InvalidLibraryException
     * @throws EngineRunningException
     */
    public void unloadLibrary(String library) throws InvalidLibraryException {
        engine.unloadLibrary(library);
    }
    
    public void unloadExternalLibrary(String library) throws InvalidLibraryException {
    	if(externalLibraries.containsKey(library))
			externalLibraries.remove(library);
    	if(engine.getLibrary(library) != null)
    		engine.unloadLibrary(library);
    }
    
    /**
     * Check if a library is contained in the manager.
     * 
     * @param library The name of the library we want to check the load status on.
     * @since 1.3.0
     */
    public boolean contains(String library) {
            return libraries.contains(library);
    }
    
    public synchronized URL getExternalLibraryURL(String name)
	{
		return isExternalLibrary(name) ? externalLibraries.get(name) : null;
	}
	
	public synchronized boolean isExternalLibrary(String name)
	{
		return externalLibraries.containsKey(name);
	}
    
    private static URL getClassResource(Class<?> klass) 
	{
		if(klass == null)
			return null;
		return klass.getClassLoader().getResource(
				klass.getName().replace('.', '/') + ".class");
	}

} // end LibraryManager class