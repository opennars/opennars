package prolog;

import nars.tuprolog.event.OutputEvent;
import nars.tuprolog.event.OutputListener;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

// Based on the work of Sara Sabioni
public class ISOIOLibraryTestCase {

	static Prolog engine = null;
	String theory = null;
	SolveInfo info = null;
	static String writePath = null;
	static String readPath = null;
	static String binPath = null;

	@BeforeClass
	public static void initTest()
	{
		try
		{	
			engine = new Prolog(new String[] {
					"nars.prolog.lib.BasicLibrary",
					"nars.prolog.lib.IOLibrary",
			"nars.prolog.lib.ISOIOLibrary"});

			File file = new File("/tmp");
			writePath = file.getCanonicalPath() 
					+ File.separator + "test"
					+ File.separator + "unit"
					+ File.separator + "writeFile.txt";
			readPath = file.getCanonicalPath() 
					+ File.separator + "test"
					+ File.separator + "unit"
					+ File.separator + "readFile.txt";
			binPath = file.getCanonicalPath() 
					+ File.separator + "test"
					+ File.separator + "unit"
					+ File.separator + "binFile.bin";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test_open() throws MalformedGoalException, InvalidTheoryException 
	{
		// Apertura di un file esistente
		info = engine.solve("open('" + writePath +"','write',X,[alias('editor'), type(text)]).");
		assertTrue(info.isSuccess());

		// Apertura di un file NON esistente
		info = engine.solve("open('" + writePath.replace(".txt", ".myext") +"','write',X,[alias('editor'), type(text)]).");
		assertFalse(info.isSuccess());

		// Passando al posto di una variabile una lista:
		info = engine.solve("open('" + writePath + "','read',[]).");
		assertFalse(info.isSuccess());

		// Passando al posto di una lista una variabile:
		info = engine.solve("open('" + writePath + "','read',X,X).");
		assertFalse(info.isSuccess());

		// Passando una proprieta' in una lista illecita:
		info = engine.solve("open('" + writePath + "','read',X,[ciao(caramelle)]).");
		assertFalse(info.isSuccess());

		// CLOSE (in questo modo teso anche la close e la flush e poi anche le funzioni ausiliarie)
		String theoryText = "test:- open('" + writePath + "','write',X),close(X,force(true)).\n";
		engine.setTheory(new Theory(theoryText));
		info = engine.solve("test.");
		assertFalse(info.isSuccess());
	}

	@Test
	public void test_2() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		String dataToWrite = "B";
		String theory = "test2:-" +
				"open('" + writePath + "','write',X,[alias(ciao),type(text),eof_action(reset),reposition(true)])," +
				"write_term('ciao','" + dataToWrite + "',[quoted(true)])," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test2.");
		assertTrue(info.isSuccess());
		assertEquals("", dataToWrite, getStringDataWritten(writePath));
	}

	@Test
	public void test_3() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		String dataToWrite1 = "term.";
		String dataToWrite2 = "ciao.";
		String theory = "test3:- " +
				"open('" + readPath + "','write',X,[alias(ciao, computer, casa, auto),type(text),eof_action(reset),reposition(true)])," +
				"open('" + writePath + "','write',Y,[alias(telefono, rosa),type(text),eof_action(reset),reposition(true)])," +
				"write_term('telefono','" + dataToWrite1 + "',[quoted(true)])," +
				"write_term('auto','" + dataToWrite2 + "',[quoted(true)])," +
				"close(X)," +
				"close(Y).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test3.");
		assertTrue(info.isSuccess());
		assertEquals("", dataToWrite1, getStringDataWritten(writePath));
		assertEquals("", dataToWrite2, getStringDataWritten(readPath));
	}

	@Test
	public void test_4() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		String dataToWrite = "term.";
		String theory = "test4:-" +
				"open('" + writePath + "','write',Y,[alias(telefono, casa),type(text),eof_action(reset),reposition(true)])," +
				"write_term('telefono','" + dataToWrite + "',[quoted(true)])," +
				"flush_output('casa')," +
				"close(Y).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test4.");
		assertTrue(info.isSuccess());
		assertEquals("", dataToWrite, getStringDataWritten(writePath));
	}

	@Test
	public void test_5() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		final String dataToRead = "ciao";
		// Per beccare l'output
		//TODO Da rivedere
		OutputListener listener = new OutputListener() {

			@Override
			public void onOutput(OutputEvent e) 
			{
				assertEquals("", dataToRead, e.getMsg());
			}
		};

		engine.addOutputListener(listener);

		theory = "test5:-" +
				"open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," +
				"read_term(X,I,[])," +
				"write('user_output', I)," +
				"close('reading').";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test5.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}

	@Test
	public void test_6() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		final String[] dataToRead = { "c", "\n", "iao" };
		// Per beccare l'output
		//TODO Da rivedere
		OutputListener listener = new OutputListener() {

			int count = 0;

			@Override
			public void onOutput(OutputEvent e) 
			{
				assertEquals("", dataToRead[count], e.getMsg());
				count++;
			}
		};

		engine.addOutputListener(listener);

		theory = "test6:-" +
				"open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," +
				"get_char('reading',M)," +
				"read_term(X,J,[])," +
				"write(M)," +
				"nl('user_output')," +
				"write(J)," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test6.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}

	@Test
	public void test_7() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		final String dataToRead = "c";
		// Per beccare l'output
		//TODO Da rivedere
		OutputListener listener = new OutputListener() {

			@Override
			public void onOutput(OutputEvent e) 
			{
				assertEquals("", dataToRead, e.getMsg());
			}
		};

		engine.addOutputListener(listener);

		theory = "test7:- put_char('user_output',c).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test7.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}
	
	@Test
	public void test_8() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		final int dataToRead = 51;
		// Per beccare l'output
		//TODO Da rivedere
		OutputListener listener = new OutputListener() {

			@Override
			public void onOutput(OutputEvent e) 
			{
				assertEquals("", dataToRead+"", e.getMsg());
			}
		};

		engine.addOutputListener(listener);

		theory = "test8:-" +
				"open('" + binPath + "','read',X,[alias(readCode, nome),type(binary),eof_action(reset),reposition(true)])," +
				"peek_byte('nome', PB)," +
				"write(PB)," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test8.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}

	@Test
	public void test_9() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		int dataToWrite = 51;

		theory = "test9:-" +
				"open('" + binPath + "','write',X,[alias(readCode, nome),type(binary),eof_action(reset),reposition(true)])," +
				"put_byte('nome'," + dataToWrite + ")," +
				"flush_output('nome')," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test9.");
		assertTrue(info.isSuccess());
		assertEquals(dataToWrite, getByteDataWritten(binPath));
	}

	@Test
	public void test_10() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		final int[] dataToRead = { 99, 105, 105 }; // 'c', 'i', 'i'

		// Per beccare l'output
		//TODO Da rivedere
		OutputListener listener = new OutputListener() {

			int count = 0;

			@Override
			public void onOutput(OutputEvent e) 
			{
				assertEquals("", dataToRead[count]+"", e.getMsg());
				count++;
			}
		};

		engine.addOutputListener(listener);

		theory = "test10:-" +
				"open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," +
				"get_code('reading',M)," +
				"peek_code('nome',N)," +
				"peek_code(X,O)," +
				"write(M)," +
				"write(N)," +
				"write(O)," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test10.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}



	private String getStringDataWritten(String filePath) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String dataRead = reader.readLine();
		reader.close();
		return dataRead;
	}

	private int getByteDataWritten(String filePath) throws IOException
	{
		FileInputStream fins = new FileInputStream(filePath);
		int dataRead = fins.read();
		fins.close();
		return dataRead;
	}
}
