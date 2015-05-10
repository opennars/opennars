/*
 * Created on 1-ott-2005
 *
 */
package nars.tuprolog;

import nars.nal.term.Term;
import nars.tuprolog.event.LibraryEvent;
import nars.tuprolog.event.WarningEvent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Alex Benini
 * 
 */
public class Libraries
{

	/* dynamically loaded built-in libraries */
	private final ArrayList<Library> libraries = new ArrayList();

	/*  */
	private final Prolog prolog;
	private final Map<String, URL> externals = new HashMap();

	/**
	 * @author Alessio Mercurio
	 * 
	 * This is the directory where optimized dex files should be written.
	 * Is required to the DexClassLoader.
	 */
	private String optimizedDirectory;


	/**
	 * Config this Manager
	 */
	public Libraries(Prolog vm)
	{
		this.prolog = vm;
	}

	/**
	 * Loads a library.
	 * 
	 * If a library with the same name is already present, a warning event is
	 * notified and the request is ignored.
	 * 
	 * @param the
	 *            name of the Java class containing the library to be loaded
	 * @return the reference to the Library just loaded
	 * @throws InvalidLibraryException
	 *             if name is not a valid library
	 */
	public synchronized Library load(String className)
			throws InvalidLibraryException
	{
		Library lib = null;
		try
		{
			lib = (Library) Class.forName(className).newInstance();
			String name = lib.getName();
			Library alib = getLibrary(name);
			if (alib != null)
			{
				if (prolog.isWarning())
				{
					String msg = "library " + alib.getName()
							+ " already loaded.";
					prolog.notifyWarning(new WarningEvent(prolog, msg));
				}
				return alib;
			}
		} catch (Exception ex)
		{
			throw new InvalidLibraryException(className, -1, -1);
		}
		bindLibrary(lib);
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
		return lib;

	}

