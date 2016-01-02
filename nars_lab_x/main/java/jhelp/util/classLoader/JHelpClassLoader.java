/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.classLoader<br>
 * Class : JHelpClassLoader<br>
 * Date : 26 mai 2010<br>
 * By JHelp
 */
package jhelp.util.classLoader;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.list.EnumerationIterator;
import jhelp.util.text.UtilText;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Loader of class, can add several other class loader and also individual .class file<br>
 * <br>
 * Last modification : 26 mai 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class JHelpClassLoader
      extends ClassLoader
{
   /** File list */
   private ArrayList<File>             files;
   /** Already loaded classes */
   private Hashtable<String, Class<?>> loadedClass;
   /** Class loaders */
   private ArrayList<ClassLoader>      loaders;

   /**
    * Constructs JHelpClassLoader
    */
   public JHelpClassLoader()
   {
      this.initialize();
   }

   /**
    * Constructs JHelpClassLoader
    * 
    * @param parent
    *           Class loader parent
    */
   public JHelpClassLoader(final ClassLoader parent)
   {
      super(parent);

      this.initialize();
   }

   /**
    * Initialize the loader
    */
   private void initialize()
   {
      this.files = new ArrayList<File>();
      this.loaders = new ArrayList<ClassLoader>();
      this.loadedClass = new Hashtable<String, Class<?>>();
   }

   /**
    * Load a class
    * 
    * @param name
    *           Class complete name
    * @param resolve
    *           Indicates if need to resolve
    * @return Loaded class
    * @throws ClassNotFoundException
    *            If class is not found
    * @see ClassLoader#loadClass(String, boolean)
    */
   @Override
   protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
   {
      Debug.println(DebugLevel.VERBOSE, "JHelpClassLoader -> loadClass : ", name);

      Class<?> clazz = this.loadedClass.get(name);

      if(clazz != null)
      {
         return clazz;
      }

      final String[] path = UtilText.cutSringInPart(name, '.');
      path[path.length - 1] += ".class";

      File file = null;
      File tempFile;
      int index;

      for(final File f : this.files)
      {
         tempFile = f;
         index = path.length - 1;

         while((tempFile != null) && (tempFile.getName().equals(path[index]) == true))
         {
            tempFile = tempFile.getParentFile();
            index--;

            if(index < 0)
            {
               file = f;
               break;
            }
         }

         if(file != null)
         {
            break;
         }
      }

      if(file != null)
      {
         try
         {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            InputStream inputStream = new FileInputStream(file);
            byte[] temp = new byte[4096];

            int read = inputStream.read(temp);
            while(read >= 0)
            {
               byteArrayOutputStream.write(temp, 0, read);

               read = inputStream.read(temp);
            }

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            inputStream.close();
            inputStream = null;

            temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream = null;

            clazz = this.defineClass(name, temp, 0, temp.length);
            temp = null;

            if(resolve == true)
            {
               this.resolveClass(clazz);
            }

            this.loadedClass.put(name, clazz);

            return clazz;
         }
         catch(final Exception exception)
         {
            Debug.printException(exception);
         }
      }

      for(final ClassLoader classLoader : this.loaders)
      {
         try
         {
            clazz = classLoader.loadClass(name);

            if(clazz != null)
            {
               this.loadedClass.put(name, clazz);

               return clazz;
            }
         }
         catch(final Exception exception)
         {
            Debug.printException(exception);
         }
      }

      clazz = super.loadClass(name, resolve);
      if(clazz != null)
      {
         this.loadedClass.put(name, clazz);

         return clazz;
      }

      throw new ClassNotFoundException("Can't find : " + name);
   }

   /**
    * Add class loader
    * 
    * @param classLoader
    *           Class loader to add
    */
   public void add(final ClassLoader classLoader)
   {
      if(this.loaders.contains(classLoader) == false)
      {
         this.loaders.add(classLoader);
      }
   }

   /**
    * Add a file. <br>
    * The file must be in the same hierarchy as its package.<br>
    * for exemple for : pack1.pack2.pack3.MyClasss the path must end like this : .../pack1/pack2/pack3/MyClass.class
    * 
    * @param file
    *           File to add
    */
   public void add(final File file)
   {
      if((file.exists() == true) && (this.files.contains(file) == false))
      {
         this.files.add(file);
      }
   }

   /**
    * URL for a resources
    * 
    * @param name
    *           Resource complete name
    * @return URL of resource or {@code null} if not found
    * @see ClassLoader#getResource(String)
    */
   @Override
   public URL getResource(final String name)
   {
      final String[] path = UtilText.cutSringInPart(name, '.');
      path[path.length - 1] += ".class";

      File file = null;
      File tempFile;
      int index;

      for(final File f : this.files)
      {
         tempFile = f;
         index = path.length - 1;

         if(tempFile.getName().equals(path[index]) == true)
         {
            while((tempFile != null) && (tempFile.getName().equals(path[index]) == true))
            {
               tempFile = tempFile.getParentFile();
               index--;

               if(index < 0)
               {
                  file = f;
                  break;
               }
            }

            if(file != null)
            {
               break;
            }
         }
      }

      if(file != null)
      {
         try
         {
            return file.toURI().toURL();
         }
         catch(final MalformedURLException e)
         {
            Debug.printException(e);
         }
      }

      URL url = null;

      for(final ClassLoader classLoader : this.loaders)
      {
         url = classLoader.getResource(name);

         if(url != null)
         {
            return url;
         }
      }

      return super.getResource(name);
   }

   /**
    * Get resource as stream for read
    * 
    * @param name
    *           Resource complete name
    * @return Stream for read or {@code null} if not found
    * @see ClassLoader#getResourceAsStream(String)
    */
   @Override
   public InputStream getResourceAsStream(final String name)
   {
      try
      {
         return this.getResource(name).openStream();
      }
      catch(final Exception e)
      {
         Debug.printException(e);

         return null;
      }
   }

   /**
    * List of resources of same name
    * 
    * @param name
    *           Resource complete name
    * @return List of resource
    * @throws IOException
    *            On reading problem
    * @see ClassLoader#getResources(String)
    */
   @Override
   public Enumeration<URL> getResources(final String name) throws IOException
   {
      final ArrayList<URL> urls = new ArrayList<URL>();

      final String[] path = UtilText.cutSringInPart(name, '.');
      path[path.length - 1] += ".class";

      File tempFile;
      int index;

      for(final File f : this.files)
      {
         tempFile = f;
         index = path.length - 1;

         if(tempFile.getName().equals(path[index]) == true)
         {
            while((tempFile != null) && (tempFile.getName().equals(path[index]) == true))
            {
               tempFile = tempFile.getParentFile();
               index--;

               if(index < 0)
               {
                  try
                  {
                     urls.add(f.toURI().toURL());
                  }
                  catch(final MalformedURLException e)
                  {
                     Debug.printException(e);
                  }

                  break;
               }
            }
         }
      }

      for(final ClassLoader classLoader : this.loaders)
      {
         for(final URL u : new EnumerationIterator<URL>(classLoader.getResources(name)))
         {
            urls.add(u);
         }
      }

      for(final URL u : new EnumerationIterator<URL>(super.getResources(name)))
      {
         urls.add(u);
      }

      return new EnumerationIterator<URL>(urls.iterator());
   }

   /**
    * Un load a class
    * 
    * @param name
    *           Class complete name
    */
   public void unloadClass(final String name)
   {
      this.loadedClass.remove(name);
   }
}