package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.atom;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.projog.core.term.Atom;
import org.projog.core.term.PTerm;

public class FileHandlesTest {
   @Test
   public void testUserInputHandle() {
      assertEquals("user_input", FileHandles.USER_INPUT_HANDLE.getName());
   }

   @Test
   public void testUserOutputHandle() {
      assertEquals("user_output", FileHandles.USER_OUTPUT_HANDLE.getName());
   }

   @Test
   public void testDefaultInputStream() {
      FileHandles fh = new FileHandles();
      assertSame(System.in, fh.getCurrentInputStream());
   }

   @Test
   public void testDefaultOututStream() {
      FileHandles fh = new FileHandles();
      assertSame(System.out, fh.getCurrentOutputStream());
   }

   @Test
   public void testDefaultInputHandle() {
      FileHandles fh = new FileHandles();
      PTerm expected = new Atom("user_input");
      PTerm actual = fh.getCurrentInputHandle();
      assertTrue(expected.strictEquals(actual));
   }

   @Test
   public void testDefaultOutputHandle() {
      FileHandles fh = new FileHandles();
      PTerm expected = new Atom("user_output");
      PTerm actual = fh.getCurrentOutputHandle();
      assertTrue(expected.strictEquals(actual));
   }

   @Test
   public void testSetInputFailure() {
      FileHandles fh = new FileHandles();
      PTerm t = atom("test");
      try {
         fh.setInput(t);
         fail("could set input for unopened file");
      } catch (ProjogException e) {
         // expected
      }
   }

   @Test
   public void testSetOutputFailure() {
      FileHandles fh = new FileHandles();
      PTerm t = atom("test");
      try {
         fh.setInput(t);
         fail("could set output for unopened file");
      } catch (ProjogException e) {
         // expected
      }
   }

   @Test
   public void testWriteAndRead() throws IOException {
      FileHandles fh = new FileHandles();
      String filename = createFileName("testWriteAndRead");
      String contentsToWrite = "test";
      write(fh, filename, contentsToWrite);
      String contentsRead = read(fh, filename);
      assertEquals(contentsToWrite, contentsRead);
   }

   @Test
   public void testIsHandle() throws IOException {
      FileHandles fh = new FileHandles();
      String filename = createFileName("testIsHandle");
      PTerm handle = openOutput(fh, filename);
      assertTrue(fh.isHandle(handle.getName()));
      fh.close(handle);
      assertFalse(fh.isHandle(handle.getName()));
   }

   private String createFileName(String name) {
      return "build/" + getClass().getName() + "_" + name + ".tmp";
   }

   private void write(FileHandles fh, String filename, String contents) throws IOException {
      PTerm handle = openOutput(fh, filename);
      fh.setOutput(handle);
      assertSame(handle, fh.getCurrentOutputHandle());
      PrintStream ps = fh.getCurrentOutputStream();
      ps.append(contents);
      fh.close(handle);
      assertFalse(ps.checkError());
      ps.append("extra stuff after close was called");
      assertTrue(ps.checkError());
   }

   private String read(FileHandles fh, String filename) throws IOException {
      PTerm handle = openInput(fh, filename);
      fh.setInput(handle);
      assertSame(handle, fh.getCurrentInputHandle());
      InputStream is = fh.getCurrentInputStream();
      String contents = "";
      int next;
      while ((next = is.read()) != -1) {
         contents += (char) next;
      }
      fh.close(handle);
      try {
         is.read();
         fail("could read from closed input stream");
      } catch (IOException e) {
         // expected now stream has been closed
      }
      return contents;
   }

   private PTerm openOutput(FileHandles fh, String filename) throws IOException {
      PTerm handle = fh.openOutput(filename);
      try {
         fh.openOutput(filename);
         fail("was able to reopen already opened file for output");
      } catch (ProjogException e) {
         // expected
      }
      return handle;
   }

   private PTerm openInput(FileHandles fh, String filename) throws IOException {
      PTerm handle = fh.openInput(filename);
      try {
         fh.openInput(filename);
         fail("was able to reopen already opened file for input");
      } catch (ProjogException e) {
         // expected
      }
      return handle;
   }
}