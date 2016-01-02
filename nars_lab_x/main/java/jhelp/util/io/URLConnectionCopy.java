/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.io<br>
 * Class : URLConnectionCopy<br>
 * Date : 15 sept. 2010<br>
 * By JHelp
 */
package jhelp.util.io;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * URL connection able to open a read stream that copy everything it read in same time in a file.<br>
 * It does a copy while reading <br>
 * <br>
 * Last modification : 15 sept. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class URLConnectionCopy
      extends URLConnection
{
   /** Connection base */
   private final URLConnection connection;
   /** File where copy */
   private final File          fileCopy;

   /**
    * Constructs URLConnectionCopy
    * 
    * @param url
    *           URL to read
    * @param fileCopy
    *           File where write the copy
    * @throws IOException
    *            On opening connection issue or the file can't be create
    */
   public URLConnectionCopy(final URL url, final File fileCopy)
         throws IOException
   {
      super(url);

      if(UtilIO.createFile(fileCopy) == false)
      {
         throw new IOException("Can't create the file " + fileCopy.getAbsolutePath());
      }

      this.fileCopy = fileCopy;

      this.connection = url.openConnection();
   }

   /**
    * Adds a general request property specified by a key-value pair. This method will not overwrite existing values associated
    * with the same key.
    * 
    * @param key
    *           the keyword by which the request is known (e.g., " <code>accept</code>").
    * @param value
    *           the value associated with it.
    * @throws IllegalStateException
    *            if already connected
    * @throws NullPointerException
    *            if key is null
    * @see #getRequestProperties()
    * @see URLConnection#addRequestProperty(String, String)
    */
   @Override
   public void addRequestProperty(final String key, final String value)
   {
      this.connection.addRequestProperty(key, value);
   }

   /**
    * Opens a communications link to the resource referenced by this URL, if such a connection has not already been established.
    * <p>
    * If the <code>connect</code> method is called when the connection has already been opened (indicated by the
    * <code>connected</code> field having the value {@code true}), the call is ignored.
    * <p>
    * URLConnection objects go through two phases: first they are created, then they are connected. After being created, and
    * before being connected, various options can be specified (e.g., doInput and UseCaches). After connecting, it is an error
    * to try to set them. Operations that depend on being connected, like getContentLength, will implicitly perform the
    * connection, if necessary.
    * 
    * @throws SocketTimeoutException
    *            if the timeout expires before the connection can be established
    * @exception IOException
    *               if an I/O error occurs while opening the connection.
    * @see #getConnectTimeout()
    * @see #setConnectTimeout(int)
    * @see URLConnection#connect()
    */
   @Override
   public void connect() throws IOException
   {
      this.connection.connect();
   }

   /**
    * Returns the value of the <code>allowUserInteraction</code> field for this object.
    * 
    * @return the value of the <code>allowUserInteraction</code> field for this object.
    * @see #setAllowUserInteraction(boolean)
    * @see URLConnection#getAllowUserInteraction()
    */
   @Override
   public boolean getAllowUserInteraction()
   {
      return this.connection.getAllowUserInteraction();
   }

   /**
    * Returns setting for connect timeout.
    * <p>
    * 0 return implies that the option is disabled (i.e., timeout of infinity).
    * 
    * @return an <code>int</code> that indicates the connect timeout value in milliseconds
    * @see #setConnectTimeout(int)
    * @see #connect()
    * @see URLConnection#getConnectTimeout()
    */
   @Override
   public int getConnectTimeout()
   {
      return this.connection.getConnectTimeout();
   }

   /**
    * Retrieves the contents of this URL connection.
    * <p>
    * This method first determines the content type of the object by calling the <code>getContentType</code> method. If this is
    * the first time that the application has seen that specific content type, a content handler for that content type is
    * created:
    * <ol>
    * <li>If the application has set up a content handler factory instance using the <code>setContentHandlerFactory</code>
    * method, the <code>createContentHandler</code> method of that instance is called with the content type as an argument; the
    * result is a content handler for that content type.
    * <li>If no content handler factory has yet been set up, or if the factory's <code>createContentHandler</code> method
    * returns {@code null}, then the application loads the class named: <blockquote>
    * 
    * <pre>
    *         sun.net.www.content.&lt;&lt;i&gt;contentType&lt;/i&gt;&gt;
    * </pre>
    * 
    * </blockquote> where &lt;<i>contentType</i>&gt; is formed by taking the content-type string, replacing all slash characters
    * with a <code>period</code> ('.'), and all other non-alphanumeric characters with the underscore character '<code>_</code>
    * '. The alphanumeric characters are specifically the 26 uppercase ASCII letters '<code>A</code>' through ' <code>Z</code>',
    * the 26 lowercase ASCII letters '<code>a</code>' through ' <code>z</code>', and the 10 ASCII digits '<code>0</code>'
    * through ' <code>9</code>'. If the specified class does not exist, or is not a subclass of <code>ContentHandler</code>,
    * then an <code>UnknownServiceException</code> is thrown.
    * </ol>
    * 
    * @return the object fetched. The <code>instanceof</code> operator should be used to determine the specific kind of object
    *         returned.
    * @exception IOException
    *               if an I/O error occurs while getting the content.
    * @exception UnknownServiceException
    *               if the protocol does not support the content type.
    * @see java.net.ContentHandlerFactory#createContentHandler(String)
    * @see URLConnection#getContentType()
    * @see URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
    * @see URLConnection#getContent()
    */
   @Override
   public Object getContent() throws IOException
   {
      return this.connection.getContent();
   }

   /**
    * Retrieves the contents of this URL connection.
    * 
    * @param classes
    *           the <code>Class</code> array indicating the requested types
    * @return the object fetched that is the first match of the type specified in the classes array. null if none of the
    *         requested types are supported. The <code>instanceof</code> operator should be used to determine the specific kind
    *         of object returned.
    * @exception IOException
    *               if an I/O error occurs while getting the content.
    * @exception UnknownServiceException
    *               if the protocol does not support the content type.
    * @see URLConnection#getContent()
    * @see java.net.ContentHandlerFactory#createContentHandler(String)
    * @see URLConnection#getContent(Class[])
    * @see URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
    * @see URLConnection#getContent(Class[])
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object getContent(@SuppressWarnings("rawtypes") final Class[] classes) throws IOException
   {
      return this.connection.getContent(classes);
   }

   /**
    * Returns the value of the <code>content-encoding</code> header field.
    * 
    * @return the content encoding of the resource that the URL references, or {@code null} if not known.
    * @see URLConnection#getHeaderField(String)
    * @see URLConnection#getContentEncoding()
    */
   @Override
   public String getContentEncoding()
   {
      return this.connection.getContentEncoding();
   }

   /**
    * Returns the value of the <code>content-length</code> header field.
    * 
    * @return the content length of the resource that this connection's URL references, or <code>-1</code> if the content length
    *         is not known.
    * @see URLConnection#getContentLength()
    */
   @Override
   public int getContentLength()
   {
      return this.connection.getContentLength();
   }

   /**
    * Returns the value of the <code>content-type</code> header field.
    * 
    * @return the content type of the resource that the URL references, or {@code null} if not known.
    * @see URLConnection#getHeaderField(String)
    * @see URLConnection#getContentType()
    */
   @Override
   public String getContentType()
   {
      return this.connection.getContentType();
   }

   // /**
   // * @return
   // * @see java.net.URLConnection#getContentLengthLong()
   // */
   // public long getContentLengthLong()
   // {
   // return this.connection.getContentLengthLong();
   // }

   /**
    * Returns the value of the <code>date</code> header field.
    * 
    * @return the sending date of the resource that the URL references, or <code>0</code> if not known. The value returned is
    *         the number of milliseconds since January 1, 1970 GMT.
    * @see URLConnection#getHeaderField(String)
    * @see URLConnection#getDate()
    */
   @Override
   public long getDate()
   {
      return this.connection.getDate();
   }

   /**
    * Returns the default value of a <code>URLConnection</code>'s <code>useCaches</code> flag.
    * <p>
    * Ths default is "sticky", being a part of the static state of all URLConnections. This flag applies to the next, and all
    * following URLConnections that are created.
    * 
    * @return the default value of a <code>URLConnection</code>'s <code>useCaches</code> flag.
    * @see #setDefaultUseCaches(boolean)
    * @see URLConnection#getDefaultUseCaches()
    */
   @Override
   public boolean getDefaultUseCaches()
   {
      return this.connection.getDefaultUseCaches();
   }

   /**
    * Returns the value of this <code>URLConnection</code>'s <code>doInput</code> flag.
    * 
    * @return the value of this <code>URLConnection</code>'s <code>doInput</code> flag.
    * @see #setDoInput(boolean)
    * @see URLConnection#getDoInput()
    */
   @Override
   public boolean getDoInput()
   {
      return this.connection.getDoInput();
   }

   /**
    * Returns the value of this <code>URLConnection</code>'s <code>doOutput</code> flag.
    * 
    * @return the value of this <code>URLConnection</code>'s <code>doOutput</code> flag.
    * @see #setDoOutput(boolean)
    * @see URLConnection#getDoOutput()
    */
   @Override
   public boolean getDoOutput()
   {
      return this.connection.getDoOutput();
   }

   /**
    * Returns the value of the <code>expires</code> header field.
    * 
    * @return the expiration date of the resource that this URL references, or 0 if not known. The value is the number of
    *         milliseconds since January 1, 1970 GMT.
    * @see URLConnection#getHeaderField(String)
    * @see URLConnection#getExpiration()
    */
   @Override
   public long getExpiration()
   {
      return this.connection.getExpiration();
   }

   /**
    * Returns the value for the <code>n</code><sup>th</sup> header field. It returns {@code null} if there are fewer than
    * <code>n+1</code>fields.
    * <p>
    * This method can be used in conjunction with the {@link #getHeaderFieldKey(int) getHeaderFieldKey} method to iterate
    * through all the headers in the message.
    * 
    * @param n
    *           an index, where n>=0
    * @return the value of the <code>n</code><sup>th</sup> header field or {@code null} if there are fewer than
    *         <code>n+1</code> fields
    * @see URLConnection#getHeaderFieldKey(int)
    * @see URLConnection#getHeaderField(int)
    */
   @Override
   public String getHeaderField(final int n)
   {
      return this.connection.getHeaderField(n);
   }

   /**
    * Returns the value of the named header field.
    * <p>
    * If called on a connection that sets the same header multiple times with possibly different values, only the last value is
    * returned.
    * 
    * @param name
    *           the name of a header field.
    * @return the value of the named header field, or {@code null} if there is no such field in the header.
    * @see URLConnection#getHeaderField(String)
    */
   @Override
   public String getHeaderField(final String name)
   {
      return this.connection.getHeaderField(name);
   }

   /**
    * Returns the value of the named field parsed as date. The result is the number of milliseconds since January 1, 1970 GMT
    * represented by the named field.
    * <p>
    * This form of <code>getHeaderField</code> exists because some connection types (e.g., <code>http-ng</code>) have pre-parsed
    * headers. Classes for that connection type can override this method and short-circuit the parsing.
    * 
    * @param name
    *           the name of the header field.
    * @param Default
    *           a default value.
    * @return the value of the field, parsed as a date. The value of the <code>Default</code> argument is returned if the field
    *         is missing or malformed.
    * @see URLConnection#getHeaderFieldDate(String, long)
    */
   @Override
   public long getHeaderFieldDate(final String name, final long Default)
   {
      return this.connection.getHeaderFieldDate(name, Default);
   }

   /**
    * Returns the value of the named field parsed as a number.
    * <p>
    * This form of <code>getHeaderField</code> exists because some connection types (e.g., <code>http-ng</code>) have pre-parsed
    * headers. Classes for that connection type can override this method and short-circuit the parsing.
    * 
    * @param name
    *           the name of the header field.
    * @param Default
    *           the default value.
    * @return the value of the named field, parsed as an integer. The <code>Default</code> value is returned if the field is
    *         missing or malformed.
    * @see URLConnection#getHeaderFieldInt(String, int)
    */
   @Override
   public int getHeaderFieldInt(final String name, final int Default)
   {
      return this.connection.getHeaderFieldInt(name, Default);
   }

   /**
    * Returns the key for the <code>n</code><sup>th</sup> header field. It returns {@code null} if there are fewer than
    * <code>n+1</code> fields.
    * 
    * @param n
    *           an index, where n>=0
    * @return the key for the <code>n</code><sup>th</sup> header field, or {@code null} if there are fewer than
    *         <code>n+1</code> fields.
    * @see URLConnection#getHeaderFieldKey(int)
    */
   @Override
   public String getHeaderFieldKey(final int n)
   {
      return this.connection.getHeaderFieldKey(n);
   }

   /**
    * Returns an unmodifiable Map of the header fields. The Map keys are Strings that represent the response-header field names.
    * Each Map value is an unmodifiable List of Strings that represents the corresponding field values.
    * 
    * @return a Map of header fields
    * @see URLConnection#getHeaderFields()
    */
   @Override
   public Map<String, List<String>> getHeaderFields()
   {
      return this.connection.getHeaderFields();
   }

   // /**
   // * @param name
   // * @param Default
   // * @return
   // * @see java.net.URLConnection#getHeaderFieldLong(java.lang.String, long)
   // */
   // public long getHeaderFieldLong(String name, long Default)
   // {
   // return this.connection.getHeaderFieldLong(name, Default);
   // }

   /**
    * Returns the value of this object's <code>ifModifiedSince</code> field.
    * 
    * @return the value of this object's <code>ifModifiedSince</code> field.
    * @see #setIfModifiedSince(long)
    * @see URLConnection#getIfModifiedSince()
    */
   @Override
   public long getIfModifiedSince()
   {
      return this.connection.getIfModifiedSince();
   }

   /**
    * Returns an input stream that reads and copy the reading in the file in same time from this open connection. A
    * SocketTimeoutException can be thrown when reading from the returned input stream if the read timeout expires before data
    * is available for read.
    * 
    * @return an input stream that reads from this open connection.
    * @exception IOException
    *               if an I/O error occurs while creating the input stream.
    * @exception UnknownServiceException
    *               if the protocol does not support input.
    * @see #setReadTimeout(int)
    * @see #getReadTimeout()
    * @see URLConnection#getInputStream()
    */
   @Override
   public InputStream getInputStream() throws IOException
   {
      return new InputStreamCopy(this.connection.getInputStream(), new FileOutputStream(this.fileCopy));
   }

   /**
    * Returns the value of the <code>last-modified</code> header field. The result is the number of milliseconds since January
    * 1, 1970 GMT.
    * 
    * @return the date the resource referenced by this <code>URLConnection</code> was last modified, or 0 if not known.
    * @see URLConnection#getHeaderField(String)
    * @see URLConnection#getLastModified()
    */
   @Override
   public long getLastModified()
   {
      return this.connection.getLastModified();
   }

   /**
    * Returns an output stream that writes to this connection.
    * 
    * @return an output stream that writes to this connection.
    * @exception IOException
    *               if an I/O error occurs while creating the output stream.
    * @exception UnknownServiceException
    *               if the protocol does not support output.
    * @see URLConnection#getOutputStream()
    */
   @Override
   public OutputStream getOutputStream() throws IOException
   {
      return this.connection.getOutputStream();
   }

   /**
    * Returns a permission object representing the permission necessary to make the connection represented by this object. This
    * method returns null if no permission is required to make the connection. By default, this method returns
    * <code>java.security.AllPermission</code>. Subclasses should override this method and return the permission that best
    * represents the permission required to make a a connection to the URL. For example, a <code>URLConnection</code>
    * representing a <code>file:</code> URL would return a <code>java.io.FilePermission</code> object.
    * <p>
    * The permission returned may dependent upon the state of the connection. For example, the permission before connecting may
    * be different from that after connecting. For example, an HTTP sever, say foo.com, may redirect the connection to a
    * different host, say bar.com. Before connecting the permission returned by the connection will represent the permission
    * needed to connect to foo.com, while the permission returned after connecting will be to bar.com.
    * <p>
    * Permissions are generally used for two purposes: to protect caches of objects obtained through URLConnections, and to
    * check the right of a recipient to learn about a particular URL. In the first case, the permission should be obtained
    * <em>after</em> the object has been obtained. For example, in an HTTP connection, this will represent the permission to
    * connect to the host from which the data was ultimately fetched. In the second case, the permission should be obtained and
    * tested <em>before</em> connecting.
    * 
    * @return the permission object representing the permission necessary to make the connection represented by this
    *         URLConnection.
    * @exception IOException
    *               if the computation of the permission requires network or file I/O and an exception occurs while computing
    *               it.
    * @see URLConnection#getPermission()
    */
   @Override
   public Permission getPermission() throws IOException
   {
      return this.connection.getPermission();
   }

   /**
    * Returns setting for read timeout. 0 return implies that the option is disabled (i.e., timeout of infinity).
    * 
    * @return an <code>int</code> that indicates the read timeout value in milliseconds
    * @see #setReadTimeout(int)
    * @see InputStream#read()
    * @see URLConnection#getReadTimeout()
    */
   @Override
   public int getReadTimeout()
   {
      return this.connection.getReadTimeout();
   }

   /**
    * Returns an unmodifiable Map of general request properties for this connection. The Map keys are Strings that represent the
    * request-header field names. Each Map value is a unmodifiable List of Strings that represents the corresponding field
    * values.
    * 
    * @return a Map of the general request properties for this connection.
    * @throws IllegalStateException
    *            if already connected
    * @see URLConnection#getRequestProperties()
    */
   @Override
   public Map<String, List<String>> getRequestProperties()
   {
      return this.connection.getRequestProperties();
   }

   /**
    * Returns the value of the named general request property for this connection.
    * 
    * @param key
    *           the keyword by which the request is known (e.g., "accept").
    * @return the value of the named general request property for this connection. If key is null, then null is returned.
    * @throws IllegalStateException
    *            if already connected
    * @see #setRequestProperty(String, String)
    * @see URLConnection#getRequestProperty(String)
    */
   @Override
   public String getRequestProperty(final String key)
   {
      return this.connection.getRequestProperty(key);
   }

   /**
    * Returns the value of this <code>URLConnection</code>'s <code>URL</code> field.
    * 
    * @return the value of this <code>URLConnection</code>'s <code>URL</code> field.
    * @see URLConnection#getURL()
    */
   @Override
   public URL getURL()
   {
      return this.connection.getURL();
   }

   /**
    * Returns the value of this <code>URLConnection</code>'s <code>useCaches</code> field.
    * 
    * @return the value of this <code>URLConnection</code>'s <code>useCaches</code> field.
    * @see #setUseCaches(boolean)
    * @see URLConnection#getUseCaches()
    */
   @Override
   public boolean getUseCaches()
   {
      return this.connection.getUseCaches();
   }

   /**
    * Set the value of the <code>allowUserInteraction</code> field of this <code>URLConnection</code>.
    * 
    * @param allowuserinteraction
    *           the new value.
    * @throws IllegalStateException
    *            if already connected
    * @see #getAllowUserInteraction()
    * @see URLConnection#setAllowUserInteraction(boolean)
    */
   @Override
   public void setAllowUserInteraction(final boolean allowuserinteraction)
   {
      this.connection.setAllowUserInteraction(allowuserinteraction);
   }

   /**
    * Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced
    * by this URLConnection. If the timeout expires before the connection can be established, a java.net.SocketTimeoutException
    * is raised. A timeout of zero is interpreted as an infinite timeout.
    * <p>
    * Some non-standard implmentation of this method may ignore the specified timeout. To see the connect timeout set, please
    * call getConnectTimeout().
    * 
    * @param timeout
    *           an <code>int</code> that specifies the connect timeout value in milliseconds
    * @throws IllegalArgumentException
    *            if the timeout parameter is negative
    * @see #getConnectTimeout()
    * @see #connect()
    * @see URLConnection#setConnectTimeout(int)
    */
   @Override
   public void setConnectTimeout(final int timeout)
   {

      this.connection.setConnectTimeout(timeout);
   }

   /**
    * Sets the default value of the <code>useCaches</code> field to the specified value.
    * 
    * @param defaultusecaches
    *           the new value.
    * @see #getDefaultUseCaches()
    * @see URLConnection#setDefaultUseCaches(boolean)
    */
   @Override
   public void setDefaultUseCaches(final boolean defaultusecaches)
   {
      this.connection.setDefaultUseCaches(defaultusecaches);
   }

   /**
    * Sets the value of the <code>doInput</code> field for this <code>URLConnection</code> to the specified value.
    * <p>
    * A URL connection can be used for input and/or output. Set the DoInput flag to true if you intend to use the URL connection
    * for input, false if not. The default is true.
    * 
    * @param doinput
    *           the new value.
    * @throws IllegalStateException
    *            if already connected
    * @see #getDoInput()
    * @see URLConnection#setDoInput(boolean)
    */
   @Override
   public void setDoInput(final boolean doinput)
   {
      this.connection.setDoInput(doinput);
   }

   /**
    * Sets the value of the <code>doOutput</code> field for this <code>URLConnection</code> to the specified value.
    * <p>
    * A URL connection can be used for input and/or output. Set the DoOutput flag to true if you intend to use the URL
    * connection for output, false if not. The default is false.
    * 
    * @param dooutput
    *           the new value.
    * @throws IllegalStateException
    *            if already connected
    * @see #getDoOutput()
    * @see URLConnection#setDoOutput(boolean)
    */
   @Override
   public void setDoOutput(final boolean dooutput)
   {
      this.connection.setDoOutput(dooutput);
   }

   /**
    * Sets the value of the <code>ifModifiedSince</code> field of this <code>URLConnection</code> to the specified value.
    * 
    * @param ifmodifiedsince
    *           the new value.
    * @throws IllegalStateException
    *            if already connected
    * @see #getIfModifiedSince()
    * @see URLConnection#setIfModifiedSince(long)
    */
   @Override
   public void setIfModifiedSince(final long ifmodifiedsince)
   {
      this.connection.setIfModifiedSince(ifmodifiedsince);
   }

   /**
    * Sets the read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout when reading from
    * Input stream when a connection is established to a resource. If the timeout expires before there is data available for
    * read, a java.net.SocketTimeoutException is raised. A timeout of zero is interpreted as an infinite timeout.
    * <p>
    * Some non-standard implementation of this method ignores the specified timeout. To see the read timeout set, please call
    * getReadTimeout().
    * 
    * @param timeout
    *           an <code>int</code> that specifies the timeout value to be used in milliseconds
    * @throws IllegalArgumentException
    *            if the timeout parameter is negative
    * @see #getReadTimeout()
    * @see InputStream#read()
    * @see URLConnection#setReadTimeout(int)
    */
   @Override
   public void setReadTimeout(final int timeout)
   {
      this.connection.setReadTimeout(timeout);
   }

   /**
    * Sets the general request property. If a property with the key already exists, overwrite its value with the new value.
    * <p>
    * NOTE: HTTP requires all request properties which can legally have multiple instances with the same key to use a
    * comma-seperated list syntax which enables multiple properties to be appended into a single property.
    * 
    * @param key
    *           the keyword by which the request is known (e.g., " <code>accept</code>").
    * @param value
    *           the value associated with it.
    * @throws IllegalStateException
    *            if already connected
    * @throws NullPointerException
    *            if key is {@code null}
    * @see #getRequestProperty(String)
    * @see URLConnection#setRequestProperty(String, String)
    */
   @Override
   public void setRequestProperty(final String key, final String value)
   {
      this.connection.setRequestProperty(key, value);
   }

   /**
    * Sets the value of the <code>useCaches</code> field of this <code>URLConnection</code> to the specified value.
    * <p>
    * Some protocols do caching of documents. Occasionally, it is important to be able to "tunnel through" and ignore the caches
    * (e.g., the "reload" button in a browser). If the UseCaches flag on a connection is true, the connection is allowed to use
    * whatever caches it can. If false, caches are to be ignored. The default value comes from DefaultUseCaches, which defaults
    * to true.
    * 
    * @param usecaches
    *           a <code>boolean</code> indicating whether or not to allow caching
    * @throws IllegalStateException
    *            if already connected
    * @see #getUseCaches()
    * @see URLConnection#setUseCaches(boolean)
    */
   @Override
   public void setUseCaches(final boolean usecaches)
   {
      this.connection.setUseCaches(usecaches);
   }
}