package nars.task.in;

import nars.NAR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

/**
 * Creates a PrintWriter that pipes into a TextInput's BufferedReader. Useful
 * for TextInput implementations that dynamically generate new input from Java
 * execution.
 */
public class PrintWriterInput extends ReaderInput {

	/**
	 * Printing to out will be piped into TextInput
	 */
	public final PipedWriter out;
	boolean outClosed = false;

	public PrintWriterInput(NAR n) throws IOException {
		super(n);

		out = new PipedWriter();
		setInput(new BufferedReader(new PipedReader(out)));

	}

	public void append(CharSequence c) {
		try {
			out.append(c);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			out.close();
			outClosed = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
