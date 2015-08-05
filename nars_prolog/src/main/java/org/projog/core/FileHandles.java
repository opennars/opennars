package org.projog.core;

import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.projog.core.term.TermUtils.getAtomName;

/**
 * Collection of input and output streams.
 * <p>
 * Each {@link KB} has a single unique {@code FileHandles} instance.
 * 
 * @see KnowledgeBaseUtils#getFileHandles(KB)
 */
public final class FileHandles {
   public static final PAtom USER_OUTPUT_HANDLE = new PAtom("user_output");
   public static final PAtom USER_INPUT_HANDLE = new PAtom("user_input");

   private final Object lock = new Object();
   private final Map<String, InputStream> inputHandles = new HashMap<>();
   private final Map<String, PrintStream> outputHandles = new HashMap<>();

   /** Current input used by get_char and read */
   private PTerm currentInputHandle;
   /** Current output used by put_char, nl, write and write_canonical */
   private PTerm currentOutputHandle;

   private InputStream in;
   private PrintStream out;

   FileHandles() {
      PAtom userInputHandle = USER_INPUT_HANDLE;
      PAtom userOutputHandle = USER_OUTPUT_HANDLE;
      inputHandles.put(userInputHandle.getName(), System.in);
      outputHandles.put(userOutputHandle.getName(), System.out);
      setInput(userInputHandle);
      setOutput(userOutputHandle);
   }

   /**
    * Return the {@code Term} representing the current input stream.
    * <p>
    * By default this will be an {@code Atom} with the name "{@code user_input}".
    */
   public PTerm getCurrentInputHandle() {
      return currentInputHandle;
   }

   /**
    * Return the {@code Term} representing the current output stream.
    * <p>
    * By default this will be an {@code Atom} with the name "{@code user_output}".
    */
   public PTerm getCurrentOutputHandle() {
      return currentOutputHandle;
   }

   /**
    * Return the current input stream.
    * <p>
    * By default this will be {@code System.in}.
    */
   public InputStream getCurrentInputStream() {
      return in;
   }

   /**
    * Return the current output stream.
    * <p>
    * By default this will be {@code System.out}.
    */
   public PrintStream getCurrentOutputStream() {
      return out;
   }

   /**
    * Sets the current input stream to the input stream represented by the specified {@code Term}.
    * 
    * @throws ProjogException if the specified {@link PTerm} does not represent an {@link PAtom}
    */
   public void setInput(PTerm handle) {
      String handleName = getAtomName(handle);
      synchronized (lock) {
         if (inputHandles.containsKey(handleName)) {
            currentInputHandle = handle;
            in = inputHandles.get(handleName);
         } else {
            throw new ProjogException("cannot find file input handle with name:" + handleName);
         }
      }
   }

   /**
    * Sets the current output stream to the output stream represented by the specified {@code Term}.
    * 
    * @throws ProjogException if the specified {@link PTerm} does not represent an {@link PAtom}
    */
   public void setOutput(PTerm handle) {
      String handleName = getAtomName(handle);
      synchronized (lock) {
         if (outputHandles.containsKey(handleName)) {
            currentOutputHandle = handle;
            out = outputHandles.get(handleName);
         } else {
            throw new ProjogException("cannot find file output handle with name:" + handleName);
         }
      }
   }

   /**
    * Creates an intput file stream to read from the file with the specified name
    * 
    * @param fileName the system-dependent filename
    * @return a reference to the newly created stream (as required by {@link #setInput(PTerm)} and {@link #close(PTerm)})
    * @throws ProjogException if this object's collection of input streams already includes the specified file
    * @throws IOException if the file cannot be opened for reading
    */
   public PAtom openInput(String fileName) throws IOException {
      String handleName = fileName + "_input_handle";
      synchronized (lock) {
         if (inputHandles.containsKey(handleName)) {
            throw new ProjogException("Can not open input for: " + fileName + " as it is already open");
         } else {
            InputStream is = new FileInputStream(fileName);
            inputHandles.put(handleName, is);
         }
      }
      return new PAtom(handleName);
   }

   /**
    * Creates an output file stream to write to the file with the specified name
    * 
    * @param fileName the system-dependent filename
    * @return a reference to the newly created stream (as required by {@link #setOutput(PTerm)} and {@link #close(PTerm)})
    * @throws ProjogException if this object's collection of output streams already includes the specified file
    * @throws IOException if the file cannot be opened
    */
   public PAtom openOutput(String fileName) throws IOException {
      String handleName = fileName + "_output_handle";
      synchronized (lock) {
         if (outputHandles.containsKey(handleName)) {
            throw new ProjogException("Can not open output for: " + fileName + " as it is already open");
         } else {
            OutputStream os = new FileOutputStream(fileName);
            outputHandles.put(handleName, new PrintStream(os));
         }
      }
      return new PAtom(handleName);
   }

   /**
    * Closes the stream represented by the specified {@code Term}.
    * 
    * @throws ProjogException if the specified {@link PTerm} does not represent an {@link PAtom}
    * @throws IOException if an I/O error occurs
    */
   public void close(PTerm handle) throws IOException {
      String handleName = getAtomName(handle);
      synchronized (lock) {
         PrintStream ps = outputHandles.get(handleName);
         if (ps != null) {
            outputHandles.remove(handleName);
            ps.close();
            return;
         }
         InputStream is = inputHandles.get(handleName);
         if (is != null) {
            inputHandles.remove(handleName);
            is.close();
            return;
         }
      }
   }

   public boolean isHandle(String handle) {
      return inputHandles.containsKey(handle) || outputHandles.containsKey(handle);
   }
}