	/**
	 * Loads a library.
	 * 
	 * If a library with the same name is already present, a warning event is
	 * notified and the request is ignored.
	 * 
	 * @param the
	 *            name of the Java class containing the library to be loaded
	 * @param the
	 *            list of the paths where the library may be contained
	 * @return the reference to the Library just loaded
	 * @throws InvalidLibraryException
	 *             if name is not a valid library
	 */
	public synchronized Library load(String className, String[] paths)
			throws InvalidLibraryException
	{
		Library lib = null;
		URL[] urls = null;
		ClassLoader loader = null;
		String dexPath;

		try
		{
			/**
			 * @author Alessio Mercurio
			 * 
			 * Dalvik Virtual Machine
			 */
			if (System.getProperty("java.vm.name").equals("Dalvik"))
			{
				/*
				 * Only the first path is used. Dex file doesn't contain .class files 
				 * and therefore getResource() method can't be used to locate the files at runtime.
				 */
				
				dexPath = paths[0];

				/**
				 * Description of DexClassLoader
			     * A class loader that loads classes from .jar files containing a classes.dex entry. 
			     * This can be used to execute code not installed as part of an application.
			     * @param dexPath jar file path where is contained the library.
			     * @param optimizedDirectory directory where optimized dex files should be written; must not be null
			     * @param libraryPath the list of directories containing native libraries, delimited by File.pathSeparator; may be null
			     * @param parent the parent class loader
			     */
				/**
				 * Here before we were using directly the class DexClassLoader referencing android.jar that
				 * contains all the stub classes of Android.
				 * This caused the need to have the file android.jar in the classpath even during the execution
				 * on the Java SE platform even if it is clearly useless. Therefore we decided to remove this 
				 * reference and instantiate the DexClassLoader through reflection.
				 * This is simplified by the fact that, a part the constructor, we do not use any specific method 
				 * of DexClassLoader but we use it as any other ClassLoader.
				 * A similar approach has been adopted also in the class AndroidDynamicClassLoader.
				 */
				loader = (ClassLoader) Class.forName("dalvik.system.DexClassLoader")
											.getConstructor(String.class, String.class, String.class, ClassLoader.class)
											.newInstance(dexPath, this.getOptimizedDirectory(), null, getClass().getClassLoader());
				lib = (Library) Class.forName(className, true, loader).newInstance();
			} else
			{
				urls = new URL[paths.length];

				for (int i = 0; i < paths.length; i++)
				{
					File file = new File(paths[i]);
					if (paths[i].contains(".class"))
						file = new File(paths[i].substring(0,
								paths[i].lastIndexOf(File.separator) + 1));
					urls[i] = (file.toURI().toURL());
				}
				// JVM
				if (!System.getProperty("java.vm.name").equals("IKVM.NET"))
				{
					loader = URLClassLoader.newInstance(urls, getClass()
							.getClassLoader());
					lib = (Library) Class.forName(className, true, loader)
							.newInstance();
				} 
                                //else {			// .NET
//				{
//					Assembly asm = null;
//					boolean classFound = false;
//					className = "cli."
//							+ className.substring(0, className.indexOf(","))
//									.trim();
//					for (int i = 0; i < paths.length; i++)
//					{
//						try
//						{
//							asm = Assembly.LoadFrom(paths[i]);
//							loader = new AssemblyCustomClassLoader(asm, urls);
//							lib = (Library) Class.forName(className, true, loader).newInstance();
//							if (lib != null)
//							{
//								classFound = true;
//								break;
//							}
//						} catch (Exception e)
//						{
//							e.printStackTrace();
//							continue;
//						}
//					}
//					if (!classFound)
//						throw new InvalidLibraryException(className, -1, -1);
//				}
			}

			String name = lib.getName();
			Library alib = getLibrary(name);
			if (alib != null)
			{
				if (prolog.isWarning())
				{
					String msg = "library " + alib.getName()
							+ " already loaded.";
					prolog.notifyWarning(new WarningEvent(prolog, msg));
				}
				return alib;
			}
		} catch (Exception ex)
		{
			throw new InvalidLibraryException(className, -1, -1);
		}
		
		/**
		 * @author Alessio Mercurio
		 * 
		 * Dalvik Virtual Machine
		 */
		if(System.getProperty("java.vm.name").equals("Dalvik"))
		{
			try
			{
				/* 
				 * getResource() can't be used with dex files.  
				 */
				
				File file = new File(paths[0]);
				URL url = (file.toURI().toURL());
				externals.put(className, url);
			} 
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			externals.put(className, getClassResource(lib.getClass()));
		}
		
		bindLibrary(lib);
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
		return lib;
	}

	/**
	 * Loads a specific instance of a library.
	 * 
	 * If a library of the same class is already present, a warning event is
	 * notified. Then, the current instance of that library is discarded, and
	 * the new instance gets loaded.
	 * 
	 * @param lib
	 *            the (Java class) name of the library to be loaded
	 * @throws InvalidLibraryException
	 *             if name is not a valid library
	 */
	public synchronized void load(Library lib)
			throws InvalidLibraryException
	{
		String name = lib.getName();
		Library alib = getLibrary(name);
		if (alib != null)
		{
			if (prolog.isWarning())
			{
				String msg = "library " + alib.getName() + " already loaded.";
				prolog.notifyWarning(new WarningEvent(prolog, msg));
			}
			unloadLibrary(name);
		}
		bindLibrary(lib);
		LibraryEvent ev = new LibraryEvent(prolog, lib.getName());
		prolog.notifyLoadedLibrary(ev);
	}

	/**
	 * Gets the list of current libraries loaded
	 * 
	 * @return the list of the library names
	 */
	public synchronized String[] getLibraries()
	{
		String[] libs = new String[libraries.size()];
		for (int i = 0; i < libs.length; i++)
		{
			libs[i] = libraries.get(i).getName();
		}
		return libs;
	}

	/**
	 * Unloads a previously loaded library
	 * 
	 * @param name
	 *            of the library to be unloaded
	 * @throws InvalidLibraryException
	 *             if name is not a valid loaded library
	 */
	public synchronized void unloadLibrary(String name)
			throws InvalidLibraryException
	{
		boolean found = false;
		Iterator<Library> it = libraries.listIterator();
		while (it.hasNext())
		{
			Library lib = it.next();
			if (lib.getName().equals(name))
			{
				found = true;
				it.remove();
				lib.dismiss();
				getPrimitives().deletePrimitiveInfo(lib);
				break;
			}
		}
		if (!found)
		{
			throw new InvalidLibraryException();
		}
		if (externals.containsKey(name))
			externals.remove(name);
		getTheories().removeLibraryTheory(name);
		getTheories().rebindPrimitives();
		LibraryEvent ev = new LibraryEvent(prolog, name);
		prolog.notifyUnloadedLibrary(ev);
	}

	/**
	 * Binds a library.
	 * 
	 * @param lib
	 *            is library object
	 * @return the reference to the Library just loaded
	 * @throws InvalidLibraryException
	 *             if name is not a valid library
	 */
	private Library bindLibrary(Library lib) throws InvalidLibraryException
	{
		try
		{
			String name = lib.getName();
			lib.setEngine(prolog);
			libraries.add(lib);
			// set primitives
			getPrimitives().createPrimitiveInfo(lib);
			// set theory
			String th = lib.getTheory();
			if (th != null)
			{
				getTheories().consult(new Theory(th), false, name);
				getTheories().solveTheoryGoal();
			}
			// in current theory there could be predicates and functors
			// which become builtins after lib loading
			getTheories().rebindPrimitives();
			//
			return lib;
		} catch (InvalidTheoryException ex)
		{
			System.out.println(ex.getMessage());
			System.out.println("line "+ex.line+"  "+ex.pos);
			throw new InvalidLibraryException(lib.getName(), ex.line, ex.pos);
		} catch (Exception ex)
		{
			 ex.printStackTrace();
			throw new InvalidLibraryException(lib.getName(), -1, -1);
		}

	}

	/**
	 * Gets the reference to a loaded library
	 * 
	 * @param name
	 *            the name of the library already loaded
	 * @return the reference to the library loaded, null if the library is not
	 *         found
	 */
	public synchronized Library getLibrary(String name)
	{
		for (Library alib : libraries)
		{
			if (alib.getName().equals(name))
			{
				return alib;
			}
		}
		return null;
	}

	public synchronized void onSolveBegin(Term g)
	{
		for (Library alib : libraries)
		{
			alib.onSolveBegin(g);
		}
	}

	public synchronized void onSolveHalt()
	{
		for (Library alib : libraries)
		{
			alib.onSolveHalt();
		}
	}

	public synchronized void onSolveEnd()
	{
		for (Library alib : libraries)
		{
			alib.onSolveEnd();
		}
	}

	public synchronized URL getExternalLibraryURL(String name)
	{
		return isExternalLibrary(name) ? externals.get(name) : null;
	}

	public synchronized boolean isExternalLibrary(String name)
	{
		return externals.containsKey(name);
	}

	private static URL getClassResource(Class<?> klass)
	{
		if (klass == null)
			return null;
		return klass.getClassLoader().getResource(
				klass.getName().replace('.', '/') + ".class");
	}

	/**
	 * @author Alessio Mercurio
	 * 
	 * Used to set optimized directory required by the DexClassLoader.
	 * The directory is created Android side.
	 */
	
	public void setOptimizedDirectory(String optimizedDirectory)
	{
		this.optimizedDirectory = optimizedDirectory;
	}
	
	public String getOptimizedDirectory()
	{
		return optimizedDirectory;
	}



	public Theories getTheories() {
		return prolog.getTheories();
	}


	public Primitives getPrimitives() {
		return prolog.getPrimitives();
	}

}